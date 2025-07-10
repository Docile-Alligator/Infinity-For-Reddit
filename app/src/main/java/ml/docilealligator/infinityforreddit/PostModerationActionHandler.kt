package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.post.Post

interface PostModerationActionHandler {
    fun approvePost(post: Post, position: Int)
    fun removePost(post: Post, position: Int, isSpam: Boolean)
    fun toggleSticky(post: Post, position: Int)
    fun toggleLock(post: Post, position: Int)
}