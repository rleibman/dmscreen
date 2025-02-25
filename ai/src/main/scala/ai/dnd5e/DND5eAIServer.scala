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

package ai.dnd5e

import ai.{
  ChatAssistant,
  ChatLanguageModel,
  EmbeddingStoreWrapper,
  LangChainConfiguration,
  LangChainServiceBuilder,
  QdrantContainer,
  StreamAssistant,
  StreamingChatLanguageModel,
  chat,
  streamedChat
}
import dev.langchain4j.data.document.{Document, Metadata}
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dmscreen.{Campaign, DMScreenError, DMScreenSession}
import dmscreen.db.DMScreenZIORepository
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.{*, given}
import zio.*
import zio.json.*
import zio.json.ast.*

trait DND5eAIServer {

  def generateEncounterDescription(encounter: Encounter)
    : ZIO[DMScreenSession & DND5eZIORepository & DMScreenZIORepository, DMScreenError, String]

}

object DND5eAIServer {

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

  def createTemplate(
    campaign:  Campaign,
    sceneOpt:  Option[Scene],
    encounter: Encounter
  ): String = {

    val info = encounter.info
    val scene = encounter.header.sceneId
    val sceneInfo = sceneOpt.map(_.info)

    s"""You are a Dungeon Master assistant specializing in generating immersive encounter descriptions for Dungeons & Dragons.
       | Use descriptive, evocative language to set the scene and provide narrative hooks.
       | Incorporate biome details, time of day, and atmospheric elements into the response.
       | Use monster knowledge from the embedded storage to foreshadow threats or provide contextual details.
       |
       | Encounter Context:
       | The Encounter is part of the campaign called ${campaign.header.name}
       | ${sceneOpt.fold("")(scene => s"It is part of the scene called ${scene.header.name}")}
       | ${sceneInfo.fold("")(si => s"Notes about the scene: ${si.notes}")}
       |  ${info.timeOfDay match {
        case EncounterTimeOfDay.unimportant => ""
        case other                          => s"Encounter Time of Day: ${other.toString}"
      }}
       |  ${info.biome match {
        case Biome.Unimportant => ""
        case other             => s"Biome: ${other.toString}"
      }}
    | Monsters:
    | ${info.monsters.map(monster => s"  ${monster.monsterHeader.name} ").mkString("\n")}
    |
    | NPCs:
    | ${info.npcs.map(npc => s"  ${npc.name} ").mkString("\n")}
    |
    | Use your embedded knowledge of these creatures to subtly hint at their presence or describe their behavior in the environment.
    | Formatting Rules
    | - Write in a narrative style suitable for a Dungeon Master to read aloud.
    | - Include sensory details (sight, sound, smell, touch) to make the scene vivid.
    | - Introduce subtle hints about hidden dangers or environmental hazards.
    | - If applicable, mention traps, natural hazards, or clues to the presence of enemies.
    | - Keep the description between 100-250 words.
    |
    |""".stripMargin
  }

  def live: ZLayer[DND5eZIORepository, Throwable, DND5eAIServer] = {
    val initLayer: ZLayer[
      EmbeddingStoreWrapper & QdrantContainer & DND5eZIORepository & ChatLanguageModel & LangChainConfiguration &
        ChatAssistant,
      Throwable,
      DND5eAIServer
    ] =
      ZLayer.fromZIO {
        for {
          repo <- ZIO.service[DND5eZIORepository]
          monsters <- repo
            .fullBestiary(MonsterSearch(pageSize = 500)).provideLayer(DMScreenSession.adminSession.toLayer) // Bring default monsters in
          _         <- ZIO.logInfo(s"Got ${monsters.results.size} monsters")
          qdrant    <- ZIO.service[QdrantContainer]
          _         <- qdrant.createCollectionAsync("monsters")
          _         <- ingestMonsters(monsters.results)
          assistant <- ZIO.service[ChatAssistant]
        } yield new DND5eAIServer {
          def generateEncounterDescription(encounter: Encounter)
            : ZIO[DMScreenSession & DND5eZIORepository & DMScreenZIORepository, DMScreenError, String] = {
            for {
              campaign  <- ZIO.serviceWithZIO[DMScreenZIORepository](_.campaign(encounter.header.campaignId))
              dnd5eRepo <- ZIO.service[DND5eZIORepository]
              scene     <- ZIO.foreach(encounter.header.sceneId)(id => dnd5eRepo.scene(id))
              template = createTemplate(campaign.get, scene.flatten, encounter)
              res <- chat(template).provideLayer(ZLayer.succeed(assistant))
            } yield res
          }.mapError(e => DMScreenError("", Some(e)))
        }
      }

    ZLayer.makeSome[DND5eZIORepository, DND5eAIServer](
      LangChainServiceBuilder.ollamaChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.chatAssistantLayerWithStore,
      QdrantContainer.live.orDie,
      EmbeddingStoreWrapper.qdrantStoreLayer("monsters").orDie,
      LangChainConfiguration.live,
      initLayer
    )
  }

}
