package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.user.FetchUserFlairs;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.user.SelectUserFlair;
import ml.docilealligator.infinityforreddit.user.UserFlair;
import ml.docilealligator.infinityforreddit.adapters.UserFlairRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySelectUserFlairBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class SelectUserFlairActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    private static final String USER_FLAIRS_STATE = "UFS";

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
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private ArrayList<UserFlair> mUserFlairs;
    private String mSubredditName;
    private UserFlairRecyclerViewAdapter mAdapter;
    private ActivitySelectUserFlairBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        binding = ActivitySelectUserFlairBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutSelectUserFlairActivity);
        }

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(binding.toolbarSelectUserFlairActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarSelectUserFlairActivity);

        mSubredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        setTitle(mSubredditName);

        if (savedInstanceState != null) {
            mUserFlairs = savedInstanceState.getParcelableArrayList(USER_FLAIRS_STATE);
        }
        bindView();
    }

    private void bindView() {
        if (mUserFlairs == null) {
            FetchUserFlairs.fetchUserFlairsInSubreddit(mOauthRetrofit, accessToken, mSubredditName,
                    new FetchUserFlairs.FetchUserFlairsInSubredditListener() {
                        @Override
                        public void fetchSuccessful(ArrayList<UserFlair> userFlairs) {
                            mUserFlairs = userFlairs;
                            instantiateRecyclerView();
                        }

                        @Override
                        public void fetchFailed() {

                        }
                    });
        } else {
            instantiateRecyclerView();
        }
    }

    private void instantiateRecyclerView() {
        mAdapter = new UserFlairRecyclerViewAdapter(this, mCustomThemeWrapper, mUserFlairs, (userFlair, editUserFlair) -> {
            if (editUserFlair) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_flair, null);
                EditText flairEditText = dialogView.findViewById(R.id.flair_edit_text_edit_flair_dialog);
                flairEditText.setText(userFlair.getText());
                flairEditText.requestFocus();
                Utils.showKeyboard(this, new Handler(), flairEditText);
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_flair)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            Utils.hideKeyboard(this);
                            userFlair.setText(flairEditText.getText().toString());
                            selectUserFlair(userFlair);
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            Utils.hideKeyboard(this);
                        })
                        .setOnDismissListener(dialogInterface -> {
                            Utils.hideKeyboard(this);
                        })
                        .show();
            } else {
                if (userFlair == null) {
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.clear_user_flair)
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> selectUserFlair(userFlair))
                            .setNegativeButton(R.string.no, null)
                            .show();
                } else {
                    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                            .setTitle(R.string.select_this_user_flair)
                            .setMessage(userFlair.getText())
                            .setPositiveButton(R.string.yes, (dialogInterface, i) -> selectUserFlair(userFlair))
                            .setNegativeButton(R.string.no, null)
                            .show();
                }
            }
        });
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(SelectUserFlairActivity.this);
        binding.recyclerViewSelectUserFlairActivity.setLayoutManager(mLinearLayoutManager);
        binding.recyclerViewSelectUserFlairActivity.setAdapter(mAdapter);
    }

    private void selectUserFlair(@Nullable UserFlair userFlair) {
        SelectUserFlair.selectUserFlair(mOauthRetrofit, accessToken, userFlair, mSubredditName, accountName,
                new SelectUserFlair.SelectUserFlairListener() {
                    @Override
                    public void success() {
                        if (userFlair == null) {
                            Toast.makeText(SelectUserFlairActivity.this, R.string.clear_user_flair_success, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SelectUserFlairActivity.this, R.string.select_user_flair_success, Toast.LENGTH_SHORT).show();
                        }
                        finish();
                    }

                    @Override
                    public void failed(String errorMessage) {
                        if (errorMessage == null || errorMessage.equals("")) {
                            if (userFlair == null) {
                                Snackbar.make(binding.getRoot(), R.string.clear_user_flair_success, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(binding.getRoot(), R.string.select_user_flair_success, Snackbar.LENGTH_SHORT).show();
                            }
                        } else {
                            Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(USER_FLAIRS_STATE, mUserFlairs);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSelectUserFlairActivity, null, binding.toolbarSelectUserFlairActivity);
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}