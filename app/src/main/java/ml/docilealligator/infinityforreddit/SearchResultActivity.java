package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchResultActivity extends AppCompatActivity implements SearchPostSortTypeBottomSheetFragment.SearchSortTypeSelectionCallback,
        SearchUserAndSubredditSortTypeBottomSheetFragment.SearchUserAndSubredditSortTypeSelectionCallback {
    static final String EXTRA_QUERY = "QK";
    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private String mQuery;
    private String mSubredditName;

    @BindView(R.id.toolbar_search_result_activity) Toolbar toolbar;
    @BindView(R.id.tab_layout_search_result_activity) TabLayout tabLayout;
    @BindView(R.id.view_pager_search_result_activity) ViewPager viewPager;

    private SectionsPagerAdapter sectionsPagerAdapter;

    private SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment;
    private SearchUserAndSubredditSortTypeBottomSheetFragment searchUserAndSubredditSortTypeBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_result);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(sectionsPagerAdapter);
        viewPager.setOffscreenPageLimit(2);

        tabLayout.setupWithViewPager(viewPager);

        searchPostSortTypeBottomSheetFragment = new SearchPostSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SearchPostSortTypeBottomSheetFragment.EXTRA_FRAGMENT_POSITION, viewPager.getCurrentItem());
        searchPostSortTypeBottomSheetFragment.setArguments(bundle);

        searchUserAndSubredditSortTypeBottomSheetFragment = new SearchUserAndSubredditSortTypeBottomSheetFragment();

        // Get the intent, verify the action and get the query
        Intent intent = getIntent();
        String query = intent.getExtras().getString(EXTRA_QUERY);

        if(intent.hasExtra(EXTRA_SUBREDDIT_NAME)) {
            mSubredditName = intent.getExtras().getString(EXTRA_SUBREDDIT_NAME);
        }

        if(query != null) {
            mQuery = query;
            setTitle(query);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_result_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_sort_search_result_activity:
                switch (viewPager.getCurrentItem()) {
                    case 0: {
                        searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                        break;
                    }
                    case 1:
                    case 2:
                        Bundle bundle = new Bundle();
                        bundle.putInt(SearchUserAndSubredditSortTypeBottomSheetFragment.EXTRA_FRAGMENT_POSITION, viewPager.getCurrentItem());
                        searchUserAndSubredditSortTypeBottomSheetFragment.setArguments(bundle);
                        searchUserAndSubredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchUserAndSubredditSortTypeBottomSheetFragment.getTag());
                        break;
                }
                return true;
            case R.id.action_search_search_result_activity:
                Intent intent = new Intent(this, SearchActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.action_refresh_search_result_activity:
                sectionsPagerAdapter.refresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void searchSortTypeSelected(String sortType) {
        sectionsPagerAdapter.changeSortType(sortType, 0);
    }

    @Override
    public void searchUserAndSubredditSortTypeSelected(String sortType, int fragmentPosition) {
        sectionsPagerAdapter.changeSortType(sortType, fragmentPosition);
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
                    bundle.putInt(PostFragment.EXTRA_POST_TYPE, PostDataSource.TYPE_SEARCH);
                    bundle.putString(PostFragment.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_RELEVANCE);
                    bundle.putString(PostFragment.EXTRA_SUBREDDIT_NAME, mSubredditName);
                    bundle.putString(PostFragment.EXTRA_QUERY, mQuery);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                case 1: {
                    SubredditListingFragment mFragment = new SubredditListingFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString(SubredditListingFragment.EXTRA_QUERY_KEY, mQuery);
                    bundle.putBoolean(SubredditListingFragment.EXTRA_IS_POSTING, false);
                    mFragment.setArguments(bundle);
                    return mFragment;
                }
                default: {
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

        void changeSortType(String sortType, int fragmentPosition) {
            switch (fragmentPosition) {
                case 0:
                    postFragment.changeSortType(sortType);
                    break;
                case 1:
                    subredditListingFragment.changeSortType(sortType);
                    break;
                case 2:
                    userListingFragment.changeSortType(sortType);
            }
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
