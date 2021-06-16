package ml.docilealligator.infinityforreddit.settings;

import android.app.WallpaperColors;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.CustomThemeListingActivity;
import ml.docilealligator.infinityforreddit.activities.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeViewModel;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreferenceFragment extends PreferenceFragmentCompat {

    private AppCompatActivity activity;
    @Inject
    @Named("light_theme")
    SharedPreferences lightThemeSharedPreferences;
    @Inject
    @Named("dark_theme")
    SharedPreferences darkThemeSharedPreferences;
    @Inject
    @Named("amoled_theme")
    SharedPreferences amoledThemeSharedPreferences;
    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;
    @Inject
    CustomThemeWrapper customThemeWrapper;
    @Inject
    Executor executor;
    public CustomThemeViewModel customThemeViewModel;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);
        SwitchPreference amoledDarkSwitch = findPreference(SharedPreferencesUtils.AMOLED_DARK_KEY);
        Preference customizeLightThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_LIGHT_THEME);
        Preference customizeDarkThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_DARK_THEME);
        Preference customizeAmoledThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_AMOLED_THEME);
        Preference selectAndCustomizeThemePreference = findPreference(SharedPreferencesUtils.MANAGE_THEMES);
        SwitchPreference enableMaterialYouSwitchPreference = findPreference(SharedPreferencesUtils.ENABLE_MATERIAL_YOU);

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        if (themePreference != null && amoledDarkSwitch != null) {
            if (systemDefault) {
                themePreference.setEntries(R.array.settings_theme_q);
            } else {
                themePreference.setEntries(R.array.settings_theme);
            }

            themePreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int option = Integer.parseInt((String) newValue);
                switch (option) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                        customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                        if (amoledDarkSwitch.isChecked()) {
                            customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                        } else {
                            customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                        }
                        break;
                    case 2:
                        if (systemDefault) {
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                        } else {
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                        }

                        if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
                            customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.LIGHT);
                        } else {
                            if (amoledDarkSwitch.isChecked()) {
                                customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.AMOLED);
                            } else {
                                customThemeWrapper.setThemeType(CustomThemeSharedPreferencesUtils.DARK);
                            }
                        }
                }
                return true;
            });
        }

        if (amoledDarkSwitch != null) {
            amoledDarkSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_NO) {
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    ActivityCompat.recreate(activity);
                }
                return true;
            });
        }

        if (customizeLightThemePreference != null) {
            customizeLightThemePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_TYPE, CustomizeThemeActivity.EXTRA_LIGHT_THEME);
                startActivity(intent);
                return true;
            });
        }

        if (customizeDarkThemePreference != null) {
            customizeDarkThemePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_TYPE, CustomizeThemeActivity.EXTRA_DARK_THEME);
                startActivity(intent);
                return true;
            });
        }

        if (customizeAmoledThemePreference != null) {
            customizeAmoledThemePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, CustomizeThemeActivity.class);
                intent.putExtra(CustomizeThemeActivity.EXTRA_THEME_TYPE, CustomizeThemeActivity.EXTRA_AMOLED_THEME);
                startActivity(intent);
                return true;
            });
        }

        if (selectAndCustomizeThemePreference != null) {
            selectAndCustomizeThemePreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, CustomThemeListingActivity.class);
                startActivity(intent);
                return true;
            });
        }

        if (enableMaterialYouSwitchPreference != null) {
            Handler handler = new Handler();
            enableMaterialYouSwitchPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    if (true) {
                        executor.execute(() -> {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                                WallpaperManager wallpaperManager = WallpaperManager.getInstance(activity);
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
                    return true;
                }
            });
        }

        customThemeViewModel = new ViewModelProvider(this,
                new CustomThemeViewModel.Factory(redditDataRoomDatabase))
                .get(CustomThemeViewModel.class);
        customThemeViewModel.getCurrentLightThemeLiveData().observe(this, customTheme -> {
            if (customizeLightThemePreference != null) {
                if (customTheme != null) {
                    customizeLightThemePreference.setVisible(true);
                    customizeLightThemePreference.setSummary(customTheme.name);
                } else {
                    customizeLightThemePreference.setVisible(false);
                }
            }
        });
        customThemeViewModel.getCurrentDarkThemeLiveData().observe(this, customTheme -> {
            if (customizeDarkThemePreference != null) {
                if (customTheme != null) {
                    customizeDarkThemePreference.setVisible(true);
                    customizeDarkThemePreference.setSummary(customTheme.name);
                } else {
                    customizeDarkThemePreference.setVisible(false);
                }
            }
        });
        customThemeViewModel.getCurrentAmoledThemeLiveData().observe(this, customTheme -> {
            if (customizeAmoledThemePreference != null) {
                if (customTheme != null) {
                    customizeAmoledThemePreference.setVisible(true);
                    customizeAmoledThemePreference.setSummary(customTheme.name);
                } else {
                    customizeAmoledThemePreference.setVisible(false);
                }
            }
        });
    }

    private int shiftColorTo255(int color, double ratio) {
        int offset = (int) (Math.min(Math.min(255 - Color.red(color), 255 - Color.green(color)), 255 - Color.blue(color)) * ratio);
        return Color.argb(Color.alpha(color), Color.red(color) + offset,
                Color.green(color) + offset,
                Color.blue(color) + offset);
    }

    private int shiftColorTo0(int color, double ratio) {
        int offset = (int) (Math.min(Math.min(Color.red(color), Color.green(color)), Color.blue(color)) * ratio);
        return Color.argb(Color.alpha(color), Color.red(color) - offset,
                Color.green(color) - offset,
                Color.blue(color) - offset);

    }

    @ColorInt
    public int getAppropriateTextColor(@ColorInt int color) {
        // Counting the perceptive luminance - human eye favors green color...
        double luminance = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255;
        return luminance < 0.5 ? Color.BLACK : Color.WHITE;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
