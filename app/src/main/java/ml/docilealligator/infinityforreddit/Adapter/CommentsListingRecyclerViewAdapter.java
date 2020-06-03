package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.net.Uri;
import android.os.Bundle;
import android.text.style.SuperscriptSpan;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.CommentData;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Fragment.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.Utils.APIUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import ml.docilealligator.infinityforreddit.VoteThing;
import retrofit2.Retrofit;

public class CommentsListingRecyclerViewAdapter extends PagedListAdapter<CommentData, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<CommentData> DIFF_CALLBACK = new DiffUtil.ItemCallback<CommentData>() {
        @Override
        public boolean areItemsTheSame(@NonNull CommentData commentData, @NonNull CommentData t1) {
            return commentData.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CommentData commentData, @NonNull CommentData t1) {
            return commentData.getCommentMarkdown().equals(t1.getCommentMarkdown());
        }
    };
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private Markwon mMarkwon;
    private String mAccessToken;
    private String mAccountName;
    private int mColorPrimaryLightTheme;
    private int mSecondaryTextColor;
    private int mCommentBackgroundColor;
    private int mCommentColor;
    private int mDividerColor;
    private int mUsernameColor;
    private int mAuthorFlairColor;
    private int mSubredditColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mButtonTextColor;
    private int mColorAccent;
    private int mCommentIconAndInfoColor;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private boolean mShowCommentDivider;
    private boolean mShowAbsoluteNumberOfVotes;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;

    public CommentsListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                              CustomThemeWrapper customThemeWrapper, String accessToken,
                                              String accountName, boolean voteButtonsOnTheRight,
                                              boolean showElapsedTime, boolean showCommentDivider,
                                              boolean showAbsoluteNumberOfVotes,
                                              RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mContext = context;
        mOauthRetrofit = oauthRetrofit;
        mMarkwon = Markwon.builder(mContext)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                    }
                })
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(SimpleExtPlugin.create(plugin ->
                                plugin.addExtension(1, '^', (configuration, props) -> {
                                    return new SuperscriptSpan();
                                })
                        )
                )
                .build();
        mAccessToken = accessToken;
        mAccountName = accountName;
        mVoteButtonsOnTheRight = voteButtonsOnTheRight;
        mShowElapsedTime = showElapsedTime;
        mShowCommentDivider = showCommentDivider;
        mShowAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
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
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentViewHolder) {
            CommentData comment = getItem(holder.getAdapterPosition());
            if (comment != null) {
                if (comment.getSubredditName().substring(2).equals(comment.getLinkAuthor())) {
                    ((CommentViewHolder) holder).authorTextView.setText("u/" + comment.getLinkAuthor());
                    ((CommentViewHolder) holder).authorTextView.setTextColor(mUsernameColor);
                    ((CommentViewHolder) holder).authorTextView.setOnClickListener(view -> {
                        Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, comment.getLinkAuthor());
                        mContext.startActivity(intent);
                    });
                } else {
                    ((CommentViewHolder) holder).authorTextView.setText("r/" + comment.getSubredditName());
                    ((CommentViewHolder) holder).authorTextView.setTextColor(mSubredditColor);
                    ((CommentViewHolder) holder).authorTextView.setOnClickListener(view -> {
                        Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, comment.getSubredditName());
                        mContext.startActivity(intent);
                    });
                }

                if (comment.getAuthorFlairHTML() != null && !comment.getAuthorFlairHTML().equals("")) {
                    ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).authorFlairTextView, comment.getAuthorFlairHTML());
                } else if (comment.getAuthorFlair() != null && !comment.getAuthorFlair().equals("")) {
                    ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.VISIBLE);
                    ((CommentViewHolder) holder).authorFlairTextView.setText(comment.getAuthorFlair());
                }

                if (mShowElapsedTime) {
                    ((CommentViewHolder) holder).commentTimeTextView.setText(
                            Utils.getElapsedTime(mContext, comment.getCommentTimeMillis()));
                } else {
                    ((CommentViewHolder) holder).commentTimeTextView.setText(comment.getCommentTime());
                }

                if (comment.getAwards() != null && !comment.getAwards().equals("")) {
                    Utils.setHTMLWithImageToTextView(((CommentViewHolder) holder).awardsTextView, comment.getAwards());
                }

                mMarkwon.setMarkdown(((CommentViewHolder) holder).commentMarkdownView, comment.getCommentMarkdown());

                ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                        comment.getScore() + comment.getVoteType()));

                switch (comment.getVoteType()) {
                    case CommentData.VOTE_TYPE_UPVOTE:
                        ((CommentViewHolder) holder).upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case CommentData.VOTE_TYPE_DOWNVOTE:
                        ((CommentViewHolder) holder).downvoteButton
                                .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                ((CommentViewHolder) holder).moreButton.setOnClickListener(view -> {
                    Bundle bundle = new Bundle();
                    if (comment.getAuthor().equals(mAccountName)) {
                        bundle.putString(CommentMoreBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                    }
                    bundle.putParcelable(CommentMoreBottomSheetFragment.EXTRA_COMMENT, comment);
                    bundle.putInt(CommentMoreBottomSheetFragment.EXTRA_POSITION, holder.getAdapterPosition() - 1);
                    CommentMoreBottomSheetFragment commentMoreBottomSheetFragment = new CommentMoreBottomSheetFragment();
                    commentMoreBottomSheetFragment.setArguments(bundle);
                    commentMoreBottomSheetFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), commentMoreBottomSheetFragment.getTag());
                });

                ((CommentViewHolder) holder).linearLayout.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, comment.getLinkId());
                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getId());
                    mContext.startActivity(intent);
                });

                ((CommentViewHolder) holder).verticalBlock.setVisibility(View.GONE);

                ((CommentViewHolder) holder).commentMarkdownView.setOnClickListener(view ->
                        ((CommentViewHolder) holder).linearLayout.callOnClick());

                ((CommentViewHolder) holder).replyButton.setVisibility(View.GONE);

                ((CommentViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                        newVoteType = APIUtils.DIR_UPVOTE;
                        ((CommentViewHolder) holder).upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType()));

                    VoteThing.voteThing(mContext, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            if (newVoteType.equals(APIUtils.DIR_UPVOTE)) {
                                comment.setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                                ((CommentViewHolder) holder).upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((CommentViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                            } else {
                                comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                                ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
                            }

                            ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    comment.getScore() + comment.getVoteType()));
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                        }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                ((CommentViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
                        //Not downvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                        newVoteType = APIUtils.DIR_DOWNVOTE;
                        ((CommentViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                        newVoteType = APIUtils.DIR_UNVOTE;
                        ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
                    }

                    ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                            comment.getScore() + comment.getVoteType()));

                    VoteThing.voteThing(mContext, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(APIUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                                ((CommentViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((CommentViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                            } else {
                                comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                                ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
                            }

                            ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((CommentViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes,
                                    comment.getScore() + comment.getVoteType()));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                        }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                if (comment.isSaved()) {
                    ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                ((CommentViewHolder) holder).saveButton.setOnClickListener(view -> {
                    if (comment.isSaved()) {
                        comment.setSaved(false);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(false);
                                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        comment.setSaved(true);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(true);
                                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                ((CommentViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_saved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
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
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).authorFlairTextView.setText("");
            ((CommentViewHolder) holder).authorFlairTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).awardsTextView.setText("");
            ((CommentViewHolder) holder).awardsTextView.setVisibility(View.GONE);
            ((CommentViewHolder) holder).upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((CommentViewHolder) holder).scoreTextView.setTextColor(mCommentIconAndInfoColor);
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

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout_item_comment)
        LinearLayout linearLayout;
        @BindView(R.id.vertical_block_item_post_comment)
        View verticalBlock;
        @BindView(R.id.author_text_view_item_post_comment)
        TextView authorTextView;
        @BindView(R.id.author_flair_text_view_item_post_comment)
        TextView authorFlairTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment)
        TextView commentTimeTextView;
        @BindView(R.id.awards_text_view_item_comment)
        TextView awardsTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment)
        TextView commentMarkdownView;
        @BindView(R.id.bottom_constraint_layout_item_post_comment)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.up_vote_button_item_post_comment)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment)
        TextView scoreTextView;
        @BindView(R.id.down_vote_button_item_post_comment)
        ImageView downvoteButton;
        @BindView(R.id.more_button_item_post_comment)
        ImageView moreButton;
        @BindView(R.id.save_button_item_post_comment)
        ImageView saveButton;
        @BindView(R.id.expand_button_item_post_comment)
        ImageView expandButton;
        @BindView(R.id.reply_button_item_post_comment)
        ImageView replyButton;
        @BindView(R.id.divider_item_comment)
        View commentDivider;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(expandButton.getId(), ConstraintSet.END);
                constraintSet.clear(replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(moreButton.getId(), ConstraintSet.START, expandButton.getId(), ConstraintSet.END);
                constraintSet.connect(moreButton.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.END);
                constraintSet.connect(expandButton.getId(), ConstraintSet.START, replyButton.getId(), ConstraintSet.END);
                constraintSet.connect(replyButton.getId(), ConstraintSet.START, replyButton.getId(), ConstraintSet.END);
                constraintSet.setHorizontalBias(moreButton.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }

            if (mShowCommentDivider) {
                commentDivider.setVisibility(View.VISIBLE);
            }

            itemView.setBackgroundColor(mCommentBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            authorFlairTextView.setTextColor(mAuthorFlairColor);
            commentTimeTextView.setTextColor(mSecondaryTextColor);
            awardsTextView.setTextColor(mSecondaryTextColor);
            commentMarkdownView.setTextColor(mCommentColor);
            upvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mCommentIconAndInfoColor);
            downvoteButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            moreButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            expandButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            saveButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            replyButton.setColorFilter(mCommentIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentDivider.setBackgroundColor(mDividerColor);
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
