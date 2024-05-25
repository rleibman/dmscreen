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

import zio.json.JsonCodec
import zio.json.ast.Json

enum EventType {

  case add, copy, move, remove, replace, test

}

case class JsonPath(value: String)

sealed abstract class DMScreenOperation

sealed case class Add(
  path:  JsonPath,
  value: Json
) extends DMScreenOperation

sealed case class Copy(
  from: JsonPath,
  to:   JsonPath
) extends DMScreenOperation

sealed case class Move(
  from: JsonPath,
  to:   JsonPath
) extends DMScreenOperation

sealed case class Remove(path: JsonPath) extends DMScreenOperation

sealed case class Replace(
  path:  JsonPath,
  value: Json
) extends DMScreenOperation

sealed case class Test(
  path:  JsonPath,
  value: Json
) extends DMScreenOperation

given JsonCodec[JsonPath] = JsonCodec.string.transform(JsonPath.apply, _.value)
given JsonCodec[DMScreenOperation] = JsonCodec.derived[DMScreenOperation]
