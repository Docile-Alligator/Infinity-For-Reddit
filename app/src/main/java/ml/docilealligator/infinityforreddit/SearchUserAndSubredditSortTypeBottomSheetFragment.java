package ml.docilealligator.infinityforreddit;


import android.app.Activity;
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
public class SearchUserAndSubredditSortTypeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    interface SearchUserAndSubredditSortTypeSelectionCallback {
        void searchUserAndSubredditSortTypeSelected(String sortType, int fragmentPosition);
    }

    static final String EXTRA_FRAGMENT_POSITION = "EFP";

    @BindView(R.id.relevance_type_text_view_search_user_and_subreddit_sort_type_bottom_sheet_fragment) TextView relevanceTypeTextView;
    @BindView(R.id.activity_type_text_view_search_user_and_subreddit_sort_type_bottom_sheet_fragment) TextView activityTypeTextView;

    public SearchUserAndSubredditSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_search_user_and_subreddit_sort_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        Activity activity = getActivity();

        int position = getArguments().getInt(EXTRA_FRAGMENT_POSITION);

        relevanceTypeTextView.setOnClickListener(view -> {
            ((SearchUserAndSubredditSortTypeSelectionCallback) activity).searchUserAndSubredditSortTypeSelected(PostDataSource.SORT_TYPE_RELEVANCE, position);
            dismiss();
        });

        activityTypeTextView.setOnClickListener(view -> {
            ((SearchUserAndSubredditSortTypeSelectionCallback) activity).searchUserAndSubredditSortTypeSelected(PostDataSource.SORT_TYPE_HOT, position);
            dismiss();
        });

        return rootView;
    }

}
