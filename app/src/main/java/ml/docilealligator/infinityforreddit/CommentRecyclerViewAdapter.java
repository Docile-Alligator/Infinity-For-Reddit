package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.santalu.aspectratioimageview.AspectRatioImageView;

import java.util.ArrayList;
import java.util.Locale;

import CustomView.AspectRatioGifImageView;
import SubredditDatabase.SubredditRoomDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

class CommentRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_POST_DETAIL = 0;
    private static final int VIEW_TYPE_FIRST_LOADING = 1;
    private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 2;
    private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 3;
    private static final int VIEW_TYPE_COMMENT = 4;
    private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 5;
    private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 6;
    private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 7;

    private Activity mActivity;
    private Retrofit mRetrofit;
    private Retrofit mOauthRetrofit;
    private RequestManager mGlide;
    private SharedPreferences mSharedPreferences;
    private Post mPost;
    private ArrayList<CommentData> mVisibleComments;
    private String mSubredditNamePrefixed;
    private Locale mLocale;
    private CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;
    private boolean isInitiallyLoading;
    private boolean isInitiallyLoadingFailed;
    private boolean mHasMoreComments;
    private boolean loadMoreCommentsFailed;

    interface CommentRecyclerViewAdapterCallback {
        void updatePost(Post post);
        void retryFetchingMoreComments();
    }

    CommentRecyclerViewAdapter(Activity activity, Retrofit retrofit, Retrofit oauthRetrofit, RequestManager glide,
                               SharedPreferences sharedPreferences, Post post, String subredditNamePrefixed,
                               Locale locale, LoadSubredditIconAsyncTask loadSubredditIconAsyncTask,
                               CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
        mActivity = activity;
        mRetrofit = retrofit;
        mOauthRetrofit = oauthRetrofit;
        mGlide = glide;
        mSharedPreferences = sharedPreferences;
        mPost = post;
        mVisibleComments = new ArrayList<>();
        mSubredditNamePrefixed = subredditNamePrefixed;
        mLocale = locale;
        mLoadSubredditIconAsyncTask = loadSubredditIconAsyncTask;
        mCommentRecyclerViewAdapterCallback = commentRecyclerViewAdapterCallback;
        isInitiallyLoading = true;
        isInitiallyLoadingFailed = false;
        mHasMoreComments = false;
        loadMoreCommentsFailed = false;
    }

    @Override
    public int getItemViewType(int position) {
        if(position == 0) {
            return VIEW_TYPE_POST_DETAIL;
        }

        if(mVisibleComments.size() == 0) {
            if(position == 1) {
                if(isInitiallyLoading) {
                    return VIEW_TYPE_FIRST_LOADING;
                } else if(isInitiallyLoadingFailed) {
                    return VIEW_TYPE_FIRST_LOADING_FAILED;
                } else {
                    return VIEW_TYPE_NO_COMMENT_PLACEHOLDER;
                }
            }
        }

        if(position == mVisibleComments.size() + 1) {
            if(mHasMoreComments) {
                return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
            } else {
                return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
            }
        }

        CommentData comment = mVisibleComments.get(position - 1);
        if(!comment.isPlaceHolder()) {
            return VIEW_TYPE_COMMENT;
        } else {
            return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_POST_DETAIL:
                return new PostDetailViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_detail, parent, false));
            case VIEW_TYPE_FIRST_LOADING:
                return new LoadCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments, parent, false));
            case VIEW_TYPE_FIRST_LOADING_FAILED:
                return new LoadCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_comments_failed_placeholder, parent, false));
            case VIEW_TYPE_NO_COMMENT_PLACEHOLDER:
                return new NoCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_no_comment_placeholder, parent, false));
            case VIEW_TYPE_COMMENT:
                return new CommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
            case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
                return new LoadMoreChildCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_load_more_comments_placeholder, parent, false));
            case VIEW_TYPE_IS_LOADING_MORE_COMMENTS:
                return new IsLoadingMoreCommentsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_loading, parent, false));
            default:
                return new LoadMoreCommentsFailedViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment_footer_error, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if(holder.getItemViewType() == VIEW_TYPE_POST_DETAIL) {
            ((PostDetailViewHolder) holder).mTitleTextView.setText(mPost.getTitle());

            if(mPost.getSubredditIconUrl() == null) {
                if(mLoadSubredditIconAsyncTask != null) {
                    mLoadSubredditIconAsyncTask.cancel(true);
                }
                mLoadSubredditIconAsyncTask = new LoadSubredditIconAsyncTask(
                        SubredditRoomDatabase.getDatabase(mActivity).subredditDao(), mPost.getSubredditNamePrefixed().substring(2),
                        iconImageUrl -> {
                            if(!iconImageUrl.equals("")) {
                                mGlide.load(iconImageUrl)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                        .into(((PostDetailViewHolder) holder).mSubredditIconGifImageView);
                            } else {
                                mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                        .into(((PostDetailViewHolder) holder).mSubredditIconGifImageView);
                            }

                            mPost.setSubredditIconUrl(iconImageUrl);
                        });

                mLoadSubredditIconAsyncTask.execute();
            } else if(!mPost.getSubredditIconUrl().equals("")) {
                mGlide.load(mPost.getSubredditIconUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((PostDetailViewHolder) holder).mSubredditIconGifImageView);
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((PostDetailViewHolder) holder).mSubredditIconGifImageView);
            }

            switch (mPost.getVoteType()) {
                case 1:
                    //Upvote
                    ((PostDetailViewHolder) holder).mUpvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), PorterDuff.Mode.SRC_IN);
                    break;
                case -1:
                    //Downvote
                    ((PostDetailViewHolder) holder).mDownvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.minusButtonColor), PorterDuff.Mode.SRC_IN);
                    break;
                case 0:
                    ((PostDetailViewHolder) holder).mUpvoteButton.clearColorFilter();
                    ((PostDetailViewHolder) holder).mDownvoteButton.clearColorFilter();
            }

            if(mPost.getPostType() != Post.TEXT_TYPE && mPost.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
                ((PostDetailViewHolder) holder).mRelativeLayout.setVisibility(View.VISIBLE);
                ((PostDetailViewHolder) holder).mImageView.setVisibility(View.VISIBLE);
                ((PostDetailViewHolder) holder).mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
                loadImage((PostDetailViewHolder) holder);
            } else {
                ((PostDetailViewHolder) holder).mRelativeLayout.setVisibility(View.GONE);
                ((PostDetailViewHolder) holder).mImageView.setVisibility(View.GONE);
            }

            if(mPost.isCrosspost()) {
                ((PostDetailViewHolder) holder).mCrosspostImageView.setVisibility(View.VISIBLE);
            }

            ((PostDetailViewHolder) holder).mSubredditTextView.setText(mPost.getSubredditNamePrefixed());

            ((PostDetailViewHolder) holder).mPostTimeTextView.setText(mPost.getPostTime());

            if(mPost.getGilded() > 0) {
                ((PostDetailViewHolder) holder).mGildedImageView.setVisibility(View.VISIBLE);
                mGlide.load(R.drawable.gold).into(((PostDetailViewHolder) holder).mGildedImageView);
                ((PostDetailViewHolder) holder).mGildedNumberTextView.setVisibility(View.VISIBLE);
                String gildedNumber = mActivity.getResources().getString(R.string.gilded, mPost.getGilded());
                ((PostDetailViewHolder) holder).mGildedNumberTextView.setText(gildedNumber);
            }

            if(mPost.isNSFW()) {
                ((PostDetailViewHolder) holder).mNSFWChip.setVisibility(View.VISIBLE);
            } else {
                ((PostDetailViewHolder) holder).mNSFWChip.setVisibility(View.GONE);
            }

            String scoreWithVote = Integer.toString(mPost.getScore() + mPost.getVoteType());
            ((PostDetailViewHolder) holder).mScoreTextView.setText(scoreWithVote);

            switch (mPost.getPostType()) {
                case Post.IMAGE_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("IMAGE");

                    ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(mActivity, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, mPost.getUrl());
                        intent.putExtra(ViewImageActivity.TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, mPost.getSubredditNamePrefixed().substring(2)
                                + "-" + mPost.getId().substring(3));
                        mActivity.startActivity(intent);
                    });
                    break;
                case Post.LINK_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("LINK");

                    ((PostDetailViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                    String domain = Uri.parse(mPost.getUrl()).getHost();
                    ((PostDetailViewHolder) holder).linkTextView.setText(domain);

                    ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        // add share action to menu list
                        builder.addDefaultShareMenuItem();
                        builder.setToolbarColor(mActivity.getResources().getColor(R.color.colorPrimary));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mActivity, Uri.parse(mPost.getUrl()));
                    });
                    break;
                case Post.GIF_VIDEO_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("GIF");

                    final Uri gifVideoUri = Uri.parse(mPost.getVideoUrl());
                    ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        intent.setData(gifVideoUri);
                        intent.putExtra(ViewVideoActivity.TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPost.isDashVideo());
                        intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
                        if(mPost.isDownloadableGifOrVideo()) {
                            intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditNamePrefixed());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPost.getId());
                        }
                        mActivity.startActivity(intent);
                    });
                    break;
                case Post.VIDEO_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("VIDEO");

                    final Uri videoUri = Uri.parse(mPost.getVideoUrl());
                    ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
                        Intent intent = new Intent(mActivity, ViewVideoActivity.class);
                        intent.setData(videoUri);
                        intent.putExtra(ViewVideoActivity.TITLE_KEY, mPost.getTitle());
                        intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPost.isDashVideo());
                        intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
                        if(mPost.isDownloadableGifOrVideo()) {
                            intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditNamePrefixed());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPost.getId());
                        }
                        mActivity.startActivity(intent);
                    });
                    break;
                case Post.NO_PREVIEW_LINK_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("LINK");

                    ((PostDetailViewHolder) holder).linkTextView.setVisibility(View.VISIBLE);
                    String noPreviewLinkDomain = Uri.parse(mPost.getUrl()).getHost();
                    ((PostDetailViewHolder) holder).linkTextView.setText(noPreviewLinkDomain);

                    if(!mPost.getSelfText().equals("")) {
                        ((PostDetailViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
                        ((PostDetailViewHolder) holder).mContentMarkdownView.setMarkdown(getCustomSpannableConfiguration(), mPost.getSelfText());
                    }
                    ((PostDetailViewHolder) holder).mNoPreviewLinkImageView.setVisibility(View.VISIBLE);
                    ((PostDetailViewHolder) holder).mNoPreviewLinkImageView.setOnClickListener(view -> {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        // add share action to menu list
                        builder.addDefaultShareMenuItem();
                        builder.setToolbarColor(mActivity.getResources().getColor(R.color.colorPrimary));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(mActivity, Uri.parse(mPost.getUrl()));
                    });
                    break;
                case Post.TEXT_TYPE:
                    ((PostDetailViewHolder) holder).mTypeChip.setText("TEXT");

                    if(!mPost.getSelfText().equals("")) {
                        ((PostDetailViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
                        ((PostDetailViewHolder) holder).mContentMarkdownView.setMarkdown(getCustomSpannableConfiguration(), mPost.getSelfText());
                    }
                    break;
            }
        } else if(holder.getItemViewType() == VIEW_TYPE_COMMENT) {
            CommentData comment = mVisibleComments.get(holder.getAdapterPosition() - 1);

            String authorPrefixed = "u/" + comment.getAuthor();
            ((CommentViewHolder) holder).authorTextView.setText(authorPrefixed);

            ((CommentViewHolder) holder).commentTimeTextView.setText(comment.getCommentTime());

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

            ((CommentViewHolder) holder).commentMarkdownView.setMarkdown(spannableConfiguration, comment.getCommentContent());
            ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(comment.getScore()));

            ViewGroup.LayoutParams params = ((CommentViewHolder) holder).verticalBlock.getLayoutParams();
            params.width = comment.getDepth() * 16;
            ((CommentViewHolder) holder).verticalBlock.setLayoutParams(params);

            if (comment.hasReply()) {
                if(comment.isExpanded()) {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_less_black_20dp);
                } else {
                    ((CommentViewHolder) holder).expandButton.setImageResource(R.drawable.ic_expand_more_black_20dp);
                }
                ((CommentViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
            }

            switch (comment.getVoteType()) {
                case 1:
                    ((CommentViewHolder) holder).upvoteButton
                            .setColorFilter(ContextCompat.getColor(mActivity, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    break;
                case 2:
                    ((CommentViewHolder) holder).downvoteButton
                            .setColorFilter(ContextCompat.getColor(mActivity, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    break;
            }
        } else if(holder instanceof LoadMoreChildCommentsViewHolder) {
            CommentData placeholder;
            placeholder = mVisibleComments.get(holder.getAdapterPosition() - 1);

            ViewGroup.LayoutParams params = ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.getLayoutParams();
            params.width = placeholder.getDepth() * 16;
            ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.setLayoutParams(params);

            if(placeholder.isLoadingMoreChildren()) {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);
            } else if(placeholder.isLoadMoreChildrenFailed()) {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments_failed);
            } else {
                ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.comment_load_more_comments);
            }
        } else if(holder instanceof LoadMoreCommentsFailedViewHolder) {
            ((LoadMoreCommentsFailedViewHolder) holder).errorTextView.setText(R.string.post_load_comments_failed);
        }
    }

    private void loadImage(PostDetailViewHolder holder) {
        RequestBuilder imageRequestBuilder = mGlide.load(mPost.getPreviewUrl())
                .apply(new RequestOptions().override(mPost.getPreviewWidth(), mPost.getPreviewHeight()))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        holder.mLoadImageProgressBar.setVisibility(View.GONE);
                        holder.mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                        holder.mLoadImageErrorTextView.setOnClickListener(view -> {
                            holder.mLoadImageProgressBar.setVisibility(View.VISIBLE);
                            holder.mLoadImageErrorTextView.setVisibility(View.GONE);
                            loadImage(holder);
                        });
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        holder.mLoadWrapper.setVisibility(View.GONE);
                        return false;
                    }
                });

        if(mPost.isNSFW()) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
                    .into(holder.mImageView);
        } else {
            imageRequestBuilder.into(holder.mImageView);
        }
    }

    private SpannableConfiguration getCustomSpannableConfiguration() {
        return SpannableConfiguration.builder(mActivity).linkResolver((view, link) -> {
            if(link.startsWith("/u/") || link.startsWith("u/")) {
                Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                mActivity.startActivity(intent);
            } else if(link.startsWith("/r/") || link.startsWith("r/")) {
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
    }

    void updatePost(Post post) {
        mPost = post;
        notifyItemChanged(0);
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
                for(int i = 0; i < children.size(); i++) {
                    children.get(i).setExpanded(false);
                }
                mVisibleComments.addAll(position + 1, children);
                notifyItemRangeInserted(position + 2, children.size());
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
        notifyItemRangeRemoved(position + 2, allChildrenSize);
    }

    void addComments(ArrayList<CommentData> comments, boolean hasMoreComments) {
        if(mVisibleComments.size() == 0) {
            isInitiallyLoading = false;
            isInitiallyLoadingFailed = false;
            if(comments.size() == 0) {
                notifyItemChanged(1);
            } else {
                notifyItemRemoved(1);
            }
        }

        int sizeBefore = mVisibleComments.size();
        mVisibleComments.addAll(comments);
        notifyItemRangeInserted(sizeBefore + 1, comments.size());

        if(mHasMoreComments != hasMoreComments) {
            if(hasMoreComments) {
                notifyItemInserted(mVisibleComments.size() + 1);
            } else {
                notifyItemRemoved(mVisibleComments.size() + 1);
            }
        }
        mHasMoreComments = hasMoreComments;
    }

    void addComment(CommentData comment) {
        if(mVisibleComments.size() == 0 || isInitiallyLoadingFailed) {
            notifyItemRemoved(1);
        }

        mVisibleComments.add(0, comment);

        if(isInitiallyLoading) {
            notifyItemInserted(2);
        } else {
            notifyItemInserted(1);
        }
    }

    void addChildComment(CommentData comment, String parentFullname, int parentPosition) {
        if(!parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
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
            notifyItemChanged(parentPosition + 1);
        } else {
            mVisibleComments.add(parentPosition + 1, comment);
            notifyItemInserted(parentPosition + 2);
        }
    }

    void initiallyLoading() {
        if(mLoadSubredditIconAsyncTask != null) {
            mLoadSubredditIconAsyncTask.cancel(true);
        }

        if(mVisibleComments.size() != 0) {
            int previousSize = mVisibleComments.size();
            mVisibleComments.clear();
            notifyItemRangeRemoved(1, previousSize);
        }

        if(isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            isInitiallyLoading = true;
            isInitiallyLoadingFailed = false;
            notifyItemChanged(1);
        } else {
            isInitiallyLoading = true;
            isInitiallyLoadingFailed = false;
            notifyItemInserted(1);
        }
    }

    void initiallyLoadCommentsFailed() {
        isInitiallyLoading = false;
        isInitiallyLoadingFailed = true;
        notifyItemChanged(1);
    }

    void loadMoreCommentsFailed() {
        loadMoreCommentsFailed = true;
        notifyItemChanged(mVisibleComments.size() + 1);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        if (holder instanceof CommentViewHolder) {
            ((CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
            ((CommentViewHolder) holder).upvoteButton.clearColorFilter();
            ((CommentViewHolder) holder).downvoteButton.clearColorFilter();
        }
    }

    @Override
    public int getItemCount() {
        if(isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
            return 2;
        }

        if(mHasMoreComments || loadMoreCommentsFailed) {
            return mVisibleComments.size() + 2;
        }

        return mVisibleComments.size() + 1;
    }

    class PostDetailViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.subreddit_icon_name_linear_layout_item_post_detail) LinearLayout mSubredditIconNameLinearLayout;
        @BindView(R.id.subreddit_icon_circle_image_view_item_post_detail) AspectRatioGifImageView mSubredditIconGifImageView;
        @BindView(R.id.subreddit_text_view_item_post_detail) TextView mSubredditTextView;
        @BindView(R.id.post_time_text_view_item_post_detail) TextView mPostTimeTextView;
        @BindView(R.id.title_text_view_item_post_detail) TextView mTitleTextView;
        @BindView(R.id.content_markdown_view_item_post_detail) MarkwonView mContentMarkdownView;
        @BindView(R.id.type_text_view_item_post_detail) Chip mTypeChip;
        @BindView(R.id.gilded_image_view_item_post_detail) ImageView mGildedImageView;
        @BindView(R.id.gilded_number_text_view_item_post_detail) TextView mGildedNumberTextView;
        @BindView(R.id.crosspost_image_view_item_post_detail) ImageView mCrosspostImageView;
        @BindView(R.id.nsfw_text_view_item_post_detail) Chip mNSFWChip;
        @BindView(R.id.link_text_view_item_post_detail) TextView linkTextView;
        @BindView(R.id.image_view_wrapper_item_post_detail) RelativeLayout mRelativeLayout;
        @BindView(R.id.load_wrapper_item_post_detail) RelativeLayout mLoadWrapper;
        @BindView(R.id.progress_bar_item_post_detail) ProgressBar mLoadImageProgressBar;
        @BindView(R.id.load_image_error_text_view_item_post_detail) TextView mLoadImageErrorTextView;
        @BindView(R.id.image_view_item_post_detail) AspectRatioImageView mImageView;
        @BindView(R.id.image_view_no_preview_link_item_post_detail) ImageView mNoPreviewLinkImageView;
        @BindView(R.id.plus_button_item_post_detail) ImageView mUpvoteButton;
        @BindView(R.id.score_text_view_item_post_detail) TextView mScoreTextView;
        @BindView(R.id.minus_button_item_post_detail) ImageView mDownvoteButton;
        @BindView(R.id.share_button_item_post_detail) ImageView mShareButton;

        PostDetailViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            mSubredditIconNameLinearLayout.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                        mPost.getSubredditNamePrefixed().substring(2));
                mActivity.startActivity(intent);
            });

            mShareButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String extraText = mPost.getTitle() + "\n" + mPost.getPermalink();
                intent.putExtra(Intent.EXTRA_TEXT, extraText);
                mActivity.startActivity(Intent.createChooser(intent, "Share"));
            });

            mUpvoteButton.setOnClickListener(view -> {
                ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
                ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                mDownvoteButton.clearColorFilter();

                if(previousUpvoteButtonColorFilter == null) {
                    //Not upvoted before
                    mPost.setVoteType(1);
                    newVoteType = RedditUtils.DIR_UPVOTE;
                    mUpvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Upvoted before
                    mPost.setVoteType(0);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    mUpvoteButton.clearColorFilter();
                }

                mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if(newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                            mPost.setVoteType(1);
                            mUpvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mPost.setVoteType(0);
                            mUpvoteButton.clearColorFilter();
                        }

                        mDownvoteButton.clearColorFilter();
                        mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        mScoreTextView.setText(Integer.toString(mPost.getScore() + previousVoteType));
                        mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                        mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });

            mDownvoteButton.setOnClickListener(view -> {
                ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
                ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
                int previousVoteType = mPost.getVoteType();
                String newVoteType;

                mUpvoteButton.clearColorFilter();

                if(previousDownvoteButtonColorFilter == null) {
                    //Not upvoted before
                    mPost.setVoteType(-1);
                    newVoteType = RedditUtils.DIR_DOWNVOTE;
                    mDownvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Upvoted before
                    mPost.setVoteType(0);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    mDownvoteButton.clearColorFilter();
                }

                mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        if(newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                            mPost.setVoteType(-1);
                            mDownvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mPost.setVoteType(0);
                            mDownvoteButton.clearColorFilter();
                        }

                        mUpvoteButton.clearColorFilter();
                        mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(mActivity, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                        mPost.setVoteType(previousVoteType);
                        mScoreTextView.setText(Integer.toString(mPost.getScore() + previousVoteType));
                        mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                        mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);

                        mCommentRecyclerViewAdapterCallback.updatePost(mPost);
                    }
                }, mPost.getFullName(), newVoteType);
            });
        }
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
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mVisibleComments.get(getAdapterPosition() - 1).getAuthor());
                mActivity.startActivity(intent);
            });

            shareButton.setOnClickListener(view -> {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String extraText = mVisibleComments.get(getAdapterPosition() - 1).getPermalink();
                intent.putExtra(Intent.EXTRA_TEXT, extraText);
                mActivity.startActivity(Intent.createChooser(intent, "Share"));
            });

            expandButton.setOnClickListener(view -> {
                if(mVisibleComments.get(getAdapterPosition() - 1).isExpanded()) {
                    collapseChildren(getAdapterPosition() - 1);
                    expandButton.setImageResource(R.drawable.ic_expand_more_black_20dp);
                } else {
                    expandChildren(getAdapterPosition() - 1);
                    mVisibleComments.get(getAdapterPosition() - 1).setExpanded(true);
                    expandButton.setImageResource(R.drawable.ic_expand_less_black_20dp);
                }
            });

            replyButton.setOnClickListener(view -> {
                Intent intent = new Intent(mActivity, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, mVisibleComments.get(getAdapterPosition() - 1).getDepth() + 1);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, mVisibleComments.get(getAdapterPosition() - 1).getCommentContent());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mVisibleComments.get(getAdapterPosition() - 1).getFullName());
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);
                intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, getAdapterPosition() - 1);
                mActivity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
            });

            upvoteButton.setOnClickListener(view -> {
                int previousVoteType = mVisibleComments.get(getAdapterPosition() - 1).getVoteType();
                String newVoteType;

                downvoteButton.clearColorFilter();

                if(previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
                    //Not upvoted before
                    mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                    newVoteType = RedditUtils.DIR_UPVOTE;
                    upvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Upvoted before
                    mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    upvoteButton.clearColorFilter();
                }

                scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition() - 1).getScore() + mVisibleComments.get(getAdapterPosition() - 1).getVoteType()));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position) {
                        if(newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                            mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                            upvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                            upvoteButton.clearColorFilter();
                        }

                        downvoteButton.clearColorFilter();
                        scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition() - 1).getScore() + mVisibleComments.get(getAdapterPosition() - 1).getVoteType()));
                    }

                    @Override
                    public void onVoteThingFail(int position) { }
                }, mVisibleComments.get(getAdapterPosition() - 1).getFullName(), newVoteType, getAdapterPosition());
            });

            downvoteButton.setOnClickListener(view -> {
                int previousVoteType = mVisibleComments.get(getAdapterPosition() - 1).getVoteType();
                String newVoteType;

                upvoteButton.clearColorFilter();

                if(previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
                    //Not downvoted before
                    mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                    newVoteType = RedditUtils.DIR_DOWNVOTE;
                    downvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    //Downvoted before
                    mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                    newVoteType = RedditUtils.DIR_UNVOTE;
                    downvoteButton.clearColorFilter();
                }

                scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition() - 1).getScore() + mVisibleComments.get(getAdapterPosition() - 1).getVoteType()));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingListener() {
                    @Override
                    public void onVoteThingSuccess(int position1) {
                        if(newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                            mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                            downvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                        } else {
                            mVisibleComments.get(getAdapterPosition() - 1).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                            downvoteButton.clearColorFilter();
                        }

                        upvoteButton.clearColorFilter();
                        scoreTextView.setText(Integer.toString(mVisibleComments.get(getAdapterPosition() - 1).getScore() + mVisibleComments.get(getAdapterPosition() - 1).getVoteType()));
                    }

                    @Override
                    public void onVoteThingFail(int position1) { }
                }, mVisibleComments.get(getAdapterPosition() - 1).getFullName(), newVoteType, getAdapterPosition());
            });
        }
    }

    class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.vertical_block_item_load_more_comments) View verticalBlock;
        @BindView(R.id.placeholder_text_view_item_load_more_comments) TextView placeholderTextView;

        LoadMoreChildCommentsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            placeholderTextView.setOnClickListener(view -> {
                int parentPosition = getParentPosition(getAdapterPosition() - 1);
                CommentData parentComment = mVisibleComments.get(parentPosition);

                mVisibleComments.get(getAdapterPosition() - 1).setLoadingMoreChildren(true);
                mVisibleComments.get(getAdapterPosition() - 1).setLoadMoreChildrenFailed(false);
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

                                            int placeholderPosition = getAdapterPosition() - 1;
                                            if(mVisibleComments.get(getAdapterPosition() - 1).getFullName().equals(parentComment.getFullName())) {
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
                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
                                        } else {
                                            mVisibleComments.get(parentPosition).getChildren()
                                                    .remove(mVisibleComments.get(parentPosition).getChildren().size() - 1);
                                            mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();

                                            int placeholderPosition = getAdapterPosition() - 1;
                                            if(mVisibleComments.get(getAdapterPosition() - 1).getFullName().equals(parentComment.getFullName())) {
                                                for(int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                                                    if(mVisibleComments.get(i).getFullName().equals(parentComment.getFullName())) {
                                                        placeholderPosition = i;
                                                        break;
                                                    }
                                                }
                                            }

                                            mVisibleComments.remove(placeholderPosition);
                                            notifyItemRemoved(placeholderPosition + 1);

                                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                                            notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
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
                                                notifyItemRangeInserted(placeholderPosition + 1, expandedComments.size());
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
                                        int placeholderPosition = getAdapterPosition() - 1;
                                        if(!mVisibleComments.get(getAdapterPosition() - 1).getFullName().equals(parentComment.getFullName())) {
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

    class LoadCommentsViewHolder extends RecyclerView.ViewHolder {
        LoadCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class LoadCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        LoadCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class NoCommentViewHolder extends RecyclerView.ViewHolder {
        NoCommentViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class IsLoadingMoreCommentsViewHolder extends RecyclerView.ViewHolder {
        IsLoadingMoreCommentsViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    class LoadMoreCommentsFailedViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_comment_footer_error) TextView errorTextView;
        @BindView(R.id.retry_button_item_comment_footer_error) Button retryButton;

        public LoadMoreCommentsFailedViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            retryButton.setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingMoreComments());
        }
    }
}
