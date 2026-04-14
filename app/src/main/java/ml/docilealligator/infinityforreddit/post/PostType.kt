package ml.docilealligator.infinityforreddit.post

import androidx.annotation.IntDef

@IntDef(
    PostType.Companion.FRONT_PAGE,
    PostType.Companion.SUBREDDIT,
    PostType.Companion.USER,
    PostType.Companion.SEARCH,
    PostType.Companion.MULTIREDDIT,
    PostType.Companion.ANONYMOUS_FRONT_PAGE,
    PostType.Companion.ANONYMOUS_MULTIREDDIT,
    PostType.Companion.READ_POSTS,
)
@Retention(AnnotationRetention.SOURCE)
annotation class PostType {
    companion object {
        const val FRONT_PAGE: Int = 0
        const val SUBREDDIT: Int = 1
        const val USER: Int = 2
        const val SEARCH: Int = 3
        const val MULTIREDDIT: Int = 4
        const val ANONYMOUS_FRONT_PAGE: Int = 5
        const val ANONYMOUS_MULTIREDDIT: Int = 6
        const val READ_POSTS: Int = 100
    }
}
