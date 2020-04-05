package ml.docilealligator.infinityforreddit.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.MultiRedditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.InsertMultiRedditAsyncTask;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.RefreshMultiRedditsEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MultiReddit.DeleteMultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.FetchMyMultiReddits;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditViewModel;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import retrofit2.Retrofit;

public class MultiRedditListingActivity extends BaseActivity {

    private static final String INSERT_MULTI_REDDIT_STATE = "ISSS";
    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";

    @BindView(R.id.coordinator_layout_multi_reddit_listing_activity)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_multi_reddit_listing_activity)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_multi_reddit_listing_activity)
    Toolbar mToolbar;
    @BindView(R.id.swipe_refresh_layout_multi_reddit_listing_activity)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_multi_reddit_listing_activity)
    RecyclerView mRecyclerView;
    @BindView(R.id.fetch_multi_reddit_listing_info_linear_layout_multi_reddit_listing_activity)
    LinearLayout mErrorLinearLayout;
    @BindView(R.id.fetch_multi_reddit_listing_info_image_view_multi_reddit_listing_activity)
    ImageView mErrorImageView;
    @BindView(R.id.fetch_multi_reddit_listing_info_text_view_multi_reddit_listing_activity)
    TextView mErrorTextView;
    @BindView(R.id.fab_multi_reddit_listing_activity)
    FloatingActionButton fab;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    MultiRedditViewModel mMultiRedditViewModel;
    private RequestManager mGlide;

    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private boolean mInsertSuccess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_reddit_listing);

        ButterKnife.bind(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(mAppBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(mToolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    mRecyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSwipeRefreshLayout.setOnRefreshListener(this::loadMultiReddits);

        if (savedInstanceState != null) {
            mInsertSuccess = savedInstanceState.getBoolean(INSERT_MULTI_REDDIT_STATE);
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccountAndBindView();
            } else {
                bindView();
            }
        } else {
            getCurrentAccountAndBindView();
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            bindView();
        }).execute();
    }

    private void bindView() {
        loadMultiReddits();

        mGlide = Glide.with(this);

        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MultiRedditListingActivity.this, CreateMultiRedditActivity.class);
            startActivity(intent);
        });

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        MultiRedditListingRecyclerViewAdapter adapter = new MultiRedditListingRecyclerViewAdapter(this,
                mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, mAccessToken, mAccountName);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        mMultiRedditViewModel = new ViewModelProvider(this,
                new MultiRedditViewModel.Factory(getApplication(), mRedditDataRoomDatabase, mAccountName))
                .get(MultiRedditViewModel.class);

        mMultiRedditViewModel.getAllMultiReddits().observe(this, subscribedUserData -> {
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mErrorLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(mErrorImageView);
            } else {
                mErrorLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mErrorImageView);
            }
            adapter.setMultiReddits(subscribedUserData);
        });

        mMultiRedditViewModel.getAllFavoriteMultiReddits().observe(this, favoriteSubscribedUserData -> {
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                mErrorLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mErrorImageView);
            }
            adapter.setFavoriteMultiReddits(favoriteSubscribedUserData);
        });
    }

    private void loadMultiReddits() {
        mSwipeRefreshLayout.setRefreshing(true);
        FetchMyMultiReddits.fetchMyMultiReddits(mOauthRetrofit, mAccessToken, new FetchMyMultiReddits.FetchMyMultiRedditsListener() {
            @Override
            public void success(ArrayList<MultiReddit> multiReddits) {
                new InsertMultiRedditAsyncTask(mRedditDataRoomDatabase, multiReddits, mAccountName, () -> {
                    mInsertSuccess = true;
                    mSwipeRefreshLayout.setRefreshing(false);
                }).execute();
            }

            @Override
            public void failed() {
                mInsertSuccess = false;
                mSwipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MultiRedditListingActivity.this, R.string.error_loading_multi_reddit_list, Toast.LENGTH_SHORT).show();
            }
        });
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(INSERT_MULTI_REDDIT_STATE, mInsertSuccess);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
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
        mCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(mAppBarLayout, mToolbar);
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
        mSwipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        mErrorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        applyFABTheme(fab);
    }

    public void deleteMultiReddit(MultiReddit multiReddit) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_multi_reddit_dialog_message)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteMultiReddit.deleteMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase,
                                mAccessToken, mAccountName, multiReddit.getPath(), new DeleteMultiReddit.DeleteMultiRedditListener() {
                                    @Override
                                    public void success() {
                                        Toast.makeText(MultiRedditListingActivity.this,
                                                R.string.delete_multi_reddit_success, Toast.LENGTH_SHORT).show();
                                        loadMultiReddits();
                                    }

                                    @Override
                                    public void failed() {
                                        Toast.makeText(MultiRedditListingActivity.this,
                                                R.string.delete_multi_reddit_failed, Toast.LENGTH_SHORT).show();
                                    }
                                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    @Subscribe
    public void onRefreshMultiRedditsEvent(RefreshMultiRedditsEvent event) {
        loadMultiReddits();
    }
}
