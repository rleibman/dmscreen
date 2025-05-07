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

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import auth.Session
import dmscreen.*
import zio.{Console, EnvironmentTag, Scope, Task, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

object TestWithRAG extends ZIOApp {

  override type Environment = StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper & Session[User]
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap
    : ULayer[StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper & Session[User]] =
    ZLayer.make[StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper & Session[User]](
      LangChainServiceBuilder.ollamaStreamingChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.streamingAssistantLayerWithStore,
      QdrantContainer.live.orDie,
      EmbeddingStoreWrapper.qdrantStoreLayer("monsters").orDie,
      LangChainConfig.live,
      DMScreenSession.adminSession.toLayer
    )

  private def ingestMonsters: ZIO[EmbeddingStoreWrapper, Throwable, Unit] = {
    for {
      storeWrapper <- ZIO.service[EmbeddingStoreWrapper]
      _ <- ZIO.attemptBlocking {
        EmbeddingStoreIngestor.ingest(
          FileSystemDocumentLoader.loadDocument("/home/rleibman/dnd/Books/D&D 5E - Monster Manual.pdf"),
          storeWrapper.store
        )
      }
    } yield ()
  }

  def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      _      <- ZIO.logInfo("Starting MonsterRAG")
      qdrant <- ZIO.service[QdrantContainer]
      _      <- qdrant.client.createCollection("monsters")
      _      <- ingestMonsters
      _      <- Console.printLine("Welcome to Llama 3! Ask me a question !")
      _ <- ZIO.iterate(true)(identity) { _ =>
        for {
          question <- Console.readLine
          _ <- streamedChat(question)
            .takeWhile(_.nonEmpty)
            .foreach(token => Console.print(token))
            .when(question.nonEmpty)
          _ <- Console.printLine("")
        } yield question.nonEmpty
      }
    } yield (())

}
