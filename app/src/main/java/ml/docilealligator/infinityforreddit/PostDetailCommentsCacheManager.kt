package ml.docilealligator.infinityforreddit

import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.thing.SortType

class PostDetailCommentsCacheManager(
    val cacheMap: AutoRemovalLinkedHashMap<String, PostDetailCommentsCache>
) {
    fun saveCache(
        post: Post,
        visibleComments: ArrayList<Comment>,
        children: ArrayList<String>?,
        sortType: SortType.Type,
        scrollPosition: Int
    ) {
        cacheMap[post.id] = PostDetailCommentsCache(
            post,
            visibleComments,
            children,
            sortType,
            scrollPosition
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

    fun setCapacity(capacity: Int) {
        if (capacity < 0) {
            return
        }

        cacheMap.maxSize = capacity
    }
}