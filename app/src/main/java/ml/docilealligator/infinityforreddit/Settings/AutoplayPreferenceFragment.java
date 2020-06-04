package ml.docilealligator.infinityforreddit.Settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.Event.ChangeAutoplayNsfwVideosEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeVideoAutoplayEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

public class AutoplayPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.autoplay_preferences, rootKey);

        ListPreference videoAutoplayListPreference = findPreference(SharedPreferencesUtils.VIDEO_AUTOPLAY);
        SwitchPreference autoplayNsfwVideosSwitchPreference = findPreference(SharedPreferencesUtils.AUTOPLAY_NSFW_VIDEOS);

        if (videoAutoplayListPreference != null && autoplayNsfwVideosSwitchPreference != null) {
            videoAutoplayListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeVideoAutoplayEvent((String) newValue));
                return true;
            });

            autoplayNsfwVideosSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeAutoplayNsfwVideosEvent((Boolean) newValue));
                return true;
            });
        }
    }
}