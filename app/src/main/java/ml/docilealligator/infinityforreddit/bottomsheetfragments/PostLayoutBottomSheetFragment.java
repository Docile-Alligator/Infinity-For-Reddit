package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.Context;
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
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class PostLayoutBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    @BindView(R.id.card_layout_text_view_post_layout_bottom_sheet_fragment)
    TextView cardLayoutTextView;
    @BindView(R.id.card_layout_2_text_view_post_layout_bottom_sheet_fragment)
    TextView cardLayout2TextView;
    @BindView(R.id.compact_layout_text_view_post_layout_bottom_sheet_fragment)
    TextView compactLayoutTextView;
    @BindView(R.id.gallery_layout_text_view_post_layout_bottom_sheet_fragment)
    TextView galleryLayoutTextView;
    private BaseActivity activity;
    public PostLayoutBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_post_layot_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        cardLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD);
            dismiss();
        });
        compactLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_COMPACT);
            dismiss();
        });
        galleryLayoutTextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_GALLERY);
            dismiss();
        });
        cardLayout2TextView.setOnClickListener(view -> {
            ((PostLayoutSelectionCallback) activity).postLayoutSelected(SharedPreferencesUtils.POST_LAYOUT_CARD_2);
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

    public interface PostLayoutSelectionCallback {
        void postLayoutSelected(int postLayout);
    }

}
