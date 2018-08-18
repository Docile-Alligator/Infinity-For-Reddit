package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.graphics.ColorFilter;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;

import java.util.ArrayList;

class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private ArrayList<CommentData> mCommentData;
    private RequestQueue mVoteThingRequestQueue;
    private RequestQueue mAcquireAccessTokenRequestQueue;

    CommentRecyclerViewAdapter(Context context, ArrayList<CommentData> commentData,
                               RequestQueue voteThingRequestQueue, RequestQueue acquireAccessTokenRequestQueue) {
        mContext = context;
        mCommentData = commentData;
        mVoteThingRequestQueue = voteThingRequestQueue;
        mAcquireAccessTokenRequestQueue = acquireAccessTokenRequestQueue;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        ((CommentViewHolder) holder).authorTextView.setText(mCommentData.get(position).getAuthor());
        ((CommentViewHolder) holder).commentTimeTextView.setText(mCommentData.get(position).getCommentTime());
        ((CommentViewHolder) holder).commentTextView.setText(mCommentData.get(position).getCommentContent());
        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore()));
        ((CommentViewHolder) holder).upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean isDownvotedBefore = ((CommentViewHolder) holder).downvoteButton.getColorFilter() != null;
                final ColorFilter minusButtonColorFilter = ((CommentViewHolder) holder).downvoteButton.getColorFilter();
                ((CommentViewHolder) holder).downvoteButton.clearColorFilter();

                if (((CommentViewHolder) holder).upvoteButton.getColorFilter() == null) {
                    ((CommentViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    if(isDownvotedBefore) {
                        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() + 2));
                    } else {
                        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() + 1));
                    }

                    new VoteThing(mContext, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            if(isDownvotedBefore) {
                                mCommentData.get(position).setScore(mCommentData.get(position).getScore() + 2);
                            } else {
                                mCommentData.get(position).setScore(mCommentData.get(position).getScore() + 1);
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                            Toast.makeText(mContext, "Cannot upvote this comment", Toast.LENGTH_SHORT).show();
                            ((CommentViewHolder) holder).upvoteButton.clearColorFilter();
                            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore()));
                            ((CommentViewHolder) holder).downvoteButton.setColorFilter(minusButtonColorFilter);
                        }
                    }, mCommentData.get(position).getFullName(), RedditUtils.DIR_UPVOTE, ((CommentViewHolder) holder).getAdapterPosition(), 1);
                } else {
                    //Upvoted before
                    ((CommentViewHolder) holder).upvoteButton.clearColorFilter();
                    ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() - 1));

                    new VoteThing(mContext, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            mCommentData.get(position).setScore(mCommentData.get(position).getScore() - 1);
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                            Toast.makeText(mContext, "Cannot unvote this comment", Toast.LENGTH_SHORT).show();
                            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() + 1));
                            ((CommentViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                            mCommentData.get(position).setScore(mCommentData.get(position).getScore() + 1);
                        }
                    }, mCommentData.get(position).getFullName(), RedditUtils.DIR_UNVOTE, ((CommentViewHolder) holder).getAdapterPosition(), 1);
                }
            }
        });

        ((CommentViewHolder) holder).downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final boolean isUpvotedBefore = ((CommentViewHolder) holder).upvoteButton.getColorFilter() != null;

                final ColorFilter upvoteButtonColorFilter = ((CommentViewHolder) holder).upvoteButton.getColorFilter();
                ((CommentViewHolder) holder).upvoteButton.clearColorFilter();

                if (((CommentViewHolder) holder).downvoteButton.getColorFilter() == null) {
                    ((CommentViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    if (isUpvotedBefore) {
                        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() - 2));
                    } else {
                        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() - 1));
                    }

                    new VoteThing(mContext, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            if(isUpvotedBefore) {
                                mCommentData.get(position).setScore(mCommentData.get(position).getScore() - 2);
                            } else {
                                mCommentData.get(position).setScore(mCommentData.get(position).getScore() - 1);
                            }
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                            Toast.makeText(mContext, "Cannot downvote this comment", Toast.LENGTH_SHORT).show();
                            ((CommentViewHolder) holder).downvoteButton.clearColorFilter();
                            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore()));
                            ((CommentViewHolder) holder).upvoteButton.setColorFilter(upvoteButtonColorFilter);
                        }
                    }, mCommentData.get(position).getFullName(), RedditUtils.DIR_DOWNVOTE, holder.getAdapterPosition(), 1);
                } else {
                    //Down voted before
                    ((CommentViewHolder) holder).downvoteButton.clearColorFilter();
                    ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore() + 1));

                    new VoteThing(mContext, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position) {
                            mCommentData.get(position).setScore(mCommentData.get(position).getScore());
                        }

                        @Override
                        public void onVoteThingFail(int position) {
                            Toast.makeText(mContext, "Cannot unvote this comment", Toast.LENGTH_SHORT).show();
                            ((CommentViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(mCommentData.get(position).getScore()));
                            mCommentData.get(position).setScore(mCommentData.get(position).getScore());
                        }
                    }, mCommentData.get(position).getFullName(), RedditUtils.DIR_UNVOTE, holder.getAdapterPosition(), 1);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCommentData.size();
    }

    private class CommentViewHolder extends RecyclerView.ViewHolder {
        private TextView authorTextView;
        private TextView commentTimeTextView;
        private TextView commentTextView;
        private ImageView upvoteButton;
        private ImageView downvoteButton;
        private TextView scoreTextView;
        private ImageView replyButton;

        public CommentViewHolder(View itemView) {
            super(itemView);
            authorTextView = itemView.findViewById(R.id.author_text_view_item_post_comment);
            commentTimeTextView = itemView.findViewById(R.id.comment_time_text_view_item_post_comment);
            commentTextView = itemView.findViewById(R.id.comment_text_view_item_post_comment);
            upvoteButton = itemView.findViewById(R.id.plus_button_item_post_comment);
            downvoteButton = itemView.findViewById(R.id.minus_button_item_post_comment);
            scoreTextView = itemView.findViewById(R.id.score_text_view_item_post_comment);
            replyButton = itemView.findViewById(R.id.reply_button_item_post_comment);
        }
    }
}
