package dmscreen.mail

package mail

import courier.{Envelope, Text}
import dmscreen.{ConfigurationService, ConfigurationServiceImpl, SmtpConfig}
import zio.*
import zio.test.*
import zio.test.Assertion.*

import javax.mail.internet.InternetAddress

object PostmanSpec extends ZIOSpec[Postman & SmtpConfig & ConfigurationService] {

  override def spec =
    suite("PostmanSpec")(
      test("sending an email") {
        for {
          postman <- ZIO.service[Postman]
          delivered <- postman
            .deliver(
              Envelope
                .from(new InternetAddress("system@dmscreen.leibman.net"))
                .to(new InternetAddress("roberto.leibman@gmail.com"))
                .subject("hello")
                .content(Text("body of hello"))
            )
        } yield assertTrue(delivered.nonEmpty)

      }
    )

  override def bootstrap: ULayer[Environment] = {
    val smptConfigLayer: URLayer[ConfigurationService, SmtpConfig] =
      ZLayer.fromZIO(ZIO.serviceWithZIO[ConfigurationService](_.appConfig).map(_.dmscreen.smtp)).orDie // Specialize the configuration service to pass it to Postman

    ZLayer.make[Environment](
      Postman.live,
      ConfigurationServiceImpl.live,
      smptConfigLayer
    )
  }

}
