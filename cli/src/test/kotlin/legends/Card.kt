package legends

data class Card(
    val name: String,
    val rarity: String = "",
    val type: String = "",
    val subtypes: List<String> = emptyList(),
    val cost: Int = -1,
    val power: Int = -1,
    val health: Int = -1,
    val set: Map<String, String> = emptyMap(),
    val collectible: Boolean = false,
    val soulSummon: Int = -1,
    val soulTrap: Int = -1,
    val text: String = "",
    val attributes: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val unique: Boolean = true,
    val imageUrl: String = "",
    val id: String = "",
    val code: String = ""
) {
    fun createCard(): tesl.model.Card {
        return tesl.model.Card(
            name, rarity, type, subtypes, cost, power, health, set, collectible, soulSummon, soulTrap, text, attributes, keywords, unique, imageUrl, id, code
        )
    }
}