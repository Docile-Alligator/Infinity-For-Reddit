package ml.docilealligator.infinityforreddit.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.API.DownloadFile;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.DownloadMediaEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NotificationUtils;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class DownloadMediaService extends Service {
    public static final String EXTRA_URL = "EU";
    public static final String EXTRA_FILE_NAME = "EFN";
    public static final String EXTRA_MEDIA_TYPE = "EIG";
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
    private int mediaType;

    public DownloadMediaService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private String getNotificationChannelId() {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_GIF;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_VIDEO;
            default:
                return NotificationUtils.CHANNEL_ID_DOWNLOAD_IMAGE;
        }
    }

    private String getNotificationChannel() {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.CHANNEL_DOWNLOAD_GIF;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.CHANNEL_DOWNLOAD_VIDEO;
            default:
                return NotificationUtils.CHANNEL_DOWNLOAD_IMAGE;
        }
    }

    private int getNotificationId() {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID;
            case EXTRA_MEDIA_TYPE_VIDEO:
                return NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID;
            default:
                return NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID;
        }
    }

    private String getDownloadLocation() {
        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                return mSharedPreferences.getString(SharedPreferencesUtils.GIF_DOWNLOAD_LOCATION, "");
            case EXTRA_MEDIA_TYPE_VIDEO:
                return mSharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
            default:
                return mSharedPreferences.getString(SharedPreferencesUtils.IMAGE_DOWNLOAD_LOCATION, "");
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        String fileUrl = intent.getStringExtra(EXTRA_URL);
        String fileName;
        fileName = intent.getStringExtra(EXTRA_FILE_NAME);
        mediaType = intent.getIntExtra(EXTRA_MEDIA_TYPE, EXTRA_MEDIA_TYPE_IMAGE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel;
            serviceChannel = new NotificationChannel(
                    getNotificationChannelId(),
                    getNotificationChannel(),
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(serviceChannel);
        }

        switch (mediaType) {
            case EXTRA_MEDIA_TYPE_GIF:
                startForeground(
                        NotificationUtils.DOWNLOAD_GIF_NOTIFICATION_ID,
                        createNotification(R.string.downloading_gif, fileName, null)
                );
                break;
            case EXTRA_MEDIA_TYPE_VIDEO:
                startForeground(
                        NotificationUtils.DOWNLOAD_VIDEO_NOTIFICATION_ID,
                        createNotification(R.string.downloading_video, fileName, null)
                );
                break;
            default:
                startForeground(
                        NotificationUtils.DOWNLOAD_IMAGE_NOTIFICATION_ID,
                        createNotification(R.string.downloading_image, fileName, null)
                );
        }

        retrofit.create(DownloadFile.class).downloadFile(fileUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String destinationFileDirectory = getDownloadLocation();
                    String destinationFileUriString;
                    boolean isDefaultDestination;
                    if (destinationFileDirectory.equals("")) {
                        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                            File directory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            if (directory != null) {
                                String directoryPath = directory.getAbsolutePath() + "/Infinity/";
                                File infinityDir = new File(directoryPath);
                                if (!infinityDir.exists() && !infinityDir.mkdir()) {
                                    downloadFinished(null, fileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                                destinationFileUriString = directoryPath + fileName;
                            } else {
                                downloadFinished(null, fileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                return;
                            }
                        } else {
                            destinationFileUriString = Environment.DIRECTORY_PICTURES + "/Infinity/";
                        }
                        isDefaultDestination = true;
                    } else {
                        isDefaultDestination = false;
                        DocumentFile picFile = DocumentFile.fromTreeUri(DownloadMediaService.this, Uri.parse(destinationFileDirectory)).createFile("image/*", fileName);
                        if (picFile == null) {
                            downloadFinished(null, fileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                            return;
                        }
                        destinationFileUriString = picFile.getUri().toString();
                    }

                    new SaveImageOrGifAndCopyToExternalStorageAsyncTask(response.body(), mediaType,
                            isDefaultDestination, fileName, destinationFileUriString, getContentResolver(),
                            new SaveImageOrGifAndCopyToExternalStorageAsyncTask.SaveImageOrGifAndCopyToExternalStorageAsyncTaskListener() {
                                @Override
                                public void finished(Uri destinationFileUri, int errorCode) {
                                    downloadFinished(destinationFileUri, fileName, errorCode);
                                }

                                @Override
                                public void updateProgressNotification(int stringResId) {
                                    updateNotification(stringResId, fileName, null);
                                }
                            }).execute();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                downloadFinished(null, fileName, ERROR_FILE_CANNOT_DOWNLOAD);
            }
        });
        return super.onStartCommand(intent, flags, startId);
    }

    private Notification createNotification(int stringResId, String fileName, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder;
        builder = new NotificationCompat.Builder(this, getNotificationChannelId());
        builder.setContentTitle(fileName).setContentText(getString(stringResId));
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(int stringResId, String fileName, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(getNotificationId(), createNotification(stringResId, fileName, pendingIntent));
        }
    }

    private void downloadFinished(Uri destinationFileUri, String fileName, int errorCode) {
        if (errorCode != NO_ERROR) {
            switch (errorCode) {
                case ERROR_CANNOT_GET_DESTINATION_DIRECTORY:
                    updateNotification(R.string.downloading_image_or_gif_failed_cannot_get_destination_directory, fileName, null);
                    break;
                case ERROR_FILE_CANNOT_DOWNLOAD:
                    updateNotification(R.string.downloading_media_failed_cannot_download_media, fileName, null);
                    break;
                case ERROR_FILE_CANNOT_SAVE:
                    updateNotification(R.string.downloading_media_failed_cannot_save_to_destination_directory, fileName, null);
                    break;
            }
            EventBus.getDefault().post(new DownloadMediaEvent(false));
        } else {
            MediaScannerConnection.scanFile(
                    this, new String[]{destinationFileUri.toString()}, null,
                    (path, uri) -> {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(destinationFileUri, "image/*");
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        updateNotification(R.string.downloading_media_finished, fileName, pendingIntent);
                        EventBus.getDefault().post(new DownloadMediaEvent(true));
                    }
            );
        }
        stopForeground(false);
    }

    private static class SaveImageOrGifAndCopyToExternalStorageAsyncTask extends AsyncTask<Void, Integer, Void> {

        private ResponseBody response;
        private int mediaType;
        private boolean isDefaultDestination;
        private String destinationFileName;
        @NonNull
        private String destinationFileUriString;
        private ContentResolver contentResolver;
        private SaveImageOrGifAndCopyToExternalStorageAsyncTaskListener saveImageOrGifAndCopyToExternalStorageAsyncTaskListener;
        private int errorCode = NO_ERROR;

        interface SaveImageOrGifAndCopyToExternalStorageAsyncTaskListener {
            void finished(Uri destinationFileUri, int errorCode);
            void updateProgressNotification(int stringResId);
        }

        public SaveImageOrGifAndCopyToExternalStorageAsyncTask(ResponseBody response, int mediaType,
                                                               boolean isDefaultDestination,
                                                               String destinationFileName,
                                                               @NonNull String destinationFileUriString,
                                                               ContentResolver contentResolver,
                                                               SaveImageOrGifAndCopyToExternalStorageAsyncTaskListener saveImageOrGifAndCopyToExternalStorageAsyncTaskListener) {
            this.response = response;
            this.mediaType = mediaType;
            this.isDefaultDestination = isDefaultDestination;
            this.destinationFileName = destinationFileName;
            this.destinationFileUriString = destinationFileUriString;
            this.contentResolver = contentResolver;
            this.saveImageOrGifAndCopyToExternalStorageAsyncTaskListener = saveImageOrGifAndCopyToExternalStorageAsyncTaskListener;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            saveImageOrGifAndCopyToExternalStorageAsyncTaskListener.updateProgressNotification(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            switch (mediaType) {
                case EXTRA_MEDIA_TYPE_IMAGE:
                    publishProgress(R.string.downloading_image_save_image);
                    break;
                case EXTRA_MEDIA_TYPE_GIF:
                    publishProgress(R.string.downloading_gif_save_gif);
                    break;
                case EXTRA_MEDIA_TYPE_VIDEO:
                    publishProgress(R.string.downloading_video_save_video);
            }

            try {
                writeResponseBodyToDisk(response);
            } catch (IOException e) {
                errorCode = ERROR_FILE_CANNOT_SAVE;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveImageOrGifAndCopyToExternalStorageAsyncTaskListener.finished(Uri.parse(destinationFileUriString), errorCode);
        }

        private void writeResponseBodyToDisk(ResponseBody body) throws IOException {
            if (isDefaultDestination) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
                    String relativeLocation = Environment.DIRECTORY_PICTURES + "/Infinity/";

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, destinationFileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/*");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 1);

                    final Uri contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
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
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
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
                    destinationFileUriString = destinationFileUriString.replaceAll("%3A", ":").replaceAll("%2F", "/");
                }
            }
        }
    }
}
