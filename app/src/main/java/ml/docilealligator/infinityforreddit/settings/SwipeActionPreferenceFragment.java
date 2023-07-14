package ml.ino6962.postinfinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.ino6962.postinfinityforreddit.R;
import ml.ino6962.postinfinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.ino6962.postinfinityforreddit.events.ChangeDisableSwipingBetweenTabsEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeEnableSwipeActionSwitchEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeSwipeActionEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeSwipeActionThresholdEvent;
import ml.ino6962.postinfinityforreddit.events.ChangeVibrateWhenActionTriggeredEvent;
import ml.ino6962.postinfinityforreddit.utils.SharedPreferencesUtils;

public class SwipeActionPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.swipe_action_preferences, rootKey);

        SwitchPreference enableSwipeActionSwitch = findPreference(SharedPreferencesUtils.ENABLE_SWIPE_ACTION);
        ListPreference swipeLeftActionListPreference = findPreference(SharedPreferencesUtils.SWIPE_LEFT_ACTION);
        ListPreference swipeRightActionListPreference = findPreference(SharedPreferencesUtils.SWIPE_RIGHT_ACTION);
        SwitchPreference vibrateWhenActionTriggeredSwitch = findPreference(SharedPreferencesUtils.VIBRATE_WHEN_ACTION_TRIGGERED);
        SwitchPreference disableSwipingBetweenTabsSwitch = findPreference(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS);
        ListPreference swipeActionThresholdListPreference = findPreference(SharedPreferencesUtils.SWIPE_ACTION_THRESHOLD);

        if (enableSwipeActionSwitch != null) {
            enableSwipeActionSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeEnableSwipeActionSwitchEvent((Boolean) newValue));
                return true;
            });
        }

        if (swipeLeftActionListPreference != null) {
            swipeLeftActionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (swipeRightActionListPreference != null) {
                    EventBus.getDefault().post(new ChangeSwipeActionEvent(Integer.parseInt((String) newValue), Integer.parseInt(swipeRightActionListPreference.getValue())));
                } else {
                    EventBus.getDefault().post(new ChangeSwipeActionEvent(Integer.parseInt((String) newValue), -1));
                }
                return true;
            });
        }

        if (swipeRightActionListPreference != null) {
            swipeRightActionListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                if (swipeLeftActionListPreference != null) {
                    EventBus.getDefault().post(new ChangeSwipeActionEvent(Integer.parseInt(swipeLeftActionListPreference.getValue()), Integer.parseInt((String) newValue)));
                } else {
                    EventBus.getDefault().post(new ChangeSwipeActionEvent(-1, Integer.parseInt((String) newValue)));
                }
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

        if (swipeActionThresholdListPreference != null) {
            swipeActionThresholdListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeSwipeActionThresholdEvent(Float.parseFloat((String) newValue)));
                return true;
            });
        }
    }
}