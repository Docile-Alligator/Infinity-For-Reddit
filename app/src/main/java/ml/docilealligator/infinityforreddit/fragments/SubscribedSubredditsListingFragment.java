package ml.docilealligator.infinityforreddit.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
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
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.adapters.SubscribedSubredditsRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentSubscribedSubredditsListingBinding;
import ml.docilealligator.infinityforreddit.subscribedsubreddit.SubscribedSubredditViewModel;
import ml.docilealligator.infinityforreddit.thing.SelectThingReturnKey;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribedSubredditsListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_ACCOUNT_PROFILE_IMAGE_URL = "EAPIU";
    public static final String EXTRA_IS_SUBREDDIT_SELECTION = "EISS";
    public static final String EXTRA_EXTRA_CLEAR_SELECTION = "EECS";

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
    private FragmentSubscribedSubredditsListingBinding binding;

    public SubscribedSubredditsListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSubscribedSubredditsListingBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        if ((mActivity.isImmersiveInterface())) {
            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, false);
                    binding.recyclerViewSubscribedSubredditsListingFragment.setPadding(
                            0, 0, 0, allInsets.bottom
                    );
                    return WindowInsetsCompat.CONSUMED;
                }
            });
            //binding.recyclerViewSubscribedSubredditsListingFragment.setPadding(0, 0, 0, mActivity.getNavBarHeight());
        }/* else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            Resources resources = getResources();
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewSubscribedSubredditsListingFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }*/

        if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setEnabled(false);
        }

        mGlide = Glide.with(this);

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerViewSubscribedSubredditsListingFragment.setLayoutManager(mLinearLayoutManager);

        SubscribedSubredditsRecyclerViewAdapter adapter;
        if (getArguments().getBoolean(EXTRA_IS_SUBREDDIT_SELECTION)) {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRedditDataRoomDatabase,
                    mCustomThemeWrapper, mActivity.accessToken, mActivity.accountName, getArguments().getBoolean(EXTRA_EXTRA_CLEAR_SELECTION),
                    (name, iconUrl, subredditIsUser) -> {
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_NAME, name);
                        returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_SUBREDDIT_OR_USER_ICON, iconUrl);
                        returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, subredditIsUser ? SelectThingReturnKey.THING_TYPE.USER : SelectThingReturnKey.THING_TYPE.SUBREDDIT);
                        mActivity.setResult(Activity.RESULT_OK, returnIntent);
                        mActivity.finish();
                    });
        } else {
            adapter = new SubscribedSubredditsRecyclerViewAdapter(mActivity, mExecutor, mOauthRetrofit, mRedditDataRoomDatabase,
                    mCustomThemeWrapper, mActivity.accessToken, mActivity.accountName);
        }

        binding.recyclerViewSubscribedSubredditsListingFragment.setAdapter(adapter);
        new FastScrollerBuilder(binding.recyclerViewSubscribedSubredditsListingFragment).useMd2Style().build();

        mSubscribedSubredditViewModel = new ViewModelProvider(this,
                new SubscribedSubredditViewModel.Factory(mRedditDataRoomDatabase, mActivity.accountName))
                .get(SubscribedSubredditViewModel.class);
        mSubscribedSubredditViewModel.getAllSubscribedSubreddits().observe(getViewLifecycleOwner(), subscribedSubredditData -> {
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setRefreshing(false);
            if (subscribedSubredditData == null || subscribedSubredditData.size() == 0) {
                binding.recyclerViewSubscribedSubredditsListingFragment.setVisibility(View.GONE);
                binding.noSubscriptionsLinearLayoutSubredditsListingFragment.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.noSubscriptionsImageViewSubredditsListingFragment);
            } else {
                binding.noSubscriptionsLinearLayoutSubredditsListingFragment.setVisibility(View.GONE);
                binding.recyclerViewSubscribedSubredditsListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageViewSubredditsListingFragment);
            }

            if (!mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                adapter.addUser(mActivity.accountName, getArguments().getString(EXTRA_ACCOUNT_PROFILE_IMAGE_URL));
            }
            adapter.setSubscribedSubreddits(subscribedSubredditData);
        });

        mSubscribedSubredditViewModel.getAllFavoriteSubscribedSubreddits().observe(getViewLifecycleOwner(), favoriteSubscribedSubredditData -> {
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setRefreshing(false);
            if (favoriteSubscribedSubredditData != null && favoriteSubscribedSubredditData.size() > 0) {
                binding.noSubscriptionsLinearLayoutSubredditsListingFragment.setVisibility(View.GONE);
                binding.recyclerViewSubscribedSubredditsListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.noSubscriptionsImageViewSubredditsListingFragment);
            }

            adapter.setFavoriteSubscribedSubreddits(favoriteSubscribedSubredditData);
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
        binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setRefreshing(false);
    }

    @Override
    public void applyTheme() {
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setOnRefreshListener(() -> ((SubscribedThingListingActivity) mActivity).loadSubscriptions(true));
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        } else {
            binding.swipeRefreshLayoutSubscribedSubredditsListingFragment.setEnabled(false);
        }
        binding.errorTextViewSubscribedSubredditsListingFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.errorTextViewSubscribedSubredditsListingFragment.setTypeface(mActivity.contentTypeface);
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
