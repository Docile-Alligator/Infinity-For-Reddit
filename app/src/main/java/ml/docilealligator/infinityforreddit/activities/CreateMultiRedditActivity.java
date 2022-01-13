package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.multireddit.CreateMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CreateMultiRedditActivity extends BaseActivity {

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String SELECTED_SUBREDDITS_STATE = "SSS";
    @BindView(R.id.coordinator_layout_create_multi_reddit_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_create_multi_reddit_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_create_multi_reddit_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_create_multi_reddit_activity)
    Toolbar toolbar;
    @BindView(R.id.multi_reddit_name_edit_text_create_multi_reddit_activity)
    EditText nameEditText;
    @BindView(R.id.divider_1_create_multi_reddit_activity)
    View divider1;
    @BindView(R.id.description_edit_text_create_multi_reddit_activity)
    EditText descriptionEditText;
    @BindView(R.id.divider_2_create_multi_reddit_activity)
    View divider2;
    @BindView(R.id.visibility_wrapper_linear_layout_create_multi_reddit_activity)
    LinearLayout visibilityLinearLayout;
    @BindView(R.id.visibility_text_view_create_multi_reddit_activity)
    TextView visibilityTextView;
    @BindView(R.id.visibility_switch_create_multi_reddit_activity)
    Switch visibilitySwitch;
    @BindView(R.id.select_subreddit_text_view_create_multi_reddit_activity)
    TextView selectSubredditTextView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
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
    private ArrayList<String> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();
        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_multi_reddit);

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
            mSubreddits = savedInstanceState.getStringArrayList(SELECTED_SUBREDDITS_STATE);
        } else {
            mSubreddits = new ArrayList<>();
        }
        bindView();
    }

    private void bindView() {
        selectSubredditTextView.setOnClickListener(view -> {
            Intent intent = new Intent(CreateMultiRedditActivity.this, SelectedSubredditsAndUsersActivity.class);
            intent.putStringArrayListExtra(SelectedSubredditsAndUsersActivity.EXTRA_SELECTED_SUBREDDITS, mSubreddits);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_multi_reddit_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_create_multi_reddit_activity) {
            if (nameEditText.getText() == null || nameEditText.getText().toString().equals("")) {
                Snackbar.make(coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (mAccessToken != null) {
                String jsonModel = new MultiRedditJSONModel(nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                        visibilitySwitch.isChecked(), mSubreddits).createJSONModel();
                CreateMultiReddit.createMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                        "/user/" + mAccountName + "/m/" + nameEditText.getText().toString(),
                        jsonModel, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorCode) {
                                if (errorCode == 409) {
                                    Snackbar.make(coordinatorLayout, R.string.duplicate_multi_reddit, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(coordinatorLayout, R.string.create_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                CreateMultiReddit.anonymousCreateMultiReddit(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        "/user/-/m/" + nameEditText.getText().toString(),
                        nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                        mSubreddits, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorType) {
                                //Will not be called
                            }
                        });
            }
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                mSubreddits = data.getStringArrayListExtra(
                        SubredditMultiselectionActivity.EXTRA_RETURN_SELECTED_SUBREDDITS);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArrayList(SELECTED_SUBREDDITS_STATE, mSubreddits);
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
