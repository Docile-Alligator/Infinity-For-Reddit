package ml.docilealligator.infinityforreddit;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.io.IOException;

public class WallpaperSetter {
    public static final int HOME_SCREEN = 0;
    public static final int LOCK_SCREEN = 1;
    public static final int BOTH_SCREENS = 2;

    public void set(String url, int setTo, Context ctx) {
        Toast.makeText(ctx, R.string.save_image_first, Toast.LENGTH_SHORT).show();
        Glide.with(ctx).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                WallpaperManager manager = WallpaperManager.getInstance(ctx);

                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager windowManager = (WindowManager) ctx.getSystemService(Context.WINDOW_SERVICE);

                Rect rect = null;

                if (windowManager != null) {
                    windowManager.getDefaultDisplay().getMetrics(metrics);
                    int height = metrics.heightPixels;
                    int width = metrics.widthPixels;

                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                        resource = ThumbnailUtils.extractThumbnail(resource, width, height);
                    }

                    float imageAR = (float) resource.getWidth() / (float) resource.getHeight();
                    float screenAR = (float) width / (float) height;

                    if (imageAR > screenAR) {
                        int desiredWidth = (int) (resource.getHeight() * screenAR);
                        rect = new Rect((resource.getWidth() - desiredWidth) / 2, 0, resource.getWidth(), resource.getHeight());
                    } else {
                        int desiredHeight = (int) (resource.getWidth() / screenAR);
                        rect = new Rect(0, (resource.getHeight() - desiredHeight) / 2, resource.getWidth(), (resource.getHeight() + desiredHeight) / 2);
                    }
                }
                try {
                    switch (setTo) {
                        case HOME_SCREEN:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(resource, rect, true, WallpaperManager.FLAG_SYSTEM);
                            }
                            break;
                        case LOCK_SCREEN:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(resource, rect, true, WallpaperManager.FLAG_LOCK);
                            }
                            break;
                        case BOTH_SCREENS:
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                manager.setBitmap(resource, rect, true, WallpaperManager.FLAG_SYSTEM | WallpaperManager.FLAG_LOCK);
                            } else {
                                manager.setBitmap(resource);
                            }
                            break;
                    }
                    Toast.makeText(ctx, R.string.wallpaper_set, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    Toast.makeText(ctx, R.string.error_set_wallpaper, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }
}
