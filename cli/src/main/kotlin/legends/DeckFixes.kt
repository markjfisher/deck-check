package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck

object DeckFixes {
    fun fix(deck: Deck): Deck {
        val cards = deck.cards.map { card ->
            when(card.name) {
                "Moon Bishop" -> Card(
                    name = card.name,
                    rarity = card.rarity,
                    type = card.type,
                    subtypes = listOf("Khajiit"), // Has no subtypes in API
                    cost = card.cost,
                    power = card.power,
                    health = card.health,
                    set = card.set,
                    collectible = card.collectible,
                    soulSummon = card.soulSummon,
                    soulTrap = card.soulTrap,
                    text = card.text,
                    attributes = card.attributes,
                    keywords = card.keywords,
                    unique = card.unique,
                    imageUrl = card.imageUrl,
                    id = card.id
                )
                "Suthay Bootlegger" -> Card(
                    name = card.name,
                    rarity = card.rarity,
                    type = card.type,
                    subtypes = listOf("Khajiit"), // Has no subtypes in API
                    cost = card.cost,
                    power = card.power,
                    health = card.health,
                    set = card.set,
                    collectible = card.collectible,
                    soulSummon = card.soulSummon,
                    soulTrap = card.soulTrap,
                    text = card.text,
                    attributes = card.attributes,
                    keywords = card.keywords,
                    unique = card.unique,
                    imageUrl = card.imageUrl,
                    id = card.id
                )
                "Gavel of the Ordinator" -> Card(
                    name = card.name,
                    rarity = "Rare",
                    type = card.type,
                    subtypes = card.subtypes,
                    cost = card.cost,
                    power = card.power,
                    health = card.health,
                    set = card.set,
                    collectible = card.collectible,
                    soulSummon = "100",
                    soulTrap = "20",
                    text = card.text,
                    attributes = card.attributes,
                    keywords = card.keywords,
                    unique = card.unique,
                    imageUrl = card.imageUrl,
                    id = card.id
                )
                else -> card
            }
        }
        return Deck(cards)
    }


}
