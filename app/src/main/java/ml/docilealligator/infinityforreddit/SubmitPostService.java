package ml.docilealligator.infinityforreddit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Event.SubmitImagePostEvent;
import ml.docilealligator.infinityforreddit.Event.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.Event.SubmitVideoPostEvent;
import retrofit2.Retrofit;

public class SubmitPostService extends Service {
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_TITLE = "ET";
    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_KIND = "EK";
    public static final String EXTRA_FLAIR = "EF";
    public static final String EXTRA_IS_SPOILER = "EIS";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final int EXTRA_POST_TEXT_OR_LINK = 0;
    public static final int EXTRA_POST_TYPE_IMAGE = 1;
    public static final int EXTRA_POST_TYPE_VIDEO = 2;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;
    @Inject
    @Named("upload_video")
    Retrofit mUploadVideoRetrofit;
    private String mAccessToken;
    private String subredditName;
    private String title;
    private String flair;
    private boolean isSpoiler;
    private boolean isNSFW;
    private String content;
    private String kind;
    private Uri mediaUri;

    public SubmitPostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        mAccessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN);
        subredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);
        title = intent.getStringExtra(EXTRA_TITLE);
        flair = intent.getStringExtra(EXTRA_FLAIR);
        isSpoiler = intent.getBooleanExtra(EXTRA_IS_SPOILER, false);
        isNSFW = intent.getBooleanExtra(EXTRA_IS_NSFW, false);
        int postType = intent.getIntExtra(EXTRA_POST_TYPE, EXTRA_POST_TEXT_OR_LINK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtils.CHANNEL_SUBMIT_POST,
                    NotificationUtils.CHANNEL_SUBMIT_POST,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(serviceChannel);
        }

        if (postType == EXTRA_POST_TEXT_OR_LINK) {
            content = intent.getStringExtra(EXTRA_CONTENT);
            kind = intent.getStringExtra(EXTRA_KIND);
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID, createNotification(R.string.posting));
            submitTextOrLinkPost();
        } else if (postType == EXTRA_POST_TYPE_IMAGE) {
            mediaUri = intent.getData();
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID, createNotification(R.string.posting_image));
            submitImagePost();
        } else {
            mediaUri = intent.getData();
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID, createNotification(R.string.posting_video));
            submitVideoPost();
        }

        return START_NOT_STICKY;
    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_SUBMIT_POST)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(getResources().getColor(R.color.notificationIconColor))
                .build();
    }

    private void submitTextOrLinkPost() {
        SubmitPost.submitTextOrLinkPost(mOauthRetrofit, mAccessToken, getResources().getConfiguration().locale,
                subredditName, title, content, flair, isSpoiler, isNSFW, kind, new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(true, post, null));

                        stopService();
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(false, null, errorMessage));

                        stopService();
                    }
                });
    }

    private void submitImagePost() {
        Glide.with(this)
                .asBitmap()
                .load(mediaUri)
                .into(new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        SubmitPost.submitImagePost(mOauthRetrofit, mUploadMediaRetrofit, mAccessToken,
                                getResources().getConfiguration().locale, subredditName, title, resource,
                                flair, isSpoiler, isNSFW, new SubmitPost.SubmitPostListener() {
                                    @Override
                                    public void submitSuccessful(Post post) {
                                        EventBus.getDefault().post(new SubmitImagePostEvent(true, null));
                                        Toast.makeText(SubmitPostService.this, R.string.image_is_processing, Toast.LENGTH_SHORT).show();

                                        stopService();
                                    }

                                    @Override
                                    public void submitFailed(@Nullable String errorMessage) {
                                        EventBus.getDefault().post(new SubmitImagePostEvent(false, errorMessage));

                                        stopService();
                                    }
                                });
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        EventBus.getDefault().post(new SubmitImagePostEvent(false, getString(R.string.error_processing_image)));

                        stopService();
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void submitVideoPost() {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(mediaUri, "r")) {
            if (pfd != null) {
                FileInputStream in = new FileInputStream(pfd.getFileDescriptor());
                byte[] buffer;
                buffer = new byte[in.available()];
                while (in.read(buffer) != -1) ;

                Glide.with(this)
                        .asBitmap()
                        .load(mediaUri)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                String type = getContentResolver().getType(mediaUri);
                                if (type != null) {
                                    SubmitPost.submitVideoPost(mOauthRetrofit, mUploadMediaRetrofit, mUploadVideoRetrofit,
                                            mAccessToken, getResources().getConfiguration().locale, subredditName, title,
                                            buffer, type, resource, flair, isSpoiler, isNSFW,
                                            new SubmitPost.SubmitPostListener() {
                                                @Override
                                                public void submitSuccessful(Post post) {
                                                    EventBus.getDefault().post(new SubmitVideoPostEvent(true, false, null));
                                                    Toast.makeText(SubmitPostService.this, R.string.video_is_processing, Toast.LENGTH_SHORT).show();

                                                    stopService();
                                                }

                                                @Override
                                                public void submitFailed(@Nullable String errorMessage) {
                                                    EventBus.getDefault().post(new SubmitVideoPostEvent(false, false, errorMessage));

                                                    stopService();
                                                }
                                            });
                                } else {
                                    EventBus.getDefault().post(new SubmitVideoPostEvent(false, true, null));
                                }
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                EventBus.getDefault().post(new SubmitVideoPostEvent(false, true, null));

                                stopService();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            } else {
                EventBus.getDefault().post(new SubmitVideoPostEvent(false, true, null));
            }
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new SubmitVideoPostEvent(false, true, null));

            stopService();
        }
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }
}
