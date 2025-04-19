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

package dmscreen.mail

import auth.*
import dmscreen.*
import courier.{Envelope, Mailer, Multipart}
import dmscreen.{ConfigurationService, DMScreenTask, SmtpConfig}

import scala.concurrent.duration.*
import zio.{RIO, Task, ZIO, *}

import javax.mail.internet.InternetAddress

//object EmailGenerator {
//
//  def lostPasswordEmail(
//    user: User
//  ): RIO[TokenHolder[DMScreenTask] & Session[User] & ConfigurationService, Envelope] =
//    for {
//      tokenHolder <- ZIO.service[TokenHolder[DMScreenTask]]
//      token       <- tokenHolder.createToken(user, TokenPurpose.LostPassword)
//      webHostName <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig).map(_.dmscreen.smtp.webHostname)
//    } yield {
//      val linkUrl = s"http://$webHostName/unauth/loginForm?passwordReset=true&token=$token"
//      Envelope
//        .from(new InternetAddress("dmscreen@leibman.net", "dmscreen administrator"))
//        .to(new InternetAddress(user.email))
//        .subject("dmscreen.leibman.net, you lost your password")
//        .content(Multipart().html(s"""<html><body>
//                                     | <p>So sorry you lost your password</p>
//                                     | <p>Here's a link to generate a new one.</p>
//                                     | <p>please Click here: <a href="$linkUrl">$linkUrl</a>.</p>
//                                     | <p>Note that this link has a limited time</p>
//                                     |</body></html>""".stripMargin))
//    }
//
//  def registrationEmail(
//    user: User
//  ): RIO[TokenHolder[DMScreenTask] & Session[User] & ConfigurationService, Envelope] =
//    for {
//      tokenHolder <- ZIO.service[TokenHolder[DMScreenTask]]
//      token       <- tokenHolder.createToken(user, TokenPurpose.NewUser)
//      webHostName <- ZIO.serviceWithZIO[ConfigurationService](_.appConfig).map(_.dmscreen.smtp.webHostname)
//    } yield {
//      val linkUrl =
//        s"http://$webHostName/unauth/confirmRegistration?token=$token"
//      Envelope
//        .from(new InternetAddress("dmscreen@leibman.net", "dmscreen administrator"))
//        .to(new InternetAddress(user.email))
//        .subject("Welcome to dmscreen")
//        .content(Multipart().html(s"""<html><body>
//                                     | <p>Thanks for registering!</p>
//                                     | <p>All you have to do now is activate your account.</p>
//                                     | <p>Please click here: <a href="$linkUrl">$linkUrl</a>.</p>
//                                     | <p>Note that this link is time sensitive, eventually it will expire</p>
//                                     |</body></html>""".stripMargin))
//    }
//
//}

trait Postman {

  def deliver(email: Envelope): UIO[Unit]

}

/** An instatiation of the Postman that user the courier mailer
  */
object Postman {

  def live: ZLayer[SmtpConfig, Nothing, Postman] =
    ZLayer.fromZIO {
      for {
        config <- ZIO.service[SmtpConfig]
      } yield new Postman {

        lazy val mailer: Mailer = {
//          java.lang.System.setProperty("mail.smtp.localhost", config.localhost)
//          java.lang.System.setProperty("mail.smtp.localaddress", config.localhost)
          if (config.auth)
            Mailer(config.host, config.port)()
//              .auth(config.auth)
//              .as(
//                config.user,
//                config.password
//              )
//              .startTls(config.startTTLS)()
          else
            Mailer(config.host, config.port).auth(config.auth)()
        }

        override def deliver(email: Envelope): UIO[Unit] =
          ZIO
            .fromFuture(implicit ec => mailer(email.bcc(new InternetAddress("roberto+dmscreen@leibman.net")))).tapBoth(
              e => ZIO.logError(s"Error sending email: $e"),
              msg => ZIO.logDebug(s"Email sent to ${email.to.mkString(", ")}: $msg")
            ).forkDaemon.unit

      }
    }

}
