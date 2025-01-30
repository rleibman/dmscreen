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

package ai

import com.dimafeng.testcontainers.GenericContainer
import org.testcontainers.containers.Container
import zio.*
import scala.jdk.CollectionConverters.*

import java.io.IOException

case class OllamaContainer() extends GenericContainer("ollama/ollama:latest") {

  private def execInContainerZIO(commands: String*): IO[IOException, Container.ExecResult] =
    ZIO.attemptBlockingIO {
      execInContainer(commands*)
    }

  def pullModel(model: String): IO[IOException, Container.ExecResult] = execInContainerZIO("ollama", "pull", model)

}

object OllamaContainer {

  val live: TaskLayer[OllamaContainer] = ZLayer.fromZIO(for {
    _ <- ZIO.logInfo("Starting Ollama Container")
    c <- ZIO.attemptBlocking {
      val c = OllamaContainer()
      c.container.setPortBindings(List("11434:11434").asJava)
      c.start()
      c
    }
    _ <- ZIO.logInfo("Started Ollama Container")
  } yield c)

}
