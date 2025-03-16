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
import ml.docilealligator.infinityforreddit.databinding.FragmentSearchUserAndSubredditSortTypeBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchUserAndSubredditSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_FRAGMENT_POSITION = "EFP";
    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    private BaseActivity activity;

    public SearchUserAndSubredditSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    public static SearchUserAndSubredditSortTypeBottomSheetFragment getNewInstance(int fragmentPosition, SortType currentSortType) {
        SearchUserAndSubredditSortTypeBottomSheetFragment fragment = new SearchUserAndSubredditSortTypeBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_FRAGMENT_POSITION, fragmentPosition);
        bundle.putString(EXTRA_CURRENT_SORT_TYPE, currentSortType.getType().fullName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSearchUserAndSubredditSortTypeBottomSheetBinding binding = FragmentSearchUserAndSubredditSortTypeBottomSheetBinding.inflate(inflater, container, false);

        String currentSortType = getArguments().getString(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.RELEVANCE.fullName)) {
            binding.relevanceTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.relevanceTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.ACTIVITY.fullName)) {
            binding.activityTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setCompoundDrawablesRelativeWithIntrinsicBounds(binding.activityTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_check_circle_day_night_24dp), null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        int position = getArguments() != null ? getArguments().getInt(EXTRA_FRAGMENT_POSITION) : -1;
        if(position < 0) {
            dismiss();
            return binding.getRoot();
        }

        binding.relevanceTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).searchUserAndSubredditSortTypeSelected(new SortType(SortType.Type.RELEVANCE), position);
            dismiss();
        });

        binding.activityTypeTextViewSearchUserAndSubredditSortTypeBottomSheetFragment.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).searchUserAndSubredditSortTypeSelected(new SortType(SortType.Type.ACTIVITY), position);
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
