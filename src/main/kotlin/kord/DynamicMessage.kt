package archives.tater.bot.outofcontext.kord

import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.behavior.edit
import dev.kord.core.entity.channel.MessageChannel
import dev.kord.core.entity.channel.TextChannel
import dev.kord.core.event.interaction.ComponentInteractionCreateEvent
import dev.kord.rest.builder.message.MessageBuilder
import kotlinx.coroutines.flow.channelFlow

abstract class DynamicMessage<D>(val id: String) {

    protected fun customId(componentId: String) = "$id:$componentId"

    open fun content(data: D): String = ""
    open fun MessageBuilder.embeds(data: D) {}
    open fun MessageBuilder.components(data: D) {}

    fun MessageBuilder.send(data: D) {
        content = content(data)
        embeds(data)
        components(data)
    }

    suspend fun update(message: MessageBehavior, data: D, content: Boolean = false, embeds: Boolean = false, components: Boolean = false) {
        message.edit {
            if (content) this.content = content(data)
            if (embeds) embeds(data)
            if (components) components(data)
        }
    }

    abstract suspend fun ComponentInteractionCreateEvent.onInteract(data: D, componentId: String)

    suspend fun ComponentInteractionCreateEvent.update(data: D, content: Boolean = false, embeds: Boolean = false, components: Boolean = false) {
        interaction.deferPublicMessageUpdate()
        update(interaction.message, data, content, embeds, components)
    }
}
