package legends

import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String,
    val description: String = "",
    val rules: MutableList<String> = mutableListOf(),
    val reasons: MutableList<String> = mutableListOf(),
    val players: MutableList<Player> = mutableListOf()
) {
    override fun toString(): String {
        return """
            |Id: $id
            |Description: $description
            |Players:
            |${players.joinToString("\n") { " - $it" }}
            |Rules:
            |${rules.joinToString("\n") { " - $it" }}
        """.trimMargin()
    }

    fun hasPlayer(ign: String)= players.any { it.name == ign }

    fun addPlayer(ign: String, deck: String) {
        removePlayer(ign)
        players.add(Player(name = ign, deck = deck))
    }

    fun removePlayer(ign: String) = players.removeAll { it.name == ign }
}

@Serializable
data class Player(
    val name: String,
    val deck: String
) {
    override fun toString(): String {
        return "$name, deck: $deck"
    }
}