@file:OptIn(KordPreview::class)

package archives.tater.bot.outofcontext

import archives.tater.bot.outofcontext.kord.LobbyMessage
import archives.tater.bot.outofcontext.kord.LobbyState
import archives.tater.bot.outofcontext.kord.dynamicMessages
import archives.tater.bot.outofcontext.kord.runGame
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.Snowflake
import dev.kord.core.Kord
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.event.gateway.ReadyEvent
import dev.kord.core.event.interaction.ChatInputCommandInteractionCreateEvent
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.on
import dev.kord.rest.builder.interaction.integer
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.coroutines.Job
import okhttp3.internal.wait

val games = mutableMapOf<Snowflake, StoryState>()

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
            val state = LobbyState(interaction.command.integers["rounds"]?.toInt() ?: 1)
            val progress = Job()
            state.onStart = {
                kord.runGame(StoryState(state.players), interaction.channel)
                progress.complete()
            }
            state.onCancel = {
                progress.complete()
            }
            interaction.respondPublic {
                with (LobbyMessage) {
                     send(state)
                }
            }.getFollowupMessage(Snowflake(0)).message.liveRun {  // TODO get original message not followup
                on<ComponentInteractionCreateEvent> {
                    val (messageType, componentId) = interaction.componentId.split(':')
                    dynamicMessages.find { it.id == messageType }?.run{
                        onInteract(state, componentId)
                    }
                }
                progress.join()
            }
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
