package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Event.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

/**
 * A simple {@link PreferenceFragmentCompat} subclass.
 */
public class MainPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    SharedPreferences sharedPreferences;
    private Activity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        SwitchPreference confirmToExitSwitch = findPreference(SharedPreferencesUtils.CONFIRM_TO_EXIT);
        SwitchPreference nsfwSwitch = findPreference(SharedPreferencesUtils.NSFW_KEY);
        SwitchPreference blurNSFWSwitch = findPreference(SharedPreferencesUtils.BLUR_NSFW_KEY);
        SwitchPreference blurSpoilerSwitch = findPreference(SharedPreferencesUtils.BLUR_SPOILER_KEY);

        if (confirmToExitSwitch != null) {
            confirmToExitSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    EventBus.getDefault().post(new RecreateActivityEvent());
                    return true;
                }
            });
        }

        if (nsfwSwitch != null) {
            nsfwSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeNSFWEvent((Boolean) newValue));
                if (blurNSFWSwitch != null) {
                    blurNSFWSwitch.setVisible((Boolean) newValue);
                }
                return true;
            });
        }

        if (blurNSFWSwitch != null) {
            boolean nsfwEnabled = sharedPreferences.getBoolean(SharedPreferencesUtils.NSFW_KEY, false);

            if (nsfwEnabled) {
                blurNSFWSwitch.setVisible(true);
            } else {
                blurNSFWSwitch.setVisible(false);
            }

            blurNSFWSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeNSFWBlurEvent((Boolean) newValue));
                return true;
            });
        }

        if (blurSpoilerSwitch != null) {
            blurSpoilerSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeSpoilerBlurEvent((Boolean) newValue));
                return true;
            });
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (Activity) context;
    }
}
