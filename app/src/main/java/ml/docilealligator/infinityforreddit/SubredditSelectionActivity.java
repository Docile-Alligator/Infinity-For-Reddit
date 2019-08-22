package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedUserDatabase.SubscribedUserData;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

public class SubredditSelectionActivity extends AppCompatActivity {

    static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";
    static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";
    static final String EXTRA_RETURN_SUBREDDIT_IS_USER = "ERSIU";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 0;
    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    private static final String ACCOUNT_PROFILE_IMAGE_URL = "APIU";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.appbar_layout_subreddit_selection_activity) AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_subreddit_selection_activity) Toolbar toolbar;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private String mAccountProfileImageUrl;
    private boolean mInsertSuccess = false;

    private Fragment mFragment;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selection);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            Resources resources = getResources();

            if(resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT || resources.getBoolean(R.bool.isTablet)) {
                Window window = getWindow();
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

                boolean lightNavBar = false;
                if((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                    lightNavBar = true;
                }
                boolean finalLightNavBar = lightNavBar;

                View decorView = window.getDecorView();
                if(finalLightNavBar) {
                    decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                }
                appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                    @Override
                    void onStateChanged(AppBarLayout appBarLayout, State state) {
                        if(state == State.COLLAPSED) {
                            if(finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        } else if(state == State.EXPANDED) {
                            if(finalLightNavBar) {
                                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                            }
                        }
                    }
                });

                int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (statusBarResourceId > 0) {
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                    params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                    toolbar.setLayoutParams(params);
                }
            }
        }

        boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
        int themeType = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
        switch (themeType) {
            case 0:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
                break;
            case 1:
                AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
                break;
            case 2:
                if(systemDefault) {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
                } else {
                    AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
                }

        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getCurrentAccountAndBindView();
        } else {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            mAccountProfileImageUrl = savedInstanceState.getString(ACCOUNT_PROFILE_IMAGE_URL);

            if(!mNullAccessToken && mAccountName == null) {
                getCurrentAccountAndBindView();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_subreddit_selection_activity, mFragment).commit();
            }
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
                mAccountProfileImageUrl = account.getProfileImageUrl();
            }
            bindView();
        }).execute();
    }

    private void bindView() {
        loadSubscriptions();

        mFragment = new SubscribedSubredditsListingFragment();
        Bundle bundle = new Bundle();
        bundle.putString(SubscribedSubredditsListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
        bundle.putString(SubscribedSubredditsListingFragment.EXTRA_ACCOUNT_PROFILE_IMAGE_URL, mAccountProfileImageUrl);
        bundle.putBoolean(SubscribedSubredditsListingFragment.EXTRA_IS_SUBREDDIT_SELECTION, true);
        if(getIntent().hasExtra(EXTRA_EXTRA_CLEAR_SELECTION)) {
            bundle.putBoolean(SubscribedSubredditsListingFragment.EXTRA_EXTRA_CLEAR_SELECTION,
                    getIntent().getExtras().getBoolean(EXTRA_EXTRA_CLEAR_SELECTION));
        }
        mFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_subreddit_selection_activity, mFragment).commit();
    }

    private void loadSubscriptions() {
        if (!mInsertSuccess) {
            FetchSubscribedThing.fetchSubscribedThing(mOauthRetrofit, mAccessToken, mAccountName, null,
                    new ArrayList<>(), new ArrayList<>(),
                    new ArrayList<>(),
                    new FetchSubscribedThing.FetchSubscribedThingListener() {
                        @Override
                        public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                  ArrayList<SubscribedUserData> subscribedUserData,
                                                                  ArrayList<SubredditData> subredditData) {
                            new InsertSubscribedThingsAsyncTask(
                                    mRedditDataRoomDatabase.subscribedSubredditDao(),
                                    mRedditDataRoomDatabase.subscribedUserDao(),
                                    mRedditDataRoomDatabase.subredditDao(),
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddit_selection_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_search_subreddit_selection_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
                startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
                return true;
        }

        return false;
    }

    void getSelectedSubreddit(String name, String iconUrl, boolean subredditIsUser) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_IS_USER, subredditIsUser);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                String name = data.getExtras().getString(SearchActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                String iconUrl = data.getExtras().getString(SearchActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
                returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
                returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_IS_USER, false);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(ACCOUNT_PROFILE_IMAGE_URL, mAccountProfileImageUrl);
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
}
