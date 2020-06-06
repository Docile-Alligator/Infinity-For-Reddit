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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.FileProvider;

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
                createNotification(R.string.downloading_reddit_video, fileName + ".mp4", false, null)
        );

        DownloadRedditVideo downloadRedditVideo = retrofit.create(DownloadRedditVideo.class);

        File directory = getExternalCacheDir();
        if (directory != null) {
            String directoryPath = directory.getAbsolutePath() + "/";
            downloadRedditVideo.downloadFile(videoUrl).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> videoResponse) {
                    if (videoResponse.isSuccessful() && videoResponse.body() != null) {
                        String videoFilePath = writeResponseBodyToDisk(videoResponse.body(), directoryPath + fileName + "-cache.mp4");
                        if (videoFilePath != null) {
                            resultFile = videoFilePath;
                            updateNotification(R.string.downloading_reddit_video_audio_track, fileName + ".mp3", false);

                            downloadRedditVideo.downloadFile(audioUrl).enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> audioResponse) {
                                    if (audioResponse.isSuccessful() && audioResponse.body() != null) {
                                        String audioFilePath = writeResponseBodyToDisk(audioResponse.body(), directoryPath + fileName + "-cache.mp3");
                                        if (audioFilePath != null) {
                                            updateNotification(R.string.downloading_reddit_video_muxing, null, false);
                                            String outputFilePath = directoryPath + fileName + ".mp4";
                                            if (muxVideoAndAudio(videoFilePath, audioFilePath, outputFilePath)) {
                                                resultFile = outputFilePath;
                                            }
                                            new File(audioFilePath).delete();
                                        }
                                    }
                                    copyVideoToPublicDir(resultFile, fileName, new String[]{videoFilePath, resultFile});
                                }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    copyVideoToPublicDir(resultFile, fileName, new String[]{videoFilePath, resultFile});
                                }
                            });

                        } else {
                            downloadFinished(false);
                        }
                    } else {
                        downloadFinished(false);
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    downloadFinished(false);
                }
            });
        } else {
            downloadFinished(false);
        }

        return START_NOT_STICKY;
    }

    private void addMediaFileAndShareNotification(File f, int stringResId, String fileName) {
        MediaScannerConnection.scanFile(
                this, new String[]{f.getAbsolutePath()}, null,
                (path, uri) -> openVideoNotification(f, stringResId, fileName)
        );
    }

    private void openVideoNotification(File f, int stringResId, String fileName) {
        final Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        Uri selectedUri = FileProvider.getUriForFile(this, getApplicationContext().getPackageName() + ".provider", f);
        viewIntent.setDataAndType(selectedUri, getContentResolver().getType(selectedUri));
        viewIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, viewIntent, PendingIntent.FLAG_CANCEL_CURRENT);
        updateNotification(stringResId, fileName, true, contentIntent);
    }

    private void downloadFinished(boolean isSuccessful) {
        EventBus.getDefault().post(new DownloadRedditVideoEvent(isSuccessful));
        if (!isSuccessful)
            updateNotification(R.string.downloading_reddit_video_failed, null, true);
        stopService();
    }

    private Notification createNotification(int stringResId, String fileName, boolean finished, PendingIntent contentIntent) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO);
        if (fileName != null) {
            builder.setContentTitle(getString(stringResId, fileName));
        } else {
            builder.setContentTitle(getString(stringResId));
        }
        if (!finished)
            builder.setContentText(getString(R.string.please_wait));
        if (contentIntent != null)
            builder.setContentIntent(contentIntent);
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(mCustomThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(int stringResId, String fileName, boolean finished) {
        updateNotification(stringResId, fileName, finished, null);
    }

    private void updateNotification(int stringResId, String fileName, boolean finished, PendingIntent contentIntent) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID,
                    createNotification(stringResId, fileName, finished, contentIntent));
        }
    }

    private void copyVideoToPublicDir(String resFilePath, String newFileName, String[] tempFiles) {
        new CopyFileAsyncTask(new File(resFilePath), newFileName + ".mp4",
                getContentResolver(), new CopyFileAsyncTask.CopyFileAsyncTaskListener() {
            @Override
            public void successful(File dstFile) {
                if (dstFile != null) {
                    addMediaFileAndShareNotification(dstFile, R.string.downloading_reddit_video_finished, newFileName + ".mp4");
                } else {
                    updateNotification(R.string.downloading_reddit_video_finished, newFileName + ".mp4", true);
                }

                for (int i = 0; i < tempFiles.length; i++) {
                    new File(tempFiles[i]).delete();
                }
                downloadFinished(true);
            }

            @Override
            public void failed() {
                for (int i = 0; i < tempFiles.length; i++) {
                    new File(tempFiles[i]).delete();
                }
                downloadFinished(false);
            }
        }).execute();
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

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);


                if (videoBufferInfo.size < 0 || audioBufferInfo.size < 0) {
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
        private File destinationFile;
        private ContentResolver contentResolver;
        private CopyFileAsyncTaskListener copyFileAsyncTaskListener;
        private boolean successful;

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
                copyFileAsyncTaskListener.successful(destinationFile);
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
                String directoryPath = directory.getAbsolutePath() + "/Infinity/";
                destinationFile = new File(directoryPath, outputFileName);

                try (InputStream in = new FileInputStream(src)) {
                    try (OutputStream out = new FileOutputStream(destinationFile)) {
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

        interface CopyFileAsyncTaskListener {

            void successful(File dstFile);

            void failed();
        }
    }
}
