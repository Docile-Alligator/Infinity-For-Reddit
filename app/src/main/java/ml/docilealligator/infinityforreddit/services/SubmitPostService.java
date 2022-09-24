package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.AnyAccountAccessTokenAuthenticator;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitCrosspostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitGalleryPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitImagePostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitPollPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitVideoOrGifPostEvent;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.SubmitPost;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubmitPostService extends Service {
    public static final String EXTRA_ACCOUNT = "EA";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_TITLE = "ET";
    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_REDDIT_GALLERY_PAYLOAD = "ERGP";
    public static final String EXTRA_POLL_PAYLOAD = "EPP";
    public static final String EXTRA_KIND = "EK";
    public static final String EXTRA_FLAIR = "EF";
    public static final String EXTRA_IS_SPOILER = "EIS";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS = "ERPRN";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final int EXTRA_POST_TEXT_OR_LINK = 0;
    public static final int EXTRA_POST_TYPE_IMAGE = 1;
    public static final int EXTRA_POST_TYPE_VIDEO = 2;
    public static final int EXTRA_POST_TYPE_GALLERY = 3;
    public static final int EXTRA_POST_TYPE_POLL = 4;
    public static final int EXTRA_POST_TYPE_CROSSPOST = 5;

    private static final String EXTRA_MEDIA_URI = "EU";
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
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
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private Handler handler;
    private ServiceHandler serviceHandler;

    public SubmitPostService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            Account account = bundle.getParcelable(EXTRA_ACCOUNT);
            String subredditName = bundle.getString(EXTRA_SUBREDDIT_NAME);
            String title = bundle.getString(EXTRA_TITLE);
            Flair flair = bundle.getParcelable(EXTRA_FLAIR);
            boolean isSpoiler = bundle.getBoolean(EXTRA_IS_SPOILER, false);
            boolean isNSFW = bundle.getBoolean(EXTRA_IS_NSFW, false);
            boolean receivePostReplyNotifications = bundle.getBoolean(EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, true);
            int postType = bundle.getInt(EXTRA_POST_TYPE, EXTRA_POST_TEXT_OR_LINK);

            Retrofit newAuthenticatorOauthRetrofit = mOauthRetrofit.newBuilder().client(new OkHttpClient.Builder().authenticator(new AnyAccountAccessTokenAuthenticator(mRetrofit, mRedditDataRoomDatabase, account, mCurrentAccountSharedPreferences))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                    .build())
                    .build();

            if (postType == EXTRA_POST_TEXT_OR_LINK) {
                String content = bundle.getString(EXTRA_CONTENT);
                String kind = bundle.getString(EXTRA_KIND);
                submitTextOrLinkPost(newAuthenticatorOauthRetrofit, account, subredditName, title, content, flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications, kind);
            } else if (postType == EXTRA_POST_TYPE_CROSSPOST) {
                String content = bundle.getString(EXTRA_CONTENT);
                submitCrosspost(mExecutor, handler, newAuthenticatorOauthRetrofit, account, subredditName, title, content,
                        flair, isSpoiler, isNSFW, receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_IMAGE) {
                Uri mediaUri = Uri.parse(bundle.getString(EXTRA_MEDIA_URI));
                submitImagePost(newAuthenticatorOauthRetrofit, account, mediaUri, subredditName, title, flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_VIDEO) {
                Uri mediaUri = Uri.parse(bundle.getString(EXTRA_MEDIA_URI));
                submitVideoPost(newAuthenticatorOauthRetrofit, account, mediaUri, subredditName, title, flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_GALLERY) {
                submitGalleryPost(newAuthenticatorOauthRetrofit, account, bundle.getString(EXTRA_REDDIT_GALLERY_PAYLOAD));
            } else {
                submitPollPost(newAuthenticatorOauthRetrofit, account, bundle.getString(EXTRA_POLL_PAYLOAD));
            }
        }
    }

    @Override
    public void onCreate() {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        handler = new Handler();
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        NotificationUtils.CHANNEL_SUBMIT_POST,
                        NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(NotificationUtils.CHANNEL_SUBMIT_POST)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);
        int postType = intent.getIntExtra(EXTRA_POST_TYPE, EXTRA_POST_TEXT_OR_LINK);
        Bundle bundle = intent.getExtras();

        if (postType == EXTRA_POST_TEXT_OR_LINK) {
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset, createNotification(R.string.posting));
        } else if (postType == EXTRA_POST_TYPE_CROSSPOST) {
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset, createNotification(R.string.posting));
        } else if (postType == EXTRA_POST_TYPE_IMAGE) {
            bundle.putString(EXTRA_MEDIA_URI, intent.getData().toString());
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset, createNotification(R.string.posting_image));
        } else if (postType == EXTRA_POST_TYPE_VIDEO) {
            bundle.putString(EXTRA_MEDIA_URI, intent.getData().toString());
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset, createNotification(R.string.posting_video));
        } else {
            startForeground(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset, createNotification(R.string.posting_gallery));
        }

        Message msg = serviceHandler.obtainMessage();
        msg.setData(bundle);
        serviceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_SUBMIT_POST)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void submitTextOrLinkPost(Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, String subredditName, String title, String content,
                                      Flair flair, boolean isSpoiler, boolean isNSFW, boolean receivePostReplyNotifications,
                                      String kind) {
        SubmitPost.submitTextOrLinkPost(mExecutor, handler, newAuthenticatorOauthRetrofit, selectedAccount.getAccessToken(),
                subredditName, title, content, flair, isSpoiler,
                isNSFW, receivePostReplyNotifications, kind, new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(true, post, null)));

                        stopService();
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(false, null, errorMessage)));

                        stopService();
                    }
                });
    }

    private void submitCrosspost(Executor executor, Handler handler, Retrofit newAuthenticatorOauthRetrofit,
                                 Account selectedAccount, String subredditName,
                                 String title, String content, Flair flair, boolean isSpoiler, boolean isNSFW,
                                 boolean receivePostReplyNotifications) {
        SubmitPost.submitCrosspost(executor, handler, newAuthenticatorOauthRetrofit, selectedAccount.getAccessToken(), subredditName, title,
                content, flair, isSpoiler, isNSFW, receivePostReplyNotifications, APIUtils.KIND_CROSSPOST,
                new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitCrosspostEvent(true, post, null)));

                        stopService();
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitCrosspostEvent(false, null, errorMessage)));

                        stopService();
                    }
                });
    }

    private void submitImagePost(Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, Uri mediaUri, String subredditName, String title,
                                 Flair flair, boolean isSpoiler, boolean isNSFW, boolean receivePostReplyNotifications) {
        try {
            Bitmap resource = Glide.with(this).asBitmap().load(mediaUri).submit().get();
            SubmitPost.submitImagePost(mExecutor, handler, newAuthenticatorOauthRetrofit, mUploadMediaRetrofit,
                    selectedAccount.getAccessToken(), subredditName, title, resource, flair, isSpoiler, isNSFW, receivePostReplyNotifications,
                    new SubmitPost.SubmitPostListener() {
                        @Override
                        public void submitSuccessful(Post post) {
                            handler.post(() -> {
                                EventBus.getDefault().post(new SubmitImagePostEvent(true, null));
                                Toast.makeText(SubmitPostService.this, R.string.image_is_processing, Toast.LENGTH_SHORT).show();
                            });

                            stopService();
                        }

                        @Override
                        public void submitFailed(@Nullable String errorMessage) {
                            handler.post(() -> EventBus.getDefault().post(new SubmitImagePostEvent(false, errorMessage)));

                            stopService();
                        }
                    });
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitImagePostEvent(false, getString(R.string.error_processing_image))));
            stopService();
        }
    }

    private void submitVideoPost(Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, Uri mediaUri, String subredditName, String title,
                                 Flair flair, boolean isSpoiler, boolean isNSFW, boolean receivePostReplyNotifications) {
        try {
            InputStream in = getContentResolver().openInputStream(mediaUri);
            String type = getContentResolver().getType(mediaUri);
            String cacheFilePath;
            if (type != null && type.contains("gif")) {
                cacheFilePath = getExternalCacheDir() + "/" + mediaUri.getLastPathSegment() + ".gif";
            } else {
                cacheFilePath = getExternalCacheDir() + "/" + mediaUri.getLastPathSegment() + ".mp4";
            }

            copyFileToCache(in, cacheFilePath);

            Bitmap resource = Glide.with(this).asBitmap().load(mediaUri).submit().get();

            if (type != null) {
                SubmitPost.submitVideoPost(mExecutor, handler, newAuthenticatorOauthRetrofit, mUploadMediaRetrofit,
                        mUploadVideoRetrofit, selectedAccount.getAccessToken(), subredditName, title, new File(cacheFilePath),
                        type, resource, flair, isSpoiler, isNSFW, receivePostReplyNotifications,
                        new SubmitPost.SubmitPostListener() {
                            @Override
                            public void submitSuccessful(Post post) {
                                handler.post(() -> {
                                    EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(true, false, null));
                                    if (type.contains("gif")) {
                                        Toast.makeText(SubmitPostService.this, R.string.gif_is_processing, Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(SubmitPostService.this, R.string.video_is_processing, Toast.LENGTH_SHORT).show();
                                    }
                                });


                                stopService();
                            }

                            @Override
                            public void submitFailed(@Nullable String errorMessage) {
                                handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, false, errorMessage)));

                                stopService();
                            }
                        });
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, true, null)));

                stopService();
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, true, null)));

            stopService();
        }
    }

    private void submitGalleryPost(Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, String payload) {
        try {
            Response<String> response = newAuthenticatorOauthRetrofit.create(RedditAPI.class).submitGalleryPost(APIUtils.getOAuthHeader(selectedAccount.getAccessToken()), payload).execute();
            if (response.isSuccessful()) {
                JSONObject responseObject = new JSONObject(response.body()).getJSONObject(JSONUtils.JSON_KEY);
                if (responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
                    JSONArray error = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                            .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
                    if (error.length() != 0) {
                        String errorMessage;
                        if (error.length() >= 2) {
                            errorMessage = error.getString(1);
                        } else {
                            errorMessage = error.getString(0);
                        }
                        handler.post(() -> EventBus.getDefault().post(new SubmitGalleryPostEvent(false, null, errorMessage)));
                    } else {
                        handler.post(() -> EventBus.getDefault().post(new SubmitGalleryPostEvent(false, null, null)));
                    }
                } else {
                    String postUrl = responseObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.URL_KEY);
                    handler.post(() -> {
                        EventBus.getDefault().post(new SubmitGalleryPostEvent(true, postUrl, null));
                    });
                }
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitGalleryPostEvent(false, null, response.message())));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitGalleryPostEvent(false, null, e.getMessage())));
        } finally {
            stopService();
        }
    }

    private void submitPollPost(Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, String payload) {
        try {
            Response<String> response = newAuthenticatorOauthRetrofit.create(RedditAPI.class).submitPollPost(APIUtils.getOAuthHeader(selectedAccount.getAccessToken()), payload).execute();
            if (response.isSuccessful()) {
                JSONObject responseObject = new JSONObject(response.body()).getJSONObject(JSONUtils.JSON_KEY);
                if (responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() != 0) {
                    JSONArray error = responseObject.getJSONArray(JSONUtils.ERRORS_KEY)
                            .getJSONArray(responseObject.getJSONArray(JSONUtils.ERRORS_KEY).length() - 1);
                    if (error.length() != 0) {
                        String errorMessage;
                        if (error.length() >= 2) {
                            errorMessage = error.getString(1);
                        } else {
                            errorMessage = error.getString(0);
                        }
                        handler.post(() -> EventBus.getDefault().post(new SubmitPollPostEvent(false, null, errorMessage)));
                    } else {
                        handler.post(() -> EventBus.getDefault().post(new SubmitPollPostEvent(false, null, null)));
                    }
                } else {
                    String postUrl = responseObject.getJSONObject(JSONUtils.DATA_KEY).getString(JSONUtils.URL_KEY);
                    handler.post(() -> {
                        EventBus.getDefault().post(new SubmitPollPostEvent(true, postUrl, null));
                    });
                }
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitPollPostEvent(false, null, response.message())));
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitPollPostEvent(false, null, e.getMessage())));
        } finally {
            stopService();
        }
    }

    private static void copyFileToCache(InputStream fileInputStream, String destinationFilePath) throws IOException {
        OutputStream out = new FileOutputStream(destinationFilePath);
        byte[] buf = new byte[2048];
        int len;
        while ((len = fileInputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
    }

    private void stopService() {
        stopForeground(true);
        stopSelf();
    }
}
