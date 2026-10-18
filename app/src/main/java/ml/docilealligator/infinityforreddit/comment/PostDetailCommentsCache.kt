package ml.docilealligator.infinityforreddit.comment

import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.thing.SortType

class PostDetailCommentsCache(
    val post: Post,
    val visibleComments: ArrayList<Comment>,
    val children: ArrayList<String>?,
    val sortType: SortType.Type,
    val scrollPosition: Int
    ) {
}