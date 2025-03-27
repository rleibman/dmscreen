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

import dev.langchain4j.data.message.UserMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.request.{ChatRequest, ChatRequestParameters, ResponseFormat}
import dev.langchain4j.model.ollama.{OllamaChatModel, OllamaStreamingChatModel}
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever
import dev.langchain4j.service.{AiServices, TokenStream}
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.memory.chat.InMemoryChatMemoryStore
import zio.*
import zio.stream.ZStream

val MODEL = "llama3.2:3b" // Good, but too big (3.7 minutes)
//val MODEL = "gemma3:1b" // Tame, a bit better with higher temperature
//val MODEL = "phi" //Super bad
//val MODEL = "openhermes" //Way too slow (but it did complete in 2.3 minutes) and hallucinated
//val MODEL = "tinyllama:latest" //Broken, and hallucinates
//val MODEL = "gemma3:4b" //A bit too big (times out)
//val MODEL = "llama3.2:1b"
//val MODEL = "gemma2" //Decent, but too big (and slow)"
//val MODEL = "llama3.1" //Not bad
//val MODEL = "llama3.3" //Too big
val BASE_URL = "http://localhost:11434"
val timeout = 240.seconds

trait StreamAssistant {

  def chat(message: String): TokenStream

}

trait ChatAssistant {

  def chat(message: String): String

}
type StreamingLangChainEnvironment = ChatMemory & StreamingChatLanguageModel & StreamAssistant & LangChainConfiguration
type LangChainEnvironment = ChatMemory & ChatLanguageModel & ChatAssistant & LangChainConfiguration

case class LangChainConfiguration(
)

object LangChainConfiguration {

  val live = ZLayer.succeed(LangChainConfiguration())

}

object LangChainServiceBuilder {

  // Use configuration
  def messageWindowChatMemoryLayer(): URLayer[LangChainConfiguration, ChatMemory] =
    ZLayer.succeed(
      ChatMemory.fromJava(
        MessageWindowChatMemory
          .builder()
          .id("12345")
          .maxMessages(1000)
          .chatMemoryStore(new InMemoryChatMemoryStore())
          .build()
      )
    )

  // Use configuration
  def ollamaChatModelLayer: URLayer[LangChainConfiguration, ChatLanguageModel] =
    ZLayer.succeed(
      ChatLanguageModel.fromJava(
        OllamaChatModel.builder
          .baseUrl(BASE_URL)
          .modelName(MODEL)
          .timeout(timeout)
          .temperature(1.1)
          .topK(40)
          .topP(0.9)
          .build
      )
    )

  // Use configuration
  def ollamaStreamingChatModelLayer: URLayer[LangChainConfiguration, StreamingChatLanguageModel] =
    ZLayer.succeed(
      StreamingChatLanguageModel.fromJava(
        OllamaStreamingChatModel.builder
          .baseUrl(BASE_URL)
          .modelName(MODEL)
          .timeout(timeout)
          .temperature(1.1)
          .topK(40)
          .topP(0.9)
          .build
      )
    )

  def streamingAssistantLayerWithStore: URLayer[
    StreamingChatLanguageModel & ChatMemory & EmbeddingStoreWrapper & LangChainConfiguration,
    StreamAssistant
  ] =
    ZLayer.fromZIO(
      for {
        store       <- ZIO.serviceWith[EmbeddingStoreWrapper](_.store)
        chatMemory  <- ZIO.service[ChatMemory]
        streamModel <- ZIO.service[StreamingChatLanguageModel]
      } yield {
        val base: AiServices[StreamAssistant] = AiServices
          .builder(classOf[StreamAssistant])
          .streamingChatLanguageModel(streamModel)
          .chatMemory(chatMemory)

        val ret: StreamAssistant = base.contentRetriever(EmbeddingStoreContentRetriever.from(store)).build()
        ret
      }
    )

  private def chatAssistant(storeOpt: Option[EmbeddingStore[TextSegment]] = None)
    : ZIO[ChatLanguageModel & ChatMemory, Nothing, ChatAssistant] =
    for {
      chatMemory <- ZIO.service[ChatMemory] // Do we really want memory? Should it be "by user"?
      model      <- ZIO.service[ChatLanguageModel]
    } yield {
      val base: AiServices[ChatAssistant] = AiServices
        .builder(classOf[ChatAssistant])
        .chatLanguageModel(model)
        .chatMemory(chatMemory)

      storeOpt.fold(base)(store => base.contentRetriever(EmbeddingStoreContentRetriever.from(store))).build
    }

  val chatAssistantLayerWithStore
    : URLayer[EmbeddingStoreWrapper & ChatLanguageModel & ChatMemory & LangChainConfiguration, ChatAssistant] =
    ZLayer.fromZIO(
      for {
        store      <- ZIO.serviceWith[EmbeddingStoreWrapper](_.store)
        chatMemory <- ZIO.service[ChatMemory]
        model      <- ZIO.service[ChatLanguageModel]
      } yield {
        val base: AiServices[ChatAssistant] = AiServices
          .builder(classOf[ChatAssistant])
          .chatLanguageModel(model)
          .chatMemory(chatMemory)

        val ret: ChatAssistant = base.contentRetriever(EmbeddingStoreContentRetriever.from(store)).build()
        ret
      }
    )

  def chatAssistantLayer(storeOpt: Option[EmbeddingStore[TextSegment]] = None)
    : URLayer[ChatLanguageModel & ChatMemory & LangChainConfiguration, ChatAssistant] =
    ZLayer.fromZIO(chatAssistant(storeOpt))

  def streamingAssistantLayer(storeOpt: Option[EmbeddingStore[TextSegment]] = None)
    : URLayer[StreamingChatLanguageModel & ChatMemory & LangChainConfiguration, StreamAssistant] =
    ZLayer.fromZIO(
      for {
        chatMemory  <- ZIO.service[ChatMemory]
        streamModel <- ZIO.service[StreamingChatLanguageModel]
      } yield {
        val base: AiServices[StreamAssistant] = AiServices
          .builder(classOf[StreamAssistant])
          .streamingChatLanguageModel(streamModel)
          .chatMemory(chatMemory)

        val ret: StreamAssistant =
          storeOpt.fold(base)(store => base.contentRetriever(EmbeddingStoreContentRetriever.from(store))).build
        ret
      }
    )

}

def streamedChat(message: String): ZStream[StreamAssistant, Throwable, String] =
  ZStream.unwrap(for {
    aiServices <- ZIO.service[StreamAssistant]
  } yield ZStream.async[Any, Throwable, String] { callback =>
    aiServices
      .chat(message)
      .onPartialResponse(str => callback(ZIO.succeed(Chunk(str))))
      .onCompleteResponse(_ => callback(ZIO.succeed(Chunk.empty)))
      .onError(error => callback(ZIO.fail(Some(error))))
      .start()
  })

def chat(
  question:       String,
  responseFormat: Option[ResponseFormat] = None
): ZIO[ChatLanguageModel, Throwable, String] = {
  val request = responseFormat
    .fold(ChatRequest.builder())(fmt =>
      ChatRequest.builder().parameters(ChatRequestParameters.builder().responseFormat(fmt).build())
    )
    .messages(UserMessage.from(question))
    .build();
  for {
    model <- ZIO.service[ChatLanguageModel]
    res   <- ZIO.attemptBlocking(model.chat(request).aiMessage().text())
  } yield res
}
