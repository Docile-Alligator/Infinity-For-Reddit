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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.EditCommentActivity;
import ml.docilealligator.infinityforreddit.activities.FullMarkdownActivity;
import ml.docilealligator.infinityforreddit.activities.GiveAwardActivity;
import ml.docilealligator.infinityforreddit.activities.ReportActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customviews.LandscapeExpandedRoundedBottomSheetDialogFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentMoreBottomSheetFragment extends LandscapeExpandedRoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT = "ECF";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_EDIT_AND_DELETE_AVAILABLE = "EEADA";
    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_SHOW_REPLY_AND_SAVE_OPTION = "ESSARO";
    public static final String EXTRA_COMMENT_MARKDOWN = "ECM";
    public static final String EXTRA_IS_NSFW = "EIN";
    @BindView(R.id.edit_text_view_comment_more_bottom_sheet_fragment)
    TextView editTextView;
    @BindView(R.id.delete_text_view_comment_more_bottom_sheet_fragment)
    TextView deleteTextView;
    @BindView(R.id.reply_text_view_comment_more_bottom_sheet_fragment)
    TextView replyTextView;
    @BindView(R.id.save_text_view_comment_more_bottom_sheet_fragment)
    TextView saveTextView;
    @BindView(R.id.share_text_view_comment_more_bottom_sheet_fragment)
    TextView shareTextView;
    @BindView(R.id.copy_text_view_comment_more_bottom_sheet_fragment)
    TextView copyTextView;
    @BindView(R.id.give_award_text_view_comment_more_bottom_sheet_fragment)
    TextView giveAwardTextView;
    @BindView(R.id.view_full_markdown_text_view_comment_more_bottom_sheet_fragment)
    TextView viewFullMarkdownTextView;
    @BindView(R.id.report_view_comment_more_bottom_sheet_fragment)
    TextView reportTextView;
    @BindView(R.id.see_removed_view_comment_more_bottom_sheet_fragment)
    TextView seeRemovedTextView;
    private BaseActivity activity;

    public CommentMoreBottomSheetFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_comment_more_bottom_sheet, container, false);
        ButterKnife.bind(this, rootView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
                && (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
            rootView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }

        Bundle bundle = getArguments();
        if (bundle == null) {
            dismiss();
            return rootView;
        }
        Comment comment = bundle.getParcelable(EXTRA_COMMENT);
        if (comment == null) {
            dismiss();
            return rootView;
        }
        String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);
        boolean editAndDeleteAvailable = bundle.getBoolean(EXTRA_EDIT_AND_DELETE_AVAILABLE, false);
        boolean showReplyAndSaveOption = bundle.getBoolean(EXTRA_SHOW_REPLY_AND_SAVE_OPTION, false);

        if (accessToken != null && !accessToken.equals("")) {
            giveAwardTextView.setVisibility(View.VISIBLE);
            giveAwardTextView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, GiveAwardActivity.class);
                intent.putExtra(GiveAwardActivity.EXTRA_THING_FULLNAME, comment.getFullName());
                intent.putExtra(GiveAwardActivity.EXTRA_ITEM_POSITION, bundle.getInt(EXTRA_POSITION));
                if (activity instanceof ViewPostDetailActivity) {
                    activity.startActivityForResult(intent, ViewPostDetailActivity.GIVE_AWARD_REQUEST_CODE);
                } else if (activity instanceof ViewUserDetailActivity) {
                    activity.startActivityForResult(intent, ViewUserDetailActivity.GIVE_AWARD_REQUEST_CODE);
                }
                dismiss();
            });

            if (editAndDeleteAvailable) {
                editTextView.setVisibility(View.VISIBLE);
                deleteTextView.setVisibility(View.VISIBLE);

                editTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(activity, EditCommentActivity.class);
                    intent.putExtra(EditCommentActivity.EXTRA_FULLNAME, comment.getFullName());
                    intent.putExtra(EditCommentActivity.EXTRA_CONTENT, comment.getCommentMarkdown());
                    intent.putExtra(EditCommentActivity.EXTRA_POSITION, bundle.getInt(EXTRA_POSITION));
                    if (activity instanceof ViewPostDetailActivity) {
                        activity.startActivityForResult(intent, ViewPostDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                    } else {
                        activity.startActivityForResult(intent, ViewUserDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                    }

                    dismiss();
                });

                deleteTextView.setOnClickListener(view -> {
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
            replyTextView.setVisibility(View.VISIBLE);
            saveTextView.setVisibility(View.VISIBLE);
            if (comment.isSaved()) {
                saveTextView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_bookmark_24dp), null, null, null);
                saveTextView.setText(R.string.unsave_comment);
            } else {
                saveTextView.setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable(activity, R.drawable.ic_bookmark_border_24dp), null, null, null);
                saveTextView.setText(R.string.save_comment);
            }
            replyTextView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY, comment.getCommentMarkdown());
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, comment.getCommentRawText());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, bundle.getInt(EXTRA_POSITION));
                activity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);

                dismiss();
            });

            saveTextView.setOnClickListener(view -> {
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).saveComment(comment, bundle.getInt(EXTRA_POSITION));
                }
                dismiss();
            });
        }

        shareTextView.setOnClickListener(view -> {
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

        copyTextView.setOnClickListener(view -> {
            dismiss();
            CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
            Bundle copyBundle = new Bundle();
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, comment.getCommentMarkdown());
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, comment.getCommentRawText());
            copyTextBottomSheetFragment.setArguments(copyBundle);
            copyTextBottomSheetFragment.show(activity.getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
        });

        viewFullMarkdownTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_IS_NSFW, bundle.getBoolean(EXTRA_IS_NSFW, false));
            intent.putExtra(FullMarkdownActivity.EXTRA_COMMENT_MARKDOWN, bundle.getString(EXTRA_COMMENT_MARKDOWN, ""));
            activity.startActivity(intent);

            dismiss();
        });

        reportTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, comment.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, comment.getFullName());
            activity.startActivity(intent);

            dismiss();
        });

        if ("[deleted]".equals(comment.getAuthor()) ||
                "[deleted]".equals(comment.getCommentRawText()) ||
                "[removed]".equals(comment.getCommentRawText())
        ) {
            seeRemovedTextView.setVisibility(View.VISIBLE);

            seeRemovedTextView.setOnClickListener(view -> {
                dismiss();
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).showRemovedComment(comment, bundle.getInt(EXTRA_POSITION));
                }
            });
        }

        if (activity.typeface != null) {
            Utils.setFontToAllTextViews(rootView, activity.typeface);
        }

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (BaseActivity) context;
    }
}
