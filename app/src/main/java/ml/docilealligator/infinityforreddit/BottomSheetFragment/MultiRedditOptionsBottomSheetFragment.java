package ml.docilealligator.infinityforreddit.BottomSheetFragment;

import android.content.Context;
import android.content.Intent;
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
import ml.docilealligator.infinityforreddit.Activity.EditMultiRedditActivity;
import ml.docilealligator.infinityforreddit.Activity.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiReddit;
import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiRedditOptionsBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final String EXTRA_MULTI_REDDIT = "EMR";

    @BindView(R.id.edit_multi_reddit_text_view_multi_reddit_options_bottom_sheet_fragment)
    TextView editMultiRedditTextView;
    @BindView(R.id.delete_multi_reddit_text_view_multi_reddit_options_bottom_sheet_fragment)
    TextView deleteMultiRedditTextView;
    private SubscribedThingListingActivity subscribedThingListingActivity;

    public MultiRedditOptionsBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_multi_reddit_options_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        MultiReddit multiReddit = getArguments().getParcelable(EXTRA_MULTI_REDDIT);

        editMultiRedditTextView.setOnClickListener(view -> {
            if (multiReddit != null) {
                Intent editIntent = new Intent(subscribedThingListingActivity, EditMultiRedditActivity.class);
                editIntent.putExtra(EditMultiRedditActivity.EXTRA_MULTI_PATH, multiReddit.getPath());
                startActivity(editIntent);
            }
            dismiss();
        });

        deleteMultiRedditTextView.setOnClickListener(view -> {
            subscribedThingListingActivity.deleteMultiReddit(multiReddit);
            dismiss();
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        subscribedThingListingActivity = (SubscribedThingListingActivity) context;
    }
}
