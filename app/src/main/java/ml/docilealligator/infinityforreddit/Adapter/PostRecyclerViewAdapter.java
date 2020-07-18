package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.card.MaterialCardView;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import im.ene.toro.CacheManager;
import im.ene.toro.ToroPlayer;
import im.ene.toro.ToroUtil;
import im.ene.toro.exoplayer.ExoCreator;
import im.ene.toro.exoplayer.ExoPlayerViewHelper;
import im.ene.toro.exoplayer.Playable;
import im.ene.toro.media.PlaybackInfo;
import im.ene.toro.widget.Container;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Activity.FilteredThingActivity;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadSubredditIconAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadUserDataAsyncTask;
import ml.docilealligator.infinityforreddit.BottomSheetFragment.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.CustomView.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.Event.PostUpdateEventToDetailActivity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import ml.docilealligator.infinityforreddit.VoteThing;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

/**
 * Created by alex on 2/25/18.
 */

public class PostRecyclerViewAdapter extends PagedListAdapter<Post, RecyclerView.ViewHolder> implements CacheManager {
    private static final int VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE = 1;
    private static final int VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE = 2;
    private static final int VIEW_TYPE_POST_CARD_IMAGE_AND_GIF_AUTOPLAY_TYPE = 3;
    private static final int VIEW_TYPE_POST_CARD_LINK_TYPE = 4;
    private static final int VIEW_TYPE_POST_CARD_NO_PREVIEW_LINK_TYPE = 5;
    private static final int VIEW_TYPE_POST_CARD_TEXT_TYPE = 6;
    private static final int VIEW_TYPE_POST_CARD_GALLERY_TYPE = 7;
    private static final int VIEW_TYPE_POST_COMPACT = 8;
    private static final int VIEW_TYPE_ERROR = 9;
    private static final int VIEW_TYPE_LOADING = 10;
    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post post, @NonNull Post t1) {
            return post.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post post, @NonNull Post t1) {
            return false;
        }
    };
    private AppCompatActivity mActivity;
    private Retrofit mOauthRetrofit;
    private Retrofit mRetrofit;
    private int mImageViewWidth;
    private String mAccessToken;
    private RequestManager mGlide;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private Locale mLocale;
    private UserDao mUserDao;
    private boolean canStartActivity = true;
    private int mPostType;
    private int mPostLayout;
    private int mColorPrimaryLightTheme;
    private int mColorAccent;
    private int mCardViewBackgroundColor;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mPostTitleColor;
    private int mPostContentColor;
    private int mStickiedPostIconTint;
    private int mPostTypeBackgroundColor;
    private int mPostTypeTextColor;
    private int mSubredditColor;
    private int mUsernameColor;
    private int mSpoilerBackgroundColor;
    private int mSpoilerTextColor;
    private int mFlairBackgroundColor;
    private int mFlairTextColor;
    private int mAwardsBackgroundColor;
    private int mAwardsTextColor;
    private int mNSFWBackgroundColor;
    private int mNSFWTextColor;
    private int mArchivedIconTint;
    private int mLockedIconTint;
    private int mCrosspostIconTint;
    private int mNoPreviewLinkBackgroundColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mButtonTextColor;
    private int mPostIconAndInfoColor;
    private int mDividerColor;
    private float mScale;
    private boolean mDisplaySubredditName;
    private boolean mVoteButtonsOnTheRight;
    private boolean mNeedBlurNSFW;
    private boolean mNeedBlurSpoiler;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mShowDividerInCompactLayout;
    private boolean mShowAbsoluteNumberOfVotes;
    private boolean mAutoplay = false;
    private boolean mAutoplayNsfwVideos;
    private boolean mMuteAutoplayingVideos;
    private boolean mShowThumbnailOnTheRightInCompactLayout;
    private double mStartAutoplayVisibleAreaOffset;
    private boolean mMuteNSFWVideo;
    private Drawable mCommentIcon;
    private NetworkState networkState;
    private ExoCreator mExoCreator;
    private Callback mCallback;
    private ShareLinkBottomSheetFragment mShareLinkBottomSheetFragment;

    public PostRecyclerViewAdapter(AppCompatActivity activity, Retrofit oauthRetrofit, Retrofit retrofit,
                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                   CustomThemeWrapper customThemeWrapper, Locale locale, int imageViewWidth,
                                   String accessToken, int postType, int postLayout, boolean displaySubredditName,
                                   SharedPreferences sharedPreferences, ExoCreator exoCreator, Callback callback) {
        super(DIFF_CALLBACK);
        if (activity != null) {
            mActivity = activity;
            mOauthRetrofit = oauthRetrofit;
            mRetrofit = retrofit;
            mImageViewWidth = imageViewWidth;
            mAccessToken = accessToken;
            mPostType = postType;
            mDisplaySubredditName = displaySubredditName;
            mNeedBlurNSFW = sharedPreferences.getBoolean(SharedPreferencesUtils.BLUR_NSFW_KEY, true);
            mNeedBlurSpoiler = sharedPreferences.getBoolean(SharedPreferencesUtils.BLUR_SPOILER_KEY, false);
            mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
            mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
            mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
            mShowDividerInCompactLayout = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_DIVIDER_IN_COMPACT_LAYOUT, true);
            mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
            String autoplayString = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
                mAutoplay = true;
            } else if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                mAutoplay = Utils.isConnectedToWifi(activity);
            }
            mAutoplayNsfwVideos = sharedPreferences.getBoolean(SharedPreferencesUtils.AUTOPLAY_NSFW_VIDEOS, true);
            mMuteAutoplayingVideos = sharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_AUTOPLAYING_VIDEOS, true);
            mShowThumbnailOnTheRightInCompactLayout = sharedPreferences.getBoolean(
                    SharedPreferencesUtils.SHOW_THUMBNAIL_ON_THE_LEFT_IN_COMPACT_LAYOUT, false);

            Resources resources = activity.getResources();
            mStartAutoplayVisibleAreaOffset = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                    sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT, 75) / 100.0 :
                    sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE, 50) / 100.0;

            mMuteNSFWVideo = sharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false);

            mPostLayout = postLayout;

            mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
            mColorAccent = customThemeWrapper.getColorAccent();
            mCardViewBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
            mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
            mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
            mPostTitleColor = customThemeWrapper.getPostTitleColor();
            mPostContentColor = customThemeWrapper.getPostContentColor();
            mStickiedPostIconTint = customThemeWrapper.getStickiedPostIconTint();
            mPostTypeBackgroundColor = customThemeWrapper.getPostTypeBackgroundColor();
            mPostTypeTextColor = customThemeWrapper.getPostTypeTextColor();
            mSubredditColor = customThemeWrapper.getSubreddit();
            mUsernameColor = customThemeWrapper.getUsername();
            mSpoilerBackgroundColor = customThemeWrapper.getSpoilerBackgroundColor();
            mSpoilerTextColor = customThemeWrapper.getSpoilerTextColor();
            mFlairBackgroundColor = customThemeWrapper.getFlairBackgroundColor();
            mFlairTextColor = customThemeWrapper.getFlairTextColor();
            mAwardsBackgroundColor = customThemeWrapper.getAwardsBackgroundColor();
            mAwardsTextColor = customThemeWrapper.getAwardsTextColor();
            mNSFWBackgroundColor = customThemeWrapper.getNsfwBackgroundColor();
            mNSFWTextColor = customThemeWrapper.getNsfwTextColor();
            mArchivedIconTint = customThemeWrapper.getArchivedIconTint();
            mLockedIconTint = customThemeWrapper.getLockedIconTint();
            mCrosspostIconTint = customThemeWrapper.getCrosspostIconTint();
            mNoPreviewLinkBackgroundColor = customThemeWrapper.getNoPreviewLinkBackgroundColor();
            mUpvotedColor = customThemeWrapper.getUpvoted();
            mDownvotedColor = customThemeWrapper.getDownvoted();
            mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
            mButtonTextColor = customThemeWrapper.getButtonTextColor();
            mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
            mDividerColor = customThemeWrapper.getDividerColor();

            mCommentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
            if (mCommentIcon != null) {
                DrawableCompat.setTint(mCommentIcon, mPostIconAndInfoColor);
            }

            mScale = resources.getDisplayMetrics().density;
            mGlide = Glide.with(mActivity);
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mLocale = locale;
            mUserDao = redditDataRoomDatabase.userDao();
            mExoCreator = exoCreator;
            mCallback = callback;
            mShareLinkBottomSheetFragment = new ShareLinkBottomSheetFragment();
        }
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_CARD) {
                Post post = getItem(position);
                if (post != null) {
                    switch (post.getPostType()) {
                        case Post.VIDEO_TYPE:
                            if (mAutoplay) {
                                if (!mAutoplayNsfwVideos && post.isNSFW()) {
                                    return VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE;
                                }
                                return VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE;
                            }
                            return VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE;
                        case Post.GIF_TYPE:
                            if (mAutoplay) {
                                if (!mAutoplayNsfwVideos && post.isNSFW()) {
                                    return VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE;
                                }
                                return VIEW_TYPE_POST_CARD_IMAGE_AND_GIF_AUTOPLAY_TYPE;
                            }
                            return VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE;
                        case Post.IMAGE_TYPE:
                            return VIEW_TYPE_POST_CARD_IMAGE_AND_GIF_AUTOPLAY_TYPE;
                        case Post.LINK_TYPE:
                            return VIEW_TYPE_POST_CARD_LINK_TYPE;
                        case Post.NO_PREVIEW_LINK_TYPE:
                            return VIEW_TYPE_POST_CARD_NO_PREVIEW_LINK_TYPE;
                        case Post.GALLERY_TYPE:
                            return VIEW_TYPE_POST_CARD_GALLERY_TYPE;
                        default:
                            return VIEW_TYPE_POST_CARD_TEXT_TYPE;
                    }
                }
                return VIEW_TYPE_POST_CARD_TEXT_TYPE;
            } else {
                return VIEW_TYPE_POST_COMPACT;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE) {
            return new PostVideoAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_video_type_autoplay, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_VIDEO_AND_GIF_PREVIEW_TYPE) {
            return new PostVideoAndGifPreviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_video_and_gif_preview, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_IMAGE_AND_GIF_AUTOPLAY_TYPE) {
            return new PostImageAndGifAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_image_and_gif_autoplay, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_LINK_TYPE) {
            return new PostLinkTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_link, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_NO_PREVIEW_LINK_TYPE) {
            return new PostNoPreviewLinkTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_no_preview_link, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_GALLERY_TYPE) {
            return new PostGalleryTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_gallery, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_TEXT_TYPE) {
            return new PostTextTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_text, parent, false));
        } else if (viewType == VIEW_TYPE_POST_COMPACT) {
            if (mShowThumbnailOnTheRightInCompactLayout) {
                return new PostCompactRightThumbnailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_compact_right_thumbnail, parent, false));
            } else {
                return new PostCompactLeftThumbnailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_compact, parent, false));
            }
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostBaseViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                String subredditNamePrefixed = post.getSubredditNamePrefixed();
                String subredditName = subredditNamePrefixed.substring(2);
                String authorPrefixed = "u/" + post.getAuthor();
                String flair = post.getFlair();
                int nAwards = post.getnAwards();

                ((PostBaseViewHolder) holder).subredditTextView.setText(subredditNamePrefixed);
                ((PostBaseViewHolder) holder).userTextView.setText(authorPrefixed);

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mActivity != null && getItemCount() > 0) {
                                    if (iconImageUrl == null || iconImageUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconImageUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconImageUrl);
                                    }
                                }
                            }).execute();
                        } else if (!post.getAuthorIconUrl().equals("")) {
                            mGlide.load(post.getAuthorIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostBaseViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostBaseViewHolder) holder).iconGifImageView);
                        }
                    } else {
                        if (post.getSubredditIconUrl() == null) {
                            new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase, subredditName, mRetrofit,
                                    iconImageUrl -> {
                                        if (mActivity != null && getItemCount() > 0) {
                                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                                mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .into(((PostBaseViewHolder) holder).iconGifImageView);
                                            } else {
                                                mGlide.load(iconImageUrl)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                        .into(((PostBaseViewHolder) holder).iconGifImageView);
                                            }

                                            if (holder.getAdapterPosition() >= 0) {
                                                post.setSubredditIconUrl(iconImageUrl);
                                            }
                                        }
                                    }).execute();
                        } else if (!post.getSubredditIconUrl().equals("")) {
                            mGlide.load(post.getSubredditIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostBaseViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostBaseViewHolder) holder).iconGifImageView);
                        }
                    }
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mActivity != null && getItemCount() > 0) {
                                if (iconImageUrl == null || iconImageUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostBaseViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconImageUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostBaseViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconImageUrl);
                                }
                            }
                        }).execute();
                    } else if (!post.getAuthorIconUrl().equals("")) {
                        mGlide.load(post.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                    }
                }

                if (mShowElapsedTime) {
                    ((PostBaseViewHolder) holder).postTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
                } else {
                    ((PostBaseViewHolder) holder).postTimeTextView.setText(Utils.getFormattedTime(mLocale, post.getPostTimeMillis(), mTimeFormatPattern));
                }

                ((PostBaseViewHolder) holder).titleTextView.setText(post.getTitle());
                ((PostBaseViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                if (post.isLocked()) {
                    ((PostBaseViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (post.isNSFW()) {
                    ((PostBaseViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                if (post.isSpoiler()) {
                    ((PostBaseViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
                }

                if (flair != null && !flair.equals("")) {
                    ((PostBaseViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((PostBaseViewHolder) holder).flairTextView, flair);
                }

                if (nAwards > 0) {
                    ((PostBaseViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                    if (nAwards == 1) {
                        ((PostBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.one_award));
                    } else {
                        ((PostBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.n_awards, nAwards));
                    }
                }

                switch (post.getVoteType()) {
                    case 1:
                        //Upvoted
                        ((PostBaseViewHolder) holder).upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostBaseViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case -1:
                        //Downvoted
                        ((PostBaseViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostBaseViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (mPostType == PostDataSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
                    ((PostBaseViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    mGlide.load(R.drawable.ic_thumbtack_24dp).into(((PostBaseViewHolder) holder).stickiedPostImageView);
                }

                if (post.isArchived()) {
                    ((PostBaseViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);

                    ((PostBaseViewHolder) holder).upvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostBaseViewHolder) holder).downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostBaseViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                ((PostBaseViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));

                if (post.isSaved()) {
                    ((PostBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (holder instanceof PostVideoAutoplayViewHolder) {
                    ((PostVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio((float) post.getPreviewWidth() / post.getPreviewHeight());
                    ((PostVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.VISIBLE);
                    if (mImageViewWidth > post.getPreviewWidth()) {
                        mGlide.load(post.getPreviewUrl()).override(Target.SIZE_ORIGINAL).into(((PostVideoAutoplayViewHolder) holder).previewImageView);
                    } else {
                        mGlide.load(post.getPreviewUrl()).into(((PostVideoAutoplayViewHolder) holder).previewImageView);
                    }
                    ((PostVideoAutoplayViewHolder) holder).setVolume(mMuteAutoplayingVideos || (post.isNSFW() && mMuteNSFWVideo) ? 0f : 1f);
                    ((PostVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                } else if (holder instanceof PostVideoAndGifPreviewViewHolder) {
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        ((PostVideoAndGifPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.video));
                    } else {
                        ((PostVideoAndGifPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gif));
                    }
                    ((PostVideoAndGifPreviewViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((PostVideoAndGifPreviewViewHolder) holder).imageView
                            .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());
                    loadImage(holder, post);

                    if (post.getPreviewWidth() <= 0 || post.getPreviewHeight() <= 0) {
                        ((PostVideoAndGifPreviewViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ((PostVideoAndGifPreviewViewHolder) holder).imageView.getLayoutParams().height = (int) (400 * mScale);
                    }
                } else if (holder instanceof PostImageAndGifAutoplayViewHolder) {
                    if (post.getPostType() == Post.IMAGE_TYPE) {
                        ((PostImageAndGifAutoplayViewHolder) holder).typeTextView.setText(R.string.image);
                    } else {
                        ((PostImageAndGifAutoplayViewHolder) holder).typeTextView.setText(R.string.gif);
                    }
                    ((PostImageAndGifAutoplayViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    if (post.getPreviewWidth() <= 0 || post.getPreviewHeight() <= 0) {
                        ((PostImageAndGifAutoplayViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ((PostImageAndGifAutoplayViewHolder) holder).imageView.getLayoutParams().height = (int) (400 * mScale);
                    } else {
                        ((PostImageAndGifAutoplayViewHolder) holder).imageView
                                .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());
                    }
                    loadImage(holder, post);
                } else if (holder instanceof PostLinkTypeViewHolder) {
                    ((PostLinkTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((PostLinkTypeViewHolder) holder).imageView
                            .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());
                    loadImage(holder, post);

                    String domain = Uri.parse(post.getUrl()).getHost();
                    ((PostLinkTypeViewHolder) holder).linkTextView.setText(domain);
                } else if (holder instanceof PostNoPreviewLinkTypeViewHolder) {
                    ((PostNoPreviewLinkTypeViewHolder) holder).linkTextView.setText(Uri.parse(post.getUrl()).getHost());
                } else if (holder instanceof PostGalleryTypeViewHolder) {
                    if (post.getPreviewUrl() != null && !post.getPreviewUrl().equals("")) {
                        ((PostGalleryTypeViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.VISIBLE);
                        ((PostGalleryTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryTypeViewHolder) holder).imageView
                                .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());

                        loadImage(holder, post);
                    } else {
                        ((PostGalleryTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                    }
                } else if (holder instanceof PostTextTypeViewHolder) {
                    if (post.getSelfTextPlainTrimmed() != null && !post.getSelfTextPlainTrimmed().equals("")) {
                        ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
                        ((PostTextTypeViewHolder) holder).contentTextView.setText(post.getSelfTextPlainTrimmed());
                    }
                }
            }
        } else if (holder instanceof PostCompactBaseViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                final String subredditNamePrefixed = post.getSubredditNamePrefixed();
                String subredditName = subredditNamePrefixed.substring(2);
                String authorPrefixed = "u/" + post.getAuthor();
                final String title = post.getTitle();
                int voteType = post.getVoteType();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                int nAwards = post.getnAwards();
                boolean isArchived = post.isArchived();

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mActivity != null && getItemCount() > 0) {
                                    if (iconImageUrl == null || iconImageUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconImageUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconImageUrl);
                                    }
                                }
                            }).execute();
                        } else if (!post.getAuthorIconUrl().equals("")) {
                            mGlide.load(post.getAuthorIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                        }
                    } else {
                        if (post.getSubredditIconUrl() == null) {
                            new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase, subredditName, mRetrofit,
                                    iconImageUrl -> {
                                        if (mActivity != null && getItemCount() > 0) {
                                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                                mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                            } else {
                                                mGlide.load(iconImageUrl)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                        .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                            }

                                            if (holder.getAdapterPosition() >= 0) {
                                                post.setSubredditIconUrl(iconImageUrl);
                                            }
                                        }
                                    }).execute();
                        } else if (!post.getSubredditIconUrl().equals("")) {
                            mGlide.load(post.getSubredditIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                        }
                    }

                    ((PostCompactBaseViewHolder) holder).nameTextView.setTextColor(mSubredditColor);
                    ((PostCompactBaseViewHolder) holder).nameTextView.setText(subredditNamePrefixed);
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mActivity != null && getItemCount() > 0) {
                                if (iconImageUrl == null || iconImageUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconImageUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconImageUrl);
                                }
                            }
                        }).execute();
                    } else if (!post.getAuthorIconUrl().equals("")) {
                        mGlide.load(post.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                    }

                    ((PostCompactBaseViewHolder) holder).nameTextView.setTextColor(mUsernameColor);
                    ((PostCompactBaseViewHolder) holder).nameTextView.setText(authorPrefixed);
                }

                if (mShowElapsedTime) {
                    ((PostCompactBaseViewHolder) holder).postTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
                } else {
                    ((PostCompactBaseViewHolder) holder).postTimeTextView.setText(Utils.getFormattedTime(mLocale, post.getPostTimeMillis(), mTimeFormatPattern));
                }

                if (mShowDividerInCompactLayout) {
                    ((PostCompactBaseViewHolder) holder).divider.setVisibility(View.VISIBLE);
                } else {
                    ((PostCompactBaseViewHolder) holder).divider.setVisibility(View.GONE);
                }

                ((PostCompactBaseViewHolder) holder).titleTextView.setText(title);
                ((PostCompactBaseViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                if (post.isLocked()) {
                    ((PostCompactBaseViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (nsfw) {
                    ((PostCompactBaseViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                if (spoiler) {
                    ((PostCompactBaseViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
                }

                if (flair != null && !flair.equals("")) {
                    ((PostCompactBaseViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((PostCompactBaseViewHolder) holder).flairTextView, flair);
                }

                if (nAwards > 0) {
                    ((PostCompactBaseViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                    if (nAwards == 1) {
                        ((PostCompactBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.one_award));
                    } else {
                        ((PostCompactBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.n_awards, nAwards));
                    }
                }

                switch (voteType) {
                    case 1:
                        //Upvoted
                        ((PostCompactBaseViewHolder) holder).upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactBaseViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case -1:
                        //Downvoted
                        ((PostCompactBaseViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactBaseViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                    ((PostCompactBaseViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    if (post.getPostType() != Post.GIF_TYPE && post.getPostType() != Post.VIDEO_TYPE) {
                        ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    }
                    if (post.getPostType() == Post.GALLERY_TYPE && post.getPreviewUrl() == null || post.getPreviewUrl().equals("")) {
                        ((PostCompactBaseViewHolder) holder).noPreviewLinkImageFrameLayout.setVisibility(View.VISIBLE);
                    } else {
                        ((PostCompactBaseViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                    }
                    loadImage(holder, post);
                }

                if (mPostType == PostDataSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
                    ((PostCompactBaseViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    mGlide.load(R.drawable.ic_thumbtack_24dp).into(((PostCompactBaseViewHolder) holder).stickiedPostImageView);
                }

                if (isArchived) {
                    ((PostCompactBaseViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);

                    ((PostCompactBaseViewHolder) holder).upvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostCompactBaseViewHolder) holder).downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostCompactBaseViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.image);
                        break;
                    case Post.LINK_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.link);

                        ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setText(domain);
                        break;
                    case Post.GIF_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gif);
                        ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.video);
                        ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostCompactBaseViewHolder) holder).noPreviewLinkImageFrameLayout.setVisibility(View.VISIBLE);
                        break;
                    case Post.GALLERY_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gallery);
                        break;
                    case Post.TEXT_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.text);
                        break;
                }

                ((PostCompactBaseViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));

                if (post.isSaved()) {
                    ((PostCompactBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostCompactBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }
            }
        }
    }

    private void loadImage(final RecyclerView.ViewHolder holder, final Post post) {
        if (holder instanceof PostImageAndGifAutoplayViewHolder) {
            String url = post.getPostType() == Post.IMAGE_TYPE ? post.getPreviewUrl() : post.getUrl();
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostImageAndGifAutoplayViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostImageAndGifAutoplayViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                    ((PostImageAndGifAutoplayViewHolder) holder).errorRelativeLayout.setOnClickListener(view -> {
                        ((PostImageAndGifAutoplayViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostImageAndGifAutoplayViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostImageAndGifAutoplayViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                    ((PostImageAndGifAutoplayViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });

            if ((post.isNSFW() && mNeedBlurNSFW && !(post.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostImageAndGifAutoplayViewHolder) holder).imageView);
            } else {
                if (mImageViewWidth > post.getPreviewWidth()) {
                    imageRequestBuilder.override(Target.SIZE_ORIGINAL).into(((PostImageAndGifAutoplayViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostImageAndGifAutoplayViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostVideoAndGifPreviewViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostVideoAndGifPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostVideoAndGifPreviewViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                    ((PostVideoAndGifPreviewViewHolder) holder).errorRelativeLayout.setOnClickListener(view -> {
                        ((PostVideoAndGifPreviewViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostVideoAndGifPreviewViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostVideoAndGifPreviewViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                    ((PostVideoAndGifPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });

            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostVideoAndGifPreviewViewHolder) holder).imageView);
            } else {
                if (mImageViewWidth > post.getPreviewWidth()) {
                    imageRequestBuilder.override(Target.SIZE_ORIGINAL).into(((PostVideoAndGifPreviewViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostVideoAndGifPreviewViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostLinkTypeViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostLinkTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostLinkTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                    ((PostLinkTypeViewHolder) holder).errorRelativeLayout.setOnClickListener(view -> {
                        ((PostLinkTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostLinkTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostLinkTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                    ((PostLinkTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });

            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostLinkTypeViewHolder) holder).imageView);
            } else {
                if (mImageViewWidth > post.getPreviewWidth()) {
                    imageRequestBuilder.override(Target.SIZE_ORIGINAL).into(((PostLinkTypeViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostLinkTypeViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostGalleryTypeViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostGalleryTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostGalleryTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                    ((PostGalleryTypeViewHolder) holder).errorRelativeLayout.setOnClickListener(view -> {
                        ((PostGalleryTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostGalleryTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                    ((PostGalleryTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });

            if ((post.isNSFW() && mNeedBlurNSFW && !(post.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostGalleryTypeViewHolder) holder).imageView);
            } else {
                if (mImageViewWidth > post.getPreviewWidth()) {
                    imageRequestBuilder.override(Target.SIZE_ORIGINAL).into(((PostGalleryTypeViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostGalleryTypeViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostCompactBaseViewHolder) {
            String previewUrl = post.getThumbnailPreviewUrl().equals("") ? post.getPreviewUrl() : post.getThumbnailPreviewUrl();
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(previewUrl)
                    .error(R.drawable.ic_error_outline_black_24dp).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.GONE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    });
            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder
                        .transform(new BlurTransformation(50, 2)).into(((PostCompactBaseViewHolder) holder).imageView);
            } else {
                imageRequestBuilder.into(((PostCompactBaseViewHolder) holder).imageView);
            }
        }
    }

    private void shareLink(Post post) {
        Bundle bundle = new Bundle();
        bundle.putString(ShareLinkBottomSheetFragment.EXTRA_POST_LINK, post.getPermalink());
        if (post.getPostType() != Post.TEXT_TYPE) {
            bundle.putInt(ShareLinkBottomSheetFragment.EXTRA_MEDIA_TYPE, post.getPostType());
            switch (post.getPostType()) {
                case Post.IMAGE_TYPE:
                case Post.GIF_TYPE:
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, post.getUrl());
                    break;
                case Post.VIDEO_TYPE:
                    bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, post.getVideoDownloadUrl());
                    break;
            }
        }
        mShareLinkBottomSheetFragment.setArguments(bundle);
        mShareLinkBottomSheetFragment.show(mActivity.getSupportFragmentManager(), mShareLinkBottomSheetFragment.getTag());
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    public void setVoteButtonsPosition(boolean voteButtonsOnTheRight) {
        mVoteButtonsOnTheRight = voteButtonsOnTheRight;
    }

    public void setPostLayout(int postLayout) {
        mPostLayout = postLayout;
    }

    public void setBlurNSFW(boolean needBlurNSFW) {
        mNeedBlurNSFW = needBlurNSFW;
    }

    public void setBlurSpoiler(boolean needBlurSpoiler) {
        mNeedBlurSpoiler = needBlurSpoiler;
    }

    public void setShowElapsedTime(boolean showElapsedTime) {
        mShowElapsedTime = showElapsedTime;
    }

    public void setTimeFormat(String timeFormat) {
        mTimeFormatPattern = timeFormat;
    }

    public void setShowDividerInCompactLayout(boolean showDividerInCompactLayout) {
        mShowDividerInCompactLayout = showDividerInCompactLayout;
    }

    public void setShowAbsoluteNumberOfVotes(boolean showAbsoluteNumberOfVotes) {
        mShowAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(getItemCount() - 1);
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void removeFooter() {
        if (hasExtraRow()) {
            notifyItemRemoved(getItemCount() - 1);
        }

        networkState = null;
    }

    public void setAutoplay(boolean autoplay) {
        mAutoplay = autoplay;
    }

    public boolean isAutoplay() {
        return mAutoplay;
    }

    public void setAutoplayNsfwVideos(boolean autoplayNsfwVideos) {
        mAutoplayNsfwVideos = autoplayNsfwVideos;
    }

    public void setMuteAutoplayingVideos(boolean muteAutoplayingVideos) {
        mMuteAutoplayingVideos = muteAutoplayingVideos;
    }

    public void setShowThumbnailOnTheRightInCompactLayout(boolean showThumbnailOnTheRightInCompactLayout) {
        mShowThumbnailOnTheRightInCompactLayout = showThumbnailOnTheRightInCompactLayout;
    }

    public void setStartAutoplayVisibleAreaOffset(double startAutoplayVisibleAreaOffset) {
        this.mStartAutoplayVisibleAreaOffset = startAutoplayVisibleAreaOffset / 100.0;
    }

    public void setMuteNSFWVideo(boolean muteNSFWVideo) {
        this.mMuteNSFWVideo = muteNSFWVideo;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof PostBaseViewHolder) {
            if (holder instanceof PostVideoAutoplayViewHolder) {
                ((PostVideoAutoplayViewHolder) holder).muteButton.setVisibility(View.GONE);
                ((PostVideoAutoplayViewHolder) holder).resetVolume();
                mGlide.clear(((PostVideoAutoplayViewHolder) holder).previewImageView);
                ((PostVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostImageAndGifAutoplayViewHolder) {
                mGlide.clear(((PostImageAndGifAutoplayViewHolder) holder).imageView);
                ((PostImageAndGifAutoplayViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.FIT_START);
                ((PostImageAndGifAutoplayViewHolder) holder).imageView.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;
                ((PostImageAndGifAutoplayViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
            } else if (holder instanceof PostVideoAndGifPreviewViewHolder) {
                mGlide.clear(((PostVideoAndGifPreviewViewHolder) holder).imageView);
            } else if (holder instanceof PostLinkTypeViewHolder) {
                mGlide.clear(((PostLinkTypeViewHolder) holder).imageView);
                ((PostLinkTypeViewHolder) holder).imageView.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;
                ((PostLinkTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
            } else if (holder instanceof PostGalleryTypeViewHolder) {
                mGlide.clear(((PostGalleryTypeViewHolder) holder).imageView);
                ((PostGalleryTypeViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.GONE);
                ((PostGalleryTypeViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                ((PostGalleryTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostTextTypeViewHolder) {
                ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.GONE);
            }

            mGlide.clear(((PostBaseViewHolder) holder).iconGifImageView);
            ((PostBaseViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).flairTextView.setText("");
            ((PostBaseViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).awardsTextView.setText("");
            ((PostBaseViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostBaseViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostBaseViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (holder instanceof PostCompactBaseViewHolder) {
            mGlide.clear(((PostCompactBaseViewHolder) holder).imageView);
            mGlide.clear(((PostCompactBaseViewHolder) holder).iconGifImageView);
            ((PostCompactBaseViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).relativeLayout.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).flairTextView.setText("");
            ((PostCompactBaseViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).awardsTextView.setText("");
            ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).imageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).noPreviewLinkImageFrameLayout.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostCompactBaseViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostCompactBaseViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    @Nullable
    @Override
    public Object getKeyForOrder(int order) {
        if (super.getItemCount() <= 0 || order >= super.getItemCount()) {
            return null;
        }
        return getItem(order);
    }

    @Nullable
    @Override
    public Integer getOrderForKey(@NonNull Object key) {
        if (getCurrentList() != null && key instanceof Post) {
            return getCurrentList().indexOf(key);
        }

        return null;
    }

    public interface Callback {
        void retryLoadingMore();

        void typeChipClicked(int filter);
    }

    class PostBaseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardView;
        AspectRatioGifImageView iconGifImageView;
        TextView subredditTextView;
        TextView userTextView;
        ImageView stickiedPostImageView;
        TextView postTimeTextView;
        TextView titleTextView;
        CustomTextView typeTextView;
        ImageView archivedImageView;
        ImageView lockedImageView;
        ImageView crosspostImageView;
        CustomTextView nsfwTextView;
        CustomTextView spoilerTextView;
        CustomTextView flairTextView;
        CustomTextView awardsTextView;
        ConstraintLayout bottomConstraintLayout;
        ImageView upvoteButton;
        TextView scoreTextView;
        ImageView downvoteButton;
        TextView commentsCountTextView;
        ImageView saveButton;
        ImageView shareButton;

        PostBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(MaterialCardView cardView,
                         AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         CustomTextView typeTextView,
                         ImageView archivedImageView,
                         ImageView lockedImageView,
                         ImageView crosspostImageView,
                         CustomTextView nsfwTextView,
                         CustomTextView spoilerTextView,
                         CustomTextView flairTextView,
                         CustomTextView awardsTextView,
                         ConstraintLayout bottomConstraintLayout,
                         ImageView upvoteButton,
                         TextView scoreTextView,
                         ImageView downvoteButton,
                         TextView commentsCountTextView,
                         ImageView saveButton,
                         ImageView shareButton) {
            this.cardView = cardView;
            this.iconGifImageView = iconGifImageView;
            this.subredditTextView = subredditTextView;
            this.userTextView = userTextView;
            this.stickiedPostImageView = stickiedPostImageView;
            this.postTimeTextView = postTimeTextView;
            this.titleTextView = titleTextView;
            this.typeTextView = typeTextView;
            this.archivedImageView = archivedImageView;
            this.lockedImageView = lockedImageView;
            this.crosspostImageView = crosspostImageView;
            this.nsfwTextView = nsfwTextView;
            this.spoilerTextView = spoilerTextView;
            this.flairTextView = flairTextView;
            this.awardsTextView = awardsTextView;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.commentsCountTextView = commentsCountTextView;
            this.saveButton = saveButton;
            this.shareButton = shareButton;

            scoreTextView.setOnClickListener(null);

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountTextView.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            subredditTextView.setTextColor(mSubredditColor);
            userTextView.setTextColor(mUsernameColor);
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            stickiedPostImageView.setColorFilter(mStickiedPostIconTint, PorterDuff.Mode.SRC_IN);
            typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            typeTextView.setBorderColor(mPostTypeBackgroundColor);
            typeTextView.setTextColor(mPostTypeTextColor);
            spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            spoilerTextView.setTextColor(mSpoilerTextColor);
            nsfwTextView.setBackgroundColor(mNSFWBackgroundColor);
            nsfwTextView.setBorderColor(mNSFWBackgroundColor);
            nsfwTextView.setTextColor(mNSFWTextColor);
            flairTextView.setBackgroundColor(mFlairBackgroundColor);
            flairTextView.setBorderColor(mFlairBackgroundColor);
            flairTextView.setTextColor(mFlairTextColor);
            awardsTextView.setBackgroundColor(mAwardsBackgroundColor);
            awardsTextView.setBorderColor(mAwardsBackgroundColor);
            awardsTextView.setTextColor(mAwardsTextColor);
            archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentsCountTextView.setTextColor(mPostIconAndInfoColor);
            commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(mCommentIcon, null, null, null);
            saveButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            shareButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

            cardView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position >= 0 && canStartActivity) {
                    canStartActivity = false;

                    Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, getItem(position));
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, getAdapterPosition());
                    mActivity.startActivity(intent);
                }
            });

            userTextView.setOnClickListener(view -> {
                if (canStartActivity) {
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        canStartActivity = false;
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mActivity.startActivity(intent);
                    }
                }
            });

            if (mDisplaySubredditName) {
                subredditTextView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mActivity.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditName());
                                mActivity.startActivity(intent);
                            }
                        }
                    }
                });

                iconGifImageView.setOnClickListener(view -> subredditTextView.performClick());
            } else {
                subredditTextView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                                mActivity.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditName());
                                mActivity.startActivity(intent);
                            }
                        }
                    }
                });

                iconGifImageView.setOnClickListener(view -> userTextView.performClick());
            }

            if (!(mActivity instanceof FilteredThingActivity)) {
                nsfwTextView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                        intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                        intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                        intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                        mActivity.startActivity(intent);
                    }
                });
                typeTextView.setOnClickListener(view -> {
                    int position = getAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        mCallback.typeChipClicked(post.getPostType());
                    }
                });
            }

            upvoteButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isArchived()) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != 1) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                upvoteButton
                                        .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mUpvotedColor);
                            } else {
                                post.setVoteType(0);
                                upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, post.getFullName(), newVoteType, getAdapterPosition());
                }
            });

            downvoteButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isArchived()) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != -1) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        downvoteButton
                                .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                downvoteButton
                                        .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mDownvotedColor);
                            } else {
                                post.setVoteType(0);
                                downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, post.getFullName(), newVoteType, getAdapterPosition());
                }
            });

            saveButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isSaved()) {
                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(false);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    } else {
                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                }
            });

            shareButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    shareLink(post);
                }
            });
        }
    }

    class PostVideoAutoplayViewHolder extends PostBaseViewHolder implements ToroPlayer {
        @BindView(R.id.card_view_item_post_video_type_autoplay)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_video_type_autoplay)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_video_type_autoplay)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_video_type_autoplay)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_video_type_autoplay)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_video_type_autoplay)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_video_type_autoplay)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_video_type_autoplay)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_video_type_autoplay)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_video_type_autoplay)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_video_type_autoplay)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_video_type_autoplay)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_video_type_autoplay)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_video_type_autoplay)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_video_type_autoplay)
        CustomTextView awardsTextView;
        @BindView(R.id.aspect_ratio_frame_layout_item_post_video_type_autoplay)
        AspectRatioFrameLayout aspectRatioFrameLayout;
        @BindView(R.id.preview_image_view_item_post_video_type_autoplay)
        GifImageView previewImageView;
        @BindView(R.id.player_view_item_post_video_type_autoplay)
        PlayerView videoPlayer;
        @BindView(R.id.mute_exo_playback_control_view)
        ImageView muteButton;
        @BindView(R.id.fullscreen_exo_playback_control_view)
        ImageView fullscreenButton;
        @BindView(R.id.bottom_constraint_layout_item_post_video_type_autoplay)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_video_type_autoplay)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_video_type_autoplay)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_video_type_autoplay)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_video_type_autoplay)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_video_type_autoplay)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_video_type_autoplay)
        ImageView shareButton;

        @Nullable
        ExoPlayerViewHelper helper;
        private Uri mediaUri;
        private float volume;

        PostVideoAutoplayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            aspectRatioFrameLayout.setOnClickListener(null);

            muteButton.setOnClickListener(view -> {
                if (helper != null) {
                    if (helper.getVolume() != 0) {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_white_rounded_18dp));
                        helper.setVolume(0f);
                        volume = 0f;
                    } else {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_white_rounded_18dp));
                        helper.setVolume(1f);
                        volume = 1f;
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                    intent.setData(Uri.parse(post.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                    intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                    intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, post.getTitle());
                    intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, helper.getLatestPlaybackInfo().getResumePosition());
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                }
            });
        }

        void bindVideoUri(Uri videoUri) {
            mediaUri = videoUri;
        }

        void setVolume(float volume) {
            this.volume = volume;
        }

        void resetVolume() {
            volume = 0f;
        }

        @NonNull
        @Override
        public View getPlayerView() {
            return videoPlayer;
        }

        @NonNull
        @Override
        public PlaybackInfo getCurrentPlaybackInfo() {
            return helper != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.EventListener() {
                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.length; i++) {
                                String mimeType = trackGroups.get(i).getFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_white_rounded_18dp));
                                    } else {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_white_rounded_18dp));
                                    }
                                    break;
                                }
                            }
                        } else {
                            muteButton.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onMetadata(Metadata metadata) {

                    }

                    @Override
                    public void onCues(List<Cue> cues) {

                    }

                    @Override
                    public void onRenderedFirstFrame() {
                        mGlide.clear(previewImageView);
                        previewImageView.setVisibility(View.GONE);
                    }
                });
            }
            helper.initialize(container, playbackInfo);
        }

        @Override
        public void play() {
            if (helper != null) helper.play();
        }

        @Override
        public void pause() {
            if (helper != null) helper.pause();
        }

        @Override
        public boolean isPlaying() {
            return helper != null && helper.isPlaying();
        }

        @Override
        public void release() {
            if (helper != null) {
                helper.release();
                helper = null;
            }
        }

        @Override
        public boolean wantsToPlay() {
            return ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        @Override
        public int getPlayerOrder() {
            return getAdapterPosition();
        }
    }

    class PostVideoAndGifPreviewViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_video_and_gif_preview)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_video_and_gif_preview)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_video_and_gif_preview)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_video_and_gif_preview)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_video_and_gif_preview)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_video_and_gif_preview)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_video_and_gif_preview)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_video_and_gif_preview)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_video_and_gif_preview)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_video_and_gif_preview)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_video_and_gif_preview)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_video_and_gif_preview)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_video_and_gif_preview)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_video_and_gif_preview)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_video_and_gif_preview)
        CustomTextView awardsTextView;
        @BindView(R.id.progress_bar_item_post_video_and_gif_preview)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_video_and_gif_preview)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_post_video_and_gif_preview)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.load_image_error_text_view_item_post_video_and_gif_preview)
        TextView errorTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_video_and_gif_preview)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_video_and_gif_preview)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_video_and_gif_preview)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_video_and_gif_preview)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_video_and_gif_preview)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_video_and_gif_preview)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_video_and_gif_preview)
        ImageView shareButton;

        PostVideoAndGifPreviewViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            errorTextView.setTextColor(mPrimaryTextColor);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        intent.setData(Uri.parse(post.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                        intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                        intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                        intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, post.getTitle());
                        intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                        mActivity.startActivity(intent);
                    } else if (post.getPostType() == Post.GIF_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, post.getSubredditName()
                                + "-" + post.getId() + ".gif");
                        intent.putExtra(ViewImageOrGifActivity.GIF_URL_KEY, post.getVideoUrl());
                        intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, post.getTitle());
                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }

    class PostImageAndGifAutoplayViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_image_and_gif_autoplay)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_image_and_gif_autoplay)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_image_and_gif_autoplay)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_image_and_gif_autoplay)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_image_and_gif_autoplay)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_image_and_gif_autoplay)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_image_and_gif_autoplay)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_image_and_gif_autoplay)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_image_and_gif_autoplay)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_image_and_gif_autoplay)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_image_and_gif_autoplay)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_image_and_gif_autoplay)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_image_and_gif_autoplay)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_image_and_gif_autoplay)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_image_and_gif_autoplay)
        CustomTextView awardsTextView;
        @BindView(R.id.progress_bar_item_post_image_and_gif_autoplay)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_image_and_gif_autoplay)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_post_image_and_gif_autoplay)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.load_image_error_text_view_item_post_image_and_gif_autoplay)
        TextView errorTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_image_and_gif_autoplay)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_image_and_gif_autoplay)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_image_and_gif_autoplay)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_image_and_gif_autoplay)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_image_and_gif_autoplay)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_image_and_gif_autoplay)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_image_and_gif_autoplay)
        ImageView shareButton;

        PostImageAndGifAutoplayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            errorTextView.setTextColor(mPrimaryTextColor);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.getPostType() == Post.IMAGE_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.IMAGE_URL_KEY, post.getUrl());
                        intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, post.getSubredditName()
                                + "-" + post.getId() + ".jpg");
                        intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, post.getTitle());
                        mActivity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, post.getSubredditName()
                                + "-" + post.getId() + ".gif");
                        intent.putExtra(ViewImageOrGifActivity.GIF_URL_KEY, post.getVideoUrl());
                        intent.putExtra(ViewImageOrGifActivity.POST_TITLE_KEY, post.getTitle());
                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }

    class PostLinkTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_link)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_link)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_link)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_link)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_link)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_link)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_link)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_link)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_link)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_link)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_link)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_link)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_link)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_link)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_link)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_link)
        TextView linkTextView;
        @BindView(R.id.progress_bar_item_post_link)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_link)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_post_link)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.load_image_error_text_view_item_post_link)
        TextView errorTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_link)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_link)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_link)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_link)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_link)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_link)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_link)
        ImageView shareButton;

        PostLinkTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            linkTextView.setTextColor(mSecondaryTextColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            errorTextView.setTextColor(mPrimaryTextColor);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(post.getUrl());
                    if (uri.getScheme() == null && uri.getHost() == null) {
                        intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                    } else {
                        intent.setData(uri);
                    }
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                }
            });
        }
    }

    class PostNoPreviewLinkTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_no_preview_link_type)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_no_preview_link_type)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_no_preview_link_type)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_no_preview_link_type)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_no_preview_link_type)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_no_preview_link_type)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_no_preview_link_type)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_no_preview_link_type)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_no_preview_link_type)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_no_preview_link_type)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_no_preview_link_type)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_no_preview_link_type)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_no_preview_link_type)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_no_preview_link_type)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_no_preview_link_type)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_no_preview_link_type)
        TextView linkTextView;
        @BindView(R.id.image_view_no_preview_link_item_post_no_preview_link_type)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_no_preview_link_type)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_no_preview_link_type)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_no_preview_link_type)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_no_preview_link_type)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_no_preview_link_type)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_no_preview_link_type)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_no_preview_link_type)
        ImageView shareButton;

        PostNoPreviewLinkTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            linkTextView.setTextColor(mSecondaryTextColor);
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);

            noPreviewLinkImageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(post.getUrl());
                    if (uri.getScheme() == null && uri.getHost() == null) {
                        intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                    } else {
                        intent.setData(uri);
                    }
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                }
            });
        }
    }

    class PostGalleryTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_gallery_type)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_gallery_type)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_gallery_type)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_gallery_type)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_gallery_type)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_gallery_type)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_gallery_type)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_gallery_type)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_gallery_type)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_gallery_type)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_gallery_type)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_gallery_type)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_gallery_type)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_gallery_type)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_gallery_type)
        CustomTextView awardsTextView;
        @BindView(R.id.image_wrapper_relative_layout_item_post_gallery)
        RelativeLayout imageWrapperRelativeLayout;
        @BindView(R.id.progress_bar_item_post_gallery_type)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_gallery_type)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_post_gallery_type)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.load_image_error_text_view_item_post_gallery_type)
        TextView errorTextView;
        @BindView(R.id.image_view_no_preview_gallery_item_post_gallery_type)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_gallery_type)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_gallery_type)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_gallery_type)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_gallery_type)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_gallery_type)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_gallery_type)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_gallery_type)
        ImageView shareButton;

        PostGalleryTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            noPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            errorTextView.setTextColor(mPrimaryTextColor);

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                    intent.putParcelableArrayListExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, post.getGallery());
                    mActivity.startActivity(intent);
                }
            });

            noPreviewLinkImageView.setOnClickListener(view -> {
                imageView.performClick();
            });
        }
    }

    class PostTextTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.card_view_item_post_text_type)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post_text_type)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_text_type)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_text_type)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_text_type)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_text_type)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_text_type)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_text_type)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_text_type)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_text_type)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_text_type)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_text_type)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_text_type)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_text_type)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_text_type)
        CustomTextView awardsTextView;
        @BindView(R.id.content_text_view_item_post_text_type)
        TextView contentTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_text_type)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_text_type)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_text_type)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_text_type)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_text_type)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_text_type)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_text_type)
        ImageView shareButton;

        PostTextTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(cardView,
                    iconGifImageView,
                    subredditTextView,
                    userTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    archivedImageView,
                    lockedImageView,
                    crosspostImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountTextView,
                    saveButton,
                    shareButton);

            contentTextView.setTextColor(mPostContentColor);
        }
    }

    class PostCompactBaseViewHolder extends RecyclerView.ViewHolder {
        AspectRatioGifImageView iconGifImageView;
        TextView nameTextView;
        ImageView stickiedPostImageView;
        TextView postTimeTextView;
        ConstraintLayout titleAndImageConstraintLayout;
        TextView titleTextView;
        CustomTextView typeTextView;
        ImageView archivedImageView;
        ImageView lockedImageView;
        ImageView crosspostImageView;
        CustomTextView nsfwTextView;
        CustomTextView spoilerTextView;
        CustomTextView flairTextView;
        CustomTextView awardsTextView;
        TextView linkTextView;
        RelativeLayout relativeLayout;
        ProgressBar progressBar;
        ImageView imageView;
        ImageView playButtonImageView;
        FrameLayout noPreviewLinkImageFrameLayout;
        Barrier imageBarrier;
        ConstraintLayout bottomConstraintLayout;
        ImageView upvoteButton;
        TextView scoreTextView;
        ImageView downvoteButton;
        TextView commentsCountTextView;
        ImageView saveButton;
        ImageView shareButton;
        View divider;

        PostCompactBaseViewHolder(View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                                         TextView nameTextView, ImageView stickiedPostImageView,
                                         TextView postTimeTextView, ConstraintLayout titleAndImageConstraintLayout,
                                         TextView titleTextView, CustomTextView typeTextView,
                                         ImageView archivedImageView, ImageView lockedImageView,
                                         ImageView crosspostImageView, CustomTextView nsfwTextView,
                                         CustomTextView spoilerTextView, CustomTextView flairTextView,
                                         CustomTextView awardsTextView, TextView linkTextView,
                                         RelativeLayout relativeLayout, ProgressBar progressBar,
                                         ImageView imageView, ImageView playButtonImageView,
                                         FrameLayout noPreviewLinkImageFrameLayout,
                                         ImageView noPreviewLinkImageView, Barrier imageBarrier,
                                         ConstraintLayout bottomConstraintLayout, ImageView upvoteButton,
                                         TextView scoreTextView, ImageView downvoteButton,
                                         TextView commentsCountTextView, ImageView saveButton,
                                         ImageView shareButton, View divider) {
            this.iconGifImageView = iconGifImageView;
            this.nameTextView = nameTextView;
            this.stickiedPostImageView = stickiedPostImageView;
            this.postTimeTextView = postTimeTextView;
            this.titleAndImageConstraintLayout = titleAndImageConstraintLayout;
            this.titleTextView = titleTextView;
            this.typeTextView = typeTextView;
            this.archivedImageView = archivedImageView;
            this.lockedImageView = lockedImageView;
            this.crosspostImageView = crosspostImageView;
            this.nsfwTextView = nsfwTextView;
            this.spoilerTextView = spoilerTextView;
            this.flairTextView = flairTextView;
            this.awardsTextView = awardsTextView;
            this.linkTextView = linkTextView;
            this.relativeLayout = relativeLayout;
            this.progressBar = progressBar;
            this.imageView = imageView;
            this.playButtonImageView = playButtonImageView;
            this.noPreviewLinkImageFrameLayout = noPreviewLinkImageFrameLayout;
            this.imageBarrier = imageBarrier;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.commentsCountTextView = commentsCountTextView;
            this.saveButton = saveButton;
            this.shareButton = shareButton;
            this.divider = divider;

            scoreTextView.setOnClickListener(null);

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountTextView.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            stickiedPostImageView.setColorFilter(mStickiedPostIconTint, PorterDuff.Mode.SRC_IN);
            typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            typeTextView.setBorderColor(mPostTypeBackgroundColor);
            typeTextView.setTextColor(mPostTypeTextColor);
            spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            spoilerTextView.setTextColor(mSpoilerTextColor);
            nsfwTextView.setBackgroundColor(mNSFWBackgroundColor);
            nsfwTextView.setBorderColor(mNSFWBackgroundColor);
            nsfwTextView.setTextColor(mNSFWTextColor);
            flairTextView.setBackgroundColor(mFlairBackgroundColor);
            flairTextView.setBorderColor(mFlairBackgroundColor);
            flairTextView.setTextColor(mFlairTextColor);
            awardsTextView.setBackgroundColor(mAwardsBackgroundColor);
            awardsTextView.setBorderColor(mAwardsBackgroundColor);
            awardsTextView.setTextColor(mAwardsTextColor);
            archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            linkTextView.setTextColor(mSecondaryTextColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);
            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentsCountTextView.setTextColor(mPostIconAndInfoColor);
            commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(mCommentIcon, null, null, null);
            saveButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            shareButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            divider.setBackgroundColor(mDividerColor);

            imageView.setClipToOutline(true);
            noPreviewLinkImageFrameLayout.setClipToOutline(true);

            itemView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && canStartActivity) {
                    canStartActivity = false;

                    Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, getAdapterPosition());
                    mActivity.startActivity(intent);
                }
            });

            nameTextView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && canStartActivity) {
                    canStartActivity = false;
                    if (mDisplaySubredditName) {
                        if (post.getSubredditNamePrefixed().startsWith("u/")) {
                            Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                    post.getSubredditNamePrefixed().substring(2));
                            mActivity.startActivity(intent);
                        } else {
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                    post.getSubredditNamePrefixed().substring(2));
                            mActivity.startActivity(intent);
                        }
                    } else {
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mActivity.startActivity(intent);
                    }
                }
            });

            iconGifImageView.setOnClickListener(view -> nameTextView.performClick());

            nsfwTextView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && !(mActivity instanceof FilteredThingActivity)) {
                    Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                    intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                    intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                    intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                    mActivity.startActivity(intent);
                }
            });

            typeTextView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && !(mActivity instanceof FilteredThingActivity)) {
                    mCallback.typeChipClicked(post.getPostType());
                }
            });

            imageView.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    switch (post.getPostType()) {
                        case Post.IMAGE_TYPE: {
                            Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                            intent.putExtra(ViewImageOrGifActivity.IMAGE_URL_KEY, post.getUrl());
                            intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, post.getSubredditName()
                                    + "-" + post.getId() + ".jpg");
                            mActivity.startActivity(intent);
                            break;
                        }
                        case Post.LINK_TYPE: {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                            mActivity.startActivity(intent);
                            break;
                        }
                        case Post.GIF_TYPE: {
                            Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewImageOrGifActivity.FILE_NAME_KEY, post.getSubredditName()
                                    + "-" + post.getId() + ".gif");
                            intent.putExtra(ViewImageOrGifActivity.GIF_URL_KEY, post.getVideoUrl());
                            mActivity.startActivity(intent);
                            break;
                        }
                        case Post.VIDEO_TYPE: {
                            Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                            intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                            mActivity.startActivity(intent);
                            break;
                        }
                        case Post.GALLERY_TYPE: {
                            Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                            intent.putExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, post.getGallery());
                            mActivity.startActivity(intent);
                        }
                    }
                }
            });

            noPreviewLinkImageFrameLayout.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.getPostType() == Post.GALLERY_TYPE) {
                        Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                        intent.putExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, post.getGallery());
                        mActivity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                        Uri uri = Uri.parse(post.getUrl());
                        if (uri.getScheme() == null && uri.getHost() == null) {
                            intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                        } else {
                            intent.setData(uri);
                        }
                        intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                        mActivity.startActivity(intent);
                    }
                }
            });

            upvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.isArchived()) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != 1) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                upvoteButton
                                        .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mUpvotedColor);
                            } else {
                                post.setVoteType(0);
                                upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, post.getFullName(), newVoteType, getAdapterPosition());
                }
            });

            downvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.isArchived()) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != -1) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        downvoteButton
                                .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                downvoteButton
                                        .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mDownvotedColor);
                            } else {
                                post.setVoteType(0);
                                downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, post.getFullName(), newVoteType, getAdapterPosition());
                }
            });

            saveButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (post.isSaved()) {
                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(false);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    } else {
                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                }
            });

            shareButton.setOnClickListener(view -> {
                int position = getAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    shareLink(post);
                }
            });
        }
    }

    class PostCompactLeftThumbnailViewHolder extends PostCompactBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_compact)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.name_text_view_item_post_compact)
        TextView nameTextView;
        @BindView(R.id.stickied_post_image_view_item_post_compact)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_compact)
        TextView postTimeTextView;
        @BindView(R.id.title_and_image_constraint_layout)
        ConstraintLayout titleAndImageConstraintLayout;
        @BindView(R.id.title_text_view_item_post_compact)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_compact)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_compact)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_compact)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_compact)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_compact)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_compact)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_compact)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_compact)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_compact)
        TextView linkTextView;
        @BindView(R.id.image_view_wrapper_item_post_compact)
        RelativeLayout relativeLayout;
        @BindView(R.id.progress_bar_item_post_compact)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_compact)
        ImageView imageView;
        @BindView(R.id.play_button_image_view_item_post_compact)
        ImageView playButtonImageView;
        @BindView(R.id.frame_layout_image_view_no_preview_link_item_post_compact)
        FrameLayout noPreviewLinkImageFrameLayout;
        @BindView(R.id.image_view_no_preview_link_item_post_compact)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.barrier2)
        Barrier imageBarrier;
        @BindView(R.id.bottom_constraint_layout_item_post_compact)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_compact)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_compact)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_compact)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_compact)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_compact)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_compact)
        ImageView shareButton;
        @BindView(R.id.divider_item_post_compact)
        View divider;

        PostCompactLeftThumbnailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            setBaseView(iconGifImageView, nameTextView, stickiedPostImageView, postTimeTextView,
                    titleAndImageConstraintLayout, titleTextView, typeTextView, archivedImageView,
                    lockedImageView, crosspostImageView, nsfwTextView, spoilerTextView,
                    flairTextView, awardsTextView, linkTextView, relativeLayout, progressBar, imageView,
                    playButtonImageView, noPreviewLinkImageFrameLayout, noPreviewLinkImageView,
                    imageBarrier, bottomConstraintLayout, upvoteButton, scoreTextView, downvoteButton,
                    commentsCountTextView, saveButton, shareButton, divider);
        }
    }

    class PostCompactRightThumbnailViewHolder extends PostCompactBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_compact_right_thumbnail)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.name_text_view_item_post_compact_right_thumbnail)
        TextView nameTextView;
        @BindView(R.id.stickied_post_image_view_item_post_compact_right_thumbnail)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_compact_right_thumbnail)
        TextView postTimeTextView;
        @BindView(R.id.title_and_image_constraint_layout)
        ConstraintLayout titleAndImageConstraintLayout;
        @BindView(R.id.title_text_view_item_post_compact_right_thumbnail)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_compact_right_thumbnail)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_compact_right_thumbnail)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_compact_right_thumbnail)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_compact_right_thumbnail)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_compact_right_thumbnail)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_compact_right_thumbnail)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_compact_right_thumbnail)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_compact_right_thumbnail)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_compact_right_thumbnail)
        TextView linkTextView;
        @BindView(R.id.image_view_wrapper_item_post_compact_right_thumbnail)
        RelativeLayout relativeLayout;
        @BindView(R.id.progress_bar_item_post_compact_right_thumbnail)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_compact_right_thumbnail)
        ImageView imageView;
        @BindView(R.id.play_button_image_view_item_post_compact_right_thumbnail)
        ImageView playButtonImageView;
        @BindView(R.id.frame_layout_image_view_no_preview_link_item_post_compact_right_thumbnail)
        FrameLayout noPreviewLinkImageFrameLayout;
        @BindView(R.id.image_view_no_preview_link_item_post_compact_right_thumbnail)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.barrier2)
        Barrier imageBarrier;
        @BindView(R.id.bottom_constraint_layout_item_post_compact_right_thumbnail)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_compact_right_thumbnail)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_compact_right_thumbnail)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_compact_right_thumbnail)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_compact_right_thumbnail)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_compact_right_thumbnail)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_compact_right_thumbnail)
        ImageView shareButton;
        @BindView(R.id.divider_item_post_compact_right_thumbnail)
        View divider;

        PostCompactRightThumbnailViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            setBaseView(iconGifImageView, nameTextView, stickiedPostImageView, postTimeTextView,
                    titleAndImageConstraintLayout, titleTextView, typeTextView, archivedImageView,
                    lockedImageView, crosspostImageView, nsfwTextView, spoilerTextView,
                    flairTextView, awardsTextView, linkTextView, relativeLayout, progressBar, imageView,
                    playButtonImageView, noPreviewLinkImageFrameLayout, noPreviewLinkImageView,
                    imageBarrier, bottomConstraintLayout, upvoteButton, scoreTextView, downvoteButton,
                    commentsCountTextView, saveButton, shareButton, divider);
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error)
        Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            errorTextView.setText(R.string.load_more_posts_error);
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setOnClickListener(view -> mCallback.retryLoadingMore());
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
            itemView.setOnClickListener(view -> retryButton.performClick());
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_footer_loading)
        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }
}
