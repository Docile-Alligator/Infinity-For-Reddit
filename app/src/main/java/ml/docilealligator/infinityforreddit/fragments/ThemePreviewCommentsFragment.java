package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
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

    @BindView(R.id.linear_layout_award_background_theme_preview_comments_fragment)
    LinearLayout linearLayoutAwardBackground;
    @BindView(R.id.vertical_block_award_background_theme_preview_comments_fragment)
    View verticalBlockAwardBackground;
    @BindView(R.id.author_type_image_view_award_background_theme_preview_comments_fragment)
    ImageView authorTypeImageViewAwardBackground;
    @BindView(R.id.author_text_view_award_background_theme_preview_comments_fragment)
    TextView authorTextViewAwardBackground;
    @BindView(R.id.author_flair_text_view_award_background_theme_preview_comments_fragment)
    TextView flairTextViewAwardBackground;
    @BindView(R.id.comment_time_text_view_award_background_theme_preview_comments_fragment)
    TextView commentTimeTextViewAwardBackground;
    @BindView(R.id.comment_markdown_view_award_background_theme_preview_comments_fragment)
    TextView contentTextViewAwardBackground;
    @BindView(R.id.up_vote_button_award_background_theme_preview_comments_fragment)
    ImageView upvoteButtonAwardBackground;
    @BindView(R.id.score_text_view_award_background_theme_preview_comments_fragment)
    TextView scoreTextViewAwardBackground;
    @BindView(R.id.down_vote_button_award_background_theme_preview_comments_fragment)
    ImageView downvoteButtonAwardBackground;
    @BindView(R.id.more_button_award_background_theme_preview_comments_fragment)
    ImageView moreButtonAwardBackground;
    @BindView(R.id.expand_button_award_background_theme_preview_comments_fragment)
    ImageView expandButtonAwardBackground;
    @BindView(R.id.save_button_award_background_theme_preview_comments_fragment)
    ImageView saveButtonAwardBackground;
    @BindView(R.id.reply_button_award_background_theme_preview_comments_fragment)
    ImageView replyButtonAwardBackground;
    @BindView(R.id.divider_award_background_theme_preview_comments_fragment)
    View dividerAwardBackground;

    @BindView(R.id.linear_layout_fully_collapsed_theme_preview_comments_fragment)
    LinearLayout linearLayoutFullyCollapsed;
    @BindView(R.id.vertical_block_fully_collapsed_theme_preview_comments_fragment)
    View verticalBlockFullyCollapsed;
    @BindView(R.id.author_text_view_fully_collapsed_theme_preview_comments_fragment)
    TextView authorTextViewFullyCollapsed;
    @BindView(R.id.score_text_view_fully_collapsed_theme_preview_comments_fragment)
    TextView scoreTextViewFullyCollapsed;
    @BindView(R.id.time_text_view_fully_collapsed_theme_preview_comments_fragment)
    TextView timeTextViewFullyCollapsed;
    private CustomThemePreviewActivity activity;

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

        linearLayoutAwardBackground.setBackgroundColor(customTheme.awardedCommentBackgroundColor);
        authorTypeImageViewAwardBackground.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        authorTextViewAwardBackground.setTextColor(customTheme.moderator);
        commentTimeTextViewAwardBackground.setTextColor(customTheme.secondaryTextColor);
        contentTextViewAwardBackground.setTextColor(customTheme.commentColor);
        flairTextViewAwardBackground.setTextColor(customTheme.authorFlairTextColor);
        dividerAwardBackground.setBackgroundColor(customTheme.dividerColor);
        upvoteButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        scoreTextViewAwardBackground.setTextColor(customTheme.commentIconAndInfoColor);
        downvoteButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        moreButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        expandButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        saveButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        replyButtonAwardBackground.setColorFilter(customTheme.commentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

        linearLayoutFullyCollapsed.setBackgroundColor(customTheme.fullyCollapsedCommentBackgroundColor);
        authorTextViewFullyCollapsed.setTextColor(customTheme.username);
        scoreTextViewFullyCollapsed.setTextColor(customTheme.secondaryTextColor);
        timeTextViewFullyCollapsed.setTextColor(customTheme.secondaryTextColor);

        if (activity.typeface != null) {
            authorTextView.setTypeface(activity.typeface);
            commentTimeTextView.setTypeface(activity.typeface);
            flairTextView.setTypeface(activity.typeface);
            scoreTextView.setTypeface(activity.typeface);
            authorTextViewAwardBackground.setTypeface(activity.typeface);
            commentTimeTextViewAwardBackground.setTypeface(activity.typeface);
            flairTextViewAwardBackground.setTypeface(activity.typeface);
            scoreTextViewAwardBackground.setTypeface(activity.typeface);
            authorTextViewFullyCollapsed.setTypeface(activity.typeface);
            scoreTextViewFullyCollapsed.setTypeface(activity.typeface);
            timeTextViewFullyCollapsed.setTypeface(activity.typeface);
        }
        if (activity.contentTypeface != null) {
            contentTextView.setTypeface(activity.contentTypeface);
            contentTextViewAwardBackground.setTypeface(activity.contentTypeface);
        }
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CustomThemePreviewActivity) context;
    }
}
