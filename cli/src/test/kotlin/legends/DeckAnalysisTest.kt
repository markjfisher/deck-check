package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.CardSet
import io.elderscrollslegends.Deck
import legends.DeckAnalysis.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class DeckAnalysisTest {
    @Test
    fun `rarity counts`() {
        val commonCards = listOf(Card(name = "common card 1", rarity = "Common"))
        val rareCards = listOf(Card(name = "common card 2", rarity = "Rare"))
        val epicCards = listOf(Card(name = "common card 3", rarity = "Epic"))
        val legendaryCards = listOf(Card(name = "common card 3", rarity = "Legendary"))

        val deck = Deck(cards = listOf(commonCards, rareCards, epicCards, legendaryCards).flatten())
        val da = DeckAnalysis(deck)
        assertThat(da.byRarity["Common"]).isEqualTo(commonCards)
        assertThat(da.byRarity["Rare"]).isEqualTo(rareCards)
        assertThat(da.byRarity["Epic"]).isEqualTo(epicCards)
        assertThat(da.byRarity["Legendary"]).isEqualTo(legendaryCards)
    }

    @Test
    fun `all subtypes of a deck`() {
        val nord1 = Card(name = "nord card 1", subtypes = listOf("Nord"))
        val nord2 = Card(name = "nord card 2", subtypes = listOf("Nord"))
        val vamp = Card(name = "vampire card 1", subtypes = listOf("Vampire"))
        val charus = Card(name = "Chaurus card 1", subtypes = listOf("Chaurus"))

        val deck = Deck(cards = listOf(nord1, nord2, vamp, charus))
        val da = DeckAnalysis(deck)

        assertThat(da.subtypes).containsAll(listOf("Nord", "Vampire", "Chaurus"))
    }

    @Test
    fun `creatures actions items supports are filtered correctly`() {
        val legendaryCreature = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature", rarity = "Legendary")
        val commonAction1 = Card(name = "common action 1", type = "Action", rarity = "Common")
        val commonAction2 = Card(name = "common action 2", type = "Action", rarity = "Common")
        val rareAction1 = Card(name = "rare action 1", type = "Action", rarity = "Rare")
        val commonItem1 = Card(name = "common item 1", type = "Item", rarity = "Common")
        val commonItem2 = Card(name = "common item 2", type = "Item", rarity = "Common")
        val epicItem1 = Card(name = "epic item 1", type = "Item", rarity = "Epic")
        val rareSupport1 = Card(name = "rare support 1", type = "Support", rarity = "Rare")

        val deck = Deck(cards = listOf(legendaryCreature, commonAction1, commonAction1, commonAction2, rareAction1, commonItem1, commonItem2, commonItem2, epicItem1, rareSupport1, rareSupport1))
        val da = DeckAnalysis(deck)

        assertThat(da.creatures).containsOnly(legendaryCreature)
        assertThat(da.actions).containsOnly(commonAction1, commonAction2, rareAction1)
        assertThat(da.items).containsOnly(commonItem1, commonItem2, epicItem1)
        assertThat(da.supports).containsOnly(rareSupport1)

        assertThat(da.creaturesByRarity("Common")).isEmpty()
        assertThat(da.creaturesByRarity("Rare")).isEmpty()
        assertThat(da.creaturesByRarity("Epic")).isEmpty()
        assertThat(da.creaturesByRarity("Legendary")).containsOnly("creature 1")

        assertThat(da.actionsByRarity("Common")).containsOnly("common action 1", "common action 2")
        assertThat(da.actionsByRarity("Rare")).containsOnly("rare action 1")
        assertThat(da.actionsByRarity("Epic")).isEmpty()
        assertThat(da.actionsByRarity("Legendary")).isEmpty()

        assertThat(da.itemsByRarity("Common")).containsOnly("common item 1", "common item 2")
        assertThat(da.itemsByRarity("Rare")).isEmpty()
        assertThat(da.itemsByRarity("Epic")).containsOnly("epic item 1")
        assertThat(da.itemsByRarity("Legendary")).isEmpty()

        assertThat(da.supportsByRarity("Common")).isEmpty()
        assertThat(da.supportsByRarity("Rare")).containsOnly("rare support 1")
        assertThat(da.supportsByRarity("Epic")).isEmpty()
        assertThat(da.supportsByRarity("Legendary")).isEmpty()

        assertThat(da.raritiesOfType("Creature")).containsOnly("Legendary")
        assertThat(da.raritiesOfType("Action")).containsOnly("Common", "Rare")
        assertThat(da.raritiesOfType("Item")).containsOnly("Common", "Epic")
        assertThat(da.raritiesOfType("Support")).containsOnly("Rare")
    }

    @Test
    fun `creaturesOfSubtype retrieves correct cards`() {
        val creature1 = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature")
        val creature2 = Card(name = "creature 2", subtypes = listOf("Nord"), type = "Creature")
        val creature3 = Card(name = "creature 3", subtypes = listOf("Factotum"), type = "Creature")
        val creature4 = Card(name = "creature 4", subtypes = listOf("Vampire"), type = "Creature")
        val deck = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        val da = DeckAnalysis(deck)

        assertThat(da.creaturesOfSubtype("Nord")).containsOnly("creature 1", "creature 2")
        assertThat(da.creaturesOfSubtype("Factotum")).containsOnly("creature 3")
        assertThat(da.creaturesOfSubtype("Vampire")).containsOnly("creature 4")
    }

    @Test
    fun `is undead check`() {
        val creature1 = Card(name = "Tenarr Zalviit Lurker", subtypes = listOf("Khajiit", "Vampire"), type = "Creature")
        val creature2 = Card(name = "Death Hound", subtypes = listOf("Beast", "Vampire"), type = "Creature")
        val creature3 = Card(
            name = "Reflective Automaton",
            subtypes = listOf("Animal", "Argonian", "Ash Creature", "Beast", "Breton", "Centaur", "Chaurus", "Daedra", "Dark Elf", "Defense", "Dragon", "Dreugh", "Dwemer", "Elytra", "Fabricant", "Factotum", "Falmer", "Fish", "Gargoyle", "Giant", "Goblin", "God", "Grummite", "Harpy", "High Elf", "Imp", "Imperfect", "Imperial", "Insect", "Khajiit", "Kwama", "Lurcher", "Mammoth", "Mantikora", "Minotaur", "Mudcrab", "Mummy", "Nereid", "Netch", "Nord", "Ogre", "Orc", "Pastry", "Reachman", "Redguard", "Reptile", "Skeever", "Skeleton", "Spider", "Spirit", "Spriggan", "Troll", "Vampire", "Wamasu", "Werewolf", "Wolf", "Wood Elf", "Wraith"), type = "Creature"
        )
        val creature4 = Card(name = "Skeletal Dragon", subtypes = listOf("Dragon", "Skeleton"), type = "Creature")
        val deck1 = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        assertThat(DeckAnalysis(deck1).isUndead()).isTrue()

        val creature5 = Card(name = "Some vampire", subtypes = listOf("Vampire"), type = "Creature")
        val deck2 = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4, creature5))
        assertThat(DeckAnalysis(deck2).isUndead()).isTrue()

        val creature6 = Card(name = "Some other khajiit", subtypes = listOf("Khajiit"), type = "Creature")
        val deck3 = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4, creature5, creature6))
        assertThat(DeckAnalysis(deck3).isUndead()).isFalse()

    }

    @Test
    fun `cost to cards`() {
        val creature1 = Card(name = "creature 1", subtypes = listOf("Nord"), type = "Creature", cost = 0)
        val creature2 = Card(name = "creature 2", subtypes = listOf("Nord"), type = "Creature", cost = 0)
        val creature3 = Card(name = "creature 3", subtypes = listOf("Factotum"), type = "Creature", cost = 2)
        val creature4 = Card(name = "creature 4", subtypes = listOf("Vampire"), type = "Creature", cost = 4)
        val deck = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        val da = DeckAnalysis(deck)

        assertThat(da.costToCards[0]).containsExactlyInAnyOrder(creature1, creature2)
        assertThat(da.costToCards[1]).isEmpty()
        assertThat(da.costToCards[2]).containsExactlyInAnyOrder(creature3)
        assertThat(da.costToCards[3]).isEmpty()
        assertThat(da.costToCards[4]).containsExactlyInAnyOrder(creature4)
        (5..30).forEach { i ->
            assertThat(da.costToCards[i]).isEmpty()
        }

        assertThat(da.costs).containsExactly(0, 2, 4)

        assertThat(da.countByCost[0]).isEqualTo(3)
        assertThat(da.countByCost[1]).isEqualTo(0)
        assertThat(da.countByCost[2]).isEqualTo(2)
        assertThat(da.countByCost[3]).isEqualTo(0)
        assertThat(da.countByCost[4]).isEqualTo(1)
        (5..30).forEach { i ->
            assertThat(da.countByCost[i]).isEqualTo(0)
        }
    }

    @Test
    fun `Soul gem cost`() {
        val creature1 = Card(name = "creature 1", soulSummon = "100")
        val creature2 = Card(name = "creature 2", soulSummon = "200")
        val creature3 = Card(name = "creature 3", soulSummon = "300")
        val creature4 = Card(name = "creature 4", soulSummon = "")
        val deck = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        val da = DeckAnalysis(deck)

        assertThat(da.soulGemCost).isEqualTo(1000)
    }

    @Test
    fun `Non-integer soulSummon`() {
        val creature1 = Card(name = "creature 1", soulSummon = "Not an int")
        val deck = Deck(cards = listOf(creature1))
        val da = DeckAnalysis(deck)
        assertThat(da.soulGemCost).isEqualTo(0)
    }

    @Test
    fun `bySet returns unique cards mapped by set name`() {
        val creature1 = Card(name = "creature 1", set = CardSet(id = "s1"))
        val creature2 = Card(name = "creature 2", set = CardSet(id = "s1"))
        val creature3 = Card(name = "creature 3", set = CardSet(id = "s2"))
        val creature4 = Card(name = "creature 4", set = CardSet(id = "s3"))
        val deck = Deck(cards = listOf(creature1, creature1, creature2, creature3, creature3, creature4))
        val da = DeckAnalysis(deck)

        assertThat(da.bySet("s1")).containsExactlyInAnyOrder(creature1, creature2)
        assertThat(da.bySet("s2")).containsExactlyInAnyOrder(creature3)
        assertThat(da.bySet("s3")).containsExactlyInAnyOrder(creature4)
        assertThat(da.bySet("s4")).isEmpty()
    }

    @Test
    fun `mana to card count for curve`() {
        val creature1a = Card(name = "creature 1a", cost = 0)
        val creature1b = Card(name = "creature 1b", cost = 0)
        val creature2a = Card(name = "creature 2a", cost = 1)
        val creature2b = Card(name = "creature 2b", cost = 1)
        val creature3a = Card(name = "creature 3a", cost = 2)
        val creature4a = Card(name = "creature 4a", cost = 3)
        val creature7 = Card(name = "creature 7", cost = 7)
        val creature8 = Card(name = "creature 8", cost = 8)
        val creature9 = Card(name = "creature 9", cost = 9)
        val deck = Deck(cards = listOf(creature1a, creature1a, creature1b, creature2a, creature2b, creature3a, creature3a, creature4a, creature7, creature7, creature8, creature9))
        val da = DeckAnalysis(deck)

        // Actual costs
        val costToCountMap = da.costToCountMap
        assertThat(costToCountMap[0]).isEqualTo(3)
        assertThat(costToCountMap[1]).isEqualTo(2)
        assertThat(costToCountMap[2]).isEqualTo(2)
        assertThat(costToCountMap[3]).isEqualTo(1)
        assertThat(costToCountMap[7]).isEqualTo(2)
        assertThat(costToCountMap[8]).isEqualTo(1)
        assertThat(costToCountMap[9]).isEqualTo(1)

        // Grouped for curve
        val manaToCardCount = da.manaCurve
        assertThat(manaToCardCount[0]).isEqualTo(3)
        assertThat(manaToCardCount[1]).isEqualTo(2)
        assertThat(manaToCardCount[2]).isEqualTo(2)
        assertThat(manaToCardCount[3]).isEqualTo(1)
        assertThat(manaToCardCount[7]).isEqualTo(4)
    }

    @Test
    fun `sorted cards list comes back by cost then name`() {
        val c1 = Card(name = "b0", cost = 0)
        val c2 = Card(name = "a0", cost = 0)
        val c3 = Card(name = "a1", cost = 1)
        val c4 = Card(name = "b1", cost = 1)
        val c5 = Card(name = "c2", cost = 2)
        val c6 = Card(name = "a2", cost = 2)
        val c7 = Card(name = "b2", cost = 2)
        val c8 = Card(name = "x3", cost = 3)
        val c9 = Card(name = "m4", cost = 4)
        val deck = Deck(cards = listOf(c1, c1, c2, c2, c3, c4, c5, c5, c6, c7, c8, c9))

        assertThat(DeckAnalysis(deck).cardCountSorted).isEqualTo(
            listOf(
                CardCount(2, c2),
                CardCount(2, c1),
                CardCount(1, c3),
                CardCount(1, c4),
                CardCount(1, c6),
                CardCount(1, c7),
                CardCount(2, c5),
                CardCount(1, c8),
                CardCount(1, c9)
            )
        )
    }

    @Test
    fun `prophecy count`() {
        val c1 = Card(name = "1", keywords = listOf("x", "Prophecy"))
        val c2 = Card(name = "2", keywords = listOf("x", ""))
        val c3 = Card(name = "3", keywords = listOf("x", "Prophecy"))
        val c4 = Card(name = "4", keywords = listOf("x", ""))
        val c5 = Card(name = "5", keywords = listOf("x", "Prophecy"))
        val c6 = Card(name = "6", keywords = listOf("x", ""))
        val c7 = Card(name = "7", keywords = listOf("x", "Prophecy"))
        val c8 = Card(name = "8", keywords = listOf("x", ""))
        val c9 = Card(name = "9", keywords = listOf("x", "Prophecy"))
        val deck = Deck(cards = listOf(c1, c1, c2, c2, c3, c4, c5, c5, c6, c7, c8, c9))

        assertThat(DeckAnalysis(deck).prophecyCount).isEqualTo(7)
    }

    @Test
    fun `attributes amalgamated from all cards`() {
        val c1 = Card(name = "1", attributes = listOf("Strength"))
        val c2 = Card(name = "2", attributes = listOf("Agility"))
        val c3 = Card(name = "3", attributes = listOf("Intelligence"))
        val c4 = Card(name = "4", attributes = listOf("Strength", "Agility"))
        val c5 = Card(name = "5", attributes = listOf("Strength", "Intelligence"))
        val c6 = Card(name = "6", attributes = listOf("Agility", "Intelligence"))
        val c7 = Card(name = "7", attributes = listOf("Strength", "Agility", "Intelligence"))
        val c8 = Card(name = "8")
        val c9 = Card(name = "9")
        val deck = Deck(cards = listOf(c1, c1, c2, c2, c3, c4, c5, c5, c6, c7, c8, c9))

        assertThat(DeckAnalysis(deck).attributes).containsExactlyEntriesOf(mapOf("Strength" to 6, "Agility" to 5, "Intelligence" to 5))
    }

    @Test
    fun `keywords amalgamated from all cards`() {
        val c1 = Card(name = "1", keywords = listOf("k1"))
        val c2 = Card(name = "2", keywords = listOf("k2"))
        val c3 = Card(name = "3", keywords = listOf("k3"))
        val c4 = Card(name = "4", keywords = listOf("k1", "k2"))
        val c5 = Card(name = "5", keywords = listOf("k1", "k3"))
        val c6 = Card(name = "6", keywords = listOf("k2", "k3"))
        val c7 = Card(name = "7", keywords = listOf("k1", "k2", "k3"))
        val c8 = Card(name = "8")
        val c9 = Card(name = "9")
        val deck = Deck(cards = listOf(c1, c1, c2, c2, c3, c4, c5, c5, c6, c7, c8, c9))

        assertThat(DeckAnalysis(deck).keywords).containsExactlyInAnyOrder("k1", "k2", "k3")
    }

    @Test
    fun `deck class tests`() {
        val cRed = Card(name = "red", attributes = listOf("Strength"))
        val cBlue = Card(name = "blue", attributes = listOf("Intelligence"))
        val cYellow = Card(name = "yellow", attributes = listOf("Willpower"))
        val cPurple = Card(name = "purple", attributes = listOf("Endurance"))
        val cGreen = Card(name = "green", attributes = listOf("Agility"))
        val cGray = Card(name = "gray", attributes = listOf("Neutral"))
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SINGLE_RED)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SINGLE_BLUE)
        assertThat(DeckAnalysis(Deck(cards = listOf(cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SINGLE_YELLOW)
        assertThat(DeckAnalysis(Deck(cards = listOf(cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SINGLE_PURPLE)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SINGLE_GREEN)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGray))).deckClass).isEqualTo(DeckAnalysis.DeckClass.NEUTRAL)

        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.ARCHER)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cBlue))).deckClass).isEqualTo(DeckAnalysis.DeckClass.ASSASSIN)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.BATTLEMAGE)
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.CRUSADER)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.MAGE)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.MONK)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SCOUT)
        assertThat(DeckAnalysis(Deck(cards = listOf(cPurple, cBlue))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SORCERER)
        assertThat(DeckAnalysis(Deck(cards = listOf(cPurple, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.SPELLSWORD)
        assertThat(DeckAnalysis(Deck(cards = listOf(cPurple, cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.WARRIOR)

        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cBlue, cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.HOUSE_DAGOTH)
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed, cYellow, cGreen))).deckClass).isEqualTo(DeckAnalysis.DeckClass.HOUSE_HLAALU)
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed, cYellow, cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.HOUSE_REDORAN)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cGreen, cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.HOUSE_TELVANNI)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cYellow, cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.TRIBUNAL_TEMPLE)

        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cYellow, cGreen))).deckClass).isEqualTo(DeckAnalysis.DeckClass.ALDEMERI_DOMINION)
        assertThat(DeckAnalysis(Deck(cards = listOf(cPurple, cRed, cBlue))).deckClass).isEqualTo(DeckAnalysis.DeckClass.DAGGERFALL_COVENANT)
        assertThat(DeckAnalysis(Deck(cards = listOf(cGreen, cPurple, cRed))).deckClass).isEqualTo(DeckAnalysis.DeckClass.EBONHEART_PACT)
        assertThat(DeckAnalysis(Deck(cards = listOf(cYellow, cGreen, cPurple))).deckClass).isEqualTo(DeckAnalysis.DeckClass.EMPIRE_OF_CYRODIIL)
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed, cBlue, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.GUILDSWORN)

        // different order, with neutral
        assertThat(DeckAnalysis(Deck(cards = listOf(cRed, cGreen, cGray))).deckClass).isEqualTo(DeckAnalysis.DeckClass.ARCHER)
        assertThat(DeckAnalysis(Deck(cards = listOf(cBlue, cRed, cGray, cYellow))).deckClass).isEqualTo(DeckAnalysis.DeckClass.GUILDSWORN)

    }

    @Test
    fun `class ability from colour`() {
        assertThat(ClassAbility.fromColour(ClassColour.GREEN)).isEqualTo(ClassAbility.AGILITY)
        assertThat(ClassAbility.fromColour(ClassColour.RED)).isEqualTo(ClassAbility.STRENGTH)
        assertThat(ClassAbility.fromColour(ClassColour.BLUE)).isEqualTo(ClassAbility.INTELLIGENCE)
        assertThat(ClassAbility.fromColour(ClassColour.YELLOW)).isEqualTo(ClassAbility.WILLPOWER)
        assertThat(ClassAbility.fromColour(ClassColour.PURPLE)).isEqualTo(ClassAbility.ENDURANCE)
    }
}