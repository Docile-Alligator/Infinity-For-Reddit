package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.multireddit.EditMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.FetchMultiRedditInfo;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class EditMultiRedditActivity extends BaseActivity {
    public static final String EXTRA_MULTI_PATH = "EMP";
    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String MULTI_REDDIT_STATE = "MRS";
    private static final String MULTI_PATH_STATE = "MPS";
    @BindView(R.id.coordinator_layout_edit_multi_reddit_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_edit_multi_reddit_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_edit_multi_reddit_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
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
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private String mAccountName;
    private MultiReddit multiReddit;
    private String multipath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

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

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "-");

        if (mAccessToken == null) {
            visibilityLinearLayout.setVisibility(View.GONE);
        }

        if (savedInstanceState != null) {
            multiReddit = savedInstanceState.getParcelable(MULTI_REDDIT_STATE);
            multipath = savedInstanceState.getString(MULTI_PATH_STATE);
        } else {
            multipath = getIntent().getStringExtra(EXTRA_MULTI_PATH);
        }

        bindView();
    }

    private void bindView() {
        if (multiReddit == null) {
            if (mAccessToken == null) {
                FetchMultiRedditInfo.anonymousFetchMultiRedditInfo(mExecutor, new Handler(),
                        mRedditDataRoomDatabase, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                            @Override
                            public void success(MultiReddit multiReddit) {
                                EditMultiRedditActivity.this.multiReddit = multiReddit;
                                progressBar.setVisibility(View.GONE);
                                linearLayout.setVisibility(View.VISIBLE);
                                nameEditText.setText(multiReddit.getDisplayName());
                                descriptionEditText.setText(multiReddit.getDescription());
                            }

                            @Override
                            public void failed() {
                                //Will not be called
                            }
                        });
            } else {
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
            }
        } else {
            progressBar.setVisibility(View.GONE);
            linearLayout.setVisibility(View.VISIBLE);
            nameEditText.setText(multiReddit.getDisplayName());
            descriptionEditText.setText(multiReddit.getDescription());
            visibilitySwitch.setChecked(!multiReddit.getVisibility().equals("public"));
        }

        selectSubredditTextView.setOnClickListener(view -> {
            Intent intent = new Intent(EditMultiRedditActivity.this, SelectedSubredditsAndUsersActivity.class);
            if (multiReddit.getSubreddits() != null) {
                intent.putStringArrayListExtra(SelectedSubredditsAndUsersActivity.EXTRA_SELECTED_SUBREDDITS, multiReddit.getSubreddits());
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
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_edit_multi_reddit_activity) {
            if (nameEditText.getText() == null || nameEditText.getText().toString().equals("")) {
                Snackbar.make(coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (mAccessToken == null) {
                EditMultiReddit.anonymousEditMultiReddit(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        multiReddit, new EditMultiReddit.EditMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed() {
                                //Will not be called
                            }
                        });
            } else {
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
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                multiReddit.setSubreddits(data.getStringArrayListExtra(
                        SelectedSubredditsAndUsersActivity.EXTRA_RETURN_SELECTED_SUBREDDITS));
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
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

        if (typeface != null) {
            Utils.setFontToAllTextViews(coordinatorLayout, typeface);
        }
    }
}
