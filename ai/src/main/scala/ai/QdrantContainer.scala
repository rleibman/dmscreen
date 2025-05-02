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

import com.dimafeng.testcontainers.*
import io.qdrant.client.QdrantGrpcClient
import io.qdrant.client.grpc.Collections
import zio.{IO, Task, TaskLayer, ZIO, ZLayer}

import java.io.IOException
import scala.jdk.CollectionConverters.*

case class QdrantContainer(
  dockerImageName: String = QdrantContainer.defaultDockerImageName,
  httpPort:        Int = QdrantContainer.defaultHttpPort,
  rpcPort:         Int = QdrantContainer.defaultRPCPort
) extends SingleContainer[org.testcontainers.qdrant.QdrantContainer] {

  override val container: org.testcontainers.qdrant.QdrantContainer = {
    val c: org.testcontainers.qdrant.QdrantContainer = new org.testcontainers.qdrant.QdrantContainer(dockerImageName)
    c
  }

  val client = QdrantClient(
    QdrantGrpcClient.newBuilder(host, rpcPort, false).build()
  )

}

object QdrantContainer {

  val defaultDockerImageName = s"qdrant/qdrant:latest"
  val defaultHttpPort = 6333
  val defaultRPCPort = 6334

  val live: TaskLayer[QdrantContainer] = ZLayer.fromZIO(for {
    _ <- ZIO.logInfo("Starting QDrant Container")
    c <- ZIO.attemptBlocking {
      val c = QdrantContainer()
      c.container.setPortBindings(List(s"$defaultHttpPort:$defaultHttpPort", s"$defaultRPCPort:$defaultRPCPort").asJava)
      c.start()
      c
    }
    _ <- ZIO.logInfo("Started QDrant Container")
  } yield c)

}
