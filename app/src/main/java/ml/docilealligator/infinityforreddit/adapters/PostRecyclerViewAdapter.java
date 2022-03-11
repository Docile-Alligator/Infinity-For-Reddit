package ml.docilealligator.infinityforreddit.adapters;

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
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlayerView;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

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
import ml.docilealligator.infinityforreddit.FetchGfycatOrRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.FetchStreamableVideo;
import ml.docilealligator.infinityforreddit.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.StreamableVideo;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.FilteredPostsActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.apis.GfycatAPI;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by alex on 2/25/18.
 */

public class PostRecyclerViewAdapter extends PagingDataAdapter<Post, RecyclerView.ViewHolder> implements CacheManager {
    private static final int VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE = 1;
    private static final int VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE = 2;
    private static final int VIEW_TYPE_POST_CARD_TEXT_TYPE = 3;
    private static final int VIEW_TYPE_POST_COMPACT = 4;
    private static final int VIEW_TYPE_POST_GALLERY = 5;
    private static final int VIEW_TYPE_POST_CARD_2_VIDEO_AUTOPLAY_TYPE = 6;
    private static final int VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE = 7;
    private static final int VIEW_TYPE_POST_CARD_2_TEXT_TYPE = 8;

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

    private BaseActivity mActivity;
    private PostFragment mFragment;
    private SharedPreferences mSharedPreferences;
    private Executor mExecutor;
    private Retrofit mOauthRetrofit;
    private Retrofit mGfycatRetrofit;
    private Retrofit mRedgifsRetrofit;
    private Retrofit mStreamableRetrofit;
    private String mAccessToken;
    private RequestManager mGlide;
    private int mMaxResolution;
    private SaveMemoryCenterInisdeDownsampleStrategy mSaveMemoryCenterInsideDownsampleStrategy;
    private Locale mLocale;
    private boolean canStartActivity = true;
    private int mPostType;
    private int mPostLayout;
    private int mDefaultLinkPostLayout;
    private int mColorAccent;
    private int mCardViewBackgroundColor;
    private int mReadPostCardViewBackgroundColor;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mPostTitleColor;
    private int mPostContentColor;
    private int mReadPostTitleColor;
    private int mReadPostContentColor;
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
    private int mMediaIndicatorIconTint;
    private int mMediaIndicatorBackgroundColor;
    private int mNoPreviewPostTypeBackgroundColor;
    private int mNoPreviewPostTypeIconTint;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mPostIconAndInfoColor;
    private int mDividerColor;
    private float mScale;
    private boolean mDisplaySubredditName;
    private boolean mVoteButtonsOnTheRight;
    private boolean mNeedBlurNsfw;
    private boolean mDoNotBlurNsfwInNsfwSubreddits;
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
    private boolean mAutomaticallyTryRedgifs;
    private boolean mLongPressToHideToolbarInCompactLayout;
    private boolean mCompactLayoutToolbarHiddenByDefault;
    private boolean mDataSavingMode = false;
    private boolean mDisableImagePreview;
    private boolean mOnlyDisablePreviewInVideoAndGifPosts;
    private boolean mMarkPostsAsRead;
    private boolean mMarkPostsAsReadAfterVoting;
    private boolean mMarkPostsAsReadOnScroll;
    private boolean mHidePostType;
    private boolean mHidePostFlair;
    private boolean mHideTheNumberOfAwards;
    private boolean mHideSubredditAndUserPrefix;
    private boolean mHideTheNumberOfVotes;
    private boolean mHideTheNumberOfComments;
    private boolean mLegacyAutoplayVideoControllerUI;
    private boolean mFixedHeightPreviewInCard;
    private boolean mHideTextPostContent;
    private Drawable mCommentIcon;
    private ExoCreator mExoCreator;
    private Callback mCallback;
    private boolean canPlayVideo = true;

    public PostRecyclerViewAdapter(BaseActivity activity, PostFragment fragment, Executor executor, Retrofit oauthRetrofit,
                                   Retrofit gfycatRetrofit, Retrofit redgifsRetrofit, Retrofit streambleRetrofit,
                                   CustomThemeWrapper customThemeWrapper, Locale locale,
                                   String accessToken, String accountName, int postType, int postLayout, boolean displaySubredditName,
                                   SharedPreferences sharedPreferences, SharedPreferences nsfwAndSpoilerSharedPreferences,
                                   SharedPreferences postHistorySharedPreferences,
                                   ExoCreator exoCreator, Callback callback) {
        super(DIFF_CALLBACK);
        if (activity != null) {
            mActivity = activity;
            mFragment = fragment;
            mSharedPreferences = sharedPreferences;
            mExecutor = executor;
            mOauthRetrofit = oauthRetrofit;
            mGfycatRetrofit = gfycatRetrofit;
            mRedgifsRetrofit = redgifsRetrofit;
            mStreamableRetrofit = streambleRetrofit;
            mAccessToken = accessToken;
            mPostType = postType;
            mDisplaySubredditName = displaySubredditName;
            mNeedBlurNsfw = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
            mDoNotBlurNsfwInNsfwSubreddits = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
            mNeedBlurSpoiler = nsfwAndSpoilerSharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
            mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
            mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
            mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
            mShowDividerInCompactLayout = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_DIVIDER_IN_COMPACT_LAYOUT, true);
            mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
            String autoplayString = sharedPreferences.getString(SharedPreferencesUtils.VIDEO_AUTOPLAY, SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_NEVER);
            int networkType = Utils.getConnectedNetwork(activity);
            if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ALWAYS_ON)) {
                mAutoplay = true;
            } else if (autoplayString.equals(SharedPreferencesUtils.VIDEO_AUTOPLAY_VALUE_ON_WIFI)) {
                mAutoplay = networkType == Utils.NETWORK_TYPE_WIFI;
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
            mAutomaticallyTryRedgifs = sharedPreferences.getBoolean(SharedPreferencesUtils.AUTOMATICALLY_TRY_REDGIFS, true);

            mLongPressToHideToolbarInCompactLayout = sharedPreferences.getBoolean(SharedPreferencesUtils.LONG_PRESS_TO_HIDE_TOOLBAR_IN_COMPACT_LAYOUT, false);
            mCompactLayoutToolbarHiddenByDefault = sharedPreferences.getBoolean(SharedPreferencesUtils.POST_COMPACT_LAYOUT_TOOLBAR_HIDDEN_BY_DEFAULT, false);

            String dataSavingModeString = sharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
                mDataSavingMode = true;
            } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                mDataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
            }
            mDisableImagePreview = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW, false);
            mOnlyDisablePreviewInVideoAndGifPosts = sharedPreferences.getBoolean(SharedPreferencesUtils.ONLY_DISABLE_PREVIEW_IN_VIDEO_AND_GIF_POSTS, false);

            mMarkPostsAsRead = postHistorySharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false);
            mMarkPostsAsReadAfterVoting = postHistorySharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, false);
            mMarkPostsAsReadOnScroll = postHistorySharedPreferences.getBoolean((accountName == null ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, false);

            mHidePostType = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_TYPE, false);
            mHidePostFlair = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_FLAIR, false);
            mHideTheNumberOfAwards = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_AWARDS, false);
            mHideSubredditAndUserPrefix = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBREDDIT_AND_USER_PREFIX, false);
            mHideTheNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES, false);
            mHideTheNumberOfComments = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_COMMENTS, false);
            mLegacyAutoplayVideoControllerUI = sharedPreferences.getBoolean(SharedPreferencesUtils.LEGACY_AUTOPLAY_VIDEO_CONTROLLER_UI, false);
            mFixedHeightPreviewInCard = sharedPreferences.getBoolean(SharedPreferencesUtils.FIXED_HEIGHT_PREVIEW_IN_CARD, false);
            mHideTextPostContent = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_TEXT_POST_CONTENT, false);

            mPostLayout = postLayout;
            mDefaultLinkPostLayout = Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.DEFAULT_LINK_POST_LAYOUT_KEY, "-1"));

            mColorAccent = customThemeWrapper.getColorAccent();
            mCardViewBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
            mReadPostCardViewBackgroundColor = customThemeWrapper.getReadPostCardViewBackgroundColor();
            mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
            mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
            mPostTitleColor = customThemeWrapper.getPostTitleColor();
            mPostContentColor = customThemeWrapper.getPostContentColor();
            mReadPostTitleColor = customThemeWrapper.getReadPostTitleColor();
            mReadPostContentColor = customThemeWrapper.getReadPostContentColor();
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
            mMediaIndicatorIconTint = customThemeWrapper.getMediaIndicatorIconColor();
            mMediaIndicatorBackgroundColor = customThemeWrapper.getMediaIndicatorBackgroundColor();
            mNoPreviewPostTypeBackgroundColor = customThemeWrapper.getNoPreviewPostTypeBackgroundColor();
            mNoPreviewPostTypeIconTint = customThemeWrapper.getNoPreviewPostTypeIconTint();
            mUpvotedColor = customThemeWrapper.getUpvoted();
            mDownvotedColor = customThemeWrapper.getDownvoted();
            mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
            mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
            mDividerColor = customThemeWrapper.getDividerColor();

            mCommentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
            if (mCommentIcon != null) {
                DrawableCompat.setTint(mCommentIcon, mPostIconAndInfoColor);
            }

            mScale = resources.getDisplayMetrics().density;
            mGlide = Glide.with(mActivity);
            mMaxResolution = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000"));
            mSaveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(mMaxResolution);
            mLocale = locale;
            mExoCreator = exoCreator;
            mCallback = callback;
        }
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    @Override
    public int getItemViewType(int position) {
        if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_CARD) {
            Post post = getItem(position);
            if (post != null) {
                switch (post.getPostType()) {
                    case Post.VIDEO_TYPE:
                        if (mAutoplay) {
                            if ((!mAutoplayNsfwVideos && post.isNSFW()) || post.isSpoiler()) {
                                return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                            }
                            return VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE;
                        }
                        return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                    case Post.GIF_TYPE:
                    case Post.IMAGE_TYPE:
                    case Post.GALLERY_TYPE:
                        return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                    case Post.LINK_TYPE:
                    case Post.NO_PREVIEW_LINK_TYPE:
                        switch (mDefaultLinkPostLayout) {
                            case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                                return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                                return VIEW_TYPE_POST_GALLERY;
                            case SharedPreferencesUtils.POST_LAYOUT_COMPACT:
                                return VIEW_TYPE_POST_COMPACT;
                        }
                        return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                    default:
                        return VIEW_TYPE_POST_CARD_TEXT_TYPE;
                }
            }
            return VIEW_TYPE_POST_CARD_TEXT_TYPE;
        } else if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_COMPACT) {
            Post post = getItem(position);
            if (post != null) {
                if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                    switch (mDefaultLinkPostLayout) {
                        case SharedPreferencesUtils.POST_LAYOUT_CARD:
                            return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                        case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                            return VIEW_TYPE_POST_GALLERY;
                        case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                            return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                    }
                }
            }
            return VIEW_TYPE_POST_COMPACT;
        } else if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_GALLERY) {
            return VIEW_TYPE_POST_GALLERY;
        } else {
            Post post = getItem(position);
            if (post != null) {
                switch (post.getPostType()) {
                    case Post.VIDEO_TYPE:
                        if (mAutoplay) {
                            if ((!mAutoplayNsfwVideos && post.isNSFW()) || post.isSpoiler()) {
                                return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                            }
                            return VIEW_TYPE_POST_CARD_2_VIDEO_AUTOPLAY_TYPE;
                        }
                        return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                    case Post.GIF_TYPE:
                    case Post.IMAGE_TYPE:
                    case Post.GALLERY_TYPE:
                        return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                    case Post.LINK_TYPE:
                    case Post.NO_PREVIEW_LINK_TYPE:
                        switch (mDefaultLinkPostLayout) {
                            case SharedPreferencesUtils.POST_LAYOUT_CARD:
                                return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                                return VIEW_TYPE_POST_GALLERY;
                            case SharedPreferencesUtils.POST_LAYOUT_COMPACT:
                                return VIEW_TYPE_POST_COMPACT;
                        }
                        return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                    default:
                        return VIEW_TYPE_POST_CARD_2_TEXT_TYPE;
                }
            }
            return VIEW_TYPE_POST_CARD_2_TEXT_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE) {
            if (mDataSavingMode) {
                return new PostWithPreviewTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_with_preview, parent, false));
            }
            return new PostVideoAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(mLegacyAutoplayVideoControllerUI ? R.layout.item_post_video_type_autoplay_legacy_controller : R.layout.item_post_video_type_autoplay, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE) {
            return new PostWithPreviewTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_with_preview, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_TEXT_TYPE) {
            return new PostTextTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_text, parent, false));
        } else if (viewType == VIEW_TYPE_POST_COMPACT) {
            if (mShowThumbnailOnTheRightInCompactLayout) {
                return new PostCompactRightThumbnailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_compact_right_thumbnail, parent, false));
            } else {
                return new PostCompactLeftThumbnailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_compact, parent, false));
            }
        } else if (viewType == VIEW_TYPE_POST_GALLERY) {
            return new PostGalleryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_gallery, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_2_VIDEO_AUTOPLAY_TYPE) {
            if (mDataSavingMode) {
                return new PostCard2WithPreviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card_2_with_preview, parent, false));
            }
            return new PostCard2VideoAutoplayViewHolder(LayoutInflater.from(parent.getContext()).inflate(mLegacyAutoplayVideoControllerUI ? R.layout.item_post_card_2_video_autoplay_legacy_controller : R.layout.item_post_card_2_video_autoplay, parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE) {
            return new PostCard2WithPreviewViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card_2_with_preview, parent, false));
        } else {
            //VIEW_TYPE_POST_CARD_2_TEXT_TYPE
            return new PostCard2TextTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_card_2_text, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostBaseViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                ((PostBaseViewHolder) holder).post = post;
                ((PostBaseViewHolder) holder).currentPosition = position;
                if (post.isRead()) {
                    if (((PostBaseViewHolder) holder).itemViewIsNotCardView) {
                        holder.itemView.setBackgroundColor(mReadPostCardViewBackgroundColor);
                    } else {
                        holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                    }

                    ((PostBaseViewHolder) holder).titleTextView.setTextColor(mReadPostTitleColor);
                }
                String authorPrefixed = "u/" + post.getAuthor();

                if (mHideSubredditAndUserPrefix) {
                    ((PostBaseViewHolder) holder).subredditTextView.setText(post.getSubredditName());
                    ((PostBaseViewHolder) holder).userTextView.setText(post.getAuthor());
                } else {
                    ((PostBaseViewHolder) holder).subredditTextView.setText("r/" + post.getSubredditName());
                    ((PostBaseViewHolder) holder).userTextView.setText(authorPrefixed);
                }

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(post.getSubredditNamePrefixed())) {
                        if (post.getAuthorIconUrl() == null) {
                            mFragment.loadIcon(post.getAuthor(), false, (subredditOrUserName, iconUrl) -> {
                                if (mActivity != null && getItemCount() > 0 && post.getAuthor().equals(subredditOrUserName)) {
                                    if (iconUrl == null || iconUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getBindingAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconUrl);
                                    }
                                }
                            });
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
                            mFragment.loadIcon(post.getSubredditName(), true, (subredditOrUserName, iconUrl) -> {
                                if (mActivity != null && getItemCount() > 0 && post.getSubredditName().equals(subredditOrUserName)) {
                                    if (iconUrl == null || iconUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getBindingAdapterPosition() >= 0) {
                                        post.setSubredditIconUrl(iconUrl);
                                    }
                                }
                            });
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
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditName() : post.getAuthor();
                        mFragment.loadIcon(authorName, post.getAuthor().equals("[deleted]"), (subredditOrUserName, iconUrl) -> {
                            if (mActivity != null && getItemCount() > 0) {
                                if (iconUrl == null || iconUrl.equals("") && authorName.equals(subredditOrUserName)) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostBaseViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostBaseViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getBindingAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconUrl);
                                }
                            }
                        });
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
                if (!mHideTheNumberOfVotes) {
                    ((PostBaseViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                } else {
                    ((PostBaseViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.vote));
                }

                if (post.isLocked()) {
                    ((PostBaseViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (post.isNSFW()) {
                    ((PostBaseViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                if (post.isSpoiler()) {
                    ((PostBaseViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
                }

                if (post.getFlair() != null && !post.getFlair().equals("")) {
                    if (mHidePostFlair) {
                        ((PostBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
                    } else {
                        ((PostBaseViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                        Utils.setHTMLWithImageToTextView(((PostBaseViewHolder) holder).flairTextView, post.getFlair(), false);
                    }
                }

                if (post.getNAwards() > 0 && !mHideTheNumberOfAwards) {
                    ((PostBaseViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                    if (post.getNAwards() == 1) {
                        ((PostBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.one_award));
                    } else {
                        ((PostBaseViewHolder) holder).awardsTextView.setText(mActivity.getString(R.string.n_awards, post.getNAwards()));
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

                if (mPostType == PostPagingSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
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

                if (!mHideTheNumberOfComments) {
                    ((PostBaseViewHolder) holder).commentsCountTextView.setVisibility(View.VISIBLE);
                    ((PostBaseViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));
                } else {
                    ((PostBaseViewHolder) holder).commentsCountTextView.setVisibility(View.GONE);
                }

                if (post.isSaved()) {
                    ((PostBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (mHidePostType) {
                    ((PostBaseViewHolder) holder).typeTextView.setVisibility(View.GONE);
                } else {
                    ((PostBaseViewHolder) holder).typeTextView.setVisibility(View.VISIBLE);
                }

                if (holder instanceof PostVideoAutoplayViewHolder) {
                    ((PostVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.VISIBLE);
                    Post.Preview preview = getSuitablePreview(post.getPreviews());
                    if (!mFixedHeightPreviewInCard && preview != null) {
                        ((PostVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio((float) preview.getPreviewWidth() / preview.getPreviewHeight());
                        mGlide.load(preview.getPreviewUrl()).centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostVideoAutoplayViewHolder) holder).previewImageView);
                    } else {
                        ((PostVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio(1);
                    }
                    if (mFragment.getMasterMutingOption() == null) {
                        ((PostVideoAutoplayViewHolder) holder).setVolume(mMuteAutoplayingVideos || (post.isNSFW() && mMuteNSFWVideo) ? 0f : 1f);
                    } else {
                        ((PostVideoAutoplayViewHolder) holder).setVolume(mFragment.getMasterMutingOption() ? 0f : 1f);
                    }

                    if ((post.isGfycat() || post.isRedgifs()) && !post.isLoadGfycatOrStreamableVideoSuccess()) {
                        ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                                (post.isGfycat() ? mGfycatRetrofit : mRedgifsRetrofit).create(GfycatAPI.class).getGfycatData(post.getGfycatId());
                        FetchGfycatOrRedgifsVideoLinks.fetchGfycatOrRedgifsVideoLinksInRecyclerViewAdapter(mExecutor, new Handler(),
                                ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall, post.getGfycatId(),
                                post.isGfycat(), mAutomaticallyTryRedgifs,
                                new FetchGfycatOrRedgifsVideoLinks.FetchGfycatOrRedgifsVideoLinksListener() {
                                    @Override
                                    public void success(String webm, String mp4) {
                                        post.setVideoDownloadUrl(mp4);
                                        post.setVideoUrl(mp4);
                                        post.setLoadGfyOrStreamableVideoSuccess(true);
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                                        }
                                    }

                                    @Override
                                    public void failed(int errorCode) {
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostVideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    } else if(post.isStreamable() && !post.isLoadGfycatOrStreamableVideoSuccess()) {
                        ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                                mStreamableRetrofit.create(StreamableAPI.class).getStreamableData(post.getStreamableShortCode());
                        FetchStreamableVideo.fetchStreamableVideoInRecyclerViewAdapter(mExecutor, new Handler(),
                                ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall,
                                new FetchStreamableVideo.FetchStreamableVideoListener() {
                                    @Override
                                    public void success(StreamableVideo streamableVideo) {
                                        StreamableVideo.Media media = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile : streamableVideo.mp4;
                                        post.setVideoDownloadUrl(media.url);
                                        post.setVideoUrl(media.url);
                                        post.setLoadGfyOrStreamableVideoSuccess(true);
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                                        }
                                    }

                                    @Override
                                    public void failed() {
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostVideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    } else {
                        ((PostVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                    }
                } else if (holder instanceof PostWithPreviewTypeViewHolder) {
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.video));
                    } else if (post.getPostType() == Post.GIF_TYPE) {
                        if (!mAutoplay) {
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        }
                        ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gif));
                    } else if (post.getPostType() == Post.IMAGE_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.image));
                    } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.link));
                        ((PostWithPreviewTypeViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostWithPreviewTypeViewHolder) holder).linkTextView.setText(domain);
                        if (post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_link);
                        }
                    } else if (post.getPostType() == Post.GALLERY_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gallery));
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_gallery_24dp));
                    }

                    if (post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    }

                    if (mDataSavingMode && mDisableImagePreview) {
                        ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        if (post.getPostType() == Post.VIDEO_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_image_24dp);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.LINK_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_link);
                        } else if (post.getPostType() == Post.GALLERY_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_gallery_24dp);
                        }
                    } else if (mDataSavingMode && mOnlyDisablePreviewInVideoAndGifPosts && (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE)) {
                        ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                    } else {
                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostWithPreviewTypeViewHolder) holder).preview = preview;
                        if (preview != null) {
                            ((PostWithPreviewTypeViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.VISIBLE);
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostWithPreviewTypeViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostWithPreviewTypeViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostWithPreviewTypeViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostWithPreviewTypeViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                            if (post.getPostType() == Post.VIDEO_TYPE) {
                                ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                                ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                                ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_image_24dp);
                                ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.LINK_TYPE) {
                                ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_link);
                            } else if (post.getPostType() == Post.GALLERY_TYPE) {
                                ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setImageResource(R.drawable.ic_gallery_24dp);
                            }
                        }
                    }
                } else if (holder instanceof PostTextTypeViewHolder) {
                    if (!mHideTextPostContent && !post.isSpoiler() && post.getSelfTextPlainTrimmed() != null && !post.getSelfTextPlainTrimmed().equals("")) {
                        ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
                        if (post.isRead()) {
                            ((PostTextTypeViewHolder) holder).contentTextView.setTextColor(mReadPostContentColor);
                        }
                        ((PostTextTypeViewHolder) holder).contentTextView.setText(post.getSelfTextPlainTrimmed());
                    }
                } else if (holder instanceof PostCard2VideoAutoplayViewHolder) {
                    ((PostCard2VideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.VISIBLE);
                    Post.Preview preview = getSuitablePreview(post.getPreviews());
                    if (!mFixedHeightPreviewInCard && preview != null) {
                        ((PostCard2VideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio((float) preview.getPreviewWidth() / preview.getPreviewHeight());
                        mGlide.load(preview.getPreviewUrl()).centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostCard2VideoAutoplayViewHolder) holder).previewImageView);
                    } else {
                        ((PostCard2VideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio(1);
                    }
                    if (mFragment.getMasterMutingOption() == null) {
                        ((PostCard2VideoAutoplayViewHolder) holder).setVolume(mMuteAutoplayingVideos || (post.isNSFW() && mMuteNSFWVideo) ? 0f : 1f);
                    } else {
                        ((PostCard2VideoAutoplayViewHolder) holder).setVolume(mFragment.getMasterMutingOption() ? 0f : 1f);
                    }

                    if ((post.isGfycat() || post.isRedgifs()) && !post.isLoadGfycatOrStreamableVideoSuccess()) {
                        ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                                (post.isGfycat() ? mGfycatRetrofit : mRedgifsRetrofit).create(GfycatAPI.class).getGfycatData(post.getGfycatId());
                        FetchGfycatOrRedgifsVideoLinks.fetchGfycatOrRedgifsVideoLinksInRecyclerViewAdapter(mExecutor, new Handler(),
                                ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall, post.getGfycatId(),
                                post.isGfycat(), mAutomaticallyTryRedgifs,
                                new FetchGfycatOrRedgifsVideoLinks.FetchGfycatOrRedgifsVideoLinksListener() {
                                    @Override
                                    public void success(String webm, String mp4) {
                                        post.setVideoDownloadUrl(mp4);
                                        post.setVideoUrl(mp4);
                                        post.setLoadGfyOrStreamableVideoSuccess(true);
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostCard2VideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                                        }
                                    }

                                    @Override
                                    public void failed(int errorCode) {
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostCard2VideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    } else if(post.isStreamable() && !post.isLoadGfycatOrStreamableVideoSuccess()) {
                        ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                                mStreamableRetrofit.create(StreamableAPI.class).getStreamableData(post.getStreamableShortCode());
                        FetchStreamableVideo.fetchStreamableVideoInRecyclerViewAdapter(mExecutor, new Handler(),
                                ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall,
                                new FetchStreamableVideo.FetchStreamableVideoListener() {
                                    @Override
                                    public void success(StreamableVideo streamableVideo) {
                                        StreamableVideo.Media media = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile : streamableVideo.mp4;
                                        post.setVideoDownloadUrl(media.url);
                                        post.setVideoUrl(media.url);
                                        post.setLoadGfyOrStreamableVideoSuccess(true);
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostCard2VideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                                        }
                                    }

                                    @Override
                                    public void failed() {
                                        if (position == holder.getBindingAdapterPosition()) {
                                            ((PostCard2VideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                        }
                                    }
                                });
                    } else {
                        ((PostCard2VideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(post.getVideoUrl()));
                    }
                } else if (holder instanceof PostCard2WithPreviewViewHolder) {
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        ((PostCard2WithPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.video));
                    } else if (post.getPostType() == Post.GIF_TYPE) {
                        if (!mAutoplay) {
                            ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                            ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        }
                        ((PostCard2WithPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gif));
                    } else if (post.getPostType() == Post.IMAGE_TYPE) {
                        ((PostCard2WithPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.image));
                    } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                        ((PostCard2WithPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.link));
                        ((PostCard2WithPreviewViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCard2WithPreviewViewHolder) holder).linkTextView.setText(domain);
                        if (post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                        }
                    } else if (post.getPostType() == Post.GALLERY_TYPE) {
                        ((PostCard2WithPreviewViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gallery));
                        ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_gallery_24dp));
                    }

                    if (post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                        ((PostCard2WithPreviewViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    }

                    if (mDataSavingMode && mDisableImagePreview) {
                        ((PostCard2WithPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                        ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                        if (post.getPostType() == Post.VIDEO_TYPE) {
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                            ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_image_24dp);
                            ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.LINK_TYPE) {
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                        } else if (post.getPostType() == Post.GALLERY_TYPE) {
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_24dp);
                        }
                    } else if (mDataSavingMode && mOnlyDisablePreviewInVideoAndGifPosts && (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE)) {
                        ((PostCard2WithPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                        ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                        ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                        ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                    } else {
                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostCard2WithPreviewViewHolder) holder).preview = preview;
                        if (preview != null) {
                            ((PostCard2WithPreviewViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostCard2WithPreviewViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostCard2WithPreviewViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostCard2WithPreviewViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostCard2WithPreviewViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostCard2WithPreviewViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostCard2WithPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                            ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            if (post.getPostType() == Post.VIDEO_TYPE) {
                                ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                                ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                                ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_image_24dp);
                                ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.LINK_TYPE) {
                                ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                            } else if (post.getPostType() == Post.GALLERY_TYPE) {
                                ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_24dp);
                            }
                        }
                    }
                } else if (holder instanceof PostCard2TextTypeViewHolder) {
                    if (!mHideTextPostContent && !post.isSpoiler() && post.getSelfTextPlainTrimmed() != null && !post.getSelfTextPlainTrimmed().equals("")) {
                        ((PostCard2TextTypeViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
                        if (post.isRead()) {
                            ((PostCard2TextTypeViewHolder) holder).contentTextView.setTextColor(mReadPostContentColor);
                        }
                        ((PostCard2TextTypeViewHolder) holder).contentTextView.setText(post.getSelfTextPlainTrimmed());
                    }
                }
                mCallback.currentlyBindItem(holder.getBindingAdapterPosition());
            }
        } else if (holder instanceof PostCompactBaseViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                ((PostCompactBaseViewHolder) holder).post = post;
                ((PostCompactBaseViewHolder) holder).currentPosition = position;
                if (post.isRead()) {
                    holder.itemView.setBackgroundColor(mReadPostCardViewBackgroundColor);
                    ((PostCompactBaseViewHolder) holder).titleTextView.setTextColor(mReadPostTitleColor);
                }
                final String subredditNamePrefixed = post.getSubredditNamePrefixed();
                String subredditName = subredditNamePrefixed.substring(2);
                String authorPrefixed = "u/" + post.getAuthor();
                final String title = post.getTitle();
                int voteType = post.getVoteType();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                int nAwards = post.getNAwards();
                boolean isArchived = post.isArchived();

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            mFragment.loadIcon(post.getAuthor(), false, (subredditOrUserName, iconUrl) -> {
                                if (mActivity != null && getItemCount() > 0 && post.getAuthor().equals(subredditOrUserName)) {
                                    if (iconUrl == null || iconUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getBindingAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconUrl);
                                    }
                                }
                            });
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
                            mFragment.loadIcon(subredditName, true, (subredditOrUserName, iconUrl) -> {
                                if (mActivity != null && getItemCount() > 0 && subredditName.equals(subredditOrUserName)) {
                                    if (iconUrl == null || iconUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getBindingAdapterPosition() >= 0) {
                                        post.setSubredditIconUrl(iconUrl);
                                    }
                                }
                            });
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
                    if (mHideSubredditAndUserPrefix) {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getSubredditName());
                    } else {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText("r/" + post.getSubredditName());
                    }
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditName() : post.getAuthor();
                        mFragment.loadIcon(authorName, post.getAuthor().equals("[deleted]"), (subredditOrUserName, iconUrl) -> {
                            if (mActivity != null && getItemCount() > 0 && authorName.equals(subredditOrUserName)) {
                                if (iconUrl == null || iconUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostCompactBaseViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getBindingAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconUrl);
                                }
                            }
                        });
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
                    if (mHideSubredditAndUserPrefix) {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getAuthor());
                    } else {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(authorPrefixed);
                    }
                }

                if (mShowElapsedTime) {
                    ((PostCompactBaseViewHolder) holder).postTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
                } else {
                    ((PostCompactBaseViewHolder) holder).postTimeTextView.setText(Utils.getFormattedTime(mLocale, post.getPostTimeMillis(), mTimeFormatPattern));
                }

                if (mCompactLayoutToolbarHiddenByDefault) {
                    ViewGroup.LayoutParams params = ((PostCompactBaseViewHolder) holder).bottomConstraintLayout.getLayoutParams();
                    params.height = 0;
                    ((PostCompactBaseViewHolder) holder).bottomConstraintLayout.setLayoutParams(params);
                } else {
                    ViewGroup.LayoutParams params = ((PostCompactBaseViewHolder) holder).bottomConstraintLayout.getLayoutParams();
                    params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    ((PostCompactBaseViewHolder) holder).bottomConstraintLayout.setLayoutParams(params);
                }

                if (mShowDividerInCompactLayout) {
                    ((PostCompactBaseViewHolder) holder).divider.setVisibility(View.VISIBLE);
                } else {
                    ((PostCompactBaseViewHolder) holder).divider.setVisibility(View.GONE);
                }

                ((PostCompactBaseViewHolder) holder).titleTextView.setText(title);
                if (!mHideTheNumberOfVotes) {
                    ((PostCompactBaseViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                } else {
                    ((PostCompactBaseViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.vote));
                }

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
                    if (mHidePostFlair) {
                        ((PostCompactBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
                    } else {
                        ((PostCompactBaseViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                        Utils.setHTMLWithImageToTextView(((PostCompactBaseViewHolder) holder).flairTextView, flair, false);
                    }
                }

                if (nAwards > 0 && !mHideTheNumberOfAwards) {
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

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE && !(mDataSavingMode && mDisableImagePreview)) {
                    ((PostCompactBaseViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    if (post.getPostType() == Post.GALLERY_TYPE && post.getPreviews() != null && post.getPreviews().isEmpty()) {
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_gallery_24dp);
                    }
                    if (post.getPreviews() != null && !post.getPreviews().isEmpty()) {
                        ((PostCompactBaseViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        loadImage(holder);
                    }
                }

                if (mPostType == PostPagingSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
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

                if (mHidePostType) {
                    ((PostCompactBaseViewHolder) holder).typeTextView.setVisibility(View.GONE);
                } else {
                    ((PostCompactBaseViewHolder) holder).typeTextView.setVisibility(View.VISIBLE);
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.image);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_image_24dp);
                        }
                        break;
                    case Post.LINK_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.link);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_link);
                        }

                        ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setText(domain);
                        break;
                    case Post.GIF_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gif);
                        if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_image_24dp);
                        } else {
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_24dp));
                        }
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.video);
                        if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                        } else {
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_24dp));
                        }
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_link);
                        break;
                    case Post.GALLERY_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gallery);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_gallery_24dp);
                        } else {
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_gallery_24dp));
                        }
                        break;
                    case Post.TEXT_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.text);
                        break;
                }

                if (!mHideTheNumberOfComments) {
                    ((PostCompactBaseViewHolder) holder).commentsCountTextView.setVisibility(View.VISIBLE);
                    ((PostCompactBaseViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));
                } else {
                    ((PostCompactBaseViewHolder) holder).commentsCountTextView.setVisibility(View.GONE);
                }

                if (post.isSaved()) {
                    ((PostCompactBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostCompactBaseViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                mCallback.currentlyBindItem(holder.getBindingAdapterPosition());
            }
        } else if (holder instanceof PostGalleryViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                ((PostGalleryViewHolder) holder).post = post;
                ((PostGalleryViewHolder) holder).currentPosition = position;
                if (post.isRead()) {
                    holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                    ((PostGalleryViewHolder) holder).titleTextView.setTextColor(mReadPostTitleColor);
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE: {
                        ((PostGalleryViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.VISIBLE);

                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostGalleryViewHolder) holder).preview = preview;
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostGalleryViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostGalleryViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostGalleryViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostGalleryViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostGalleryViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            if (post.getPostType() == Post.VIDEO_TYPE) {
                                ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                                ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                                ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                            } else if (post.getPostType() == Post.LINK_TYPE) {
                                ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                            } else if (post.getPostType() == Post.GALLERY_TYPE) {
                                ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_24dp);
                            }
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_image_24dp);
                        }
                        break;
                    }
                    case Post.GIF_TYPE: {
                        ((PostGalleryViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));

                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostGalleryViewHolder) holder).preview = preview;
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostGalleryViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostGalleryViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostGalleryViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostGalleryViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostGalleryViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_image_24dp);
                        }
                        break;
                    }
                    case Post.VIDEO_TYPE: {
                        ((PostGalleryViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));

                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostGalleryViewHolder) holder).preview = preview;
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostGalleryViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostGalleryViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostGalleryViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostGalleryViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostGalleryViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_outline_video_24dp);
                        }
                        break;
                    }
                    case Post.LINK_TYPE: {
                        ((PostGalleryViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_link_post_type_indicator));

                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostGalleryViewHolder) holder).preview = preview;
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostGalleryViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostGalleryViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostGalleryViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostGalleryViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostGalleryViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                        }
                        break;
                    }
                    case Post.NO_PREVIEW_LINK_TYPE: {
                        ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_link);
                        break;
                    }
                    case Post.TEXT_TYPE: {
                        ((PostGalleryViewHolder) holder).titleTextView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).titleTextView.setText(post.getTitle());
                        break;
                    }
                    case Post.GALLERY_TYPE: {
                        ((PostGalleryViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.VISIBLE);
                        ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_gallery_24dp));

                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        ((PostGalleryViewHolder) holder).preview = preview;
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                int height = (int) (400 * mScale);
                                ((PostGalleryViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                ((PostGalleryViewHolder) holder).imageView.getLayoutParams().height = height;
                            } else {
                                ((PostGalleryViewHolder) holder).imageView
                                        .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                            ((PostGalleryViewHolder) holder).imageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                @Override
                                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                    ((PostGalleryViewHolder) holder).imageView.removeOnLayoutChangeListener(this);
                                    loadImage(holder);
                                }
                            });
                        } else {
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_24dp);
                        }
                        break;
                    }
                }
            }
        }
    }

    @Nullable
    private Post.Preview getSuitablePreview(ArrayList<Post.Preview> previews) {
        Post.Preview preview;
        if (!previews.isEmpty()) {
            int previewIndex;
            if (mDataSavingMode && previews.size() > 2) {
                previewIndex = previews.size() / 2;
            } else {
                previewIndex = 0;
            }
            preview = previews.get(previewIndex);
            if (preview.getPreviewWidth() * preview.getPreviewHeight() > mMaxResolution) {
                for (int i = previews.size() - 1; i >= 1; i--) {
                    preview = previews.get(i);
                    if (preview.getPreviewWidth() * preview.getPreviewHeight() <= mMaxResolution) {
                        return preview;
                    }
                }
            }
            return preview;
        }

        return null;
    }

    private void loadImage(final RecyclerView.ViewHolder holder) {
        if (holder instanceof PostWithPreviewTypeViewHolder) {
            Post post = ((PostWithPreviewTypeViewHolder) holder).post;
            Post.Preview preview = ((PostWithPreviewTypeViewHolder) holder).preview;
            if (preview != null) {
                String url;
                boolean blurImage = (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(post.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || (post.isSpoiler() && mNeedBlurSpoiler);
                if (post.getPostType() == Post.GIF_TYPE && mAutoplay && !blurImage) {
                    url = post.getUrl();
                } else {
                    url = preview.getPreviewUrl();
                }
                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(((PostWithPreviewTypeViewHolder) holder).glideRequestListener);
                if (post.getAuthor().startsWith("GLIBG10")) {
                    Log.i("sssssaffd", "s " + url + " " + ((PostWithPreviewTypeViewHolder) holder).imageView.getRatio());
                }
                if (blurImage) {
                    imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                            .into(((PostWithPreviewTypeViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostWithPreviewTypeViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostCompactBaseViewHolder) {
            Post post = ((PostCompactBaseViewHolder) holder).post;
            String postCompactThumbnailPreviewUrl;
            ArrayList<Post.Preview> previews = post.getPreviews();
            if (previews != null && !previews.isEmpty()) {
                if (previews.size() >= 2) {
                    postCompactThumbnailPreviewUrl = previews.get(1).getPreviewUrl();
                } else {
                    postCompactThumbnailPreviewUrl = previews.get(0).getPreviewUrl();
                }

                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(postCompactThumbnailPreviewUrl)
                        .error(R.drawable.ic_error_outline_black_24dp).listener(((PostCompactBaseViewHolder) holder).requestListener);
                if ((post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (post.isSpoiler() && mNeedBlurSpoiler)) {
                    imageRequestBuilder
                            .transform(new BlurTransformation(50, 2)).into(((PostCompactBaseViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostCompactBaseViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostGalleryViewHolder) {
            Post post = ((PostGalleryViewHolder) holder).post;
            Post.Preview preview = ((PostGalleryViewHolder) holder).preview;
            if (preview != null) {
                String url;
                boolean blurImage = (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(post.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || post.isSpoiler() && mNeedBlurSpoiler;
                if (post.getPostType() == Post.GIF_TYPE && mAutoplay && !blurImage) {
                    url = post.getUrl();
                } else {
                    url = preview.getPreviewUrl();
                }
                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(((PostGalleryViewHolder) holder).requestListener);

                if (blurImage) {
                    imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                            .into(((PostGalleryViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostGalleryViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostCard2WithPreviewViewHolder) {
            Post post = ((PostCard2WithPreviewViewHolder) holder).post;
            Post.Preview preview = ((PostCard2WithPreviewViewHolder) holder).preview;
            if (preview != null) {
                String url;
                boolean blurImage = (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(post.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || (post.isSpoiler() && mNeedBlurSpoiler);
                if (post.getPostType() == Post.GIF_TYPE && mAutoplay && !blurImage) {
                    url = post.getUrl();
                } else {
                    url = preview.getPreviewUrl();
                }
                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(((PostCard2WithPreviewViewHolder) holder).requestListener);

                if (blurImage) {
                    imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                            .into(((PostCard2WithPreviewViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostCard2WithPreviewViewHolder) holder).imageView);
                }
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
        ShareLinkBottomSheetFragment shareLinkBottomSheetFragment = new ShareLinkBottomSheetFragment();
        shareLinkBottomSheetFragment.setArguments(bundle);
        shareLinkBottomSheetFragment.show(mActivity.getSupportFragmentManager(), shareLinkBottomSheetFragment.getTag());
    }

    @Nullable
    public Post getItemByPosition(int position) {
        if (position >= 0 && super.getItemCount() > position) {
            return super.getItem(position);
        }

        return null;
    }

    public void setVoteButtonsPosition(boolean voteButtonsOnTheRight) {
        mVoteButtonsOnTheRight = voteButtonsOnTheRight;
    }

    public void setPostLayout(int postLayout) {
        mPostLayout = postLayout;
    }

    public void setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(boolean needBlurNsfw, boolean doNotBlurNsfwInNsfwSubreddits) {
        mNeedBlurNsfw = needBlurNsfw;
        mDoNotBlurNsfwInNsfwSubreddits = doNotBlurNsfwInNsfwSubreddits;
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

    public int getNextItemPositionWithoutBeingHidden(int fromPosition) {
        int temp = fromPosition;
        while (temp >= 0 && temp < super.getItemCount()) {
            Post post = getItem(temp);
            if (post != null && post.isHiddenInRecyclerView()) {
                temp++;
            } else {
                break;
            }
        }

        return temp;
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

    public void setLongPressToHideToolbarInCompactLayout(boolean longPressToHideToolbarInCompactLayout) {
        mLongPressToHideToolbarInCompactLayout = longPressToHideToolbarInCompactLayout;
    }

    public void setCompactLayoutToolbarHiddenByDefault(boolean compactLayoutToolbarHiddenByDefault) {
        mCompactLayoutToolbarHiddenByDefault = compactLayoutToolbarHiddenByDefault;
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        mDataSavingMode = dataSavingMode;
    }

    public void setDisableImagePreview(boolean disableImagePreview) {
        mDisableImagePreview = disableImagePreview;
    }

    public void setOnlyDisablePreviewInVideoPosts(boolean onlyDisablePreviewInVideoAndGifPosts) {
        mOnlyDisablePreviewInVideoAndGifPosts = onlyDisablePreviewInVideoAndGifPosts;
    }

    public void setHidePostType(boolean hidePostType) {
        mHidePostType = hidePostType;
    }

    public void setHidePostFlair(boolean hidePostFlair) {
        mHidePostFlair = hidePostFlair;
    }

    public void setHideTheNumberOfAwards(boolean hideTheNumberOfAwards) {
        mHideTheNumberOfAwards = hideTheNumberOfAwards;
    }

    public void setHideSubredditAndUserPrefix(boolean hideSubredditAndUserPrefix) {
        mHideSubredditAndUserPrefix = hideSubredditAndUserPrefix;
    }

    public void setHideTheNumberOfVotes(boolean hideTheNumberOfVotes) {
        mHideTheNumberOfVotes = hideTheNumberOfVotes;
    }

    public void setHideTheNumberOfComments(boolean hideTheNumberOfComments) {
        mHideTheNumberOfComments = hideTheNumberOfComments;
    }

    public void setDefaultLinkPostLayout(int defaultLinkPostLayout) {
        mDefaultLinkPostLayout = defaultLinkPostLayout;
    }

    public void setFixedHeightPreviewInCard(boolean fixedHeightPreviewInCard) {
        mFixedHeightPreviewInCard = fixedHeightPreviewInCard;
    }

    public void setHideTextPostContent(boolean hideTextPostContent) {
        mHideTextPostContent = hideTextPostContent;
    }

    public void setPostFeedMaxResolution(int postFeedMaxResolution) {
        mMaxResolution = postFeedMaxResolution;
        if (mSaveMemoryCenterInsideDownsampleStrategy != null) {
            mSaveMemoryCenterInsideDownsampleStrategy.setThreshold(postFeedMaxResolution);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostBaseViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostBaseViewHolder) holder).currentPosition;
                if (position < getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostBaseViewHolder) holder).markPostRead(post, false);
                }
            }
            if (((PostBaseViewHolder) holder).itemViewIsNotCardView) {
                holder.itemView.setBackgroundColor(mCardViewBackgroundColor);
            } else {
                holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            }
            mGlide.clear(((PostBaseViewHolder) holder).iconGifImageView);
            ((PostBaseViewHolder) holder).titleTextView.setTextColor(mPostTitleColor);
            if (holder instanceof PostVideoAutoplayViewHolder) {
                ((PostVideoAutoplayViewHolder) holder).mediaUri = null;
                if (((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall != null && !((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.isCanceled()) {
                    ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.cancel();
                    ((PostVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall = null;
                }
                ((PostVideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.GONE);
                ((PostVideoAutoplayViewHolder) holder).muteButton.setVisibility(View.GONE);
                ((PostVideoAutoplayViewHolder) holder).resetVolume();
                mGlide.clear(((PostVideoAutoplayViewHolder) holder).previewImageView);
                ((PostVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostWithPreviewTypeViewHolder) {
                mGlide.clear(((PostWithPreviewTypeViewHolder) holder).imageView);
                ((PostWithPreviewTypeViewHolder) holder).imageWrapperRelativeLayout.setVisibility(View.GONE);
                ((PostWithPreviewTypeViewHolder) holder).errorTextView.setVisibility(View.GONE);
                ((PostWithPreviewTypeViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
                ((PostWithPreviewTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                ((PostWithPreviewTypeViewHolder) holder).linkTextView.setVisibility(View.GONE);
            } else if (holder instanceof PostTextTypeViewHolder) {
                ((PostTextTypeViewHolder) holder).contentTextView.setText("");
                ((PostTextTypeViewHolder) holder).contentTextView.setTextColor(mPostContentColor);
                ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.GONE);
            } else if (holder instanceof PostCard2VideoAutoplayViewHolder) {
                ((PostCard2VideoAutoplayViewHolder) holder).mediaUri = null;
                if (((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall != null && !((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.isCanceled()) {
                    ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.cancel();
                    ((PostCard2VideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall = null;
                }
                ((PostCard2VideoAutoplayViewHolder) holder).errorLoadingGfycatImageView.setVisibility(View.GONE);
                ((PostCard2VideoAutoplayViewHolder) holder).muteButton.setVisibility(View.GONE);
                ((PostCard2VideoAutoplayViewHolder) holder).resetVolume();
                mGlide.clear(((PostCard2VideoAutoplayViewHolder) holder).previewImageView);
                ((PostCard2VideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostCard2WithPreviewViewHolder) {
                mGlide.clear(((PostCard2WithPreviewViewHolder) holder).imageView);
                ((PostCard2WithPreviewViewHolder) holder).imageView.setVisibility(View.GONE);
                ((PostCard2WithPreviewViewHolder) holder).errorTextView.setVisibility(View.GONE);
                ((PostCard2WithPreviewViewHolder) holder).noPreviewImageView.setVisibility(View.GONE);
                ((PostCard2WithPreviewViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((PostCard2WithPreviewViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
                ((PostCard2WithPreviewViewHolder) holder).linkTextView.setVisibility(View.GONE);
            } else if (holder instanceof PostCard2TextTypeViewHolder) {
                ((PostCard2TextTypeViewHolder) holder).contentTextView.setText("");
                ((PostCard2TextTypeViewHolder) holder).contentTextView.setTextColor(mPostContentColor);
                ((PostCard2TextTypeViewHolder) holder).contentTextView.setVisibility(View.GONE);
            }

            mGlide.clear(((PostBaseViewHolder) holder).iconGifImageView);
            ((PostBaseViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).flairTextView.setText("");
            ((PostBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).awardsTextView.setText("");
            ((PostBaseViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((PostBaseViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostBaseViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostBaseViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (holder instanceof PostCompactBaseViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostCompactBaseViewHolder) holder).currentPosition;
                if (position < getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostCompactBaseViewHolder) holder).markPostRead(post, false);
                }
            }
            holder.itemView.setBackgroundColor(mCardViewBackgroundColor);
            ((PostCompactBaseViewHolder) holder).titleTextView.setTextColor(mPostTitleColor);
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
            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.GONE);
            ((PostCompactBaseViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostCompactBaseViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostCompactBaseViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
        } else if (holder instanceof PostGalleryViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostGalleryViewHolder) holder).currentPosition;
                if (position < super.getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostGalleryViewHolder) holder).markPostRead(post, false);
                }
            }
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));

            ((PostGalleryViewHolder) holder).titleTextView.setText("");
            ((PostGalleryViewHolder) holder).titleTextView.setVisibility(View.GONE);
            mGlide.clear(((PostGalleryViewHolder) holder).imageView);
            ((PostGalleryViewHolder) holder).imageView.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).errorTextView.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).videoOrGifIndicatorImageView.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).noPreviewImageView.setVisibility(View.GONE);
        }
    }

    @Nullable
    @Override
    public Object getKeyForOrder(int order) {
        if (super.getItemCount() <= 0 || order >= super.getItemCount()) {
            return null;
        }
        return order;
    }

    @Nullable
    @Override
    public Integer getOrderForKey(@NonNull Object key) {
        if (key instanceof Integer) {
            return (Integer) key;
        }

        return null;
    }

    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int direction, int swipeLeftAction, int swipeRightAction) {
        if (viewHolder instanceof PostBaseViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            }
        } else if (viewHolder instanceof PostCompactBaseViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostCompactBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostCompactBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostCompactBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostCompactBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            }
        }
    }

    public interface Callback {
        void typeChipClicked(int filter);

        void flairChipClicked(String flair);

        void nsfwChipClicked();

        void currentlyBindItem(int position);

        void delayTransition();
    }

    private void openViewPostDetailActivity(Post post, int position) {
        if (canStartActivity) {
            canStartActivity = false;
            Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, position);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_FRAGMENT_ID, mFragment.getPostFragmentId());
            intent.putExtra(ViewPostDetailActivity.EXTRA_IS_NSFW_SUBREDDIT, mFragment.getIsNsfwSubreddit());
            mActivity.startActivity(intent);
        }
    }

    private void openMedia(Post post) {
        if (canStartActivity) {
            canStartActivity = false;
            if (post.getPostType() == Post.VIDEO_TYPE) {
                Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                if (post.isImgur()) {
                    intent.setData(Uri.parse(post.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                } else if (post.isGfycat()) {
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                    intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                } else if (post.isRedgifs()) {
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                    intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                } else if (post.isStreamable()) {
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                    intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, post.getStreamableShortCode());
                } else {
                    intent.setData(Uri.parse(post.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                }
                intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, post.getTitle());
                intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            } else if (post.getPostType() == Post.IMAGE_TYPE) {
                Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, post.getUrl());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, post.getSubredditName()
                        + "-" + post.getId() + ".jpg");
                intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, post.getTitle());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, post.getSubredditName());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            } else if (post.getPostType() == Post.GIF_TYPE) {
                Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, post.getSubredditName()
                        + "-" + post.getId() + ".gif");
                intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, post.getVideoUrl());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, post.getTitle());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, post.getSubredditName());
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                Uri uri = Uri.parse(post.getUrl());
                intent.setData(uri);
                intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            } else if (post.getPostType() == Post.GALLERY_TYPE) {
                Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                intent.putParcelableArrayListExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, post.getGallery());
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_SUBREDDIT_NAME, post.getSubredditName());
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            }
        }
    }

    public void setCanPlayVideo(boolean canPlayVideo) {
        this.canPlayVideo = canPlayVideo;
    }

    public class PostBaseViewHolder extends RecyclerView.ViewHolder {
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
        Post post;
        Post.Preview preview;

        boolean itemViewIsNotCardView = false;
        int currentPosition;

        PostBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
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

            if (itemViewIsNotCardView) {
                itemView.setBackgroundColor(mCardViewBackgroundColor);
            } else {
                itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            }

            if (mActivity.typeface != null) {
                subredditTextView.setTypeface(mActivity.typeface);
                userTextView.setTypeface(mActivity.typeface);
                postTimeTextView.setTypeface(mActivity.typeface);
                typeTextView.setTypeface(mActivity.typeface);
                spoilerTextView.setTypeface(mActivity.typeface);
                nsfwTextView.setTypeface(mActivity.typeface);
                flairTextView.setTypeface(mActivity.typeface);
                awardsTextView.setTypeface(mActivity.typeface);
                scoreTextView.setTypeface(mActivity.typeface);
                commentsCountTextView.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                titleTextView.setTypeface(mActivity.titleTypeface);
            }

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

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position >= 0 && canStartActivity) {
                    Post post = getItem(position);
                    if (post != null) {
                        markPostRead(post, true);

                        openViewPostDetailActivity(post, getBindingAdapterPosition());
                    }
                }
            });

            userTextView.setOnClickListener(view -> {
                if (canStartActivity) {
                    int position = getBindingAdapterPosition();
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
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        if (canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                    post.getSubredditName());
                            mActivity.startActivity(intent);
                        }
                    }
                });

                iconGifImageView.setOnClickListener(view -> subredditTextView.performClick());
            } else {
                subredditTextView.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        if (canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                    post.getSubredditName());
                            mActivity.startActivity(intent);
                        }
                    }
                });

                iconGifImageView.setOnClickListener(view -> userTextView.performClick());
            }

            if (!(mActivity instanceof FilteredPostsActivity)) {
                nsfwTextView.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        mCallback.nsfwChipClicked();
                    }
                });
                typeTextView.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        mCallback.typeChipClicked(post.getPostType());
                    }
                });

                flairTextView.setOnClickListener(view -> {
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        mCallback.flairChipClicked(post.getFlair());
                    }
                });
            }

            upvoteButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mMarkPostsAsReadAfterVoting) {
                        markPostRead(post, true);
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

                    if (!mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                                }
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            if (getBindingAdapterPosition() == position) {
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                                }
                                upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                                downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            downvoteButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mMarkPostsAsReadAfterVoting) {
                        markPostRead(post, true);
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

                    if (!mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                                }
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            if (getBindingAdapterPosition() == position) {
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                                }
                                upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                                downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            saveButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
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
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }
                                });
                    } else {
                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }
                                });
                    }
                }
            });

            shareButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    shareLink(post);
                }
            });
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
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
                         ImageView shareButton, boolean itemViewIsNotCardView) {
            this.itemViewIsNotCardView = itemViewIsNotCardView;

            setBaseView(iconGifImageView, subredditTextView, userTextView, stickiedPostImageView, postTimeTextView,
                    titleTextView, typeTextView, archivedImageView, lockedImageView, crosspostImageView,
                    nsfwTextView, spoilerTextView, flairTextView, awardsTextView, bottomConstraintLayout,
                    upvoteButton, scoreTextView, downvoteButton, commentsCountTextView, saveButton, shareButton);
        }

        void markPostRead(Post post, boolean changePostItemColor) {
            if (mAccessToken != null && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead(true);
                if (changePostItemColor) {
                    if (itemViewIsNotCardView) {
                        itemView.setBackgroundColor(mReadPostCardViewBackgroundColor);
                    } else {
                        itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                    }
                    titleTextView.setTextColor(mReadPostTitleColor);
                    if (this instanceof PostTextTypeViewHolder) {
                        ((PostTextTypeViewHolder) this).contentTextView.setTextColor(mReadPostContentColor);
                    }
                }
                if (mActivity != null && mActivity instanceof MarkPostAsReadInterface) {
                    ((MarkPostAsReadInterface) mActivity).markPostAsRead(post);
                    mFragment.markPostAsRead(post);
                }
            }
        }
    }

    class PostVideoAutoplayViewHolder extends PostBaseViewHolder implements ToroPlayer {
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
        @BindView(R.id.error_loading_gfycat_image_view_item_post_video_type_autoplay)
        ImageView errorLoadingGfycatImageView;
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
        public Call<String> fetchGfycatOrStreamableVideoCall;
        //public FetchGfycatOrRedgifsVideoLinks fetchGfycatOrRedgifsVideoLinks;

        PostVideoAutoplayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(
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
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_white_rounded_24dp));
                        helper.setVolume(0f);
                        volume = 0f;
                        mFragment.videoAutoplayChangeMutingOption(true);
                    } else {
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_white_rounded_24dp));
                        helper.setVolume(1f);
                        volume = 1f;
                        mFragment.videoAutoplayChangeMutingOption(false);
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                if (canStartActivity) {
                    canStartActivity = false;
                    int position = getBindingAdapterPosition();
                    if (position < 0) {
                        return;
                    }
                    Post post = getItem(position);
                    if (post != null) {
                        markPostRead(post, true);
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        if (post.isImgur()) {
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                        } else if (post.isGfycat()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                            if (post.isLoadGfycatOrStreamableVideoSuccess()) {
                                intent.setData(Uri.parse(post.getVideoUrl()));
                                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            }
                        } else if (post.isRedgifs()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                            if (post.isLoadGfycatOrStreamableVideoSuccess()) {
                                intent.setData(Uri.parse(post.getVideoUrl()));
                                intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            }
                        } else if (post.isStreamable()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                            intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, post.getStreamableShortCode());
                        } else {
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                        }
                        intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, post.getTitle());
                        if (helper != null) {
                            intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, helper.getLatestPlaybackInfo().getResumePosition());
                        }
                        intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                        mActivity.startActivity(intent);
                    }
                }
            });

            previewImageView.setOnLongClickListener(view -> fullscreenButton.performClick());
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
            return helper != null && mediaUri != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (mediaUri == null) {
                return;
            }
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.DefaultEventListener() {
                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.length; i++) {
                                String mimeType = trackGroups.get(i).getFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    if (mFragment.getMasterMutingOption() != null) {
                                        volume = mFragment.getMasterMutingOption() ? 0f : 1f;
                                    }
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_white_rounded_24dp));
                                    } else {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_white_rounded_24dp));
                                    }
                                    break;
                                }
                            }
                        } else {
                            muteButton.setVisibility(View.GONE);
                        }
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
            if (helper != null && mediaUri != null) {
                helper.play();
            }
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
            return canPlayVideo && mediaUri != null && ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        @Override
        public int getPlayerOrder() {
            return getBindingAdapterPosition();
        }
    }

    class PostWithPreviewTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_with_preview)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_with_preview)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_with_preview)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_with_preview)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_with_preview)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_with_preview)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_with_preview)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_with_preview)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_with_preview)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_with_preview)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_with_preview)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_with_preview)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_with_preview)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_with_preview)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_with_preview)
        TextView linkTextView;
        @BindView(R.id.video_or_gif_indicator_image_view_item_post_with_preview)
        ImageView videoOrGifIndicatorImageView;
        @BindView(R.id.image_wrapper_relative_layout_item_post_with_preview)
        FrameLayout imageWrapperRelativeLayout;
        @BindView(R.id.progress_bar_item_post_with_preview)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_with_preview)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_text_view_item_post_with_preview)
        TextView errorTextView;
        @BindView(R.id.image_view_no_preview_gallery_item_post_with_preview)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_with_preview)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_with_preview)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_with_preview)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_with_preview)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_with_preview)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_with_preview)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_with_preview)
        ImageView shareButton;
        RequestListener<Drawable> glideRequestListener;

        PostWithPreviewTypeViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(
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

            if (mActivity.typeface != null) {
                linkTextView.setTypeface(mActivity.typeface);
                errorTextView.setTypeface(mActivity.typeface);
            }
            linkTextView.setTextColor(mSecondaryTextColor);
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewLinkImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            videoOrGifIndicatorImageView.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            videoOrGifIndicatorImageView.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            errorTextView.setTextColor(mPrimaryTextColor);

            imageView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);
                    openMedia(post);
                }
            });

            errorTextView.setOnClickListener(view -> {
                progressBar.setVisibility(View.VISIBLE);
                errorTextView.setVisibility(View.GONE);
                loadImage(this);
            });

            noPreviewLinkImageView.setOnClickListener(view -> {
                imageView.performClick();
            });

            glideRequestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    errorTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            };
        }
    }

    class PostTextTypeViewHolder extends PostBaseViewHolder {
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
            setBaseView(
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

            if (mActivity.contentTypeface != null) {
                contentTextView.setTypeface(mActivity.titleTypeface);
            }
            contentTextView.setTextColor(mPostContentColor);
        }
    }

    public class PostCompactBaseViewHolder extends RecyclerView.ViewHolder {
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
        FrameLayout noPreviewPostImageFrameLayout;
        ImageView noPreviewPostImageView;
        Barrier imageBarrier;
        ConstraintLayout bottomConstraintLayout;
        ImageView upvoteButton;
        TextView scoreTextView;
        ImageView downvoteButton;
        TextView commentsCountTextView;
        ImageView saveButton;
        ImageView shareButton;
        View divider;
        RequestListener<Drawable> requestListener;
        Post post;

        int currentPosition;

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
            this.noPreviewPostImageFrameLayout = noPreviewLinkImageFrameLayout;
            this.noPreviewPostImageView = noPreviewLinkImageView;
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

            if (((ViewGroup) itemView).getLayoutTransition() != null) {
                ((ViewGroup) itemView).getLayoutTransition().setAnimateParentHierarchy(false);
            }

            if (mActivity.typeface != null) {
                nameTextView.setTypeface(mActivity.typeface);
                postTimeTextView.setTypeface(mActivity.typeface);
                typeTextView.setTypeface(mActivity.typeface);
                spoilerTextView.setTypeface(mActivity.typeface);
                nsfwTextView.setTypeface(mActivity.typeface);
                flairTextView.setTypeface(mActivity.typeface);
                awardsTextView.setTypeface(mActivity.typeface);
                linkTextView.setTypeface(mActivity.typeface);
                scoreTextView.setTypeface(mActivity.typeface);
                commentsCountTextView.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                titleTextView.setTypeface(mActivity.titleTypeface);
            }

            itemView.setBackgroundColor(mCardViewBackgroundColor);
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
            playButtonImageView.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            playButtonImageView.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewLinkImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
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
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && canStartActivity) {
                    markPostRead(post, true);
                    openViewPostDetailActivity(post, getBindingAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(view -> {
                if (mLongPressToHideToolbarInCompactLayout) {
                    if (bottomConstraintLayout.getLayoutParams().height == 0) {
                        ViewGroup.LayoutParams params = bottomConstraintLayout.getLayoutParams();
                        params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        bottomConstraintLayout.setLayoutParams(params);
                        mCallback.delayTransition();
                    } else {
                        mCallback.delayTransition();
                        ViewGroup.LayoutParams params = bottomConstraintLayout.getLayoutParams();
                        params.height = 0;
                        bottomConstraintLayout.setLayoutParams(params);
                    }
                }
                return true;
            });

            nameTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && canStartActivity) {
                    canStartActivity = false;
                    if (mDisplaySubredditName) {
                        Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                post.getSubredditName());
                        mActivity.startActivity(intent);
                    } else {
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mActivity.startActivity(intent);
                    }
                }
            });

            iconGifImageView.setOnClickListener(view -> nameTextView.performClick());

            nsfwTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && !(mActivity instanceof FilteredPostsActivity)) {
                    mCallback.nsfwChipClicked();
                }
            });

            typeTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && !(mActivity instanceof FilteredPostsActivity)) {
                    mCallback.typeChipClicked(post.getPostType());
                }
            });

            flairTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null && !(mActivity instanceof FilteredPostsActivity)) {
                    mCallback.flairChipClicked(post.getFlair());
                }
            });

            imageView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);
                    openMedia(post);
                }
            });

            noPreviewLinkImageFrameLayout.setOnClickListener(view -> {
                imageView.performClick();
            });

            upvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mMarkPostsAsReadAfterVoting) {
                        markPostRead(post, true);
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

                    if (!mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                                }
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            if (getBindingAdapterPosition() == position) {
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                                }
                                upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                                downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            downvoteButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mMarkPostsAsReadAfterVoting) {
                        markPostRead(post, true);
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

                    if (!mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mDownvotedColor);
                                }

                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
                                }
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            if (getBindingAdapterPosition() == position) {
                                if (!mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                                }
                                upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                                downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            saveButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
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
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }
                                });
                    } else {
                        saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }
                                });
                    }
                }
            });

            shareButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    shareLink(post);
                }
            });

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            };
        }

        void markPostRead(Post post, boolean changePostItemColor) {
            if (mAccessToken != null && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead(true);
                if (changePostItemColor) {
                    itemView.setBackgroundColor(mReadPostCardViewBackgroundColor);
                    titleTextView.setTextColor(mReadPostTitleColor);
                }
                if (mActivity != null && mActivity instanceof MarkPostAsReadInterface) {
                    ((MarkPostAsReadInterface) mActivity).markPostAsRead(post);
                    mFragment.markPostAsRead(post);
                }
            }
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

    class PostGalleryViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_post_gallery)
        ProgressBar progressBar;
        @BindView(R.id.video_or_gif_indicator_image_view_item_post_gallery)
        ImageView videoOrGifIndicatorImageView;
        @BindView(R.id.image_view_item_post_gallery)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_text_view_item_gallery)
        TextView errorTextView;
        @BindView(R.id.image_view_no_preview_item_post_gallery)
        ImageView noPreviewImageView;
        @BindView(R.id.title_text_view_item_post_gallery)
        TextView titleTextView;
        RequestListener<Drawable> requestListener;
        Post post;
        Post.Preview preview;

        int currentPosition;

        public PostGalleryViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mActivity.typeface != null) {
                errorTextView.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                titleTextView.setTypeface(mActivity.titleTypeface);
            }
            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            titleTextView.setTextColor(mPostTitleColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            videoOrGifIndicatorImageView.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            videoOrGifIndicatorImageView.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            errorTextView.setTextColor(mPrimaryTextColor);

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position >= 0 && canStartActivity) {
                    Post post = getItem(position);
                    if (post != null) {
                        markPostRead(post, true);

                        if (post.getPostType() == Post.TEXT_TYPE || !mSharedPreferences.getBoolean(SharedPreferencesUtils.CLICK_TO_SHOW_MEDIA_IN_GALLERY_LAYOUT, false)) {
                            openViewPostDetailActivity(post, getBindingAdapterPosition());
                        } else {
                            openMedia(post);
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position >= 0 && canStartActivity) {
                    Post post = getItem(position);
                    if (post != null) {
                        markPostRead(post, true);

                        if (post.getPostType() == Post.TEXT_TYPE || mSharedPreferences.getBoolean(SharedPreferencesUtils.CLICK_TO_SHOW_MEDIA_IN_GALLERY_LAYOUT, false)) {
                            openViewPostDetailActivity(post, getBindingAdapterPosition());
                        } else {
                            openMedia(post);
                        }
                    }
                }

                return true;
            });

            errorTextView.setOnClickListener(view -> {
                progressBar.setVisibility(View.VISIBLE);
                errorTextView.setVisibility(View.GONE);
                loadImage(this);
            });

            noPreviewImageView.setOnClickListener(view -> {
                itemView.performClick();
            });

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    errorTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            };
        }

        void markPostRead(Post post, boolean changePostItemColor) {
            if (mAccessToken != null && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead(true);
                if (changePostItemColor) {
                    itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                    titleTextView.setTextColor(mReadPostTitleColor);
                }
                if (mActivity != null && mActivity instanceof MarkPostAsReadInterface) {
                    ((MarkPostAsReadInterface) mActivity).markPostAsRead(post);
                    mFragment.markPostAsRead(post);
                }
            }
        }
    }

    class PostCard2VideoAutoplayViewHolder extends PostBaseViewHolder implements ToroPlayer {
        @BindView(R.id.icon_gif_image_view_item_post_card_2_video_autoplay)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_card_2_video_autoplay)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_card_2_video_autoplay)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_card_2_video_autoplay)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_card_2_video_autoplay)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_card_2_video_autoplay)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_card_2_video_autoplay)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_card_2_video_autoplay)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_card_2_video_autoplay)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_card_2_video_autoplay)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_card_2_video_autoplay)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_card_2_video_autoplay)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_card_2_video_autoplay)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_card_2_video_autoplay)
        CustomTextView awardsTextView;
        @BindView(R.id.aspect_ratio_frame_layout_item_post_card_2_video_autoplay)
        AspectRatioFrameLayout aspectRatioFrameLayout;
        @BindView(R.id.preview_image_view_item_post_card_2_video_autoplay)
        GifImageView previewImageView;
        @BindView(R.id.error_loading_gfycat_image_view_item_post_card_2_video_autoplay)
        ImageView errorLoadingGfycatImageView;
        @BindView(R.id.player_view_item_post_card_2_video_autoplay)
        PlayerView videoPlayer;
        @BindView(R.id.mute_exo_playback_control_view)
        ImageView muteButton;
        @BindView(R.id.fullscreen_exo_playback_control_view)
        ImageView fullscreenButton;
        @BindView(R.id.bottom_constraint_layout_item_post_card_2_video_autoplay)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_card_2_video_autoplay)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_card_2_video_autoplay)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_card_2_video_autoplay)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_card_2_video_autoplay)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_card_2_video_autoplay)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_card_2_video_autoplay)
        ImageView shareButton;
        @BindView(R.id.divider_item_post_card_2_video_autoplay)
        View divider;

        @Nullable
        ExoPlayerViewHelper helper;
        private Uri mediaUri;
        private float volume;
        public Call<String> fetchGfycatOrStreamableVideoCall;

        PostCard2VideoAutoplayViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(
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
                    shareButton,
                    true);

            divider.setBackgroundColor(mDividerColor);

            aspectRatioFrameLayout.setOnClickListener(null);

            muteButton.setOnClickListener(view -> {
                if (helper != null) {
                    if (helper.getVolume() != 0) {
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_white_rounded_24dp));
                        helper.setVolume(0f);
                        volume = 0f;
                        mFragment.videoAutoplayChangeMutingOption(true);
                    } else {
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_white_rounded_24dp));
                        helper.setVolume(1f);
                        volume = 1f;
                        mFragment.videoAutoplayChangeMutingOption(false);
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);
                    Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                    if (post.isImgur()) {
                        intent.setData(Uri.parse(post.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                    } else if (post.isGfycat()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                        intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                        if (post.isLoadGfycatOrStreamableVideoSuccess()) {
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                        }
                    } else if (post.isRedgifs()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                        intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, post.getGfycatId());
                        if (post.isLoadGfycatOrStreamableVideoSuccess()) {
                            intent.setData(Uri.parse(post.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                        }
                    } else if (post.isStreamable()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                        intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, post.getStreamableShortCode());
                    } else {
                        intent.setData(Uri.parse(post.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                        intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                        intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                    }
                    intent.putExtra(ViewVideoActivity.EXTRA_POST_TITLE, post.getTitle());
                    if (helper != null) {
                        intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, helper.getLatestPlaybackInfo().getResumePosition());
                    }
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                }
            });

            previewImageView.setOnLongClickListener(view -> fullscreenButton.performClick());
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
            return helper != null && mediaUri != null ? helper.getLatestPlaybackInfo() : new PlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (mediaUri == null) {
                return;
            }
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.DefaultEventListener() {
                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.length; i++) {
                                String mimeType = trackGroups.get(i).getFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    if (mFragment.getMasterMutingOption() != null) {
                                        volume = mFragment.getMasterMutingOption() ? 0f : 1f;
                                    }
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_white_rounded_24dp));
                                    } else {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_white_rounded_24dp));
                                    }
                                    break;
                                }
                            }
                        } else {
                            muteButton.setVisibility(View.GONE);
                        }
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
            if (helper != null && mediaUri != null) {
                helper.play();
            }
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
            return canPlayVideo && mediaUri != null && ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        @Override
        public int getPlayerOrder() {
            return getBindingAdapterPosition();
        }
    }

    class PostCard2WithPreviewViewHolder extends PostBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_card_2_with_preview)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_card_2_with_preview)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_card_2_with_preview)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_card_2_with_preview)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_card_2_with_preview)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_card_2_with_preview)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_card_2_with_preview)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_card_2_with_preview)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_card_2_with_preview)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_card_2_with_preview)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_card_2_with_preview)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_card_2_with_preview)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_card_2_with_preview)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_card_2_with_preview)
        CustomTextView awardsTextView;
        @BindView(R.id.link_text_view_item_post_card_2_with_preview)
        TextView linkTextView;
        @BindView(R.id.video_or_gif_indicator_image_view_item_post_card_2_with_preview)
        ImageView videoOrGifIndicatorImageView;
        @BindView(R.id.progress_bar_item_post_card_2_with_preview)
        ProgressBar progressBar;
        @BindView(R.id.image_view_item_post_card_2_with_preview)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_text_view_item_post_card_2_with_preview)
        TextView errorTextView;
        @BindView(R.id.image_view_no_preview_gallery_item_post_card_2_with_preview)
        ImageView noPreviewImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_card_2_with_preview)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_card_2_with_preview)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_card_2_with_preview)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_card_2_with_preview)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_card_2_with_preview)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_card_2_with_preview)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_card_2_with_preview)
        ImageView shareButton;
        @BindView(R.id.divider_item_post_card_2_with_preview)
        View divider;
        RequestListener<Drawable> requestListener;

        PostCard2WithPreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(
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
                    shareButton,
                    true);

            if (mActivity.typeface != null) {
                linkTextView.setTypeface(mActivity.typeface);
                errorTextView.setTypeface(mActivity.typeface);
            }
            linkTextView.setTextColor(mSecondaryTextColor);
            noPreviewImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            videoOrGifIndicatorImageView.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            videoOrGifIndicatorImageView.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            errorTextView.setTextColor(mPrimaryTextColor);
            divider.setBackgroundColor(mDividerColor);

            imageView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);
                    openMedia(post);
                }
            });

            errorTextView.setOnClickListener(view -> {
                progressBar.setVisibility(View.VISIBLE);
                errorTextView.setVisibility(View.GONE);
                loadImage(this);
            });

            noPreviewImageView.setOnClickListener(view -> {
                imageView.performClick();
            });

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    errorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    errorTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            };
        }
    }

    class PostCard2TextTypeViewHolder extends PostBaseViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_card_2_text)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.subreddit_name_text_view_item_post_card_2_text)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post_card_2_text)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post_card_2_text)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_item_post_card_2_text)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_item_post_card_2_text)
        TextView titleTextView;
        @BindView(R.id.content_text_view_item_post_card_2_text)
        TextView contentTextView;
        @BindView(R.id.type_text_view_item_post_card_2_text)
        CustomTextView typeTextView;
        @BindView(R.id.archived_image_view_item_post_card_2_text)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_card_2_text)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_card_2_text)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_card_2_text)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_card_2_text)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_card_2_text)
        CustomTextView flairTextView;
        @BindView(R.id.awards_text_view_item_post_card_2_text)
        CustomTextView awardsTextView;
        @BindView(R.id.bottom_constraint_layout_item_post_card_2_text)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_card_2_text)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_card_2_text)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_card_2_text)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_card_2_text)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_card_2_text)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_card_2_text)
        ImageView shareButton;
        @BindView(R.id.divider_item_post_card_2_text)
        View divider;

        PostCard2TextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(
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
                    shareButton,
                    true);

            if (mActivity.contentTypeface != null) {
                contentTextView.setTypeface(mActivity.contentTypeface);
            }
            contentTextView.setTextColor(mPostContentColor);
            divider.setBackgroundColor(mDividerColor);
        }
    }
}
