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

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback {

    private static final String FRAGMENT_OUT_STATE = "FOS";
    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String IS_IN_LAZY_MODE_STATE = "IILMS";

    private static final int LOGIN_ACTIVITY_REQUEST_CODE = 0;

    @BindView(R.id.drawer_layout) DrawerLayout drawer;
    @BindView(R.id.collapsing_toolbar_layout_main_activity) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.profile_linear_layout_main_activity) LinearLayout profileLinearLayout;
    @BindView(R.id.subscriptions_linear_layout_main_activity) LinearLayout subscriptionLinearLayout;
    @BindView(R.id.settings_linear_layout_main_activity) LinearLayout settingsLinearLayout;
    @BindView(R.id.fab_main_activity) FloatingActionButton fab;

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private GifImageView mProfileImageView;
    private ImageView mBannerImageView;

    private Fragment mFragment;
    private RequestManager glide;
    private AppBarLayout.LayoutParams params;
    private BottomSheetDialog postTypedialog;
    private SortTypeBottomSheetFragment sortTypeBottomSheetFragment;

    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess = false;

    private Menu mMenu;

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

        View postTypeDialogView = View.inflate(this, R.layout.post_type_bottom_sheet, null);
        LinearLayout textTypeLinearLayout = postTypeDialogView.findViewById(R.id.text_type_linear_layout_post_type_bottom_sheet);
        LinearLayout linkTypeLinearLayout = postTypeDialogView.findViewById(R.id.link_type_linear_layout_post_type_bottom_sheet);
        LinearLayout imageTypeLinearLayout = postTypeDialogView.findViewById(R.id.image_type_linear_layout_post_type_bottom_sheet);
        LinearLayout videoTypeLinearLayout = postTypeDialogView.findViewById(R.id.video_type_linear_layout_post_type_bottom_sheet);

        postTypedialog = new BottomSheetDialog(this);
        postTypedialog.setContentView(postTypeDialogView);

        sortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        params = (AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams();

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        if (accessToken.equals("")) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivityForResult(loginIntent, LOGIN_ACTIVITY_REQUEST_CODE);
        } else {
            if (savedInstanceState == null) {
                replaceFragment(PostDataSource.SORT_TYPE_BEST);
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();

                mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
                isInLazyMode = savedInstanceState.getBoolean(IS_IN_LAZY_MODE_STATE);
            }

            glide = Glide.with(this);

            View header = findViewById(R.id.nav_header_main_activity);
            mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
            mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
            mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
            mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

            loadUserData();

            mName = mUserInfoSharedPreferences.getString(SharedPreferencesUtils.USER_KEY, "");
            mProfileImageUrl = mUserInfoSharedPreferences.getString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, "");
            mBannerImageUrl = mUserInfoSharedPreferences.getString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, "");
            mKarma = mUserInfoSharedPreferences.getString(SharedPreferencesUtils.KARMA_KEY, "");

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

        textTypeLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PostTextActivity.class);
            startActivity(intent);
            postTypedialog.dismiss();
        });

        linkTypeLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PostLinkActivity.class);
            startActivity(intent);
            postTypedialog.dismiss();
        });

        imageTypeLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PostImageActivity.class);
            startActivity(intent);
            postTypedialog.dismiss();
        });

        videoTypeLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, PostVideoActivity.class);
            startActivity(intent);
            postTypedialog.dismiss();
        });

        fab.setOnClickListener(view -> postTypedialog.show());
    }

    private void replaceFragment(String sortType) {
        mFragment = new PostFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_FRONT_PAGE);
        bundle.putString(PostFragment.EXTRA_SORT_TYPE, sortType);
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mFragment instanceof FragmentCommunicator) {
            switch (item.getItemId()) {
                case R.id.action_sort_main_activity:
                    sortTypeBottomSheetFragment.show(getSupportFragmentManager(), sortTypeBottomSheetFragment.getTag());
                    return true;
                case R.id.action_search_main_activity:
                    Intent intent = new Intent(this, SearchActivity.class);
                    startActivity(intent);
                    return true;
                case R.id.action_refresh_main_activity:
                    ((FragmentCommunicator) mFragment).refresh();
                    mFetchUserInfoSuccess = false;
                    loadUserData();
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
        outState.putBoolean(IS_IN_LAZY_MODE_STATE, isInLazyMode);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        replaceFragment(sortType);
    }
}
