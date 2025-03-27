package ai

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.model.chat.request.*
import dev.langchain4j.model.chat.request.json.*
import dev.langchain4j.model.ollama.OllamaChatModel
import dmscreen.DMScreenSession
import zio.{EnvironmentTag, Scope, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

import scala.jdk.CollectionConverters.*

object TestMonsterExtractor extends ZIOApp {

  override type Environment = LangChainEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[LangChainEnvironment] =
    ZLayer.make[LangChainEnvironment](
      LangChainServiceBuilder.ollamaChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.chatAssistantLayer(),
      LangChainConfiguration.live
    )

  override def run: ZIO[Environment & ZIOAppArgs & Scope, Any, Any] = {
    val model = OllamaChatModel
      .builder()
      .baseUrl(BASE_URL)
      .modelName(MODEL)
      .temperature(0.0)
      .build()

    val monsterText =
      """Aartuk Starhorror
        |Medium Plant, Typically Lawful Evil
        |
        |Armor Class 14 (natural armor)
        |Hit Points 52 (8d8 + 16)
        |Speed 20 ft., climb 20 ft.
        |
        |STR
        |12 (+1)
        |DEX
        |10 (+0)
        |CON
        |14 (+2)
        |INT
        |13 (+1)
        |WIS
        |16 (+3)
        |CHA
        |10 (+0)
        |
        |Skills Stealth +4
        |Senses Darkvision 60 ft., Passive Perception 13
        |Languages Aartuk
        |Challenge 2 (450 XP)
        |Proficiency Bonus +2
        |
        |Traits
        |Spider Climb. The aartuk can climb difficult surfaces, including upside down on ceilings, without needing to make an ability check.
        |
        |Actions
        |Multiattack. The aartuk makes two Branch attacks, two Radiant Pellet attacks, or one of each.
        |
        |Branch. Melee Weapon Attack: +3 to hit, reach 10 ft., one target. Hit: 8 (2d6 + 1) bludgeoning damage.
        |
        |Radiant Pellet. Ranged Spell Attack: +2 to hit, range 60 ft., one target. Hit: 7 (3d4) radiant damage.
        |
        |Spellcasting (Psionics). The aartuk casts one of the following spells, requiring no spell components and using Wisdom as the spellcasting ability:
        |
        |1/day each: revivify, speak with plants
        |
        |Bonus Actions
        |Rally the Troops (1/Day). The aartuk magically ends the charmed and frightened conditions on itself and each creature of its choice that it can see within 30 feet of itself.
        |
        |Tongue (Recharge 6). The aartuk tries to use its gooey tongue to snare one Medium or smaller creature it can see within 30 feet of itself. The target must make a DC 12 Dexterity saving throw. On a failed save, the target is grappled by the tongue (escape DC 11) and pulled up to 25 feet toward the aartuk. The tongue can grapple one creature at a time.""".stripMargin

    val responseFormat: ResponseFormat = ResponseFormat
      .builder()
      .`type`(ResponseFormatType.JSON)
      .jsonSchema(
        JsonSchema
          .builder()
          .name("Monster")
          .rootElement(
            JsonObjectSchema
              .builder()
              .addStringProperty("name")
              .addEnumProperty(
                "monsterType",
                List(
                  "Aberration",
                  "Beast",
                  "Celestial",
                  "Construct",
                  "Dragon",
                  "Elemental",
                  "Fey",
                  "Fiend",
                  "Giant",
                  "Humanoid",
                  "Monstrosity",
                  "Ooze",
                  "Plant",
                  "Undead",
                  "Other"
                ).asJava
              )
              .addEnumProperty(
                "biome",
                List(
                  "Arctic",
                  "Coastal",
                  "Desert",
                  "Forest",
                  "Grassland",
                  "Hill",
                  "Mountain",
                  "Swamp",
                  "Underdark",
                  "Underwater",
                  "Urban",
                  "Unknown"
                ).asJava
              )
              .addEnumProperty(
                "alignment",
                List(
                  "Lawful Good",
                  "Neutral Good",
                  "Chaotic Good",
                  "Lawful Neutral",
                  "Neutral",
                  "Chaotic Neutral",
                  "Lawful Evil",
                  "Neutral Evil",
                  "Chaotic Evil"
                ).asJava
              )
              .addEnumProperty(
                "cr",
                List(
                  "0",
                  "1/8",
                  "1/4",
                  "1/2",
                  "1",
                  "2",
                  "3",
                  "4",
                  "5",
                  "6",
                  "7",
                  "8",
                  "9",
                  "10",
                  "11",
                  "12",
                  "13",
                  "14",
                  "15",
                  "16",
                  "17",
                  "18",
                  "19",
                  "20",
                  "21",
                  "22",
                  "23",
                  "24",
                  "25",
                  "26",
                  "27",
                  "28",
                  "29",
                  "30"
                ).asJava,
                "Experience points, also known as XP"
              )
              .addIntegerProperty("xp", "Experience Points, also known as XP")
              .addIntegerProperty("armorClass", "Armor class, also known as AC")
              .addIntegerProperty("hitPoints", "Hit points, also known as HP")
              .addEnumProperty("size", List("Tiny", "Small", "Medium", "Large", "Huge", "Gargantuan").asJava)
              .addIntegerProperty("STR", "Strength, typically 1-20")
              .addIntegerProperty("DEX", "Dexterity, typically 1-20")
              .addIntegerProperty("CON", "Constitution, typically 1-20")
              .addIntegerProperty("INT", "Intelligence, typically 1-20")
              .addIntegerProperty("WIS", "Wisdom, typically 1-20")
              .addIntegerProperty("CHA", "Charisma, typically 1-20")
              .addIntegerProperty("Proficiency Bonus")
              .addIntegerProperty("Initiative Bonus")
              .addProperty("Skills", JsonArraySchema.builder().items(JsonStringSchema.builder().build()).build())
              .addProperty("Senses", JsonArraySchema.builder().items(JsonStringSchema.builder().build()).build())
              .addStringProperty("Traits")
              .addStringProperty("Traits")
              .addStringProperty("Actions")
              .addStringProperty("Bonus Actions")
              .required(
                "name",
                "source",
                "monsterType",
                "biome",
                "alignment",
                "cr",
                "xp",
                "armorClass",
                "hitPoints",
                "size",
                "STR",
                "DEX",
                "CON",
                "INT",
                "WIS",
                "CHA"
              )
              .build()
          ).build()
      ).build()

    val chatRequest = ChatRequest
      .builder()
      .parameters(ChatRequestParameters.builder().responseFormat(responseFormat).build())
      .messages(UserMessage.from(monsterText))
      .build();

    for {
      json     <- ZIO.attemptBlocking(model.chat(chatRequest).aiMessage().text())
      response <- zio.Console.printLine(json)
    } yield response
  }

}
