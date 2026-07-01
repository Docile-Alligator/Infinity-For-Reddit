package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CommentIndentationView;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFullyCollapsedBinding;
import ml.docilealligator.infinityforreddit.databinding.ItemLoadMoreCommentsPlaceholderBinding;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragmentNew;
import ml.docilealligator.infinityforreddit.markdown.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.markdown.emote.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.emote.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.imageandgif.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.imageandgif.ImageAndGifPlugin;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.markdown.video.VideoEntry;
import ml.docilealligator.infinityforreddit.markdown.video.VideoPlugin;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;
import ml.docilealligator.infinityforreddit.thing.SaveThing;
import ml.docilealligator.infinityforreddit.thing.VoteThing;
import ml.docilealligator.infinityforreddit.user.UserProfileImagesBatchLoader;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsRecyclerViewAdapterNew extends ListAdapter<Comment, RecyclerView.ViewHolder> {
    public static final int DIVIDER_NORMAL = 0;
    public static final int DIVIDER_PARENT = 1;

    private static final int VIEW_TYPE_COMMENT = 12;
    private static final int VIEW_TYPE_COMMENT_FULLY_COLLAPSED = 13;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 14;

    private final BaseActivity mActivity;
    private final ViewPostDetailFragmentNew mFragment;
    private final Retrofit mOauthRetrofit;
    private final EmoteCloseBracketInlineProcessor mEmoteCloseBracketInlineProcessor;
    private final EmotePlugin mEmotePlugin;
    private final ImageAndGifPlugin mImageAndGifPlugin;
    private final VideoPlugin mVideoPlugin;
    private final Markwon mCommentMarkwon;
    private final ImageAndGifEntry mImageAndGifEntry;
    private final VideoEntry mVideoEntry;
    private final String mAccessToken;
    private final String mAccountName;
    @Nullable
    private Post mPost;
    private final Locale mLocale;
    private final RequestManager mGlide;
    private final RecyclerView.RecycledViewPool recycledViewPool;
    private final String mSingleCommentId;
    private final boolean mVoteButtonsOnTheRight;
    private final boolean mShowElapsedTime;
    private final String mTimeFormatPattern;
    private final boolean mCommentToolbarHidden;
    private final boolean mCommentToolbarHideOnClick;
    private final boolean mSwapTapAndLong;
    private final boolean mShowCommentDivider;
    private final int mDividerType;
    private final boolean mShowAbsoluteNumberOfVotes;
    private final boolean mFullyCollapseComment;
    private final boolean mShowOnlyOneCommentLevelIndicator;
    private final boolean mShowAuthorAvatar;
    private final boolean mAlwaysShowChildCommentCount;
    private final boolean mHideTheNumberOfVotes;
    private final boolean mNeedBlurNsfw;
    private final boolean mDoNotBlurNsfwInNsfwSubreddits;
    private final boolean mNeedBlurSpoiler;
    private final int mDepthThreshold;
    private final CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private final Drawable expandDrawable;
    private final Drawable collapseDrawable;

    private final int mSecondaryTextColor;
    private final int mPrimaryTextColor;
    private final int mCommentTextColor;
    private final int mCommentBackgroundColor;
    private final int mDividerColor;
    private final int mUsernameColor;
    private final int mSubmitterColor;
    private final int mModeratorColor;
    private final int mCurrentUserColor;
    private final int mAuthorFlairTextColor;
    private final int mUpvotedColor;
    private final int mDownvotedColor;
    private final int mSingleCommentThreadBackgroundColor;
    private final int mVoteAndReplyUnavailableVoteButtonColor;
    private final int mCommentIconAndInfoColor;
    private final int mFullyCollapsedCommentBackgroundColor;
    private final int[] verticalBlockColors;

    private int mSearchedPosition = -1;

    private boolean canStartActivity = true;

    public static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(
                        @NonNull Comment oldComment, @NonNull Comment newComment) {
                    return Objects.equals(oldComment.getId(), newComment.getId());
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Comment oldComment, @NonNull Comment newComment) {
                    return Objects.equals(oldComment.getCommentMarkdown(), newComment.getCommentMarkdown())
                            && Objects.equals(oldComment.getApprovedBy(), newComment.getApprovedBy())
                            && Objects.equals(oldComment.getMoreChildrenIds(), newComment.getMoreChildrenIds())
                            && Objects.equals(oldComment.getMediaMetadataMap(), newComment.getMediaMetadataMap())
                            && oldComment.getVoteType() == newComment.getVoteType()
                            && oldComment.isExpanded() == newComment.isExpanded()
                            && oldComment.isAdmin() == newComment.isAdmin()
                            && oldComment.isEdited() == newComment.isEdited()
                            && oldComment.isLocked() == newComment.isLocked()
                            && oldComment.isRemoved() == newComment.isRemoved()
                            && oldComment.isApproved() == newComment.isApproved()
                            && oldComment.hasReply() == newComment.hasReply()
                            && oldComment.isSaved() == newComment.isSaved()
                            && oldComment.isSpam() == newComment.isSpam()
                            && oldComment.isLoadingMoreChildren() == newComment.isLoadingMoreChildren()
                            && oldComment.isLoadMoreChildrenFailed() == newComment.isLoadMoreChildrenFailed();
                }
            };

    public CommentsRecyclerViewAdapterNew(BaseActivity activity, ViewPostDetailFragmentNew fragment,
                                          CustomThemeWrapper customThemeWrapper,
                                          Retrofit oauthRetrofit,
                                          @Nullable String accessToken, @NonNull String accountName,
                                          @Nullable Post post, Locale locale, String singleCommentId,
                                          SharedPreferences sharedPreferences,
                                          SharedPreferences nsfwAndSpoilerSharedPreferences,
                                          CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        super(DIFF_CALLBACK);

        mActivity = activity;
        mFragment = fragment;
        mOauthRetrofit = oauthRetrofit;
        mAccessToken = accessToken;
        mAccountName = accountName;
        mGlide = Glide.with(activity);
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mCommentTextColor = customThemeWrapper.getCommentColor();
        int commentSpoilerBackgroundColor = mCommentTextColor | 0xFF000000;
        int linkColor = customThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (mActivity.contentTypeface != null) {
                    textView.setTypeface(mActivity.contentTypeface);
                }
                textView.setTextColor(mCommentTextColor);
                textView.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost != null && mPost.isNSFW());
                    mActivity.startActivity(intent);
                });
            }

            @Override
            public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                builder.linkColor(linkColor);
            }
        };
        EvenBetterLinkMovementMethod.OnLinkLongClickListener onLinkLongClickListener = (textView, url) -> {
            if (!activity.isDestroyed() && !activity.isFinishing()) {
                UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = UrlMenuBottomSheetFragment.newInstance(url);
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), null);
            }
            return true;
        };
        mEmoteCloseBracketInlineProcessor = new EmoteCloseBracketInlineProcessor();
        mEmotePlugin = EmotePlugin.create(activity,
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.EMBEDDED_MEDIA_TYPE, "15")),
                mediaMetadata -> {
                    Intent intent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, mPost != null && mPost.isNSFW());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost != null ? mPost.getSubredditName() : "Unknown");
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                    if (canStartActivity) {
                        canStartActivity = false;
                        activity.startActivity(intent);
                    }
                });
        mImageAndGifPlugin = new ImageAndGifPlugin();
        mVideoPlugin = new VideoPlugin();
        mCommentMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, mEmoteCloseBracketInlineProcessor, mEmotePlugin, mImageAndGifPlugin,
                mVideoPlugin, mCommentTextColor, commentSpoilerBackgroundColor, onLinkLongClickListener);

        mNeedBlurNsfw = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        mDoNotBlurNsfwInNsfwSubreddits = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        mNeedBlurSpoiler = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        mImageAndGifEntry = new ImageAndGifEntry(activity, mGlide, Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.EMBEDDED_MEDIA_TYPE, "15")), false,
                mediaMetadata -> {
                    Intent intent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, mPost != null && mPost.isNSFW());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, mPost != null ? mPost.getSubredditName() : "Unknown");
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                    if (canStartActivity) {
                        canStartActivity = false;
                        activity.startActivity(intent);
                    }
                });
        mVideoEntry = new VideoEntry(activity, Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.EMBEDDED_MEDIA_TYPE, "15")), new VideoEntry.OnItemClickListener() {
            @Override
            public void onItemClick(@org.jetbrains.annotations.Nullable MediaMetadata mediaMetadata) {
                if (canStartActivity) {
                    canStartActivity = false;
                    if (mediaMetadata == null) {
                        return;
                    }

                    Intent intent = new Intent(activity, ViewVideoActivity.class);
                    intent.setData(Uri.parse(mediaMetadata.original.url));
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_TYPE, ViewVideoActivity.VIDEO_TYPE_MARKDOWN_PARSED);
                    intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, MediaMetadata.getDownloadUrlForMarkdownParsedVideo(mediaMetadata.original.url));
                    if (post != null) {
                        intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, post.getSubredditName());
                    }
                    intent.putExtra(ViewVideoActivity.EXTRA_ID, mediaMetadata.id);
                    activity.startActivity(intent);
                }
            }
        });
        recycledViewPool = new RecyclerView.RecycledViewPool();
        mPost = post;
        mLocale = locale;
        mSingleCommentId = singleCommentId;

        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mCommentToolbarHidden = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDDEN, false);
        mCommentToolbarHideOnClick = sharedPreferences.getBoolean(SharedPreferencesUtils.COMMENT_TOOLBAR_HIDE_ON_CLICK, true);
        mSwapTapAndLong = sharedPreferences.getBoolean(SharedPreferencesUtils.SWAP_TAP_AND_LONG_COMMENTS, false);
        mShowCommentDivider = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false);
        mDividerType = Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.COMMENT_DIVIDER_TYPE, "0"));
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
        mFullyCollapseComment = sharedPreferences.getBoolean(SharedPreferencesUtils.FULLY_COLLAPSE_COMMENT, false);
        mShowOnlyOneCommentLevelIndicator = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ONLY_ONE_COMMENT_LEVEL_INDICATOR, false);
        mShowAuthorAvatar = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AUTHOR_AVATAR, false);
        mAlwaysShowChildCommentCount = sharedPreferences.getBoolean(SharedPreferencesUtils.ALWAYS_SHOW_CHILD_COMMENT_COUNT, false);
        mHideTheNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_THE_NUMBER_OF_VOTES_IN_COMMENTS, false);
        mDepthThreshold = sharedPreferences.getInt(SharedPreferencesUtils.SHOW_FEWER_TOOLBAR_OPTIONS_THRESHOLD, 5);

        mCommentRecyclerViewAdapterCallback = commentRecyclerViewAdapterCallback;

        expandDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_more_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());
        collapseDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_less_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());

        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mDividerColor = customThemeWrapper.getDividerColor();
        mCommentBackgroundColor = customThemeWrapper.getCommentBackgroundColor();
        mSubmitterColor = customThemeWrapper.getSubmitter();
        mModeratorColor = customThemeWrapper.getModerator();
        mCurrentUserColor = customThemeWrapper.getCurrentUser();
        mAuthorFlairTextColor = customThemeWrapper.getAuthorFlairTextColor();
        mUsernameColor = customThemeWrapper.getUsername();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mSingleCommentThreadBackgroundColor = customThemeWrapper.getSingleCommentThreadBackgroundColor();
        mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableButtonColor();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        mFullyCollapsedCommentBackgroundColor = customThemeWrapper.getFullyCollapsedCommentBackgroundColor();

        verticalBlockColors = new int[] {
                customThemeWrapper.getCommentVerticalBarColor1(),
                customThemeWrapper.getCommentVerticalBarColor2(),
                customThemeWrapper.getCommentVerticalBarColor3(),
                customThemeWrapper.getCommentVerticalBarColor4(),
                customThemeWrapper.getCommentVerticalBarColor5(),
                customThemeWrapper.getCommentVerticalBarColor6(),
                customThemeWrapper.getCommentVerticalBarColor7(),
        };
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= getCurrentList().size() || position < 0) {
            return VIEW_TYPE_COMMENT;
        }

        Comment comment = getItem(position);
        if (comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
            if ((mFullyCollapseComment && !comment.isExpanded() && comment.hasExpandedBefore())
                    || (comment.isFilteredOut() && !comment.hasExpandedBefore())) {
                return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
            }
            return VIEW_TYPE_COMMENT;
        } else {
            return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_COMMENT_FULLY_COLLAPSED:
                return new CommentFullyCollapsedViewHolder(ItemCommentFullyCollapsedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
                return new LoadMoreChildCommentsViewHolder(ItemLoadMoreCommentsPlaceholderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            default:
                return new CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (position >= getCurrentList().size() || position < 0) {
            return;
        }

        if (holder instanceof CommentBaseViewHolder) {
            Comment comment = getItem(position);
            if (comment != null) {
                if (comment.getId().equals(mSingleCommentId)) {
                    holder.itemView.setBackgroundColor(mSingleCommentThreadBackgroundColor);
                }

                String authorPrefixed = "u/" + comment.getAuthor();
                ((CommentBaseViewHolder) holder).authorTextView.setText(authorPrefixed);

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentBaseViewHolder) holder).authorFlairTextView, comment.getAuthorFlairHTML(), true);
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (comment.isSubmitter()) {
                    ((CommentBaseViewHolder) holder).authorTextView.setTextColor(mSubmitterColor);
                    Drawable submitterDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_mic_14dp, mSubmitterColor);
                    ((CommentBaseViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            submitterDrawable, null, null, null);
                } else if (comment.isModerator()) {
                    ((CommentBaseViewHolder) holder).authorTextView.setTextColor(mModeratorColor);
                    Drawable moderatorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_verified_user_14dp, mModeratorColor);
                    ((CommentBaseViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            moderatorDrawable, null, null, null);
                } else if (comment.getAuthor().equals(mAccountName)) {
                    ((CommentBaseViewHolder) holder).authorTextView.setTextColor(mCurrentUserColor);
                    Drawable currentUserDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_current_user_14dp, mCurrentUserColor);
                    ((CommentBaseViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            currentUserDrawable, null, null, null);
                }

                if (mShowAuthorAvatar) {
                    if (comment.getAuthorIconUrl() == null && comment.getAuthorFullName() != null && !comment.getAuthorFullName().isEmpty()) {
                        if (position >= 0) {
                            List<Comment> commentBatch = getCurrentList().subList(position, Math.min(getCurrentList().size(), UserProfileImagesBatchLoader.BATCH_SIZE + position));
                            mFragment.loadIcon(commentBatch, (authorFullName, iconUrl) -> {
                                int currentPosition = holder.getBindingAdapterPosition();
                                if (currentPosition < 0 || currentPosition >= getCurrentList().size()) {
                                    return;
                                }

                                if (authorFullName.equals(comment.getAuthorFullName())) {
                                    comment.setAuthorIconUrl(iconUrl);
                                }

                                Comment currentComment = getItem(currentPosition);
                                if (currentComment != null && authorFullName.equals(currentComment.getAuthorFullName())) {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((CommentBaseViewHolder) holder).authorIconImageView);
                                }
                            });
                        }
                    } else {
                        mGlide.load(comment.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((CommentBaseViewHolder) holder).authorIconImageView);
                    }
                }

                if (mShowElapsedTime) {
                    ((CommentBaseViewHolder) holder).commentTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentBaseViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }

                if (mCommentToolbarHidden) {
                    ((CommentBaseViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = 0;
                    if (!mHideTheNumberOfVotes) {
                        ((CommentBaseViewHolder) holder).topScoreTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    ((CommentBaseViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    ((CommentBaseViewHolder) holder).topScoreTextView.setVisibility(View.GONE);
                }

                mEmoteCloseBracketInlineProcessor.setMediaMetadataMap(comment.getMediaMetadataMap());
                mImageAndGifPlugin.setMediaMetadataMap(comment.getMediaMetadataMap());
                mVideoPlugin.setMediaMetadataMap(comment.getMediaMetadataMap());
                ((CommentBaseViewHolder) holder).mMarkwonAdapter.setMarkdown(mCommentMarkwon, comment.getCommentMarkdown());
                // noinspection NotifyDataSetChanged
                ((CommentBaseViewHolder) holder).mMarkwonAdapter.notifyDataSetChanged();

                if (!mHideTheNumberOfVotes) {
                    String commentText = "";
                    String topScoreText = "";
                    if (comment.isScoreHidden()) {
                        commentText = mActivity.getString(R.string.hidden);
                    } else {
                        commentText = Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType());
                        topScoreText = mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType()));
                    }
                    ((CommentBaseViewHolder) holder).scoreTextView.setText(commentText);
                    ((CommentBaseViewHolder) holder).topScoreTextView.setText(topScoreText);
                } else {
                    ((CommentBaseViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.vote));
                }

                if (comment.isEdited()) {
                    ((CommentBaseViewHolder) holder).editedTextView.setVisibility(View.VISIBLE);
                } else {
                    ((CommentBaseViewHolder) holder).editedTextView.setVisibility(View.GONE);
                }

                ((CommentBaseViewHolder) holder).commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentBaseViewHolder) holder).commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);
                if (comment.getDepth() >= mDepthThreshold) {
                    ((CommentBaseViewHolder) holder).saveButton.setVisibility(View.GONE);
                    ((CommentBaseViewHolder) holder).replyButton.setVisibility(View.GONE);
                } else {
                    ((CommentBaseViewHolder) holder).saveButton.setVisibility(View.VISIBLE);
                    ((CommentBaseViewHolder) holder).replyButton.setVisibility(View.VISIBLE);
                }

                if (comment.hasReply()) {
                    if (comment.getChildCount() > 0 && (mAlwaysShowChildCommentCount || !comment.isExpanded())) {
                        ((CommentBaseViewHolder) holder).expandButton.setText("+" + comment.getChildCount());
                    }
                    if (comment.isExpanded()) {
                        ((CommentBaseViewHolder) holder).expandButton.setCompoundDrawablesWithIntrinsicBounds(collapseDrawable, null, null, null);
                    } else {
                        ((CommentBaseViewHolder) holder).expandButton.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
                    }
                    ((CommentBaseViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
                }

                switch (comment.getVoteType()) {
                    case Comment.VOTE_TYPE_UPVOTE:
                        ((CommentBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                        ((CommentBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                        ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        ((CommentBaseViewHolder) holder).topScoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case Comment.VOTE_TYPE_DOWNVOTE:
                        ((CommentBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                        ((CommentBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                        ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        ((CommentBaseViewHolder) holder).topScoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (mPost.isArchived()) {
                    ((CommentBaseViewHolder) holder).replyButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                    ((CommentBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                    ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mVoteAndReplyUnavailableVoteButtonColor);
                    ((CommentBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                }

                if (mPost.isLocked() || comment.isLocked()) {
                    ((CommentBaseViewHolder) holder).replyButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                }

                if (comment.isSaved()) {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (position == mSearchedPosition) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#03A9F4"));
                }

                if (mShowCommentDivider) {
                    if (mDividerType == DIVIDER_PARENT && comment.getDepth() == 0) {
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        params.setMargins(0, (int) Utils.convertDpToPixel(16, mActivity), 0, 0);
                    }
                }
            }
        } else if (holder instanceof CommentFullyCollapsedViewHolder) {
            Comment comment = getItem(position);
            if (comment != null) {
                String authorWithPrefix = "u/" + comment.getAuthor();
                ((CommentFullyCollapsedViewHolder) holder).binding.userNameTextViewItemCommentFullyCollapsed.setText(authorWithPrefix);

                if (mShowAuthorAvatar) {
                    if (comment.getAuthorIconUrl() == null && comment.getAuthorFullName() != null && !comment.getAuthorFullName().isEmpty()) {
                        if (position >= 0) {
                            List<Comment> commentBatch = getCurrentList().subList(position, Math.min(getCurrentList().size(), UserProfileImagesBatchLoader.BATCH_SIZE + position));
                            mFragment.loadIcon(commentBatch, (authorFullName, iconUrl) -> {
                                if (authorFullName.equals(comment.getAuthorFullName())) {
                                    comment.setAuthorIconUrl(iconUrl);
                                }

                                Comment currentComment = getItem(holder.getBindingAdapterPosition());
                                if (currentComment != null && authorFullName.equals(currentComment.getAuthorFullName())) {
                                    mGlide.load(iconUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((CommentFullyCollapsedViewHolder) holder).binding.authorIconImageViewItemCommentFullyCollapsed);
                                }
                            });
                        }
                    } else {
                        mGlide.load(comment.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((CommentFullyCollapsedViewHolder) holder).binding.authorIconImageViewItemCommentFullyCollapsed);
                    }
                }

                if (comment.getChildCount() > 0) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextViewItemCommentFullyCollapsed.setVisibility(View.VISIBLE);
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextViewItemCommentFullyCollapsed.setText("+" + comment.getChildCount());
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.childCountTextViewItemCommentFullyCollapsed.setVisibility(View.GONE);
                }
                if (mShowElapsedTime) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.timeTextViewItemCommentFullyCollapsed.setText(Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.timeTextViewItemCommentFullyCollapsed.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }
                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextViewItemCommentFullyCollapsed.setText(mActivity.getString(R.string.top_score,
                            Utils.getNVotes(mShowAbsoluteNumberOfVotes, comment.getScore() + comment.getVoteType())));
                } else if (mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextViewItemCommentFullyCollapsed.setText(mActivity.getString(R.string.vote));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).binding.scoreTextViewItemCommentFullyCollapsed.setText(mActivity.getString(R.string.hidden));
                }
                ((CommentFullyCollapsedViewHolder) holder).binding.verticalBlockIndentationItemCommentFullyCollapsed.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentFullyCollapsedViewHolder) holder).binding.verticalBlockIndentationItemCommentFullyCollapsed.setLevelAndColors(comment.getDepth(), verticalBlockColors);

                if (mShowCommentDivider) {
                    if (mDividerType == DIVIDER_PARENT && comment.getDepth() == 0) {
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        params.setMargins(0, (int) Utils.convertDpToPixel(16, mActivity), 0, 0);
                    }
                }
            }
        } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
            Comment placeholder = getItem(position);
            if (placeholder != null) {
                ((LoadMoreChildCommentsViewHolder) holder).binding.verticalBlockIndentationItemLoadMoreCommentsPlaceholder.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((LoadMoreChildCommentsViewHolder) holder).binding.verticalBlockIndentationItemLoadMoreCommentsPlaceholder.setLevelAndColors(placeholder.getDepth(), verticalBlockColors);

                if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                    if (placeholder.isLoadingMoreChildren()) {
                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setText(R.string.loading);
                    } else if (placeholder.isLoadMoreChildrenFailed()) {
                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setText(R.string.comment_load_more_comments_failed);
                    } else {
                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setText(R.string.comment_load_more_comments);
                    }
                } else {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setText(R.string.comment_continue_thread);
                }

                if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setOnClickListener(view -> {
                        mCommentRecyclerViewAdapterCallback.fetchMoreChildComments(holder.getBindingAdapterPosition());
                        ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setText(R.string.loading);
                    });
                } else {
                    ((LoadMoreChildCommentsViewHolder) holder).binding.placeholderTextViewItemLoadMoreComments.setOnClickListener(view -> {
                        Comment comment = getItem(position);
                        if (comment != null) {
                            Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, mPost);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getParentId());
                            intent.putExtra(ViewPostDetailActivity.EXTRA_CONTEXT_NUMBER, "0");
                            mActivity.startActivity(intent);
                        }
                    });
                }
            }
        }
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    public ArrayList<Comment> getVisibleComments() {
        return new ArrayList<>(getCurrentList());
    }

    public void initiallyLoading() {
        resetSearchedPosition(false);
    }

    public void onItemSwipe(RecyclerView.ViewHolder viewHolder, int direction, int swipeLeftAction, int swipeRightAction) {
        if (viewHolder instanceof CommentBaseViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentBaseViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentBaseViewHolder) viewHolder).downvoteButton.performClick();
                }
            }
        }
    }

    public void setSaveComment(int position, boolean isSaved) {
        Comment comment = getItem(position);
        if (comment != null) {
            comment.setSaved(isSaved);
        }
    }

    public int getSearchedPosition() {
        return mSearchedPosition;
    }

    public void highlightSearchResult(int searchedPosition) {
        mSearchedPosition = searchedPosition;
        notifyItemChanged(searchedPosition);
    }

    public void resetSearchedPosition(boolean notifyOldSearchedPosition) {
        if (notifyOldSearchedPosition) {
            notifyItemChanged(mSearchedPosition);
        }
        mSearchedPosition = -1;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentBaseViewHolder) {
            holder.itemView.setBackgroundColor(mCommentBackgroundColor);
            ((CommentBaseViewHolder) holder).authorTextView.setTextColor(mUsernameColor);
            ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.GONE);
            ((CommentBaseViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mGlide.clear(((CommentBaseViewHolder) holder).authorIconImageView);
            ((CommentBaseViewHolder) holder).topScoreTextView.setTextColor(mSecondaryTextColor);
            ((CommentBaseViewHolder) holder).expandButton.setVisibility(View.GONE);
            ((CommentBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
            ((CommentBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
            ((CommentBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
            ((CommentBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            ((CommentBaseViewHolder) holder).expandButton.setText("");
            ((CommentBaseViewHolder) holder).replyButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
            params.setMargins(0, 0, 0, 0);
        }
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        mEmotePlugin.setDataSavingMode(dataSavingMode);
        mImageAndGifEntry.setDataSavingMode(dataSavingMode);
    }

    public void updatePost(@NonNull Post post) {
        mPost = post;
        mImageAndGifEntry.setBlurImage(
                (post.isNSFW() && mNeedBlurNsfw
                        && !(mDoNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit()))
                        || (post.isSpoiler() && mNeedBlurSpoiler)
        );
    }

    public interface CommentRecyclerViewAdapterCallback {
        void expandComment(int position);
        void collapseComment(int position);
        void fetchMoreChildComments(int position);
    }

    public class CommentBaseViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        ImageView authorIconImageView;
        TextView authorTextView;
        TextView authorFlairTextView;
        TextView commentTimeTextView;
        TextView topScoreTextView;
        RecyclerView commentMarkdownView;
        TextView editedTextView;
        ConstraintLayout bottomConstraintLayout;
        MaterialButton upvoteButton;
        TextView scoreTextView;
        MaterialButton downvoteButton;
        View placeholder;
        MaterialButton moreButton;
        MaterialButton saveButton;
        TextView expandButton;
        MaterialButton replyButton;
        CommentIndentationView commentIndentationView;
        View commentDivider;
        CustomMarkwonAdapter mMarkwonAdapter;

        CommentBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(LinearLayout linearLayout,
                         ImageView authorIconImageView,
                         TextView authorTextView,
                         TextView authorFlairTextView,
                         TextView commentTimeTextView,
                         TextView topScoreTextView,
                         RecyclerView commentMarkdownView,
                         TextView editedTextView,
                         ConstraintLayout bottomConstraintLayout,
                         MaterialButton upvoteButton,
                         TextView scoreTextView,
                         MaterialButton downvoteButton,
                         View placeholder,
                         MaterialButton moreButton,
                         MaterialButton saveButton,
                         TextView expandButton,
                         MaterialButton replyButton,
                         CommentIndentationView commentIndentationView,
                         View commentDivider) {
            this.linearLayout = linearLayout;
            this.authorIconImageView = authorIconImageView;
            this.authorTextView = authorTextView;
            this.authorFlairTextView = authorFlairTextView;
            this.commentTimeTextView = commentTimeTextView;
            this.topScoreTextView = topScoreTextView;
            this.commentMarkdownView = commentMarkdownView;
            this.editedTextView = editedTextView;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.placeholder = placeholder;
            this.moreButton = moreButton;
            this.saveButton = saveButton;
            this.expandButton = expandButton;
            this.replyButton = replyButton;
            this.commentIndentationView = commentIndentationView;
            this.commentDivider = commentDivider;

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.END);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.END);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.END);
                constraintSet.clear(expandButton.getId(), ConstraintSet.START);
                constraintSet.clear(expandButton.getId(), ConstraintSet.END);
                constraintSet.clear(saveButton.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(replyButton.getId(), ConstraintSet.START);
                constraintSet.clear(replyButton.getId(), ConstraintSet.END);
                constraintSet.clear(moreButton.getId(), ConstraintSet.START);
                constraintSet.clear(moreButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.START, placeholder.getId(), ConstraintSet.END);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.START, upvoteButton.getId(), ConstraintSet.END);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.START, scoreTextView.getId(), ConstraintSet.END);
                constraintSet.connect(placeholder.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(placeholder.getId(), ConstraintSet.START, moreButton.getId(), ConstraintSet.END);
                constraintSet.connect(moreButton.getId(), ConstraintSet.START, expandButton.getId(), ConstraintSet.END);
                constraintSet.connect(moreButton.getId(), ConstraintSet.END, placeholder.getId(), ConstraintSet.START);
                constraintSet.connect(expandButton.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(expandButton.getId(), ConstraintSet.END, moreButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(saveButton.getId(), ConstraintSet.END, expandButton.getId(), ConstraintSet.START);
                constraintSet.connect(replyButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.connect(replyButton.getId(), ConstraintSet.END, saveButton.getId(), ConstraintSet.START);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            if (linearLayout.getLayoutTransition() != null) {
                linearLayout.getLayoutTransition().setAnimateParentHierarchy(false);
            }

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    commentDivider.setBackgroundColor(mDividerColor);
                    commentDivider.setVisibility(View.VISIBLE);
                }
            }

            if (mActivity.typeface != null) {
                authorTextView.setTypeface(mActivity.typeface);
                commentTimeTextView.setTypeface(mActivity.typeface);
                authorFlairTextView.setTypeface(mActivity.typeface);
                topScoreTextView.setTypeface(mActivity.typeface);
                editedTextView.setTypeface(mActivity.typeface);
                scoreTextView.setTypeface(mActivity.typeface);
                expandButton.setTypeface(mActivity.typeface);
            }

            if (mShowAuthorAvatar) {
                authorIconImageView.setVisibility(View.VISIBLE);
            } else {
                ((ConstraintLayout.LayoutParams) authorTextView.getLayoutParams()).leftMargin = 0;
                ((ConstraintLayout.LayoutParams) authorFlairTextView.getLayoutParams()).leftMargin = 0;
            }

            commentMarkdownView.setRecycledViewPool(recycledViewPool);
            LinearLayoutManagerBugFixed linearLayoutManager = new SwipeLockLinearLayoutManager(mActivity, new SwipeLockInterface() {
                @Override
                public void lockSwipe() {
                    mActivity.lockSwipeRightToGoBack();
                }

                @Override
                public void unlockSwipe() {
                    mActivity.unlockSwipeRightToGoBack();
                }
            });
            commentMarkdownView.setLayoutManager(linearLayoutManager);
            mMarkwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(mActivity, mImageAndGifEntry, mVideoEntry);
            commentMarkdownView.setAdapter(mMarkwonAdapter);

            itemView.setBackgroundColor(mCommentBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);
            authorFlairTextView.setTextColor(mAuthorFlairTextColor);
            topScoreTextView.setTextColor(mSecondaryTextColor);
            editedTextView.setTextColor(mSecondaryTextColor);
            commentDivider.setBackgroundColor(mDividerColor);
            upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            scoreTextView.setTextColor(mCommentIconAndInfoColor);
            downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            moreButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            expandButton.setTextColor(mCommentIconAndInfoColor);
            saveButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            replyButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));

            authorFlairTextView.setOnClickListener(view -> authorTextView.performClick());

            editedTextView.setOnClickListener(view -> {
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.edited_time, mShowElapsedTime ?
                            Utils.getElapsedTime(mActivity, comment.getEditedTimeMillis()) :
                            Utils.getFormattedTime(mLocale, comment.getEditedTimeMillis(), mTimeFormatPattern)
                    ), Toast.LENGTH_SHORT).show();
                }
            });

            moreButton.setOnClickListener(view -> {
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_IS_NSFW, mPost.isNSFW());
                    if (comment.getDepth() >= mDepthThreshold) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_SHOW_REPLY_AND_SAVE_OPTION, true);
                    }
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(mFragment.getChildFragmentManager(), commentMoreBottomSheetFragment.getTag());
                }
            });

            replyButton.setOnClickListener(view -> {
                if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mPost.isLocked()) {
                    Toast.makeText(mActivity, R.string.locked_post_reply_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    if (comment.isLocked()) {
                        Toast.makeText(mActivity, R.string.locked_comment_reply_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Intent intent = new Intent(mActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, comment.getCommentMarkdown());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, comment.getCommentRawText());
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_SUBREDDIT_NAME_KEY, mPost.getSubredditName());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                    intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, getBindingAdapterPosition());
                    mFragment.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
                }
            });

            upvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                    downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));

                    if (previousVoteType != Comment.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                        scoreTextView.setTextColor(mUpvotedColor);
                        topScoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType())));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                                    scoreTextView.setTextColor(mUpvotedColor);
                                    topScoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                    topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                    comment.getScore() + comment.getVoteType())));
                                }
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                        }
                    }, comment.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            scoreTextView.setOnClickListener(view -> {
                upvoteButton.performClick();
            });

            downvoteButton.setOnClickListener(view -> {
                if (mPost.isArchived()) {
                    Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                    upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));

                    if (previousVoteType != Comment.VOTE_TYPE_DOWNVOTE) {
                        //Not downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                        scoreTextView.setTextColor(mDownvotedColor);
                        topScoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                        topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType())));
                    }

                    int position = getBindingAdapterPosition();
                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                                if (currentPosition == position) {
                                    downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                                    downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                                    scoreTextView.setTextColor(mDownvotedColor);
                                    topScoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                    downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
                                    topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                    comment.getScore() + comment.getVoteType())));
                                }
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                        }
                    }, comment.getFullName(), newVoteType, getBindingAdapterPosition());
                }
            });

            saveButton.setOnClickListener(view -> {
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    int position = getBindingAdapterPosition();
                    if (comment.isSaved()) {
                        comment.setSaved(false);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(false);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        comment.setSaved(true);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(true);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            authorTextView.setOnClickListener(view -> {
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment == null || comment.isAuthorDeleted()) {
                    return;
                }
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, comment.getAuthor());
                mActivity.startActivity(intent);
            });

            authorIconImageView.setOnClickListener(view -> {
                authorTextView.performClick();
            });

            expandButton.setOnClickListener(view -> {
                if (expandButton.getVisibility() == View.VISIBLE) {
                    mCommentRecyclerViewAdapterCallback.expandComment(getBindingAdapterPosition());
                } else if (mFullyCollapseComment) {
                    mCommentRecyclerViewAdapterCallback.collapseComment(getBindingAdapterPosition());
                }
            });

            if (mSwapTapAndLong) {
                if (mCommentToolbarHideOnClick) {
                    View.OnLongClickListener hideToolbarOnLongClickListener = view -> hideToolbar();
                    itemView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    commentTimeTextView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    mMarkwonAdapter.setOnLongClickListener(v -> {
                        if (v instanceof TextView) {
                            if (((TextView) v).getSelectionStart() == -1 && ((TextView) v).getSelectionEnd() == -1) {
                                hideToolbar();
                            }
                        }
                        return true;
                    });
                }
                mMarkwonAdapter.setOnClickListener(v -> {
                    if (v instanceof SpoilerOnClickTextView) {
                        if (((SpoilerOnClickTextView) v).isSpoilerOnClick()) {
                            ((SpoilerOnClickTextView) v).setSpoilerOnClick(false);
                            return;
                        }
                    }
                    expandComments();
                });
                itemView.setOnClickListener(view -> expandComments());
            } else {
                if (mCommentToolbarHideOnClick) {
                    mMarkwonAdapter.setOnClickListener(view -> {
                        if (view instanceof SpoilerOnClickTextView) {
                            if (((SpoilerOnClickTextView) view).isSpoilerOnClick()) {
                                ((SpoilerOnClickTextView) view).setSpoilerOnClick(false);
                                return;
                            }
                        }
                        hideToolbar();
                    });
                    View.OnClickListener hideToolbarOnClickListener = view -> hideToolbar();
                    itemView.setOnClickListener(hideToolbarOnClickListener);
                    commentTimeTextView.setOnClickListener(hideToolbarOnClickListener);
                }
                mMarkwonAdapter.setOnLongClickListener(view -> {
                    if (view instanceof TextView) {
                        if (((TextView) view).getSelectionStart() == -1 && ((TextView) view).getSelectionEnd() == -1) {
                            expandComments();
                        }
                    }
                    return true;
                });
                itemView.setOnLongClickListener(view -> {
                    expandComments();
                    return true;
                });
            }
        }

        private boolean expandComments() {
            expandButton.performClick();
            return true;
        }

        private boolean hideToolbar() {
            if (bottomConstraintLayout.getLayoutParams().height == 0) {
                bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                topScoreTextView.setVisibility(View.GONE);
                mFragment.delayTransition();
            } else {
                mFragment.delayTransition();
                bottomConstraintLayout.getLayoutParams().height = 0;
                if (!mHideTheNumberOfVotes) {
                    topScoreTextView.setVisibility(View.VISIBLE);
                }
            }
            return true;
        }
    }

    class CommentViewHolder extends CommentBaseViewHolder {
        ItemCommentBinding binding;

        CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.linearLayoutItemComment,
                    binding.authorIconImageViewItemPostComment,
                    binding.authorTextViewItemPostComment,
                    binding.authorFlairTextViewItemPostComment,
                    binding.commentTimeTextViewItemPostComment,
                    binding.topScoreTextViewItemPostComment,
                    binding.commentMarkdownViewItemPostComment,
                    binding.editedTextViewItemPostComment,
                    binding.bottomConstraintLayoutItemPostComment,
                    binding.upvoteButtonItemPostComment,
                    binding.scoreTextViewItemPostComment,
                    binding.downvoteButtonItemPostComment,
                    binding.placeholderItemPostComment,
                    binding.moreButtonItemPostComment,
                    binding.saveButtonItemPostComment,
                    binding.expandButtonItemPostComment,
                    binding.replyButtonItemPostComment,
                    binding.verticalBlockIndentationItemComment,
                    binding.dividerItemComment);
        }
    }

    class CommentFullyCollapsedViewHolder extends RecyclerView.ViewHolder {
        ItemCommentFullyCollapsedBinding binding;

        public CommentFullyCollapsedViewHolder(@NonNull ItemCommentFullyCollapsedBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (mActivity.typeface != null) {
                binding.userNameTextViewItemCommentFullyCollapsed.setTypeface(mActivity.typeface);
                binding.childCountTextViewItemCommentFullyCollapsed.setTypeface(mActivity.typeface);
                binding.scoreTextViewItemCommentFullyCollapsed.setTypeface(mActivity.typeface);
                binding.timeTextViewItemCommentFullyCollapsed.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mFullyCollapsedCommentBackgroundColor);
            binding.userNameTextViewItemCommentFullyCollapsed.setTextColor(mUsernameColor);
            binding.childCountTextViewItemCommentFullyCollapsed.setTextColor(mSecondaryTextColor);
            binding.scoreTextViewItemCommentFullyCollapsed.setTextColor(mSecondaryTextColor);
            binding.timeTextViewItemCommentFullyCollapsed.setTextColor(mSecondaryTextColor);

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    binding.dividerItemCommentFullyCollapsed.setBackgroundColor(mDividerColor);
                    binding.dividerItemCommentFullyCollapsed.setVisibility(View.VISIBLE);
                }
            }

            if (mShowAuthorAvatar) {
                binding.authorIconImageViewItemCommentFullyCollapsed.setVisibility(View.VISIBLE);
            } else {
                binding.userNameTextViewItemCommentFullyCollapsed.setPaddingRelative(0, binding.userNameTextViewItemCommentFullyCollapsed.getPaddingTop(), binding.userNameTextViewItemCommentFullyCollapsed.getPaddingEnd(), binding.userNameTextViewItemCommentFullyCollapsed.getPaddingBottom());
            }

            itemView.setOnClickListener(view -> {
                mCommentRecyclerViewAdapterCallback.expandComment(getBindingAdapterPosition());
            });

            itemView.setOnLongClickListener(view -> {
                itemView.performClick();
                return true;
            });
        }
    }

    class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {
        ItemLoadMoreCommentsPlaceholderBinding binding;

        LoadMoreChildCommentsViewHolder(@NonNull ItemLoadMoreCommentsPlaceholderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    binding.dividerItemLoadMoreCommentsPlaceholder.setBackgroundColor(mDividerColor);
                    binding.dividerItemLoadMoreCommentsPlaceholder.setVisibility(View.VISIBLE);
                }
            }

            if (mActivity.typeface != null) {
                binding.placeholderTextViewItemLoadMoreComments.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            binding.placeholderTextViewItemLoadMoreComments.setTextColor(mPrimaryTextColor);
        }
    }
}
