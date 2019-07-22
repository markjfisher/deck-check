package legends

import com.jessecorbett.diskord.api.rest.CreateMessage
import com.jessecorbett.diskord.api.rest.Embed
import com.jessecorbett.diskord.api.rest.EmbedAuthor
import com.jessecorbett.diskord.api.rest.EmbedImage
import com.jessecorbett.diskord.dsl.bot
import com.jessecorbett.diskord.dsl.command
import com.jessecorbett.diskord.dsl.commands
import com.jessecorbett.diskord.util.mention
import com.jessecorbett.diskord.util.words
import com.natpryce.konfig.*
import io.elderscrollslegends.CardCache
import io.elderscrollslegends.Deck
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import mu.KotlinLogging
import org.jeasy.rules.api.Facts
import org.jeasy.rules.api.Rules
import org.jeasy.rules.core.DefaultRulesEngine
import org.jeasy.rules.mvel.MVELRule

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

        runBlocking {
            logger.info {"Loading card cache..."}
            CardCache.load()
            logger.info {"... complete loading."}
            bot(config[token]) {
                commands(prefix = "!") {
                    command(command = "deck") {
                        val deckArgs = words.drop(1)
                        val deckCommand = DeckCommands.values().find { it.cmd == deckArgs[0] } ?: DeckCommands.CMD_HELP
                        val replyData = deckCommand.run(deckArgs.drop(1), author.mention, author.username)

                        replyData.text.forEach { text ->
                            channel.createMessage(
                                CreateMessage(
                                    content = text,
                                    embed = replyData.embed
                                    // fileContent = fileData
                                )
                            )
                        }
                    }

                    command(command = "tournament") {
                        val tournamentArgs = words.drop(1)
                        val tournamentCommand = TournamentCommands.values().find { it.cmd == tournamentArgs[0] } ?: TournamentCommands.CMD_HELP
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

