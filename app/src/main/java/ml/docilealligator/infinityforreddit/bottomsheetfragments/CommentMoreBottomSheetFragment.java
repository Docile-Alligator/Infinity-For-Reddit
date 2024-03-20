package ml.docilealligator.infinityforreddit.bottomsheetfragments;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.CommentFilterPreferenceActivity;
import ml.docilealligator.infinityforreddit.activities.EditCommentActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.databinding.FragmentCommentMoreBottomSheetBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentMoreBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT = "ECF";
    public static final String EXTRA_EDIT_AND_DELETE_AVAILABLE = "EEADA";
    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_SHOW_REPLY_AND_SAVE_OPTION = "ESSARO";
    public static final String EXTRA_IS_NSFW = "EIN";

    private FragmentCommentMoreBottomSheetBinding binding;
    private BaseActivity activity;

    public CommentMoreBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCommentMoreBottomSheetBinding.inflate(inflater, container, false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            binding.getRoot().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            dismiss();
            return binding.getRoot();
        }
        Comment comment = bundle.getParcelable(EXTRA_COMMENT);
        if (comment == null) {
            dismiss();
            return binding.getRoot();
        }
        boolean editAndDeleteAvailable = bundle.getBoolean(EXTRA_EDIT_AND_DELETE_AVAILABLE, false);
        boolean showReplyAndSaveOption = bundle.getBoolean(EXTRA_SHOW_REPLY_AND_SAVE_OPTION, false);

        if (!activity.accountName.equals(Account.ANONYMOUS_ACCOUNT) && !"".equals(activity.accessToken)) {
            if (editAndDeleteAvailable) {
                binding.editTextViewCommentMoreBottomSheetFragment.setVisibility(View.VISIBLE);
                binding.deleteTextViewCommentMoreBottomSheetFragment.setVisibility(View.VISIBLE);

                binding.editTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
                    Intent intent = new Intent(activity, EditCommentActivity.class);
                    intent.putExtra(EditCommentActivity.EXTRA_FULLNAME, comment.getFullName());
                    intent.putExtra(EditCommentActivity.EXTRA_CONTENT, comment.getCommentMarkdown());
                    if (comment.getMediaMetadataMap() != null) {
                        ArrayList<MediaMetadata> mediaMetadataList = new ArrayList<>(comment.getMediaMetadataMap().values());
                        intent.putParcelableArrayListExtra(EditCommentActivity.EXTRA_MEDIA_METADATA_LIST, mediaMetadataList);
                    }
                    intent.putExtra(EditCommentActivity.EXTRA_POSITION, bundle.getInt(EXTRA_POSITION));
                    if (activity instanceof ViewPostDetailActivity) {
                        activity.startActivityForResult(intent, ViewPostDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                    } else {
                        activity.startActivityForResult(intent, ViewUserDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                    }

                    dismiss();
                });

                binding.deleteTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
                    dismiss();
                    if (activity instanceof ViewPostDetailActivity) {
                        ((ViewPostDetailActivity) activity).deleteComment(comment.getFullName(), bundle.getInt(EXTRA_POSITION));
                    } else if (activity instanceof ViewUserDetailActivity) {
                        ((ViewUserDetailActivity) activity).deleteComment(comment.getFullName());
                    }
                });
            }
        }

        if (showReplyAndSaveOption) {
            binding.replyTextViewCommentMoreBottomSheetFragment.setVisibility(View.VISIBLE);
            binding.saveTextViewCommentMoreBottomSheetFragment.setVisibility(View.VISIBLE);
            if (comment.isSaved()) {
                binding.saveTextViewCommentMoreBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_bookmark_24dp), null, null, null);
                binding.saveTextViewCommentMoreBottomSheetFragment.setText(R.string.unsave_comment);
            } else {
                binding.saveTextViewCommentMoreBottomSheetFragment.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_bookmark_border_24dp), null, null, null);
                binding.saveTextViewCommentMoreBottomSheetFragment.setText(R.string.save_comment);
            }
            binding.replyTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, comment.getCommentMarkdown());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, comment.getCommentRawText());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, bundle.getInt(EXTRA_POSITION));
                activity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);

                dismiss();
            });

            binding.saveTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).saveComment(comment, bundle.getInt(EXTRA_POSITION));
                }
                dismiss();
            });
        }

        binding.shareTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
            dismiss();
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, comment.getPermalink());
                activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
            }
        });

        binding.shareTextViewCommentMoreBottomSheetFragment.setOnLongClickListener(view -> {
            dismiss();
            activity.copyLink(comment.getPermalink());
            return true;
        });

        binding.copyTextViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
            dismiss();
            CopyTextBottomSheetFragment.show(activity.getSupportFragmentManager(),
                    comment.getCommentRawText(), comment.getCommentMarkdown());
        });

        binding.reportViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, comment.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, comment.getFullName());
            activity.startActivity(intent);

            dismiss();
        });

        binding.addToCommentFilterViewCommentMoreBottomSheetFragment.setOnClickListener(view -> {
            Intent intent = new Intent(activity, CommentFilterPreferenceActivity.class);
            intent.putExtra(CommentFilterPreferenceActivity.EXTRA_COMMENT, comment);
            activity.startActivity(intent);
        });

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(binding.getRoot(), activity.typeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
