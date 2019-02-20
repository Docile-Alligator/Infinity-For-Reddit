package ml.docilealligator.infinityforreddit;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.multilevelview.models.RecyclerViewItem;

import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

public class CommentAdapter extends PagedListAdapter<CommentData, RecyclerView.ViewHolder> {
    private Context mContext;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private SharedPreferences mSharedPreferences;
    private RecyclerView mRecyclerView;
    private String subredditNamePrefixed;
    private String article;
    private Locale locale;

    private NetworkState networkState;
    private RetryLoadingMoreCallback retryLoadingMoreCallback;

    interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    CommentAdapter(Context context, Retrofit retrofit, Retrofit oauthRetrofit,
                   SharedPreferences sharedPreferences, RecyclerView recyclerView,
                   String subredditNamePrefixed, String article, Locale locale) {
        super(DIFF_CALLBACK);
        mContext = context;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mSharedPreferences = sharedPreferences;
        mRecyclerView = recyclerView;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.article = article;
        this.locale = locale;
    }

    static final DiffUtil.ItemCallback<CommentData> DIFF_CALLBACK = new DiffUtil.ItemCallback<CommentData>() {
        @Override
        public boolean areItemsTheSame(@NonNull CommentData commentData, @NonNull CommentData t1) {
            return commentData.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CommentData commentData, @NonNull CommentData t1) {
            return commentData.getCommentContent().equals(t1.getCommentContent());
        }
    };

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        final CommentData commentItem = getItem(i);

        String authorPrefixed = "u/" + commentItem.getAuthor();
        ((CommentViewHolder) viewHolder).authorTextView.setText(authorPrefixed);
        ((CommentViewHolder) viewHolder).authorTextView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, commentItem.getAuthor());
            mContext.startActivity(intent);
        });

        ((CommentViewHolder) viewHolder).commentTimeTextView.setText(commentItem.getCommentTime());
        SpannableConfiguration spannableConfiguration = SpannableConfiguration.builder(mContext).linkResolver((view, link) -> {
            if(link.startsWith("/u/")) {
                Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                mContext.startActivity(intent);
            } else if(link.startsWith("/r/")) {
                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, link.substring(3));
                mContext.startActivity(intent);
            } else {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                // add share action to menu list
                builder.addDefaultShareMenuItem();
                builder.setToolbarColor(mContext.getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(mContext, Uri.parse(link));
            }
        }).build();

        ((CommentViewHolder) viewHolder).commentMarkdownView.setMarkdown(spannableConfiguration, commentItem.getCommentContent());
        ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore()));

        ((CommentViewHolder) viewHolder).verticalBlock.getLayoutParams().width = commentItem.getDepth() * 16;
        if(commentItem.hasReply()) {
            setExpandButton(((CommentViewHolder) viewHolder).expandButton, commentItem.isExpanded());
        }

        ((CommentViewHolder) viewHolder).expandButton.setOnClickListener(view -> {
            if(commentItem.hasChildren() && commentItem.getChildren().size() > 0) {
                setExpandButton(((CommentViewHolder) viewHolder).expandButton, commentItem.isExpanded());
            } else {
                ((CommentViewHolder) viewHolder).loadMoreCommentsProgressBar.setVisibility(View.VISIBLE);
                FetchComment.fetchComment(mRetrofit, subredditNamePrefixed, article, commentItem.getId(),
                        locale, false, commentItem.getDepth(), new FetchComment.FetchCommentListener() {
                            @Override
                            public void onFetchCommentSuccess(List<?> commentData,
                                                              String parentId, String commaSeparatedChildren) {
                                commentItem.addChildren((List<RecyclerViewItem>) commentData);
                                ((CommentViewHolder) viewHolder).loadMoreCommentsProgressBar
                                        .setVisibility(View.GONE);
                                ((CommentViewHolder) viewHolder).expandButton
                                        .setImageResource(R.drawable.ic_expand_less_black_20dp);

                                /*ParseComment.parseComment(response, new ArrayList<>(),
                                        locale, false, commentItem.getDepth(),
                                        new ParseComment.ParseCommentListener() {
                                            @Override
                                            public void onParseCommentSuccess(List<?> commentData,
                                                                              String parentId, String commaSeparatedChildren) {
                                                commentItem.addChildren((List<RecyclerViewItem>) commentData);
                                                ((CommentViewHolder) viewHolder).loadMoreCommentsProgressBar
                                                        .setVisibility(View.GONE);
                                                ((CommentViewHolder) viewHolder).expandButton
                                                        .setImageResource(R.drawable.ic_expand_less_black_20dp);
                                            }

                                            @Override
                                            public void onParseCommentFailed() {
                                                ((CommentViewHolder) viewHolder).loadMoreCommentsProgressBar
                                                        .setVisibility(View.GONE);
                                            }
                                        });*/
                            }

                            @Override
                            public void onFetchCommentFailed() {
                                ((CommentViewHolder) viewHolder).loadMoreCommentsProgressBar
                                        .setVisibility(View.GONE);
                            }
                        });
            }
        });

        switch (commentItem.getVoteType()) {
            case 1:
                ((CommentViewHolder) viewHolder).upvoteButton
                        .setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            case 2:
                ((CommentViewHolder) viewHolder).downvoteButton
                        .setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
        }

        ((CommentViewHolder) viewHolder).upvoteButton.setOnClickListener(view -> {
            final boolean isDownvotedBefore = ((CommentViewHolder) viewHolder).downvoteButton.getColorFilter() != null;
            final ColorFilter minusButtonColorFilter = ((CommentViewHolder) viewHolder).downvoteButton.getColorFilter();
            ((CommentViewHolder) viewHolder).downvoteButton.clearColorFilter();

            if (((CommentViewHolder) viewHolder).upvoteButton.getColorFilter() == null) {
                ((CommentViewHolder) viewHolder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                if(isDownvotedBefore) {
                    ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() + 2));
                } else {
                    ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() + 1));
                }

                VoteThing.voteThing(mOauthRetrofit,mSharedPreferences,  new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position1) {
                        commentItem.setVoteType(1);
                        if(isDownvotedBefore) {
                            commentItem.setScore(commentItem.getScore() + 2);
                        } else {
                            commentItem.setScore(commentItem.getScore() + 1);
                        }
                    }

                    @Override
                    public void onVoteThingFail(int position1) {
                        Toast.makeText(mContext, "Cannot upvote this comment", Toast.LENGTH_SHORT).show();
                        ((CommentViewHolder) viewHolder).upvoteButton.clearColorFilter();
                        ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore()));
                        ((CommentViewHolder) viewHolder).downvoteButton.setColorFilter(minusButtonColorFilter);
                    }
                }, commentItem.getFullName(), RedditUtils.DIR_UPVOTE, ((CommentViewHolder) viewHolder).getAdapterPosition());
            } else {
                //Upvoted before
                ((CommentViewHolder) viewHolder).upvoteButton.clearColorFilter();
                ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() - 1));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position1) {
                        commentItem.setVoteType(0);
                        commentItem.setScore(commentItem.getScore() - 1);
                    }

                    @Override
                    public void onVoteThingFail(int position1) {
                        Toast.makeText(mContext, "Cannot unvote this comment", Toast.LENGTH_SHORT).show();
                        ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() + 1));
                        ((CommentViewHolder) viewHolder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                        commentItem.setScore(commentItem.getScore() + 1);
                    }
                }, commentItem.getFullName(), RedditUtils.DIR_UNVOTE, ((CommentViewHolder) viewHolder).getAdapterPosition());
            }
        });

        ((CommentViewHolder) viewHolder).downvoteButton.setOnClickListener(view -> {
            final boolean isUpvotedBefore = ((CommentViewHolder) viewHolder).upvoteButton.getColorFilter() != null;

            final ColorFilter upvoteButtonColorFilter = ((CommentViewHolder) viewHolder).upvoteButton.getColorFilter();
            ((CommentViewHolder) viewHolder).upvoteButton.clearColorFilter();

            if (((CommentViewHolder) viewHolder).downvoteButton.getColorFilter() == null) {
                ((CommentViewHolder) viewHolder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                if (isUpvotedBefore) {
                    ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() - 2));
                } else {
                    ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() - 1));
                }

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position12) {
                        commentItem.setVoteType(-1);
                        if(isUpvotedBefore) {
                            commentItem.setScore(commentItem.getScore() - 2);
                        } else {
                            commentItem.setScore(commentItem.getScore() - 1);
                        }
                    }

                    @Override
                    public void onVoteThingFail(int position12) {
                        Toast.makeText(mContext, "Cannot downvote this comment", Toast.LENGTH_SHORT).show();
                        ((CommentViewHolder) viewHolder).downvoteButton.clearColorFilter();
                        ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore()));
                        ((CommentViewHolder) viewHolder).upvoteButton.setColorFilter(upvoteButtonColorFilter);
                    }
                }, commentItem.getFullName(), RedditUtils.DIR_DOWNVOTE, viewHolder.getAdapterPosition());
            } else {
                //Down voted before
                ((CommentViewHolder) viewHolder).downvoteButton.clearColorFilter();
                ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore() + 1));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position12) {
                        commentItem.setVoteType(0);
                        commentItem.setScore(commentItem.getScore());
                    }

                    @Override
                    public void onVoteThingFail(int position12) {
                        Toast.makeText(mContext, "Cannot unvote this comment", Toast.LENGTH_SHORT).show();
                        ((CommentViewHolder) viewHolder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((CommentViewHolder) viewHolder).scoreTextView.setText(Integer.toString(commentItem.getScore()));
                        commentItem.setScore(commentItem.getScore());
                    }
                }, commentItem.getFullName(), RedditUtils.DIR_UNVOTE, viewHolder.getAdapterPosition());
            }
        });
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        ((CommentMultiLevelRecyclerViewAdapter.CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
        ((CommentMultiLevelRecyclerViewAdapter.CommentViewHolder) holder).loadMoreCommentsProgressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        if(hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    void setNetworkState(NetworkState newNetworkState) {
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

    private void setExpandButton(ImageView expandButton, boolean isExpanded) {
        // set the icon based on the current state
        expandButton.setVisibility(View.VISIBLE);
        expandButton.setImageResource(isExpanded ? R.drawable.ic_expand_less_black_20dp : R.drawable.ic_expand_more_black_20dp);
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author_text_view_item_post_comment) TextView authorTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment) TextView commentTimeTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment) MarkwonView commentMarkdownView;
        @BindView(R.id.plus_button_item_post_comment) ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment) TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_comment) ImageView downvoteButton;
        @BindView(R.id.expand_button_item_post_comment) ImageView expandButton;
        @BindView(R.id.load_more_comments_progress_bar) ProgressBar loadMoreCommentsProgressBar;
        @BindView(R.id.reply_button_item_post_comment) ImageView replyButton;
        @BindView(R.id.vertical_block_item_post_comment) View verticalBlock;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
