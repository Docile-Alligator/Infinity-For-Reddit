package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.post.Post;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareLinkBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_LINK = "EPL";
    public static final String EXTRA_MEDIA_LINK = "EML";
    public static final String EXTRA_MEDIA_TYPE = "EMT";
    public static final String EXTRA_GIF_VARIANT_LINK = "EGVL";

    @BindView(R.id.share_post_link_text_view_share_link_bottom_sheet_fragment)
    TextView sharePostLinkTextView;
    @BindView(R.id.share_media_link_text_view_share_link_bottom_sheet_fragment)
    TextView shareMediaLinkTextView;
    @BindView(R.id.share_media_link_extra_text_view_share_link_bottom_sheet_fragment)
    TextView shareMediaLinkExtraTextView;
    @BindView(R.id.copy_post_link_text_view_share_link_bottom_sheet_fragment)
    TextView copyPostLinkTextView;
    @BindView(R.id.copy_media_link_text_view_share_link_bottom_sheet_fragment)
    TextView copyMediaLinkTextView;
    @BindView(R.id.copy_media_link_extra_text_view_share_link_bottom_sheet_fragment)
    TextView copyMediaLinkExtraTextView;

    private Activity activity;

    public ShareLinkBottomSheetFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_share_link_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        String postLink = getArguments().getString(EXTRA_POST_LINK);
        String mediaLink = getArguments().containsKey(EXTRA_MEDIA_LINK) ? getArguments().getString(EXTRA_MEDIA_LINK) : null;
        String gifVariantLink = getArguments().containsKey(EXTRA_GIF_VARIANT_LINK) ? getArguments().getString(EXTRA_GIF_VARIANT_LINK) : null;

        if (mediaLink != null) {
            shareMediaLinkTextView.setVisibility(View.VISIBLE);
            copyMediaLinkTextView.setVisibility(View.VISIBLE);
            if (gifVariantLink != null) {
                shareMediaLinkExtraTextView.setVisibility(View.VISIBLE);
                copyMediaLinkExtraTextView.setVisibility(View.VISIBLE);
            }

            int mediaType = getArguments().getInt(EXTRA_MEDIA_TYPE);
            switch (mediaType) {
                case Post.IMAGE_TYPE:
                    shareMediaLinkTextView.setText(R.string.share_image_link);
                    copyMediaLinkTextView.setText(R.string.copy_image_link);
                    shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_24dp), null, null, null);
                    break;
                case Post.GIF_TYPE:
                    shareMediaLinkTextView.setText(R.string.share_gif_link);
                    copyMediaLinkTextView.setText(R.string.copy_gif_link);
                    shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_image_24dp), null, null, null);
                    break;
                case Post.VIDEO_TYPE:
                    shareMediaLinkTextView.setText(R.string.share_video_link);
                    copyMediaLinkTextView.setText(R.string.copy_video_link);
                    if (gifVariantLink != null) {
                        shareMediaLinkExtraTextView.setText(R.string.share_gif_link);
                        copyMediaLinkExtraTextView.setText(R.string.copy_gif_link);
                        shareMediaLinkExtraTextView.setCompoundDrawablesWithIntrinsicBounds(
                                activity.getDrawable(R.drawable.ic_image_24dp), null, null, null);
                    }
                    shareMediaLinkTextView.setCompoundDrawablesWithIntrinsicBounds(
                            activity.getDrawable(R.drawable.ic_outline_video_24dp), null, null, null);
                    break;
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    shareMediaLinkTextView.setText(R.string.share_link);
                    copyMediaLinkTextView.setText(R.string.copy_link);
                    break;
            }

            shareMediaLinkTextView.setOnClickListener(view -> {
                shareLink(mediaLink);
                dismiss();
            });
            copyMediaLinkTextView.setOnClickListener(view -> {
                copyLink(mediaLink);
                dismiss();
            });

            if (gifVariantLink != null) {
                shareMediaLinkExtraTextView.setOnClickListener(view -> {
                    shareLink(gifVariantLink);
                    dismiss();
                });

                copyMediaLinkExtraTextView.setOnClickListener(view -> {
                    copyLink(gifVariantLink);
                    dismiss();
                });
            }
        }

        sharePostLinkTextView.setOnClickListener(view -> {
            shareLink(postLink);
            dismiss();
        });
        copyPostLinkTextView.setOnClickListener(view -> {
            copyLink(postLink);
            dismiss();
        });
        return rootView;
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
        ClipboardManager clipboard = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("simple text", link);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(activity, R.string.copy_success, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(activity, R.string.copy_link_failed, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
