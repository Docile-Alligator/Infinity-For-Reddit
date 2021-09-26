package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.MaterialYouUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class MaterialYouService extends Service {

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;

    public MaterialYouService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        NotificationUtils.CHANNEL_ID_MATERIAL_YOU,
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(NotificationUtils.CHANNEL_MATERIAL_YOU)
                        .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(serviceChannel);

        Notification notification = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_MATERIAL_YOU)
                .setContentTitle(getString(R.string.material_you_notification_title))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(customThemeWrapper.getColorPrimaryLightTheme())
                .build();
        startForeground(NotificationUtils.MATERIAL_YOU_NOTIFICATION_ID, notification);

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false)) {
            MaterialYouUtils.changeTheme(this, executor, new Handler(), redditDataRoomDatabase,
                    customThemeWrapper, lightThemeSharedPreferences, darkThemeSharedPreferences,
                    amoledThemeSharedPreferences, () -> {
                        stopService();
                    });
        } else {
            stopService();
        }

        return START_NOT_STICKY;
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }
}