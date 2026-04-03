package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter
import ml.docilealligator.infinityforreddit.post.Post

class PostDetailCommentsCacheManager(
    val cacheMap: LinkedHashMap<Post, PostDetailCommentsCache> = AutoRemovalLinkedHashMap<Post, PostDetailCommentsCache>(10)
) {
    fun saveCache(
        post: Post,
        visibleComments: ArrayList<Comment>,
        children: ArrayList<String>?,
        commentFilter: CommentFilter?,
        scrollPosition: Int,
        hasMoreChildren: Boolean
    ) {
        cacheMap[post] = PostDetailCommentsCache(
            visibleComments,
            children,
            commentFilter,
            scrollPosition,
            hasMoreChildren
        )
    }

    fun getCache(post: Post): PostDetailCommentsCache? {
        return cacheMap[post]
    }

    fun removeCache(post: Post) {
        cacheMap.remove(post);
    }
}