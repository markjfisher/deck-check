package legends

import io.elderscrollslegends.Card
import io.elderscrollslegends.CardCache
import io.elderscrollslegends.Deck
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class RealAPITests {

    @Test
    @Disabled("Runs against live API")
    fun `missing cards test`() {
        CardCache.load()
        val deck = Deck.importCode("SPABvMAAAA")
        val da = DeckAnalysis(deck)

        Assertions.assertThat(da.creatureCount).isEqualTo(1)
    }

    @Test
    @Disabled("Runs against live API")
    fun `sorting cards for abell`() {
        println("Reading cards....")
        val cards = Card.all()
        println("... done")

        val greenCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Agility" }
        val yellowCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Willpower" }
        val redCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Strength" }
        val blueCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Intelligence" }
        val purpleCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Endurance" }
        val greyCards = cards.filter { it.attributes.size == 1 && it.attributes.first() == "Neutral" }

        val grCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Agility", "Strength") }
        val gyCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Agility", "Willpower") }
        val gbCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Agility", "Intelligence") }
        val gpCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Agility", "Endurance") }

        val yrCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Willpower", "Strength") }
        val ybCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Willpower", "Intelligence") }
        val ypCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Willpower", "Endurance") }

        val rbCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Strength", "Intelligence") }
        val rpCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Strength", "Endurance") }

        val bpCards = cards.filter { it.attributes.size == 2 && it.attributes.toHashSet() == setOf("Intelligence", "Endurance") }

        val gbr = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Agility", "Intelligence", "Strength")}
        val ryg = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Strength", "Willpower", "Agility")}
        val ryp = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Strength", "Willpower", "Endurance")}
        val bgp = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Intelligence", "Agility", "Endurance")}
        val byp = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Intelligence", "Willpower", "Endurance")}

        val byg = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Intelligence", "Willpower", "Agility")}
        val prb = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Endurance", "Strength", "Intelligence")}
        val gpr = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Agility", "Endurance", "Strength")}
        val ygp = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Willpower", "Agility", "Endurance")}
        val rby = cards.filter { it.attributes.size == 3 && it.attributes.toHashSet() == setOf("Strength", "Intelligence", "Willpower")}

        val archer = grCards
        val assassin = gbCards
        val battlemage = rbCards
        val crusader = yrCards
        val mage = ybCards
        val monk = gyCards
        val scout = gpCards
        val sorcerer = bpCards
        val spellsword = ypCards
        val warrior = rpCards

        val houseDagoth = gbr
        val houseHlaalu = ryg
        val houseRedoran = ryp
        val houseTelvanni = bgp
        val tribunalTemple = byp

        val aldemeriDominion = byg
        val daggerfallCovenant = prb
        val ebonheartPact = gpr
        val empireOfCyrodiil = ygp
        val guildsworn = rby

        listOf(greenCards, yellowCards, redCards, blueCards, purpleCards, greyCards).forEach { cs ->
            cs.sortedWith(compareBy<Card>{ it.cost }.thenBy { it.rarity }.thenBy { it.name })
                .forEach { card ->
                    println(String.format("%s|%d|%s|%s", card.name, card.cost, card.rarity, card.attributes.joinToString(",")))
                }
        }
        listOf(archer, assassin, battlemage, crusader, mage, monk, scout, sorcerer, spellsword, warrior).forEach { cs ->
            cs.sortedWith(compareBy<Card>{ it.cost }.thenBy { it.rarity }.thenBy { it.name })
                .forEach { card ->
                    println(String.format("%s|%d|%s|%s", card.name, card.cost, card.rarity, card.attributes.joinToString(",")))
                }
        }
        listOf(houseDagoth, houseHlaalu, houseRedoran, houseTelvanni, tribunalTemple).forEach { cs ->
            cs.sortedWith(compareBy<Card>{ it.cost }.thenBy { it.rarity }.thenBy { it.name })
                .forEach { card ->
                    println(String.format("%s|%d|%s|%s", card.name, card.cost, card.rarity, card.attributes.joinToString(",")))
                }
        }
        listOf(aldemeriDominion, daggerfallCovenant, ebonheartPact, empireOfCyrodiil, guildsworn).forEach { cs ->
            cs.sortedWith(compareBy<Card>{ it.cost }.thenBy { it.rarity }.thenBy { it.name })
                .forEach { card ->
                    println(String.format("%s|%d|%s|%s", card.name, card.cost, card.rarity, card.attributes.joinToString(",")))
                }
        }
    }

    @Test
    @Disabled("Runs against live API")
    fun `Find creatures without a subtype`() {
        println("Reading cards....")
        val cards = Card.all()
        println("... done")

        val nonSubtypedCards = cards.filter { it.type == "Creature" }
            .filter { it.subtypes.isEmpty() }

        nonSubtypedCards.forEach {
            println("${it.name} : https://api.elderscrollslegends.io/v1/cards?name=${it.name.replace(" ", "%20")}")
        }
    }
}