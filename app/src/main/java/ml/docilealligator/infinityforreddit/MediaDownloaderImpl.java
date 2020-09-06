package ml.docilealligator.infinityforreddit;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;

import androidx.documentfile.provider.DocumentFile;

import java.io.File;

public class MediaDownloaderImpl implements MediaDownloader {

    @Override
    public void download(String url, String fileName, Context ctx) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(fileName);

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        //Android Q support
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //ctx.getContentResolver().takePersistableUriPermission(Uri.parse("content://com.android.providers.downloads.documents/tree/downloads"), Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            request.setDestinationInExternalPublicDir(DocumentFile.fromTreeUri(ctx, Uri.parse("content://com.android.providers.downloads.documents/tree/downloads")).toString(), fileName);
            //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName);
            //request.setDestinationUri(Uri.parse(Paths.get("content://com.android.providers.downloads.documents/tree/downloads", fileName).toString()));
        } else {
            String path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File directory = new File(path + "/Infinity/");
            boolean saveToInfinityFolder = true;
            if (!directory.exists()) {
                if (!directory.mkdir()) {
                    saveToInfinityFolder = false;
                }
            } else {
                if (directory.isFile()) {
                    if (!(directory.delete() && directory.mkdir())) {
                        saveToInfinityFolder = false;
                    }
                }
            }

            if (saveToInfinityFolder) {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES + "/Infinity/", fileName);
            } else {
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, fileName);
            }
        }

        DownloadManager manager = (DownloadManager) ctx.getSystemService(Context.DOWNLOAD_SERVICE);

        if (manager == null) {
            Toast.makeText(ctx, R.string.download_failed, Toast.LENGTH_SHORT).show();
            return;
        }

        manager.enqueue(request);
        Toast.makeText(ctx, R.string.download_started, Toast.LENGTH_SHORT).show();


        /*Intent intent = new Intent(ctx, DownloadVideoService.class);
        intent.putExtra(DownloadVideoService.EXTRA_VIDEO_URL, url);
        intent.putExtra(DownloadVideoService.EXTRA_FILE_NAME, fileName);
        intent.putExtra(DownloadVideoService.EXTRA_IS_REDDIT_VIDEO, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ctx.startForegroundService(intent);
        } else {
            ctx.startService(intent);
        }
        Toast.makeText(ctx, R.string.download_started, Toast.LENGTH_SHORT).show();*/
    }
}
