package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment

interface CommentModerationActionHandler {
    fun approveComment(comment: Comment, position: Int)
    fun removeComment(comment: Comment, position: Int, isSpam: Boolean)
    fun toggleLock(comment: Comment, position: Int)
}