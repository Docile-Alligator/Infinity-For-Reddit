package ml.docilealligator.infinityforreddit.broadcastreceivers;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DownloadedMediaDeleteActionBroadcastReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTIFICATION_ID = "ENI";
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri mediaUri = intent.getData();
        if (mediaUri != null) {
            context.getContentResolver().delete(mediaUri, null, null);
        }

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
    }
}
