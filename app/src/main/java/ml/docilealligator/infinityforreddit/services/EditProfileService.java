package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import com.bumptech.glide.Glide;
import jp.wasabeef.glide.transformations.CropTransformation;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitChangeAvatarEvent;
import ml.docilealligator.infinityforreddit.events.SubmitChangeBannerEvent;
import ml.docilealligator.infinityforreddit.events.SubmitSaveProfileEvent;
import ml.docilealligator.infinityforreddit.utils.EditProfileUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Retrofit;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileNotFoundException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class EditProfileService extends Service {
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_DISPLAY_NAME = "EDN";
    public static final String EXTRA_ABOUT_YOU = "EAY";
    public static final String EXTRA_POST_TYPE = "EPT";

    public static final int EXTRA_POST_TYPE_UNKNOWN = 0x500;
    public static final int EXTRA_POST_TYPE_CHANGE_BANNER = 0x501;
    public static final int EXTRA_POST_TYPE_CHANGE_AVATAR = 0x502;
    public static final int EXTRA_POST_TYPE_SAVE_EDIT_PROFILE = 0x503;

    private static final String EXTRA_MEDIA_URI = "EU";

    private static final int MAX_BANNER_WIDTH = 1280;
    private static final int MIN_BANNER_WIDTH = 640;
    private static final int AVATAR_SIZE = 256;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private Handler handler;
    private ServiceHandler serviceHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        ((Infinity) getApplication()).getAppComponent().inject(this);
        handler = new Handler();
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        NotificationUtils.CHANNEL_SUBMIT_POST,
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(NotificationUtils.CHANNEL_SUBMIT_POST)
                        .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);
        Bundle bundle = intent.getExtras();
        final int postType = intent.getIntExtra(EXTRA_POST_TYPE, EXTRA_POST_TYPE_UNKNOWN);
        switch (postType) {
            case EXTRA_POST_TYPE_CHANGE_BANNER:
                bundle.putString(EXTRA_MEDIA_URI, intent.getData().toString());
                startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.submit_change_banner));
                break;
            case EXTRA_POST_TYPE_CHANGE_AVATAR:
                bundle.putString(EXTRA_MEDIA_URI, intent.getData().toString());
                startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.submit_change_avatar));
                break;
            case EXTRA_POST_TYPE_SAVE_EDIT_PROFILE:
                startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.submit_save_profile));
                break;
            default:
            case EXTRA_POST_TYPE_UNKNOWN:
                break;
        }

        Message msg = serviceHandler.obtainMessage();
        msg.setData(bundle);
        serviceHandler.sendMessage(msg);
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void submitChangeBanner(String accessToken, Uri mediaUri, String accountName) {
        try {
            final int width = getWidthBanner(mediaUri);
            final int height = Math.round(width * 3 / 10f); // ratio 10:3
            CropTransformation bannerCrop = new CropTransformation(width, height, CropTransformation.CropType.CENTER);
            Bitmap resource = Glide.with(this).asBitmap().skipMemoryCache(true)
                    .load(mediaUri).transform(bannerCrop).submit().get();
            EditProfileUtils.uploadBanner(mOauthRetrofit, accessToken, accountName, resource, new EditProfileUtils.EditProfileUtilsListener() {
                @Override
                public void success() {
                    handler.post(() -> EventBus.getDefault().post(new SubmitChangeBannerEvent(true, "")));
                    stopService();
                }

                @Override
                public void failed(String message) {
                    handler.post(() -> EventBus.getDefault().post(new SubmitChangeBannerEvent(false, message)));
                    stopService();
                }
            });
        } catch (InterruptedException | ExecutionException | FileNotFoundException e) {
            e.printStackTrace();
            stopService();
        }
    }

    private void submitChangeAvatar(String accessToken, Uri mediaUri, String accountName) {
        try {
            final CropTransformation avatarCrop = new CropTransformation(AVATAR_SIZE, AVATAR_SIZE, CropTransformation.CropType.CENTER);
            final Bitmap resource = Glide.with(this).asBitmap().skipMemoryCache(true)
                    .load(mediaUri).transform(avatarCrop).submit().get();
            EditProfileUtils.uploadAvatar(mOauthRetrofit, accessToken, accountName, resource, new EditProfileUtils.EditProfileUtilsListener() {
                @Override
                public void success() {
                    handler.post(() -> EventBus.getDefault().post(new SubmitChangeAvatarEvent(true, "")));
                    stopService();
                }

                @Override
                public void failed(String message) {
                    handler.post(() -> EventBus.getDefault().post(new SubmitChangeAvatarEvent(false, message)));
                    stopService();
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            stopService();
        }
    }

    private void submitSaveEditProfile(String accessToken,
                                       String accountName,
                                       String displayName,
                                       String publicDesc
    ) {
        EditProfileUtils.updateProfile(mOauthRetrofit,
                accessToken,
                accountName,
                displayName,
                publicDesc,
                new EditProfileUtils.EditProfileUtilsListener() {
                    @Override
                    public void success() {
                        handler.post(() -> EventBus.getDefault().post(new SubmitSaveProfileEvent(true, "")));
                        stopService();
                    }

                    @Override
                    public void failed(String message) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitSaveProfileEvent(false, message)));
                        stopService();
                    }
                });

    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_SUBMIT_POST)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }


    private int getWidthBanner(Uri mediaUri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(mediaUri), null, options);
        return Math.max(Math.min(options.outWidth, MAX_BANNER_WIDTH), MIN_BANNER_WIDTH);
    }

    private class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);
            String accountName = bundle.getString(EXTRA_ACCOUNT_NAME);
            final int postType = bundle.getInt(EXTRA_POST_TYPE, EXTRA_POST_TYPE_UNKNOWN);
            switch (postType) {
                case EXTRA_POST_TYPE_CHANGE_BANNER:
                    submitChangeBanner(accessToken,
                            Uri.parse(bundle.getString(EXTRA_MEDIA_URI)),
                            accountName);
                    break;
                case EXTRA_POST_TYPE_CHANGE_AVATAR:
                    submitChangeAvatar(accessToken,
                            Uri.parse(bundle.getString(EXTRA_MEDIA_URI)),
                            accountName);
                    break;
                case EXTRA_POST_TYPE_SAVE_EDIT_PROFILE:
                    submitSaveEditProfile(
                            accessToken,
                            accountName,
                            bundle.getString(EXTRA_DISPLAY_NAME),
                            bundle.getString(EXTRA_ABOUT_YOU)
                    );
                    break;
                default:
                case EXTRA_POST_TYPE_UNKNOWN:
                    break;
            }
        }
    }
}
