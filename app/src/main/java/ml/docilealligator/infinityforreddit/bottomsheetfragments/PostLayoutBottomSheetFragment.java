package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostLayoutBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostLayoutBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private FragmentPostLayoutBottomSheetBinding binding;
    private BaseActivity activity;
    public PostLayoutBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentPostLayoutBottomSheetBinding.inflate(inflater, container, false);

        binding.cardLayoutTextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD);
            dismiss();
        });
        binding.compactLayoutTextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_COMPACT);
            dismiss();
        });
        binding.compactLayout2TextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_COMPACT_2);
            dismiss();
        });
        binding.galleryLayoutTextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_GALLERY);
            dismiss();
        });
        binding.cardLayout2TextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD_2);
            dismiss();
        });
        binding.cardLayout3TextViewPostLayoutBottomSheetFragment.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD_3);
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
        this.activity = (BaseActivity) context;
    }

    public interface PostLayoutSelectionCallback {
        void postLayoutSelected(int postLayout);
    }

}
