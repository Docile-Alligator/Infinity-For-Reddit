package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
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
import androidx.annotation.OptIn;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.common.Tracks;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.common.util.Util;
import androidx.media3.ui.AspectRatioFrameLayout;
import androidx.media3.ui.DefaultTimeBar;
import androidx.media3.ui.PlayerView;
import androidx.media3.ui.TimeBar;
import androidx.paging.PagingDataAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.button.MaterialButton;
import com.google.common.collect.ImmutableList;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Provider;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.FetchVideoLinkListener;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.FilteredPostsActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.apis.RedgifsAPI;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.PostOptionsBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard2GalleryTypeBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard2TextBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard2VideoAutoplayBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard2VideoAutoplayLegacyControllerBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard2WithPreviewBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard3GalleryTypeBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard3TextBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard3VideoTypeAutoplayBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard3VideoTypeAutoplayLegacyControllerBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCard3WithPreviewBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCompactBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostCompactRightThumbnailBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostGalleryBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostGalleryGalleryTypeBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostGalleryTypeBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostTextBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostVideoTypeAutoplayBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostVideoTypeAutoplayLegacyControllerBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostWithPreviewBinding;
import ml.docilealligator.infinityforreddit.events.PostUpdateEventToPostDetailFragment;
import ml.docilealligator.infinityforreddit.fragments.PostFragment;
import ml.docilealligator.infinityforreddit.post.FetchStreamableVideo;
import ml.docilealligator.infinityforreddit.post.MarkPostAsReadInterface;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.thing.FetchRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.thing.SaveThing;
import ml.docilealligator.infinityforreddit.thing.StreamableVideo;
import ml.docilealligator.infinityforreddit.thing.VoteThing;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.videoautoplay.CacheManager;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoCreator;
import ml.docilealligator.infinityforreddit.videoautoplay.ExoPlayerViewHelper;
import ml.docilealligator.infinityforreddit.videoautoplay.Playable;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroPlayer;
import ml.docilealligator.infinityforreddit.videoautoplay.ToroUtil;
import ml.docilealligator.infinityforreddit.videoautoplay.media.PlaybackInfo;
import ml.docilealligator.infinityforreddit.videoautoplay.widget.Container;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Call;
import retrofit2.Retrofit;

/**
 * Created by alex on 2/25/18.
 */

public class PostRecyclerViewAdapter extends PagingDataAdapter<Post, RecyclerView.ViewHolder> implements CacheManager {
    private static final int VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE = 1;
    private static final int VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE = 2;
    private static final int VIEW_TYPE_POST_CARD_GALLERY_TYPE = 3;
    private static final int VIEW_TYPE_POST_CARD_TEXT_TYPE = 4;
    private static final int VIEW_TYPE_POST_COMPACT = 5;
    private static final int VIEW_TYPE_POST_GALLERY = 6;
    private static final int VIEW_TYPE_POST_GALLERY_GALLERY_TYPE = 7;
    private static final int VIEW_TYPE_POST_CARD_2_VIDEO_AUTOPLAY_TYPE = 8;
    private static final int VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE = 9;
    private static final int VIEW_TYPE_POST_CARD_2_GALLERY_TYPE = 10;
    private static final int VIEW_TYPE_POST_CARD_2_TEXT_TYPE = 11;
    private static final int VIEW_TYPE_POST_CARD_3_VIDEO_AUTOPLAY_TYPE = 12;
    private static final int VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE = 13;
    private static final int VIEW_TYPE_POST_CARD_3_GALLERY_TYPE = 14;
    private static final int VIEW_TYPE_POST_CARD_3_TEXT_TYPE = 15;

    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<>() {
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
    private SharedPreferences mCurrentAccountSharedPreferences;
    private Executor mExecutor;
    private Retrofit mOauthRetrofit;
    private Retrofit mRedgifsRetrofit;
    private Provider<StreamableAPI> mStreamableApiProvider;
    private String mAccessToken;
    private String mAccountName;
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
    private int mFilledCardViewBackgroundColor;
    private int mReadPostFilledCardViewBackgroundColor;
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
    private int mModeratorColor;
    private int mSpoilerBackgroundColor;
    private int mSpoilerTextColor;
    private int mFlairBackgroundColor;
    private int mFlairTextColor;
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
    private boolean mHideSubredditAndUserPrefix;
    private boolean mHideTheNumberOfVotes;
    private boolean mHideTheNumberOfComments;
    private boolean mLegacyAutoplayVideoControllerUI;
    private boolean mFixedHeightPreviewInCard;
    private boolean mHideTextPostContent;
    private boolean mEasierToWatchInFullScreen;
    private ExoCreator mExoCreator;
    private Callback mCallback;
    private boolean canPlayVideo = true;
    private RecyclerView.RecycledViewPool mGalleryRecycledViewPool;

    public PostRecyclerViewAdapter(BaseActivity activity, PostFragment fragment, Executor executor, Retrofit oauthRetrofit,
                                   Retrofit redgifsRetrofit, Provider<StreamableAPI> streamableApiProvider,
                                   CustomThemeWrapper customThemeWrapper, Locale locale,
                                   @Nullable String accessToken, @NonNull String accountName, int postType, int postLayout, boolean displaySubredditName,
                                   SharedPreferences sharedPreferences, SharedPreferences currentAccountSharedPreferences,
                                   SharedPreferences nsfwAndSpoilerSharedPreferences,
                                   SharedPreferences postHistorySharedPreferences,
                                   ExoCreator exoCreator, Callback callback) {
        super(DIFF_CALLBACK);
        if (activity != null) {
            mActivity = activity;
            mFragment = fragment;
            mSharedPreferences = sharedPreferences;
            mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
            mExecutor = executor;
            mOauthRetrofit = oauthRetrofit;
            mRedgifsRetrofit = redgifsRetrofit;
            mStreamableApiProvider = streamableApiProvider;
            mAccessToken = accessToken;
            mAccountName = accountName;
            mPostType = postType;
            mDisplaySubredditName = displaySubredditName;
            mNeedBlurNsfw = nsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
            mDoNotBlurNsfwInNsfwSubreddits = nsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
            mNeedBlurSpoiler = nsfwAndSpoilerSharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
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

            mMarkPostsAsRead = postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_BASE, false);
            mMarkPostsAsReadAfterVoting = postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_AFTER_VOTING_BASE, false);
            mMarkPostsAsReadOnScroll = postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.MARK_POSTS_AS_READ_ON_SCROLL_BASE, false);

            mHidePostType = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_TYPE, false);
            mHidePostFlair = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_FLAIR, false);
            mHideSubredditAndUserPrefix = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBREDDIT_AND_USER_PREFIX, false);
            mHideTheNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES, false);
            mHideTheNumberOfComments = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_COMMENTS, false);
            mLegacyAutoplayVideoControllerUI = sharedPreferences.getBoolean(SharedPreferencesUtils.LEGACY_AUTOPLAY_VIDEO_CONTROLLER_UI, false);
            mFixedHeightPreviewInCard = sharedPreferences.getBoolean(SharedPreferencesUtils.FIXED_HEIGHT_PREVIEW_IN_CARD, false);
            mHideTextPostContent = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_TEXT_POST_CONTENT, false);
            mEasierToWatchInFullScreen = sharedPreferences.getBoolean(SharedPreferencesUtils.EASIER_TO_WATCH_IN_FULL_SCREEN, false);

            mPostLayout = postLayout;
            mDefaultLinkPostLayout = Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.DEFAULT_LINK_POST_LAYOUT_KEY, "-1"));

            mColorAccent = customThemeWrapper.getColorAccent();
            mCardViewBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
            //mCardViewBackgroundColor = Color.parseColor("#FBEEFC");
            mReadPostCardViewBackgroundColor = customThemeWrapper.getReadPostCardViewBackgroundColor();
            mFilledCardViewBackgroundColor = customThemeWrapper.getFilledCardViewBackgroundColor();
            mReadPostFilledCardViewBackgroundColor = customThemeWrapper.getReadPostFilledCardViewBackgroundColor();
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
            mModeratorColor = customThemeWrapper.getModerator();
            mSpoilerBackgroundColor = customThemeWrapper.getSpoilerBackgroundColor();
            mSpoilerTextColor = customThemeWrapper.getSpoilerTextColor();
            mFlairBackgroundColor = customThemeWrapper.getFlairBackgroundColor();
            mFlairTextColor = customThemeWrapper.getFlairTextColor();
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

            mScale = resources.getDisplayMetrics().density;
            mGlide = Glide.with(mActivity);
            mMaxResolution = Integer.parseInt(mSharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000"));
            mSaveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(mMaxResolution);
            mLocale = locale;
            mExoCreator = exoCreator;
            mCallback = callback;

            mGalleryRecycledViewPool = new RecyclerView.RecycledViewPool();
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
                        return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                    case Post.GALLERY_TYPE:
                        return VIEW_TYPE_POST_CARD_GALLERY_TYPE;
                    case Post.LINK_TYPE:
                    case Post.NO_PREVIEW_LINK_TYPE:
                        switch (mDefaultLinkPostLayout) {
                            case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                                return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_CARD_3:
                                return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
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
                        case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                            return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                        case SharedPreferencesUtils.POST_LAYOUT_CARD_3:
                            return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
                        case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                            return VIEW_TYPE_POST_GALLERY;
                    }
                }
            }
            return VIEW_TYPE_POST_COMPACT;
        } else if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_GALLERY) {
            Post post = getItem(position);
            if (post != null) {
                if (post.getPostType() == Post.GALLERY_TYPE) {
                    return VIEW_TYPE_POST_GALLERY_GALLERY_TYPE;
                } else {
                    return VIEW_TYPE_POST_GALLERY;
                }
            } else {
                return VIEW_TYPE_POST_GALLERY;
            }
        } else if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_CARD_2) {
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
                        return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                    case Post.GALLERY_TYPE:
                        return VIEW_TYPE_POST_CARD_2_GALLERY_TYPE;
                    case Post.LINK_TYPE:
                    case Post.NO_PREVIEW_LINK_TYPE:
                        switch (mDefaultLinkPostLayout) {
                            case SharedPreferencesUtils.POST_LAYOUT_CARD:
                                return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_CARD_3:
                                return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
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
        } else {
            Post post = getItem(position);
            if (post != null) {
                switch (post.getPostType()) {
                    case Post.VIDEO_TYPE:
                        if (mAutoplay) {
                            if ((!mAutoplayNsfwVideos && post.isNSFW()) || post.isSpoiler()) {
                                return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
                            }
                            return VIEW_TYPE_POST_CARD_3_VIDEO_AUTOPLAY_TYPE;
                        }
                        return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
                    case Post.GIF_TYPE:
                    case Post.IMAGE_TYPE:
                        return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
                    case Post.GALLERY_TYPE:
                        return VIEW_TYPE_POST_CARD_3_GALLERY_TYPE;
                    case Post.LINK_TYPE:
                    case Post.NO_PREVIEW_LINK_TYPE:
                        switch (mDefaultLinkPostLayout) {
                            case SharedPreferencesUtils.POST_LAYOUT_CARD:
                                return VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_CARD_2:
                                return VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE;
                            case SharedPreferencesUtils.POST_LAYOUT_GALLERY:
                                return VIEW_TYPE_POST_GALLERY;
                            case SharedPreferencesUtils.POST_LAYOUT_COMPACT:
                                return VIEW_TYPE_POST_COMPACT;
                        }
                        return VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE;
                    default:
                        return VIEW_TYPE_POST_CARD_3_TEXT_TYPE;
                }
            }
            return VIEW_TYPE_POST_CARD_3_TEXT_TYPE;
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_POST_CARD_VIDEO_AUTOPLAY_TYPE) {
            if (mDataSavingMode) {
                return new PostWithPreviewTypeViewHolder(ItemPostWithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }

            if (mLegacyAutoplayVideoControllerUI) {
                return new PostVideoAutoplayLegacyControllerViewHolder(ItemPostVideoTypeAutoplayLegacyControllerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new PostVideoAutoplayViewHolder(ItemPostVideoTypeAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        } else if (viewType == VIEW_TYPE_POST_CARD_WITH_PREVIEW_TYPE) {
            return new PostWithPreviewTypeViewHolder(ItemPostWithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_GALLERY_TYPE) {
            return new PostGalleryTypeViewHolder(ItemPostGalleryTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_TEXT_TYPE) {
            return new PostTextTypeViewHolder(ItemPostTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_COMPACT) {
            if (mShowThumbnailOnTheRightInCompactLayout) {
                return new PostCompactRightThumbnailViewHolder(ItemPostCompactRightThumbnailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new PostCompactLeftThumbnailViewHolder(ItemPostCompactBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        } else if (viewType == VIEW_TYPE_POST_GALLERY) {
            return new PostGalleryViewHolder(ItemPostGalleryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_GALLERY_GALLERY_TYPE) {
            return new PostGalleryGalleryTypeViewHolder(ItemPostGalleryGalleryTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_2_VIDEO_AUTOPLAY_TYPE) {
            if (mDataSavingMode) {
                return new PostCard2WithPreviewViewHolder(ItemPostCard2WithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }

            if (mLegacyAutoplayVideoControllerUI) {
                return new PostCard2VideoAutoplayLegacyControllerViewHolder(ItemPostCard2VideoAutoplayLegacyControllerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new PostCard2VideoAutoplayViewHolder(ItemPostCard2VideoAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        } else if (viewType == VIEW_TYPE_POST_CARD_2_WITH_PREVIEW_TYPE) {
            return new PostCard2WithPreviewViewHolder(ItemPostCard2WithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_2_GALLERY_TYPE) {
            return new PostCard2GalleryTypeViewHolder(ItemPostCard2GalleryTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_2_TEXT_TYPE) {
            return new PostCard2TextTypeViewHolder(ItemPostCard2TextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_3_VIDEO_AUTOPLAY_TYPE) {
            if (mDataSavingMode) {
                return new PostMaterial3CardWithPreviewViewHolder(ItemPostCard3WithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
            if (mLegacyAutoplayVideoControllerUI) {
                return new PostMaterial3CardVideoAutoplayLegacyControllerViewHolder(ItemPostCard3VideoTypeAutoplayLegacyControllerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            } else {
                return new PostMaterial3CardVideoAutoplayViewHolder(ItemPostCard3VideoTypeAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            }
        } else if (viewType == VIEW_TYPE_POST_CARD_3_WITH_PREVIEW_TYPE) {
            return new PostMaterial3CardWithPreviewViewHolder(ItemPostCard3WithPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_POST_CARD_3_GALLERY_TYPE) {
            return new PostMaterial3CardGalleryTypeViewHolder(ItemPostCard3GalleryTypeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else {
            //VIEW_TYPE_POST_CARD_3_TEXT_TYPE
            return new PostMaterial3CardTextTypeViewHolder(ItemPostCard3TextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            Post post = getItem(position);
            if (post == null) {
                return;
            }

            ((PostViewHolder) holder).post = post;
            ((PostViewHolder) holder).currentPosition = position;

            if (post.isRead()) {
                ((PostViewHolder) holder).setItemViewBackgroundColor(true);
                ((PostBaseViewHolder) holder).titleTextView.setTextColor(mReadPostTitleColor);
            }

            if (mDisplaySubredditName) {
                if (post.getAuthorNamePrefixed().equals(post.getSubredditNamePrefixed())) {
                    if (post.getAuthorIconUrl() == null) {
                        mFragment.loadIcon(post.getAuthor(), false, (subredditOrUserName, iconUrl) -> {
                            if (mActivity != null && getItemCount() > 0 && post.getAuthor().equals(subredditOrUserName)) {
                                if (iconUrl == null || iconUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostViewHolder) holder).iconGifImageView);
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
                                .into(((PostViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostViewHolder) holder).iconGifImageView);
                    }
                } else {
                    if (post.getSubredditIconUrl() == null) {
                        mFragment.loadIcon(post.getSubredditName(), true, (subredditOrUserName, iconUrl) -> {
                            if (mActivity != null && getItemCount() > 0 && post.getSubredditName().equals(subredditOrUserName)) {
                                if (iconUrl == null || iconUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostViewHolder) holder).iconGifImageView);
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
                                .into(((PostViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostViewHolder) holder).iconGifImageView);
                    }
                }
            } else {
                if (post.getAuthorIconUrl() == null) {
                    String authorName = post.isAuthorDeleted() ? post.getSubredditName() : post.getAuthor();
                    mFragment.loadIcon(authorName, post.isAuthorDeleted(), (subredditOrUserName, iconUrl) -> {
                        if (mActivity != null && getItemCount() > 0) {
                            if (iconUrl == null || iconUrl.equals("") && authorName.equals(subredditOrUserName)) {
                                mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .into(((PostViewHolder) holder).iconGifImageView);
                            } else {
                                mGlide.load(iconUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                        .into(((PostViewHolder) holder).iconGifImageView);
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
                            .into(((PostViewHolder) holder).iconGifImageView);
                } else {
                    mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((PostViewHolder) holder).iconGifImageView);
                }
            }

            if (mShowElapsedTime) {
                ((PostViewHolder) holder).postTimeTextView.setText(
                        Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
            } else {
                ((PostViewHolder) holder).postTimeTextView.setText(Utils.getFormattedTime(mLocale, post.getPostTimeMillis(), mTimeFormatPattern));
            }

            ((PostViewHolder) holder).titleTextView.setText(post.getTitle());
            if (!mHideTheNumberOfVotes) {
                ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));
            } else {
                ((PostViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.vote));
            }

            if (((PostViewHolder) holder).typeTextView != null) {
                if (mHidePostType) {
                    ((PostViewHolder) holder).typeTextView.setVisibility(View.GONE);
                } else {
                    ((PostViewHolder) holder).typeTextView.setVisibility(View.VISIBLE);
                }
            }

            if (((PostViewHolder) holder).lockedImageView != null && post.isLocked()) {
                ((PostViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
            }

            if (((PostViewHolder) holder).nsfwTextView != null && post.isNSFW()) {
                ((PostViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
            }

            if (post.isSpoiler()) {
                ((PostViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
            }

            if (((PostViewHolder) holder).flairTextView != null && post.getFlair() != null && !post.getFlair().isEmpty()) {
                if (mHidePostFlair) {
                    ((PostViewHolder) holder).flairTextView.setVisibility(View.GONE);
                } else {
                    ((PostViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((PostViewHolder) holder).flairTextView, post.getFlair(), false);
                }
            }

            if (post.isArchived()) {
                if (((PostViewHolder) holder).archivedImageView != null) {
                    ((PostViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);
                }

                ((PostViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                ((PostViewHolder) holder).scoreTextView.setTextColor(mVoteAndReplyUnavailableVoteButtonColor);
                ((PostViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
            }

            if (((PostViewHolder) holder).crosspostImageView != null && post.isCrosspost()) {
                ((PostViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
            }

            switch (post.getVoteType()) {
                case 1:
                    //Upvoted
                    ((PostViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                    ((PostViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                    ((PostViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                    break;
                case -1:
                    //Downvoted
                    ((PostViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                    ((PostViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                    ((PostViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                    break;
            }

            if (mPostType == PostPagingSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
                ((PostViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.ic_thumbtack_24dp).into(((PostViewHolder) holder).stickiedPostImageView);
            }

            if (!mHideTheNumberOfComments) {
                ((PostViewHolder) holder).commentsCountButton.setVisibility(View.VISIBLE);
                ((PostViewHolder) holder).commentsCountButton.setText(Integer.toString(post.getNComments()));
            } else {
                ((PostViewHolder) holder).commentsCountButton.setVisibility(View.GONE);
            }

            if (post.isSaved()) {
                ((PostViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
            } else {
                ((PostViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
            }

            if (holder instanceof PostBaseViewHolder) {
                if (mHideSubredditAndUserPrefix) {
                    ((PostBaseViewHolder) holder).subredditTextView.setText(post.getSubredditName());
                    ((PostBaseViewHolder) holder).userTextView.setText(post.getAuthor());
                } else {
                    ((PostBaseViewHolder) holder).subredditTextView.setText(post.getSubredditNamePrefixed());
                    ((PostBaseViewHolder) holder).userTextView.setText(post.getAuthorNamePrefixed());
                }

                ((PostBaseViewHolder) holder).userTextView.setTextColor(
                        post.isModerator() ? mModeratorColor : mUsernameColor);

                if (holder instanceof PostBaseVideoAutoplayViewHolder) {
                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.previewImageView.setVisibility(View.VISIBLE);
                    Post.Preview preview = getSuitablePreview(post.getPreviews());
                    if (!mFixedHeightPreviewInCard && preview != null) {
                        ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.aspectRatioFrameLayout.setAspectRatio((float) preview.getPreviewWidth() / preview.getPreviewHeight());
                        mGlide.load(preview.getPreviewUrl()).centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.previewImageView);
                    } else {
                        ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.aspectRatioFrameLayout.setAspectRatio(1);
                    }
                    if (!((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.isManuallyPaused) {
                        if (mFragment.getMasterMutingOption() == null) {
                            ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.setVolume(mMuteAutoplayingVideos || (post.isNSFW() && mMuteNSFWVideo) ? 0f : 1f);
                        } else {
                            ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.setVolume(mFragment.getMasterMutingOption() ? 0f : 1f);
                        }
                    }

                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.loadVideo(position);
                } else if (holder instanceof PostWithPreviewTypeViewHolder) {
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.VISIBLE);
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        if (((PostWithPreviewTypeViewHolder) holder).typeTextView != null) {
                            ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.video));
                        }
                    } else if (post.getPostType() == Post.GIF_TYPE) {
                        if (!mAutoplay) {
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.VISIBLE);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));
                        }
                        if (((PostWithPreviewTypeViewHolder) holder).typeTextView != null) {
                            ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.gif));
                        }
                    } else if (post.getPostType() == Post.IMAGE_TYPE) {
                        if (((PostWithPreviewTypeViewHolder) holder).typeTextView != null) {
                            ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.image));
                        }
                    } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                        if (((PostWithPreviewTypeViewHolder) holder).typeTextView != null) {
                            ((PostWithPreviewTypeViewHolder) holder).typeTextView.setText(mActivity.getString(R.string.link));
                        }
                        ((PostWithPreviewTypeViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostWithPreviewTypeViewHolder) holder).linkTextView.setText(domain);
                        if (post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                        }
                    }

                    if (mDataSavingMode && mDisableImagePreview) {
                        ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                        if (post.getPostType() == Post.VIDEO_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_video_day_night_24dp);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                        } else if (post.getPostType() == Post.LINK_TYPE) {
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                        }
                    } else if (mDataSavingMode && mOnlyDisablePreviewInVideoAndGifPosts && (post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE)) {
                        ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                        ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_video_day_night_24dp);
                        ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                    } else {
                        if (post.getPostType() == Post.GIF_TYPE && ((post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(mAutoplay && mAutoplayNsfwVideos)) || (post.isSpoiler() && mNeedBlurSpoiler))) {
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                            ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                            ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                        } else {
                            Post.Preview preview = getSuitablePreview(post.getPreviews());
                            ((PostWithPreviewTypeViewHolder) holder).preview = preview;
                            if (preview != null) {
                                if (((PostWithPreviewTypeViewHolder) holder).imageWrapperFrameLayout != null) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageWrapperFrameLayout.setVisibility(View.VISIBLE);
                                }
                                ((PostWithPreviewTypeViewHolder) holder).imageView.setVisibility(View.VISIBLE);
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
                                ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.VISIBLE);
                                if (post.getPostType() == Post.VIDEO_TYPE) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_video_day_night_24dp);
                                    ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                                } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                                    ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                                } else if (post.getPostType() == Post.LINK_TYPE) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                                } else if (post.getPostType() == Post.GALLERY_TYPE) {
                                    ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                                }
                            }
                        }
                    }
                } else if (holder instanceof PostBaseGalleryTypeViewHolder) {
                    if (mDataSavingMode && mDisableImagePreview) {
                        ((PostBaseGalleryTypeViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                        ((PostBaseGalleryTypeViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                    } else {
                        ((PostBaseGalleryTypeViewHolder) holder).frameLayout.setVisibility(View.VISIBLE);
                        ((PostBaseGalleryTypeViewHolder) holder).imageIndexTextView.setText(mActivity.getString(R.string.image_index_in_gallery, 1, post.getGallery().size()));
                        Post.Preview preview = getSuitablePreview(post.getPreviews());
                        if (preview != null) {
                            if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                ((PostBaseGalleryTypeViewHolder) holder).adapter.setRatio(-1);
                            } else {
                                ((PostBaseGalleryTypeViewHolder) holder).adapter.setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                            }
                        } else {
                            ((PostBaseGalleryTypeViewHolder) holder).adapter.setRatio(-1);
                        }
                        ((PostBaseGalleryTypeViewHolder) holder).adapter.setGalleryImages(post.getGallery());
                        ((PostBaseGalleryTypeViewHolder) holder).adapter.setBlurImage(
                                (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (post.isSpoiler() && mNeedBlurSpoiler));
                    }
                } else if (holder instanceof PostTextTypeViewHolder) {
                    if (!mHideTextPostContent && !post.isSpoiler() && post.getSelfTextPlainTrimmed() != null && !post.getSelfTextPlainTrimmed().equals("")) {
                        ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
                        if (post.isRead()) {
                            ((PostTextTypeViewHolder) holder).contentTextView.setTextColor(mReadPostContentColor);
                        }
                        ((PostTextTypeViewHolder) holder).contentTextView.setText(post.getSelfTextPlainTrimmed());
                    }
                }
                mCallback.currentlyBindItem(holder.getBindingAdapterPosition());
            } else if (holder instanceof PostCompactBaseViewHolder) {
                if (mDisplaySubredditName) {
                    ((PostCompactBaseViewHolder) holder).nameTextView.setTextColor(mSubredditColor);
                    if (mHideSubredditAndUserPrefix) {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getSubredditName());
                    } else {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getSubredditNamePrefixed());
                    }
                } else {
                    ((PostCompactBaseViewHolder) holder).nameTextView.setTextColor(
                            post.isModerator() ? mModeratorColor : mUsernameColor);

                    if (mHideSubredditAndUserPrefix) {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getAuthor());
                    } else {
                        ((PostCompactBaseViewHolder) holder).nameTextView.setText(post.getAuthorNamePrefixed());
                    }
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

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE && !(mDataSavingMode && mDisableImagePreview)) {
                    ((PostCompactBaseViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    if (post.getPostType() == Post.GALLERY_TYPE && post.getPreviews() != null && post.getPreviews().isEmpty()) {
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                    }
                    if (post.getPreviews() != null && !post.getPreviews().isEmpty()) {
                        ((PostCompactBaseViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                        ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        loadImage(holder);
                    }
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.image);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_image_day_night_24dp);
                        }
                        break;
                    case Post.LINK_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.link);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_link_day_night_24dp);
                        }

                        ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCompactBaseViewHolder) holder).linkTextView.setText(domain);
                        break;
                    case Post.GIF_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gif);
                        if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_image_day_night_24dp);
                        } else {
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_24dp));
                        }
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.video);
                        if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_video_day_night_24dp);
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
                        ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_link_day_night_24dp);
                        break;
                    case Post.GALLERY_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.gallery);
                        if (mDataSavingMode && mDisableImagePreview) {
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).noPreviewPostImageView.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                        } else {
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                            ((PostCompactBaseViewHolder) holder).playButtonImageView.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_gallery_day_night_24dp));
                        }
                        break;
                    case Post.TEXT_TYPE:
                        ((PostCompactBaseViewHolder) holder).typeTextView.setText(R.string.text);
                        break;
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
                    ((PostGalleryViewHolder) holder).binding.titleTextViewItemPostGallery.setTextColor(mReadPostTitleColor);
                }

                if (mDataSavingMode && (mDisableImagePreview ||
                        ((post.getPostType() == Post.VIDEO_TYPE || post.getPostType() == Post.GIF_TYPE) && mOnlyDisablePreviewInVideoAndGifPosts))) {
                    ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                    if (post.getPostType() == Post.VIDEO_TYPE) {
                        ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_video_day_night_24dp);
                        ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.GONE);
                    } else if (post.getPostType() == Post.IMAGE_TYPE || post.getPostType() == Post.GIF_TYPE) {
                        ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                        ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.GONE);
                    } else if (post.getPostType() == Post.LINK_TYPE) {
                        ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                    } else if (post.getPostType() == Post.GALLERY_TYPE) {
                        ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                    }
                } else {
                    switch (post.getPostType()) {
                        case Post.IMAGE_TYPE: {
                            Post.Preview preview = getSuitablePreview(post.getPreviews());
                            ((PostGalleryViewHolder) holder).preview = preview;
                            if (preview != null) {
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setVisibility(View.VISIBLE);

                                if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                    int height = (int) (400 * mScale);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.getLayoutParams().height = height;
                                } else {
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery
                                            .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                                }
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.removeOnLayoutChangeListener(this);
                                        loadImage(holder);
                                    }
                                });
                            } else {
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                            }
                            break;
                        }
                        case Post.GIF_TYPE: {
                            if (post.getPostType() == Post.GIF_TYPE && ((post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(mAutoplay && mAutoplayNsfwVideos)) || (post.isSpoiler() && mNeedBlurSpoiler))) {
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                            } else {
                                Post.Preview preview = getSuitablePreview(post.getPreviews());
                                ((PostGalleryViewHolder) holder).preview = preview;
                                if (preview != null) {
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setVisibility(View.VISIBLE);
                                    ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.VISIBLE);
                                    ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));

                                    if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                        int height = (int) (400 * mScale);
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.getLayoutParams().height = height;
                                    } else {
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery
                                                .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                                    }
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                        @Override
                                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                            ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.removeOnLayoutChangeListener(this);
                                            loadImage(holder);
                                        }
                                    });
                                } else {
                                    ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                                    ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_image_day_night_24dp);
                                }
                            }
                            break;
                        }
                        case Post.VIDEO_TYPE: {
                            Post.Preview preview = getSuitablePreview(post.getPreviews());
                            ((PostGalleryViewHolder) holder).preview = preview;
                            if (preview != null) {
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_play_circle_36dp));

                                if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                    int height = (int) (400 * mScale);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.getLayoutParams().height = height;
                                } else {
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery
                                            .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                                }
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.removeOnLayoutChangeListener(this);
                                        loadImage(holder);
                                    }
                                });
                            } else {
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_video_day_night_24dp);
                            }
                            break;
                        }
                        case Post.LINK_TYPE: {
                            Post.Preview preview = getSuitablePreview(post.getPreviews());
                            ((PostGalleryViewHolder) holder).preview = preview;
                            if (preview != null) {
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_link_post_type_indicator_day_night_24dp));

                                if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                                    int height = (int) (400 * mScale);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.getLayoutParams().height = height;
                                } else {
                                    ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery
                                            .setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                                }
                                ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                                    @Override
                                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                        ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.removeOnLayoutChangeListener(this);
                                        loadImage(holder);
                                    }
                                });
                            } else {
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                                ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                            }
                            break;
                        }
                        case Post.NO_PREVIEW_LINK_TYPE: {
                            ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setImageResource(R.drawable.ic_link_day_night_24dp);
                            break;
                        }
                        case Post.TEXT_TYPE: {
                            ((PostGalleryViewHolder) holder).binding.titleTextViewItemPostGallery.setVisibility(View.VISIBLE);
                            ((PostGalleryViewHolder) holder).binding.titleTextViewItemPostGallery.setText(post.getTitle());
                            break;
                        }
                    }
                }
            }
        } else if (holder instanceof PostGalleryBaseGalleryTypeViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                ((PostGalleryBaseGalleryTypeViewHolder) holder).post = post;
                ((PostGalleryBaseGalleryTypeViewHolder) holder).currentPosition = position;
                if (post.isRead()) {
                    holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                }

                if (mDataSavingMode && mDisableImagePreview) {
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).noPreviewImageView.setVisibility(View.VISIBLE);
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).noPreviewImageView.setImageResource(R.drawable.ic_gallery_day_night_24dp);
                } else {
                    Post.Preview preview = getSuitablePreview(post.getPreviews());
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).preview = preview;

                    ((PostGalleryBaseGalleryTypeViewHolder) holder).frameLayout.setVisibility(View.VISIBLE);
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).imageIndexTextView.setText(mActivity.getString(R.string.image_index_in_gallery, 1, post.getGallery().size()));
                    if (preview != null) {
                        if (mFixedHeightPreviewInCard || (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0)) {
                            ((PostGalleryBaseGalleryTypeViewHolder) holder).adapter.setRatio(-1);
                        } else {
                            ((PostGalleryBaseGalleryTypeViewHolder) holder).adapter.setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                        }
                    } else {
                        ((PostGalleryBaseGalleryTypeViewHolder) holder).adapter.setRatio(-1);
                    }
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).adapter.setGalleryImages(post.getGallery());
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).adapter.setBlurImage(
                            (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (post.isSpoiler() && mNeedBlurSpoiler));
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
            ((PostWithPreviewTypeViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            Post post = ((PostWithPreviewTypeViewHolder) holder).post;
            Post.Preview preview = ((PostWithPreviewTypeViewHolder) holder).preview;
            if (preview != null) {
                String url;
                boolean blurImage = (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(post.getPostType() == Post.GIF_TYPE && mAutoplay && mAutoplayNsfwVideos)) || (post.isSpoiler() && mNeedBlurSpoiler);
                if (post.getPostType() == Post.GIF_TYPE && mAutoplay && !blurImage) {
                    url = post.getUrl();
                } else {
                    url = preview.getPreviewUrl();
                }
                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(((PostWithPreviewTypeViewHolder) holder).glideRequestListener);
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
                        .error(R.drawable.ic_error_outline_black_day_night_24dp).listener(((PostCompactBaseViewHolder) holder).requestListener);
                if ((post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (post.isSpoiler() && mNeedBlurSpoiler)) {
                    imageRequestBuilder
                            .transform(new BlurTransformation(50, 2)).into(((PostCompactBaseViewHolder) holder).imageView);
                } else {
                    imageRequestBuilder.into(((PostCompactBaseViewHolder) holder).imageView);
                }
            }
        } else if (holder instanceof PostGalleryViewHolder) {
            ((PostGalleryViewHolder) holder).binding.progressBarItemPostGallery.setVisibility(View.VISIBLE);
            Post post = ((PostGalleryViewHolder) holder).post;
            Post.Preview preview = ((PostGalleryViewHolder) holder).preview;
            if (preview != null) {
                String url;
                boolean blurImage = (post.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(post.getPostType() == Post.GIF_TYPE && mAutoplay && mAutoplayNsfwVideos)) || post.isSpoiler() && mNeedBlurSpoiler;
                if (post.getPostType() == Post.GIF_TYPE && mAutoplay && !blurImage) {
                    url = post.getUrl();
                } else {
                    url = preview.getPreviewUrl();
                }
                RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url).listener(((PostGalleryViewHolder) holder).requestListener);

                if (blurImage) {
                    imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                            .into(((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery);
                } else {
                    imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery);
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

    public void setEasierToWatchInFullScreen(boolean easierToWatchInFullScreen) {
        this.mEasierToWatchInFullScreen = easierToWatchInFullScreen;
    }

    @OptIn(markerClass = UnstableApi.class)
    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostViewHolder) holder).currentPosition;
                if (position < getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostViewHolder) holder).markPostRead(post, false);
                }
            }

            ((PostViewHolder) holder).setItemViewBackgroundColor(false);

            ((PostViewHolder) holder).titleTextView.setTextColor(mPostTitleColor);
            mGlide.clear(((PostViewHolder) holder).iconGifImageView);

            ((PostViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);

            if (((PostViewHolder) holder).crosspostImageView != null) {
                ((PostViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            }
            if (((PostViewHolder) holder).archivedImageView != null) {
                ((PostViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            }
            if (((PostViewHolder) holder).lockedImageView != null) {
                ((PostViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            }
            if (((PostViewHolder) holder).nsfwTextView != null) {
                ((PostViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            }
            if (((PostViewHolder) holder).spoilerTextView != null) {
                ((PostViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            }
            if (((PostViewHolder) holder).flairTextView != null) {
                ((PostViewHolder) holder).flairTextView.setText("");
                ((PostViewHolder) holder).flairTextView.setVisibility(View.GONE);
            }

            ((PostViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
            ((PostViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
            ((PostViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

            if (holder instanceof PostBaseViewHolder) {
                if (holder instanceof PostBaseVideoAutoplayViewHolder) {
                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.mediaUri = null;
                    if (((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.fetchRedgifsOrStreamableVideoCall != null
                            && !((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.fetchRedgifsOrStreamableVideoCall.isCanceled()) {
                        ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.fetchRedgifsOrStreamableVideoCall.cancel();
                        ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.fetchRedgifsOrStreamableVideoCall = null;
                    }
                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.errorLoadingRedgifsImageView.setVisibility(View.GONE);
                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.muteButton.setVisibility(View.GONE);
                    if (!((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.isManuallyPaused) {
                        ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.resetVolume();
                    }
                    mGlide.clear(((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.previewImageView);
                    ((PostBaseVideoAutoplayViewHolder) holder).toroPlayer.previewImageView.setVisibility(View.GONE);
                } else if (holder instanceof PostWithPreviewTypeViewHolder) {
                    mGlide.clear(((PostWithPreviewTypeViewHolder) holder).imageView);
                    if (((PostWithPreviewTypeViewHolder) holder).imageWrapperFrameLayout != null) {
                        ((PostWithPreviewTypeViewHolder) holder).imageWrapperFrameLayout.setVisibility(View.GONE);
                    }
                    ((PostWithPreviewTypeViewHolder) holder).imageView.setVisibility(View.GONE);
                    ((PostWithPreviewTypeViewHolder) holder).loadImageErrorTextView.setVisibility(View.GONE);
                    ((PostWithPreviewTypeViewHolder) holder).imageViewNoPreviewGallery.setVisibility(View.GONE);
                    ((PostWithPreviewTypeViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostWithPreviewTypeViewHolder) holder).videoOrGifIndicator.setVisibility(View.GONE);
                    ((PostWithPreviewTypeViewHolder) holder).linkTextView.setVisibility(View.GONE);
                } else if (holder instanceof PostBaseGalleryTypeViewHolder) {
                    ((PostBaseGalleryTypeViewHolder) holder).frameLayout.setVisibility(View.GONE);
                    ((PostBaseGalleryTypeViewHolder) holder).noPreviewImageView.setVisibility(View.GONE);
                    ((PostBaseGalleryTypeViewHolder) holder).adapter.setGalleryImages(null);
                } else if (holder instanceof PostTextTypeViewHolder) {
                    ((PostTextTypeViewHolder) holder).contentTextView.setText("");
                    ((PostTextTypeViewHolder) holder).contentTextView.setTextColor(mPostContentColor);
                    ((PostTextTypeViewHolder) holder).contentTextView.setVisibility(View.GONE);
                }
            } else if (holder instanceof PostCompactBaseViewHolder) {
                mGlide.clear(((PostCompactBaseViewHolder) holder).imageView);
                ((PostCompactBaseViewHolder) holder).relativeLayout.setVisibility(View.GONE);
                ((PostCompactBaseViewHolder) holder).linkTextView.setVisibility(View.GONE);
                ((PostCompactBaseViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((PostCompactBaseViewHolder) holder).imageView.setVisibility(View.GONE);
                ((PostCompactBaseViewHolder) holder).playButtonImageView.setVisibility(View.GONE);
                ((PostCompactBaseViewHolder) holder).noPreviewPostImageFrameLayout.setVisibility(View.GONE);
            }
        } else if (holder instanceof PostGalleryViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostGalleryViewHolder) holder).currentPosition;
                if (position < super.getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostGalleryViewHolder) holder).markPostRead(post, false);
                }
            }
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));

            ((PostGalleryViewHolder) holder).binding.titleTextViewItemPostGallery.setText("");
            ((PostGalleryViewHolder) holder).binding.titleTextViewItemPostGallery.setVisibility(View.GONE);
            mGlide.clear(((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery);
            ((PostGalleryViewHolder) holder).binding.imageViewItemPostGallery.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).binding.progressBarItemPostGallery.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).binding.loadImageErrorTextViewItemGallery.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).binding.videoOrGifIndicatorImageViewItemPostGallery.setVisibility(View.GONE);
            ((PostGalleryViewHolder) holder).binding.imageViewNoPreviewItemPostGallery.setVisibility(View.GONE);
        } else if (holder instanceof PostGalleryBaseGalleryTypeViewHolder) {
            if (mMarkPostsAsReadOnScroll) {
                int position = ((PostGalleryBaseGalleryTypeViewHolder) holder).currentPosition;
                if (position < super.getItemCount() && position >= 0) {
                    Post post = getItem(position);
                    ((PostGalleryBaseGalleryTypeViewHolder) holder).markPostRead(post, false);
                }
            }
            holder.itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            ((PostGalleryBaseGalleryTypeViewHolder) holder).frameLayout.setVisibility(View.GONE);
            ((PostGalleryBaseGalleryTypeViewHolder) holder).noPreviewImageView.setVisibility(View.GONE);
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
        openMedia(post, 0);
    }

    private void openMedia(Post post, int galleryItemIndex) {
        openMedia(post, galleryItemIndex, -1);
    }

    private void openMedia(Post post, long videoProgress) {
        openMedia(post, 0, videoProgress);
    }

    private void openMedia(Post post, int galleryItemIndex, long videoProgress) {
        if (canStartActivity) {
            canStartActivity = false;
            if (post.getPostType() == Post.VIDEO_TYPE) {
                Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                if (post.isImgur()) {
                    intent.setData(Uri.parse(post.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                } else if (post.isRedgifs()) {
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                    intent.putExtra(ViewVideoActivity.EXTRA_REDGIFS_ID, post.getRedgifsId());
                    if (post.isLoadRedgifsOrStreamableVideoSuccess()) {
                        intent.setData(Uri.parse(post.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                    }
                } else if (post.isStreamable()) {
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                    intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, post.getStreamableShortCode());
                    if (post.isLoadRedgifsOrStreamableVideoSuccess()) {
                        intent.setData(Uri.parse(post.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                    }
                } else {
                    intent.setData(Uri.parse(post.getVideoUrl()));
                    intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                }
                intent.putExtra(ViewVideoActivity.EXTRA_POST, post);
                if (videoProgress > 0) {
                    intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, videoProgress);
                }
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
                if (post.getMp4Variant() != null) {
                    Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                    intent.setData(Uri.parse(post.getMp4Variant()));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_DIRECT);
                    intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, post.getId());
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getMp4Variant());
                    intent.putExtra(ViewVideoActivity.EXTRA_POST, post);
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                } else {
                    Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, post.getSubredditName()
                            + "-" + post.getId() + ".gif");
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, post.getVideoUrl());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, post.getTitle());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, post.getSubredditName());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, post.isNSFW());
                    mActivity.startActivity(intent);
                }
            } else if (post.getPostType() == Post.LINK_TYPE || post.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                Uri uri = Uri.parse(post.getUrl());
                intent.setData(uri);
                intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, post.isNSFW());
                mActivity.startActivity(intent);
            } else if (post.getPostType() == Post.GALLERY_TYPE) {
                Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_POST, post);
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_GALLERY_ITEM_INDEX, galleryItemIndex);
                mActivity.startActivity(intent);
            }
        }
    }

    public void setCanPlayVideo(boolean canPlayVideo) {
        this.canPlayVideo = canPlayVideo;
    }

    public abstract class PostViewHolder extends RecyclerView.ViewHolder {
        AspectRatioGifImageView iconGifImageView;
        ImageView stickiedPostImageView;
        TextView postTimeTextView;
        TextView titleTextView;

        @Nullable
        CustomTextView typeTextView;
        @Nullable
        ImageView archivedImageView;
        @Nullable
        ImageView lockedImageView;
        @Nullable
        ImageView crosspostImageView;
        @Nullable
        CustomTextView nsfwTextView;
        @Nullable
        CustomTextView spoilerTextView;
        @Nullable
        CustomTextView flairTextView;

        MaterialButton upvoteButton;
        TextView scoreTextView;
        MaterialButton downvoteButton;
        MaterialButton commentsCountButton;
        MaterialButton saveButton;
        MaterialButton shareButton;
        Post post;
        int currentPosition;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         @Nullable CustomTextView typeTextView,
                         @Nullable ImageView archivedImageView,
                         @Nullable ImageView lockedImageView,
                         @Nullable ImageView crosspostImageView,
                         @Nullable CustomTextView nsfwTextView,
                         @Nullable CustomTextView spoilerTextView,
                         @Nullable CustomTextView flairTextView,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton) {
            this.iconGifImageView = iconGifImageView;
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

            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.commentsCountButton = commentsCountButton;
            this.saveButton = saveButton;
            this.shareButton = shareButton;

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

            userTextView.setOnClickListener(view -> {
                if (!canStartActivity) {
                    return;
                }
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post == null || post.isAuthorDeleted()) {
                    return;
                }
                canStartActivity = false;
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                mActivity.startActivity(intent);
            });

            setOnClickListeners(typeTextView,
                    nsfwTextView,
                    flairTextView,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView nameTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         @Nullable CustomTextView typeTextView,
                         @Nullable CustomTextView nsfwTextView,
                         @Nullable CustomTextView flairTextView,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton) {
            this.iconGifImageView = iconGifImageView;
            this.stickiedPostImageView = stickiedPostImageView;
            this.postTimeTextView = postTimeTextView;
            this.titleTextView = titleTextView;
            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.commentsCountButton = commentsCountButton;
            this.saveButton = saveButton;
            this.shareButton = shareButton;

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
                    } else if (!post.isAuthorDeleted()) {
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mActivity.startActivity(intent);
                    }
                }
            });

            iconGifImageView.setOnClickListener(view -> nameTextView.performClick());

            setOnClickListeners(typeTextView,
                    nsfwTextView,
                    flairTextView,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);
        }

        void setOnClickListeners(MaterialButton upvoteButton,
                                 TextView scoreTextView,
                                 MaterialButton downvoteButton,
                                 MaterialButton commentsCountButton,
                                 MaterialButton saveButton,
                                 MaterialButton shareButton) {
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

            itemView.setOnTouchListener((v, event) -> {
                if (event.getActionMasked() == MotionEvent.ACTION_UP || event.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    if (mFragment.isRecyclerViewItemSwipeable(this)) {
                        mActivity.unlockSwipeRightToGoBack();
                    }
                } else {
                    if (mFragment.isRecyclerViewItemSwipeable(this)) {
                        mActivity.lockSwipeRightToGoBack();
                    }
                }
                return false;
            });

            upvoteButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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

                    ColorStateList previousUpvoteButtonIconTint = upvoteButton.getIconTint();
                    ColorStateList previousDownvoteButtonIconTint = downvoteButton.getIconTint();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();
                    Drawable previousUpvoteButtonDrawable = upvoteButton.getIcon();
                    Drawable previousDownvoteButtonDrawable = downvoteButton.getIcon();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                    downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

                    if (previousVoteType != 1) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                        scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
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
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                                    scoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
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
                                upvoteButton.setIcon(previousUpvoteButtonDrawable);
                                upvoteButton.setIconTint(previousUpvoteButtonIconTint);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                                downvoteButton.setIcon(previousDownvoteButtonDrawable);
                                downvoteButton.setIconTint(previousDownvoteButtonIconTint);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            scoreTextView.setOnClickListener(view -> {
                upvoteButton.performClick();
            });

            downvoteButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
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

                    ColorStateList previousUpvoteButtonIconTint = upvoteButton.getIconTint();
                    ColorStateList previousDownvoteButtonIconTint = downvoteButton.getIconTint();
                    int previousScoreTextViewColor = scoreTextView.getCurrentTextColor();
                    Drawable previousUpvoteButtonDrawable = upvoteButton.getIcon();
                    Drawable previousDownvoteButtonDrawable = downvoteButton.getIcon();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                    upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

                    if (previousVoteType != -1) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                        scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
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
                                    downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                                    downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                                    scoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                post.setVoteType(0);
                                if (currentPosition == position) {
                                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                    downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                                    scoreTextView.setTextColor(mPostIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
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
                                upvoteButton.setIcon(previousUpvoteButtonDrawable);
                                upvoteButton.setIconTint(previousUpvoteButtonIconTint);
                                scoreTextView.setTextColor(previousScoreTextViewColor);
                                downvoteButton.setIcon(previousDownvoteButtonDrawable);
                                downvoteButton.setIconTint(previousDownvoteButtonIconTint);
                            }

                            EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                        }
                    }, post.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            commentsCountButton.setOnClickListener(view -> itemView.performClick());

            saveButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Post post = getItem(position);
                if (post != null) {
                    if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isSaved()) {
                        saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(false);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }
                                });
                    } else {
                        saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                        }
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToPostDetailFragment(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        if (getBindingAdapterPosition() == position) {
                                            saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
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

            shareButton.setOnLongClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return false;
                }
                Post post = getItem(position);
                if (post != null) {
                    mActivity.copyLink(post.getPermalink());
                    return true;
                }
                return false;
            });
        }

        void setOnClickListeners(@Nullable CustomTextView typeTextView,
                                 @Nullable CustomTextView nsfwTextView,
                                 @Nullable CustomTextView flairTextView,
                                 MaterialButton upvoteButton,
                                 TextView scoreTextView,
                                 MaterialButton downvoteButton,
                                 MaterialButton commentsCountButton,
                                 MaterialButton saveButton,
                                 MaterialButton shareButton) {
            setOnClickListeners(upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            if (!(mActivity instanceof FilteredPostsActivity)) {
                if (nsfwTextView != null) {
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
                }

                if (typeTextView != null) {
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
                }

                if (flairTextView != null) {
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
            }
        }

        abstract void setItemViewBackgroundColor(boolean isReadPost);
        abstract void markPostRead(Post post, boolean changePostItemColor);
    }

    @UnstableApi
    public abstract class VideoAutoplayImpl implements ToroPlayer {
        View itemView;
        AspectRatioFrameLayout aspectRatioFrameLayout;
        GifImageView previewImageView;
        ImageView errorLoadingRedgifsImageView;
        PlayerView videoPlayer;
        ImageView muteButton;
        ImageView fullscreenButton;
        ImageView playPauseButton;
        @Nullable
        Container container;
        @Nullable
        ExoPlayerViewHelper helper;
        private Uri mediaUri;
        private float volume;
        public Call<String> fetchRedgifsOrStreamableVideoCall;
        private boolean isManuallyPaused;
        private Drawable playDrawable;
        private Drawable pauseDrawable;

        public VideoAutoplayImpl(View itemView, AspectRatioFrameLayout aspectRatioFrameLayout,
                                 GifImageView previewImageView, ImageView errorLoadingRedgifsImageView,
                                 PlayerView videoPlayer, ImageView muteButton, ImageView fullscreenButton,
                                 ImageView playPauseButton, DefaultTimeBar progressBar,
                                 Drawable playDrawable, Drawable pauseDrawable) {
            this.itemView = itemView;
            this.aspectRatioFrameLayout = aspectRatioFrameLayout;
            this.previewImageView = previewImageView;
            this.errorLoadingRedgifsImageView = errorLoadingRedgifsImageView;
            this.videoPlayer = videoPlayer;
            this.muteButton = muteButton;
            this.fullscreenButton = fullscreenButton;
            this.playPauseButton = playPauseButton;
            this.playDrawable = playDrawable;
            this.pauseDrawable = pauseDrawable;

            aspectRatioFrameLayout.setOnClickListener(null);

            muteButton.setOnClickListener(view -> {
                if (helper != null) {
                    if (helper.getVolume() != 0) {
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_24dp));
                        helper.setVolume(0f);
                        volume = 0f;
                        mFragment.videoAutoplayChangeMutingOption(true);
                    } else {
                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_24dp));
                        helper.setVolume(1f);
                        volume = 1f;
                        mFragment.videoAutoplayChangeMutingOption(false);
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                Post post = getPost();
                if (post != null) {
                    markPostRead(post, true);

                    if (helper != null) {
                        openMedia(post, helper.getLatestPlaybackInfo().getResumePosition());
                    } else {
                        openMedia(post, -1);
                    }
                }
            });

            playPauseButton.setOnClickListener(view -> {
                if (isPlaying()) {
                    pause();
                    isManuallyPaused = true;
                    savePlaybackInfo(getPlayerOrder(), getCurrentPlaybackInfo());
                } else {
                    isManuallyPaused = false;
                    play();
                }
            });

            progressBar.addListener(new TimeBar.OnScrubListener() {
                @Override
                public void onScrubStart(TimeBar timeBar, long position) {

                }

                @Override
                public void onScrubMove(TimeBar timeBar, long position) {

                }

                @Override
                public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
                    if (!canceled) {
                        savePlaybackInfo(getPlayerOrder(), getCurrentPlaybackInfo());
                    }
                }
            });

            previewImageView.setOnClickListener(view -> fullscreenButton.performClick());

            videoPlayer.setOnClickListener(view -> {
                if (mEasierToWatchInFullScreen && videoPlayer.isControllerFullyVisible()) {
                    fullscreenButton.performClick();
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

        private void savePlaybackInfo(int order, @Nullable PlaybackInfo playbackInfo) {
            if (container != null) container.savePlaybackInfo(order, playbackInfo);
        }

        void loadVideo(int position) {
            Post post = getPost();
            if (post.isRedgifs() && !post.isLoadRedgifsOrStreamableVideoSuccess()) {
                fetchRedgifsOrStreamableVideoCall =
                        mRedgifsRetrofit.create(RedgifsAPI.class).getRedgifsData(
                                APIUtils.getRedgifsOAuthHeader(mCurrentAccountSharedPreferences
                                        .getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "")),
                                post.getRedgifsId(), APIUtils.USER_AGENT);
                FetchRedgifsVideoLinks.fetchRedgifsVideoLinksInRecyclerViewAdapter(mExecutor, new Handler(),
                        fetchRedgifsOrStreamableVideoCall,
                        new FetchVideoLinkListener() {
                            @Override
                            public void onFetchRedgifsVideoLinkSuccess(String webm, String mp4) {
                                post.setVideoDownloadUrl(mp4);
                                post.setVideoUrl(mp4);
                                post.setLoadRedgifsOrStreamableVideoSuccess(true);
                                if (position == getAdapterPosition()) {
                                    bindVideoUri(Uri.parse(post.getVideoUrl()));
                                }
                            }

                            @Override
                            public void failed(@Nullable Integer messageRes) {
                                if (position == getAdapterPosition()) {
                                    loadFallbackDirectVideo();
                                }
                            }
                        });
            } else if(post.isStreamable() && !post.isLoadRedgifsOrStreamableVideoSuccess()) {
                fetchRedgifsOrStreamableVideoCall =
                        mStreamableApiProvider.get().getStreamableData(post.getStreamableShortCode());
                FetchStreamableVideo.fetchStreamableVideoInRecyclerViewAdapter(mExecutor, new Handler(),
                        fetchRedgifsOrStreamableVideoCall,
                        new FetchVideoLinkListener() {
                            @Override
                            public void onFetchStreamableVideoLinkSuccess(StreamableVideo streamableVideo) {
                                StreamableVideo.Media media = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile : streamableVideo.mp4;
                                post.setVideoDownloadUrl(media.url);
                                post.setVideoUrl(media.url);
                                post.setLoadRedgifsOrStreamableVideoSuccess(true);
                                if (position == getAdapterPosition()) {
                                    bindVideoUri(Uri.parse(post.getVideoUrl()));
                                }
                            }

                            @Override
                            public void failed(@Nullable Integer messageRes) {
                                if (position == getAdapterPosition()) {
                                    loadFallbackDirectVideo();
                                }
                            }
                        });
            } else {
                bindVideoUri(Uri.parse(post.getVideoUrl()));
            }
        }

        void loadFallbackDirectVideo() {
            Post post = getPost();
            if (post.getVideoFallBackDirectUrl() != null) {
                mediaUri = Uri.parse(post.getVideoFallBackDirectUrl());
                post.setVideoDownloadUrl(post.getVideoFallBackDirectUrl());
                post.setVideoUrl(post.getVideoFallBackDirectUrl());
                post.setLoadRedgifsOrStreamableVideoSuccess(true);
                if (container != null) {
                    container.onScrollStateChanged(RecyclerView.SCROLL_STATE_IDLE);
                }
            }
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

        @OptIn(markerClass = UnstableApi.class)
        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            if (this.container == null) {
                this.container = container;
            }
            if (mediaUri == null) {
                return;
            }
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.DefaultEventListener() {
                    @Override
                    public void onEvents(@NonNull Player player, @NonNull Player.Events events) {
                        if (events.containsAny(
                                Player.EVENT_PLAY_WHEN_READY_CHANGED,
                                Player.EVENT_PLAYBACK_STATE_CHANGED,
                                Player.EVENT_PLAYBACK_SUPPRESSION_REASON_CHANGED)) {
                            playPauseButton.setImageDrawable(Util.shouldShowPlayButton(player) ? playDrawable : pauseDrawable);
                        }
                    }

                    @Override
                    public void onTracksChanged(@NonNull Tracks tracks) {
                        ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.size(); i++) {
                                String mimeType = trackGroups.get(i).getTrackFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    if (mFragment.getMasterMutingOption() != null) {
                                        volume = mFragment.getMasterMutingOption() ? 0f : 1f;
                                    }
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_unmute_24dp));
                                    } else {
                                        muteButton.setImageDrawable(ContextCompat.getDrawable(mActivity, R.drawable.ic_mute_24dp));
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

                    @Override
                    public void onPlayerError(@NonNull PlaybackException error) {
                        Post post = getPost();
                        if (post.getVideoFallBackDirectUrl() == null || post.getVideoFallBackDirectUrl().equals(mediaUri.toString())) {
                            errorLoadingRedgifsImageView.setVisibility(View.VISIBLE);
                        } else {
                            loadFallbackDirectVideo();
                        }
                    }
                });
            }
            helper.initialize(container, playbackInfo);
        }

        @Override
        public void play() {
            if (helper != null && mediaUri != null) {
                if (!isPlaying() && isManuallyPaused) {
                    helper.play();
                    pause();
                    helper.setVolume(volume);
                } else {
                    helper.play();
                }
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
            isManuallyPaused = false;
            container = null;
        }

        @Override
        public boolean wantsToPlay() {
            return canPlayVideo && mediaUri != null && ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        abstract int getAdapterPosition();

        abstract Post getPost();

        abstract void markPostRead(Post post, boolean changePostItemColor);
    }

    public abstract class PostBaseViewHolder extends PostViewHolder {
        TextView subredditTextView;
        TextView userTextView;
        Post.Preview preview;

        PostBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         @Nullable CustomTextView typeTextView,
                         @Nullable ImageView archivedImageView,
                         @Nullable ImageView lockedImageView,
                         @Nullable ImageView crosspostImageView,
                         @Nullable CustomTextView nsfwTextView,
                         @Nullable CustomTextView spoilerTextView,
                         @Nullable CustomTextView flairTextView,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton) {
            super.setBaseView(
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
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            this.subredditTextView = subredditTextView;
            this.userTextView = userTextView;

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
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountButton.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            setItemViewBackgroundColor(false);

            if (mActivity.typeface != null) {
                subredditTextView.setTypeface(mActivity.typeface);
                userTextView.setTypeface(mActivity.typeface);
                postTimeTextView.setTypeface(mActivity.typeface);
                if (typeTextView != null) {
                    typeTextView.setTypeface(mActivity.typeface);
                }
                if (spoilerTextView != null) {
                    spoilerTextView.setTypeface(mActivity.typeface);
                }
                if (nsfwTextView != null) {
                    nsfwTextView.setTypeface(mActivity.typeface);
                }
                if (flairTextView != null) {
                    flairTextView.setTypeface(mActivity.typeface);
                }
                upvoteButton.setTypeface(mActivity.typeface);
                commentsCountButton.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                titleTextView.setTypeface(mActivity.titleTypeface);
            }

            subredditTextView.setTextColor(mSubredditColor);
            userTextView.setTextColor(mUsernameColor);
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            stickiedPostImageView.setColorFilter(mStickiedPostIconTint, PorterDuff.Mode.SRC_IN);

            if (typeTextView != null) {
                typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
                typeTextView.setBorderColor(mPostTypeBackgroundColor);
                typeTextView.setTextColor(mPostTypeTextColor);
            }

            if (spoilerTextView != null) {
                spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
                spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
                spoilerTextView.setTextColor(mSpoilerTextColor);
            }

            if (nsfwTextView != null) {
                nsfwTextView.setBackgroundColor(mNSFWBackgroundColor);
                nsfwTextView.setBorderColor(mNSFWBackgroundColor);
                nsfwTextView.setTextColor(mNSFWTextColor);
            }

            if (flairTextView != null) {
                flairTextView.setBackgroundColor(mFlairBackgroundColor);
                flairTextView.setBorderColor(mFlairBackgroundColor);
                flairTextView.setTextColor(mFlairTextColor);
            }

            if (archivedImageView != null) {
                archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            }

            if (lockedImageView != null) {
                lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            }

            if (crosspostImageView != null) {
                crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            }

            upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            commentsCountButton.setTextColor(mPostIconAndInfoColor);
            commentsCountButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            saveButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            shareButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

            itemView.setOnLongClickListener(v -> {
                Post post = getItem(getBindingAdapterPosition());
                if (post == null) {
                    return false;
                }

                PostOptionsBottomSheetFragment postOptionsBottomSheetFragment;
                if (post.getPostType() == Post.GALLERY_TYPE && this instanceof PostBaseGalleryTypeViewHolder) {
                    postOptionsBottomSheetFragment = PostOptionsBottomSheetFragment.newInstance(post,
                            ((LinearLayoutManagerBugFixed) ((PostBaseGalleryTypeViewHolder) this).galleryRecyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                } else {
                    postOptionsBottomSheetFragment = PostOptionsBottomSheetFragment.newInstance(post);
                }
                postOptionsBottomSheetFragment.show(mActivity.getSupportFragmentManager(), postOptionsBottomSheetFragment.getTag());
                return true;
            });
        }

        @Override
        void markPostRead(Post post, boolean changePostItemColor) {
            if (!mAccountName.equals(Account.ANONYMOUS_ACCOUNT) && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead();
                if (changePostItemColor) {
                    setItemViewBackgroundColor(true);
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

    @UnstableApi
    abstract class PostBaseVideoAutoplayViewHolder extends PostBaseViewHolder implements ToroPlayer {
        VideoAutoplayImpl toroPlayer;

        @OptIn(markerClass = UnstableApi.class)
        PostBaseVideoAutoplayViewHolder(View rootView,
                                        AspectRatioGifImageView iconGifImageView,
                                        TextView subredditTextView,
                                        TextView userTextView,
                                        ImageView stickiedPostImageView,
                                        TextView postTimeTextView,
                                        TextView titleTextView,
                                        @Nullable CustomTextView typeTextView,
                                        @Nullable ImageView crosspostImageView,
                                        @Nullable ImageView archivedImageView,
                                        @Nullable ImageView lockedImageView,
                                        @Nullable CustomTextView nsfwTextView,
                                        @Nullable CustomTextView spoilerTextView,
                                        @Nullable CustomTextView flairTextView,
                                        AspectRatioFrameLayout aspectRatioFrameLayout,
                                        GifImageView previewImageView,
                                        ImageView errorLoadingRedgifsImageView,
                                        PlayerView videoPlayer,
                                        ImageView muteButton,
                                        ImageView fullscreenButton,
                                        ImageView playPauseButton,
                                        DefaultTimeBar progressBar,
                                        ConstraintLayout bottomConstraintLayout,
                                        MaterialButton upvoteButton,
                                        TextView scoreTextView,
                                        MaterialButton downvoteButton,
                                        MaterialButton commentsCountButton,
                                        MaterialButton saveButton,
                                        MaterialButton shareButton) {
            super(rootView);
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
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            toroPlayer = new VideoAutoplayImpl(rootView, aspectRatioFrameLayout, previewImageView,
                    errorLoadingRedgifsImageView, videoPlayer, muteButton, fullscreenButton, playPauseButton,
                    progressBar,
                    AppCompatResources.getDrawable(mActivity, R.drawable.ic_play_arrow_24dp),
                    AppCompatResources.getDrawable(mActivity, R.drawable.ic_pause_24dp)) {
                @Override
                public int getPlayerOrder() {
                    return getBindingAdapterPosition();
                }

                @Override
                int getAdapterPosition() {
                    return getBindingAdapterPosition();
                }

                @Override
                Post getPost() {
                    return post;
                }

                @Override
                void markPostRead(Post post, boolean changePostItemColor) {
                    PostBaseVideoAutoplayViewHolder.this.markPostRead(post, changePostItemColor);
                }
            };
        }

        @NonNull
        @Override
        public View getPlayerView() {
            return toroPlayer.getPlayerView();
        }

        @NonNull
        @Override
        public PlaybackInfo getCurrentPlaybackInfo() {
            return toroPlayer.getCurrentPlaybackInfo();
        }

        @Override
        public void initialize(@NonNull Container container, @NonNull PlaybackInfo playbackInfo) {
            toroPlayer.initialize(container, playbackInfo);
        }

        @Override
        public void play() {
            toroPlayer.play();
        }

        @Override
        public void pause() {
            toroPlayer.pause();
        }

        @Override
        public boolean isPlaying() {
            return toroPlayer.isPlaying();
        }

        @Override
        public void release() {
            toroPlayer.release();
        }

        @Override
        public boolean wantsToPlay() {
            return toroPlayer.wantsToPlay();
        }

        @Override
        public int getPlayerOrder() {
            return toroPlayer.getPlayerOrder();
        }
    }

    @UnstableApi
    class PostVideoAutoplayViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostVideoAutoplayViewHolder(ItemPostVideoTypeAutoplayBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostVideoTypeAutoplay,
                    binding.subredditNameTextViewItemPostVideoTypeAutoplay,
                    binding.userTextViewItemPostVideoTypeAutoplay,
                    binding.stickiedPostImageViewItemPostVideoTypeAutoplay,
                    binding.postTimeTextViewItemPostVideoTypeAutoplay,
                    binding.titleTextViewItemPostVideoTypeAutoplay,
                    binding.typeTextViewItemPostVideoTypeAutoplay,
                    binding.crosspostImageViewItemPostVideoTypeAutoplay,
                    binding.archivedImageViewItemPostVideoTypeAutoplay,
                    binding.lockedImageViewItemPostVideoTypeAutoplay,
                    binding.nsfwTextViewItemPostVideoTypeAutoplay,
                    binding.spoilerCustomTextViewItemPostVideoTypeAutoplay,
                    binding.flairCustomTextViewItemPostVideoTypeAutoplay,
                    binding.aspectRatioFrameLayoutItemPostVideoTypeAutoplay,
                    binding.previewImageViewItemPostVideoTypeAutoplay,
                    binding.errorLoadingVideoImageViewItemPostVideoTypeAutoplay,
                    binding.playerViewItemPostVideoTypeAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostVideoTypeAutoplay,
                    binding.upvoteButtonItemPostVideoTypeAutoplay,
                    binding.scoreTextViewItemPostVideoTypeAutoplay,
                    binding.downvoteButtonItemPostVideoTypeAutoplay,
                    binding.commentsCountButtonItemPostVideoTypeAutoplay,
                    binding.saveButtonItemPostVideoTypeAutoplay,
                    binding.shareButtonItemPostVideoTypeAutoplay);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor));
        }
    }

    @UnstableApi
    class PostVideoAutoplayLegacyControllerViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostVideoAutoplayLegacyControllerViewHolder(ItemPostVideoTypeAutoplayLegacyControllerBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostVideoTypeAutoplay,
                    binding.subredditNameTextViewItemPostVideoTypeAutoplay,
                    binding.userTextViewItemPostVideoTypeAutoplay,
                    binding.stickiedPostImageViewItemPostVideoTypeAutoplay,
                    binding.postTimeTextViewItemPostVideoTypeAutoplay,
                    binding.titleTextViewItemPostVideoTypeAutoplay,
                    binding.typeTextViewItemPostVideoTypeAutoplay,
                    binding.crosspostImageViewItemPostVideoTypeAutoplay,
                    binding.archivedImageViewItemPostVideoTypeAutoplay,
                    binding.lockedImageViewItemPostVideoTypeAutoplay,
                    binding.nsfwTextViewItemPostVideoTypeAutoplay,
                    binding.spoilerCustomTextViewItemPostVideoTypeAutoplay,
                    binding.flairCustomTextViewItemPostVideoTypeAutoplay,
                    binding.aspectRatioFrameLayoutItemPostVideoTypeAutoplay,
                    binding.previewImageViewItemPostVideoTypeAutoplay,
                    binding.errorLoadingVideoImageViewItemPostVideoTypeAutoplay,
                    binding.playerViewItemPostVideoTypeAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostVideoTypeAutoplay,
                    binding.upvoteButtonItemPostVideoTypeAutoplay,
                    binding.scoreTextViewItemPostVideoTypeAutoplay,
                    binding.downvoteButtonItemPostVideoTypeAutoplay,
                    binding.commentsCountButtonItemPostVideoTypeAutoplay,
                    binding.saveButtonItemPostVideoTypeAutoplay,
                    binding.shareButtonItemPostVideoTypeAutoplay);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor));
        }
    }

    class PostWithPreviewTypeViewHolder extends PostBaseViewHolder {
        TextView linkTextView;
        ImageView imageViewNoPreviewGallery;
        ProgressBar progressBar;
        ImageView videoOrGifIndicator;
        TextView loadImageErrorTextView;
        @Nullable
        FrameLayout imageWrapperFrameLayout;
        AspectRatioGifImageView imageView;
        RequestListener<Drawable> glideRequestListener;

        PostWithPreviewTypeViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        PostWithPreviewTypeViewHolder(@NonNull ItemPostWithPreviewBinding binding) {
            super(binding.getRoot());
            setBaseView(
                    binding.iconGifImageViewItemPostWithPreview,
                    binding.subredditNameTextViewItemPostWithPreview,
                    binding.userTextViewItemPostWithPreview,
                    binding.stickiedPostImageViewItemPostWithPreview,
                    binding.postTimeTextViewItemPostWithPreview,
                    binding.titleTextViewItemPostWithPreview,
                    binding.typeTextViewItemPostWithPreview,
                    binding.archivedImageViewItemPostWithPreview,
                    binding.lockedImageViewItemPostWithPreview,
                    binding.crosspostImageViewItemPostWithPreview,
                    binding.nsfwTextViewItemPostWithPreview,
                    binding.spoilerCustomTextViewItemPostWithPreview,
                    binding.flairCustomTextViewItemPostWithPreview,
                    binding.bottomConstraintLayoutItemPostWithPreview,
                    binding.upvoteButtonItemPostWithPreview,
                    binding.scoreTextViewItemPostWithPreview,
                    binding.downvoteButtonItemPostWithPreview,
                    binding.commentsCountButtonItemPostWithPreview,
                    binding.saveButtonItemPostWithPreview,
                    binding.shareButtonItemPostWithPreview,
                    binding.linkTextViewItemPostWithPreview,
                    binding.imageViewNoPreviewGalleryItemPostWithPreview,
                    binding.progressBarItemPostWithPreview,
                    binding.videoOrGifIndicatorImageViewItemPostWithPreview,
                    binding.loadImageErrorTextViewItemPostWithPreview,
                    binding.imageWrapperRelativeLayoutItemPostWithPreview,
                    binding.imageViewItemPostWithPreview);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         @Nullable CustomTextView typeTextView,
                         @Nullable ImageView archivedImageView,
                         @Nullable ImageView lockedImageView,
                         @Nullable ImageView crosspostImageView,
                         @Nullable CustomTextView nsfwTextView,
                         @Nullable CustomTextView spoilerTextView,
                         @Nullable CustomTextView flairTextView,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton,
                         TextView linkTextView,
                         ImageView imageViewNoPreviewGallery,
                         ProgressBar progressBar,
                         ImageView videoOrGifIndicator,
                         TextView loadImageErrorTextView,
                         @Nullable FrameLayout imageWrapperFrameLayout,
                         AspectRatioGifImageView imageView) {
            super.setBaseView(
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
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            this.linkTextView = linkTextView;
            this.imageViewNoPreviewGallery = imageViewNoPreviewGallery;
            this.progressBar = progressBar;
            this.videoOrGifIndicator = videoOrGifIndicator;
            this.loadImageErrorTextView = loadImageErrorTextView;
            this.imageWrapperFrameLayout = imageWrapperFrameLayout;
            this.imageView = imageView;

            if (mActivity.typeface != null) {
                linkTextView.setTypeface(mActivity.typeface);
                loadImageErrorTextView.setTypeface(mActivity.typeface);
            }
            linkTextView.setTextColor(mSecondaryTextColor);
            imageViewNoPreviewGallery.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            imageViewNoPreviewGallery.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            videoOrGifIndicator.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            videoOrGifIndicator.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            loadImageErrorTextView.setTextColor(mPrimaryTextColor);

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

            imageView.setOnLongClickListener(view -> itemView.performLongClick());

            loadImageErrorTextView.setOnClickListener(view -> {
                progressBar.setVisibility(View.VISIBLE);
                loadImageErrorTextView.setVisibility(View.GONE);
                loadImage(this);
            });

            imageViewNoPreviewGallery.setOnClickListener(view -> {
                imageView.performClick();
            });

            imageViewNoPreviewGallery.setOnLongClickListener(view -> itemView.performLongClick());

            glideRequestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    loadImageErrorTextView.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    loadImageErrorTextView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            };
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor));
        }
    }

    public abstract class PostBaseGalleryTypeViewHolder extends PostBaseViewHolder {
        FrameLayout frameLayout;
        RecyclerView galleryRecyclerView;
        CustomTextView imageIndexTextView;
        ImageView noPreviewImageView;

        PostGalleryTypeImageRecyclerViewAdapter adapter;
        private boolean swipeLocked;

        PostBaseGalleryTypeViewHolder(View rootView,
                                      AspectRatioGifImageView iconGifImageView,
                                      TextView subredditTextView,
                                      TextView userTextView,
                                      ImageView stickiedPostImageView,
                                      TextView postTimeTextView,
                                      TextView titleTextView,
                                      @Nullable CustomTextView typeTextView,
                                      @Nullable ImageView archivedImageView,
                                      @Nullable ImageView lockedImageView,
                                      @Nullable ImageView crosspostImageView,
                                      @Nullable CustomTextView nsfwTextView,
                                      @Nullable CustomTextView spoilerTextView,
                                      @Nullable CustomTextView flairTextView,
                                      FrameLayout frameLayout,
                                      RecyclerView galleryRecyclerView,
                                      CustomTextView imageIndexTextView,
                                      ImageView noPreviewImageView,
                                      ConstraintLayout bottomConstraintLayout,
                                      MaterialButton upvoteButton,
                                      TextView scoreTextView,
                                      MaterialButton downvoteButton,
                                      MaterialButton commentsCountButton,
                                      MaterialButton saveButton,
                                      MaterialButton shareButton) {
            super(rootView);
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
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            this.frameLayout = frameLayout;
            this.galleryRecyclerView = galleryRecyclerView;
            this.imageIndexTextView = imageIndexTextView;
            this.noPreviewImageView = noPreviewImageView;

            imageIndexTextView.setTextColor(mMediaIndicatorIconTint);
            imageIndexTextView.setBackgroundColor(mMediaIndicatorBackgroundColor);
            imageIndexTextView.setBorderColor(mMediaIndicatorBackgroundColor);
            if (mActivity.typeface != null) {
                imageIndexTextView.setTypeface(mActivity.typeface);
            }

            noPreviewImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);

            adapter = new PostGalleryTypeImageRecyclerViewAdapter(mGlide, mActivity.typeface,
                    mSaveMemoryCenterInsideDownsampleStrategy, mColorAccent, mPrimaryTextColor, mScale,
                    galleryImage -> itemView.performLongClick());
            galleryRecyclerView.setAdapter(adapter);
            galleryRecyclerView.setOnTouchListener((v, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    if (mActivity.mSliderPanel != null) {
                        mActivity.mSliderPanel.requestDisallowInterceptTouchEvent(false);
                    }

                    if (mActivity.mViewPager2 != null) {
                        mActivity.mViewPager2.setUserInputEnabled(true);
                    }
                    mActivity.unlockSwipeRightToGoBack();
                    swipeLocked = false;
                } else {
                    if (mActivity.mSliderPanel != null) {
                        mActivity.mSliderPanel.requestDisallowInterceptTouchEvent(true);
                    }
                    if (mActivity.mViewPager2 != null) {
                        mActivity.mViewPager2.setUserInputEnabled(false);
                    }
                    mActivity.lockSwipeRightToGoBack();
                    swipeLocked = true;
                }

                return false;
            });
            new PagerSnapHelper().attachToRecyclerView(galleryRecyclerView);
            galleryRecyclerView.setRecycledViewPool(mGalleryRecycledViewPool);
            LinearLayoutManagerBugFixed layoutManager = new LinearLayoutManagerBugFixed(mActivity, RecyclerView.HORIZONTAL, false);
            galleryRecyclerView.setLayoutManager(layoutManager);
            galleryRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    imageIndexTextView.setText(mActivity.getString(R.string.image_index_in_gallery, layoutManager.findFirstVisibleItemPosition() + 1, post.getGallery().size()));
                }
            });
            galleryRecyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                private float downX;
                private float downY;
                private boolean dragged;
                private final int minTouchSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop();

                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    int action = e.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downX = e.getRawX();
                            downY = e.getRawY();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(Math.abs(e.getRawX() - downX) > minTouchSlop || Math.abs(e.getRawY() - downY) > minTouchSlop) {
                                dragged = true;
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!dragged) {
                                int position = getBindingAdapterPosition();
                                if (position >= 0) {
                                    if (post != null) {
                                        markPostRead(post, true);
                                        openMedia(post, layoutManager.findFirstVisibleItemPosition());
                                    }
                                }
                            }

                            downX = 0;
                            downY = 0;
                            dragged = false;
                    }
                    return false;
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                }
            });

            noPreviewImageView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                if (post != null) {
                    markPostRead(post, true);
                    openMedia(post, 0);
                }
            });

            noPreviewImageView.setOnLongClickListener(view -> itemView.performLongClick());
        }

        public boolean isSwipeLocked() {
            return swipeLocked;
        }
    }

    public class PostGalleryTypeViewHolder extends PostBaseGalleryTypeViewHolder {

        PostGalleryTypeViewHolder(ItemPostGalleryTypeBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostGalleryType,
                    binding.subredditNameTextViewItemPostGalleryType,
                    binding.userTextViewItemPostGalleryType,
                    binding.stickiedPostImageViewItemPostGalleryType,
                    binding.postTimeTextViewItemPostGalleryType,
                    binding.titleTextViewItemPostGalleryType,
                    binding.typeTextViewItemPostGalleryType,
                    binding.archivedImageViewItemPostGalleryType,
                    binding.lockedImageViewItemPostGalleryType,
                    binding.crosspostImageViewItemPostGalleryType,
                    binding.nsfwTextViewItemPostGalleryType,
                    binding.spoilerTextViewItemPostGalleryType,
                    binding.flairTextViewItemPostGalleryType,
                    binding.galleryFrameLayoutItemPostGalleryType,
                    binding.galleryRecyclerViewItemPostGalleryType,
                    binding.imageIndexTextViewItemPostGalleryType,
                    binding.noPreviewImageViewItemPostGalleryType,
                    binding.bottomConstraintLayoutItemPostGalleryType,
                    binding.upvoteButtonItemPostGalleryType,
                    binding.scoreTextViewItemPostGalleryType,
                    binding.downvoteButtonItemPostGalleryType,
                    binding.commentsCountButtonItemPostGalleryType,
                    binding.saveButtonItemPostGalleryType,
                    binding.shareButtonItemPostGalleryType);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor));
        }
    }

    class PostTextTypeViewHolder extends PostBaseViewHolder {
        TextView contentTextView;

        PostTextTypeViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        PostTextTypeViewHolder(@NonNull ItemPostTextBinding binding) {
            super(binding.getRoot());
            setBaseView(
                    binding.iconGifImageViewItemPostTextType,
                    binding.subredditNameTextViewItemPostTextType,
                    binding.userTextViewItemPostTextType,
                    binding.stickiedPostImageViewItemPostTextType,
                    binding.postTimeTextViewItemPostTextType,
                    binding.titleTextViewItemPostTextType,
                    binding.typeTextViewItemPostTextType,
                    binding.archivedImageViewItemPostTextType,
                    binding.lockedImageViewItemPostTextType,
                    binding.crosspostImageViewItemPostTextType,
                    binding.nsfwTextViewItemPostTextType,
                    binding.spoilerCustomTextViewItemPostTextType,
                    binding.flairCustomTextViewItemPostTextType,
                    binding.bottomConstraintLayoutItemPostTextType,
                    binding.upvoteButtonItemPostTextType,
                    binding.scoreTextViewItemPostTextType,
                    binding.downvoteButtonItemPostTextType,
                    binding.commentsCountButtonItemPostTextType,
                    binding.saveButtonItemPostTextType,
                    binding.shareButtonItemPostTextType,
                    binding.contentTextViewItemPostTextType);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         @Nullable CustomTextView typeTextView,
                         @Nullable ImageView archivedImageView,
                         @Nullable ImageView lockedImageView,
                         @Nullable ImageView crosspostImageView,
                         @Nullable CustomTextView nsfwTextView,
                         @Nullable CustomTextView spoilerTextView,
                         @Nullable CustomTextView flairTextView,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton,
                         TextView contentTextView) {
            super.setBaseView(
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
                    bottomConstraintLayout,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            this.contentTextView = contentTextView;

            if (mActivity.contentTypeface != null) {
                contentTextView.setTypeface(mActivity.titleTypeface);
            }
            contentTextView.setTextColor(mPostContentColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor));
        }
    }

    public class PostCompactBaseViewHolder extends PostViewHolder {
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
        TextView linkTextView;
        RelativeLayout relativeLayout;
        ProgressBar progressBar;
        ImageView imageView;
        ImageView playButtonImageView;
        FrameLayout noPreviewPostImageFrameLayout;
        ImageView noPreviewPostImageView;
        Barrier imageBarrier;
        ConstraintLayout bottomConstraintLayout;
        View divider;
        RequestListener<Drawable> requestListener;

        PostCompactBaseViewHolder(View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView nameTextView,
                         ImageView stickiedPostImageView,
                         TextView postTimeTextView,
                         ConstraintLayout titleAndImageConstraintLayout,
                         TextView titleTextView,
                         CustomTextView typeTextView,
                         ImageView archivedImageView,
                         ImageView lockedImageView,
                         ImageView crosspostImageView,
                         CustomTextView nsfwTextView,
                         CustomTextView spoilerTextView,
                         CustomTextView flairTextView,
                         TextView linkTextView,
                         RelativeLayout relativeLayout,
                         ProgressBar progressBar,
                         ImageView imageView,
                         ImageView playButtonImageView,
                         FrameLayout noPreviewLinkImageFrameLayout,
                         ImageView noPreviewLinkImageView,
                         Barrier imageBarrier,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton,
                         View divider) {
            super.setBaseView(iconGifImageView,
                    nameTextView,
                    stickiedPostImageView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    nsfwTextView,
                    flairTextView,
                    upvoteButton,
                    scoreTextView,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

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
            this.linkTextView = linkTextView;
            this.relativeLayout = relativeLayout;
            this.progressBar = progressBar;
            this.imageView = imageView;
            this.playButtonImageView = playButtonImageView;
            this.noPreviewPostImageFrameLayout = noPreviewLinkImageFrameLayout;
            this.noPreviewPostImageView = noPreviewLinkImageView;
            this.imageBarrier = imageBarrier;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.divider = divider;

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
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountButton.getId(), 0);
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
                linkTextView.setTypeface(mActivity.typeface);
                upvoteButton.setTypeface(mActivity.typeface);
                commentsCountButton.setTypeface(mActivity.typeface);
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
            archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            linkTextView.setTextColor(mSecondaryTextColor);
            playButtonImageView.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            playButtonImageView.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewLinkImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            commentsCountButton.setTextColor(mPostIconAndInfoColor);
            commentsCountButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            saveButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            shareButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            divider.setBackgroundColor(mDividerColor);

            imageView.setClipToOutline(true);
            noPreviewLinkImageFrameLayout.setClipToOutline(true);

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

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }

        @Override
        void markPostRead(Post post, boolean changePostItemColor) {
            if (!mAccountName.equals(Account.ANONYMOUS_ACCOUNT) && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead();
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
        PostCompactLeftThumbnailViewHolder(@NonNull ItemPostCompactBinding binding) {
            super(binding.getRoot());

            setBaseView(binding.iconGifImageViewItemPostCompact,
                    binding.nameTextViewItemPostCompact,
                    binding.stickiedPostImageViewItemPostCompact,
                    binding.postTimeTextViewItemPostCompact,
                    binding.titleAndImageConstraintLayout,
                    binding.titleTextViewItemPostCompact,
                    binding.typeTextViewItemPostCompact,
                    binding.archivedImageViewItemPostCompact,
                    binding.lockedImageViewItemPostCompact,
                    binding.crosspostImageViewItemPostCompact,
                    binding.nsfwTextViewItemPostCompact,
                    binding.spoilerCustomTextViewItemPostCompact,
                    binding.flairCustomTextViewItemPostCompact,
                    binding.linkTextViewItemPostCompact,
                    binding.imageViewWrapperItemPostCompact,
                    binding.progressBarItemPostCompact,
                    binding.imageViewItemPostCompact,
                    binding.playButtonImageViewItemPostCompact,
                    binding.frameLayoutImageViewNoPreviewLinkItemPostCompact,
                    binding.imageViewNoPreviewLinkItemPostCompact,
                    binding.barrier2,
                    binding.bottomConstraintLayoutItemPostCompact,
                    binding.upvoteButtonItemPostCompact,
                    binding.scoreTextViewItemPostCompact,
                    binding.downvoteButtonItemPostCompact,
                    binding.commentsCountButtonItemPostCompact,
                    binding.saveButtonItemPostCompact,
                    binding.shareButtonItemPostCompact,
                    binding.dividerItemPostCompact);
        }
    }

    class PostCompactRightThumbnailViewHolder extends PostCompactBaseViewHolder {
        PostCompactRightThumbnailViewHolder(@NonNull ItemPostCompactRightThumbnailBinding binding) {
            super(binding.getRoot());

            setBaseView(binding.iconGifImageViewItemPostCompactRightThumbnail,
                    binding.nameTextViewItemPostCompactRightThumbnail,
                    binding.stickiedPostImageViewItemPostCompactRightThumbnail,
                    binding.postTimeTextViewItemPostCompactRightThumbnail,
                    binding.titleAndImageConstraintLayout,
                    binding.titleTextViewItemPostCompactRightThumbnail,
                    binding.typeTextViewItemPostCompactRightThumbnail,
                    binding.archivedImageViewItemPostCompactRightThumbnail,
                    binding.lockedImageViewItemPostCompactRightThumbnail,
                    binding.crosspostImageViewItemPostCompactRightThumbnail,
                    binding.nsfwTextViewItemPostCompactRightThumbnail,
                    binding.spoilerCustomTextViewItemPostCompactRightThumbnail,
                    binding.flairCustomTextViewItemPostCompactRightThumbnail,
                    binding.linkTextViewItemPostCompactRightThumbnail,
                    binding.imageViewWrapperItemPostCompactRightThumbnail,
                    binding.progressBarItemPostCompactRightThumbnail,
                    binding.imageViewItemPostCompactRightThumbnail,
                    binding.playButtonImageViewItemPostCompactRightThumbnail,
                    binding.frameLayoutImageViewNoPreviewLinkItemPostCompactRightThumbnail,
                    binding.imageViewNoPreviewLinkItemPostCompactRightThumbnail,
                    binding.barrier2,
                    binding.bottomConstraintLayoutItemPostCompactRightThumbnail,
                    binding.upvoteButtonItemPostCompactRightThumbnail,
                    binding.scoreTextViewItemPostCompactRightThumbnail,
                    binding.downvoteButtonItemPostCompactRightThumbnail,
                    binding.commentsCountButtonItemPostCompactRightThumbnail,
                    binding.saveButtonItemPostCompactRightThumbnail,
                    binding.shareButtonItemPostCompactRightThumbnail,
                    binding.dividerItemPostCompactRightThumbnail);
        }
    }

    class PostGalleryViewHolder extends RecyclerView.ViewHolder {
        ItemPostGalleryBinding binding;
        RequestListener<Drawable> requestListener;
        Post post;
        Post.Preview preview;

        int currentPosition;

        public PostGalleryViewHolder(@NonNull ItemPostGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (mActivity.typeface != null) {
                binding.loadImageErrorTextViewItemGallery.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                binding.titleTextViewItemPostGallery.setTypeface(mActivity.titleTypeface);
            }
            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            binding.titleTextViewItemPostGallery.setTextColor(mPostTitleColor);
            binding.progressBarItemPostGallery.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            binding.imageViewNoPreviewItemPostGallery.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            binding.imageViewNoPreviewItemPostGallery.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);
            binding.videoOrGifIndicatorImageViewItemPostGallery.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            binding.videoOrGifIndicatorImageViewItemPostGallery.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            binding.loadImageErrorTextViewItemGallery.setTextColor(mPrimaryTextColor);

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

            binding.loadImageErrorTextViewItemGallery.setOnClickListener(view -> {
                binding.progressBarItemPostGallery.setVisibility(View.VISIBLE);
                binding.loadImageErrorTextViewItemGallery.setVisibility(View.GONE);
                loadImage(this);
            });

            binding.imageViewNoPreviewItemPostGallery.setOnClickListener(view -> {
                itemView.performClick();
            });

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    binding.progressBarItemPostGallery.setVisibility(View.GONE);
                    binding.loadImageErrorTextViewItemGallery.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    binding.loadImageErrorTextViewItemGallery.setVisibility(View.GONE);
                    binding.progressBarItemPostGallery.setVisibility(View.GONE);
                    return false;
                }
            };
        }

        void markPostRead(Post post, boolean changePostItemColor) {
            if (!mAccountName.equals(Account.ANONYMOUS_ACCOUNT) && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead();
                if (changePostItemColor) {
                    itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                    binding.titleTextViewItemPostGallery.setTextColor(mReadPostTitleColor);
                }
                if (mActivity != null && mActivity instanceof MarkPostAsReadInterface) {
                    ((MarkPostAsReadInterface) mActivity).markPostAsRead(post);
                    mFragment.markPostAsRead(post);
                }
            }
        }
    }

    class PostGalleryBaseGalleryTypeViewHolder extends RecyclerView.ViewHolder {

        FrameLayout frameLayout;
        RecyclerView recyclerView;
        CustomTextView imageIndexTextView;
        ImageView noPreviewImageView;

        PostGalleryTypeImageRecyclerViewAdapter adapter;
        private final LinearLayoutManagerBugFixed layoutManager;

        Post post;
        Post.Preview preview;

        int currentPosition;

        public PostGalleryBaseGalleryTypeViewHolder(@NonNull View itemView,
                                                    FrameLayout frameLayout,
                                                    RecyclerView recyclerView,
                                                    CustomTextView imageIndexTextView,
                                                    ImageView noPreviewImageView) {
            super(itemView);

            this.frameLayout = frameLayout;
            this.recyclerView = recyclerView;
            this.imageIndexTextView = imageIndexTextView;
            this.noPreviewImageView = noPreviewImageView;

            if (mActivity.typeface != null) {
                imageIndexTextView.setTypeface(mActivity.typeface);
            }

            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            noPreviewImageView.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            noPreviewImageView.setColorFilter(mNoPreviewPostTypeIconTint, android.graphics.PorterDuff.Mode.SRC_IN);

            imageIndexTextView.setTextColor(mMediaIndicatorIconTint);
            imageIndexTextView.setBackgroundColor(mMediaIndicatorBackgroundColor);
            imageIndexTextView.setBorderColor(mMediaIndicatorBackgroundColor);

            adapter = new PostGalleryTypeImageRecyclerViewAdapter(mGlide, mActivity.typeface,
                    mSaveMemoryCenterInsideDownsampleStrategy, mColorAccent, mPrimaryTextColor, mScale,
                    galleryImage -> {});
            recyclerView.setOnTouchListener((v, motionEvent) -> {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP || motionEvent.getActionMasked() == MotionEvent.ACTION_CANCEL) {
                    if (mActivity.mSliderPanel != null) {
                        mActivity.mSliderPanel.requestDisallowInterceptTouchEvent(false);
                    }
                    if (mActivity.mViewPager2 != null) {
                        mActivity.mViewPager2.setUserInputEnabled(true);
                    }
                    mActivity.unlockSwipeRightToGoBack();
                } else {
                    if (mActivity.mSliderPanel != null) {
                        mActivity.mSliderPanel.requestDisallowInterceptTouchEvent(true);
                    }
                    if (mActivity.mViewPager2 != null) {
                        mActivity.mViewPager2.setUserInputEnabled(false);
                    }
                    mActivity.lockSwipeRightToGoBack();
                }

                return false;
            });
            recyclerView.setAdapter(adapter);
            new PagerSnapHelper().attachToRecyclerView(recyclerView);
            recyclerView.setRecycledViewPool(mGalleryRecycledViewPool);
            layoutManager = new LinearLayoutManagerBugFixed(mActivity, RecyclerView.HORIZONTAL, false);
            recyclerView.setLayoutManager(layoutManager);
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    imageIndexTextView.setText(mActivity.getString(R.string.image_index_in_gallery, layoutManager.findFirstVisibleItemPosition() + 1, post.getGallery().size()));
                }
            });
            recyclerView.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
                private float downX;
                private float downY;
                private boolean dragged;
                private long downTime;
                private final int minTouchSlop = ViewConfiguration.get(mActivity).getScaledTouchSlop();
                private final int longClickThreshold = ViewConfiguration.getLongPressTimeout();

                @Override
                public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
                    int action = e.getAction();
                    switch (action) {
                        case MotionEvent.ACTION_DOWN:
                            downX = e.getRawX();
                            downY = e.getRawY();
                            downTime = System.currentTimeMillis();
                            break;
                        case MotionEvent.ACTION_MOVE:
                            if(Math.abs(e.getRawX() - downX) > minTouchSlop || Math.abs(e.getRawY() - downY) > minTouchSlop) {
                                dragged = true;
                            }
                            if (!dragged) {
                                if (System.currentTimeMillis() - downTime >= longClickThreshold) {
                                    onLongClick();
                                }
                            }
                            break;
                        case MotionEvent.ACTION_UP:
                            if (!dragged) {
                                if (System.currentTimeMillis() - downTime < longClickThreshold) {
                                    onClick();
                                }
                            }
                        case MotionEvent.ACTION_CANCEL:
                            downX = 0;
                            downY = 0;
                            dragged = false;

                    }
                    return false;
                }

                @Override
                public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

                }

                @Override
                public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

                }
            });

            noPreviewImageView.setOnClickListener(view -> {
                onClick();
            });

            noPreviewImageView.setOnLongClickListener(view -> onLongClick());
        }

        void onClick() {
            int position = getBindingAdapterPosition();
            if (position >= 0 && canStartActivity) {
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);

                    if (post.getPostType() == Post.TEXT_TYPE || !mSharedPreferences.getBoolean(SharedPreferencesUtils.CLICK_TO_SHOW_MEDIA_IN_GALLERY_LAYOUT, false)) {
                        openViewPostDetailActivity(post, getBindingAdapterPosition());
                    } else {
                        openMedia(post, layoutManager.findFirstVisibleItemPosition());
                    }
                }
            }
        }

        boolean onLongClick() {
            int position = getBindingAdapterPosition();
            if (position >= 0 && canStartActivity) {
                Post post = getItem(position);
                if (post != null) {
                    markPostRead(post, true);

                    if (post.getPostType() == Post.TEXT_TYPE || mSharedPreferences.getBoolean(SharedPreferencesUtils.CLICK_TO_SHOW_MEDIA_IN_GALLERY_LAYOUT, false)) {
                        openViewPostDetailActivity(post, getBindingAdapterPosition());
                    } else {
                        openMedia(post, layoutManager.findFirstVisibleItemPosition());
                    }
                }
            }

            return true;
        }

        void markPostRead(Post post, boolean changePostItemColor) {
            if (!mAccountName.equals(Account.ANONYMOUS_ACCOUNT) && !post.isRead() && mMarkPostsAsRead) {
                post.markAsRead();
                if (changePostItemColor) {
                    itemView.setBackgroundTintList(ColorStateList.valueOf(mReadPostCardViewBackgroundColor));
                }
                if (mActivity != null && mActivity instanceof MarkPostAsReadInterface) {
                    ((MarkPostAsReadInterface) mActivity).markPostAsRead(post);
                    mFragment.markPostAsRead(post);
                }
            }
        }
    }

    class PostGalleryGalleryTypeViewHolder extends PostGalleryBaseGalleryTypeViewHolder {

        public PostGalleryGalleryTypeViewHolder(@NonNull ItemPostGalleryGalleryTypeBinding binding) {
            super(binding.getRoot(), binding.galleryFrameLayoutItemPostGalleryGalleryType,
                    binding.galleryRecyclerViewItemPostGalleryGalleryType, binding.imageIndexTextViewItemPostGalleryGalleryType,
                    binding.imageViewNoPreviewItemPostGalleryGalleryType);
        }
    }

    @UnstableApi
    class PostCard2VideoAutoplayViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostCard2VideoAutoplayViewHolder(ItemPostCard2VideoAutoplayBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard2VideoAutoplay,
                    binding.subredditNameTextViewItemPostCard2VideoAutoplay,
                    binding.userTextViewItemPostCard2VideoAutoplay,
                    binding.stickiedPostImageViewItemPostCard2VideoAutoplay,
                    binding.postTimeTextViewItemPostCard2VideoAutoplay,
                    binding.titleTextViewItemPostCard2VideoAutoplay,
                    binding.typeTextViewItemPostCard2VideoAutoplay,
                    binding.crosspostImageViewItemPostCard2VideoAutoplay,
                    binding.archivedImageViewItemPostCard2VideoAutoplay,
                    binding.lockedImageViewItemPostCard2VideoAutoplay,
                    binding.nsfwTextViewItemPostCard2VideoAutoplay,
                    binding.spoilerCustomTextViewItemPostCard2VideoAutoplay,
                    binding.flairCustomTextViewItemPostCard2VideoAutoplay,
                    binding.aspectRatioFrameLayoutItemPostCard2VideoAutoplay,
                    binding.previewImageViewItemPostCard2VideoAutoplay,
                    binding.errorLoadingVideoImageViewItemPostCard2VideoAutoplay,
                    binding.playerViewItemPostCard2VideoAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostCard2VideoAutoplay,
                    binding.upvoteButtonItemPostCard2VideoAutoplay,
                    binding.scoreTextViewItemPostCard2VideoAutoplay,
                    binding.downvoteButtonItemPostCard2VideoAutoplay,
                    binding.commentsCountButtonItemPostCard2VideoAutoplay,
                    binding.saveButtonItemPostCard2VideoAutoplay,
                    binding.shareButtonItemPostCard2VideoAutoplay);

            binding.dividerItemPostCard2VideoAutoplay.setBackgroundColor(mDividerColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }
    }

    @UnstableApi
    class PostCard2VideoAutoplayLegacyControllerViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostCard2VideoAutoplayLegacyControllerViewHolder(ItemPostCard2VideoAutoplayLegacyControllerBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard2VideoAutoplay,
                    binding.subredditNameTextViewItemPostCard2VideoAutoplay,
                    binding.userTextViewItemPostCard2VideoAutoplay,
                    binding.stickiedPostImageViewItemPostCard2VideoAutoplay,
                    binding.postTimeTextViewItemPostCard2VideoAutoplay,
                    binding.titleTextViewItemPostCard2VideoAutoplay,
                    binding.typeTextViewItemPostCard2VideoAutoplay,
                    binding.crosspostImageViewItemPostCard2VideoAutoplay,
                    binding.archivedImageViewItemPostCard2VideoAutoplay,
                    binding.lockedImageViewItemPostCard2VideoAutoplay,
                    binding.nsfwTextViewItemPostCard2VideoAutoplay,
                    binding.spoilerCustomTextViewItemPostCard2VideoAutoplay,
                    binding.flairCustomTextViewItemPostCard2VideoAutoplay,
                    binding.aspectRatioFrameLayoutItemPostCard2VideoAutoplay,
                    binding.previewImageViewItemPostCard2VideoAutoplay,
                    binding.errorLoadingVideoImageViewItemPostCard2VideoAutoplay,
                    binding.playerViewItemPostCard2VideoAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostCard2VideoAutoplay,
                    binding.upvoteButtonItemPostCard2VideoAutoplay,
                    binding.scoreTextViewItemPostCard2VideoAutoplay,
                    binding.downvoteButtonItemPostCard2VideoAutoplay,
                    binding.commentsCountButtonItemPostCard2VideoAutoplay,
                    binding.saveButtonItemPostCard2VideoAutoplay,
                    binding.shareButtonItemPostCard2VideoAutoplay);

            binding.dividerItemPostCard2VideoAutoplay.setBackgroundColor(mDividerColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }
    }

    class PostCard2WithPreviewViewHolder extends PostWithPreviewTypeViewHolder {

        PostCard2WithPreviewViewHolder(@NonNull ItemPostCard2WithPreviewBinding binding) {
            super(binding.getRoot());
            setBaseView(
                    binding.iconGifImageViewItemPostCard2WithPreview,
                    binding.subredditNameTextViewItemPostCard2WithPreview,
                    binding.userTextViewItemPostCard2WithPreview,
                    binding.stickiedPostImageViewItemPostCard2WithPreview,
                    binding.postTimeTextViewItemPostCard2WithPreview,
                    binding.titleTextViewItemPostCard2WithPreview,
                    binding.typeTextViewItemPostCard2WithPreview,
                    binding.archivedImageViewItemPostCard2WithPreview,
                    binding.lockedImageViewItemPostCard2WithPreview,
                    binding.crosspostImageViewItemPostCard2WithPreview,
                    binding.nsfwTextViewItemPostCard2WithPreview,
                    binding.spoilerCustomTextViewItemPostCard2WithPreview,
                    binding.flairCustomTextViewItemPostCard2WithPreview,
                    binding.bottomConstraintLayoutItemPostCard2WithPreview,
                    binding.upvoteButtonItemPostCard2WithPreview,
                    binding.scoreTextViewItemPostCard2WithPreview,
                    binding.downvoteButtonItemPostCard2WithPreview,
                    binding.commentsCountButtonItemPostCard2WithPreview,
                    binding.saveButtonItemPostCard2WithPreview,
                    binding.shareButtonItemPostCard2WithPreview,
                    binding.linkTextViewItemPostCard2WithPreview,
                    binding.imageViewNoPreviewGalleryItemPostCard2WithPreview,
                    binding.progressBarItemPostCard2WithPreview,
                    binding.videoOrGifIndicatorImageViewItemPostCard2WithPreview,
                    binding.loadImageErrorTextViewItemPostCard2WithPreview,
                    null,
                    binding.imageViewItemPostCard2WithPreview);

            binding.dividerItemPostCard2WithPreview.setBackgroundColor(mDividerColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }
    }

    public class PostCard2GalleryTypeViewHolder extends PostBaseGalleryTypeViewHolder {

        PostCard2GalleryTypeViewHolder(ItemPostCard2GalleryTypeBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard2GalleryType,
                    binding.subredditNameTextViewItemPostCard2GalleryType,
                    binding.userTextViewItemPostCard2GalleryType,
                    binding.stickiedPostImageViewItemPostCard2GalleryType,
                    binding.postTimeTextViewItemPostCard2GalleryType,
                    binding.titleTextViewItemPostCard2GalleryType,
                    binding.typeTextViewItemPostCard2GalleryType,
                    binding.archivedImageViewItemPostCard2GalleryType,
                    binding.lockedImageViewItemPostCard2GalleryType,
                    binding.crosspostImageViewItemPostCard2GalleryType,
                    binding.nsfwTextViewItemPostCard2GalleryType,
                    binding.spoilerCustomTextViewItemPostCard2GalleryType,
                    binding.flairCustomTextViewItemPostCard2GalleryType,
                    binding.galleryFrameLayoutItemPostCard2GalleryType,
                    binding.galleryRecyclerViewItemPostCard2GalleryType,
                    binding.imageIndexTextViewItemPostCard2GalleryType,
                    binding.noPreviewImageViewItemPostCard2GalleryType,
                    binding.bottomConstraintLayoutItemPostCard2GalleryType,
                    binding.upvoteButtonItemPostCard2GalleryType,
                    binding.scoreTextViewItemPostCard2GalleryType,
                    binding.downvoteButtonItemPostCard2GalleryType,
                    binding.commentsCountButtonItemPostCard2GalleryType,
                    binding.saveButtonItemPostCard2GalleryType,
                    binding.shareButtonItemPostCard2GalleryType);

            binding.dividerItemPostCard2GalleryType.setBackgroundColor(mDividerColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }
    }

    class PostCard2TextTypeViewHolder extends PostTextTypeViewHolder {
        PostCard2TextTypeViewHolder(@NonNull ItemPostCard2TextBinding binding) {
            super(binding.getRoot());

            setBaseView(
                    binding.iconGifImageViewItemPostCard2Text,
                    binding.subredditNameTextViewItemPostCard2Text,
                    binding.userTextViewItemPostCard2Text,
                    binding.stickiedPostImageViewItemPostCard2Text,
                    binding.postTimeTextViewItemPostCard2Text,
                    binding.titleTextViewItemPostCard2Text,
                    binding.typeTextViewItemPostCard2Text,
                    binding.archivedImageViewItemPostCard2Text,
                    binding.lockedImageViewItemPostCard2Text,
                    binding.crosspostImageViewItemPostCard2Text,
                    binding.nsfwTextViewItemPostCard2Text,
                    binding.spoilerCustomTextViewItemPostCard2Text,
                    binding.flairCustomTextViewItemPostCard2Text,
                    binding.bottomConstraintLayoutItemPostCard2Text,
                    binding.upvoteButtonItemPostCard2Text,
                    binding.scoreTextViewItemPostCard2Text,
                    binding.downvoteButtonItemPostCard2Text,
                    binding.commentsCountButtonItemPostCard2Text,
                    binding.saveButtonItemPostCard2Text,
                    binding.shareButtonItemPostCard2Text,
                    binding.contentTextViewItemPostCard2Text);

            binding.dividerItemPostCard2Text.setBackgroundColor(mDividerColor);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundColor(isReadPost ? mReadPostCardViewBackgroundColor : mCardViewBackgroundColor);
        }
    }

    @UnstableApi
    class PostMaterial3CardVideoAutoplayViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostMaterial3CardVideoAutoplayViewHolder(ItemPostCard3VideoTypeAutoplayBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard3VideoTypeAutoplay,
                    binding.subredditNameTextViewItemPostCard3VideoTypeAutoplay,
                    binding.userTextViewItemPostCard3VideoTypeAutoplay,
                    binding.stickiedPostImageViewItemPostCard3VideoTypeAutoplay,
                    binding.postTimeTextViewItemPostCard3VideoTypeAutoplay,
                    binding.titleTextViewItemPostCard3VideoTypeAutoplay,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    binding.aspectRatioFrameLayoutItemPostCard3VideoTypeAutoplay,
                    binding.previewImageViewItemPostCard3VideoTypeAutoplay,
                    binding.errorLoadingVideoImageViewItemPostCard3VideoTypeAutoplay,
                    binding.playerViewItemPostCard3VideoTypeAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostCard3VideoTypeAutoplay,
                    binding.upvoteButtonItemPostCard3VideoTypeAutoplay,
                    binding.scoreTextViewItemPostCard3VideoTypeAutoplay,
                    binding.downvoteButtonItemPostCard3VideoTypeAutoplay,
                    binding.commentsCountButtonItemPostCard3VideoTypeAutoplay,
                    binding.saveButtonItemPostCard3VideoTypeAutoplay,
                    binding.shareButtonItemPostCard3VideoTypeAutoplay);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostFilledCardViewBackgroundColor : mFilledCardViewBackgroundColor));
        }
    }

    @UnstableApi
    class PostMaterial3CardVideoAutoplayLegacyControllerViewHolder extends PostBaseVideoAutoplayViewHolder {
        PostMaterial3CardVideoAutoplayLegacyControllerViewHolder(ItemPostCard3VideoTypeAutoplayLegacyControllerBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard3VideoTypeAutoplay,
                    binding.subredditNameTextViewItemPostCard3VideoTypeAutoplay,
                    binding.userTextViewItemPostCard3VideoTypeAutoplay,
                    binding.stickiedPostImageViewItemPostCard3VideoTypeAutoplay,
                    binding.postTimeTextViewItemPostCard3VideoTypeAutoplay,
                    binding.titleTextViewItemPostCard3VideoTypeAutoplay,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    binding.aspectRatioFrameLayoutItemPostCard3VideoTypeAutoplay,
                    binding.previewImageViewItemPostCard3VideoTypeAutoplay,
                    binding.errorLoadingVideoImageViewItemPostCard3VideoTypeAutoplay,
                    binding.playerViewItemPostCard3VideoTypeAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.bottomConstraintLayoutItemPostCard3VideoTypeAutoplay,
                    binding.upvoteButtonItemPostCard3VideoTypeAutoplay,
                    binding.scoreTextViewItemPostCard3VideoTypeAutoplay,
                    binding.downvoteButtonItemPostCard3VideoTypeAutoplay,
                    binding.commentsCountButtonItemPostCard3VideoTypeAutoplay,
                    binding.saveButtonItemPostCard3VideoTypeAutoplay,
                    binding.shareButtonItemPostCard3VideoTypeAutoplay);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostFilledCardViewBackgroundColor : mFilledCardViewBackgroundColor));
        }
    }

    public class PostMaterial3CardWithPreviewViewHolder extends PostWithPreviewTypeViewHolder {
        PostMaterial3CardWithPreviewViewHolder(@NonNull ItemPostCard3WithPreviewBinding binding) {
            super(binding.getRoot());
            setBaseView(binding.iconGifImageViewItemPostCard3WithPreview,
                    binding.subredditNameTextViewItemPostCard3WithPreview,
                    binding.userTextViewItemPostCard3WithPreview,
                    binding.stickiedPostImageViewItemPostCard3WithPreview,
                    binding.postTimeTextViewItemPostCard3WithPreview,
                    binding.titleTextViewItemPostCard3WithPreview,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    binding.bottomConstraintLayoutItemPostCard3WithPreview,
                    binding.upvoteButtonItemPostCard3WithPreview,
                    binding.scoreTextViewItemPostCard3WithPreview,
                    binding.downvoteButtonItemPostCard3WithPreview,
                    binding.commentsCountButtonItemPostCard3WithPreview,
                    binding.saveButtonItemPostCard3WithPreview,
                    binding.shareButtonItemPostCard3WithPreview,
                    binding.linkTextViewItemPostCard3WithPreview,
                    binding.imageViewNoPreviewGalleryItemPostCard3WithPreview,
                    binding.progressBarItemPostCard3WithPreview,
                    binding.videoOrGifIndicatorImageViewItemPostCard3WithPreview,
                    binding.loadImageErrorTextViewItemPostCard3WithPreview,
                    binding.imageWrapperRelativeLayoutItemPostCard3WithPreview,
                    binding.imageViewItemPostCard3WithPreview);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostFilledCardViewBackgroundColor : mFilledCardViewBackgroundColor));
        }
    }

    public class PostMaterial3CardGalleryTypeViewHolder extends PostBaseGalleryTypeViewHolder {
        PostMaterial3CardGalleryTypeViewHolder(ItemPostCard3GalleryTypeBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostCard3GalleryType,
                    binding.subredditNameTextViewItemPostCard3GalleryType,
                    binding.userTextViewItemPostCard3GalleryType,
                    binding.stickiedPostImageViewItemPostCard3GalleryType,
                    binding.postTimeTextViewItemPostCard3GalleryType,
                    binding.titleTextViewItemPostCard3GalleryType,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    binding.galleryFrameLayoutItemPostCard3GalleryType,
                    binding.galleryRecyclerViewItemPostCard3GalleryType,
                    binding.imageIndexTextViewItemPostCard3GalleryType,
                    binding.noPreviewImageViewItemPostCard3GalleryType,
                    binding.bottomConstraintLayoutItemPostCard3GalleryType,
                    binding.upvoteButtonItemPostCard3GalleryType,
                    binding.scoreTextViewItemPostCard3GalleryType,
                    binding.downvoteButtonItemPostCard3GalleryType,
                    binding.commentsCountButtonItemPostCard3GalleryType,
                    binding.saveButtonItemPostCard3GalleryType,
                    binding.shareButtonItemPostCard3GalleryType);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostFilledCardViewBackgroundColor : mFilledCardViewBackgroundColor));
        }
    }

    class PostMaterial3CardTextTypeViewHolder extends PostTextTypeViewHolder {
        PostMaterial3CardTextTypeViewHolder(@NonNull ItemPostCard3TextBinding binding) {
            super(binding.getRoot());
            setBaseView(
                    binding.iconGifImageViewItemPostCard3TextType,
                    binding.subredditNameTextViewItemPostCard3TextType,
                    binding.userTextViewItemPostCard3TextType,
                    binding.stickiedPostImageViewItemPostCard3TextType,
                    binding.postTimeTextViewItemPostCard3TextType,
                    binding.titleTextViewItemPostCard3TextType,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    binding.bottomConstraintLayoutItemPostCard3TextType,
                    binding.upvoteButtonItemPostCard3TextType,
                    binding.scoreTextViewItemPostCard3TextType,
                    binding.downvoteButtonItemPostCard3TextType,
                    binding.commentsCountButtonItemPostCard3TextType,
                    binding.saveButtonItemPostCard3TextType,
                    binding.shareButtonItemPostCard3TextType,
                    binding.contentTextViewItemPostCard3TextType);
        }

        @Override
        void setItemViewBackgroundColor(boolean isReadPost) {
            itemView.setBackgroundTintList(ColorStateList.valueOf(isReadPost ? mReadPostFilledCardViewBackgroundColor : mFilledCardViewBackgroundColor));
        }
    }
}
