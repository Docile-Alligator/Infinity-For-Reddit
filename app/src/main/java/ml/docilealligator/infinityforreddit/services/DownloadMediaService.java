package ml.docilealligator.infinityforreddit.services;

import static android.os.Environment.getExternalStoragePublicDirectory;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import ml.docilealligator.infinityforreddit.DownloadProgressResponseBody;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.VideoLinkFetcher;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.apis.DownloadFile;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.broadcastreceivers.DownloadedMediaDeleteActionBroadcastReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.post.ImgurMedia;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DownloadMediaService extends JobService {
    public static final String EXTRA_URL = "EU";
    public static final String EXTRA_FILE_NAME = "EFN";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_MEDIA_TYPE = "EIG";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final String EXTRA_REDGIFS_ID = "EGI";
    public static final String EXTRA_STREAMABLE_SHORT_CODE = "ESSC";
    public static final String EXTRA_IS_ALL_GALLERY_MEDIA = "EIAGM";
    public static final int EXTRA_MEDIA_TYPE_IMAGE = 0;
    public static final int EXTRA_MEDIA_TYPE_GIF = 1;
    public static final int EXTRA_MEDIA_TYPE_VIDEO = 2;

    public static final String EXTRA_ALL_GALLERY_IMAGE_URLS = "EAGIU";
    public static final String EXTRA_ALL_GALLERY_IMAGE_MEDIA_TYPES = "EAGIMT";
    public static final String EXTRA_ALL_GALLERY_IMAGE_FILE_NAMES = "EAGIFN";

    private static final int NO_ERROR = -1;
    private static final int ERROR_CANNOT_GET_DESTINATION_DIRECTORY = 0;
    private static final int ERROR_FILE_CANNOT_DOWNLOAD = 1;
    private static final int ERROR_FILE_CANNOT_SAVE = 2;
    private static final int ERROR_FILE_CANNOT_FETCH_REDGIFS_VIDEO_LINK = 3;
    private static final int ERROR_CANNOT_FETCH_STREAMABLE_VIDEO_LINK = 4;
    private static final int ERROR_INVALID_ARGUMENT = 5;

    private static int JOB_ID = 20000;

    @Inject
    @Named("download_media")
    Retrofit retrofit;
    @Inject
    @Named("redgifs")
    Retrofit mRedgifsRetrofit;
    @Inject
    Provider<StreamableAPI> mStreamableApiProvider;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private NotificationManagerCompat notificationManager;

    public DownloadMediaService() {
    }

    /**
     *
     * @param context
     * @param contentEstimatedBytes
     * @param post
     * @param galleryIndex if post is not a gallery post, then galleryIndex should be 0
     * @return JobInfo for DownloadMediaService
     */
    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, Post post, int galleryIndex) {
        PersistableBundle extras = new PersistableBundle();
        if (post.getPostType() == Post.IMAGE_TYPE) {
            extras.putString(EXTRA_URL, post.getUrl());
            extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);
            extras.putString(EXTRA_FILE_NAME, post.getSubredditName()
                    + "-" + post.getId() + ".jpg");
            extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
            extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);
        } else if (post.getPostType() == Post.GIF_TYPE) {
            extras.putString(EXTRA_URL, post.getVideoUrl());
            extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_GIF);
            extras.putString(EXTRA_FILE_NAME, post.getSubredditName() + "-" + post.getId() + ".gif");
            extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
            extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);
        } else if (post.getPostType() == Post.VIDEO_TYPE) {
            if (post.isStreamable()) {
                if (post.isLoadedStreamableVideoAlready()) {
                    extras.putString(EXTRA_URL, post.getVideoUrl());
                } else {
                    extras.putString(EXTRA_STREAMABLE_SHORT_CODE, post.getStreamableShortCode());
                }

                extras.putString(EXTRA_FILE_NAME, "Streamable-" + post.getStreamableShortCode() + ".mp4");
            } else if (post.isRedgifs()) {
                extras.putString(EXTRA_URL, post.getVideoUrl());
                extras.putString(EXTRA_REDGIFS_ID, post.getRedgifsId());

                String redgifsId = post.getRedgifsId();
                if (redgifsId != null && redgifsId.contains("-")) {
                    redgifsId = redgifsId.substring(0, redgifsId.indexOf('-'));
                }
                extras.putString(EXTRA_FILE_NAME, "Redgifs-" + redgifsId + ".mp4");
            } else if (post.isImgur()) {
                extras.putString(EXTRA_URL, post.getVideoUrl());
                extras.putString(EXTRA_FILE_NAME, "Imgur-" + FilenameUtils.getName(post.getVideoUrl()));
            }

            extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_VIDEO);
            extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
            extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);
        } else if (post.getPostType() == Post.GALLERY_TYPE) {
            Post.Gallery media = post.getGallery().get(galleryIndex);
            if (media.mediaType == Post.Gallery.TYPE_VIDEO) {
                extras.putString(EXTRA_URL, media.url);
                extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_VIDEO);
                extras.putString(EXTRA_FILE_NAME, media.fileName);
                extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
                extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);
            } else {
                extras.putString(EXTRA_URL, media.hasFallback() ? media.fallbackUrl : media.url); // Retrieve original instead of the one additionally compressed by reddit
                extras.putInt(EXTRA_MEDIA_TYPE, media.mediaType == Post.Gallery.TYPE_GIF ? EXTRA_MEDIA_TYPE_GIF: EXTRA_MEDIA_TYPE_IMAGE);
                extras.putString(EXTRA_FILE_NAME, media.fileName);
                extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
                extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    public static JobInfo constructGalleryDownloadAllMediaJobInfo(Context context, long contentEstimatedBytes, Post post) {
        PersistableBundle extras = new PersistableBundle();
        if (post.getPostType() == Post.GALLERY_TYPE) {
            extras.putString(EXTRA_SUBREDDIT_NAME, post.getSubredditName());
            extras.putInt(EXTRA_IS_NSFW, post.isNSFW() ? 1 : 0);

            ArrayList<Post.Gallery> gallery = post.getGallery();

            StringBuilder concatUrlsBuilder = new StringBuilder();
            StringBuilder concatMediaTypesBuilder = new StringBuilder();
            StringBuilder concatFileNamesBuilder = new StringBuilder();

            for (int i = 0; i < gallery.size(); i++) {
                Post.Gallery media = gallery.get(i);

                if (media.mediaType == Post.Gallery.TYPE_VIDEO) {
                    concatUrlsBuilder.append(media.url).append(" ");
                    concatMediaTypesBuilder.append(EXTRA_MEDIA_TYPE_VIDEO).append(" ");
                    concatFileNamesBuilder.append(media.fileName).append(" ");
                } else {
                    concatUrlsBuilder.append(media.hasFallback() ? media.fallbackUrl : media.url).append(" "); // Retrieve original instead of the one additionally compressed by reddit
                    concatMediaTypesBuilder.append(media.mediaType == Post.Gallery.TYPE_GIF ? EXTRA_MEDIA_TYPE_GIF: EXTRA_MEDIA_TYPE_IMAGE).append(" ");
                    concatFileNamesBuilder.append(media.fileName).append(" ");
                }
            }

            if (concatUrlsBuilder.length() > 0) {
                concatUrlsBuilder.deleteCharAt(concatUrlsBuilder.length() - 1);
            }

            if (concatMediaTypesBuilder.length() > 0) {
                concatMediaTypesBuilder.deleteCharAt(concatMediaTypesBuilder.length() - 1);
            }

            if (concatFileNamesBuilder.length() > 0) {
                concatFileNamesBuilder.deleteCharAt(concatFileNamesBuilder.length() - 1);
            }

            extras.putString(EXTRA_ALL_GALLERY_IMAGE_URLS, concatUrlsBuilder.toString());
            extras.putString(EXTRA_ALL_GALLERY_IMAGE_MEDIA_TYPES, concatMediaTypesBuilder.toString());
            extras.putString(EXTRA_ALL_GALLERY_IMAGE_FILE_NAMES, concatFileNamesBuilder.toString());
            extras.putInt(EXTRA_IS_ALL_GALLERY_MEDIA, 1);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, ImgurMedia imgurMedia) {
        PersistableBundle extras = new PersistableBundle();
        extras.putString(EXTRA_URL, imgurMedia.getLink());
        extras.putString(EXTRA_FILE_NAME, imgurMedia.getFileName());
        if (imgurMedia.getType() == ImgurMedia.TYPE_VIDEO) {
            extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_VIDEO);
        } else {
            extras.putInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    public static JobInfo constructImgurAlbumDownloadAllMediaJobInfo(Context context, long contentEstimatedBytes, List<ImgurMedia> imgurMedia) {
        PersistableBundle extras = new PersistableBundle();

        StringBuilder concatUrlsBuilder = new StringBuilder();
        StringBuilder concatMediaTypesBuilder = new StringBuilder();
        StringBuilder concatFileNamesBuilder = new StringBuilder();

        for (int i = 0; i < imgurMedia.size(); i++) {
            ImgurMedia media = imgurMedia.get(i);

            if (media.getType() == ImgurMedia.TYPE_VIDEO) {
                concatUrlsBuilder.append(media.getLink()).append(" ");
                concatMediaTypesBuilder.append(EXTRA_MEDIA_TYPE_VIDEO).append(" ");
                concatFileNamesBuilder.append(media.getFileName()).append(" ");
            } else {
                concatUrlsBuilder.append(media.getLink()).append(" "); // Retrieve original instead of the one additionally compressed by reddit
                concatMediaTypesBuilder.append(EXTRA_MEDIA_TYPE_IMAGE).append(" ");
                concatFileNamesBuilder.append(media.getFileName()).append(" ");
            }
        }

        if (concatUrlsBuilder.length() > 0) {
            concatUrlsBuilder.deleteCharAt(concatUrlsBuilder.length() - 1);
        }

        if (concatMediaTypesBuilder.length() > 0) {
            concatMediaTypesBuilder.deleteCharAt(concatMediaTypesBuilder.length() - 1);
        }

        if (concatFileNamesBuilder.length() > 0) {
            concatFileNamesBuilder.deleteCharAt(concatFileNamesBuilder.length() - 1);
        }

        extras.putString(EXTRA_ALL_GALLERY_IMAGE_URLS, concatUrlsBuilder.toString());
        extras.putString(EXTRA_ALL_GALLERY_IMAGE_MEDIA_TYPES, concatMediaTypesBuilder.toString());
        extras.putString(EXTRA_ALL_GALLERY_IMAGE_FILE_NAMES, concatFileNamesBuilder.toString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, PersistableBundle extras) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadMediaService.class))
                    .setOverrideDeadline(0)
                    .setExtras(extras)
                    .build();
        }
    }

    @Override
    public void onCreate() {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        notificationManager = NotificationManagerCompat.from(this);
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        PersistableBundle extras = params.getExtras();
        int mediaType = extras.getInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getNotificationChannelId(mediaType));

        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        getNotificationChannelId(mediaType),
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(getNotificationChannel(mediaType))
                        .build();
        notificationManager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);
        String notificationTitle = extras.containsKey(EXTRA_FILE_NAME) ?
                extras.getString(EXTRA_FILE_NAME) :
                (extras.getInt(EXTRA_IS_ALL_GALLERY_MEDIA, 0) == 1 ?
                        getString(R.string.download_all_gallery_media_notification_title) : getString(R.string.download_all_imgur_album_media_notification_title));
        switch (extras.getInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE)) {
            case EXTRA_MEDIA_TYPE_GIF:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle),
                            JobService.JOB_END_NOTIFICATION_POLICY_DETACH);
                } else {
                    notificationManager.notify(NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle));
                }
                break;
            case EXTRA_MEDIA_TYPE_VIDEO:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle),
                            JobService.JOB_END_NOTIFICATION_POLICY_DETACH);
                } else {
                    notificationManager.notify(NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle));
                }
                break;
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                    setNotification(params,
                            NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle),
                            JobService.JOB_END_NOTIFICATION_POLICY_DETACH);
                } else {
                    notificationManager.notify(NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID + randomNotificationIdOffset,
                            createNotification(builder, notificationTitle));
                }
        }

        mExecutor.execute(() -> {
            String subredditName = extras.getString(EXTRA_SUBREDDIT_NAME);
            boolean isNsfw = extras.getInt(EXTRA_IS_NSFW, 0) == 1;

            if (extras.containsKey(EXTRA_ALL_GALLERY_IMAGE_URLS)) {
                // Download all images in a gallery post
                String concatUrls = extras.getString(EXTRA_ALL_GALLERY_IMAGE_URLS);
                String concatMediaTypes = extras.getString(EXTRA_ALL_GALLERY_IMAGE_MEDIA_TYPES);
                String concatFileNames = extras.getString(EXTRA_ALL_GALLERY_IMAGE_FILE_NAMES);

                String[] urls = concatUrls.split(" ");
                String[] mediaTypes = concatMediaTypes.split(" ");
                String[] fileNames = concatFileNames.split(" ");

                boolean allImagesDownloadedSuccessfully = true;
                for (int i = 0; i < urls.length; i++) {
                    String mimeType = Integer.parseInt(mediaTypes[i]) == EXTRA_MEDIA_TYPE_VIDEO ? "video/*" : "image/*";
                    int finalI = i;
                    allImagesDownloadedSuccessfully &= downloadMedia(params, urls[i], extras, builder, mediaType, randomNotificationIdOffset, fileNames[i],
                            mimeType, subredditName, isNsfw, true,
                            new DownloadProgressResponseBody.ProgressListener() {
                                long time = 0;
                                @Override
                                public void update(long bytesRead, long contentLength, boolean done) {
                                    if (!done) {
                                        if (contentLength != -1) {
                                            long currentTime = System.currentTimeMillis();
                                            if (currentTime - time > 1000) {
                                                time = currentTime;
                                                int currentMediaProgress = (int) (((float) bytesRead / contentLength + (float) finalI / urls.length) * 100);
                                                updateNotification(builder, mediaType, 0,
                                                        currentMediaProgress, randomNotificationIdOffset,
                                                        null, null);
                                            }
                                        }
                                    }
                                }
                            });
                }

                updateNotification(builder, mediaType,
                        allImagesDownloadedSuccessfully ? R.string.downloading_media_finished : R.string.download_gallery_failed_some_images,
                        -1, randomNotificationIdOffset,
                        null, null);
                jobFinished(params, false);
            } else {
                String fileUrl = extras.getString(EXTRA_URL);
                String fileName = extras.getString(EXTRA_FILE_NAME);
                String mimeType = mediaType == EXTRA_MEDIA_TYPE_VIDEO ? "video/*" : "image/*";

                downloadMedia(params, fileUrl, extras, builder, mediaType, randomNotificationIdOffset, fileName,
                        mimeType, subredditName, isNsfw, false, new DownloadProgressResponseBody.ProgressListener() {
                            long time = 0;
                            @Override
                            public void update(long bytesRead, long contentLength, boolean done) {
                                if (!done) {
                                    if (contentLength != -1) {
                                        long currentTime = System.currentTimeMillis();
                                        if (currentTime - time > 1000) {
                                            time = currentTime;
                                            updateNotification(builder, mediaType, 0,
                                                    (int) ((100 * bytesRead) / contentLength), randomNotificationIdOffset, null, null);
                                        }
                                    }
                                }
                            }
                        });
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    /**
     *
     * @param params
     * @param fileUrl
     * @param intent
     * @param builder
     * @param mediaType
     * @param randomNotificationIdOffset
     * @param fileName
     * @param mimeType
     * @param subredditName
     * @param isNsfw
     * @param multipleDownloads
     * @param progressListener
     * @return true if download succeeded or false otherwise.
     */
    private boolean downloadMedia(JobParameters params, String fileUrl, PersistableBundle intent,
                               NotificationCompat.Builder builder, int mediaType, int randomNotificationIdOffset,
                               String fileName, String mimeType, String subredditName, boolean isNsfw,
                               boolean multipleDownloads, DownloadProgressResponseBody.ProgressListener progressListener) {
        if (fileUrl == null) {
            // Only Redgifs and Streamble video can go inside this if clause.
            String redgifsId = intent.getString(EXTRA_REDGIFS_ID, null);
            String streamableShortCode = intent.getString(EXTRA_STREAMABLE_SHORT_CODE, null);

            if (redgifsId == null && streamableShortCode == null) {
                downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                        null,
                        ERROR_INVALID_ARGUMENT,
                        multipleDownloads);
                return false;
            }

            fileUrl = VideoLinkFetcher.fetchVideoLinkSync(mRedgifsRetrofit, mStreamableApiProvider, mCurrentAccountSharedPreferences,
                    redgifsId == null ? ViewVideoActivity.VIDEO_TYPE_STREAMABLE : ViewVideoActivity.VIDEO_TYPE_REDGIFS,
                    redgifsId, streamableShortCode);

            if (fileUrl == null) {
                downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                        null,
                        redgifsId == null ? ERROR_CANNOT_FETCH_STREAMABLE_VIDEO_LINK : ERROR_FILE_CANNOT_FETCH_REDGIFS_VIDEO_LINK,
                        multipleDownloads);
                return false;
            }
        }

        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(chain -> {
                    okhttp3.Response originalResponse = chain.proceed(chain.request());
                    return originalResponse.newBuilder()
                            .body(new DownloadProgressResponseBody(originalResponse.body(), progressListener))
                            .build();
                })
                .addInterceptor(chain -> chain.proceed(
                        chain.request()
                                .newBuilder()
                                .header("User-Agent", APIUtils.USER_AGENT)
                                .build()
                ))
                .build();

        retrofit = retrofit.newBuilder().client(client).build();

        boolean separateDownloadFolder = mSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_FOLDER_FOR_EACH_SUBREDDIT, false);

        Response<ResponseBody> response;
        String destinationFileUriString = null;
        boolean isDefaultDestination = true;
        try {
            response = retrofit.create(DownloadFile.class).downloadFile(fileUrl).execute();
            if (response.isSuccessful() && response.body() != null) {
                String destinationFileDirectory = getDownloadLocation(mediaType, isNsfw);
                if (destinationFileDirectory.equals("")) {
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                        File directory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        if (directory != null) {
                            String directoryPath = separateDownloadFolder && subredditName != null && !subredditName.equals("") ? directory.getAbsolutePath() + "/Infinity/" + subredditName + "/" : directory.getAbsolutePath() + "/Infinity/";
                            File infinityDir = new File(directoryPath);
                            if (!infinityDir.exists() && !infinityDir.mkdirs()) {
                                downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                        null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                                return false;
                            }
                            destinationFileUriString = directoryPath + fileName;
                        } else {
                            downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                    null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                            return false;
                        }
                    } else {
                        String dir = mediaType == EXTRA_MEDIA_TYPE_VIDEO ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
                        destinationFileUriString = separateDownloadFolder && subredditName != null && !subredditName.equals("") ? dir + "/Infinity/" + subredditName + "/" : dir + "/Infinity/";
                    }
                } else {
                    isDefaultDestination = false;
                    DocumentFile picFile;
                    DocumentFile dir;
                    if (separateDownloadFolder && subredditName != null && !subredditName.equals("")) {
                        dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory));
                        if (dir == null) {
                            downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                    null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                            return false;
                        }
                        dir = dir.findFile(subredditName);
                        if (dir == null) {
                            dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory)).createDirectory(subredditName);
                            if (dir == null) {
                                downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                        null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                                return false;
                            }
                        }
                    } else {
                        dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory));
                        if (dir == null) {
                            downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                    null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                            return false;
                        }
                    }
                    DocumentFile checkForDuplicates = dir.findFile(fileName);
                    int extensionPosition = fileName.lastIndexOf('.');
                    String extension = fileName.substring(extensionPosition);
                    int num = 1;
                    while (checkForDuplicates != null) {
                        fileName = fileName.substring(0, extensionPosition) + " (" + num + ")" + extension;
                        checkForDuplicates = dir.findFile(fileName);
                        num++;
                    }
                    picFile = dir.createFile(mimeType, fileName);
                    if (picFile == null) {
                        downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType,
                                null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, multipleDownloads);
                        return false;
                    }
                    destinationFileUriString = picFile.getUri().toString();
                }
            } else {
                downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType, null,
                        ERROR_FILE_CANNOT_DOWNLOAD, multipleDownloads);
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            downloadFinished(params, builder, mediaType, randomNotificationIdOffset, mimeType, null,
                    ERROR_FILE_CANNOT_DOWNLOAD, multipleDownloads);
            return false;
        }

        try {
            Uri destinationFileUri = writeResponseBodyToDisk(response.body(), isDefaultDestination, destinationFileUriString,
                    fileName, mediaType);
            downloadFinished(params, builder, mediaType, randomNotificationIdOffset,
                    mimeType, destinationFileUri, NO_ERROR, multipleDownloads);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            downloadFinished(params, builder, mediaType, randomNotificationIdOffset,
                    mimeType, null, ERROR_FILE_CANNOT_SAVE, multipleDownloads);
            return false;
        }
    }

    private Notification createNotification(NotificationCompat.Builder builder, String fileName) {
        builder.setContentTitle(fileName).setContentText(getString(R.string.downloading)).setProgress(100, 0, false);
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(NotificationCompat.Builder builder, int mediaType, int contentStringResId, int progress, int randomNotificationIdOffset,
                                    Uri mediaUri, String mimeType) {
        if (notificationManager != null) {
            if (progress < 0) {
                builder.setProgress(0, 0, false);
            } else {
                builder.setProgress(100, progress, false);
            }
            if (contentStringResId != 0) {
                builder.setContentText(getString(contentStringResId));
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(getString(contentStringResId)));
            }
            if (mediaUri != null) {
                int pendingIntentFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_CANCEL_CURRENT;

                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(mediaUri, mimeType);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getActivity(DownloadMediaService.this, 0, intent, pendingIntentFlags);
                builder.setContentIntent(pendingIntent);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, mediaUri);
                shareIntent.setType(mimeType);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intentAction = Intent.createChooser(shareIntent, getString(R.string.share));
                PendingIntent shareActionPendingIntent = PendingIntent.getActivity(this, 1, intentAction, pendingIntentFlags);

                builder.addAction(new NotificationCompat.Action(R.drawable.ic_notification, getString(R.string.share), shareActionPendingIntent));

                Intent deleteIntent = new Intent(this, DownloadedMediaDeleteActionBroadcastReceiver.class);
                deleteIntent.setData(mediaUri);
                deleteIntent.putExtra(DownloadedMediaDeleteActionBroadcastReceiver.EXTRA_NOTIFICATION_ID, getNotificationId(mediaType, randomNotificationIdOffset));
                PendingIntent deleteActionPendingIntent = PendingIntent.getBroadcast(this, 2, deleteIntent, pendingIntentFlags);
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_notification, getString(R.string.delete), deleteActionPendingIntent));
            }
            notificationManager.notify(getNotificationId(mediaType, randomNotificationIdOffset), builder.build());
        }
    }

    private String getNotificationChannelId(int mediaType) {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_GIF;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_VIDEO;
            default:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_IMAGE;
        }
    }

    private String getNotificationChannel(int mediaType) {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.CHANNEL_DOWNLOAD_GIF;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.CHANNEL_DOWNLOAD_VIDEO;
            default:
                return NotificationUtils.CHANNEL_DOWNLOAD_IMAGE;
        }
    }

    private int getNotificationId(int mediaType, int randomNotificationIdOffset) {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID + randomNotificationIdOffset;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset;
            default:
                return NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID + randomNotificationIdOffset;
        }
    }

    private String getDownloadLocation(int mediaType, boolean isNsfw) {
        if (isNsfw && mSharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_NSFW_MEDIA_IN_DIFFERENT_FOLDER, false)) {
            return mSharedPreferences.getString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, "");
        }
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return mSharedPreferences.getString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, "");
            case EXTRA_MEDIA_TYPE_VIDEO:
                return mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
            default:
                return mSharedPreferences.getString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, "");
        }
    }

    private Uri writeResponseBodyToDisk(ResponseBody body, boolean isDefaultDestination,
                                        String destinationFileUriString, String destinationFileName,
                                        int mediaType) throws IOException {
        ContentResolver contentResolver = getContentResolver();
        if (isDefaultDestination) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                InputStream inputStream = body.byteStream();
                OutputStream outputStream = new FileOutputStream(destinationFileUriString);
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                }

                outputStream.flush();
            } else {
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, destinationFileName);
                String mimeType;
                switch (mediaType) {
                    case EXTRA_MEDIA_TYPE_VIDEO:
                        mimeType = "video/mpeg";
                        break;
                    case EXTRA_MEDIA_TYPE_GIF:
                        mimeType = "image/gif";
                        break;
                    default:
                        mimeType = "image/jpeg";
                }
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destinationFileUriString);
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

                final Uri contentUri = mediaType == EXTRA_MEDIA_TYPE_VIDEO ? MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY) : MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri uri = contentResolver.insert(contentUri, contentValues);

                if (uri == null) {
                    throw new IOException("Failed to create new MediaStore record.");
                }

                OutputStream stream = contentResolver.openOutputStream(uri);

                if (stream == null) {
                    throw new IOException("Failed to get output stream.");
                }

                InputStream in = body.byteStream();
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    stream.write(buf, 0, len);
                }
                contentValues.clear();
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                contentResolver.update(uri, contentValues, null, null);
                destinationFileUriString = uri.toString();
            }
        } else {
            try (OutputStream stream = contentResolver.openOutputStream(Uri.parse(destinationFileUriString))) {
                if (stream == null) {
                    throw new IOException("Failed to get output stream.");
                }

                InputStream in = body.byteStream();

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    stream.write(buf, 0, len);
                }
            }
        }
        return Uri.parse(destinationFileUriString);
    }

    private void downloadFinished(JobParameters parameters, NotificationCompat.Builder builder, int mediaType,
                                  int randomNotificationIdOffset, String mimeType, Uri destinationFileUri,
                                  int errorCode, boolean multipleDownloads) {
        if (errorCode != NO_ERROR) {
            if (!multipleDownloads) {
                switch (errorCode) {
                    case ERROR_CANNOT_GET_DESTINATION_DIRECTORY:
                        updateNotification(builder, mediaType, R.string.downloading_image_or_gif_failed_cannot_get_destination_directory,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                    case ERROR_FILE_CANNOT_DOWNLOAD:
                        updateNotification(builder, mediaType, R.string.downloading_media_failed_cannot_download_media,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                    case ERROR_FILE_CANNOT_SAVE:
                        updateNotification(builder, mediaType, R.string.downloading_media_failed_cannot_save_to_destination_directory,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                    case ERROR_FILE_CANNOT_FETCH_REDGIFS_VIDEO_LINK:
                        updateNotification(builder, mediaType, R.string.download_media_failed_cannot_fetch_redgifs_url,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                    case ERROR_CANNOT_FETCH_STREAMABLE_VIDEO_LINK:
                        updateNotification(builder, mediaType, R.string.download_media_failed_cannot_fetch_streamable_url,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                    case ERROR_INVALID_ARGUMENT:
                        updateNotification(builder, mediaType, R.string.download_media_failed_invalid_argument,
                                -1, randomNotificationIdOffset, null, null);
                        break;
                }
            }
        } else {
            MediaScannerConnection.scanFile(
                    DownloadMediaService.this, new String[]{destinationFileUri.toString()}, null,
                    (path, uri) -> {
                        if (!multipleDownloads) {
                            updateNotification(builder, mediaType, R.string.downloading_media_finished, -1,
                                    randomNotificationIdOffset, destinationFileUri, mimeType);
                        }
                    }
            );
        }

        if (!multipleDownloads) {
            jobFinished(parameters, false);
        }
    }
}
