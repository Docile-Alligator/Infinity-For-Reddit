package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.EditMultiRedditActivity;
import ml.docilealligator.infinityforreddit.activities.SubscribedThingListingActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentMultiRedditOptionsBottomSheetBinding;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class MultiRedditOptionsBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_MULTI_REDDIT = "EMR";

    private SubscribedThingListingActivity subscribedThingListingActivity;

    public MultiRedditOptionsBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentMultiRedditOptionsBottomSheetBinding binding = FragmentMultiRedditOptionsBottomSheetBinding.inflate(inflater, container, false);

        MultiReddit multiReddit = getArguments().getParcelable(EXTRA_MULTI_REDDIT);

        binding.copyMultiRedditPathTextViewMultiRedditOptionsBottomSheetFragment.setOnClickListener(view -> {
            if (multiReddit != null) {
                ClipboardManager clipboard = (ClipboardManager) subscribedThingListingActivity.getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData clip = ClipData.newPlainText("simple text", multiReddit.getPath());
                    clipboard.setPrimaryClip(clip);
                    Toast.makeText(subscribedThingListingActivity, multiReddit.getPath(), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(subscribedThingListingActivity, R.string.copy_multi_reddit_path_failed, Toast.LENGTH_SHORT).show();
                }
            }
            dismiss();
        });

        binding.editMultiRedditTextViewMultiRedditOptionsBottomSheetFragment.setOnClickListener(view -> {
            if (multiReddit != null) {
                Intent editIntent = new Intent(subscribedThingListingActivity, EditMultiRedditActivity.class);
                editIntent.putExtra(EditMultiRedditActivity.EXTRA_MULTI_PATH, multiReddit.getPath());
                startActivity(editIntent);
            }
            dismiss();
        });

        binding.deleteMultiRedditTextViewMultiRedditOptionsBottomSheetFragment.setOnClickListener(view -> {
            subscribedThingListingActivity.deleteMultiReddit(multiReddit);
            dismiss();
        });

        if (subscribedThingListingActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), subscribedThingListingActivity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        subscribedThingListingActivity = (SubscribedThingListingActivity) context;
    }
}
