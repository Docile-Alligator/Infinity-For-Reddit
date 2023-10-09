package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.FragmentThemePreviewCommentsBinding;
import ml.docilealligator.infinityforreddit.utils.Utils;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewCommentsFragment extends Fragment {

    private FragmentThemePreviewCommentsBinding binding;
    private CustomThemePreviewActivity activity;

    public ThemePreviewCommentsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentThemePreviewCommentsBinding.inflate(inflater, container, false);

        CustomTheme customTheme = activity.getCustomTheme();

        Drawable expandDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_more_grey_24dp, customTheme.commentIconAndInfoColor);

        binding.linearLayoutThemePreviewCommentsFragment.setBackgroundColor(customTheme.commentBackgroundColor);
        binding.authorTypeImageViewThemePreviewCommentsFragment.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.authorTextViewThemePreviewCommentsFragment.setTextColor(customTheme.moderator);
        binding.commentTimeTextViewThemePreviewCommentsFragment.setTextColor(customTheme.secondaryTextColor);
        binding.commentMarkdownViewThemePreviewCommentsFragment.setTextColor(customTheme.commentColor);
        binding.authorFlairTextViewThemePreviewCommentsFragment.setTextColor(customTheme.authorFlairTextColor);
        binding.dividerThemePreviewCommentsFragment.setBackgroundColor(customTheme.dividerColor);
        binding.upvoteButtonThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.upvoteButtonThemePreviewCommentsFragment.setTextColor(customTheme.commentIconAndInfoColor);
        binding.downvoteButtonThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.moreButtonThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.expandButtonThemePreviewCommentsFragment.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
        binding.saveButtonThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.replyButtonThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));

        binding.linearLayoutAwardBackgroundThemePreviewCommentsFragment.setBackgroundColor(customTheme.awardedCommentBackgroundColor);
        binding.authorTypeImageViewAwardBackgroundThemePreviewCommentsFragment.setColorFilter(customTheme.moderator, android.graphics.PorterDuff.Mode.SRC_IN);
        binding.authorTextViewAwardBackgroundThemePreviewCommentsFragment.setTextColor(customTheme.moderator);
        binding.commentTimeTextViewAwardBackgroundThemePreviewCommentsFragment.setTextColor(customTheme.secondaryTextColor);
        binding.commentMarkdownViewAwardBackgroundThemePreviewCommentsFragment.setTextColor(customTheme.commentColor);
        binding.authorFlairTextViewAwardBackgroundThemePreviewCommentsFragment.setTextColor(customTheme.authorFlairTextColor);
        binding.dividerAwardBackgroundThemePreviewCommentsFragment.setBackgroundColor(customTheme.dividerColor);
        binding.upvoteButtonAwardBackgroundThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.upvoteButtonAwardBackgroundThemePreviewCommentsFragment.setTextColor(customTheme.commentIconAndInfoColor);
        binding.downvoteButtonAwardBackgroundThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.moreButtonAwardBackgroundThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.expandButtonAwardBackgroundThemePreviewCommentsFragment.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
        binding.saveButtonAwardBackgroundThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));
        binding.replyButtonAwardBackgroundThemePreviewCommentsFragment.setIconTint(ColorStateList.valueOf(customTheme.commentIconAndInfoColor));

        binding.linearLayoutFullyCollapsedThemePreviewCommentsFragment.setBackgroundColor(customTheme.fullyCollapsedCommentBackgroundColor);
        binding.authorTextViewFullyCollapsedThemePreviewCommentsFragment.setTextColor(customTheme.username);
        binding.scoreTextViewFullyCollapsedThemePreviewCommentsFragment.setTextColor(customTheme.secondaryTextColor);
        binding.timeTextViewFullyCollapsedThemePreviewCommentsFragment.setTextColor(customTheme.secondaryTextColor);

        if (activity.typeface != null) {
            binding.authorTextViewThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.commentTimeTextViewThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.authorFlairTextViewThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.upvoteButtonThemePreviewCommentsFragment.setTypeface(activity.typeface);

            binding.authorTextViewAwardBackgroundThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.commentTimeTextViewAwardBackgroundThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.authorFlairTextViewAwardBackgroundThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.upvoteButtonAwardBackgroundThemePreviewCommentsFragment.setTypeface(activity.typeface);

            binding.authorTextViewFullyCollapsedThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.scoreTextViewFullyCollapsedThemePreviewCommentsFragment.setTypeface(activity.typeface);
            binding.timeTextViewFullyCollapsedThemePreviewCommentsFragment.setTypeface(activity.typeface);
        }
        if (activity.contentTypeface != null) {
            binding.commentMarkdownViewThemePreviewCommentsFragment.setTypeface(activity.contentTypeface);
            binding.commentMarkdownViewAwardBackgroundThemePreviewCommentsFragment.setTypeface(activity.contentTypeface);
        }
        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CustomThemePreviewActivity) context;
    }
}
