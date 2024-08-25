package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.io.FileNotFoundException;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.CropTransformation;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitChangeAvatarEvent;
import ml.docilealligator.infinityforreddit.events.SubmitChangeBannerEvent;
import ml.docilealligator.infinityforreddit.events.SubmitSaveProfileEvent;
import ml.docilealligator.infinityforreddit.utils.EditProfileUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import retrofit2.Retrofit;

public class EditProfileService extends JobService {
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_DISPLAY_NAME = "EDN";
    public static final String EXTRA_ABOUT_YOU = "EAY";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_MEDIA_URI = "EU";

    public static final int EXTRA_POST_TYPE_UNKNOWN = 0x500;
    public static final int EXTRA_POST_TYPE_CHANGE_BANNER = 0x501;
    public static final int EXTRA_POST_TYPE_CHANGE_AVATAR = 0x502;
    public static final int EXTRA_POST_TYPE_SAVE_EDIT_PROFILE = 0x503;

    private static final int MAX_BANNER_WIDTH = 1280;
    private static final int MIN_BANNER_WIDTH = 640;
    private static final int AVATAR_SIZE = 256;

    private static int JOB_ID = 10000;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private Handler handler;

    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, PersistableBundle extras) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, EditProfileService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, EditProfileService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ((Infinity) getApplication()).getAppComponent().inject(this);
        handler = new Handler();
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        NotificationUtils.CHANNEL_SUBMIT_POST,
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(NotificationUtils.CHANNEL_SUBMIT_POST)
                        .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);

        PersistableBundle bundle = params.getExtras();
        String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);
        String accountName = bundle.getString(EXTRA_ACCOUNT_NAME);
        final int postType = bundle.getInt(EXTRA_POST_TYPE, EXTRA_POST_TYPE_UNKNOWN);
        switch (postType) {
            case EXTRA_POST_TYPE_CHANGE_BANNER:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_change_banner),
                            JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
                } else {
                    manager.notify(NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_change_banner));
                }
                break;
            case EXTRA_POST_TYPE_CHANGE_AVATAR:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_change_avatar),
                            JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
                } else {
                    manager.notify(NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_change_avatar));
                }
                break;
            case EXTRA_POST_TYPE_SAVE_EDIT_PROFILE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_save_profile),
                            JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
                } else {
                    manager.notify(NotificationUtils.EDIT_PROFILE_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(R.string.submit_save_profile));
                }
                break;
            default:
            case EXTRA_POST_TYPE_UNKNOWN:
                return false;
        }

        mExecutor.execute(() -> {
            switch (postType) {
                case EXTRA_POST_TYPE_CHANGE_BANNER:
                    submitChangeBannerSync(params, accessToken,
                            Uri.parse(bundle.getString(EXTRA_MEDIA_URI)),
                            accountName);
                    break;
                case EXTRA_POST_TYPE_CHANGE_AVATAR:
                    submitChangeAvatarSync(params, accessToken,
                            Uri.parse(bundle.getString(EXTRA_MEDIA_URI)),
                            accountName);
                    break;
                case EXTRA_POST_TYPE_SAVE_EDIT_PROFILE:
                    submitSaveEditProfileSync(
                            params,
                            accessToken,
                            accountName,
                            bundle.getString(EXTRA_DISPLAY_NAME),
                            bundle.getString(EXTRA_ABOUT_YOU)
                    );
                    break;
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @WorkerThread
    private void submitChangeBannerSync(JobParameters parameters, String accessToken, Uri mediaUri, String accountName) {
        try {
            final int width = getWidthBanner(mediaUri);
            final int height = Math.round(width * 3 / 10f); // ratio 10:3
            CropTransformation bannerCrop = new CropTransformation(width, height, CropTransformation.CropType.CENTER);
            Bitmap resource = Glide.with(this).asBitmap().skipMemoryCache(true)
                    .load(mediaUri).transform(bannerCrop).submit().get();
            String potentialError = EditProfileUtils.uploadBannerSync(mOauthRetrofit, accessToken, accountName, resource);
            if (potentialError == null) {
                //Successful
                handler.post(() -> EventBus.getDefault().post(new SubmitChangeBannerEvent(true, "")));
                jobFinished(parameters, false);
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitChangeBannerEvent(false, potentialError)));
                jobFinished(parameters, false);
            }
        } catch (InterruptedException | ExecutionException | FileNotFoundException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitChangeBannerEvent(false, e.getLocalizedMessage())));
            jobFinished(parameters, false);
        }
    }

    @WorkerThread
    private void submitChangeAvatarSync(JobParameters parameters, String accessToken, Uri mediaUri, String accountName) {
        try {
            final CropTransformation avatarCrop = new CropTransformation(AVATAR_SIZE, AVATAR_SIZE, CropTransformation.CropType.CENTER);
            final Bitmap resource = Glide.with(this).asBitmap().skipMemoryCache(true)
                    .load(mediaUri).transform(avatarCrop).submit().get();
            String potentialError = EditProfileUtils.uploadAvatarSync(mOauthRetrofit, accessToken, accountName, resource);
            if (potentialError == null) {
                //Successful
                handler.post(() -> EventBus.getDefault().post(new SubmitChangeAvatarEvent(true, "")));
                jobFinished(parameters, false);
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitChangeAvatarEvent(false, potentialError)));
                jobFinished(parameters, false);
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitChangeAvatarEvent(false, e.getLocalizedMessage())));
            jobFinished(parameters, false);
        }
    }

    @WorkerThread
    private void submitSaveEditProfileSync(JobParameters parameters, @Nullable String accessToken,
                                           @NonNull String accountName,
                                           String displayName,
                                           String publicDesc
    ) {
        String potentialError = EditProfileUtils.updateProfileSync(mOauthRetrofit, accessToken, accountName,
                displayName, publicDesc);
        if (potentialError == null) {
            //Successful
            handler.post(() -> EventBus.getDefault().post(new SubmitSaveProfileEvent(true, "")));
            jobFinished(parameters, false);
        } else {
            handler.post(() -> EventBus.getDefault().post(new SubmitSaveProfileEvent(false, potentialError)));
            jobFinished(parameters, false);
        }
    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_SUBMIT_POST)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private int getWidthBanner(Uri mediaUri) throws FileNotFoundException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(mediaUri), null, options);
        return Math.max(Math.min(options.outWidth, MAX_BANNER_WIDTH), MIN_BANNER_WIDTH);
    }
}
