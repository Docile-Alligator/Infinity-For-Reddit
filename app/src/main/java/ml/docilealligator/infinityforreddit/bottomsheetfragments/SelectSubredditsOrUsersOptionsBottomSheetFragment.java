package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.SelectedSubredditsAndUsersActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SelectSubredditsOrUsersOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private SelectedSubredditsAndUsersActivity activity;

    public SelectSubredditsOrUsersOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding binding = FragmentSelectSubredditsOrUsersOptionsBottomSheetBinding.inflate(inflater, container, false);

        binding.selectSubredditsTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            activity.selectSubreddits();
            dismiss();
        });

        binding.selectUsersTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            activity.selectUsers();
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
        activity = (SelectedSubredditsAndUsersActivity) context;
    }
}