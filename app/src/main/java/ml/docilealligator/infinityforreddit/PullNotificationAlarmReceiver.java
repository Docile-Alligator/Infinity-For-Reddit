package ml.docilealligator.infinityforreddit;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Utils.NotificationUtils;
import retrofit2.Retrofit;

public class PullNotificationAlarmReceiver extends BroadcastReceiver {

    @Inject
    @Named("oauth_without_authenticator")
    Retrofit mOauthWithoutAuthenticatorRetrofit;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Override
    public void onReceive(Context context, Intent receivedIntent) {
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);

        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = null;
        if (pm != null) {
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "ml.docilealligator.infinityforreddit:notification");
            wl.acquire(2*60*1000L /*2 minutes*/);
        }

        Log.i("asasdfsdaf", "time: " + System.currentTimeMillis());

        NotificationManagerCompat testNotificationManager = NotificationUtils.getNotificationManager(context);

        NotificationCompat.Builder testSummaryBuilder = NotificationUtils.buildSummaryNotification(context,
                testNotificationManager, "test",
                Long.toString(System.currentTimeMillis()),
                NotificationUtils.CHANNEL_ID_NEW_MESSAGES, NotificationUtils.CHANNEL_NEW_MESSAGES,
                NotificationUtils.getAccountGroupName("test"), Color.BLACK);
        testNotificationManager.notify(NotificationUtils.getSummaryIdUnreadMessage(12), testSummaryBuilder.build());

        WorkManager workManager = WorkManager.getInstance(context);

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest pullNotificationRequest =
                new OneTimeWorkRequest.Builder(PullNotificationWorker.class)
                        .setConstraints(constraints)
                        .build();

        workManager.enqueueUniqueWork(PullNotificationWorker.UNIQUE_WORKER_NAME,
                ExistingWorkPolicy.KEEP, pullNotificationRequest);

        if (wl != null) {
            wl.release();
        }
    }

    public static void setNotificationAlarm(Context context, long notificationInterval) {
        AlarmManager am =(AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (am != null) {
            Intent intent = new Intent(context, PullNotificationAlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationUtils.PULL_NOTIFICATION_ALARM_RECEIVER_REQUEST_CODE, intent, 0);
            if ((notificationInterval == 15 || notificationInterval == 30)) {
                am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60 * 1000, 1000 * 60 * notificationInterval, pendingIntent); // Millisec * Second * Minute
            } else {
                am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, 60 * 1000, 1000 * 60 * 60 * notificationInterval, pendingIntent); // Millisec * Second * Minute
            }
        }
    }

    public static void cancelAlarm(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            Intent intent = new Intent(context, AlarmManager.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, NotificationUtils.PULL_NOTIFICATION_ALARM_RECEIVER_REQUEST_CODE, intent, 0);
            alarmManager.cancel(pendingIntent);
        }
    }
}
