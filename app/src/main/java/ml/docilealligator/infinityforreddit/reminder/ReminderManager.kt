package ml.docilealligator.infinityforreddit.reminder

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase

class ReminderManager(
    private val applicationContext: Application,
    private val redditRoomDatabase: RedditDataRoomDatabase,
    private val alarmManager: AlarmManager
) {
    suspend fun setReminder(reminder: Reminder) {
        redditRoomDatabase.reminderDao().insert(reminder)

        val alarmIntent = Intent(applicationContext,
            ReminderAlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(applicationContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        }

        alarmManager.set(
            AlarmManager.RTC_WAKEUP,
            reminder.reminderTime,
            alarmIntent
        )
    }

    fun getAllRemindersFlow(): Flow<List<Reminder>> {
        return redditRoomDatabase.reminderDao().getAllRemindersFlow()
    }
}