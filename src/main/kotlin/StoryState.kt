package archives.tater.bot.outofcontext

import dev.kord.core.entity.User
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Job

data class StoryState(
    val segments: List<List<CompletableDeferred<String>>>,
    val users: List<User>,
) {
    private val size = users.size

    constructor(users: List<User>, parent: Job? = null) : this(
        List(users.size) { List(users.size) { CompletableDeferred<String>(parent) } },
        users
    )

    data class NextResult(
        val last: CompletableDeferred<String>?,
        val next: CompletableDeferred<String>,
        val end: Boolean,
    )

    fun nextFor(user: User): NextResult {
        assert(user in users) { "User was not in this state" }
        val offset = users.indexOf(user)
        repeat(users.size) {
            val storyIndex = (offset + it).mod(users.size)
            val segment = segments[storyIndex][it]
            if (!segment.isCompleted)
                return NextResult(segments[storyIndex].getOrNull(it - 1), segment, it + 1 == users.size)
        }
        throw AssertionError("User has no segments left")
    }
}
