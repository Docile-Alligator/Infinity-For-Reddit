package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter

class PostDetailCommentsCache(
    val visibleComments: ArrayList<Comment>,
    val children: ArrayList<String>?,
    val commentFilter: CommentFilter?,
    val scrollPosition: Int,
    val hasMoreChildren: Boolean
    ) {
}