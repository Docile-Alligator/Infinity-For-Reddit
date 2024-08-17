package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.PostFilterUsageListingActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentNewPostFilterUsageBottomSheetBinding;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class NewPostFilterUsageBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private PostFilterUsageListingActivity activity;

    public NewPostFilterUsageBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentNewPostFilterUsageBottomSheetBinding binding = FragmentNewPostFilterUsageBottomSheetBinding.inflate(inflater, container, false);

        binding.homeTextViewNewPostFilterUsageBottomSheetFragment.setOnClickListener(view -> {
            activity.newPostFilterUsage(PostFilterUsage.HOME_TYPE);
            dismiss();
        });

        binding.subredditTextViewNewPostFilterUsageBottomSheetFragment.setOnClickListener(view -> {
            activity.newPostFilterUsage(PostFilterUsage.SUBREDDIT_TYPE);
            dismiss();
        });

        binding.userTextViewNewPostFilterUsageBottomSheetFragment.setOnClickListener(view -> {
            activity.newPostFilterUsage(PostFilterUsage.USER_TYPE);
            dismiss();
        });

        binding.multiredditTextViewNewPostFilterUsageBottomSheetFragment.setOnClickListener(view -> {
            activity.newPostFilterUsage(PostFilterUsage.MULTIREDDIT_TYPE);
            dismiss();
        });

        binding.searchTextViewNewPostFilterUsageBottomSheetFragment.setOnClickListener(view -> {
            activity.newPostFilterUsage(PostFilterUsage.SEARCH_TYPE);
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (PostFilterUsageListingActivity) context;
    }
}