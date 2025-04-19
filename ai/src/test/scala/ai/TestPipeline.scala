package ai

import dev.langchain4j.data.document.{Document, Metadata}
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.{*, given}
import dmscreen.{*, given}
import zio.*
import zio.json.*
import zio.json.ast.*
import scala.jdk.CollectionConverters.*
import dmscreen.User
import auth.Session

object TestPipeline extends ZIOApp {

  override type Environment = StreamingLangChainEnvironment & QdrantContainer & EmbeddingStoreWrapper &
    TestDMScreenServerEnvironment & Session[User]
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[Environment] =
    ZLayer.make[Environment](
      LangChainServiceBuilder.ollamaStreamingChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.streamingAssistantLayerWithStore,
      QdrantContainer.live.orDie,
      EmbeddingStoreWrapper.qdrantStoreLayer("monsters").orDie,
      LangChainConfiguration.live,
      TestEnvironmentBuilder.live,
      DMScreenSession.adminSession.toLayer
    )

  extension (monster: Monster) {

    def toDocument: Document = {

      val header = monster.header
      val fullJson = monster.header.toJsonAST.toOption
        .flatMap(a => a.asObject)
        .map(_.merge(monster.jsonInfo))
        .get
        .toJsonPretty

      val metadata = Metadata()
      metadata.put("id", header.id.value)
      metadata.put("name", header.name)
      metadata.put("type", header.monsterType.toString)
      metadata.put("size", header.size.toString)
      metadata.put("challengeRating", header.cr.toString)
      metadata.put("armorClass", header.armorClass)
      metadata.put("hitPoints", header.maximumHitPoints)
      metadata.put("xp", header.xp)
      metadata.put("alignment", header.alignment.toString)
      header.biome.fold(())(biome => metadata.put("biome", biome.toString))
      header.alignment.fold(())(alignment => metadata.put("alignment", alignment.name))

      Document.from(fullJson, metadata)
    }

  }

  def ingestMonsters(monsters: List[Monster]): ZIO[EmbeddingStoreWrapper, Throwable, Unit] =
    (for {
      storeWrapper <- ZIO.service[EmbeddingStoreWrapper]
      res <- ZIO.foreachPar(monsters) { monster =>
        ZIO.attemptBlocking {
          EmbeddingStoreIngestor.ingest(
            monster.toDocument,
            storeWrapper.store
          )
        }
      }
    } yield res).unit

  def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, Unit] = {
    for {
      _ <- ZIO.logInfo("Starting MonsterRAG")

      repo     <- ZIO.service[DND5eZIORepository]
      monsters <- repo.fullBestiary(MonsterSearch(pageSize = 500)) // Bring some monsters in
      _        <- ZIO.logInfo(s"Got ${monsters.results.size} monsters")
      qdrant   <- ZIO.service[QdrantContainer]
      _        <- qdrant.createCollectionAsync("monsters")
      _        <- ingestMonsters(monsters.results)
      _        <- Console.printLine("Welcome to Llama 3! Ask me a question !")
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

}
