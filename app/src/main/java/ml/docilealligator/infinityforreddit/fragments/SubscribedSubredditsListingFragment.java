package ml.docilealligator.infinityforreddit.fragments;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import ml.docilealligator.infinityforreddit.FragmentCommunicator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.SubredditSelectionActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.adapters.SubscribedSubredditsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribedSubredditsListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_ACCOUNT_PROFILE_IMAGE_URL = "EAPIU";
    public static final String EXTRA_IS_SUBREDDIT_SELECTION = "EISS";
    public static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";

    @BindView(R.id.swipe_refresh_layout_subscribed_subreddits_listing_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_subscribed_subreddits_listing_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_subreddits_listing_fragment)
    LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_subreddits_listing_fragment)
    ImageView mImageView;
    @BindView(R.id.error_text_view_subscribed_subreddits_listing_fragment)
    TextView mErrorTextView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    public SubscribedSubredditViewModel mSubscribedSubredditViewModel;
    private BaseActivity mActivity;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;

    public SubscribedSubredditsListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_subscribed_subreddits_listing, container, false);

        ButterKnife.bind(this, rootView);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            mRecyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            Resources resources = getResources();
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        String accountName = getArguments().getString(EXTRA_ACCOUNT_NAME, "-");
        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);

        if (accessToken == null) {
            mSwipeRefreshLayout.setEnabled(false);
        }

        mGlide = Glide.with(this);

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        SubscribedSubredditsRecyclerViewAdapter adapter;
        if (getArguments().getBoolean(EXTRA_IS_SUBREDDIT_SELECTION)) {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRedditDataRoomDatabase,
                    mCustomThemeWrapper, accessToken, getArguments().getBoolean(EXTRA_EXTRA_CLEAR_SELECTION),
                    (name, iconUrl, subredditIsUser) -> ((SubredditSelectionActivity) mActivity).getSelectedSubreddit(name, iconUrl, subredditIsUser));
        } else {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRedditDataRoomDatabase,
                    mCustomThemeWrapper, accessToken);
        }

        mRecyclerView.setAdapter(adapter);
        new FastScrollerBuilder(mRecyclerView).build();

        mSubscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, accountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(getViewLifecycleOwner(), subscribedSubredditData -> {
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

            if (accessToken != null) {
                adapter.addUser(accountName, getArguments().getString(EXTRA_ACCOUNT_PROFILE_IMAGE_URL));
            }
            adapter.setSubscribedSubreddits(subscribedSubredditData);
        });

        mSubscribedSubredditViewModel.getAllFavoriteSubscribedSubreddits().observe(getViewLifecycleOwner(), favoriteSubscribedSubredditData -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (favoriteSubscribedSubredditData != null && favoriteSubscribedSubredditData.size() > 0) {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }

            adapter.setFavoriteSubscribedSubreddits(favoriteSubscribedSubredditData);
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void stopRefreshProgressbar() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void applyTheme() {
        if (mActivity instanceof SubscribedThingListingActivity) {
            mSwipeRefreshLayout.setOnRefreshListener(() -> ((SubscribedThingListingActivity) mActivity).loadSubscriptions(true));
            mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
            mSwipeRefreshLayout.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        } else {
            mSwipeRefreshLayout.setEnabled(false);
        }
        mErrorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            mErrorTextView.setTypeface(mActivity.contentTypeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void changeSearchQuery(String searchQuery) {
        mSubscribedSubredditViewModel.setSearchQuery(searchQuery);
    }
}
