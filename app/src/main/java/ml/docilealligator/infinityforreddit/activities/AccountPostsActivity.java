package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityAccountPostsBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class AccountPostsActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface {

    static final String EXTRA_USER_WHERE = "EUW";

    private static final String FRAGMENT_OUT_STATE = "FOS";

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private SlidrInterface mSlidrInterface;
    private String mAccessToken;
    private String mAccountName;
    private String mUserWhere;
    private Fragment mFragment;
    private PostLayoutBottomSheetFragment postLayoutBottomSheetFragment;
    private ActivityAccountPostsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityAccountPostsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.accountPostsAppbarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.accountPostsToolbar);
            }
        }

        mUserWhere = getIntent().getExtras().getString(EXTRA_USER_WHERE);
        if (mUserWhere.equals(PostPagingSource.USER_WHERE_UPVOTED)) {
            binding.accountPostsToolbar.setTitle(R.string.upvoted);
        } else if (mUserWhere.equals(PostPagingSource.USER_WHERE_DOWNVOTED)) {
            binding.accountPostsToolbar.setTitle(R.string.downvoted);
        } else if (mUserWhere.equals(PostPagingSource.USER_WHERE_HIDDEN)) {
            binding.accountPostsToolbar.setTitle(R.string.hidden);
        } else if (mUserWhere.equals(PostPagingSource.USER_WHERE_GILDED)) {
            binding.accountPostsToolbar.setTitle(R.string.gilded);
        }

        setSupportActionBar(binding.accountPostsToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.accountPostsToolbar);

        postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction()
                    .replace(binding.accountPostsFrameLayout.getId(), mFragment)
                    .commit();
        } else {
            initializeFragment();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mFragment != null) {
            return ((FragmentCommunicator) mFragment).handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
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
        binding.accountPostsCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(
                binding.accountPostsAppbarLayout, binding.accountPostsCollapsingToolbarLayout,
                binding.accountPostsToolbar);
    }

    private void initializeFragment() {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
        bundle.putString(PostFragment.EXTRA_USER_NAME, mAccountName);
        bundle.putString(PostFragment.EXTRA_USER_WHERE, mUserWhere);
        bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
        bundle.putBoolean(PostFragment.EXTRA_DISABLE_READ_POSTS, true);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(binding.accountPostsFrameLayout.getId(), mFragment)
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.account_posts_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_refresh_account_posts_activity) {
            if (mFragment != null) {
                ((PostFragment) mFragment).refresh();
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_account_posts_activity) {
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        } else if (itemId == android.R.id.home) {
            finish();
            return true;
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

    @Override
    public void sortTypeSelected(SortType sortType) {
        if (mFragment != null) {
            ((PostFragment) mFragment).changeSortType(sortType);
        }
    }

    @Override
    public void sortTypeSelected(String sortType) {

    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        ((FragmentCommunicator) mFragment).changeNSFW(changeNSFWEvent.nsfw);
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        if (mFragment != null) {
            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + mAccountName, postLayout).apply();
            ((FragmentCommunicator) mFragment).changePostLayout(postLayout);
        }
    }

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            ((PostFragment) mFragment).goBackToTop();
        }
    }

    @Override
    public void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    @Override
    public void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
        }
    }
}
