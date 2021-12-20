package ml.docilealligator.infinityforreddit.services;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;

import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.DownloadProgressResponseBody;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.DownloadFile;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.DownloadMediaEvent;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class DownloadMediaService extends Service {
    public static final String EXTRA_URL = "EU";
    public static final String EXTRA_FILE_NAME = "EFN";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_MEDIA_TYPE = "EIG";
    public static final String EXTRA_IS_NSFW = "EIN";
    public static final int EXTRA_MEDIA_TYPE_IMAGE = 0;
    public static final int EXTRA_MEDIA_TYPE_GIF = 1;
    public static final int EXTRA_MEDIA_TYPE_VIDEO = 2;

    private static final int NO_ERROR = -1;
    private static final int ERROR_CANNOT_GET_DESTINATION_DIRECTORY = 0;
    private static final int ERROR_FILE_CANNOT_DOWNLOAD = 1;
    private static final int ERROR_FILE_CANNOT_SAVE = 2;
    @Inject
    @Named("download_media")
    Retrofit retrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private NotificationManagerCompat notificationManager;
    private NotificationCompat.Builder builder;
    private ServiceHandler serviceHandler;

    public DownloadMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        private boolean downloadFinished;

        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            int randomNotificationIdOffset = msg.arg1;
            Bundle intent = msg.getData();
            downloadFinished = false;
            String fileUrl = intent.getString(EXTRA_URL);
            String fileName = intent.getString(EXTRA_FILE_NAME);
            String subredditName = intent.getString(EXTRA_SUBREDDIT_NAME);
            int mediaType = intent.getInt(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);
            boolean isNsfw = intent.getBoolean(EXTRA_IS_NSFW, false);
            String mimeType = mediaType == EXTRA_MEDIA_TYPE_VIDEO ? "video/*" : "image/*";

            final DownloadProgressResponseBody.ProgressListener progressListener = new DownloadProgressResponseBody.ProgressListener() {
                long time = 0;

                @Override public void update(long bytesRead, long contentLength, boolean done) {
                    if (!done) {
                        if (contentLength != -1) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - time > 1000) {
                                time = currentTime;
                                updateNotification(mediaType, 0,
                                        (int) ((100 * bytesRead) / contentLength), randomNotificationIdOffset, null);
                            }
                        }
                    }
                }
            };

            OkHttpClient client = new OkHttpClient.Builder()
                    .addNetworkInterceptor(chain -> {
                        okhttp3.Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new DownloadProgressResponseBody(originalResponse.body(), progressListener))
                                .build();
                    })
                    .build();

            retrofit = retrofit.newBuilder().client(client).build();

            boolean separateDownloadFolder = mSharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_FOLDER_FOR_EACH_SUBREDDIT, false);

            Response<ResponseBody> response = null;
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
                                    downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                            null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                                destinationFileUriString = directoryPath + fileName;
                            } else {
                                downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                        null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                return;
                            }
                        } else {
                            String dir = mediaType == EXTRA_MEDIA_TYPE_VIDEO ? Environment.DIRECTORY_MOVIES : Environment.DIRECTORY_PICTURES;
                            destinationFileUriString = separateDownloadFolder && subredditName != null && !subredditName.equals("") ? dir + "/Infinity/" + subredditName + "/" : dir + "/Infinity/";
                        }
                        isDefaultDestination = true;
                    } else {
                        isDefaultDestination = false;
                        DocumentFile picFile;
                        DocumentFile dir;
                        if (separateDownloadFolder && subredditName != null && !subredditName.equals("")) {
                            dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory));
                            if (dir == null) {
                                downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                        null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                return;
                            }
                            dir = dir.findFile(subredditName);
                            if (dir == null) {
                                dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory)).createDirectory(subredditName);
                                if (dir == null) {
                                    downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                            null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                            }
                        } else {
                            dir = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory));
                            if (dir == null) {
                                downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                        null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                return;
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
                            downloadFinished(mediaType, randomNotificationIdOffset, mimeType,
                                    null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                            return;
                        }
                        destinationFileUriString = picFile.getUri().toString();
                    }
                } else {
                    downloadFinished(mediaType, randomNotificationIdOffset, mimeType, null,
                            ERROR_FILE_CANNOT_DOWNLOAD);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (response != null && response.body() != null) {
                    Uri destinationFileUri = writeResponseBodyToDisk(response.body(), isDefaultDestination, destinationFileUriString,
                            fileName, mediaType);
                    downloadFinished(mediaType, randomNotificationIdOffset,
                            mimeType, destinationFileUri, NO_ERROR);
                }
            } catch (IOException e) {
                e.printStackTrace();
                downloadFinished(mediaType, randomNotificationIdOffset,
                        mimeType, null, ERROR_FILE_CANNOT_SAVE);
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
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mediaType == EXTRA_MEDIA_TYPE_VIDEO ? "video/*" : "image/*");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destinationFileUriString);
                    contentValues.put(mediaType == EXTRA_MEDIA_TYPE_VIDEO ? MediaStore.Video.Media.IS_PENDING : MediaStore.Images.Media.IS_PENDING, 1);

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
                    contentValues.put(mediaType == EXTRA_MEDIA_TYPE_VIDEO ? MediaStore.Video.Media.IS_PENDING : MediaStore.Images.Media.IS_PENDING, 0);
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

        private void downloadFinished(int mediaType, int randomNotificationIdOffset, String mimeType, Uri destinationFileUri, int errorCode) {
            if (downloadFinished) {
                return;
            }

            downloadFinished = true;
            if (errorCode != NO_ERROR) {
                switch (errorCode) {
                    case ERROR_CANNOT_GET_DESTINATION_DIRECTORY:
                        updateNotification(mediaType, R.string.downloading_image_or_gif_failed_cannot_get_destination_directory,
                                -1, randomNotificationIdOffset, null);
                        break;
                    case ERROR_FILE_CANNOT_DOWNLOAD:
                        updateNotification(mediaType, R.string.downloading_media_failed_cannot_download_media,
                                -1, randomNotificationIdOffset, null);
                        break;
                    case ERROR_FILE_CANNOT_SAVE:
                        updateNotification(mediaType, R.string.downloading_media_failed_cannot_save_to_destination_directory,
                                -1, randomNotificationIdOffset, null);
                        break;
                }
                EventBus.getDefault().post(new DownloadMediaEvent(false));
            } else {
                MediaScannerConnection.scanFile(
                        DownloadMediaService.this, new String[]{destinationFileUri.toString()}, null,
                        (path, uri) -> {
                            Intent intent = new Intent();
                            intent.setAction(android.content.Intent.ACTION_VIEW);
                            intent.setDataAndType(destinationFileUri, mimeType);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            PendingIntent pendingIntent = PendingIntent.getActivity(DownloadMediaService.this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                            updateNotification(mediaType, R.string.downloading_media_finished, -1,
                                    randomNotificationIdOffset, pendingIntent);
                            EventBus.getDefault().post(new DownloadMediaEvent(true));
                        }
                );
            }
            stopForeground(false);
        }
    }

    @Override
    public void onCreate() {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        notificationManager = NotificationManagerCompat.from(this);
        // Start up the thread running the service. Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block. We also make it
        // background priority so CPU-intensive work doesn't disrupt our UI.
        HandlerThread thread = new HandlerThread("ServiceStartArguments",
                Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();

        // Get the HandlerThread's Looper and use it for our Handler
        serviceHandler = new ServiceHandler(thread.getLooper());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);
        builder = new NotificationCompat.Builder(this, getNotificationChannelId(mediaType));

        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        getNotificationChannelId(mediaType),
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(getNotificationChannel(mediaType))
                        .build();
        notificationManager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);
        switch (intent.getIntExtra(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE)) {
            case EXTRA_MEDIA_TYPE_GIF:
                startForeground(
                        NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(intent.getStringExtra(EXTRA_FILE_NAME))
                );
                break;
            case EXTRA_MEDIA_TYPE_VIDEO:
                startForeground(
                        NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(intent.getStringExtra(EXTRA_FILE_NAME))
                );
                break;
            default:
                startForeground(
                        NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID + randomNotificationIdOffset,
                        createNotification(intent.getStringExtra(EXTRA_FILE_NAME))
                );
        }

        Message msg = serviceHandler.obtainMessage();
        Bundle bundle = intent.getExtras();
        msg.setData(bundle);
        msg.arg1 = randomNotificationIdOffset;
        serviceHandler.sendMessage(msg);

        return START_NOT_STICKY;
    }

    private Notification createNotification(String fileName) {
        builder.setContentTitle(fileName).setContentText(getString(R.string.downloading)).setProgress(100, 0, false);
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(int mediaType, int contentStringResId, int progress, int randomNotificationIdOffset,
                                    PendingIntent pendingIntent) {
        if (notificationManager != null) {
            if (progress < 0) {
                builder.setProgress(0, 0, false);
            } else {
                builder.setProgress(100, progress, false);
            }
            if (contentStringResId != 0) {
                builder.setContentText(getString(contentStringResId));
            }
            if (pendingIntent != null) {
                builder.setContentIntent(pendingIntent);
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
}
