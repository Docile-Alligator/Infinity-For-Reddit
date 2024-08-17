package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.asynctasks.InsertMultireddit;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityMultiredditSelectionBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.MultiRedditListingFragment;
import ml.docilealligator.infinityforreddit.multireddit.FetchMyMultiReddits;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class MultiredditSelectionActivity extends BaseActivity implements ActivityToolbarInterface {

    public static final String EXTRA_RETURN_MULTIREDDIT = "ERM";

    private static final String INSERT_SUBSCRIBED_MULTIREDDIT_STATE = "ISSS";
    private static final String FRAGMENT_OUT_STATE = "FOS";

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
    private boolean mInsertSuccess = false;
    private Fragment mFragment;
    private ActivityMultiredditSelectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityMultiredditSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutMultiredditSelectionActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarMultiredditSelectionActivity);
            }
        }

        setSupportActionBar(binding.toolbarMultiredditSelectionActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (savedInstanceState == null) {
            bindView(true);
        } else {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_MULTIREDDIT_STATE);
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_multireddit_selection_activity, mFragment).commit();
            bindView(false);
        }
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutMultiredditSelectionActivity,
                binding.collapsingToolbarLayoutMultiredditSelectionActivity, binding.toolbarMultiredditSelectionActivity);
    }

    private void bindView(boolean initializeFragment) {
        if (isFinishing() || isDestroyed()) {
            return;
        }

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            loadMultiReddits();
        }

        if (initializeFragment) {
            mFragment = new MultiRedditListingFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(MultiRedditListingFragment.EXTRA_IS_GETTING_MULTIREDDIT_INFO, true);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_multireddit_selection_activity, mFragment).commit();
        }
    }

    private void loadMultiReddits() {
        if (!mInsertSuccess) {
            FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, accessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
                @Override
                public void success(ArrayList<MultiReddit> multiReddits) {
                    InsertMultireddit.insertMultireddits(mExecutor, new Handler(), mRedditDataRoomDatabase,
                            multiReddits, accountName, () -> {
                        mInsertSuccess = true;
                        ((MultiRedditListingFragment) mFragment).stopRefreshProgressbar();
                    });
                }

                @Override
                public void failed() {
                    mInsertSuccess = false;
                    ((MultiRedditListingFragment) mFragment).stopRefreshProgressbar();
                    Toast.makeText(MultiredditSelectionActivity.this, R.string.error_loading_multi_reddit_list, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    public void getSelectedMultireddit(MultiReddit multiReddit) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RETURN_MULTIREDDIT, multiReddit);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        outState.putBoolean(INSERT_SUBSCRIBED_MULTIREDDIT_STATE, mInsertSuccess);
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

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            ((MultiRedditListingFragment) mFragment).goBackToTop();
        }
    }
}