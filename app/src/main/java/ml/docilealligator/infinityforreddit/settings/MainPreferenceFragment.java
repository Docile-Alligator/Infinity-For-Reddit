package ml.ino6962.postinfinityforreddit.settings;


import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.biometric.BiometricManager;
import androidx.preference.Preference;

import javax.inject.Inject;
import javax.inject.Named;

import ml.ino6962.postinfinityforreddit.Infinity;
import ml.ino6962.postinfinityforreddit.R;
import ml.ino6962.postinfinityforreddit.activities.LinkResolverActivity;
import ml.ino6962.postinfinityforreddit.activities.PostFilterPreferenceActivity;
import ml.ino6962.postinfinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.ino6962.postinfinityforreddit.utils.SharedPreferencesUtils;

public class MainPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.main_preferences, rootKey);
        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        Preference securityPreference = findPreference(SharedPreferencesUtils.SECURITY);
        Preference postFilterPreference = findPreference(SharedPreferencesUtils.POST_FILTER);
        Preference privacyPolicyPreference = findPreference(SharedPreferencesUtils.PRIVACY_POLICY_KEY);
        Preference redditUserAgreementPreference = findPreference(SharedPreferencesUtils.REDDIT_USER_AGREEMENT_KEY);

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

        if (privacyPolicyPreference != null) {
            privacyPolicyPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(activity, LinkResolverActivity.class);
                    intent.setData(Uri.parse("https://docile-alligator.github.io/"));
                    activity.startActivity(intent);
                    return true;
                }
            });
        }

        if (redditUserAgreementPreference != null) {
            redditUserAgreementPreference.setOnPreferenceClickListener(preference -> {
                Intent intent = new Intent(activity, LinkResolverActivity.class);
                intent.setData(Uri.parse("https://www.redditinc.com/policies/user-agreement-september-12-2021"));
                activity.startActivity(intent);
                return true;
            });
        }
    }
}
