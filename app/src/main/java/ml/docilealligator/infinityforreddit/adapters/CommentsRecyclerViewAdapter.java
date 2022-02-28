package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
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
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.html.tag.SuperScriptHandler;
import io.noties.markwon.inlineparser.AutolinkInlineProcessor;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.movement.MovementMethodPlugin;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import me.saket.bettermovementmethod.BetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.CommentActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UrlMenuBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.FetchComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.CommentIndentationView;
import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;
import ml.docilealligator.infinityforreddit.fragments.ViewPostDetailFragment;
import ml.docilealligator.infinityforreddit.markdown.SpoilerParserPlugin;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_FIRST_LOADING = 9;
    private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 10;
    private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 11;
    private static final int VIEW_TYPE_COMMENT = 12;
    private static final int VIEW_TYPE_COMMENT_FULLY_COLLAPSED = 13;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 14;
    private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 15;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 16;
    private static final int VIEW_TYPE_VIEW_ALL_COMMENTS = 17;

    private BaseActivity mActivity;
    private ViewPostDetailFragment mFragment;
    private Executor mExecutor;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private Markwon mCommentMarkwon;
    private String mAccessToken;
    private String mAccountName;
    private Post mPost;
    private ArrayList<Comment> mVisibleComments;
    private Locale mLocale;
    private RequestManager mGlide;
    private String mSingleCommentId;
    private boolean mIsSingleCommentThreadMode;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private boolean mExpandChildren;
    private boolean mCommentToolbarHidden;
    private boolean mCommentToolbarHideOnClick;
    private boolean mSwapTapAndLong;
    private boolean mShowCommentDivider;
    private boolean mShowAbsoluteNumberOfVotes;
    private boolean mFullyCollapseComment;
    private boolean mShowOnlyOneCommentLevelIndicator;
    private boolean mHideCommentAwards;
    private boolean mShowAuthorAvatar;
    private boolean mAlwaysShowChildCommentCount;
    private int mDepthThreshold;
    private CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private boolean isInitiallyLoading;
    private boolean isInitiallyLoadingFailed;
    private boolean mHasMoreComments;
    private boolean loadMoreCommentsFailed;
    private Drawable expandDrawable;
    private Drawable collapseDrawable;

    private int mColorPrimaryLightTheme;
    private int mColorAccent;
    private int mCircularProgressBarBackgroundColor;
    private int mSecondaryTextColor;
    private int mPrimaryTextColor;
    private int mCommentTextColor;
    private int mCommentBackgroundColor;
    private int mDividerColor;
    private int mUsernameColor;
    private int mSubmitterColor;
    private int mModeratorColor;
    private int mCurrentUserColor;
    private int mAuthorFlairTextColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mSingleCommentThreadBackgroundColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mButtonTextColor;
    private int mPostIconAndInfoColor;
    private int mCommentIconAndInfoColor;
    private int mFullyCollapsedCommentBackgroundColor;
    private int mAwardedCommentBackgroundColor;
    private int[] verticalBlockColors;

    private Drawable mCommentIcon;

    private int mSearchCommentIndex = -1;

    public CommentsRecyclerViewAdapter(BaseActivity activity, ViewPostDetailFragment fragment,
                                       CustomThemeWrapper customThemeWrapper,
                                       Executor executor, Retrofit retrofit, Retrofit oauthRetrofit,
                                       String accessToken, String accountName,
                                       Post post, Locale locale, String singleCommentId,
                                       boolean isSingleCommentThreadMode,
                                       SharedPreferences sharedPreferences,
                                       CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        mActivity = activity;
        mFragment = fragment;
        mExecutor = executor;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mGlide = Glide.with(activity);
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mCommentTextColor = customThemeWrapper.getCommentColor();
        int commentSpoilerBackgroundColor = mCommentTextColor | 0xFF000000;
        int linkColor = customThemeWrapper.getLinkColor();
        mCommentMarkwon = Markwon.builder(mActivity)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                    plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                }))
                .usePlugin(HtmlPlugin.create(plugin -> {
                    plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @NonNull
                    @Override
                    public String processMarkdown(@NonNull String markdown) {
                        return Utils.fixSuperScript(markdown);
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
                })
                .usePlugin(SpoilerParserPlugin.create(mCommentTextColor, commentSpoilerBackgroundColor))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(MovementMethodPlugin.create(BetterLinkMovementMethod.newInstance().setOnLinkLongClickListener((textView, url) -> {
                    if (!activity.isDestroyed() && !activity.isFinishing()) {
                        UrlMenuBottomSheetFragment urlMenuBottomSheetFragment = new UrlMenuBottomSheetFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(UrlMenuBottomSheetFragment.EXTRA_URL, url);
                        urlMenuBottomSheetFragment.setArguments(bundle);
                        urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
                    }
                    return true;
                })))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
        mAccessToken = accessToken;
        mAccountName = accountName;
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
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
        mFullyCollapseComment = sharedPreferences.getBoolean(SharedPreferencesUtils.FULLY_COLLAPSE_COMMENT, false);
        mShowOnlyOneCommentLevelIndicator = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ONLY_ONE_COMMENT_LEVEL_INDICATOR, false);
        mHideCommentAwards = sharedPreferences.getBoolean(SharedPreferencesUtils.HIDE_COMMENT_AWARDS, false);
        mShowAuthorAvatar = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_AUTHOR_AVATAR, false);
        mAlwaysShowChildCommentCount = sharedPreferences.getBoolean(SharedPreferencesUtils.ALWAYS_SHOW_CHILD_COMMENT_COUNT, false);
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
        mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        mFullyCollapsedCommentBackgroundColor = customThemeWrapper.getFullyCollapsedCommentBackgroundColor();
        mAwardedCommentBackgroundColor = customThemeWrapper.getAwardedCommentBackgroundColor();

        verticalBlockColors = new int[] {
                customThemeWrapper.getCommentVerticalBarColor1(),
                customThemeWrapper.getCommentVerticalBarColor2(),
                customThemeWrapper.getCommentVerticalBarColor3(),
                customThemeWrapper.getCommentVerticalBarColor4(),
                customThemeWrapper.getCommentVerticalBarColor5(),
                customThemeWrapper.getCommentVerticalBarColor6(),
                customThemeWrapper.getCommentVerticalBarColor7(),
        };

        mCommentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
        if (mCommentIcon != null) {
            DrawableCompat.setTint(mCommentIcon, mPostIconAndInfoColor);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (mVisibleComments.size() == 0) {
            if (isInitiallyLoading) {
                return VIEW_TYPE_FIRST_LOADING;
            } else if (isInitiallyLoadingFailed) {
                return VIEW_TYPE_FIRST_LOADING_FAILED;
            } else {
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
                return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
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
        if (holder instanceof CommentViewHolder) {
            Comment comment = getCurrentComment(position);
            if (comment != null) {
                if (mIsSingleCommentThreadMode && comment.getId().equals(mSingleCommentId)) {
                    holder.itemView.setBackgroundColor(mSingleCommentThreadBackgroundColor);
                } else if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                    holder.itemView.setBackgroundColor(mAwardedCommentBackgroundColor);
                }

                String authorPrefixed = "u/" + comment.getAuthor();
                ((CommentViewHolder) holder).authorTextView.setText(authorPrefixed);

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).authorFlairTextView, comment.getAuthorFlairHTML(), true);
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (comment.isSubmitter()) {
                    ((CommentViewHolder) holder).authorTextView.setTextColor(mSubmitterColor);
                    Drawable submitterDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_mic_14dp, mSubmitterColor);
                    ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            submitterDrawable, null, null, null);
                } else if (comment.isModerator()) {
                    ((CommentViewHolder) holder).authorTextView.setTextColor(mModeratorColor);
                    Drawable moderatorDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_verified_user_14dp, mModeratorColor);
                    ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            moderatorDrawable, null, null, null);
                } else if (comment.getAuthor().equals(mAccountName)) {
                    ((CommentViewHolder) holder).authorTextView.setTextColor(mCurrentUserColor);
                    Drawable currentUserDrawable = Utils.getTintedDrawable(mActivity, R.drawable.ic_current_user_14dp, mCurrentUserColor);
                    ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(
                            currentUserDrawable, null, null, null);
                }

                if (comment.getAuthorIconUrl() == null) {
                    mFragment.loadIcon(comment.getAuthor(), (authorName, iconUrl) -> {
                        if (authorName.equals(comment.getAuthor())) {
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentViewHolder) holder).authorIconImageView);
                            comment.setAuthorIconUrl(iconUrl);
                        }
                    });
                } else {
                    mGlide.load(comment.getAuthorIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(((CommentViewHolder) holder).authorIconImageView);
                }

                if (mShowElapsedTime) {
                    ((CommentViewHolder) holder).commentTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }

                if (mCommentToolbarHidden) {
                    ((CommentViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = 0;
                    ((CommentViewHolder) holder).topScoreTextView.setVisibility(View.VISIBLE);
                } else {
                    ((CommentViewHolder) holder).bottomConstraintLayout.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
                    ((CommentViewHolder) holder).topScoreTextView.setVisibility(View.GONE);
                }

                if (!mHideCommentAwards && comment.getAwards() != null && !comment.getAwards().equals("")) {
                    ((CommentViewHolder) holder).awardsTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).awardsTextView, comment.getAwards(), true);
                }

                mCommentMarkwon.setMarkdown(((CommentViewHolder) holder).commentMarkdownView, comment.getCommentMarkdown());
                ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType()));
                ((CommentViewHolder) holder).topScoreTextView.setText(mActivity.getString(R.string.top_score,
                        Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType())));

                ((CommentViewHolder) holder).commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentViewHolder) holder).commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);
                if (comment.getDepth() >= mDepthThreshold) {
                    ((CommentViewHolder) holder).saveButton.setVisibility(View.GONE);
                    ((CommentViewHolder) holder).replyButton.setVisibility(View.GONE);
                } else {
                    ((CommentViewHolder) holder).saveButton.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).replyButton.setVisibility(View.VISIBLE);
                }

                if (comment.hasReply()) {
                    if (comment.getChildCount() > 0 && (mAlwaysShowChildCommentCount || !comment.isExpanded())) {
                        ((CommentViewHolder) holder).expandButton.setText("+" + comment.getChildCount());
                    }
                    if (comment.isExpanded()) {
                        ((CommentViewHolder) holder).expandButton.setCompoundDrawablesWithIntrinsicBounds(collapseDrawable, null, null, null);
                    } else {
                        ((CommentViewHolder) holder).expandButton.setCompoundDrawablesWithIntrinsicBounds(expandDrawable, null, null, null);
                    }
                    ((CommentViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
                }

                switch (comment.getVoteType()) {
                    case Comment.VOTE_TYPE_UPVOTE:
                        ((CommentViewHolder) holder).upvoteButton
                                .setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        ((CommentViewHolder) holder).topScoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case Comment.VOTE_TYPE_DOWNVOTE:
                        ((CommentViewHolder) holder).downvoteButton
                                .setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        ((CommentViewHolder) holder).topScoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (mPost.isArchived()) {
                    ((CommentViewHolder) holder).replyButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).upvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                    ((CommentViewHolder) holder).downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                }

                if (mPost.isLocked()) {
                    ((CommentViewHolder) holder).replyButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor,
                                    PorterDuff.Mode.SRC_IN);
                }

                if (comment.isSaved()) {
                    ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                if (position == mSearchCommentIndex) {
                    holder.itemView.setBackgroundColor(Color.parseColor("#03A9F4"));
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
                            mGlide.load(iconUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((CommentFullyCollapsedViewHolder) holder).authorIconImageView);
                            comment.setAuthorIconUrl(iconUrl);
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
                ((CommentFullyCollapsedViewHolder) holder).scoreTextView.setText(mActivity.getString(R.string.top_score,
                        Utils.getNVotes(mShowAbsoluteNumberOfVotes, comment.getScore() + comment.getVoteType())));
                ((CommentFullyCollapsedViewHolder) holder).commentIndentationView.setShowOnlyOneDivider(mShowOnlyOneCommentLevelIndicator);
                ((CommentFullyCollapsedViewHolder) holder).commentIndentationView.setLevelAndColors(comment.getDepth(), verticalBlockColors);
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

                        Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
                        FetchComment.fetchMoreComment(mExecutor, new Handler(), retrofit, mAccessToken,
                                parentComment.getMoreChildrenFullnames(),
                                parentComment.getMoreChildrenStartingIndex(), parentComment.getDepth() + 1,
                                mExpandChildren, new FetchComment.FetchMoreCommentListener() {
                                    @Override
                                    public void onFetchMoreCommentSuccess(ArrayList<Comment> expandedComments,
                                                                          int childrenStartingIndex) {
                                        if (mVisibleComments.size() > parentPosition
                                                && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                            if (mVisibleComments.get(parentPosition).isExpanded()) {
                                                if (mVisibleComments.get(parentPosition).getChildren().size() > childrenStartingIndex) {
                                                    mVisibleComments.get(parentPosition).setMoreChildrenStartingIndex(childrenStartingIndex);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);

                                                    int placeholderPosition = commentPosition;
                                                    if (mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                        for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                            if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                                placeholderPosition = i;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                    mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                    ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);

                                                    mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                    if (mIsSingleCommentThreadMode) {
                                                        notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                                    } else {
                                                        notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                                    }
                                                } else {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();

                                                    int placeholderPosition = commentPosition;
                                                    if (mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                        for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                            if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                                placeholderPosition = i;
                                                                break;
                                                            }
                                                        }
                                                    }

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
                                            } else {
                                                if (mVisibleComments.get(parentPosition).hasReply() && mVisibleComments.get(parentPosition).getChildren().size() <= childrenStartingIndex) {
                                                    mVisibleComments.get(parentPosition).getChildren()
                                                            .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                                    mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();
                                                }
                                            }

                                            mVisibleComments.get(parentPosition).addChildren(expandedComments);
                                            if (mIsSingleCommentThreadMode) {
                                                notifyItemChanged(parentPosition + 1);
                                            } else {
                                                notifyItemChanged(parentPosition);
                                            }
                                        } else {
                                            for (int i = 0; i < mVisibleComments.size(); i++) {
                                                if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                    if (mVisibleComments.get(i).isExpanded()) {
                                                        int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();

                                                        if (!mVisibleComments.get(i).getFullName()
                                                                .equals(mVisibleComments.get(placeholderPosition).getFullName())) {
                                                            for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                                                if (mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                                    placeholderPosition = j;
                                                                }
                                                            }
                                                        }

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

                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadingMoreChildren(false);
                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                            .setLoadMoreChildrenFailed(false);
                                                    mVisibleComments.get(i).addChildren(expandedComments);
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
                                        if (parentPosition < mVisibleComments.size()
                                                && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                            if (mVisibleComments.get(parentPosition).isExpanded()) {
                                                int commentPosition = mIsSingleCommentThreadMode ? holder.getBindingAdapterPosition() - 1 : holder.getBindingAdapterPosition();
                                                int placeholderPosition = commentPosition;
                                                if (commentPosition >= mVisibleComments.size() || commentPosition < 0 || !mVisibleComments.get(commentPosition).getFullName().equals(parentComment.getFullName())) {
                                                    for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                        if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                            placeholderPosition = i;
                                                            break;
                                                        }
                                                    }
                                                }

                                                if (placeholderPosition >= mVisibleComments.size() || placeholderPosition < 0) {
                                                    mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                    mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                                }
                                                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                            }

                                            mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                    .setLoadingMoreChildren(false);
                                            mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                    .setLoadMoreChildrenFailed(true);
                                        } else {
                                            for (int i = 0; i < mVisibleComments.size(); i++) {
                                                if (mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                    if (mVisibleComments.get(i).isExpanded()) {
                                                        int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();
                                                        if (!mVisibleComments.get(placeholderPosition).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                            for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                                                if (mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                                    placeholderPosition = j;
                                                                    break;
                                                                }
                                                            }
                                                        }

                                                        mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                        mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                                        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                                    }

                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadingMoreChildren(false);
                                                    mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadMoreChildrenFailed(true);

                                                    break;
                                                }
                                            }
                                        }
                                    }
                                });
                    }
                });
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setOnClickListener(view -> {
                    Comment comment = getCurrentComment(position);
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

    private void expandChildren(ArrayList<Comment> comments, ArrayList<Comment> newList, int position) {
        if (comments != null && comments.size() > 0) {
            newList.addAll(position, comments);
            for (int i = 0; i < comments.size(); i++) {
                position++;
                if (comments.get(i).getChildren() != null && comments.get(i).getChildren().size() > 0) {
                    expandChildren(comments.get(i).getChildren(), newList, position);
                    position = position + comments.get(i).getChildren().size();
                }
                comments.get(i).setExpanded(true);
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
            expandChildren(mVisibleComments.get(parentPosition).getChildren(), newList, 0);
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
        if (commentAuthor != null)
            mVisibleComments.get(position).setAuthor(commentAuthor);

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
        if (viewHolder instanceof CommentViewHolder) {
            if (direction == ItemTouchHelper.LEFT || direction == ItemTouchHelper.START) {
                if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeLeftAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentViewHolder) viewHolder).downvoteButton.performClick();
                }
            } else {
                if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_UPVOTE) {
                    ((CommentViewHolder) viewHolder).upvoteButton.performClick();
                } else if (swipeRightAction == SharedPreferencesUtils.SWIPE_ACITON_DOWNVOTE) {
                    ((CommentViewHolder) viewHolder).downvoteButton.performClick();
                }
            }
        }
    }

    public void giveAward(String awardsHTML, int awardCount, int position) {
        position = mIsSingleCommentThreadMode ? position + 1 : position;
        Comment comment = getCurrentComment(position);
        if (comment != null) {
            comment.addAwards(awardsHTML);
            notifyItemChanged(position);
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
        if (holder instanceof CommentViewHolder) {
            holder.itemView.setBackgroundColor(mCommentBackgroundColor);
            ((CommentViewHolder) holder).authorTextView.setTextColor(mUsernameColor);
            ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).authorTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            mGlide.clear(((CommentViewHolder) holder).authorIconImageView);
            ((CommentViewHolder) holder).topScoreTextView.setTextColor(mSecondaryTextColor);
            ((CommentViewHolder) holder).awardsTextView.setText("");
            ((CommentViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
            ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
            ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).expandButton.setText("");
            ((CommentViewHolder) holder).replyButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public int getItemCount() {
        if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            return 1;
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

    public interface CommentRecyclerViewAdapterCallback {
        void retryFetchingComments();

        void retryFetchingMoreComments();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout_item_comment)
        LinearLayout linearLayout;
        @BindView(R.id.author_icon_image_view_item_post_comment)
        ImageView authorIconImageView;
        @BindView(R.id.author_text_view_item_post_comment)
        TextView authorTextView;
        @BindView(R.id.author_flair_text_view_item_post_comment)
        TextView authorFlairTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment)
        TextView commentTimeTextView;
        @BindView(R.id.top_score_text_view_item_post_comment)
        TextView topScoreTextView;
        @BindView(R.id.awards_text_view_item_comment)
        TextView awardsTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment)
        SpoilerOnClickTextView commentMarkdownView;
        @BindView(R.id.bottom_constraint_layout_item_post_comment)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.up_vote_button_item_post_comment)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment)
        TextView scoreTextView;
        @BindView(R.id.down_vote_button_item_post_comment)
        ImageView downvoteButton;
        @BindView(R.id.placeholder_item_post_comment)
        View placeholder;
        @BindView(R.id.more_button_item_post_comment)
        ImageView moreButton;
        @BindView(R.id.save_button_item_post_comment)
        ImageView saveButton;
        @BindView(R.id.expand_button_item_post_comment)
        TextView expandButton;
        @BindView(R.id.reply_button_item_post_comment)
        ImageView replyButton;
        @BindView(R.id.vertical_block_indentation_item_comment)
        CommentIndentationView commentIndentationView;
        @BindView(R.id.divider_item_comment)
        View commentDivider;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

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
                commentDivider.setBackgroundColor(mDividerColor);
                commentDivider.setVisibility(View.VISIBLE);
            }

            if (mActivity.typeface != null) {
                authorTextView.setTypeface(mActivity.typeface);
                commentTimeTextView.setTypeface(mActivity.typeface);
                authorFlairTextView.setTypeface(mActivity.typeface);
                topScoreTextView.setTypeface(mActivity.typeface);
                awardsTextView.setTypeface(mActivity.typeface);
                scoreTextView.setTypeface(mActivity.typeface);
                expandButton.setTypeface(mActivity.typeface);
            }
            if (mActivity.contentTypeface != null) {
                commentMarkdownView.setTypeface(mActivity.contentTypeface);
            }

            if (mShowAuthorAvatar) {
                authorIconImageView.setVisibility(View.VISIBLE);
            } else {
                ((ConstraintLayout.LayoutParams) authorTextView.getLayoutParams()).leftMargin = 0;
                ((ConstraintLayout.LayoutParams) authorFlairTextView.getLayoutParams()).leftMargin = 0;
            }

            itemView.setBackgroundColor(mCommentBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);
            commentMarkdownView.setTextColor(mCommentTextColor);
            authorFlairTextView.setTextColor(mAuthorFlairTextColor);
            topScoreTextView.setTextColor(mSecondaryTextColor);
            awardsTextView.setTextColor(mSecondaryTextColor);
            commentDivider.setBackgroundColor(mDividerColor);
            upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mCommentIconAndInfoColor);
            downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            moreButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            expandButton.setTextColor(mCommentIconAndInfoColor);
            saveButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
            replyButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

            authorFlairTextView.setOnClickListener(view -> authorTextView.performClick());

            moreButton.setOnClickListener(view -> {
                getItemCount();
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putString(CommentMoreBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    if (mIsSingleCommentThreadMode) {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition() - 1);
                    } else {
                        bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    }
                    bundle.putString(CommentMoreBottomSheetFragment.EXTRA_COMMENT_MARKDOWN, comment.getCommentMarkdown());
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
                if (mAccessToken == null) {
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
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY, comment.getCommentMarkdown());
                    intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, comment.getCommentRawText());
                    intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
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

                if (mAccessToken == null) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != Comment.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        upvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mUpvotedColor);
                        topScoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType()));
                    topScoreTextView.setText(mActivity.getString(R.string.top_score,
                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    comment.getScore() + comment.getVoteType())));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mUpvotedColor, PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mUpvotedColor);
                                    topScoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType()));
                                topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                        Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                comment.getScore() + comment.getVoteType())));
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                        }
                    }, comment.getFullName(), newVoteType, getBindingAdapterPosition());
                }
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

                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != Comment.VOTE_TYPE_DOWNVOTE) {
                        //Not downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        downvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mDownvotedColor);
                        topScoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                        topScoreTextView.setTextColor(mSecondaryTextColor);
                    }

                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType()));
                    topScoreTextView.setText(mActivity.getString(R.string.top_score,
                            Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    comment.getScore() + comment.getVoteType())));

                    int position = getBindingAdapterPosition();
                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_DOWNVOTE);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mDownvotedColor, PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mDownvotedColor);
                                    topScoreTextView.setTextColor(mDownvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    downvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                    topScoreTextView.setTextColor(mSecondaryTextColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setColorFilter(mCommentIconAndInfoColor, PorterDuff.Mode.SRC_IN);
                                scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                        comment.getScore() + comment.getVoteType()));
                                topScoreTextView.setText(mActivity.getString(R.string.top_score,
                                        Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                                comment.getScore() + comment.getVoteType())));
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
                                    saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
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
                                    saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                if (getBindingAdapterPosition() == position) {
                                    saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                }
                                Toast.makeText(mActivity, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            });

            authorTextView.setOnClickListener(view -> {
                Comment comment = getCurrentComment(this);
                if (comment != null) {
                    Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, comment.getAuthor());
                    mActivity.startActivity(intent);
                }
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
                            expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList, 0);
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
                    commentMarkdownView.setOnLongClickListener(hideToolbarOnLongClickListener);
                    commentTimeTextView.setOnLongClickListener(hideToolbarOnLongClickListener);
                }
                commentMarkdownView.setOnClickListener(view -> {
                    if (commentMarkdownView.isSpoilerOnClick()) {
                        commentMarkdownView.setSpoilerOnClick(false);
                        return;
                    }
                    expandComments();
                });
                itemView.setOnClickListener(view -> expandComments());
            } else {
                if (mCommentToolbarHideOnClick) {
                    commentMarkdownView.setOnClickListener(view -> {
                        if (commentMarkdownView.isSpoilerOnClick()) {
                            commentMarkdownView.setSpoilerOnClick(false);
                            return;
                        }
                        hideToolbar();
                    });
                    View.OnClickListener hideToolbarOnClickListener = view -> hideToolbar();
                    itemView.setOnClickListener(hideToolbarOnClickListener);
                    commentTimeTextView.setOnClickListener(hideToolbarOnClickListener);
                }
                commentMarkdownView.setOnLongClickListener(view -> {
                    if (commentMarkdownView.getSelectionStart() == -1 && commentMarkdownView.getSelectionEnd() == -1) {
                        expandComments();
                    }
                    return true;
                });
                itemView.setOnLongClickListener(view -> {
                    expandComments();
                    return true;
                });
            }
            commentMarkdownView.setHighlightColor(Color.TRANSPARENT);
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
                topScoreTextView.setVisibility(View.VISIBLE);
            }
            return true;
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
                commentDivider.setBackgroundColor(mDividerColor);
                commentDivider.setVisibility(View.VISIBLE);
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
                        expandChildren(mVisibleComments.get(commentPosition).getChildren(), newList, 0);
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
                commentDivider.setVisibility(View.VISIBLE);
            }

            if (mActivity.typeface != null) {
                placeholderTextView.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            placeholderTextView.setTextColor(mPrimaryTextColor);
            commentDivider.setBackgroundColor(mDividerColor);
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
                    mFragment.changeToNomalThreadMode();
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
