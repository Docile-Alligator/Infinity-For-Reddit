package ml.docilealligator.infinityforreddit.utils;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;

import androidx.annotation.ColorInt;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;

public class MaterialYouUtils {
    public static void changeTheme(Context context, Executor executor, Handler handler,
                                   CustomThemeWrapper customThemeWrapper,
                                   SharedPreferences lightThemeSharedPreferences,
                                   SharedPreferences darkThemeSharedPreferences,
                                   SharedPreferences amoledThemeSharedPreferences) {
        executor.execute(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
                WallpaperColors wallpaperColors = wallpaperManager.getWallpaperColors(WallpaperManager.FLAG_SYSTEM);

                if (wallpaperColors != null) {
                    int colorPrimaryInt = shiftColorTo255(wallpaperColors.getPrimaryColor().toArgb(), 0.4);
                    int colorPrimaryDarkInt = shiftColorTo0(colorPrimaryInt, 0.4);
                    int backgroundColor = shiftColorTo255(colorPrimaryInt, 0.6);
                    int cardViewBackgroundColor = shiftColorTo255(colorPrimaryInt, 0.9);
                    Color colorAccent = wallpaperColors.getSecondaryColor();
                    int colorAccentInt = shiftColorTo255(colorAccent == null ? customThemeWrapper.getColorAccent() : colorAccent.toArgb(), 0.4);

                    int colorPrimaryAppropriateTextColor = getAppropriateTextColor(colorPrimaryInt);
                    int backgroundColorAppropriateTextColor = getAppropriateTextColor(backgroundColor);

                    lightThemeSharedPreferences.edit().putInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_DARK, colorPrimaryDarkInt)
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT, colorAccentInt)
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.BACKGROUND_COLOR, backgroundColor)
                            .putInt(CustomThemeSharedPreferencesUtils.CARD_VIEW_BACKGROUND_COLOR, cardViewBackgroundColor)
                            .putInt(CustomThemeSharedPreferencesUtils.COMMENT_BACKGROUND_COLOR, cardViewBackgroundColor)
                            .putInt(CustomThemeSharedPreferencesUtils.AWARDED_COMMENT_BACKGROUND_COLOR, cardViewBackgroundColor)
                            .putInt(CustomThemeSharedPreferencesUtils.BOTTOM_APP_BAR_BACKGROUND_COLOR, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.NAV_BAR_COLOR, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.PRIMARY_TEXT_COLOR, backgroundColorAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.BOTTOM_APP_BAR_ICON_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.PRIMARY_ICON_COLOR, backgroundColorAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.FAB_ICON_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TOOLBAR_PRIMARY_TEXT_AND_ICON_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TOOLBAR_SECONDARY_TEXT_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_INDICATOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TEXT_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_COLLAPSED_COLLAPSING_TOOLBAR_TAB_BACKGROUND, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_BACKGROUND, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TAB_INDICATOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.TAB_LAYOUT_WITH_EXPANDED_COLLAPSING_TOOLBAR_TEXT_COLOR, colorPrimaryAppropriateTextColor)
                            .putInt(CustomThemeSharedPreferencesUtils.CIRCULAR_PROGRESS_BAR_BACKGROUND, colorPrimaryInt)
                            .putBoolean(CustomThemeSharedPreferencesUtils.LIGHT_STATUS_BAR, getAppropriateTextColor(colorPrimaryDarkInt) == Color.toArgb(Color.BLACK))
                            .apply();
                    darkThemeSharedPreferences.edit()
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME, colorPrimaryInt)
                            .apply();
                    amoledThemeSharedPreferences.edit()
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_ACCENT, colorPrimaryInt)
                            .putInt(CustomThemeSharedPreferencesUtils.COLOR_PRIMARY_LIGHT_THEME, colorPrimaryInt)
                            .apply();

                    handler.post(() -> EventBus.getDefault().post(new RecreateActivityEvent()));
                }
            }
        });
    }

    private static int shiftColorTo255(int color, double ratio) {
        int offset = (int) (Math.min(Math.min(255 - Color.red(color), 255 - Color.green(color)), 255 - Color.blue(color)) * ratio);
        return Color.argb(Color.alpha(color), Color.red(color) + offset,
                Color.green(color) + offset,
                Color.blue(color) + offset);
    }

    private static int shiftColorTo0(int color, double ratio) {
        int offset = (int) (Math.min(Math.min(Color.red(color), Color.green(color)), Color.blue(color)) * ratio);
        return Color.argb(Color.alpha(color), Color.red(color) - offset,
                Color.green(color) - offset,
                Color.blue(color) - offset);

    }

    @ColorInt
    public static int getAppropriateTextColor(@ColorInt int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double luminance = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5 ? Color.BLACK : Color.WHITE;
    }
}
