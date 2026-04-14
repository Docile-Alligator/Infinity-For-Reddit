package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter
import ml.docilealligator.infinityforreddit.post.Post

class PostDetailCommentsCache(
    val post: Post,
    val visibleComments: ArrayList<Comment>,
    val children: ArrayList<String>?,
    val commentFilter: CommentFilter?,
    val scrollPosition: Int,
    val hasMoreChildren: Boolean
    ) {
}