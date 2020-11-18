package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ImmersiveInterfacePreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.immersive_interface_preferences, rootKey);

        SwitchPreference immersiveInterfaceSwitch = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY);
        SwitchPreference immersiveInterfaceIgnoreNavBarSwitch = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_IGNORE_NAV_BAR_KEY);

        if (immersiveInterfaceSwitch != null && immersiveInterfaceIgnoreNavBarSwitch != null) {
            immersiveInterfaceSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    immersiveInterfaceIgnoreNavBarSwitch.setVisible(true);
                } else {
                    immersiveInterfaceIgnoreNavBarSwitch.setVisible(false);
                }
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });

            if (immersiveInterfaceSwitch.isChecked()) {
                immersiveInterfaceIgnoreNavBarSwitch.setVisible(true);
            } else {
                immersiveInterfaceIgnoreNavBarSwitch.setVisible(false);
            }

            immersiveInterfaceIgnoreNavBarSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }
    }
}