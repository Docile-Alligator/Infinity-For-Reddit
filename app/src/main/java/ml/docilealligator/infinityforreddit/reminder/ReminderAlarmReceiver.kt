package ml.docilealligator.infinityforreddit.reminder

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ReminderAlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminder: Reminder? = intent.getParcelableExtra(EXTRA_REMINDER)
        reminder?.let {

        }
    }

    companion object {
        val EXTRA_REMINDER = "ER"
    }
}