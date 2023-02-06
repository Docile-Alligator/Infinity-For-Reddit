package ml.docilealligator.infinityforreddit.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.android.material.textfield.TextInputEditText;
import com.r0adkll.slidr.Slidr;
import com.r0adkll.slidr.model.SlidrInterface;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.AppBarStateChangeListener;
import ml.docilealligator.infinityforreddit.DeleteThing;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RecyclerViewContentScrollingInterface;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.asynctasks.AddSubredditOrUserToMultiReddit;
import ml.docilealligator.infinityforreddit.asynctasks.CheckIsFollowingUser;
import ml.docilealligator.infinityforreddit.asynctasks.SwitchAccount;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FABMoreOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.KarmaInfoBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostLayoutBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SortTimeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UserThingSortTypeBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.NavigationWrapper;
import ml.docilealligator.infinityforreddit.events.ChangeNSFWEvent;
import ml.docilealligator.infinityforreddit.events.GoBackToMainPageEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.fragments.CommentsListingFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.readpost.InsertReadPost;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.user.BlockUser;
import ml.docilealligator.infinityforreddit.user.FetchUserData;
import ml.docilealligator.infinityforreddit.user.UserDao;
import ml.docilealligator.infinityforreddit.user.UserData;
import ml.docilealligator.infinityforreddit.user.UserFollowing;
import ml.docilealligator.infinityforreddit.user.UserViewModel;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewUserDetailActivity extends BaseActivity implements SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback, PostLayoutBottomSheetFragment.PostLayoutSelectionCallback,
        ActivityToolbarInterface, FABMoreOptionsBottomSheetFragment.FABOptionSelectionCallback,
        RandomBottomSheetFragment.RandomOptionSelectionCallback, MarkPostAsReadInterface, RecyclerViewContentScrollingInterface {

    public static final String EXTRA_USER_NAME_KEY = "EUNK";
    public static final String EXTRA_MESSAGE_FULLNAME = "ENF";
    public static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
    public static final int GIVE_AWARD_REQUEST_CODE = 200;
    public static final int EDIT_COMMENT_REQUEST_CODE = 300;
    public static final int ADD_TO_MULTIREDDIT_REQUEST_CODE = 400;

    private static final String FETCH_USER_INFO_STATE = "FSIS";
    private static final String MESSAGE_FULLNAME_STATE = "MFS";
    private static final String NEW_ACCOUNT_NAME_STATE = "NANS";

    @BindView(R.id.coordinator_layout_view_user_detail_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.view_pager_view_user_detail_activity)
    ViewPager2 viewPager2;
    @BindView(R.id.appbar_layout_view_user_detail)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_view_user_detail_activity)
    MaterialToolbar toolbar;
    @BindView(R.id.toolbar_linear_layout_view_user_detail_activity)
    LinearLayout linearLayout;
    @BindView(R.id.tab_layout_view_user_detail_activity)
    TabLayout tabLayout;
    @BindView(R.id.collapsing_toolbar_layout_view_user_detail_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.banner_image_view_view_user_detail_activity)
    GifImageView bannerImageView;
    @BindView(R.id.icon_gif_image_view_view_user_detail_activity)
    GifImageView iconGifImageView;
    @BindView(R.id.user_name_text_view_view_user_detail_activity)
    TextView userNameTextView;
    @BindView(R.id.subscribe_user_chip_view_user_detail_activity)
    Chip subscribeUserChip;
    @BindView(R.id.karma_text_view_view_user_detail_activity)
    TextView karmaTextView;
    @BindView(R.id.cakeday_text_view_view_user_detail_activity)
    TextView cakedayTextView;
    @BindView(R.id.description_text_view_view_user_detail_activity)
    TextView descriptionTextView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
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
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    @Named("bottom_app_bar")
    SharedPreferences mBottomAppBarSharedPreference;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    public UserViewModel userViewModel;
    private FragmentManager fragmentManager;
    private SectionsPagerAdapter sectionsPagerAdapter;
    private RequestManager glide;
    private NavigationWrapper navigationWrapper;
    private Call<String> subredditAutocompleteCall;
    private String mAccessToken;
    private String mAccountName;
    private String username;
    private String description;
    private boolean subscriptionReady = false;
    private boolean mFetchUserInfoSuccess = false;
    private int expandedTabTextColor;
    private int expandedTabBackgroundColor;
    private int expandedTabIndicatorColor;
    private int collapsedTabTextColor;
    private int collapsedTabBackgroundColor;
    private int collapsedTabIndicatorColor;
    private int unsubscribedColor;
    private int subscribedColor;
    private int fabOption;
    private boolean showToast = false;
    private boolean hideFab;
    private boolean showBottomAppBar;
    private boolean lockBottomAppBar;
    private String mMessageFullname;
    private String mNewAccountName;
    private SlidrInterface mSlidrInterface;
    //private MaterialAlertDialogBuilder nsfwWarningBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        setTransparentStatusBarAfterToolbarCollapsed();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_view_user_detail);

        ButterKnife.bind(this);

        hideFab = mSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_FAB_IN_POST_FEED, false);
        showBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.BOTTOM_APP_BAR_KEY, false);

        navigationWrapper = new NavigationWrapper(findViewById(R.id.bottom_app_bar_bottom_app_bar), findViewById(R.id.linear_layout_bottom_app_bar),
                findViewById(R.id.option_1_bottom_app_bar), findViewById(R.id.option_2_bottom_app_bar),
                findViewById(R.id.option_3_bottom_app_bar), findViewById(R.id.option_4_bottom_app_bar),
                findViewById(R.id.fab_view_user_detail_activity),
                findViewById(R.id.navigation_rail), showBottomAppBar);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            mSlidrInterface = Slidr.attach(this);
        }

        username = getIntent().getStringExtra(EXTRA_USER_NAME_KEY);

        fragmentManager = getSupportFragmentManager();

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        lockBottomAppBar = mSharedPreferences.getBoolean(SharedPreferencesUtils.LOCK_BOTTOM_APP_BAR, false);

        if (username.equalsIgnoreCase("me")) {
            username = mAccountName;
        }

        if (savedInstanceState == null) {
            mMessageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
            mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
        } else {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            mMessageFullname = savedInstanceState.getString(MESSAGE_FULLNAME_STATE);
            mNewAccountName = savedInstanceState.getString(NEW_ACCOUNT_NAME_STATE);
        }

        checkNewAccountAndInitializeViewPager();

        fetchUserInfo();

        Resources resources = getResources();

        String title = "u/" + username;
        userNameTextView.setText(title);
        toolbar.setTitle(title);

        setSupportActionBar(toolbar);
        setToolbarGoToTop(toolbar);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    if (navigationWrapper.navigationRailView == null) {
                        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) navigationWrapper.floatingActionButton.getLayoutParams();
                        params.bottomMargin += navBarHeight;
                        navigationWrapper.floatingActionButton.setLayoutParams(params);
                    }
                }
                showToast = true;
            }

            View decorView = window.getDecorView();
            if (isChangeStatusBarIconColor()) {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if (state == State.COLLAPSED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarCollapsed());
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            decorView.setSystemUiVisibility(getSystemVisibilityToolbarExpanded());
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            } else {
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    public void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if (state == State.COLLAPSED) {
                            tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                            tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                        } else if (state == State.EXPANDED) {
                            tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                            tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                            tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                        }
                    }
                });
            }
        } else {
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                public void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.EXPANDED) {
                        tabLayout.setTabTextColors(expandedTabTextColor, expandedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(expandedTabIndicatorColor);
                        tabLayout.setBackgroundColor(expandedTabBackgroundColor);
                    } else if (state == State.COLLAPSED) {
                        tabLayout.setTabTextColors(collapsedTabTextColor, collapsedTabTextColor);
                        tabLayout.setSelectedTabIndicatorColor(collapsedTabIndicatorColor);
                        tabLayout.setBackgroundColor(collapsedTabBackgroundColor);
                    }
                }
            });
        }

        glide = Glide.with(this);
        Locale locale = getResources().getConfiguration().locale;

        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(ViewUserDetailActivity.this, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(mCustomThemeWrapper.getLinkColor());
            }
        };
        BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
            urlMenuBottomSheetFragment.show(getSupportFragmentManager(), null);
            return true;
        };
        Markwon markwon = MarkdownUtils.createLinksOnlyMarkwon(this,
                miscPlugin, onLinkLongClickListener);

        descriptionTextView.setOnLongClickListener(view -> {
            if (description != null && !description.equals("") && descriptionTextView.getSelectionStart() == -1 && descriptionTextView.getSelectionEnd() == -1) {
                CopyTextBottomSheetFragment.show(getSupportFragmentManager(), description, null);
            }
            return true;
        });

        userViewModel = new ViewModelProvider(this, new UserViewModel.Factory(getApplication(), mRedditDataRoomDatabase, username))
                .get(UserViewModel.class);
        userViewModel.getUserLiveData().observe(this, userData -> {
            if (userData != null) {
                if (userData.getBanner().equals("")) {
                    bannerImageView.setOnClickListener(null);
                } else {
                    glide.load(userData.getBanner()).into(bannerImageView);
                    bannerImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, userData.getBanner());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, username + "-banner.jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, username);
                        startActivity(intent);
                    });
                }

                if (userData.getIconUrl().equals("")) {
                    glide.load(getDrawable(R.drawable.subreddit_default_icon))
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .into(iconGifImageView);
                    iconGifImageView.setOnClickListener(null);
                } else {
                    glide.load(userData.getIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(216, 0))))
                            .into(iconGifImageView);

                    iconGifImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(this, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, userData.getIconUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, username + "-icon.jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, username);
                        startActivity(intent);
                    });
                }

                if (userData.isCanBeFollowed()) {
                    subscribeUserChip.setVisibility(View.VISIBLE);
                    subscribeUserChip.setOnClickListener(view -> {
                        if (subscriptionReady) {
                            subscriptionReady = false;
                            if (resources.getString(R.string.follow).contentEquals(subscribeUserChip.getText())) {
                                if (mAccessToken == null) {
                                    UserFollowing.anonymousFollowUser(mExecutor, new Handler(), mRetrofit,
                                            username, mRedditDataRoomDatabase, new UserFollowing.UserFollowingListener() {
                                                @Override
                                                public void onUserFollowingSuccess() {
                                                    subscribeUserChip.setText(R.string.unfollow);
                                                    subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                                                    showMessage(R.string.followed, false);
                                                    subscriptionReady = true;
                                                }

                                                @Override
                                                public void onUserFollowingFail() {
                                                    showMessage(R.string.follow_failed, false);
                                                    subscriptionReady = true;
                                                }
                                            });
                                } else {
                                    UserFollowing.followUser(mOauthRetrofit, mRetrofit, mAccessToken,
                                            username, mAccountName, mRedditDataRoomDatabase, new UserFollowing.UserFollowingListener() {
                                                @Override
                                                public void onUserFollowingSuccess() {
                                                    subscribeUserChip.setText(R.string.unfollow);
                                                    subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                                                    showMessage(R.string.followed, false);
                                                    subscriptionReady = true;
                                                }

                                                @Override
                                                public void onUserFollowingFail() {
                                                    showMessage(R.string.follow_failed, false);
                                                    subscriptionReady = true;
                                                }
                                            });
                                }
                            } else {
                                if (mAccessToken == null) {
                                    UserFollowing.anonymousUnfollowUser(mExecutor, new Handler(), username,
                                            mRedditDataRoomDatabase, new UserFollowing.UserFollowingListener() {
                                                @Override
                                                public void onUserFollowingSuccess() {
                                                    subscribeUserChip.setText(R.string.follow);
                                                    subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                                                    showMessage(R.string.unfollowed, false);
                                                    subscriptionReady = true;
                                                }

                                                @Override
                                                public void onUserFollowingFail() {
                                                    //Will not be called
                                                }
                                            });
                                } else {
                                    UserFollowing.unfollowUser(mOauthRetrofit, mRetrofit, mAccessToken,
                                            username, mAccountName, mRedditDataRoomDatabase, new UserFollowing.UserFollowingListener() {
                                                @Override
                                                public void onUserFollowingSuccess() {
                                                    subscribeUserChip.setText(R.string.follow);
                                                    subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                                                    showMessage(R.string.unfollowed, false);
                                                    subscriptionReady = true;
                                                }

                                                @Override
                                                public void onUserFollowingFail() {
                                                    showMessage(R.string.unfollow_failed, false);
                                                    subscriptionReady = true;
                                                }
                                            });
                                }
                            }
                        }
                    });

                    CheckIsFollowingUser.checkIsFollowingUser(mExecutor, new Handler(), mRedditDataRoomDatabase,
                            username, mAccountName, new CheckIsFollowingUser.CheckIsFollowingUserListener() {
                        @Override
                        public void isSubscribed() {
                            subscribeUserChip.setText(R.string.unfollow);
                            subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(subscribedColor));
                            subscriptionReady = true;
                        }

                        @Override
                        public void isNotSubscribed() {
                            subscribeUserChip.setText(R.string.follow);
                            subscribeUserChip.setChipBackgroundColor(ColorStateList.valueOf(unsubscribedColor));
                            subscriptionReady = true;
                        }
                    });
                } else {
                    subscribeUserChip.setVisibility(View.GONE);
                }

                String userFullName = "u/" + userData.getName();
                userNameTextView.setText(userFullName);
                if (!title.equals(userFullName)) {
                    getSupportActionBar().setTitle(userFullName);
                }
                String karma = getString(R.string.karma_info_user_detail, userData.getTotalKarma(), userData.getLinkKarma(), userData.getCommentKarma());
                karmaTextView.setText(karma);
                cakedayTextView.setText(getString(R.string.cakeday_info, new SimpleDateFormat("MMM d, yyyy",
                        locale).format(userData.getCakeday())));

                if (userData.getDescription() == null || userData.getDescription().equals("")) {
                    descriptionTextView.setVisibility(View.GONE);
                } else {
                    descriptionTextView.setVisibility(View.VISIBLE);
                    description = userData.getDescription();
                    markwon.setMarkdown(descriptionTextView, description);
                }

                /*if (userData.isNSFW()) {
                    if (nsfwWarningBuilder == null
                            && !mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false)) {
                        nsfwWarningBuilder = new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                                .setTitle(R.string.warning)
                                .setMessage(R.string.this_user_has_nsfw_content)
                                .setPositiveButton(R.string.leave, (dialogInterface, i)
                                        -> {
                                    finish();
                                })
                                .setNegativeButton(R.string.dismiss, null);
                        nsfwWarningBuilder.show();
                    }
                }*/
            }
        });

        karmaTextView.setOnClickListener(view -> {
            UserData userData = userViewModel.getUserLiveData().getValue();
            if (userData != null) {
                KarmaInfoBottomSheetFragment karmaInfoBottomSheetFragment = KarmaInfoBottomSheetFragment.newInstance(
                        userData.getLinkKarma(), userData.getCommentKarma(), userData.getAwarderKarma(), userData.getAwardeeKarma()
                );
                karmaInfoBottomSheetFragment.show(getSupportFragmentManager(), karmaInfoBottomSheetFragment.getTag());
            }
        });
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
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        appBarLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                appBarLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                collapsingToolbarLayout.setScrimVisibleHeightTrigger(toolbar.getHeight() + tabLayout.getHeight() + getStatusBarHeight() * 2);
            }
        });
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar, false);
        expandedTabTextColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTextColor();
        expandedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabIndicator();
        expandedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithExpandedCollapsingToolbarTabBackground();
        collapsedTabTextColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTextColor();
        collapsedTabIndicatorColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabIndicator();
        collapsedTabBackgroundColor = mCustomThemeWrapper.getTabLayoutWithCollapsedCollapsingToolbarTabBackground();
        linearLayout.setBackgroundColor(expandedTabBackgroundColor);
        unsubscribedColor = mCustomThemeWrapper.getUnsubscribed();
        subscribedColor = mCustomThemeWrapper.getSubscribed();
        userNameTextView.setTextColor(mCustomThemeWrapper.getUsername());
        karmaTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        cakedayTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        navigationWrapper.applyCustomTheme(mCustomThemeWrapper.getBottomAppBarIconColor(), mCustomThemeWrapper.getBottomAppBarBackgroundColor());
        applyFABTheme(navigationWrapper.floatingActionButton);
        descriptionTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        subscribeUserChip.setTextColor(mCustomThemeWrapper.getChipTextColor());
        applyTabLayoutTheme(tabLayout);
        if (typeface != null) {
            userNameTextView.setTypeface(typeface);
            karmaTextView.setTypeface(typeface);
            cakedayTextView.setTypeface(typeface);
            subscribeUserChip.setTypeface(typeface);
            descriptionTextView.setTypeface(typeface);
        }
    }

    private void checkNewAccountAndInitializeViewPager() {
        if (mNewAccountName != null) {
            if (mAccountName == null || !mAccountName.equals(mNewAccountName)) {
                SwitchAccount.switchAccount(mRedditDataRoomDatabase, mCurrentAccountSharedPreferences,
                        mExecutor, new Handler(), mNewAccountName, newAccount -> {
                            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
                            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

                            mNewAccountName = null;
                            if (newAccount != null) {
                                mAccessToken = newAccount.getAccessToken();
                                mAccountName = newAccount.getAccountName();
                            }

                            initializeViewPager();
                        });
            } else {
                initializeViewPager();
            }
        } else {
            initializeViewPager();
        }
    }

    private void initializeViewPager() {
        sectionsPagerAdapter = new SectionsPagerAdapter(this);
        viewPager2.setAdapter(sectionsPagerAdapter);
        viewPager2.setOffscreenPageLimit(2);
        viewPager2.setUserInputEnabled(!mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_SWIPING_BETWEEN_TABS, false));
        new TabLayoutMediator(tabLayout, viewPager2, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.posts);
                    break;
                case 1:
                    tab.setText(R.string.comments);
                    break;
            }
        }).attach();

        viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    unlockSwipeRightToGoBack();
                } else {
                    lockSwipeRightToGoBack();
                }

                if (showBottomAppBar) {
                    navigationWrapper.showNavigation();
                }
                if (!hideFab) {
                    navigationWrapper.showFab();
                }

                sectionsPagerAdapter.displaySortTypeInToolbar();
            }
        });

        fixViewPager2Sensitivity(viewPager2);

        if (mMessageFullname != null) {
            ReadMessage.readMessage(mOauthRetrofit, mAccessToken, mMessageFullname, new ReadMessage.ReadMessageListener() {
                @Override
                public void readSuccess() {
                    mMessageFullname = null;
                }

                @Override
                public void readFailed() {

                }
            });
        }

        navigationWrapper.floatingActionButton.setVisibility(hideFab ? View.GONE : View.VISIBLE);

        if (showBottomAppBar) {
            int optionCount = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_COUNT, 4);
            int option1 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_1, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME);
            int option2 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_2, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS);

            if (optionCount == 2) {
                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1), getBottomAppBarOptionDrawableResource(option2));

                if (navigationWrapper.navigationRailView == null) {
                    navigationWrapper.option2BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option1);
                    });

                    navigationWrapper.option4BottomAppBar.setOnClickListener(view -> {
                        bottomAppBarOptionAction(option2);
                    });
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
                int option3 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_3, mAccessToken == null ? SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS : SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX);
                int option4 = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_4, mAccessToken == null ? SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH : SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_PROFILE);

                navigationWrapper.bindOptionDrawableResource(getBottomAppBarOptionDrawableResource(option1),
                        getBottomAppBarOptionDrawableResource(option2), getBottomAppBarOptionDrawableResource(option3),
                        getBottomAppBarOptionDrawableResource(option4));

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

        fabOption = mBottomAppBarSharedPreference.getInt((mAccessToken == null ? "-" : "") + SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB, SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SUBMIT_POSTS);
        switch (fabOption) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_refresh_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_sort_toolbar_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_post_layout_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_SEARCH:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_search_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_SUBREDDIT:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_subreddit_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_USER:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_user_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_RANDOM:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_random_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_HIDE_READ_POSTS:
                if (mAccessToken == null) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_hide_read_posts_24dp);
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_GO_TO_TOP:
                navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_keyboard_double_arrow_up_24);
                break;
            default:
                if (mAccessToken == null) {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_filter_24dp);
                    fabOption = SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_FILTER_POSTS;
                } else {
                    navigationWrapper.floatingActionButton.setImageResource(R.drawable.ic_add_day_night_24dp);
                }
                break;
        }
        navigationWrapper.floatingActionButton.setOnClickListener(view -> {
            switch (fabOption) {
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_REFRESH: {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.refresh();
                    }
                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_SORT_TYPE: {

                    break;
                }
                case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_FAB_CHANGE_POST_LAYOUT: {
                    PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                    postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
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
        navigationWrapper.floatingActionButton.setOnLongClickListener(view -> {
            FABMoreOptionsBottomSheetFragment fabMoreOptionsBottomSheetFragment = new FABMoreOptionsBottomSheetFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(FABMoreOptionsBottomSheetFragment.EXTRA_ANONYMOUS_MODE, mAccessToken == null);
            fabMoreOptionsBottomSheetFragment.setArguments(bundle);
            fabMoreOptionsBottomSheetFragment.show(getSupportFragmentManager(), fabMoreOptionsBottomSheetFragment.getTag());
            return true;
        });
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
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mAccountName);
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
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.refresh();
                }
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE: {
                displaySortTypeBottomSheetFragment();
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT: {
                PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
                postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
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
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.hideReadPosts();
                }
                break;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.filterPosts();
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
                Intent intent = new Intent(ViewUserDetailActivity.this, AccountSavedThingActivity.class);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GILDED: {
                Intent intent = new Intent(this, AccountPostsActivity.class);
                intent.putExtra(AccountPostsActivity.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_GILDED);
                startActivity(intent);
                break;
            }
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_TOP: {
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.goBackToTop();
                }
                break;
            }
            default:
                PostTypeBottomSheetFragment postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();
                postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag());
                break;
        }
    }

    private int getBottomAppBarOptionDrawableResource(int option) {
        switch (option) {
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HOME:
                return R.drawable.ic_home_black_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBSCRIPTIONS:
                return R.drawable.ic_subscritptions_bottom_app_bar_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_INBOX:
                return R.drawable.ic_inbox_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_MULTIREDDITS:
                return R.drawable.ic_multi_reddit_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SUBMIT_POSTS:
                return R.drawable.ic_add_day_night_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_REFRESH:
                return R.drawable.ic_refresh_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_SORT_TYPE:
                return R.drawable.ic_sort_toolbar_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_CHANGE_POST_LAYOUT:
                return R.drawable.ic_post_layout_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SEARCH:
                return R.drawable.ic_search_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_SUBREDDIT:
                return R.drawable.ic_subreddit_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_USER:
                return R.drawable.ic_user_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_RANDOM:
                return R.drawable.ic_random_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDE_READ_POSTS:
                return R.drawable.ic_hide_read_posts_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_FILTER_POSTS:
                return R.drawable.ic_filter_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_UPVOTED:
                return R.drawable.ic_arrow_upward_black_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_DOWNVOTED:
                return R.drawable.ic_arrow_downward_black_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_HIDDEN:
                return R.drawable.ic_outline_lock_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_SAVED:
                return R.drawable.ic_outline_bookmarks_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GILDED:
                return R.drawable.ic_star_border_24dp;
            case SharedPreferencesUtils.OTHER_ACTIVITIES_BOTTOM_APP_BAR_OPTION_GO_TO_TOP:
                return R.drawable.ic_keyboard_double_arrow_up_24;
            default:
                return R.drawable.ic_account_circle_24dp;
        }
    }

    private void displaySortTypeBottomSheetFragment() {
        Fragment fragment = sectionsPagerAdapter.getCurrentFragment();
        if (fragment instanceof PostFragment) {
            UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment = UserThingSortTypeBottomSheetFragment.getNewInstance(((PostFragment) fragment).getSortType());
            userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
        } else if (fragment instanceof CommentsListingFragment) {
            UserThingSortTypeBottomSheetFragment userThingSortTypeBottomSheetFragment = UserThingSortTypeBottomSheetFragment.getNewInstance(((CommentsListingFragment) fragment).getSortType());
            userThingSortTypeBottomSheetFragment.show(getSupportFragmentManager(), userThingSortTypeBottomSheetFragment.getTag());
        }
    }

    private void fetchUserInfo() {
        if (!mFetchUserInfoSuccess) {
            FetchUserData.fetchUserData(mRetrofit, username, new FetchUserData.FetchUserDataListener() {
                @Override
                public void onFetchUserDataSuccess(UserData userData, int inboxCount) {
                    new ViewUserDetailActivity.InsertUserDataAsyncTask(mRedditDataRoomDatabase.userDao(), userData,
                            () -> mFetchUserInfoSuccess = true).execute();
                }

                @Override
                public void onFetchUserDataFailed() {
                    showMessage(R.string.cannot_fetch_user_info, true);
                    mFetchUserInfoSuccess = false;
                }
            });
        }
    }

    public void deleteComment(String fullName) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete_this_comment)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteThing.delete(mOauthRetrofit, fullName, mAccessToken, new DeleteThing.DeleteThingListener() {
                    @Override
                    public void deleteSuccess() {
                        Toast.makeText(ViewUserDetailActivity.this, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void deleteFailed() {
                        Toast.makeText(ViewUserDetailActivity.this, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_user_detail_activity, menu);
        if (username.equals(mAccountName)) {
            menu.findItem(R.id.action_send_private_message_view_user_detail_activity).setVisible(false);
            menu.findItem(R.id.action_report_view_user_detail_activity).setVisible(false);
            menu.findItem(R.id.action_block_user_view_user_detail_activity).setVisible(false);
        } else {
            menu.findItem(R.id.action_edit_profile_view_user_detail_activity).setVisible(false);
        }
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_sort_view_user_detail_activity) {
            displaySortTypeBottomSheetFragment();
            return true;
        } else if (itemId == R.id.action_search_view_user_detail_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_NAME, username);
            intent.putExtra(SearchActivity.EXTRA_SUBREDDIT_IS_USER, true);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_refresh_view_user_detail_activity) {
            sectionsPagerAdapter.refresh();
            mFetchUserInfoSuccess = false;
            fetchUserInfo();
            return true;
        } else if (itemId == R.id.action_change_post_layout_view_user_detail_activity) {
            PostLayoutBottomSheetFragment postLayoutBottomSheetFragment = new PostLayoutBottomSheetFragment();
            postLayoutBottomSheetFragment.show(getSupportFragmentManager(), postLayoutBottomSheetFragment.getTag());
            return true;
        } else if (itemId == R.id.action_share_view_user_detail_activity) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.reddit.com/user/" + username);
            if (shareIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            } else {
                Toast.makeText(this, R.string.no_app, Toast.LENGTH_SHORT).show();
            }
            return true;
        } else if (itemId == R.id.action_send_private_message_view_user_detail_activity) {
            if (mAccessToken == null) {
                Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }

            Intent pmIntent = new Intent(this, SendPrivateMessageActivity.class);
            pmIntent.putExtra(SendPrivateMessageActivity.EXTRA_RECIPIENT_USERNAME, username);
            startActivity(pmIntent);
            return true;
        } else if (itemId == R.id.action_add_to_multireddit_view_user_detail_activity) {
            if (mAccessToken == null) {
                Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }
            Intent intent = new Intent(this, MultiredditSelectionActivity.class);
            startActivityForResult(intent, ADD_TO_MULTIREDDIT_REQUEST_CODE);
        } else if (itemId == R.id.action_add_to_post_filter_view_user_detail_activity) {
            Intent intent = new Intent(this, PostFilterPreferenceActivity.class);
            intent.putExtra(PostFilterPreferenceActivity.EXTRA_USER_NAME, username);
            startActivity(intent);
            return true;
        } else if (itemId == R.id.action_report_view_user_detail_activity) {
            Intent reportIntent = new Intent(this, LinkResolverActivity.class);
            reportIntent.setData(Uri.parse("https://www.reddithelp.com/en/categories/rules-reporting/account-and-community-restrictions/what-should-i-do-if-i-see-something-i"));
            startActivity(reportIntent);
            return true;
        } else if (itemId == R.id.action_block_user_view_user_detail_activity) {
            if (mAccessToken == null) {
                Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                return true;
            }

            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.block_user)
                    .setMessage(R.string.are_you_sure)
                    .setPositiveButton(R.string.yes, (dialogInterface, i)
                            -> BlockUser.blockUser(mOauthRetrofit, mAccessToken, username, new BlockUser.BlockUserListener() {
                        @Override
                        public void success() {
                            Toast.makeText(ViewUserDetailActivity.this, R.string.block_user_success, Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void failed() {
                            Toast.makeText(ViewUserDetailActivity.this, R.string.block_user_failed, Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .setNegativeButton(R.string.no, null)
                    .show();
            return true;
        } else if (itemId == R.id.action_edit_profile_view_user_detail_activity) {
            startActivity(new Intent(this, EditProfileActivity.class));
            return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GIVE_AWARD_REQUEST_CODE) {
                Toast.makeText(this, R.string.give_award_success, Toast.LENGTH_SHORT).show();
                int position = data.getIntExtra(GiveAwardActivity.EXTRA_RETURN_ITEM_POSITION, 0);
                String newAwardsHTML = data.getStringExtra(GiveAwardActivity.EXTRA_RETURN_NEW_AWARDS);
                if (sectionsPagerAdapter != null) {
                    sectionsPagerAdapter.giveAward(newAwardsHTML, position);
                }
            } else if (requestCode == EDIT_COMMENT_REQUEST_CODE) {
                if (data != null) {
                    if (sectionsPagerAdapter != null) {
                        sectionsPagerAdapter.editComment(
                                data.getStringExtra(EditCommentActivity.EXTRA_EDITED_COMMENT_CONTENT),
                                data.getExtras().getInt(EditCommentActivity.EXTRA_EDITED_COMMENT_POSITION));
                    }
                }
            } else if (requestCode == ADD_TO_MULTIREDDIT_REQUEST_CODE) {
                if (data != null) {
                    MultiReddit multiReddit = data.getParcelableExtra(MultiredditSelectionActivity.EXTRA_RETURN_MULTIREDDIT);
                    if (multiReddit != null) {
                        AddSubredditOrUserToMultiReddit.addSubredditOrUserToMultiReddit(mOauthRetrofit,
                                mAccessToken, multiReddit.getPath(), "u_" + username,
                                new AddSubredditOrUserToMultiReddit.AddSubredditOrUserToMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        Toast.makeText(ViewUserDetailActivity.this,
                                                getString(R.string.add_subreddit_or_user_to_multireddit_success, username, multiReddit.getDisplayName()), Toast.LENGTH_LONG).show();
                                    }

                                    @Override
                                    public void failed(int code) {
                                        Toast.makeText(ViewUserDetailActivity.this,
                                                getString(R.string.add_subreddit_or_user_to_multireddit_failed, username, multiReddit.getDisplayName()), Toast.LENGTH_LONG).show();
                                    }
                                });
                    }

                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putString(MESSAGE_FULLNAME_STATE, mMessageFullname);
        outState.putString(NEW_ACCOUNT_NAME_STATE, mNewAccountName);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private void showMessage(int resId, boolean retry) {
        if (showToast) {
            Toast.makeText(this, resId, Toast.LENGTH_SHORT).show();
        } else {
            if (retry) {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).setAction(R.string.retry,
                        view -> fetchUserInfo()).show();
            } else {
                Snackbar.make(coordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void sortTypeSelected(SortType sortType) {
        sectionsPagerAdapter.changeSortType(sortType);
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
        sectionsPagerAdapter.changePostLayout(postLayout);
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
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
        TextInputEditText thingEditText = rootView.findViewById(R.id.text_input_edit_text_go_to_thing_edit_text);
        RecyclerView recyclerView = rootView.findViewById(R.id.recycler_view_go_to_thing_edit_text);
        thingEditText.requestFocus();
        SubredditAutocompleteRecyclerViewAdapter adapter = new SubredditAutocompleteRecyclerViewAdapter(
                this, mCustomThemeWrapper, subredditData -> {
            Utils.hideKeyboard(this);
            Intent intent = new Intent(ViewUserDetailActivity.this, ViewSubredditDetailActivity.class);
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

        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);
        thingEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (subredditAutocompleteCall != null) {
                    subredditAutocompleteCall.cancel();
                }
                subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                        editable.toString(), nsfw);
                subredditAutocompleteCall.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
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

                    }
                });
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
        View rootView = getLayoutInflater().inflate(R.layout.dialog_go_to_thing_edit_text, coordinatorLayout, false);
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
        bundle.putBoolean(RandomBottomSheetFragment.EXTRA_IS_NSFW, !mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_NSFW_FOREVER, false) && mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false));
        randomBottomSheetFragment.setArguments(bundle);
        randomBottomSheetFragment.show(getSupportFragmentManager(), randomBottomSheetFragment.getTag());
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

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        if (!getClass().getName().equals(event.excludeActivityClassName)) {
            finish();
        }
    }

    @Subscribe
    public void onChangeNSFWEvent(ChangeNSFWEvent changeNSFWEvent) {
        sectionsPagerAdapter.changeNSFW(changeNSFWEvent.nsfw);
    }

    @Subscribe
    public void goBackToMainPageEvent(GoBackToMainPageEvent event) {
        finish();
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
    public void markPostAsRead(Post post) {
        InsertReadPost.insertReadPost(mRedditDataRoomDatabase, mExecutor, mAccountName, post.getId());
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

    private static class InsertUserDataAsyncTask extends AsyncTask<Void, Void, Void> {

        private UserDao userDao;
        private UserData subredditData;
        private InsertUserDataAsyncTaskListener insertUserDataAsyncTaskListener;
        InsertUserDataAsyncTask(UserDao userDao, UserData userData,
                                InsertUserDataAsyncTaskListener insertUserDataAsyncTaskListener) {
            this.userDao = userDao;
            this.subredditData = userData;
            this.insertUserDataAsyncTaskListener = insertUserDataAsyncTaskListener;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            userDao.insert(subredditData);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            insertUserDataAsyncTaskListener.insertSuccess();
        }

        interface InsertUserDataAsyncTaskListener {
            void insertSuccess();
        }
    }

    private class SectionsPagerAdapter extends FragmentStateAdapter {

        SectionsPagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostPagingSource.TYPE_USER);
                bundle.putString(PostFragment.EXTRA_USER_NAME, username);
                bundle.putString(PostFragment.EXTRA_USER_WHERE, PostPagingSource.USER_WHERE_SUBMITTED);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                bundle.putString(PostFragment.EXTRA_ACCOUNT_NAME, mAccountName);
                fragment.setArguments(bundle);
                return fragment;
            }
            CommentsListingFragment fragment = new CommentsListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(CommentsListingFragment.EXTRA_USERNAME, username);
            bundle.putString(CommentsListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
            bundle.putString(CommentsListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
            bundle.putBoolean(CommentsListingFragment.EXTRA_ARE_SAVED_COMMENTS, false);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 2;
        }

        @Nullable
        private Fragment getCurrentFragment() {
            if (viewPager2 == null || fragmentManager == null) {
                return null;
            }
            return fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
        }

        public boolean handleKeyDown(int keyCode) {
            if (viewPager2.getCurrentItem() == 0) {
                Fragment fragment = getCurrentFragment();
                if (fragment instanceof PostFragment) {
                    return ((PostFragment) fragment).handleKeyDown(keyCode);
                }
            }
            return false;
        }

        public void refresh() {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).refresh();
            } else if (fragment instanceof CommentsListingFragment) {
                ((CommentsListingFragment) fragment).refresh();
            }
        }

        public void changeSortType(SortType sortType) {
            Fragment fragment = getCurrentFragment();
            if (fragment instanceof PostFragment) {
                ((PostFragment) fragment).changeSortType(sortType);
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            } else if (fragment instanceof CommentsListingFragment) {
                mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TYPE_USER_COMMENT, sortType.getType().name()).apply();
                if(sortType.getTime() != null) {
                    mSortTypeSharedPreferences.edit().putString(SharedPreferencesUtils.SORT_TIME_USER_COMMENT, sortType.getTime().name()).apply();
                }
                ((CommentsListingFragment) fragment).changeSortType(sortType);
                Utils.displaySortTypeInToolbar(sortType, toolbar);
            }
        }

        public void changeNSFW(boolean nsfw) {
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
            } else if (fragment instanceof CommentsListingFragment) {
                ((CommentsListingFragment) fragment).goBackToTop();
            }
        }

        void displaySortTypeInToolbar() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f" + viewPager2.getCurrentItem());
                if (fragment instanceof PostFragment) {
                    SortType sortType = ((PostFragment) fragment).getSortType();
                    Utils.displaySortTypeInToolbar(sortType, toolbar);
                } else if (fragment instanceof CommentsListingFragment) {
                    SortType sortType = ((CommentsListingFragment) fragment).getSortType();
                    Utils.displaySortTypeInToolbar(sortType, toolbar);
                }
            }
        }

        void giveAward(String awardsHTML, int position) {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f1");
                if (fragment instanceof CommentsListingFragment) {
                    ((CommentsListingFragment) fragment).giveAward(awardsHTML, position);
                }
            }
        }

        void editComment(String commentMarkdown, int position) {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f1");
                if (fragment instanceof CommentsListingFragment) {
                    ((CommentsListingFragment) fragment).editComment(commentMarkdown, position);
                }
            }
        }

        void hideReadPosts() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f0");
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).hideReadPosts();
                }
            }
        }

        void filterPosts() {
            if (fragmentManager != null) {
                Fragment fragment = fragmentManager.findFragmentByTag("f0");
                if (fragment instanceof PostFragment) {
                    ((PostFragment) fragment).filterPosts();
                }
            }
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
