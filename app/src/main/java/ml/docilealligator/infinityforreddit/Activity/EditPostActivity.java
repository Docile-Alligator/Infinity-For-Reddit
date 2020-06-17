package ml.docilealligator.infinityforreddit.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.API.RedditAPI;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditPostActivity extends BaseActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_CONTENT = "EC";
    static final String EXTRA_FULLNAME = "EF";
    static final String EXTRA_ACCESS_TOKEN = "EAT";

    @BindView(R.id.coordinator_layout_edit_post_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_edit_post_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_edit_post_activity)
    Toolbar toolbar;
    @BindView(R.id.post_title_text_view_edit_post_activity)
    TextView titleTextView;
    @BindView(R.id.divider_edit_post_activity)
    View divider;
    @BindView(R.id.post_text_content_edit_text_edit_post_activity)
    EditText contentEditText;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mFullName;
    private String mAccessToken;
    private String mPostContent;
    private boolean isSubmitting = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_post);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK_FROM_POST_DETAIL, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(toolbar);
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullName = getIntent().getStringExtra(EXTRA_FULLNAME);
        mAccessToken = getIntent().getStringExtra(EXTRA_ACCESS_TOKEN);
        titleTextView.setText(getIntent().getStringExtra(EXTRA_TITLE));
        mPostContent = getIntent().getStringExtra(EXTRA_CONTENT);
        contentEditText.setText(mPostContent);

        contentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        titleTextView.setTextColor(mCustomThemeWrapper.getPostTitleColor());
        divider.setBackgroundColor(mCustomThemeWrapper.getPostTitleColor());
        contentEditText.setTextColor(mCustomThemeWrapper.getPostContentColor());
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(contentEditText.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_post_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_send_edit_post_activity) {
            if (!isSubmitting) {
                isSubmitting = true;

                Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_SHORT).show();

                Map<String, String> params = new HashMap<>();
                params.put(APIUtils.THING_ID_KEY, mFullName);
                params.put(APIUtils.TEXT_KEY, contentEditText.getText().toString());

                mOauthRetrofit.create(RedditAPI.class)
                        .editPostOrComment(APIUtils.getOAuthHeader(mAccessToken), params)
                        .enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                isSubmitting = false;
                                Toast.makeText(EditPostActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();
                                Intent returnIntent = new Intent();
                                setResult(RESULT_OK, returnIntent);
                                finish();
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                isSubmitting = false;
                                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        });

            }
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void promptAlertDialog(int titleResId, int messageResId) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, (dialogInterface, i)
                        -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public void onBackPressed() {
        if (isSubmitting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_edit_post_detail);
        } else {
            if (contentEditText.getText().toString().equals(mPostContent)) {
                finish();
            } else {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
