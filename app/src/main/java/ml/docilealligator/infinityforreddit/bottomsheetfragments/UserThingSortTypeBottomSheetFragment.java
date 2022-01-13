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
public class UserThingSortTypeBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    @BindView(R.id.new_type_text_view_user_thing_sort_type_bottom_sheet_fragment)
    TextView newTypeTextView;
    @BindView(R.id.hot_type_text_view_user_thing_sort_type_bottom_sheet_fragment)
    TextView hotTypeTextView;
    @BindView(R.id.top_type_text_view_user_thing_sort_type_bottom_sheet_fragment)
    TextView topTypeTextView;
    @BindView(R.id.controversial_type_text_view_user_thing_sort_type_bottom_sheet_fragment)
    TextView controversialTypeTextView;
    private BaseActivity activity;

    public UserThingSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_thing_sort_type_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        newTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.NEW));
            }
            dismiss();
        });

        hotTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.HOT));
            }
            dismiss();
        });

        topTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.TOP.name());
            }
            dismiss();
        });

        controversialTypeTextView.setOnClickListener(view -> {
            if (activity != null) {
                ((SortTypeSelectionCallback) activity).sortTypeSelected(SortType.Type.CONTROVERSIAL.name());
            }
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
