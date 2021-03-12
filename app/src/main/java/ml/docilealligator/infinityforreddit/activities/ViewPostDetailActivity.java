package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.evernote.android.state.State;
import com.github.piasy.biv.BigImageViewer;
import com.github.piasy.biv.loader.glide.GlideImageLoader;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccount;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.NeedForPostListFromPostFragmentEvent;
import ml.docilealligator.infinityforreddit.events.ProvidePostListToViewPostDetailActivityEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ViewPostDetailActivity extends BaseActivity implements SortTypeSelectionCallback, ActivityToolbarInterface {

    public static final String EXTRA_POST_DATA = "EPD";
    public static final String EXTRA_POST_ID = "EPI";
    public static final String EXTRA_POST_LIST_POSITION = "EPLP";
    public static final String EXTRA_SINGLE_COMMENT_ID = "ESCI";
    public static final String EXTRA_MESSAGE_FULLNAME = "ENI";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final String EXTRA_POST_FRAGMENT_ID = "EPFI";
    public static final String EXTRA_IS_NSFW_SUBREDDIT = "EINS";
    public static final int EDIT_COMMENT_REQUEST_CODE = 3;
    public static final int GIVE_AWARD_REQUEST_CODE = 100;
    @State
    String mAccountName;
    @State
    String mNewAccountName;
    @BindView(R.id.coordinator_layout_view_post_detail)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_view_post_detail_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_view_post_detail_activity)
    Toolbar mToolbar;
    @BindView(R.id.view_pager_2_view_post_detail_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.fab_view_post_detail_activity)
    FloatingActionButton fab;
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
    @State
    ArrayList<Post> posts;
    @State
    Post post;
    private FragmentManager fragmentManager;
    private SlidrInterface mSlidrInterface;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private long postFragmentId;
    private int postListPosition = -1;
    private int orientation;
    private boolean mVolumeKeysNavigateComments;
    private boolean isNsfwSubreddit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        BigImageViewer.initialize(GlideImageLoader.with(this));

        setContentView(R.layout.activity_view_post_detail);

        Bridge.restoreInstanceState(this, savedInstanceState);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    mCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    fab.setLayoutParams(params);
                }
            }
        }

        boolean swipeBetweenPosts = mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_BETWEEN_POSTS, false);
        if (!swipeBetweenPosts && mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }
        postFragmentId = getIntent().getLongExtra(EXTRA_POST_FRAGMENT_ID, -1);
        if (swipeBetweenPosts && posts == null && postFragmentId > 0) {
            EventBus.getDefault().post(new NeedForPostListFromPostFragmentEvent(postFragmentId));
        }

        isNsfwSubreddit = getIntent().getBooleanExtra(EXTRA_IS_NSFW_SUBREDDIT, false);

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState == null) {
            post = getIntent().getParcelableExtra(EXTRA_POST_DATA);
        }

        orientation = getResources().getConfiguration().orientation;

        mToolbar.setTitle("");
        setSupportActionBar(mToolbar);
        setToolbarGoToTop(mToolbar);

        if (savedInstanceState == null) {
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        }

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);

        mVolumeKeysNavigateComments = mSharedPreferences.getBoolean(SharedPreferencesUtils.VOLUME_KEYS_NAVIGATE_COMMENTS, false);

        fab.setOnClickListener(view -> {
            if (sectionsPagerAdapter != null) {
                ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
                if (fragment != null) {
                    fragment.scrollToNextParentComment();
                }
            }
        });

        checkNewAccountAndBindView(savedInstanceState);
    }

    public void setTitle(String title) {
        if (mToolbar != null) {
            mToolbar.setTitle(title);
        }
    }

    public void showFab() {
        fab.show();
    }

    public void hideFab() {
        fab.hide();
    }

    public void showSnackBar(int resId) {
        Snackbar.make(mCoordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
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
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(mAppBarLayout, mToolbar);
        applyFABTheme(fab);
    }

    private void checkNewAccountAndBindView(Bundle savedInstanceState) {
        if (mNewAccountName != null) {
            if (mAccountName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;

                            bindView(savedInstanceState);
                        });
            } else {
                bindView(savedInstanceState);
            }
        } else {
            bindView(savedInstanceState);
        }
    }

    private void bindView(Bundle savedInstanceState) {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.setAdapter(sectionsPagerAdapter);
        if (savedInstanceState == null) {
            viewPager2.setCurrentItem(getIntent().getIntExtra(EXTRA_POST_LIST_POSITION, 0), false);
        }
    }

    public boolean isNsfwSubreddit() {
        return isNsfwSubreddit;
    }

    private void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.editComment(commentAuthor, commentContentMarkdown, position);
            }
        }
    }

    private void awardGiven(String awardsHTML, int awardCount, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.awardGiven(awardsHTML, awardCount, position);
            }
        }
    }

    public void deleteComment(String fullName, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.deleteComment(fullName, position);
            }
        }
    }

    public void showRemovedComment(Comment comment, int position) {
        if (sectionsPagerAdapter != null) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                fragment.showRemovedComment(comment, position);
            }
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onProvidePostListToViewPostDetailActivityEvent(ProvidePostListToViewPostDetailActivityEvent event) {
        if (event.postFragmentId == postFragmentId && posts == null) {
            posts = event.posts;
            if (sectionsPagerAdapter != null) {
                if (postListPosition > 0)
                    sectionsPagerAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_COMMENT_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                editComment(null,
                        data.getStringExtra(EditCommentActivity.EXTRA_EDITED_COMMENT_CONTENT),
                        data.getExtras().getInt(EditCommentActivity.EXTRA_EDITED_COMMENT_POSITION));
            }
        } else if (requestCode == GIVE_AWARD_REQUEST_CODE) {
            if (data != null && resultCode == Activity.RESULT_OK) {
                Toast.makeText(this, R.string.give_award_success, Toast.LENGTH_SHORT).show();
                int position = data.getIntExtra(GiveAwardActivity.EXTRA_RETURN_ITEM_POSITION, 0);
                String newAwardsHTML = data.getStringExtra(GiveAwardActivity.EXTRA_RETURN_NEW_AWARDS);
                int newAwardsCount = data.getIntExtra(GiveAwardActivity.EXTRA_RETURN_NEW_AWARDS_COUNT, 0);
                awardGiven(newAwardsHTML, newAwardsCount, position);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
    }

    @Override
    public void onBackPressed() {
        if (orientation == getResources().getConfiguration().orientation) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        Bridge.clear(this);
        BigImageViewer.imageLoader().cancelAll();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mVolumeKeysNavigateComments) {
            ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
            if (fragment != null) {
                switch (keyCode) {
                    case KeyEvent.KEYCODE_VOLUME_UP:
                        fragment.scrollToPreviousParentComment();
                        return true;
                    case KeyEvent.KEYCODE_VOLUME_DOWN:
                        fragment.scrollToNextParentComment();
                        return true;
                }
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.changeSortType(sortType);
            mToolbar.setTitle(sortType.getType().fullName);
        }
    }

    public void lockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.lock();
        }
    }

    public void unlockSwipeRightToGoBack() {
        if (mSlidrInterface != null) {
            mSlidrInterface.unlock();
        }
    }

    @Override
    public void onLongPress() {
        ViewPostDetailFragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment != null) {
            fragment.goToTop();
        }
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        public SectionsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            ViewPostDetailFragment fragment = new ViewPostDetailFragment();
            Bundle bundle = new Bundle();
            if (posts != null) {
                if (postListPosition == position && post != null) {
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, post);
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, position);
                    bundle.putString(ViewPostDetailFragment.EXTRA_SINGLE_COMMENT_ID, getIntent().getStringExtra(EXTRA_SINGLE_COMMENT_ID));
                    bundle.putString(ViewPostDetailFragment.EXTRA_MESSAGE_FULLNAME, getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME));
                } else {
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, posts.get(position));
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, position);
                }
            } else {
                if (post == null) {
                    bundle.putString(ViewPostDetailFragment.EXTRA_POST_ID, getIntent().getStringExtra(EXTRA_POST_ID));
                } else {
                    bundle.putParcelable(ViewPostDetailFragment.EXTRA_POST_DATA, post);
                    bundle.putInt(ViewPostDetailFragment.EXTRA_POST_LIST_POSITION, position);
                }
                bundle.putString(ViewPostDetailFragment.EXTRA_SINGLE_COMMENT_ID, getIntent().getStringExtra(EXTRA_SINGLE_COMMENT_ID));
                bundle.putString(ViewPostDetailFragment.EXTRA_MESSAGE_FULLNAME, getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME));
            }
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return posts == null ? 1 : posts.size();
        }

        @Nullable
        ViewPostDetailFragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
            if (fragment instanceof ViewPostDetailFragment) {
                return (ViewPostDetailFragment) fragment;
            }
            return null;
        }
    }
}
