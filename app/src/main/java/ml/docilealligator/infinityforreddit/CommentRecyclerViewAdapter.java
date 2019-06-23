package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Retrofit;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_COMMENT = 0;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENT = 1;

    private Activity mActivity;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private SharedPreferences mSharedPreferences;
    private String mSubredditNamePrefixed;
    private Locale mLocale;

    private ArrayList<CommentData> mVisibleComments;

    CommentRecyclerViewAdapter(Activity activity, Retrofit retrofit, Retrofit oauthRetrofit,
                               SharedPreferences sharedPreferences, ArrayList<CommentData> expandedComments,
                               String subredditNamePrefixed, Locale locale) {
        mActivity = activity;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mSharedPreferences = sharedPreferences;
        mSubredditNamePrefixed = subredditNamePrefixed;
        mLocale = locale;
        mVisibleComments = expandedComments;
    }

    @Override
    public int getItemViewType(int position) {
        CommentData comment = mVisibleComments.get(position);
        if(!comment.isPlaceHolder()) {
            return VIEW_TYPE_COMMENT;
        } else {
            return VIEW_TYPE_LOAD_MORE_COMMENT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_COMMENT) {
            return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
        } else {
            return new LoadMoreCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more_comments_placeholder, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == VIEW_TYPE_COMMENT) {
            CommentData commentItem = mVisibleComments.get(holder.getAdapterPosition());

            String authorPrefixed = "u/" + commentItem.getAuthor();
            ((CommentViewHolder) holder).authorTextView.setText(authorPrefixed);

            ((CommentViewHolder) holder).commentTimeTextView.setText(commentItem.getCommentTime());

            SpannableConfiguration spannableConfiguration = SpannableConfiguration.builder(mActivity).linkResolver((view, link) -> {
                if (link.startsWith("/u/") || link.startsWith("u/")) {
                    Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                    mActivity.startActivity(intent);
                } else if (link.startsWith("/r/") || link.startsWith("r/")) {
                    Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                    intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, link.substring(3));
                    mActivity.startActivity(intent);
                } else {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    // add share action to menu list
                    builder.addDefaultShareMenuItem();
                    builder.setToolbarColor(mActivity.getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(mActivity, Uri.parse(link));
                }
            }).build();

            ((CommentViewHolder) holder).commentMarkdownView.setMarkdown(spannableConfiguration, commentItem.getCommentContent());
            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(commentItem.getScore()));

            ((CommentViewHolder) holder).verticalBlock.getLayoutParams().width = commentItem.getDepth() * 16;

            if (commentItem.hasReply()) {
                if(commentItem.isExpanded()) {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_less_black_20dp);
                } else {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_more_black_20dp);
                }
                ((CommentViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
            }

            switch (commentItem.getVoteType()) {
                case 1:
                    ((CommentViewHolder) holder).upvoteButton
                            .setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    break;
                case 2:
                    ((CommentViewHolder) holder).downvoteButton
                            .setColorFilter(ContextCompat.getColor(mActivity, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    break;
            }
        } else {
            ((LoadMoreCommentViewHolder) holder).verticalBlock.getLayoutParams().width = mVisibleComments.get(holder.getAdapterPosition()).getDepth() * 16;
            if(mVisibleComments.get(holder.getAdapterPosition()).isLoadingMoreChildren()) {
                ((LoadMoreCommentViewHolder) holder).placeholderTextView.setText(R.string.loading);
            } else if(mVisibleComments.get(holder.getAdapterPosition()).isLoadMoreChildrenFailed()) {
                ((LoadMoreCommentViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
            } else {
                ((LoadMoreCommentViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);
            }
        }
    }

    private int getParentPosition(int position) {
        int childDepth = mVisibleComments.get(position).getDepth();
        for(int i = position; i >= 0; i--) {
            if(mVisibleComments.get(i).getDepth() < childDepth) {
                return i;
            }
        }
        return -1;
    }

    private void expandChildren(int position) {
        CommentData comment = mVisibleComments.get(position);
        if(!comment.isExpanded()) {
            comment.setExpanded(true);
            ArrayList<CommentData> children = comment.getChildren();
            if(children != null && children.size() > 0) {
                mVisibleComments.addAll(position + 1, children);
                for(int i = position + 1; i <= position + children.size(); i++) {
                    mVisibleComments.get(i).setExpanded(false);
                }
                notifyItemRangeInserted(position + 1, children.size());
            }
        }
    }

    private void collapseChildren(int position) {
        mVisibleComments.get(position).setExpanded(false);
        int depth = mVisibleComments.get(position).getDepth();
        int allChildrenSize = 0;
        for(int i = position + 1; i < mVisibleComments.size(); i++) {
            if(mVisibleComments.get(i).getDepth() > depth) {
                allChildrenSize++;
            } else {
                break;
            }
        }

        mVisibleComments.subList(position + 1, position + 1 + allChildrenSize).clear();
        notifyItemRangeRemoved(position + 1, allChildrenSize);
    }

    void addComments(ArrayList<CommentData> comments) {
        int sizeBefore = mVisibleComments.size();
        mVisibleComments.addAll(comments);
        notifyItemRangeInserted(sizeBefore, comments.size());
    }

    void addComment(CommentData comment) {
        mVisibleComments.add(0, comment);
        notifyItemInserted(0);
    }

    void addChildComment(CommentData comment, String parentFullname, int parentPosition) {
        if(parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
            for(int i = 0; i < mVisibleComments.size(); i++) {
                if(parentFullname.equals(mVisibleComments.get(i).getFullName())) {
                    parentPosition = i;
                    break;
                }
            }
        }

        mVisibleComments.get(parentPosition).addChild(comment);
        mVisibleComments.get(parentPosition).setHasReply(true);
        if(!mVisibleComments.get(parentPosition).isExpanded()) {
            expandChildren(parentPosition);
            notifyItemChanged(parentPosition);
        } else {
            mVisibleComments.add(parentPosition + 1, comment);
            notifyItemInserted(parentPosition + 1);
        }
    }

    void clearData() {
        mVisibleComments.clear();
        notifyDataSetChanged();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mVisibleComments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author_text_view_item_post_comment) TextView authorTextView;
        @BindView(R.id.comment_time_text_view_item_post_comment) TextView commentTimeTextView;
        @BindView(R.id.comment_markdown_view_item_post_comment) MarkwonView commentMarkdownView;
        @BindView(R.id.plus_button_item_post_comment) ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_comment) TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_comment) ImageView downvoteButton;
        @BindView(R.id.expand_button_item_post_comment) ImageView expandButton;
        @BindView(R.id.share_button_item_post_comment) ImageView shareButton;
        @BindView(R.id.reply_button_item_post_comment) ImageView replyButton;
        @BindView(R.id.vertical_block_item_post_comment) View verticalBlock;

        CommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            authorTextView.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mVisibleComments.get(getAdapterPosition()).getAuthor());
                mActivity.startActivity(intent);
            });

            shareButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String extraText = mVisibleComments.get(getAdapterPosition()).getPermalink();
                intent.putExtra(Intent.EXTRA_TEXT, extraText);
                mActivity.startActivity(Intent.createChooser(intent, "Share"));
            });

            expandButton.setOnClickListener(view -> {
                if(mVisibleComments.get(getAdapterPosition()).isExpanded()) {
                    collapseChildren(getAdapterPosition());
                    expandButton.setImageResource(R.drawable.ic_expand_more_black_20dp);
                } else {
                    expandChildren(getAdapterPosition());
                    mVisibleComments.get(getAdapterPosition()).setExpanded(true);
                    expandButton.setImageResource(R.drawable.ic_expand_less_black_20dp);
                }
            });

            replyButton.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, mVisibleComments.get(getAdapterPosition()).getDepth() + 1);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, mVisibleComments.get(getAdapterPosition()).getCommentContent());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mVisibleComments.get(getAdapterPosition()).getFullName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);
                intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, getAdapterPosition());
                mActivity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
            });

            upvoteButton.setOnClickListener(view -> {
                int previousVoteType = mVisibleComments.get(getAdapterPosition()).getVoteType();
                String newVoteType;

                downvoteButton.clearColorFilter();

                if(previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
                    //Not upvoted before
                    mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                    newVoteType = RedditUtils.DIR_UPVOTE;
                    upvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Upvoted before
                    mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    upvoteButton.clearColorFilter();
                }

                scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition()).getScore() + mVisibleComments.get(getAdapterPosition()).getVoteType()));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position) {
                        if(newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                            mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                            upvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                            upvoteButton.clearColorFilter();
                        }

                        downvoteButton.clearColorFilter();
                        scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition()).getScore() + mVisibleComments.get(getAdapterPosition()).getVoteType()));
                    }

                    @Override
                    public void onVoteThingFail(int position) { }
                }, mVisibleComments.get(getAdapterPosition()).getFullName(), newVoteType, getAdapterPosition());
            });

            downvoteButton.setOnClickListener(view -> {
                int previousVoteType = mVisibleComments.get(getAdapterPosition()).getVoteType();
                String newVoteType;

                upvoteButton.clearColorFilter();

                if(previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
                    //Not downvoted before
                    mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                    newVoteType = RedditUtils.DIR_DOWNVOTE;
                    downvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Downvoted before
                    mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    downvoteButton.clearColorFilter();
                }

                scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition()).getScore() + mVisibleComments.get(getAdapterPosition()).getVoteType()));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position1) {
                        if(newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                            mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                            downvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mVisibleComments.get(getAdapterPosition()).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                            downvoteButton.clearColorFilter();
                        }

                        upvoteButton.clearColorFilter();
                        scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition()).getScore() + mVisibleComments.get(getAdapterPosition()).getVoteType()));
                    }

                    @Override
                    public void onVoteThingFail(int position1) { }
                }, mVisibleComments.get(getAdapterPosition()).getFullName(), newVoteType, getAdapterPosition());
            });
        }
    }

    class LoadMoreCommentViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_item_load_more_comments) View verticalBlock;
        @BindView(R.id.placeholder_text_view_item_load_more_comments) TextView placeholderTextView;

        LoadMoreCommentViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            placeholderTextView.setOnClickListener(view -> {
                int parentPosition = getParentPosition(getAdapterPosition());
                CommentData parentComment = mVisibleComments.get(parentPosition);

                mVisibleComments.get(getAdapterPosition()).setLoadingMoreChildren(true);
                mVisibleComments.get(getAdapterPosition()).setLoadMoreChildrenFailed(false);
                placeholderTextView.setText(R.string.loading);

                FetchComment.fetchMoreComment(mRetrofit, mSubredditNamePrefixed, parentComment.getMoreChildrenFullnames(),
                        parentComment.getMoreChildrenStartingIndex(), parentComment.getDepth() + 1, mLocale,
                        new FetchComment.FetchMoreCommentListener() {
                            @Override
                            public void onFetchMoreCommentSuccess(ArrayList<CommentData> expandedComments,
                                                                  int childrenStartingIndex) {
                                if(mVisibleComments.size() > parentPosition
                                        && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                    if(mVisibleComments.get(parentPosition).isExpanded()) {
                                        if(mVisibleComments.get(parentPosition).getChildren().size() > childrenStartingIndex) {
                                            mVisibleComments.get(parentPosition).setMoreChildrenStartingIndex(childrenStartingIndex);
                                            mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                    .setLoadingMoreChildren(false);
                                            mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                                    .setLoadMoreChildrenFailed(false);

                                            int placeholderPosition = getAdapterPosition();
                                            if(mVisibleComments.get(getAdapterPosition()).getFullName().equals(parentComment.getFullName())) {
                                                for(int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                    if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                        placeholderPosition = i;
                                                        break;
                                                    }
                                                }
                                            }

                                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                            mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                            placeholderTextView.setText(R.string.comment_load_more_comments);

                                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                        } else {
                                            mVisibleComments.get(parentPosition).getChildren()
                                                    .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                            mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();

                                            int placeholderPosition = getAdapterPosition();
                                            if(mVisibleComments.get(getAdapterPosition()).getFullName().equals(parentComment.getFullName())) {
                                                for(int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                    if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                        placeholderPosition = i;
                                                        break;
                                                    }
                                                }
                                            }

                                            mVisibleComments.remove(placeholderPosition);
                                            notifyItemRemoved(placeholderPosition);

                                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                                            notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                        }
                                    } else {
                                        if(mVisibleComments.get(parentPosition).hasReply() && mVisibleComments.get(parentPosition).getChildren().size() <= childrenStartingIndex) {
                                            mVisibleComments.get(parentPosition).getChildren()
                                                    .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                            mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();
                                        }
                                    }

                                    mVisibleComments.get(parentPosition).addChildren(expandedComments);
                                } else {
                                    for(int i = 0; i < mVisibleComments.size(); i++) {
                                        if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                            if(mVisibleComments.get(i).isExpanded()) {
                                                int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();

                                                if(!mVisibleComments.get(i).getFullName()
                                                        .equals(mVisibleComments.get(placeholderPosition).getFullName())) {
                                                    for(int j = i + 1; j < mVisibleComments.size(); j++) {
                                                        if(mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                            placeholderPosition = j;
                                                        }
                                                    }
                                                }

                                                mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(false);
                                                placeholderTextView.setText(R.string.comment_load_more_comments);

                                                mVisibleComments.addAll(placeholderPosition, expandedComments);
                                                notifyItemRangeInserted(placeholderPosition, expandedComments.size());
                                            }

                                            mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                    .setLoadingMoreChildren(false);
                                            mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1)
                                                    .setLoadMoreChildrenFailed(false);
                                            mVisibleComments.get(i).addChildren(expandedComments);

                                            break;
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onFetchMoreCommentFailed() {
                                if(parentPosition < mVisibleComments.size()
                                        && parentComment.getFullName().equals(mVisibleComments.get(parentPosition).getFullName())) {
                                    if(mVisibleComments.get(parentPosition).isExpanded()) {
                                        int placeholderPosition = getAdapterPosition();
                                        if(!mVisibleComments.get(getAdapterPosition()).getFullName().equals(parentComment.getFullName())) {
                                            for(int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                    placeholderPosition = i;
                                                    break;
                                                }
                                            }
                                        }

                                        mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                        mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                        placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                    }

                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                            .setLoadingMoreChildren(false);
                                    mVisibleComments.get(parentPosition).getChildren().get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                                            .setLoadMoreChildrenFailed(true);
                                } else {
                                    for(int i = 0; i < mVisibleComments.size(); i++) {
                                        if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                            if(mVisibleComments.get(i).isExpanded()) {
                                                int placeholderPosition = i + mVisibleComments.get(i).getChildren().size();
                                                if(!mVisibleComments.get(placeholderPosition).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                    for(int j = i + 1; j < mVisibleComments.size(); j++) {
                                                        if(mVisibleComments.get(j).getFullName().equals(mVisibleComments.get(i).getFullName())) {
                                                            placeholderPosition = j;
                                                            break;
                                                        }
                                                    }
                                                }

                                                mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                                                mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                                                placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                                            }

                                            mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadingMoreChildren(false);
                                            mVisibleComments.get(i).getChildren().get(mVisibleComments.get(i).getChildren().size() - 1).setLoadMoreChildrenFailed(true);

                                            break;
                                        }
                                    }
                                }
                            }
                        });
            });
        }
    }
}
