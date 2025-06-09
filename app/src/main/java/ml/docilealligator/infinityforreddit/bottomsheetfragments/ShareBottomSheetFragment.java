package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.ActivityNotFoundException;
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
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentShareLinkBottomSheetBinding;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.ShareScreenshotUtilsKt;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_LINK = "EPL";
    public static final String EXTRA_MEDIA_LINK = "EML";
    public static final String EXTRA_MEDIA_TYPE = "EMT";
    public static final String EXTRA_POST = "EP";

    private BaseActivity activity;

    public ShareBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        FragmentShareLinkBottomSheetBinding binding = FragmentShareLinkBottomSheetBinding.inflate(inflater, container, false);

        String postLink = getArguments().getString(EXTRA_POST_LINK);
        String mediaLink = getArguments().containsKey(EXTRA_MEDIA_LINK) ? getArguments().getString(EXTRA_MEDIA_LINK) : null;
        Post post = getArguments().getParcelable(EXTRA_POST);

        binding.postLinkTextViewShareLinkBottomSheetFragment.setText(postLink);

        if (mediaLink != null) {
            binding.mediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);

            binding.mediaLinkTextViewShareLinkBottomSheetFragment.setText(mediaLink);

            int mediaType = getArguments().getInt(EXTRA_MEDIA_TYPE);
            switch (mediaType) {
                case Post.IMAGE_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_image_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_image_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_day_night_24dp), null, null, null);
                    break;
                case Post.GIF_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_gif_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_gif_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_day_night_24dp), null, null, null);
                    break;
                case Post.VIDEO_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_video_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_video_link);
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_video_day_night_24dp), null, null, null);
                    break;
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.share_link);
                    binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setText(R.string.copy_link);
                    break;
            }

            binding.shareMediaLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                shareLink(mediaLink);
                dismiss();
            });
            binding.copyMediaLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                copyLink(mediaLink);
                dismiss();
            });
        }

        binding.sharePostLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
            shareLink(postLink);
            dismiss();
        });
        binding.copyPostLinkTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
            copyLink(postLink);
            dismiss();
        });

        if (post != null) {
            binding.shareAsImageTextViewShareLinkBottomSheetFragment.setVisibility(View.VISIBLE);

            binding.shareAsImageTextViewShareLinkBottomSheetFragment.setOnClickListener(view -> {
                ShareScreenshotUtilsKt.sharePostAsScreenshot(
                        activity,
                        post,
                        activity.customThemeWrapper,
                        activity.getResources().getConfiguration().locale,
                        activity.getDefaultSharedPreferences().getString(SharedPreferencesUtils.TIME_FORMAT_KEY,
                                SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE),
                        new SaveMemoryCenterInisdeDownsampleStrategy(
                                Integer.parseInt(activity.getDefaultSharedPreferences()
                                        .getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")))
                );
                dismiss();
            });
        }

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }
        return binding.getRoot();
    }

    private void shareLink(String link) {
        try {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, link);
            activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
            Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
        }
    }

    private void copyLink(String link) {
        activity.copyLink(link);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
