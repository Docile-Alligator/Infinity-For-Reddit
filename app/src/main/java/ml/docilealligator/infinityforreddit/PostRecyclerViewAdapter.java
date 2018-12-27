package ml.docilealligator.infinityforreddit;

import android.arch.paging.PagedListAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
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

import java.util.List;

import CustomView.AspectRatioGifImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;

/**
 * Created by alex on 2/25/18.
 */

class PostRecyclerViewAdapter extends PagedListAdapter<Post, RecyclerView.ViewHolder> {
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private SharedPreferences mSharedPreferences;
    private RequestManager glide;
    private SubredditDao subredditDao;
    private boolean isLoadingMorePostSuccess = true;
    private boolean canStartActivity = true;
    private boolean hasMultipleSubreddits;

    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private NetworkState networkState;

    PostRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, SharedPreferences sharedPreferences, boolean hasMultipleSubreddits) {
        super(DIFF_CALLBACK);
        if(context != null) {
            mContext = context;
            mOauthRetrofit = oauthRetrofit;
            mSharedPreferences = sharedPreferences;
            this.hasMultipleSubreddits = hasMultipleSubreddits;
            glide = Glide.with(mContext.getApplicationContext());
            subredditDao = SubredditRoomDatabase.getDatabase(mContext.getApplicationContext()).subredditDao();
        }
    }

    static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post post, @NonNull Post t1) {
            return post.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post post, @NonNull Post t1) {
            return post.getTitle().equals(t1.getTitle());
        }
    };

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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        onBindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if(holder instanceof DataViewHolder) {
            Post post = getItem(position);
            if(post == null) {
                Log.i("is null", Integer.toString(holder.getAdapterPosition()));
            } else {
                final String id = post.getFullName();
                final String subredditName = post.getSubredditNamePrefixed();
                final String postTime = post.getPostTime();
                final String title = post.getTitle();
                final String permalink = post.getPermalink();
                int voteType = post.getVoteType();
                int gilded = post.getGilded();
                boolean nsfw = post.isNSFW();

                if(post.getSubredditIconUrl() == null) {
                    new LoadSubredditIconAsyncTask(subredditDao, subredditName,
                            new LoadSubredditIconAsyncTask.LoadSubredditIconAsyncTaskListener() {
                                @Override
                                public void loadIconSuccess(String iconImageUrl) {
                                    if(mContext != null && getItemCount() > 0) {
                                        if(!iconImageUrl.equals("")) {
                                            glide.load(iconImageUrl)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                    .error(glide.load(R.drawable.subreddit_default_icon))
                                                    .listener(new RequestListener<Drawable>() {
                                                        @Override
                                                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                            return false;
                                                        }

                                                        @Override
                                                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                            if(resource instanceof Animatable) {
                                                                //This is a gif
                                                                //((Animatable) resource).start();
                                                                ((DataViewHolder) holder).subredditIconGifImageView.startAnimation();
                                                            }
                                                            return false;
                                                        }
                                                    })
                                                    .into(((DataViewHolder) holder).subredditIconGifImageView);
                                        } else {
                                            glide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                    .into(((DataViewHolder) holder).subredditIconGifImageView);
                                        }

                                        if(holder.getAdapterPosition() >= 0) {
                                            post.setSubredditIconUrl(iconImageUrl);
                                        }
                                    }
                                }
                            }).execute();
                } else if(!post.getSubredditIconUrl().equals("")) {
                    glide.load(post.getSubredditIconUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(glide.load(R.drawable.subreddit_default_icon))
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                    if(resource instanceof Animatable) {
                                        //This is a gif
                                        //((Animatable) resource).start();
                                        ((DataViewHolder) holder).subredditIconGifImageView.startAnimation();
                                    }
                                    return false;
                                }
                            })
                            .into(((DataViewHolder) holder).subredditIconGifImageView);
                } else {
                    glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .into(((DataViewHolder) holder).subredditIconGifImageView);
                }

                ((DataViewHolder) holder).cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_TITLE, title);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                            mContext.startActivity(intent);
                        }
                    }
                });

                ((DataViewHolder) holder).subredditIconGifImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                    post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_VALUE_KEY,
                                    post.getSubredditNamePrefixed());
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_QUERY_BY_ID_KEY, false);
                            mContext.startActivity(intent);
                        }
                    }
                });

                ((DataViewHolder) holder).subredditNameTextView.setText(subredditName);

                ((DataViewHolder) holder).subredditNameTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                    post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_VALUE_KEY,
                                    post.getSubredditNamePrefixed());
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_QUERY_BY_ID_KEY, false);
                            mContext.startActivity(intent);
                        }
                    }
                });
                ((DataViewHolder) holder).postTimeTextView.setText(postTime);
                ((DataViewHolder) holder).titleTextView.setText(title);
                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore()));

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

                if(post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                    ((DataViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                    ((DataViewHolder) holder).imageView
                            .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());
                    loadImage(holder, post);
                }

                if(!hasMultipleSubreddits && post.isStickied()) {
                    ((DataViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    glide.load(R.drawable.thumbtack).into(((DataViewHolder) holder).stickiedPostImageView);
                }

                if(post.isCrosspost()) {
                    ((DataViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("IMAGE");

                        final String imageUrl = post.getUrl();
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
                    case Post.LINK_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("LINK");

                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                                // add share action to menu list
                                builder.addDefaultShareMenuItem();
                                builder.setToolbarColor(mContext.getResources().getColor(R.color.colorPrimary));
                                CustomTabsIntent customTabsIntent = builder.build();
                                customTabsIntent.launchUrl(mContext, Uri.parse(post.getUrl()));
                            }
                        });
                        break;
                    case Post.GIF_VIDEO_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("GIF");

                        final Uri gifVideoUri = Uri.parse(post.getVideoUrl());
                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, ViewVideoActivity.class);
                                intent.setData(gifVideoUri);
                                intent.putExtra(ViewVideoActivity.TITLE_KEY, title);
                                intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, post.isDashVideo());
                                intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, post.isDownloadableGifOrVideo());
                                if(post.isDownloadableGifOrVideo()) {
                                    intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, post.getGifOrVideoDownloadUrl());
                                    intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                                    intent.putExtra(ViewVideoActivity.ID_KEY, id);
                                }
                                mContext.startActivity(intent);
                            }
                        });
                        break;
                    case Post.VIDEO_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("VIDEO");

                        final Uri videoUri = Uri.parse(post.getVideoUrl());
                        ((DataViewHolder) holder).imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(mContext, ViewVideoActivity.class);
                                intent.setData(videoUri);
                                intent.putExtra(ViewVideoActivity.TITLE_KEY, title);
                                intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, post.isDashVideo());
                                intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, post.isDownloadableGifOrVideo());
                                if(post.isDownloadableGifOrVideo()) {
                                    intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, post.getGifOrVideoDownloadUrl());
                                    intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                                    intent.putExtra(ViewVideoActivity.ID_KEY, id);
                                }
                                mContext.startActivity(intent);
                            }
                        });
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("LINK");
                        final String noPreviewLinkUrl = post.getUrl();
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
                    case Post.TEXT_TYPE:
                        ((DataViewHolder) holder).typeTextView.setText("TEXT");
                        break;
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
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + 2));
                            } else {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + 1));
                            }

                            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    post.setVoteType(1);
                                    if(isDownvotedBefore) {
                                        post.setScore(post.getScore() + 2);
                                    } else {
                                        post.setScore(post.getScore() + 1);
                                    }
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot upvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore()));
                                    ((DataViewHolder) holder).downvoteButton.setColorFilter(downvoteButtonColorFilter);
                                }
                            }, id, RedditUtils.DIR_UPVOTE, holder.getAdapterPosition());
                        } else {
                            //Upvoted before
                            ((DataViewHolder) holder).upvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() - 1));

                            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    post.setVoteType(0);
                                    post.setScore(post.getScore() - 1);
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + 1));
                                    ((DataViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                                    post.setScore(post.getScore() + 1);
                                }
                            }, id, RedditUtils.DIR_UNVOTE, holder.getAdapterPosition());
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
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() - 2));
                            } else {
                                ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() - 1));
                            }

                            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    post.setVoteType(-1);
                                    if(isUpvotedBefore) {
                                        post.setScore(post.getScore() - 2);
                                    } else {
                                        post.setScore(post.getScore() - 1);
                                    }
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot downvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore()));
                                    ((DataViewHolder) holder).upvoteButton.setColorFilter(upvoteButtonColorFilter);
                                }
                            }, id, RedditUtils.DIR_DOWNVOTE, holder.getAdapterPosition());
                        } else {
                            //Down voted before
                            ((DataViewHolder) holder).downvoteButton.clearColorFilter();
                            ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + 1));

                            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                                @Override
                                public void onVoteThingSuccess(int position) {
                                    post.setVoteType(0);
                                    post.setScore(post.getScore());
                                }

                                @Override
                                public void onVoteThingFail(int position) {
                                    Toast.makeText(mContext, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                                    ((DataViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                                    ((DataViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore()));
                                    post.setScore(post.getScore());
                                }
                            }, id, RedditUtils.DIR_UNVOTE, holder.getAdapterPosition());
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
                    //mPaginationSynchronizer.getPaginationRetryNotifier().retry();
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

            /*mPaginationSynchronizer.setPaginationNotifier(mPaginationNotifier);

            if(!mPaginationSynchronizer.isLoadingMorePostsSuccess()) {
                ((LoadingViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((LoadingViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
            }*/
        }
    }

    private void loadImage(final RecyclerView.ViewHolder holder, final Post post) {
        RequestBuilder imageRequestBuilder = glide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                ((DataViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((DataViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                ((DataViewHolder)holder).errorRelativeLayout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((DataViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((DataViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    }
                });
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                ((DataViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                ((DataViewHolder) holder).progressBar.setVisibility(View.GONE);
                return false;
            }
        });

        if(post.isNSFW()) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
                    .into(((DataViewHolder) holder).imageView);
        } else {
            imageRequestBuilder.into(((DataViewHolder) holder).imageView);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (position >= getItemCount() ? VIEW_TYPE_LOADING : VIEW_TYPE_DATA);
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState != NetworkState.LOADED;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(getItemCount());
            } else {
                notifyItemInserted(getItemCount());
            }
        } else if (newExtraRow && previousState != newNetworkState) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_view_view_post_detail) CardView cardView;
        @BindView(R.id.subreddit_icon_gif_image_view_best_post_item) AspectRatioGifImageView subredditIconGifImageView;
        @BindView(R.id.subreddit_text_view_best_post_item) TextView subredditNameTextView;
        @BindView(R.id.stickied_post_image_view_best_post_item) ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_best_post_item) TextView postTimeTextView;
        @BindView(R.id.title_text_view_best_post_item) TextView titleTextView;
        @BindView(R.id.type_text_view_item_best_post) TextView typeTextView;
        @BindView(R.id.gilded_image_view_item_best_post) ImageView gildedImageView;
        @BindView(R.id.gilded_number_text_view_item_best_post) TextView gildedNumberTextView;
        @BindView(R.id.crosspost_image_view_item_best_post) ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_best_post) TextView nsfwTextView;
        @BindView(R.id.image_view_wrapper_item_best_post) RelativeLayout relativeLayout;
        @BindView(R.id.progress_bar_best_post_item) ProgressBar progressBar;
        @BindView(R.id.image_view_best_post_item) AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_best_post_item) RelativeLayout errorRelativeLayout;
        @BindView(R.id.image_view_no_preview_link_best_post_item) ImageView noPreviewLinkImageView;
        @BindView(R.id.plus_button_item_best_post) ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_best_post) TextView scoreTextView;
        @BindView(R.id.minus_button_item_best_post) ImageView downvoteButton;
        @BindView(R.id.share_button_item_best_post) ImageView shareButton;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_footer_progress_bar_item) ProgressBar progressBar;
        @BindView(R.id.relative_layout_footer_progress_bar_item) RelativeLayout relativeLayout;
        @BindView(R.id.retry_button_footer_progress_bar_item) Button retryButton;

        LoadingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if(holder instanceof DataViewHolder) {
            if(((DataViewHolder) holder).imageView.isAnimating()) {
                ((DataViewHolder) holder).imageView.stopAnimation();
            }
            if(((DataViewHolder) holder).subredditIconGifImageView.isAnimating()) {
                ((DataViewHolder) holder).subredditIconGifImageView.stopAnimation();
            }
            glide.clear(((DataViewHolder) holder).imageView);
            glide.clear(((DataViewHolder) holder).subredditIconGifImageView);
            ((DataViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).relativeLayout.setVisibility(View.GONE);
            ((DataViewHolder) holder).gildedImageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).gildedNumberTextView.setVisibility(View.GONE);
            ((DataViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((DataViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((DataViewHolder) holder).imageView.setVisibility(View.GONE);
            ((DataViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
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
