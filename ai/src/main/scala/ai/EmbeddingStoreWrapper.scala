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

import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import zio.*

case class EmbeddingStoreWrapper(store: QdrantEmbeddingStore) {

  def close(): Task[Unit] = ZIO.attemptBlocking(store.close())

}

object EmbeddingStoreWrapper {

  def qdrantStoreLayer(collectionName: String): ZLayer[QdrantContainer, Throwable, EmbeddingStoreWrapper] =
    for {
      container <- ZLayer.service[QdrantContainer]
      ret <-
        ZLayer.scoped {
          ZIO.acquireRelease(
            ZIO.attemptBlocking(
              EmbeddingStoreWrapper(
                QdrantEmbeddingStore
                  .builder()
                  .host(container.get.host)
                  .port(container.get.rpcPort)
                  .collectionName(collectionName)
                  .build()
              )
            )
          ) { store =>
            ZIO.logInfo("Closing store") *> store.close().tapError(e => ZIO.logError(s"Error closing store: $e")).orDie
          }
        }
    } yield ret

}
