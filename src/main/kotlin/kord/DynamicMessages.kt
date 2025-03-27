package archives.tater.bot.outofcontext.kord

import archives.tater.bot.outofcontext.StoryState
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.entity.User
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

data class LobbyState(
    val rounds: Int,
    val players: MutableList<User> = mutableListOf(),
    var onStart: (suspend () -> Unit)? = null,
    var onCancel: (suspend () -> Unit)? = null,
)

object LobbyMessage : DynamicMessage<LobbyState?>("lobby") {
    override fun content(data: LobbyState?): String = """
        |# Out of Context
        |Players joined:
        |${data?.players?.joinToString("\n") { it.mention } ?: "None"}
    """.trimMargin()

    override fun MessageBuilder.components(data: LobbyState?) {
        actionRow {
            interactionButton(ButtonStyle.Primary, customId("join")) {
                label = "Join"
                if (data == null) disabled = true
            }
            interactionButton(ButtonStyle.Danger, customId("leave")) {
                label = "Leave"
                if (data == null) disabled = true
            }
        }
        actionRow {
            interactionButton(ButtonStyle.Primary, customId("start")) {
                label = "Start"
                if (data == null) disabled = true
            }
            interactionButton(ButtonStyle.Danger, customId("cancel")) {
                label = "Cancel"
                if (data == null) disabled = true
            }
        }
    }

    override suspend fun ComponentInteractionCreateEvent.onInteract(data: LobbyState?, componentId: String) {
        if (data == null) return
        when (componentId) {
            "join" -> data.players.add(interaction.user)
            "leave" -> data.players.remove(interaction.user)
            "start" -> {
                update(null, components = true)
                data.onStart?.invoke()
                return
            }
            "cancel" -> {
                interaction.deferPublicMessageUpdate()
                interaction.message.delete()
                data.onCancel?.invoke()
                return
            }
            else -> return
        }
        update(data, content = true)
    }
}

val dynamicMessages = listOf(
    LobbyMessage,
)
