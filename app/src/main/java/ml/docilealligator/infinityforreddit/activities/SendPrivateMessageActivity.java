package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySendPrivateMessageBinding;
import ml.docilealligator.infinityforreddit.message.ComposeMessage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class SendPrivateMessageActivity extends BaseActivity {
    public static final String EXTRA_RECIPIENT_USERNAME = "ERU";

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private boolean isSubmitting = false;
    private ActivitySendPrivateMessageBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();
        
        super.onCreate(savedInstanceState);

        binding = ActivitySendPrivateMessageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSendPrivateMessageActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, true);

                    setMargins(binding.toolbarSendPrivateMessageActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.nestedScrollViewSendPrivateMesassgeActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom);

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        setSupportActionBar(binding.toolbarSendPrivateMessageActivity);

        String username = getIntent().getStringExtra(EXTRA_RECIPIENT_USERNAME);
        if (username != null) {
            binding.usernameEditTextSendPrivateMessageActivity.setText(username);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.send_private_message_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_send_send_private_message_activity) {
            if (!isSubmitting) {
                isSubmitting = true;
                if (binding.usernameEditTextSendPrivateMessageActivity.getText() == null || binding.usernameEditTextSendPrivateMessageActivity.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.getRoot(), R.string.message_username_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (binding.subjetEditTextSendPrivateMessageActivity.getText() == null || binding.subjetEditTextSendPrivateMessageActivity.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.getRoot(), R.string.message_subject_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (binding.contentEditTextSendPrivateMessageActivity.getText() == null || binding.contentEditTextSendPrivateMessageActivity.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(binding.getRoot(), R.string.message_content_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar sendingSnackbar = Snackbar.make(binding.getRoot(), R.string.sending_message, Snackbar.LENGTH_INDEFINITE);
                sendingSnackbar.show();

                ComposeMessage.composeMessage(mExecutor, mHandler, mOauthRetrofit, mAccessToken, getResources().getConfiguration().locale,
                        binding.usernameEditTextSendPrivateMessageActivity.getText().toString(), binding.subjetEditTextSendPrivateMessageActivity.getText().toString(),
                        binding.contentEditTextSendPrivateMessageActivity.getText().toString(), new ComposeMessage.ComposeMessageListener() {
                            @Override
                            public void composeMessageSuccess() {
                                isSubmitting = false;
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                                Toast.makeText(SendPrivateMessageActivity.this, R.string.send_message_success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void composeMessageFailed(String errorMessage) {
                                isSubmitting = false;
                                sendingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);

                                if (errorMessage == null || errorMessage.equals("")) {
                                    Snackbar.make(binding.getRoot(), R.string.send_message_failed, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSendPrivateMessageActivity,
                null, binding.toolbarSendPrivateMessageActivity);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.usernameEditTextSendPrivateMessageActivity.setTextColor(primaryTextColor);
        binding.subjetEditTextSendPrivateMessageActivity.setTextColor(primaryTextColor);
        binding.contentEditTextSendPrivateMessageActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.usernameEditTextSendPrivateMessageActivity.setHintTextColor(secondaryTextColor);
        binding.subjetEditTextSendPrivateMessageActivity.setHintTextColor(secondaryTextColor);
        binding.contentEditTextSendPrivateMessageActivity.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1SendPrivateMessageActivity.setBackgroundColor(dividerColor);
        binding.divider2SendPrivateMessageActivity.setBackgroundColor(dividerColor);
        if (typeface != null) {
            binding.usernameEditTextSendPrivateMessageActivity.setTypeface(typeface);
            binding.subjetEditTextSendPrivateMessageActivity.setTypeface(typeface);
            binding.contentEditTextSendPrivateMessageActivity.setTypeface(typeface);
        }
    }
}