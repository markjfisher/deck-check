package io.github.nerd.discordkt.discord.util

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer

/**
 * @author ashley
 * @since 5/18/17 9:36 PM
 */
internal class PermissionsDeserializer : StdDeserializer<Array<Permissions>>(Array<Permissions>::class.java) {
	override fun deserialize(p0: JsonParser?, p1: DeserializationContext?): Array<Permissions> {
		try {
			val l = p0!!.text.toLong()
			if (l == 0.toLong()) return arrayOf()
			return Permissions.getPermsFromNumber(l).toTypedArray()
		} catch (e: Exception) {
		}
		return arrayOf()
	}
}