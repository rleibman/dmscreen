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

package auth

case class EmptyNothing()

//  def liveLayer: URLayer[DMScreenZIORepository, TokenHolder] =
//    ZLayer.fromZIO(for {
//      repo <- ZIO.service[DMScreenZIORepository]
//      freq = new zio.DurationSyntax(1).hour
//      _ <- (ZIO.logInfo("Cleaning up old tokens") *> repo.tokenOperations.cleanup.provide(GameService.godLayer))
//        .repeat(Schedule.spaced(freq).jittered).forkDaemon
//    } yield {
//      new TokenHolder {
//
//        override def peek(
//          token:   Token,
//          purpose: TokenPurpose
//        ): IO[DMScreenError, Option[User]] = repo.tokenOperations.peek(token, purpose).provide(GameService.godLayer)
//
//        override def createToken(
//          user:    User,
//          purpose: TokenPurpose,
//          ttl:     Option[Duration]
//        ): IO[DMScreenError, Token] = repo.tokenOperations.createToken(user, purpose, ttl).provide(GameService.godLayer)
//
//        override def validateToken(
//          token:   Token,
//          purpose: TokenPurpose
//        ): IO[DMScreenError, Option[User]] =
//          repo.tokenOperations.validateToken(token, purpose).provide(GameService.godLayer)
//      }
//    })
//
//  def tempCache(cache: Cache[(String, TokenPurpose), Nothing, User]): TokenHolder =
//    new TokenHolder {
//
//      private val random = SecureRandom.getInstanceStrong
//
//      override def createToken(
//        user:    User,
//        purpose: TokenPurpose,
//        ttl:     Option[Duration] = Option(3.hours)
//      ): IO[DMScreenError, Token] = {
//        val t = new BigInteger(12 * 5, random).toString(32)
//        cache.get((t, purpose)).as(Token(t))
//      }
//
//      override def validateToken(
//        token:   Token,
//        purpose: TokenPurpose
//      ): IO[DMScreenError, Option[User]] = {
//        for {
//          contains <- cache.contains((token.tok, purpose))
//          u <-
//            if (contains) {
//              cache.get((token.tok, purpose)).map(Some.apply)
//            } else ZIO.none
//          _ <- cache.invalidate(token.tok, purpose)
//        } yield u
//      }
//
//      override def peek(
//        token:   Token,
//        purpose: TokenPurpose
//      ): IO[DMScreenError, Option[User]] = {
//        for {
//          contains <- cache.contains((token.tok, purpose))
//          u <-
//            if (contains) {
//              cache.get((token.tok, purpose)).map(Some.apply)
//            } else ZIO.none
//        } yield u
//      }
//
//    }

//}
