package Settings;


import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.ChangeNSFWEvent;
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

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);

        SwitchPreference nsfwSwitch = findPreference(SharedPreferencesUtils.NSFW_KEY);
        ListPreference listPreference = findPreference(SharedPreferencesUtils.THEME_KEY);

        if(nsfwSwitch != null) {
            nsfwSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeNSFWEvent((Boolean) newValue));
                return true;
            });
        }

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;

        if(listPreference != null) {
            if(systemDefault) {
                listPreference.setEntries(R.array.settings_theme_q);
            } else {
                listPreference.setEntries(R.array.settings_theme);
            }

            listPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                int option = Integer.parseInt((String) newValue);
                switch (option) {
                    case 0:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                        break;
                    case 1:
                        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                        break;
                    case 2:
                        if(systemDefault) {
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
