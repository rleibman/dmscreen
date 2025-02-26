/*
 * Copyright (c) 2024 Roberto Leibman
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dmscreen.routes

import auth.{SessionTransport, UserRepository, *, given}
import dmscreen.db.RepositoryError
import dmscreen.mail.{EmailGenerator, Postman}
import dmscreen.{BuildInfo, *, given}
import dmscreen.util.*
import pdi.jwt.exceptions.JwtExpirationException
import zio.http.*
import zio.json.*
import zio.json.ast.Json
import zio.logging.*
import zio.prelude.NonEmptyList
import zio.{Clock, IO, ZIO}

import java.time.Instant
import java.util.Locale
import scala.language.unsafeNulls

object AuthRoutes extends AppRoutes[DMScreenServerEnvironment, DMScreenSession, DMScreenError] {

  override def auth: ZIO[
    DMScreenServerEnvironment,
    DMScreenError,
    Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
  ] =
    ZIO.succeed(
      Routes(
        Method.GET / "isFirstLoginToday" -> handler { (_: Request) =>
          for {
            userOps       <- ZIO.service[UserRepository[DMScreenTask]]
            now           <- Clock.localDateTime
            firstLoginOpt <- userOps.firstLogin
          } yield json(firstLoginOpt.fold(true)(_.isAfter(now.minus(java.time.Duration.ofDays(1)))))
        },
        Method.GET / "whoami" -> handler { (_: Request) =>
          ZIO.serviceWith[DMScreenSession](session => json(session.user))
        },
        Method.POST / "changePassword" -> handler { (req: Request) =>
          (for {
            userOps <- ZIO.service[UserRepository[DMScreenTask]]
            session <- ZIO.service[DMScreenSession]
            newPass <- req.body.asString
            ret     <- userOps.changePassword(session.user, newPass)
          } yield json(ret)).mapError(DMScreenError(_))
        },
        Method.GET / "doLogout" -> handler { (_: Request) =>
          (for {
            session          <- ZIO.service[DMScreenSession]
            sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
            loginFormURL     <- ZIO.fromEither(URL.decode("/loginForm")).mapError(DMScreenError(_))
            response <- sessionTransport
              .invalidateSession(session, Response(Status.SeeOther, Headers(Header.Location(loginFormURL))))
          } yield response).mapError(DMScreenError(_))
        }
      ).nest("auth")
    )

  override def unauth: ZIO[DMScreenServerEnvironment, DMScreenError, Routes[DMScreenServerEnvironment, DMScreenError]] =
    ZIO.succeed(
      Routes(
        Method.GET / "serverVersion" -> handler {
          Response.json(BuildInfo.version)
        },
        Method.POST / "passwordReset" -> handler { (req: Request) =>
          (for {
            formData <- req.formData
            token = formData.get("token").map(TokenString.apply)
            password = formData.get("password")
            _ <- ZIO.fail(SessionError("You need to pass a token and password")).when(token.isEmpty || password.isEmpty)
            userOps     <- ZIO.service[UserRepository[DMScreenTask]]
            tokenHolder <- ZIO.service[TokenHolder[DMScreenTask]]
            userOpt <- ZIO
              .foreach(token)(tok => tokenHolder.validateToken(token.get, TokenPurpose.LostPassword)).map(_.flatten)
            passwordChanged            <- ZIO.foreach(userOpt)(user => userOps.changePassword(user, password.get))
            passwordChangeSucceededUrl <- seeOther("/loginForm?passwordChangeSucceeded")
            passwordChangeFailedUrl    <- seeOther("/loginForm?passwordChangeFailed")
          } yield passwordChanged.fold(passwordChangeFailedUrl)(isChanged =>
            if (isChanged) passwordChangeSucceededUrl else passwordChangeFailedUrl
          ))
            .mapError(DMScreenError(_))
            .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
        },
        Method.POST / "passwordRecoveryRequest" -> handler { (req: Request) =>
          (for {
            emailJson <- req.body.asString
            email     <- ZIO.fromEither(emailJson.fromJson[String]).mapError(DMScreenError(_))
            userOps   <- ZIO.service[UserRepository[DMScreenTask]]
            postman   <- ZIO.service[Postman]
            userOpt   <- userOps.userByEmail(email)
            _ <-
              ZIO
                .foreach(userOpt)(EmailGenerator.lostPasswordEmail(_, "")).flatMap(envelope =>
                  ZIO.foreach(envelope)(postman.deliver)
                ).forkDaemon
          } yield Response.ok)
            .mapError(DMScreenError(_))
            .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
        },
        Method.PUT / "userCreation" -> handler { (req: Request) =>
          (for {
            postman             <- ZIO.service[Postman]
            userOps             <- ZIO.service[UserRepository[DMScreenTask]]
            userCreationRequest <- req.bodyAs[UserCreationRequest]
            validate <- ZIO.succeed( // TODO Use cats validate instead of option
              if (userCreationRequest.user.email.trim.nn.isEmpty)
                Option("User Email cannot be empty")
              else if (userCreationRequest.user.name.trim.nn.isEmpty)
                Option("User Name cannot be empty")
              else if (userCreationRequest.password.trim.nn.isEmpty || userCreationRequest.password.trim.nn.length < 3)
                Option("Password is invalid")
              else if (userCreationRequest.user.id.nonEmpty)
                Option("You can't register an existing user")
              else
                None
            )
            exists <- userOps.userByEmail(userCreationRequest.user.email).map(_.nonEmpty)
            saved <-
              if (validate.nonEmpty || exists)
                ZIO.none
              else
                userOps.upsert(userCreationRequest.user.copy(active = false)).map(Option(_))
            _ <- ZIO.foreachDiscard(saved)(userOps.changePassword(_, userCreationRequest.password))
            _ <- ZIO.logInfo("About to send")
            _ <- ZIO
              .foreach(saved)(EmailGenerator.registrationEmail(_, ""))
              .flatMap(envelope => ZIO.foreach(envelope)(postman.deliver))
              .forkDaemon
            _ <- ZIO.logInfo("Maybe sent")
          } yield {
            if (exists)
              json(UserCreationResponse(Option("A user with that email already exists")))
            else
              json(UserCreationResponse(validate))
          })
            .mapError(DMScreenError(_))
            .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
        },
        Method.GET / "confirmRegistration" -> handler { (req: Request) =>
          (for {
            formData <- req.formData
            token <- ZIO
              .fromOption(formData.get("token")).mapBoth(_ => SessionError("Token not found"), TokenString.apply)
            userOps                  <- ZIO.service[UserRepository[DMScreenTask]]
            tokenHolder              <- ZIO.service[TokenHolder[DMScreenTask]]
            user                     <- tokenHolder.validateToken(token, TokenPurpose.NewUser)
            activate                 <- ZIO.foreach(user)(user => userOps.upsert(user.copy(active = true)))
            registrationFailedUrl    <- seeOther("/loginForm?registrationFailed")
            registrationSucceededUrl <- seeOther("/loginForm?registrationSucceeded")
          } yield activate.fold(registrationFailedUrl)(_ => registrationSucceededUrl))
            .mapError(DMScreenError(_))
            .provideSomeLayer[DMScreenServerEnvironment](DMScreenSession.adminSession.toLayer)
        },
        Method.POST / "doLogin" -> handler { (req: Request) =>
          (for {
            formData <- req.formData
            email <-
              ZIO.fromOption(formData.get("email")).orElseFail(SessionError("Missing email"))
            password <- ZIO.fromOption(formData.get("password")).orElseFail(SessionError("Missing Password"))

            userOps <- ZIO.service[UserRepository[DMScreenTask]]
            login   <- userOps.login(email, password).provideLayer(DMScreenSession.adminSession.toLayer)
            _       <- login.fold(ZIO.logDebug(s"Bad login for $email"))(_ => ZIO.logDebug(s"Good Login for $email"))
            sessionTransport <- ZIO.service[SessionTransport[DMScreenSession]]
            loginFormBadUrl  <- ZIO.fromEither(URL.decode("/loginForm?bad=true")).mapError(DMScreenError(_))
            res <- login.fold(ZIO.succeed(Response(Status.SeeOther, Headers(Header.Location(loginFormBadUrl))))) { user =>
              sessionTransport.addTokens(
                DMScreenSession(user),
                Response.ok
              )
            }
          } yield res).mapError(DMScreenError(_))

        },
        Method.GET / "refresh" -> handler { (req: Request) =>
          // This may need to be a special case, we don't want to treat refresh like a resource or an auth required
          ZIO.serviceWithZIO[SessionTransport[DMScreenSession]](_.refreshSession(req, Response.ok))
        }
      )
    )

  override def api: ZIO[
    DMScreenServerEnvironment,
    DMScreenError,
    Routes[DMScreenServerEnvironment & DMScreenSession, DMScreenError]
  ] =
    ZIO.succeed(
      Routes(
        Method.POST / Root -> handler { (req: Request) =>
          (for {
            obj <- req.bodyAs[User]
            _   <- ZIO.logInfo(s"Upserting auth with $obj")
            res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.upsert(obj))
          } yield Response.json(res.toJson)).mapError(DMScreenError(_))

        },
        Method.PUT / Root -> handler { (req: Request) =>
          (for {
            obj <- req.bodyAs[User]
            _   <- ZIO.logInfo(s"Upserting auth with $obj")
            res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.upsert(obj))
          } yield Response.json(res.toJson)).mapError(DMScreenError(_))

        },
        Method.POST / "search" -> handler { (req: Request) =>
          (for {
            search <- req.bodyAs[UserSearch]
            res    <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.search(Some(search)))
          } yield Response.json(res.toJson)).mapError(DMScreenError(_))
        },
        Method.POST / "count" -> handler { (req: Request) =>
          (for {
            search <- req.bodyAs[UserSearch]
            res    <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.count(Some(search)))
          } yield Response.json(res.toJson)).mapError(DMScreenError(_))
        },
        Method.GET / int("pk") -> handler {
          (
            pk:  Int,
            req: Request
          ) =>
            for {
              res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.get(UserId(pk)))
            } yield Response.json(res.toJson)
        },
        Method.DELETE / int("pk") -> handler {
          (
            pk:  Int,
            req: Request
          ) =>
            for {
              res <- ZIO.serviceWithZIO[UserRepository[DMScreenTask]](_.delete(UserId(pk)))
              _   <- ZIO.logInfo(s"Deleted ${pk.toString}")
            } yield Response.json(res.toJson)
        }
      ).nest("auth")
    )

}
