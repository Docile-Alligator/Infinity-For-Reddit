package ml.docilealligator.infinityforreddit.BottomSheetFragment;


import android.app.Activity;
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

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.SortTypeSelectionCallback;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostCommentSortTypeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    @BindView(R.id.best_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
    TextView bestTypeTextView;
    @BindView(R.id.confidence_type_text_view_post_comment_sort_type_bottom_sheet_fragment)
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
    private Activity activity;
    public PostCommentSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post_comment_sort_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        bestTypeTextView.setOnClickListener(view -> {
            ((SortTypeSelectionCallback) activity).sortTypeSelected(new SortType(SortType.Type.BEST));
            dismiss();
        });

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

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (Activity) context;
    }
}
