package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter
import ml.docilealligator.infinityforreddit.post.Post

class PostDetailCommentsCacheManager(
    val cacheMap: LinkedHashMap<String, PostDetailCommentsCache> = AutoRemovalLinkedHashMap<String, PostDetailCommentsCache>(10)
) {
    fun saveCache(
        post: Post,
        visibleComments: ArrayList<Comment>,
        children: ArrayList<String>?,
        commentFilter: CommentFilter?,
        scrollPosition: Int,
        hasMoreChildren: Boolean
    ) {
        cacheMap[post.id] = PostDetailCommentsCache(
            post,
            visibleComments,
            children,
            commentFilter,
            scrollPosition,
            hasMoreChildren
        )
    }

    fun getCache(post: Post?): PostDetailCommentsCache? {
        return cacheMap[post?.id]
    }

    fun getCache(postId: String?): PostDetailCommentsCache? {
        return cacheMap[postId]
    }

    fun removeCache(post: Post?) {
        post?.let {
            cacheMap.remove(it.id);
        }
    }

    fun removeCache(postId: String?) {
        cacheMap.remove(postId);
    }
}