package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentFabMoreOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FABMoreOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_ANONYMOUS_MODE = "EAM";
    public static final int FAB_OPTION_SUBMIT_POST = 0;
    public static final int FAB_OPTION_REFRESH = 1;
    public static final int FAB_OPTION_CHANGE_SORT_TYPE = 2;
    public static final int FAB_OPTION_CHANGE_POST_LAYOUT = 3;
    public static final int FAB_OPTION_SEARCH = 4;
    public static final int FAB_OPTION_GO_TO_SUBREDDIT = 5;
    public static final int FAB_OPTION_GO_TO_USER = 6;
    public static final int FAB_RANDOM = 7;
    public static final int FAB_HIDE_READ_POSTS = 8;
    public static final int FAB_FILTER_POSTS = 9;
    public static final int FAB_GO_TO_TOP = 10;

    private FABOptionSelectionCallback activity;

    public FABMoreOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentFabMoreOptionsBottomSheetBinding binding = FragmentFabMoreOptionsBottomSheetBinding.inflate(inflater, container, false);

        if (getArguments() != null && getArguments().getBoolean(EXTRA_ANONYMOUS_MODE, false)) {
            binding.submitPostTextViewFabMoreOptionsBottomSheetFragment.setVisibility(View.GONE);
            binding.hideReadPostsTextViewFabMoreOptionsBottomSheetFragment.setVisibility(View.GONE);
        } else {
            binding.submitPostTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
                activity.fabOptionSelected(FAB_OPTION_SUBMIT_POST);
                dismiss();
            });

            binding.hideReadPostsTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
                activity.fabOptionSelected(FAB_HIDE_READ_POSTS);
                dismiss();
            });
        }

        binding.refreshTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_REFRESH);
            dismiss();
        });

        binding.changeSortTypeTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_CHANGE_SORT_TYPE);
            dismiss();
        });

        binding.changePostLayoutTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_CHANGE_POST_LAYOUT);
            dismiss();
        });

        binding.searchTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_SEARCH);
            dismiss();
        });

        binding.goToSubredditTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_GO_TO_SUBREDDIT);
            dismiss();
        });

        binding.goToUserTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_GO_TO_USER);
            dismiss();
        });

        binding.randomTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_RANDOM);
            dismiss();
        });

        binding.filterPostsTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_FILTER_POSTS);
            dismiss();
        });

        binding.goToTopTextViewFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_GO_TO_TOP);
            dismiss();
        });

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) baseActivity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((BaseActivity) baseActivity).typeface);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (FABOptionSelectionCallback) context;
    }

    public interface FABOptionSelectionCallback {
        void fabOptionSelected(int option);
    }
}