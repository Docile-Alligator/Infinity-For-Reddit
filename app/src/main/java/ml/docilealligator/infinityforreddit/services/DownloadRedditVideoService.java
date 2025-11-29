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
import android.media.MediaCodec;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaScannerConnection;
import android.net.NetworkRequest;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.PersistableBundle;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.documentfile.provider.DocumentFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Random;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.DownloadProgressResponseBody;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.apis.DownloadFile;
import ml.docilealligator.infinityforreddit.broadcastreceivers.DownloadedMediaDeleteActionBroadcastReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.NotificationUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DownloadRedditVideoService extends JobService {

    public static final String EXTRA_VIDEO_URL = "EVU";
    public static final String EXTRA_SUBREDDIT = "ES";
    public static final String EXTRA_POST_ID = "EPI";
    public static final String EXTRA_IS_NSFW = "EIN";

    private static final int NO_ERROR = -1;
    private static final int ERROR_CANNOT_GET_CACHE_DIRECTORY = 0;
    private static final int ERROR_VIDEO_FILE_CANNOT_DOWNLOAD = 1;
    private static final int ERROR_VIDEO_FILE_CANNOT_SAVE = 2;
    private static final int ERROR_AUDIO_FILE_CANNOT_SAVE = 3;
    private static final int ERROR_MUX_FAILED = 4;
    private static final int ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE = 5;
    private static final int ERROR_CANNOT_GET_DESTINATION_DIRECTORY = 6;

    private static int JOB_ID = 30000;

    @Inject
    @Named("download_media")
    Retrofit retrofit;
    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;
    private NotificationManagerCompat notificationManager;
    private final String[] possibleAudioUrlSuffices = new String[]{"/CMAF_AUDIO_128.mp4", "/CMAF_AUDIO_64.mp4", "/DASH_AUDIO_128.mp4", "/DASH_audio.mp4", "/DASH_audio", "/audio.mp4", "/audio"};

    public DownloadRedditVideoService() {
    }

    public static JobInfo constructJobInfo(Context context, long contentEstimatedBytes, PersistableBundle extras) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadRedditVideoService.class))
                    .setUserInitiated(true)
                    .setRequiredNetwork(new NetworkRequest.Builder().clearCapabilities().build())
                    .setEstimatedNetworkBytes(0, contentEstimatedBytes + 500)
                    .setExtras(extras)
                    .build();
        } else {
            return new JobInfo.Builder(JOB_ID++, new ComponentName(context, DownloadRedditVideoService.class))
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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(DownloadRedditVideoService.this, NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO);

        PersistableBundle intent = params.getExtras();

        String subredditName = intent.getString(EXTRA_SUBREDDIT);
        String fileNameWithoutExtension = subredditName + "-" + intent.getString(EXTRA_POST_ID);

        NotificationChannelCompat serviceChannel =
                new NotificationChannelCompat.Builder(
                        NotificationUtils.CHANNEL_ID_DOWNLOAD_REDDIT_VIDEO,
                        NotificationManagerCompat.IMPORTANCE_LOW)
                        .setName(NotificationUtils.CHANNEL_DOWNLOAD_REDDIT_VIDEO)
                        .build();
        notificationManager.createNotificationChannel(serviceChannel);

        int randomNotificationIdOffset = new Random().nextInt(10000);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            setNotification(params,
                    NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset,
                    createNotification(builder, fileNameWithoutExtension + ".mp4"),
                    JobService.JOB_END_NOTIFICATION_POLICY_DETACH);
        } else {
            notificationManager.notify(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset,
                    createNotification(builder, fileNameWithoutExtension + ".mp4"));
        }

        String videoUrl = intent.getString(EXTRA_VIDEO_URL);

        String audioUrlPrefix = Build.VERSION.SDK_INT > Build.VERSION_CODES.N ? videoUrl.substring(0, videoUrl.lastIndexOf('/')) : null;

        boolean isNsfw = intent.getInt(EXTRA_IS_NSFW, 0) == 1;

        executor.execute(() -> {
            final DownloadProgressResponseBody.ProgressListener progressListener = new DownloadProgressResponseBody.ProgressListener() {
                long time = 0;

                @Override public void update(long bytesRead, long contentLength, boolean done) {
                    if (!done) {
                        if (contentLength != -1) {
                            long currentTime = System.currentTimeMillis();
                            if (currentTime - time > 1000) {
                                time = currentTime;
                                updateNotification(builder, 0, (int) ((100 * bytesRead) / contentLength),
                                        randomNotificationIdOffset, null);
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

            DownloadFile downloadFileRetrofit = retrofit.create(DownloadFile.class);

            boolean separateDownloadFolder = sharedPreferences.getBoolean(SharedPreferencesUtils.SEPARATE_FOLDER_FOR_EACH_SUBREDDIT, false);

            File externalCacheDirectory = Utils.getCacheDir(this);
            if (externalCacheDirectory != null) {
                String destinationFileName = fileNameWithoutExtension + ".mp4";
                String finalFileNameWithoutExtension = fileNameWithoutExtension;

                try {
                    Response<ResponseBody> videoResponse = downloadFileRetrofit.downloadFile(videoUrl).execute();
                    if (videoResponse.isSuccessful() && videoResponse.body() != null) {
                        String externalCacheDirectoryPath = externalCacheDirectory.getAbsolutePath() + "/";
                        String destinationFileDirectory;
                        if (isNsfw && sharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_NSFW_MEDIA_IN_DIFFERENT_FOLDER, false)) {
                            destinationFileDirectory = sharedPreferences.getString(SharedPreferencesUtils.NSFW_DOWNLOAD_LOCATION, "");
                        } else {
                            destinationFileDirectory = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_DOWNLOAD_LOCATION, "");
                        }
                        String destinationFileUriString;
                        boolean isDefaultDestination;
                        if (destinationFileDirectory.equals("")) {
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                File destinationDirectory = getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                if (destinationDirectory != null) {
                                    String destinationDirectoryPath = separateDownloadFolder ? destinationDirectory.getAbsolutePath() + "/Infinity/" + subredditName + "/" : destinationDirectory.getAbsolutePath() + "/Infinity/";
                                    File infinityDir = new File(destinationDirectoryPath);
                                    if (!infinityDir.exists() && !infinityDir.mkdir()) {
                                        downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                        return;
                                    }
                                    destinationFileUriString = destinationDirectoryPath + destinationFileName;
                                } else {
                                    downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                    return;
                                }
                            } else {
                                destinationFileUriString = separateDownloadFolder ? Environment.DIRECTORY_MOVIES + "/Infinity/" + subredditName + "/" : Environment.DIRECTORY_MOVIES + "/Infinity/";
                            }
                            isDefaultDestination = true;
                        } else {
                            isDefaultDestination = false;
                            DocumentFile picFile;
                            DocumentFile dir;
                            if (separateDownloadFolder) {
                                dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory));
                                if (dir == null) {
                                    downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                    return;
                                }
                                dir = dir.findFile(subredditName);
                                if (dir == null) {
                                    dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory)).createDirectory(subredditName);
                                    if (dir == null) {
                                        downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                        return;
                                    }
                                }
                            } else {
                                dir = DocumentFile.fromTreeUri(DownloadRedditVideoService.this, Uri.parse(destinationFileDirectory));
                                if (dir == null) {
                                    downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                    return;
                                }
                            }
                            DocumentFile checkForDuplicates = dir.findFile(destinationFileName);
                            int num = 1;
                            while (checkForDuplicates != null) {
                                finalFileNameWithoutExtension = finalFileNameWithoutExtension + " (" + num + ")";
                                checkForDuplicates = dir.findFile(finalFileNameWithoutExtension + ".mp4");
                                num++;
                            }
                            picFile = dir.createFile("video/mp4", finalFileNameWithoutExtension + ".mp4");
                            if (picFile == null) {
                                downloadFinished(params, builder, null, ERROR_CANNOT_GET_DESTINATION_DIRECTORY, randomNotificationIdOffset);
                                return;
                            }
                            destinationFileUriString = picFile.getUri().toString();
                        }

                        updateNotification(builder, R.string.downloading_reddit_video_audio_track, 0,
                                randomNotificationIdOffset, null);

                        String videoFilePath = externalCacheDirectoryPath + finalFileNameWithoutExtension + "-cache.mp4";
                        String savedVideoFilePath = writeResponseBodyToDisk(videoResponse.body(), videoFilePath);
                        if (savedVideoFilePath == null) {
                            downloadFinished(params, builder, null, ERROR_VIDEO_FILE_CANNOT_SAVE, randomNotificationIdOffset);
                            return;
                        }

                        if (audioUrlPrefix != null) {
                            ResponseBody audioResponse = getAudioResponse(downloadFileRetrofit, audioUrlPrefix, 0);
                            String outputFilePath = externalCacheDirectoryPath + finalFileNameWithoutExtension + ".mp4";
                            if (audioResponse != null) {
                                String audioFilePath = externalCacheDirectoryPath + finalFileNameWithoutExtension + "-cache.mp3";

                                String savedAudioFilePath = writeResponseBodyToDisk(audioResponse, audioFilePath);
                                if (savedAudioFilePath == null) {
                                    downloadFinished(params, builder, null, ERROR_AUDIO_FILE_CANNOT_SAVE, randomNotificationIdOffset);
                                    return;
                                }

                                updateNotification(builder, R.string.downloading_reddit_video_muxing, -1,
                                        randomNotificationIdOffset, null);
                                if (!muxVideoAndAudio(videoFilePath, audioFilePath, outputFilePath)) {
                                    downloadFinished(params, builder, null, ERROR_MUX_FAILED, randomNotificationIdOffset);
                                    return;
                                }

                                updateNotification(builder, R.string.downloading_reddit_video_save_file_to_public_dir, -1,
                                        randomNotificationIdOffset, null);
                                try {
                                    Uri destinationFileUri = copyToDestination(outputFilePath, destinationFileUriString, destinationFileName, isDefaultDestination);

                                    new File(videoFilePath).delete();
                                    new File(audioFilePath).delete();
                                    new File(outputFilePath).delete();

                                    downloadFinished(params, builder, destinationFileUri, NO_ERROR, randomNotificationIdOffset);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    downloadFinished(params, builder, null, ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE, randomNotificationIdOffset);
                                }
                            } else {
                                updateNotification(builder, R.string.downloading_reddit_video_muxing, -1,
                                        randomNotificationIdOffset, null);
                                if (!muxVideoAndAudio(videoFilePath, null, outputFilePath)) {
                                    downloadFinished(params, builder, null, ERROR_MUX_FAILED, randomNotificationIdOffset);
                                    return;
                                }

                                updateNotification(builder, R.string.downloading_reddit_video_save_file_to_public_dir, -1,
                                        randomNotificationIdOffset, null);
                                try {
                                    Uri destinationFileUri = copyToDestination(outputFilePath, destinationFileUriString, destinationFileName, isDefaultDestination);

                                    new File(videoFilePath).delete();
                                    new File(outputFilePath).delete();

                                    downloadFinished(params, builder, destinationFileUri, NO_ERROR, randomNotificationIdOffset);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                    downloadFinished(params, builder, null, ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE, randomNotificationIdOffset);
                                }
                            }
                        } else {
                            // do not remux video on <= Android N, just save video
                            updateNotification(builder, R.string.downloading_reddit_video_save_file_to_public_dir, -1,
                                    randomNotificationIdOffset, null);
                            try {
                                Uri destinationFileUri = copyToDestination(videoFilePath, destinationFileUriString, destinationFileName, isDefaultDestination);
                                new File(videoFilePath).delete();
                                downloadFinished(params, builder, destinationFileUri, NO_ERROR, randomNotificationIdOffset);
                            } catch (IOException e) {
                                e.printStackTrace();
                                downloadFinished(params, builder, null, ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE, randomNotificationIdOffset);
                            }
                        }
                    } else {
                        downloadFinished(params, builder, null, ERROR_VIDEO_FILE_CANNOT_DOWNLOAD, randomNotificationIdOffset);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    downloadFinished(params, builder, null, ERROR_VIDEO_FILE_CANNOT_DOWNLOAD, randomNotificationIdOffset);
                }
            } else {
                downloadFinished(params, builder, null, ERROR_CANNOT_GET_CACHE_DIRECTORY, randomNotificationIdOffset);
            }
        });

        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    @Nullable
    private ResponseBody getAudioResponse(DownloadFile downloadFileRetrofit, @NonNull String audioUrlPrefix, int audioSuffixIndex) throws IOException {
        if (audioSuffixIndex >= possibleAudioUrlSuffices.length) {
            return null;
        }

        String audioUrl = audioUrlPrefix + possibleAudioUrlSuffices[audioSuffixIndex];
        Response<ResponseBody> audioResponse = downloadFileRetrofit.downloadFile(audioUrl).execute();
        ResponseBody responseBody = audioResponse.body();
        if (audioResponse.isSuccessful() && responseBody != null) {
            return responseBody;
        }

        return getAudioResponse(downloadFileRetrofit, audioUrlPrefix, audioSuffixIndex + 1);
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
            MediaMuxer muxer = new MediaMuxer(outputFilePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            videoExtractor.selectTrack(0);
            MediaFormat videoFormat = videoExtractor.getTrackFormat(0);
            int videoTrack = muxer.addTrack(videoFormat);

            boolean sawEOS = false;
            int offset = 100;
            int sampleSize = 4096 * 1024;
            ByteBuffer videoBuf = ByteBuffer.allocate(sampleSize);
            ByteBuffer audioBuf = ByteBuffer.allocate(sampleSize);
            MediaCodec.BufferInfo videoBufferInfo = new MediaCodec.BufferInfo();

            videoExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);

            // audio not present for all videos
            MediaExtractor audioExtractor = new MediaExtractor();
            MediaCodec.BufferInfo audioBufferInfo = new MediaCodec.BufferInfo();
            int audioTrack = -1;
            if (audioFilePath != null) {
                audioExtractor.setDataSource(audioFilePath);
                audioExtractor.selectTrack(0);
                MediaFormat audioFormat = audioExtractor.getTrackFormat(0);
                audioExtractor.seekTo(0, MediaExtractor.SEEK_TO_CLOSEST_SYNC);
                audioTrack = muxer.addTrack(audioFormat);
            }

            muxer.start();

            while (!sawEOS) {
                videoBufferInfo.offset = offset;
                videoBufferInfo.size = videoExtractor.readSampleData(videoBuf, offset);

                if (videoBufferInfo.size < 0) {
                    sawEOS = true;
                    videoBufferInfo.size = 0;
                } else {
                    videoBufferInfo.presentationTimeUs = videoExtractor.getSampleTime();
                    videoBufferInfo.flags = videoExtractor.getSampleFlags();
                    muxer.writeSampleData(videoTrack, videoBuf, videoBufferInfo);
                    videoExtractor.advance();
                }
            }

            if (audioFilePath != null) {
                boolean sawEOS2 = false;
                while (!sawEOS2) {
                    audioBufferInfo.offset = offset;
                    audioBufferInfo.size = audioExtractor.readSampleData(audioBuf, offset);

                    if (audioBufferInfo.size < 0) {
                        sawEOS2 = true;
                        audioBufferInfo.size = 0;
                    } else {
                        audioBufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                        audioBufferInfo.flags = audioExtractor.getSampleFlags();
                        muxer.writeSampleData(audioTrack, audioBuf, audioBufferInfo);
                        audioExtractor.advance();
                    }
                }
            }

            muxer.stop();
            muxer.release();
        } catch (IllegalArgumentException | IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private Uri copyToDestination(String srcPath, String destinationFileUriString, String destinationFileName,
                                  boolean isDefaultDestination) throws IOException {
        ContentResolver contentResolver = getContentResolver();
        if (isDefaultDestination) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
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
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, destinationFileUriString);
                contentValues.put(MediaStore.Video.Media.IS_PENDING, 1);

                OutputStream stream = null;
                Uri uri = null;

                try {
                    final Uri contentUri = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
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
                    return uri;
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

        return Uri.parse(destinationFileUriString);
    }

    private void downloadFinished(JobParameters parameters, NotificationCompat.Builder builder, Uri destinationFileUri, int errorCode, int randomNotificationIdOffset) {
        if (errorCode != NO_ERROR) {
            switch (errorCode) {
                case ERROR_CANNOT_GET_CACHE_DIRECTORY:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_get_cache_directory, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_VIDEO_FILE_CANNOT_DOWNLOAD:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_download_video, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_VIDEO_FILE_CANNOT_SAVE:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_save_video, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_AUDIO_FILE_CANNOT_SAVE:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_save_audio, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_MUX_FAILED:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_mux, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_MUXED_VIDEO_FILE_CANNOT_SAVE:
                    updateNotification(builder, R.string.downloading_reddit_video_failed_cannot_save_mux_video, -1,
                            randomNotificationIdOffset, null);
                    break;
                case ERROR_CANNOT_GET_DESTINATION_DIRECTORY:
                    updateNotification(builder, R.string.downloading_media_failed_cannot_save_to_destination_directory, -1,
                            randomNotificationIdOffset, null);
                    break;
            }
        } else {
            MediaScannerConnection.scanFile(
                    this, new String[]{destinationFileUri.toString()}, null,
                    (path, uri) -> {
                        updateNotification(builder, R.string.downloading_reddit_video_finished, -1, randomNotificationIdOffset, destinationFileUri);
                    }
            );
        }
        jobFinished(parameters, false);
    }

    private Notification createNotification(NotificationCompat.Builder builder, String fileName) {
        builder.setContentTitle(fileName).setContentText(getString(R.string.downloading_reddit_video)).setProgress(100, 0, false);
        return builder.setSmallIcon(R.drawable.ic_notification)
                .setColor(customThemeWrapper.getColorPrimaryLightTheme())
                .build();
    }

    private void updateNotification(NotificationCompat.Builder builder, int contentStringResId, int progress, int randomNotificationIdOffset, Uri mediaUri) {
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
                intent.setDataAndType(mediaUri, "video/mp4");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, pendingIntentFlags);

                builder.setContentIntent(pendingIntent);

                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.putExtra(Intent.EXTRA_STREAM, mediaUri);
                shareIntent.setType("video/mp4");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Intent intentAction = Intent.createChooser(shareIntent, getString(R.string.share));
                PendingIntent shareActionPendingIntent = PendingIntent.getActivity(this, 1, intentAction, pendingIntentFlags);
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_notification, getString(R.string.share), shareActionPendingIntent));

                Intent deleteIntent = new Intent(this, DownloadedMediaDeleteActionBroadcastReceiver.class);
                deleteIntent.setData(mediaUri);
                deleteIntent.putExtra(DownloadedMediaDeleteActionBroadcastReceiver.EXTRA_NOTIFICATION_ID, NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset);
                PendingIntent deleteActionPendingIntent = PendingIntent.getBroadcast(this, 2, deleteIntent, pendingIntentFlags);
                builder.addAction(new NotificationCompat.Action(R.drawable.ic_notification, getString(R.string.delete), deleteActionPendingIntent));
            } else {
                builder.setContentIntent(null);
                builder.clearActions();
            }
            notificationManager.notify(NotificationUtils.DOWNLOAD_REDDIT_VIDEO_NOTIFICATION_ID + randomNotificationIdOffset, builder.build());
        }
    }
}
