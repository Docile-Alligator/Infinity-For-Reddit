package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.badge.ExperimentalBadgeUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.DeleteMultiredditInDatabase;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.NavigationWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityViewMultiRedditDetailBinding;
import ml.docilealligator.infinityforreddit.events.ChangeInboxCountEvent;
import ml.docilealligator.infinityforreddit.events.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.events.RefreshMultiRedditsEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragmentBase;
import ml.docilealligator.infinityforreddit.multireddit.DeleteMultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.post.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.readpost.ReadPostsUtils;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewMultiRedditDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostLayoutBottomSheetFragment.PostLayoutSelectionCallback, ActivityToolbarInterface, MarkPostAsReadInterface,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RandomBottomSheetFragment.RandomOptionSelectionCallback, RecyclerViewContentScrollingInterface {

    public static final String EXTRA_MULTIREDDIT_DATA = "EMD";
    public static final String EXTRA_MULTIREDDIT_PATH = "EMP";

    private static final String FRAGMENT_OUT_STATE_KEY = "FOSK";

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
    @Named("post_history")
    SharedPreferences mPostHistorySharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences bottomAppBarSharedPreference;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences mBottomAppBarSharedPreference;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private MultiReddit multiReddit;
    private String multiPath;
    private Fragment mFragment;
    private int fabOption;
    private boolean hideFab;
    private boolean showBottomAppBar;
    private boolean lockBottomAppBar;
    private Runnable autoCompleteRunnable;
    private Call<String> subredditAutocompleteCall;
    private NavigationWrapper navigationWrapper;
    private ActivityViewMultiRedditDetailBinding binding;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mFragment instanceof PostFragment)
            return ((PostFragment) mFragment).handleKeyDown(keyCode) || super.onKeyDown(keyCode, event);
        return super.onKeyDown(keyCode, event);
    }

    @ExperimentalBadgeUtils
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        binding = ActivityViewMultiRedditDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        hideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAB_IN_POST_FEED, false);
        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);

        navigationWrapper = new NavigationWrapper(findViewById(R.id.bottom_app_bar_bottom_app_bar), findViewById(R.id.linear_layout_bottom_app_bar),
                findViewById(R.id.option_1_bottom_app_bar), findViewById(R.id.option_2_bottom_app_bar),
                findViewById(R.id.option_3_bottom_app_bar), findViewById(R.id.option_4_bottom_app_bar),
                findViewById(R.id.fab_view_multi_reddit_detail_activity),
                findViewById(R.id.navigation_rail), customThemeWrapper, showBottomAppBar);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSliderPanel = Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutViewMultiRedditDetailActivity);
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

                        if (navigationWrapper.navigationRailView == null) {
                            if (navigationWrapper.bottomAppBar.getVisibility() != View.VISIBLE) {
                                setMargins(navigationWrapper.floatingActionButton,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN,
                                        (int) Utils.convertDpToPixel(16, ViewMultiRedditDetailActivity.this) + allInsets.right,
                                        (int) Utils.convertDpToPixel(16, ViewMultiRedditDetailActivity.this) + allInsets.bottom);
                            } else {
                                setMargins(navigationWrapper.floatingActionButton,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN,
                                        allInsets.bottom);
                            }
                        } else {
                            if (navigationWrapper.navigationRailView.getVisibility() != View.VISIBLE) {
                                setMargins(navigationWrapper.floatingActionButton,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN,
                                        (int) Utils.convertDpToPixel(16, ViewMultiRedditDetailActivity.this) + allInsets.right,
                                        (int) Utils.convertDpToPixel(16, ViewMultiRedditDetailActivity.this) + allInsets.bottom);

                                binding.frameLayoutViewMultiRedditDetailActivity.setPadding(allInsets.left, 0, allInsets.right, 0);
                            } else {
                                navigationWrapper.navigationRailView.setFitsSystemWindows(false);
                                navigationWrapper.navigationRailView.setPadding(0, 0, 0, allInsets.bottom);

                                setMargins(navigationWrapper.navigationRailView,
                                        allInsets.left,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN,
                                        BaseActivity.IGNORE_MARGIN
                                );

                                binding.frameLayoutViewMultiRedditDetailActivity.setPadding(0, 0, allInsets.right, 0);
                            }
                        }

                        if (navigationWrapper.bottomAppBar != null) {
                            navigationWrapper.linearLayoutBottomAppBar.setPadding(
                                    navigationWrapper.linearLayoutBottomAppBar.getPaddingLeft(),
                                    navigationWrapper.linearLayoutBottomAppBar.getPaddingTop(),
                                    navigationWrapper.linearLayoutBottomAppBar.getPaddingRight(),
                                    allInsets.bottom
                            );
                        }

                        setMargins(binding.toolbarViewMultiRedditDetailActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        return insets;
                    }
                });

                /*adjustToolbar(binding.toolbarViewMultiRedditDetailActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    if (navigationWrapper.navigationRailView == null) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
                        params.bottomMargin += navBarHeight;
                        navigationWrapper.floatingActionButton.setLayoutParams(params);
                    }
                }*/
            }
        }

        multiReddit = getIntent().getParcelableExtra(EXTRA_MULTIREDDIT_DATA);
        if (multiReddit == null) {
            multiPath = getIntent().getStringExtra(EXTRA_MULTIREDDIT_PATH);
            if (multiPath != null) {
                binding.toolbarViewMultiRedditDetailActivity.setTitle(multiPath.substring(multiPath.lastIndexOf("/", multiPath.length() - 2) + 1));
            } else {
                Toast.makeText(this, R.string.error_getting_multi_reddit_data, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
        } else {
            multiPath = multiReddit.getPath();
            binding.toolbarViewMultiRedditDetailActivity.setTitle(multiReddit.getDisplayName());
        }

        setSupportActionBar(binding.toolbarViewMultiRedditDetailActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(binding.toolbarViewMultiRedditDetailActivity);

        lockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);

        if (savedInstanceState != null) {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE_KEY);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();
        } else {
            initializeFragment();
        }

        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);

        if (showBottomAppBar) {
            int optionCount = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
            int option1 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME);
            int option2 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);

            if (optionCount == 2) {
                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1), getBottomAppBarOptionDrawableResource(option2));
                navigationWrapper.bindOptions(option1, option2);

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option2BottomAppBar, option1);
                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option4BottomAppBar, option2);
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        }
                        return false;
                    });
                }
            } else {
                int option3 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS : SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX);
                int option4 = mBottomAppBarSharedPreference.getInt((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? Account.ANONYMOUS_ACCOUNT : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH : SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE);

                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1),
                        getBottomAppBarOptionDrawableResource(option2), getBottomAppBarOptionDrawableResource(option3),
                        getBottomAppBarOptionDrawableResource(option4));
                navigationWrapper.bindOptions(option1, option2, option3, option4);

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option1BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });

                    navigationWrapper.option3BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option3);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option4);
                    });

                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option1BottomAppBar, option1);
                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option2BottomAppBar, option2);
                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option3BottomAppBar, option3);
                    navigationWrapper.setOtherActivitiesContentDescription(this, navigationWrapper.option4BottomAppBar, option4);
                } else {
                    navigationWrapper.navigationRailView.setOnItemSelectedListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.navigation_rail_option_1) {
                            bottomAppBarOptionAction(option1);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_2) {
                            bottomAppBarOptionAction(option2);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_3) {
                            bottomAppBarOptionAction(option3);
                            return true;
                        } else if (itemId == R.id.navigation_rail_option_4) {
                            bottomAppBarOptionAction(option4);
                            return true;
                        }
                        return false;
                    });
                }
            }
        } else {
            CoordinatorLayout.LayoutParams lp = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
            lp.setAnchorId(View.NO_ID);
            lp.gravity = Gravity.END | Gravity.BOTTOM;
            navigationWrapper.floatingActionButton.setLayoutParams(lp);
        }

        fabOption = bottomAppBarSharedPreference.getInt(SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_refresh_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_post_layout_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_search_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_subreddit_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_user_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_random_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_hide_read_posts_day_night_24dp);
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_keyboard_double_arrow_up_day_night_24dp);
                break;
            default:
                if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_day_night_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }

        setOtherActivitiesFabContentDescription(navigationWrapper.floatingActionButton, fabOption);

        navigationWrapper.floatingActionButton.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (mFragment instanceof PostFragment) {
                        ((PostFragment) mFragment).refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {
                    showSortTypeBottomSheetFragment();
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    showPostLayoutBottomSheetFragment();
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH: {
                    Intent intent = new Intent(this, SearchActivity.class);
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
                    if (mFragment instanceof PostFragment) {
                        ((PostFragment) mFragment).hideReadPosts();
                    }
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                    if (mFragment instanceof PostFragment) {
                        ((PostFragment) mFragment).filterPosts();
                    }
                    break;
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                    if (mFragment instanceof PostFragment) {
                        ((PostFragment) mFragment).goBackToTop();
                    }
                    break;
                default:
                    PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                    postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                    break;
            }
        });
        navigationWrapper.floatingActionButton.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment = new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, accountName.equals(Account.ANONYMOUS_ACCOUNT));
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });

        navigationWrapper.bottomAppBar.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                navigationWrapper.bottomAppBar.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                setInboxCount(mCurrentAccountSharedPreferences.getInt(SharedPreferencesUtils.INBOX_COUNT, 0));
            }
        });
    }

    private void initializeFragment() {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PostFragment.EXTRA_NAME, multiPath);
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, accountName.equals(Account.ANONYMOUS_ACCOUNT) ? PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT : PostPagingSource.TYPE_MULTI_REDDIT);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_view_multi_reddit_detail_activity, mFragment).commit();
    }

    private void bottomAppBarOptionAction(int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME: {
                EventBus.getDefault().post(new GoBackToMainPageEvent());
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX: {
                Intent intent = new Intent(this, InboxActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE: {
                Intent intent = new Intent(this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, accountName);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS: {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_SHOW_MULTIREDDITS, true);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH: {
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).refresh();
                }
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE: {
                showSortTypeBottomSheetFragment();
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT: {
                showPostLayoutBottomSheetFragment();
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SEARCH: {
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                goToSubreddit();
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                goToUser();
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_RANDOM:
                random();
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).hideReadPosts();
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).filterPosts();
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_UPVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_UPVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_DOWNVOTED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_DOWNVOTED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDDEN: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_HIDDEN);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SAVED: {
                Intent intent = new Intent(ViewMultiRedditDetailActivity.this, AccountSavedThingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
            default: {
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).goBackToTop();
                }
                break;
            }
        }
    }

    private int getBottomAppBarOptionDrawableResource(int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME:
                return R.drawable.ic_home_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                return R.drawable.ic_subscriptions_bottom_app_bar_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE:
                return R.drawable.ic_account_circle_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                return R.drawable.ic_add_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH:
                return R.drawable.ic_refresh_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                return R.drawable.ic_sort_toolbar_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                return R.drawable.ic_post_layout_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SEARCH:
                return R.drawable.ic_search_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                return R.drawable.ic_subreddit_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                return R.drawable.ic_user_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_RANDOM:
                return R.drawable.ic_random_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                return R.drawable.ic_hide_read_posts_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                return R.drawable.ic_filter_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_UPVOTED:
                return R.drawable.ic_arrow_upward_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                return R.drawable.ic_arrow_downward_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDDEN:
                return R.drawable.ic_lock_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SAVED:
                return R.drawable.ic_bookmarks_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
            default:
                return R.drawable.ic_keyboard_double_arrow_up_day_night_24dp;
        }
    }

    private void showSortTypeBottomSheetFragment() {
        if (mFragment instanceof PostFragment) {
            SortTypeBottomSheetFragment sortTypeBottomSheetFragment = SortTypeBottomSheetFragment.getNewInstance(true, ((PostFragment) mFragment).getSortType());
            sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
        }
    }

    private void showPostLayoutBottomSheetFragment() {
        PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
        postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
    }

    private void goToSubreddit() {
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, binding.getRoot(), false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        thingEditText.requestFocus();
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            Utils.hideKeyboard(this);
            Intent intent = new Intent(this, ViewSubredditDetailActivity.class);
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
                                    ParseSubredditData.parseSubredditListingData(mExecutor, handler, response.body(),
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_multi_reddit_detail_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_view_multi_reddit_detail_activity) {
            showSortTypeBottomSheetFragment();
            return true;
        } else if (itemId == R.id.action_search_view_multi_reddit_detail_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            if (multiReddit == null) {
                intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_MULTIREDDIT, MultiReddit.getDummyMultiReddit(multiPath));
            } else {
                intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_MULTIREDDIT, multiReddit);
            }
            intent.putExtra(SearchActivity.EXTRA_SEARCH_IN_THING_TYPE, SelectThingReturnKey.THING_TYPE.MULTIREDDIT);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_refresh_view_multi_reddit_detail_activity) {
            if (mFragment instanceof FragmentCommunicator) {
                ((FragmentCommunicator) mFragment).refresh();
            }
            return true;
        } else if (itemId == R.id.action_change_post_layout_view_multi_reddit_detail_activity) {
            showPostLayoutBottomSheetFragment();
            return true;
        } else if (itemId == R.id.action_edit_view_multi_reddit_detail_activity) {
            Intent editIntent = new Intent(this, EditMultiRedditActivity.class);
            editIntent.putExtra(EditMultiRedditActivity.EXTRA_MULTI_PATH, multiPath);
            startActivity(editIntent);
            return true;
        } else if (itemId == R.id.action_delete_view_multi_reddit_detail_activity) {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.delete)
                    .setMessage(R.string.delete_multi_reddit_dialog_message)
                    .setPositiveButton(R.string.delete, (dialogInterface, i)
                            -> {
                        if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                            DeleteMultiredditInDatabase.deleteMultiredditInDatabase(mExecutor, new Handler(), mRedditDataRoomDatabase, accountName, multiPath,
                                    () -> {
                                        Toast.makeText(this, R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                        } else {
                            DeleteMultiReddit.deleteMultiReddit(mExecutor, new Handler(), mOauthRetrofit, mRedditDataRoomDatabase,
                                    accessToken, accountName, multiPath, new DeleteMultiReddit.DeleteMultiRedditListener() {
                                        @Override
                                        public void success() {
                                            Toast.makeText(ViewMultiRedditDetailActivity.this,
                                                    R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().post(new RefreshMultiRedditsEvent());
                                            finish();
                                        }

                                        @Override
                                        public void failed() {
                                            Toast.makeText(ViewMultiRedditDetailActivity.this,
                                                    R.string.delete_multi_reddit_failed, Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    })
                    .setNegativeButton(R.string.cancel, null)
                    .show();
            return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE_KEY, mFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        ((PostFragment) mFragment).changeSortType(sortType);
        displaySortType();
    }

    @Override
    public void sortTypeSelected(String sortType) {
        SortTimeBottomSheetFragment sortTimeBottomSheetFragment = new SortTimeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SortTimeBottomSheetFragment.EXTRA_SORT_TYPE, sortType);
        sortTimeBottomSheetFragment.setArguments(bundle);
        sortTimeBottomSheetFragment.show(getSupportFragmentManager(), sortTimeBottomSheetFragment.getTag());
    }

    @Override
    public void postLayoutSelected(int postLayout) {
        if (mFragment != null) {
            mPostLayoutSharedPreferences.edit().putInt(SharedPreferencesUtils.POST_LAYOUT_MULTI_REDDIT_POST_BASE + multiPath, postLayout).apply();
            ((PostFragmentBase) mFragment).changePostLayout(postLayout);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutViewMultiRedditDetailActivity,
                binding.collapsingToolbarLayoutViewMultiRedditDetailActivity, binding.toolbarViewMultiRedditDetailActivity);
        navigationWrapper.applyCustomTheme(mCustomThemeWrapper.getBottomAppBarIconColor(), mCustomThemeWrapper.getBottomAppBarBackgroundColor());
        applyFABTheme(navigationWrapper.floatingActionButton);
    }

    @Override
    public void onLongPress() {
        if (mFragment != null) {
            ((PostFragment) mFragment).goBackToTop();
        }
    }

    @Override
    public void displaySortType() {
        if (mFragment != null) {
            SortType sortType = ((PostFragment) mFragment).getSortType();
            Utils.displaySortTypeInToolbar(sortType, binding.toolbarViewMultiRedditDetailActivity);
        }
    }

    @Override
    public void markPostAsRead(Post post) {
        int readPostsLimit = ReadPostsUtils.GetReadPostsLimit(accountName, mPostHistorySharedPreferences);
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, accountName, post.getId(), readPostsLimit);
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
    public void fabOptionSelected(int option) {
        switch (option) {
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SUBMIT_POST:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_REFRESH:
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).refresh();
                }
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_SORT_TYPE:
                showSortTypeBottomSheetFragment();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_CHANGE_POST_LAYOUT:
                showPostLayoutBottomSheetFragment();
                break;
            case FABMoreOptionsBottomSheetFragment.FAB_OPTION_SEARCH:
                Intent intent = new Intent(this, SearchActivity.class);
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
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).hideReadPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_FILTER_POSTS: {
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).filterPosts();
                }
                break;
            }
            case FABMoreOptionsBottomSheetFragment.FAB_GO_TO_TOP: {
                if (mFragment instanceof PostFragment) {
                    ((PostFragment) mFragment).goBackToTop();
                }
                break;
            }
        }
    }

    @Override
    public void randomOptionSelected(int option) {
        Intent intent = new Intent(this, FetchRandomSubredditOrPostActivity.class);
        intent.putExtra(FetchRandomSubredditOrPostActivity.EXTRA_RANDOM_OPTION, option);
        startActivity(intent);
    }

    @Override
    public void contentScrollUp() {
        if (showBottomAppBar && !lockBottomAppBar) {
            navigationWrapper.showNavigation();
        }
        if (!(showBottomAppBar && lockBottomAppBar) && !hideFab) {
            navigationWrapper.showFab();
        }
    }

    @Override
    public void contentScrollDown() {
        if (!(showBottomAppBar && lockBottomAppBar) && !hideFab) {
            navigationWrapper.hideFab();
        }
        if (showBottomAppBar && !lockBottomAppBar) {
            navigationWrapper.hideNavigation();
        }
    }

    @ExperimentalBadgeUtils
    private void setInboxCount(int inboxCount) {
        mHandler.post(() -> navigationWrapper.setInboxCount(this, inboxCount));
    }

    @Subscribe
    public void goBackToMainPageEvent(GoBackToMainPageEvent event) {
        finish();
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @ExperimentalBadgeUtils
    @Subscribe
    public void onChangeInboxCountEvent(ChangeInboxCountEvent event) {
        setInboxCount(event.inboxCount);
    }
}
