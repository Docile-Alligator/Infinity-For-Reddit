package ml.docilealligator.infinityforreddit;


import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * A simple {@link Fragment} subclass.
 */
public class PostTypeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    interface PostTypeSelectionCallback {
        void postTypeSelected(int postType);
    }

    static final int TYPE_TEXT = 0;
    static final int TYPE_LINK = 1;
    static final int TYPE_IMAGE = 2;
    static final int TYPE_VIDEO = 3;

    @BindView(R.id.text_type_linear_layout_post_type_bottom_sheet_fragment) LinearLayout textTypeLinearLayout;
    @BindView(R.id.link_type_linear_layout_post_type_bottom_sheet_fragment) LinearLayout linkTypeLinearLayout;
    @BindView(R.id.image_type_linear_layout_post_type_bottom_sheet_fragment) LinearLayout imageTypeLinearLayout;
    @BindView(R.id.video_type_linear_layout_post_type_bottom_sheet_fragment) LinearLayout videoTypeLinearLayout;

    public PostTypeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_post_type_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        Activity activity = getActivity();

        textTypeLinearLayout.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_TEXT);
            dismiss();
        });

        linkTypeLinearLayout.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_LINK);
            dismiss();
        });

        imageTypeLinearLayout.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_IMAGE);
            dismiss();
        });

        videoTypeLinearLayout.setOnClickListener(view -> {
            ((PostTypeSelectionCallback) activity).postTypeSelected(TYPE_VIDEO);
            dismiss();
        });

        return rootView;
    }

}
