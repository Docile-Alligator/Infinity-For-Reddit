package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.reminder.ReminderManager
import retrofit2.Retrofit

class SetReminderViewModel(
    val post: Post?,
    val postId: String?,
    val comment: Comment?,
    private val reminderManager: ReminderManager
): ViewModel() {
    fun setReminder() {

    }

    companion object {
        fun provideFactory(
            post: Post?,
            postId: String?,
            comment: Comment?,
            reminderManager: ReminderManager
        ) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return SetReminderViewModel(
                        post, postId, comment, reminderManager
                    ) as T
                }
            }
        }
    }
}