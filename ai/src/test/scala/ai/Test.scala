package ai

import zio.{Console, EnvironmentTag, Scope, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

import dmscreen.LangChainConfig

object Test extends ZIOApp {

  override type Environment = StreamingLangChainEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[StreamingLangChainEnvironment] =
    ZLayer.make[StreamingLangChainEnvironment](
      LangChainServiceBuilder.ollamaStreamingChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.streamingAssistantLayer(),
      LangChainConfig.live
    )

  def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      _ <- Console.printLine("Welcome to Llama 3! Ask me a question !")
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
    } yield ()

}
