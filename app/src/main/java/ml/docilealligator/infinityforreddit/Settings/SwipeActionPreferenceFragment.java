package ml.docilealligator.infinityforreddit.Settings;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.Event.ChangeDisableSwipingBetweenTabsEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeEnableSwipeActionSwitchEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeVibrateWhenActionTriggeredEvent;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

public class SwipeActionPreferenceFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.swipe_action_preferences, rootKey);

        SwitchPreference enableSwipeActionSwitch = findPreference(SharedPreferencesUtils.ENABLE_SWIPE_ACTION);
        SwitchPreference vibrateWhenActionTriggeredSwitch = findPreference(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED);
        SwitchPreference disableSwipingBetweenTabsSwitch = findPreference(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS);

        if (enableSwipeActionSwitch != null) {
            enableSwipeActionSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeEnableSwipeActionSwitchEvent((Boolean) newValue));
                return true;
            });
        }
        if (vibrateWhenActionTriggeredSwitch != null) {
            vibrateWhenActionTriggeredSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeVibrateWhenActionTriggeredEvent((Boolean) newValue));
                return true;
            });
        }

        if (disableSwipingBetweenTabsSwitch != null) {
            disableSwipingBetweenTabsSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeDisableSwipingBetweenTabsEvent((Boolean) newValue));
                return true;
            });
        }
    }
}