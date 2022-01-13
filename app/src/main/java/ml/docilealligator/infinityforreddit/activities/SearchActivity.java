package ml.docilealligator.infinityforreddit.activities;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.ferfalk.simplesearchview.SimpleOnQueryTextListener;
import com.ferfalk.simplesearchview.SimpleSearchView;
import com.ferfalk.simplesearchview.SimpleSearchViewListener;
import com.google.android.material.appbar.AppBarLayout;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.SearchActivityRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.recentsearchquery.DeleteRecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQuery;
import ml.docilealligator.infinityforreddit.recentsearchquery.RecentSearchQueryViewModel;
import ml.docilealligator.infinityforreddit.subreddit.ParseSubredditData;
import ml.docilealligator.infinityforreddit.subreddit.SubredditData;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SearchActivity extends BaseActivity {

    public static final String EXTRA_QUERY = "EQ";
    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_SUBREDDIT_IS_USER = "ESIU";
    public static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    public static final String EXTRA_SEARCH_ONLY_USERS = "ESOU";
    public static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
    public static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIU";
    public static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESSN";
    public static final String RETURN_EXTRA_SELECTED_USERNAMES = "RESU";
    public static final String EXTRA_RETURN_USER_NAME = "ERUN";
    public static final String EXTRA_RETURN_USER_ICON_URL = "ERUIU";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final int SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE = 101;

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    private static final int USER_SEARCH_REQUEST_CODE = 2;

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
    @BindView(R.id.divider_search_activity)
    View divider;
    @BindView(R.id.recycler_view_search_activity)
    RecyclerView recyclerView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    @Named("nsfw_and_spoiler")
    SharedPreferences mNsfwAndSpoilerSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mAccountName;
    private String mAccessToken;
    private String query;
    private String subredditName;
    private boolean subredditIsUser;
    private boolean searchOnlySubreddits;
    private boolean searchOnlyUsers;
    private SearchActivityRecyclerViewAdapter adapter;
    private SubredditAutocompleteRecyclerViewAdapter subredditAutocompleteRecyclerViewAdapter;
    private Call<String> subredditAutocompleteCall;
    RecentSearchQueryViewModel mRecentSearchQueryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_search);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        setSupportActionBar(toolbar);

        searchOnlySubreddits = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_SUBREDDITS, false);
        searchOnlyUsers = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_USERS, false);

        if (searchOnlySubreddits) {
            simpleSearchView.setHint(getText(R.string.search_only_subreddits_hint));
        } else if (searchOnlyUsers) {
            simpleSearchView.setHint(getText(R.string.search_only_users_hint));
        }

        simpleSearchView.setOnSearchViewListener(new SimpleSearchViewListener() {
            @Override
            public void onSearchViewClosed() {
                finish();
            }
        });

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, null);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.NSFW_BASE, false);

        subredditAutocompleteRecyclerViewAdapter = new SubredditAutocompleteRecyclerViewAdapter(this,
                mCustomThemeWrapper, subredditData -> {
            if (searchOnlySubreddits) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    ArrayList<String> subredditNameList = new ArrayList<>();
                    subredditNameList.add(subredditData.getName());
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, subredditNameList);
                } else {
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, subredditData.getName());
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, subredditData.getIconUrl());
                }
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                Intent intent = new Intent(SearchActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                startActivity(intent);
            }
            finish();
        });

        simpleSearchView.setOnQueryTextListener(new SimpleOnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(@NonNull String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(@NonNull String newText) {
                if (!newText.isEmpty()) {
                    if (subredditAutocompleteCall != null) {
                        subredditAutocompleteCall.cancel();
                    }
                    subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(mAccessToken),
                            newText, nsfw);
                    subredditAutocompleteCall.enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            if (response.isSuccessful()) {
                                ParseSubredditData.parseSubredditListingData(response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                    @Override
                                    public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                        subredditAutocompleteRecyclerViewAdapter.setSubreddits(subredditData);
                                        recyclerView.setAdapter(subredditAutocompleteRecyclerViewAdapter);
                                    }

                                    @Override
                                    public void onParseSubredditListingDataFail() {

                                    }
                                });
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {

                        }
                    });
                    return true;
                }
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
            bindView();
        } else {
            query = getIntent().getStringExtra(EXTRA_QUERY);
            bindView();
        }

        if (searchOnlySubreddits || searchOnlyUsers) {
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
            subredditName = intent.getStringExtra(EXTRA_SUBREDDIT_NAME);
            subredditNameTextView.setText(subredditName);
            subredditIsUser = intent.getBooleanExtra(EXTRA_SUBREDDIT_IS_USER, false);
        }
    }

    private void bindView() {
        if (mAccountName != null) {
            adapter = new SearchActivityRecyclerViewAdapter(this, mCustomThemeWrapper, new SearchActivityRecyclerViewAdapter.ItemOnClickListener() {
                @Override
                public void onClick(String query) {
                    search(query);
                }

                @Override
                public void onDelete(RecentSearchQuery recentSearchQuery) {
                    DeleteRecentSearchQuery.deleteRecentSearchQueryListener(mRedditDataRoomDatabase, recentSearchQuery, () -> {});
                }
            });
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setNestedScrollingEnabled(false);
            recyclerView.setAdapter(adapter);

            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SEARCH_HISTORY, true)) {
                mRecentSearchQueryViewModel = new ViewModelProvider(this,
                        new RecentSearchQueryViewModel.Factory(mRedditDataRoomDatabase, mAccountName))
                        .get(RecentSearchQueryViewModel.class);

                mRecentSearchQueryViewModel.getAllRecentSearchQueries().observe(this, recentSearchQueries -> {
                    if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                        divider.setVisibility(View.VISIBLE);
                    } else {
                        divider.setVisibility(View.GONE);
                    }
                    adapter.setRecentSearchQueries(recentSearchQueries);
                });
            }
        }
    }

    private void search(String query) {
        if (query.equalsIgnoreCase("suicide") && mSharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_SUICIDE_PREVENTION_ACTIVITY, true)) {
            Intent intent = new Intent(this, SuicidePreventionActivity.class);
            intent.putExtra(SuicidePreventionActivity.EXTRA_QUERY, query);
            startActivityForResult(intent, SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE);
        } else {
            openSearchResult(query);
        }
    }

    private void openSearchResult(String query) {
        if (searchOnlySubreddits) {
            Intent intent = new Intent(SearchActivity.this, SearchSubredditsResultActivity.class);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchSubredditsResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
        } else if (searchOnlyUsers) {
            Intent intent = new Intent(this, SearchUsersResultActivity.class);
            intent.putExtra(SearchUsersResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchUsersResultActivity.EXTRA_IS_MULTI_SELECTION, getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false));
            startActivityForResult(intent, USER_SEARCH_REQUEST_CODE);
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
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        simpleSearchView.setSearchBackground(new ColorDrawable(mCustomThemeWrapper.getColorPrimary()));
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        simpleSearchView.setIconsColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setTextColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setBackIconColor(toolbarPrimaryTextAndIconColorColor);
        simpleSearchView.setHintTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        int colorAccent = mCustomThemeWrapper.getColorAccent();
        searchInTextView.setTextColor(colorAccent);
        subredditNameTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        divider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        if (typeface != null) {
            Utils.setFontToAllTextViews(coordinatorLayout, typeface);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        simpleSearchView.showSearch(false);
        simpleSearchView.requestFocus();

        if (query != null) {
            simpleSearchView.setQuery(query, false);
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
            imm.hideSoftInputFromWindow(simpleSearchView.getWindowToken(), 0);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (data != null && simpleSearchView.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
                subredditName = data.getStringExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                subredditIsUser = data.getBooleanExtra(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER, false);

                if (subredditName == null) {
                    subredditNameTextView.setText(R.string.all_subreddits);
                } else {
                    subredditNameTextView.setText(subredditName);
                }
            } else if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, data.getStringArrayListExtra(SearchSubredditsResultActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES));
                } else {
                    String name = data.getStringExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                    String iconUrl = data.getStringExtra(SearchSubredditsResultActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
                    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == USER_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_USERNAMES, data.getStringArrayListExtra(SearchUsersResultActivity.RETURN_EXTRA_SELECTED_USERNAMES));
                } else {
                    String username = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_NAME);
                    String iconUrl = data.getStringExtra(SearchUsersResultActivity.EXTRA_RETURN_USER_ICON_URL);
                    returnIntent.putExtra(EXTRA_RETURN_USER_NAME, username);
                    returnIntent.putExtra(EXTRA_RETURN_USER_ICON_URL, iconUrl);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE) {
                openSearchResult(data.getStringExtra(SuicidePreventionActivity.EXTRA_RETURN_QUERY));
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
