package ml.docilealligator.infinityforreddit.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Adapter.UserFlairRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.FetchUserFlairs;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SelectUserFlair;
import ml.docilealligator.infinityforreddit.UserFlair;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SelectUserFlairActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String USER_FLAIRS_STATE = "UFS";

    @BindView(R.id.coordinator_layout_select_user_flair_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_select_user_flair_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_select_user_flair_activity)
    Toolbar toolbar;
    @BindView(R.id.recycler_view_select_user_flair_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private LinearLayoutManager mLinearLayoutManager;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private ArrayList<UserFlair> mUserFlairs;
    private String mSubredditName;
    private UserFlairRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_user_flair);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        mSubredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        setTitle(mSubredditName);

        if (savedInstanceState != null) {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            mUserFlairs = savedInstanceState.getParcelableArrayList(USER_FLAIRS_STATE);

            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            getCurrentAccountAndBindView();
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            bindView();
        }).execute();
    }

    private void bindView() {
        if (mUserFlairs == null) {
            FetchUserFlairs.fetchUserFlairsInSubreddit(mOauthRetrofit, mAccessToken, mSubredditName,
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
        mAdapter = new UserFlairRecyclerViewAdapter(mCustomThemeWrapper, mUserFlairs, (userFlair, editUserFlair) -> {
            if (editUserFlair) {
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_flair, null);
                EditText flairEditText = dialogView.findViewById(R.id.flair_edit_text_edit_flair_dialog);
                flairEditText.setText(userFlair.getText());
                flairEditText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                }
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.edit_flair)
                        .setView(dialogView)
                        .setPositiveButton(R.string.ok, (dialogInterface, i)
                                -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(flairEditText.getWindowToken(), 0);
                            }
                            userFlair.setText(flairEditText.getText().toString());
                            selectUserFlair(userFlair);
                        })
                        .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(flairEditText.getWindowToken(), 0);
                            }
                        })
                        .setOnDismissListener(dialogInterface -> {
                            if (imm != null) {
                                imm.hideSoftInputFromWindow(flairEditText.getWindowToken(), 0);
                            }
                        })
                        .show();
            } else {
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.select_this_user_flair)
                        .setMessage(userFlair.getText())
                        .setPositiveButton(R.string.yes, (dialogInterface, i) -> selectUserFlair(userFlair))
                        .setNegativeButton(R.string.no, null)
                        .show();
            }
        });
        mLinearLayoutManager = new LinearLayoutManager(SelectUserFlairActivity.this);
        recyclerView.setLayoutManager(mLinearLayoutManager);
        recyclerView.setAdapter(mAdapter);
    }

    private void selectUserFlair(UserFlair userFlair) {
        SelectUserFlair.selectUserFlair(mOauthRetrofit, mAccessToken, userFlair, mSubredditName, mAccountName,
                new SelectUserFlair.SelectUserFlairListener() {
                    @Override
                    public void success() {
                        Toast.makeText(SelectUserFlairActivity.this, R.string.select_user_flair_success, Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void failed(String errorMessage) {
                        if (errorMessage == null || errorMessage.equals("")) {
                            Snackbar.make(coordinatorLayout, R.string.select_user_flair_success, Snackbar.LENGTH_SHORT).show();
                        } else {
                            Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
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
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putParcelableArrayList(USER_FLAIRS_STATE, mUserFlairs);
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
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}