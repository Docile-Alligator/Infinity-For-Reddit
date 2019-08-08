package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback,
        PostTypeBottomSheetFragment.PostTypeSelectionCallback {

    static final String EXTRA_POST_TYPE = "EPT";

    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 0;

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.view_pager_main_activity) ViewPager viewPager;
    @BindView(R.id.collapsing_toolbar_layout_main_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.profile_linear_layout_main_activity) LinearLayout profileLinearLayout;
    @BindView(R.id.subscriptions_linear_layout_main_activity) LinearLayout subscriptionLinearLayout;
    @BindView(R.id.settings_linear_layout_main_activity) LinearLayout settingsLinearLayout;
    @BindView(R.id.tab_layout_main_activity) TabLayout tabLayout;
    @BindView(R.id.fab_main_activity) FloatingActionButton fab;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private GifImageView mProfileImageView;
    private ImageView mBannerImageView;

    private RequestManager glide;
    private AppBarLayout.LayoutParams params;
    private PostTypeBottomSheetFragment postTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment bestSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment popularAndAllSortTypeBottomSheetFragment;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess = false;

    private Menu mMenu;

    private boolean isInLazyMode = false;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        postTypeBottomSheetFragment = new PostTypeBottomSheetFragment();

        bestSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle bestBundle = new Bundle();
        bestBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
        bestSortTypeBottomSheetFragment.setArguments(bestBundle);

        popularAndAllSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
        Bundle popularBundle = new Bundle();
        popularBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
        popularAndAllSortTypeBottomSheetFragment.setArguments(popularBundle);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        if(savedInstanceState != null) {
            mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
            isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            if(!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            getCurrentAccountAndBindView();
        }

        fab.setOnClickListener(view -> postTypeBottomSheetFragment.show(getSupportFragmentManager(), postTypeBottomSheetFragment.getTag()));
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
                Intent loginIntent = new Intent(this, LoginActivity.class);
                startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST_CODE);
            } else {
                mAccessToken = account.getAccessToken();
                mName = account.getUsername();
                mProfileImageUrl = account.getProfileImageUrl();
                mBannerImageUrl = account.getBannerImageUrl();
                mKarma = Integer.toString(account.getKarma());
                bindView();
            }
        }).execute();
    }

    private void bindView() {
        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        if(getIntent().hasExtra(EXTRA_POST_TYPE)) {
            if(getIntent().getExtras().getString(EXTRA_POST_TYPE).equals("popular")) {
                viewPager.setCurrentItem(1);
            } else {
                viewPager.setCurrentItem(2);
            }
        }

        glide = Glide.with(this);

        View header = findViewById(R.id.nav_header_main_activity);
        mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
        mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
        mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
        mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

        loadUserData();

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
            Intent intent = new Intent(this, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mName);
            startActivity(intent);
        });

        subscriptionLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubscribedThingListingActivity.class);
            startActivity(intent);
        });

        settingsLinearLayout.setOnClickListener(view -> {

        });
    }

    private void loadUserData() {
        if (!mFetchUserInfoSuccess) {
            FetchMyInfo.fetchMyInfo(mOauthRetrofit, mAccessToken, new FetchMyInfo.FetchUserMyListener() {
                @Override
                public void onFetchMyInfoSuccess(String response) {
                    ParseAndSaveAccountInfo.parseAndSaveAccountInfo(response, mRedditDataRoomDatabase, new ParseAndSaveAccountInfo.ParseAndSaveAccountInfoListener() {
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
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
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_main_activity:
                if(viewPager.getCurrentItem() == 1 ||viewPager.getCurrentItem() == 2) {
                    popularAndAllSortTypeBottomSheetFragment.show(getSupportFragmentManager(), popularAndAllSortTypeBottomSheetFragment.getTag());
                } else {
                    bestSortTypeBottomSheetFragment.show(getSupportFragmentManager(), bestSortTypeBottomSheetFragment.getTag());
                }
                return true;
            case R.id.action_search_main_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, false);
                startActivity(intent);
                return true;
            case R.id.action_refresh_main_activity:
                sectionsPagerAdapter.refresh(viewPager.getCurrentItem());
                mFetchUserInfoSuccess = false;
                loadUserData();
                return true;
            case R.id.action_lazy_mode_main_activity:
                /*MenuItem lazyModeItem = mMenu.findItem(R.id.action_lazy_mode_main_activity);
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
                }*/
                return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        sectionsPagerAdapter.changeSortType(sortType, viewPager.getCurrentItem());
    }

    @Override
    public void postTypeSelected(int postType) {
        Intent intent;
        switch (postType) {
            case PostTypeBottomSheetFragment.TYPE_TEXT:
                intent = new Intent(MainActivity.this, PostTextActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_LINK:
                intent = new Intent(MainActivity.this, PostLinkActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_IMAGE:
                intent = new Intent(MainActivity.this, PostImageActivity.class);
                startActivity(intent);
                break;
            case PostTypeBottomSheetFragment.TYPE_VIDEO:
                intent = new Intent(MainActivity.this, PostVideoActivity.class);
                startActivity(intent);
        }
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PostFragment frontPagePostFragment;
        private PostFragment popularPostFragment;
        private PostFragment allPostFragment;

        SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            if (position == 0) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_BEST);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else if(position == 1) {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "popular");
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            } else {
                PostFragment fragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                bundle.putString(PostFragment.EXTRA_NAME, "all");
                bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
                bundle.putInt(PostFragment.EXTRA_FILTER, PostFragment.EXTRA_NO_FILTER);
                bundle.putString(PostFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                fragment.setArguments(bundle);
                return fragment;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Best";
                case 1:
                    return "Popular";
                case 2:
                    return "All";
            }
            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    frontPagePostFragment = (PostFragment) fragment;
                    break;
                case 1:
                    popularPostFragment = (PostFragment) fragment;
                    break;
                case 2:
                    allPostFragment = (PostFragment) fragment;
            }
            return fragment;
        }

        void changeSortType(String sortType, int fragmentPosition) {
            switch (fragmentPosition) {
                case 0:
                    frontPagePostFragment.changeSortType(sortType);
                    break;
                case 1:
                    popularPostFragment.changeSortType(sortType);
                    break;
                case 2:
                    allPostFragment.changeSortType(sortType);
            }
        }

        public void refresh(int fragmentPosition) {
            if(fragmentPosition == 0) {
                if(frontPagePostFragment != null) {
                    ((FragmentCommunicator) frontPagePostFragment).refresh();
                }
            } else if(fragmentPosition == 1) {
                if(popularPostFragment != null) {
                    ((FragmentCommunicator) popularPostFragment).refresh();
                }
            } else {
                if(allPostFragment != null) {
                    ((FragmentCommunicator) allPostFragment).refresh();
                }
            }
        }
    }
}
