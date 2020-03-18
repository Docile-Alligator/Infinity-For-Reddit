package ml.docilealligator.infinityforreddit.Activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ferfalk.simplesearchview.SimpleSearchView;
import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;

public class SearchActivity extends BaseActivity {

    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_SUBREDDIT_IS_USER = "ESIU";
    static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_search_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_search_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.search_view_search_activity)
    SimpleSearchView simpleSearchView;
    @BindView(R.id.subreddit_name_relative_layout_search_activity)
    RelativeLayout subredditNameRelativeLayout;
    @BindView(R.id.search_in_text_view_search_activity)
    TextView searchInTextView;
    @BindView(R.id.subreddit_name_text_view_search_activity)
    TextView subredditNameTextView;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String query;
    private String subredditName;
    private boolean subredditIsUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        setSupportActionBar(toolbar);

        boolean searchOnlySubreddits = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_SUBREDDITS, false);

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
                if (searchOnlySubreddits) {
                    Intent intent = new Intent(SearchActivity.this, SearchSubredditsResultActivity.class);
                    intent.putExtra(SearchSubredditsResultActivity.EXTRA_QUERY, query);
                    startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
                } else {
                    Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
                    intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
                    if (subredditName != null) {
                        if (subredditIsUser) {
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

        if (savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);

            if (subredditName == null) {
                subredditNameTextView.setText(R.string.all_subreddits);
            } else {
                subredditNameTextView.setText(subredditName);
            }
        } else {
            query = getIntent().getStringExtra(EXTRA_QUERY);
        }

        if (searchOnlySubreddits) {
            subredditNameRelativeLayout.setVisibility(View.GONE);
        } else {
            subredditNameRelativeLayout.setOnClickListener(view -> {
                Intent intent = new Intent(this, SubredditSelectionActivity.class);
                intent.putExtra(SubredditSelectionActivity.EXTRA_EXTRA_CLEAR_SELECTION, true);
                startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
            });
        }

        Intent intent = getIntent();
        if (intent.hasExtra(EXTRA_SUBREDDIT_NAME)) {
            subredditName = intent.getExtras().getString(EXTRA_SUBREDDIT_NAME);
            subredditNameTextView.setText(subredditName);
            subredditIsUser = intent.getExtras().getBoolean(EXTRA_SUBREDDIT_IS_USER);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        simpleSearchView.setSearchBackground(new ColorDrawable(mCustomThemeWrapper.getColorPrimary()));
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        simpleSearchView.setIconsColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setTextColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setBackIconColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setHintTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        searchInTextView.setTextColor(mCustomThemeWrapper.getColorAccent());
        subredditNameTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
    }

    @Override
    protected void onStart() {
        super.onStart();
        simpleSearchView.showSearch(false);
        simpleSearchView.getSearchEditText().requestFocus();

        if (query != null) {
            simpleSearchView.getSearchEditText().setText(query);
            simpleSearchView.getSearchEditText().setSelection(query.length());
            query = null;
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(simpleSearchView.getSearchEditText().getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (simpleSearchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                if (subredditName == null) {
                    subredditNameTextView.setText(R.string.all_subreddits);
                } else {
                    subredditNameTextView.setText(subredditName);
                }
            }
        } else if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
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
        if (item.getItemId() == android.R.id.home) {
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
