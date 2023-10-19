package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.CommentFilterUsageListingActivity;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsage;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCommentFilterUsageOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentFilterUsageOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT_FILTER_USAGE = "ECFU";

    private CommentFilterUsageListingActivity activity;

    public CommentFilterUsageOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCommentFilterUsageOptionsBottomSheetBinding binding = FragmentCommentFilterUsageOptionsBottomSheetBinding.inflate(inflater, container, false);

        CommentFilterUsage commentFilterUsage = getArguments().getParcelable(EXTRA_COMMENT_FILTER_USAGE);

        binding.editTextViewCommentFilterUsageOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.editCommentFilterUsage(commentFilterUsage);
            dismiss();
        });

        binding.deleteTextViewCommentFilterUsageOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.deleteCommentFilterUsage(commentFilterUsage);
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
        activity = (CommentFilterUsageListingActivity) context;
    }
}