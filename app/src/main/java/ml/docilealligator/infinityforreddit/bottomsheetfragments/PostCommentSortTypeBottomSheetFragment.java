package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostCommentSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_CURRENT_SORT_TYPE = "ECST";

    @BindView(R.id.best_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView confidenceTypeTextView;
    @BindView(R.id.top_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView topTypeTextView;
    @BindView(R.id.new_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView newTypeTextView;
    @BindView(R.id.controversial_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView controversialTypeTextView;
    @BindView(R.id.old_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView oldTypeTextView;
    @BindView(R.id.random_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView randomTypeTextView;
    @BindView(R.id.qa_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView qaTypeTextView;
    @BindView(R.id.live_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView liveTypeTextView;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post_comment_sort_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        SortType.Type currentSortType = (SortType.Type) getArguments().getSerializable(EXTRA_CURRENT_SORT_TYPE);
        if (currentSortType.equals(SortType.Type.BEST) || currentSortType.equals(SortType.Type.CONFIDENCE)) {
            confidenceTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(confidenceTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.TOP)) {
            topTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(topTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.NEW)) {
            newTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(newTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.CONTROVERSIAL)) {
            controversialTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(controversialTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.OLD)) {
            oldTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(oldTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.RANDOM)) {
            randomTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(randomTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.QA)) {
            qaTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(qaTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        } else if (currentSortType.equals(SortType.Type.LIVE)) {
            liveTypeTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(liveTypeTextView.getCompoundDrawablesRelative()[0], null, AppCompatResources.getDrawable(activity, R.drawable.ic_round_check_circle_day_night_24dp), null);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        confidenceTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONFIDENCE));
            dismiss();
        });

        topTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.TOP));
            dismiss();
        });

        newTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        controversialTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.CONTROVERSIAL));
            dismiss();
        });

        oldTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.OLD));
            dismiss();
        });

        randomTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.RANDOM));
            dismiss();
        });

        qaTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.QA));
            dismiss();
        });

        liveTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.LIVE));
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (BaseActivity) context;
    }
}
