package ml.docilealligator.infinityforreddit.Settings;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Activity.CustomThemeListingActivity;
import ml.docilealligator.infinityforreddit.Activity.CustomizeThemeActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.CustomThemeSharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

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
    CustomThemeWrapper customThemeWrapper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);
        SwitchPreference amoledDarkSwitch = findPreference(SharedPreferencesUtils.AMOLED_DARK_KEY);
        Preference customizeLightThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_LIGHT_THEME);
        Preference customizeDarkThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_DARK_THEME);
        Preference customizeAmoledThemePreference = findPreference(SharedPreferencesUtils.CUSTOMIZE_AMOLED_THEME);
        Preference selectAndCustomizeThemePreference = findPreference(SharedPreferencesUtils.SELECT_AND_CUSTOMIZE_THEME);

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
                    activity.recreate();
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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
