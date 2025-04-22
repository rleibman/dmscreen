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

trait UserRepository[F[_]] {

  def login(
    email:    String,
    password: String
  ): F[Option[User]]

  def changePassword(
    userId:   UserId,
    password: String
  ): F[Boolean]

  def userByEmail(email: String): F[Option[User]]
  def upsert(e:          User):   F[User]

  def get(pk: UserId): F[Option[User]]

  def delete(
    pk:         UserId,
    softDelete: Boolean
  ): F[Boolean]

}

trait QuillAuthService extends UserRepository[DMScreenTask]

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

  }

  val live: ZLayer[ConfigurationService, ConfigurationError, QuillAuthService] = {
    ZLayer.fromZIO {
      object ctx extends MysqlZioJdbcContext(MysqlEscape)

      import UserSchema.{*, given}
      import ctx.*

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
      } yield {
        new QuillAuthService {
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
              .provideSomeLayer[auth.Session[User]](dataSourceLayer)
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
            userId:   UserId,
            password: String
          ): DMScreenTask[Boolean] = {
            for {
              _ <- selfOrAdmin(userId)
              res <- ctx
                .run(
                  quote(
                    infix"update `dmscreenUser` set hashedPassword=SHA2(${lift(password)}, 512) where id = ${lift(userId)}"
                      .as[Update[Int]]
                  )
                ).map(_ > 0)
            } yield res
          }.provideSomeLayer[auth.Session[User]](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))

          private def validateAdmin: ZIO[auth.Session[User], DMScreenError, Unit] =
            for {
              userId <- ZIO.serviceWith[auth.Session[User]](_.user.fold(UserId.empty)(_.id))
              _      <- ZIO.fail(RepositoryError("Unauthorized")).unless(userId == UserId.admin)
            } yield ()

          private def selfOrAdmin(checkId: UserId): DMScreenTask[Unit] = {
            for {
              userId <- ZIO.serviceWith[auth.Session[User]](_.user.fold(UserId.empty)(_.id))
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
          }.provideSomeLayer[auth.Session[User]](dataSourceLayer)
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
          }.provideSomeLayer[auth.Session[User]](dataSourceLayer)
            .mapError(RepositoryError(_))
            .tapError(e => ZIO.logErrorCause(Cause.fail(e)))
        }
      }
    }

  }

}
