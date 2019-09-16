package ml.docilealligator.infinityforreddit;

import CustomView.AspectRatioGifImageView;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.util.Linkify;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.chip.Chip;
import com.libRG.CustomTextView;
import com.santalu.aspectratioimageview.AspectRatioImageView;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.urlprocessor.UrlProcessorRelativeToAbsolute;
import java.util.ArrayList;
import java.util.Locale;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;

class CommentAndPostRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private static final int VIEW_TYPE_POST_DETAIL = 0;
  private static final int VIEW_TYPE_FIRST_LOADING = 1;
  private static final int VIEW_TYPE_FIRST_LOADING_FAILED = 2;
  private static final int VIEW_TYPE_NO_COMMENT_PLACEHOLDER = 3;
  private static final int VIEW_TYPE_COMMENT = 4;
  private static final int VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS = 5;
  private static final int VIEW_TYPE_IS_LOADING_MORE_COMMENTS = 6;
  private static final int VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED = 7;
  private static final int VIEW_TYPE_VIEW_ALL_COMMENTS = 8;

  private final Activity mActivity;
  private final Retrofit mRetrofit;
  private final Retrofit mOauthRetrofit;
  private final RedditDataRoomDatabase mRedditDataRoomDatabase;
  private final RequestManager mGlide;
  private final Markwon mMarkwon;
  private final String mAccessToken;
  private final String mAccountName;
  private final ArrayList<CommentData> mVisibleComments;
  private final String mSubredditNamePrefixed;
  private final Locale mLocale;
  private final CommentRecyclerViewAdapterCallback mCommentRecyclerViewAdapterCallback;
  private Post mPost;
  private String mSingleCommentId;
  private boolean mIsSingleCommentThreadMode;
  private boolean isInitiallyLoading;
  private boolean isInitiallyLoadingFailed;
  private boolean mHasMoreComments;
  private boolean loadMoreCommentsFailed;

  CommentAndPostRecyclerViewAdapter(Activity activity, Retrofit retrofit, Retrofit oauthRetrofit,
      RedditDataRoomDatabase redditDataRoomDatabase, RequestManager glide,
      String accessToken, String accountName, Post post,
      Locale locale, String singleCommentId, boolean isSingleCommentThreadMode,
      CommentRecyclerViewAdapterCallback commentRecyclerViewAdapterCallback) {
    mActivity = activity;
    mRetrofit = retrofit;
    mOauthRetrofit = oauthRetrofit;
    mRedditDataRoomDatabase = redditDataRoomDatabase;
    mGlide = glide;
    mMarkwon = Markwon.builder(mActivity)
        .usePlugin(new AbstractMarkwonPlugin() {
          @Override
          public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
            builder.linkResolver((view, link) -> {
              Intent intent = new Intent(mActivity, LinkResolverActivity.class);
              Uri uri = Uri.parse(link);
              if (uri.getScheme() == null && uri.getHost() == null) {
                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
              } else {
                intent.setData(uri);
              }
              mActivity.startActivity(intent);
            }).urlProcessor(new UrlProcessorRelativeToAbsolute("https://www.reddit.com"));
          }
        })
        .usePlugin(StrikethroughPlugin.create())
        .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
        .build();
    mAccessToken = accessToken;
    mAccountName = accountName;
    mPost = post;
    mVisibleComments = new ArrayList<>();
    mSubredditNamePrefixed = post.getSubredditNamePrefixed();
    mLocale = locale;
    mSingleCommentId = singleCommentId;
    mIsSingleCommentThreadMode = isSingleCommentThreadMode;
    mCommentRecyclerViewAdapterCallback = commentRecyclerViewAdapterCallback;
    isInitiallyLoading = true;
    isInitiallyLoadingFailed = false;
    mHasMoreComments = false;
    loadMoreCommentsFailed = false;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == 0) {
      return VIEW_TYPE_POST_DETAIL;
    }

    if (mVisibleComments.size() == 0) {
      if (position == 1) {
        if (isInitiallyLoading) {
          return VIEW_TYPE_FIRST_LOADING;
        } else if (isInitiallyLoadingFailed) {
          return VIEW_TYPE_FIRST_LOADING_FAILED;
        } else {
          return VIEW_TYPE_NO_COMMENT_PLACEHOLDER;
        }
      }
    }

    if (mIsSingleCommentThreadMode) {
      if (position == 1) {
        return VIEW_TYPE_VIEW_ALL_COMMENTS;
      }

      if (position == mVisibleComments.size() + 2) {
        if (mHasMoreComments) {
          return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
        } else {
          return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
        }
      }

      CommentData comment = mVisibleComments.get(position - 2);
      if (!comment.isPlaceHolder()) {
        return VIEW_TYPE_COMMENT;
      } else {
        return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
      }
    } else {
      if (position == mVisibleComments.size() + 1) {
        if (mHasMoreComments) {
          return VIEW_TYPE_IS_LOADING_MORE_COMMENTS;
        } else {
          return VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED;
        }
      }

      CommentData comment = mVisibleComments.get(position - 1);
      if (!comment.isPlaceHolder()) {
        return VIEW_TYPE_COMMENT;
      } else {
        return VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS;
      }
    }
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    switch (viewType) {
      case VIEW_TYPE_POST_DETAIL:
        return new PostDetailViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_post_detail, parent, false));
      case VIEW_TYPE_FIRST_LOADING:
        return new LoadCommentsViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_load_comments, parent, false));
      case VIEW_TYPE_FIRST_LOADING_FAILED:
        return new LoadCommentsFailedViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_load_comments_failed_placeholder, parent, false));
      case VIEW_TYPE_NO_COMMENT_PLACEHOLDER:
        return new NoCommentViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_no_comment_placeholder, parent, false));
      case VIEW_TYPE_COMMENT:
        return new CommentViewHolder(
            LayoutInflater.from(parent.getContext()).inflate(R.layout.item_comment, parent, false));
      case VIEW_TYPE_LOAD_MORE_CHILD_COMMENTS:
        return new LoadMoreChildCommentsViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_load_more_comments_placeholder, parent, false));
      case VIEW_TYPE_IS_LOADING_MORE_COMMENTS:
        return new IsLoadingMoreCommentsViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_comment_footer_loading, parent, false));
      case VIEW_TYPE_LOAD_MORE_COMMENTS_FAILED:
        return new LoadMoreCommentsFailedViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_comment_footer_error, parent, false));
      default:
        return new ViewAllCommentsViewHolder(LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_view_all_comments, parent, false));
    }
  }

  @SuppressLint("SetTextI18n")
  @Override
  public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    if (holder.getItemViewType() == VIEW_TYPE_POST_DETAIL) {
      ((PostDetailViewHolder) holder).mTitleTextView.setText(mPost.getTitle());
      if (mPost.getSubredditNamePrefixed().startsWith("u/")) {
        if (mPost.getAuthorIconUrl() == null) {
          String authorName =
              mPost.getAuthor().equals("[deleted]") ? mPost.getSubredditNamePrefixed().substring(2)
                  : mPost.getAuthor();
          new LoadUserDataAsyncTask(mRedditDataRoomDatabase.userDao(), authorName, mOauthRetrofit,
              iconImageUrl -> {
                if (mActivity != null && getItemCount() > 0) {
                  if (iconImageUrl == null || iconImageUrl.equals("")) {
                    mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(
                            RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(((PostDetailViewHolder) holder).mIconGifImageView);
                  } else {
                    mGlide.load(iconImageUrl)
                        .apply(
                            RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions
                                .bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(((PostDetailViewHolder) holder).mIconGifImageView);
                  }

                  if (holder.getAdapterPosition() >= 0) {
                    mPost.setAuthorIconUrl(iconImageUrl);
                  }
                }
              }).execute();
        } else if (!mPost.getAuthorIconUrl().equals("")) {
          mGlide.load(mPost.getAuthorIconUrl())
              .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
              .error(mGlide.load(R.drawable.subreddit_default_icon)
                  .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
              .into(((PostDetailViewHolder) holder).mIconGifImageView);
        } else {
          mGlide.load(R.drawable.subreddit_default_icon)
              .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
              .into(((PostDetailViewHolder) holder).mIconGifImageView);
        }
      } else {
        if (mPost.getSubredditIconUrl() == null) {
          new LoadSubredditIconAsyncTask(
              mRedditDataRoomDatabase.subredditDao(), mPost.getSubredditNamePrefixed().substring(2),
              mRetrofit, iconImageUrl -> {
            if (iconImageUrl == null || iconImageUrl.equals("")) {
              mGlide.load(R.drawable.subreddit_default_icon)
                  .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                  .into(((PostDetailViewHolder) holder).mIconGifImageView);
            } else {
              mGlide.load(iconImageUrl)
                  .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                  .error(mGlide.load(R.drawable.subreddit_default_icon)
                      .apply(
                          RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                  .into(((PostDetailViewHolder) holder).mIconGifImageView);
            }

            mPost.setSubredditIconUrl(iconImageUrl);
          }).execute();
        } else if (!mPost.getSubredditIconUrl().equals("")) {
          mGlide.load(mPost.getSubredditIconUrl())
              .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
              .error(mGlide.load(R.drawable.subreddit_default_icon)
                  .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
              .into(((PostDetailViewHolder) holder).mIconGifImageView);
        } else {
          mGlide.load(R.drawable.subreddit_default_icon)
              .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
              .into(((PostDetailViewHolder) holder).mIconGifImageView);
        }
      }

      switch (mPost.getVoteType()) {
        case 1:
          //Upvote
          ((PostDetailViewHolder) holder).mUpvoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
                  PorterDuff.Mode.SRC_IN);
          ((PostDetailViewHolder) holder).mScoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
          break;
        case -1:
          //Downvote
          ((PostDetailViewHolder) holder).mDownvoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
                  PorterDuff.Mode.SRC_IN);
          ((PostDetailViewHolder) holder).mScoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
          break;
        case 0:
          ((PostDetailViewHolder) holder).mUpvoteButton.clearColorFilter();
          ((PostDetailViewHolder) holder).mDownvoteButton.clearColorFilter();
          ((PostDetailViewHolder) holder).mScoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
      }

      if (mPost.getPostType() != Post.TEXT_TYPE
          && mPost.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
        ((PostDetailViewHolder) holder).mRelativeLayout.setVisibility(View.VISIBLE);
        ((PostDetailViewHolder) holder).mImageView.setVisibility(View.VISIBLE);
        ((PostDetailViewHolder) holder).mImageView
            .setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
        loadImage((PostDetailViewHolder) holder);
      } else {
        ((PostDetailViewHolder) holder).mRelativeLayout.setVisibility(View.GONE);
        ((PostDetailViewHolder) holder).mImageView.setVisibility(View.GONE);
      }

      if (mPost.isArchived()) {
        ((PostDetailViewHolder) holder).mUpvoteButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        ((PostDetailViewHolder) holder).mDownvoteButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
      }

      if (mPost.isCrosspost()) {
        ((PostDetailViewHolder) holder).mCrosspostImageView.setOnClickListener(view -> {
          Intent crosspostIntent = new Intent(mActivity, ViewPostDetailActivity.class);
          crosspostIntent
              .putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
          mActivity.startActivity(crosspostIntent);
        });
        ((PostDetailViewHolder) holder).mCrosspostImageView.setVisibility(View.VISIBLE);
      }

      ((PostDetailViewHolder) holder).mSubredditTextView.setText(mPost.getSubredditNamePrefixed());
      ((PostDetailViewHolder) holder).mUserTextView.setText(mPost.getAuthorNamePrefixed());

      ((PostDetailViewHolder) holder).mPostTimeTextView.setText(mPost.getPostTime());

      if (mPost.getGilded() > 0) {
        ((PostDetailViewHolder) holder).mGildedImageView.setVisibility(View.VISIBLE);
        ((PostDetailViewHolder) holder).mGildedNumberTextView.setVisibility(View.VISIBLE);
        String gildedNumber = mActivity.getResources()
            .getString(R.string.gilded_count, mPost.getGilded());
        ((PostDetailViewHolder) holder).mGildedNumberTextView.setText(gildedNumber);
      }

      if (mPost.isArchived()) {
        ((PostDetailViewHolder) holder).mArchivedImageView.setVisibility(View.VISIBLE);
      }

      if (mPost.isLocked()) {
        ((PostDetailViewHolder) holder).mLockedImageView.setVisibility(View.VISIBLE);
      }

      if (mPost.isSpoiler() || mPost.getFlair() != null) {
        ((PostDetailViewHolder) holder).spoilerFlairlinearLayout.setVisibility(View.VISIBLE);
      }

      if (mPost.isSpoiler()) {
        ((PostDetailViewHolder) holder).spoilerTextView.setVisibility(View.VISIBLE);
        ((PostDetailViewHolder) holder).spoilerTextView
            .setBackgroundColor(
                mActivity.getResources().getColor(R.color.backgroundColorPrimaryDark));
      }

      if (mPost.getFlair() != null) {
        ((PostDetailViewHolder) holder).flairTextView.setVisibility(View.VISIBLE);
        ((PostDetailViewHolder) holder).flairTextView.setText(mPost.getFlair());
        ((PostDetailViewHolder) holder).flairTextView
            .setBackgroundColor(
                mActivity.getResources().getColor(R.color.backgroundColorPrimaryDark));
      }

      if (mPost.isNSFW()) {
        ((PostDetailViewHolder) holder).mNSFWChip.setOnClickListener(view -> {
          Intent intent = new Intent(mActivity, FilteredThingActivity.class);
          intent.putExtra(FilteredThingActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
          intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
          intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
          intent.putExtra(FilteredThingActivity.EXTRA_FILTER, Post.NSFW_TYPE);
          mActivity.startActivity(intent);
        });
        ((PostDetailViewHolder) holder).mNSFWChip.setVisibility(View.VISIBLE);
      } else {
        ((PostDetailViewHolder) holder).mNSFWChip.setVisibility(View.GONE);
      }

      String scoreWithVote = Integer.toString(mPost.getScore() + mPost.getVoteType());
      ((PostDetailViewHolder) holder).mScoreTextView.setText(scoreWithVote);

      ((PostDetailViewHolder) holder).mTypeChip.setOnClickListener(view -> {
        Intent intent = new Intent(mActivity, FilteredThingActivity.class);
        intent.putExtra(FilteredThingActivity.EXTRA_NAME, mSubredditNamePrefixed.substring(2));
        intent.putExtra(FilteredThingActivity.EXTRA_POST_TYPE, PostDataSource.TYPE_SUBREDDIT);
        intent.putExtra(FilteredThingActivity.EXTRA_SORT_TYPE, PostDataSource.SORT_TYPE_HOT);
        intent.putExtra(FilteredThingActivity.EXTRA_FILTER, mPost.getPostType());
        mActivity.startActivity(intent);

      });

      switch (mPost.getPostType()) {
        case Post.IMAGE_TYPE:
          ((PostDetailViewHolder) holder).mTypeChip.setText("IMAGE");

          ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, ViewImageActivity.class);
            intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, mPost.getUrl());
            intent.putExtra(ViewImageActivity.TITLE_KEY, mPost.getTitle());
            intent.putExtra(ViewImageActivity.FILE_NAME_KEY,
                mPost.getSubredditNamePrefixed().substring(2)
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
            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
            Uri uri = Uri.parse(mPost.getUrl());
            if (uri.getScheme() == null && uri.getHost() == null) {
              intent.setData(LinkResolverActivity.getRedditUriByPath(mPost.getUrl()));
            } else {
              intent.setData(uri);
            }
            mActivity.startActivity(intent);
          });
          break;
        case Post.GIF_VIDEO_TYPE:
          ((PostDetailViewHolder) holder).mTypeChip.setText("GIF");

          final Uri gifVideoUri = Uri.parse(mPost.getVideoUrl());
          ((PostDetailViewHolder) holder).mImageView.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, ViewVideoActivity.class);
            intent.setData(gifVideoUri);
            intent.putExtra(ViewVideoActivity.TITLE_KEY, mPost.getTitle());
            intent.putExtra(ViewVideoActivity.IS_HLS_VIDEO_KEY, mPost.isHLSVideo());
            intent
                .putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
            if (mPost.isDownloadableGifOrVideo()) {
              intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
              intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditName());
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
            intent.putExtra(ViewVideoActivity.IS_HLS_VIDEO_KEY, mPost.isHLSVideo());
            intent
                .putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
            if (mPost.isDownloadableGifOrVideo()) {
              intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
              intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditName());
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

          if (mPost.getSelfText() != null && !mPost.getSelfText().equals("")) {
            ((PostDetailViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
            mMarkwon.setMarkdown(((PostDetailViewHolder) holder).mContentMarkdownView,
                mPost.getSelfText());
          }

          ((PostDetailViewHolder) holder).mNoPreviewLinkImageView.setVisibility(View.VISIBLE);
          ((PostDetailViewHolder) holder).mNoPreviewLinkImageView.setOnClickListener(view -> {
            Intent intent = new Intent(mActivity, LinkResolverActivity.class);
            Uri uri = Uri.parse(mPost.getUrl());
            if (uri.getScheme() == null && uri.getHost() == null) {
              intent.setData(LinkResolverActivity.getRedditUriByPath(mPost.getUrl()));
            } else {
              intent.setData(uri);
            }
            mActivity.startActivity(intent);
          });
          break;
        case Post.TEXT_TYPE:
          ((PostDetailViewHolder) holder).mTypeChip.setText("TEXT");

          if (!mPost.getSelfText().equals("")) {
            ((PostDetailViewHolder) holder).mContentMarkdownView.setVisibility(View.VISIBLE);
            mMarkwon.setMarkdown(((PostDetailViewHolder) holder).mContentMarkdownView,
                mPost.getSelfText());
          }
          break;
      }
    } else if (holder.getItemViewType() == VIEW_TYPE_COMMENT) {
      CommentData comment;
      if (mIsSingleCommentThreadMode) {
        comment = mVisibleComments.get(holder.getAdapterPosition() - 2);
      } else {
        comment = mVisibleComments.get(holder.getAdapterPosition() - 1);
      }

      if (mIsSingleCommentThreadMode && comment.getId().equals(mSingleCommentId)) {
        ((CommentViewHolder) holder).itemView.setBackgroundColor(
            mActivity.getResources().getColor(R.color.singleCommentThreadBackgroundColor));
      }

      String authorPrefixed = "u/" + comment.getAuthor();
      ((CommentViewHolder) holder).authorTextView.setText(authorPrefixed);

      ((CommentViewHolder) holder).commentTimeTextView.setText(comment.getCommentTime());

      mMarkwon.setMarkdown(((CommentViewHolder) holder).commentMarkdownView,
          comment.getCommentContent());
      ((CommentViewHolder) holder).scoreTextView
          .setText(Integer.toString(comment.getScore() + comment.getVoteType()));

      ViewGroup.LayoutParams params = ((CommentViewHolder) holder).verticalBlock.getLayoutParams();
      params.width = comment.getDepth() * 16;
      ((CommentViewHolder) holder).verticalBlock.setLayoutParams(params);

      if (!mPost.isArchived() && !mPost.isLocked() && comment.getAuthor().equals(mAccountName)) {
        ((CommentViewHolder) holder).moreButton.setVisibility(View.VISIBLE);

        ((CommentViewHolder) holder).moreButton.setOnClickListener(view -> {
          ModifyCommentBottomSheetFragment modifyCommentBottomSheetFragment = new ModifyCommentBottomSheetFragment();
          Bundle bundle = new Bundle();
          bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
          bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_COMMENT_CONTENT,
              comment.getCommentContent());
          bundle.putString(ModifyCommentBottomSheetFragment.EXTRA_COMMENT_FULLNAME,
              comment.getFullName());
          if (mIsSingleCommentThreadMode) {
            bundle.putInt(ModifyCommentBottomSheetFragment.EXTRA_POSITION,
                holder.getAdapterPosition() - 2);
          } else {
            bundle.putInt(ModifyCommentBottomSheetFragment.EXTRA_POSITION,
                holder.getAdapterPosition() - 1);
          }
          modifyCommentBottomSheetFragment.setArguments(bundle);
          modifyCommentBottomSheetFragment
              .show(((AppCompatActivity) mActivity).getSupportFragmentManager(),
                  modifyCommentBottomSheetFragment.getTag());
        });
      }

      if (comment.hasReply()) {
        if (comment.isExpanded()) {
          ((CommentViewHolder) holder).expandButton
              .setImageResource(R.drawable.ic_expand_less_black_20dp);
        } else {
          ((CommentViewHolder) holder).expandButton
              .setImageResource(R.drawable.ic_expand_more_black_20dp);
        }
        ((CommentViewHolder) holder).expandButton.setVisibility(View.VISIBLE);
      }

      switch (comment.getVoteType()) {
        case CommentData.VOTE_TYPE_UPVOTE:
          ((CommentViewHolder) holder).upVoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
                  android.graphics.PorterDuff.Mode.SRC_IN);
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
          break;
        case CommentData.VOTE_TYPE_DOWNVOTE:
          ((CommentViewHolder) holder).downVoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
                  android.graphics.PorterDuff.Mode.SRC_IN);
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
          break;
      }

      if (mPost.isArchived()) {
        ((CommentViewHolder) holder).replyButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        ((CommentViewHolder) holder).upVoteButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
        ((CommentViewHolder) holder).downVoteButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
      }

      if (mPost.isLocked()) {
        ((CommentViewHolder) holder).replyButton
            .setColorFilter(
                ContextCompat.getColor(mActivity, R.color.voteAndReplyUnavailableVoteButtonColor),
                android.graphics.PorterDuff.Mode.SRC_IN);
      }

      ((CommentViewHolder) holder).replyButton.setOnClickListener(view -> {
        if (mAccessToken == null) {
          Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
          return;
        }

        if (mPost.isArchived()) {
          Toast.makeText(mActivity, R.string.archived_post_reply_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        if (mPost.isLocked()) {
          Toast.makeText(mActivity, R.string.locked_post_reply_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        Intent intent = new Intent(mActivity, CommentActivity.class);
        intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, comment.getDepth() + 1);
        intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, comment.getCommentContent());
        intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, comment.getFullName());
        intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, true);

        int parentPosition = mIsSingleCommentThreadMode ? holder.getAdapterPosition() - 2
            : holder.getAdapterPosition() - 1;
        intent.putExtra(CommentActivity.EXTRA_PARENT_POSITION_KEY, parentPosition);
        mActivity.startActivityForResult(intent, CommentActivity.WRITE_COMMENT_REQUEST_CODE);
      });

      ((CommentViewHolder) holder).upVoteButton.setOnClickListener(view -> {
        if (mPost.isArchived()) {
          Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        if (mAccessToken == null) {
          Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
          return;
        }

        int commentPosition = mIsSingleCommentThreadMode ? holder.getAdapterPosition() - 2
            : holder.getAdapterPosition() - 1;

        int previousVoteType = mVisibleComments.get(commentPosition).getVoteType();
        String newVoteType;

        ((CommentViewHolder) holder).downVoteButton.clearColorFilter();

        if (previousVoteType != CommentData.VOTE_TYPE_UPVOTE) {
          //Not upvoted before
          mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
          newVoteType = RedditUtils.DIR_UPVOTE;
          ((CommentViewHolder) holder).upVoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
                  android.graphics.PorterDuff.Mode.SRC_IN);
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
        } else {
          //Upvoted before
          mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
          newVoteType = RedditUtils.DIR_UNVOTE;
          ((CommentViewHolder) holder).upVoteButton.clearColorFilter();
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
        }

        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(
            mVisibleComments.get(commentPosition).getScore() + mVisibleComments.get(commentPosition)
                .getVoteType()));

        VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
              @Override
              public void onVoteThingSuccess(int position) {
                if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                  mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_UPVOTE);
                  ((CommentViewHolder) holder).upVoteButton
                      .setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
                          android.graphics.PorterDuff.Mode.SRC_IN);
                  ((CommentViewHolder) holder).scoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
                } else {
                  mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                  ((CommentViewHolder) holder).upVoteButton.clearColorFilter();
                  ((CommentViewHolder) holder).scoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
                }

                ((CommentViewHolder) holder).downVoteButton.clearColorFilter();
                ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(
                    mVisibleComments.get(commentPosition).getScore() + mVisibleComments
                        .get(commentPosition).getVoteType()));
              }

              @Override
              public void onVoteThingFail(int position) {
              }
            }, mVisibleComments.get(commentPosition).getFullName(), newVoteType,
            holder.getAdapterPosition());
      });

      ((CommentViewHolder) holder).downVoteButton.setOnClickListener(view -> {
        if (mPost.isArchived()) {
          Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        if (mAccessToken == null) {
          Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
          return;
        }

        int commentPosition = mIsSingleCommentThreadMode ? holder.getAdapterPosition() - 2
            : holder.getAdapterPosition() - 1;

        int previousVoteType = mVisibleComments.get(commentPosition).getVoteType();
        String newVoteType;

        ((CommentViewHolder) holder).upVoteButton.clearColorFilter();

        if (previousVoteType != CommentData.VOTE_TYPE_DOWNVOTE) {
          //Not downvoted before
          mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
          newVoteType = RedditUtils.DIR_DOWNVOTE;
          ((CommentViewHolder) holder).downVoteButton
              .setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
                  android.graphics.PorterDuff.Mode.SRC_IN);
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
        } else {
          //Downvoted before
          mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
          newVoteType = RedditUtils.DIR_UNVOTE;
          ((CommentViewHolder) holder).downVoteButton.clearColorFilter();
          ((CommentViewHolder) holder).scoreTextView
              .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
        }

        ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(
            mVisibleComments.get(commentPosition).getScore() + mVisibleComments.get(commentPosition)
                .getVoteType()));

        VoteThing.voteThing(mOauthRetrofit, mAccessToken, new VoteThing.VoteThingListener() {
              @Override
              public void onVoteThingSuccess(int position1) {
                if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                  mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_DOWNVOTE);
                  ((CommentViewHolder) holder).downVoteButton
                      .setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
                          android.graphics.PorterDuff.Mode.SRC_IN);
                  ((CommentViewHolder) holder).scoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
                } else {
                  mVisibleComments.get(commentPosition).setVoteType(CommentData.VOTE_TYPE_NO_VOTE);
                  ((CommentViewHolder) holder).downVoteButton.clearColorFilter();
                  ((CommentViewHolder) holder).scoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
                }

                ((CommentViewHolder) holder).upVoteButton.clearColorFilter();
                ((CommentViewHolder) holder).scoreTextView.setText(Integer.toString(
                    mVisibleComments.get(commentPosition).getScore() + mVisibleComments
                        .get(commentPosition).getVoteType()));
              }

              @Override
              public void onVoteThingFail(int position1) {
              }
            }, mVisibleComments.get(commentPosition).getFullName(), newVoteType,
            holder.getAdapterPosition());
      });
    } else if (holder instanceof LoadMoreChildCommentsViewHolder) {
      CommentData placeholder;
      placeholder =
          mIsSingleCommentThreadMode ? mVisibleComments.get(holder.getAdapterPosition() - 2)
              : mVisibleComments.get(holder.getAdapterPosition() - 1);

      ViewGroup.LayoutParams params = ((LoadMoreChildCommentsViewHolder) holder).verticalBlock
          .getLayoutParams();
      params.width = placeholder.getDepth() * 16;
      ((LoadMoreChildCommentsViewHolder) holder).verticalBlock.setLayoutParams(params);

      if (placeholder.isLoadingMoreChildren()) {
        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView.setText(R.string.loading);
      } else if (placeholder.isLoadMoreChildrenFailed()) {
        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView
            .setText(R.string.comment_load_more_comments_failed);
      } else {
        ((LoadMoreChildCommentsViewHolder) holder).placeholderTextView
            .setText(R.string.comment_load_more_comments);
      }
    } else if (holder instanceof LoadMoreCommentsFailedViewHolder) {
      ((LoadMoreCommentsFailedViewHolder) holder).errorTextView
          .setText(R.string.load_comments_failed);
    }
  }

  private void loadImage(PostDetailViewHolder holder) {
    RequestBuilder imageRequestBuilder = mGlide.load(mPost.getPreviewUrl())
        .apply(new RequestOptions().override(mPost.getPreviewWidth(), mPost.getPreviewHeight()))
        .listener(new RequestListener<Drawable>() {
          @Override
          public boolean onLoadFailed(@Nullable GlideException e, Object model,
              Target<Drawable> target, boolean isFirstResource) {
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
          public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target,
              DataSource dataSource, boolean isFirstResource) {
            holder.mLoadWrapper.setVisibility(View.GONE);
            return false;
          }
        });

    if (mPost.isNSFW()) {
      imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
          .into(holder.mImageView);
    } else {
      imageRequestBuilder.into(holder.mImageView);
    }
  }

  void updatePost(Post post) {
    mPost = post;
    notifyItemChanged(0);
  }

  private int getParentPosition(int position) {
    int childDepth = mVisibleComments.get(position).getDepth();
    for (int i = position; i >= 0; i--) {
      if (mVisibleComments.get(i).getDepth() < childDepth) {
        return i;
      }
    }
    return -1;
  }

  private void expandChildren(int position) {
    CommentData comment = mVisibleComments.get(position);
    if (!comment.isExpanded()) {
      comment.setExpanded(true);
      ArrayList<CommentData> children = comment.getChildren();
      if (children != null && children.size() > 0) {
        for (int i = 0; i < children.size(); i++) {
          children.get(i).setExpanded(false);
        }
        mVisibleComments.addAll(position + 1, children);
        if (mIsSingleCommentThreadMode) {
          notifyItemRangeInserted(position + 3, children.size());
        } else {
          notifyItemRangeInserted(position + 2, children.size());
        }
      }
    }
  }

  private void collapseChildren(int position) {
    mVisibleComments.get(position).setExpanded(false);
    int depth = mVisibleComments.get(position).getDepth();
    int allChildrenSize = 0;
    for (int i = position + 1; i < mVisibleComments.size(); i++) {
      if (mVisibleComments.get(i).getDepth() > depth) {
        allChildrenSize++;
      } else {
        break;
      }
    }

    mVisibleComments.subList(position + 1, position + 1 + allChildrenSize).clear();
    if (mIsSingleCommentThreadMode) {
      notifyItemRangeRemoved(position + 3, allChildrenSize);
    } else {
      notifyItemRangeRemoved(position + 2, allChildrenSize);
    }
  }

  void addComments(ArrayList<CommentData> comments, boolean hasMoreComments) {
    if (mVisibleComments.size() == 0) {
      isInitiallyLoading = false;
      isInitiallyLoadingFailed = false;
      if (comments.size() == 0) {
        notifyItemChanged(1);
      } else {
        notifyItemRemoved(1);
      }
    }

    int sizeBefore = mVisibleComments.size();
    mVisibleComments.addAll(comments);
    if (mIsSingleCommentThreadMode) {
      notifyItemRangeInserted(sizeBefore + 2, comments.size());
    } else {
      notifyItemRangeInserted(sizeBefore + 1, comments.size());
    }

    if (mHasMoreComments != hasMoreComments) {
      if (hasMoreComments) {
        if (mIsSingleCommentThreadMode) {
          notifyItemInserted(mVisibleComments.size() + 2);
        } else {
          notifyItemInserted(mVisibleComments.size() + 1);
        }
      } else {
        if (mIsSingleCommentThreadMode) {
          notifyItemRemoved(mVisibleComments.size() + 2);
        } else {
          notifyItemRemoved(mVisibleComments.size() + 1);
        }
      }
    }
    mHasMoreComments = hasMoreComments;
  }

  void addComment(CommentData comment) {
    if (mVisibleComments.size() == 0 || isInitiallyLoadingFailed) {
      notifyItemRemoved(1);
    }

    mVisibleComments.add(0, comment);

    if (isInitiallyLoading) {
      notifyItemInserted(2);
    } else {
      notifyItemInserted(1);
    }
  }

  void addChildComment(CommentData comment, String parentFullname, int parentPosition) {
    if (!parentFullname.equals(mVisibleComments.get(parentPosition).getFullName())) {
      for (int i = 0; i < mVisibleComments.size(); i++) {
        if (parentFullname.equals(mVisibleComments.get(i).getFullName())) {
          parentPosition = i;
          break;
        }
      }
    }

    mVisibleComments.get(parentPosition).addChild(comment);
    mVisibleComments.get(parentPosition).setHasReply(true);
    if (!mVisibleComments.get(parentPosition).isExpanded()) {
      expandChildren(parentPosition);
      if (mIsSingleCommentThreadMode) {
        notifyItemChanged(parentPosition + 2);
      } else {
        notifyItemChanged(parentPosition + 1);
      }
    } else {
      mVisibleComments.add(parentPosition + 1, comment);
      if (mIsSingleCommentThreadMode) {
        notifyItemInserted(parentPosition + 3);
      } else {
        notifyItemInserted(parentPosition + 2);
      }
    }
  }

  void setSingleComment(String singleCommentId, boolean isSingleCommentThreadMode) {
    mSingleCommentId = singleCommentId;
    mIsSingleCommentThreadMode = isSingleCommentThreadMode;
  }

  void initiallyLoading() {
    if (mVisibleComments.size() != 0) {
      int previousSize = mVisibleComments.size();
      mVisibleComments.clear();
      if (mIsSingleCommentThreadMode) {
        notifyItemRangeRemoved(1, previousSize + 1);
      } else {
        notifyItemRangeRemoved(1, previousSize);
      }
    }

    if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
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
    if (mIsSingleCommentThreadMode) {
      notifyItemChanged(mVisibleComments.size() + 2);
    } else {
      notifyItemChanged(mVisibleComments.size() + 1);
    }
  }

  void editComment(String commentContent, int position) {
    mVisibleComments.get(position).setCommentContent(commentContent);
    if (mIsSingleCommentThreadMode) {
      notifyItemChanged(position + 2);
    } else {
      notifyItemChanged(position + 1);
    }
  }

  void deleteComment(int position) {
    if (mVisibleComments.get(position).hasReply()) {
      mVisibleComments.get(position).setAuthor("[deleted]");
      mVisibleComments.get(position).setCommentContent("[deleted]");
      if (mIsSingleCommentThreadMode) {
        notifyItemChanged(position + 2);
      } else {
        notifyItemChanged(position + 1);
      }
    } else {
      mVisibleComments.remove(position);
      if (mIsSingleCommentThreadMode) {
        notifyItemRemoved(position + 2);
      } else {
        notifyItemRemoved(position + 1);
      }
    }
  }

  @Override
  public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
    if (holder instanceof CommentViewHolder) {
      ((CommentViewHolder) holder).moreButton.setVisibility(View.GONE);
      ((CommentViewHolder) holder).expandButton.setVisibility(View.GONE);
      ((CommentViewHolder) holder).upVoteButton.clearColorFilter();
      ((CommentViewHolder) holder).scoreTextView
          .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
      ((CommentViewHolder) holder).downVoteButton.clearColorFilter();
      ((CommentViewHolder) holder).replyButton.clearColorFilter();
      ((CommentViewHolder) holder).itemView.setBackgroundColor(
          mActivity.getResources().getColor(R.color.cardViewBackgroundColor));
    } else if (holder instanceof PostDetailViewHolder) {
      ((PostDetailViewHolder) holder).mUpvoteButton.clearColorFilter();
      ((PostDetailViewHolder) holder).mScoreTextView
          .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
      ((PostDetailViewHolder) holder).mDownvoteButton.clearColorFilter();
      ((PostDetailViewHolder) holder).spoilerFlairlinearLayout.setVisibility(View.GONE);
      ((PostDetailViewHolder) holder).flairTextView.setVisibility(View.GONE);
      ((PostDetailViewHolder) holder).spoilerTextView.setVisibility(View.GONE);
      ((PostDetailViewHolder) holder).mNSFWChip.setVisibility(View.GONE);
    }
  }

  @Override
  public int getItemCount() {
    if (isInitiallyLoading || isInitiallyLoadingFailed || mVisibleComments.size() == 0) {
      return 2;
    }

    if (mHasMoreComments || loadMoreCommentsFailed) {
      if (mIsSingleCommentThreadMode) {
        return mVisibleComments.size() + 3;
      } else {
        return mVisibleComments.size() + 2;
      }
    }

    if (mIsSingleCommentThreadMode) {
      return mVisibleComments.size() + 2;
    } else {
      return mVisibleComments.size() + 1;
    }
  }

  interface CommentRecyclerViewAdapterCallback {

    void updatePost(Post post);

    void retryFetchingComments();

    void retryFetchingMoreComments();
  }

  class PostDetailViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.icon_gif_image_view_item_post_detail)
    AspectRatioGifImageView mIconGifImageView;
    @BindView(R.id.subreddit_text_view_item_post_detail)
    TextView mSubredditTextView;
    @BindView(R.id.user_text_view_item_post_detail)
    TextView mUserTextView;
    @BindView(R.id.post_time_text_view_item_post_detail)
    TextView mPostTimeTextView;
    @BindView(R.id.title_text_view_item_post_detail)
    TextView mTitleTextView;
    @BindView(R.id.content_markdown_view_item_post_detail)
    TextView mContentMarkdownView;
    @BindView(R.id.type_text_view_item_post_detail)
    Chip mTypeChip;
    @BindView(R.id.gilded_image_view_item_post_detail)
    ImageView mGildedImageView;
    @BindView(R.id.gilded_number_text_view_item_post_detail)
    TextView mGildedNumberTextView;
    @BindView(R.id.crosspost_image_view_item_post_detail)
    ImageView mCrosspostImageView;
    @BindView(R.id.archived_image_view_item_post_detail)
    ImageView mArchivedImageView;
    @BindView(R.id.locked_image_view_item_post_detail)
    ImageView mLockedImageView;
    @BindView(R.id.nsfw_text_view_item_post_detail)
    Chip mNSFWChip;
    @BindView(R.id.spoiler_flair_linear_layout_item_post_detail)
    LinearLayout spoilerFlairlinearLayout;
    @BindView(R.id.spoiler_custom_text_view_item_post_detail)
    CustomTextView spoilerTextView;
    @BindView(R.id.flair_custom_text_view_item_post_detail)
    CustomTextView flairTextView;
    @BindView(R.id.link_text_view_item_post_detail)
    TextView linkTextView;
    @BindView(R.id.image_view_wrapper_item_post_detail)
    RelativeLayout mRelativeLayout;
    @BindView(R.id.load_wrapper_item_post_detail)
    RelativeLayout mLoadWrapper;
    @BindView(R.id.progress_bar_item_post_detail)
    ProgressBar mLoadImageProgressBar;
    @BindView(R.id.load_image_error_text_view_item_post_detail)
    TextView mLoadImageErrorTextView;
    @BindView(R.id.image_view_item_post_detail)
    AspectRatioImageView mImageView;
    @BindView(R.id.image_view_no_preview_link_item_post_detail)
    ImageView mNoPreviewLinkImageView;
    @BindView(R.id.plus_button_item_post_detail)
    ImageView mUpvoteButton;
    @BindView(R.id.score_text_view_item_post_detail)
    TextView mScoreTextView;
    @BindView(R.id.minus_button_item_post_detail)
    ImageView mDownvoteButton;
    @BindView(R.id.share_button_item_post_detail)
    ImageView mShareButton;

    PostDetailViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      mIconGifImageView.setOnClickListener(view -> mSubredditTextView.performClick());

      mSubredditTextView.setOnClickListener(view -> {
        Intent intent;
        if (mPost.getSubredditNamePrefixed().equals("u/" + mPost.getAuthor())) {
          intent = new Intent(mActivity, ViewUserDetailActivity.class);
          intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mPost.getAuthor());
        } else {
          intent = new Intent(mActivity, ViewSubredditDetailActivity.class);
          intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
              mPost.getSubredditNamePrefixed().substring(2));
        }
        mActivity.startActivity(intent);
      });

      mUserTextView.setOnClickListener(view -> {
        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
        intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mPost.getAuthor());
        mActivity.startActivity(intent);
      });

      mShareButton.setOnClickListener(view -> {
        try {
          Intent intent = new Intent(Intent.ACTION_SEND);
          intent.setType("text/plain");
          String extraText = mPost.getTitle() + "\n" + mPost.getPermalink();
          intent.putExtra(Intent.EXTRA_TEXT, extraText);
          mActivity
              .startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
          Toast.makeText(mActivity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT)
              .show();
        }
      });

      mUpvoteButton.setOnClickListener(view -> {
        if (mPost.isArchived()) {
          Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        if (mAccessToken == null) {
          Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
          return;
        }

        ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
        ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
        int previousScoreTextViewColor = mScoreTextView.getCurrentTextColor();

        int previousVoteType = mPost.getVoteType();
        String newVoteType;

        mDownvoteButton.clearColorFilter();

        if (previousUpvoteButtonColorFilter == null) {
          //Not upvoted before
          mPost.setVoteType(1);
          newVoteType = RedditUtils.DIR_UPVOTE;
          mUpvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
              android.graphics.PorterDuff.Mode.SRC_IN);
          mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
        } else {
          //Upvoted before
          mPost.setVoteType(0);
          newVoteType = RedditUtils.DIR_UNVOTE;
          mUpvoteButton.clearColorFilter();
          mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
        }

        mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

        mCommentRecyclerViewAdapterCallback.updatePost(mPost);

        VoteThing.voteThing(mOauthRetrofit, mAccessToken,
            new VoteThing.VoteThingWithoutPositionListener() {
              @Override
              public void onVoteThingSuccess() {
                if (newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                  mPost.setVoteType(1);
                  mUpvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.upvoted),
                      android.graphics.PorterDuff.Mode.SRC_IN);
                  mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.upvoted));
                } else {
                  mPost.setVoteType(0);
                  mUpvoteButton.clearColorFilter();
                  mScoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
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
                mScoreTextView.setTextColor(previousScoreTextViewColor);

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);
              }
            }, mPost.getFullName(), newVoteType);
      });

      mDownvoteButton.setOnClickListener(view -> {
        if (mPost.isArchived()) {
          Toast.makeText(mActivity, R.string.archived_post_vote_unavailable, Toast.LENGTH_SHORT)
              .show();
          return;
        }

        if (mAccessToken == null) {
          Toast.makeText(mActivity, R.string.login_first, Toast.LENGTH_SHORT).show();
          return;
        }

        ColorFilter previousUpvoteButtonColorFilter = mUpvoteButton.getColorFilter();
        ColorFilter previousDownvoteButtonColorFilter = mDownvoteButton.getColorFilter();
        int previousScoreTextViewColor = mScoreTextView.getCurrentTextColor();

        int previousVoteType = mPost.getVoteType();
        String newVoteType;

        mUpvoteButton.clearColorFilter();

        if (previousDownvoteButtonColorFilter == null) {
          //Not upvoted before
          mPost.setVoteType(-1);
          newVoteType = RedditUtils.DIR_DOWNVOTE;
          mDownvoteButton.setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
              android.graphics.PorterDuff.Mode.SRC_IN);
          mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
        } else {
          //Upvoted before
          mPost.setVoteType(0);
          newVoteType = RedditUtils.DIR_UNVOTE;
          mDownvoteButton.clearColorFilter();
          mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
        }

        mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

        mCommentRecyclerViewAdapterCallback.updatePost(mPost);

        VoteThing.voteThing(mOauthRetrofit, mAccessToken,
            new VoteThing.VoteThingWithoutPositionListener() {
              @Override
              public void onVoteThingSuccess() {
                if (newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                  mPost.setVoteType(-1);
                  mDownvoteButton
                      .setColorFilter(ContextCompat.getColor(mActivity, R.color.downvoted),
                          android.graphics.PorterDuff.Mode.SRC_IN);
                  mScoreTextView.setTextColor(ContextCompat.getColor(mActivity, R.color.downvoted));
                } else {
                  mPost.setVoteType(0);
                  mDownvoteButton.clearColorFilter();
                  mScoreTextView
                      .setTextColor(ContextCompat.getColor(mActivity, R.color.defaultTextColor));
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
                mScoreTextView.setTextColor(previousScoreTextViewColor);

                mCommentRecyclerViewAdapterCallback.updatePost(mPost);
              }
            }, mPost.getFullName(), newVoteType);
      });
    }
  }

  class CommentViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.author_text_view_item_post_comment)
    TextView authorTextView;
    @BindView(R.id.comment_time_text_view_item_post_comment)
    TextView commentTimeTextView;
    @BindView(R.id.comment_markdown_view_item_post_comment)
    TextView commentMarkdownView;
    @BindView(R.id.up_vote_button_item_post_comment)
    ImageView upVoteButton;
    @BindView(R.id.score_text_view_item_post_comment)
    TextView scoreTextView;
    @BindView(R.id.down_vote_button_item_post_comment)
    ImageView downVoteButton;
    @BindView(R.id.more_button_item_post_comment)
    ImageView moreButton;
    @BindView(R.id.expand_button_item_post_comment)
    ImageView expandButton;
    @BindView(R.id.share_button_item_post_comment)
    ImageView shareButton;
    @BindView(R.id.reply_button_item_post_comment)
    ImageView replyButton;
    @BindView(R.id.vertical_block_item_post_comment)
    View verticalBlock;

    CommentViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      authorTextView.setOnClickListener(view -> {
        Intent intent = new Intent(mActivity, ViewUserDetailActivity.class);
        if (mIsSingleCommentThreadMode) {
          intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
              mVisibleComments.get(getAdapterPosition() - 2).getAuthor());
        } else {
          intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
              mVisibleComments.get(getAdapterPosition() - 1).getAuthor());
        }
        mActivity.startActivity(intent);
      });

      shareButton.setOnClickListener(view -> {
        try {
          Intent intent = new Intent(Intent.ACTION_SEND);
          intent.setType("text/plain");
          String extraText =
              mIsSingleCommentThreadMode ? mVisibleComments.get(getAdapterPosition() - 2)
                  .getPermalink()
                  : mVisibleComments.get(getAdapterPosition() - 1).getPermalink();
          intent.putExtra(Intent.EXTRA_TEXT, extraText);
          mActivity
              .startActivity(Intent.createChooser(intent, mActivity.getString(R.string.share)));
        } catch (ActivityNotFoundException e) {
          Toast.makeText(mActivity, R.string.no_activity_found_for_share, Toast.LENGTH_SHORT)
              .show();
        }
      });

      expandButton.setOnClickListener(view -> {
        int commentPosition =
            mIsSingleCommentThreadMode ? getAdapterPosition() - 2 : getAdapterPosition() - 1;
        if (mVisibleComments.get(commentPosition).isExpanded()) {
          collapseChildren(commentPosition);
          expandButton.setImageResource(R.drawable.ic_expand_more_black_20dp);
        } else {
          expandChildren(commentPosition);
          mVisibleComments.get(commentPosition).setExpanded(true);
          expandButton.setImageResource(R.drawable.ic_expand_less_black_20dp);
        }
      });
    }
  }

  class LoadMoreChildCommentsViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.vertical_block_item_load_more_comments)
    View verticalBlock;
    @BindView(R.id.placeholder_text_view_item_load_more_comments)
    TextView placeholderTextView;

    LoadMoreChildCommentsViewHolder(View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      placeholderTextView.setOnClickListener(view -> {
        int commentPosition =
            mIsSingleCommentThreadMode ? getAdapterPosition() - 2 : getAdapterPosition() - 1;
        int parentPosition = getParentPosition(commentPosition);
        CommentData parentComment = mVisibleComments.get(parentPosition);

        mVisibleComments.get(commentPosition).setLoadingMoreChildren(true);
        mVisibleComments.get(commentPosition).setLoadMoreChildrenFailed(false);
        placeholderTextView.setText(R.string.loading);

        Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
        FetchComment
            .fetchMoreComment(retrofit, mAccessToken, parentComment.getMoreChildrenFullnames(),
                parentComment.getMoreChildrenStartingIndex(), parentComment.getDepth() + 1, mLocale,
                new FetchComment.FetchMoreCommentListener() {
                  @Override
                  public void onFetchMoreCommentSuccess(ArrayList<CommentData> expandedComments,
                      int childrenStartingIndex) {
                    if (mVisibleComments.size() > parentPosition
                        && parentComment.getFullName()
                        .equals(mVisibleComments.get(parentPosition).getFullName())) {
                      if (mVisibleComments.get(parentPosition).isExpanded()) {
                        if (mVisibleComments.get(parentPosition).getChildren().size()
                            > childrenStartingIndex) {
                          mVisibleComments.get(parentPosition)
                              .setMoreChildrenStartingIndex(childrenStartingIndex);
                          mVisibleComments.get(parentPosition).getChildren()
                              .get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                              .setLoadingMoreChildren(false);
                          mVisibleComments.get(parentPosition).getChildren()
                              .get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                              .setLoadMoreChildrenFailed(false);

                          int placeholderPosition = commentPosition;
                          if (mVisibleComments.get(commentPosition).getFullName()
                              .equals(parentComment.getFullName())) {
                            for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                              if (mVisibleComments.get(i).getFullName()
                                  .equals(parentComment.getFullName())) {
                                placeholderPosition = i;
                                break;
                              }
                            }
                          }

                          mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                          mVisibleComments.get(placeholderPosition)
                              .setLoadMoreChildrenFailed(false);
                          placeholderTextView.setText(R.string.comment_load_more_comments);

                          mVisibleComments.addAll(placeholderPosition, expandedComments);
                          if (mIsSingleCommentThreadMode) {
                            notifyItemRangeInserted(placeholderPosition + 2,
                                expandedComments.size());
                          } else {
                            notifyItemRangeInserted(placeholderPosition + 1,
                                expandedComments.size());
                          }
                        } else {
                          mVisibleComments.get(parentPosition).getChildren()
                              .remove(
                                  mVisibleComments.get(parentPosition).getChildren().size() - 1);
                          mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();

                          int placeholderPosition = commentPosition;
                          if (mVisibleComments.get(commentPosition).getFullName()
                              .equals(parentComment.getFullName())) {
                            for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                              if (mVisibleComments.get(i).getFullName()
                                  .equals(parentComment.getFullName())) {
                                placeholderPosition = i;
                                break;
                              }
                            }
                          }

                          mVisibleComments.remove(placeholderPosition);
                          if (mIsSingleCommentThreadMode) {
                            notifyItemRemoved(placeholderPosition + 2);
                          } else {
                            notifyItemRemoved(placeholderPosition + 1);
                          }

                          mVisibleComments.addAll(placeholderPosition, expandedComments);
                          if (mIsSingleCommentThreadMode) {
                            notifyItemRangeInserted(placeholderPosition + 2,
                                expandedComments.size());
                          } else {
                            notifyItemRangeInserted(placeholderPosition + 1,
                                expandedComments.size());
                          }
                        }
                      } else {
                        if (mVisibleComments.get(parentPosition).hasReply()
                            && mVisibleComments.get(parentPosition).getChildren().size()
                            <= childrenStartingIndex) {
                          mVisibleComments.get(parentPosition).getChildren()
                              .remove(
                                  mVisibleComments.get(parentPosition).getChildren().size() - 1);
                          mVisibleComments.get(parentPosition).removeMoreChildrenFullnames();
                        }
                      }

                      mVisibleComments.get(parentPosition).addChildren(expandedComments);
                    } else {
                      for (int i = 0; i < mVisibleComments.size(); i++) {
                        if (mVisibleComments.get(i).getFullName()
                            .equals(parentComment.getFullName())) {
                          if (mVisibleComments.get(i).isExpanded()) {
                            int placeholderPosition =
                                i + mVisibleComments.get(i).getChildren().size();

                            if (!mVisibleComments.get(i).getFullName()
                                .equals(mVisibleComments.get(placeholderPosition).getFullName())) {
                              for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                if (mVisibleComments.get(j).getFullName()
                                    .equals(mVisibleComments.get(i).getFullName())) {
                                  placeholderPosition = j;
                                }
                              }
                            }

                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                            mVisibleComments.get(placeholderPosition)
                                .setLoadMoreChildrenFailed(false);
                            placeholderTextView.setText(R.string.comment_load_more_comments);

                            mVisibleComments.addAll(placeholderPosition, expandedComments);
                            if (mIsSingleCommentThreadMode) {
                              notifyItemRangeInserted(placeholderPosition + 2,
                                  expandedComments.size());
                            } else {
                              notifyItemRangeInserted(placeholderPosition + 1,
                                  expandedComments.size());
                            }
                          }

                          mVisibleComments.get(i).getChildren()
                              .get(mVisibleComments.get(i).getChildren().size() - 1)
                              .setLoadingMoreChildren(false);
                          mVisibleComments.get(i).getChildren()
                              .get(mVisibleComments.get(i).getChildren().size() - 1)
                              .setLoadMoreChildrenFailed(false);
                          mVisibleComments.get(i).addChildren(expandedComments);

                          break;
                        }
                      }
                    }
                  }

                  @Override
                  public void onFetchMoreCommentFailed() {
                    if (parentPosition < mVisibleComments.size()
                        && parentComment.getFullName()
                        .equals(mVisibleComments.get(parentPosition).getFullName())) {
                      if (mVisibleComments.get(parentPosition).isExpanded()) {
                        int commentPosition = mIsSingleCommentThreadMode ? getAdapterPosition() - 2
                            : getAdapterPosition() - 1;
                        int placeholderPosition = commentPosition;
                        if (!mVisibleComments.get(commentPosition).getFullName()
                            .equals(parentComment.getFullName())) {
                          for (int i = parentPosition + 1; i < mVisibleComments.size(); i++) {
                            if (mVisibleComments.get(i).getFullName()
                                .equals(parentComment.getFullName())) {
                              placeholderPosition = i;
                              break;
                            }
                          }
                        }

                        mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                        mVisibleComments.get(placeholderPosition).setLoadMoreChildrenFailed(true);
                        placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                      }

                      mVisibleComments.get(parentPosition).getChildren()
                          .get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                          .setLoadingMoreChildren(false);
                      mVisibleComments.get(parentPosition).getChildren()
                          .get(mVisibleComments.get(parentPosition).getChildren().size() - 1)
                          .setLoadMoreChildrenFailed(true);
                    } else {
                      for (int i = 0; i < mVisibleComments.size(); i++) {
                        if (mVisibleComments.get(i).getFullName()
                            .equals(parentComment.getFullName())) {
                          if (mVisibleComments.get(i).isExpanded()) {
                            int placeholderPosition =
                                i + mVisibleComments.get(i).getChildren().size();
                            if (!mVisibleComments.get(placeholderPosition).getFullName()
                                .equals(mVisibleComments.get(i).getFullName())) {
                              for (int j = i + 1; j < mVisibleComments.size(); j++) {
                                if (mVisibleComments.get(j).getFullName()
                                    .equals(mVisibleComments.get(i).getFullName())) {
                                  placeholderPosition = j;
                                  break;
                                }
                              }
                            }

                            mVisibleComments.get(placeholderPosition).setLoadingMoreChildren(false);
                            mVisibleComments.get(placeholderPosition)
                                .setLoadMoreChildrenFailed(true);
                            placeholderTextView.setText(R.string.comment_load_more_comments_failed);
                          }

                          mVisibleComments.get(i).getChildren()
                              .get(mVisibleComments.get(i).getChildren().size() - 1)
                              .setLoadingMoreChildren(false);
                          mVisibleComments.get(i).getChildren()
                              .get(mVisibleComments.get(i).getChildren().size() - 1)
                              .setLoadMoreChildrenFailed(true);

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
      itemView
          .setOnClickListener(view -> mCommentRecyclerViewAdapterCallback.retryFetchingComments());
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

    @BindView(R.id.error_text_view_item_comment_footer_error)
    TextView errorTextView;
    @BindView(R.id.retry_button_item_comment_footer_error)
    Button retryButton;

    LoadMoreCommentsFailedViewHolder(@NonNull View itemView) {
      super(itemView);
      ButterKnife.bind(this, itemView);

      retryButton.setOnClickListener(
          view -> mCommentRecyclerViewAdapterCallback.retryFetchingMoreComments());
    }
  }

  class ViewAllCommentsViewHolder extends RecyclerView.ViewHolder {

    ViewAllCommentsViewHolder(@NonNull View itemView) {
      super(itemView);

      itemView.setOnClickListener(view -> {
        if (mActivity != null && mActivity instanceof ViewPostDetailActivity) {
          mIsSingleCommentThreadMode = false;
          mSingleCommentId = null;
          ((ViewPostDetailActivity) mActivity).changeToSingleThreadMode();
        }
      });
    }
  }
}
