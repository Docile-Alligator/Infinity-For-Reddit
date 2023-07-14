package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;

public class DeleteTheme {

    public static void deleteTheme(Executor executor, Handler handler,
                                   RedditDataRoomDatabase redditDataRoomDatabase, String themeName,
                                   DeleteThemeListener deleteThemeListener) {
        executor.execute(() -> {
            CustomTheme customTheme = redditDataRoomDatabase.customThemeDao().getCustomTheme(themeName);
            if (customTheme != null) {
                boolean isLightTheme = customTheme.isLightTheme;
                boolean isDarkTheme = customTheme.isDarkTheme;
                boolean isAmoledTheme = customTheme.isAmoledTheme;
                redditDataRoomDatabase.customThemeDao().deleteCustomTheme(themeName);
                handler.post(() -> deleteThemeListener.success(isLightTheme, isDarkTheme, isAmoledTheme));
            } else {
                handler.post(() -> deleteThemeListener.success(false, false, false));
            }
        });
    }

    public interface DeleteThemeListener {
        void success(boolean isLightTheme, boolean isDarkTheme, boolean isAmoledTheme);
    }
}
