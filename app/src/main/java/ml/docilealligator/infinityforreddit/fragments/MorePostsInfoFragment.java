package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.LoadingMorePostsStatus;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.FragmentMorePostsInfoBinding;

public class MorePostsInfoFragment extends Fragment {

    public static final String EXTRA_STATUS = "ES";

    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private FragmentMorePostsInfoBinding binding;
    private ViewPostDetailActivity mActivity;
    @LoadingMorePostsStatus
    int status;

    public MorePostsInfoFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ((Infinity) mActivity.getApplication()).getAppComponent().inject(this);

        binding = FragmentMorePostsInfoBinding.inflate(inflater, container, false);

        applyTheme();

        setStatus(getArguments().getInt(EXTRA_STATUS, LoadingMorePostsStatus.LOADING));

        binding.getRoot().setOnClickListener(view -> {
            if (status == LoadingMorePostsStatus.FAILED) {
                mActivity.fetchMorePosts(true);
            }
        });

        return binding.getRoot();
    }

    public void setStatus(@LoadingMorePostsStatus int status) {
        this.status = status;
        switch (status) {
            case LoadingMorePostsStatus.NOT_LOADING:
                binding.progressBarViewMorePostsInfoFragment.setVisibility(View.GONE);
                break;
            case LoadingMorePostsStatus.LOADING:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.loading);
                binding.progressBarViewMorePostsInfoFragment.setVisibility(View.VISIBLE);
                break;
            case LoadingMorePostsStatus.FAILED:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.load_more_posts_failed);
                binding.progressBarViewMorePostsInfoFragment.setVisibility(View.GONE);
                break;
            case LoadingMorePostsStatus.NO_MORE_POSTS:
                binding.infoTextViewMorePostsInfoFragment.setText(R.string.no_more_posts);
                binding.progressBarViewMorePostsInfoFragment.setVisibility(View.GONE);
        }
    }

    private void applyTheme() {
        binding.infoTextViewMorePostsInfoFragment.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (ViewPostDetailActivity) context;
    }
}