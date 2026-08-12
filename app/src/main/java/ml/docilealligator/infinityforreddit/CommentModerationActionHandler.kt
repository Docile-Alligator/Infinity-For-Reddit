package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post

interface CommentModerationActionHandler {
    fun approveComment(comment: Comment, position: Int)
    fun removeComment(comment: Comment, position: Int, isSpam: Boolean)
    fun toggleLock(comment: Comment, position: Int)
    fun toggleMod(comment: Comment, position: Int)
}