package ml.docilealligator.infinityforreddit.settings;

import android.os.Bundle;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeDisableSwipingBetweenTabsEvent;
import ml.docilealligator.infinityforreddit.events.ChangeEnableSwipeActionSwitchEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionEvent;
import ml.docilealligator.infinityforreddit.events.ChangeSwipeActionThresholdEvent;
import ml.docilealligator.infinityforreddit.events.ChangeVibrateWhenActionTriggeredEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SwipeActionPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.swipe_action_preferences, rootKey);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

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