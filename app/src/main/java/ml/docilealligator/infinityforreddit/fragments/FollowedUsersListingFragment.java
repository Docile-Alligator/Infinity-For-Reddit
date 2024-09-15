package ml.docilealligator.infinityforreddit.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import me.zhanghai.android.fastscroll.FastScrollerBuilder;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.FollowedUsersRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentFollowedUsersListingBinding;
import ml.docilealligator.infinityforreddit.subscribeduser.SubscribedUserViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class FollowedUsersListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_IS_USER_SELECTION = "EIUS";

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
    private FragmentFollowedUsersListingBinding binding;

    public FollowedUsersListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFollowedUsersListingBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        Resources resources = getResources();

        if ((mActivity instanceof BaseActivity && ((BaseActivity) mActivity).isImmersiveInterface())) {
            binding.recyclerViewFollowedUsersListingFragment.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewFollowedUsersListingFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        mGlide = Glide.with(this);

        if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setEnabled(false);
        }
        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerViewFollowedUsersListingFragment.setLayoutManager(mLinearLayoutManager);
        FollowedUsersRecyclerViewAdapter adapter = new FollowedUsersRecyclerViewAdapter(mActivity,
                mExecutor, mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, mActivity.accessToken,
                mActivity.accountName, subscribedUserData -> {
                    if (getArguments().getBoolean(EXTRA_IS_USER_SELECTION)) {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, subscribedUserData.getName());
                        returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.USER);
                        mActivity.setResult(Activity.RESULT_OK, returnIntent);
                        mActivity.finish();
                    } else {
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, subscribedUserData.getName());
                        mActivity.startActivity(intent);
                    }
                });
        binding.recyclerViewFollowedUsersListingFragment.setAdapter(adapter);
        new FastScrollerBuilder(binding.recyclerViewFollowedUsersListingFragment).useMd2Style().build();

        mSubscribedUserViewModel = new ViewModelProvider(this,
                new SubscribedUserViewModel.Factory(mRedditDataRoomDatabase, mActivity.accountName))
                .get(SubscribedUserViewModel.class);

        mSubscribedUserViewModel.getAllSubscribedUsers().observe(getViewLifecycleOwner(), subscribedUserData -> {
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setRefreshing(false);
            if (subscribedUserData == null || subscribedUserData.size() == 0) {
                binding.recyclerViewFollowedUsersListingFragment.setVisibility(View.GONE);
                binding.noSubscriptionsLinearLayoutFollowedUsersListingFragment.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.noSubscriptionsImageViewFollowedUsersListingFragment);
            } else {
                binding.noSubscriptionsLinearLayoutFollowedUsersListingFragment.setVisibility(View.GONE);
                binding.recyclerViewFollowedUsersListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageViewFollowedUsersListingFragment);
            }
            adapter.setSubscribedUsers(subscribedUserData);
        });

        mSubscribedUserViewModel.getAllFavoriteSubscribedUsers().observe(getViewLifecycleOwner(), favoriteSubscribedUserData -> {
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setRefreshing(false);
            if (favoriteSubscribedUserData != null && favoriteSubscribedUserData.size() > 0) {
                binding.noSubscriptionsLinearLayoutFollowedUsersListingFragment.setVisibility(View.GONE);
                binding.recyclerViewFollowedUsersListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageViewFollowedUsersListingFragment);
            }
            adapter.setFavoriteSubscribedUsers(favoriteSubscribedUserData);
        });

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void stopRefreshProgressbar() {
        binding.swipeRefreshLayoutFollowedUsersListingFragment.setRefreshing(false);
    }

    @Override
    public void applyTheme() {
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setOnRefreshListener(() -> ((SubscribedThingListingActivity) mActivity).loadSubscriptions(true));
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        } else {
            binding.swipeRefreshLayoutFollowedUsersListingFragment.setEnabled(false);
        }
        binding.errorTextViewFollowedUsersListingFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.errorTextViewFollowedUsersListingFragment.setTypeface(mActivity.typeface);
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
