package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySearchSubredditsResultBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.SubredditListingFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SearchSubredditsResultActivity extends BaseActivity implements ActivityToolbarInterface {

    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESS";

    private static final String FRAGMENT_OUT_STATE = "FOS";

    Fragment mFragment;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private ActivitySearchSubredditsResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivitySearchSubredditsResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSearchSubredditsResultActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarSearchSubredditsResultActivity);
            }
        }

        setSupportActionBar(binding.toolbarSearchSubredditsResultActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarSearchSubredditsResultActivity);

        String query = getIntent().getExtras().getString(EXTRA_QUERY);

        if (savedInstanceState == null) {
            mFragment = new SubredditListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SubredditListingFragment.EXTRA_QUERY, query);
            bundle.putBoolean(SubredditListingFragment.EXTRA_IS_GETTING_SUBREDDIT_INFO, true);
            bundle.putBoolean(SubredditListingFragment.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            mFragment.setArguments(bundle);
        } else {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
        }
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.frame_layout_search_subreddits_result_activity, mFragment)
                .commit();
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSearchSubredditsResultActivity, null, binding.toolbarSearchSubredditsResultActivity);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
            getMenuInflater().inflate(R.menu.search_subreddits_result_activity, menu);
            applyMenuItemTheme(menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_save_search_subreddits_result_activity) {
            if (mFragment != null) {
                ArrayList<String> selectedSubredditNames = ((SubredditListingFragment) mFragment).getSelectedSubredditNames();
                Intent returnIntent = new Intent();
                returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, selectedSubredditNames);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
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
            ((SubredditListingFragment) mFragment).goBackToTop();
        }
    }
}
