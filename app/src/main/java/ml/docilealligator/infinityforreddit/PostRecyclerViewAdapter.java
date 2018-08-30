package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by alex on 2/25/18.
 */

class PostRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private ArrayList<PostData> mPostData;
    private Context mContext;
    private PaginationSynchronizer mPaginationSynchronizer;
    private RequestManager glide;
    private SubredditDao subredditDao;
    private boolean isLoadingMorePostSuccess;
    private boolean canStartActivity;

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_LOADING = 1;


    PostRecyclerViewAdapter(Context context, ArrayList<PostData> postData, PaginationSynchronizer paginationSynchronizer) {
        if(context != null) {
            mContext = context;
            mPostData = postData;
            mPaginationSynchronizer = paginationSynchronizer;
            isLoadingMorePostSuccess = true;
            canStartActivity = true;
            glide = Glide.with(mContext);
            subredditDao = SubredditRoomDatabase.getDatabase(mContext).subredditDao();
        }
    }

    void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_DATA) {
            CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
            return new DataViewHolder(cardView);
        } else {
            LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_progress_bar, parent, false);
            return new LoadingViewHolder(linearLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, final int position) {
        if(holder instanceof DataViewHolder) {
            if(mPostData.get(position) == null) {
                Log.i("is null", Integer.toString(position));
            } else {
                final int adapterPosition = holder.getAdapterPosition();
                final String id = mPostData.get(adapterPosition).getFullName();
                final String subredditName = mPostData.get(adapterPosition).getSubredditNamePrefixed();
                final String postTime = mPostData.get(adapterPosition).getPostTime();
                final String title = mPostData.get(adapterPosition).getTitle();
                final String permalink = mPostData.get(adapterPosition).getPermalink();
                int voteType = mPostData.get(adapterPosition).getVoteType();
                int gilded = mPostData.get(adapterPosition).getGilded();
                boolean nsfw = mPostData.get(adapterPosition).isNSFW();

                if(mPostData.get(adapterPosition).getSubredditIconUrl() == null) {
                    new LoadSubredditIconAsyncTask(subredditDao, subredditName,
                            new LoadSubredditIconAsyncTask.LoadSubredditIconAsyncTaskListener() {
                                @Override
                                public void loadIconSuccess(String iconImageUrl) {
                                    if(!iconImageUrl.equals("")) {
                                        Glide.with(mContext).load(iconImageUrl)
                                                .into(((DataViewHolder) holder).subredditIconCircleImageView);
                                    } else {
                                        Glide.with(mContext).load(R.drawable.subreddit_default_icon)
                                                .into(((DataViewHolder) holder).subredditIconCircleImageView);
                                    }

                                    mPostData.get(adapterPosition).setSubredditIconUrl(iconImageUrl);
                                }
                            }).execute();
                } else if(!mPostData.get(position).getSubredditIconUrl().equals("")) {
                    glide.load(mPostData.get(position).getSubredditIconUrl()).into(((DataViewHolder) holder).subredditIconCircleImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon).into(((DataViewHolder) holder).subredditIconCircleImageView);
                }

                ((DataViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_TITLE, title);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, mPostData.get(adapterPosition));
                            mContext.startActivity(intent);
                        }
                    }
                });

                ((DataViewHolder) holder).subredditIconCircleImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                        intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                mPostData.get(adapterPosition).getSubredditNamePrefixed().substring(2));
                    }
                });

                ((DataViewHolder) holder).subredditNameTextView.setText(subredditName);
                ((DataViewHolder) holder).postTimeTextView.setText(postTime);
                ((DataViewHolder) holder).titleTextView.setText(title);
                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(position).getScore()));

                if(gilded > 0) {
                    ((DataViewHolder) holder).gildedImageView.setVisibility(View.VISIBLE);
                    glide.load(R.drawable.gold).into(((DataViewHolder) holder).gildedImageView);
                    ((DataViewHolder) holder).gildedNumberTextView.setVisibility(View.VISIBLE);
                    String gildedNumber = mContext.getResources().getString(R.string.gilded, gilded);
                    ((DataViewHolder) holder).gildedNumberTextView.setText(gildedNumber);
                }

                if(nsfw) {
                    ((DataViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                switch (voteType) {
                    case 1:
                        //Upvote
                        ((DataViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                    case -1:
                        //Downvote
                        ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                        break;
                }

                if(mPostData.get(position).getPostType() != PostData.TEXT_TYPE && mPostData.get(position).getPostType() != PostData.NO_PREVIEW_LINK_TYPE) {
                    ((DataViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).imageView.setVisibility(View.VISIBLE);

                    String previewUrl = mPostData.get(position).getPreviewUrl();
                    RequestBuilder imageRequestBuilder = glide.load(previewUrl).listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((DataViewHolder) holder).progressBar.setVisibility(View.GONE);
                            return false;
                        }
                    });

                    if(mPostData.get(position).isNSFW()) {
                        imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(25, 3)))
                                .into(((DataViewHolder) holder).imageView);
                    } else {
                        imageRequestBuilder.into(((DataViewHolder) holder).imageView);
                    }
                }

                if(mPostData.get(position).isStickied()) {
                    ((DataViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    glide.load(R.drawable.thumbtack).into(((DataViewHolder) holder).stickiedPostImageView);
                }

                switch (mPostData.get(position).getPostType()) {
                    case PostData.IMAGE_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("IMAGE");

                        final String imageUrl = mPostData.get(position).getUrl();
                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, ViewImageActivity.class);
                                intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, imageUrl);
                                intent.putExtra(ViewImageActivity.TITLE_KEY, title);
                                intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName.substring(2)
                                        + "-" + id.substring(3));
                                mContext.startActivity(intent);
                            }
                        });
                        break;
                    case PostData.LINK_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("LINK");

                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                // add share action to menu list
                                builder.addDefaultShareMenuItem();
                                builder.setToolbarColor(mContext.getResources().getColor(R.color.colorPrimary));
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(mContext, Uri.parse(mPostData.get(position).getUrl()));
                            }
                        });
                        break;
                    case PostData.GIF_VIDEO_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("GIF");

                        final Uri gifVideoUri = Uri.parse(mPostData.get(position).getVideoUrl());
                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, ViewVideoActivity.class);
                                intent.setData(gifVideoUri);
                                intent.putExtra(ViewVideoActivity.TITLE_KEY, title);
                                intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPostData.get(adapterPosition).isDashVideo());
                                intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPostData.get(adapterPosition).isDownloadableGifOrVideo());
                                if(mPostData.get(adapterPosition).isDownloadableGifOrVideo()) {
                                    intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPostData.get(adapterPosition).getGifOrVideoDownloadUrl());
                                    intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                                    intent.putExtra(ViewVideoActivity.ID_KEY, id);
                                }
                                mContext.startActivity(intent);
                            }
                        });
                        break;
                    case PostData.VIDEO_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("VIDEO");

                        final Uri videoUri = Uri.parse(mPostData.get(position).getVideoUrl());
                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, ViewVideoActivity.class);
                                intent.setData(videoUri);
                                intent.putExtra(ViewVideoActivity.TITLE_KEY, title);
                                intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPostData.get(adapterPosition).isDashVideo());
                                intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPostData.get(adapterPosition).isDownloadableGifOrVideo());
                                if(mPostData.get(adapterPosition).isDownloadableGifOrVideo()) {
                                    intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPostData.get(adapterPosition).getGifOrVideoDownloadUrl());
                                    intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                                    intent.putExtra(ViewVideoActivity.ID_KEY, id);
                                }
                                mContext.startActivity(intent);
                            }
                        });
                        break;
                    case PostData.NO_PREVIEW_LINK_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("LINK");
                        final String noPreviewLinkUrl = mPostData.get(position).getUrl();
                        ((DataViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((DataViewHolder) holder).noPreviewLinkImageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                // add share action to menu list
                                builder.addDefaultShareMenuItem();
                                builder.setToolbarColor(mContext.getResources().getColor(R.color.colorPrimary));
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(mContext, Uri.parse(noPreviewLinkUrl));
                            }
                        });
                        break;
                    default:
                        ((DataViewHolder) holder).typeTextView.setText("TEXT");
                }

                ((DataViewHolder) holder).upvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final boolean isDownvotedBefore = ((DataViewHolder) holder).downvoteButton.getColorFilter() != null;

                        final ColorFilter downvoteButtonColorFilter = ((DataViewHolder) holder).downvoteButton.getColorFilter();
                        ((DataViewHolder) holder).downvoteButton.clearColorFilter();

                        if (((DataViewHolder) holder).upvoteButton.getColorFilter() == null) {
                            ((DataViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                            if(isDownvotedBefore) {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() + 2));
                            } else {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() + 1));
                            }

                            VoteThing.voteThing(mContext, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    mPostData.get(position).setVoteType(1);
                                    if(isDownvotedBefore) {
                                        mPostData.get(position).setScore(mPostData.get(position).getScore() + 2);
                                    } else {
                                        mPostData.get(position).setScore(mPostData.get(position).getScore() + 1);
                                    }
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot upvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(position).getScore()));
                                    ((DataViewHolder) holder).downvoteButton.setColorFilter(downvoteButtonColorFilter);
                                }
                            }, id, RedditUtils.DIR_UPVOTE, ((DataViewHolder) holder).getAdapterPosition(), 1);
                        } else {
                            //Upvoted before
                            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() - 1));

                            VoteThing.voteThing(mContext, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    mPostData.get(position).setVoteType(0);
                                    mPostData.get(position).setScore(mPostData.get(position).getScore() - 1);
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(position).getScore() + 1));
                                    ((DataViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                                    mPostData.get(position).setScore(mPostData.get(position).getScore() + 1);
                                }
                            }, id, RedditUtils.DIR_UNVOTE, ((DataViewHolder) holder).getAdapterPosition(), 1);
                        }
                    }
                });

                ((DataViewHolder) holder).downvoteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final boolean isUpvotedBefore = ((DataViewHolder) holder).upvoteButton.getColorFilter() != null;

                        final ColorFilter upvoteButtonColorFilter = ((DataViewHolder) holder).upvoteButton.getColorFilter();
                        ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                        if (((DataViewHolder) holder).downvoteButton.getColorFilter() == null) {
                            ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                            if (isUpvotedBefore) {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() - 2));
                            } else {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() - 1));
                            }

                            VoteThing.voteThing(mContext, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    mPostData.get(position).setVoteType(-1);
                                    if(isUpvotedBefore) {
                                        mPostData.get(position).setScore(mPostData.get(position).getScore() - 2);
                                    } else {
                                        mPostData.get(position).setScore(mPostData.get(position).getScore() - 1);
                                    }
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot downvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(position).getScore()));
                                    ((DataViewHolder) holder).upvoteButton.setColorFilter(upvoteButtonColorFilter);
                                }
                            }, id, RedditUtils.DIR_DOWNVOTE, adapterPosition, 1);
                        } else {
                            //Down voted before
                            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(adapterPosition).getScore() + 1));

                            VoteThing.voteThing(mContext, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    mPostData.get(position).setVoteType(0);
                                    mPostData.get(position).setScore(mPostData.get(position).getScore());
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(mPostData.get(position).getScore()));
                                    mPostData.get(position).setScore(mPostData.get(position).getScore());
                                }
                            }, id, RedditUtils.DIR_UNVOTE, adapterPosition, 1);
                        }
                    }
                });

                ((DataViewHolder) holder).shareButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        String extraText = title + "\n" + permalink;
                        intent.putExtra(Intent.EXTRA_TEXT, extraText);
                        mContext.startActivity(Intent.createChooser(intent, "Share"));
                    }
                });
            }
        } else if(holder instanceof LoadingViewHolder) {
            ((LoadingViewHolder) holder).retryButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mPaginationSynchronizer.getPaginationRetryNotifier().retry();
                    ((LoadingViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.GONE);
                }
            });

            PaginationNotifier mPaginationNotifier = new PaginationNotifier() {
                @Override
                public void LoadMorePostSuccess() {
                    isLoadingMorePostSuccess = true;
                }

                @Override
                public void LoadMorePostFail() {
                    ((LoadingViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    isLoadingMorePostSuccess = false;
                }
            };

            mPaginationSynchronizer.setPaginationNotifier(mPaginationNotifier);

            if(!mPaginationSynchronizer.isLoadSuccess()) {
                ((LoadingViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mPostData.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= mPostData.size() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA);
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        private CardView cardView;
        private CircleImageView subredditIconCircleImageView;
        private TextView subredditNameTextView;
        private ImageView stickiedPostImageView;
        private TextView postTimeTextView;
        private TextView titleTextView;
        private TextView typeTextView;
        private ImageView gildedImageView;
        private TextView gildedNumberTextView;
        private TextView nsfwTextView;
        private RelativeLayout relativeLayout;
        private ProgressBar progressBar;
        private ImageView imageView;
        private ImageView noPreviewLinkImageView;
        private ImageView upvoteButton;
        private TextView scoreTextView;
        private ImageView downvoteButton;
        private ImageView shareButton;

        DataViewHolder(CardView itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_view_view_post_detail);
            subredditIconCircleImageView = itemView.findViewById(R.id.subreddit_icon_circle_image_view_best_post_item);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_text_view_best_post_item);
            stickiedPostImageView = itemView.findViewById(R.id.stickied_post_image_view_best_post_item);
            postTimeTextView = itemView.findViewById(R.id.post_time_text_view_best_post_item);
            titleTextView = itemView.findViewById(R.id.title_text_view_best_post_item);
            typeTextView = itemView.findViewById(R.id.type_text_view_item_best_post);
            gildedImageView = itemView.findViewById(R.id.gilded_image_view_item_best_post);
            gildedNumberTextView = itemView.findViewById(R.id.gilded_number_text_view_item_best_post);
            nsfwTextView = itemView.findViewById(R.id.nsfw_text_view_item_best_post);
            relativeLayout = itemView.findViewById(R.id.image_view_wrapper_item_best_post);
            progressBar = itemView.findViewById(R.id.progress_bar_best_post_item);
            imageView = itemView.findViewById(R.id.image_view_best_post_item);
            noPreviewLinkImageView = itemView.findViewById(R.id.image_view_no_preview_link_best_post_item);

            upvoteButton = itemView.findViewById(R.id.plus_button_item_best_post);
            scoreTextView = itemView.findViewById(R.id.score_text_view_item_best_post);
            downvoteButton = itemView.findViewById(R.id.minus_button_item_best_post);
            shareButton = itemView.findViewById(R.id.share_button_item_best_post);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        private ProgressBar progressBar;
        private RelativeLayout relativeLayout;
        private Button retryButton;

        LoadingViewHolder(LinearLayout itemView) {
            super(itemView);
            progressBar = itemView.findViewById(R.id.progress_bar_footer_progress_bar_item);
            relativeLayout = itemView.findViewById(R.id.relative_layout_footer_progress_bar_item);
            retryButton = itemView.findViewById(R.id.retry_button_footer_progress_bar_item);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof DataViewHolder) {
            glide.clear(((DataViewHolder) holder).imageView);
            glide.clear(((DataViewHolder) holder).subredditIconCircleImageView);
            ((DataViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).relativeLayout.setVisibility(View.GONE);
            ((DataViewHolder) holder).gildedNumberTextView.setVisibility(View.GONE);
            glide.clear(((DataViewHolder) holder).gildedImageView);
            ((DataViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((DataViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((DataViewHolder) holder).imageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
        } else if(holder instanceof LoadingViewHolder) {
            if(isLoadingMorePostSuccess) {
                ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.GONE);
                ((LoadingViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            } else {
                ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                ((LoadingViewHolder) holder).progressBar.setVisibility(View.GONE);
            }
        }
    }
}
