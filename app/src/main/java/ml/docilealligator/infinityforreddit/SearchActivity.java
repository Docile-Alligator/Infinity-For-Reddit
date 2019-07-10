package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {
    static final String QUERY_KEY = "QK";

    private String mQuery;

    @BindView(R.id.toolbar_search_activity) Toolbar toolbar;
    @BindView(R.id.search_view_search_activity) SimpleSearchView simpleSearchView;
    @BindView(R.id.tab_layout_search_activity) TabLayout tabLayout;
    @BindView(R.id.transparent_overlay_search_activity) View transparentOverlay;
    @BindView(R.id.view_pager_search_activity) ViewPager viewPager;

    private SectionsPagerAdapter sectionsPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        transparentOverlay.setOnClickListener(view -> simpleSearchView.onBackPressed());

        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent intent = getIntent();
                intent.putExtra(SearchActivity.QUERY_KEY, query);
                finish();
                startActivity(intent);
                overridePendingTransition(0, 0);
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

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getExtras().getString(QUERY_KEY);
        if(query != null) {
            mQuery = query;
            setTitle(query);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (simpleSearchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity, menu);

        simpleSearchView.setMenuItem(menu.findItem(R.id.action_search_main_activity));

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_refresh_main_activity:
                sectionsPagerAdapter.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (simpleSearchView.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    private class SectionsPagerAdapter extends FragmentPagerAdapter {
        private PostFragment postFragment;
        private SubredditListingFragment subredditListingFragment;
        private UserListingFragment userListingFragment;

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @NonNull
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
                    UserListingFragment mFragment = new UserListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(UserListingFragment.QUERY_KEY, mQuery);
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

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            Fragment fragment = (Fragment) super.instantiateItem(container, position);
            switch (position) {
                case 0:
                    postFragment = (PostFragment) fragment;
                    break;
                case 1:
                    subredditListingFragment = (SubredditListingFragment) fragment;
                    break;
                case 2:
                    userListingFragment = (UserListingFragment) fragment;
                    break;
            }
            return fragment;
        }

        public void refresh() {
            if(postFragment != null) {
                ((FragmentCommunicator) postFragment).refresh();
            }
            if(subredditListingFragment != null) {
                ((FragmentCommunicator) subredditListingFragment).refresh();
            }
            if (userListingFragment != null) {
                ((FragmentCommunicator) userListingFragment).refresh();
            }
        }

        public void newSearch() {
            getItem(0);
            getItem(1);
            getItem(2);
        }
    }
}
