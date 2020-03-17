package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.drawable.DrawableCompat;
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
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.CustomView.AspectRatioGifImageView;
import ml.docilealligator.infinityforreddit.Event.PostUpdateEventToDetailActivity;
import ml.docilealligator.infinityforreddit.Fragment.ShareLinkBottomSheetFragment;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.Post.Post;
import ml.docilealligator.infinityforreddit.Post.PostDataSource;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SaveThing;
import ml.docilealligator.infinityforreddit.User.UserDao;
import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import ml.docilealligator.infinityforreddit.Utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
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
    private AppCompatActivity mActivity;
    private Retrofit mOauthRetrofit;
    private Retrofit mRetrofit;
    private String mAccessToken;
    private RequestManager mGlide;
    private RedditDataRoomDatabase mRedditDataRoomDatabase;
    private UserDao mUserDao;
    private boolean canStartActivity = true;
    private int mPostType;
    private int mPostLayout;
    private int mColorPrimaryLightTheme;
    private int mColorAccent;
    private int mCardViewBackgroundColor;
    private int mSecondaryTextColor;
    private int mPostTitleColor;
    private int mPostContentColor;
    private int mStickiedPostIconTint;
    private int mPostTypeBackgroundColor;
    private int mPostTypeTextColor;
    private int mSubredditColor;
    private int mUsernameColor;
    private int mSpoilerBackgroundColor;
    private int mSpoilerTextColor;
    private int mFlairBackgroundColor;
    private int mFlairTextColor;
    private int mNSFWBackgroundColor;
    private int mNSFWTextColor;
    private int mArchivedIconTint;
    private int mLockedIconTint;
    private int mCrosspostIconTint;
    private int mNoPreviewLinkBackgroundColor;
    private int mUpvotedColor;
    private int mDownvotedColor;
    private int mVoteAndReplyUnavailableVoteButtonColor;
    private int mButtonTextColor;
    private int mPostIconAndInfoColor;
    private float mScale;
    private boolean mDisplaySubredditName;
    private boolean mVoteButtonsOnTheRight;
    private boolean mNeedBlurNSFW;
    private boolean mNeedBlurSpoiler;
    private boolean mShowElapsedTime;
    private boolean mShowDividerInCompactLayout;
    private boolean mShowAbsoluteNumberOfVotes;
    private Drawable mCommentIcon;
    private NetworkState networkState;
    private Callback mCallback;
    private ShareLinkBottomSheetFragment mShareLinkBottomSheetFragment;

    public PostRecyclerViewAdapter(AppCompatActivity activity, Retrofit oauthRetrofit, Retrofit retrofit,
                                   RedditDataRoomDatabase redditDataRoomDatabase,
                                   CustomThemeWrapper customThemeWrapper, String accessToken,
                                   int postType, int postLayout, boolean displaySubredditName,
                                   boolean needBlurNSFW, boolean needBlurSpoiler, boolean voteButtonsOnTheRight,
                                   boolean showElapsedTime, boolean showDividerInCompactLayout,
                                   boolean showAbsoluteNumberOfVotes, Callback callback) {
        super(DIFF_CALLBACK);
        if (activity != null) {
            mActivity = activity;
            mOauthRetrofit = oauthRetrofit;
            mRetrofit = retrofit;
            mAccessToken = accessToken;
            mPostType = postType;
            mDisplaySubredditName = displaySubredditName;
            mNeedBlurNSFW = needBlurNSFW;
            mNeedBlurSpoiler = needBlurSpoiler;
            mVoteButtonsOnTheRight = voteButtonsOnTheRight;
            mShowElapsedTime = showElapsedTime;
            mShowDividerInCompactLayout = showDividerInCompactLayout;
            mShowAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
            mPostLayout = postLayout;

            mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
            mColorAccent = customThemeWrapper.getColorAccent();
            mCardViewBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
            mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
            mPostTitleColor = customThemeWrapper.getPostTitleColor();
            mPostContentColor = customThemeWrapper.getPostContentColor();
            mStickiedPostIconTint = customThemeWrapper.getStickiedPostIconTint();
            mPostTypeBackgroundColor = customThemeWrapper.getPostTypeBackgroundColor();
            mPostTypeTextColor = customThemeWrapper.getPostTypeTextColor();
            mSubredditColor = customThemeWrapper.getSubreddit();
            mUsernameColor = customThemeWrapper.getUsername();
            mSpoilerBackgroundColor = customThemeWrapper.getSpoilerBackgroundColor();
            mSpoilerTextColor = customThemeWrapper.getSpoilerTextColor();
            mFlairBackgroundColor = customThemeWrapper.getFlairBackgroundColor();
            mFlairTextColor = customThemeWrapper.getFlairTextColor();
            mNSFWBackgroundColor = customThemeWrapper.getNsfwBackgroundColor();
            mNSFWTextColor = customThemeWrapper.getNsfwTextColor();
            mArchivedIconTint = customThemeWrapper.getArchivedTint();
            mLockedIconTint = customThemeWrapper.getLockedIconTint();
            mCrosspostIconTint = customThemeWrapper.getCrosspostIconTint();
            mNoPreviewLinkBackgroundColor = customThemeWrapper.getNoPreviewLinkBackgroundColor();
            mUpvotedColor = customThemeWrapper.getUpvoted();
            mDownvotedColor = customThemeWrapper.getDownvoted();
            mVoteAndReplyUnavailableVoteButtonColor = customThemeWrapper.getVoteAndReplyUnavailableVoteButtonColor();
            mButtonTextColor = customThemeWrapper.getButtonTextColor();
            mPostIconAndInfoColor = customThemeWrapper.getPostIconAndInfoColor();

            mCommentIcon = activity.getDrawable(R.drawable.ic_comment_grey_24dp);
            if (mCommentIcon != null) {
                DrawableCompat.setTint(mCommentIcon, mPostIconAndInfoColor);
            }
            mScale = activity.getResources().getDisplayMetrics().density;
            mGlide = Glide.with(mActivity.getApplicationContext());
            mRedditDataRoomDatabase = redditDataRoomDatabase;
            mUserDao = redditDataRoomDatabase.userDao();
            mCallback = callback;
            mShareLinkBottomSheetFragment = new ShareLinkBottomSheetFragment();
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
                int voteType = post.getVoteType();
                int gilded = post.getGilded();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                boolean isArchived = post.isArchived();

                ((PostViewHolder) holder).cardView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;

                        Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, position);
                        mActivity.startActivity(intent);
                    }
                });

                ((PostViewHolder) holder).subredditTextView.setText(subredditNamePrefixed);
                ((PostViewHolder) holder).userTextView.setText(authorPrefixed);
                ((PostViewHolder) holder).userTextView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;
                        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                        mActivity.startActivity(intent);
                    }
                });

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mActivity != null && getItemCount() > 0) {
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
                                        if (mActivity != null && getItemCount() > 0) {
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

                    ((PostViewHolder) holder).subredditTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mActivity.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditName());
                                mActivity.startActivity(intent);
                            }
                        }
                    });

                    ((PostViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostViewHolder) holder).subredditTextView.performClick());
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mActivity != null && getItemCount() > 0) {
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

                    ((PostViewHolder) holder).subredditTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                                mActivity.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditName());
                                mActivity.startActivity(intent);
                            }
                        }
                    });

                    ((PostViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostViewHolder) holder).userTextView.performClick());
                }

                if (mShowElapsedTime) {
                    ((PostViewHolder) holder).postTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
                } else {
                    ((PostViewHolder) holder).postTimeTextView.setText(postTime);
                }

                ((PostViewHolder) holder).titleTextView.setText(title);
                ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                if (gilded > 0) {
                    ((PostViewHolder) holder).gildedNumberTextView.setVisibility(View.VISIBLE);
                    String gildedNumber = mActivity.getResources().getString(R.string.gilded_count, gilded);
                    ((PostViewHolder) holder).gildedNumberTextView.setText(gildedNumber);
                }

                if (post.isLocked()) {
                    ((PostViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (nsfw) {
                    if (!(mActivity instanceof FilteredThingActivity)) {
                        ((PostViewHolder) holder).nsfwTextView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                            mActivity.startActivity(intent);
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
                        ((PostViewHolder) holder).upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case -1:
                        //Downvoted
                        ((PostViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
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
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostViewHolder) holder).downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                if (!(mActivity instanceof FilteredThingActivity)) {
                    ((PostViewHolder) holder).typeTextView.setOnClickListener(view -> mCallback.typeChipClicked(post.getPostType()));
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.image);

                        final String imageUrl = post.getUrl();
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewImageActivity.class);
                            intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, imageUrl);
                            intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".jpg");
                            mActivity.startActivity(intent);
                        });

                        if (post.getPreviewWidth() <= 0 || post.getPreviewHeight() <= 0) {
                            ((PostViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            ((PostViewHolder) holder).imageView.getLayoutParams().height = (int) (400 * mScale);
                        }
                        break;
                    case Post.LINK_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.link);

                        ((PostViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostViewHolder) holder).linkTextView.setText(domain);

                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mActivity.startActivity(intent);
                        });
                        break;
                    case Post.GIF_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.gif);

                        final Uri gifVideoUri = Uri.parse(post.getVideoUrl());
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewGIFActivity.class);
                            intent.setData(gifVideoUri);
                            intent.putExtra(ViewGIFActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".gif");
                            intent.putExtra(ViewGIFActivity.IMAGE_URL_KEY, post.getVideoUrl());
                            mActivity.startActivity(intent);
                        });

                        ((PostViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.video);

                        final Uri videoUri = Uri.parse(post.getVideoUrl());
                        ((PostViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                            intent.setData(videoUri);
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, subredditName);
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, fullName);
                            mActivity.startActivity(intent);
                        });

                        ((PostViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((PostViewHolder) holder).noPreviewLinkImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mActivity.startActivity(intent);
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
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != 1) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = RedditUtils.DIR_UPVOTE;
                        ((PostViewHolder) holder).upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                ((PostViewHolder) holder).upvoteButton
                                        .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                            } else {
                                post.setVoteType(0);
                                ((PostViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            ((PostViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            ((PostViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != -1) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = RedditUtils.DIR_DOWNVOTE;
                        ((PostViewHolder) holder).downvoteButton
                                .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                ((PostViewHolder) holder).downvoteButton
                                        .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                            } else {
                                post.setVoteType(0);
                                ((PostViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            ((PostViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
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
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        ((PostViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                });

                ((PostViewHolder) holder).shareButton.setOnClickListener(view -> shareLink(post));
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
                int voteType = post.getVoteType();
                int gilded = post.getGilded();
                boolean nsfw = post.isNSFW();
                boolean spoiler = post.isSpoiler();
                String flair = post.getFlair();
                boolean isArchived = post.isArchived();

                ((PostCompactViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (canStartActivity) {
                        canStartActivity = false;

                        Intent intent = new Intent(mActivity, ViewPostDetailActivity.class);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                        intent.putExtra(ViewPostDetailActivity.EXTRA_POST_LIST_POSITION, position);
                        mActivity.startActivity(intent);
                    }
                });

                if (mDisplaySubredditName) {
                    if (authorPrefixed.equals(subredditNamePrefixed)) {
                        if (post.getAuthorIconUrl() == null) {
                            new LoadUserDataAsyncTask(mUserDao, post.getAuthor(), mRetrofit, iconImageUrl -> {
                                if (mActivity != null && getItemCount() > 0) {
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
                                        if (mActivity != null && getItemCount() > 0) {
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

                    ((PostCompactViewHolder) holder).nameTextView.setTextColor(mSubredditColor);
                    ((PostCompactViewHolder) holder).nameTextView.setText(subredditNamePrefixed);

                    ((PostCompactViewHolder) holder).nameTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            if (post.getSubredditNamePrefixed().startsWith("u/")) {
                                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mActivity.startActivity(intent);
                            } else {
                                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                                        post.getSubredditNamePrefixed().substring(2));
                                mActivity.startActivity(intent);
                            }
                        }
                    });

                    ((PostCompactViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostCompactViewHolder) holder).nameTextView.performClick());
                } else {
                    if (post.getAuthorIconUrl() == null) {
                        String authorName = post.getAuthor().equals("[deleted]") ? post.getSubredditNamePrefixed().substring(2) : post.getAuthor();
                        new LoadUserDataAsyncTask(mUserDao, authorName, mRetrofit, iconImageUrl -> {
                            if (mActivity != null && getItemCount() > 0) {
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

                    ((PostCompactViewHolder) holder).nameTextView.setTextColor(mUsernameColor);
                    ((PostCompactViewHolder) holder).nameTextView.setText(authorPrefixed);

                    ((PostCompactViewHolder) holder).nameTextView.setOnClickListener(view -> {
                        if (canStartActivity) {
                            canStartActivity = false;
                            Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, post.getAuthor());
                            mActivity.startActivity(intent);
                        }
                    });

                    ((PostCompactViewHolder) holder).iconGifImageView.setOnClickListener(view ->
                            ((PostCompactViewHolder) holder).nameTextView.performClick());
                }

                if (mShowElapsedTime) {
                    ((PostCompactViewHolder) holder).postTimeTextView.setText(
                            Utils.getElapsedTime(mActivity, post.getPostTimeMillis()));
                } else {
                    ((PostCompactViewHolder) holder).postTimeTextView.setText(postTime);
                }

                if (mShowDividerInCompactLayout) {
                    ((PostCompactViewHolder) holder).divider.setVisibility(View.VISIBLE);
                } else {
                    ((PostCompactViewHolder) holder).divider.setVisibility(View.GONE);
                }

                ((PostCompactViewHolder) holder).titleTextView.setText(title);
                ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                if (gilded > 0) {
                    ((PostCompactViewHolder) holder).gildedNumberTextView.setVisibility(View.VISIBLE);
                    String gildedNumber = mActivity.getResources().getString(R.string.gilded_count, gilded);
                    ((PostCompactViewHolder) holder).gildedNumberTextView.setText(gildedNumber);
                }

                if (post.isLocked()) {
                    ((PostCompactViewHolder) holder).lockedImageView.setVisibility(View.VISIBLE);
                }

                if (nsfw) {
                    if (!(mActivity instanceof FilteredThingActivity)) {
                        ((PostCompactViewHolder) holder).nsfwTextView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, FilteredThingActivity.class);
                            intent.putExtra(FilteredThingActivity.EXTRA_NAME, post.getSubredditNamePrefixed().substring(2));
                            intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
                            intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
                            mActivity.startActivity(intent);
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
                        ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                        break;
                    case -1:
                        //Downvoted
                        ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                        break;
                }

                if (post.getPostType() != Post.TEXT_TYPE && post.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                    ((PostCompactViewHolder) holder).relativeLayout.setVisibility(View.VISIBLE);
                    if (post.getPostType() != Post.GIF_TYPE && post.getPostType() != Post.VIDEO_TYPE) {
                        ((PostCompactViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
                    }
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
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                    ((PostCompactViewHolder) holder).downvoteButton
                            .setColorFilter(mVoteAndReplyUnavailableVoteButtonColor, android.graphics.PorterDuff.Mode.SRC_IN);
                }

                if (post.isCrosspost()) {
                    ((PostCompactViewHolder) holder).crosspostImageView.setVisibility(View.VISIBLE);
                }

                if (!(mActivity instanceof FilteredThingActivity)) {
                    ((PostCompactViewHolder) holder).typeTextView.setOnClickListener(view -> mCallback.typeChipClicked(post.getPostType()));
                }

                switch (post.getPostType()) {
                    case Post.IMAGE_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.image);

                        final String imageUrl = post.getUrl();
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewImageActivity.class);
                            intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, imageUrl);
                            intent.putExtra(ViewImageActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".jpg");
                            mActivity.startActivity(intent);
                        });
                        break;
                    case Post.LINK_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.link);

                        ((PostCompactViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String domain = Uri.parse(post.getUrl()).getHost();
                        ((PostCompactViewHolder) holder).linkTextView.setText(domain);

                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mActivity.startActivity(intent);
                        });
                        break;
                    case Post.GIF_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.gif);

                        final Uri gifVideoUri = Uri.parse(post.getVideoUrl());
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewGIFActivity.class);
                            intent.setData(gifVideoUri);
                            intent.putExtra(ViewGIFActivity.FILE_NAME_KEY, subredditName
                                    + "-" + id + ".gif");
                            intent.putExtra(ViewGIFActivity.IMAGE_URL_KEY, post.getVideoUrl());
                            mActivity.startActivity(intent);
                        });

                        ((PostCompactViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.VIDEO_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.video);

                        final Uri videoUri = Uri.parse(post.getVideoUrl());
                        ((PostCompactViewHolder) holder).imageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                            intent.setData(videoUri);
                            intent.putExtra(ViewVideoActivity.EXTRA_VIDEO_DOWNLOAD_URL, post.getVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.EXTRA_SUBREDDIT, subredditName);
                            intent.putExtra(ViewVideoActivity.EXTRA_ID, fullName);
                            mActivity.startActivity(intent);
                        });

                        ((PostCompactViewHolder) holder).playButtonImageView.setVisibility(View.VISIBLE);
                        break;
                    case Post.NO_PREVIEW_LINK_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.link);

                        String noPreviewLinkUrl = post.getUrl();
                        ((PostCompactViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                        String noPreviewLinkDomain = Uri.parse(noPreviewLinkUrl).getHost();
                        ((PostCompactViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);
                        ((PostCompactViewHolder) holder).noPreviewLinkImageView.setVisibility(View.VISIBLE);
                        ((PostCompactViewHolder) holder).noPreviewLinkImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(post.getUrl());
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(post.getUrl()));
                            } else {
                                intent.setData(uri);
                            }
                            mActivity.startActivity(intent);
                        });
                        break;
                    case Post.TEXT_TYPE:
                        ((PostCompactViewHolder) holder).typeTextView.setText(R.string.text);
                        break;
                }

                ((PostCompactViewHolder) holder).upvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostCompactViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostCompactViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostCompactViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != 1) {
                        //Not upvoted before
                        post.setVoteType(1);
                        newVoteType = RedditUtils.DIR_UPVOTE;
                        ((PostCompactViewHolder) holder).upvoteButton
                                .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                    } else {
                        //Upvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                                post.setVoteType(1);
                                ((PostCompactViewHolder) holder).upvoteButton
                                        .setColorFilter(mUpvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mUpvotedColor);
                            } else {
                                post.setVoteType(0);
                                ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
                            ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(previousDownvoteButtonColorFilter);
                            ((PostCompactViewHolder) holder).scoreTextView.setTextColor(previousScoreTextViewColor);

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }
                    }, fullName, newVoteType, holder.getAdapterPosition());
                });

                ((PostCompactViewHolder) holder).downvoteButton.setOnClickListener(view -> {
                    if (mAccessToken == null) {
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (isArchived) {
                        Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    ColorFilter previousUpvoteButtonColorFilter = ((PostCompactViewHolder) holder).upvoteButton.getColorFilter();
                    ColorFilter previousDownvoteButtonColorFilter = ((PostCompactViewHolder) holder).downvoteButton.getColorFilter();
                    int previousScoreTextViewColor = ((PostCompactViewHolder) holder).scoreTextView.getCurrentTextColor();

                    int previousVoteType = post.getVoteType();
                    String newVoteType;

                    ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);

                    if (previousVoteType != -1) {
                        //Not downvoted before
                        post.setVoteType(-1);
                        newVoteType = RedditUtils.DIR_DOWNVOTE;
                        ((PostCompactViewHolder) holder).downvoteButton
                                .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                    } else {
                        //Downvoted before
                        post.setVoteType(0);
                        newVoteType = RedditUtils.DIR_UNVOTE;
                        ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                        ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                    }

                    ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                    VoteThing.voteThing(mActivity, mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
                        @Override
                        public void onVoteThingSuccess(int position1) {
                            if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                                post.setVoteType(-1);
                                ((PostCompactViewHolder) holder).downvoteButton
                                        .setColorFilter(mDownvotedColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mDownvotedColor);
                            } else {
                                post.setVoteType(0);
                                ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                                ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
                            }

                            ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + post.getVoteType()));

                            EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                        }

                        @Override
                        public void onVoteThingFail(int position1) {
                            Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                            post.setVoteType(previousVoteType);
                            ((PostCompactViewHolder) holder).scoreTextView.setText(Utils.getNVotes(mShowAbsoluteNumberOfVotes, post.getScore() + previousVoteType));
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
                        Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mActivity, R.string.post_unsaved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(true);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_unsaved_failed, Toast.LENGTH_SHORT).show();
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
                                        Toast.makeText(mActivity, R.string.post_saved_success, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }

                                    @Override
                                    public void failed() {
                                        post.setSaved(false);
                                        ((PostCompactViewHolder) holder).saveButton.setImageResource(R.drawable.ic_bookmark_border_grey_24dp);
                                        Toast.makeText(mActivity, R.string.post_saved_failed, Toast.LENGTH_SHORT).show();
                                        EventBus.getDefault().post(new PostUpdateEventToDetailActivity(post));
                                    }
                                });
                    }
                });

                ((PostCompactViewHolder) holder).shareButton.setOnClickListener(view -> shareLink(post));
            }
        }
    }

    private void loadImage(final RecyclerView.ViewHolder holder, final Post post) {
        if (holder instanceof PostViewHolder) {
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(post.getPreviewUrl()).listener(new RequestListener<Drawable>() {
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
                imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                        .into(((PostViewHolder) holder).imageView);
            } else {
                imageRequestBuilder.into(((PostViewHolder) holder).imageView);
            }
        } else if (holder instanceof PostCompactViewHolder) {
            String previewUrl = post.getThumbnailPreviewUrl().equals("") ? post.getPreviewUrl() : post.getThumbnailPreviewUrl();
            RequestBuilder<Drawable> imageRequestBuilder = mGlide.load(previewUrl)
                    .error(R.drawable.ic_error_outline_black_24dp).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    ((PostCompactViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    ((PostCompactViewHolder) holder).progressBar.setVisibility(View.GONE);
                    return false;
                }
            });
            if ((post.isNSFW() && mNeedBlurNSFW) || post.isSpoiler() && mNeedBlurSpoiler) {
                imageRequestBuilder
                        .transform(new BlurTransformation(50, 2))
                        .into(((PostCompactViewHolder) holder).imageView);
            } else {
                imageRequestBuilder.into(((PostCompactViewHolder) holder).imageView);
            }
        }
    }

    private void shareLink(Post post) {
        Bundle bundle = new Bundle();
        bundle.putString(ShareLinkBottomSheetFragment.EXTRA_POST_LINK, post.getPermalink());
        if (post.getPostType() != Post.TEXT_TYPE) {
            bundle.putInt(ShareLinkBottomSheetFragment.EXTRA_MEDIA_TYPE, post.getPostType());
            switch (post.getPostType()) {
                case Post.IMAGE_TYPE:
                case Post.GIF_TYPE:
                case Post.LINK_TYPE:
                case Post.NO_PREVIEW_LINK_TYPE:
                    bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, post.getUrl());
                    break;
                case Post.VIDEO_TYPE:
                    bundle.putString(ShareLinkBottomSheetFragment.EXTRA_MEDIA_LINK, post.getVideoDownloadUrl());
                    break;
            }
        }
        mShareLinkBottomSheetFragment.setArguments(bundle);
        mShareLinkBottomSheetFragment.show(mActivity.getSupportFragmentManager(), mShareLinkBottomSheetFragment.getTag());
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

    public void setShowElapsedTime(boolean showElapsedTime) {
        mShowElapsedTime = showElapsedTime;
    }

    public void setShowDividerInCompactLayout(boolean showDividerInCompactLayout) {
        mShowDividerInCompactLayout = showDividerInCompactLayout;
    }

    public void setShowAbsoluteNumberOfVotes(boolean showAbsoluteNumberOfVotes) {
        mShowAbsoluteNumberOfVotes = showAbsoluteNumberOfVotes;
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
                notifyItemRemoved(getItemCount() - 1);
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void removeFooter() {
        if (hasExtraRow()) {
            notifyItemRemoved(getItemCount() -  1);
        }

        networkState = null;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
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
            ((PostViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.FIT_START);
            ((PostViewHolder) holder).imageView.getLayoutParams().height = FrameLayout.LayoutParams.WRAP_CONTENT;
            ((PostViewHolder) holder).imageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).playButtonImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).errorRelativeLayout.setVisibility(View.GONE);
            ((PostViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((PostViewHolder) holder).contentTextView.setVisibility(View.GONE);
            ((PostViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
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
            ((PostCompactViewHolder) holder).playButtonImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).noPreviewLinkImageView.setVisibility(View.GONE);
            ((PostCompactViewHolder) holder).upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            ((PostCompactViewHolder) holder).scoreTextView.setTextColor(mPostIconAndInfoColor);
            ((PostCompactViewHolder) holder).downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
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
        @BindView(R.id.subreddit_name_text_view_item_post)
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
        @BindView(R.id.play_button_image_view_item_post)
        ImageView playButtonImageView;
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

            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            subredditTextView.setTextColor(mSubredditColor);
            userTextView.setTextColor(mUsernameColor);
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            contentTextView.setTextColor(mPostContentColor);
            stickiedPostImageView.setColorFilter(mStickiedPostIconTint, PorterDuff.Mode.SRC_IN);
            typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            typeTextView.setBorderColor(mPostTypeBackgroundColor);
            typeTextView.setTextColor(mPostTypeTextColor);
            spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            spoilerTextView.setTextColor(mSpoilerTextColor);
            nsfwTextView.setBackgroundColor(mNSFWBackgroundColor);
            nsfwTextView.setBorderColor(mNSFWBackgroundColor);
            nsfwTextView.setTextColor(mNSFWTextColor);
            flairTextView.setBackgroundColor(mFlairBackgroundColor);
            flairTextView.setBorderColor(mFlairBackgroundColor);
            flairTextView.setTextColor(mFlairTextColor);
            archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            linkTextView.setTextColor(mSecondaryTextColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);
            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentsCountTextView.setTextColor(mPostIconAndInfoColor);
            commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(mCommentIcon, null, null, null);
            saveButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            shareButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
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
        @BindView(R.id.play_button_image_view_item_post_compact)
        ImageView playButtonImageView;
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
        @BindView(R.id.divider_item_post_compact)
        View divider;

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

            itemView.setBackgroundTintList(ColorStateList.valueOf(mCardViewBackgroundColor));
            postTimeTextView.setTextColor(mSecondaryTextColor);
            titleTextView.setTextColor(mPostTitleColor);
            stickiedPostImageView.setColorFilter(mStickiedPostIconTint, PorterDuff.Mode.SRC_IN);
            typeTextView.setBackgroundColor(mPostTypeBackgroundColor);
            typeTextView.setBorderColor(mPostTypeBackgroundColor);
            typeTextView.setTextColor(mPostTypeTextColor);
            spoilerTextView.setBackgroundColor(mSpoilerBackgroundColor);
            spoilerTextView.setBorderColor(mSpoilerBackgroundColor);
            spoilerTextView.setTextColor(mSpoilerTextColor);
            nsfwTextView.setBackgroundColor(mNSFWBackgroundColor);
            nsfwTextView.setBorderColor(mNSFWBackgroundColor);
            nsfwTextView.setTextColor(mNSFWTextColor);
            flairTextView.setBackgroundColor(mFlairBackgroundColor);
            flairTextView.setBorderColor(mFlairBackgroundColor);
            flairTextView.setTextColor(mFlairTextColor);
            archivedImageView.setColorFilter(mArchivedIconTint, PorterDuff.Mode.SRC_IN);
            lockedImageView.setColorFilter(mLockedIconTint, PorterDuff.Mode.SRC_IN);
            crosspostImageView.setColorFilter(mCrosspostIconTint, PorterDuff.Mode.SRC_IN);
            linkTextView.setTextColor(mSecondaryTextColor);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            noPreviewLinkImageView.setBackgroundColor(mNoPreviewLinkBackgroundColor);
            upvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            scoreTextView.setTextColor(mPostIconAndInfoColor);
            downvoteButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            commentsCountTextView.setTextColor(mPostIconAndInfoColor);
            commentsCountTextView.setCompoundDrawablesWithIntrinsicBounds(mCommentIcon, null, null, null);
            saveButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
            shareButton.setColorFilter(mPostIconAndInfoColor, android.graphics.PorterDuff.Mode.SRC_IN);
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
            errorTextView.setText(R.string.load_more_posts_error);
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setOnClickListener(view -> mCallback.retryLoadingMore());
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
            itemView.setOnClickListener(view -> retryButton.performClick());
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
