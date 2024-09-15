package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentPostCommentSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostCommentSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;

    public PostCommentSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static PostCommentSortTypeBottomSheetFragment getNewInstance(SortType.Type currentSortType) {
        PostCommentSortTypeBottomSheetFragment fragment = new PostCommentSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_CURRENT_SORT_TYPE, currentSortType);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentPostCommentSortTypeBottomSheetBinding binding = FragmentPostCommentSortTypeBottomSheetBinding.inflate(inflater, container, false);

        SortType.Type currentSortType = (SortType.Type) getArguments().getSerializable(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.BEST) || currentSortType.equals(SortType.Type.CONFIDENCE)) {
            binding.bestTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.bestTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP)) {
            binding.topTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW)) {
            binding.newTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.CONTROVERSIAL)) {
            binding.controversialTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.controversialTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.OLD)) {
            binding.oldTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.oldTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RANDOM)) {
            binding.randomTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.randomTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.QA)) {
            binding.qaTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.qaTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.LIVE)) {
            binding.liveTypeTextViewPostCommentSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.liveTypeTextViewPostCommentSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        binding.bestTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONFIDENCE));
            dismiss();
        });

        binding.topTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.TOP));
            dismiss();
        });

        binding.newTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        binding.controversialTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONTROVERSIAL));
            dismiss();
        });

        binding.oldTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.OLD));
            dismiss();
        });

        binding.randomTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.RANDOM));
            dismiss();
        });

        binding.qaTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.QA));
            dismiss();
        });

        binding.liveTypeTextViewPostCommentSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.LIVE));
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
}
