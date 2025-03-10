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

import com.dimafeng.testcontainers.*
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import dmscreen.db.RepositoryError
import io.getquill.jdbczio.Quill
import org.mariadb.jdbc.MariaDbDataSource
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy
import org.testcontainers.containers.wait.strategy.Wait
import zio.{IO, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource
import scala.io.Source
import scala.jdk.CollectionConverters.*

trait DMScreenContainer {

  def container: MariaDBContainer

}

object DMScreenContainer {

  private case class Migrator(
    path: String
  ) {

    def migrate(): ZIO[DataSource, RepositoryError, Unit] = {
      for {
        files <- ZIO.succeed {
          import scala.language.unsafeNulls
          new java.io.File(path).listFiles.sortBy(_.getName)
        }
        dataSource <- ZIO.service[DataSource]
        statements <- ZIO
          .foreach(files) {
            case file: java.io.File if file.getName.nn.endsWith("sql") =>
              ZIO.acquireReleaseWith(ZIO.attempt(Source.fromFile(file)))(f => ZIO.attempt(f.close()).orDie) { source =>
                val str = source
                  .getLines()
                  .map(str => str.replaceAll("--.*", "").nn.trim.nn)
                  .mkString("\n")
                ZIO.attempt(str.split(";\n").nn)
              }
            case _ => ZIO.fail(RepositoryError("File must be of either sql or JSON type."))
          }.mapBoth(
            RepositoryError.apply,
            _.flatten
              .map(_.nn.trim.nn)
              .filter(_.nonEmpty)
          ).catchSome(e => ZIO.fail(RepositoryError(e)))
        res <- {
          ZIO.acquireReleaseWith {
            ZIO.attempt {
              val conn = dataSource.getConnection.nn
              val stmt = conn.createStatement().nn
              (conn, stmt)
            }
          } { case (conn, stmt) =>
            ZIO.succeed {
              stmt.close().nn
              conn.close().nn
            }
          } { case (_, stmt) =>
            ZIO
              .foreach(statements) { statement =>
                ZIO
                  .attempt(stmt.executeUpdate(statement).nn)
                  .catchSome { case e: SQLException =>
                    ZIO.fail(RepositoryError(s"Error processing $statement", Some(e)))
                  }
              }
          }
        }.unit.mapError(RepositoryError.apply)
      } yield res
    }

  }

  val containerLayer: ZLayer[Any, RepositoryError, DMScreenContainer] = ZLayer.fromZIO((for {
    _ <- ZIO.logDebug("Creating container")
    newContainer <- ZIO.attemptBlocking {
      val c = MariaDBContainer()

      c.container.setPortBindings(List("3307:3306").asJava)
      c.container.start()
      c
    }
    _ <- ZIO.logDebug("Migrating container")
    _ <-
      Migrator("server/src/main/sql")
        .migrate()
        .provideLayer(Quill.DataSource.fromDataSource {
          val dataSource = new MariaDbDataSource()
          dataSource.setUrl(newContainer.container.getJdbcUrl)
          dataSource.setUser(newContainer.container.getUsername)
          dataSource.setPassword(newContainer.container.getPassword)
          dataSource
        })
        .mapError(RepositoryError.apply)
  } yield new DMScreenContainer {

    override def container: MariaDBContainer = newContainer

  }).mapError(RepositoryError.apply))

  // Add profileSQL=true& to the url if you need more information
  private def getConfig(container: MariaDBContainer): config.Config =
    ConfigFactory
      .parseString(s"""
      DMScreen.db.dataSourceClassName=org.mariadb.jdbc.MariaDbDataSource
      DMScreen.db.dataSource.url="${container.container.getJdbcUrl}"
      DMScreen.db.dataSource.user="${container.container.getUsername}"
      DMScreen.db.dataSource.password="${container.container.getPassword}"
      DMScreen.db.dataSource.cachePrepStmts=true
      DMScreen.db.dataSource.prepStmtCacheSize=250
      DMScreen.db.dataSource.prepStmtCacheSqlLimit=2048
      DMScreen.db.maximumPoolSize=10
    """).nn

  val configLayer: ZLayer[DMScreenContainer & ConfigurationService, ConfigurationError, ConfigurationService] =
    ZLayer.fromZIO {
      for {
        container  <- ZIO.serviceWith[DMScreenContainer](_.container)
        baseConfig <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig)
      } yield {
        val newConfig: AppConfig = baseConfig.copy(dmscreen =
          baseConfig.dmscreen.copy(db =
            baseConfig.dmscreen.db.copy(
              dataSource = baseConfig.dmscreen.db.dataSource.copy(
                // Add profileSQL=true& to the url if you need more information
                url = container.container.getJdbcUrl.nn,
                user = container.container.getUsername.nn,
                password = container.container.getPassword.nn
              )
            )
          )
        )
        new ConfigurationService {
          override def appConfig: IO[ConfigurationError, AppConfig] = ZIO.succeed(newConfig)
        }
      }
    }

}
