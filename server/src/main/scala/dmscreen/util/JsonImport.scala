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

import zio.*
import zio.json.*
import zio.json.ast.Json
import zio.stream.*

import java.io.File
import java.net.URI

/** Thanks ChatGPT!
  */

/** Note, this only works for json files where each object in an array of objects is a json object, it won't work for
  * other types of json (e.g. arrays, strings, etc.) BTW, yeah, this is an ugly parser, blame chatgpt
  * @param uri
  * @tparam Data
  * @return
  */
def jsonStream[Data: JsonDecoder](file: File): ZStream[Any, Throwable, Data] = {
  def countOpenBraces(s:  String): Int = s.count(_ == '{')
  def countCloseBraces(s: String): Int = s.count(_ == '}')

  ZStream
    .fromFile(file)
    .via(ZPipeline.utf8Decode)
    .via(ZPipeline.splitOn("\n"))
    .mapAccum((new StringBuilder(), 0, false)) { case ((builder, openBraces, skipping), line) =>
      // Check if we're still skipping the initial array brackets
      if (!skipping && line.contains("[")) {
        ((builder, openBraces, true), Chunk.empty)
      } else {
        val newBuilder = builder.append(line).append("\n")
        val newOpenBraces = openBraces + countOpenBraces(line) - countCloseBraces(line)
        // Check if the line contains the closing bracket of the array and we're done
        if (line.contains("]") && newOpenBraces == 0) {
          ((new StringBuilder(), 0, true), Chunk.empty)
        } else if (newOpenBraces == 0 && builder.nonEmpty) {
          val jsonString = newBuilder.toString()
          jsonString.fromJson[Data] match {
            case Right(data) => ((new StringBuilder(), 0, true), Chunk(data))
            case Left(e)     => throw new RuntimeException(e)
//              ((newBuilder, newOpenBraces, true), Chunk.empty)
          }
        } else {
          ((newBuilder, newOpenBraces, true), Chunk.empty)
        }
      }
    }
    .collect {
      case data if data.nonEmpty => data
    }
    .flattenChunks
}

object JsonImport extends ZIOAppDefault {

  def run: ZIO[ZIOAppArgs & Scope, Throwable, Unit] = {
    val file = File("/home/rleibman/projects/dmscreen/common/shared/src/main/resources/5e-SRD-Monsters.json")
    case class MyObject(
      id:   Int,
      name: String
    )
    object MyObject {
      implicit val decoder: JsonDecoder[MyObject] = DeriveJsonDecoder.gen[MyObject]
    }

    jsonStream[Json](file).take(5).runCollect.map(_.foreach(data => println(data)))
  }

}
