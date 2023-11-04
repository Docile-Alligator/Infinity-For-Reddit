package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.CommentFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCommentFilterOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class CommentFilterOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT_FILTER = "ECF";
    private CommentFilterPreferenceActivity activity;

    public CommentFilterOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentCommentFilterOptionsBottomSheetBinding binding = FragmentCommentFilterOptionsBottomSheetBinding.inflate(inflater, container, false);

        CommentFilter commentFilter = getArguments().getParcelable(EXTRA_COMMENT_FILTER);

        binding.editTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.editCommentFilter(commentFilter);
            dismiss();
        });

        binding.applyToTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.applyCommentFilterTo(commentFilter);
            dismiss();
        });

        binding.deleteTextViewCommentFilterOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.deleteCommentFilter(commentFilter);
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
        activity = (CommentFilterPreferenceActivity) context;
    }
}