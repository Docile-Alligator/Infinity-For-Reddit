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
import ml.docilealligator.infinityforreddit.databinding.FragmentSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class SortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_NO_BEST_TYPE = "ENBT";
    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;
    public SortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static SortTypeBottomSheetFragment getNewInstance(boolean isNoBestType, SortType currentSortType) {
        SortTypeBottomSheetFragment fragment = new SortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_NO_BEST_TYPE, isNoBestType);
        bundle.putString(EXTRA_CURRENT_SORT_TYPE, currentSortType.getType().fullName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSortTypeBottomSheetBinding binding = FragmentSortTypeBottomSheetBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        if (getArguments().getBoolean(EXTRA_NO_BEST_TYPE)) {
            binding.bestTypeTextViewSortTypeBottomSheetFragment.setVisibility(View.GONE);
        } else {
            binding.bestTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.BEST));
                dismiss();
            });
        }

        String currentSortType = getArguments().getString(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.BEST.fullName)) {
            binding.bestTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.bestTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.HOT.fullName)) {
            binding.hotTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.hotTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW.fullName)) {
            binding.newTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RISING.fullName)) {
            binding.risingTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.risingTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP.fullName)) {
            binding.topTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.CONTROVERSIAL.fullName)) {
            binding.controversialTypeTextViewSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.controversialTypeTextViewSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        }

        binding.hotTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.HOT));
            dismiss();
        });

        binding.newTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        binding.risingTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.RISING));
            dismiss();
        });

        binding.topTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            dismiss();
        });

        binding.controversialTypeTextViewSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.CONTROVERSIAL.name());
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
        activity = (BaseActivity) context;
    }
}
