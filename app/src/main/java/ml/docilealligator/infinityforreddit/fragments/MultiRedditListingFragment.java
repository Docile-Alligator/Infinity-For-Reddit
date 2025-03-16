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
import androidx.recyclerview.widget.RecyclerView;

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
import ml.docilealligator.infinityforreddit.activities.ViewMultiRedditDetailActivity;
import ml.docilealligator.infinityforreddit.adapters.MultiRedditListingRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.MultiRedditOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.FragmentMultiRedditListingBinding;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditViewModel;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Retrofit;

public class MultiRedditListingFragment extends Fragment implements FragmentCommunicator {

    public static final String EXTRA_IS_MULTIREDDIT_SELECTION = "EIMS";

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
    @Inject
    Executor mExecutor;

    public MultiRedditViewModel mMultiRedditViewModel;
    private BaseActivity mActivity;
    private RequestManager mGlide;
    private LinearLayoutManagerBugFixed mLinearLayoutManager;
    private FragmentMultiRedditListingBinding binding;

    public MultiRedditListingFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMultiRedditListingBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        applyTheme();

        if ((mActivity != null && ((BaseActivity) mActivity).isImmersiveInterface())) {
            binding.recyclerViewMultiRedditListingFragment.setPadding(0, 0, 0, ((BaseActivity) mActivity).getNavBarHeight());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && mSharedPreferences.getBoolean(SharedPreferencesUtils.IMMERSIVE_INTERFACE_KEY, true)) {
            Resources resources = getResources();
            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                binding.recyclerViewMultiRedditListingFragment.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
            }
        }

        boolean isGettingMultiredditInfo = getArguments().getBoolean(EXTRA_IS_MULTIREDDIT_SELECTION, false);

        if (mActivity.accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
            binding.swipeRefreshLayoutMultiRedditListingFragment.setEnabled(false);
        }

        mGlide = Glide.with(this);

        mLinearLayoutManager = new LinearLayoutManagerBugFixed(mActivity);
        binding.recyclerViewMultiRedditListingFragment.setLayoutManager(mLinearLayoutManager);
        MultiRedditListingRecyclerViewAdapter adapter = new MultiRedditListingRecyclerViewAdapter(mActivity,
                mExecutor, mOauthRetrofit, mRedditDataRoomDatabase, mCustomThemeWrapper, mActivity.accessToken,
                mActivity.accountName, new MultiRedditListingRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onClick(MultiReddit multiReddit) {
                if (isGettingMultiredditInfo) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(SelectThingReturnKey.RETRUN_EXTRA_MULTIREDDIT, multiReddit);
                    returnIntent.putExtra(SelectThingReturnKey.RETURN_EXTRA_THING_TYPE, SelectThingReturnKey.THING_TYPE.MULTIREDDIT);
                    mActivity.setResult(Activity.RESULT_OK, returnIntent);
                    mActivity.finish();
                } else {
                    Intent intent = new Intent(mActivity, ViewMultiRedditDetailActivity.class);
                    intent.putExtra(ViewMultiRedditDetailActivity.EXTRA_MULTIREDDIT_DATA, multiReddit);
                    mActivity.startActivity(intent);
                }
            }

            @Override
            public void onLongClick(MultiReddit multiReddit) {
                if (!isGettingMultiredditInfo) {
                    showOptionsBottomSheetFragment(multiReddit);
                }
            }
        });
        binding.recyclerViewMultiRedditListingFragment.setAdapter(adapter);
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.recyclerViewMultiRedditListingFragment.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    if (dy > 0) {
                        ((SubscribedThingListingActivity) mActivity).hideFabInMultiredditTab();
                    } else {
                        ((SubscribedThingListingActivity) mActivity).showFabInMultiredditTab();
                    }
                }
            });
        }
        new FastScrollerBuilder(binding.recyclerViewMultiRedditListingFragment).useMd2Style().build();

        mMultiRedditViewModel = new ViewModelProvider(this,
                new MultiRedditViewModel.Factory(mRedditDataRoomDatabase, mActivity.accountName))
                .get(MultiRedditViewModel.class);

        mMultiRedditViewModel.getAllMultiReddits().observe(getViewLifecycleOwner(), multiReddits -> {
            if (multiReddits == null || multiReddits.size() == 0) {
                binding.recyclerViewMultiRedditListingFragment.setVisibility(View.GONE);
                binding.fetchMultiRedditListingInfoLinearLayoutMultiRedditListingFragment.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.error_image).into(binding.fetchMultiRedditListingInfoImageViewMultiRedditListingFragment);
            } else {
                binding.fetchMultiRedditListingInfoLinearLayoutMultiRedditListingFragment.setVisibility(View.GONE);
                binding.recyclerViewMultiRedditListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.fetchMultiRedditListingInfoImageViewMultiRedditListingFragment);
            }
            adapter.setMultiReddits(multiReddits);
        });

        mMultiRedditViewModel.getAllFavoriteMultiReddits().observe(getViewLifecycleOwner(), favoriteMultiReddits -> {
            if (favoriteMultiReddits != null && favoriteMultiReddits.size() > 0) {
                binding.fetchMultiRedditListingInfoLinearLayoutMultiRedditListingFragment.setVisibility(View.GONE);
                binding.recyclerViewMultiRedditListingFragment.setVisibility(View.VISIBLE);
                mGlide.clear(binding.fetchMultiRedditListingInfoImageViewMultiRedditListingFragment);
            }
            adapter.setFavoriteMultiReddits(favoriteMultiReddits);
        });

        return binding.getRoot();
    }

    private void showOptionsBottomSheetFragment(MultiReddit multiReddit) {
        MultiRedditOptionsBottomSheetFragment fragment = new MultiRedditOptionsBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(MultiRedditOptionsBottomSheetFragment.EXTRA_MULTI_REDDIT, multiReddit);
        fragment.setArguments(bundle);
        fragment.show(mActivity.getSupportFragmentManager(), fragment.getTag());
    }

    public void goBackToTop() {
        if (mLinearLayoutManager != null) {
            mLinearLayoutManager.scrollToPositionWithOffset(0, 0);
        }
    }

    public void changeSearchQuery(String searchQuery) {
        mMultiRedditViewModel.setSearchQuery(searchQuery);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void applyTheme() {
        if (mActivity instanceof SubscribedThingListingActivity) {
            binding.swipeRefreshLayoutMultiRedditListingFragment.setOnRefreshListener(() -> ((SubscribedThingListingActivity) mActivity).loadSubscriptions(true));
            binding.swipeRefreshLayoutMultiRedditListingFragment.setProgressBackgroundColorSchemeColor(mCustomThemeWrapper.getCircularProgressBarBackground());
            binding.swipeRefreshLayoutMultiRedditListingFragment.setColorSchemeColors(mCustomThemeWrapper.getColorAccent());
        } else {
            binding.swipeRefreshLayoutMultiRedditListingFragment.setEnabled(false);
        }

        binding.fetchMultiRedditListingInfoTextViewMultiRedditListingFragment.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
        if (mActivity.typeface != null) {
            binding.fetchMultiRedditListingInfoTextViewMultiRedditListingFragment.setTypeface(mActivity.typeface);
        }
    }

    @Override
    public void stopRefreshProgressbar() {
        binding.swipeRefreshLayoutMultiRedditListingFragment.setRefreshing(false);
    }
}