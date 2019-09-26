package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck

import legends.DeckAnalysis.ClassColour.*
import java.awt.Color
import java.lang.Exception

class DeckAnalysis(private val deck: Deck) {
    companion object {
        const val colourAlpha = 0x2f
    }

    val byRarity: Map<String, List<Card>>
    val deckClass: DeckClass
    val deckClassName: String

    val attributes: Map<String, Int>
    val keywords: List<String>
    val attributesText: String
    val keywordsText: String
    val legendaryCount: Int
    val epicCount: Int
    val rareCount: Int
    val commonCount: Int
    val creaturesMap: Map<String, CardCount>
    val actionsMap: Map<String, CardCount>
    val itemsMap: Map<String, CardCount>
    val supportsMap: Map<String, CardCount>
    val creatures: List<Card>
    val actions: List<Card>
    val items: List<Card>
    val supports: List<Card>
    val creatureCount: Int
    val actionsCount: Int
    val itemsCount: Int
    val supportsCount: Int
    val prophecyCount: Int
    val c1: Int
    val c2: Int
    val c3: Int
    val totalUnique: Int
    val totalCards: Int
    val costToCountMap: Map<Int, Int>
    val manaCurve: Map<Int, Int>
    val subtypes: List<String>
    val costToCards: Map<Int, List<Card>>
    val costs: List<Int>
    val countByCost: Map<Int, Int>
    val setToCards: Map<String, List<Card>>
    val soulGemCost: Int
    val cardCountSorted: List<CardCount>

    init {
        byRarity = deck.cards
            .sortedBy { it.name }
            .groupBy { it.rarity }
            .map { (rarity, cards) ->
                rarity to cards
            }
            .toMap()

        val allClassColours = deck
            .cards
            .flatMap { it.attributes }
            .toSet()
            .map { ClassAbility.valueOf(it.toUpperCase()).classColour }
            .toSet()

        val withoutNeutral = allClassColours - GREY
        deckClass = DeckClass
            .values()
            .find { it.classColours.containsAll(withoutNeutral) } ?: DeckClass.NEUTRAL

        deckClassName = deckClass
            .name
            .replace("_", " ")
            .toLowerCase()
            .split(" ")
            .joinToString(" ") { it.capitalize() }

        attributes = groupAndCount { it.attributes }
        keywords = deck.cards.flatMap { it.keywords }.toHashSet().toList().sorted()
        attributesText = mapToText(groupAndCount { it.attributes })
        keywordsText = mapToText(groupAndCount { it.keywords })

        legendaryCount = byRarity["Legendary"]?.size ?: 0
        epicCount = byRarity["Epic"]?.size ?: 0
        rareCount = byRarity["Rare"]?.size ?: 0
        commonCount = byRarity["Common"]?.size ?: 0

        creaturesMap = byType("Creature")
        actionsMap = byType("Action")
        itemsMap = byType("Item")
        supportsMap = byType("Support")

        creatures = creaturesMap.values.map { it.card }
        actions = actionsMap.values.map { it.card }
        items = itemsMap.values.map { it.card }
        supports = supportsMap.values.map { it.card }

        creatureCount = creaturesMap.map { it.value.count }.sum()
        actionsCount = actionsMap.map { it.value.count }.sum()
        itemsCount = itemsMap.map { it.value.count }.sum()
        supportsCount = supportsMap.map { it.value.count }.sum()

        prophecyCount = deck
            .cards
            .flatMap { it.keywords }
            .groupBy { it }
            .map { (k, v) -> k to v.size }
            .toMap()["Prophecy"] ?: 0

        c1 = deck.of(1).size
        c2 = deck.of(2).size
        c3 = deck.of(3).size

        totalUnique = c1 + c2 + c3
        totalCards = c1 + c2 * 2 + c3 * 3

        costToCountMap = deck.cards
            .groupBy { it.cost }
            .toSortedMap()
            .map { entry ->
                val cost = entry.key
                val count = entry.value
                    .groupBy { card -> card.name }
                    .map { it.value.size }
                    .sum()
                (cost to count)
            }
            .toMap()

        manaCurve = (0..30).fold(mutableMapOf(), { acc, cost ->
            val x = if (cost < 8) cost else 7
            val y = costToCountMap[cost] ?: 0
            val sevenPlus = acc.getOrDefault(7, 0)
            acc[x] = sevenPlus + y
            acc
        })

        subtypes = deck.cards.map { it.subtypes }.flatten().toHashSet().toList().sorted()

        costs = deck.cards.map { it.cost }.toHashSet().toList().sorted()

        costToCards = (0..30).fold(mutableMapOf<Int, List<Card>>(), { acc, cost ->
            acc[cost] = deck.cards.filter { it.cost == cost }.toHashSet().toList().sortedBy { it.name }
            acc
        }).toMap()

        countByCost = (0..30).fold(mutableMapOf<Int, Int>(), { acc, cost ->
            acc[cost] = deck.cards.filter { it.cost == cost }.size
            acc
        }).toMap()

        setToCards = deck.cards
            .groupBy { it.set.id }
            .map { (set, cards) ->
                (set to cards.toHashSet().toList().sortedBy { it.name })
            }
            .toMap()

        soulGemCost = deck.cards.map {
            if (it.soulSummon.isEmpty()) 0
            else { try { it.soulSummon.toInt() } catch (_: Exception) { 0 } }
        }.sum()

        // Sorts all the cards by cost, then name, and then groups the same card into a count to give List<CardCount>
        // so that each card is represented only once in the list, but its count is still captured in the ordering
        cardCountSorted = deck.cards
            .sortedWith(compareBy<Card>{ it.cost }.thenBy { it.name })
            .groupBy { Pair(it.cost, it.name) }
            .map {
                CardCount(count = it.value.size, card = it.value.first())
            }

    }

    fun creaturesByRarity(rarity: String): List<String> = creatures.filter { it.rarity == rarity }.map { it.name }
    fun actionsByRarity(rarity: String): List<String> = actions.filter { it.rarity == rarity }.map { it.name }
    fun itemsByRarity(rarity: String): List<String> = items.filter { it.rarity == rarity }.map { it.name }
    fun supportsByRarity(rarity: String): List<String> = supports.filter { it.rarity == rarity }.map { it.name }
    fun bySet(id: String): List<Card> = setToCards.getOrDefault(id, emptyList())

    fun raritiesOfType(type: String): List<String> =
        byType(type).values.map { it.card.rarity }.toHashSet().toList().sorted()

    fun creaturesOfSubtype(type: String): List<String> =
        creatures.filter { it.subtypes == listOf(type) }.map { it.name }

    fun isUndead(): Boolean {
        // check that every card contains at least one of the undead types
        val undeadTypes = setOf("Skeleton", "Spirit", "Vampire", "Mummy")
        return creatures.all { card ->
            card.subtypes.any { undeadTypes.contains(it) }
        }
    }

    data class CardCount(
        val count: Int,
        val card: Card
    )

    private fun groupAndCount(mapper: (card: Card) -> List<String>): Map<String, Int> {
        return deck
            .cards
            .flatMap { mapper(it) }
            .groupBy { it }
            .map { (k, v) -> k to v.size }
            .toMap()
    }

    private fun mapToText(map: Map<String, Int>): String {
        return map.map { (name, count) ->
            "$name: $count"
        }.joinToString(", ")
    }

    private fun byType(type: String): Map<String, CardCount> {
        return deck.cards
            .filter { it.type == type }
            .sortedBy { it.name }
            .groupBy { it.name }
            .map { (name, typeCards) ->
                val first = typeCards.first()
                name to CardCount(
                    count = typeCards.size,
                    card = first
                )
            }
            .toMap()
    }

    fun createManaString(): String {
        val maxValue = manaCurve.values.max()!!
        val maxValueLength = "$maxValue".length
        val increment = maxValue / 20.0
        val maxLabelLength = 4

        return manaCurve.map { (cost, count) ->
            val barChunks = ((count * 8) / increment).toInt().div(8)
            val remainder = ((count * 8) / increment).toInt().rem(8)

            var bar = "█".repeat(barChunks)
            if (remainder > 0) {
                bar += ('█'.toInt() + (8 - remainder)).toChar()
            }
            if (bar == "") {
                bar = "▏"
            }

            val costText = if (cost < 7) "$cost" else "7+"
            " ${costText.padEnd(maxLabelLength)}| ${count.toString().padEnd(maxValueLength)} $bar"
        }.joinToString("\n")

    }


    enum class ClassColour(val hexColor: Color) {
        GREEN(Color(0x00, 0x80, 0x00, colourAlpha)),
        RED(Color(0x80, 0x00, 0x00, colourAlpha)),
        BLUE(Color(0x00, 0x00, 0x80, colourAlpha)),
        YELLOW(Color(0xff, 0xff, 0x00, colourAlpha)),
        PURPLE(Color(0x80, 0x00, 0x80, colourAlpha)),
        GREY(Color(0xc0, 0xc0, 0xc0, colourAlpha))
    }

    enum class ClassAbility(val classColour: ClassColour) {
        AGILITY(GREEN),
        STRENGTH(RED),
        INTELLIGENCE(BLUE),
        WILLPOWER(YELLOW),
        ENDURANCE(PURPLE),
        NEUTRAL(GREY);

        companion object {
            fun fromColour(classColour: ClassColour): ClassAbility {
                return values().find { it.classColour == classColour } ?: NEUTRAL
            }
        }
    }

    enum class DeckClass(val classColours: List<ClassColour>) {
        NEUTRAL(emptyList()),

        SINGLE_GREEN(listOf(GREEN)),
        SINGLE_RED(listOf(RED)),
        SINGLE_BLUE(listOf(BLUE)),
        SINGLE_YELLOW(listOf(YELLOW)),
        SINGLE_PURPLE(listOf(PURPLE)),

        ARCHER(listOf(GREEN, RED)),
        ASSASSIN(listOf(GREEN, BLUE)),
        BATTLEMAGE(listOf(BLUE, RED)),
        CRUSADER(listOf(RED, YELLOW)),
        MAGE(listOf(BLUE, YELLOW)),
        MONK(listOf(GREEN, YELLOW)),
        SCOUT(listOf(GREEN, PURPLE)),
        SORCERER(listOf(PURPLE, BLUE)),
        SPELLSWORD(listOf(PURPLE, YELLOW)),
        WARRIOR(listOf(PURPLE, RED)),

        HOUSE_DAGOTH(listOf(GREEN, BLUE, RED)),
        HOUSE_HLAALU(listOf(RED, YELLOW, GREEN)),
        HOUSE_REDORAN(listOf(RED, YELLOW, PURPLE)),
        HOUSE_TELVANNI(listOf(BLUE, GREEN, PURPLE)),
        TRIBUNAL_TEMPLE(listOf(BLUE, YELLOW, PURPLE)),

        ALDEMERI_DOMINION(listOf(BLUE, YELLOW, GREEN)),
        DAGGERFALL_COVENANT(listOf(PURPLE, RED, BLUE)),
        EBONHEART_PACT(listOf(GREEN, PURPLE, RED)),
        EMPIRE_OF_CYRODIIL(listOf(YELLOW, GREEN, PURPLE)),
        GUILDSWORN(listOf(RED, BLUE, YELLOW))
    }


}