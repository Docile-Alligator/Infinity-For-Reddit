package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchSubredditsResultActivity extends AppCompatActivity {

    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";

    private static final String FRAGMENT_OUT_STATE = "FOS";

    @BindView(R.id.toolbar_search_subreddits_result_activity) Toolbar toolbar;

    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_subreddits_result);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String query = getIntent().getExtras().getString(EXTRA_QUERY);

        if(savedInstanceState == null) {
            mFragment = new SubredditListingFragment();
            Bundle bundle = new Bundle();
            bundle.putString(SubredditListingFragment.EXTRA_QUERY_KEY, query);
            bundle.putBoolean(SubredditListingFragment.EXTRA_IS_POSTING, true);
            mFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_search_subreddits_result_activity, mFragment).commit();
        } else {
            mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
            getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_search_subreddits_result_activity, mFragment).commit();

        }
    }

    void getSelectedSubreddit(String name, String iconUrl) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mFragment != null) {
            getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
        }
    }
}
