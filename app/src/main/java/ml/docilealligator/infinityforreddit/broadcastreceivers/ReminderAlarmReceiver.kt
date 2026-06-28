package ml.docilealligator.infinityforreddit.broadcastreceivers

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.reminder.Reminder
import ml.docilealligator.infinityforreddit.utils.NotificationUtils
import java.util.Random
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class ReminderAlarmReceiver: BroadcastReceiver() {
    @Inject
    lateinit var mRedditRoomDatabase: RedditDataRoomDatabase
    @Inject
    lateinit var mCustomThemeWrapper: CustomThemeWrapper

    @OptIn(DelicateCoroutinesApi::class)
    override fun onReceive(context: Context, intent: Intent) {
        val reminder: Reminder? = intent.getParcelableExtra(EXTRA_REMINDER)
        reminder?.let {
            (context.applicationContext as Infinity).appComponent.inject(this)
            val notificationManager = NotificationUtils.getNotificationManager(context)
            val builder = NotificationUtils.buildNotification(
                notificationManager,
                context, context.getString(R.string.reminder), it.content, context.getString(if (it.commentId.isNotEmpty()) R.string.comment else R.string.post),
                NotificationUtils.CHANNEL_ID_NEW_MESSAGES,
                NotificationUtils.CHANNEL_NEW_MESSAGES,
                NotificationUtils.GROUP_REMINDER, mCustomThemeWrapper.colorPrimaryLightTheme
            )

            val intent = Intent(context, ViewPostDetailActivity::class.java)
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, it.postId)
            intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, it.commentId)
            val pendingIntent =
                PendingIntent.getActivity(context, it.createdAt.toInt(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            builder.setContentIntent(pendingIntent)

            try {
                notificationManager.notify(
                    NotificationUtils.REMINDER_NOTIFICATION_ID + Random().nextInt(10000), builder.build()
                )
            } catch (e: SecurityException) {
                e.printStackTrace()
            }

            doAsync(GlobalScope) {
                try {
                    mRedditRoomDatabase.reminderDao().deleteReminder(it)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    companion object {
        const val EXTRA_REMINDER = "ER"
    }
}

fun BroadcastReceiver.doAsync(
    appScope: CoroutineScope,
    coroutineContext: CoroutineContext = EmptyCoroutineContext,
    block: suspend CoroutineScope.() -> Unit
) {
    val pendingResult = goAsync()
    appScope.launch(coroutineContext) { block() }.invokeOnCompletion { pendingResult.finish() }
}