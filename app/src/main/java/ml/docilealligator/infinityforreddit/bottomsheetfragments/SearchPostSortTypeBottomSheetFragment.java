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
import ml.docilealligator.infinityforreddit.databinding.FragmentSearchPostSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPostSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;
    public SearchPostSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static SearchPostSortTypeBottomSheetFragment getNewInstance(SortType currentSortType) {
        SearchPostSortTypeBottomSheetFragment fragment = new SearchPostSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_CURRENT_SORT_TYPE, currentSortType.getType().fullName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSearchPostSortTypeBottomSheetBinding binding = FragmentSearchPostSortTypeBottomSheetBinding.inflate(inflater, container, false);

        String currentSortType = getArguments().getString(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.RELEVANCE.fullName)) {
            binding.relevanceTypeTextViewSearchSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.relevanceTypeTextViewSearchSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.HOT.fullName)) {
            binding.hotTypeTextViewSearchSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.hotTypeTextViewSearchSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP.fullName)) {
            binding.topTypeTextViewSearchSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.topTypeTextViewSearchSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW.fullName)) {
            binding.newTypeTextViewSearchSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.newTypeTextViewSearchSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RISING.fullName)) {
            binding.commentsTypeTextViewSearchSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.commentsTypeTextViewSearchSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        binding.relevanceTypeTextViewSearchSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.RELEVANCE.name());
            dismiss();
        });

        binding.hotTypeTextViewSearchSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.HOT.name());
            dismiss();
        });

        binding.topTypeTextViewSearchSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            dismiss();
        });

        binding.newTypeTextViewSearchSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        binding.commentsTypeTextViewSearchSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.COMMENTS.name());
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
