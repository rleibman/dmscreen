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

import dev.langchain4j.data.message.ChatMessage

import java.util
import java.util.List

object ChatMemory {

  def fromJava(j: dev.langchain4j.memory.ChatMemory): ChatMemory =
    new ChatMemory() {
      def toJava: dev.langchain4j.memory.ChatMemory = j

      /** The ID of the {@link ChatMemory} .
        *
        * @return
        *   The ID of the {@link ChatMemory} .
        */
      override def id: AnyRef = j.id

      /** Adds a message to the chat memory.
        *
        * @param message
        *   The {@link ChatMessage} to add.
        */
      override def add(message: ChatMessage): Unit = j.add(message)

      /** Retrieves messages from the chat memory. Depending on the implementation, it may not return all previously
        * added messages, but rather a subset, a summary, or a combination thereof.
        *
        * @return
        *   A list of {@link ChatMessage} objects that represent the current state of the chat memory.
        */
      override def messages: util.List[ChatMessage] = j.messages()

      /** Clears the chat memory.
        */
      override def clear(): Unit = j.clear()
    }

}

given Conversion[ChatMemory, dev.langchain4j.memory.ChatMemory] = _.toJava

trait ChatMemory {

  def toJava: dev.langchain4j.memory.ChatMemory

  /** The ID of the {@link ChatMemory} .
    *
    * @return
    *   The ID of the {@link ChatMemory} .
    */
  def id: AnyRef

  /** Adds a message to the chat memory.
    *
    * @param message
    *   The {@link ChatMessage} to add.
    */
  def add(message: ChatMessage): Unit

  /** Retrieves messages from the chat memory. Depending on the implementation, it may not return all previously added
    * messages, but rather a subset, a summary, or a combination thereof.
    *
    * @return
    *   A list of {@link ChatMessage} objects that represent the current state of the chat memory.
    */
  def messages: util.List[ChatMessage]

  /** Clears the chat memory.
    */
  def clear(): Unit

}
