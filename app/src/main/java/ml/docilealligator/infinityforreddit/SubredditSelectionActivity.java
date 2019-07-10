package ml.docilealligator.infinityforreddit;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import SubscribedSubredditDatabase.SubscribedSubredditViewModel;
import butterknife.BindView;
import butterknife.ButterKnife;

public class SubredditSelectionActivity extends AppCompatActivity {

    @BindView(R.id.recycler_view_subreddit_selection_activity) RecyclerView recyclerView;

    private SubscribedSubredditViewModel mSubscribedSubredditViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_selection);

        ButterKnife.bind(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        SubscribedSubredditsRecyclerViewAdapter adapter = new SubscribedSubredditsRecyclerViewAdapter(this);
        recyclerView.setAdapter(adapter);

        mSubscribedSubredditViewModel = ViewModelProviders.of(this).get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, adapter::setSubscribedSubreddits);
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
}
