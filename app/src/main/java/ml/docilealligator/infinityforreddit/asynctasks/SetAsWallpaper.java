package ml.docilealligator.infinityforreddit.asynctasks;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.WallpaperSetter;

public class SetAsWallpaper {

    public static void setAsWallpaper(Executor executor, Handler handler, Bitmap bitmap, int setTo,
                                      WallpaperManager manager, WindowManager windowManager,
                                      WallpaperSetter.SetWallpaperListener setWallpaperListener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                DisplayMetrics metrics = new DisplayMetrics();
                Rect rect = null;
                Bitmap bitmapFinal = bitmap;

                if (windowManager != null) {
                    windowManager.getDefaultDisplay().getMetrics(metrics);
                    int height = metrics.heightPixels;
                    int width = metrics.widthPixels;

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        bitmapFinal = ThumbnailUtils.extractThumbnail(bitmapFinal, width, height);
                    }

                    float imageAR = (float) bitmapFinal.getWidth() / (float) bitmapFinal.getHeight();
                    float screenAR = (float) width / (float) height;

                    if (imageAR > screenAR) {
                        int desiredWidth = (int) (bitmapFinal.getHeight() * screenAR);
                        rect = new Rect((bitmapFinal.getWidth() - desiredWidth) / 2, 0, bitmapFinal.getWidth(), bitmapFinal.getHeight());
                    } else {
                        int desiredHeight = (int) (bitmapFinal.getWidth() / screenAR);
                        rect = new Rect(0, (bitmapFinal.getHeight() - desiredHeight) / 2, bitmapFinal.getWidth(), (bitmapFinal.getHeight() + desiredHeight) / 2);
                    }
                }
                try {
                    switch (setTo) {
                        case WallpaperSetter.HOME_SCREEN:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(bitmapFinal, rect, true, WallpaperManager.FLAG_SYSTEM);
                            }
                            break;
                        case WallpaperSetter.LOCK_SCREEN:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(bitmapFinal, rect, true, WallpaperManager.FLAG_LOCK);
                            }
                            break;
                        case WallpaperSetter.BOTH_SCREENS:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(bitmapFinal, rect, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                            } else {
                                manager.setBitmap(bitmapFinal);
                            }
                            break;
                    }

                    handler.post(setWallpaperListener::success);
                } catch (IOException e) {
                    handler.post(setWallpaperListener::failed);
                }
            }
        });
    }
}
