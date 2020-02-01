package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class GesturesAndButtonsPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    SharedPreferences sharedPreferences;
    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.gestures_and_buttons_preference, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        SwitchPreference lockJumpToNextTopLevelCommentButtonSwitch =
                findPreference(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON);
        SwitchPreference swipeUpToHideJumpToNextTopLevelCommentButtonSwitch =
                findPreference(SharedPreferencesUtils.SWIPE_UP_TO_HIDE_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON);

        if (lockJumpToNextTopLevelCommentButtonSwitch != null && swipeUpToHideJumpToNextTopLevelCommentButtonSwitch != null) {
            lockJumpToNextTopLevelCommentButtonSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if ((Boolean) newValue) {
                    swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(false);
                } else {
                    swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(true);
                }
                return true;
            });

            if (!sharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_JUMP_TO_NEXT_TOP_LEVEL_COMMENT_BUTTON, false)) {
                swipeUpToHideJumpToNextTopLevelCommentButtonSwitch.setVisible(true);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
