package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditData;
import SubredditDatabase.SubredditRoomDatabase;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditRoomDatabase;
import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import SubscribedUserDatabase.SubscribedUserData;
import SubscribedUserDatabase.SubscribedUserRoomDatabase;
import SubscribedUserDatabase.SubscribedUserViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String FRAGMENT_OUT_STATE = "FOS";
    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 0;

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.collapsing_toolbar_layout_main_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.search_view_main_activity) SimpleSearchView simpleSearchView;
    @BindView(R.id.transparent_overlay_main_activity) View transparentOverlay;
    @BindView(R.id.subscribed_subreddit_recycler_view_main_activity) RecyclerView subscribedSubredditRecyclerView;
    @BindView(R.id.subscriptions_label_main_activity) TextView subscriptionsLabelTextView;
    @BindView(R.id.subscribed_user_recycler_view_main_activity) RecyclerView subscribedUserRecyclerView;
    @BindView(R.id.following_label_main_activity) TextView followingLabelTextView;
    @BindView(R.id.profile_linear_layout_main_activity) LinearLayout profileLinearLayout;

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private GifImageView mProfileImageView;
    private ImageView mBannerImageView;

    private Fragment mFragment;
    private RequestManager glide;
    private AppBarLayout.LayoutParams params;

    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess = false;
    private boolean mInsertSuccess = false;

    private Menu mMenu;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;
    private SubscribedUserViewModel mSubscribedUserViewModel;

    private boolean isInLazyMode = false;

    @Inject
    @Named("user_info")
    SharedPreferences mUserInfoSharedPreferences;

    @Inject
    @Named("auth_info")
    SharedPreferences mAuthInfoSharedPreferences;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        transparentOverlay.setOnClickListener(view -> simpleSearchView.onBackPressed());

        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                intent.putExtra(SearchActivity.QUERY_KEY, query);
                startActivity(intent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

            @Override
            public boolean onQueryTextCleared() {
                return false;
            }
        });

        simpleSearchView.setOnSearchViewListener(new SimpleSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                transparentOverlay.setVisibility(View.VISIBLE);
            }

            @Override
            public void onSearchViewClosed() {
                transparentOverlay.setVisibility(View.GONE);
            }

            @Override
            public void onSearchViewShownAnimation() {

            }

            @Override
            public void onSearchViewClosedAnimation() {

            }
        });

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        if (accessToken.equals("")) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST_CODE);
        } else {
            if (savedInstanceState == null) {
                mFragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.POST_TYPE_KEY, PostDataSource.TYPE_FRONT_PAGE);
                mFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();

                mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
                mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
                isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            }

            glide = Glide.with(this);

            View header = findViewById(R.id.nav_header_main_activity);
            mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
            mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
            mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
            mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

            loadUserData();

            subscribedSubredditRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            subscribedSubredditRecyclerView.setNestedScrollingEnabled(false);

            subscribedUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            subscribedUserRecyclerView.setNestedScrollingEnabled(false);

            mName = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.USER_KEY, "");
            mProfileImageUrl = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, "");
            mBannerImageUrl = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, "");
            mKarma = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.KARMA_KEY, "");

            mNameTextView.setText(mName);
            mKarmaTextView.setText(mKarma);

            if (!mProfileImageUrl.equals("")) {
                glide.load(mProfileImageUrl)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                        .error(glide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0))))
                        .into(mProfileImageView);
            } else {
                glide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(144, 0)))
                        .into(mProfileImageView);
            }

            if (!mBannerImageUrl.equals("")) {
                glide.load(mBannerImageUrl).into(mBannerImageView);
            }

            profileLinearLayout.setOnClickListener(view -> {
                Intent intent = new Intent(MainActivity.this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mName);
                startActivity(intent);
            });

            final SubscribedSubredditRecyclerViewAdapter subredditadapter =
                    new SubscribedSubredditRecyclerViewAdapter(this, drawer::closeDrawers);
            subscribedSubredditRecyclerView.setAdapter(subredditadapter);

            mSubscribedSubredditViewModel = ViewModelProviders.of(this).get(SubscribedSubredditViewModel.class);
            mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
                if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                    subscriptionsLabelTextView.setVisibility(View.GONE);
                } else {
                    subscriptionsLabelTextView.setVisibility(View.VISIBLE);
                }

                subredditadapter.setSubscribedSubreddits(subscribedSubredditData);
            });

            final SubscribedUserRecyclerViewAdapter userAdapter =
                    new SubscribedUserRecyclerViewAdapter(this, drawer::closeDrawers);
            subscribedUserRecyclerView.setAdapter(userAdapter);
            mSubscribedUserViewModel = ViewModelProviders.of(this).get(SubscribedUserViewModel.class);
            mSubscribedUserViewModel.getAllSubscribedUsers().observe(this, subscribedUserData -> {
                if (subscribedUserData == null || subscribedUserData.size() == 0) {
                    followingLabelTextView.setVisibility(View.GONE);
                } else {
                    followingLabelTextView.setVisibility(View.VISIBLE);
                }
                userAdapter.setSubscribedUsers(subscribedUserData);
            });
        }
    }

    private void loadUserData() {
        if (!mFetchUserInfoSuccess) {
            FetchMyInfo.fetchMyInfo(mOauthRetrofit, mAuthInfoSharedPreferences, new FetchMyInfo.FetchUserMyListener() {
                @Override
                public void onFetchMyInfoSuccess(String response) {
                    ParseMyInfo.parseMyInfo(response, new ParseMyInfo.ParseMyInfoListener() {
                        @Override
                        public void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                            mNameTextView.setText(name);
                            if (!profileImageUrl.equals("")) {
                                glide.load(profileImageUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                                        .error(glide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0))))
                                        .into(mProfileImageView);
                            } else {
                                glide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                                        .into(mProfileImageView);
                            }
                            if (!bannerImageUrl.equals("")) {
                                glide.load(bannerImageUrl).into(mBannerImageView);
                            }

                            mName = name;
                            mProfileImageUrl = profileImageUrl;
                            mBannerImageUrl = bannerImageUrl;
                            mKarma = getString(R.string.karma_info, karma);

                            mKarmaTextView.setText(mKarma);

                            SharedPreferences.Editor editor = mUserInfoSharedPreferences.edit();
                            editor.putString(SharedPreferencesUtils.USER_KEY, name);
                            editor.putString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, profileImageUrl);
                            editor.putString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, bannerImageUrl);
                            editor.putString(SharedPreferencesUtils.KARMA_KEY, mKarma);
                            editor.apply();
                            mFetchUserInfoSuccess = true;
                        }

                        @Override
                        public void onParseMyInfoFail() {
                            mFetchUserInfoSuccess = false;
                        }
                    });
                }

                @Override
                public void onFetchMyInfoFail() {
                    mFetchUserInfoSuccess = false;
                }
            });
        }

        if (!mInsertSuccess) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, mAuthInfoSharedPreferences, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            new InsertSubscribedThingsAsyncTask(
                                    SubscribedSubredditRoomDatabase.getDatabase(MainActivity.this).subscribedSubredditDao(),
                                    SubscribedUserRoomDatabase.getDatabase(MainActivity.this).subscribedUserDao(),
                                    SubredditRoomDatabase.getDatabase(MainActivity.this).subredditDao(),
                                    subscribedSubredditData,
                                    subscribedUserData,
                                    subredditData,
                                    () -> mInsertSuccess = true).execute();
                        }

                        @Override
                        public void onFetchSubscribedThingFail() {
                            mInsertSuccess = false;
                        }
                    });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (simpleSearchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }
        if(requestCode == LOGIN_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
            overridePendingTransition(0, 0);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        mMenu = menu;
        simpleSearchView.setMenuItem(mMenu.findItem(R.id.action_search_main_activity));
        MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);
        if(isInLazyMode) {
            lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
            collapsingToolbarLayout.setLayoutParams(params);
        } else {
            lazyModeItem.setTitle(R.string.action_start_lazy_mode);
            params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
            collapsingToolbarLayout.setLayoutParams(params);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mFragment instanceof FragmentCommunicator) {
            switch (item.getItemId()) {
                case R.id.action_refresh_main_activity:
                    /*((FragmentCommunicator) mFragment).refresh();
                    mFetchUserInfoSuccess = false;
                    mInsertSuccess = false;
                    loadUserData();*/
                    Intent intent = new Intent(this, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_DATA, "asdfasdfas");
                    startActivity(intent);
                    return true;
                case R.id.action_lazy_mode_main_activity:
                    MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);
                    if(isInLazyMode) {
                        isInLazyMode = false;
                        ((FragmentCommunicator) mFragment).stopLazyMode();
                        lazyModeItem.setTitle(R.string.action_start_lazy_mode);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_ENTER_ALWAYS);
                        collapsingToolbarLayout.setLayoutParams(params);
                    } else {
                        isInLazyMode = true;
                        ((FragmentCommunicator) mFragment).startLazyMode();
                        lazyModeItem.setTitle(R.string.action_stop_lazy_mode);
                        params.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL);
                        collapsingToolbarLayout.setLayoutParams(params);
                    }
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if (simpleSearchView.onBackPressed()) {
                return;
            }

            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }

        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
    }
}
