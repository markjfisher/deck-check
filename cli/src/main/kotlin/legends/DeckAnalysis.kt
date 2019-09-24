package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.Deck

import legends.DeckAnalysis.ClassColour.*
import java.lang.Exception

class DeckAnalysis(private val deck: Deck) {
    val byRarity: Map<String, List<Card>>
    val deckClassName: String
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
        val deckClass = DeckClass
            .values()
            .find { it.classColours == withoutNeutral } ?: DeckClass.NEUTRAL

        deckClassName = deckClass
            .name
            .replace("_", " ")
            .toLowerCase()
            .split(" ")
            .joinToString(" ") { it.capitalize() }

        attributesText = groupAndCountAsText { it.attributes }
        keywordsText = groupAndCountAsText { it.keywords }

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

        subtypes = deck.cards.map { it.subtypes }.flatten().toHashSet().toList()

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

    private fun groupAndCountAsText(mapper: (card: Card) -> List<String>): String {
        return deck
            .cards
            .flatMap { mapper(it) }
            .groupBy { it }
            .map { (k, v) -> k to v.size }
            .toMap()
            .map { (name, count) ->
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

    enum class ClassColour {
        GREEN,
        RED,
        BLUE,
        YELLOW,
        PURPLE,
        GREY
    }

    enum class ClassAbility(val classColour: ClassColour) {
        AGILITY(GREEN),
        STRENGTH(RED),
        INTELLIGENCE(BLUE),
        WILLPOWER(YELLOW),
        ENDURANCE(PURPLE),
        NEUTRAL(GREY)
    }

    enum class DeckClass(val classColours: Set<ClassColour>) {
        NEUTRAL(emptySet()),

        SINGLE_GREEN(setOf(GREEN)),
        SINGLE_RED(setOf(RED)),
        SINGLE_BLUE(setOf(BLUE)),
        SINGLE_YELLOW(setOf(YELLOW)),
        SINGLE_PURPLE(setOf(PURPLE)),

        ARCHER(setOf(GREEN, RED)),
        ASSASSIN(setOf(GREEN, BLUE)),
        BATTLEMAGE(setOf(BLUE, RED)),
        CRUSADER(setOf(RED, YELLOW)),
        MAGE(setOf(BLUE, YELLOW)),
        MONK(setOf(GREEN, YELLOW)),
        SCOUT(setOf(GREEN, PURPLE)),
        SORCERER(setOf(PURPLE, BLUE)),
        SPELLSWORD(setOf(PURPLE, YELLOW)),
        WARRIOR(setOf(PURPLE, RED)),

        HOUSE_DAGOTH(setOf(GREEN, BLUE, RED)),
        HOUSE_HLAALU(setOf(RED, YELLOW, GREEN)),
        HOUSE_REDORAN(setOf(RED, YELLOW, PURPLE)),
        HOUSE_TELVANNI(setOf(BLUE, GREEN, PURPLE)),
        TRIBUNAL_TEMPLE(setOf(BLUE, YELLOW, PURPLE)),

        ALDEMERI_DOMINION(setOf(BLUE, YELLOW, GREEN)),
        DAGGERFALL_COVENANT(setOf(PURPLE, RED, BLUE)),
        EBONHEART_PACT(setOf(GREEN, PURPLE, RED)),
        EMPIRE_OF_CYRODIIL(setOf(YELLOW, GREEN, PURPLE)),
        GUILDSWORN(setOf(RED, BLUE, YELLOW))
    }


}