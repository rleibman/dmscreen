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

enum TreasureTheme(description: String) {

  case arcana extends TreasureTheme("Gemstones plus magic items of an eldritch or esoteric nature")
  case armaments extends TreasureTheme("Coins or trade bars plus magic items that are useful in battle")
  case contraptions
      extends TreasureTheme("Implements: Coins, trade bars, or trade goods plus magic items that focus on utility")
  case relics extends TreasureTheme("Art objects plus magic items that have religious origins or purposes")
  case other extends TreasureTheme("Other") // Not really used

}

object Treasure {

  lazy val empty: Treasure = Treasure()

}

case class Treasure(
  cp:      Long = 0,
  sp:      Long = 0,
  ep:      Long = 0,
  gp:      Long = 0,
  pp:      Long = 0,
  items:   List[String] = List.empty,
  isHoard: Boolean = false
) {

  lazy val isEmpty:  Boolean = cp == 0 && sp == 0 && ep == 0 && gp == 0 && pp == 0 && items.isEmpty
  lazy val nonEmpty: Boolean = !isEmpty

  def +(other: Treasure): Treasure =
    Treasure(
      cp + other.cp,
      sp + other.sp,
      ep + other.ep,
      gp + other.gp,
      pp + other.pp,
      items ++ other.items,
      isHoard || other.isHoard
    )

}

enum TreasureRarity {

  case common, uncommon, rare, veryRare, legendary, artifact

}
