package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.post.Post

interface PostModerationActionHandler {
    fun approvePost(post: Post?)
}