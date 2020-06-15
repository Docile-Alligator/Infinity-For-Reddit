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
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

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

import ml.docilealligator.infinityforreddit.API.DownloadRedditVideo;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.DownloadRedditVideoEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.NotificationUtils;
import ml.docilealligator.infinityforreddit.R;
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

    @Inject
    @Named("download_reddit_video")
    Retrofit retrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
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

        String videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL);
        String audioUrl = videoUrl.substring(0, videoUrl.lastIndexOf('/')) + "/audio";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO,
                    NotificationUtils.CHANNEL_DOWNLOAD_REDDIT_VIDEO,
                    NotificationManager.IMPORTANCE_LOW
            );

            NotificationManagerCompat manager = NotificationManagerCompat.from(this);
            manager.createNotificationChannel(serviceChannel);
        }

        String fileName = intent.getStringExtra(EXTRA_SUBREDDIT) + "-" + intent.getStringExtra(EXTRA_POST_ID);

        startForeground(
                NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                createNotification(R.string.downloading_reddit_video, fileName + ".mp4", null)
        );

        DownloadRedditVideo downloadRedditVideo = retrofit.create(DownloadRedditVideo.class);

        File directory = getExternalCacheDir();
        String destinationFileName = fileName + ".mp4";
        if (directory != null) {
            String directoryPath = directory.getAbsolutePath() + "/";
            downloadRedditVideo.downloadFile(videoUrl).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> videoResponse) {
                    if (videoResponse.isSuccessful() && videoResponse.body() != null) {
                        updateNotification(R.string.downloading_reddit_video_audio_track, destinationFileName, null);
                        downloadRedditVideo.downloadFile(audioUrl).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> audioResponse) {
                                if (audioResponse.isSuccessful() && audioResponse.body() != null) {
                                    String videoFilePath = directoryPath + fileName + "-cache.mp4";
                                    String audioFilePath = directoryPath + fileName + "-cache.mp3";
                                    String outputFilePath = directoryPath + fileName + ".mp4";
                                    new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                            audioResponse.body(), videoFilePath, audioFilePath, outputFilePath,
                                            destinationFileName, getContentResolver(),
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
                                    String videoFilePath = directoryPath + fileName + "-cache.mp4";
                                    String destinationFileName = fileName + ".mp4";
                                    new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                            null, videoFilePath, null, null,
                                            destinationFileName, getContentResolver(),
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
                                String videoFilePath = directoryPath + fileName + "-cache.mp4";
                                String destinationFileName = fileName + ".mp4";
                                new SaveTempMuxAndCopyAsyncTask(videoResponse.body(),
                                        null, videoFilePath, null, null,
                                        destinationFileName, getContentResolver(),
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
            }
            EventBus.getDefault().post(new DownloadRedditVideoEvent(false));
        } else {
            MediaScannerConnection.scanFile(
                    this, new String[]{destinationFileUri.toString()}, null,
                    (path, uri) -> {
                        Intent intent = new Intent();
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setDataAndType(destinationFileUri, "video/*");
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
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
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
        private ContentResolver contentResolver;
        private SaveTempMuxAndCopyAsyncTaskListener saveTempMuxAndCopyAsyncTaskListener;
        private Uri destinationFileUri;
        private int errorCode = NO_ERROR;

        public SaveTempMuxAndCopyAsyncTask(ResponseBody videoResponse, ResponseBody audioResponse,
                                           String videoFilePath, String audioFilePath, String outputFilePath,
                                           String destinationFileName, ContentResolver contentResolver,
                                           SaveTempMuxAndCopyAsyncTaskListener saveTempMuxAndCopyAsyncTaskListener) {
            this.videoResponse = videoResponse;
            this.audioResponse = audioResponse;
            this.videoFilePath = videoFilePath;
            this.audioFilePath = audioFilePath;
            this.outputFilePath = outputFilePath;
            this.destinationFileName = destinationFileName;
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
                copyToDestination(outputFilePath);
            } else {
                publishProgress(R.string.downloading_reddit_video_save_file_to_public_dir);
                copyToDestination(videoFilePath);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            saveTempMuxAndCopyAsyncTaskListener.finished(destinationFileUri, errorCode);
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
                        Log.i("asdfsadf", "file download: " + fileSizeDownloaded + " of " + fileSize);
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

        private void copyToDestination(String srcPath) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (!copy(new File(srcPath), destinationFileName)) {
                    errorCode = ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE;
                }
            } else {
                try {
                    copyFileQ(new File(srcPath), destinationFileName);
                } catch (IOException e) {
                    e.printStackTrace();
                    errorCode = ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE;
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        private void copyFileQ(File src, String outputFileName) throws IOException {
            String relativeLocation = Environment.DIRECTORY_MOVIES + "/Infinity/";

            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName);
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativeLocation);
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

                InputStream in = new FileInputStream(src);

                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    stream.write(buf, 0, len);
                }

                contentValues.clear();
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0);
                contentResolver.update(uri, contentValues, null, null);
                destinationFileUri = uri;
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

        private boolean copy(File src, String outputFileName) {
            File directory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (directory != null) {
                String directoryPath = directory.getAbsolutePath() + "/Infinity/";
                String destinationFilePath = directoryPath + outputFileName;
                destinationFileUri = Uri.parse(destinationFilePath);

                try (InputStream in = new FileInputStream(src)) {
                    try (OutputStream out = new FileOutputStream(destinationFilePath)) {
                        byte[] buf = new byte[1024];
                        int len;
                        while ((len = in.read(buf)) > 0) {
                            out.write(buf, 0, len);
                        }

                        src.delete();
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    src.delete();
                    return false;
                }
            } else {
                src.delete();
                return false;
            }
        }

        interface SaveTempMuxAndCopyAsyncTaskListener {
            void finished(Uri destinationFileUri, int errorCode);
            void updateProgressNotification(int stringResId);
        }
    }
}
