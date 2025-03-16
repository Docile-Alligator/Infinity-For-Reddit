package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.PersistableBundle;
import android.widget.Toast;

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
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.network.AnyAccountAccessTokenAuthenticator;
import ml.docilealligator.infinityforreddit.subreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.UploadedImage;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SubmitCrosspostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitGalleryPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitImagePostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitPollPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitTextOrLinkPostEvent;
import ml.docilealligator.infinityforreddit.events.SubmitVideoOrGifPostEvent;
import ml.docilealligator.infinityforreddit.markdown.RichTextJSONConverter;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.SubmitPost;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SubmitPostService extends JobService {
    public static final String EXTRA_ACCOUNT = "EA";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_TITLE = "ET";
    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_IS_RICHTEXT_JSON = "EIRJ";
    public static final String EXTRA_UPLOADED_IMAGES = "EUI";
    public static final String EXTRA_URL = "EU";
    public static final String EXTRA_REDDIT_GALLERY_PAYLOAD = "ERGP";
    public static final String EXTRA_POLL_PAYLOAD = "EPP";
    public static final String EXTRA_KIND = "EK";
    public static final String EXTRA_FLAIR = "EF";
    public static final String EXTRA_IS_SPOILER = "EIS";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS = "ERPRN";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_MEDIA_URI = "EMU";
    public static final int EXTRA_POST_TEXT_OR_LINK = 0;
    public static final int EXTRA_POST_TYPE_IMAGE = 1;
    public static final int EXTRA_POST_TYPE_VIDEO = 2;
    public static final int EXTRA_POST_TYPE_GALLERY = 3;
    public static final int EXTRA_POST_TYPE_POLL = 4;
    public static final int EXTRA_POST_TYPE_CROSSPOST = 5;
    private static int JOB_ID = 1000;

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

    public SubmitPostService() {
    }

    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, PersistableBundle extras) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, SubmitPostService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, SubmitPostService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
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
        int postType = bundle.getInt(EXTRA_POST_TYPE, EXTRA_POST_TEXT_OR_LINK);

        if (postType == EXTRA_POST_TEXT_OR_LINK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting));
            }
        } else if (postType == EXTRA_POST_TYPE_CROSSPOST) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting));
            }
        } else if (postType == EXTRA_POST_TYPE_IMAGE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_image),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_image));
            }
        } else if (postType == EXTRA_POST_TYPE_VIDEO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_video),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_video));
            }
        } else if (postType == EXTRA_POST_TYPE_GALLERY) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_gallery),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_gallery));
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                setNotification(params,
                        NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_poll),
                        JobService.JOB_END_NOTIFICATION_POLICY_REMOVE);
            } else {
                manager.notify(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(R.string.posting_poll));
            }
        }

        mExecutor.execute(() -> {
            Account account = Account.fromJson(bundle.getString(EXTRA_ACCOUNT));
            String subredditName = bundle.getString(EXTRA_SUBREDDIT_NAME);
            String title = bundle.getString(EXTRA_TITLE);
            Flair flair = Flair.fromJson(bundle.getString(EXTRA_FLAIR));
            boolean isSpoiler = bundle.getInt(EXTRA_IS_SPOILER, 0) == 1;
            boolean isNSFW = bundle.getInt(EXTRA_IS_NSFW, 0) == 1;
            boolean receivePostReplyNotifications = bundle.getInt(EXTRA_RECEIVE_POST_REPLY_NOTIFICATIONS, 1) == 1;

            Retrofit newAuthenticatorOauthRetrofit = mOauthRetrofit.newBuilder().client(new OkHttpClient.Builder().authenticator(new AnyAccountAccessTokenAuthenticator(mRetrofit, mRedditDataRoomDatabase, account, mCurrentAccountSharedPreferences))
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .writeTimeout(30, TimeUnit.SECONDS)
                            .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                            .build())
                    .build();

            if (postType == EXTRA_POST_TEXT_OR_LINK) {
                String content = bundle.getString(EXTRA_CONTENT, "");
                boolean isRichTextJSON = bundle.getInt(EXTRA_IS_RICHTEXT_JSON, 0) == 1;
                String kind = bundle.getString(EXTRA_KIND);
                if (isRichTextJSON) {
                    List<UploadedImage> uploadedImages = UploadedImage.fromListJson(bundle.getString(EXTRA_UPLOADED_IMAGES));
                    try {
                        content = new RichTextJSONConverter().constructRichTextJSON(SubmitPostService.this, content, uploadedImages);
                    } catch (JSONException e) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(false, null, getString(R.string.convert_to_richtext_json_failed))));
                        return;
                    }
                }
                submitTextOrLinkPost(params, manager, randomNotificationIdOffset, newAuthenticatorOauthRetrofit,
                        account, subredditName, title, content, bundle.getString(EXTRA_URL), flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications, isRichTextJSON, kind);
            } else if (postType == EXTRA_POST_TYPE_CROSSPOST) {
                submitCrosspost(params, manager, randomNotificationIdOffset, mExecutor, handler,
                        newAuthenticatorOauthRetrofit, account, subredditName, title, bundle.getString(EXTRA_CONTENT),
                        flair, isSpoiler, isNSFW, receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_IMAGE) {
                Uri mediaUri = Uri.parse(bundle.getString(EXTRA_MEDIA_URI));
                submitImagePost(params, manager, randomNotificationIdOffset, newAuthenticatorOauthRetrofit, account,
                        mediaUri, subredditName, title, bundle.getString(EXTRA_CONTENT), flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_VIDEO) {
                Uri mediaUri = Uri.parse(bundle.getString(EXTRA_MEDIA_URI));
                submitVideoPost(params, manager, randomNotificationIdOffset, newAuthenticatorOauthRetrofit, account,
                        mediaUri, subredditName, title, bundle.getString(EXTRA_CONTENT), flair, isSpoiler, isNSFW,
                        receivePostReplyNotifications);
            } else if (postType == EXTRA_POST_TYPE_GALLERY) {
                submitGalleryPost(params, manager, randomNotificationIdOffset, newAuthenticatorOauthRetrofit,
                        account, bundle.getString(EXTRA_REDDIT_GALLERY_PAYLOAD));
            } else {
                submitPollPost(params, manager, randomNotificationIdOffset, newAuthenticatorOauthRetrofit, account,
                        bundle.getString(EXTRA_POLL_PAYLOAD));
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Override
    public void onCreate() {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        handler = new Handler();
    }

    private Notification createNotification(int stringResId) {
        return new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_SUBMIT_POST)
                .setContentTitle(getString(stringResId))
                .setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void submitTextOrLinkPost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                      Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount,
                                      String subredditName, String title, String content, @Nullable String url,
                                      Flair flair, boolean isSpoiler, boolean isNSFW,
                                      boolean receivePostReplyNotifications, boolean isRichtextJSON,
                                      String kind) {
        SubmitPost.submitTextOrLinkPost(mExecutor, handler, newAuthenticatorOauthRetrofit, selectedAccount.getAccessToken(),
                subredditName, title, content, url, flair, isSpoiler,
                isNSFW, receivePostReplyNotifications, isRichtextJSON, kind, new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(true, post, null)));

                        stopJob(parameters, manager, randomNotificationIdOffset);
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitTextOrLinkPostEvent(false, null, errorMessage)));

                        stopJob(parameters, manager, randomNotificationIdOffset);
                    }
                });
    }

    private void submitCrosspost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                 Executor executor, Handler handler, Retrofit newAuthenticatorOauthRetrofit,
                                 Account selectedAccount, String subredditName,
                                 String title, String content, Flair flair, boolean isSpoiler, boolean isNSFW,
                                 boolean receivePostReplyNotifications) {
        SubmitPost.submitCrosspost(executor, handler, newAuthenticatorOauthRetrofit, selectedAccount.getAccessToken(), subredditName, title,
                content, flair, isSpoiler, isNSFW, receivePostReplyNotifications, APIUtils.KIND_CROSSPOST,
                new SubmitPost.SubmitPostListener() {
                    @Override
                    public void submitSuccessful(Post post) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitCrosspostEvent(true, post, null)));

                        stopJob(parameters, manager, randomNotificationIdOffset);
                    }

                    @Override
                    public void submitFailed(@Nullable String errorMessage) {
                        handler.post(() -> EventBus.getDefault().post(new SubmitCrosspostEvent(false, null, errorMessage)));

                        stopJob(parameters, manager, randomNotificationIdOffset);
                    }
                });
    }

    private void submitImagePost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                 Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, Uri mediaUri,
                                 String subredditName, String title, String content, Flair flair,
                                 boolean isSpoiler, boolean isNSFW, boolean receivePostReplyNotifications) {
        try {
            Bitmap resource = Glide.with(this).asBitmap().load(mediaUri).submit().get();
            SubmitPost.submitImagePost(mExecutor, handler, newAuthenticatorOauthRetrofit, mUploadMediaRetrofit,
                    selectedAccount.getAccessToken(), subredditName, title, content, resource, flair, isSpoiler, isNSFW, receivePostReplyNotifications,
                    new SubmitPost.SubmitPostListener() {
                        @Override
                        public void submitSuccessful(Post post) {
                            handler.post(() -> {
                                EventBus.getDefault().post(new SubmitImagePostEvent(true, null));
                                Toast.makeText(SubmitPostService.this, R.string.image_is_processing, Toast.LENGTH_SHORT).show();
                            });

                            stopJob(parameters, manager, randomNotificationIdOffset);
                        }

                        @Override
                        public void submitFailed(@Nullable String errorMessage) {
                            handler.post(() -> EventBus.getDefault().post(new SubmitImagePostEvent(false, errorMessage)));

                            stopJob(parameters, manager, randomNotificationIdOffset);
                        }
                    });
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitImagePostEvent(false, getString(R.string.error_processing_image))));
            stopJob(parameters, manager, randomNotificationIdOffset);
        }
    }

    private void submitVideoPost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                 Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, Uri mediaUri,
                                 String subredditName, String title, String content, Flair flair,
                                 boolean isSpoiler, boolean isNSFW, boolean receivePostReplyNotifications) {
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
                        mUploadVideoRetrofit, selectedAccount.getAccessToken(), subredditName, title, content,
                        new File(cacheFilePath), type, resource, flair, isSpoiler, isNSFW, receivePostReplyNotifications,
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


                                stopJob(parameters, manager, randomNotificationIdOffset);
                            }

                            @Override
                            public void submitFailed(@Nullable String errorMessage) {
                                handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, false, errorMessage)));

                                stopJob(parameters, manager, randomNotificationIdOffset);
                            }
                        });
            } else {
                handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, true, null)));

                stopJob(parameters, manager, randomNotificationIdOffset);
            }
        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
            handler.post(() -> EventBus.getDefault().post(new SubmitVideoOrGifPostEvent(false, true, null)));

            stopJob(parameters, manager, randomNotificationIdOffset);
        }
    }

    private void submitGalleryPost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                   Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, String payload) {
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
            stopJob(parameters, manager, randomNotificationIdOffset);
        }
    }

    private void submitPollPost(JobParameters parameters, NotificationManagerCompat manager, int randomNotificationIdOffset,
                                Retrofit newAuthenticatorOauthRetrofit, Account selectedAccount, String payload) {
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
            stopJob(parameters, manager, randomNotificationIdOffset);
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

    private void stopJob(JobParameters parameters, NotificationManagerCompat notificationManager, int randomNotificationIdOffset) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            notificationManager.cancel(NotificationUtils.SUBMIT_POST_SERVICE_NOTIFICATION_ID + randomNotificationIdOffset);
        }
        jobFinished(parameters, false);
    }
}
