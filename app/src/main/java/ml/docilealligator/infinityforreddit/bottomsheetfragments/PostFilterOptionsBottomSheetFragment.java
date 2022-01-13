package ml.docilealligator.infinityforreddit.bottomsheetfragments;

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
import ml.docilealligator.infinityforreddit.activities.PostFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostFilterOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    @BindView(R.id.edit_text_view_post_filter_options_bottom_sheet_fragment)
    TextView editTextView;
    @BindView(R.id.apply_to_text_view_post_filter_options_bottom_sheet_fragment)
    TextView applyToTextView;
    @BindView(R.id.delete_text_view_post_filter_options_bottom_sheet_fragment)
    TextView deleteTextView;
    public static final String EXTRA_POST_FILTER = "EPF";
    private PostFilterPreferenceActivity activity;

    public PostFilterOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post_filter_options_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        PostFilter postFilter = getArguments().getParcelable(EXTRA_POST_FILTER);

        editTextView.setOnClickListener(view -> {
            activity.editPostFilter(postFilter);
            dismiss();
        });

        applyToTextView.setOnClickListener(view -> {
            activity.applyPostFilterTo(postFilter);
            dismiss();
        });

        deleteTextView.setOnClickListener(view -> {
            activity.deletePostFilter(postFilter);
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
        activity = (PostFilterPreferenceActivity) context;
    }
}