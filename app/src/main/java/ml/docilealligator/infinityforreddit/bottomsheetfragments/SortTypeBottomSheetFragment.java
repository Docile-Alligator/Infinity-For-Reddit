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
public class SortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_NO_BEST_TYPE = "ENBT";
    @BindView(R.id.best_type_text_view_sort_type_bottom_sheet_fragment)
    TextView bestTypeTextView;
    @BindView(R.id.hot_type_text_view_sort_type_bottom_sheet_fragment)
    TextView hotTypeTextView;
    @BindView(R.id.new_type_text_view_sort_type_bottom_sheet_fragment)
    TextView newTypeTextView;
    @BindView(R.id.rising_type_text_view_sort_type_bottom_sheet_fragment)
    TextView risingTypeTextView;
    @BindView(R.id.top_type_text_view_sort_type_bottom_sheet_fragment)
    TextView topTypeTextView;
    @BindView(R.id.controversial_type_text_view_sort_type_bottom_sheet_fragment)
    TextView controversialTypeTextView;
    private BaseActivity activity;
    public SortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sort_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        if (getArguments() == null || getArguments().getBoolean(EXTRA_NO_BEST_TYPE)) {
            bestTypeTextView.setVisibility(View.GONE);
        } else {
            bestTypeTextView.setOnClickListener(view -> {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.BEST));
                dismiss();
            });
        }

        hotTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.HOT));
            dismiss();
        });

        newTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            dismiss();
        });

        risingTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.RISING));
            dismiss();
        });

        topTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            dismiss();
        });

        controversialTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.CONTROVERSIAL.name());
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
        activity = (BaseActivity) context;
    }
}
