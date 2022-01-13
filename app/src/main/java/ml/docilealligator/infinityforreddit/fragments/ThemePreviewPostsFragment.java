package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.libRG.CustomTextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewPostsFragment extends Fragment {

    @BindView(R.id.card_view_theme_preview_posts_fragment)
    CardView cardView;
    @BindView(R.id.icon_gif_image_view_theme_preview_posts_fragment)
    AspectRatioGifImageView iconImageView;
    @BindView(R.id.subreddit_name_text_view_theme_preview_posts_fragment)
    TextView subredditNameTextView;
    @BindView(R.id.user_text_view_theme_preview_posts_fragment)
    TextView usernameTextView;
    @BindView(R.id.stickied_post_image_view_theme_preview_posts_fragment)
    ImageView stickiedPostImageView;
    @BindView(R.id.post_time_text_view_best_theme_preview_posts_fragment)
    TextView postTimeTextView;
    @BindView(R.id.title_text_view_best_theme_preview_posts_fragment)
    TextView titleTextView;
    @BindView(R.id.content_text_view_theme_preview_posts_fragment)
    TextView contentTextView;
    @BindView(R.id.type_text_view_theme_preview_posts_fragment)
    CustomTextView typeTextView;
    @BindView(R.id.spoiler_custom_text_view_theme_preview_posts_fragment)
    CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_text_view_theme_preview_posts_fragment)
    CustomTextView nsfwTextView;
    @BindView(R.id.flair_custom_text_view_theme_preview_posts_fragment)
    CustomTextView flairTextView;
    @BindView(R.id.awards_text_view_theme_preview_posts_fragment)
    CustomTextView awardsTextView;
    @BindView(R.id.archived_image_view_theme_preview_posts_fragment)
    ImageView archivedImageView;
    @BindView(R.id.locked_image_view_theme_preview_posts_fragment)
    ImageView lockedImageView;
    @BindView(R.id.crosspost_image_view_theme_preview_posts_fragment)
    ImageView crosspostImageView;
    @BindView(R.id.link_text_view_theme_preview_posts_fragment)
    TextView linkTextView;
    @BindView(R.id.progress_bar_theme_preview_posts_fragment)
    ProgressBar progressBar;
    @BindView(R.id.image_view_no_preview_link_theme_preview_posts_fragment)
    ImageView noPreviewLinkImageView;
    @BindView(R.id.plus_button_theme_preview_posts_fragment)
    ImageView upvoteButton;
    @BindView(R.id.score_text_view_theme_preview_posts_fragment)
    TextView scoreTextView;
    @BindView(R.id.minus_button_theme_preview_posts_fragment)
    ImageView downvoteButton;
    @BindView(R.id.comments_count_theme_preview_posts_fragment)
    TextView commentsCountTextView;
    @BindView(R.id.save_button_theme_preview_posts_fragment)
    ImageView saveButton;
    @BindView(R.id.share_button_theme_preview_posts_fragment)
    ImageView shareButton;
    private CustomThemePreviewActivity activity;

    public ThemePreviewPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_theme_preview_posts, container, false);
        ButterKnife.bind(this, rootView);

        CustomTheme customTheme = activity.getCustomTheme();

        cardView.setBackgroundTintList(ColorStateList.valueOf(customTheme.cardViewBackgroundColor));
        Glide.with(this).load(R.drawable.subreddit_default_icon)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                .into(iconImageView);
        subredditNameTextView.setTextColor(customTheme.subreddit);
        usernameTextView.setTextColor(customTheme.username);
        postTimeTextView.setTextColor(customTheme.secondaryTextColor);
        titleTextView.setTextColor(customTheme.postTitleColor);
        contentTextView.setTextColor(customTheme.postContentColor);
        stickiedPostImageView.setColorFilter(customTheme.stickiedPostIconTint, PorterDuff.Mode.SRC_IN);
        typeTextView.setBackgroundColor(customTheme.postTypeBackgroundColor);
        typeTextView.setBorderColor(customTheme.postTypeBackgroundColor);
        typeTextView.setTextColor(customTheme.postTypeTextColor);
        spoilerTextView.setBackgroundColor(customTheme.spoilerBackgroundColor);
        spoilerTextView.setBorderColor(customTheme.spoilerBackgroundColor);
        spoilerTextView.setTextColor(customTheme.spoilerTextColor);
        nsfwTextView.setBackgroundColor(customTheme.nsfwBackgroundColor);
        nsfwTextView.setBorderColor(customTheme.nsfwBackgroundColor);
        nsfwTextView.setTextColor(customTheme.nsfwTextColor);
        flairTextView.setBackgroundColor(customTheme.flairBackgroundColor);
        flairTextView.setBorderColor(customTheme.flairBackgroundColor);
        flairTextView.setTextColor(customTheme.flairTextColor);
        awardsTextView.setBackgroundColor(customTheme.awardsBackgroundColor);
        awardsTextView.setBorderColor(customTheme.awardsBackgroundColor);
        awardsTextView.setTextColor(customTheme.awardsTextColor);
        archivedImageView.setColorFilter(customTheme.archivedTint, PorterDuff.Mode.SRC_IN);
        lockedImageView.setColorFilter(customTheme.lockedIconTint, PorterDuff.Mode.SRC_IN);
        crosspostImageView.setColorFilter(customTheme.crosspostIconTint, PorterDuff.Mode.SRC_IN);
        linkTextView.setTextColor(customTheme.secondaryTextColor);
        progressBar.setIndeterminateTintList(ColorStateList.valueOf(customTheme.colorAccent));
        noPreviewLinkImageView.setBackgroundColor(customTheme.noPreviewPostTypeBackgroundColor);
        upvoteButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        scoreTextView.setTextColor(customTheme.postIconAndInfoColor);
        downvoteButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        commentsCountTextView.setTextColor(customTheme.postIconAndInfoColor);
        Drawable commentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
        if (commentIcon != null) {
            DrawableCompat.setTint(commentIcon, customTheme.postIconAndInfoColor);
        }
        commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(commentIcon, null, null, null);
        saveButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        shareButton.setColorFilter(customTheme.postIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

        if (activity.typeface != null) {
            subredditNameTextView.setTypeface(activity.typeface);
            usernameTextView.setTypeface(activity.typeface);
            postTimeTextView.setTypeface(activity.typeface);
            typeTextView.setTypeface(activity.typeface);
            spoilerTextView.setTypeface(activity.typeface);
            nsfwTextView.setTypeface(activity.typeface);
            flairTextView.setTypeface(activity.typeface);
            awardsTextView.setTypeface(activity.typeface);
            linkTextView.setTypeface(activity.typeface);
            scoreTextView.setTypeface(activity.typeface);
            commentsCountTextView.setTypeface(activity.typeface);
        }
        if (activity.titleTypeface != null) {
            titleTextView.setTypeface(activity.titleTypeface);
        }
        if (activity.contentTypeface != null) {
            contentTextView.setTypeface(activity.contentTypeface);
        }
        return rootView;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CustomThemePreviewActivity) context;
    }
}
