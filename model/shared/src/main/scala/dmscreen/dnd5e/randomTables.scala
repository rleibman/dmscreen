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

package dmscreen.dnd5e

import dmscreen.DiceRoll

opaque type RandomTableId = Long

object RandomTableId {

  def empty: RandomTableId = RandomTableId(0)

  def apply(randomTableId: Long): RandomTableId = randomTableId

  extension (randomTableId: RandomTableId) {

    def value: Long = randomTableId

  }

}

enum RandomTableType {

  case treasure, environment, npcs, encounter, adventure, other

}

case class RandomTable(
  id:        RandomTableId,
  name:      String,
  tableType: RandomTableType,
  subType:   String,
  diceRoll:  DiceRoll,
  entries:   List[RandomTableEntry]
) {

  def findEntry(roll: Int): Option[RandomTableEntry] = entries.find(e => e.rangeLow <= roll && roll <= e.rangeHigh)

}

case class RandomTableEntry(
  randomTableId: RandomTableId,
  rangeLow:      Int,
  rangeHigh:     Int,
  name:          String,
  description:   String
)
