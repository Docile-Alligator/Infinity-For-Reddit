package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.fragments.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SearchPostSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SearchUserAndSubredditSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySearchResultBinding;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.fragments.SubredditListingFragment;
import ml.docilealligator.infinityforreddit.fragments.UserListingFragment;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.recentsearchquery.InsertRecentSearchQuery;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchResultActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface,
        FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback, RandomBottomSheetFragment.RandomOptionSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, RecyclerViewContentScrollingInterface {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_TRENDING_SOURCE = "ETS";
    public static final String EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME = "ESISOUN";
    public static final String EXTRA_SEARCH_IN_MULTIREDDIT = "ESIM";
    public static final String EXTRA_SEARCH_IN_THING_TYPE = "ESITT";
    public static final String EXTRA_SHOULD_RETURN_SUBREDDIT_AND_USER_NAME = "ESRSAUN";

    private static final String INSERT_SEARCH_QUERY_SUCCESS_STATE = "ISQSS";

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("sort_type")
    SharedPreferences mSortTypeSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences bottomAppBarSharedPreference;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor executor;
    private Runnable autoCompleteRunnable;
    private Call<String> subredditAutocompleteCall;
    private String mQuery;
    private String mSearchInSubredditOrUserName;
    private MultiReddit mSearchInMultiReddit;
    @SelectThingReturnKey.THING_TYPE
    private int mSearchInThingType;
    private boolean mInsertSearchQuerySuccess;
    private boolean mReturnSubredditAndUserName;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private int fabOption;
    private ActivitySearchResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        binding = ActivitySearchResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        mViewPager2 = binding.viewPagerSearchResultActivity;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSearchResultActivity);            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarSearchResultActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) binding.fabSearchResultActivity.getLayoutParams();
                    params.bottomMargin += navBarHeight;
                    binding.fabSearchResultActivity.setLayoutParams(params);
                }
            }
        }

        setSupportActionBar(binding.toolbarSearchResultActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarSearchResultActivity);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getStringExtra(EXTRA_QUERY);

        mSearchInSubredditOrUserName = intent.getStringExtra(EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME);
        mSearchInMultiReddit = intent.getParcelableExtra(EXTRA_SEARCH_IN_MULTIREDDIT);
        mSearchInThingType = intent.getIntExtra(EXTRA_SEARCH_IN_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);
        mReturnSubredditAndUserName = intent.getBooleanExtra(EXTRA_SHOULD_RETURN_SUBREDDIT_AND_USER_NAME, false);

        if (query != null) {
            mQuery = query;
            setTitle(query);
        }

        fragmentManager = getSupportFragmentManager();

        if (savedInstanceState != null) {
            mInsertSearchQuerySuccess = savedInstanceState.getBoolean(INSERT_SEARCH_QUERY_SUCCESS_STATE);
        }
        bindView(savedInstanceState);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (sectionsPagerAdapter != null) {
            return sectionsPagerAdapter.handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSearchResultActivity,
                binding.collapsingToolbarLayoutSearchResultActivity, binding.toolbarSearchResultActivity);
        applyTabLayoutTheme(binding.tabLayoutSearchResultActivity);
        applyFABTheme(binding.fabSearchResultActivity);
    }

    private void bindView(Bundle savedInstanceState) {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        binding.viewPagerSearchResultActivity.setAdapter(sectionsPagerAdapter);
        binding.viewPagerSearchResultActivity.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        binding.viewPagerSearchResultActivity.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                binding.fabSearchResultActivity.show();
                sectionsPagerAdapter.displaySortTypeInToolbar();
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }
            }
        });
        new TabLayoutMediator(binding.tabLayoutSearchResultActivity, binding.viewPagerSearchResultActivity, (tab, position) -> {
            if (mReturnSubredditAndUserName) {
                if (position == 0) {
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.subreddits));
                } else {
                    Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.users));
                }
            } else {
                switch (position) {
                    case 0:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.posts));
                        break;
                    case 1:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.subreddits));
                        break;
                    case 2:
                        Utils.setTitleWithCustomFontToTab(typeface, tab, getString(R.string.users));
                        break;
                }
            }
        }).attach();
        fixViewPager2Sensitivity(binding.viewPagerSearchResultActivity);

        if (savedInstanceState == null) {
            binding.viewPagerSearchResultActivity.setCurrentItem(Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.DEFAULT_SEARCH_RESULT_TAB, "0")), false);
        }

        fabOption = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_refresh_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_post_layout_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_search_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_subreddit_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_user_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_random_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    binding.fabSearchResultActivity.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    binding.fabSearchResultActivity.setImageResource(R.drawable.ic_hide_read_posts_day_night_24dp);
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_filter_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                binding.fabSearchResultActivity.setImageResource(R.drawable.ic_keyboard_double_arrow_up_day_night_24dp);
                break;
            default:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    binding.fabSearchResultActivity.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    binding.fabSearchResultActivity.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }

        setOtherActivitiesFabContentDescription(binding.fabSearchResultActivity, fabOption);

        binding.fabSearchResultActivity.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {
                    Fragment fragment = sectionsPagerAdapter.getCurrentFragment();
                    if (fragment instanceof PostFragment) {
                        SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment = SearchPostSortTypeBottomSheetFragment.getNewInstance(((PostFragment) fragment).getSortType());
                        searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                    }
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                    postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH: {
                    Intent intent = new Intent(this, SearchActivity.class);
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME, mSearchInSubredditOrUserName);
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_MULTIREDDIT, mSearchInMultiReddit);
                    intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_THING_TYPE, mSearchInThingType);
                    startActivity(intent);
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                    goToSubreddit();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                    goToUser();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                    random();
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.hideReadPosts();
                    }
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.filterPosts();
                    }
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.goBackToTop();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        binding.fabSearchResultActivity.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment = new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, accountName.equals(Account.ANONYMOUS_ACCOUNT));
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });

        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT) && mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SEARCH_HISTORY, true) && !mInsertSearchQuerySuccess && mQuery != null) {
            InsertRecentSearchQuery.insertRecentSearchQueryListener(executor, new Handler(getMainLooper()),
                    mRedditDataRoomDatabase, accountName, mQuery, mSearchInSubredditOrUserName, mSearchInMultiReddit,
                    mSearchInThingType, () -> mInsertSearchQuerySuccess = true);
        }
    }

    private void displaySortTypeBottomSheetFragment() {
        Fragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment instanceof PostFragment) {
            SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment = SearchPostSortTypeBottomSheetFragment.getNewInstance(((PostFragment) fragment).getSortType());
            searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
        } else {
            if (fragment instanceof SubredditListingFragment) {
                SearchUserAndSubredditSortTypeBottomSheetFragment searchUserAndSubredditSortTypeBottomSheetFragment
                        = SearchUserAndSubredditSortTypeBottomSheetFragment.getNewInstance(binding.viewPagerSearchResultActivity.getCurrentItem(), ((SubredditListingFragment) fragment).getSortType());
                searchUserAndSubredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchUserAndSubredditSortTypeBottomSheetFragment.getTag());
            } else if (fragment instanceof UserListingFragment) {
                SearchUserAndSubredditSortTypeBottomSheetFragment searchUserAndSubredditSortTypeBottomSheetFragment
                        = SearchUserAndSubredditSortTypeBottomSheetFragment.getNewInstance(binding.viewPagerSearchResultActivity.getCurrentItem(), ((UserListingFragment) fragment).getSortType());
                searchUserAndSubredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchUserAndSubredditSortTypeBottomSheetFragment.getTag());
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_result_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_sort_search_result_activity) {
            displaySortTypeBottomSheetFragment();
            return true;
        } else if (itemId == R.id.action_search_search_result_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME, mSearchInSubredditOrUserName);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_MULTIREDDIT, mSearchInMultiReddit);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_THING_TYPE, mSearchInThingType);
            intent.putExtra(SearchActivity.EXTRA_QUERY, mQuery);
            finish();
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_refresh_search_result_activity) {
            if (sectionsPagerAdapter != null) {
                sectionsPagerAdapter.refresh();
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_search_result_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSERT_SEARCH_QUERY_SUCCESS_STATE, mInsertSearchQuerySuccess);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changeSortType(sortType);
        }

    }

    @Override
    public void sortTypeSelected(String sortType) {
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void searchUserAndSubredditSortTypeSelected(SortType sortType, int fragmentPosition) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changeSortType(sortType, fragmentPosition);
        }

    }

    @Override
    public void postLayoutSelected(int postLayout) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changePostLayout(postLayout);
        }

    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
        }
    }

    @Override
    public void onLongPress() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (sectionsPagerAdapter != null) {
            sectionsPagerAdapter.displaySortTypeInToolbar();
        }
    }

    @Override
    public void fabOptionSelected(int option) {
        switch (option) {
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SUBMIT_POST:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_REFRESH:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_SORT_TYPE:
                displaySortTypeBottomSheetFragment();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_POST_LAYOUT:
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME, mSearchInSubredditOrUserName);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_MULTIREDDIT, mSearchInMultiReddit);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_THING_TYPE, mSearchInThingType);
                startActivity(intent);
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_SUBREDDIT: {
                goToSubreddit();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_GO_TO_USER: {
                goToUser();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_RANDOM: {
                random();
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_HIDE_READ_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_FILTER_POSTS: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
        }
    }

    private void goToSubreddit() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, binding.getRoot(), false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        thingEditText.requestFocus();
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            Utils.hideKeyboard(this);
            Intent intent = new Intent(SearchResultActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
            startActivity(intent);
        });
        recyclerView.setAdapter(adapter);
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                startActivity(subredditIntent);
                return true;
            }
            return false;
        });

        Handler handler = new Handler();
        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);
        thingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (subredditAutocompleteCall != null && subredditAutocompleteCall.isExecuted()) {
                    subredditAutocompleteCall.cancel();
                }
                if (autoCompleteRunnable != null) {
                    handler.removeCallbacks(autoCompleteRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String currentQuery = editable.toString().trim();
                if (!currentQuery.isEmpty()) {
                    autoCompleteRunnable = () -> {
                        subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(accessToken),
                                currentQuery, nsfw);
                        subredditAutocompleteCall.enqueue(new Callback<String>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                subredditAutocompleteCall = null;
                                if (response.isSuccessful()) {
                                    ParseSubredditData.parseSubredditListingData(executor, handler, response.body(),
                                            nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                                @Override
                                                public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                                    adapter.setSubreddits(subredditData);
                                                }

                                                @Override
                                                public void onParseSubredditListingDataFail() {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                subredditAutocompleteCall = null;
                            }
                        });
                    };

                    handler.postDelayed(autoCompleteRunnable, 500);
                }
            }
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_subreddit)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    Intent subredditIntent = new Intent(this, ViewSubredditDetailActivity.class);
                    subredditIntent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, thingEditText.getText().toString());
                    startActivity(subredditIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void goToUser() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, binding.getRoot(), false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        thingEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), thingEditText);
        thingEditText.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                Utils.hideKeyboard(this);
                Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                startActivity(userIntent);
                return true;
            }
            return false;
        });
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.go_to_user)
                .setView(rootView)
                .setPositiveButton(R.string.ok, (dialogInterface, i)
                        -> {
                    Utils.hideKeyboard(this);
                    Intent userIntent = new Intent(this, ViewUserDetailActivity.class);
                    userIntent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, thingEditText.getText().toString());
                    startActivity(userIntent);
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
                    Utils.hideKeyboard(this);
                })
                .setOnDismissListener(dialogInterface -> {
                    Utils.hideKeyboard(this);
                })
                .show();
    }

    private void random() {
        RandomBottomSheetFragment randomBottomSheetFragment = new RandomBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false));
        randomBottomSheetFragment.setArguments(bundle);
        randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
    }

    @Override
    public void postTypeSelected(int postType) {
        Intent intent;
        switch (postType) {
            case PostTypeBottomSheetFragment.TYPE_TEXT:
                intent = new Intent(this, PostTextActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(this, PostLinkActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(this, PostImageActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(this, PostVideoActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_GALLERY:
                intent = new Intent(this, PostGalleryActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_POLL:
                intent = new Intent(this, PostPollActivity.class);
                startActivity(intent);
        }
    }

    @Override
    public void contentScrollUp() {
        binding.fabSearchResultActivity.show();
    }

    @Override
    public void contentScrollDown() {
        binding.fabSearchResultActivity.hide();
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        public SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (mReturnSubredditAndUserName) {
                if (position == 0) {
                    return createSubredditListingFragment(true);
                }
                return createUserListingFragment(true);
            } else {
                switch (position) {
                    case 0: {
                        return createPostFragment();
                    }
                    case 1: {
                        return createSubredditListingFragment(false);
                    }
                    default: {
                        return createUserListingFragment(false);
                    }
                }
            }
        }

        private Fragment createPostFragment() {
            PostFragment mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            switch (mSearchInThingType) {
                case SelectThingReturnKey.THING_TYPE.SUBREDDIT:
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SEARCH);
                    bundle.putString(PostFragment.EXTRA_NAME, mSearchInSubredditOrUserName);
                    break;
                case SelectThingReturnKey.THING_TYPE.USER:
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_SEARCH);
                    bundle.putString(PostFragment.EXTRA_NAME, "u_" + mSearchInSubredditOrUserName);
                    break;
                case SelectThingReturnKey.THING_TYPE.MULTIREDDIT:
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_MULTI_REDDIT);
                    bundle.putString(PostFragment.EXTRA_NAME, mSearchInMultiReddit.getPath());
            }
            bundle.putString(PostFragment.EXTRA_QUERY, mQuery);
            bundle.putString(PostFragment.EXTRA_TRENDING_SOURCE, getIntent().getStringExtra(EXTRA_TRENDING_SOURCE));
            mFragment.setArguments(bundle);
            return mFragment;
        }

        private Fragment createSubredditListingFragment(boolean returnSubredditName) {
            SubredditListingFragment mFragment = new SubredditListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SubredditListingFragment.EXTRA_QUERY, mQuery);
            bundle.putBoolean(SubredditListingFragment.EXTRA_IS_GETTING_SUBREDDIT_INFO, returnSubredditName);
            mFragment.setArguments(bundle);
            return mFragment;
        }

        private Fragment createUserListingFragment(boolean returnUsername) {
            UserListingFragment mFragment = new UserListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(UserListingFragment.EXTRA_QUERY, mQuery);
            bundle.putBoolean(UserListingFragment.EXTRA_IS_GETTING_USER_INFO, returnUsername);
            mFragment.setArguments(bundle);
            return mFragment;
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + binding.viewPagerSearchResultActivity.getCurrentItem());
        }

        public boolean handleKeyDown(int keyCode) {
            if (binding.viewPagerSearchResultActivity.getCurrentItem() == 0) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof PostFragment) {
                    return ((PostFragment) fragment).handleKeyDown(keyCode);
                }
            }

            return false;
        }

        void changeSortType(SortType sortType) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeSortType(sortType);
                displaySortTypeInToolbar();
            }
        }

        void changeSortType(SortType sortType, int fragmentPosition) {
            Fragment fragment = fragmentManager.findFragmentByTag("f" + fragmentPosition);
            if (fragment instanceof SubredditListingFragment) {
                ((SubredditListingFragment) fragment).changeSortType(sortType);
            } else if (fragment instanceof UserListingFragment) {
                ((UserListingFragment) fragment).changeSortType(sortType);
            }
            displaySortTypeInToolbar();
        }

        public void refresh() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof FragmentCommunicator) {
                ((FragmentCommunicator) fragment).refresh();
            }
        }

        void changeNSFW(boolean nsfw) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeNSFW(nsfw);
            }
        }

        void changePostLayout(int postLayout) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changePostLayout(postLayout);
            }
        }

        void goBackToTop() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).goBackToTop();
            } else if (fragment instanceof SubredditListingFragment) {
                ((SubredditListingFragment) fragment).goBackToTop();
            } else if (fragment instanceof UserListingFragment) {
                ((UserListingFragment) fragment).goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                SortType sortType = ((PostFragment) fragment).getSortType();
                Utils.displaySortTypeInToolbar(sortType, binding.toolbarSearchResultActivity);
            } else if (fragment instanceof SubredditListingFragment) {
                SortType sortType = ((SubredditListingFragment) fragment).getSortType();
                Utils.displaySortTypeInToolbar(sortType, binding.toolbarSearchResultActivity);
            } else if (fragment instanceof UserListingFragment) {
                SortType sortType = ((UserListingFragment) fragment).getSortType();
                Utils.displaySortTypeInToolbar(sortType, binding.toolbarSearchResultActivity);
            }
        }

        void filterPosts() {
            Fragment fragment = fragmentManager.findFragmentByTag("f0");
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).filterPosts();
            }
        }

        void hideReadPosts() {
            Fragment fragment = fragmentManager.findFragmentByTag("f0");
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).hideReadPosts();
            }
        }

        @Override
        public int getItemCount() {
            if (mReturnSubredditAndUserName) {
                return 2;
            }
            return 3;
        }
    }

    @Override
    public void lockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.lock();
        }
    }

    @Override
    public void unlockSwipeRightToGoBack() {
        if (mSliderPanel != null) {
            mSliderPanel.unlock();
        }
    }
}
