package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.ferfalk.simplesearchview.SimpleSearchView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_SUBREDDIT_IS_USER = "ESIU";
    static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.search_view_search_activity) SimpleSearchView simpleSearchView;
    @BindView(R.id.subreddit_name_relative_layout_search_activity) RelativeLayout subredditNameRelativeLayout;
    @BindView(R.id.subreddit_name_text_view_search_activity) TextView subredditNameTextView;

    private String subredditName;
    private boolean subredditIsUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Window window = getWindow();
            if((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            window.setNavigationBarColor(ContextCompat.getColor(this, R.color.navBarColor));
        }

        setSupportActionBar(toolbar);

        boolean searchOnlySubreddits = getIntent().getExtras().getBoolean(EXTRA_SEARCH_ONLY_SUBREDDITS);

        simpleSearchView.setOnSearchViewListener(new SimpleSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                finish();
            }

            @Override
            public void onSearchViewShownAnimation() {

            }

            @Override
            public void onSearchViewClosedAnimation() {

            }
        });

        simpleSearchView.setOnQueryTextListener(new SimpleSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(searchOnlySubreddits) {
                    Intent intent = new Intent(SearchActivity.this, SearchSubredditsResultActivity.class);
                    intent.putExtra(SearchSubredditsResultActivity.EXTRA_QUERY, query);
                    startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                    intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
                    if(subredditName != null) {
                        if(subredditIsUser) {
                            intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                        } else {
                            intent.putExtra(SearchResultActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                        }
                    }
                    startActivity(intent);
                    finish();
                }
                return true;
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

        if(savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);

            if(subredditName == null) {
                subredditNameTextView.setText(R.string.all_subreddits);
            } else {
                subredditNameTextView.setText(subredditName);
            }
        }

        if(searchOnlySubreddits) {
            subredditNameRelativeLayout.setVisibility(View.GONE);
        } else {
            subredditNameRelativeLayout.setOnClickListener(view -> {
                Intent intent = new Intent(this, SubredditSelectionActivity.class);
                intent.putExtra(SubredditSelectionActivity.EXTRA_EXTRA_CLEAR_SELECTION, true);
                startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
            });
        }

        Intent intent = getIntent();
        if(intent.hasExtra(EXTRA_SUBREDDIT_NAME)) {
            subredditName = intent.getExtras().getString(EXTRA_SUBREDDIT_NAME);
            subredditNameTextView.setText(subredditName);
            subredditIsUser = intent.getExtras().getBoolean(EXTRA_SUBREDDIT_IS_USER);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        simpleSearchView.showSearch(false);
        simpleSearchView.getSearchEditText().requestFocus();
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (simpleSearchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if(requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                if(subredditName == null) {
                    subredditNameTextView.setText(R.string.all_subreddits);
                } else {
                    subredditNameTextView.setText(subredditName);
                }
            }
        } else if(requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                String name = data.getExtras().getString(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                String iconUrl = data.getExtras().getString(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                Intent returnIntent = new Intent();
                returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
                returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_activity, menu);
        simpleSearchView.setMenuItem(menu.findItem(R.id.action_search_search_activity));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}
