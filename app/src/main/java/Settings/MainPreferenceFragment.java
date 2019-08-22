package Settings;


import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class MainPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);

        SwitchPreference nsfwSwitch = findPreference(SharedPreferencesUtils.NSFW_KEY);
        if(nsfwSwitch != null) {
            nsfwSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeNSFWEvent((Boolean) newValue));
                return true;
            });
        }
    }
}
