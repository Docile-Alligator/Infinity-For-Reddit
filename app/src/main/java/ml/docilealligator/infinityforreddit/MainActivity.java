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
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private String nameState = "NS";
    private String profileImageUrlState = "PIUS";
    private String bannerImageUrlState = "BIUS";
    private String karmaState = "KS";
    private String fetchUserInfoState = "FUIS";
    private String insertSubscribedSubredditState = "ISSS";

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
    private boolean mIsInserting;
    private boolean mInsertSuccess;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;
    private SubscribedUserViewModel mSubscribedUserViewModel;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
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
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                mFragment = new BestPostFragment();
                fragmentTransaction.replace(R.id.frame_layout_content_main, mFragment).commit();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, "outStateFragment");
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            }

            Calendar now = Calendar.getInstance();
            Calendar queryAccessTokenTime = Calendar.getInstance();
            queryAccessTokenTime.setTimeInMillis(getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getLong(SharedPreferencesUtils.QUERY_ACCESS_TOKEN_TIME_KEY, 0));
            int interval = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getInt(SharedPreferencesUtils.ACCESS_TOKEN_EXPIRE_INTERVAL_KEY, 0);
            queryAccessTokenTime.add(Calendar.SECOND, interval - 300);

            if(now.after(queryAccessTokenTime)) {
                new AcquireAccessToken(this).refreshAccessToken(Volley.newRequestQueue(this),
                        new AcquireAccessToken.AcquireAccessTokenListener() {
                            @Override
                            public void onAcquireAccessTokenSuccess() {
                                loadUserData(savedInstanceState);
                            }

                            @Override
                            public void onAcquireAccessTokenFail() {}
                        });
            } else {
                loadUserData(savedInstanceState);
            }

            View header = findViewById(R.id.nav_header_main_activity);
            mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
            mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
            mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
            mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

            RecyclerView subscribedSubredditRecyclerView = findViewById(R.id.subscribed_subreddit_recycler_view_main_activity);
            subscribedSubredditRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            subscribedSubredditRecyclerView.setNestedScrollingEnabled(false);
            final TextView subscriptionsLabelTextView = findViewById(R.id.subscriptions_label_main_activity);

            RecyclerView subscribedUserRecyclerView = findViewById(R.id.subscribed_user_recycler_view_main_activity);
            subscribedUserRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            subscribedUserRecyclerView.setNestedScrollingEnabled(false);
            final TextView followingLabelTextView = findViewById(R.id.following_label_main_activity);

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

            final SubscribedSubredditRecyclerViewAdapter subredditadapter = new SubscribedSubredditRecyclerViewAdapter(this);
            subscribedSubredditRecyclerView.setAdapter(subredditadapter);
            mSubscribedSubredditViewModel = ViewModelProviders.of(this).get(SubscribedSubredditViewModel.class);
            mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, new Observer<List<SubscribedSubredditData>>() {
                @Override
                public void onChanged(@Nullable final List<SubscribedSubredditData> subscribedSubredditData) {
                    if(!mIsInserting) {
                        if(subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                            subscriptionsLabelTextView.setVisibility(View.GONE);
                        } else {
                            subscriptionsLabelTextView.setVisibility(View.VISIBLE);
                        }

                        subredditadapter.setSubscribedSubreddits(subscribedSubredditData);
                    }
                }
            });

            final SubscribedUserRecyclerViewAdapter userAdapter = new SubscribedUserRecyclerViewAdapter(this);
            subscribedUserRecyclerView.setAdapter(userAdapter);
            mSubscribedUserViewModel = ViewModelProviders.of(this).get(SubscribedUserViewModel.class);
            mSubscribedUserViewModel.getAllSubscribedUsers().observe(this, new Observer<List<SubscribedUserData>>() {
                @Override
                public void onChanged(@Nullable final List<SubscribedUserData> subscribedUserData) {
                    if(!mIsInserting) {
                        if(subscribedUserData == null || subscribedUserData.size() == 0) {
                            followingLabelTextView.setVisibility(View.GONE);
                        } else {
                            followingLabelTextView.setVisibility(View.VISIBLE);
                        }
                        userAdapter.setSubscribedUsers(subscribedUserData);
                    }
                }
            });
        }
    }

    private void loadUserData(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            if(!mFetchUserInfoSuccess) {
                new FetchUserInfo(this, Volley.newRequestQueue(this)).queryUserInfo(new FetchUserInfo.FetchUserInfoListener() {
                    @Override
                    public void onFetchUserInfoSuccess(String response) {
                        new ParseUserInfo().parseUserInfo(response, new ParseUserInfo.ParseUserInfoListener() {
                            @Override
                            public void onParseUserInfoSuccess(String name, String profileImageUrl, String bannerImageUrl, int karma) {
                                mNameTextView.setText(name);
                                if(!mProfileImageUrl.equals("")) {
                                    glide.load(profileImageUrl).into(mProfileImageView);
                                }
                                if(!mBannerImageUrl.equals("")) {
                                    glide.load(bannerImageUrl).into(mBannerImageView);
                                }

                                mName = name;
                                mProfileImageUrl = profileImageUrl;
                                mBannerImageUrl = bannerImageUrl;
                                mKarma = getString(R.string.karma_info, karma);

                                mKarmaTextView.setText(mKarma);

                                SharedPreferences.Editor editor = getSharedPreferences(SharedPreferencesUtils.USER_INFO_FILE_KEY, Context.MODE_PRIVATE).edit();
                                editor.putString(SharedPreferencesUtils.USER_KEY, name);
                                editor.putString(SharedPreferencesUtils.PROFILE_IMAGE_URL_KEY, profileImageUrl);
                                editor.putString(SharedPreferencesUtils.BANNER_IMAGE_URL_KEY, bannerImageUrl);
                                editor.putString(SharedPreferencesUtils.KARMA_KEY, mKarma);
                                editor.apply();
                                mFetchUserInfoSuccess = true;
                            }

                            @Override
                            public void onParseUserInfoFail() {
                                mFetchUserInfoSuccess = false;
                            }
                        });
                    }

                    @Override
                    public void onFetchUserInfoFail() {

                    }
                }, 1);
            }

            if(!mInsertSuccess) {
                new FetchSubscribedThing(this, Volley.newRequestQueue(this), new ArrayList<SubscribedSubredditData>(),
                        new ArrayList<SubscribedUserData>(), new ArrayList<SubredditData>())
                        .fetchSubscribedSubreddits(new FetchSubscribedThing.FetchSubscribedSubredditsListener() {
                            @Override
                            public void onFetchSubscribedSubredditsSuccess(ArrayList<SubscribedSubredditData> subscribedSubredditData,
                                                                           ArrayList<SubscribedUserData> subscribedUserData,
                                                                           ArrayList<SubredditData> subredditData) {
                                mIsInserting = true;
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
                                                mIsInserting = false;
                                                mInsertSuccess = true;
                                            }
                                        }).execute();
                            }

                            @Override
                            public void onFetchSubscribedSubredditsFail() {

                            }
                        }, 1);
            }
        }
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
            getSupportFragmentManager().putFragment(outState, "outStateFragment", mFragment);
        }
        outState.putString(nameState, mName);
        outState.putString(profileImageUrlState, mProfileImageUrl);
        outState.putString(bannerImageUrlState, mBannerImageUrl);
        outState.putString(karmaState, mKarma);
        outState.putBoolean(fetchUserInfoState, mFetchUserInfoSuccess);
        outState.putBoolean(insertSubscribedSubredditState, mInsertSuccess);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mName = savedInstanceState.getString(nameState);
        mProfileImageUrl = savedInstanceState.getString(profileImageUrlState);
        mBannerImageUrl = savedInstanceState.getString(bannerImageUrlState);
        mKarma = savedInstanceState.getString(karmaState);
        mFetchUserInfoSuccess = savedInstanceState.getBoolean(fetchUserInfoState);
        mInsertSuccess = savedInstanceState.getBoolean(insertSubscribedSubredditState);
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
