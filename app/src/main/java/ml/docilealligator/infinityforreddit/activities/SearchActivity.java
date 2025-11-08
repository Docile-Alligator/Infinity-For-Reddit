package ml.docilealligator.infinityforreddit.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.inputmethod.EditorInfoCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.SearchActivityRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.SubredditAutocompleteRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySearchBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
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
    public static final String EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME = "ESISOUN";
    public static final String EXTRA_SEARCH_IN_MULTIREDDIT = "ESIM";
    public static final String EXTRA_SEARCH_IN_THING_TYPE = "ESITY";
    public static final String EXTRA_SEARCH_ONLY_SUBREDDITS = "ESOS";
    public static final String EXTRA_SEARCH_ONLY_USERS = "ESOU";
    public static final String EXTRA_SEARCH_SUBREDDITS_AND_USERS = "ESSAU";
    public static final String RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES = "RESSN";
    public static final String RETURN_EXTRA_SELECTED_USERNAMES = "RESU";
    public static final String EXTRA_IS_MULTI_SELECTION = "EIMS";
    public static final int SUICIDE_PREVENTION_ACTIVITY_REQUEST_CODE = 101;

    private static final String SEARCH_IN_SUBREDDIT_OR_NAME_STATE = "SNS";
    private static final String SEARCH_IN_THING_TYPE_STATE = "SITTS";
    private static final String SEARCH_IN_MULTIREDDIT_STATE = "SIMS";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;
    private static final int USER_SEARCH_REQUEST_CODE = 2;

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
    @Inject
    Executor executor;
    private String query;
    private String searchInSubredditOrUserName;
    private MultiReddit searchInMultiReddit;
    @SelectThingReturnKey.THING_TYPE
    private int searchInThingType;
    private boolean searchOnlySubreddits;
    private boolean searchOnlyUsers;
    private boolean searchSubredditsAndUsers;
    private SearchActivityRecyclerViewAdapter adapter;
    private SubredditAutocompleteRecyclerViewAdapter subredditAutocompleteRecyclerViewAdapter;
    private Handler handler;
    private Runnable autoCompleteRunnable;
    private Call<String> subredditAutocompleteCall;
    RecentSearchQueryViewModel mRecentSearchQueryViewModel;
    private ActivityResultLauncher<Intent> requestThingSelectionForCurrentActivityLauncher;
    private ActivityResultLauncher<Intent> requestThingSelectionForAnotherActivityLauncher;
    private ActivitySearchBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivitySearchBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSearchActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, true);

                    setMargins(binding.toolbar,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.nestedScrollViewSearchActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom
                    );

                    return WindowInsetsCompat.CONSUMED;
                }
            });
        }

        setSupportActionBar(binding.toolbar);

        binding.clearSearchEditViewSearchActivity.setVisibility(View.GONE);
        binding.deleteAllRecentSearchesButtonSearchActivity.setVisibility(View.GONE);

        searchOnlySubreddits = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_SUBREDDITS, false);
        searchOnlyUsers = getIntent().getBooleanExtra(EXTRA_SEARCH_ONLY_USERS, false);
        searchSubredditsAndUsers = getIntent().getBooleanExtra(EXTRA_SEARCH_SUBREDDITS_AND_USERS, false);

        if (searchOnlySubreddits) {
            binding.searchEditTextSearchActivity.setHint(R.string.search_only_subreddits_hint);
        } else if (searchOnlyUsers) {
            binding.searchEditTextSearchActivity.setHint(R.string.search_only_users_hint);
        } else if (searchSubredditsAndUsers) {
            binding.searchEditTextSearchActivity.setHint(R.string.search_subreddits_and_users_hint);
        }

        boolean nsfw = mNsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.NSFW_BASE, false);

        subredditAutocompleteRecyclerViewAdapter = new SubredditAutocompleteRecyclerViewAdapter(this,
                mCustomThemeWrapper, subredditData -> {
            if (searchOnlySubreddits || searchSubredditsAndUsers) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    ArrayList<String> subredditNameList = new ArrayList<>();
                    subredditNameList.add(subredditData.getName());
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, subredditNameList);
                } else {
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, subredditData.getName());
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, subredditData.getIconUrl());
                }
                setResult(Activity.RESULT_OK, returnIntent);
            } else {
                Intent intent = new Intent(SearchActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditData.getName());
                startActivity(intent);
            }
            finish();
        });

        if (accountName.equals(Account.ANONYMOUS_ACCOUNT) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            binding.searchEditTextSearchActivity.setImeOptions(binding.searchEditTextSearchActivity.getImeOptions() | EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING);
        }

        handler = new Handler();

        binding.searchEditTextSearchActivity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (subredditAutocompleteCall != null && subredditAutocompleteCall.isExecuted()) {
                    subredditAutocompleteCall.cancel();
                }
                if (autoCompleteRunnable != null) {
                    handler.removeCallbacks(autoCompleteRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String currentQuery = s.toString().trim();
                if (!currentQuery.isEmpty()) {
                    binding.clearSearchEditViewSearchActivity.setVisibility(View.VISIBLE);

                    autoCompleteRunnable = () -> {
                        subredditAutocompleteCall = mOauthRetrofit.create(RedditAPI.class).subredditAutocomplete(APIUtils.getOAuthHeader(accessToken),
                                currentQuery, nsfw);
                        subredditAutocompleteCall.enqueue(new Callback<>() {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                subredditAutocompleteCall = null;
                                if (response.isSuccessful() && !call.isCanceled()) {
                                    ParseSubredditData.parseSubredditListingData(executor, handler,
                                            response.body(), nsfw, new ParseSubredditData.ParseSubredditListingDataListener() {
                                                @Override
                                                public void onParseSubredditListingDataSuccess(ArrayList<SubredditData> subredditData, String after) {
                                                    binding.recentSearchQueryRecyclerViewSearchActivity.setVisibility(View.GONE);
                                                    binding.subredditAutocompleteRecyclerViewSearchActivity.setVisibility(View.VISIBLE);
                                                    subredditAutocompleteRecyclerViewAdapter.setSubreddits(subredditData);
                                                }

                                                @Override
                                                public void onParseSubredditListingDataFail() {

                                                }
                                            });
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                                subredditAutocompleteCall = null;
                            }
                        });
                    };

                    handler.postDelayed(autoCompleteRunnable, 500);
                } else {
                    binding.recentSearchQueryRecyclerViewSearchActivity.setVisibility(View.VISIBLE);
                    binding.subredditAutocompleteRecyclerViewSearchActivity.setVisibility(View.GONE);
                    binding.clearSearchEditViewSearchActivity.setVisibility(View.GONE);
                }
            }
        });

        binding.searchEditTextSearchActivity.setOnEditorActionListener((v, actionId, event) -> {
            if ((actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) || (event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                if (!binding.searchEditTextSearchActivity.getText().toString().isEmpty()) {
                    search(binding.searchEditTextSearchActivity.getText().toString());
                    return true;
                }
            }
            return false;
        });

        binding.clearSearchEditViewSearchActivity.setOnClickListener(view -> {
            binding.searchEditTextSearchActivity.getText().clear();
        });

        binding.linkHandlerImageViewSearchActivity.setOnClickListener(view -> {
            if (!binding.searchEditTextSearchActivity.getText().toString().equals("")) {
                Intent intent = new Intent(this, LinkResolverActivity.class);
                intent.setData(Uri.parse(binding.searchEditTextSearchActivity.getText().toString()));
                startActivity(intent);
                finish();
            }
        });

        binding.deleteAllRecentSearchesButtonSearchActivity.setOnClickListener(view -> {
            new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                    .setTitle(R.string.confirm)
                    .setMessage(R.string.confirm_delete_all_recent_searches)
                    .setPositiveButton(R.string.yes, (dialogInterface, i) -> {
                        Utils.hideKeyboard(SearchActivity.this);

                        executor.execute(() -> {
                            List<RecentSearchQuery> deletedQueries = mRedditDataRoomDatabase.recentSearchQueryDao().getAllRecentSearchQueries(accountName);
                            mRedditDataRoomDatabase.recentSearchQueryDao().deleteAllRecentSearchQueries(accountName);
                            handler.post(() -> Snackbar.make(view, R.string.all_recent_searches_deleted, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.undo, v -> executor.execute(() -> mRedditDataRoomDatabase.recentSearchQueryDao().insertAll(deletedQueries)))
                                    .addCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar transientBottomBar, int event) {
                                            Utils.showKeyboard(SearchActivity.this, handler, binding.searchEditTextSearchActivity);
                                        }
                                    })
                                    .show());
                        });
                    })
                    .setNegativeButton(R.string.no, null)
                    .show();
        });

        if (savedInstanceState != null) {
            searchInSubredditOrUserName = savedInstanceState.getString(SEARCH_IN_SUBREDDIT_OR_NAME_STATE);
            searchInThingType = savedInstanceState.getInt(SEARCH_IN_THING_TYPE_STATE);
            searchInMultiReddit = savedInstanceState.getParcelable(SEARCH_IN_MULTIREDDIT_STATE);

            setSearchInThingText();
        } else {
            query = getIntent().getStringExtra(EXTRA_QUERY);
            searchInSubredditOrUserName = getIntent().getStringExtra(EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME);
            searchInMultiReddit = getIntent().getParcelableExtra(EXTRA_SEARCH_IN_MULTIREDDIT);
            searchInThingType = getIntent().getIntExtra(EXTRA_SEARCH_IN_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);
            if (searchInSubredditOrUserName != null) {
                binding.subredditNameTextViewSearchActivity.setText(searchInSubredditOrUserName);
            } else if (searchInMultiReddit != null) {
                binding.subredditNameTextViewSearchActivity.setText(searchInMultiReddit.getDisplayName());
            }
        }
        bindView();

        requestThingSelectionForCurrentActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            Intent returnIntent = result.getData();
            if (returnIntent == null || returnIntent.getExtras() == null) {
                return;
            }

            searchInSubredditOrUserName = returnIntent.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME);
            searchInMultiReddit = returnIntent.getParcelableExtra(SelectThingReturnKey.RETRUN_EXTRA_MULTIREDDIT);
            searchInThingType = returnIntent.getIntExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);

            setSearchInThingText();
        });

        requestThingSelectionForAnotherActivityLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            setResult(RESULT_OK, result.getData());
            finish();
        });

        if (searchOnlySubreddits || searchOnlyUsers || searchSubredditsAndUsers) {
            binding.subredditNameRelativeLayoutSearchActivity.setVisibility(View.GONE);
            binding.dividerSearchActivity.setVisibility(View.GONE);
        } else {
            binding.subredditNameRelativeLayoutSearchActivity.setOnClickListener(view -> {
                Intent intent = new Intent(this, SubscribedThingListingActivity.class);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_THING_SELECTION_MODE, true);
                intent.putExtra(SubscribedThingListingActivity.EXTRA_EXTRA_CLEAR_SELECTION, true);
                requestThingSelectionForCurrentActivityLauncher.launch(intent);
            });
        }
    }

    private void bindView() {
        if (!accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            adapter = new SearchActivityRecyclerViewAdapter(this, mCustomThemeWrapper, new SearchActivityRecyclerViewAdapter.ItemOnClickListener() {
                @Override
                public void onClick(RecentSearchQuery recentSearchQuery, boolean searchImmediately) {
                    if (searchImmediately) {
                        searchInSubredditOrUserName = recentSearchQuery.getSearchInSubredditOrUserName();
                        searchInMultiReddit = MultiReddit.getDummyMultiReddit(recentSearchQuery.getMultiRedditPath());
                        if (searchInMultiReddit != null && recentSearchQuery.getMultiRedditDisplayName() != null) {
                            searchInMultiReddit.setDisplayName(recentSearchQuery.getMultiRedditDisplayName());
                        }
                        searchInThingType = recentSearchQuery.getSearchInThingType();
                        search(recentSearchQuery.getSearchQuery());
                    } else {
                        binding.searchEditTextSearchActivity.setText(recentSearchQuery.getSearchQuery());
                        binding.searchEditTextSearchActivity.setSelection(recentSearchQuery.getSearchQuery().length());
                    }
                }

                @Override
                public void onDelete(RecentSearchQuery recentSearchQuery) {
                    Utils.hideKeyboard(SearchActivity.this);

                    executor.execute(() -> {
                        mRedditDataRoomDatabase.recentSearchQueryDao().deleteRecentSearchQueries(recentSearchQuery);
                        Snackbar.make(binding.getRoot(), R.string.recent_search_deleted, Snackbar.LENGTH_SHORT)
                                .setAction(R.string.undo, v -> executor.execute(() -> mRedditDataRoomDatabase.recentSearchQueryDao().insert(recentSearchQuery)))
                                .addCallback(new Snackbar.Callback() {
                                    @Override
                                    public void onDismissed(Snackbar transientBottomBar, int event) {
                                        Utils.showKeyboard(SearchActivity.this, handler, binding.searchEditTextSearchActivity);
                                    }
                                })
                                .show();
                    });
                }
            });
            binding.recentSearchQueryRecyclerViewSearchActivity.setVisibility(View.VISIBLE);
            binding.recentSearchQueryRecyclerViewSearchActivity.setNestedScrollingEnabled(false);
            binding.recentSearchQueryRecyclerViewSearchActivity.setAdapter(adapter);
            binding.recentSearchQueryRecyclerViewSearchActivity.addItemDecoration(new RecyclerView.ItemDecoration() {
                final int spacing = (int) Utils.convertDpToPixel(16, SearchActivity.this);
                final int halfSpacing = spacing / 2;

                @Override
                public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                    int column = ((GridLayoutManager.LayoutParams) view.getLayoutParams()).getSpanIndex();
                    boolean toTheLeft = column == 0;

                    if (toTheLeft) {
                        outRect.left = spacing;
                        outRect.right = halfSpacing;
                    } else {
                        outRect.left = halfSpacing;
                        outRect.right = spacing;
                    }
                    outRect.top = spacing;
                }
            });

            binding.subredditAutocompleteRecyclerViewSearchActivity.setAdapter(subredditAutocompleteRecyclerViewAdapter);

            if (mSharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_SEARCH_HISTORY, true)) {
                mRecentSearchQueryViewModel = new ViewModelProvider(this,
                        new RecentSearchQueryViewModel.Factory(mRedditDataRoomDatabase, accountName))
                        .get(RecentSearchQueryViewModel.class);

                mRecentSearchQueryViewModel.getAllRecentSearchQueries().observe(this, recentSearchQueries -> {
                    if (recentSearchQueries != null && !recentSearchQueries.isEmpty()) {
                        binding.dividerSearchActivity.setVisibility(View.VISIBLE);
                        binding.deleteAllRecentSearchesButtonSearchActivity.setVisibility(View.VISIBLE);
                    } else {
                        binding.dividerSearchActivity.setVisibility(View.GONE);
                        binding.deleteAllRecentSearchesButtonSearchActivity.setVisibility(View.GONE);
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
        } else if (searchSubredditsAndUsers) {
            Intent intent = new Intent(this, SearchResultActivity.class);
            intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchResultActivity.EXTRA_SHOULD_RETURN_SUBREDDIT_AND_USER_NAME, true);
            requestThingSelectionForAnotherActivityLauncher.launch(intent);
        } else {
            Intent intent = new Intent(SearchActivity.this, SearchResultActivity.class);
            intent.putExtra(SearchResultActivity.EXTRA_QUERY, query);
            intent.putExtra(SearchResultActivity.EXTRA_SEARCH_IN_SUBREDDIT_OR_USER_NAME, searchInSubredditOrUserName);
            intent.putExtra(SearchResultActivity.EXTRA_SEARCH_IN_MULTIREDDIT, searchInMultiReddit);
            intent.putExtra(SearchResultActivity.EXTRA_SEARCH_IN_THING_TYPE, searchInThingType);
            startActivity(intent);
            finish();
        }
    }

    private void setSearchInThingText() {
        switch (searchInThingType) {
            case SelectThingReturnKey.THING_TYPE.SUBREDDIT:
            case SelectThingReturnKey.THING_TYPE.USER:
                if (searchInSubredditOrUserName == null) {
                    binding.subredditNameTextViewSearchActivity.setText(R.string.all_subreddits);
                } else {
                    binding.subredditNameTextViewSearchActivity.setText(searchInSubredditOrUserName);
                }
                break;
            case SelectThingReturnKey.THING_TYPE.MULTIREDDIT:
                binding.subredditNameTextViewSearchActivity.setText(searchInMultiReddit.getDisplayName());

        }
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSearchActivity, null, binding.toolbar);
        int toolbarPrimaryTextAndIconColorColor = mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor();
        binding.searchEditTextSearchActivity.setTextColor(toolbarPrimaryTextAndIconColorColor);
        binding.searchEditTextSearchActivity.setHintTextColor(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor());
        binding.clearSearchEditViewSearchActivity.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        binding.linkHandlerImageViewSearchActivity.setColorFilter(mCustomThemeWrapper.getToolbarPrimaryTextAndIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        int colorAccent = mCustomThemeWrapper.getColorAccent();
        binding.searchInTextViewSearchActivity.setTextColor(colorAccent);
        binding.subredditNameTextViewSearchActivity.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        binding.deleteAllRecentSearchesButtonSearchActivity.setIconTint(ColorStateList.valueOf(mCustomThemeWrapper.getPrimaryIconColor()));
        binding.dividerSearchActivity.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        if (typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), typeface);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        binding.searchEditTextSearchActivity.requestFocus();

        if (query != null) {
            binding.searchEditTextSearchActivity.setText(query);
            binding.searchEditTextSearchActivity.setSelection(query.length());
            query = null;
        }

        Utils.showKeyboard(this, new Handler(), binding.searchEditTextSearchActivity);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES, data.getStringArrayListExtra(SearchSubredditsResultActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES));
                } else {
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.SUBREDDIT);
                }
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            } else if (requestCode == USER_SEARCH_REQUEST_CODE) {
                Intent returnIntent = new Intent();
                if (getIntent().getBooleanExtra(EXTRA_IS_MULTI_SELECTION, false)) {
                    returnIntent.putStringArrayListExtra(RETURN_EXTRA_SELECTED_USERNAMES, data.getStringArrayListExtra(SearchUsersResultActivity.RETURN_EXTRA_SELECTED_USERNAMES));
                } else {
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, data.getStringExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON));
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.USER);
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
        outState.putString(SEARCH_IN_SUBREDDIT_OR_NAME_STATE, searchInSubredditOrUserName);
        outState.putInt(SEARCH_IN_THING_TYPE_STATE, searchInThingType);
        outState.putParcelable(SEARCH_IN_MULTIREDDIT_STATE, searchInMultiReddit);
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