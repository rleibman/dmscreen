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

package dmscreen.util

import scala.scalajs.js

extension (value: js.UndefOr[String | js.Array[String] | Double]) {

  def asDouble(default: Double = 0.0): Double =
    value match {
      case s: String           => s.toDoubleOption.getOrElse(default)
      case d: Double           => d
      case a: js.Array[String] => a.headOption.fold(default)(_.toDoubleOption.getOrElse(default))
      case _: Unit             => default
    }
  def asInt(default: Int = 0): Int =
    value match {
      case s: String           => s.toIntOption.getOrElse(default)
      case d: Double           => d.toInt
      case a: js.Array[String] => a.headOption.fold(default)(_.toIntOption.getOrElse(default))
      case _: Unit             => default
    }
  def asLong(default: Long = 0): Long =
    value match {
      case s: String           => s.toLongOption.getOrElse(default)
      case d: Double           => d.toLong
      case a: js.Array[String] => a.headOption.fold(default)(_.toLongOption.getOrElse(default))
      case _: Unit             => default
    }

}
