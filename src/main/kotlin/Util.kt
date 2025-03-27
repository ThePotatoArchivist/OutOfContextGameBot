@file:OptIn(KordPreview::class)

package archives.tater.bot.outofcontext

import dev.kord.common.annotation.KordPreview
import dev.kord.core.behavior.MessageBehavior
import dev.kord.core.entity.Message
import dev.kord.core.live.LiveMessage
import dev.kord.core.live.live
import jdk.jfr.internal.EventWriterKey.block
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

/**
 * Executes the suspending `block`, then cancels if it hasn't already been canceled once finished
 */
suspend inline fun Message.liveRun(scope: CoroutineScope = kord + SupervisorJob(kord.coroutineContext.job), block: LiveMessage.() -> Unit) {
    live(kord).apply {
        block()
        if (isActive) cancel()
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Deferred<T>.getOrNull() = if (isCompleted) this.getCompleted() else null
