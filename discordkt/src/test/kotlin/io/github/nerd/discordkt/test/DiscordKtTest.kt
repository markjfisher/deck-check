package io.github.nerd.discordkt.test

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import io.github.nerd.discordkt.discord.auth.Authentication
import io.github.nerd.discordkt.discord.discord
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.events.*
import io.github.nerd.discordkt.discord.model.embed
import io.github.nerd.discordkt.discord.util.Status
import io.github.nerd.discordkt.discord.util.codeBlock
import io.github.nerd.discordkt.discordLogger
import java.io.File


/**
 * @author ashley
 * @since 5/14/17 5:21 PM
 */
object DiscordKtTest {
	@JvmStatic
	fun main(args: Array<String>) {
		val logbackLogger = discordLogger as Logger
		logbackLogger.level = Level.TRACE

		val botAuth = Authentication.bot(args[0])
		val userAuth = Authentication.user(args[1])

		val discord = discord(botAuth).get()

		discord.on<GuildCreateEvent> {
			println("guild lol ${it.guild.name}")
		}

		discord.on<ReactionAddEvent> {
			if (it.reaction.user != discord.me)
				it.reaction.message.reply(it.reaction.emoji.mention)

		}

		discord.on<ReactionRemoveEvent> {
			it.reaction.message.react(it.reaction.emoji)
		}

		discord.on<MessageUpdateEvent> {
			it.newMessage.reply(embed = embed {
				title = "Message edited"
				color = 0xFF6600

				field {
					name = "Old message"
					description = it.oldMessage.content.codeBlock()
				}

				field {
					name = "New message"
					description = it.newMessage.content.codeBlock()
				}

				footer {
					text = "Edited at ${it.newMessage.editedTime!!}"
				}
			})
		}

		discord.on<MessageDeleteEvent> {
			val message = it.message
			val b = message.channel?.messages?.has(message) ?: false
			message.reply(if (b) "Uh oh, the deleted message is still cached!"
			else "Phew, the deleted message isn't cached.", embed = embed {
				title = "Message deleted"
				if (message.channel is GuildTextChannel)
					description = "Message deleted in channel ${(message.channel as GuildTextChannel).mention}"
				color = 0xFF0000

				field {
					name = "Author"
					description = message.author.mention
				}

				field {
					name = "Message text"
					description = message.content.codeBlock()
				}
			})
		}

		discord.on<MessageRecvEvent> { event ->
			if (!event.message.invites.isEmpty()) {
				event.message.invites.forEach {
					it.thenApply {
						event.reply(embed = embed {
							title = "Invite"

							field {
								name = "Guild"
								description = it.guild.name
							}

							field {
								name = "Channel"
								description = "${it.channel.name} (${it.channel.type})"
							}

							it.inviter?.let { inviter ->
								author {
									name = inviter.fullName
									icon = inviter.avatarURL
								}
							}

						})
					}.exceptionally {
						it.printStackTrace()
						null
					}
				}
			}
			when (event.content) {
				"!ping" -> {
					event.reply("pong!")
				}
				"!owner" -> {
					val chan = event.channel
					if (chan is GuildTextChannel) {
						event.reply("tha owner is ${chan.guild.owner.user.mention}")
					} else {
						event.reply("this isn't a guild!")
					}
				}
				"!embed" -> {
					event.reply(embed = embed {
						title = "embed!!!"
						description = "Your wife says, \"Care isn't growing eyebrows.\"\n" +
								"You say, \"That's a puzzle.\"\n" +
								"Secretly, you're very excited to hear this news.\n" +
								"You're in the bathtub thinking about her."
						color = 0xFACADE
						url = "https://google.com/"

						author {
							name = "John Smith"
							url = "https://reddit.com/"
							icon = "https://images.genius.com/5433b7013185948f409fd123a7e5489f.1000x1000x1.jpg"
						}

						field {
							name = "field 1"
							description = "this is field one"
							inline = true
						}

						field {
							name = "field two"
							description = "this is field 2"
							inline = true
						}

						footer {
							text = "foooteeeerrrrr!!!!"
							icon = "https://images.rapgenius.com/0e02f90dbafc454188f2f1dda4d102dc.905x344x1.png"
						}
					})
				}
				"!av" -> event.reply(event.author.avatarURL)
				"!files" -> event.reply(files = arrayOf(fileAtHome("1.png"),
						fileAtHome("2.jpeg")))
				"!invis" -> {
					if (discord.me.presence.status == Status.INVISIBLE) {
						discord.me.online()
					} else {
						discord.me.invisible("Hide and Seek")
					}
				}
				"!status" -> event.reply("your status is: ${event.message.author.presence.status.title}")
				"!delet" -> event.delete()
				"!nick" -> {
					if (event.channel is GuildTextChannel) {
						val guild = (event.channel as GuildTextChannel).guild
						if (guild.me.nickname == null) {
							guild.me.nickname = "im cool ${discord.emojis["sunglasses"]}"
						} else {
							guild.me.nickname = ""
						}
					} else {
						event.reply("this isn't a guild!")
					}
				}
				"!perms" -> {
					if (event.channel is GuildTextChannel) {
						(event.channel as GuildTextChannel).guild.roles.forEach {
							println("${it.position}: ${it.name}")
						}
					}
				}
				"!has" -> {
					if (event.channel is GuildTextChannel) {
						val chan = event.channel as GuildTextChannel
						event.reply(chan.guild.me.allPermissions(chan)
								.joinToString("\n", "```", "```"))
						chan.parent.takeIf { it != null }
					}
				}
				"!react" -> discord.emojis["eyes"]?.let { event.react(it) }
				"!last" -> {
					event.channel?.lastMessage?.let {
						event.reply(embed = embed {
							title = "Last message"

							author {
								name = it.author.fullName
								icon = it.author.avatarURL
							}

							description = it.content

							footer {
								text = "Sent at ${it.timestamp}"
							}
						})
					}
				}
				"!changeavatar" -> discord.me.changeAvatar(fileAtHome("3.png"))
				"am i cool" -> event.reply(if (discord.application?.owner == event.author) "yes" else "no")
				"!application" -> discord.application?.let {
					event.reply(embed = embed {
						color = 0xe6607b

						author {
							name = discord.me.fullName
							icon = it.iconURL
						}

						field {
							name = "Owner"
							description = it.owner.fullName
						}

						field {
							name = "Application name"
							description = it.name
						}

						it.description?.let {
							field {
								name = "Application description"
								description = it
							}
						}

						it.icon?.let { _ ->
							field {
								name = "Application icon"
								description = it.iconURL
							}
						}
					})
				}
				"!pm" -> discord.privateChannels[event.author].thenAccept {
					it?.send("hey lol")
				}
			}
		}
	}

	private fun fileAtHome(fileName: String) = File(System.getProperty("user.home"), fileName)
}