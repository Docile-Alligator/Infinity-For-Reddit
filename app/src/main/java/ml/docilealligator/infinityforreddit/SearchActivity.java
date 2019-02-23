package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

public class SearchActivity extends AppCompatActivity {
    static final String QUERY_KEY = "QK";

    private String mQuery;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Toolbar toolbar = findViewById(R.id.toolbar_search_activity);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ViewPager viewPager = findViewById(R.id.view_pager_search_activity);
        PagerAdapter pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        TabLayout tabLayout = findViewById(R.id.tab_layout_search_activity);
        tabLayout.setupWithViewPager(viewPager);

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getExtras().getString(QUERY_KEY);
        if(query != null) {
            mQuery = query;
            setTitle(query);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: {
                    PostFragment mFragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.POST_TYPE_KEY, PostDataSource.TYPE_SEARCH);
                    bundle.putString(PostFragment.NAME_KEY, mQuery);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                case 1: {
                    SubredditListingFragment mFragment = new SubredditListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(SubredditListingFragment.QUERY_KEY, mQuery);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                default:
                {
                    PostFragment mFragment = new PostFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(PostFragment.POST_TYPE_KEY, PostDataSource.TYPE_FRONT_PAGE);
                    bundle.putString(PostFragment.NAME_KEY, mQuery);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
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
                    return "Posts";
                case 1:
                    return "Subreddits";
                case 2:
                    return "Users";
            }
            return null;
        }
    }
}
