package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditDao;
import SubredditDatabase.SubredditData;
import SubredditDatabase.SubredditRoomDatabase;
import SubscribedSubredditDatabase.SubscribedSubredditDao;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditRoomDatabase;
import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import SubscribedUserDatabase.SubscribedUserDao;
import SubscribedUserDatabase.SubscribedUserData;
import SubscribedUserDatabase.SubscribedUserRoomDatabase;
import SubscribedUserDatabase.SubscribedUserViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private static final String FRAGMENT_OUT_STATE = "FOS";
    private static final String NAME_STATE = "NS";
    private static final String PROFILE_IMAGE_URL_STATE = "PIUS";
    private static final String BANNER_IMAGE_URL_STATE = "BIUS";
    private static final String KARMA_STATE = "KS";
    private static final String FETCH_USER_INFO_STATE = "FUIS";
    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";

    @BindView(R.id.subscribed_subreddit_recycler_view_main_activity) RecyclerView subscribedSubredditRecyclerView;
    @BindView(R.id.subscriptions_label_main_activity) TextView subscriptionsLabelTextView;
    @BindView(R.id.subscribed_user_recycler_view_main_activity) RecyclerView subscribedUserRecyclerView;
    @BindView(R.id.following_label_main_activity) TextView followingLabelTextView;

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private CircleImageView mProfileImageView;
    private ImageView mBannerImageView;

    private Fragment mFragment;
    private RequestManager glide;

    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess;
    private boolean mInsertSuccess;

    private FragmentCommunicator mFragmentCommunicator;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;
    private SubscribedUserViewModel mSubscribedUserViewModel;

    @Inject @Named("user_info")
    SharedPreferences mUserInfoSharedPreferences;

    @Inject @Named("auth_info")
    SharedPreferences mAuthInfoSharedPreferences;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmNetworkComponent().inject(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        if(accessToken.equals("")) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            if(savedInstanceState == null) {
                mFragment = new PostFragment();
                Bundle bundle = new Bundle();
                bundle.putBoolean(PostFragment.IS_BEST_POST_KEY, true);
                mFragment.setArguments(bundle);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            }

            loadUserData(savedInstanceState);

            View header = findViewById(R.id.nav_header_main_activity);
            mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
            mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
            mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
            mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

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
            glide = Glide.with(this);
            if(!mProfileImageUrl.equals("")) {
                glide.load(mProfileImageUrl).into(mProfileImageView);
            }
            if(!mBannerImageUrl.equals("")) {
                glide.load(mBannerImageUrl).into(mBannerImageView);
            }

            final SubscribedSubredditRecyclerViewAdapter subredditadapter = new SubscribedSubredditRecyclerViewAdapter(this,
                    new SubscribedSubredditRecyclerViewAdapter.OnItemClickListener() {
                        @Override
                        public void onClick() {
                            drawer.closeDrawers();
                        }
                    });
            subscribedSubredditRecyclerView.setAdapter(subredditadapter);

            mSubscribedSubredditViewModel = ViewModelProviders.of(this).get(SubscribedSubredditViewModel.class);
            mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, new Observer<List<SubscribedSubredditData>>() {
                @Override
                public void onChanged(@Nullable final List<SubscribedSubredditData> subscribedSubredditData) {
                    if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                        subscriptionsLabelTextView.setVisibility(View.GONE);
                    } else {
                        subscriptionsLabelTextView.setVisibility(View.VISIBLE);
                    }

                    subredditadapter.setSubscribedSubreddits(subscribedSubredditData);
                }
            });

            final SubscribedUserRecyclerViewAdapter userAdapter = new SubscribedUserRecyclerViewAdapter(this,
                    new SubscribedUserRecyclerViewAdapter.OnItemClickListener() {
                        @Override
                        public void onClick() {
                            drawer.closeDrawers();
                        }
                    });
            subscribedUserRecyclerView.setAdapter(userAdapter);
            mSubscribedUserViewModel = ViewModelProviders.of(this).get(SubscribedUserViewModel.class);
            mSubscribedUserViewModel.getAllSubscribedUsers().observe(this, new Observer<List<SubscribedUserData>>() {
                @Override
                public void onChanged(@Nullable final List<SubscribedUserData> subscribedUserData) {
                    if (subscribedUserData == null || subscribedUserData.size() == 0) {
                        followingLabelTextView.setVisibility(View.GONE);
                    } else {
                        followingLabelTextView.setVisibility(View.VISIBLE);
                    }
                    userAdapter.setSubscribedUsers(subscribedUserData);
                }
            });
        }
    }

    private void loadUserData(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            if (!mFetchUserInfoSuccess) {
                FetchMyInfo.fetchMyInfo(mOauthRetrofit, mAuthInfoSharedPreferences, new FetchMyInfo.FetchUserMyListener() {
                    @Override
                    public void onFetchMyInfoSuccess(String response) {
                        ParseMyInfo.parseMyInfo(response, new ParseMyInfo.ParseMyInfoListener() {
                            @Override
                            public void onParseMyInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                                mNameTextView.setText(name);
                                if (!mProfileImageUrl.equals("")) {
                                    glide.load(profileImageUrl).into(mProfileImageView);
                                }
                                if (!mBannerImageUrl.equals("")) {
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
                        new ArrayList<SubscribedSubredditData>(), new ArrayList<SubscribedUserData>(),
                        new ArrayList<SubredditData>(),
                        new FetchSubscribedThing.FetchSubscribedThingListener() {
                            @Override
                            public void onFetchSubscribedThingSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                      ArrayList<SubscribedUserData> subscribedUserData,
                                                                      ArrayList<SubredditData> subredditData) {
                                new InsertSubscribedThingsAsyncTask(
                                        SubscribedSubredditRoomDatabase.getDatabase(MainActivity.this),
                                        SubscribedUserRoomDatabase.getDatabase(MainActivity.this),
                                        SubredditRoomDatabase.getDatabase(MainActivity.this),
                                        subscribedSubredditData,
                                        subscribedUserData,
                                        subredditData,
                                        new InsertSubscribedThingsAsyncTask.InsertSubscribedThingListener() {
                                            @Override
                                            public void insertSuccess() {
                                                mInsertSuccess = true;
                                            }
                                        }).execute();
                            }

                            @Override
                            public void onFetchSubscribedThingFail() {
                                mInsertSuccess = false;
                            }
                        });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_main_activity:
                if(mFragment instanceof FragmentCommunicator) {
                    ((FragmentCommunicator) mFragment).refresh();
                }
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }
        outState.putString(NAME_STATE, mName);
        outState.putString(PROFILE_IMAGE_URL_STATE, mProfileImageUrl);
        outState.putString(BANNER_IMAGE_URL_STATE, mBannerImageUrl);
        outState.putString(KARMA_STATE, mKarma);
        outState.putBoolean(FETCH_USER_INFO_STATE, mFetchUserInfoSuccess);
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mName = savedInstanceState.getString(NAME_STATE);
        mProfileImageUrl = savedInstanceState.getString(PROFILE_IMAGE_URL_STATE);
        mBannerImageUrl = savedInstanceState.getString(BANNER_IMAGE_URL_STATE);
        mKarma = savedInstanceState.getString(KARMA_STATE);
        mFetchUserInfoSuccess = savedInstanceState.getBoolean(FETCH_USER_INFO_STATE);
        mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
        mNameTextView.setText(mName);
        mKarmaTextView.setText(mKarma);
        if(!mProfileImageUrl.equals("")) {
            glide.load(mProfileImageUrl).into(mProfileImageView);
        }
        if(!mBannerImageUrl.equals("")) {
            glide.load(mBannerImageUrl).into(mBannerImageView);
        }
    }

    private static class InsertSubscribedThingsAsyncTask extends AsyncTask<Void, Void, Void> {

        interface InsertSubscribedThingListener {
            void insertSuccess();
        }

        private final SubscribedSubredditDao mSubscribedSubredditDao;
        private final SubscribedUserDao mUserDao;
        private final SubredditDao mSubredditDao;
        private List<SubscribedSubredditData> subscribedSubredditData;
        private List<SubscribedUserData> subscribedUserData;
        private List<SubredditData> subredditData;
        private InsertSubscribedThingListener insertSubscribedThingListener;

        InsertSubscribedThingsAsyncTask(SubscribedSubredditRoomDatabase subscribedSubredditDb,
                                        SubscribedUserRoomDatabase userDb,
                                        SubredditRoomDatabase subredditDb,
                                        List<SubscribedSubredditData> subscribedSubredditData,
                                        List<SubscribedUserData> subscribedUserData,
                                        List<SubredditData> subredditData,
                                        InsertSubscribedThingListener insertSubscribedThingListener) {
            mSubscribedSubredditDao = subscribedSubredditDb.subscribedSubredditDao();
            mUserDao = userDb.subscribedUserDao();
            mSubredditDao = subredditDb.subredditDao();
            this.subscribedSubredditData = subscribedSubredditData;
            this.subscribedUserData = subscribedUserData;
            this.subredditData = subredditData;
            this.insertSubscribedThingListener = insertSubscribedThingListener;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            for(SubscribedSubredditData s : subscribedSubredditData) {
                mSubscribedSubredditDao.insert(s);
            }
            for(SubscribedUserData s : subscribedUserData) {
                mUserDao.insert(s);
            }
            for(SubredditData s : subredditData) {
                mSubredditDao.insert(s);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            insertSubscribedThingListener.insertSuccess();
        }
    }
}
