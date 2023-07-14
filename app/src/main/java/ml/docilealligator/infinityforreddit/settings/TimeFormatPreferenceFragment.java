package ml.ino6962.postinfinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.ino6962.postinfinityforreddit.R;
import ml.ino6962.postinfinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.ino6962.postinfinityforreddit.events.ChangeShowElapsedTimeEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeTimeFormatEvent;
import ml.ino6962.postinfinityforreddit.utils.SharedPreferencesUtils;

public class TimeFormatPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.time_format_preferences, rootKey);

        SwitchPreference showElapsedTimeSwitch = findPreference(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY);
        ListPreference timeFormatList = findPreference(SharedPreferencesUtils.TIME_FORMAT_KEY);

        if (showElapsedTimeSwitch != null) {
            showElapsedTimeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeShowElapsedTimeEvent((Boolean) newValue));
                return true;
            });
        }

        if (timeFormatList != null) {
            timeFormatList.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeTimeFormatEvent((String) newValue));
                return true;
            });
        }
    }
}
