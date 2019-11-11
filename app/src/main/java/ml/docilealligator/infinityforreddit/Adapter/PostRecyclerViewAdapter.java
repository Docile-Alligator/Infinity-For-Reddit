package ml.docilealligator.infinityforreddit.Adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.content.ContextCompat;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Activity.FilteredThingActivity;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewGIFActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewImageActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewPostDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewSubredditDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewVideoActivity;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadSubredditIconAsyncTask;
import ml.docilealligator.infinityforreddit.AsyncTask.LoadUserDataAsyncTask;
import ml.docilealligator.infinityforreddit.CustomView.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.Event.PostUpdateEventToDetailActivity;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.Post;
import ml.docilealligator.infinityforreddit.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.RedditUtils;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.VoteThing;
import retrofit2.Retrofit;

/**
 * Created by alex on 2/25/18.
 */

public class PostRecyclerViewAdapter extends PagedListAdapter<Post, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Post> DIFF_CALLBACK = new DiffUtil.ItemCallback<Post>() {
        @Override
        public boolean areItemsTheSame(@NonNull Post post, @NonNull Post t1) {
            return post.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Post post, @NonNull Post t1) {
            return false;
        }
    };
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private Retrofit mRetrofit;
    private String mAccessToken;
    private RequestManager mGlide;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private UserDao mUserDao;
    private boolean canStartActivity = true;
    private int mPostType;
    private int mPostLayout;
    private boolean mDisplaySubredditName;
    private boolean mVoteButtonsOnTheRight;
    private boolean mNeedBlurNSFW;
    private boolean mNeedBlurSpoiler;
    private NetworkState networkState;
    private Callback mCallback;

    public PostRecyclerViewAdapter(Context context, Retrofit oauthRetrofit, Retrofit retrofit,
                                   RedditDataRoomDatabase redditDataRoomDatabase, String accessToken,
                                   int postType, int postLayout, boolean displaySubredditName,
                                   boolean needBlurNSFW, boolean needBlurSpoiler, boolean voteButtonsOnTheRight,
                                   Callback callback) {
        super(DIFF_CALLBACK);
        if (context != null) {
            mContext = context;
            mOauthRetrofit = oauthRetrofit;
            mRetrofit = retrofit;
            mAccessToken = accessToken;
            mPostType = postType;
            mDisplaySubredditName = displaySubredditName;
            mNeedBlurNSFW = needBlurNSFW;
            mNeedBlurSpoiler = needBlurSpoiler;
            mVoteButtonsOnTheRight = voteButtonsOnTheRight;
            mPostLayout = postLayout;
            mGlide = Glide.with(mContext.getApplicationContext());
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mUserDao = redditDataRoomDatabase.userDao();
            mCallback = callback;
        }
    }

    public void setCanStartActivity(boolean canStartActivity) {
        this.canStartActivity = canStartActivity;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            if (mPostLayout == SharedPreferencesUtils.POST_LAYOUT_CARD) {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
                return new PostViewHolder(cardView);
            } else {
                MaterialCardView cardView = (MaterialCardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_compact, parent, false);
                return new PostCompactViewHolder(cardView);
            }
        } else if (viewType == VIEW_TYPE_ERROR) {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false);
            return new ErrorViewHolder(relativeLayout);
        } else {
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false);
            return new LoadingViewHolder(relativeLayout);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                final String fullName = post.getFullName();
                final String id = post.getId();
                final String subredditNamePrefixed = post.getSubredditNamePrefixed();
                String subredditName = subredditNamePrefixed.substring(2);
                String authorPrefixed = "u/" + post.getAuthor();
                final String postTime = post.getPostTime();
                final String title = post.getTitle();
                final String permalink = post.getPermalink();
                int voteType = post.getVoteType();
                int gilded = post.getGilded();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                boolean isArchived = post.isArchived();

                ((PostViewHolder) holder).cardView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;

                        Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, position);
                        mContext.startActivity(intent);
                    }
                });

                ((PostViewHolder) holder).subredditTextView.setText(subredditNamePrefixed);
                ((PostViewHolder) holder).userTextView.setText(authorPrefixed);
                ((PostViewHolder) holder).userTextView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;
                        Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mContext.startActivity(intent);
                    }
                });

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mContext != null && getItemCount() > 0) {
                                    if (iconImageUrl == null || iconImageUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconImageUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconImageUrl);
                                    }
                                }
                            }).execute();
                        } else if (!post.getAuthorIconUrl().equals("")) {
                            mGlide.load(post.getAuthorIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostViewHolder) holder).iconGifImageView);
                        }
                    } else {
                        if (post.getSubredditIconUrl() == null) {
                            new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase, subredditName, mRetrofit,
                                    iconImageUrl -> {
                                        if (mContext != null && getItemCount() > 0) {
                                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                                mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .into(((PostViewHolder) holder).iconGifImageView);
                                            } else {
                                                mGlide.load(iconImageUrl)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                        .into(((PostViewHolder) holder).iconGifImageView);
                                            }

                                            if (holder.getAdapterPosition() >= 0) {
                                                post.setSubredditIconUrl(iconImageUrl);
                                            }
                                        }
                                    }).execute();
                        } else if (!post.getSubredditIconUrl().equals("")) {
                            mGlide.load(post.getSubredditIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostViewHolder) holder).iconGifImageView);
                        }
                    }

                    ((PostViewHolder) holder).subredditTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));

                    ((PostViewHolder) holder).subredditTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mContext.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    ((PostViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostViewHolder) holder).subredditTextView.performClick());
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mContext != null && getItemCount() > 0) {
                                if (iconImageUrl == null || iconImageUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconImageUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconImageUrl);
                                }
                            }
                        }).execute();
                    } else if (!post.getAuthorIconUrl().equals("")) {
                        mGlide.load(post.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((PostViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostViewHolder) holder).iconGifImageView);
                    }

                    ((PostViewHolder) holder).subredditTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDarkDayNightTheme));

                    ((PostViewHolder) holder).subredditTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                            mContext.startActivity(intent);
                        }
                    });

                    ((PostViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostViewHolder) holder).subredditTextView.performClick());
                }

                ((PostViewHolder) holder).postTimeTextView.setText(postTime);
                ((PostViewHolder) holder).titleTextView.setText(title);
                ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                if (gilded > 0) {
                    ((PostViewHolder) holder).gildedNumberTextView.setVisibility(View.VISIBLE);
                    String gildedNumber = mContext.getResources().getString(R.string.gilded_count, gilded);
                    ((PostViewHolder) holder).gildedNumberTextView.setText(gildedNumber);
                }

                if (post.isLocked()) {
                    ((PostViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (nsfw) {
                    if (!(mContext instanceof FilteredThingActivity)) {
                        ((PostViewHolder) holder).nsfwTextView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                            mContext.startActivity(intent);
                        });
                    }
                    ((PostViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                if (spoiler) {
                    ((PostViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
                }

                if (flair != null) {
                    ((PostViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                    ((PostViewHolder) holder).flairTextView.setText(flair);
                }

                switch (voteType) {
                    case 1:
                        //Upvoted
                        ((PostViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                        break;
                    case -1:
                        //Downvoted
                        ((PostViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                        break;
                }

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                    ((PostViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    ((PostViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((PostViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                    ((PostViewHolder) holder).imageView
                            .setRatio((float) post.getPreviewHeight() / post.getPreviewWidth());
                    loadImage(holder, post);
                }

                if (mPostType == PostDataSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
                    ((PostViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    mGlide.load(R.drawable.ic_thumbtack_24dp).into(((PostViewHolder) holder).stickiedPostImageView);
                }

                if (isArchived) {
                    ((PostViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);

                    ((PostViewHolder) holder).upvoteButton
                            .setColorFilter(ContextCompat.getColor(mContext, R.color.voteAndReplyUnavailableVoteButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostViewHolder) holder).downvoteButton
                            .setColorFilter(ContextCompat.getColor(mContext, R.color.voteAndReplyUnavailableVoteButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                if (!(mContext instanceof FilteredThingActivity)) {
                    ((PostViewHolder) holder).typeTextView.setOnClickListener(view -> mCallback.typeChipClicked(post.getPostType()));
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.image);

                        final String imageUrl = post.getUrl();
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewImageActivity.class);
                            intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, imageUrl);
                            intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".jpg");
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.LINK_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.link);

                        ((PostViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostViewHolder) holder).linkTextView.setText(domain);

                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.GIF_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.gif);

                        final Uri gifVideoUri = Uri.parse(post.getVideoUrl());
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewGIFActivity.class);
                            intent.setData(gifVideoUri);
                            intent.putExtra(ViewGIFActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".gif");
                            intent.putExtra(ViewGIFActivity.IMAGE_URL_KEY, post.getVideoUrl());
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.video);

                        final Uri videoUri = Uri.parse(post.getVideoUrl());
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewVideoActivity.class);
                            intent.setData(videoUri);
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                            intent.putExtra(ViewVideoActivity.ID_KEY, fullName);
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((PostViewHolder) holder).noPreviewLinkImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.TEXT_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.text);
                        if (post.getSelfTextPlainTrimmed() != null && !post.getSelfTextPlainTrimmed().equals("")) {
                            ((PostViewHolder) holder).contentTextView.setVisibility(View.VISIBLE);
                            ((PostViewHolder) holder).contentTextView.setText(post.getSelfTextPlainTrimmed());
                        }
                        break;
                }

                ((PostViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mContext, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostViewHolder) holder).downvoteButton.clearColorFilter();

                    if (previousUpvoteButtonColorFilter == null) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = RedditUtils.DIR_UPVOTE;
                        ((PostViewHolder) holder).upvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostViewHolder) holder).upvoteButton.clearColorFilter();
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                ((PostViewHolder) holder).upvoteButton
                                        .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                            } else {
                                post.setVoteType(0);
                                ((PostViewHolder) holder).upvoteButton.clearColorFilter();
                                ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((PostViewHolder) holder).downvoteButton.clearColorFilter();
                            ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mContext, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + previousVoteType));
                            ((PostViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mContext, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostViewHolder) holder).upvoteButton.clearColorFilter();

                    if (previousDownvoteButtonColorFilter == null) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = RedditUtils.DIR_DOWNVOTE;
                        ((PostViewHolder) holder).downvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostViewHolder) holder).downvoteButton.clearColorFilter();
                        ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                ((PostViewHolder) holder).downvoteButton
                                        .setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                            } else {
                                post.setVoteType(0);
                                ((PostViewHolder) holder).downvoteButton.clearColorFilter();
                                ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((PostViewHolder) holder).upvoteButton.clearColorFilter();
                            ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mContext, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + previousVoteType));
                            ((PostViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));

                if (post.isSaved()) {
                    ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                ((PostViewHolder) holder).saveButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isSaved()) {
                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(false);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    } else {
                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                });

                ((PostViewHolder) holder).shareButton.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        String extraText = title + "\n" + permalink;
                        intent.putExtra(Intent.EXTRA_TEXT, extraText);
                        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        } else if (holder instanceof PostCompactViewHolder) {
            Post post = getItem(position);
            if (post != null) {
                final String fullName = post.getFullName();
                final String id = post.getId();
                final String subredditNamePrefixed = post.getSubredditNamePrefixed();
                String subredditName = subredditNamePrefixed.substring(2);
                String authorPrefixed = "u/" + post.getAuthor();
                final String postTime = post.getPostTime();
                final String title = post.getTitle();
                final String permalink = post.getPermalink();
                int voteType = post.getVoteType();
                int gilded = post.getGilded();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                boolean isArchived = post.isArchived();

                ((PostCompactViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;

                        Intent intent = new Intent(mContext, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, position);
                        mContext.startActivity(intent);
                    }
                });

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mContext != null && getItemCount() > 0) {
                                    if (iconImageUrl == null || iconImageUrl.equals("")) {
                                        mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .into(((PostCompactViewHolder) holder).iconGifImageView);
                                    } else {
                                        mGlide.load(iconImageUrl)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                .into(((PostCompactViewHolder) holder).iconGifImageView);
                                    }

                                    if (holder.getAdapterPosition() >= 0) {
                                        post.setAuthorIconUrl(iconImageUrl);
                                    }
                                }
                            }).execute();
                        } else if (!post.getAuthorIconUrl().equals("")) {
                            mGlide.load(post.getAuthorIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostCompactViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostCompactViewHolder) holder).iconGifImageView);
                        }
                    } else {
                        if (post.getSubredditIconUrl() == null) {
                            new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase, subredditName, mRetrofit,
                                    iconImageUrl -> {
                                        if (mContext != null && getItemCount() > 0) {
                                            if (iconImageUrl == null || iconImageUrl.equals("")) {
                                                mGlide.load(R.drawable.subreddit_default_icon)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .into(((PostCompactViewHolder) holder).iconGifImageView);
                                            } else {
                                                mGlide.load(iconImageUrl)
                                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                                        .into(((PostCompactViewHolder) holder).iconGifImageView);
                                            }

                                            if (holder.getAdapterPosition() >= 0) {
                                                post.setSubredditIconUrl(iconImageUrl);
                                            }
                                        }
                                    }).execute();
                        } else if (!post.getSubredditIconUrl().equals("")) {
                            mGlide.load(post.getSubredditIconUrl())
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(((PostCompactViewHolder) holder).iconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(((PostCompactViewHolder) holder).iconGifImageView);
                        }
                    }

                    ((PostCompactViewHolder) holder).nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorAccent));
                    ((PostCompactViewHolder) holder).nameTextView.setText(subredditNamePrefixed);

                    ((PostCompactViewHolder) holder).nameTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mContext.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mContext, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mContext.startActivity(intent);
                            }
                        }
                    });

                    ((PostCompactViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostCompactViewHolder) holder).nameTextView.performClick());
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mContext != null && getItemCount() > 0) {
                                if (iconImageUrl == null || iconImageUrl.equals("")) {
                                    mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .into(((PostCompactViewHolder) holder).iconGifImageView);
                                } else {
                                    mGlide.load(iconImageUrl)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                            .into(((PostCompactViewHolder) holder).iconGifImageView);
                                }

                                if (holder.getAdapterPosition() >= 0) {
                                    post.setAuthorIconUrl(iconImageUrl);
                                }
                            }
                        }).execute();
                    } else if (!post.getAuthorIconUrl().equals("")) {
                        mGlide.load(post.getAuthorIconUrl())
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((PostCompactViewHolder) holder).iconGifImageView);
                    } else {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((PostCompactViewHolder) holder).iconGifImageView);
                    }

                    ((PostCompactViewHolder) holder).nameTextView.setTextColor(mContext.getResources().getColor(R.color.colorPrimaryDarkDayNightTheme));
                    ((PostCompactViewHolder) holder).nameTextView.setText(authorPrefixed);

                    ((PostCompactViewHolder) holder).nameTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                            mContext.startActivity(intent);
                        }
                    });

                    ((PostCompactViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostCompactViewHolder) holder).nameTextView.performClick());
                }

                ((PostCompactViewHolder) holder).postTimeTextView.setText(postTime);
                ((PostCompactViewHolder) holder).titleTextView.setText(title);
                ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                if (gilded > 0) {
                    ((PostCompactViewHolder) holder).gildedNumberTextView.setVisibility(View.VISIBLE);
                    String gildedNumber = mContext.getResources().getString(R.string.gilded_count, gilded);
                    ((PostCompactViewHolder) holder).gildedNumberTextView.setText(gildedNumber);
                }

                if (post.isLocked()) {
                    ((PostCompactViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (nsfw) {
                    if (!(mContext instanceof FilteredThingActivity)) {
                        ((PostCompactViewHolder) holder).nsfwTextView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                            mContext.startActivity(intent);
                        });
                    }
                    ((PostCompactViewHolder) holder).nsfwTextView.setVisibility(View.VISIBLE);
                }

                if (spoiler) {
                    ((PostCompactViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
                }

                if (flair != null) {
                    ((PostCompactViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
                    ((PostCompactViewHolder) holder).flairTextView.setText(flair);
                }

                switch (voteType) {
                    case 1:
                        //Upvoted
                        ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                        break;
                    case -1:
                        //Downvoted
                        ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                        break;
                }

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                    ((PostCompactViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    ((PostCompactViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    ((PostCompactViewHolder) holder).imageView.setVisibility(View.VISIBLE);
                    loadImage(holder, post);
                }

                if (mPostType == PostDataSource.TYPE_SUBREDDIT && !mDisplaySubredditName && post.isStickied()) {
                    ((PostCompactViewHolder) holder).stickiedPostImageView.setVisibility(View.VISIBLE);
                    mGlide.load(R.drawable.ic_thumbtack_24dp).into(((PostCompactViewHolder) holder).stickiedPostImageView);
                }

                if (isArchived) {
                    ((PostCompactViewHolder) holder).archivedImageView.setVisibility(View.VISIBLE);

                    ((PostCompactViewHolder) holder).upvoteButton
                            .setColorFilter(ContextCompat.getColor(mContext, R.color.voteAndReplyUnavailableVoteButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostCompactViewHolder) holder).downvoteButton
                            .setColorFilter(ContextCompat.getColor(mContext, R.color.voteAndReplyUnavailableVoteButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostCompactViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                if (!(mContext instanceof FilteredThingActivity)) {
                    ((PostCompactViewHolder) holder).typeTextView.setOnClickListener(view -> mCallback.typeChipClicked(post.getPostType()));
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.image);

                        final String imageUrl = post.getUrl();
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewImageActivity.class);
                            intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, imageUrl);
                            intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".jpg");
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.LINK_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.link);

                        ((PostCompactViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCompactViewHolder) holder).linkTextView.setText(domain);

                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.GIF_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.gif);

                        final Uri gifVideoUri = Uri.parse(post.getVideoUrl());
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewGIFActivity.class);
                            intent.setData(gifVideoUri);
                            intent.putExtra(ViewGIFActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".gif");
                            intent.putExtra(ViewGIFActivity.IMAGE_URL_KEY, post.getVideoUrl());
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.video);

                        final Uri videoUri = Uri.parse(post.getVideoUrl());
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, ViewVideoActivity.class);
                            intent.setData(videoUri);
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, subredditName);
                            intent.putExtra(ViewVideoActivity.ID_KEY, fullName);
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostCompactViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostCompactViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostCompactViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((PostCompactViewHolder) holder).noPreviewLinkImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
                        break;
                    case Post.TEXT_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.text);
                        break;
                }

                ((PostCompactViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mContext, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostCompactViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostCompactViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostCompactViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostCompactViewHolder) holder).downvoteButton.clearColorFilter();

                    if (previousUpvoteButtonColorFilter == null) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = RedditUtils.DIR_UPVOTE;
                        ((PostCompactViewHolder) holder).upvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostCompactViewHolder) holder).upvoteButton.clearColorFilter();
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                ((PostCompactViewHolder) holder).upvoteButton
                                        .setColorFilter(ContextCompat.getColor(mContext, R.color.upvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.upvoted));
                            } else {
                                post.setVoteType(0);
                                ((PostCompactViewHolder) holder).upvoteButton.clearColorFilter();
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((PostCompactViewHolder) holder).downvoteButton.clearColorFilter();
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mContext, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + previousVoteType));
                            ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostCompactViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mContext, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostCompactViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostCompactViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostCompactViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostCompactViewHolder) holder).upvoteButton.clearColorFilter();

                    if (previousDownvoteButtonColorFilter == null) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = RedditUtils.DIR_DOWNVOTE;
                        ((PostCompactViewHolder) holder).downvoteButton
                                .setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostCompactViewHolder) holder).downvoteButton.clearColorFilter();
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                    }

                    ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                ((PostCompactViewHolder) holder).downvoteButton
                                        .setColorFilter(ContextCompat.getColor(mContext, R.color.downvoted), android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.downvoted));
                            } else {
                                post.setVoteType(0);
                                ((PostCompactViewHolder) holder).downvoteButton.clearColorFilter();
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
                            }

                            ((PostCompactViewHolder) holder).upvoteButton.clearColorFilter();
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mContext, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Integer.toString(post.getScore() + previousVoteType));
                            ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostCompactViewHolder) holder).commentsCountTextView.setText(Integer.toString(post.getNComments()));

                if (post.isSaved()) {
                    ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                } else {
                    ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                }

                ((PostCompactViewHolder) holder).saveButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mContext, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (post.isSaved()) {
                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                        SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(false);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    } else {
                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                        SaveThing.saveThing(mOauthRetrofit, mAccessToken, post.getFullName(),
                                new SaveThing.SaveThingListener() {
                                    @Override
                                    public void success() {
                                        post.setSaved(true);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mContext, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                });

                ((PostCompactViewHolder) holder).shareButton.setOnClickListener(view -> {
                    try {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        String extraText = title + "\n" + permalink;
                        intent.putExtra(Intent.EXTRA_TEXT, extraText);
                        mContext.startActivity(Intent.createChooser(intent, mContext.getString(R.string.share)));
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mContext, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private void loadImage(final RecyclerView.ViewHolder holder, final Post post) {
        if (holder instanceof PostViewHolder) {
            RequestBuilder imageRequestBuilder = mGlide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostViewHolder) holder).progressBar.setVisibility(View.GONE);
                    ((PostViewHolder) holder).errorRelativeLayout.setVisibility(View.VISIBLE);
                    ((PostViewHolder) holder).errorRelativeLayout.setOnClickListener(view -> {
                        ((PostViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                        ((PostViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                        loadImage(holder, post);
                    });
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
                    ((PostViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });

            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
                        .into(((PostViewHolder) holder).imageView);
            } else {
                imageRequestBuilder.into(((PostViewHolder) holder).imageView);
            }
        } else if (holder instanceof PostCompactViewHolder) {
            RequestBuilder imageRequestBuilder = mGlide.load(post.getPreviewUrl()).error(R.drawable.ic_error_outline_black_24dp);
            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
                        .into(((PostCompactViewHolder) holder).imageView);
            } else {
                imageRequestBuilder.into(((PostCompactViewHolder) holder).imageView);
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
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    public void setVoteButtonsPosition(boolean voteButtonsOnTheRight) {
        mVoteButtonsOnTheRight = voteButtonsOnTheRight;
    }

    public void setPostLayout(int postLayout) {
        mPostLayout = postLayout;
    }

    public void setBlurNSFW(boolean needBlurNSFW) {
        mNeedBlurNSFW = needBlurNSFW;
    }

    public void setBlurSpoiler(boolean needBlurSpoiler) {
        mNeedBlurSpoiler = needBlurSpoiler;
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

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof PostViewHolder) {
            mGlide.clear(((PostViewHolder) holder).imageView);
            mGlide.clear(((PostViewHolder) holder).iconGifImageView);
            ((PostViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).relativeLayout.setVisibility(View.GONE);
            ((PostViewHolder) holder).gildedNumberTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).linkTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((PostViewHolder) holder).imageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
            ((PostViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).contentTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).upvoteButton.clearColorFilter();
            ((PostViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
            ((PostViewHolder) holder).downvoteButton.clearColorFilter();
        } else if (holder instanceof PostCompactViewHolder) {
            mGlide.clear(((PostCompactViewHolder) holder).imageView);
            mGlide.clear(((PostCompactViewHolder) holder).iconGifImageView);
            ((PostCompactViewHolder) holder).stickiedPostImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).relativeLayout.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).gildedNumberTextView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).crosspostImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).archivedImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).lockedImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).nsfwTextView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).flairTextView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).linkTextView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).progressBar.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).imageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).upvoteButton.clearColorFilter();
            ((PostCompactViewHolder) holder).scoreTextView.setTextColor(ContextCompat.getColor(mContext, R.color.defaultTextColor));
            ((PostCompactViewHolder) holder).downvoteButton.clearColorFilter();
        }
    }

    public interface Callback {
        void retryLoadingMore();

        void typeChipClicked(int filter);
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.card_view_item_post)
        MaterialCardView cardView;
        @BindView(R.id.icon_gif_image_view_item_post)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.name_text_view_item_post)
        TextView subredditTextView;
        @BindView(R.id.user_text_view_item_post)
        TextView userTextView;
        @BindView(R.id.stickied_post_image_view_item_post)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_best_item_post)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_best_item_post)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post)
        CustomTextView typeTextView;
        @BindView(R.id.gilded_number_text_view_item_post)
        TextView gildedNumberTextView;
        @BindView(R.id.archived_image_view_item_post)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post)
        CustomTextView flairTextView;
        @BindView(R.id.link_text_view_item_post)
        TextView linkTextView;
        @BindView(R.id.image_view_wrapper_item_post)
        RelativeLayout relativeLayout;
        @BindView(R.id.progress_bar_item_post)
        ProgressBar progressBar;
        @BindView(R.id.image_view_best_post_item)
        AspectRatioGifImageView imageView;
        @BindView(R.id.load_image_error_relative_layout_item_post)
        RelativeLayout errorRelativeLayout;
        @BindView(R.id.image_view_no_preview_link_item_post)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.content_text_view_item_post)
        TextView contentTextView;
        @BindView(R.id.bottom_constraint_layout_item_post)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post)
        ImageView shareButton;

        PostViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            scoreTextView.setOnClickListener(view -> {
                //Do nothing in order to prevent clicking this to start ViewPostDetailActivity
            });

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountTextView.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
            }
        }
    }

    class PostCompactViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.icon_gif_image_view_item_post_compact)
        AspectRatioGifImageView iconGifImageView;
        @BindView(R.id.name_text_view_item_post_compact)
        TextView nameTextView;
        @BindView(R.id.stickied_post_image_view_item_post_compact)
        ImageView stickiedPostImageView;
        @BindView(R.id.post_time_text_view_best_item_post_compact)
        TextView postTimeTextView;
        @BindView(R.id.title_text_view_best_item_post_compact)
        TextView titleTextView;
        @BindView(R.id.type_text_view_item_post_compact)
        CustomTextView typeTextView;
        @BindView(R.id.gilded_number_text_view_item_post_compact)
        TextView gildedNumberTextView;
        @BindView(R.id.archived_image_view_item_post_compact)
        ImageView archivedImageView;
        @BindView(R.id.locked_image_view_item_post_compact)
        ImageView lockedImageView;
        @BindView(R.id.crosspost_image_view_item_post_compact)
        ImageView crosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_compact)
        CustomTextView nsfwTextView;
        @BindView(R.id.spoiler_custom_text_view_item_post_compact)
        CustomTextView spoilerTextView;
        @BindView(R.id.flair_custom_text_view_item_post_compact)
        CustomTextView flairTextView;
        @BindView(R.id.link_text_view_item_post_compact)
        TextView linkTextView;
        @BindView(R.id.image_view_wrapper_item_post_compact)
        RelativeLayout relativeLayout;
        @BindView(R.id.progress_bar_item_post_compact)
        ProgressBar progressBar;
        @BindView(R.id.image_view_best_post_item)
        ImageView imageView;
        @BindView(R.id.image_view_no_preview_link_item_post_compact)
        ImageView noPreviewLinkImageView;
        @BindView(R.id.bottom_constraint_layout_item_post_compact)
        ConstraintLayout bottomConstraintLayout;
        @BindView(R.id.plus_button_item_post_compact)
        ImageView upvoteButton;
        @BindView(R.id.score_text_view_item_post_compact)
        TextView scoreTextView;
        @BindView(R.id.minus_button_item_post_compact)
        ImageView downvoteButton;
        @BindView(R.id.comments_count_item_post_compact)
        TextView commentsCountTextView;
        @BindView(R.id.save_button_item_post_compact)
        ImageView saveButton;
        @BindView(R.id.share_button_item_post_compact)
        ImageView shareButton;

        PostCompactViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            scoreTextView.setOnClickListener(view -> {
                //Do nothing in order to prevent clicking this to start ViewPostDetailActivity
            });

            if (mVoteButtonsOnTheRight) {
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(bottomConstraintLayout);
                constraintSet.clear(upvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(scoreTextView.getId(), ConstraintSet.START);
                constraintSet.clear(downvoteButton.getId(), ConstraintSet.START);
                constraintSet.clear(saveButton.getId(), ConstraintSet.END);
                constraintSet.clear(shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(upvoteButton.getId(), ConstraintSet.END, scoreTextView.getId(), ConstraintSet.START);
                constraintSet.connect(scoreTextView.getId(), ConstraintSet.END, downvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(downvoteButton.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.START, saveButton.getId(), ConstraintSet.END);
                constraintSet.connect(commentsCountTextView.getId(), ConstraintSet.END, upvoteButton.getId(), ConstraintSet.START);
                constraintSet.connect(saveButton.getId(), ConstraintSet.START, shareButton.getId(), ConstraintSet.END);
                constraintSet.connect(shareButton.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
                constraintSet.setHorizontalBias(commentsCountTextView.getId(), 0);
                constraintSet.applyTo(bottomConstraintLayout);
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
            errorTextView.setText(R.string.load_posts_error);
            retryButton.setOnClickListener(view -> mCallback.retryLoadingMore());
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
