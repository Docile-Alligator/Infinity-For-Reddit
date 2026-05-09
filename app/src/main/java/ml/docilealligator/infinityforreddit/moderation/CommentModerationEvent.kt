package ml.docilealligator.infinityforreddit.moderation

import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post

sealed class CommentModerationEvent(open val comment: Comment, open val position: Int, val toastMessageResId: Int) {
    data class Approved(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.approved)
    data class ApproveFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.approve_failed)

    data class Removed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.removed)
    data class RemoveFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.remove_failed)

    data class MarkedAsSpam(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.marked_as_spam)
    data class MarkAsSpamFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.mark_as_spam_failed)

    data class Locked(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.locked)
    data class LockFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.lock_failed)

    data class Unlocked(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.unlocked)
    data class UnlockFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.unlock_failed)

    data class SetReceiveNotification(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.reply_notifications_enabled)
    data class SetReceiveNotificationFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.toggle_reply_notifications_failed)

    data class UnsetReceiveNotification(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.reply_notifications_disabled)
    data class UnsetReceiveNotificationFailed(override val comment: Comment, override val position: Int) : CommentModerationEvent(comment, position, R.string.toggle_reply_notifications_failed)
}