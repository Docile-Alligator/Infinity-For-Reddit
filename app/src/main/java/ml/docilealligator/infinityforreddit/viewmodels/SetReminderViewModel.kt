package ml.docilealligator.infinityforreddit.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.AppResult
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.comment.Comment
import ml.docilealligator.infinityforreddit.post.Post
import ml.docilealligator.infinityforreddit.reminder.Reminder
import ml.docilealligator.infinityforreddit.reminder.ReminderManager
import kotlin.math.min

class SetReminderViewModel(
    val accountName: String,
    val post: Post?,
    val postId: String?,
    val comment: Comment?,
    private val reminderManager: ReminderManager
): ViewModel() {
    private val _setReminderResult: MutableStateFlow<AppResult<Unit, Int>?> = MutableStateFlow(null)
    val setReminderResult = _setReminderResult.asStateFlow()

    val content: String
        get() = comment?.commentRawText?.let {
            it.substring(0, min(it.length, 200))
        } ?: post?.title ?: ""

    fun setReminder(
        reminderTime: Long
    ) {
        (post?.id ?: postId)?.let {
            viewModelScope.launch {
                reminderManager.setReminder(
                    Reminder(
                        accountName, it, comment?.id ?: "", content, System.currentTimeMillis(), reminderTime
                    )
                )
                _setReminderResult.value = AppResult.Success(Unit)
            }
        } ?: run {
            _setReminderResult.value = AppResult.Error(R.string.invalid_reminder_post_id)
        }
    }

    companion object {
        fun provideFactory(
            accountName: String,
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
                        accountName, post, postId, comment, reminderManager
                    ) as T
                }
            }
        }
    }
}