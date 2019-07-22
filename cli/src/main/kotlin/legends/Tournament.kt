package legends

import kotlinx.serialization.Serializable

@Serializable
data class Tournament(
    val id: String,
    val description: String = "",
    val rules: MutableList<String> = mutableListOf()
) {
    override fun toString(): String {
        val head = """
            |Id: $id
            |Description: $description
            |Rules:
            |
        """.trimMargin()
        val rules = rules.joinToString("\n") { " - $it" }
        return head + rules
    }
}
