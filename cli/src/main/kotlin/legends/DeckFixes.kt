package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck

object DeckFixes {
    fun fix(deck: Deck): Deck {
        val cards = deck.cards.map { card ->
            when(card.name) {
                "Moonmoth Castellan" -> Card(
                    name = card.name,
                    rarity = card.rarity,
                    type = card.type,
                    subtypes = listOf("Nord"), // Incorrectly showing Imperial in API
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
                "Mad Dash" -> Card(
                    name = card.name,
                    rarity = "Common",
                    type = card.type,
                    subtypes = card.subtypes,
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
                else -> card
            }
        }
        return Deck(cards)
    }


}
