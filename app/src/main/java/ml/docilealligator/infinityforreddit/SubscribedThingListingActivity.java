package ml.docilealligator.infinityforreddit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditData;
import SubredditDatabase.SubredditRoomDatabase;
import SubscribedSubredditDatabase.SubscribedSubredditData;
import SubscribedSubredditDatabase.SubscribedSubredditRoomDatabase;
import SubscribedUserDatabase.SubscribedUserData;
import SubscribedUserDatabase.SubscribedUserRoomDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;

public class SubscribedThingListingActivity extends AppCompatActivity {

    private static final String INSERT_SUBSCRIBED_SUBREDDIT_STATE = "ISSS";

    @BindView(R.id.toolbar_subscribed_thing_listing_activity) Toolbar toolbar;
    @BindView(R.id.tab_layout_subscribed_thing_listing_activity) TabLayout tabLayout;
    @BindView(R.id.view_pager_subscribed_thing_listing_activity) ViewPager viewPager;

    private boolean mInsertSuccess = false;

    private SectionsPagerAdapter sectionsPagerAdapter;

    @Inject
    @Named("auth_info")
    SharedPreferences mAuthInfoSharedPreferences;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_thing_listing);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        if(savedInstanceState != null) {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE);
        }

        loadSubscriptions();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSERT_SUBSCRIBED_SUBREDDIT_STATE, mInsertSuccess);
    }

    private void loadSubscriptions() {
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
                                    SubscribedSubredditRoomDatabase.getDatabase(SubscribedThingListingActivity.this).subscribedSubredditDao(),
                                    SubscribedUserRoomDatabase.getDatabase(SubscribedThingListingActivity.this).subscribedUserDao(),
                                    SubredditRoomDatabase.getDatabase(SubscribedThingListingActivity.this).subredditDao(),
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

    private class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    return new SubscribedSubredditsListingFragment();
                }
                default:
                {
                    return new FollowedUsersListingFragment();
                }
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.subreddits);
                case 1:
                    return getString(R.string.users);
            }

            return null;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }
    }
}
