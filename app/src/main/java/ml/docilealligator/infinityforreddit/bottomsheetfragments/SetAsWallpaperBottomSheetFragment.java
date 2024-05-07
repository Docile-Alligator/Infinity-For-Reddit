package ml.docilealligator.infinityforreddit.bottomsheetfragments;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import ml.docilealligator.infinityforreddit.SetAsWallpaperCallback;
import ml.docilealligator.infinityforreddit.activities.ViewImgurMediaActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentSetAsWallpaperBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SetAsWallpaperBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_VIEW_PAGER_POSITION = "EVPP";

    private Activity mActivity;

    public SetAsWallpaperBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FragmentSetAsWallpaperBottomSheetBinding binding = FragmentSetAsWallpaperBottomSheetBinding.inflate(inflater, container, false);

        Bundle bundle = getArguments();
        int viewPagerPosition = bundle == null ? -1 : bundle.getInt(EXTRA_VIEW_PAGER_POSITION);

        binding.bothTextViewSetAsWallpaperBottomSheetFragment.setOnClickListener(view -> {
            if (mActivity instanceof SetAsWallpaperCallback) {
                ((SetAsWallpaperCallback) mActivity).setToBoth(viewPagerPosition);
            }
            dismiss();
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            binding.homeScreenTextViewSetAsWallpaperBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.lockScreenTextViewSetAsWallpaperBottomSheetFragment.setVisibility(View.VISIBLE);

            binding.homeScreenTextViewSetAsWallpaperBottomSheetFragment.setOnClickListener(view -> {
                if (mActivity instanceof SetAsWallpaperCallback) {
                    ((SetAsWallpaperCallback) mActivity).setToHomeScreen(viewPagerPosition);
                }
                dismiss();
            });

            binding.lockScreenTextViewSetAsWallpaperBottomSheetFragment.setOnClickListener(view -> {
                if (mActivity instanceof SetAsWallpaperCallback) {
                    ((SetAsWallpaperCallback) mActivity).setToLockScreen(viewPagerPosition);
                }
                dismiss();
            });
        }

        if (mActivity instanceof ViewVideoActivity) {
            if (((ViewVideoActivity) mActivity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewVideoActivity) mActivity).typeface);
            }
        } else if (mActivity instanceof ViewImgurMediaActivity) {
            if (((ViewImgurMediaActivity) mActivity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewImgurMediaActivity) mActivity).typeface);
            }
        } else if (mActivity instanceof ViewRedditGalleryActivity) {
            if (((ViewRedditGalleryActivity) mActivity).typeface != null) {
                Utils.setFontToAllTextViews(binding.getRoot(), ((ViewRedditGalleryActivity) mActivity).typeface);
            }
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.mActivity = (Activity) context;
    }
}