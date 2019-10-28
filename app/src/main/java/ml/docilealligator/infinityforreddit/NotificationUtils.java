package ml.docilealligator.infinityforreddit;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class NotificationUtils {
    public static final String CHANNEL_SUBMIT_POST = "Submit Post";
    static final String CHANNEL_ID_NEW_MESSAGES = "new_messages";
    static final String CHANNEL_NEW_MESSAGES = "New Messages";
    public static final int SUBMIT_POST_SERVICE_NOTIFICATION_ID = 10000;

    private static final int SUMMARY_BASE_ID_UNREAD_MESSAGE = 0;
    private static final int NOTIFICATION_BASE_ID_UNREAD_MESSAGE = 1;

    private static final String GROUP_USER_BASE = "ml.docilealligator.infinityforreddit.";

    static NotificationCompat.Builder buildNotification(NotificationManagerCompat notificationManager,
                                                        Context context, String title, String content,
                                                        String summary, String channelId, String channelName,
                                                        String group) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(context.getApplicationContext(), channelId)
                .setContentTitle(title)
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(context.getResources().getColor(R.color.notificationIconColor))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .setSummaryText(summary)
                        .bigText(content))
                .setGroup(group)
                .setAutoCancel(true);
    }

    static NotificationCompat.Builder buildSummaryNotification(Context context, NotificationManagerCompat notificationManager,
                                                               String title, String content, String channelId,
                                                               String channelName, String group) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        return new NotificationCompat.Builder(context, channelId)
                .setContentTitle(title)
                //set content text to support devices running API level < 24
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(context.getResources().getColor(R.color.notificationIconColor))
                .setGroup(group)
                .setGroupSummary(true)
                .setAutoCancel(true);
    }

    static NotificationManagerCompat getNotificationManager(Context context) {
        return NotificationManagerCompat.from(context);
    }

    static String getAccountGroupName(String accountName) {
        return GROUP_USER_BASE + accountName;
    }

    static int getSummaryIdUnreadMessage(int accountIndex) {
        return SUMMARY_BASE_ID_UNREAD_MESSAGE + accountIndex * 1000;
    }

    static int getNotificationIdUnreadMessage(int accountIndex, int messageIndex) {
        return NOTIFICATION_BASE_ID_UNREAD_MESSAGE + accountIndex * 1000 + messageIndex;
    }
}
