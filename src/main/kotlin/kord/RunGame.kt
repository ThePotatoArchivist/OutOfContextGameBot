@file:OptIn(KordPreview::class, KordPreview::class)

package archives.tater.bot.outofcontext.kord

import archives.tater.bot.outofcontext.StoryState
import archives.tater.bot.outofcontext.getOrNull
import archives.tater.bot.outofcontext.liveRun
import dev.kord.common.annotation.KordPreview
import dev.kord.common.entity.ButtonStyle
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.TextInputStyle
import dev.kord.core.behavior.channel.MessageChannelBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.behavior.interaction.ModalParentInteractionBehavior
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.behavior.interaction.respondEphemeral
import dev.kord.core.behavior.interaction.respondPublic
import dev.kord.core.behavior.interaction.response.PopupInteractionResponseBehavior
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.core.live.LiveMessage
import dev.kord.core.live.live
import dev.kord.core.live.on
import dev.kord.rest.builder.message.actionRow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.job

@OptIn(KordPreview::class)
suspend fun CoroutineScope.runGame(state: StoryState, channel: MessageChannelBehavior) {
    channel.createMessage {
        content = "Begin Story"
        actionRow {
            interactionButton(ButtonStyle.Primary, "test:test") {
                label = "Write"
            }
        }
    }.liveRun {
        awaitResponse(state)
    }
}

suspend fun segmentModal(interaction: ModalParentInteractionBehavior, previous: String? = null, end: Boolean = false): PopupInteractionResponseBehavior {
    return interaction.modal("Story Segment", "segment") {
        actionRow {
            if (previous != null)
                textInput(TextInputStyle.Paragraph, "previous", "Previous Segment") {
                    disabled = true
                    value = previous
                }
            textInput(TextInputStyle.Paragraph, "segment", when {
                previous == null -> "Start a story"
                end -> "Finish the story"
                else -> "Continue the story"
            }) {
                required = true
            }
        }
    }
}

suspend fun LiveMessage.awaitResponse(state: StoryState) {
    val completion = Job()
    on<ComponentInteractionCreateEvent> {
        val (last, next, end) = state.nextFor(it.interaction.user)
        val response = segmentModal(it.interaction, last?.getOrNull(), end)
        next.complete("Woohoo TODO") // TODO
        // TODO deduplicate this
        message.edit {
            actionRow {
                interactionButton(ButtonStyle.Primary, "test:test") {
                    label = "Write"
                    disabled = true
                }
            }
        }
        completion.complete()
        it.interaction.respondEphemeral {
            content = "Continue Story"
            actionRow {
                interactionButton(ButtonStyle.Primary, "test:test") {
                    label = "Write"
                }
            }
        }.getFollowupMessage(Snowflake(0)).message.liveRun {
            awaitResponse(state)
        }
    }
    completion.join()
}
