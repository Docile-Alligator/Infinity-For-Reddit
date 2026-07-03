package ml.docilealligator.infinityforreddit.broadcastreceivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.reminder.ReminderManager
import javax.inject.Inject

class BootCompletedBroadcastReceiver: BroadcastReceiver() {
    @Inject
    lateinit var mReminderManager: ReminderManager

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            (context.applicationContext as Infinity).appComponent.inject(this)

            doAsync(GlobalScope) {
                try {
                    mReminderManager.checkAndSetAllAlarmsSync()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}