package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.adapters.UploadedImagesRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UploadedImagesBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_UPLOADED_IMAGES = "EUI";

    private MaterialButton uploadButton;
    private MaterialButton captureButton;
    private RecyclerView uploadedImagesRecyclerView;
    private UploadedImagesRecyclerViewAdapter adapter;
    private UploadImageEnabledActivity activity;

    public UploadedImagesBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_uploaded_images_bottom_sheet, container, false);
        uploadButton = rootView.findViewById(R.id.upload_button_uploaded_images_bottom_sheet_fragment);
        captureButton = rootView.findViewById(R.id.capture_button_uploaded_images_bottom_sheet_fragment);

        uploadButton.setOnClickListener(view -> {
            activity.uploadImage();
            dismiss();
        });

        captureButton.setOnClickListener(view -> {
            activity.captureImage();
            dismiss();
        });

        uploadedImagesRecyclerView = rootView.findViewById(R.id.recycler_view_uploaded_images_bottom_sheet);
        adapter = new UploadedImagesRecyclerViewAdapter(getActivity(),
                getArguments().getParcelableArrayList(EXTRA_UPLOADED_IMAGES), uploadedImage -> {
            activity.insertImageUrl(uploadedImage);
            dismiss();
        });
        uploadedImagesRecyclerView.setAdapter(adapter);

        Activity baseActivity = getActivity();
        if (baseActivity instanceof BaseActivity) {
            if (((BaseActivity) activity).typeface != null) {
                Utils.setFontToAllTextViews(rootView, ((BaseActivity) activity).typeface);
            }
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.activity = (UploadImageEnabledActivity) context;
    }
}