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

import dev.langchain4j.agent.tool.ToolSpecification
import dev.langchain4j.data.message.{AiMessage, ChatMessage, UserMessage}
import dev.langchain4j.model.StreamingResponseHandler

import java.util
import java.util.Collections.singletonList

object StreamingChatLanguageModel {

  def fromJava(j: dev.langchain4j.model.chat.StreamingChatLanguageModel): StreamingChatLanguageModel =
    new StreamingChatLanguageModel() {
      def toJava: dev.langchain4j.model.chat.StreamingChatLanguageModel = j

      def generate(
        messages: util.List[ChatMessage],
        handler:  StreamingResponseHandler[AiMessage]
      ): Unit = j.generate(messages, handler)
    }

}

given Conversion[StreamingChatLanguageModel, dev.langchain4j.model.chat.StreamingChatLanguageModel] = _.toJava

trait StreamingChatLanguageModel {

  def toJava: dev.langchain4j.model.chat.StreamingChatLanguageModel

  /** Generates a response from the model based on a message from a user.
    *
    * @param userMessage
    *   The message from the user.
    * @param handler
    *   The handler for streaming the response.
    */
  def generate(
    userMessage: String,
    handler:     StreamingResponseHandler[AiMessage]
  ): Unit = {
    generate(singletonList(UserMessage.from(userMessage)), handler)
  }

  /** Generates a response from the model based on a message from a user.
    *
    * @param userMessage
    *   The message from the user.
    * @param handler
    *   The handler for streaming the response.
    */
  def generate(
    userMessage: UserMessage,
    handler:     StreamingResponseHandler[AiMessage]
  ): Unit = {
    generate(singletonList(userMessage), handler)
  }

  /** Generates a response from the model based on a sequence of messages. Typically, the sequence contains messages in
    * the following order: System (optional) - User - AI - User - AI - User ...
    *
    * @param messages
    *   A list of messages.
    * @param handler
    *   The handler for streaming the response.
    */
  def generate(
    messages: util.List[ChatMessage],
    handler:  StreamingResponseHandler[AiMessage]
  ): Unit

  /** Generates a response from the model based on a list of messages and a list of tool specifications. The response
    * may either be a text message or a request to execute one of the specified tools. Typically, the list contains
    * messages in the following order: System (optional) - User - AI - User - AI - User ...
    *
    * @param messages
    *   A list of messages.
    * @param toolSpecifications
    *   A list of tools that the model is allowed to execute. The model autonomously decides whether to use any of these
    *   tools.
    * @param handler
    *   The handler for streaming the response. {@link AiMessage} can contain either a textual response or a request to
    *   execute one of the tools.
    */
  def generate(
    messages:           util.List[ChatMessage],
    toolSpecifications: util.List[ToolSpecification],
    handler:            StreamingResponseHandler[AiMessage]
  ): Unit = {
    throw new IllegalArgumentException("Tools are currently not supported by this model")
  }

  /** Generates a response from the model based on a list of messages and a tool specification.
    *
    * @param messages
    *   A list of messages.
    * @param toolSpecification
    *   A tool that the model is allowed to execute.
    * @param handler
    *   The handler for streaming the response.
    */
  def generate(
    messages:          util.List[ChatMessage],
    toolSpecification: ToolSpecification,
    handler:           StreamingResponseHandler[AiMessage]
  ): Unit = {
    throw new IllegalArgumentException("Tools are currently not supported by this model")
  }

}
