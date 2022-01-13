package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeShowElapsedTimeEvent;
import ml.docilealligator.infinityforreddit.events.ChangeTimeFormatEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class TimeFormatPreferenceFragment extends CustomFontPreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.time_format_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

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
