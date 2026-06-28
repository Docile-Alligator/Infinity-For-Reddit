package ml.docilealligator.infinityforreddit.reminder

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Intent
import kotlinx.coroutines.flow.Flow
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.broadcastreceivers.ReminderAlarmReceiver

class ReminderManager(
    private val applicationContext: Application,
    private val redditRoomDatabase: RedditDataRoomDatabase,
    private val alarmManager: AlarmManager
) {
    suspend fun setReminder(reminder: Reminder) {
        redditRoomDatabase.reminderDao().insert(reminder)

        val alarmIntent = Intent(
            applicationContext,
            ReminderAlarmReceiver::class.java
        ).let { intent ->
            intent.putExtra(ReminderAlarmReceiver.EXTRA_REMINDER, reminder)
            PendingIntent.getBroadcast(
                applicationContext, reminder.createdAt.toInt(), intent,
                PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
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