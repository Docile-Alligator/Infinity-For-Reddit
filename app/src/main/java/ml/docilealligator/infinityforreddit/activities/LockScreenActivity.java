package ml.docilealligator.infinityforreddit.activities;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;
import static androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class LockScreenActivity extends BaseActivity {

    @BindView(R.id.text_view_lock_screen_activity)
    TextView textView;
    @BindView(R.id.unlock_button_lock_screen_activity)
    MaterialButton unlockButton;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("security")
    SharedPreferences mSecurityPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock_screen);

        ButterKnife.bind(this);

        applyCustomTheme();

        unlockButton.setOnClickListener(view -> {
            authenticate();
        });

        authenticate();
    }

    private void authenticate() {
        BiometricManager biometricManager = BiometricManager.from(this);
        if (biometricManager.canAuthenticate(BIOMETRIC_STRONG | DEVICE_CREDENTIAL) == BiometricManager.BIOMETRIC_SUCCESS) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(this,
                    executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationSucceeded(
                        @NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    saveUnlockTime();
                    finish();
                }
            });

            BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(getString(R.string.unlock_account_section))
                    .setAllowedAuthenticators(BIOMETRIC_STRONG | DEVICE_CREDENTIAL)
                    .build();

            biometricPrompt.authenticate(promptInfo);
        } else {
            saveUnlockTime();
            finish();
        }
    }

    private void saveUnlockTime() {
        mSecurityPreferences.edit().putLong(SharedPreferencesUtils.LAST_UNLOCK_TIME, System.currentTimeMillis()).apply();
    }


    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        textView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        unlockButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        unlockButton.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
    }

    @Override
    public void onBackPressed() { }
}