package ml.docilealligator.infinityforreddit.BottomSheetFragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;

public class FABMoreOptionsBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final int FAB_OPTION_SUBMIT_POST = 0;
    public static final int FAB_OPTION_REFRESH = 1;
    public static final int FAB_OPTION_CHANGE_SORT_TYPE = 2;
    public static final int FAB_OPTION_CHANGE_POST_LAYOUT = 3;
    public static final int FAB_OPTION_SEARCH = 4;

    @BindView(R.id.submit_post_text_view_fab_more_options_bottom_sheet_fragment)
    TextView submitPostTextView;
    @BindView(R.id.refresh_text_view_fab_more_options_bottom_sheet_fragment)
    TextView refreshTextView;
    @BindView(R.id.change_sort_type_text_view_fab_more_options_bottom_sheet_fragment)
    TextView changeSortTypeTextView;
    @BindView(R.id.change_post_layout_text_view_fab_more_options_bottom_sheet_fragment)
    TextView changePostLayoutTextView;
    @BindView(R.id.search_text_view_fab_more_options_bottom_sheet_fragment)
    TextView searchTextView;
    private FABOptionSelectionCallback activity;

    public FABMoreOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_fab_more_options_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        submitPostTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_SUBMIT_POST);
            dismiss();
        });

        refreshTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_REFRESH);
            dismiss();
        });

        changeSortTypeTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_CHANGE_SORT_TYPE);
            dismiss();
        });

        changePostLayoutTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_CHANGE_POST_LAYOUT);
            dismiss();
        });

        searchTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_SEARCH);
            dismiss();
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (FABOptionSelectionCallback) context;
    }

    public interface FABOptionSelectionCallback {
        void fabOptionSelected(int option);
    }
}