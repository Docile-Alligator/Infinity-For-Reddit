package ml.docilealligator.infinityforreddit.viewmodels

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader
import retrofit2.Retrofit
import java.util.concurrent.Executor

class ViewPostDetailActivityViewModel(
    executor: Executor?, handler: Handler?, redditDataRoomDatabase: RedditDataRoomDatabase?,
    retrofit: Retrofit?
) : ViewModel() {
    var post: Post? = null

    private val _posts = MutableLiveData<List<Post>>()
    val posts: LiveData<List<Post>> = _posts

    private val mLoader: UserProfileImagesBatchLoader =
        UserProfileImagesBatchLoader(executor, handler, redditDataRoomDatabase, retrofit)

    fun setPosts(posts: List<Post>) {
        _posts.postValue(posts);
    }

    fun getPost(index: Int): Post? {
        return _posts.value?.getOrNull(index)
    }

    fun loadAuthorImages(comments: MutableList<Comment?>, loadIconListener: LoadIconListener) {
        mLoader.loadAuthorImages(comments, loadIconListener)
    }

    interface LoadIconListener {
        fun loadIconSuccess(authorFullName: String?, iconUrl: String?)
    }

    companion object {
        fun provideFactory(executor: Executor?,
                           handler: Handler?,
                           redditDataRoomDatabase: RedditDataRoomDatabase?,
                           retrofit: Retrofit?): ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return ViewPostDetailActivityViewModel(
                        executor, handler, redditDataRoomDatabase, retrofit
                    ) as T
                }
            }
        }
    }
}