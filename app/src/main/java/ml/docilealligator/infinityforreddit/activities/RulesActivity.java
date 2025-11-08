package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.RulesRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.customviews.slidr.widget.SliderPanel;
import ml.docilealligator.infinityforreddit.databinding.ActivityRulesBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.post.FetchRules;
import ml.docilealligator.infinityforreddit.subreddit.Rule;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class RulesActivity extends BaseActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mSubredditName;
    private RulesRecyclerViewAdapter mAdapter;
    private ActivityRulesBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityRulesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        SliderPanel sliderPanel = null;
        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            sliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutRulesActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }

                ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = Utils.getInsets(insets, false);

                        setMargins(binding.toolbarRulesActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        binding.recyclerViewRulesActivity.setPadding(
                                allInsets.left,
                                0,
                                allInsets.right,
                                allInsets.bottom);

                        return WindowInsetsCompat.CONSUMED;
                    }
                });

                /*adjustToolbar(binding.toolbarRulesActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    binding.recyclerViewRulesActivity.setPadding(0, 0, 0, navBarHeight);
                }*/
            }
        }

        binding.appbarLayoutRulesActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimary());
        setSupportActionBar(binding.toolbarRulesActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);

        mAdapter = new RulesRecyclerViewAdapter(this, mCustomThemeWrapper, sliderPanel, mSubredditName);
        binding.recyclerViewRulesActivity.setAdapter(mAdapter);

        FetchRules.fetchRules(mExecutor, new Handler(),
                accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit,
                accessToken, accountName, mSubredditName, new FetchRules.FetchRulesListener() {
                    @Override
                    public void success(ArrayList<Rule> rules) {
                        binding.progressBarRulesActivity.setVisibility(View.GONE);
                        if (rules == null || rules.size() == 0) {
                            binding.errorTextViewRulesActivity.setVisibility(View.VISIBLE);
                            binding.errorTextViewRulesActivity.setText(R.string.no_rule);
                            binding.errorTextViewRulesActivity.setOnClickListener(view -> {
                            });
                        }
                        mAdapter.changeDataset(rules);
                    }

                    @Override
                    public void failed() {
                        displayError();
                    }
                });
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutRulesActivity,
                binding.collapsingToolbarLayoutRulesActivity, binding.toolbarRulesActivity);
        binding.progressBarRulesActivity.setIndicatorColor(mCustomThemeWrapper.getColorAccent());
        binding.errorTextViewRulesActivity.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            binding.errorTextViewRulesActivity.setTypeface(typeface);
        }
    }

    private void displayError() {
        binding.progressBarRulesActivity.setVisibility(View.GONE);
        binding.errorTextViewRulesActivity.setVisibility(View.VISIBLE);
        binding.errorTextViewRulesActivity.setText(R.string.error_loading_rules);
        binding.errorTextViewRulesActivity.setOnClickListener(view -> {
            binding.progressBarRulesActivity.setVisibility(View.VISIBLE);
            binding.errorTextViewRulesActivity.setVisibility(View.GONE);
            FetchRules.fetchRules(mExecutor, new Handler(),
                    accountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit,
                    accessToken, accountName, mSubredditName, new FetchRules.FetchRulesListener() {
                @Override
                public void success(ArrayList<Rule> rules) {
                    binding.progressBarRulesActivity.setVisibility(View.GONE);
                    if (rules == null || rules.size() == 0) {
                        binding.errorTextViewRulesActivity.setVisibility(View.VISIBLE);
                        binding.errorTextViewRulesActivity.setText(R.string.no_rule);
                        binding.errorTextViewRulesActivity.setOnClickListener(view -> {
                        });
                    }
                    mAdapter.changeDataset(rules);
                }

                @Override
                public void failed() {
                    displayError();
                }
            });
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
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onChangeNetworkStatusEvent(ChangeNetworkStatusEvent changeNetworkStatusEvent) {
        String dataSavingMode = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        if (mAdapter != null && dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            mAdapter.setDataSavingMode(changeNetworkStatusEvent.connectedNetwork == Utils.NETWORK_TYPE_CELLULAR);
        }
    }
}
