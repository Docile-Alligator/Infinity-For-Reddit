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
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.API.DownloadFile;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.DownloadProgressResponseBody;
import ml.docilealligator.infinityforreddit.Event.DownloadRedditVideoEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.NotificationUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static android.os.Environment.getExternalStoragePublicDirectory;

public class DownloadRedditVideoService extends Service {

    public static final String EXTRA_VIDEO_URL = "EVU";
    public static final String EXTRA_SUBREDDIT = "ES";
    public static final String EXTRA_POST_ID = "EPI";

    private static final int NO_ERROR = -1;
    private static final int ERROR_CANNOT_GET_CACHE_DIRECTORY = 0;
    private static final int ERROR_VIDEO_FILE_CANNOT_DOWNLOAD = 1;
    private static final int ERROR_VIDEO_FILE_CANNOT_SAVE = 2;
    private static final int ERROR_AUDIO_FILE_CANNOT_SAVE = 3;
    private static final int ERROR_MUX_FAILED = 4;
    private static final int ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE = 5;
    private static final int ERROR_CANNOT_GET_DESTINATION_DIRECTORY = 6;

    @Inject
    @Named("download_media")
    Retrofit retrofit;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    String resultFile;

    public DownloadRedditVideoService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        ((Infinity) getApplication()).getAppComponent().inject(this);


        final DownloadProgressResponseBody.ProgressListener progressListener = new DownloadProgressResponseBody.ProgressListener() {
            boolean firstUpdate = true;

            @Override public void update(long bytesRead, long contentLength, boolean done) {
                if (done) {
                    Log.i("adfasdf", "completed");
                } else {
                    if (firstUpdate) {
                        firstUpdate = false;
                        if (contentLength == -1) {
                            Log.i("adfasdf", "content-length: unknown");
                        } else {
                            Log.i("adfasdf", "content-length: " + contentLength);
                        }
                    }

                    Log.i("adfasdf", "bytes read " + bytesRead);

                    if (contentLength != -1) {
                        Log.i("adfasdf", "progress: " + ((100 * bytesRead) / contentLength));
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




        String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
        String audioUrl = videoUrl.substring(0, videoUrl.lastIndexOf('/')) + "/DASH_audio.mp4";
        String subredditName = intent.getStringExtra(EXTRA_SUBREDDIT);
        final String[] fileNameWithoutExtension = {subredditName + "-" + intent.getStringExtra(EXTRA_POST_ID)};

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel;
            serviceChannel = new NotificationChannel(
                    NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO,
                    NotificationUtils.CHANNEL_DOWNLOAD_REDDIT_VIDEO,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(serviceChannel);
        }

        startForeground(
                NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                createNotification(R.string.downloading_reddit_video, fileNameWithoutExtension[0] + ".mp4", null)
        );

        DownloadFile downloadFile = retrofit.create(DownloadFile.class);

        boolean separateDownloadFolder = sharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_FOLDER_FOR_EACH_SUBREDDIT, false);

        File directory = getExternalCacheDir();
        String destinationFileName = fileNameWithoutExtension[0] + ".mp4";
        if (directory != null) {
            String directoryPath = directory.getAbsolutePath() + "/";
            downloadFile.downloadFile(videoUrl).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> videoResponse) {
                    if (videoResponse.isSuccessful() && videoResponse.body() != null) {
                        String destinationFileDirectory = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
                        String destinationFileUriString;
                        boolean isDefaultDestination;
                        if (destinationFileDirectory.equals("")) {
                            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                File directory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                if (directory != null) {
                                    String directoryPath = separateDownloadFolder ? directory.getAbsolutePath() + "/Infinity/" + subredditName + "/" : directory.getAbsolutePath() + "/Infinity/";
                                    File infinityDir = new File(directoryPath);
                                    if (!infinityDir.exists() && !infinityDir.mkdir()) {
                                        downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                        return;
                                    }
                                    destinationFileUriString = directoryPath + destinationFileName;
                                } else {
                                    downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                            } else {
                                destinationFileUriString = separateDownloadFolder ? Environment.DIRECTORY_PICTURES + "/Infinity/" + subredditName + "/" : Environment.DIRECTORY_PICTURES + "/Infinity/";
                            }
                            isDefaultDestination = true;
                        } else {
                            isDefaultDestination = false;
                            DocumentFile picFile;
                            DocumentFile dir;
                            if (separateDownloadFolder) {
                                dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory));
                                if (dir == null) {
                                    downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                                dir = dir.findFile(subredditName);
                                if (dir == null) {
                                    dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory)).createDirectory(subredditName);
                                    if (dir == null) {
                                        downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                        return;
                                    }
                                }
                            } else {
                                dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory));
                                if (dir == null) {
                                    downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                    return;
                                }
                            }
                            DocumentFile checkForDuplicates = dir.findFile(destinationFileName);
                            int num = 1;
                            while (checkForDuplicates != null) {
                                fileNameWithoutExtension[0] = fileNameWithoutExtension[0] + " (" + num + ")";
                                checkForDuplicates = dir.findFile(fileNameWithoutExtension[0] + ".mp4");
                                num++;
                            }
                            picFile = dir.createFile("video/*", fileNameWithoutExtension[0] + ".mp4");
                            if (picFile == null) {
                                downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_DESTINATION_DIRECTORY);
                                return;
                            }
                            destinationFileUriString = picFile.getUri().toString();
                        }

                        updateNotification(R.string.downloading_reddit_video_audio_track, destinationFileName, null);
                        downloadFile.downloadFile(audioUrl).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> audioResponse) {
                                if (audioResponse.isSuccessful() && audioResponse.body() != null) {
                                    String videoFilePath = directoryPath + fileNameWithoutExtension[0] + "-cache.mp4";
                                    String audioFilePath = directoryPath + fileNameWithoutExtension[0] + "-cache.mp3";
                                    String outputFilePath = directoryPath + fileNameWithoutExtension[0] + ".mp4";
                                    new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                            audioResponse.body(), videoFilePath, audioFilePath, outputFilePath,
                                            destinationFileName, destinationFileUriString, isDefaultDestination,
                                            getContentResolver(),
                                            new SaveTempMuxAndCopyAsyncTask.SaveTempMuxAndCopyAsyncTaskListener() {
                                                @Override
                                                public void finished(Uri destinationFileUri, int errorCode) {
                                                    new File(videoFilePath).delete();
                                                    new File(audioFilePath).delete();
                                                    new File(outputFilePath).delete();
                                                    downloadFinished(destinationFileUri, destinationFileName, errorCode);
                                                }

                                                @Override
                                                public void updateProgressNotification(int stringResId) {
                                                    updateNotification(stringResId, destinationFileName, null);
                                                }
                                            }).execute();
                                } else {
                                    String videoFilePath = directoryPath + fileNameWithoutExtension[0] + "-cache.mp4";
                                    new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                            null, videoFilePath, null, null,
                                            destinationFileName, destinationFileUriString, isDefaultDestination,
                                            getContentResolver(),
                                            new SaveTempMuxAndCopyAsyncTask.SaveTempMuxAndCopyAsyncTaskListener() {
                                                @Override
                                                public void finished(Uri destinationFileUri, int errorCode) {
                                                    new File(videoFilePath).delete();
                                                    downloadFinished(destinationFileUri, destinationFileName, errorCode);
                                                }

                                                @Override
                                                public void updateProgressNotification(int stringResId) {
                                                    updateNotification(stringResId, destinationFileName, null);
                                                }
                                            }).execute();
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                String videoFilePath = directoryPath + fileNameWithoutExtension[0] + "-cache.mp4";
                                new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                        null, videoFilePath, null, null,
                                        destinationFileName, destinationFileUriString, isDefaultDestination,
                                        getContentResolver(),
                                        new SaveTempMuxAndCopyAsyncTask.SaveTempMuxAndCopyAsyncTaskListener() {
                                            @Override
                                            public void finished(Uri destinationFileUri, int errorCode) {
                                                new File(videoFilePath).delete();
                                                downloadFinished(destinationFileUri, destinationFileName, errorCode);
                                            }

                                            @Override
                                            public void updateProgressNotification(int stringResId) {
                                                updateNotification(stringResId, destinationFileName, null);
                                            }
                                        }).execute();
                            }
                        });
                    } else {
                        downloadFinished(null, destinationFileName, ERROR_VIDEO_FILE_CANNOT_DOWNLOAD);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    downloadFinished(null, destinationFileName, ERROR_VIDEO_FILE_CANNOT_DOWNLOAD);
                }
            });
        } else {
            downloadFinished(null, destinationFileName, ERROR_CANNOT_GET_CACHE_DIRECTORY);
        }

        return START_NOT_STICKY;
    }

    private void downloadFinished(Uri destinationFileUri, String fileName, int errorCode) {
        if (errorCode != NO_ERROR) {
            switch (errorCode) {
                case ERROR_CANNOT_GET_CACHE_DIRECTORY:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_get_cache_directory, fileName, null);
                    break;
                case ERROR_VIDEO_FILE_CANNOT_DOWNLOAD:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_download_video, fileName, null);
                    break;
                case ERROR_VIDEO_FILE_CANNOT_SAVE:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_save_video, fileName, null);
                    break;
                case ERROR_AUDIO_FILE_CANNOT_SAVE:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_save_audio, fileName, null);
                    break;
                case ERROR_MUX_FAILED:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_mux, fileName, null);
                    break;
                case ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE:
                    updateNotification(R.string.downloading_reddit_video_failed_cannot_save_mux_video, fileName, null);
                    break;
                case ERROR_CANNOT_GET_DESTINATION_DIRECTORY:
                    updateNotification(R.string.downloading_media_failed_cannot_save_to_destination_directory, fileName, null);
                    break;
            }
            EventBus.getDefault().post(new DownloadRedditVideoEvent(false));
        } else {
            MediaScannerConnection.scanFile(
                    this, new String[]{destinationFileUri.toString()}, null,
                    (path, uri) -> {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(destinationFileUri, "video/*");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
                        updateNotification(R.string.downloading_reddit_video_finished, fileName, pendingIntent);
                        EventBus.getDefault().post(new DownloadRedditVideoEvent(true));
                    }
            );
        }
        stopForeground(false);
    }

    private Notification createNotification(int stringResId, String fileName, PendingIntent pendingIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO);
        builder.setContentTitle(fileName).setContentText(getString(stringResId));
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(customThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(int stringResId, String fileName, PendingIntent pendingIntent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                    createNotification(stringResId, fileName, pendingIntent));
        }
    }

    private static class SaveTempMuxAndCopyAsyncTask extends AsyncTask<Void, Integer, Void> {

        private ResponseBody videoResponse;
        private ResponseBody audioResponse;
        private String videoFilePath;
        private String audioFilePath;
        private String outputFilePath;
        private String destinationFileName;
        @NonNull
        private String destinationFileUriString;
        private boolean isDefaultDestination;
        private ContentResolver contentResolver;
        private SaveTempMuxAndCopyAsyncTaskListener saveTempMuxAndCopyAsyncTaskListener;
        private int errorCode = NO_ERROR;

        public SaveTempMuxAndCopyAsyncTask(ResponseBody videoResponse, ResponseBody audioResponse,
                                           String videoFilePath, String audioFilePath, String outputFilePath,
                                           String destinationFileName, @NonNull String destinationFileUriString,
                                           boolean isDefaultDestination, ContentResolver contentResolver,
                                           SaveTempMuxAndCopyAsyncTaskListener saveTempMuxAndCopyAsyncTaskListener) {
            this.videoResponse = videoResponse;
            this.audioResponse = audioResponse;
            this.videoFilePath = videoFilePath;
            this.audioFilePath = audioFilePath;
            this.outputFilePath = outputFilePath;
            this.destinationFileName = destinationFileName;
            this.destinationFileUriString = destinationFileUriString;
            this.isDefaultDestination = isDefaultDestination;
            this.contentResolver = contentResolver;
            this.saveTempMuxAndCopyAsyncTaskListener = saveTempMuxAndCopyAsyncTaskListener;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            saveTempMuxAndCopyAsyncTaskListener.updateProgressNotification(values[0]);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            publishProgress(R.string.downloading_reddit_video_save_video);
            String savedVideoFilePath = writeResponseBodyToDisk(videoResponse, videoFilePath);
            if (savedVideoFilePath == null) {
                errorCode = ERROR_VIDEO_FILE_CANNOT_SAVE;
                return null;
            }
            if (audioResponse != null) {
                publishProgress(R.string.downloading_reddit_video_save_audio);
                String savedAudioFilePath = writeResponseBodyToDisk(audioResponse, audioFilePath);
                if (savedAudioFilePath == null) {
                    errorCode = ERROR_AUDIO_FILE_CANNOT_SAVE;
                    return null;
                }

                publishProgress(R.string.downloading_reddit_video_muxing);
                if (!muxVideoAndAudio(videoFilePath, audioFilePath, outputFilePath)) {
                    errorCode = ERROR_MUX_FAILED;
                    return null;
                }

                publishProgress(R.string.downloading_reddit_video_save_file_to_public_dir);
                try {
                    copyToDestination(outputFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorCode = ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE;
                }
            } else {
                publishProgress(R.string.downloading_reddit_video_save_file_to_public_dir);
                try {
                    copyToDestination(videoFilePath);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorCode = ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveTempMuxAndCopyAsyncTaskListener.finished(Uri.parse(destinationFileUriString), errorCode);
        }

        private String writeResponseBodyToDisk(ResponseBody body, String filePath) {
            try {
                File file = new File(filePath);

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[4096];

                    long fileSize = body.contentLength();
                    long fileSizeDownloaded = 0;

                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(file);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);

                        fileSizeDownloaded += read;
                    }

                    outputStream.flush();

                    return file.getPath();
                } catch (IOException e) {
                    return null;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException e) {
                return null;
            }
        }

        private boolean muxVideoAndAudio(String videoFilePath, String audioFilePath, String outputFilePath) {
            try {
                File file = new File(outputFilePath);
                file.createNewFile();
                MediaExtractor videoExtractor = new MediaExtractor();
                videoExtractor.setDataSource(videoFilePath);
                MediaExtractor audioExtractor = new MediaExtractor();
                audioExtractor.setDataSource(audioFilePath);
                MediaMuxer muxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                videoExtractor.selectTrack(0);
                MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
                int videoTrack = muxer.addTrack(videoFormat);

                audioExtractor.selectTrack(0);
                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
                int audioTrack = muxer.addTrack(audioFormat);
                boolean sawEOS = false;
                int offset = 100;
                int sampleSize = 2048 * 1024;
                ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
                ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
                MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
                MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

                videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

                muxer.start();

                while (!sawEOS) {
                    videoBufferInfo.offset = offset;
                    videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                        sawEOS = true;
                        videoBufferInfo.size = 0;
                    } else {
                        videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                        videoBufferInfo.flags = videoExtractor.getSampleFlags();
                        muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                        videoExtractor.advance();
                    }
                }

                boolean sawEOS2 = false;
                while (!sawEOS2) {
                    audioBufferInfo.offset = offset;
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                    if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
                        sawEOS2 = true;
                        audioBufferInfo.size = 0;
                    } else {
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                        audioExtractor.advance();
                    }
                }

                try {
                    muxer.stop();
                    muxer.release();
                } catch (IllegalStateException ignore) {}
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }

        private void copyToDestination(String srcPath) throws IOException {
            if (isDefaultDestination) {
                if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    InputStream in = new FileInputStream(srcPath);
                    OutputStream out = new FileOutputStream(destinationFileUriString);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }

                    new File(srcPath).delete();
                } else {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, destinationFileName);
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/*");
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destinationFileUriString);
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 1);

                    OutputStream stream = null;
                    Uri uri = null;

                    try {
                        final Uri contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        uri = contentResolver.insert(contentUri, contentValues);

                        if (uri == null) {
                            throw new IOException("Failed to create new MediaStore record.");
                        }

                        stream = contentResolver.openOutputStream(uri);

                        if (stream == null) {
                            throw new IOException("Failed to get output stream.");
                        }

                        InputStream in = new FileInputStream(srcPath);

                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            stream.write(buf, 0, len);
                        }

                        contentValues.clear();
                        contentValues.put(MediaStore.Video.Media.IS_PENDING, 0);
                        contentResolver.update(uri, contentValues, null, null);
                        destinationFileUriString = uri.toString();
                    } catch (IOException e) {
                        if (uri != null) {
                            // Don't leave an orphan entry in the MediaStore
                            contentResolver.delete(uri, null, null);
                        }

                        throw e;
                    } finally {
                        if (stream != null) {
                            stream.close();
                        }
                    }
                }
            } else {
                OutputStream stream = contentResolver.openOutputStream(Uri.parse(destinationFileUriString));
                if (stream == null) {
                    throw new IOException("Failed to get output stream.");
                }

                InputStream in = new FileInputStream(srcPath);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    stream.write(buf, 0, len);
                }
            }
        }

        interface SaveTempMuxAndCopyAsyncTaskListener {
            void finished(Uri destinationFileUri, int errorCode);
            void updateProgressNotification(int stringResId);
        }
    }
}
