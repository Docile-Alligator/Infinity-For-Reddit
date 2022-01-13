package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.r0adkll.slidr.Slidr;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.ActivityToolbarInterface;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.SubredditMultiselectionRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SubredditMultiselectionActivity extends BaseActivity implements ActivityToolbarInterface {

    static final String EXTRA_RETURN_SELECTED_SUBREDDITS = "ERSS";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_subreddits_multiselection_activity)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_subreddits_multiselection_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.collapsing_toolbar_layout_subscribed_subreddits_multiselection_activity)
    CollapsingToolbarLayout mCollapsingToolbarLayout;
    @BindView(R.id.toolbar_subscribed_subreddits_multiselection_activity)
    Toolbar mToolbar;
    @BindView(R.id.swipe_refresh_layout_subscribed_subscribed_subreddits_multiselection_activity)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_subscribed_subscribed_subreddits_multiselection_activity)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_subscribed_subreddits_multiselection_activity)
    LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_subscribed_subreddits_multiselection_activity)
    ImageView mImageView;
    @BindView(R.id.error_text_view_subscribed_subreddits_multiselection_activity)
    TextView mErrorTextView;
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
    CustomThemeWrapper mCustomThemeWrapper;
    public SubscribedSubredditViewModel mSubscribedSubredditViewModel;
    private String mAccountName;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private SubredditMultiselectionRecyclerViewAdapter mAdapter;
    private RequestManager mGlide;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscribed_subreddits_multiselection);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    mCoordinatorLayout.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    mRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mSwipeRefreshLayout.setEnabled(false);

        mAccountName = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCOUNT_NAME, "-");

        bindView();
    }

    private void bindView() {
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mAdapter = new SubredditMultiselectionRecyclerViewAdapter(this, mCustomThemeWrapper);
        mRecyclerView.setAdapter(mAdapter);

        mSubscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }

            mAdapter.setSubscribedSubreddits(subscribedSubredditData);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.subreddit_multiselection_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            finish();
            return true;
        } else if (itemId == R.id.action_save_subreddit_multiselection_activity) {
            if (mAdapter != null) {
                Intent returnIntent = new Intent();
                returnIntent.putStringArrayListExtra(EXTRA_RETURN_SELECTED_SUBREDDITS,
                        mAdapter.getAllSelectedSubreddits());
                setResult(RESULT_OK, returnIntent);
            }
            finish();
            return true;
        } else if (itemId == R.id.action_search_subreddit_multiselection_activity) {
            Intent intent = new Intent(this, SearchActivity.class);
            intent.putExtra(SearchActivity.EXTRA_SEARCH_ONLY_SUBREDDITS, true);
            intent.putExtra(SearchActivity.EXTRA_IS_MULTI_SELECTION, true);
            startActivityForResult(intent, SUBREDDIT_SEARCH_REQUEST_CODE);
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SEARCH_REQUEST_CODE && resultCode == RESULT_OK && data != null && mAdapter != null) {
            Intent returnIntent = new Intent();
            ArrayList<String> selectedSubreddits = mAdapter.getAllSelectedSubreddits();
            ArrayList<String> searchedSubreddits = data.getStringArrayListExtra(SearchActivity.RETURN_EXTRA_SELECTED_SUBREDDIT_NAMES);
            if (searchedSubreddits != null) {
                selectedSubreddits.addAll(searchedSubreddits);
            }
            returnIntent.putStringArrayListExtra(EXTRA_RETURN_SELECTED_SUBREDDITS, selectedSubreddits);
            setResult(RESULT_OK, returnIntent);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(mAppBarLayout, mCollapsingToolbarLayout, mToolbar);
        mErrorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            mErrorTextView.setTypeface(typeface);
        }
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}
