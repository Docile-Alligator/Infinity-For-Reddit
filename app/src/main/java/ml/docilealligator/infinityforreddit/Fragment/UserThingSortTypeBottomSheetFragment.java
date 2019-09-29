package ml.docilealligator.infinityforreddit.Fragment;


import android.app.Activity;
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
import ml.docilealligator.infinityforreddit.PostDataSource;
import ml.docilealligator.infinityforreddit.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserThingSortTypeBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public interface UserThingSortTypeSelectionCallback {
        void userThingSortTypeSelected(String sortType);
    }

    static final String EXTRA_NO_BEST_TYPE = "ENBT";

    @BindView(R.id.new_type_text_view_user_thing_sort_type_bottom_sheet_fragment) TextView newTypeTextView;
    @BindView(R.id.hot_type_text_view_user_thing_sort_type_bottom_sheet_fragment) TextView hotTypeTextView;
    @BindView(R.id.top_type_text_view_user_thing_sort_type_bottom_sheet_fragment) TextView topTypeTextView;
    @BindView(R.id.controversial_type_text_view_user_thing_sort_type_bottom_sheet_fragment) TextView controversialTypeTextView;

    public UserThingSortTypeBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_thing_sort_type_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        Activity activity = getActivity();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        newTypeTextView.setOnClickListener(view -> {
            if(activity != null) {
                ((UserThingSortTypeSelectionCallback) activity).userThingSortTypeSelected(PostDataSource.SORT_TYPE_NEW);
            }
            dismiss();
        });

        hotTypeTextView.setOnClickListener(view -> {
            if(activity != null) {
                ((UserThingSortTypeSelectionCallback) activity).userThingSortTypeSelected(PostDataSource.SORT_TYPE_HOT);
            }
            dismiss();
        });

        topTypeTextView.setOnClickListener(view -> {
            if(activity != null) {
                ((UserThingSortTypeSelectionCallback) activity).userThingSortTypeSelected(PostDataSource.SORT_TYPE_TOP);
            }
            dismiss();
        });

        controversialTypeTextView.setOnClickListener(view -> {
            if(activity != null) {
                ((UserThingSortTypeSelectionCallback) activity).userThingSortTypeSelected(PostDataSource.SORT_TYPE_CONTROVERSIAL);
            }
            dismiss();
        });

        return rootView;
    }

}
