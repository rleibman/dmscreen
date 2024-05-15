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

import com.typesafe.config.{Config as TypesafeConfig, ConfigFactory}
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import zio.*
import zio.config.magnolia.DeriveConfig
import zio.config.typesafe.TypesafeConfigProvider

import java.io.File

case class ConfigurationError(
  override val msg:   String = "",
  override val cause: Option[Throwable] = None
) extends DMScreenError(msg, cause)

case class DataSourceConfig(
  driver:                String,
  url:                   String,
  user:                  String,
  password:              String,
  maximumPoolSize:       Int = 20,
  minimumIdle:           Int = 1000,
  connectionTimeoutMins: Long = 5
)

case class DatabaseConfig(
  dataSource: DataSourceConfig
) {}

case class DMScreenConfiguration(
  db:               DatabaseConfig,
  host:             String,
  port:             Int,
  staticContentDir: String
)

object AppConfig {

  def read(typesafeConfig: TypesafeConfig): UIO[AppConfig] = {
    TypesafeConfigProvider
      .fromTypesafeConfig(typesafeConfig)
      .load(DeriveConfig.derived[AppConfig].desc)
      .orDie
  }

}

case class AppConfig(
  dmscreen: DMScreenConfiguration
) {

  def dataSource: HikariDataSource = {
    val dsConfig = HikariConfig()
    dsConfig.setDriverClassName(dmscreen.db.dataSource.driver)
    dsConfig.setJdbcUrl(dmscreen.db.dataSource.url)
    dsConfig.setUsername(dmscreen.db.dataSource.user)
    dsConfig.setPassword(dmscreen.db.dataSource.password)
    dsConfig.setMaximumPoolSize(dmscreen.db.dataSource.maximumPoolSize)
    dsConfig.setMinimumIdle(dmscreen.db.dataSource.minimumIdle)
    dsConfig.setConnectionTimeout(dmscreen.db.dataSource.connectionTimeoutMins * 60 * 1000)

    HikariDataSource(dsConfig)
  }

}

trait ConfigurationService {

  def appConfig: IO[ConfigurationError, AppConfig]

}

// utility to read config
object ConfigurationService {

  def withConfig(typesafeConfig: TypesafeConfig): ConfigurationService =
    new ConfigurationService {

      lazy override val appConfig: IO[ConfigurationError, AppConfig] = {
        AppConfig.read(typesafeConfig)
      }

    }

  def withConfig(withMe: AppConfig): ConfigurationService =
    new ConfigurationService {

      lazy override val appConfig: IO[ConfigurationError, AppConfig] = ZIO.succeed(withMe)

    }

  val live: ULayer[ConfigurationService] = ZLayer.succeed(new ConfigurationService {

    lazy override val appConfig: IO[ConfigurationError, AppConfig] = {
      import scala.language.unsafeNulls
      val confFileName = java.lang.System.getProperty("application.conf", "./src/main/resources/application.conf")

      val confFile = new File(confFileName)
      AppConfig.read(
        ConfigFactory
          .parseFile(confFile)
          .withFallback(ConfigFactory.load())
          .resolve()
      )
    }

  })

  def typedConfig: ZIO[ConfigurationService, ConfigurationError, AppConfig] = ZIO.environmentWithZIO(_.get.appConfig)

}
