package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityCreateMultiRedditBinding;
import ml.docilealligator.infinityforreddit.multireddit.CreateMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CreateMultiRedditActivity extends BaseActivity {

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 1;
    private static final String SELECTED_SUBREDDITS_STATE = "SSS";
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
    private ActivityCreateMultiRedditBinding binding;
    private ArrayList<String> mSubreddits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();
        
        super.onCreate(savedInstanceState);
        binding = ActivityCreateMultiRedditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutCreateMultiRedditActivity);
        }

        setSupportActionBar(binding.toolbarCreateMultiRedditActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.visibilityWrapperLinearLayoutCreateMultiRedditActivity.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.multiRedditNameEditTextCreateMultiRedditActivity.setImeOptions(binding.multiRedditNameEditTextCreateMultiRedditActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
                binding.descriptionEditTextCreateMultiRedditActivity.setImeOptions(binding.descriptionEditTextCreateMultiRedditActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
            }
        }

        if (savedInstanceState != null) {
            mSubreddits = savedInstanceState.getStringArrayList(SELECTED_SUBREDDITS_STATE);
        } else {
            mSubreddits = new ArrayList<>();
        }
        bindView();
    }

    private void bindView() {
        binding.selectSubredditTextViewCreateMultiRedditActivity.setOnClickListener(view -> {
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
            if (binding.multiRedditNameEditTextCreateMultiRedditActivity.getText() == null || binding.multiRedditNameEditTextCreateMultiRedditActivity.getText().toString().equals("")) {
                Snackbar.make(binding.coordinatorLayoutCreateMultiRedditActivity, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                String jsonModel = new MultiRedditJSONModel(binding.multiRedditNameEditTextCreateMultiRedditActivity.getText().toString(), binding.descriptionEditTextCreateMultiRedditActivity.getText().toString(),
                        binding.visibilitySwitchCreateMultiRedditActivity.isChecked(), mSubreddits).createJSONModel();
                CreateMultiReddit.createMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, accessToken,
                        "/user/" + accountName + "/m/" + binding.multiRedditNameEditTextCreateMultiRedditActivity.getText().toString(),
                        jsonModel, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorCode) {
                                if (errorCode == 409) {
                                    Snackbar.make(binding.coordinatorLayoutCreateMultiRedditActivity, R.string.duplicate_multi_reddit, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(binding.coordinatorLayoutCreateMultiRedditActivity, R.string.create_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
            } else {
                CreateMultiReddit.anonymousCreateMultiReddit(mExecutor, new Handler(), mRedditDataRoomDatabase,
                        "/user/-/m/" + binding.multiRedditNameEditTextCreateMultiRedditActivity.getText().toString(),
                        binding.multiRedditNameEditTextCreateMultiRedditActivity.getText().toString(), binding.descriptionEditTextCreateMultiRedditActivity.getText().toString(),
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
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.coordinatorLayoutCreateMultiRedditActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutCreateMultiRedditActivity,
                binding.collapsingToolbarLayoutCreateMultiRedditActivity, binding.toolbarCreateMultiRedditActivity);
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.multiRedditNameEditTextCreateMultiRedditActivity.setTextColor(primaryTextColor);
        binding.multiRedditNameEditTextCreateMultiRedditActivity.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1CreateMultiRedditActivity.setBackgroundColor(dividerColor);
        binding.divider2CreateMultiRedditActivity.setBackgroundColor(dividerColor);
        binding.descriptionEditTextCreateMultiRedditActivity.setTextColor(primaryTextColor);
        binding.descriptionEditTextCreateMultiRedditActivity.setHintTextColor(secondaryTextColor);
        binding.visibilityTextViewCreateMultiRedditActivity.setTextColor(primaryTextColor);
        binding.selectSubredditTextViewCreateMultiRedditActivity.setTextColor(primaryTextColor);

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.coordinatorLayoutCreateMultiRedditActivity, typeface);
        }
    }
}
