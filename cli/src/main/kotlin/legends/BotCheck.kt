package legends

import com.jessecorbett.diskord.api.rest.CreateMessage
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.mention
import com.jessecorbett.diskord.util.sendFile
import com.jessecorbett.diskord.util.words
import com.natpryce.konfig.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

object BotCheck {

    private val token = Key("deck-check.bot.token", stringType)
    private val adminsKey = Key("deck-check.bot.admins", stringType)

    private val config = ConfigurationProperties.systemProperties() overriding
            EnvironmentVariables() overriding
            ConfigurationProperties.fromResource("deck-check.properties")

    val admins = mutableListOf<String>()
    val tournaments = mutableListOf<Tournament>()

    @UnstableDefault
    @JvmStatic
    fun main(args: Array<String>) {
        admins.addAll(config[adminsKey].split(","))

        while(true) {
            try {
                runBot()
            } catch (e: Exception) {
                logger.error(e) { "Caught exception running bot. Restarting..." }
            }
        }

    }

    private fun runBot() {
        runBlocking {
            bot(config[token]) {
                commands(prefix = "!") {
                    command(command = "deck") {
                        val deckArgs = words.drop(1)
                        val deckCommand = when {
                            deckArgs.isEmpty() -> DeckCommands.CMD_HELP
                            else -> DeckCommands.values().find { it.cmd == deckArgs[0] } ?: DeckCommands.CMD_HELP
                        }
                        val replyData = deckCommand.run(deckArgs.drop(1), author.mention, author.username)

                        if (replyData.fileData != null) {
                            channel.sendFile(data = replyData.fileData, comment = replyData.text.first())
                        } else {
                            replyData.text.forEach { text ->
                                channel.createMessage(
                                    CreateMessage(
                                        content = text,
                                        embed = replyData.embed
                                    )
                                )
                            }
                        }
                    }

                    command(command = "tournament") {
                        val tournamentArgs = words.drop(1)
                        val tournamentCommand =
                            TournamentCommands.values().find { it.cmd == tournamentArgs[0] }
                                ?: TournamentCommands.CMD_HELP
                        val replyData = tournamentCommand.run(tournamentArgs.drop(1), author.mention, author.username)

                        replyData.text.forEach { text ->
                            channel.createMessage(
                                CreateMessage(
                                    content = text,
                                    embed = replyData.embed
                                )
                            )
                        }
                    }
                }

                started {
                    logger.info { "started with sessionId: ${it.sessionId}" }
                }
            }
        }
    }
}
