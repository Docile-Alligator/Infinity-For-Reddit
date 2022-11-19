package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ImmersiveInterfacePreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.immersive_interface_preferences, rootKey);

        SwitchPreference immersiveInterfaceSwitch = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY);
        SwitchPreference immersiveInterfaceIgnoreNavBarSwitch = findPreference(SharedPreferencesUtils.IMMERSIVE_INTERFACE_IGNORE_NAV_BAR_KEY);
        SwitchPreference disableImmersiveInterfaceInLandscapeModeSwitch = findPreference(SharedPreferencesUtils.DISABLE_IMMERSIVE_INTERFACE_IN_LANDSCAPE_MODE);

        if (immersiveInterfaceSwitch != null && immersiveInterfaceIgnoreNavBarSwitch != null
                && disableImmersiveInterfaceInLandscapeModeSwitch != null) {
            immersiveInterfaceSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                immersiveInterfaceIgnoreNavBarSwitch.setVisible((Boolean) newValue);
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });

            immersiveInterfaceIgnoreNavBarSwitch.setVisible(immersiveInterfaceSwitch.isChecked());

            immersiveInterfaceIgnoreNavBarSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });

            disableImmersiveInterfaceInLandscapeModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
            });
        }
    }
}