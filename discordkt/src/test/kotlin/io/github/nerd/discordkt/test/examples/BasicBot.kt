package io.github.nerd.discordkt.test.examples

import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.discord
import io.github.nerd.discordkt.discord.events.MessageRecvEvent

/**
 * @author ashley
 * @since 6/25/17 2:30 AM
 * This class defines a simple bot as an example.
 */
object BasicBot {
	@JvmStatic
	fun main(args: Array<String>) {
		// let's create a discord instance with a bot token and connect.
		val discord = discord(Authentication.bot(args.first()))
				.get() // wait for the bot to connect using the Future...

		// okay, so let's listen for some incoming messages.
		discord.on<MessageRecvEvent> { event ->
			// here, we register a listener
			if (event.content.startsWith("!ping", ignoreCase = true)) { // check if the message starts with "!ping"
				// if it does, send a message with the text "Pong!," followed by the ping_pong emoji, in the same channel.
				event.reply("Pong! ${discord.emojis["ping_pong"]}")
			}
		}

		// that's it!
	}
}