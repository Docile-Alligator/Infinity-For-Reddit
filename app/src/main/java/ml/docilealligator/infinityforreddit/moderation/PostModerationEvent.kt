package ml.docilealligator.infinityforreddit.moderation

import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.post.Post

sealed class PostModerationEvent(open val post: Post, open val position: Int, val toastMessageResId: Int) {
    data class Approved(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.approved)
    data class ApproveFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.approve_failed)

    data class Removed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.removed)
    data class RemoveFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.remove_failed)

    data class MarkedAsSpam(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.marked_as_spam)
    data class MarkAsSpamFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.mark_as_spam_failed)

    data class SetStickyPost(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.set_sticky_post)
    data class SetStickyPostFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.set_sticky_post_failed)

    data class UnsetStickyPost(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unset_sticky_post)
    data class UnsetStickyPostFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unset_sticky_post_failed)

    data class Locked(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.locked)
    data class LockFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.lock_failed)

    data class Unlocked(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unlocked)
    data class UnlockFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unlock_failed)

    data class MarkedNSFW(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.mark_nsfw_success)
    data class MarkNSFWFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.mark_nsfw_failed)

    data class UnmarkedNSFW(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unmark_nsfw_success)
    data class UnmarkNSFWFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unmark_nsfw_failed)

    data class MarkedSpoiler(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.mark_spoiler_success)
    data class MarkSpoilerFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.mark_spoiler_failed)

    data class UnmarkedSpoiler(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unmark_spoiler_success)
    data class UnmarkSpoilerFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.unmark_spoiler_failed)

    data class DistinguishedAsMod(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.distinguished_as_mod)
    data class DistinguishAsModFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.distinguish_as_mod_failed)

    data class UndistinguishedAsMod(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.undistinguished_as_mod)
    data class UndistinguishAsModFailed(override val post: Post, override val position: Int) : PostModerationEvent(post, position, R.string.undistinguish_as_mod_failed)
}