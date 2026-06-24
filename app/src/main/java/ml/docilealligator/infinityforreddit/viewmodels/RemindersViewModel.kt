package ml.docilealligator.infinityforreddit.viewmodels

import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.reminder.Reminder
import retrofit2.Retrofit

class RemindersViewModel(
    private val retrofit: Retrofit,
    private val oauthRetrofit: Retrofit,
    private val mRedditDataRoomDatabase: RedditDataRoomDatabase,
    private val mCurrentAccountSharedPreferences: SharedPreferences
) : ViewModel() {
    private val _reminders = MutableStateFlow<List<Reminder>?>(null)
    val reminders = _reminders.asStateFlow()

    suspend fun initializeReminders() {
        _reminders.value = mRedditDataRoomDatabase.reminderDao().getAllReminders()
    }

    companion object {
        fun provideFactory(
            retrofit: Retrofit,
            oauthRetrofit: Retrofit,
            redditRoomDatabase: RedditDataRoomDatabase,
            currentAccountSharedPreferences: SharedPreferences
        ) : ViewModelProvider.Factory {
            return object: ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(
                    modelClass: Class<T>,
                    extras: CreationExtras
                ): T {
                    return RemindersViewModel(
                        retrofit,
                        oauthRetrofit,
                        redditRoomDatabase,
                        currentAccountSharedPreferences
                    ) as T
                }
            }
        }
    }
}