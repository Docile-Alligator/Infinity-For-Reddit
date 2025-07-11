package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.activities.PostGalleryActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSelectOrCaptureImageBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SelectOrCaptureImageBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    private PostGalleryActivity mActivity;

    public SelectOrCaptureImageBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentSelectOrCaptureImageBottomSheetBinding binding = FragmentSelectOrCaptureImageBottomSheetBinding.inflate(inflater, container, false);

        binding.selectImageTextViewSelectOrCaptureImageBottomSheetFragment.setOnClickListener(view -> {
            mActivity.selectImage();
            dismiss();
        });

        binding.captureImageTextViewSelectOrCaptureImageBottomSheetFragment.setOnClickListener(view -> {
            mActivity.captureImage();
            dismiss();
        });

        if (mActivity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), mActivity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (PostGalleryActivity) context;
    }
}