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
import dev.langchain4j.model.chat.response.StreamingChatResponseHandler

import java.util
import java.util.Collections.singletonList

object StreamingChatLanguageModel {

  def fromJava(j: dev.langchain4j.model.chat.StreamingChatLanguageModel): StreamingChatLanguageModel =
    new StreamingChatLanguageModel() {
      def toJava: dev.langchain4j.model.chat.StreamingChatLanguageModel = j

      def chat(
        messages: util.List[ChatMessage],
        handler:  StreamingChatResponseHandler
      ): Unit = j.chat(messages, handler)
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
  def chat(
    userMessage: String,
    handler:     StreamingChatResponseHandler
  ): Unit = {
    chat(singletonList(UserMessage.from(userMessage): ChatMessage), handler)
  }

  /** Generates a response from the model based on a message from a user.
    *
    * @param userMessage
    *   The message from the user.
    * @param handler
    *   The handler for streaming the response.
    */
  def chat(
    userMessage: UserMessage,
    handler:     StreamingChatResponseHandler
  ): Unit = {
    chat(singletonList(userMessage), handler)
  }

  /** Generates a response from the model based on a sequence of messages. Typically, the sequence contains messages in
    * the following order: System (optional) - User - AI - User - AI - User ...
    *
    * @param messages
    *   A list of messages.
    * @param handler
    *   The handler for streaming the response.
    */
  def chat(
    messages: util.List[ChatMessage],
    handler:  StreamingChatResponseHandler
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
  def chat(
    messages:           util.List[ChatMessage],
    toolSpecifications: util.List[ToolSpecification],
    handler:            StreamingChatResponseHandler
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
  def chat(
    messages:          util.List[ChatMessage],
    toolSpecification: ToolSpecification,
    handler:           StreamingChatResponseHandler
  ): Unit = {
    throw new IllegalArgumentException("Tools are currently not supported by this model")
  }

}
