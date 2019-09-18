package ml.docilealligator.infinityforreddit;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import retrofit2.Retrofit;

class CommentsListingRecyclerViewAdapter extends PagedListAdapter<CommentData, RecyclerView.ViewHolder> {
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private Markwon mMarkwon;
    private String mAccessToken;
    private String mAccountName;
    private int mTextColorPrimaryDark;
    private int mColorAccent;

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;

    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;

    interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    protected CommentsListingRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                                 String accessToken, String accountName,
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
                            if(uri.getScheme() == null && uri.getHost() == null) {
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
                .build();
        mAccessToken = accessToken;
        mAccountName = accountName;
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;
        mTextColorPrimaryDark = mContext.getResources().getColor(R.color.colorPrimaryDarkDayNightTheme);
        mColorAccent = mContext.getResources().getColor(R.color.colorAccent);
    }

    private static final DiffUtil.ItemCallback<CommentData> DIFF_CALLBACK = new DiffUtil.ItemCallback<CommentData>() {
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        } else if(viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DataViewHolder) {
            CommentData comment = getItem(holder.getAdapterPosition());
            if(comment != null) {
                if(comment.getSubredditName().substring(2).equals(comment.getLinkAuthor())) {
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

                ((DataViewHolder) holder).commentTimeTextView.setText(comment.getCommentTime());

                mMarkwon.setMarkdown(((DataViewHolder) holder).commentMarkdownView, comment.getCommentContent());

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

                if(comment.getAuthor().equals(mAccountName)) {
                    ((DataViewHolder) holder).moreButton.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).moreButton.setOnClickListener(view -> {
                        ModifyCommentBottomSheetFragment modifyCommentBottomSheetFragment = new ModifyCommentBottomSheetFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
                        bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_COMMENT_CONTENT, comment.getCommentContent());
                        bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_COMMENT_FULLNAME, comment.getFullName());
                        bundle.putInt(ModifyCommentBottomSheetFragment.EXTRA_POSITION, holder.getAdapterPosition() - 1);
                        modifyCommentBottomSheetFragment.setArguments(bundle);
                        modifyCommentBottomSheetFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), modifyCommentBottomSheetFragment.getTag());
                    });
                }

                ((DataViewHolder) holder).linearLayout.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                    intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, comment.getLinkId());
                    intent.putExtra(ViewPostDetailActivity.EXTRA_SINGLE_COMMENT_ID, comment.getId());
                    mContext.startActivity(intent);
                });

                ((DataViewHolder) holder).verticalBlock.setVisibility(View.GONE);

                ((DataViewHolder) holder).commentMarkdownView.setOnClickListener(view ->
                        ((DataViewHolder) holder).linearLayout.callOnClick());

                ((DataViewHolder) holder).shareButton.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        String extraText = comment.getPermalink();
                        intent.putExtra(Intent.EXTRA_TEXT, extraText);
                        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
                    }
                });

                ((DataViewHolder) holder).replyButton.setVisibility(View.GONE);

                ((DataViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if(mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((DataViewHolder) holder).downvoteButton.clearColorFilter();

                    if(previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
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
                            if(newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
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
                        public void onVoteThingFail(int position) { }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                ((DataViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if(mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int previousVoteType = comment.getVoteType();
                    String newVoteType;

                    ((DataViewHolder) holder).upvoteButton.clearColorFilter();

                    if(previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
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
                            if(newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
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
                        public void onVoteThingFail(int position1) { }
                    }, comment.getFullName(), newVoteType, holder.getAdapterPosition());
                });

                if(comment.isSaved()) {
                    ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_24px);
                } else {
                    ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_border_24px);
                }

                ((DataViewHolder) holder).saveButton.setOnClickListener(view -> {
                    if (comment.isSaved()) {
                        comment.setSaved(false);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(false);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_border_24px);
                                Toast.makeText(mContext, R.string.comment_unsaved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(true);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_24px);
                                Toast.makeText(mContext, R.string.comment_unsaved_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        comment.setSaved(true);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, comment.getFullName(), new SaveThing.SaveThingListener() {
                            @Override
                            public void success() {
                                comment.setSaved(true);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_24px);
                                Toast.makeText(mContext, R.string.comment_saved_success, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void failed() {
                                comment.setSaved(false);
                                ((DataViewHolder) holder).saveButton.setImageResource(R.drawable.ic_baseline_bookmark_border_24px);
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
        if(holder instanceof DataViewHolder) {
            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
            ((DataViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
        }
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

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.linear_layout_item_comment) LinearLayout linearLayout;
        @BindView(R.id.vertical_block_item_post_comment) View verticalBlock;
        @BindView(R.id.author_text_view_item_post_comment) TextView authorTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment) TextView commentTimeTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment) TextView commentMarkdownView;
        @BindView(R.id.up_vote_button_item_post_comment) ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment) TextView scoreTextView;
        @BindView(R.id.down_vote_button_item_post_comment) ImageView downvoteButton;
        @BindView(R.id.more_button_item_post_comment) ImageView moreButton;
        @BindView(R.id.save_button_item_post_comment) ImageView saveButton;
        @BindView(R.id.share_button_item_post_comment) ImageView shareButton;
        @BindView(R.id.reply_button_item_post_comment) ImageView replyButton;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error) TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error) Button retryButton;

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
