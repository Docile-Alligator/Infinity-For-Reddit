package ml.docilealligator.infinityforreddit.broadcastreceivers;

import static android.content.Context.NOTIFICATION_SERVICE;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class DownloadedMediaDeleteActionBroadcastReceiver extends BroadcastReceiver {
    public static final String EXTRA_NOTIFICATION_ID = "ENI";
    @Override
    public void onReceive(Context context, Intent intent) {
        Uri mediaUri = intent.getData();
        if (mediaUri != null) {
            try {
                context.getContentResolver().delete(mediaUri, null, null);
            } catch (Exception e) {
                DocumentFile file = DocumentFile.fromSingleUri(context, mediaUri);
                if (file != null) {
                    if (!file.delete()) {
                        new File(mediaUri.toString()).delete();
                    }
                }
            }

        }

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        manager.cancel(intent.getIntExtra(EXTRA_NOTIFICATION_ID, -1));
    }
}
