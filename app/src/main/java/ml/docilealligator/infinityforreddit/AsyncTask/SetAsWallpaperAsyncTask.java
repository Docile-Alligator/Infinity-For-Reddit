package ml.docilealligator.infinityforreddit.AsyncTask;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;

import ml.docilealligator.infinityforreddit.WallpaperSetter;

public class SetAsWallpaperAsyncTask extends AsyncTask<Void, Void, Void> {

    private Bitmap bitmap;
    private int setTo;
    private WallpaperManager manager;
    private WindowManager windowManager;
    private WallpaperSetter.SetWallpaperListener setWallpaperListener;
    private boolean success = true;

    public SetAsWallpaperAsyncTask(Bitmap bitmap, int setTo, WallpaperManager manager, WindowManager windowManager,
                                   WallpaperSetter.SetWallpaperListener setWallpaperListener) {
        this.bitmap = bitmap;
        this.setTo = setTo;
        this.manager = manager;
        this.windowManager = windowManager;
        this.setWallpaperListener = setWallpaperListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        DisplayMetrics metrics = new DisplayMetrics();
        Rect rect = null;

        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
            int height = metrics.heightPixels;
            int width = metrics.widthPixels;

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
            }

            float imageAR = (float) bitmap.getWidth() / (float) bitmap.getHeight();
            float screenAR = (float) width / (float) height;

            if (imageAR > screenAR) {
                int desiredWidth = (int) (bitmap.getHeight() * screenAR);
                rect = new Rect((bitmap.getWidth() - desiredWidth) / 2, 0, bitmap.getWidth(), bitmap.getHeight());
            } else {
                int desiredHeight = (int) (bitmap.getWidth() / screenAR);
                rect = new Rect(0, (bitmap.getHeight() - desiredHeight) / 2, bitmap.getWidth(), (bitmap.getHeight() + desiredHeight) / 2);
            }
        }
        try {
            switch (setTo) {
                case WallpaperSetter.HOME_SCREEN:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, rect, true, WallpaperManager.FLAG_SYSTEM);
                    }
                    break;
                case WallpaperSetter.LOCK_SCREEN:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, rect, true, WallpaperManager.FLAG_LOCK);
                    }
                    break;
                case WallpaperSetter.BOTH_SCREENS:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        manager.setBitmap(bitmap, rect, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                    } else {
                        manager.setBitmap(bitmap);
                    }
                    break;
            }
        } catch (IOException e) {
            success = false;
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (success) {
            setWallpaperListener.success();
        } else {
            setWallpaperListener.failed();
        }
    }
}
