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

package dmscreen.db

import auth.*
import dmscreen.*
import io.getquill.*
import io.getquill.jdbczio.*
import zio.*
import zio.cache.*

import java.time.LocalDateTime
import javax.sql.DataSource

trait QuillAuthService extends UserRepository[DMScreenTask] with TokenHolder[DMScreenTask]

object QuillAuthService {

  object UserSchema {

    inline def qUsers =
      quote {
        querySchema[User](
          "dmscreenUser",
          _.id           -> "id",
          _.email        -> "email",
          _.name         -> "name",
          _.deleted      -> "deleted",
          _.lastLoggedIn -> "lastUpdated"
        )
      }

    inline def qTokens =
      quote {
        querySchema[Token]("token")
      }

    given MappedEncoding[TokenString, String] = MappedEncoding[TokenString, String](_.str)
    given MappedEncoding[String, TokenString] = MappedEncoding[String, TokenString](TokenString.apply)

    given MappedEncoding[TokenPurpose, String] = MappedEncoding[TokenPurpose, String](_.toString)

    given MappedEncoding[String, TokenPurpose] =
      MappedEncoding[String, TokenPurpose](s =>
        TokenPurpose.values.find(_.toString.equalsIgnoreCase(s)).getOrElse(TokenPurpose.NewUser)
      )

  }

  val live: ZLayer[ConfigurationService, ConfigurationError, QuillAuthService] = {
    ZLayer.fromZIO {
      object ctx extends MysqlZioJdbcContext(MysqlEscape)

      import UserSchema.{*, given}
      import ctx.*

      def cleanupTokens: ZIO[DataSource, RepositoryError, Boolean] =
        (for {
          now <- Clock.localDateTime
          b <- ctx.run(
            infix"DELETE FROM token WHERE expireTime >= ${lift(now)}".as[Delete[Token]]
          )
        } yield b > 0)
          .mapError(RepositoryError.apply)
          .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

      for {
        config <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
        dataSourceLayer = Quill.DataSource.fromDataSource(config.dataSource)
        cache <- zio.cache.Cache.make[UserId, Any, DMScreenError, Option[User]](
          capacity = 10,
          timeToLive = 1.hour,
          lookup = Lookup { userId =>
            ctx
              .run(
                qUsers.filter(v => !v.deleted && v.id == lift(userId))
              )
              .map(_.headOption)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError(_))
              .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
          }
        )
        freq = new zio.DurationSyntax(1).hour
        _ <- (ZIO.logInfo("Cleaning up old tokens") *> cleanupTokens.provide(dataSourceLayer))
          .repeat(Schedule.spaced(freq).jittered).forkDaemon
      } yield {
        new QuillAuthService {
          override def firstLogin: DMScreenTask[Option[LocalDateTime]] = ???

          override def login(
            email:    String,
            password: String
          ): DMScreenTask[Option[User]] = {
            inline def sql =
              quote(infix"""select u.id
               from `dmscreenUser` u
               where u.deleted = 0 and
               u.active = 1 and
               u.email = ${lift(email)} and
               u.hashedpassword = SHA2(${lift(password)}, 512)""".as[Query[Long]])

            (for {
              _ <- validateAdmin // Only admin can "log-in" the user (i.e. if the user is already logged in, they shouldn't call this method)
              userId <- ctx.run(sql).map(_.headOption.map(UserId.apply))
              user   <- ZIO.foreach(userId)(cache.get)
            } yield user.flatten)
              .provideSomeLayer[DMScreenSession](dataSourceLayer)
              .mapError(RepositoryError(_))
              .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
          }

          override def userByEmail(email: String): DMScreenTask[Option[User]] = {
            validateAdmin *> ctx
              .run(qUsers.filter(v => !v.deleted && v.email == lift(email)))
              .map(_.headOption)
              .provideLayer(dataSourceLayer)
              .mapError(RepositoryError(_))
              .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
          }

          override def changePassword(
            user:     User,
            password: String
          ): DMScreenTask[Boolean] = {
            for {
              _ <- selfOrAdmin(user.id)
              res <- ctx
                .run(
                  quote(
                    infix"update `dmscreenUser` set hashedPassword=SHA2(${lift(password)}, 512) where id = ${lift(user.id)}"
                      .as[Update[Int]]
                  )
                ).map(_ > 0)
            } yield res
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          private def validateAdmin: ZIO[DMScreenSession, DMScreenError, Unit] =
            for {
              userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
              _      <- ZIO.fail(RepositoryError("Unauthorized")).unless(userId == UserId.admin)
            } yield ()

          private def selfOrAdmin(checkId: UserId): DMScreenTask[Unit] = {
            for {
              userId <- ZIO.serviceWith[DMScreenSession](_.user.id)
              _      <- ZIO.fail(RepositoryError("Unauthorized")).unless(userId == checkId || userId == UserId.admin)
            } yield ()
          }

          override def upsert(e: User): DMScreenTask[User] = {
            for {
              _ <- selfOrAdmin(e.id).when(e.id != UserId.empty)
              _ <- cache.invalidate(e.id).when(e.id != UserId.empty)
              e <-
                if (e.id != UserId.empty) {
                  ctx
                    .run(
                      qUsers
                        .filter(_.id == lift(e.id))
                        .updateValue(lift(e))
                    ).as(e)
                } else {
                  ctx
                    .run(
                      qUsers
                        .insertValue(lift(e))
                        .returningGenerated(_.id)
                    )
                    .map(id => e.copy(id = id))
                }
            } yield e
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def get(pk: UserId): DMScreenTask[Option[User]] = selfOrAdmin(pk) *> cache.get(pk)

          override def delete(
            pk:         UserId,
            softDelete: Boolean
          ): DMScreenTask[Boolean] = {
            for {
              _ <- selfOrAdmin(pk)
              _ <- cache.invalidate(pk)
              res <-
                if (softDelete) {
                  ctx
                    .run(
                      qUsers
                        .filter(_.id == lift(pk))
                        .update(_.deleted -> true)
                    ).map(_ > 0)
                } else {
                  ctx
                    .run(
                      qUsers
                        .filter(_.id == lift(pk))
                        .delete
                    ).map(_ > 0)
                }
            } yield res
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def search(search: Option[UserSearch]): DMScreenTask[Seq[User]] = {
            validateAdmin *> search
              .fold(ctx.run(qUsers.filter(!_.deleted))) { s =>
                ctx.run(
                  qUsers
                    .filter(u => (u.email like lift(s"%${s.text}%")) && !u.deleted)
                    .drop(lift(s.pageSize * s.pageIndex))
                    .take(lift(s.pageSize))
                )
              }
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def count(search: Option[UserSearch]): DMScreenTask[Long] = {
            validateAdmin *> search.fold(ctx.run(qUsers.filter(!_.deleted).size)) { s =>
              ctx.run(qUsers.filter(u => (u.email like lift(s"%${s.text}%")) && !u.deleted).size)
            }
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def validateToken(
            tok:     TokenString,
            purpose: TokenPurpose
          ): DMScreenTask[Option[User]] =
            ctx.transaction(for {
              user <- peek(tok, purpose)
              _ <- ctx.run(
                infix"DELETE FROM token WHERE tok = ${lift(tok)} && tokenPurpose = ${lift(purpose)}"
                  .as[Delete[Token]]
              )
            } yield user)
              .provideSomeLayer[DMScreenSession](dataSourceLayer)
              .mapError(RepositoryError.apply)
              .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def createToken(
            user:    User,
            purpose: TokenPurpose,
            ttl:     Option[Duration]
          ): DMScreenTask[Token] = {
            for {
              tok <- TokenString.random
              expireTime <- Clock.localDateTime.map(
                _.plus(java.time.Duration.ofMillis(ttl.fold(Long.MaxValue)(_.toMillis)))
              )
              token = Token(
                tok = tok,
                tokenPurpose = purpose,
                expireTime = expireTime,
                userId = user.id
              )
              _ <- ctx.run(qTokens.insertValue(lift(token)))
            } yield token
          }.provideSomeLayer[DMScreenSession](dataSourceLayer)
            .mapError(RepositoryError.apply)
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          override def peek(
            tok:     TokenString,
            purpose: TokenPurpose
          ): DMScreenTask[Option[User]] =
            ctx
              .run(
                qTokens
                  .filter(t => t.tok == lift(tok) && t.tokenPurpose == lift(purpose))
                  .join(qUsers).on(_.userId == _.id).map(_._2)
              ).map(_.headOption)
              .provideSomeLayer[DMScreenSession](dataSourceLayer)
              .mapError(RepositoryError.apply)
              .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

        }
      }
    }
  }

}
