package io.github.nerd.discordkt.discord.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author ashley
 * @since 5/19/17 1:59 AM
 */
@Embed.EmbedMarker
@JsonIgnoreProperties(ignoreUnknown = true) // todo thumbnail
class Embed internal constructor() {
	@JsonProperty("title") var title = ""
	@JsonProperty("color") var color: Int = 0
	@JsonProperty("description") var description = ""
	@JsonProperty("url") var url = ""

	@JsonProperty("type") val type = ""

	@JsonProperty("author") private var author: Author? = null
	@JsonProperty("footer") private var footer: Footer? = null
	@JsonProperty("fields") private val fields = arrayListOf<Field>()

	private fun <T : EmbedElement> initElem(elem: T, init: T.() -> Unit, set: (T) -> Unit): T {
		elem.init()
		set(elem)
		return elem
	}

	fun author(init: Author.() -> Unit) = initElem(Author(), init, { this@Embed.author = it })
	fun footer(init: Footer.() -> Unit) = initElem(Footer(), init, { this@Embed.footer = it })
	fun field(init: Field.() -> Unit) = initElem(Field(), init, { this@Embed.fields.add(it) })

	@Embed.EmbedMarker
	interface EmbedElement

	class Author internal constructor() : EmbedElement {
		@JsonProperty("name") var name = ""
		@JsonProperty("url") var url = ""
		@JsonProperty("icon_url") var icon = ""
		@JsonProperty("proxy_icon_url") val proxiedIconURL = ""
	}

	class Footer internal constructor() : EmbedElement {
		@JsonProperty("text") var text = ""
		@JsonProperty("icon_url") var icon = ""
		@JsonProperty("proxy_icon_url") val proxiedIconURL = ""
	}

	class Field internal constructor() : EmbedElement {
		@JsonProperty("name") var name = ""
		@JsonProperty("value")
		var description = ""
		@JsonProperty("inline") var inline = false
	}

	@DslMarker annotation class EmbedMarker
}

fun embed(init: Embed.() -> Unit): Embed {
	val e = Embed()
	e.init()
	return e
}