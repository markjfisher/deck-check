package io.github.nerd.discordkt.discord.entity.channel

import com.fasterxml.jackson.annotation.JsonValue
import io.github.nerd.discordkt.discord.Discord
import io.github.nerd.discordkt.discord.entity.Message
import io.github.nerd.discordkt.discord.entity.guild.channel.GuildTextChannel
import io.github.nerd.discordkt.discord.exception.ensurePermission
import io.github.nerd.discordkt.discord.exception.ensureThat
import io.github.nerd.discordkt.discord.model.Embed
import io.github.nerd.discordkt.discord.model.entity.ModelBasicMessage
import io.github.nerd.discordkt.discord.model.entity.ModelMessage
import io.github.nerd.discordkt.discord.util.IdentifiableCache
import io.github.nerd.discordkt.discord.util.Permissions
import io.github.nerd.discordkt.mapper
import io.github.nerd.discordkt.tika
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File
import java.util.concurrent.CompletableFuture

/**
 * @author ashley
 * @since 5/24/17 10:40 PM
 */
abstract class TextChannel(override val id: Long,
                           protected val discord: Discord) : Channel {
	/**
	 * The last message sent in this channel
	 */
	val lastMessage: Message?
		get() = messages.firstOrNull()

	/**
	 * A cache of all messages in this channel
	 */
	val messages = MessageCache(this.id, this.discord)

	/**
	 * All pinned messages in this channel.
	 */
	val pins = object : IdentifiableCache<Message>() {
		override val values: List<Message>
			get() = super.values.sortedBy { it.timestamp }.asReversed()
	}

	/**
	 * Sends a message to this channel
	 */
	fun send(content: String = "",
	         tts: Boolean = false,
	         files: Array<File> = arrayOf(),
	         embed: Embed? = null): CompletableFuture<Message> {
		if (this is DirectPrivateChannel && discord.me.isBot)
			ensureThat(!this.recipient.isBot, "You cannot private message other bots.")
		ensureThat(!content.isBlank() || files.isNotEmpty() || null != embed,
				"Cannot send an empty message.")
		ensureThat(content.length <= 2000, "You cannot send messages of more than 2000 characters.")
		ensureThat(files.size <= 10, "You can only upload a maximum of 10 files in one message.")
		if (this is GuildTextChannel) {
			this.guild.ensurePermission(Permissions.SEND_MESSAGES, this)
		}
		val messageJson = mapper.writeValueAsString(ModelBasicMessage(content, this, tts, embed))
		val fileParts = files.mapIndexed { index, file ->
			MultipartBody.Part.createFormData("file$index", file.name,
				RequestBody.create(MediaType.parse(tika.detect(file)), file))
		}.toTypedArray()
		return discord.api.sendMessage(this.id, MultipartBody.Part.createFormData("payload_json", messageJson), fileParts)
				.thenApply { it.toEntity(discord) }
	}


	// todo message cache and stuff
	class MessageCache(private val channelID: Long, private val discord: Discord) : IdentifiableCache<Message>() {
		/**
		 * All messages from this channel, sorted by date
		 */
		override val values: List<Message>
			get() = super.values.sortedBy { it.timestamp }.asReversed()

		fun after(after: Message, count: Int = 50) {

		}

		fun before(before: Message, count: Int = 50) {

		}

		fun around(around: Message, count: Int = 50) {

		}

		/**
		 * Procures the specified amount of messages from this channel
		 */
		fun fetchMessages(count: Int = 50): CompletableFuture<List<Message>>
				= discord.api.fetchMessages(channelID, count).thenApply {
			modelsToEntities(it)
		}

		private fun modelsToEntities(models: List<ModelMessage>): List<Message> {
			val result = arrayListOf<Message>()
			models.forEach { result.add(it.toEntity(discord)) }
			return result
		}
	}

	@JsonValue private val value = id
}