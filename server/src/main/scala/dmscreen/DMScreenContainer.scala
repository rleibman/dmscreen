package dmscreen

import com.dimafeng.testcontainers.*
import com.mysql.cj.jdbc.MysqlDataSource
import com.typesafe.config
import com.typesafe.config.ConfigFactory
import dmscreen.dnd5e.RepositoryError
import io.getquill.extras.*
import io.getquill.jdbczio.Quill
import io.getquill.query as qquery
import zio.{IO, ZIO, ZLayer}

import java.sql.SQLException
import javax.sql.DataSource
import scala.io.Source

trait DMScreenContainer {

  def container: MySQLContainer

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
                ZIO.attempt(stmt.executeUpdate(statement).nn)
              }.catchSome { case e: SQLException =>
                ZIO.fail(RepositoryError(e))
              }
          }
        }.unit.mapError(RepositoryError.apply)
      } yield res
    }

  }

  val containerLayer: ZLayer[Any, RepositoryError, DMScreenContainer] = ZLayer.fromZIO((for {
    _ <- ZIO.logDebug("Creating container")
    newContainer <- ZIO.succeed {
      val c = MySQLContainer()
      c.container.start()
      c
    }
    _ <- ZIO.logDebug("Migrating container")
    _ <-
      Migrator("server/src/main/sql")
        .migrate()
        .provideLayer(Quill.DataSource.fromDataSource {
          val dataSource = new MysqlDataSource()
          dataSource.setURL(newContainer.container.getJdbcUrl)
          dataSource.setUser(newContainer.container.getUsername)
          dataSource.setPassword(newContainer.container.getPassword)
          dataSource
        })
        .mapError(RepositoryError.apply)
  } yield new DMScreenContainer {

    override def container: MySQLContainer = newContainer

  }).mapError(RepositoryError.apply))

  private def getConfig(container: MySQLContainer): config.Config =
    ConfigFactory
      .parseString(s"""
      DMScreen.db.dataSourceClassName=com.mysql.cj.jdbc.MysqlDataSource
      DMScreen.db.dataSource.url="${container.container.getJdbcUrl}?logger=com.mysql.cj.log.Slf4JLogger&profileSQL=true&serverTimezone=UTC&useLegacyDatetimeCode=false"
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
          baseConfig.dmscreen.copy(dataSource =
            baseConfig.dmscreen.dataSource.copy(
              url = s"${container.container.getJdbcUrl}?logger=com.mysql.cj.log.Slf4JLogger&profileSQL=true&serverTimezone=UTC&useLegacyDatetimeCode=false",
              user = container.container.getUsername.nn,
              password = container.container.getPassword.nn
            )
          )
        )
        new ConfigurationService {
          override def appConfig: IO[ConfigurationError, AppConfig] = ZIO.succeed(newConfig)
        }
      }
    }

}
