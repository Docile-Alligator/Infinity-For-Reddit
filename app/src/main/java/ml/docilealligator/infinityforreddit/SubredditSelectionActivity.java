package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import butterknife.ButterKnife;

public class SubredditSelectionActivity extends AppCompatActivity {

    static final String EXTRA_RETURN_SUBREDDIT_NAME_KEY = "ERSNK";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL_KEY = "ERSIUK";

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selection);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

        SubscribedSubredditsListingFragment fragment = new SubscribedSubredditsListingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PostFragment.POST_TYPE_KEY, PostDataSource.TYPE_FRONT_PAGE);
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_subreddit_selection_activity, fragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return false;
    }

    void getSelectedSubreddit(String name, String iconUrl) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME_KEY, name);
        returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL_KEY, iconUrl);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
