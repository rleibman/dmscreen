package ai

import zio.{Console, EnvironmentTag, Scope, ULayer, ZIO, ZIOApp, ZIOAppArgs, ZLayer}

object Test extends ZIOApp {

  override type Environment = LangChainEnvironment
  override val environmentTag: EnvironmentTag[Environment] = EnvironmentTag[Environment]

  override def bootstrap: ULayer[LangChainEnvironment] =
    ZLayer.make[LangChainEnvironment](
      LangChainServiceBuilder.ollamaStreamingChatModelLayer,
      LangChainServiceBuilder.messageWindowChatMemoryLayer(),
      LangChainServiceBuilder.assistantLayer(),
      LangChainConfiguration.live
    )

  def run: ZIO[Environment & ZIOAppArgs & Scope, Throwable, Unit] =
    for {
      _ <- Console.printLine("Welcome to Llama 3! Ask me a question !")
      _ <- ZIO.iterate(true)(identity) { _ =>
        for {
          question <- Console.readLine
          _ <- chat(question)
            .takeWhile(_.nonEmpty)
            .foreach(token => Console.print(token))
            .when(question.nonEmpty)
          _ <- Console.printLine("")
        } yield question.nonEmpty
      }
    } yield ()

}
