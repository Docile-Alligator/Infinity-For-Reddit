package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.thing.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSortTimeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class SortTimeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_SORT_TYPE = "EST";

    private BaseActivity activity;
    public SortTimeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSortTimeBottomSheetBinding binding = FragmentSortTimeBottomSheetBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        String sortType = getArguments() != null ? getArguments().getString(EXTRA_SORT_TYPE) : null;
        if (sortType == null) {
            dismiss();
            return binding.getRoot();
        }

        binding.hourTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.HOUR));
            dismiss();
        });

        binding.dayTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.DAY));
            dismiss();
        });

        binding.weekTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.WEEK));
            dismiss();
        });

        binding.monthTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.MONTH));
            dismiss();
        });

        binding.yearTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.YEAR));
            dismiss();
        });

        binding.allTimeTextViewSortTimeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.ALL));
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
