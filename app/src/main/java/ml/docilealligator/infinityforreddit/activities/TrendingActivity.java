package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.r0adkll.slidr.Slidr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.TrendingSearch;
import ml.docilealligator.infinityforreddit.adapters.TrendingSearchRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.post.ParsePost;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class TrendingActivity extends BaseActivity {

    private static final String TRENDING_SEARCHES_STATE = "TSS";

    @BindView(R.id.coordinator_layout_trending_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_trending_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_trending_activity)
    CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.toolbar_trending_activity)
    Toolbar toolbar;
    @BindView(R.id.swipe_refresh_layout_trending_activity)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view_trending_activity)
    RecyclerView recyclerView;
    @BindView(R.id.fetch_trending_search_linear_layout_trending_activity)
    LinearLayout errorLinearLayout;
    @BindView(R.id.fetch_trending_search_image_view_trending_activity)
    ImageView errorImageView;
    @BindView(R.id.fetch_trending_search_text_view_trending_activity)
    TextView errorTextView;
    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("post_layout")
    SharedPreferences mPostLayoutSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mAccessToken;
    private boolean isRefreshing = false;
    private ArrayList<TrendingSearch> trendingSearches;
    private TrendingSearchRecyclerViewAdapter adapter;
    private RequestManager mGlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    coordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(toolbar);

                int navBarResourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
                if (navBarResourceId > 0) {
                    recyclerView.setPadding(0, 0, 0, recyclerView.getPaddingBottom() + getResources().getDimensionPixelSize(navBarResourceId));
                }
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarGoToTop(toolbar);

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        mGlide = Glide.with(this);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int windowWidth = displayMetrics.widthPixels;

        String dataSavingModeString = mSharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        boolean dataSavingMode = false;
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            dataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            int networkType = Utils.getConnectedNetwork(this);
            dataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
        }
        boolean disableImagePreview = mSharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW, false);
        adapter = new TrendingSearchRecyclerViewAdapter(this, mCustomThemeWrapper, windowWidth,
                dataSavingMode, disableImagePreview, new TrendingSearchRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(TrendingSearch trendingSearch) {
                Intent intent = new Intent(TrendingActivity.this, SearchResultActivity.class);
                intent.putExtra(SearchResultActivity.EXTRA_QUERY, trendingSearch.queryString);
                intent.putExtra(SearchResultActivity.EXTRA_TRENDING_SOURCE, "trending");
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout.setEnabled(mSharedPreferences.getBoolean(SharedPreferencesUtils.PULL_TO_REFRESH, true));
        swipeRefreshLayout.setOnRefreshListener(this::fetchTrendingSearches);

        errorLinearLayout.setOnClickListener(view -> fetchTrendingSearches());

        if (savedInstanceState != null) {
            trendingSearches = savedInstanceState.getParcelableArrayList(TRENDING_SEARCHES_STATE);
        }
        if (trendingSearches != null) {
            adapter.setTrendingSearches(trendingSearches);
        } else {
            fetchTrendingSearches();
        }
    }

    private void fetchTrendingSearches() {
        if (isRefreshing) {
            return;
        }
        isRefreshing = true;

        errorLinearLayout.setVisibility(View.GONE);
        Glide.with(this).clear(errorImageView);
        swipeRefreshLayout.setRefreshing(true);
        trendingSearches = null;
        adapter.setTrendingSearches(null);
        Handler handler = new Handler();
        Call<String> trendingCall;
        if (mAccessToken == null) {
            trendingCall = mRetrofit.create(RedditAPI.class).getTrendingSearches();
        } else {
            trendingCall = mOauthRetrofit.create(RedditAPI.class).getTrendingSearchesOauth(APIUtils.getOAuthHeader(mAccessToken));
        }
        trendingCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    mExecutor.execute(() -> {
                        try {
                            JSONArray trendingSearchesArray = new JSONObject(response.body()).getJSONArray(JSONUtils.TRENDING_SEARCHES_KEY);
                            ArrayList<TrendingSearch> trendingSearchList = new ArrayList<>();
                            for (int i = 0; i < trendingSearchesArray.length(); i++) {
                                try {
                                    JSONObject trendingSearchObject = trendingSearchesArray.getJSONObject(i);
                                    String queryString = trendingSearchObject.getString(JSONUtils.QUERY_STRING_KEY);
                                    String displayString = trendingSearchObject.getString(JSONUtils.DISPLAY_STRING_KEY);
                                    JSONArray childrenWithOnlyOneChild = trendingSearchObject
                                            .getJSONObject(JSONUtils.RESULTS_KEY)
                                            .getJSONObject(JSONUtils.DATA_KEY)
                                            .getJSONArray(JSONUtils.CHILDREN_KEY);
                                    if (childrenWithOnlyOneChild.length() > 0) {
                                        Post post = ParsePost.parseBasicData(childrenWithOnlyOneChild.getJSONObject(0)
                                                .getJSONObject(JSONUtils.DATA_KEY));

                                        trendingSearchList.add(new TrendingSearch(queryString, displayString,
                                                post.getTitle(), post.getPreviews()));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            handler.post(() -> {
                                trendingSearches = trendingSearchList;
                                swipeRefreshLayout.setRefreshing(false);
                                adapter.setTrendingSearches(trendingSearches);
                                isRefreshing = false;
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                            handler.post(() -> {
                                swipeRefreshLayout.setRefreshing(false);
                                showErrorView(R.string.error_parse_trending_search);
                                isRefreshing = false;
                            });
                        }
                    });
                } else {
                    handler.post(() -> {
                        swipeRefreshLayout.setRefreshing(false);
                        showErrorView(R.string.error_fetch_trending_search);
                        isRefreshing = false;
                    });
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                handler.post(() -> {
                    swipeRefreshLayout.setRefreshing(false);
                    showErrorView(R.string.error_fetch_trending_search);
                    isRefreshing = false;
                });
            }
        });
    }

    private void showErrorView(int stringId) {
        errorLinearLayout.setVisibility(View.VISIBLE);
        mGlide.load(R.drawable.error_image).into(errorImageView);
        errorTextView.setText(stringId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.trending_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_refresh_trending_activity) {
            fetchTrendingSearches();
            return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(TRENDING_SEARCHES_STATE, trendingSearches);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, collapsingToolbarLayout, toolbar);
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        swipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        errorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            errorTextView.setTypeface(typeface);
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }
}