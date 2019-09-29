package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Event.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class MainPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);

        Activity activity = getActivity();
        if (activity != null) {
            ((Infinity) activity.getApplication()).getAppComponent().inject(this);

            SwitchPreference nsfwSwitch = findPreference(SharedPreferencesUtils.NSFW_KEY);
            SwitchPreference blurNSFWSwitch = findPreference(SharedPreferencesUtils.BLUR_NSFW_KEY);
            SwitchPreference blurSpoilerSwitch = findPreference(SharedPreferencesUtils.BLUR_SPOILER_KEY);
            ListPreference themePreference = findPreference(SharedPreferencesUtils.THEME_KEY);

            if (nsfwSwitch != null) {
                nsfwSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeNSFWEvent((Boolean) newValue));
                    if (blurNSFWSwitch != null) {
                        blurNSFWSwitch.setVisible((Boolean) newValue);
                    }
                    return true;
                });
            }

            if (blurNSFWSwitch != null) {
                boolean nsfwEnabled = sharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_KEY, false);

                if (nsfwEnabled) {
                    blurNSFWSwitch.setVisible(true);
                } else {
                    blurNSFWSwitch.setVisible(false);
                }

                blurNSFWSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeNSFWBlurEvent((Boolean) newValue));
                    return true;
                });
            }

            if (blurSpoilerSwitch != null) {
                blurSpoilerSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeSpoilerBlurEvent((Boolean) newValue));
                    return true;
                });
            }

            boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

            if (themePreference != null) {
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
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                            break;
                        case 2:
                            if (systemDefault) {
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                            } else {
                                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                            }
                    }
                    return true;
                });
            }
        }
    }
}
