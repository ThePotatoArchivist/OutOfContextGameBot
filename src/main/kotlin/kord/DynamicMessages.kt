package archives.tater.bot.outofcontext.kord

import archives.tater.bot.outofcontext.Game
import archives.tater.bot.outofcontext.games
import dev.kord.common.entity.ButtonStyle
import dev.kord.core.behavior.interaction.modal
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.MessageBuilder
import dev.kord.rest.builder.message.actionRow

object StartMessage : DynamicMessage<Game>("start") {
    override fun content(data: Game): String = """
        |# Out of Context
        |Players joined:
        |${data.players.joinToString("\n") { it.mention }}
    """.trimMargin()

    override fun MessageBuilder.components(data: Game) {
        actionRow {
            interactionButton(ButtonStyle.Primary, customIdOf("join")) {
                label = "Join"
                if (data.started) disabled = true
            }
            interactionButton(ButtonStyle.Danger, customIdOf("leave")) {
                label = "Leave"
                if (data.started) disabled = true
            }
        }
        actionRow {
            interactionButton(ButtonStyle.Primary, customIdOf("start")) {
                label = "Start"
                if (data.started) disabled = true
            }
            interactionButton(ButtonStyle.Danger, customIdOf("cancel")) {
                label = "Cancel"
                if (data.started) disabled = true
            }
        }
    }

    override suspend fun ComponentInteractionCreateEvent.onInteract(data: Game, componentId: String) {
        when (componentId) {
            "join" -> data.players.add(interaction.user)
            "leave" -> data.players.remove(interaction.user)
            "start" -> {
                repeat(data.players.size) {
                    data.writings.add(mutableListOf())
                }
                data.started = true
                update(data, components = true)
                return
            }
            "cancel" -> {
                games.remove(interaction.message.interaction!!.id)
                interaction.deferPublicMessageUpdate()
                interaction.message.delete()
                return
            }
            else -> return
        }
        update(data, content = true)
    }
}

val dynamicMessages = listOf(
    StartMessage,
)
