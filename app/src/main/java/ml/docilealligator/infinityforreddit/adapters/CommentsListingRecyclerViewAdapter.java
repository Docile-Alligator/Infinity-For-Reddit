package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.VoteThing;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewImageOrGifActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.activities.ViewSubredditDetailActivity;
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
import ml.docilealligator.infinityforreddit.markdown.EvenBetterLinkMovementMethod;
import ml.docilealligator.infinityforreddit.markdown.CustomMarkwonAdapter;
import ml.docilealligator.infinityforreddit.markdown.EmoteCloseBracketInlineProcessor;
import ml.docilealligator.infinityforreddit.markdown.EmotePlugin;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifEntry;
import ml.docilealligator.infinityforreddit.markdown.ImageAndGifPlugin;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentsListingRecyclerViewAdapter extends PagedListAdapter<Comment, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Comment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Comment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Comment comment, @NonNull Comment t1) {
            return comment.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Comment comment, @NonNull Comment t1) {
            return comment.getCommentMarkdown().equals(t1.getCommentMarkdown());
        }
    };
    private final BaseActivity mActivity;
    private final Retrofit mOauthRetrofit;
    private final Locale mLocale;
    private final EmoteCloseBracketInlineProcessor mEmoteCloseBracketInlineProcessor;
    private final EmotePlugin mEmotePlugin;
    private final ImageAndGifPlugin mImageAndGifPlugin;
    private final Markwon mMarkwon;
    private final ImageAndGifEntry mImageAndGifEntry;
    private final RecyclerView.RecycledViewPool recycledViewPool;
    private final String mAccessToken;
    private final String mAccountName;
    private final int mColorPrimaryLightTheme;
    private final int mSecondaryTextColor;
    private final int mCommentBackgroundColor;
    private int mCommentColor;
    private final int mDividerColor;
    private final int mUsernameColor;
    private final int mAuthorFlairColor;
    private final int mSubredditColor;
    private final int mUpvotedColor;
    private final int mDownvotedColor;
    private final int mButtonTextColor;
    private final int mColorAccent;
    private final int mCommentIconAndInfoColor;
    private final boolean mVoteButtonsOnTheRight;
    private final boolean mShowElapsedTime;
    private final String mTimeFormatPattern;
    private final boolean mShowCommentDivider;
    private final boolean mShowAbsoluteNumberOfVotes;
    private boolean canStartActivity = true;
    private NetworkState networkState;
    private final RetryLoadingMoreCallback mRetryLoadingMoreCallback;

    public CommentsListingRecyclerViewAdapter(BaseActivity activity, Retrofit oauthRetrofit,
                                              CustomThemeWrapper customThemeWrapper, Locale locale,
                                              SharedPreferences sharedPreferences, String accessToken,
                                              @NonNull String accountName, String username,
                                              RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mActivity = activity;
        mOauthRetrofit = oauthRetrofit;
        mCommentColor = customThemeWrapper.getCommentColor();
        int commentSpoilerBackgroundColor = mCommentColor | 0xFF000000;
        mLocale = locale;
        mAccessToken = accessToken;
        mAccountName = accountName;
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mShowCommentDivider = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_COMMENT_DIVIDER, false);
        mShowAbsoluteNumberOfVotes = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ABSOLUTE_NUMBER_OF_VOTES, true);
        mVoteButtonsOnTheRight = sharedPreferences.getBoolean(SharedPreferencesUtils.VOTE_BUTTONS_ON_THE_RIGHT_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;
        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mCommentBackgroundColor = customThemeWrapper.getCommentBackgroundColor();
        mCommentColor = customThemeWrapper.getCommentColor();
        mDividerColor = customThemeWrapper.getDividerColor();
        mSubredditColor = customThemeWrapper.getSubreddit();
        mUsernameColor = customThemeWrapper.getUsername();
        mAuthorFlairColor = customThemeWrapper.getAuthorFlairTextColor();
        mUpvotedColor = customThemeWrapper.getUpvoted();
        mDownvotedColor = customThemeWrapper.getDownvoted();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();
        mColorAccent = customThemeWrapper.getColorAccent();
        mCommentIconAndInfoColor = customThemeWrapper.getCommentIconAndInfoColor();
        int linkColor = customThemeWrapper.getLinkColor();
        MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
            @Override
            public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                if (mActivity.contentTypeface != null) {
                    textView.setTypeface(mActivity.contentTypeface);
                }
                textView.setTextColor(mCommentColor);
                textView.setHighlightColor(Color.TRANSPARENT);
            }

            @Override
            public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                builder.linkResolver((view, link) -> {
                    Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                    Uri uri = Uri.parse(link);
                    intent.setData(uri);
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
                urlMenuBottomSheetFragment.show(activity.getSupportFragmentManager(), urlMenuBottomSheetFragment.getTag());
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
            intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, username);
            intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
            if (canStartActivity) {
                canStartActivity = false;
                activity.startActivity(intent);
            }
        });
        mImageAndGifPlugin = new ImageAndGifPlugin();
        mMarkwon = MarkdownUtils.createFullRedditMarkwon(mActivity,
                miscPlugin, mEmoteCloseBracketInlineProcessor, mEmotePlugin, mImageAndGifPlugin, mCommentColor,
                commentSpoilerBackgroundColor, onLinkLongClickListener);
        mImageAndGifEntry = new ImageAndGifEntry(activity, Glide.with(activity), mediaMetadata -> {
            Intent intent = new Intent(activity, ViewImageOrGifActivity.class);
            if (mediaMetadata.isGIF) {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_GIF_URL_KEY, mediaMetadata.original.url);
            } else {
                intent.putExtra(ViewImageOrGifActivity.EXTRA_IMAGE_URL_KEY, mediaMetadata.original.url);
            }
            intent.putExtra(ViewImageOrGifActivity.EXTRA_SUBREDDIT_OR_USERNAME_KEY, username);
            intent.putExtra(ViewImageOrGifActivity.EXTRA_FILE_NAME_KEY, mediaMetadata.fileName);
            if (canStartActivity) {
                canStartActivity = false;
                activity.startActivity(intent);
            }
        });
        recycledViewPool = new RecyclerView.RecycledViewPool();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new CommentViewHolder(ItemCommentBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentBaseViewHolder) {
            Comment comment = getItem(holder.getBindingAdapterPosition());
            if (comment != null) {
                String name = "r/" + comment.getSubredditName();
                ((CommentBaseViewHolder) holder).authorTextView.setText(name);
                ((CommentBaseViewHolder) holder).authorTextView.setTextColor(mSubredditColor);

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentBaseViewHolder) holder).authorFlairTextView, comment.getAuthorFlairHTML(), true);
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentBaseViewHolder) holder).authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (mShowElapsedTime) {
                    ((CommentBaseViewHolder) holder).commentTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, comment.getCommentTimeMillis()));
                } else {
                    ((CommentBaseViewHolder) holder).commentTimeTextView.setText(Utils.getFormattedTime(mLocale, comment.getCommentTimeMillis(), mTimeFormatPattern));
                }

                mEmoteCloseBracketInlineProcessor.setMediaMetadataMap(comment.getMediaMetadataMap());
                mImageAndGifPlugin.setMediaMetadataMap(comment.getMediaMetadataMap());
                ((CommentBaseViewHolder) holder).markwonAdapter.setMarkdown(mMarkwon, comment.getCommentMarkdown());
                // noinspection NotifyDataSetChanged
                ((CommentBaseViewHolder) holder).markwonAdapter.notifyDataSetChanged();

                String commentScoreText = "";
                if (comment.isScoreHidden()) {
                    commentScoreText = mActivity.getString(R.string.hidden);
                } else {
                    commentScoreText = Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType());
                }
                ((CommentBaseViewHolder) holder).scoreTextView.setText(commentScoreText);

                switch (comment.getVoteType()) {
                    case Comment.VOTE_TYPE_UPVOTE:
                        ((CommentBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                        ((CommentBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                        ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case Comment.VOTE_TYPE_DOWNVOTE:
                        ((CommentBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_filled_24dp);
                        ((CommentBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mDownvotedColor));
                        ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (comment.isSaved()) {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentBaseViewHolder) holder).saveButton.setIconResource(R.drawable.ic_bookmark_border_grey_24dp);
                }
            }
        }
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
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentBaseViewHolder) {
            ((CommentBaseViewHolder) holder).authorFlairTextView.setText("");
            ((CommentBaseViewHolder) holder).authorFlairTextView.setVisibility(View.GONE);
            ((CommentBaseViewHolder) holder).upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
            ((CommentBaseViewHolder) holder).upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            ((CommentBaseViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
            ((CommentBaseViewHolder) holder).downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
            ((CommentBaseViewHolder) holder).downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
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
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
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

    public void editComment(String commentContentMarkdown, int position) {
        Comment comment = getItem(position);
        if (comment != null) {
            comment.setCommentMarkdown(commentContentMarkdown);
            notifyItemChanged(position);
        }
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    public void setDataSavingMode(boolean dataSavingMode) {
        mEmotePlugin.setDataSavingMode(dataSavingMode);
        mImageAndGifEntry.setDataSavingMode(dataSavingMode);
    }

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    public class CommentBaseViewHolder extends RecyclerView.ViewHolder {
        LinearLayout linearLayout;
        TextView authorTextView;
        TextView authorFlairTextView;
        TextView commentTimeTextView;
        RecyclerView commentMarkdownView;
        ConstraintLayout bottomConstraintLayout;
        MaterialButton upvoteButton;
        TextView scoreTextView;
        MaterialButton downvoteButton;
        View placeholder;
        MaterialButton moreButton;
        MaterialButton saveButton;
        MaterialButton replyButton;
        View commentDivider;
        CustomMarkwonAdapter markwonAdapter;

        CommentBaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(LinearLayout linearLayout,
                         TextView authorTextView,
                         TextView authorFlairTextView,
                         TextView commentTimeTextView,
                         RecyclerView commentMarkdownView,
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
            this.authorTextView = authorTextView;
            this.authorFlairTextView = authorFlairTextView;
            this.commentTimeTextView = commentTimeTextView;
            this.commentMarkdownView = commentMarkdownView;
            this.bottomConstraintLayout = bottomConstraintLayout;
            this.upvoteButton = upvoteButton;
            this.scoreTextView = scoreTextView;
            this.downvoteButton = downvoteButton;
            this.placeholder = placeholder;
            this.moreButton = moreButton;
            this.saveButton = saveButton;
            this.replyButton = replyButton;
            this.commentDivider = commentDivider;

            replyButton.setVisibility(View.GONE);

            ((ConstraintLayout.LayoutParams) authorTextView.getLayoutParams()).setMarginStart(0);
            ((ConstraintLayout.LayoutParams) authorFlairTextView.getLayoutParams()).setMarginStart(0);

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

            linearLayout.getLayoutTransition().setAnimateParentHierarchy(false);

            commentIndentationView.setVisibility(View.GONE);

            if (mShowCommentDivider) {
                commentDivider.setVisibility(View.VISIBLE);
            }

            if (mActivity.typeface != null) {
                authorTextView.setTypeface(mActivity.typeface);
                authorFlairTextView.setTypeface(mActivity.typeface);
                commentTimeTextView.setTypeface(mActivity.typeface);
                upvoteButton.setTypeface(mActivity.typeface);
            }
            itemView.setBackgroundColor(mCommentBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            authorFlairTextView.setTextColor(mAuthorFlairColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);
            upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            scoreTextView.setTextColor(mCommentIconAndInfoColor);
            downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            moreButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            saveButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            replyButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
            commentDivider.setBackgroundColor(mDividerColor);

            authorTextView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, comment.getSubredditName());
                    mActivity.startActivity(intent);
                }
            });

            moreButton.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Bundle bundle = new Bundle();
                    if (comment.getAuthor().equals(mAccountName)) {
                        bundle.putBoolean(CommentMoreBottomSheetFragment.EXTRA_EDIT_AND_DELETE_AVAILABLE, true);
                    }
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(mActivity.getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
                }
            });

            itemView.setOnClickListener(view -> {
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(getBindingAdapterPosition());
                if (comment != null) {
                    Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, comment.getLinkId());
                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getId());
                    mActivity.startActivity(intent);
                }
            });

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
            markwonAdapter = MarkdownUtils.createCustomTablesAndImagesAdapter(mImageAndGifEntry);
            markwonAdapter.setOnClickListener(view -> {
                if (view instanceof SpoilerOnClickTextView) {
                    if (((SpoilerOnClickTextView) view).isSpoilerOnClick()) {
                        ((SpoilerOnClickTextView) view).setSpoilerOnClick(false);
                        return;
                    }
                }
                if (canStartActivity) {
                    canStartActivity = false;
                    itemView.performClick();
                }
            });
            commentMarkdownView.setAdapter(markwonAdapter);

            upvoteButton.setOnClickListener(view -> {
                if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
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
                    } else {
                        //Upvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                        upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    if (!comment.isScoreHidden()) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                    }

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            int currentPosition = getBindingAdapterPosition();
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(Comment.VOTE_TYPE_UPVOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_filled_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mUpvotedColor));
                                    scoreTextView.setTextColor(mUpvotedColor);
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                    upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                if (!comment.isScoreHidden()) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
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
                if (mAccountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                    Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return;
                }

                int position = getBindingAdapterPosition();
                if (position < 0) {
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
                    } else {
                        //Downvoted before
                        comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                        downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                        scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    if (!comment.isScoreHidden()) {
                        scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                comment.getScore() + comment.getVoteType()));
                    }

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
                                }
                            } else {
                                comment.setVoteType(Comment.VOTE_TYPE_NO_VOTE);
                                if (currentPosition == position) {
                                    downvoteButton.setIconResource(R.drawable.ic_downvote_24dp);
                                    downvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                    scoreTextView.setTextColor(mCommentIconAndInfoColor);
                                }
                            }

                            if (currentPosition == position) {
                                upvoteButton.setIconResource(R.drawable.ic_upvote_24dp);
                                upvoteButton.setIconTint(ColorStateList.valueOf(mCommentIconAndInfoColor));
                                if (!comment.isScoreHidden()) {
                                    scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                            comment.getScore() + comment.getVoteType()));
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
                int position = getBindingAdapterPosition();
                if (position < 0) {
                    return;
                }
                Comment comment = getItem(position);
                if (comment != null) {
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
        }
    }

    class CommentViewHolder extends CommentBaseViewHolder {
        ItemCommentBinding binding;

        CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            setBaseView(binding.linearLayoutItemComment,
                    binding.authorTextViewItemPostComment,
                    binding.authorFlairTextViewItemPostComment,
                    binding.commentTimeTextViewItemPostComment,
                    binding.commentMarkdownViewItemPostComment,
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

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error)
        Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (mActivity.typeface != null) {
                errorTextView.setTypeface(mActivity.typeface);
                retryButton.setTypeface(mActivity.typeface);
            }
            errorTextView.setText(R.string.load_comments_failed);
            retryButton.setOnClickListener(view -> mRetryLoadingMoreCallback.retryLoadingMore());
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
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
