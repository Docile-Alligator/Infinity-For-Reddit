package ml.docilealligator.infinityforreddit;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.asynctasks.SetAsWallpaper;

public class WallpaperSetter {
    public static final int HOME_SCREEN = 0;
    public static final int LOCK_SCREEN = 1;
    public static final int BOTH_SCREENS = 2;

    public static void set(Executor executor, Handler handler, String url, int setTo, Context context, SetWallpaperListener setWallpaperListener) {
        Toast.makeText(context, R.string.save_image_first, Toast.LENGTH_SHORT).show();
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);

        Glide.with(context).asBitmap().load(url).into(new CustomTarget<Bitmap>() {
            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                SetAsWallpaper.setAsWallpaper(executor, handler, resource, setTo, wallpaperManager,
                        windowManager, setWallpaperListener);
            }

            @Override
            public void onLoadCleared(@Nullable Drawable placeholder) {

            }
        });
    }

    public interface SetWallpaperListener {
        void success();
        void failed();
    }
}
