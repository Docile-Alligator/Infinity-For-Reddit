package ml.docilealligator.infinityforreddit;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FilteredPostsActivity extends AppCompatActivity implements SortTypeBottomSheetFragment.SortTypeSelectionCallback,
        SearchPostSortTypeBottomSheetFragment.SearchSortTypeSelectionCallback {

    static final String EXTRA_NAME = "ESN";
    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_FILTER = "EF";
    static final String EXTRA_POST_TYPE = "EPT";
    static final String EXTRA_SORT_TYPE = "EST";

    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.toolbar_filtered_posts_activity) Toolbar toolbar;

    private String name;
    private int postType;

    private Fragment mFragment;

    private SortTypeBottomSheetFragment bestSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment popularAndAllSortTypeBottomSheetFragment;
    private SortTypeBottomSheetFragment subredditSortTypeBottomSheetFragment;
    private SearchPostSortTypeBottomSheetFragment searchPostSortTypeBottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtered_posts);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        name = getIntent().getExtras().getString(EXTRA_NAME);
        int filter = getIntent().getExtras().getInt(EXTRA_FILTER);
        postType = getIntent().getExtras().getInt(EXTRA_POST_TYPE);
        String sortType = getIntent().getExtras().getString(EXTRA_SORT_TYPE);

        switch (postType) {
            case PostDataSource.TYPE_FRONT_PAGE:
                getSupportActionBar().setTitle(name);

                bestSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                Bundle bestBundle = new Bundle();
                bestBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, false);
                bestSortTypeBottomSheetFragment.setArguments(bestBundle);
                break;
            case PostDataSource.TYPE_SEARCH:
                getSupportActionBar().setTitle(R.string.search);

                searchPostSortTypeBottomSheetFragment = new SearchPostSortTypeBottomSheetFragment();
                Bundle searchBundle = new Bundle();
                searchPostSortTypeBottomSheetFragment.setArguments(searchBundle);
                break;
            case PostDataSource.TYPE_SUBREDDIT:
                if(name.equals("popular") || name.equals("all")) {
                    getSupportActionBar().setTitle(name.substring(0, 1).toUpperCase() + name.substring(1));

                    popularAndAllSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                    Bundle popularBundle = new Bundle();
                    popularBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
                    popularAndAllSortTypeBottomSheetFragment.setArguments(popularBundle);
                } else {
                    String subredditNamePrefixed = "r/" + name;
                    getSupportActionBar().setTitle(subredditNamePrefixed);

                    subredditSortTypeBottomSheetFragment = new SortTypeBottomSheetFragment();
                    Bundle bottomSheetBundle = new Bundle();
                    bottomSheetBundle.putBoolean(SortTypeBottomSheetFragment.EXTRA_NO_BEST_TYPE, true);
                    subredditSortTypeBottomSheetFragment.setArguments(bottomSheetBundle);
                }
                break;
            case PostDataSource.TYPE_USER:
                String usernamePrefixed = "u/" + name;
                getSupportActionBar().setTitle(usernamePrefixed);
                break;
        }

        switch (filter) {
            case Post.TEXT_TYPE:
                toolbar.setSubtitle(R.string.text);
                break;
            case Post.LINK_TYPE:
            case Post.NO_PREVIEW_LINK_TYPE:
                toolbar.setSubtitle(R.string.link);
                break;
            case Post.IMAGE_TYPE:
                toolbar.setSubtitle(R.string.image);
                break;
            case Post.VIDEO_TYPE:
                toolbar.setSubtitle(R.string.video);
                break;
            case Post.GIF_VIDEO_TYPE:
                toolbar.setSubtitle(R.string.gif);
        }

        if(savedInstanceState == null) {
            mFragment = new PostFragment();
            Bundle bundle = new Bundle();
            bundle.putString(PostFragment.EXTRA_NAME, name);
            bundle.putInt(PostFragment.EXTRA_POST_TYPE, postType);
            bundle.putString(PostFragment.EXTRA_SORT_TYPE, sortType);
            bundle.putInt(PostFragment.EXTRA_FILTER, filter);
            if(postType == PostDataSource.TYPE_SEARCH) {
                bundle.putString(PostFragment.EXTRA_QUERY, getIntent().getExtras().getString(EXTRA_QUERY));
            }
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
        } else {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_filtered_posts_activity, mFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.filtered_posts_activity, menu);
        if(postType == PostDataSource.TYPE_USER) {
            menu.findItem(R.id.action_sort_filtered_posts_activity).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_sort_filtered_posts_activity:
                switch (postType) {
                    case PostDataSource.TYPE_FRONT_PAGE:
                        bestSortTypeBottomSheetFragment.show(getSupportFragmentManager(), bestSortTypeBottomSheetFragment.getTag());
                        break;
                    case PostDataSource.TYPE_SEARCH:
                        searchPostSortTypeBottomSheetFragment.show(getSupportFragmentManager(), searchPostSortTypeBottomSheetFragment.getTag());
                        break;
                    case PostDataSource.TYPE_SUBREDDIT:
                        if(name.equals("popular") || name.equals("all")) {
                            popularAndAllSortTypeBottomSheetFragment.show(getSupportFragmentManager(), popularAndAllSortTypeBottomSheetFragment.getTag());
                        } else {
                            subredditSortTypeBottomSheetFragment.show(getSupportFragmentManager(), subredditSortTypeBottomSheetFragment.getTag());
                        }
                }
                return true;
            case R.id.action_refresh_filtered_posts_activity:
                ((FragmentCommunicator) mFragment).refresh();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }
    }

    @Override
    public void searchSortTypeSelected(String sortType) {
        ((PostFragment)mFragment).changeSortType(sortType);
    }

    @Override
    public void sortTypeSelected(String sortType) {
        ((PostFragment)mFragment).changeSortType(sortType);
    }
}
