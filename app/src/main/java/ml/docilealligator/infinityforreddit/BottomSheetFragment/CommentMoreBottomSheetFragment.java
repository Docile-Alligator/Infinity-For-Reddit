package ml.docilealligator.infinityforreddit.BottomSheetFragment;


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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.deishelon.roundedbottomsheet.RoundedBottomSheetDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.EditCommentActivity;
import ml.docilealligator.infinityforreddit.Activity.ReportActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.CommentData;
import ml.docilealligator.infinityforreddit.R;


/**
 * A simple {@link Fragment} subclass.
 */
public class CommentMoreBottomSheetFragment extends RoundedBottomSheetDialogFragment {

    public static final String EXTRA_COMMENT = "ECF";
    public static final String EXTRA_ACCESS_TOKEN = "EAT";
    public static final String EXTRA_POSITION = "EP";
    @BindView(R.id.edit_text_view_comment_more_bottom_sheet_fragment)
    TextView editTextView;
    @BindView(R.id.delete_text_view_comment_more_bottom_sheet_fragment)
    TextView deleteTextView;
    @BindView(R.id.save_text_view_comment_more_bottom_sheet_fragment)
    TextView shareTextView;
    @BindView(R.id.copy_text_view_comment_more_bottom_sheet_fragment)
    TextView copyTextView;
    @BindView(R.id.report_view_comment_more_bottom_sheet_fragment)
    TextView reportTextView;
    private AppCompatActivity activity;
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
        CommentData commentData = bundle.getParcelable(EXTRA_COMMENT);
        if (commentData == null) {
            dismiss();
            return rootView;
        }
        String accessToken = bundle.getString(EXTRA_ACCESS_TOKEN);

        if (accessToken != null && !accessToken.equals("")) {
            editTextView.setVisibility(View.VISIBLE);
            deleteTextView.setVisibility(View.VISIBLE);

            editTextView.setOnClickListener(view -> {
                Intent intent = new Intent(activity, EditCommentActivity.class);
                intent.putExtra(EditCommentActivity.EXTRA_ACCESS_TOKEN, accessToken);
                intent.putExtra(EditCommentActivity.EXTRA_FULLNAME, commentData.getFullName());
                intent.putExtra(EditCommentActivity.EXTRA_CONTENT, commentData.getCommentMarkdown());
                intent.putExtra(EditCommentActivity.EXTRA_POSITION, bundle.getInt(EXTRA_POSITION));
                if (activity instanceof ViewPostDetailActivity) {
                    activity.startActivityForResult(intent, ViewPostDetailActivity.EDIT_COMMENT_REQUEST_CODE);
                } else {
                    startActivity(intent);
                }

                dismiss();
            });

            deleteTextView.setOnClickListener(view -> {
                dismiss();
                if (activity instanceof ViewPostDetailActivity) {
                    ((ViewPostDetailActivity) activity).deleteComment(commentData.getFullName(), bundle.getInt(EXTRA_POSITION));
                } else if (activity instanceof ViewUserDetailActivity) {
                    ((ViewUserDetailActivity) activity).deleteComment(commentData.getFullName());
                }
            });
        }

        shareTextView.setOnClickListener(view -> {
            dismiss();
            try {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, commentData.getPermalink());
                activity.startActivity(Intent.createChooser(intent, getString(R.string.share)));
            } catch (ActivityNotFoundException e) {
                Toast.makeText(activity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
            }
        });

        copyTextView.setOnClickListener(view -> {
            dismiss();
            CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();
            Bundle copyBundle = new Bundle();
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, commentData.getCommentMarkdown());
            copyBundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, commentData.getCommentRawText());
            copyTextBottomSheetFragment.setArguments(copyBundle);
            copyTextBottomSheetFragment.show(activity.getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
        });

        reportTextView.setOnClickListener(view -> {
            Intent intent = new Intent(activity, ReportActivity.class);
            intent.putExtra(ReportActivity.EXTRA_SUBREDDIT_NAME, commentData.getSubredditName());
            intent.putExtra(ReportActivity.EXTRA_THING_FULLNAME, commentData.getFullName());
            activity.startActivity(intent);

            dismiss();
        });

        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (AppCompatActivity) context;
    }
}
