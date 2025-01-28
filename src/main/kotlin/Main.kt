package archives.tater.bot.outofcontext

import archives.tater.bot.outofcontext.kord.StartMessage
import archives.tater.bot.outofcontext.kord.StartMessage.onInteract
import archives.tater.bot.outofcontext.kord.StartMessage.send
import archives.tater.bot.outofcontext.kord.dynamicMessages
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.Kord
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.core.entity.interaction.ChatInputCommandInteraction
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.event.interaction.ModalSubmitInteractionCreateEvent
import dev.kord.core.live.live
import dev.kord.core.live.on
import dev.kord.core.on
import dev.kord.gateway.Intent
import dev.kord.rest.builder.interaction.integer
import dev.kord.rest.builder.message.actionRow
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonNull.content

class Game(val rounds: Int) {
    var started = false
    val players = mutableListOf<User>()
    val writings = mutableListOf<MutableList<String>>()
}

val games = mutableMapOf<Snowflake, Game>()

suspend fun main() {
    val dotenv = Dotenv.load()

    with (Kord(dotenv["BOT_TOKEN"])) {
        createGlobalChatInputCommand("start", "Start a game of Out of Context") {
            dmPermission = false

            integer("rounds", "How many sections each player should write for each story, default 1") {
                minValue = 1
            }
        }

        on<ChatInputCommandInteractionCreateEvent> {
            interaction.respondPublic {
                with (StartMessage) {
                    send(Game(interaction.command.integers["rounds"]?.toInt() ?: 1).also {
                        games[interaction.id] = it
                    })
                }
            }
        }

        on<ComponentInteractionCreateEvent> {
            val game = games[interaction.message.interaction!!.id]!!
            val (messageType, componentId) = interaction.componentId.split(':')
            dynamicMessages.find { it.id == messageType }?.run{
                onInteract(game, componentId)
            }
        }

        on<ModalSubmitInteractionCreateEvent> {
            interaction.deferPublicMessageUpdate()
        }

        on<ReadyEvent> {
            println("Logged in!")

            editPresence {
                playing("/start")
            }
        }

        login {
//            intents += Intent.GuildMessages + Intent.Guilds
        }
    }
}
