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
public class SortTimeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_SORT_TYPE = "EST";

    @BindView(R.id.hour_text_view_sort_time_bottom_sheet_fragment)
    TextView hourTextView;
    @BindView(R.id.day_text_view_sort_time_bottom_sheet_fragment)
    TextView dayTextView;
    @BindView(R.id.week_text_view_sort_time_bottom_sheet_fragment)
    TextView weekTextView;
    @BindView(R.id.month_text_view_sort_time_bottom_sheet_fragment)
    TextView monthTextView;
    @BindView(R.id.year_text_view_sort_time_bottom_sheet_fragment)
    TextView yearTextView;
    @BindView(R.id.all_time_text_view_sort_time_bottom_sheet_fragment)
    TextView allTimeTextView;
    private BaseActivity activity;
    public SortTimeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sort_time_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        String sortType = getArguments() != null ? getArguments().getString(EXTRA_SORT_TYPE) : null;
        if (sortType == null) {
            dismiss();
            return rootView;
        }

        hourTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.HOUR));
            dismiss();
        });

        dayTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.DAY));
            dismiss();
        });

        weekTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.WEEK));
            dismiss();
        });

        monthTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.MONTH));
            dismiss();
        });

        yearTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.YEAR));
            dismiss();
        });

        allTimeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity)
                    .sortTypeSelected(new SortType(SortType.Type.valueOf(sortType), SortType.Time.ALL));
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
