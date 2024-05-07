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
import ml.docilealligator.infinityforreddit.databinding.FragmentFilteredThingFabMoreOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FilteredThingFABMoreOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final int FAB_OPTION_FILTER = 0;
    public static final int FAB_OPTION_HIDE_READ_POSTS = 1;

    private FABOptionSelectionCallback activity;

    public FilteredThingFABMoreOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentFilteredThingFabMoreOptionsBottomSheetBinding binding =
                FragmentFilteredThingFabMoreOptionsBottomSheetBinding.inflate(inflater, container, false);

        binding.filterTextViewFilteredThingFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_FILTER);
            dismiss();
        });

        binding.hideReadPostsTextViewFilteredThingFabMoreOptionsBottomSheetFragment.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_HIDE_READ_POSTS);
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
        activity = (FABOptionSelectionCallback) context;
    }

    public interface FABOptionSelectionCallback {
        void fabOptionSelected(int option);
    }
}