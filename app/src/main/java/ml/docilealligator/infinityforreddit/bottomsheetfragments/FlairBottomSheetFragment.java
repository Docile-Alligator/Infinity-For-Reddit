package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetBehavior;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.FlairBottomSheetRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentFlairBottomSheetBinding;
import ml.docilealligator.infinityforreddit.events.FlairSelectedEvent;
import ml.docilealligator.infinityforreddit.subreddit.FetchFlairs;
import ml.docilealligator.infinityforreddit.subreddit.Flair;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;


public class FlairBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_SUBREDDIT_NAME = "ESN";
    public static final String EXTRA_CALLING_FRAGMENT_ID = "ECFI";
    public static final String EXTRA_SHOW_REMOVE_FLAIR_OPTION = "ESRFO";
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private String mSubredditName;
    private BaseActivity mActivity;
    private Handler mHandler;
    private FlairBottomSheetRecyclerViewAdapter mAdapter;
    private FragmentFlairBottomSheetBinding binding;

    public FlairBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentFlairBottomSheetBinding.inflate(inflater, container, false);

        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        if (mActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), mActivity.typeface);
        }

        long callingFragmentId = getArguments().getLong(EXTRA_CALLING_FRAGMENT_ID, -1);
        mAdapter = new FlairBottomSheetRecyclerViewAdapter(mActivity, mCustomThemeWrapper, flair -> {
            if (callingFragmentId <= 0) {
                //PostXXXActivity
                ((FlairSelectionCallback) mActivity).flairSelected(flair);
            } else {
                EventBus.getDefault().post(new FlairSelectedEvent(callingFragmentId, flair));
            }
            dismiss();
        });

        binding.recyclerViewBottomSheetFragment.setAdapter(mAdapter);

        mSubredditName = getArguments().getString(EXTRA_SUBREDDIT_NAME);

        mHandler = new Handler(Looper.getMainLooper());

        fetchFlairs();

        if (getArguments().getBoolean(EXTRA_SHOW_REMOVE_FLAIR_OPTION, false)) {
            binding.removeFlairTextViewFlairBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.removeFlairTextViewFlairBottomSheetFragment.setOnClickListener(view -> {
                EventBus.getDefault().post(new FlairSelectedEvent(callingFragmentId, new Flair("", "", false)));
                dismiss();
            });
        }

        return binding.getRoot();
    }

    private void fetchFlairs() {
        FetchFlairs.fetchFlairsInSubreddit(mExecutor, mHandler, mOauthRetrofit, mActivity.accessToken,
                mSubredditName, new FetchFlairs.FetchFlairsInSubredditListener() {
                    @Override
                    public void fetchSuccessful(List<Flair> flairs) {
                        if (binding == null) {
                            return;
                        }

                        binding.progressBarFlairBottomSheetFragment.setVisibility(View.GONE);
                        if (flairs == null || flairs.isEmpty()) {
                            binding.errorTextViewFlairBottomSheetFragment.setVisibility(View.VISIBLE);
                            binding.errorTextViewFlairBottomSheetFragment.setText(R.string.no_flair);
                        } else {
                            binding.errorTextViewFlairBottomSheetFragment.setVisibility(View.GONE);
                            mAdapter.changeDataset(flairs);
                        }
                    }

                    @Override
                    public void fetchFailed() {
                        if (binding == null) {
                            return;
                        }

                        binding.progressBarFlairBottomSheetFragment.setVisibility(View.GONE);
                        binding.errorTextViewFlairBottomSheetFragment.setVisibility(View.VISIBLE);
                        binding.errorTextViewFlairBottomSheetFragment.setText(R.string.error_loading_flairs);
                        binding.errorTextViewFlairBottomSheetFragment.setOnClickListener(view -> fetchFlairs());
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        View parentView = (View) requireView().getParent();
        BottomSheetBehavior.from(parentView).setState(BottomSheetBehavior.STATE_EXPANDED);
        BottomSheetBehavior.from(parentView).setSkipCollapsed(true);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (BaseActivity) context;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    public interface FlairSelectionCallback {
        void flairSelected(Flair flair);
    }
}
