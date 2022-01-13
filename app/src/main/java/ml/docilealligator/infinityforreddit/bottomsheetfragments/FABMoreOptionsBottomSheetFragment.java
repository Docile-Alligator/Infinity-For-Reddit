package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class FABMoreOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_ANONYMOUS_MODE = "EAM";
    public static final int FAB_OPTION_SUBMIT_POST = 0;
    public static final int FAB_OPTION_REFRESH = 1;
    public static final int FAB_OPTION_CHANGE_SORT_TYPE = 2;
    public static final int FAB_OPTION_CHANGE_POST_LAYOUT = 3;
    public static final int FAB_OPTION_SEARCH = 4;
    public static final int FAB_OPTION_GO_TO_SUBREDDIT = 5;
    public static final int FAB_OPTION_GO_TO_USER = 6;
    public static final int FAB_RANDOM = 7;
    public static final int FAB_HIDE_READ_POSTS = 8;
    public static final int FAB_FILTER_POSTS = 9;

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
    @BindView(R.id.go_to_subreddit_text_view_fab_more_options_bottom_sheet_fragment)
    TextView goToSubredditTextView;
    @BindView(R.id.go_to_user_text_view_fab_more_options_bottom_sheet_fragment)
    TextView goToUserTextView;
    @BindView(R.id.random_text_view_fab_more_options_bottom_sheet_fragment)
    TextView randomTextView;
    @BindView(R.id.hide_read_posts_text_view_fab_more_options_bottom_sheet_fragment)
    TextView hideReadPostsTextView;
    @BindView(R.id.filter_posts_text_view_fab_more_options_bottom_sheet_fragment)
    TextView filterPostsTextView;
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

        if (getArguments() != null && getArguments().getBoolean(EXTRA_ANONYMOUS_MODE, false)) {
            submitPostTextView.setVisibility(View.GONE);
            hideReadPostsTextView.setVisibility(View.GONE);
        } else {
            submitPostTextView.setOnClickListener(view -> {
                activity.fabOptionSelected(FAB_OPTION_SUBMIT_POST);
                dismiss();
            });

            hideReadPostsTextView.setOnClickListener(view -> {
                activity.fabOptionSelected(FAB_HIDE_READ_POSTS);
                dismiss();
            });
        }

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

        goToSubredditTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_GO_TO_SUBREDDIT);
            dismiss();
        });

        goToUserTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_OPTION_GO_TO_USER);
            dismiss();
        });

        randomTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_RANDOM);
            dismiss();
        });

        filterPostsTextView.setOnClickListener(view -> {
            activity.fabOptionSelected(FAB_FILTER_POSTS);
            dismiss();
        });

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) baseActivity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((BaseActivity) baseActivity).typeface);
            }
        }

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