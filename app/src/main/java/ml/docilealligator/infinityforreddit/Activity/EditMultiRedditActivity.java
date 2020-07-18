package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MultiReddit.EditMultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.FetchMultiRedditInfo;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class EditMultiRedditActivity extends BaseActivity {
    public static final String EXTRA_MULTI_PATH = "EMP";
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String MULTI_REDDIT_STATE = "MRS";
    private static final String MULTI_PATH_STATE = "MPS";
    @BindView(R.id.coordinator_layout_edit_multi_reddit_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_edit_multi_reddit_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_edit_multi_reddit_activity)
    Toolbar toolbar;
    @BindView(R.id.progress_bar_edit_multi_reddit_activity)
    ProgressBar progressBar;
    @BindView(R.id.linear_layout_edit_multi_reddit_activity)
    LinearLayout linearLayout;
    @BindView(R.id.multi_reddit_name_edit_text_edit_multi_reddit_activity)
    EditText nameEditText;
    @BindView(R.id.divider_1_edit_multi_reddit_activity)
    View divider1;
    @BindView(R.id.description_edit_text_edit_multi_reddit_activity)
    EditText descriptionEditText;
    @BindView(R.id.divider_2_edit_multi_reddit_activity)
    View divider2;
    @BindView(R.id.visibility_wrapper_linear_layout_edit_multi_reddit_activity)
    LinearLayout visibilityLinearLayout;
    @BindView(R.id.visibility_text_view_edit_multi_reddit_activity)
    TextView visibilityTextView;
    @BindView(R.id.visibility_switch_edit_multi_reddit_activity)
    Switch visibilitySwitch;
    @BindView(R.id.select_subreddit_text_view_edit_multi_reddit_activity)
    TextView selectSubredditTextView;
    @Inject
    @Named("oauth")
    Retrofit mRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private MultiReddit multiReddit;
    private String multipath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_multi_reddit);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            multiReddit = savedInstanceState.getParcelable(MULTI_REDDIT_STATE);
            multipath = savedInstanceState.getString(MULTI_PATH_STATE);

            if (!mNullAccessToken && mAccountName == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            multipath = getIntent().getStringExtra(EXTRA_MULTI_PATH);
            getCurrentAccountAndBindView();
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
                Toast.makeText(this, R.string.logged_out, Toast.LENGTH_SHORT).show();
                finish();
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
                bindView();
            }
        }).execute();
    }

    private void bindView() {
        if (multiReddit == null) {
            FetchMultiRedditInfo.fetchMultiRedditInfo(mRetrofit, mAccessToken, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                @Override
                public void success(MultiReddit multiReddit) {
                    EditMultiRedditActivity.this.multiReddit = multiReddit;
                    progressBar.setVisibility(View.GONE);
                    linearLayout.setVisibility(View.VISIBLE);
                    nameEditText.setText(multiReddit.getDisplayName());
                    descriptionEditText.setText(multiReddit.getDescription());
                    visibilitySwitch.setChecked(!multiReddit.getVisibility().equals("public"));
                }

                @Override
                public void failed() {
                    Snackbar.make(coordinatorLayout, R.string.cannot_fetch_multireddit, Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            nameEditText.setText(multiReddit.getDisplayName());
            descriptionEditText.setText(multiReddit.getDescription());
            visibilitySwitch.setChecked(!multiReddit.getVisibility().equals("public"));
        }

        selectSubredditTextView.setOnClickListener(view -> {
            Intent intent = new Intent(EditMultiRedditActivity.this, SelectedSubredditsActivity.class);
            if (multiReddit.getSubreddits() != null) {
                intent.putStringArrayListExtra(SelectedSubredditsActivity.EXTRA_SELECTED_SUBREDDITS, multiReddit.getSubreddits());
            }
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_multi_reddit_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_save_edit_multi_reddit_activity:
                if (mAccountName == null || mAccessToken == null) {
                    Snackbar.make(coordinatorLayout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                if (nameEditText.getText() == null || nameEditText.getText().toString().equals("")) {
                    Snackbar.make(coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                String jsonModel = new MultiRedditJSONModel(nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                        visibilitySwitch.isChecked(), multiReddit.getSubreddits()).createJSONModel();
                EditMultiReddit.editMultiReddit(mRetrofit, mAccessToken, multiReddit.getPath(),
                        jsonModel, new EditMultiReddit.EditMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed() {
                                Snackbar.make(coordinatorLayout, R.string.edit_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        });
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                multiReddit.setSubreddits(data.getStringArrayListExtra(
                        SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putParcelable(MULTI_REDDIT_STATE, multiReddit);
        outState.putString(MULTI_PATH_STATE, multipath);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
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
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorAccent()));
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        nameEditText.setTextColor(primaryTextColor);
        nameEditText.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        divider1.setBackgroundColor(dividerColor);
        divider2.setBackgroundColor(dividerColor);
        descriptionEditText.setTextColor(primaryTextColor);
        descriptionEditText.setHintTextColor(secondaryTextColor);
        visibilityTextView.setTextColor(primaryTextColor);
        selectSubredditTextView.setTextColor(primaryTextColor);
    }
}
