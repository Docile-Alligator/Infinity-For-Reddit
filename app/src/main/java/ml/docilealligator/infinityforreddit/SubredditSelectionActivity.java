package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditSelectionActivity extends AppCompatActivity {

    static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";
    static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";
    static final String EXTRA_RETURN_SUBREDDIT_IS_USER = "ERSIU";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 0;
    private static final String NULL_ACCOUNT_NAME_STATE = "NATS";
    private static final String ACCOUNT_NAME_STATE = "ATS";
    private static final String ACCOUNT_PROFILE_IMAGE_URL = "APIU";
    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.toolbar_subreddit_selection_activity) Toolbar toolbar;

    private boolean mNullAccountName = false;
    private String mAccountName;
    private String mAccountProfileImageUrl;

    private Fragment mFragment;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selection);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if(savedInstanceState == null) {
            getCurrentAccountAndInitializeFragment();
        } else {
            mNullAccountName = savedInstanceState.getBoolean(NULL_ACCOUNT_NAME_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            mAccountProfileImageUrl = savedInstanceState.getString(ACCOUNT_PROFILE_IMAGE_URL);

            if(!mNullAccountName && mAccountName == null) {
                getCurrentAccountAndInitializeFragment();
            } else {
                mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_subreddit_selection_activity, mFragment).commit();
            }
        }
    }

    private void getCurrentAccountAndInitializeFragment() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccountName = true;
            } else {
                mAccountName = account.getUsername();
                mAccountProfileImageUrl = account.getProfileImageUrl();
            }
            initializeFragment();
        }).execute();
    }

    private void initializeFragment() {
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
        outState.putBoolean(NULL_ACCOUNT_NAME_STATE, mNullAccountName);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
        outState.putString(ACCOUNT_PROFILE_IMAGE_URL, mAccountProfileImageUrl);
    }
}
