package ml.docilealligator.infinityforreddit.fragments;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.CustomThemePreviewActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomTheme;
import ml.docilealligator.infinityforreddit.databinding.FragmentThemePreviewPostsBinding;

/**
 * A simple {@link Fragment} subclass.
 */
public class ThemePreviewPostsFragment extends Fragment {

    private FragmentThemePreviewPostsBinding binding;
    private CustomThemePreviewActivity activity;

    public ThemePreviewPostsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentThemePreviewPostsBinding.inflate(inflater, container, false);

        CustomTheme customTheme = activity.getCustomTheme();

        binding.cardViewThemePreviewPostsFragment.setBackgroundTintList(ColorStateList.valueOf(customTheme.cardViewBackgroundColor));
        Glide.with(this).load(R.drawable.subreddit_default_icon)
                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                .into(binding.iconGifImageViewThemePreviewPostsFragment);
        binding.subredditNameTextViewThemePreviewPostsFragment.setTextColor(customTheme.subreddit);
        binding.userTextViewThemePreviewPostsFragment.setTextColor(customTheme.username);
        binding.postTimeTextViewBestThemePreviewPostsFragment.setTextColor(customTheme.secondaryTextColor);
        binding.titleTextViewBestThemePreviewPostsFragment.setTextColor(customTheme.postTitleColor);
        binding.contentTextViewThemePreviewPostsFragment.setTextColor(customTheme.postContentColor);
        binding.stickiedPostImageViewThemePreviewPostsFragment.setColorFilter(customTheme.stickiedPostIconTint, PorterDuff.Mode.SRC_IN);
        binding.typeTextViewThemePreviewPostsFragment.setBackgroundColor(customTheme.postTypeBackgroundColor);
        binding.typeTextViewThemePreviewPostsFragment.setBorderColor(customTheme.postTypeBackgroundColor);
        binding.typeTextViewThemePreviewPostsFragment.setTextColor(customTheme.postTypeTextColor);
        binding.spoilerCustomTextViewThemePreviewPostsFragment.setBackgroundColor(customTheme.spoilerBackgroundColor);
        binding.spoilerCustomTextViewThemePreviewPostsFragment.setBorderColor(customTheme.spoilerBackgroundColor);
        binding.spoilerCustomTextViewThemePreviewPostsFragment.setTextColor(customTheme.spoilerTextColor);
        binding.nsfwTextViewThemePreviewPostsFragment.setBackgroundColor(customTheme.nsfwBackgroundColor);
        binding.nsfwTextViewThemePreviewPostsFragment.setBorderColor(customTheme.nsfwBackgroundColor);
        binding.nsfwTextViewThemePreviewPostsFragment.setTextColor(customTheme.nsfwTextColor);
        binding.flairCustomTextViewThemePreviewPostsFragment.setBackgroundColor(customTheme.flairBackgroundColor);
        binding.flairCustomTextViewThemePreviewPostsFragment.setBorderColor(customTheme.flairBackgroundColor);
        binding.flairCustomTextViewThemePreviewPostsFragment.setTextColor(customTheme.flairTextColor);
        binding.awardsTextViewThemePreviewPostsFragment.setBackgroundColor(customTheme.awardsBackgroundColor);
        binding.awardsTextViewThemePreviewPostsFragment.setBorderColor(customTheme.awardsBackgroundColor);
        binding.awardsTextViewThemePreviewPostsFragment.setTextColor(customTheme.awardsTextColor);
        binding.archivedImageViewThemePreviewPostsFragment.setColorFilter(customTheme.archivedTint, PorterDuff.Mode.SRC_IN);
        binding.lockedImageViewThemePreviewPostsFragment.setColorFilter(customTheme.lockedIconTint, PorterDuff.Mode.SRC_IN);
        binding.crosspostImageViewThemePreviewPostsFragment.setColorFilter(customTheme.crosspostIconTint, PorterDuff.Mode.SRC_IN);
        binding.linkTextViewThemePreviewPostsFragment.setTextColor(customTheme.secondaryTextColor);
        binding.progressBarThemePreviewPostsFragment.setIndeterminateTintList(ColorStateList.valueOf(customTheme.colorAccent));
        binding.imageViewNoPreviewLinkThemePreviewPostsFragment.setBackgroundColor(customTheme.noPreviewPostTypeBackgroundColor);
        binding.upvoteButtonThemePreviewPostsFragment.setIconTint(ColorStateList.valueOf(customTheme.postIconAndInfoColor));
        binding.upvoteButtonThemePreviewPostsFragment.setTextColor(customTheme.postIconAndInfoColor);
        binding.downvoteButtonThemePreviewPostsFragment.setIconTint(ColorStateList.valueOf(customTheme.postIconAndInfoColor));
        binding.commentsCountButtonThemePreviewPostsFragment.setTextColor(customTheme.postIconAndInfoColor);
        binding.commentsCountButtonThemePreviewPostsFragment.setIconTint(ColorStateList.valueOf(customTheme.postIconAndInfoColor));
        binding.saveButtonThemePreviewPostsFragment.setIconTint(ColorStateList.valueOf(customTheme.postIconAndInfoColor));
        binding.shareButtonThemePreviewPostsFragment.setIconTint(ColorStateList.valueOf(customTheme.postIconAndInfoColor));

        if (activity.typeface != null) {
            binding.subredditNameTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.userTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.postTimeTextViewBestThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.typeTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.spoilerCustomTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.nsfwTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.flairCustomTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.awardsTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.linkTextViewThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.upvoteButtonThemePreviewPostsFragment.setTypeface(activity.typeface);
            binding.commentsCountButtonThemePreviewPostsFragment.setTypeface(activity.typeface);
        }
        if (activity.titleTypeface != null) {
            binding.titleTextViewBestThemePreviewPostsFragment.setTypeface(activity.titleTypeface);
        }
        if (activity.contentTypeface != null) {
            binding.contentTextViewThemePreviewPostsFragment.setTypeface(activity.contentTypeface);
        }

        return binding.getRoot();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (CustomThemePreviewActivity) context;
    }
}
