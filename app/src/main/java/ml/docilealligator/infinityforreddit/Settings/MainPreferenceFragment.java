package ml.docilealligator.infinityforreddit.Settings;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Event.ChangeNSFWBlurEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSavePostFeedScrolledPositionEvent;
import ml.docilealligator.infinityforreddit.Event.ChangeSpoilerBlurEvent;
import ml.docilealligator.infinityforreddit.Event.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

public class MainPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("post_feed_scrolled_position_cache")
    SharedPreferences cache;
    private AppCompatActivity activity;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        Preference securityPreference = findPreference(SharedPreferencesUtils.SECURITY);
        SwitchPreference savePostFeedScrolledPositionSwitch = findPreference(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION);
        SwitchPreference confirmToExitSwitch = findPreference(SharedPreferencesUtils.CONFIRM_TO_EXIT);
        SwitchPreference nsfwSwitch = findPreference(SharedPreferencesUtils.NSFW_KEY);
        SwitchPreference blurNSFWSwitch = findPreference(SharedPreferencesUtils.BLUR_NSFW_KEY);
        SwitchPreference blurSpoilerSwitch = findPreference(SharedPreferencesUtils.BLUR_SPOILER_KEY);

        if (savePostFeedScrolledPositionSwitch != null) {
            savePostFeedScrolledPositionSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                if (!(Boolean) newValue) {
                    cache.edit().clear().apply();
                }
                EventBus.getDefault().post(new ChangeSavePostFeedScrolledPositionEvent((Boolean) newValue));
                return true;
            });
        }

        if (confirmToExitSwitch != null) {
            confirmToExitSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new RecreateActivityEvent());
                return true;
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

        BiometricManager biometricManager = BiometricManager.from(activity);
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
            if (securityPreference != null) {
                securityPreference.setVisible(false);
            }
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
