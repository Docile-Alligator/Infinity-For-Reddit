package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.message.ComposeMessage;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SendPrivateMessageActivity extends BaseActivity {
    public static final String EXTRA_RECIPIENT_USERNAME = "ERU";
    @BindView(R.id.coordinator_layout_send_private_message_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_send_private_message_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_send_private_message_activity)
    Toolbar toolbar;
    @BindView(R.id.username_edit_text_send_private_message_activity)
    EditText usernameEditText;
    @BindView(R.id.divider_1_send_private_message_activity)
    View divider1;
    @BindView(R.id.subjet_edit_text_send_private_message_activity)
    EditText subjectEditText;
    @BindView(R.id.divider_2_send_private_message_activity)
    View divider2;
    @BindView(R.id.content_edit_text_send_private_message_activity)
    EditText messageEditText;
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
    private String mAccessToken;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_private_message);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        setSupportActionBar(toolbar);

        String username = getIntent().getStringExtra(EXTRA_RECIPIENT_USERNAME);
        if (username != null) {
            usernameEditText.setText(username);
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
                if (usernameEditText.getText() == null || usernameEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(coordinatorLayout, R.string.message_username_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (subjectEditText.getText() == null || subjectEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(coordinatorLayout, R.string.message_subject_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                if (messageEditText.getText() == null || messageEditText.getText().toString().equals("")) {
                    isSubmitting = false;
                    Snackbar.make(coordinatorLayout, R.string.message_content_required, Snackbar.LENGTH_LONG).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar sendingSnackbar = Snackbar.make(coordinatorLayout, R.string.sending_message, Snackbar.LENGTH_INDEFINITE);
                sendingSnackbar.show();

                ComposeMessage.composeMessage(mOauthRetrofit, mAccessToken, getResources().getConfiguration().locale,
                        usernameEditText.getText().toString(), subjectEditText.getText().toString(),
                        messageEditText.getText().toString(), new ComposeMessage.ComposeMessageListener() {
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
                                    Snackbar.make(coordinatorLayout, R.string.send_message_failed, Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_LONG).show();
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
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        usernameEditText.setTextColor(primaryTextColor);
        subjectEditText.setTextColor(primaryTextColor);
        messageEditText.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        usernameEditText.setHintTextColor(secondaryTextColor);
        subjectEditText.setHintTextColor(secondaryTextColor);
        messageEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        divider1.setBackgroundColor(dividerColor);
        divider2.setBackgroundColor(dividerColor);
        if (typeface != null) {
            usernameEditText.setTypeface(typeface);
            subjectEditText.setTypeface(typeface);
            messageEditText.setTypeface(typeface);
        }
    }
}