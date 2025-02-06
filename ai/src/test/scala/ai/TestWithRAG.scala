package ai

import dev.langchain4j.data.document.loader.FileSystemDocumentLoader
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.qdrant.QdrantEmbeddingStore
import zio.{Console, EnvironmentTag, Scope, Task, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

object TestWithRAG extends ZIOApp {

  override type Environment = StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper] =
    ZLayer.make[StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper](
      LangChainServiceBuilder.ollamaStreamingChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.streamingAssistantLayerWithStore,
      QdrantContainer.live.orDie,
      EmbeddingStoreWrapper.qdrantStoreLayer("monsters").orDie,
      LangChainConfiguration.live
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
      _      <- qdrant.createCollectionAsync("monsters")
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
