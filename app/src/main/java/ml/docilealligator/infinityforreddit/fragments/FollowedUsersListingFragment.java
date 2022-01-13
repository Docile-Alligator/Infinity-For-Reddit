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
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.adapters.FollowedUsersRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowedUsersListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_ACCOUNT_NAME = "EAN";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";

    @BindView(R.id.swipe_refresh_layout_followed_users_listing_fragment)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view_followed_users_listing_fragment)
    RecyclerView mRecyclerView;
    @BindView(R.id.no_subscriptions_linear_layout_followed_users_listing_fragment)
    LinearLayout mLinearLayout;
    @BindView(R.id.no_subscriptions_image_view_followed_users_listing_fragment)
    ImageView mImageView;
    @BindView(R.id.error_text_view_followed_users_listing_fragment)
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
    SubscribedUserViewModel mSubscribedUserViewModel;
    private BaseActivity mActivity;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;

    public FollowedUsersListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_followed_users_listing, container, false);

        ButterKnife.bind(this, rootView);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        Resources resources = getResources();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            mRecyclerView.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mGlide = Glide.with(this);

        String accessToken = getArguments().getString(EXTRA_ACCESS_TOKEN);
        if (accessToken == null) {
            mSwipeRefreshLayout.setEnabled(false);
        }
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        FollowedUsersRecyclerViewAdapter adapter = new FollowedUsersRecyclerViewAdapter(mActivity,
                mExecutor, mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, accessToken);
        mRecyclerView.setAdapter(adapter);
        new FastScrollerBuilder(mRecyclerView).build();

        mSubscribedUserViewModel = new ViewModelProvider(this,
                new SubscribedUserViewModel.Factory(mActivity.getApplication(), mRedditDataRoomDatabase, getArguments().getString(EXTRA_ACCOUNT_NAME)))
                .get(SubscribedUserViewModel.class);

        mSubscribedUserViewModel.getAllSubscribedUsers().observe(getViewLifecycleOwner(), subscribedUserData -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                mRecyclerView.setVisibility(View.GONE);
                mLinearLayout.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(mImageView);
            } else {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }
            adapter.setSubscribedUsers(subscribedUserData);
        });

        mSubscribedUserViewModel.getAllFavoriteSubscribedUsers().observe(getViewLifecycleOwner(), favoriteSubscribedUserData -> {
            mSwipeRefreshLayout.setRefreshing(false);
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                mLinearLayout.setVisibility(View.GONE);
                mRecyclerView.setVisibility(View.VISIBLE);
                mGlide.clear(mImageView);
            }
            adapter.setFavoriteSubscribedUsers(favoriteSubscribedUserData);
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
            mErrorTextView.setTypeface(mActivity.typeface);
        }
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void changeSearchQuery(String searchQuery) {
        mSubscribedUserViewModel.setSearchQuery(searchQuery);
    }
}
