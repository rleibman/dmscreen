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
  driver:                String = "",
  url:                   String = "",
  user:                  String = "",
  password:              String = "",
  maximumPoolSize:       Int = 10,
  minimumIdle:           Int = 1000,
  connectionTimeoutMins: Long = 5
)

case class DMScreenConfiguration(
  host:             String = "0.0.0.0",
  port:             Int = 8188,
  dataSource:       DataSourceConfig = DataSourceConfig(),
  staticContentDir: String = "/Volumes/Personal/projects/dmscreen/debugDist"
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
  dmscreen: DMScreenConfiguration = DMScreenConfiguration()
) {

  def dataSource = {
    val dsConfig = HikariConfig()
    dsConfig.setDriverClassName(dmscreen.dataSource.driver)
    dsConfig.setJdbcUrl(dmscreen.dataSource.url)
    dsConfig.setUsername(dmscreen.dataSource.user)
    dsConfig.setPassword(dmscreen.dataSource.password)
    dsConfig.setMaximumPoolSize(dmscreen.dataSource.maximumPoolSize)
    dsConfig.setMinimumIdle(dmscreen.dataSource.minimumIdle)
    dsConfig.setConnectionTimeout(dmscreen.dataSource.connectionTimeoutMins * 60 * 1000)

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
