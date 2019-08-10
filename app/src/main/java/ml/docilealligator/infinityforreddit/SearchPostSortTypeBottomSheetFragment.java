package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class SearchPostSortTypeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    interface SearchSortTypeSelectionCallback {
        void searchSortTypeSelected(String sortType);
    }

    @BindView(R.id.relevance_type_text_view_search_sort_type_bottom_sheet_fragment) TextView relevanceTypeTextView;
    @BindView(R.id.hot_type_text_view_search_sort_type_bottom_sheet_fragment) TextView hotTypeTextView;
    @BindView(R.id.top_type_text_view_search_sort_type_bottom_sheet_fragment) TextView topTypeTextView;
    @BindView(R.id.new_type_text_view_search_sort_type_bottom_sheet_fragment) TextView newTypeTextView;
    @BindView(R.id.comments_type_text_view_search_sort_type_bottom_sheet_fragment) TextView commentsTypeTextView;

    public SearchPostSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_post_sort_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        Activity activity = getActivity();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        relevanceTypeTextView.setOnClickListener(view -> {
            ((SearchSortTypeSelectionCallback) activity).searchSortTypeSelected(PostDataSource.SORT_TYPE_RELEVANCE);
            dismiss();
        });

        hotTypeTextView.setOnClickListener(view -> {
            ((SearchSortTypeSelectionCallback) activity).searchSortTypeSelected(PostDataSource.SORT_TYPE_HOT);
            dismiss();
        });

        topTypeTextView.setOnClickListener(view -> {
            ((SearchSortTypeSelectionCallback) activity).searchSortTypeSelected(PostDataSource.SORT_TYPE_TOP);
            dismiss();
        });

        newTypeTextView.setOnClickListener(view -> {
            ((SearchSortTypeSelectionCallback) activity).searchSortTypeSelected(PostDataSource.SORT_TYPE_NEW);
            dismiss();
        });

        commentsTypeTextView.setOnClickListener(view -> {
            ((SearchSortTypeSelectionCallback) activity).searchSortTypeSelected(PostDataSource.SORT_TYPE_COMMENTS);
            dismiss();
        });

        return rootView;
    }

}
