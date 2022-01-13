package ml.docilealligator.infinityforreddit.settings;


import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeLockBottomAppBarEvent;
import ml.docilealligator.infinityforreddit.events.ChangePullToRefreshEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class GesturesAndButtonsPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gestures_and_buttons_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        SwitchPreference lockJumpToNextTopLevelCommentButtonSwitch =
                findPreference(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON);
        SwitchPreference lockBottomAppBarSwitch = findPreference(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR);
        SwitchPreference swipeUpToHideJumpToNextTopLevelCommentButtonSwitch =
                findPreference(SharedPreferencesUtils.SWIPE_UP_TO_HIDE_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON);
        SwitchPreference pullToRefreshSwitch = findPreference(SharedPreferencesUtils.PULL_TO_REFRESH);

        if (lockJumpToNextTopLevelCommentButtonSwitch != null && lockBottomAppBarSwitch != null &&
                swipeUpToHideJumpToNextTopLevelCommentButtonSwitch != null) {
            lockJumpToNextTopLevelCommentButtonSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(false);
                } else {
                    swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(true);
                }
                return true;
            });

            if (sharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false)) {
                lockBottomAppBarSwitch.setVisible(true);
                lockBottomAppBarSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                    EventBus.getDefault().post(new ChangeLockBottomAppBarEvent((Boolean) newValue));
                    return true;
                });
            }

            if (!sharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false)) {
                swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(true);
            }
        }

        if (pullToRefreshSwitch != null) {
            pullToRefreshSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    EventBus.getDefault().post(new ChangePullToRefreshEvent((Boolean) newValue));
                    return true;
                }
            });
        }
    }
}
