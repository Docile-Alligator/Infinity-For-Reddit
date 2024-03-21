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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.inputmethod.EditorInfoCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditMultiRedditBinding;
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
    private MultiReddit multiReddit;
    private String multipath;
    private ActivityEditMultiRedditBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityEditMultiRedditBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutEditMultiRedditActivity);
        }

        setSupportActionBar(binding.toolbarEditMultiRedditActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.visibilityWrapperLinearLayoutEditMultiRedditActivity.setVisibility(View.GONE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                binding.multiRedditNameEditTextEditMultiRedditActivity.setImeOptions(binding.multiRedditNameEditTextEditMultiRedditActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
                binding.descriptionEditTextEditMultiRedditActivity.setImeOptions(binding.descriptionEditTextEditMultiRedditActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
            }
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
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                FetchMultiRedditInfo.anonymousFetchMultiRedditInfo(mExecutor, new Handler(),
                        mRedditDataRoomDatabase, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                            @Override
                            public void success(MultiReddit multiReddit) {
                                EditMultiRedditActivity.this.multiReddit = multiReddit;
                                binding.progressBarEditMultiRedditActivity.setVisibility(View.GONE);
                                binding.linearLayoutEditMultiRedditActivity.setVisibility(View.VISIBLE);
                                binding.multiRedditNameEditTextEditMultiRedditActivity.setText(multiReddit.getDisplayName());
                                binding.descriptionEditTextEditMultiRedditActivity.setText(multiReddit.getDescription());
                            }

                            @Override
                            public void failed() {
                                //Will not be called
                            }
                        });
            } else {
                FetchMultiRedditInfo.fetchMultiRedditInfo(mRetrofit, accessToken, multipath, new FetchMultiRedditInfo.FetchMultiRedditInfoListener() {
                    @Override
                    public void success(MultiReddit multiReddit) {
                        EditMultiRedditActivity.this.multiReddit = multiReddit;
                        binding.progressBarEditMultiRedditActivity.setVisibility(View.GONE);
                        binding.linearLayoutEditMultiRedditActivity.setVisibility(View.VISIBLE);
                        binding.multiRedditNameEditTextEditMultiRedditActivity.setText(multiReddit.getDisplayName());
                        binding.descriptionEditTextEditMultiRedditActivity.setText(multiReddit.getDescription());
                        binding.visibilitySwitchEditMultiRedditActivity.setChecked(!multiReddit.getVisibility().equals("public"));
                    }

                    @Override
                    public void failed() {
                        Snackbar.make(binding.coordinatorLayoutEditMultiRedditActivity, R.string.cannot_fetch_multireddit, Snackbar.LENGTH_SHORT).show();
                    }
                });
            }
        } else {
            binding.progressBarEditMultiRedditActivity.setVisibility(View.GONE);
            binding.linearLayoutEditMultiRedditActivity.setVisibility(View.VISIBLE);
            binding.multiRedditNameEditTextEditMultiRedditActivity.setText(multiReddit.getDisplayName());
            binding.descriptionEditTextEditMultiRedditActivity.setText(multiReddit.getDescription());
            binding.visibilitySwitchEditMultiRedditActivity.setChecked(!multiReddit.getVisibility().equals("public"));
        }
            binding.selectSubredditTextViewEditMultiRedditActivity.setOnClickListener(view -> {
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
            if (binding.multiRedditNameEditTextEditMultiRedditActivity.getText() == null || binding.multiRedditNameEditTextEditMultiRedditActivity.getText().toString().equals("")) {
                Snackbar.make(binding.coordinatorLayoutEditMultiRedditActivity, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                String name = binding.multiRedditNameEditTextEditMultiRedditActivity.getText().toString();
                multiReddit.setDisplayName(name);
                multiReddit.setName(name);
                multiReddit.setDescription(binding.descriptionEditTextEditMultiRedditActivity.getText().toString());
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
                String jsonModel = new MultiRedditJSONModel(binding.multiRedditNameEditTextEditMultiRedditActivity.getText().toString(), binding.descriptionEditTextEditMultiRedditActivity.getText().toString(),
                        binding.visibilitySwitchEditMultiRedditActivity.isChecked(), multiReddit.getSubreddits()).createJSONModel();
                EditMultiReddit.editMultiReddit(mRetrofit, accessToken, multiReddit.getPath(),
                        jsonModel, new EditMultiReddit.EditMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed() {
                                Snackbar.make(binding.coordinatorLayoutEditMultiRedditActivity, R.string.edit_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
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
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.coordinatorLayoutEditMultiRedditActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutEditMultiRedditActivity, binding.collapsingToolbarLayoutEditMultiRedditActivity, binding.toolbarEditMultiRedditActivity);
        binding.progressBarEditMultiRedditActivity.setIndeterminateTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorAccent()));
        int primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.multiRedditNameEditTextEditMultiRedditActivity.setTextColor(primaryTextColor);
        binding.multiRedditNameEditTextEditMultiRedditActivity.setHintTextColor(secondaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1EditMultiRedditActivity.setBackgroundColor(dividerColor);
        binding.divider2EditMultiRedditActivity.setBackgroundColor(dividerColor);
        binding.descriptionEditTextEditMultiRedditActivity.setTextColor(primaryTextColor);
        binding.descriptionEditTextEditMultiRedditActivity.setHintTextColor(secondaryTextColor);
        binding.visibilityTextViewEditMultiRedditActivity.setTextColor(primaryTextColor);
        binding.selectSubredditTextViewEditMultiRedditActivity.setTextColor(primaryTextColor);

        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.coordinatorLayoutEditMultiRedditActivity, typeface);
        }
    }
}
