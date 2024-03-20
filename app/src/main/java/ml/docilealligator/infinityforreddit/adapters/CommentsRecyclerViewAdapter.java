package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.button.MaterialButton;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.FetchComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CommentIndentationView;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockInterface;
import ml.docilealligator.infinityforreddit.customviews.SwipeLockLinearLayoutManager;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentBinding;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.markdown.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.markdown.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifPlugin;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static final int DIVIDER_NORMAL = 0;
    public static final int DIVIDER_PARENT = 1;

    private static final int VIEW_TYPE_FIRST_LOADING = 9;
    private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 10;
    private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 11;
    private static final int VIEW_TYPE_COMMENT = 12;
    private static final int VIEW_TYPE_COMMENT_FULLY_COLLAPSED = 13;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 14;
    private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 15;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 16;
    private static final int VIEW_TYPE_VIEW_ALL_COMMENTS = 17;

    private final BaseActivity mActivity;
    private final ViewPostDetailFragment mFragment;
    private final Executor mExecutor;
    private final Retrofit mRetrofit;
    private final Retrofit mOauthRetrofit;
    private final EmoteCloseBracketInlineProcessor mEmoteCloseBracketInlineProcessor;
    private final EmotePlugin mEmotePlugin;
    private final ImageAndGifPlugin mImageAndGifPlugin;
    private final Markwon mCommentMarkwon;
    private final ImageAndGifEntry mImageAndGifEntry;
    private final String mAccessToken;
    private final String mAccountName;
    private final Post mPost;
    private final ArrayList<Comment> mVisibleComments;
    private final Locale mLocale;
    private final RequestManager mGlide;
    private final RecyclerView.RecycledViewPool recycledViewPool;
    private String mSingleCommentId;
    private boolean mIsSingleCommentThreadMode;
    private final boolean mVoteButtonsOnTheRight;
    private final boolean mShowElapsedTime;
    private final String mTimeFormatPattern;
    private final boolean mExpandChildren;
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
    private final int mDepthThreshold;
    private final CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private boolean isInitiallyLoading;
    private boolean isInitiallyLoadingFailed;
    private boolean mHasMoreComments;
    private boolean loadMoreCommentsFailed;
    private final Drawable expandDrawable;
    private final Drawable collapseDrawable;

    private final int mColorPrimaryLightTheme;
    private final int mColorAccent;
    private final int mCircularProgressBarBackgroundColor;
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
    private final int mButtonTextColor;
    private final int mCommentIconAndInfoColor;
    private final int mFullyCollapsedCommentBackgroundColor;
    private final int[] verticalBlockColors;

    private int mSearchCommentIndex = -1;

    private boolean canStartActivity = true;

    public CommentsRecyclerViewAdapter(BaseActivity activity, ViewPostDetailFragment fragment,
                                       CustomThemeWrapper customThemeWrapper,
                                       Executor executor, Retrofit retrofit, Retrofit oauthRetrofit,
                                       @Nullable String accessToken, @NonNull String accountName,
                                       Post post, Locale locale, String singleCommentId,
                                       boolean isSingleCommentThreadMode,
                                       SharedPreferences sharedPreferences,
                                       SharedPreferences nsfwAndSpoilerSharedPreferences,
                                       CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        mActivity = activity;
        mFragment = fragment;
        mExecutor = executor;
        mRetrofit =
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
                    intent.putExtra(LinkResolverActivity.EXTRA_IS_NSFW, mPost.isNSFW());
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
        mEmotePlugin = EmotePlugin.create(activity, mediaMetadata -> {
            Intent intent = new Intent(activity, ViewImageOrGifActivity.class);
            if (mediaMetadata.isGIF) {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
            } else {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
            }
            intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, post.isNSFW());
            intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, post.getSubredditName());
            intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
            if (canStartActivity) {
                canStartActivity = false;
                activity.startActivity(intent);
            }
        });
        mImageAndGifPlugin = new ImageAndGifPlugin();
        mCommentMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, mEmoteCloseBracketInlineProcessor, mEmotePlugin, mImageAndGifPlugin, mCommentTextColor,
                commentSpoilerBackgroundColor, onLinkLongClickListener);

        boolean needBlurNsfw = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.BLUR_NSFW_BASE, true);
        boolean doNotBlurNsfwInNsfwSubreddits = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.DO_NOT_BLUR_NSFW_IN_NSFW_SUBREDDITS, false);
        boolean needBlurSpoiler = nsfwAndSpoilerSharedPreferences.getBoolean((mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : mAccountName) + SharedPreferencesUtils.BLUR_SPOILER_BASE, false);
        boolean blurImage = (post.isNSFW() && needBlurNsfw && !(doNotBlurNsfwInNsfwSubreddits && mFragment != null && mFragment.getIsNsfwSubreddit())) || (post.isSpoiler() && needBlurSpoiler);
        mImageAndGifEntry = new ImageAndGifEntry(activity, mGlide, blurImage,
                mediaMetadata -> {
                    Intent intent = new Intent(activity, ViewImageOrGifActivity.class);
                    if (mediaMetadata.isGIF) {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
                    } else {
                        intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
                    }
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_IS_NSFW, post.isNSFW());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, post.getSubredditName());
                    intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
                    if (canStartActivity) {
                        canStartActivity = false;
                        activity.startActivity(intent);
                    }
                });
        recycledViewPool = new RecyclerView.RecycledViewPool();
        mPost = post;
        mVisibleComments = new ArrayList<>();
        mLocale = locale;
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;

        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mExpandChildren = !sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_TOP_LEVEL_COMMENTS_FIRST, false);
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
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        mHasMoreComments = false;
        loadMoreCommentsFailed = false;

        expandDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_more_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());
        collapseDrawable = Utils.getTintedDrawable(activity, R.drawable.ic_expand_less_grey_24dp, customThemeWrapper.getCommentIconAndInfoColor());

        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mColorAccent = customThemeWrapper.getColorAccent();
        mCircularProgressBarBackgroundColor = customThemeWrapper.getCircularProgressBarBackground();
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
        mButtonTextColor = customThemeWrapper.getButtonTextColor();
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
        if (mVisibleComments.size() == 0) {
            if (isInitiallyLoading) {
                return VIEW_TYPE_FIRST_LOADING;
            } else if (isInitiallyLoadingFailed) {
                if(mIsSingleCommentThreadMode && position == 0) {
                    return VIEW_TYPE_VIEW_ALL_COMMENTS;
                }
                return VIEW_TYPE_FIRST_LOADING_FAILED;
            } else {
                if(mIsSingleCommentThreadMode && position == 0) {
                    return VIEW_TYPE_VIEW_ALL_COMMENTS;
                }
                return VIEW_TYPE_NO_COMMENT_PLACEHOLDER;
            }
        }

        if (mIsSingleCommentThreadMode) {
            if (position == 0) {
                return VIEW_TYPE_VIEW_ALL_COMMENTS;
            }

            if (position == mVisibleComments.size() + 1) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position - 1);
            if (comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
                if (mFullyCollapseComment && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        } else {
            if (position == mVisibleComments.size()) {
                if (mHasMoreComments) {
                    return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
                } else {
                    return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
                }
            }

            Comment comment = mVisibleComments.get(position);
            if (comment.getPlaceholderType() == Comment.NOT_PLACEHOLDER) {
                if (mFullyCollapseComment && !comment.isExpanded() && comment.hasExpandedBefore()) {
                    return VIEW_TYPE_COMMENT_FULLY_COLLAPSED;
                }
                return VIEW_TYPE_COMMENT;
            } else {
                return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_FIRST_LOADING:
                return new LoadCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments, parent, false));
            case VIEW_TYPE_FIRST_LOADING_FAILED:
                return new LoadCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments_failed_placeholder, parent, false));
            case VIEW_TYPE_NO_COMMENT_PLACEHOLDER:
                return new NoCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_comment_placeholder, parent, false));
            case VIEW_TYPE_COMMENT:
                return new CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
            case VIEW_TYPE_COMMENT_FULLY_COLLAPSED:
                return new CommentFullyCollapsedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_fully_collapsed, parent, false));
            case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
                return new LoadMoreChildCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more_comments_placeholder, parent, false));
            case VIEW_TYPE_IS_LOADING_MORE_COMMENTS:
                return new IsLoadingMoreCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_loading, parent, false));
            case VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED:
                return new LoadMoreCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_error, parent, false));
            default:
                return new ViewAllCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view_all_comments, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentBaseViewHolder) {
            Comment comment = getCurrentComment(position);
            if (comment != null) {
                if (mIsSingleCommentThreadMode && comment.getId().equals(mSingleCommentId)) {
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

                if (comment.getAuthorIconUrl() == null) {
                    mFragment.loadIcon(comment.getAuthor(), (authorName, iconUrl) -> {
                        if (authorName.equals(comment.getAuthor())) {
                            comment.setAuthorIconUrl(iconUrl);
                        }

                        Comment currentComment = getCurrentComment(holder);
                        if (currentComment != null && authorName.equals(currentComment.getAuthor())) {
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentBaseViewHolder) holder).authorIconImageView);
                        }
                    });
                } else {
                    mGlide.load(comment.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((CommentBaseViewHolder) holder).authorIconImageView);
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

                if (mPost.isLocked()) {
                    ((CommentBaseViewHolder) holder).replyButton.setIconTint(ColorStateList.valueOf(mVoteAndReplyUnavailableVoteButtonColor));
                }

                if (comment.isSaved()) {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (position == mSearchCommentIndex) {
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
            Comment comment = getCurrentComment(position);
            if (comment != null) {
                String authorWithPrefix = "u/" + comment.getAuthor();
                ((CommentFullyCollapsedViewHolder) holder).usernameTextView.setText(authorWithPrefix);

                if (comment.getAuthorIconUrl() == null) {
                    mFragment.loadIcon(comment.getAuthor(), (authorName, iconUrl) -> {
                        if (authorName.equals(comment.getAuthor())) {
                            comment.setAuthorIconUrl(iconUrl);
                        }

                        Comment currentComment = getCurrentComment(holder);
                        if (currentComment != null && authorName.equals(currentComment.getAuthor())) {
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentFullyCollapsedViewHolder) holder).authorIconImageView);
                        }
                    });
                } else {
                    mGlide.load(comment.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((CommentFullyCollapsedViewHolder) holder).authorIconImageView);
                }

                if (comment.getChildCount() > 0) {
                    ((CommentFullyCollapsedViewHolder) holder).childCountTextView.setVisibility(View.VISIBLE);
                    ((CommentFullyCollapsedViewHolder) holder).childCountTextView.setText("+" + comment.getChildCount());
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).childCountTextView.setVisibility(View.GONE);
                }
                if (mShowElapsedTime) {
                    ((CommentFullyCollapsedViewHolder) holder).commentTimeTextView.setText(Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }
                if (!comment.isScoreHidden() && !mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.top_score,
                            Utils.getNVotes(mShowAbsoluteNumberOfVotes, comment.getScore() + comment.getVoteType())));
                } else if (mHideTheNumberOfVotes) {
                    ((CommentFullyCollapsedViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.vote));
                } else {
                    ((CommentFullyCollapsedViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.hidden));
                }
                ((CommentFullyCollapsedViewHolder) holder).commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentFullyCollapsedViewHolder) holder).commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);

                if (mShowCommentDivider) {
                    if (mDividerType == DIVIDER_PARENT && comment.getDepth() == 0) {
                        RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) holder.itemView.getLayoutParams();
                        params.setMargins(0, (int) Utils.convertDpToPixel(16, mActivity), 0, 0);
                    }
                }
            }
        } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
            Comment placeholder;
            placeholder = mIsSingleCommentThreadMode ? mVisibleComments.get(holder.getBindingAdapterPosition() - 1)
                    : mVisibleComments.get(holder.getBindingAdapterPosition());

            ((LoadMoreChildCommentsViewHolder) holder).commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
            ((LoadMoreChildCommentsViewHolder) holder).commentIndentationView.setLevelAndColors(placeholder.getDepth(), verticalBlockColors);

            if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                if (placeholder.isLoadingMoreChildren()) {
                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);
                } else if (placeholder.isLoadMoreChildrenFailed()) {
                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                } else {
                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);
                }
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_continue_thread);
            }

            if (placeholder.getPlaceholderType() == Comment.PLACEHOLDER_LOAD_MORE_COMMENTS) {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setOnClickListener(view -> {
                    int commentPosition = mIsSingleCommentThreadMode ? holder.getBindingAdapterPosition() - 1 : holder.getBindingAdapterPosition();
                    int parentPosition = getParentPosition(commentPosition);
                    if (parentPosition >= 0) {
                        Comment parentComment = mVisibleComments.get(parentPosition);

                        mVisibleComments.get(commentPosition).setLoadingMoreChildren(true);
                        mVisibleComments.get(commentPosition).setLoadMoreChildrenFailed(false);
                        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);

                        Retrofit retrofit = mAccountName.equals(Account.ANONYMOUS_ACCOUNT) ? mRetrofit : mOauthRetrofit;
                        SortType.Type sortType = mCommentRecyclerViewAdapterCallback.getSortType();
                        FetchComment.fetchMoreComment(mExecutor, new Handler(), retrofit, mAccessToken,
                                mAccountName, parentComment.getMoreChildrenIds(),
                                mExpandChildren, mPost.getFullName(), sortType, new FetchComment.FetchMoreCommentListener() {
                                    @Override
                                    public void onFetchMoreCommentSuccess(ArrayList<Comment> topLevelComments,
                                                                          ArrayList<Comment> expandedComments,
                                                                          ArrayList<String> moreChildrenIds) {
                                        if (mVisibleComments.size() > parentPosition
                                                && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                            if (mVisibleComments.get(parentPosition).isExpanded()) {
                                                if (!moreChildrenIds.isEmpty()) {
                                                    mVisibleComments.get(parentPosition).setMoreChildrenIds(moreChildrenIds);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);

                                                    int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), commentPosition);
                                                    if (placeholderPosition != -1) {
                                                        mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                        mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);

                                                        mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                        } else {
                                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                        }
                                                    }
                                                } else {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenIds();

                                                    int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), commentPosition);
                                                    if (placeholderPosition != -1) {
                                                        mVisibleComments.remove(placeholderPosition);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRemoved(placeholderPosition + 1);
                                                        } else {
                                                            notifyItemRemoved(placeholderPosition);
                                                        }

                                                        mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                        if (mIsSingleCommentThreadMode) {
                                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                        } else {
                                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (mVisibleComments.get(parentPosition).hasReply() && moreChildrenIds.isEmpty()) {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenIds();
                                                }
                                            }

                                            mVisibleComments.get(parentPosition).addChildren(topLevelComments);
                                            if (mIsSingleCommentThreadMode) {
                                                notifyItemChanged(parentPosition + 1);
                                            } else {
                                                notifyItemChanged(parentPosition);
                                            }
                                        } else {
                                            for (int i = 0; i < mVisibleComments.size(); i++) {
                                                if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                    if (mVisibleComments.get(i).isExpanded()) {
                                                        int placeholderPositionHint = i + mVisibleComments.get(i).getChildren().size();
                                                        int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), placeholderPositionHint);

                                                        if (placeholderPosition != -1) {
                                                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                            mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                            ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);

                                                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                            if (mIsSingleCommentThreadMode) {
                                                                notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                            } else {
                                                                notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                            }
                                                        }
                                                    }

                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);
                                                    mVisibleComments.get(i).addChildren(topLevelComments);
                                                    if (mIsSingleCommentThreadMode) {
                                                        notifyItemChanged(i + 1);
                                                    } else {
                                                        notifyItemChanged(i);
                                                    }

                                                    break;
                                                }
                                            }
                                        }
                                    }

                                    @Override
                                    public void onFetchMoreCommentFailed() {
                                        int currentParentPosition = findCommentPosition(parentComment.getFullName(), parentPosition);
                                        if (currentParentPosition == -1) {
                                            // note: returning here is probably a mistake, because
                                            // parent is just not visible, but it can still exist in the comments tree.
                                            return;
                                        }
                                        Comment currentParentComment = mVisibleComments.get(currentParentPosition);

                                        if (currentParentComment.isExpanded()) {
                                            int placeholderPositionHint = currentParentPosition + currentParentComment.getChildren().size();
                                            int placeholderPosition = findLoadMoreCommentsPlaceholderPosition(parentComment.getFullName(), placeholderPositionHint);

                                            if (placeholderPosition != -1) {
                                                mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                            }
                                            ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                        }
                                        currentParentComment.getChildren().get(currentParentComment.getChildren().size() - 1)
                                                .setLoadingMoreChildren(false);
                                        currentParentComment.getChildren().get(currentParentComment.getChildren().size() - 1)
                                                .setLoadMoreChildrenFailed(true);
                                    }
                                });
                    }
                });
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setOnClickListener(view -> {
                    Comment comment = getCurrentComment(holder);
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

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    private int getParentPosition(int position) {
        if (position >= 0 && position < mVisibleComments.size()) {
            int childDepth = mVisibleComments.get(position).getDepth();
            for (int i = position; i >= 0; i--) {
                if (mVisibleComments.get(i).getDepth() < childDepth) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Find position of comment with given {@code fullName} and
     * {@link Comment#NOT_PLACEHOLDER} placeholder type
     * @return position of the placeholder or -1 if not found
     */
    private int findCommentPosition(String fullName, int positionHint) {
        return findCommentPosition(fullName, positionHint, Comment.NOT_PLACEHOLDER);
    }

    /**
     * Find position of comment with given {@code fullName} and
     * {@link Comment#PLACEHOLDER_LOAD_MORE_COMMENTS} placeholder type
     * @return position of the placeholder or -1 if not found
     */
    private int findLoadMoreCommentsPlaceholderPosition(String fullName, int positionHint) {
        return findCommentPosition(fullName, positionHint, Comment.PLACEHOLDER_LOAD_MORE_COMMENTS);
    }

    private int findCommentPosition(String fullName, int positionHint, int placeholderType) {
        if (0 <= positionHint && positionHint < mVisibleComments.size()
                && mVisibleComments.get(positionHint).getFullName().equals(fullName)
                && mVisibleComments.get(positionHint).getPlaceholderType() == placeholderType) {
            return positionHint;
        }

        for (int i = 0; i < mVisibleComments.size(); i++) {
            Comment comment = mVisibleComments.get(i);
            if (comment.getFullName().equals(fullName) && comment.getPlaceholderType() == placeholderType) {
                return i;
            }
        }
        return -1;
    }

    private void expandChildren(ArrayList<Comment> comments, ArrayList<Comment> newList) {
        if (comments != null && comments.size() > 0) {
            for (Comment comment : comments) {
                newList.add(comment);
                expandChildren(comment.getChildren(), newList);
                comment.setExpanded(true);
            }
        }
    }

    private void collapseChildren(int position) {
        mVisibleComments.get(position).setExpanded(false);
        int depth = mVisibleComments.get(position).getDepth();
        int allChildrenSize = 0;
        for (int i = position + 1; i < mVisibleComments.size(); i++) {
            if (mVisibleComments.get(i).getDepth() > depth) {
                allChildrenSize++;
            } else {
                break;
            }
        }

        if (allChildrenSize > 0) {
            mVisibleComments.subList(position + 1, position + 1 + allChildrenSize).clear();
        }
        if (mIsSingleCommentThreadMode) {
            notifyItemRangeRemoved(position + 2, allChildrenSize);
            if (mFullyCollapseComment) {
                notifyItemChanged(position + 1);
            }
        } else {
            notifyItemRangeRemoved(position + 1, allChildrenSize);
            if (mFullyCollapseComment) {
                notifyItemChanged(position);
            }
        }
    }

    public void addComments(@NonNull ArrayList<Comment> comments, boolean hasMoreComments) {
        if (mVisibleComments.size() == 0) {
            isInitiallyLoading = false;
            isInitiallyLoadingFailed = false;
            if (comments.size() == 0) {
                notifyItemChanged(0);
            } else {
                notifyItemRemoved(0);
            }
        }

        int sizeBefore = mVisibleComments.size();
        mVisibleComments.addAll(comments);
        if (mIsSingleCommentThreadMode) {
            notifyItemRangeInserted(sizeBefore, comments.size() + 1);
        } else {
            notifyItemRangeInserted(sizeBefore, comments.size());
        }

        if (mHasMoreComments != hasMoreComments) {
            if (hasMoreComments) {
                if (mIsSingleCommentThreadMode) {
                    notifyItemInserted(mVisibleComments.size() + 1);
                } else {
                    notifyItemInserted(mVisibleComments.size());
                }
            } else {
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(mVisibleComments.size() + 1);
                } else {
                    notifyItemRemoved(mVisibleComments.size());
                }
            }
        }
        mHasMoreComments = hasMoreComments;
    }

    public void addComment(Comment comment) {
        if (mVisibleComments.size() == 0 || isInitiallyLoadingFailed) {
            notifyItemRemoved(1);
        }

        mVisibleComments.add(0, comment);

        if (isInitiallyLoading) {
            notifyItemInserted(1);
        } else {
            notifyItemInserted(0);
        }
    }

    public void addChildComment(Comment comment, String parentFullname, int parentPosition) {
        if (!parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
            for (int i = 0; i < mVisibleComments.size(); i++) {
                if (parentFullname.equals(mVisibleComments.get(i).getFullName())) {
                    parentPosition = i;
                    break;
                }
            }
        }

        mVisibleComments.get(parentPosition).addChild(comment);
        mVisibleComments.get(parentPosition).setHasReply(true);
        if (!mVisibleComments.get(parentPosition).isExpanded()) {
            ArrayList<Comment> newList = new ArrayList<>();
            expandChildren(mVisibleComments.get(parentPosition).getChildren(), newList);
            mVisibleComments.get(parentPosition).setExpanded(true);
            mVisibleComments.addAll(parentPosition + 1, newList);
            if (mIsSingleCommentThreadMode) {
                notifyItemChanged(parentPosition + 1);
                notifyItemRangeInserted(parentPosition + 2, newList.size());
            } else {
                notifyItemChanged(parentPosition);
                notifyItemRangeInserted(parentPosition + 1, newList.size());
            }
        } else {
            mVisibleComments.add(parentPosition + 1, comment);
            if (mIsSingleCommentThreadMode) {
                notifyItemChanged(parentPosition + 1);
                notifyItemInserted(parentPosition + 2);
            } else {
                notifyItemChanged(parentPosition);
                notifyItemInserted(parentPosition + 1);
            }
        }
    }

    public void setSingleComment(String singleCommentId, boolean isSingleCommentThreadMode) {
        mSingleCommentId = singleCommentId;
        mIsSingleCommentThreadMode = isSingleCommentThreadMode;
    }

    public ArrayList<Comment> getVisibleComments() {
        return mVisibleComments;
    }

    public void initiallyLoading() {
        resetCommentSearchIndex();
        int removedItemCount = getItemCount();
        mVisibleComments.clear();
        notifyItemRangeRemoved(0, removedItemCount);
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        notifyItemInserted(0);
    }

    public void initiallyLoadCommentsFailed() {
        isInitiallyLoading = false;
        isInitiallyLoadingFailed = true;
        notifyItemChanged(0);
    }

    public void loadMoreCommentsFailed() {
        loadMoreCommentsFailed = true;
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(mVisibleComments.size() + 1);
        } else {
            notifyItemChanged(mVisibleComments.size());
        }
    }

    public void editComment(String commentAuthor, String commentContentMarkdown, int position) {
        if (commentAuthor != null) {
            mVisibleComments.get(position).setAuthor(commentAuthor);
        }

        mVisibleComments.get(position).setSubmittedByAuthor(mVisibleComments.get(position).isSubmitter());

        mVisibleComments.get(position).setCommentMarkdown(commentContentMarkdown);
        if (mIsSingleCommentThreadMode) {
            notifyItemChanged(position + 1);
        } else {
            notifyItemChanged(position);
        }
    }

    public void deleteComment(int position) {
        if (mVisibleComments != null && position >= 0 && position < mVisibleComments.size()) {
            if (mVisibleComments.get(position).hasReply()) {
                mVisibleComments.get(position).setAuthor("[deleted]");
                mVisibleComments.get(position).setCommentMarkdown("[deleted]");
                if (mIsSingleCommentThreadMode) {
                    notifyItemChanged(position + 1);
                } else {
                    notifyItemChanged(position);
                }
            } else {
                mVisibleComments.remove(position);
                if (mIsSingleCommentThreadMode) {
                    notifyItemRemoved(position + 1);
                } else {
                    notifyItemRemoved(position);
                }
            }
        }
    }

    public int getNextParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition + 1; i - 1 < mVisibleComments.size() && i - 1 >= 0; i++) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition + 1; i < mVisibleComments.size(); i++) {
                    if (mVisibleComments.get(i).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    public int getPreviousParentCommentPosition(int currentPosition) {
        if (mVisibleComments != null && !mVisibleComments.isEmpty()) {
            if (mIsSingleCommentThreadMode) {
                for (int i = currentPosition - 1; i - 1 >= 0; i--) {
                    if (mVisibleComments.get(i - 1).getDepth() == 0) {
                        return i;
                    }
                }
            } else {
                for (int i = currentPosition - 1; i >= 0; i--) {
                    if (mVisibleComments.get(i).getDepth() == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
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
        Comment comment = getCurrentComment(position);
        if (comment != null) {
            comment.setSaved(isSaved);
        }
    }

    public int getSearchCommentIndex() {
        return mSearchCommentIndex;
    }

    public void highlightSearchResult(int searchCommentIndex) {
        mSearchCommentIndex = searchCommentIndex;
    }

    public void resetCommentSearchIndex() {
        mSearchCommentIndex = -1;
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

    @Override
    public int getItemCount() {
        if (isInitiallyLoading) {
            return 1;
        }

        if (isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            return mIsSingleCommentThreadMode ? 2 : 1;
        }

        if (mHasMoreComments || loadMoreCommentsFailed) {
            if (mIsSingleCommentThreadMode) {
                return mVisibleComments.size() + 2;
            } else {
                return mVisibleComments.size() + 1;
            }
        }

        if (mIsSingleCommentThreadMode) {
            return mVisibleComments.size() + 1;
        } else {
            return mVisibleComments.size();
        }
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        mEmotePlugin.setDataSavingMode(dataSavingMode);
        mImageAndGifEntry.setDataSavingMode(dataSavingMode);
    }

    public interface CommentRecyclerViewAdapterCallback {
        void retryFetchingComments();

        void retryFetchingMoreComments();

        SortType.Type getSortType();
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
                    ((ViewPostDetailActivity) mActivity).lockSwipeRightToGoBack();
                }

                @Override
                public void unlockSwipe() {
                    ((ViewPostDetailActivity) mActivity).unlockSwipeRightToGoBack();
                }
            });
            commentMarkdownView.setLayoutManager(linearLayoutManager);
            mMarkwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(mImageAndGifEntry);
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
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Toast.makeText(view.getContext(), view.getContext().getString(R.string.edited_time, mShowElapsedTime ?
                            Utils.getElapsedTime(mActivity, comment.getEditedTimeMillis()) :
                            Utils.getFormattedTime(mLocale, comment.getEditedTimeMillis(), mTimeFormatPattern)
                    ), Toast.LENGTH_SHORT).show();
                }
            });

            moreButton.setOnClickListener(view -> {
                getItemCount();
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    if (mIsSingleCommentThreadMode) {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition() - 1);
                    } else {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    }
                    bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_IS_NSFW, mPost.isNSFW());
                    if (comment.getDepth() >= mDepthThreshold) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_SHOW_REPLY_AND_SAVE_OPTION, true);
                    }
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(mActivity.getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
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

                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Intent intent = new Intent(mActivity, CommentActivity.class);
                    intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY, comment.getCommentMarkdown());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_BODY_KEY, comment.getCommentRawText());
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
                    intent.putExtra(CommentActivity.EXTRA_SUBREDDIT_NAME_KEY, mPost.getSubredditName());
                    intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

                    int parentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, parentPosition);
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

                Comment comment = getCurrentComment(this);
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

                Comment comment = getCurrentComment(this);
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
                Comment comment = getCurrentComment(this);
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
                Comment comment = getCurrentComment(this);
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
                    int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    Comment comment = getCurrentComment(this);
                    if (comment != null) {
                        if (mVisibleComments.get(commentPosition).isExpanded()) {
                            collapseChildren(commentPosition);
                            if (comment.getChildCount() > 0) {
                                expandButton.setText("+" + comment.getChildCount());
                            }
                            expandButton.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
                        } else {
                            comment.setExpanded(true);
                            ArrayList<Comment> newList = new ArrayList<>();
                            expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList);
                            mVisibleComments.get(commentPosition).setExpanded(true);
                            mVisibleComments.addAll(commentPosition + 1, newList);

                            if (mIsSingleCommentThreadMode) {
                                notifyItemRangeInserted(commentPosition + 2, newList.size());
                            } else {
                                notifyItemRangeInserted(commentPosition + 1, newList.size());
                            }
                            if (mAlwaysShowChildCommentCount && comment.getChildCount() > 0) {
                                expandButton.setText("+" + comment.getChildCount());
                            } else {
                                expandButton.setText("");
                            }
                            expandButton.setCompoundDrawablesWithIntrinsicBounds(collapseDrawable, null, null, null);
                        }
                    }
                } else if (mFullyCollapseComment) {
                    int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                    if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                        collapseChildren(commentPosition);
                    }
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

    @Nullable
    private Comment getCurrentComment(RecyclerView.ViewHolder holder) {
        return getCurrentComment(holder.getBindingAdapterPosition());
    }

    @Nullable
    private Comment getCurrentComment(int position) {
        if (mIsSingleCommentThreadMode) {
            if (position - 1 >= 0 && position - 1 < mVisibleComments.size()) {
                return mVisibleComments.get(position - 1);
            }
        } else {
            if (position >= 0 && position < mVisibleComments.size()) {
                return mVisibleComments.get(position);
            }
        }

        return null;
    }

    class CommentFullyCollapsedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_indentation_item_comment_fully_collapsed)
        CommentIndentationView commentIndentationView;
        @BindView(R.id.author_icon_image_view_item_comment_fully_collapsed)
        ImageView authorIconImageView;
        @BindView(R.id.user_name_text_view_item_comment_fully_collapsed)
        TextView usernameTextView;
        @BindView(R.id.child_count_text_view_item_comment_fully_collapsed)
        TextView childCountTextView;
        @BindView(R.id.score_text_view_item_comment_fully_collapsed)
        TextView scoreTextView;
        @BindView(R.id.time_text_view_item_comment_fully_collapsed)
        TextView commentTimeTextView;
        @BindView(R.id.divider_item_comment_fully_collapsed)
        View commentDivider;

        public CommentFullyCollapsedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mActivity.typeface != null) {
                usernameTextView.setTypeface(mActivity.typeface);
                childCountTextView.setTypeface(mActivity.typeface);
                scoreTextView.setTypeface(mActivity.typeface);
                commentTimeTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mFullyCollapsedCommentBackgroundColor);
            usernameTextView.setTextColor(mUsernameColor);
            childCountTextView.setTextColor(mSecondaryTextColor);
            scoreTextView.setTextColor(mSecondaryTextColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    commentDivider.setBackgroundColor(mDividerColor);
                    commentDivider.setVisibility(View.VISIBLE);
                }
            }

            if (mShowAuthorAvatar) {
                authorIconImageView.setVisibility(View.VISIBLE);
            } else {
                usernameTextView.setPaddingRelative(0, usernameTextView.getPaddingTop(), usernameTextView.getPaddingEnd(), usernameTextView.getPaddingBottom());
            }

            itemView.setOnClickListener(view -> {
                int commentPosition = mIsSingleCommentThreadMode ? getBindingAdapterPosition() - 1 : getBindingAdapterPosition();
                if (commentPosition >= 0 && commentPosition < mVisibleComments.size()) {
                    Comment comment = getCurrentComment(this);
                    if (comment != null) {
                        comment.setExpanded(true);
                        ArrayList<Comment> newList = new ArrayList<>();
                        expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList);
                        mVisibleComments.get(commentPosition).setExpanded(true);
                        mVisibleComments.addAll(commentPosition + 1, newList);

                        if (mIsSingleCommentThreadMode) {
                            notifyItemChanged(commentPosition + 1);
                            notifyItemRangeInserted(commentPosition + 2, newList.size());
                        } else {
                            notifyItemChanged(commentPosition);
                            notifyItemRangeInserted(commentPosition + 1, newList.size());
                        }
                    }
                }
            });

            itemView.setOnLongClickListener(view -> {
                itemView.performClick();
                return true;
            });
        }
    }

    class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_indentation_item_load_more_comments_placeholder)
        CommentIndentationView commentIndentationView;
        @BindView(R.id.placeholder_text_view_item_load_more_comments)
        TextView placeholderTextView;
        @BindView(R.id.divider_item_load_more_comments_placeholder)
        View commentDivider;

        LoadMoreChildCommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mShowCommentDivider) {
                if (mDividerType == DIVIDER_NORMAL) {
                    commentDivider.setBackgroundColor(mDividerColor);
                    commentDivider.setVisibility(View.VISIBLE);
                }
            }

            if (mActivity.typeface != null) {
                placeholderTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            placeholderTextView.setTextColor(mPrimaryTextColor);
        }
    }

    class LoadCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.comment_progress_bar_item_load_comments)
        CircleProgressBar circleProgressBar;

        LoadCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            circleProgressBar.setBackgroundTintList(ColorStateList.valueOf(mCircularProgressBarBackgroundColor));
            circleProgressBar.setColorSchemeColors(mColorAccent);
        }
    }

    class LoadCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_load_comments_failed_placeholder)
        TextView errorTextView;

        LoadCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingComments());
            if (mActivity.typeface != null) {
                errorTextView.setTypeface(mActivity.typeface);
            }
            errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class NoCommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_no_comment_placeholder)
        TextView errorTextView;

        NoCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                errorTextView.setTypeface(mActivity.typeface);
            }
            errorTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class IsLoadingMoreCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_comment_footer_loading)
        ProgressBar progressbar;

        IsLoadingMoreCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressbar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }

    class LoadMoreCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_comment_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_comment_footer_error)
        Button retryButton;

        LoadMoreCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                errorTextView.setTypeface(mActivity.typeface);
                retryButton.setTypeface(mActivity.typeface);
            }
            errorTextView.setText(R.string.load_comments_failed);
            retryButton.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingMoreComments());
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
        }
    }

    class ViewAllCommentsViewHolder extends RecyclerView.ViewHolder {

        ViewAllCommentsViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setOnClickListener(view -> {
                if (mActivity != null && mActivity instanceof ViewPostDetailActivity) {
                    mIsSingleCommentThreadMode = false;
                    mSingleCommentId = null;
                    notifyItemRemoved(0);
                    mFragment.changeToNormalThreadMode();
                }
            });

            if (mActivity.typeface != null) {
                ((TextView) itemView).setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundTintList(ColorStateList.valueOf(mCommentBackgroundColor));
            ((TextView) itemView).setTextColor(mColorAccent);
        }
    }
}
