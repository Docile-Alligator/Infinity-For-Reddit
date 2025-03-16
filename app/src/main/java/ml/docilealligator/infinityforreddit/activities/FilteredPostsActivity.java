package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;

import ml.docilealligator.infinityforreddit.readpost.ReadPostsUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FilteredThingFABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SearchPostSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UserThingSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityFilteredThingBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.post.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.subreddit.SubredditViewModel;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class FilteredPostsActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface,
        MarkPostAsReadInterface, FilteredThingFABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RecyclerViewContentScrollingInterface {

    public static final String EXTRA_NAME = "ESN";
    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_TRENDING_SOURCE = "ETS";
    public static final String EXTRA_POST_TYPE_FILTER = "EPTF";
    public static final String EXTRA_CONSTRUCTED_POST_FILTER = "ECPF";
    public static final String EXTRA_CONTAIN_FLAIR = "ECF";
    public static final String EXTRA_POST_TYPE = "EPT";
    public static final String EXTRA_USER_WHERE = "EUW";

    private static final String FRAGMENT_OUT_STATE = "FOS";
    private static final int CUSTOMIZE_POST_FILTER_ACTIVITY_REQUEST_CODE = 1000;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_history")
    SharedPreferences mPostHistorySharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    public SubredditViewModel mSubredditViewModel;
    private String name;
    private String userWhere;
    private int postType;
    private PostFragment mFragment;
    private Menu mMenu;
    private boolean isNsfwSubreddit = false;
    private ActivityFilteredThingBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivityFilteredThingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutFilteredPostsActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarFilteredPostsActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.fabFilteredThingActivity.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    binding.fabFilteredThingActivity.setLayoutParams(params);
                }
            }
        }

        setSupportActionBar(binding.toolbarFilteredPostsActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarFilteredPostsActivity);

        name = getIntent().getStringExtra(EXTRA_NAME);
        postType = getIntent().getIntExtra(EXTRA_POST_TYPE, PostPagingSource.TYPE_FRONT_PAGE);

        int filter = getIntent().getIntExtra(EXTRA_POST_TYPE_FILTER, -1000);
        PostFilter postFilter = getIntent().getParcelableExtra(EXTRA_CONSTRUCTED_POST_FILTER);
        if (postFilter == null) {
            postFilter = new PostFilter();
            switch (filter) {
                case Post.NSFW_TYPE:
                    postFilter.onlyNSFW = true;
                    break;
                case Post.TEXT_TYPE:
                    postFilter.containTextType = true;
                    postFilter.containLinkType = false;
                    postFilter.containImageType = false;
                    postFilter.containGifType = false;
                    postFilter.containVideoType = false;
                    postFilter.containGalleryType = false;
                    break;
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    postFilter.containTextType = false;
                    postFilter.containLinkType = true;
                    postFilter.containImageType = false;
                    postFilter.containGifType = false;
                    postFilter.containVideoType = false;
                    postFilter.containGalleryType = false;
                    break;
                case Post.IMAGE_TYPE:
                    postFilter.containTextType = false;
                    postFilter.containLinkType = false;
                    postFilter.containImageType = true;
                    postFilter.containGifType = false;
                    postFilter.containVideoType = false;
                    postFilter.containGalleryType = false;
                    break;
                case Post.GIF_TYPE:
                    postFilter.containTextType = false;
                    postFilter.containLinkType = false;
                    postFilter.containImageType = false;
                    postFilter.containGifType = true;
                    postFilter.containVideoType = false;
                    postFilter.containGalleryType = false;
                    break;
                case Post.VIDEO_TYPE:
                    postFilter.containTextType = false;
                    postFilter.containLinkType = false;
                    postFilter.containImageType = false;
                    postFilter.containGifType = false;
                    postFilter.containVideoType = true;
                    postFilter.containGalleryType = false;
                    break;
                case Post.GALLERY_TYPE:
                    postFilter.containTextType = false;
                    postFilter.containLinkType = false;
                    postFilter.containImageType = false;
                    postFilter.containGifType = false;
                    postFilter.containVideoType = false;
                    postFilter.containGalleryType = true;
                    break;
            }

            String flair = getIntent().getStringExtra(EXTRA_CONTAIN_FLAIR);
            if (flair != null) {
                postFilter.containFlairs = flair;
            }
        }
        postFilter.allowNSFW = !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);

        if (postType == PostPagingSource.TYPE_USER) {
            userWhere = getIntent().getStringExtra(EXTRA_USER_WHERE);
            if (userWhere != null && !PostPagingSource.USER_WHERE_SUBMITTED.equals(userWhere) && mMenu != null) {
                mMenu.findItem(R.id.action_sort_filtered_thing_activity).setVisible(false);
            }
        }

        if (savedInstanceState != null) {
            mFragment = (PostFragment) getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
            bindView(postFilter, false);
        } else {
            bindView(postFilter, true);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mFragment != null) {
            return mFragment.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        }

        return super.onKeyDown(keyCode, event);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutFilteredPostsActivity,
                binding.collapsingToolbarLayoutFilteredPostsActivity, binding.toolbarFilteredPostsActivity);
        applyFABTheme(binding.fabFilteredThingActivity);
    }

    private void bindView(PostFilter postFilter, boolean initializeFragment) {
        switch (postType) {
            case PostPagingSource.TYPE_FRONT_PAGE:
            case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                getSupportActionBar().setTitle(R.string.home);
                break;
            case PostPagingSource.TYPE_SEARCH:
                getSupportActionBar().setTitle(R.string.search);
                break;
            case PostPagingSource.TYPE_SUBREDDIT:
                if (name.equals("popular") || name.equals("all")) {
                    getSupportActionBar().setTitle(name.substring(0, 1).toUpperCase() + name.substring(1));
                } else {
                    String subredditNamePrefixed = "r/" + name;
                    getSupportActionBar().setTitle(subredditNamePrefixed);

                    mSubredditViewModel = new ViewModelProvider(this,
                            new SubredditViewModel.Factory(mRedditDataRoomDatabase, name))
                            .get(SubredditViewModel.class);
                    mSubredditViewModel.getSubredditLiveData().observe(this, subredditData -> {
                        if (subredditData != null) {
                            isNsfwSubreddit = subredditData.isNSFW();
                        }
                    });
                }
                break;
            case PostPagingSource.TYPE_MULTI_REDDIT:
            case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                String multiRedditName;
                if (name.endsWith("/")) {
                    multiRedditName = name.substring(0, name.length() - 1);
                    multiRedditName = multiRedditName.substring(multiRedditName.lastIndexOf("/") + 1);
                } else {
                    multiRedditName = name.substring(name.lastIndexOf("/") + 1);
                }
                getSupportActionBar().setTitle(multiRedditName);
                break;
            case PostPagingSource.TYPE_USER:
                String usernamePrefixed = "u/" + name;
                getSupportActionBar().setTitle(usernamePrefixed);
                break;
        }

        if (initializeFragment) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, postType);
            bundle.putParcelable(PostFragment.EXTRA_FILTER, postFilter);
            if (postType == PostPagingSource.TYPE_USER) {
                bundle.putString(PostFragment.EXTRA_USER_NAME, name);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, userWhere);
            } else if (postType == PostPagingSource.TYPE_SUBREDDIT || postType == PostPagingSource.TYPE_MULTI_REDDIT
                    || postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
                bundle.putString(PostFragment.EXTRA_NAME, name);
            } else if (postType == PostPagingSource.TYPE_SEARCH) {
                bundle.putString(PostFragment.EXTRA_NAME, name);
                bundle.putString(PostFragment.EXTRA_QUERY, getIntent().getStringExtra(EXTRA_QUERY));
                bundle.putString(PostFragment.EXTRA_TRENDING_SOURCE, getIntent().getStringExtra(EXTRA_TRENDING_SOURCE));
            }
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
        }

        binding.fabFilteredThingActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, CustomizePostFilterActivity.class);
            if (mFragment != null) {
                intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, mFragment.getPostFilter());
            }
            startActivityForResult(intent, CUSTOMIZE_POST_FILTER_ACTIVITY_REQUEST_CODE);
        });

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.fabFilteredThingActivity.setOnLongClickListener(view -> {
                FilteredThingFABMoreOptionsBottomSheetFragment filteredThingFABMoreOptionsBottomSheetFragment
                        = new FilteredThingFABMoreOptionsBottomSheetFragment();
                filteredThingFABMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), filteredThingFABMoreOptionsBottomSheetFragment.getTag());
                return true;
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtered_posts_activity, menu);
        applyMenuItemTheme(menu);
        mMenu = menu;
        if (userWhere != null && !PostPagingSource.USER_WHERE_SUBMITTED.equals(userWhere)) {
            mMenu.findItem(R.id.action_sort_filtered_thing_activity).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_filtered_thing_activity) {
            switch (postType) {
                case PostPagingSource.TYPE_FRONT_PAGE:
                    SortTypeBottomSheetFragment bestSortTypeBottomSheetFragment = SortTypeBottomSheetFragment.getNewInstance(false, mFragment.getSortType());
                    bestSortTypeBottomSheetFragment.show(getSupportFragmentManager(), bestSortTypeBottomSheetFragment.getTag());
                    break;
                case PostPagingSource.TYPE_SEARCH:
                    SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment = SearchPostSortTypeBottomSheetFragment.getNewInstance(mFragment.getSortType());
                    searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                    break;
                case PostPagingSource.TYPE_SUBREDDIT:
                case PostPagingSource.TYPE_MULTI_REDDIT:
                case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
                    SortTypeBottomSheetFragment sortTypeBottomSheetFragment = SortTypeBottomSheetFragment.getNewInstance(true, mFragment.getSortType());
                    sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
                    break;
                case PostPagingSource.TYPE_USER:
                    UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment = UserThingSortTypeBottomSheetFragment.getNewInstance(mFragment.getSortType());
                    userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
            }
            return true;
        } else if (itemId == R.id.action_refresh_filtered_thing_activity) {
            if (mFragment != null) {
                ((FragmentCommunicator) mFragment).refresh();
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_filtered_post_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CUSTOMIZE_POST_FILTER_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            if (mFragment != null) {
                mFragment.changePostFilter(data.getParcelableExtra(CustomizePostFilterActivity.RETURN_EXTRA_POST_FILTER));
            }
        }
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
        mFragment.changeSortType(sortType);
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        if (mFragment != null) {
            switch (postType) {
                case PostPagingSource.TYPE_FRONT_PAGE:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_FRONT_PAGE_POST, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_SUBREDDIT:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SUBREDDIT_POST_BASE + name, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_USER:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_USER_POST_BASE + name, postLayout).apply();
                    break;
                case PostPagingSource.TYPE_SEARCH:
                    mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_SEARCH_POST, postLayout).apply();
            }
            mFragment.changePostLayout(postLayout);
        }
    }

    @Override
    public void sortTypeSelected(String sortType) {
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            mFragment.goBackToTop();
        }
    }

    @Override
    public void markPostAsRead(Post post) {
        int readPostsLimit = ReadPostsUtils.GetReadPostsLimit(accountName, mPostHistorySharedPreferences);
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, accountName, post.getId(), readPostsLimit);
    }

    @Override
    public void fabOptionSelected(int option) {
        if (option == FilteredThingFABMoreOptionsBottomSheetFragment.FAB_OPTION_FILTER) {
            Intent intent = new Intent(this, CustomizePostFilterActivity.class);
            if (mFragment != null) {
                intent.putExtra(CustomizePostFilterActivity.EXTRA_POST_FILTER, mFragment.getPostFilter());
            }
            startActivityForResult(intent, CUSTOMIZE_POST_FILTER_ACTIVITY_REQUEST_CODE);
        } else if (option == FilteredThingFABMoreOptionsBottomSheetFragment.FAB_OPTION_HIDE_READ_POSTS) {
            if (mFragment != null) {
                mFragment.hideReadPosts();
            }
        }
    }

    @Override
    public void contentScrollUp() {
        binding.fabFilteredThingActivity.show();
    }

    @Override
    public void contentScrollDown() {
        binding.fabFilteredThingActivity.hide();
    }

    public boolean isNsfwSubreddit() {
        return isNsfwSubreddit;
    }
}
