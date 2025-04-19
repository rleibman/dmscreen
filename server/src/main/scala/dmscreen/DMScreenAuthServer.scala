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

package dmscreen

import auth.*
import courier.{Envelope, Multipart}
import dmscreen.db.UserRepository
import dmscreen.mail.Postman
import zio.*

import javax.mail.internet.InternetAddress

object DMScreenAuthServer {

  val live: ZLayer[
    ConfigurationService & Postman & UserRepository[DMScreenTask],
    ConfigurationError,
    AuthServer[User, UserId]
  ] = ZLayer.fromZIO {
    for {
      config  <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      postman <- ZIO.service[Postman]
      repo    <- ZIO.service[UserRepository[DMScreenTask]]
    } yield new AuthServer[User, UserId] {
      override def getPK(user: User): UserId = user.id

      override def login(
        userName: String,
        password: String
      ): IO[AuthError, Option[User]] =
        repo.login(userName, password).provide(DMScreenSession.adminSession.toLayer).mapError(AuthError(_))

      override def logout(): ZIO[Session[User], AuthError, Unit] =
        ZIO.serviceWithZIO[Session[User]](u => ZIO.logDebug(s"User ${u.user} logged out"))

      override def changePassword(
        userPK:      UserId,
        newPassword: String
      ): ZIO[Session[User], AuthError, Unit] = repo.changePassword(userPK, newPassword).mapError(AuthError(_)).unit

      override def userByEmail(email: String): IO[AuthError, Option[User]] =
        repo.userByEmail(email).provide(DMScreenSession.adminSession.toLayer).mapError(AuthError(_))

      override def userByPK(pk: UserId): IO[AuthError, Option[User]] =
        repo.get(pk).provide(DMScreenSession.adminSession.toLayer).mapError(AuthError(_))

      override def createUser(
        name:     String,
        email:    String,
        password: String
      ): IO[AuthError, User] =
        (for {
          exists <- repo.userByEmail(email)
          _      <- ZIO.fail(EmailAlreadyExists(email)).when(exists.isDefined)
          now    <- Clock.localDateTime
          user   <- repo.upsert(User(id = UserId.empty, email = email, name = name, created = now))
          _      <- changePassword(user.id, password)
        } yield user).provide(DMScreenSession.adminSession.toLayer).mapError(AuthError(_))

      override def sendEmail(
        subject: String,
        body:    String,
        user:    User
      ): IO[AuthError, Unit] = {
        val email = Envelope
          .from(new InternetAddress("dmscreen@leibman.net", "dmscreen administrator"))
          .to(new InternetAddress(user.email))
          .bcc(new InternetAddress("roberto@leibman.net", "Roberto Leibman"))
          .subject(subject)
          .content(Multipart().html(body))
        postman.deliver(email)
      }

      override def activateUser(userPK: UserId): IO[AuthError, Unit] =
        (for {
          user <- repo.get(userPK)
          _    <- ZIO.fail(AuthBadRequest(s"user ${userPK.value} not found")).when(user.isEmpty)
          _    <- user.fold(ZIO.unit)(u => repo.upsert(u.copy(active = true)))
        } yield ()).provide(DMScreenSession.adminSession.toLayer).mapError(AuthError(_))

      override def getEmailBodyHtml(
        user:    User,
        purpose: UserCodePurpose,
        url:     String
      ): String =
        purpose match {
          case UserCodePurpose.LostPassword =>
            s"""<html><body>
               | <p>So sorry you lost your password</p>
               | <p>Here's a link to generate a new one.</p>
               | <p>please Click here: <a href="http://${config.dmscreen.smtp.webHostname}/#$url">http://localhost:8078/#$url</a>.</p>
               | <p>Note that this link has a limited time</p>
               |</body></html>""".stripMargin
          case UserCodePurpose.NewUser =>
            s"""<html><body>
               | <p>Thanks for registering!</p>
               | <p>All you have to do now is activate your account.</p>
               | <p>Please click here: <a href="http://${config.dmscreen.smtp.webHostname}/#$url">http://localhost:8078/#$url</a>.</p>
               | <p>Note that this link is time sensitive, eventually it will expire</p>
               |</body></html>""".stripMargin
        }
    }
  }

}
