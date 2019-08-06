package ml.docilealligator.infinityforreddit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import org.greenrobot.eventbus.EventBus;

import java.io.FileInputStream;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Named;

import retrofit2.Retrofit;

public class PostMediaService extends Service {
    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_CONTENT = "EC";
    static final String EXTRA_KIND = "EK";
    static final String EXTRA_FLAIR = "EF";
    static final String EXTRA_IS_SPOILER = "EIS";
    static final String EXTRA_IS_NSFW = "EIN";
    static final String EXTRA_POST_TYPE = "EPT";
    static final int EXTRA_POST_TEXT_OR_LINK = 0;
    static final int EXTRA_POST_TYPE_IMAGE = 1;
    static final int EXTRA_POST_TYPE_VIDEO = 2;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;

    @Inject
    @Named("upload_video")
    Retrofit mUploadVideoRetrofit;

    @Inject
    @Named("user_info")
    SharedPreferences mUserInfoSharedPreferences;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    public PostMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getmAppComponent().inject(this);

        String subredditName = intent.getExtras().getString(EXTRA_SUBREDDIT_NAME);
        String title = intent.getExtras().getString(EXTRA_TITLE);
        String flair = intent.getExtras().getString(EXTRA_FLAIR);
        boolean isSpoiler = intent.getExtras().getBoolean(EXTRA_IS_SPOILER);
        boolean isNSFW = intent.getExtras().getBoolean(EXTRA_IS_NSFW);
        int postType = intent.getExtras().getInt(EXTRA_POST_TYPE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtils.CHANNEL_POST_MEDIA,
                    NotificationUtils.CHANNEL_POST_MEDIA,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }

        if(postType == EXTRA_POST_TEXT_OR_LINK) {
            String content = intent.getExtras().getString(EXTRA_CONTENT);
            String kind = intent.getExtras().getString(EXTRA_KIND);
            startForeground(1, createNotification(R.string.posting));
            submitTextOrLinkPost(subredditName, title, content, flair, isSpoiler, isNSFW, kind);
        } else if(postType == EXTRA_POST_TYPE_IMAGE) {
            Uri imageUri = intent.getData();
            startForeground(1, createNotification(R.string.posting_image));
            submitImagePost(imageUri, subredditName, title, flair, isSpoiler, isNSFW);
        } else {
            Uri videoUri = intent.getData();
            startForeground(1, createNotification(R.string.posting_video));
            submitVideoPost(videoUri, subredditName, title, flair, isSpoiler, isNSFW);
        }

        return START_NOT_STICKY;
    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_POST_MEDIA)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .build();
    }

    private void submitTextOrLinkPost(String subredditName, String title, String content, String flair,
                                      boolean isSpoiler, boolean isNSFW, String kind) {
        SubmitPost.submitTextOrLinkPost(mOauthRetrofit, sharedPreferences, getResources().getConfiguration().locale,
                subredditName, title, content, flair, isSpoiler, isNSFW, kind, new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(true, post, null));

                        stopForeground(true);
                        stopSelf();
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(false, null, errorMessage));

                        stopForeground(true);
                        stopSelf();
                    }
                });
    }

    private void submitImagePost(Uri imageUri, String subredditName, String title, String flair,
                                 boolean isSpoiler, boolean isNSFW) {
        Glide.with(this)
                .asBitmap()
                .load(imageUri)
                .into(new CustomTarget<Bitmap>() {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        SubmitPost.submitImagePost(mOauthRetrofit, mUploadMediaRetrofit, sharedPreferences,
                                getResources().getConfiguration().locale, subredditName, title, resource,
                                flair, isSpoiler, isNSFW, new SubmitPost.SubmitPostListener() {
                                    @Override
                                    public void submitSuccessful(Post post) {
                                        EventBus.getDefault().post(new SubmitImagePostEvent(true, null));
                                        Toast.makeText(PostMediaService.this, R.string.image_is_processing, Toast.LENGTH_SHORT).show();

                                        stopForeground(true);
                                        stopSelf();
                                    }

                                    @Override
                                    public void submitFailed(@Nullable String errorMessage) {
                                        EventBus.getDefault().post(new SubmitImagePostEvent(false, errorMessage));

                                        stopForeground(true);
                                        stopSelf();
                                    }
                                });
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {

                    }
                });
    }

    private void submitVideoPost(Uri videoUri, String subredditName, String title, String flair,
                                 boolean isSpoiler, boolean isNSFW) {
        try (ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(videoUri, "r")) {
            FileInputStream in = new FileInputStream(pfd.getFileDescriptor());
            byte[] buffer;
            buffer = new byte[in.available()];
            while (in.read(buffer) != -1);

            Glide.with(this)
                    .asBitmap()
                    .load(videoUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            SubmitPost.submitVideoPost(mOauthRetrofit, mUploadMediaRetrofit, mUploadVideoRetrofit,
                                    sharedPreferences, getResources().getConfiguration().locale, subredditName, title,
                                    buffer, getContentResolver().getType(videoUri), resource, flair, isSpoiler, isNSFW,
                                    new SubmitPost.SubmitPostListener() {
                                        @Override
                                        public void submitSuccessful(Post post) {
                                            EventBus.getDefault().post(new SubmitVideoPostEvent(true, false, null));
                                            Toast.makeText(PostMediaService.this, R.string.video_is_processing, Toast.LENGTH_SHORT).show();

                                            stopForeground(true);
                                            stopSelf();
                                        }

                                        @Override
                                        public void submitFailed(@Nullable String errorMessage) {
                                            EventBus.getDefault().post(new SubmitVideoPostEvent(false, false, errorMessage));

                                            stopForeground(true);
                                            stopSelf();
                                        }
                                    });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
            EventBus.getDefault().post(new SubmitVideoPostEvent(false, true, null));

            stopForeground(true);
            stopSelf();
        }
    }
}
