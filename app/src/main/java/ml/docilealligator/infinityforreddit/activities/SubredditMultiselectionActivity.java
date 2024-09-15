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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.SubredditMultiselectionRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivitySubscribedSubredditsMultiselectionBinding;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class SubredditMultiselectionActivity extends BaseActivity implements ActivityToolbarInterface {

    static final String EXTRA_RETURN_SELECTED_SUBREDDITS = "ERSS";

    private static final int SUBREDDIT_SEARCH_REQUEST_CODE = 1;

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
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private SubredditMultiselectionRecyclerViewAdapter mAdapter;
    private RequestManager mGlide;
    private ActivitySubscribedSubredditsMultiselectionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        binding = ActivitySubscribedSubredditsMultiselectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSubredditsMultiselectionActivity);
            }

            if (isImmersiveInterface()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    window.setDecorFitsSystemWindows(false);
                } else {
                    window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                }
                adjustToolbar(binding.toolbarSubscribedSubredditsMultiselectionActivity);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    binding.recyclerViewSubscribedSubscribedSubredditsMultiselectionActivity.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        setSupportActionBar(binding.toolbarSubscribedSubredditsMultiselectionActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        binding.swipeRefreshLayoutSubscribedSubscribedSubredditsMultiselectionActivity.setEnabled(false);

        bindView();
    }

    private void bindView() {
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(this);
        binding.recyclerViewSubscribedSubscribedSubredditsMultiselectionActivity.setLayoutManager(mLinearLayoutManager);
        mAdapter = new SubredditMultiselectionRecyclerViewAdapter(this, mCustomThemeWrapper);
        binding.recyclerViewSubscribedSubscribedSubredditsMultiselectionActivity.setAdapter(mAdapter);

        mSubscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(mRedditDataRoomDatabase, accountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(this, subscribedSubredditData -> {
            binding.swipeRefreshLayoutSubscribedSubscribedSubredditsMultiselectionActivity.setRefreshing(false);
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                binding.recyclerViewSubscribedSubscribedSubredditsMultiselectionActivity.setVisibility(View.GONE);
                binding.noSubscriptionsLinearLayoutSubscribedSubredditsMultiselectionActivity.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.noSubscriptionsImageViewSubscribedSubredditsMultiselectionActivity);
            } else {
                binding.noSubscriptionsLinearLayoutSubscribedSubredditsMultiselectionActivity.setVisibility(View.GONE);
                binding.recyclerViewSubscribedSubscribedSubredditsMultiselectionActivity.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageViewSubscribedSubredditsMultiselectionActivity);
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
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSubredditsMultiselectionActivity,
                binding.collapsingToolbarLayoutSubscribedSubredditsMultiselectionActivity, binding.toolbarSubscribedSubredditsMultiselectionActivity);
        binding.errorTextViewSubscribedSubredditsMultiselectionActivity.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (typeface != null) {
            binding.errorTextViewSubscribedSubredditsMultiselectionActivity.setTypeface(typeface);
        }
    }

    @Override
    public void onLongPress() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }
}
