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

import ai.*
import dev.langchain4j.data.document.{Document, Metadata}
import dev.langchain4j.model.chat.request.json.{JsonObjectSchema, JsonSchema}
import dev.langchain4j.model.chat.request.{ResponseFormat, ResponseFormatType}
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dmscreen.db.DMScreenZIORepository
import dmscreen.db.dnd5e.DND5eZIORepository
import dmscreen.dnd5e.{*, given}
import dmscreen.{Campaign, DMScreenError, DMScreenSession}
import zio.*
import zio.json.*
import zio.json.ast.*

trait DND5eAIServer[F[_]] {

  def generateEncounterDescription(encounter: Encounter): F[String]

  def generateNPCDetails(nonPlayerCharacter: NonPlayerCharacter): F[NonPlayerCharacter]

  def generateNPCDescription(npc: NonPlayerCharacter): F[String]

}

type AIIO[A] = ZIO[DMScreenSession & DND5eZIORepository & DMScreenZIORepository, DMScreenError, A]

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
      metadata.put("challengeRating", header.cr.name)
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

  def generateEncounterDescriptionTemplate(
    campaign:  Campaign,
    sceneOpt:  Option[Scene],
    encounter: Encounter
  ): String = {

    val info = encounter.info
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

  def live: ZLayer[DND5eZIORepository, Throwable, DND5eAIServer[AIIO]] = {
    val initLayer: ZLayer[
      EmbeddingStoreWrapper & QdrantContainer & DND5eZIORepository & ChatLanguageModel & LangChainConfiguration &
        ChatAssistant,
      Throwable,
      DND5eAIServer[AIIO]
    ] =
      ZLayer.fromZIO {
        for {
          repo   <- ZIO.service[DND5eZIORepository]
          config <- ZIO.service[LangChainConfiguration]
          _ <- {
            for {
              monsters <- repo
                .fullBestiary(MonsterSearch(pageSize = config.maxDND5eMonsters)) // Bring default monsters in
                .provideLayer(DMScreenSession.adminSession.toLayer)
              _      <- ZIO.logInfo(s"Got ${monsters.results.size} monsters")
              qdrant <- ZIO.service[QdrantContainer]
              _      <- qdrant.createCollectionAsync("monsters")
              _      <- ingestMonsters(monsters.results)
            } yield ()
          }
            .when(config.maxDND5eMonsters > 0)
          model <- ZIO.service[ChatLanguageModel]
        } yield new DND5eAIServer[AIIO] {
          def generateEncounterDescription(encounter: Encounter)
            : ZIO[DMScreenSession & DND5eZIORepository & DMScreenZIORepository, DMScreenError, String] = {
            for {
              campaign  <- ZIO.serviceWithZIO[DMScreenZIORepository](_.campaign(encounter.header.campaignId))
              dnd5eRepo <- ZIO.service[DND5eZIORepository]
              scene     <- ZIO.foreach(encounter.header.sceneId)(id => dnd5eRepo.scene(id))
              template = generateEncounterDescriptionTemplate(campaign.get, scene.flatten, encounter)
              res <- chat(template).provideLayer(ZLayer.succeed(model))
            } yield res
          }.mapError(e => DMScreenError("", Some(e)))

          def npcDetailsFormat: ResponseFormat =
            ResponseFormat
              .builder()
              .`type`(ResponseFormatType.JSON)
              .jsonSchema(
                JsonSchema
                  .builder()
                  .name("nonPlayerCharacter")
                  .rootElement(
                    JsonObjectSchema
                      .builder()
                      .required(
                        "faith",
                        "personalityTraits",
                        "ideals",
                        "bonds",
                        "flaws",
                        "organizations",
                        "allies",
                        "enemies",
                        "backstory",
                        "appearance"
                      )
                      .addStringProperty(
                        "faith",
                        "typically refers to a character's belief in a deity or a higher power, which can influence their actions and motivations, especially for classes like Clerics and Paladins, but it's primarily a narrative and roleplaying element, not a mechanical one. Faith should align with the race, class, background, etc."
                      )
                      .addStringProperty(
                        "personalityTraits",
                        "Personality traits are the core aspects of a character's personality. They are the defining characteristics that make a character unique. They can be positive or negative, and they can be used to help guide roleplaying decisions. For example, a character might be brave, cowardly, loyal, deceitful, etc. Personality traits are typically simple sentences. We should include 2 or 3 personality traits."
                      )
                      .addStringProperty(
                        "ideals",
                        """Ideals are the things that a character believes in. They are the principles that guide a character's actions and decisions. Ideals can be moral, ethical, or philosophical in nature. They can be things like "honor," "freedom," "justice," etc. Ideals are typically simple sentences. We should include 1 or 2 ideals."""
                      )
                      .addStringProperty(
                        "bonds",
                        """Bonds are the connections that a character has to other people, places, or things. They are the relationships that are important to a character. Bonds can be family members, friends, mentors, or even objects. They can be things like "my family," "my mentor," "my homeland," etc. Bonds are typically simple sentences. We should include 1 or 2 bonds."""
                      )
                      .addStringProperty(
                        "flaws",
                        """Flaws are the weaknesses or shortcomings that a character has. They are the things that can get a character into trouble or cause them to make bad decisions. Flaws can be things like "greed," "arrogance," "cowardice," etc. Flaws are typically simple sentences. We should include 1 or 2 flaws."""
                      )
                      .addStringProperty(
                        "organizations",
                        "Organizations are groups that a character is a part of or has a connection to. They can be guilds, factions, secret societies, or any other kind of group. Organizations can provide a character with resources, allies, and enemies."
                      )
                      .addStringProperty(
                        "allies",
                        "Allies are other characters that a character has a positive relationship with. They can be friends, family members, mentors, or any other kind of positive relationship. Allies can provide a character with help, support, and information."
                      )
                      .addStringProperty(
                        "enemies",
                        "Enemies are other characters that a character has a negative relationship with. They can be rivals, enemies, or any other kind of negative relationship. Enemies can provide a character with obstacles, challenges, and conflict."
                      )
                      .addStringProperty(
                        "backstory",
                        "The backstory is the history of a character's life before the start of the campaign. It includes details about the character's family, upbringing, education, training, and any significant events that have shaped the character into who they are today. The backstory can provide a character with motivation, goals, and connections to the world around them. You should draw from the character's background if it's provided"
                      )
                      .addStringProperty(
                        "appearance",
                        "A full and lengthy description of the appearance of the character, including physical features, clothing, and equipment. Should be informed by the race, class, background, etc."
                      )
                      .build()
                  )
                  .build()
              )
              .build()

          extension (s: String) {
            def orElse(other: Option[String]): Option[String] = {
              if (s.trim.isEmpty) other else Some(s)
            }
          }

          override def generateNPCDetails(nonPlayerCharacter: NonPlayerCharacter): AIIO[NonPlayerCharacter] = {
            val info = nonPlayerCharacter.info
            val template =
              s"""You are a Dungeon Master assistant specializing in generating Non Player Characters for Dungeons & Dragons.
                | Given the following NPC details, add the details that are not currently filled in.
                |
                | Notes:
                | - The details necessary are faith, personality traits, ideals, bonds, flaws, organizations, allies, enemies, backstory and appearance.
                | - The character's alignment is "${info.alignment.name}", this means that they are ${info.alignment.description}.
                | - Pay particular attention to character's relation to player, to their background and their alignment to ensure that the details are consistent with the character's history and motivations.
                | - Very important: All Fields are required.
                | NPC Details (in JSON format):
                | ${nonPlayerCharacter.toJsonPretty}
                |""".stripMargin

            case class NPCDetails(
              faith:             Option[String],
              personalityTraits: Option[String],
              ideals:            Option[String],
              bonds:             Option[String],
              flaws:             Option[String],
              organizations:     Option[String],
              allies:            Option[String],
              enemies:           Option[String],
              backstory:         Option[String],
              appearance:        Option[String]
            )

            given JsonCodec[NPCDetails] = JsonCodec.derived[NPCDetails]

            for {
              res <- chat(template, Some(npcDetailsFormat))
                .provideLayer(ZLayer.succeed(model)).mapError(DMScreenError(_))
              details <- ZIO.fromEither(res.fromJson[NPCDetails]).mapError(DMScreenError(_))
            } yield {

              val newInfo = info.copy(
                faith = info.faith.filter(_.nonEmpty).orElse(details.faith),
                traits = info.traits.copy(
                  personalityTraits =
                    info.traits.personalityTraits.filter(_.nonEmpty).orElse(details.personalityTraits),
                  ideals = info.traits.ideals.filter(_.nonEmpty).orElse(details.ideals),
                  bonds = info.traits.bonds.filter(_.nonEmpty).orElse(details.bonds),
                  flaws = info.traits.flaws.filter(_.nonEmpty).orElse(details.flaws),
                  appearance = info.traits.appearance.filter(_.nonEmpty).orElse(details.appearance)
                ),
                organizations = info.organizations.orElse(details.organizations).getOrElse(""),
                allies = info.allies.orElse(details.allies).getOrElse(""),
                enemies = info.enemies.orElse(details.enemies).getOrElse(""),
                backstory = info.backstory.orElse(details.backstory).getOrElse("")
              )

              NonPlayerCharacter(
                nonPlayerCharacter.header,
                newInfo.toJsonAST.toOption.get
              )

            }
          }

          override def generateNPCDescription(npc: NonPlayerCharacter): AIIO[String] = ???
        }
      }

    ZLayer.makeSome[DND5eZIORepository, DND5eAIServer[AIIO]](
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
