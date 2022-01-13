package ml.docilealligator.infinityforreddit.settings;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.biometric.BiometricManager;
import androidx.preference.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class MainPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        Preference securityPreference = findPreference(SharedPreferencesUtils.SECURITY);
        Preference postFilterPreference = findPreference(SharedPreferencesUtils.POST_FILTER);

        BiometricManager biometricManager = BiometricManager.from(activity);
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) != BiometricManager.BIOMETRIC_SUCCESS) {
            if (securityPreference != null) {
                securityPreference.setVisible(false);
            }
        }

        if (postFilterPreference != null) {
            postFilterPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, PostFilterPreferenceActivity.class);
                activity.startActivity(intent);
                return true;
            });
        }
    }
}
