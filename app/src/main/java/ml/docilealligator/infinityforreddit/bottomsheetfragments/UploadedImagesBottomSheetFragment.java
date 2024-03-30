package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.UploadedImagesRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentUploadedImagesBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UploadedImagesBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_UPLOADED_IMAGES = "EUI";

    private UploadedImagesRecyclerViewAdapter adapter;
    private UploadImageEnabledActivity activity;

    public UploadedImagesBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentUploadedImagesBottomSheetBinding binding = FragmentUploadedImagesBottomSheetBinding.inflate(inflater, container, false);

        binding.getRoot().setNestedScrollingEnabled(true);

        binding.uploadButtonUploadedImagesBottomSheetFragment.setOnClickListener(view -> {
            activity.uploadImage();
            dismiss();
        });

        binding.captureButtonUploadedImagesBottomSheetFragment.setOnClickListener(view -> {
            activity.captureImage();
            dismiss();
        });

        adapter = new UploadedImagesRecyclerViewAdapter(getActivity(),
                getArguments().getParcelableArrayList(EXTRA_UPLOADED_IMAGES), uploadedImage -> {
            activity.insertImageUrl(uploadedImage);
            dismiss();
        });
        binding.recyclerViewUploadedImagesBottomSheet.setAdapter(adapter);

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((BaseActivity) activity).typeface);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (UploadImageEnabledActivity) context;
    }
}