package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.CardSet
import io.elderscrollslegends.Deck

object DeckFixes {
    fun fix(deck: Deck): Deck {
        val cards = deck.cards.map { card ->
            when(card.name) {
                "Moon Bishop" -> copyCard(card, subtypes = listOf("Khajiit"))
                "Seeker of the Black Arts" -> copyCard(card, subtypes = listOf("Khajiit"))
                "Rebellion General" -> copyCard(card, subtypes = listOf("Khajiit"))
                "Suthay Bootlegger" -> copyCard(card, subtypes = listOf("Khajiit"))
                "Pouncing Senche" -> copyCard(card, subtypes = listOf("Khajiit"))
                "Queen's Captain" -> copyCard(card, subtypes = listOf("Khajiit"))

                "Gravesinger" -> copyCard(card, subtypes = listOf("Imperial"), keywords = emptyList())
                "Cauldron Keeper" -> copyCard(card, subtypes = listOf("Imperial"))

                "Brotherhood Assassin" -> copyCard(card, subtypes = listOf("Dark Elf"))
                "Imposter" -> copyCard(card, subtypes = listOf("Dark Elf"))

                "Master Swordsmith" -> copyCard(card, subtypes = listOf("Redguard"))

                "Vigilant of Stendarr" -> copyCard(card, subtypes = listOf("Breton"))

                "Gavel of the Ordinator" -> copyCard(card, rarity = "Rare", soulSummon = "100", soulTrap = "20")

                "Transmogrify" -> copyCard(card, type = "Action")

                "Duskfang" -> copyCard(card, type = "Item")
                else -> card
            }
        }
        return Deck(cards)
    }

    private fun copyCard(
        card: Card,
        name: String = card.name,
        rarity: String = card.rarity,
        type: String = card.type,
        subtypes: List<String> = card.subtypes,
        cost: Int = card.cost,
        power: Int = card.power,
        health: Int = card.health,
        set: CardSet = card.set,
        collectible: Boolean = card.collectible,
        soulSummon: String = card.soulSummon,
        soulTrap: String = card.soulTrap,
        text: String = card.text,
        attributes: List<String> = card.attributes,
        keywords: List<String> = card.keywords,
        unique: Boolean = card.unique,
        imageUrl: String = card.imageUrl,
        id: String = card.id
    ): Card {
        return Card(
            name = name,
            rarity = rarity,
            type = type,
            subtypes = subtypes,
            cost = cost,
            power = power,
            health = health,
            set = set,
            collectible = collectible,
            soulSummon = soulSummon,
            soulTrap = soulTrap,
            text = text,
            attributes = attributes,
            keywords = keywords,
            unique = unique,
            imageUrl = imageUrl,
            id = id
        )
    }

}
