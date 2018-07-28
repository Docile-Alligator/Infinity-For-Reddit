package ml.docilealligator.infinityforreddit;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import java.util.Collections;
import java.util.Comparator;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private String nameState = "NS";
    private String profileImageUrlState = "PIUS";
    private String bannerImageUrlState = "BIUS";
    private String karmaState = "KS";

    private TextView mNameTextView;
    private TextView mKarmaTextView;
    private CircleImageView mProfileImageView;
    private ImageView mBannerImageView;
    private RecyclerView mSubscribedSubredditRecyclerView;

    private Fragment mFragment;
    private RequestManager glide;

    private String mName;
    private String mProfileImageUrl;
    private String mBannerImageUrl;
    private String mKarma;
    private boolean mFetchUserInfoSuccess;

    private ArrayList<SubredditData> mSubredditData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        View header = findViewById(R.id.nav_header_main_activity);
        mNameTextView = header.findViewById(R.id.name_text_view_nav_header_main);
        mKarmaTextView = header.findViewById(R.id.karma_text_view_nav_header_main);
        mProfileImageView = header.findViewById(R.id.profile_image_view_nav_header_main);
        mBannerImageView = header.findViewById(R.id.banner_image_view_nav_header_main);

        mSubscribedSubredditRecyclerView = findViewById(R.id.subscribed_subreddit_recycler_view_main_activity);
        mSubscribedSubredditRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE).getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
        if(accessToken.equals("")) {
            Intent loginIntent = new Intent(this, LoginActivity.class);
            startActivity(loginIntent);
        } else {
            if(savedInstanceState == null) {
                FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                mFragment = new BestPostFragment();
                fragmentTransaction.replace(R.id.frame_layout_content_main, mFragment).commit();
            } else {
                mFragment = getFragmentManager().getFragment(savedInstanceState, "outStateFragment");
                getFragmentManager().beginTransaction().replace(R.id.frame_layout_content_main, mFragment).commit();
            }
        }

        if(savedInstanceState == null && !mFetchUserInfoSuccess) {
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
        new FetchSubscribedSubreddits(this, Volley.newRequestQueue(this), new ArrayList<SubredditData>())
                .fetchSubscribedSubreddits(new FetchSubscribedSubreddits.FetchSubscribedSubredditsListener() {
                    @Override
                    public void onFetchSubscribedSubredditsSuccess(ArrayList<SubredditData> subredditData) {
                        Collections.sort(subredditData, new Comparator<SubredditData>() {
                            @Override
                            public int compare(SubredditData subredditData, SubredditData t1) {
                                return subredditData.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
                            }
                        });
                        mSubredditData = subredditData;
                        mSubscribedSubredditRecyclerView.setAdapter(new SubscribedSubredditRecyclerViewAdapter(
                                MainActivity.this, mSubredditData));
                    }

                    @Override
                    public void onFetchSubscribedSubredditsFail() {

                    }
                }, 1);
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
            getFragmentManager().putFragment(outState, "outStateFragment", mFragment);
        }
        outState.putString(nameState, mName);
        outState.putString(profileImageUrlState, mProfileImageUrl);
        outState.putString(bannerImageUrlState, mBannerImageUrl);
        outState.putString(karmaState, mKarma);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mName = savedInstanceState.getString(nameState);
        mProfileImageUrl = savedInstanceState.getString(profileImageUrlState);
        mBannerImageUrl = savedInstanceState.getString(bannerImageUrlState);
        mKarma = savedInstanceState.getString(karmaState);
        mNameTextView.setText(mName);
        mKarmaTextView.setText(mKarma);
        if(!mProfileImageUrl.equals("")) {
            glide.load(mProfileImageUrl).into(mProfileImageView);
        }
        if(!mBannerImageUrl.equals("")) {
            glide.load(mBannerImageUrl).into(mBannerImageView);
        }
    }
}
