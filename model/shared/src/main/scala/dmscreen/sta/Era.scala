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

package dmscreen.sta

case class HistoricalEvent(
  year:        Int,
  description: String
)
val historicalEvents = List(
  HistoricalEvent(2024, "The Bell Riots, the Irish Reunification, the Europa Mission is launched"),
  HistoricalEvent(2053, "World War III ends"),
  HistoricalEvent(2063, "First contact between Humans and Vulcans"),
  HistoricalEvent(2064, "After first contact between Humans and Vulcans, but before Star Trek: Enterprise"),
  HistoricalEvent(2151, "First season of Star Trek: Enterprise"),
  HistoricalEvent(2152, "Second season of Star Trek: Enterprise"),
  HistoricalEvent(2153, "Third season of Star Trek: Enterprise"),
  HistoricalEvent(2154, "Fourth season of Star Trek: Enterprise"),
  HistoricalEvent(2155, "After the fourth season of Star Trek: Enterprise"),
  HistoricalEvent(2156, "The Earth-Romulan War begins"),
  HistoricalEvent(2157, "During the Earth-Romulan War"),
  HistoricalEvent(2160, "The Earth-Romulan War ends"),
  HistoricalEvent(2161, "The founding of the United Federation of Planets"),
  HistoricalEvent(2162, "After the events of Star Trek: Enterprise"),
  HistoricalEvent(2254, "The events of The Cage"),
  HistoricalEvent(2256, "First season of Star Trek: Discovery"),
  HistoricalEvent(2257, "First/second season of Star Trek: Discovery"),
  HistoricalEvent(2258, "Second season of Star Trek: Discovery"),
  HistoricalEvent(2259, "First season of Star Trek: Strange New Worlds"),
  HistoricalEvent(2260, "Second season of Star Trek: Strange New Worlds"),
  HistoricalEvent(2261, "After the second season of Star Trek: Strange New Worlds"),
  HistoricalEvent(
    2265,
    "The USS Enterprise is damaged at the galactic barrier; death of Gary Mitchell, Lee Kelso and others"
  ),
  HistoricalEvent(2266, "First season of Star Trek: The Original Series"),
  HistoricalEvent(2267, "Second season of Star Trek: The Original Series"),
  HistoricalEvent(2268, "Third season of Star Trek: The Original Series"),
  HistoricalEvent(2269, "First season of Star Trek: The Animated Series"),
  HistoricalEvent(2270, "Second season of Star Trek: The Animated Series"),
  HistoricalEvent(2271, "After the end of the original five year mission"),
  HistoricalEvent(2273, "The events of Star Trek: The Motion Picture"),
  HistoricalEvent(2274, "After the events of Star Trek: The Motion Picture"),
  HistoricalEvent(2285, "The events of Star Trek: The Wrath of Khan and The Search for Spock"),
  HistoricalEvent(2286, "The events of Star Trek: The Voyage Home"),
  HistoricalEvent(2287, "The events of Star Trek: The Final Frontier"),
  HistoricalEvent(2288, "After the events of Star Trek: The Final Frontier"),
  HistoricalEvent(2293, "The events of Star Trek: The Undiscovered Country, and launching of the Enterprise-B"),
  HistoricalEvent(2294, "After the launching of the Enterprise-B and the \"death\" of Kirk"),
  HistoricalEvent(2344, "The destruction of the Enterprise-C at Nerendra III"),
  HistoricalEvent(2345, "After the destruction of the USS Enterprise-C"),
  HistoricalEvent(2347, "During the Cardassian-Federation War"),
  HistoricalEvent(2353, "During the Cardassian-Federation War, death of Jack Crusher"),
  HistoricalEvent(2354, "During the Cardassian-Federation War"),
  HistoricalEvent(2355, "The destruction of the USS Stargazer in the Maxia-Zeta System"),
  HistoricalEvent(2356, "After the destruction of the USS Stargazer"),
  HistoricalEvent(2364, "First season of Star Trek: The Next Generation"),
  HistoricalEvent(2365, "Second season of Star Trek: The Next Generation"),
  HistoricalEvent(2366, "Third season of Star Trek: The Next Generation"),
  HistoricalEvent(2367, "Fourth season of Star Trek: The Next Generation, Klingon Civil War begins"),
  HistoricalEvent(2368, "Fifth season of Star Trek: The Next Generation, Klingon Civil War ends"),
  HistoricalEvent(2369, "Sixth season of Star Trek: The Next Generation, first season Star Trek: Deep Space Nine"),
  HistoricalEvent(2370, "Seventh season of Star Trek: The Next Generation, second season Star Trek: Deep Space Nine"),
  HistoricalEvent(
    2371,
    "The events of Star Trek: Generations (24th century), third season Star Trek: DS9, first season Star Trek: VOY"
  ),
  HistoricalEvent(2372, "Fourth season Star Trek: DS9, second season Star Trek: VOY. Federation-Klingon War begins"),
  HistoricalEvent(
    2373,
    "Fifth season Star Trek: DS9, third season Star Trek: VOY, Star Trek: First Contact. Federation-Klingon War ends. Dominion War begins."
  ),
  HistoricalEvent(2374, "Sixth season Star Trek: DS9, fourth season Star Trek: VOY"),
  HistoricalEvent(2375, "Seventh season Star Trek: DS9, fifth season Star Trek: VOY, Star Trek: Insurrection"),
  HistoricalEvent(2376, "Post Dominion War, sixth season Star Trek: VOY"),
  HistoricalEvent(2377, "Seventh season Star Trek: VOY"),
  HistoricalEvent(2378, "After the return of Voyager to the Alpha Quadrant"),
  HistoricalEvent(2379, "The events of Star Trek: Nemesis, the Romulan coup, death of Data"),
  HistoricalEvent(2380, "First season of Star Trek: Lower Decks"),
  HistoricalEvent(2381, "Second, third and fourth seasons of Star Trek: Lower Decks"),
  HistoricalEvent(2382, "After the fourth season of Star Trek: Lower Decks"),
  HistoricalEvent(2383, "First season of Star Trek: Prodigy"),
  HistoricalEvent(2384, "After the first season of Star Trek: Prodigy"),
  HistoricalEvent(2385, "The attack on the Utopia Planitia shipyards by artificial life forms"),
  HistoricalEvent(2386, "After the ban on artificial life forms"),
  HistoricalEvent(2387, "The destruction of Romulus"),
  HistoricalEvent(2388, "After the destruction of Romulus"),
  HistoricalEvent(2399, "First season of Star Trek: Picard"),
  HistoricalEvent(2400, "After the events of the first season of Star Trek: Picard"),
  HistoricalEvent(2401, "Second and third season of Star Trek: Picard"),
  HistoricalEvent(2402, "Epilogue of Star Trek: Picard season 3, lauching of the Enterprise-G"),
  HistoricalEvent(2403, "After the events of season 3 of Star Trek: Picard"),
  HistoricalEvent(2409, "After the events of season 3 of Star Trek: Picard; events of Star Trek Online begin"),
  HistoricalEvent(2410, "After the events of season 3 of Star Trek: Picard"),
  HistoricalEvent(3069, "The Burn destroys much of the galaxy's dilithium"),
  HistoricalEvent(3070, "After the events of the Burn"),
  HistoricalEvent(3188, "Third season of Star Trek: Discovery. Michael Burnham arrives in 32nd century"),
  HistoricalEvent(3189, "Third season of Star Trek: Discovery. The USS Discovery arrives in the 32nd century"),
  HistoricalEvent(3190, "Fourth season of Star Trek: Discovery"),
  HistoricalEvent(3191, "Fifth season of Star Trek Discovery"),
  HistoricalEvent(3192, "After the fifth season of Star Trek Discovery")
)

enum Era {

  case Enterprise,
    OriginalSeries,
    NextGeneration,
    PicardProdigy,
    Discovery,
    LowerDecks,
    Other

}
