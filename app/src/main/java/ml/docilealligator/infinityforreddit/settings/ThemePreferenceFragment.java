package ml.docilealligator.infinityforreddit.settings;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
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
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.MaterialYouUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
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

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);
        SwitchPreference amoledDarkSwitch = findPreference(SharedPreferencesUtils.AMOLED_DARK_KEY);
        Preference customizeLightThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_LIGHT_THEME);
        Preference customizeDarkThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_DARK_THEME);
        Preference customizeAmoledThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_AMOLED_THEME);
        Preference selectAndCustomizeThemePreference = findPreference(SharedPreferencesUtils.MANAGE_THEMES);
        SwitchPreference enableMaterialYouSwitchPreference = findPreference(SharedPreferencesUtils.ENABLE_MATERIAL_YOU);
        Preference applyMaterialYouPreference = findPreference(SharedPreferencesUtils.APPLY_MATERIAL_YOU);

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

                        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_NO) {
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

        if (enableMaterialYouSwitchPreference != null && applyMaterialYouPreference != null) {
            applyMaterialYouPreference.setVisible(
                    sharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_MATERIAL_YOU, false));

            enableMaterialYouSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    MaterialYouUtils.changeTheme(activity, executor, new Handler(),
                            redditDataRoomDatabase, customThemeWrapper,
                            lightThemeSharedPreferences, darkThemeSharedPreferences,
                            amoledThemeSharedPreferences, null);
                    applyMaterialYouPreference.setVisible(true);
                } else {
                    applyMaterialYouPreference.setVisible(false);
                }
                return true;
            });

            applyMaterialYouPreference.setOnPreferenceClickListener(preference -> {
                MaterialYouUtils.changeTheme(activity, executor, new Handler(),
                        redditDataRoomDatabase, customThemeWrapper,
                        lightThemeSharedPreferences, darkThemeSharedPreferences,
                        amoledThemeSharedPreferences, null);
                return true;
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
}
