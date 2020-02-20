package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
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
import ml.docilealligator.infinityforreddit.Fragment.CommentMoreBottomSheetFragment;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
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
    private int mTextColorPrimaryDark;
    private int mColorAccent;
    private boolean mVoteButtonsOnTheRight;
    private boolean mShowElapsedTime;
    private boolean mShowCommentDivider;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;

    public CommentsListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                              String accessToken, String accountName,
                                              boolean voteButtonsOnTheRight, boolean showElapsedTime,
                                              boolean showCommentDivider,
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
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;
        mTextColorPrimaryDark = mContext.getResources().getColor(R.color.colorPrimaryDarkDayNightTheme);
        mColorAccent = mContext.getResources().getColor(R.color.colorAccent);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            CommentData comment = getItem(holder.getAdapterPosition());
            if (comment != null) {
                if (comment.getSubredditName().substring(2).equals(comment.getLinkAuthor())) {
                    ((DataViewHolder) holder).authorTextView.setText("u/" + comment.getLinkAuthor());
                    ((DataViewHolder) holder).authorTextView.setTextColor(mTextColorPrimaryDark);
                    ((DataViewHolder) holder).authorTextView.setOnClickListener(view -> {
                        Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, comment.getLinkAuthor());
                        mContext.startActivity(intent);
                    });
                } else {
                    ((DataViewHolder) holder).authorTextView.setText("r/" + comment.getSubredditName());
                    ((DataViewHolder) holder).authorTextView.setTextColor(mColorAccent);
                    ((DataViewHolder) holder).authorTextView.setOnClickListener(view -> {
                        Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, comment.getSubredditName());
                        mContext.startActivity(intent);
                    });
                }

                if (mShowElapsedTime) {
                    ((DataViewHolder) holder).commentTimeTextView.setText(
                            Utils.getElapsedTime(mContext, comment.getCommentTimeMillis()));
                } else {
                    ((DataViewHolder) holder).commentTimeTextView.setText(comment.getCommentTime());
                }

                mMarkwon.setMarkdown(((DataViewHolder) holder).commentMarkdownView, comment.getCommentMarkdown());

                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore() + comment.getVoteType()));

                switch (comment.getVoteType()) {
                    case CommentData.VOTE_TYPE_UPVOTE:
                        ((DataViewHolder) holder).upvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                        break;
                    case CommentData.VOTE_TYPE_DOWNVOTE:
                        ((DataViewHolder) holder).downvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                        break;
                }

                ((DataViewHolder) holder).moreButton.setOnClickListener(view -> {
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

                ((DataViewHolder) holder).linearLayout.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, comment.getLinkId());
                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getId());
                    mContext.startActivity(intent);
                });

                ((DataViewHolder) holder).verticalBlock.setVisibility(View.GONE);

                ((DataViewHolder) holder).commentMarkdownView.setOnClickListener(view ->
                        ((DataViewHolder) holder).linearLayout.callOnClick());

                ((DataViewHolder) holder).replyButton.setVisibility(View.GONE);

                ((DataViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((DataViewHolder) holder).downvoteButton.clearColorFilter();

                    if (previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
                        //Not upvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                        newVoteType = RedditUtils.DIR_UPVOTE;
                        ((DataViewHolder) holder).upvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                    } else {
                        //Upvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore() + comment.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                                comment.setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                                ((DataViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                            } else {
                                comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                                ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                                ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore() + comment.getVoteType()));
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                        }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                ((DataViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((DataViewHolder) holder).upvoteButton.clearColorFilter();

                    if (previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
                        //Not downvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                        newVoteType = RedditUtils.DIR_DOWNVOTE;
                        ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                    } else {
                        //Downvoted before
                        comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                        ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore() + comment.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                                comment.setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                                ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                            } else {
                                comment.setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                                ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                                ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore() + comment.getVoteType()));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                        }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                if (comment.isSaved()) {
                    ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                ((DataViewHolder) holder).saveButton.setOnClickListener(view -> {
                    if (comment.isSaved()) {
                        comment.setSaved(false);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(false);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        comment.setSaved(true);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(true);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                Toast.makeText(mContext, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
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
        if (holder instanceof DataViewHolder) {
            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
            ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
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

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout_item_comment)
        LinearLayout linearLayout;
        @BindView(R.id.vertical_block_item_post_comment)
        View verticalBlock;
        @BindView(R.id.author_text_view_item_post_comment)
        TextView authorTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment)
        TextView commentTimeTextView;
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

        DataViewHolder(View itemView) {
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
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
