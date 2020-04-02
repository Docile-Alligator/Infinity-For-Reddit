package ml.docilealligator.infinityforreddit.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Activity.ThemePreviewActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomTheme;
import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewCommentsFragment extends Fragment {

    @BindView(R.id.linear_layout_theme_preview_comments_fragment)
    LinearLayout linearLayout;
    @BindView(R.id.vertical_block_theme_preview_comments_fragment)
    View verticalBlock;
    @BindView(R.id.author_type_image_view_theme_preview_comments_fragment)
    ImageView authorTypeImageView;
    @BindView(R.id.author_text_view_theme_preview_comments_fragment)
    TextView authorTextView;
    @BindView(R.id.author_flair_text_view_theme_preview_comments_fragment)
    TextView flairTextView;
    @BindView(R.id.comment_time_text_view_theme_preview_comments_fragment)
    TextView commentTimeTextView;
    @BindView(R.id.comment_markdown_view_theme_preview_comments_fragment)
    TextView contentTextView;
    @BindView(R.id.up_vote_button_theme_preview_comments_fragment)
    ImageView upvoteButton;
    @BindView(R.id.score_text_view_theme_preview_comments_fragment)
    TextView scoreTextView;
    @BindView(R.id.down_vote_button_theme_preview_comments_fragment)
    ImageView downvoteButton;
    @BindView(R.id.more_button_theme_preview_comments_fragment)
    ImageView moreButton;
    @BindView(R.id.expand_button_theme_preview_comments_fragment)
    ImageView expandButton;
    @BindView(R.id.save_button_theme_preview_comments_fragment)
    ImageView saveButton;
    @BindView(R.id.reply_button_theme_preview_comments_fragment)
    ImageView replyButton;
    @BindView(R.id.divider_theme_preview_comments_fragment)
    View divider;
    private ThemePreviewActivity activity;

    public ThemePreviewCommentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_theme_preview_comments, container, false);
        ButterKnife.bind(this, rootView);

        CustomTheme customTheme = activity.getCustomTheme();
        linearLayout.setBackgroundColor(customTheme.commentBackgroundColor);
        authorTypeImageView.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        authorTextView.setTextColor(customTheme.moderator);
        commentTimeTextView.setTextColor(customTheme.secondaryTextColor);
        contentTextView.setTextColor(customTheme.commentColor);
        flairTextView.setTextColor(customTheme.authorFlairTextColor);
        divider.setBackgroundColor(customTheme.dividerColor);
        upvoteButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        scoreTextView.setTextColor(customTheme.commentIconAndInfoColor);
        downvoteButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        moreButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        expandButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        saveButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        replyButton.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (ThemePreviewActivity) context;
    }
}
