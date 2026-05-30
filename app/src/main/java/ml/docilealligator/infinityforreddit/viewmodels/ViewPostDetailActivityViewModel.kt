package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader

class ViewPostDetailActivityViewModel(
    private val accessToken: String?,
    private val loader: UserProfileImagesBatchLoader
) : ViewModel() {
    var post: Post? = null

    var posts: List<Post>? = null

    fun getPost(index: Int): Post? {
        return posts?.getOrNull(index)
    }

    fun loadAuthorImages(comments: MutableList<Comment?>, loadIconListener: LoadIconListener) {
        loader.loadAuthorImages(accessToken, comments, loadIconListener)
    }

    interface LoadIconListener {
        fun loadIconSuccess(authorFullName: String?, iconUrl: String?)
    }

    companion object {
        fun provideFactory(accessToken: String?, loader: UserProfileImagesBatchLoader): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailActivityViewModel(accessToken, loader) as T
                }
            }
        }
    }
}