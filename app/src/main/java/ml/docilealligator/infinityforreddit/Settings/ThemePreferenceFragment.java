package ml.docilealligator.infinityforreddit.Settings;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

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

    private Activity activity;
    @Inject
    CustomThemeWrapper customThemeWrapper;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.theme_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);
        SwitchPreference amoledDarkSwitch = findPreference(SharedPreferencesUtils.AMOLED_DARK_KEY);

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
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
