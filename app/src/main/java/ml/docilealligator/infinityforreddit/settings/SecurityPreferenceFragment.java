package ml.docilealligator.infinityforreddit.settings;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceManager;
import androidx.preference.SwitchPreference;

import org.greenrobot.eventbus.EventBus;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.events.ChangeAppLockEvent;
import ml.docilealligator.infinityforreddit.events.ChangeRequireAuthToAccountSectionEvent;
import ml.docilealligator.infinityforreddit.events.ToggleSecureModeEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SecurityPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(SharedPreferencesUtils.SECURITY_SHARED_PREFERENCES_FILE);
        setPreferencesFromResource(R.xml.security_preferences, rootKey);

        ((Infinity) activity.getApplication()).getAppComponent().inject(this);

        if (activity.typeface != null) {
            setFont(activity.typeface);
        }

        SwitchPreference requireAuthToAccountSectionSwitch = findPreference(SharedPreferencesUtils.REQUIRE_AUTHENTICATION_TO_GO_TO_ACCOUNT_SECTION_IN_NAVIGATION_DRAWER);
        SwitchPreference secureModeSwitch = findPreference(SharedPreferencesUtils.SECURE_MODE);
        SwitchPreference appLockSwitch = findPreference(SharedPreferencesUtils.APP_LOCK);
        ListPreference appLockTimeoutListPreference = findPreference(SharedPreferencesUtils.APP_LOCK_TIMEOUT);

        if (requireAuthToAccountSectionSwitch != null) {
            requireAuthToAccountSectionSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeRequireAuthToAccountSectionEvent((Boolean) newValue));
                return true;
            });
        }

        if (secureModeSwitch != null) {
            secureModeSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ToggleSecureModeEvent((Boolean) newValue));
                return true;
            });
        }

        if (appLockSwitch != null && appLockTimeoutListPreference != null) {
            appLockSwitch.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeAppLockEvent((Boolean) newValue, Long.parseLong(appLockTimeoutListPreference.getValue())));
                return true;
            });

            appLockTimeoutListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                EventBus.getDefault().post(new ChangeAppLockEvent(appLockSwitch.isChecked(), Long.parseLong((String) newValue)));
                return true;
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Executor executor = ContextCompat.getMainExecutor(activity);
        BiometricPrompt biometricPrompt = new BiometricPrompt(SecurityPreferenceFragment.this,
                executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                activity.onBackPressed();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle(activity.getString(R.string.unlock))
                .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}