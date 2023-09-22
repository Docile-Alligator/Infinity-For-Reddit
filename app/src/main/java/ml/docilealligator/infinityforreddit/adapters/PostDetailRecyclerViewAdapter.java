package ml.docilealligator.infinityforreddit.adapters;

import static ml.docilealligator.infinityforreddit.activities.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.exoplayer2.Tracks;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.DefaultTimeBar;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.TimeBar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.collect.ImmutableList;
import com.libRG.CustomTextView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import javax.inject.Provider;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.FetchGfycatOrRedgifsVideoLinks;
import ml.docilealligator.infinityforreddit.FetchStreamableVideo;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.StreamableVideo;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.FilteredPostsActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewRedditGalleryActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.apis.GfycatAPI;
import ml.docilealligator.infinityforreddit.apis.RedgifsAPI;
import ml.docilealligator.infinityforreddit.apis.StreamableAPI;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.asynctasks.LoadUserData;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailGalleryBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailImageAndGifAutoplayBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailLinkBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailNoPreviewBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailTextBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailVideoAndGifPreviewBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailVideoAutoplayBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemPostDetailVideoAutoplayLegacyControllerBinding;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
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

public class PostDetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements CacheManager {
    private static final int VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY = 1;
    private static final int VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW = 2;
    private static final int VIEW_TYPE_POST_DETAIL_IMAGE = 3;
    private static final int VIEW_TYPE_POST_DETAIL_GIF_AUTOPLAY = 4;
    private static final int VIEW_TYPE_POST_DETAIL_LINK = 5;
    private static final int VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK = 6;
    private static final int VIEW_TYPE_POST_DETAIL_GALLERY = 7;
    private static final int VIEW_TYPE_POST_DETAIL_TEXT_TYPE = 8;
    private BaseActivity mActivity;
    private ViewPostDetailFragment mFragment;
    private Executor mExecutor;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private Retrofit mGfycatRetrofit;
    private Retrofit mRedgifsRetrofit;
    private final Provider<StreamableAPI> mStreamableApiProvider;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private SharedPreferences mCurrentAccountSharedPreferences;
    private RequestManager mGlide;
    private SaveMemoryCenterInisdeDownsampleStrategy mSaveMemoryCenterInsideDownsampleStrategy;
    private Markwon mPostDetailMarkwon;
    private final MarkwonAdapter mMarkwonAdapter;
    private String mAccessToken;
    private String mAccountName;
    private Post mPost;
    private String mSubredditNamePrefixed;
    private Locale mLocale;
    private boolean mNeedBlurNsfw;
    private boolean mDoNotBlurNsfwInNsfwSubreddits;
    private boolean mNeedBlurSpoiler;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mShowAbsoluteNumberOfVotes;
    private boolean mAutoplay = false;
    private boolean mAutoplayNsfwVideos;
    private boolean mMuteAutoplayingVideos;
    private double mStartAutoplayVisibleAreaOffset;
    private boolean mMuteNSFWVideo;
    private boolean mAutomaticallyTryRedgifs;
    private boolean mDataSavingMode;
    private boolean mDisableImagePreview;
    private boolean mOnlyDisablePreviewInVideoAndGifPosts;
    private boolean mHidePostType;
    private boolean mHidePostFlair;
    private boolean mHideUpvoteRatio;
    private boolean mHideTheNumberOfAwards;
    private boolean mHideSubredditAndUserPrefix;
    private boolean mHideTheNumberOfVotes;
    private boolean mHideTheNumberOfComments;
    private boolean mSeparatePostAndComments;
    private boolean mLegacyAutoplayVideoControllerUI;
    private boolean mEasierToWatchInFullScreen;
    private PostDetailRecyclerViewAdapterCallback mPostDetailRecyclerViewAdapterCallback;

    private int mColorAccent;
    private int mCardViewColor;
    private int mSecondaryTextColor;
    private int mPostTitleColor;
    private int mPrimaryTextColor;
    private int mPostTypeBackgroundColor;
    private int mPostTypeTextColor;
    private int mSubredditColor;
    private int mUsernameColor;
    private int mModeratorColor;
    private int mAuthorFlairTextColor;
    private int mSpoilerBackgroundColor;
    private int mSpoilerTextColor;
    private int mFlairBackgroundColor;
    private int mFlairTextColor;
    private int mNSFWBackgroundColor;
    private int mNSFWTextColor;
    private int mArchivedTintColor;
    private int mLockedTintColor;
    private int mCrosspostTintColor;
    private int mMediaIndicatorIconTint;
    private int mMediaIndicatorBackgroundColor;
    private int mUpvoteRatioTintColor;
    private int mNoPreviewPostTypeBackgroundColor;
    private int mNoPreviewPostTypeIconTint;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mPostIconAndInfoColor;
    private int mCommentColor;

    private Drawable mCommentIcon;
    private float mScale;
    private ExoCreator mExoCreator;
    private boolean canStartActivity = true;
    private boolean canPlayVideo = true;

    public PostDetailRecyclerViewAdapter(BaseActivity activity, ViewPostDetailFragment fragment,
                                         Executor executor, CustomThemeWrapper customThemeWrapper,
                                         Retrofit retrofit, Retrofit oauthRetrofit, Retrofit gfycatRetrofit,
                                         Retrofit redgifsRetrofit, Provider<StreamableAPI> streamableApiProvider,
                                         RedditDataRoomDatabase redditDataRoomDatabase, RequestManager glide,
                                         boolean separatePostAndComments, String accessToken,
                                         String accountName, Post post, Locale locale,
                                         SharedPreferences sharedPreferences,
                                         SharedPreferences currentAccountSharedPreferences,
                                         SharedPreferences nsfwAndSpoilerSharedPreferences,
                                         SharedPreferences postDetailsSharedPreferences,
                                         ExoCreator exoCreator,
                                         PostDetailRecyclerViewAdapterCallback postDetailRecyclerViewAdapterCallback) {
        mActivity = activity;
        mFragment = fragment;
        mExecutor = executor;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mGfycatRetrofit = gfycatRetrofit;
        mRedgifsRetrofit = redgifsRetrofit;
        mStreamableApiProvider = streamableApiProvider;
        mRedditDataRoomDatabase = redditDataRoomDatabase;
        mGlide = glide;
        mSaveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        mCurrentAccountSharedPreferences = currentAccountSharedPreferences;
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        int markdownColor = customThemeWrapper.getPostContentColor();
        int postSpoilerBackgroundColor = markdownColor | 0xFF000000;
        int linkColor = customThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (mActivity.contentTypeface != null) {
                    textView.setTypeface(mActivity.contentTypeface);
                }
                textView.setTextColor(markdownColor);
                textView.setHighlightColor(Color.TRANSPARENT);
                textView.setOnLongClickListener(view -> {
                    if (textView.getSelectionStart() == -1 && textView.getSelectionEnd() == -1) {
                        CopyTextBottomSheetFragment.show(
                                mActivity.getSupportFragmentManager(),
                                mPost.getSelfTextPlain(), mPost.getSelfText()
                        );
                    }
                    return true;
                });
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                    mActivity.startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(linkColor);
            }
        };
        BetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            if (activity != null && !activity.isDestroyed() && !activity.isFinishing()) {
                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
                urlMenuBottomSheetFragment.setArguments(bundle);
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
            }
            return true;
        };
        mPostDetailMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, markdownColor, postSpoilerBackgroundColor, onLinkLongClickListener);
        mMarkwonAdapter = MarkdownUtils.createTablesAdapter();

        mSeparatePostAndComments = separatePostAndComments;
        mLegacyAutoplayVideoControllerUI = sharedPreferences.getBoolean(SharedPreferencesUtils.LEGACY_AUTOPLAY_VIDEO_CONTROLLER_UI, false);
        mEasierToWatchInFullScreen = sharedPreferences.getBoolean(SharedPreferencesUtils.EASIER_TO_WATCH_IN_FULL_SCREEN, false);
        mAccessToken = accessToken;
        mAccountName = accountName;
        mPost = post;
        mSubredditNamePrefixed = post.getSubredditNamePrefixed();
        mLocale = locale;

        mNeedBlurNsfw = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        mDoNotBlurNsfwInNsfwSubreddits = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        mNeedBlurSpoiler = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName == null ? "" : mAccountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
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

        Resources resources = activity.getResources();
        mStartAutoplayVisibleAreaOffset = resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT ?
                sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_PORTRAIT, 75) / 100.0 :
                sharedPreferences.getInt(SharedPreferencesUtils.START_AUTOPLAY_VISIBLE_AREA_OFFSET_LANDSCAPE, 50) / 100.0;

        mMuteNSFWVideo = sharedPreferences.getBoolean(SharedPreferencesUtils.MUTE_NSFW_VIDEO, false);
        mAutomaticallyTryRedgifs = sharedPreferences.getBoolean(SharedPreferencesUtils.AUTOMATICALLY_TRY_REDGIFS, true);

        String dataSavingModeString = sharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
        if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
            mDataSavingMode = true;
        } else if (dataSavingModeString.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
            mDataSavingMode = networkType == Utils.NETWORK_TYPE_CELLULAR;
        }
        mDisableImagePreview = sharedPreferences.getBoolean(SharedPreferencesUtils.DISABLE_IMAGE_PREVIEW, false);
        mOnlyDisablePreviewInVideoAndGifPosts = sharedPreferences.getBoolean(SharedPreferencesUtils.ONLY_DISABLE_PREVIEW_IN_VIDEO_AND_GIF_POSTS, false);

        mHidePostType = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_TYPE, false);
        mHidePostFlair = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_POST_FLAIR, false);
        mHideUpvoteRatio = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_UPVOTE_RATIO, false);
        mHideTheNumberOfAwards = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_AWARDS, false);
        mHideSubredditAndUserPrefix = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_SUBREDDIT_AND_USER_PREFIX, false);
        mHideTheNumberOfVotes = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES, false);
        mHideTheNumberOfComments = postDetailsSharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_COMMENTS, false);

        mPostDetailRecyclerViewAdapterCallback = postDetailRecyclerViewAdapterCallback;
        mScale = resources.getDisplayMetrics().density;

        mColorAccent = customThemeWrapper.getColorAccent();
        mCardViewColor = customThemeWrapper.getCardViewBackgroundColor();
        mPostTitleColor = customThemeWrapper.getPostTitleColor();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mPostTypeBackgroundColor = customThemeWrapper.getPostTypeBackgroundColor();
        mPostTypeTextColor = customThemeWrapper.getPostTypeTextColor();
        mAuthorFlairTextColor = customThemeWrapper.getAuthorFlairTextColor();
        mSpoilerBackgroundColor = customThemeWrapper.getSpoilerBackgroundColor();
        mSpoilerTextColor = customThemeWrapper.getSpoilerTextColor();
        mNSFWBackgroundColor = customThemeWrapper.getNsfwBackgroundColor();
        mNSFWTextColor = customThemeWrapper.getNsfwTextColor();
        mArchivedTintColor = customThemeWrapper.getArchivedIconTint();
        mLockedTintColor = customThemeWrapper.getLockedIconTint();
        mCrosspostTintColor = customThemeWrapper.getCrosspostIconTint();
        mMediaIndicatorIconTint = customThemeWrapper.getMediaIndicatorIconColor();
        mMediaIndicatorBackgroundColor = customThemeWrapper.getMediaIndicatorBackgroundColor();
        mUpvoteRatioTintColor = customThemeWrapper.getUpvoteRatioIconTint();
        mNoPreviewPostTypeBackgroundColor = customThemeWrapper.getNoPreviewPostTypeBackgroundColor();
        mNoPreviewPostTypeIconTint = customThemeWrapper.getNoPreviewPostTypeIconTint();
        mFlairBackgroundColor = customThemeWrapper.getFlairBackgroundColor();
        mFlairTextColor = customThemeWrapper.getFlairTextColor();
        mSubredditColor = customThemeWrapper.getSubreddit();
        mUsernameColor = customThemeWrapper.getUsername();
        mModeratorColor = customThemeWrapper.getModerator();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
        mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
        mCommentColor = customThemeWrapper.getCommentColor();

        mCommentIcon = AppCompatResources.getDrawable(activity, R.drawable.ic_comment_grey_24dp);
        if (mCommentIcon != null) {
            mCommentIcon.setTint(mPostIconAndInfoColor);
        }

        mExoCreator = exoCreator;
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    @Override
    public int getItemViewType(int position) {
        switch (mPost.getPostType()) {
            case Post.VIDEO_TYPE:
                if (mAutoplay && !mSeparatePostAndComments) {
                    if ((!mAutoplayNsfwVideos && mPost.isNSFW()) || mPost.isSpoiler()) {
                        return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                    }
                    return VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY;
                } else {
                    return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                }
            case Post.GIF_TYPE:
                if (mAutoplay) {
                    if ((!mAutoplayNsfwVideos && mPost.isNSFW()) || mPost.isSpoiler()) {
                        return VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK;
                    }
                    return VIEW_TYPE_POST_DETAIL_GIF_AUTOPLAY;
                } else {
                    if ((mPost.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                        return VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK;
                    }
                    return VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW;
                }
            case Post.IMAGE_TYPE:
                return VIEW_TYPE_POST_DETAIL_IMAGE;
            case Post.LINK_TYPE:
                return VIEW_TYPE_POST_DETAIL_LINK;
            case Post.NO_PREVIEW_LINK_TYPE:
                return VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK;
            case Post.GALLERY_TYPE:
                return VIEW_TYPE_POST_DETAIL_GALLERY;
            default:
                return VIEW_TYPE_POST_DETAIL_TEXT_TYPE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_POST_DETAIL_VIDEO_AUTOPLAY:
                if (mDataSavingMode) {
                    if (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts) {
                        return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                    }
                    return new PostDetailVideoAndGifPreviewHolder(ItemPostDetailVideoAndGifPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }

                if (mLegacyAutoplayVideoControllerUI) {
                    return new PostDetailVideoAutoplayLegacyControllerViewHolder(ItemPostDetailVideoAutoplayLegacyControllerBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                } else {
                    return new PostDetailVideoAutoplayViewHolder(ItemPostDetailVideoAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
            case VIEW_TYPE_POST_DETAIL_VIDEO_AND_GIF_PREVIEW:
                if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                    return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
                return new PostDetailVideoAndGifPreviewHolder(ItemPostDetailVideoAndGifPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_POST_DETAIL_IMAGE:
                if (mDataSavingMode && mDisableImagePreview) {
                    return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
                return new PostDetailImageAndGifAutoplayViewHolder(ItemPostDetailImageAndGifAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_POST_DETAIL_GIF_AUTOPLAY:
                if (mDataSavingMode && (mDisableImagePreview || mOnlyDisablePreviewInVideoAndGifPosts)) {
                    return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
                return new PostDetailImageAndGifAutoplayViewHolder(ItemPostDetailImageAndGifAutoplayBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_POST_DETAIL_LINK:
                if (mDataSavingMode && mDisableImagePreview) {
                    return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
                return new PostDetailLinkViewHolder(ItemPostDetailLinkBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_POST_DETAIL_NO_PREVIEW_LINK:
                return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_POST_DETAIL_GALLERY:
                if (mDataSavingMode && mDisableImagePreview) {
                    return new PostDetailNoPreviewViewHolder(ItemPostDetailNoPreviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
                }
                return new PostDetailGalleryViewHolder(ItemPostDetailGalleryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default:
                return new PostDetailTextViewHolder(ItemPostDetailTextBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostDetailBaseViewHolder) {
            ((PostDetailBaseViewHolder) holder).titleTextView.setText(mPost.getTitle());
            if (mPost.getSubredditNamePrefixed().startsWith("u/")) {
                if (mPost.getAuthorIconUrl() == null) {
                    String authorName = mPost.isAuthorDeleted() ? mPost.getSubredditNamePrefixed().substring(2) : mPost.getAuthor();
                    LoadUserData.loadUserData(mExecutor, new Handler(), mRedditDataRoomDatabase, authorName, mOauthRetrofit, iconImageUrl -> {
                        if (mActivity != null && getItemCount() > 0) {
                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                            } else {
                                mGlide.load(iconImageUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                        .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                            }

                            if (holder.getBindingAdapterPosition() >= 0) {
                                mPost.setAuthorIconUrl(iconImageUrl);
                            }
                        }
                    });
                } else if (!mPost.getAuthorIconUrl().equals("")) {
                    mGlide.load(mPost.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                } else {
                    mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                }
            } else {
                if (mPost.getSubredditIconUrl() == null) {
                    LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(),
                            mRedditDataRoomDatabase, mPost.getSubredditNamePrefixed().substring(2),
                            mAccessToken, mOauthRetrofit, mRetrofit, iconImageUrl -> {
                                if (iconImageUrl == null || iconImageUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconImageUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                                }

                                mPost.setSubredditIconUrl(iconImageUrl);
                            });
                } else if (!mPost.getSubredditIconUrl().equals("")) {
                    mGlide.load(mPost.getSubredditIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                } else {
                    mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((PostDetailBaseViewHolder) holder).iconGifImageView);
                }
            }

            if (mPost.getAuthorFlairHTML() != null && !mPost.getAuthorFlairHTML().equals("")) {
                ((PostDetailBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((PostDetailBaseViewHolder) holder).authorFlairTextView, mPost.getAuthorFlairHTML(), true);
            } else if (mPost.getAuthorFlair() != null && !mPost.getAuthorFlair().equals("")) {
                ((PostDetailBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                ((PostDetailBaseViewHolder) holder).authorFlairTextView.setText(mPost.getAuthorFlair());
            }

            switch (mPost.getVoteType()) {
                case 1:
                    //Upvoted
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setTextColor(mUpvotedColor);
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                    break;
                case -1:
                    //Downvoted
                    ((PostDetailBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                    ((PostDetailBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                    break;
                case 0:
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setTextColor(mPostIconAndInfoColor);
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                    ((PostDetailBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                    ((PostDetailBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                    ((PostDetailBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            }

            if (mPost.isArchived()) {
                ((PostDetailBaseViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);
                ((PostDetailBaseViewHolder) holder).upvoteButton.setTextColor(mVoteAndReplyUnavailableVoteButtonColor);
                ((PostDetailBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                ((PostDetailBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
            }

            if (mPost.isCrosspost()) {
                ((PostDetailBaseViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
            }

            if (!mHideSubredditAndUserPrefix) {
                ((PostDetailBaseViewHolder) holder).subredditTextView.setText("r/" + mPost.getSubredditName());
                ((PostDetailBaseViewHolder) holder).userTextView.setText(mPost.getAuthorNamePrefixed());
            } else {
                ((PostDetailBaseViewHolder) holder).subredditTextView.setText(mPost.getSubredditName());
                ((PostDetailBaseViewHolder) holder).userTextView.setText(mPost.getAuthor());
            }

            if (mPost.isModerator()) {
                ((PostDetailBaseViewHolder) holder).userTextView.setTextColor(mModeratorColor);
                Drawable moderatorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_verified_user_14dp, mModeratorColor);
                ((PostDetailBaseViewHolder) holder).userTextView.setCompoundDrawablesWithIntrinsicBounds(
                        moderatorDrawable, null, null, null);
            }

            if (mShowElapsedTime) {
                ((PostDetailBaseViewHolder) holder).postTimeTextView.setText(
                        Utils.getElapsedTime(mActivity, mPost.getPostTimeMillis()));
            } else {
                ((PostDetailBaseViewHolder) holder).postTimeTextView.setText(Utils.getFormattedTime(mLocale, mPost.getPostTimeMillis(), mTimeFormatPattern));
            }

            if (mPost.isLocked()) {
                ((PostDetailBaseViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
            }

            if (mPost.isSpoiler()) {
                ((PostDetailBaseViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
            }

            if (!mHidePostFlair && mPost.getFlair() != null && !mPost.getFlair().equals("")) {
                ((PostDetailBaseViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((PostDetailBaseViewHolder) holder).flairTextView, mPost.getFlair(), false);
            }

            if (!mHideTheNumberOfAwards && mPost.getAwards() != null && !mPost.getAwards().equals("")) {
                ((PostDetailBaseViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                Utils.setHTMLWithImageToTextView(((PostDetailBaseViewHolder) holder).awardsTextView, mPost.getAwards(), true);
            }

            if (mHideUpvoteRatio) {
                ((PostDetailBaseViewHolder) holder).upvoteRatioTextView.setVisibility(View.GONE);
            } else {
                ((PostDetailBaseViewHolder) holder).upvoteRatioTextView.setText(mPost.getUpvoteRatio() + "%");
            }

            if (mPost.isNSFW()) {
                ((PostDetailBaseViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
            } else {
                ((PostDetailBaseViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            }

            if (!mHideTheNumberOfVotes) {
                ((PostDetailBaseViewHolder) holder).upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, mPost.getScore() + mPost.getVoteType()));
            } else {
                ((PostDetailBaseViewHolder) holder).upvoteButton.setText(mActivity.getString(R.string.vote));
            }

            ((PostDetailBaseViewHolder) holder).commentsCountButton.setText(Integer.toString(mPost.getNComments()));

            if (mPost.isSaved()) {
                ((PostDetailBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
            } else {
                ((PostDetailBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
            }

            if (mPost.getSelfText() != null && !mPost.getSelfText().equals("")) {
                ((PostDetailBaseViewHolder) holder).contentMarkdownView.setVisibility(View.VISIBLE);
                ((PostDetailBaseViewHolder) holder).contentMarkdownView.setAdapter(mMarkwonAdapter);
                mMarkwonAdapter.setMarkdown(mPostDetailMarkwon, mPost.getSelfText());
                // noinspection NotifyDataSetChanged
                mMarkwonAdapter.notifyDataSetChanged();
            }

            if (holder instanceof PostDetailBaseVideoAutoplayViewHolder) {
                ((PostDetailBaseVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.VISIBLE);
                Post.Preview preview = getSuitablePreview(mPost.getPreviews());
                if (preview != null) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio((float) preview.getPreviewWidth() / preview.getPreviewHeight());
                    mGlide.load(preview.getPreviewUrl()).centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostDetailBaseVideoAutoplayViewHolder) holder).previewImageView);
                } else {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).aspectRatioFrameLayout.setAspectRatio(1);
                }
                if (!((PostDetailBaseVideoAutoplayViewHolder) holder).isManuallyPaused) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).setVolume((mMuteAutoplayingVideos || (mPost.isNSFW() && mMuteNSFWVideo)) ? 0f : 1f);
                }

                if (mPost.isGfycat() || mPost.isRedgifs() && !mPost.isLoadGfycatOrStreamableVideoSuccess()) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                            mPost.isGfycat() ? mGfycatRetrofit.create(GfycatAPI.class).getGfycatData(mPost.getGfycatId()) :
                                    mRedgifsRetrofit.create(RedgifsAPI.class).getRedgifsData(APIUtils.getRedgifsOAuthHeader(mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.REDGIFS_ACCESS_TOKEN, "")), mPost.getGfycatId(), APIUtils.USER_AGENT);
                    FetchGfycatOrRedgifsVideoLinks.fetchGfycatOrRedgifsVideoLinksInRecyclerViewAdapter(mExecutor, new Handler(),
                            ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall,
                            mPost.isGfycat(), mAutomaticallyTryRedgifs,
                            new FetchGfycatOrRedgifsVideoLinks.FetchGfycatOrRedgifsVideoLinksListener() {
                                @Override
                                public void success(String webm, String mp4) {
                                    mPost.setVideoDownloadUrl(mp4);
                                    mPost.setVideoUrl(mp4);
                                    mPost.setLoadGfyOrStreamableVideoSuccess(true);
                                    ((PostDetailBaseVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(mPost.getVideoUrl()));
                                }

                                @Override
                                public void failed(int errorCode) {
                                    ((PostDetailBaseVideoAutoplayViewHolder) holder).mErrorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                }
                            });
                } else if(mPost.isStreamable() && !mPost.isLoadGfycatOrStreamableVideoSuccess()) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall =
                            mStreamableApiProvider.get().getStreamableData(mPost.getStreamableShortCode());
                    FetchStreamableVideo.fetchStreamableVideoInRecyclerViewAdapter(mExecutor, new Handler(),
                            ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall,
                            new FetchStreamableVideo.FetchStreamableVideoListener() {
                                @Override
                                public void success(StreamableVideo streamableVideo) {
                                    StreamableVideo.Media media = streamableVideo.mp4 == null ? streamableVideo.mp4Mobile : streamableVideo.mp4;
                                    mPost.setVideoDownloadUrl(media.url);
                                    mPost.setVideoUrl(media.url);
                                    mPost.setLoadGfyOrStreamableVideoSuccess(true);
                                    ((PostDetailBaseVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(mPost.getVideoUrl()));
                                }

                                @Override
                                public void failed() {
                                    ((PostDetailBaseVideoAutoplayViewHolder) holder).mErrorLoadingGfycatImageView.setVisibility(View.VISIBLE);
                                }
                            });
                } else {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).bindVideoUri(Uri.parse(mPost.getVideoUrl()));
                }
            } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
                if (!mHidePostType) {
                    if (mPost.getPostType() == Post.GIF_TYPE) {
                        ((PostDetailVideoAndGifPreviewHolder) holder).binding.typeTextViewItemPostDetailVideoAndGifPreview.setText(mActivity.getString(R.string.gif));
                    } else {
                        ((PostDetailVideoAndGifPreviewHolder) holder).binding.typeTextViewItemPostDetailVideoAndGifPreview.setText(mActivity.getString(R.string.video));
                    }
                }
                Post.Preview preview = getSuitablePreview(mPost.getPreviews());
                if (preview != null) {
                    ((PostDetailVideoAndGifPreviewHolder) holder).binding.imageViewItemPostDetailVideoAndGifPreview.setRatio((float) preview.getPreviewHeight() / (float) preview.getPreviewWidth());
                    loadImage((PostDetailVideoAndGifPreviewHolder) holder, preview);
                }
            } else if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
                if (!mHidePostType) {
                    if (mPost.getPostType() == Post.IMAGE_TYPE) {
                        ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.typeTextViewItemPostDetailImageAndGifAutoplay.setText(R.string.image);
                    } else {
                        ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.typeTextViewItemPostDetailImageAndGifAutoplay.setText(R.string.gif);
                    }
                }

                Post.Preview preview = getSuitablePreview(mPost.getPreviews());
                if (preview != null) {
                    if (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0) {
                        int height = (int) (400 * mScale);
                        ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay.setScaleType(ImageView.ScaleType.CENTER_CROP);
                        ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay.getLayoutParams().height = height;
                    } else {
                        ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay.setRatio((float) preview.getPreviewHeight() / (float) preview.getPreviewWidth());
                    }
                    loadImage((PostDetailImageAndGifAutoplayViewHolder) holder, preview);
                }
            } else if (holder instanceof PostDetailLinkViewHolder) {
                String domain = Uri.parse(mPost.getUrl()).getHost();
                ((PostDetailLinkViewHolder) holder).binding.linkTextViewItemPostDetailLink.setText(domain);
                Post.Preview preview = getSuitablePreview(mPost.getPreviews());
                if (preview != null) {
                    ((PostDetailLinkViewHolder) holder).binding.imageViewItemPostDetailLink.setRatio((float) preview.getPreviewHeight() / (float) preview.getPreviewWidth());
                    loadImage((PostDetailLinkViewHolder) holder, preview);
                }
            } else if (holder instanceof PostDetailNoPreviewViewHolder) {
                if (mPost.getPostType() == Post.LINK_TYPE || mPost.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                    if (!mHidePostType) {
                        ((PostDetailNoPreviewViewHolder) holder).binding.typeTextViewItemPostDetailNoPreview.setText(R.string.link);
                    }
                    String noPreviewLinkDomain = Uri.parse(mPost.getUrl()).getHost();
                    ((PostDetailNoPreviewViewHolder) holder).binding.linkTextViewItemPostDetailNoPreview.setVisibility(View.VISIBLE);
                    ((PostDetailNoPreviewViewHolder) holder).binding.linkTextViewItemPostDetailNoPreview.setText(noPreviewLinkDomain);
                    ((PostDetailNoPreviewViewHolder) holder).binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setImageResource(R.drawable.ic_link);
                } else {
                    ((PostDetailNoPreviewViewHolder) holder).binding.linkTextViewItemPostDetailNoPreview.setVisibility(View.GONE);
                    switch (mPost.getPostType()) {
                        case Post.VIDEO_TYPE:
                            if (!mHidePostType) {
                                ((PostDetailNoPreviewViewHolder) holder).binding.typeTextViewItemPostDetailNoPreview.setText(R.string.video);
                            }
                            ((PostDetailNoPreviewViewHolder) holder).binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setImageResource(R.drawable.ic_outline_video_24dp);
                            break;
                        case Post.IMAGE_TYPE:
                            if (!mHidePostType) {
                                ((PostDetailNoPreviewViewHolder) holder).binding.typeTextViewItemPostDetailNoPreview.setText(R.string.image);
                            }
                            ((PostDetailNoPreviewViewHolder) holder).binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setImageResource(R.drawable.ic_image_24dp);
                            break;
                        case Post.GIF_TYPE:
                            if (!mHidePostType) {
                                ((PostDetailNoPreviewViewHolder) holder).binding.typeTextViewItemPostDetailNoPreview.setText(R.string.gif);
                            }
                            ((PostDetailNoPreviewViewHolder) holder).binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setImageResource(R.drawable.ic_image_24dp);
                            break;
                        case Post.GALLERY_TYPE:
                            if (!mHidePostType) {
                                ((PostDetailNoPreviewViewHolder) holder).binding.typeTextViewItemPostDetailNoPreview.setText(R.string.gallery);
                            }
                            ((PostDetailNoPreviewViewHolder) holder).binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setImageResource(R.drawable.ic_gallery_24dp);
                            break;
                    }
                }
            } else if (holder instanceof PostDetailGalleryViewHolder) {
                if (mDataSavingMode && mDisableImagePreview) {
                    ((PostDetailGalleryViewHolder) holder).binding.noPreviewPostTypeImageViewItemPostDetailGallery.setVisibility(View.VISIBLE);
                    ((PostDetailGalleryViewHolder) holder).binding.noPreviewPostTypeImageViewItemPostDetailGallery.setImageResource(R.drawable.ic_gallery_24dp);
                } else {
                    ((PostDetailGalleryViewHolder) holder).binding.galleryFrameLayoutItemPostDetailGallery.setVisibility(View.VISIBLE);
                    ((PostDetailGalleryViewHolder) holder).binding.imageIndexTextViewItemPostDetailGallery.setText(mActivity.getString(R.string.image_index_in_gallery, 1, mPost.getGallery().size()));
                    Post.Preview preview = getSuitablePreview(mPost.getPreviews());
                    if (preview != null) {
                        if (preview.getPreviewWidth() <= 0 || preview.getPreviewHeight() <= 0) {
                            ((PostDetailGalleryViewHolder) holder).adapter.setRatio(-1);
                        } else {
                            ((PostDetailGalleryViewHolder) holder).adapter.setRatio((float) preview.getPreviewHeight() / preview.getPreviewWidth());
                        }
                    } else {
                        ((PostDetailGalleryViewHolder) holder).adapter.setRatio(-1);
                    }
                    ((PostDetailGalleryViewHolder) holder).adapter.setGalleryImages(mPost.getGallery());
                    ((PostDetailGalleryViewHolder) holder).adapter.setBlurImage(
                            (mPost.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (mPost.isSpoiler() && mNeedBlurSpoiler));
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
            if (preview.getPreviewWidth() * preview.getPreviewHeight() > 5_000_000) {
                for (int i = previews.size() - 1; i >= 1; i--) {
                    preview = previews.get(i);
                    if (preview.getPreviewWidth() * preview.getPreviewHeight() <= 5_000_000) {
                        return preview;
                    }
                }
            }

            return preview;
        }

        return null;
    }

    private void loadImage(PostDetailBaseViewHolder holder, @NonNull Post.Preview preview) {
        if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
            boolean blurImage = (mPost.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()) && !(mPost.getPostType() == Post.GIF_TYPE && mAutoplayNsfwVideos)) || (mPost.isSpoiler() && mNeedBlurSpoiler);
            String url = mPost.getPostType() == Post.IMAGE_TYPE || blurImage ? preview.getPreviewUrl() : mPost.getUrl();
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(url)
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.progressBarItemPostDetailImageAndGifAutoplay.setVisibility(View.GONE);
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailImageAndGifAutoplay.setVisibility(View.VISIBLE);
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailImageAndGifAutoplay.setOnClickListener(view -> {
                                ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.progressBarItemPostDetailImageAndGifAutoplay.setVisibility(View.VISIBLE);
                                ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailImageAndGifAutoplay.setVisibility(View.GONE);
                                loadImage(holder, preview);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailImageAndGifAutoplayViewHolder) holder).binding.loadWrapperItemPostDetailImageAndGifAutoplay.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if (blurImage) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10))).into(((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay);
            } else {
                imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay);
            }
        } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(preview.getPreviewUrl())
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailVideoAndGifPreviewHolder) holder).binding.progressBarItemPostDetailVideoAndGifPreview.setVisibility(View.GONE);
                            ((PostDetailVideoAndGifPreviewHolder) holder).binding.loadImageErrorTextViewItemPostDetailVideoAndGifPreview.setVisibility(View.VISIBLE);
                            ((PostDetailVideoAndGifPreviewHolder) holder).binding.loadImageErrorTextViewItemPostDetailVideoAndGifPreview.setOnClickListener(view -> {
                                ((PostDetailVideoAndGifPreviewHolder) holder).binding.progressBarItemPostDetailVideoAndGifPreview.setVisibility(View.VISIBLE);
                                ((PostDetailVideoAndGifPreviewHolder) holder).binding.loadImageErrorTextViewItemPostDetailVideoAndGifPreview.setVisibility(View.GONE);
                                loadImage(holder, preview);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailVideoAndGifPreviewHolder) holder).binding.loadWrapperItemPostDetailVideoAndGifPreview.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if ((mPost.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostDetailVideoAndGifPreviewHolder) holder).binding.imageViewItemPostDetailVideoAndGifPreview);
            } else {
                imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostDetailVideoAndGifPreviewHolder) holder).binding.imageViewItemPostDetailVideoAndGifPreview);
            }
        } else if (holder instanceof PostDetailLinkViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(preview.getPreviewUrl())
                    .listener(new RequestListener<>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((PostDetailLinkViewHolder) holder).binding.progressBarItemPostDetailLink.setVisibility(View.GONE);
                            ((PostDetailLinkViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailLink.setVisibility(View.VISIBLE);
                            ((PostDetailLinkViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailLink.setOnClickListener(view -> {
                                ((PostDetailLinkViewHolder) holder).binding.progressBarItemPostDetailLink.setVisibility(View.VISIBLE);
                                ((PostDetailLinkViewHolder) holder).binding.loadImageErrorTextViewItemPostDetailLink.setVisibility(View.GONE);
                                loadImage(holder, preview);
                            });
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((PostDetailLinkViewHolder) holder).binding.loadWrapperItemPostDetailLink.setVisibility(View.GONE);
                            return false;
                        }
                    });

            if ((mPost.isNSFW() && mNeedBlurNsfw && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (mPost.isSpoiler() && mNeedBlurSpoiler)) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostDetailLinkViewHolder) holder).binding.imageViewItemPostDetailLink);
            } else {
                imageRequestBuilder.centerInside().downsample(mSaveMemoryCenterInsideDownsampleStrategy).into(((PostDetailLinkViewHolder) holder).binding.imageViewItemPostDetailLink);
            }
        }
    }

    public void updatePost(Post post) {
        mPost = post;
        notifyItemChanged(0);
    }

    public void setBlurNsfwAndDoNotBlurNsfwInNsfwSubreddits(boolean needBlurNsfw, boolean doNotBlurNsfwInNsfwSubreddits) {
        mNeedBlurNsfw = needBlurNsfw;
        mDoNotBlurNsfwInNsfwSubreddits = doNotBlurNsfwInNsfwSubreddits;
    }

    public void setBlurSpoiler(boolean needBlurSpoiler) {
        mNeedBlurSpoiler = needBlurSpoiler;
    }

    public void setAutoplay(boolean autoplay) {
        mAutoplay = autoplay;
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        mDataSavingMode = dataSavingMode;
    }

    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int direction, int swipeLeftAction, int swipeRightAction) {
        if (viewHolder instanceof PostDetailBaseViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostDetailBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostDetailBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((PostDetailBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((PostDetailBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            }
        }
    }

    public void giveAward(String awardsHTML, int awardCount) {
        if (mPost != null) {
            mPost.addAwards(awardsHTML);
            mPost.addAwards(awardCount);
            notifyItemChanged(0);
        }
    }

    public void addOneComment() {
        if (mPost != null) {
            mPost.setNComments(mPost.getNComments() + 1);
            notifyItemChanged(0);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostDetailBaseViewHolder) {
            ((PostDetailBaseViewHolder) holder).upvoteButton.setTextColor(mPostIconAndInfoColor);
            ((PostDetailBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
            ((PostDetailBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            ((PostDetailBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
            ((PostDetailBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            ((PostDetailBaseViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostDetailBaseViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostDetailBaseViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostDetailBaseViewHolder) holder).contentMarkdownView.setVisibility(View.GONE);

            if (holder instanceof PostDetailBaseVideoAutoplayViewHolder) {
                if (((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall != null && !((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.isCanceled()) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall.cancel();
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).fetchGfycatOrStreamableVideoCall = null;
                }
                ((PostDetailBaseVideoAutoplayViewHolder) holder).mErrorLoadingGfycatImageView.setVisibility(View.GONE);
                ((PostDetailBaseVideoAutoplayViewHolder) holder).muteButton.setVisibility(View.GONE);
                if (!((PostDetailBaseVideoAutoplayViewHolder) holder).isManuallyPaused) {
                    ((PostDetailBaseVideoAutoplayViewHolder) holder).resetVolume();
                }
                mGlide.clear(((PostDetailBaseVideoAutoplayViewHolder) holder).previewImageView);
                ((PostDetailBaseVideoAutoplayViewHolder) holder).previewImageView.setVisibility(View.GONE);
            } else if (holder instanceof PostDetailVideoAndGifPreviewHolder) {
                mGlide.clear(((PostDetailVideoAndGifPreviewHolder) holder).binding.imageViewItemPostDetailVideoAndGifPreview);
            } else if (holder instanceof PostDetailImageAndGifAutoplayViewHolder) {
                mGlide.clear(((PostDetailImageAndGifAutoplayViewHolder) holder).binding.imageViewItemPostDetailImageAndGifAutoplay);
            } else if (holder instanceof PostDetailLinkViewHolder) {
                mGlide.clear(((PostDetailLinkViewHolder) holder).binding.imageViewItemPostDetailLink);
            } else if (holder instanceof PostDetailGalleryViewHolder) {
                ((PostDetailGalleryViewHolder) holder).binding.galleryFrameLayoutItemPostDetailGallery.setVisibility(View.GONE);
                ((PostDetailGalleryViewHolder) holder).binding.noPreviewPostTypeImageViewItemPostDetailGallery.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }

    @Nullable
    @Override
    public Object getKeyForOrder(int order) {
        return mPost;
    }

    @Nullable
    @Override
    public Integer getOrderForKey(@NonNull Object key) {
        return 0;
    }

    public void setCanPlayVideo(boolean canPlayVideo) {
        this.canPlayVideo = canPlayVideo;
    }

    public interface PostDetailRecyclerViewAdapterCallback {
        void updatePost(Post post);
    }

    public class PostDetailBaseViewHolder extends RecyclerView.ViewHolder {
        AspectRatioGifImageView iconGifImageView;
        TextView subredditTextView;
        TextView userTextView;
        TextView authorFlairTextView;
        TextView postTimeTextView;
        TextView titleTextView;
        CustomTextView typeTextView;
        ImageView crosspostImageView;
        ImageView archivedImageView;
        ImageView lockedImageView;
        CustomTextView nsfwTextView;
        CustomTextView spoilerTextView;
        CustomTextView flairTextView;
        TextView awardsTextView;
        TextView upvoteRatioTextView;
        RecyclerView contentMarkdownView;
        ConstraintLayout bottomConstraintLayout;
        MaterialButton upvoteButton;
        MaterialButton downvoteButton;
        MaterialButton commentsCountButton;
        MaterialButton saveButton;
        MaterialButton shareButton;

        PostDetailBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(AspectRatioGifImageView iconGifImageView,
                         TextView subredditTextView,
                         TextView userTextView,
                         TextView authorFlairTextView,
                         TextView postTimeTextView,
                         TextView titleTextView,
                         CustomTextView typeTextView,
                         ImageView crosspostImageView,
                         ImageView archivedImageView,
                         ImageView lockedImageView,
                         CustomTextView nSFWTextView,
                         CustomTextView spoilerTextView,
                         CustomTextView flairTextView,
                         TextView awardsTextView,
                         TextView upvoteRatioTextView,
                         RecyclerView contentMarkdownView,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButtonToggleGroup voteButtonToggleGroup,
                         MaterialButton upvoteButton,
                         MaterialButton downvoteButton,
                         MaterialButton commentsCountButton,
                         MaterialButton saveButton,
                         MaterialButton shareButton) {
            this.iconGifImageView = iconGifImageView;
            this.subredditTextView = subredditTextView;
            this.userTextView = userTextView;
            this.authorFlairTextView = authorFlairTextView;
            this.postTimeTextView = postTimeTextView;
            this.titleTextView = titleTextView;
            this.typeTextView = typeTextView;
            this.crosspostImageView = crosspostImageView;
            this.archivedImageView = archivedImageView;
            this.lockedImageView = lockedImageView;
            this.nsfwTextView = nSFWTextView;
            this.spoilerTextView = spoilerTextView;
            this.flairTextView = flairTextView;
            this.awardsTextView = awardsTextView;
            this.upvoteRatioTextView = upvoteRatioTextView;
            this.contentMarkdownView = contentMarkdownView;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.upvoteButton = upvoteButton;
            this.downvoteButton = downvoteButton;
            this.commentsCountButton = commentsCountButton;
            this.saveButton = saveButton;
            this.shareButton = shareButton;

            iconGifImageView.setOnClickListener(view -> subredditTextView.performClick());

            subredditTextView.setOnClickListener(view -> {
                Intent intent;
                intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                        mPost.getSubredditName());
                mActivity.startActivity(intent);
            });

            userTextView.setOnClickListener(view -> {
                if (mPost.isAuthorDeleted()) {
                    return;
                }
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mPost.getAuthor());
                mActivity.startActivity(intent);
            });

            authorFlairTextView.setOnClickListener(view -> userTextView.performClick());

            crosspostImageView.setOnClickListener(view -> {
                Intent crosspostIntent = new Intent(mActivity, ViewPostDetailActivity.class);
                crosspostIntent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
                mActivity.startActivity(crosspostIntent);
            });

            if (!mHidePostType) {
                typeTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(mActivity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                    intent.putExtra(FilteredPostsActivity.EXTRA_FILTER, mPost.getPostType());
                    mActivity.startActivity(intent);
                });
            } else {
                typeTextView.setVisibility(View.GONE);
            }

            if (!mHidePostFlair) {
                flairTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(mActivity, FilteredPostsActivity.class);
                    intent.putExtra(FilteredPostsActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
                    intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                    intent.putExtra(FilteredPostsActivity.EXTRA_CONTAIN_FLAIR, mPost.getFlair());
                    mActivity.startActivity(intent);
                });
            } else {
                flairTextView.setVisibility(View.GONE);
            }

            nSFWTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, FilteredPostsActivity.class);
                intent.putExtra(FilteredPostsActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
                intent.putExtra(FilteredPostsActivity.EXTRA_POST_TYPE, PostPagingSource.TYPE_SUBREDDIT);
                intent.putExtra(FilteredPostsActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                mActivity.startActivity(intent);
            });

            contentMarkdownView.setLayoutManager(new SwipeLockLinearLayoutManager(mActivity, new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    ((ViewPostDetailActivity) mActivity).lockSwipeRightToGoBack();
                }

                @Override
                public void unlockSwipe() {
                    ((ViewPostDetailActivity) mActivity).unlockSwipeRightToGoBack();
                }
            }));

            upvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int previousUpvoteButtonTextColor = upvoteButton.getCurrentTextColor();
                int previousDownvoteButtonTextColor = downvoteButton.getCurrentTextColor();
                Drawable previousUpvoteButtonDrawable = upvoteButton.getIcon();
                Drawable previousDownvoteButtonDrawable = downvoteButton.getIcon();

                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

                if (previousVoteType != 1) {
                    //Not upvoted before
                    mPost.setVoteType(1);
                    newVoteType = APIUtils.DIR_UPVOTE;
                    upvoteButton.setTextColor(mUpvotedColor);
                    upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                    upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                } else {
                    //Upvoted before
                    mPost.setVoteType(0);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    upvoteButton.setTextColor(mPostIconAndInfoColor);
                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                    upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                }

                if (!mHideTheNumberOfVotes) {
                    upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            mPost.getScore() + mPost.getVoteType()));
                }

                mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                            mPost.setVoteType(1);
                            upvoteButton.setTextColor(mUpvotedColor);
                            upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                            upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                        } else {
                            mPost.setVoteType(0);
                            upvoteButton.setTextColor(mPostIconAndInfoColor);
                            upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                            upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                        }

                        downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                        if (!mHideTheNumberOfVotes) {
                            upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    mPost.getScore() + mPost.getVoteType()));
                        }

                        mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        if (!mHideTheNumberOfVotes) {
                            upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    mPost.getScore() + previousVoteType));
                        }
                        upvoteButton.setTextColor(previousUpvoteButtonTextColor);
                        upvoteButton.setIcon(previousUpvoteButtonDrawable);
                        upvoteButton.setIconTint(ColorStateList.valueOf(previousUpvoteButtonTextColor));
                        downvoteButton.setIcon(previousDownvoteButtonDrawable);
                        downvoteButton.setIconTint(ColorStateList.valueOf(previousDownvoteButtonTextColor));

                        mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });

            downvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int previousUpvoteButtonTextColor = upvoteButton.getTextColors().getDefaultColor();
                int previousDownvoteButtonTextColor = downvoteButton.getTextColors().getDefaultColor();
                Drawable previousUpvoteButtonDrawable = upvoteButton.getIcon();
                Drawable previousDownvoteButtonDrawable = downvoteButton.getIcon();

                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                upvoteButton.setTextColor(mPostIconAndInfoColor);
                upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));

                if (previousVoteType != -1) {
                    //Not downvoted before
                    mPost.setVoteType(-1);
                    newVoteType = APIUtils.DIR_DOWNVOTE;
                    downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                    downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                } else {
                    //Downvoted before
                    mPost.setVoteType(0);
                    newVoteType = APIUtils.DIR_UNVOTE;
                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                    downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                }

                if (!mHideTheNumberOfVotes) {
                    upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            mPost.getScore() + mPost.getVoteType()));
                }

                mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                            mPost.setVoteType(-1);
                            downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                            downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                        } else {
                            mPost.setVoteType(0);
                            downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                            downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                        }

                        upvoteButton.setTextColor(mPostIconAndInfoColor);
                        upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
                        if (!mHideTheNumberOfVotes) {
                            upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    mPost.getScore() + mPost.getVoteType()));
                        }

                        mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        if (!mHideTheNumberOfVotes) {
                            upvoteButton.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    mPost.getScore() + previousVoteType));
                        }
                        upvoteButton.setTextColor(previousUpvoteButtonTextColor);
                        upvoteButton.setIcon(previousUpvoteButtonDrawable);
                        upvoteButton.setIconTint(ColorStateList.valueOf(previousUpvoteButtonTextColor));
                        downvoteButton.setIcon(previousDownvoteButtonDrawable);
                        downvoteButton.setIconTint(ColorStateList.valueOf(previousDownvoteButtonTextColor));

                        mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });

            if (!mHideTheNumberOfComments) {
                this.commentsCountButton.setOnClickListener(view -> {
                    if (mPost.isArchived()) {
                        Toast.makeText(mActivity, R.string.archived_post_comment_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mPost.isLocked()) {
                        Toast.makeText(mActivity, R.string.locked_post_comment_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(mActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TITLE_KEY, mPost.getTitle());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, mPost.getSelfText());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, mPost.getSelfTextPlain());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                    mActivity.startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
                });
            } else {
                this.commentsCountButton.setVisibility(View.GONE);
            }

            this.saveButton.setOnClickListener(view -> {
                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isSaved()) {
                    this.saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                    SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    mPost.setSaved(false);
                                    PostDetailBaseViewHolder.this.saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                    mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                                }

                                @Override
                                public void failed() {
                                    mPost.setSaved(true);
                                    PostDetailBaseViewHolder.this.saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                    mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                                }
                            });
                } else {
                    this.saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                    SaveThing.saveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                            new SaveThing.SaveThingListener() {
                                @Override
                                public void success() {
                                    mPost.setSaved(true);
                                    PostDetailBaseViewHolder.this.saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                    mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                                }

                                @Override
                                public void failed() {
                                    mPost.setSaved(false);
                                    PostDetailBaseViewHolder.this.saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                                    Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                    mPostDetailRecyclerViewAdapterCallback.updatePost(mPost);
                                }
                            });
                }
            });

            this.shareButton.setOnClickListener(view -> {
                Bundle bundle = new Bundle();
                bundle.putString(ShareLinkBottomSheetFragment.EXTRA_POST_LINK, mPost.getPermalink());
                if (mPost.getPostType() != Post.TEXT_TYPE) {
                    bundle.putInt(ShareLinkBottomSheetFragment.EXTRA_MEDIA_TYPE, mPost.getPostType());
                    switch (mPost.getPostType()) {
                        case Post.IMAGE_TYPE:
                        case Post.GIF_TYPE:
                        case Post.LINK_TYPE:
                        case Post.NO_PREVIEW_LINK_TYPE:
                            bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getUrl());
                            break;
                        case Post.VIDEO_TYPE:
                            bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, mPost.getVideoDownloadUrl());
                            break;
                    }
                }
                ShareLinkBottomSheetFragment shareLinkBottomSheetFragment = new ShareLinkBottomSheetFragment();
                shareLinkBottomSheetFragment.setArguments(bundle);
                shareLinkBottomSheetFragment.show(mActivity.getSupportFragmentManager(), shareLinkBottomSheetFragment.getTag());
            });

            this.shareButton.setOnLongClickListener(view -> {
                mActivity.copyLink(mPost.getPermalink());
                return true;
            });

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(voteButtonToggleGroup.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(voteButtonToggleGroup.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountButton.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountButton.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            if (mActivity.typeface != null) {
                subredditTextView.setTypeface(mActivity.typeface);
                userTextView.setTypeface(mActivity.typeface);
                authorFlairTextView.setTypeface(mActivity.typeface);
                postTimeTextView.setTypeface(mActivity.typeface);
                typeTextView.setTypeface(mActivity.typeface);
                spoilerTextView.setTypeface(mActivity.typeface);
                nSFWTextView.setTypeface(mActivity.typeface);
                flairTextView.setTypeface(mActivity.typeface);
                awardsTextView.setTypeface(mActivity.typeface);
                upvoteRatioTextView.setTypeface(mActivity.typeface);
                upvoteButton.setTypeface(mActivity.typeface);
                commentsCountButton.setTypeface(mActivity.typeface);
            }
            if (mActivity.titleTypeface != null) {
                titleTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCardViewColor);
            subredditTextView.setTextColor(mSubredditColor);
            userTextView.setTextColor(mUsernameColor);
            authorFlairTextView.setTextColor(mAuthorFlairTextColor);
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            typeTextView.setBorderColor(mPostTypeBackgroundColor);
            typeTextView.setTextColor(mPostTypeTextColor);
            spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            spoilerTextView.setTextColor(mSpoilerTextColor);
            nSFWTextView.setBackgroundColor(mNSFWBackgroundColor);
            nSFWTextView.setBorderColor(mNSFWBackgroundColor);
            nSFWTextView.setTextColor(mNSFWTextColor);
            flairTextView.setBackgroundColor(mFlairBackgroundColor);
            flairTextView.setBorderColor(mFlairBackgroundColor);
            flairTextView.setTextColor(mFlairTextColor);
            archivedImageView.setColorFilter(mArchivedTintColor, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedTintColor, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostTintColor, PorterDuff.Mode.SRC_IN);
            awardsTextView.setTextColor(mSecondaryTextColor);
            Drawable upvoteRatioDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_upvote_ratio, mUpvoteRatioTintColor);
            upvoteRatioTextView.setCompoundDrawablesWithIntrinsicBounds(
                    upvoteRatioDrawable, null, null, null);
            upvoteRatioTextView.setTextColor(mSecondaryTextColor);
            upvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            upvoteButton.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            commentsCountButton.setTextColor(mPostIconAndInfoColor);
            commentsCountButton.setIcon(mCommentIcon);
            saveButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
            shareButton.setIconTint(ColorStateList.valueOf(mPostIconAndInfoColor));
        }
    }

    class PostDetailBaseVideoAutoplayViewHolder extends PostDetailBaseViewHolder implements ToroPlayer {
        public Call<String> fetchGfycatOrStreamableVideoCall;
        AspectRatioGifImageView mIconGifImageView;
        TextView mSubredditTextView;
        TextView mUserTextView;
        TextView mAuthorFlairTextView;
        TextView mPostTimeTextView;
        TextView mTitleTextView;
        CustomTextView mTypeTextView;
        ImageView mCrosspostImageView;
        ImageView mArchivedImageView;
        ImageView mLockedImageView;
        CustomTextView mNSFWTextView;
        CustomTextView mSpoilerTextView;
        CustomTextView mFlairTextView;
        TextView mAwardsTextView;
        TextView mUpvoteRatioTextView;
        AspectRatioFrameLayout aspectRatioFrameLayout;
        PlayerView playerView;
        GifImageView previewImageView;
        ImageView mErrorLoadingGfycatImageView;
        ImageView muteButton;
        ImageView fullscreenButton;
        ImageView pauseButton;
        ImageView playButton;
        DefaultTimeBar progressBar;
        RecyclerView mContentMarkdownView;
        ConstraintLayout mBottomConstraintLayout;
        MaterialButton mUpvoteButton;
        MaterialButton mDownvoteButton;
        MaterialButton commentsCountButton;
        MaterialButton mSaveButton;
        MaterialButton mShareButton;
        @Nullable
        Container container;
        @Nullable
        ExoPlayerViewHelper helper;
        private Uri mediaUri;
        private float volume;
        private boolean isManuallyPaused;

        public PostDetailBaseVideoAutoplayViewHolder(@NonNull View itemView,
                                                     AspectRatioGifImageView iconGifImageView,
                                                     TextView subredditTextView,
                                                     TextView userTextView,
                                                     TextView authorFlairTextView,
                                                     TextView postTimeTextView,
                                                     TextView titleTextView,
                                                     CustomTextView typeTextView,
                                                     ImageView crosspostImageView,
                                                     ImageView archivedImageView,
                                                     ImageView lockedImageView,
                                                     CustomTextView nsfwTextView,
                                                     CustomTextView spoilerTextView,
                                                     CustomTextView flairTextView,
                                                     TextView awardsTextView,
                                                     TextView upvoteRatioTextView,
                                                     AspectRatioFrameLayout aspectRatioFrameLayout,
                                                     PlayerView playerView,
                                                     GifImageView previewImageView,
                                                     ImageView errorLoadingGfycatImageView,
                                                     ImageView muteButton,
                                                     ImageView fullscreenButton,
                                                     ImageView pauseButton,
                                                     ImageView playButton,
                                                     DefaultTimeBar progressBar,
                                                     RecyclerView contentMarkdownView,
                                                     ConstraintLayout bottomConstraintLayout,
                                                     MaterialButtonToggleGroup voteButtonToggleGroup,
                                                     MaterialButton upvoteButton,
                                                     MaterialButton downvoteButton,
                                                     MaterialButton commentsCountButton,
                                                     MaterialButton saveButton,
                                                     MaterialButton shareButton) {
            super(itemView);
            setBaseView(iconGifImageView,
                    subredditTextView,
                    userTextView,
                    authorFlairTextView,
                    postTimeTextView,
                    titleTextView,
                    typeTextView,
                    crosspostImageView,
                    archivedImageView,
                    lockedImageView,
                    nsfwTextView,
                    spoilerTextView,
                    flairTextView,
                    awardsTextView,
                    upvoteRatioTextView,
                    contentMarkdownView,
                    bottomConstraintLayout,
                    voteButtonToggleGroup,
                    upvoteButton,
                    downvoteButton,
                    commentsCountButton,
                    saveButton,
                    shareButton);

            this.aspectRatioFrameLayout = aspectRatioFrameLayout;
            this.previewImageView = previewImageView;
            this.mErrorLoadingGfycatImageView = errorLoadingGfycatImageView;
            this.playerView = playerView;
            this.muteButton = muteButton;
            this.fullscreenButton = fullscreenButton;
            this.pauseButton = pauseButton;
            this.playButton = playButton;
            this.progressBar = progressBar;

            aspectRatioFrameLayout.setOnClickListener(null);

            muteButton.setOnClickListener(view -> {
                if (helper != null) {
                    if (helper.getVolume() != 0) {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_24dp));
                        helper.setVolume(0f);
                        volume = 0f;
                    } else {
                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_24dp));
                        helper.setVolume(1f);
                        volume = 1f;
                    }
                }
            });

            fullscreenButton.setOnClickListener(view -> {
                if (canStartActivity) {
                    canStartActivity = false;
                    Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                    if (mPost.isImgur()) {
                        intent.setData(Uri.parse(mPost.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                    } else if (mPost.isGfycat()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                        intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        if (mPost.isLoadGfycatOrStreamableVideoSuccess()) {
                            intent.setData(Uri.parse(mPost.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                        }
                    } else if (mPost.isRedgifs()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                        intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        if (mPost.isLoadGfycatOrStreamableVideoSuccess()) {
                            intent.setData(Uri.parse(mPost.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                        }
                    } else if (mPost.isStreamable()) {
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                        intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, mPost.getStreamableShortCode());
                    } else {
                        intent.setData(Uri.parse(mPost.getVideoUrl()));
                        intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                        intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, mPost.getSubredditName());
                        intent.putExtra(ViewVideoActivity.EXTRA_ID, mPost.getId());
                    }
                    intent.putExtra(ViewVideoActivity.EXTRA_POST, mPost);
                    if (helper != null) {
                        intent.putExtra(ViewVideoActivity.EXTRA_PROGRESS_SECONDS, helper.getLatestPlaybackInfo().getResumePosition());
                    }
                    intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                    mActivity.startActivity(intent);
                }
            });

            pauseButton.setOnClickListener(view -> {
                pause();
                isManuallyPaused = true;
                savePlaybackInfo(getPlayerOrder(), getCurrentPlaybackInfo());
            });

            playButton.setOnClickListener(view -> {
                isManuallyPaused = false;
                play();
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
            playerView.setOnClickListener(view -> {
                if (mEasierToWatchInFullScreen && playerView.isControllerVisible()) {
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

        @NonNull
        @Override
        public View getPlayerView() {
            return playerView;
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
            if (this.container == null) {
                this.container = container;
            }
            if (helper == null) {
                helper = new ExoPlayerViewHelper(this, mediaUri, null, mExoCreator);
                helper.addEventListener(new Playable.DefaultEventListener() {
                    @Override
                    public void onTracksChanged(@NonNull Tracks tracks) {
                        ImmutableList<Tracks.Group> trackGroups = tracks.getGroups();
                        if (!trackGroups.isEmpty()) {
                            for (int i = 0; i < trackGroups.size(); i++) {
                                String mimeType = trackGroups.get(i).getTrackFormat(0).sampleMimeType;
                                if (mimeType != null && mimeType.contains("audio")) {
                                    helper.setVolume(volume);
                                    muteButton.setVisibility(View.VISIBLE);
                                    if (volume != 0f) {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_unmute_24dp));
                                    } else {
                                        muteButton.setImageDrawable(mActivity.getDrawable(R.drawable.ic_mute_24dp));
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
            container = null;
        }

        @Override
        public boolean wantsToPlay() {
            return canPlayVideo && mediaUri != null && ToroUtil.visibleAreaOffset(this, itemView.getParent()) >= mStartAutoplayVisibleAreaOffset;
        }

        @Override
        public int getPlayerOrder() {
            return 0;
        }
    }

    class PostDetailVideoAutoplayViewHolder extends PostDetailBaseVideoAutoplayViewHolder {
        PostDetailVideoAutoplayViewHolder(@NonNull ItemPostDetailVideoAutoplayBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostDetailVideoAutoplay,
                    binding.subredditTextViewItemPostDetailVideoAutoplay,
                    binding.userTextViewItemPostDetailVideoAutoplay,
                    binding.authorFlairTextViewItemPostDetailVideoAutoplay,
                    binding.postTimeTextViewItemPostDetailVideoAutoplay,
                    binding.titleTextViewItemPostDetailVideoAutoplay,
                    binding.typeTextViewItemPostDetailVideoAutoplay,
                    binding.crosspostImageViewItemPostDetailVideoAutoplay,
                    binding.archivedImageViewItemPostDetailVideoAutoplay,
                    binding.lockedImageViewItemPostDetailVideoAutoplay,
                    binding.nsfwTextViewItemPostDetailVideoAutoplay,
                    binding.spoilerCustomTextViewItemPostDetailVideoAutoplay,
                    binding.flairCustomTextViewItemPostDetailVideoAutoplay,
                    binding.awardsTextViewItemPostDetailVideoAutoplay,
                    binding.upvoteRatioTextViewItemPostDetailVideoAutoplay,
                    binding.aspectRatioFrameLayoutItemPostDetailVideoAutoplay,
                    binding.playerViewItemPostDetailVideoAutoplay,
                    binding.previewImageViewItemPostDetailVideoAutoplay,
                    binding.errorLoadingGfycatImageViewItemPostDetailVideoAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_pause),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.contentMarkdownViewItemPostDetailVideoAutoplay,
                    binding.bottomConstraintLayoutItemPostDetailVideoAutoplay,
                    binding.voteButtonToggleItemPostDetailVideoAutoplay,
                    binding.upvoteButtonItemPostDetailVideoAutoplay,
                    binding.downvoteButtonItemPostDetailVideoAutoplay,
                    binding.commentsCountButtonItemPostDetailVideoAutoplay,
                    binding.saveButtonItemPostDetailVideoAutoplay,
                    binding.shareButtonItemPostDetailVideoAutoplay);
        }
    }

    class PostDetailVideoAutoplayLegacyControllerViewHolder extends PostDetailBaseVideoAutoplayViewHolder {
        PostDetailVideoAutoplayLegacyControllerViewHolder(ItemPostDetailVideoAutoplayLegacyControllerBinding binding) {
            super(binding.getRoot(),
                    binding.iconGifImageViewItemPostDetailVideoAutoplay,
                    binding.subredditTextViewItemPostDetailVideoAutoplay,
                    binding.userTextViewItemPostDetailVideoAutoplay,
                    binding.authorFlairTextViewItemPostDetailVideoAutoplay,
                    binding.postTimeTextViewItemPostDetailVideoAutoplay,
                    binding.titleTextViewItemPostDetailVideoAutoplay,
                    binding.typeTextViewItemPostDetailVideoAutoplay,
                    binding.crosspostImageViewItemPostDetailVideoAutoplay,
                    binding.archivedImageViewItemPostDetailVideoAutoplay,
                    binding.lockedImageViewItemPostDetailVideoAutoplay,
                    binding.nsfwTextViewItemPostDetailVideoAutoplay,
                    binding.spoilerCustomTextViewItemPostDetailVideoAutoplay,
                    binding.flairCustomTextViewItemPostDetailVideoAutoplay,
                    binding.awardsTextViewItemPostDetailVideoAutoplay,
                    binding.upvoteRatioTextViewItemPostDetailVideoAutoplay,
                    binding.aspectRatioFrameLayoutItemPostDetailVideoAutoplay,
                    binding.playerViewItemPostDetailVideoAutoplay,
                    binding.previewImageViewItemPostDetailVideoAutoplay,
                    binding.errorLoadingGfycatImageViewItemPostDetailVideoAutoplay,
                    binding.getRoot().findViewById(R.id.mute_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.fullscreen_exo_playback_control_view),
                    binding.getRoot().findViewById(R.id.exo_pause),
                    binding.getRoot().findViewById(R.id.exo_play),
                    binding.getRoot().findViewById(R.id.exo_progress),
                    binding.contentMarkdownViewItemPostDetailVideoAutoplay,
                    binding.bottomConstraintLayoutItemPostDetailVideoAutoplay,
                    binding.voteButtonToggleItemPostDetailVideoAutoplay,
                    binding.upvoteButtonItemPostDetailVideoAutoplay,
                    binding.downvoteButtonItemPostDetailVideoAutoplay,
                    binding.commentsCountButtonItemPostDetailVideoAutoplay,
                    binding.saveButtonItemPostDetailVideoAutoplay,
                    binding.shareButtonItemPostDetailVideoAutoplay);
        }
    }

    class PostDetailVideoAndGifPreviewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailVideoAndGifPreviewBinding binding;

        PostDetailVideoAndGifPreviewHolder(@NonNull ItemPostDetailVideoAndGifPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailVideoAndGifPreview,
                    binding.subredditTextViewItemPostDetailVideoAndGifPreview,
                    binding.userTextViewItemPostDetailVideoAndGifPreview,
                    binding.authorFlairTextViewItemPostDetailVideoAndGifPreview,
                    binding.postTimeTextViewItemPostDetailVideoAndGifPreview,
                    binding.titleTextViewItemPostDetailVideoAndGifPreview,
                    binding.typeTextViewItemPostDetailVideoAndGifPreview,
                    binding.crosspostImageViewItemPostDetailVideoAndGifPreview,
                    binding.archivedImageViewItemPostDetailVideoAndGifPreview,
                    binding.lockedImageViewItemPostDetailVideoAndGifPreview,
                    binding.nsfwTextViewItemPostDetailVideoAndGifPreview,
                    binding.spoilerCustomTextViewItemPostDetailVideoAndGifPreview,
                    binding.flairCustomTextViewItemPostDetailVideoAndGifPreview,
                    binding.awardsTextViewItemPostDetailVideoAndGifPreview,
                    binding.upvoteRatioTextViewItemPostDetailVideoAndGifPreview,
                    binding.contentMarkdownViewItemPostDetailVideoAndGifPreview,
                    binding.bottomConstraintLayoutItemPostDetailVideoAndGifPreview,
                    binding.voteButtonToggleItemPostDetailVideoAndGifPreview,
                    binding.upvoteButtonItemPostDetailVideoAndGifPreview,
                    binding.downvoteButtonItemPostDetailVideoAndGifPreview,
                    binding.commentsCountButtonItemPostDetailVideoAndGifPreview,
                    binding.saveButtonItemPostDetailVideoAndGifPreview,
                    binding.shareButtonItemPostDetailVideoAndGifPreview);

            binding.videoOrGifIndicatorImageViewItemPostDetail.setColorFilter(mMediaIndicatorIconTint, PorterDuff.Mode.SRC_IN);
            binding.videoOrGifIndicatorImageViewItemPostDetail.setBackgroundTintList(ColorStateList.valueOf(mMediaIndicatorBackgroundColor));
            binding.progressBarItemPostDetailVideoAndGifPreview.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            binding.loadImageErrorTextViewItemPostDetailVideoAndGifPreview.setTextColor(mPrimaryTextColor);

            binding.imageViewItemPostDetailVideoAndGifPreview.setOnClickListener(view -> {
                if (canStartActivity) {
                    canStartActivity = false;
                    if (mPost.getPostType() == Post.VIDEO_TYPE) {
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        if (mPost.isImgur()) {
                            intent.setData(Uri.parse(mPost.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_IMGUR);
                        } else if (mPost.isGfycat()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        } else if (mPost.isRedgifs()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        } else if (mPost.isStreamable()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                            intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, mPost.getStreamableShortCode());
                        } else {
                            intent.setData(Uri.parse(mPost.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, mPost.getSubredditName());
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, mPost.getId());
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                        }
                        intent.putExtra(ViewVideoActivity.EXTRA_POST, mPost);
                        intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.GIF_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mPost.getSubredditName()
                                + "-" + mPost.getId() + ".gif");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mPost.getVideoUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }

    class PostDetailImageAndGifAutoplayViewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailImageAndGifAutoplayBinding binding;

        PostDetailImageAndGifAutoplayViewHolder(@NonNull ItemPostDetailImageAndGifAutoplayBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailImageAndGifAutoplay,
                    binding.subredditTextViewItemPostDetailImageAndGifAutoplay,
                    binding.userTextViewItemPostDetailImageAndGifAutoplay,
                    binding.authorFlairTextViewItemPostDetailImageAndGifAutoplay,
                    binding.postTimeTextViewItemPostDetailImageAndGifAutoplay,
                    binding.titleTextViewItemPostDetailImageAndGifAutoplay,
                    binding.typeTextViewItemPostDetailImageAndGifAutoplay,
                    binding.crosspostImageViewItemPostDetailImageAndGifAutoplay,
                    binding.archivedImageViewItemPostDetailImageAndGifAutoplay,
                    binding.lockedImageViewItemPostDetailImageAndGifAutoplay,
                    binding.nsfwTextViewItemPostDetailImageAndGifAutoplay,
                    binding.spoilerCustomTextViewItemPostDetailImageAndGifAutoplay,
                    binding.flairCustomTextViewItemPostDetailImageAndGifAutoplay,
                    binding.awardsTextViewItemPostDetailImageAndGifAutoplay,
                    binding.upvoteRatioTextViewItemPostDetailImageAndGifAutoplay,
                    binding.contentMarkdownViewItemPostDetailImageAndGifAutoplay,
                    binding.bottomConstraintLayoutItemPostDetailImageAndGifAutoplay,
                    binding.voteButtonToggleItemPostDetailImageAndGifAutoplay,
                    binding.upvoteButtonItemPostDetailImageAndGifAutoplay,
                    binding.downvoteButtonItemPostDetailImageAndGifAutoplay,
                    binding.commentsCountButtonItemPostDetailImageAndGifAutoplay,
                    binding.saveButtonItemPostDetailImageAndGifAutoplay,
                    binding.shareButtonItemPostDetailImageAndGifAutoplay);

            binding.progressBarItemPostDetailImageAndGifAutoplay.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            binding.loadImageErrorTextViewItemPostDetailImageAndGifAutoplay.setTextColor(mPrimaryTextColor);

            binding.imageViewItemPostDetailImageAndGifAutoplay.setOnClickListener(view -> {
                if (canStartActivity) {
                    canStartActivity = false;
                    if (mPost.getPostType() == Post.IMAGE_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mPost.getUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mPost.getSubredditNamePrefixed().substring(2)
                                + "-" + mPost.getId().substring(3) + ".jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.GIF_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mPost.getSubredditName()
                                + "-" + mPost.getId() + ".gif");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mPost.getVideoUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }

    class PostDetailLinkViewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailLinkBinding binding;

        PostDetailLinkViewHolder(@NonNull ItemPostDetailLinkBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailLink,
                    binding.subredditTextViewItemPostDetailLink,
                    binding.userTextViewItemPostDetailLink,
                    binding.authorFlairTextViewItemPostDetailLink,
                    binding.postTimeTextViewItemPostDetailLink,
                    binding.titleTextViewItemPostDetailLink,
                    binding.typeTextViewItemPostDetailLink,
                    binding.crosspostImageViewItemPostDetailLink,
                    binding.archivedImageViewItemPostDetailLink,
                    binding.lockedImageViewItemPostDetailLink,
                    binding.nsfwTextViewItemPostDetailLink,
                    binding.spoilerCustomTextViewItemPostDetailLink,
                    binding.flairCustomTextViewItemPostDetailLink,
                    binding.awardsTextViewItemPostDetailLink,
                    binding.upvoteRatioTextViewItemPostDetailLink,
                    binding.contentMarkdownViewItemPostDetailLink,
                    binding.bottomConstraintLayoutItemPostDetailLink,
                    binding.voteButtonToggleItemPostDetailLink,
                    binding.upvoteButtonItemPostDetailLink,
                    binding.downvoteButtonItemPostDetailLink,
                    binding.commentsCountButtonItemPostDetailLink,
                    binding.saveButtonItemPostDetailLink,
                    binding.shareButtonItemPostDetailLink);

            if (mActivity.typeface != null) {
                binding.linkTextViewItemPostDetailLink.setTypeface(mActivity.typeface);
            }
            binding.linkTextViewItemPostDetailLink.setTextColor(mSecondaryTextColor);
            binding.progressBarItemPostDetailLink.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            binding.loadImageErrorTextViewItemPostDetailLink.setTextColor(mPrimaryTextColor);

            binding.imageViewItemPostDetailLink.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                Uri uri = Uri.parse(mPost.getUrl());
                intent.setData(uri);
                intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                mActivity.startActivity(intent);
            });
        }
    }

    class PostDetailNoPreviewViewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailNoPreviewBinding binding;

        PostDetailNoPreviewViewHolder(@NonNull ItemPostDetailNoPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailNoPreview,
                    binding.subredditTextViewItemPostDetailNoPreview,
                    binding.userTextViewItemPostDetailNoPreview,
                    binding.authorFlairTextViewItemPostDetailNoPreview,
                    binding.postTimeTextViewItemPostDetailNoPreview,
                    binding.titleTextViewItemPostDetailNoPreview,
                    binding.typeTextViewItemPostDetailNoPreview,
                    binding.crosspostImageViewItemPostDetailNoPreview,
                    binding.archivedImageViewItemPostDetailNoPreview,
                    binding.lockedImageViewItemPostDetailNoPreview,
                    binding.nsfwTextViewItemPostDetailNoPreview,
                    binding.spoilerCustomTextViewItemPostDetailNoPreview,
                    binding.flairCustomTextViewItemPostDetailNoPreview,
                    binding.awardsTextViewItemPostDetailNoPreview,
                    binding.upvoteRatioTextViewItemPostDetailNoPreview,
                    binding.contentMarkdownViewItemPostDetailNoPreview,
                    binding.bottomConstraintLayoutItemPostDetailNoPreview,
                    binding.voteButtonToggleItemPostDetailNoPreview,
                    binding.upvoteButtonItemPostDetailNoPreview,
                    binding.downvoteButtonItemPostDetailNoPreview,
                    binding.commentsCountButtonItemPostDetailNoPreview,
                    binding.saveButtonItemPostDetailNoPreview,
                    binding.shareButtonItemPostDetailNoPreview);

            if (mActivity.typeface != null) {
                binding.linkTextViewItemPostDetailNoPreview.setTypeface(mActivity.typeface);
            }
            binding.linkTextViewItemPostDetailNoPreview.setTextColor(mSecondaryTextColor);
            binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setColorFilter(mNoPreviewPostTypeIconTint, PorterDuff.Mode.SRC_IN);

            binding.imageViewNoPreviewPostTypeItemPostDetailNoPreview.setOnClickListener(view -> {
                if (mPost != null) {
                    if (mPost.getPostType() == Post.VIDEO_TYPE) {
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        if (mPost.isGfycat()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_GFYCAT);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        } else if (mPost.isRedgifs()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_REDGIFS);
                            intent.putExtra(ViewVideoActivity.EXTRA_GFYCAT_ID, mPost.getGfycatId());
                        } else if (mPost.isStreamable()) {
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_STREAMABLE);
                            intent.putExtra(ViewVideoActivity.EXTRA_STREAMABLE_SHORT_CODE, mPost.getStreamableShortCode());
                        } else {
                            intent.setData(Uri.parse(mPost.getVideoUrl()));
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, mPost.getSubredditName());
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, mPost.getId());
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, mPost.getVideoDownloadUrl());
                        }
                        intent.putExtra(ViewVideoActivity.EXTRA_POST, mPost);
                        intent.putExtra(ViewVideoActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.IMAGE_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mPost.getUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mPost.getSubredditName()
                                + "-" + mPost.getId() + ".jpg");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.GIF_TYPE) {
                        Intent intent = new Intent(mActivity, ViewImageOrGifActivity.class);
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mPost.getSubredditName()
                                + "-" + mPost.getId() + ".gif");
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mPost.getVideoUrl());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_POST_TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.LINK_TYPE || mPost.getPostType() == Post.NO_PREVIEW_LINK_TYPE) {
                        Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                        Uri uri = Uri.parse(mPost.getUrl());
                        intent.setData(uri);
                        intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
                        mActivity.startActivity(intent);
                    } else if (mPost.getPostType() == Post.GALLERY_TYPE) {
                        Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                        intent.putParcelableArrayListExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, mPost.getGallery());
                        intent.putExtra(ViewRedditGalleryActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
                        mActivity.startActivity(intent);
                    }
                }
            });
        }
    }

    class PostDetailGalleryViewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailGalleryBinding binding;
        PostGalleryTypeImageRecyclerViewAdapter adapter;

        PostDetailGalleryViewHolder(@NonNull ItemPostDetailGalleryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailGallery,
                    binding.subredditTextViewItemPostDetailGallery,
                    binding.userTextViewItemPostDetailGallery,
                    binding.authorFlairTextViewItemPostDetailGallery,
                    binding.postTimeTextViewItemPostDetailGallery,
                    binding.titleTextViewItemPostDetailGallery,
                    binding.typeTextViewItemPostDetailGallery,
                    binding.crosspostImageViewItemPostDetailGallery,
                    binding.archivedImageViewItemPostDetailGallery,
                    binding.lockedImageViewItemPostDetailGallery,
                    binding.nsfwTextViewItemPostDetailGallery,
                    binding.spoilerCustomTextViewItemPostDetailGallery,
                    binding.flairCustomTextViewItemPostDetailGallery,
                    binding.awardsTextViewItemPostDetailGallery,
                    binding.upvoteRatioTextViewItemPostDetailGallery,
                    binding.contentMarkdownViewItemPostDetailGallery,
                    binding.bottomConstraintLayoutItemPostDetailGallery,
                    binding.voteButtonToggleItemPostDetailGallery,
                    binding.upvoteButtonItemPostDetailGallery,
                    binding.downvoteButtonItemPostDetailGallery,
                    binding.commentsCountButtonItemPostDetailGallery,
                    binding.saveButtonItemPostDetailGallery,
                    binding.shareButtonItemPostDetailGallery);

            if (mActivity.typeface != null) {
                binding.imageIndexTextViewItemPostDetailGallery.setTypeface(mActivity.typeface);
            }

            binding.imageIndexTextViewItemPostDetailGallery.setTextColor(mMediaIndicatorIconTint);
            binding.imageIndexTextViewItemPostDetailGallery.setBackgroundColor(mMediaIndicatorBackgroundColor);
            binding.imageIndexTextViewItemPostDetailGallery.setBorderColor(mMediaIndicatorBackgroundColor);
            binding.noPreviewPostTypeImageViewItemPostDetailGallery.setBackgroundColor(mNoPreviewPostTypeBackgroundColor);
            binding.noPreviewPostTypeImageViewItemPostDetailGallery.setColorFilter(mNoPreviewPostTypeIconTint, PorterDuff.Mode.SRC_IN);

            adapter = new PostGalleryTypeImageRecyclerViewAdapter(mGlide, mActivity.typeface, mPostDetailMarkwon,
                    mSaveMemoryCenterInsideDownsampleStrategy, mColorAccent, mPrimaryTextColor,
                    mCardViewColor, mCommentColor, mScale);
            binding.galleryRecyclerViewItemPostDetailGallery.setAdapter(adapter);
            new PagerSnapHelper().attachToRecyclerView(binding.galleryRecyclerViewItemPostDetailGallery);
            binding.galleryRecyclerViewItemPostDetailGallery.setOnTouchListener((v, motionEvent) -> {
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
            LinearLayoutManagerBugFixed layoutManager = new LinearLayoutManagerBugFixed(mActivity, RecyclerView.HORIZONTAL, false);
            binding.galleryRecyclerViewItemPostDetailGallery.setLayoutManager(layoutManager);
            binding.galleryRecyclerViewItemPostDetailGallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    binding.imageIndexTextViewItemPostDetailGallery.setText(mActivity.getString(R.string.image_index_in_gallery, layoutManager.findFirstVisibleItemPosition() + 1, mPost.getGallery().size()));
                }
            });
            binding.galleryRecyclerViewItemPostDetailGallery.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
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
                                    if (mPost != null) {
                                        Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                                        intent.putParcelableArrayListExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, mPost.getGallery());
                                        intent.putExtra(ViewRedditGalleryActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
                                        intent.putExtra(ViewRedditGalleryActivity.EXTRA_GALLERY_ITEM_INDEX, layoutManager.findFirstVisibleItemPosition());
                                        mActivity.startActivity(intent);
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

            binding.noPreviewPostTypeImageViewItemPostDetailGallery.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewRedditGalleryActivity.class);
                intent.putParcelableArrayListExtra(ViewRedditGalleryActivity.EXTRA_REDDIT_GALLERY, mPost.getGallery());
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
                intent.putExtra(ViewRedditGalleryActivity.EXTRA_GALLERY_ITEM_INDEX, layoutManager.findFirstVisibleItemPosition());
                mActivity.startActivity(intent);
            });
        }
    }

    class PostDetailTextViewHolder extends PostDetailBaseViewHolder {
        ItemPostDetailTextBinding binding;

        PostDetailTextViewHolder(@NonNull ItemPostDetailTextBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.iconGifImageViewItemPostDetailText,
                    binding.subredditTextViewItemPostDetailText,
                    binding.userTextViewItemPostDetailText,
                    binding.authorFlairTextViewItemPostDetailText,
                    binding.postTimeTextViewItemPostDetailText,
                    binding.titleTextViewItemPostDetailText,
                    binding.typeTextViewItemPostDetailText,
                    binding.crosspostImageViewItemPostDetailText,
                    binding.archivedImageViewItemPostDetailText,
                    binding.lockedImageViewItemPostDetailText,
                    binding.nsfwTextViewItemPostDetailText,
                    binding.spoilerCustomTextViewItemPostDetailText,
                    binding.flairCustomTextViewItemPostDetailText,
                    binding.awardsTextViewItemPostDetailText,
                    binding.upvoteRatioTextViewItemPostDetailText,
                    binding.contentMarkdownViewItemPostDetailText,
                    binding.bottomConstraintLayoutItemPostDetailText,
                    binding.voteButtonToggleItemPostDetailText,
                    binding.upvoteButtonItemPostDetailText,
                    binding.downvoteButtonItemPostDetailText,
                    binding.commentsCountButtonItemPostDetailText,
                    binding.saveButtonItemPostDetailText,
                    binding.shareButtonItemPostDetailText);
        }
    }
}
