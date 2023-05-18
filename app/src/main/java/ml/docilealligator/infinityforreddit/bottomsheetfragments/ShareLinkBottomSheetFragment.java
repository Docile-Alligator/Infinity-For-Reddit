package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.LocalSave;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ShareLinkBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {
    public static final String EXTRA_POST_ID = "EPID";
    public static final String EXTRA_POST_SUBREDDIT = "EPS";
    public static final String EXTRA_POST_TITLE = "EPT";
    public static final String EXTRA_POST_FLAIR = "EPF";
    public static final String EXTRA_POST_LINK = "EPL";
    public static final String EXTRA_MEDIA_LINK = "EML";
    public static final String EXTRA_MEDIA_TYPE = "EMT";


    @BindView(R.id.manage_tags)
    TextView manageTagsTextView;

    @BindView(R.id.remove_local_post)
    TextView removeLocalTextView;
    @BindView(R.id.add_local_post)
    TextView addLocalTextView;


    @BindView(R.id.post_link_text_view_share_link_bottom_sheet_fragment)
    TextView postLinkTextView;
    @BindView(R.id.share_post_link_text_view_share_link_bottom_sheet_fragment)
    TextView sharePostLinkTextView;
    @BindView(R.id.copy_post_link_text_view_share_link_bottom_sheet_fragment)
    TextView copyPostLinkTextView;
    @BindView(R.id.media_link_text_view_share_link_bottom_sheet_fragment)
    TextView mediaLinkTextView;
    @BindView(R.id.share_media_link_text_view_share_link_bottom_sheet_fragment)
    TextView shareMediaLinkTextView;
    @BindView(R.id.copy_media_link_text_view_share_link_bottom_sheet_fragment)
    TextView copyMediaLinkTextView;

    private BaseActivity activity;

    public ShareLinkBottomSheetFragment() {
        // Required empty public constructor
    }

    private void ShowTags(String postID)
    {
        MaterialAlertDialogBuilder alert = new MaterialAlertDialogBuilder(activity, R.style.MaterialAlertDialogTheme).setTitle("Tags");

        LocalSave.SavedPost post = LocalSave.GetPost(postID);
        if(post == null)
        {
            alert.setMessage("Post is not saved");
            alert.setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());
            alert.show();
            return;
        }

        final EditText input = new EditText(activity);
        input.setText(post.getTags());

        alert.setView(input);

        alert.setPositiveButton("Save", (dialogInterface, i) -> post.setTags(input.getText().toString()));
        alert.setNegativeButton("Cancel", (dialogInterface, i) -> dialogInterface.dismiss());

        alert.show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_share_link_bottom_sheet, container, false);

        ButterKnife.bind(this, rootView);

        String postLink = getArguments().getString(EXTRA_POST_LINK);
        String mediaLink = getArguments().containsKey(EXTRA_MEDIA_LINK) ? getArguments().getString(EXTRA_MEDIA_LINK) : null;
        String postID = getArguments().getString(EXTRA_POST_ID);
        String postTitle = getArguments().getString(EXTRA_POST_TITLE);
        String postSubName = getArguments().getString(EXTRA_POST_SUBREDDIT);
        String postFlair = getArguments().getString(EXTRA_POST_FLAIR);

        postLinkTextView.setText(postLink);

        if (mediaLink != null) {
            mediaLinkTextView.setVisibility(View.VISIBLE);
            shareMediaLinkTextView.setVisibility(View.VISIBLE);
            copyMediaLinkTextView.setVisibility(View.VISIBLE);

            mediaLinkTextView.setText(mediaLink);

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
        }

        manageTagsTextView.setOnClickListener(view -> {
            ShowTags(postID);
        });
        addLocalTextView.setOnClickListener(view -> {
            LocalSave.AddPost(postID, postTitle, postSubName, postFlair, 0);
            dismiss();
        });
        removeLocalTextView.setOnClickListener(view -> {
            LocalSave.RemovePost(postID);
            dismiss();
        });

        sharePostLinkTextView.setOnClickListener(view -> {
            shareLink(postLink);
            dismiss();
        });
        copyPostLinkTextView.setOnClickListener(view -> {
            copyLink(postLink);
            dismiss();
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }
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
        activity.copyLink(link);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}