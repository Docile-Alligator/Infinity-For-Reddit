package ml.docilealligator.infinityforreddit.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
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

    @Inject
    @Named("download_reddit_video")
    Retrofit retrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

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

        String fileNameWithoutExtension = intent.getStringExtra(EXTRA_SUBREDDIT)
                + "-" + intent.getStringExtra(EXTRA_POST_ID);

        startForeground(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                createNotification(R.string.downloading_reddit_video, fileNameWithoutExtension + ".mp4"));

        ml.docilealligator.infinityforreddit.API.DownloadRedditVideo downloadRedditVideo = retrofit.create(ml.docilealligator.infinityforreddit.API.DownloadRedditVideo.class);
        downloadRedditVideo.downloadFile(videoUrl).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> videoResponse) {
                if (videoResponse.isSuccessful() && videoResponse.body() != null) {
                    updateNotification(R.string.downloading_reddit_video_audio_track, fileNameWithoutExtension + ".mp3");

                    downloadRedditVideo.downloadFile(audioUrl).enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> audioResponse) {
                            File directory = getExternalCacheDir();
                            if (directory != null) {
                                String directoryPath = directory.getAbsolutePath() + "/";
                                if (audioResponse.isSuccessful() && audioResponse.body() != null) {
                                    updateNotification(R.string.downloading_reddit_video_muxing, null);

                                    String videoFilePath = writeResponseBodyToDisk(videoResponse.body(),  directoryPath + fileNameWithoutExtension+ "-cache.mp4");
                                    if (videoFilePath != null) {
                                        String audioFilePath = writeResponseBodyToDisk(audioResponse.body(), directoryPath + fileNameWithoutExtension + "-cache.mp3");
                                        if (audioFilePath != null) {
                                            String outputFilePath = directoryPath + fileNameWithoutExtension + ".mp4";
                                            if(muxVideoAndAudio(videoFilePath, audioFilePath, outputFilePath)) {
                                                new CopyFileAsyncTask(new File(outputFilePath), fileNameWithoutExtension + ".mp4",
                                                        getContentResolver(), new CopyFileAsyncTask.CopyFileAsyncTaskListener() {
                                                    @Override
                                                    public void successful() {
                                                        new File(videoFilePath).delete();
                                                        new File(audioFilePath).delete();
                                                        new File(outputFilePath).delete();

                                                        updateNotification(R.string.downloading_reddit_video_finished, fileNameWithoutExtension + ".mp4");

                                                        EventBus.getDefault().post(new DownloadRedditVideoEvent(true));

                                                        stopService();
                                                    }

                                                    @Override
                                                    public void failed() {
                                                        new File(videoFilePath).delete();
                                                        new File(audioFilePath).delete();
                                                        new File(outputFilePath).delete();

                                                        EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                                        stopService();
                                                    }
                                                }).execute();
                                            } else {
                                                EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                                stopService();
                                            }
                                        } else {
                                            new File(videoFilePath).delete();

                                            EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                            stopService();
                                        }
                                    } else {
                                        EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                        stopService();
                                    }
                                } else {
                                    //No audio
                                    String videoFilePath = writeResponseBodyToDisk(videoResponse.body(),  directoryPath + fileNameWithoutExtension+ ".mp4");
                                    if (videoFilePath != null) {
                                        new CopyFileAsyncTask(new File(videoFilePath), fileNameWithoutExtension + ".mp4",
                                                getContentResolver(), new CopyFileAsyncTask.CopyFileAsyncTaskListener() {
                                            @Override
                                            public void successful() {
                                                new File(videoFilePath).delete();

                                                updateNotification(R.string.downloading_reddit_video_finished, null);

                                                EventBus.getDefault().post(new DownloadRedditVideoEvent(true));

                                                stopService();
                                            }

                                            @Override
                                            public void failed() {
                                                new File(videoFilePath).delete();

                                                EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                                stopService();
                                            }
                                        }).execute();
                                    } else {
                                        EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                        stopService();
                                    }
                                }
                            } else {
                                EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                                stopService();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                            stopService();
                        }
                    });
                } else {
                    EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                    stopService();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                EventBus.getDefault().post(new DownloadRedditVideoEvent(false));

                stopService();
            }
        });

        return START_NOT_STICKY;
    }

    private Notification createNotification(int stringResId, String fileName) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO);
        if (fileName != null) {
            builder.setContentTitle(getString(stringResId, fileName));
        } else {
            builder.setContentTitle(getString(stringResId));
        }
        return builder.setContentText(getString(R.string.please_wait))
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(int stringResId, String fileName) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                    createNotification(stringResId, fileName));
        }
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
            int sampleSize = 256 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
            audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            muxer.start();

            while (!sawEOS)
            {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0)
                {
                    // Log.d(TAG, "saw input EOS.");
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
            while (!sawEOS2)
            {
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

            muxer.stop();
            muxer.release();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private void stopService() {
        stopForeground(false);
    }

    private static class CopyFileAsyncTask extends AsyncTask<Void, Void, Void> {
        private File src;
        private String destinationFileName;
        private ContentResolver contentResolver;
        private CopyFileAsyncTaskListener copyFileAsyncTaskListener;
        private boolean successful;

        interface CopyFileAsyncTaskListener {
            void successful();
            void failed();
        }

        CopyFileAsyncTask(File src, String destinationFileName, ContentResolver contentResolver, CopyFileAsyncTaskListener copyFileAsyncTaskListener) {
            this.src = src;
            this.destinationFileName = destinationFileName;
            this.contentResolver = contentResolver;
            this.copyFileAsyncTaskListener = copyFileAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                successful = copy(src, destinationFileName);
            } else {
                try {
                    copyFileQ(src, destinationFileName);
                    successful = true;
                } catch (IOException e) {
                    e.printStackTrace();
                    successful = false;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (successful) {
                copyFileAsyncTaskListener.successful();
            } else {
                copyFileAsyncTaskListener.failed();
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
                String directoryPath = directory.getAbsolutePath() + "/Infinity/";;
                File dst = new File(directoryPath, outputFileName);

                try (InputStream in = new FileInputStream(src)) {
                    try (OutputStream out = new FileOutputStream(dst)) {
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
    }
}
