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

import _root_.auth.{AuthenticatedSession, Session, UnauthenticatedSession}
import zio.{ULayer, ZLayer}

import java.time.LocalDateTime

//type Session[User] = _root_.auth.Session[User]

object DMScreenSession {

  val empty: Session[User] = UnauthenticatedSession[User]()

  val adminSession: Session[User] = AuthenticatedSession(
    Some(
      User(
        id = UserId.admin,
        email = "admin@leibmanland.com",
        name = "admin",
        created = LocalDateTime.now,
        active = true
      )
    )
  )

}

extension (session: Session[User]) {

  def toLayer: ULayer[Session[User]] = ZLayer.succeed(session)

}
