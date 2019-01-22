package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.chip.Chip;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;
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
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.multilevelview.MultiLevelRecyclerView;
import com.santalu.aspectratioimageview.AspectRatioImageView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import CustomView.AspectRatioGifImageView;
import SubredditDatabase.SubredditRoomDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import retrofit2.Retrofit;
import ru.noties.markwon.SpannableConfiguration;
import ru.noties.markwon.view.MarkwonView;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_POST_DATA = "EPD";

    private RequestManager glide;

    private int orientation;
    private String orientationState = "OS";

    private int mMoreCommentCount;
    private Post mPost;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.subreddit_icon_name_linear_layout_view_post_detail) LinearLayout mSubredditIconNameLinearLayout;
    @BindView(R.id.subreddit_icon_circle_image_view_view_post_detail) AspectRatioGifImageView mSubredditIconGifImageView;
    @BindView(R.id.subreddit_text_view_view_post_detail) TextView mSubredditTextView;
    @BindView(R.id.post_time_text_view_view_post_detail) TextView mPostTimeTextView;
    @BindView(R.id.content_markdown_view_view_post_detail) MarkwonView mContentMarkdownView;
    @BindView(R.id.type_text_view_view_post_detail) Chip mTypeChip;
    @BindView(R.id.gilded_image_view_view_post_detail) ImageView mGildedImageView;
    @BindView(R.id.gilded_number_text_view_view_post_detail) TextView mGildedNumberTextView;
    @BindView(R.id.crosspost_image_view_view_post_detail) ImageView mCrosspostImageView;
    @BindView(R.id.nsfw_text_view_view_post_detail) Chip mNSFWChip;
    @BindView(R.id.image_view_wrapper_view_post_detail) RelativeLayout mRelativeLayout;

    @BindView(R.id.load_wrapper_view_post_detail) RelativeLayout mLoadWrapper;
    @BindView(R.id.progress_bar_view_post_detail) ProgressBar mLoadImageProgressBar;
    @BindView(R.id.load_image_error_text_view_view_post_detail) TextView mLoadImageErrorTextView;
    @BindView(R.id.image_view_view_post_detail) AspectRatioImageView mImageView;
    @BindView(R.id.image_view_no_preview_link_view_post_detail) ImageView mNoPreviewLinkImageView;

    @BindView(R.id.plus_button_view_post_detail) ImageView mUpvoteButton;
    @BindView(R.id.score_text_view_view_post_detail) TextView mScoreTextView;
    @BindView(R.id.minus_button_view_post_detail) ImageView mDownvoteButton;
    @BindView(R.id.share_button_view_post_detail) ImageView mShareButton;

    @BindView(R.id.comment_progress_bar_view_post_detail) CircleProgressBar mCommentProgressbar;
    @BindView(R.id.comment_card_view_view_post_detail) CardView mCommentCardView;
    @BindView(R.id.recycler_view_view_post_detail) MultiLevelRecyclerView mRecyclerView;

    @BindView(R.id.no_comment_wrapper_linear_layout_view_post_detail) LinearLayout mNoCommentWrapperLinearLayout;
    @BindView(R.id.no_comment_image_view_view_post_detail) ImageView mNoCommentImageView;

    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject @Named("auth_info")
    SharedPreferences mSharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_detail);
        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmNetworkComponent().inject(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        orientation = getResources().getConfiguration().orientation;

        mPost = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);

        TextView titleTextView = findViewById(R.id.title_text_view_view_post_detail);
        titleTextView.setText(mPost.getTitle());

        glide = Glide.with(this);
        if(mPost.getSubredditIconUrl() == null) {
            mLoadSubredditIconAsyncTask = new LoadSubredditIconAsyncTask(
                    SubredditRoomDatabase.getDatabase(this).subredditDao(), mPost.getSubredditNamePrefixed().substring(2),
                    iconImageUrl -> {
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
                                                mSubredditIconGifImageView.startAnimation();
                                            }
                                            return false;
                                        }
                                    })
                                    .into(mSubredditIconGifImageView);
                        } else {
                            glide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(mSubredditIconGifImageView);
                        }

                        mPost.setSubredditIconUrl(iconImageUrl);
                    });
            mLoadSubredditIconAsyncTask.execute();
        } else if(!mPost.getSubredditIconUrl().equals("")) {
            glide.load(mPost.getSubredditIconUrl())
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
                                mSubredditIconGifImageView.startAnimation();
                            }
                            return false;
                        }
                    })
                    .into(mSubredditIconGifImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(mSubredditIconGifImageView);
        }

        switch (mPost.getVoteType()) {
            case 1:
                //Upvote
                mUpvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                break;
            case -1:
                //Downvote
                mDownvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.minusButtonColor), PorterDuff.Mode.SRC_IN);
                break;
        }

        if(mPost.getPostType() != Post.TEXT_TYPE && mPost.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
            mRelativeLayout.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
            loadImage();
        }

        if(mPost.isCrosspost()) {
            mCrosspostImageView.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mSubredditTextView.setText(mPost.getSubredditNamePrefixed());
        mSubredditIconNameLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(ViewPostDetailActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                    mPost.getSubredditNamePrefixed().substring(2));
            startActivity(intent);
        });

        mPostTimeTextView.setText(mPost.getPostTime());

        if(mPost.getGilded() > 0) {
            mGildedImageView.setVisibility(View.VISIBLE);
            glide.load(R.drawable.gold).into(mGildedImageView);
            mGildedNumberTextView.setVisibility(View.VISIBLE);
            String gildedNumber = getResources().getString(R.string.gilded, mPost.getGilded());
            mGildedNumberTextView.setText(gildedNumber);
        }

        if(mPost.isNSFW()) {
            mNSFWChip.setVisibility(View.VISIBLE);
        }
        mScoreTextView.setText(Integer.toString(mPost.getScore()));

        mShareButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String extraText = mPost.getTitle() + "\n" + mPost.getPermalink();
            intent.putExtra(Intent.EXTRA_TEXT, extraText);
            startActivity(Intent.createChooser(intent, "Share"));
        });

        switch (mPost.getPostType()) {
            case Post.IMAGE_TYPE:
                mTypeChip.setText("IMAGE");
                mImageView.setOnClickListener(view -> {
                    Intent intent = new Intent(ViewPostDetailActivity.this, ViewImageActivity.class);
                    intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, mPost.getUrl());
                    intent.putExtra(ViewImageActivity.TITLE_KEY, mPost.getTitle());
                    intent.putExtra(ViewImageActivity.FILE_NAME_KEY, mPost.getSubredditNamePrefixed().substring(2)
                            + "-" + mPost.getId().substring(3));
                    startActivity(intent);
                });
                break;
            case Post.LINK_TYPE:
                mTypeChip.setText("LINK");

                mImageView.setOnClickListener(view -> {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    // add share action to menu list
                    builder.addDefaultShareMenuItem();
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(mPost.getUrl()));
                });
                break;
            case Post.GIF_VIDEO_TYPE:
                mTypeChip.setText("GIF");

                final Uri gifVideoUri = Uri.parse(mPost.getVideoUrl());
                mImageView.setOnClickListener(view -> {
                    Intent intent = new Intent(ViewPostDetailActivity.this, ViewVideoActivity.class);
                    intent.setData(gifVideoUri);
                    intent.putExtra(ViewVideoActivity.TITLE_KEY, mPost.getTitle());
                    intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPost.isDashVideo());
                    intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
                    if(mPost.isDownloadableGifOrVideo()) {
                        intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
                        intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditNamePrefixed());
                        intent.putExtra(ViewVideoActivity.ID_KEY, mPost.getId());
                    }
                    startActivity(intent);
                });
                break;
            case Post.VIDEO_TYPE:
                mTypeChip.setText("VIDEO");

                final Uri videoUri = Uri.parse(mPost.getVideoUrl());
                mImageView.setOnClickListener(view -> {
                    Intent intent = new Intent(ViewPostDetailActivity.this, ViewVideoActivity.class);
                    intent.setData(videoUri);
                    intent.putExtra(ViewVideoActivity.TITLE_KEY, mPost.getTitle());
                    intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPost.isDashVideo());
                    intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPost.isDownloadableGifOrVideo());
                    if(mPost.isDownloadableGifOrVideo()) {
                        intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPost.getGifOrVideoDownloadUrl());
                        intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPost.getSubredditNamePrefixed());
                        intent.putExtra(ViewVideoActivity.ID_KEY, mPost.getId());
                    }
                    startActivity(intent);
                });
                break;
            case Post.NO_PREVIEW_LINK_TYPE:
                mTypeChip.setText("LINK");
                if(!mPost.getSelfText().equals("")) {
                    mContentMarkdownView.setVisibility(View.VISIBLE);
                    mContentMarkdownView.setMarkdown(getCustomSpannableConfiguration(), mPost.getSelfText());
                }
                mNoPreviewLinkImageView.setVisibility(View.VISIBLE);
                mNoPreviewLinkImageView.setOnClickListener(view -> {
                    CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                    // add share action to menu list
                    builder.addDefaultShareMenuItem();
                    builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                    CustomTabsIntent customTabsIntent = builder.build();
                    customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(mPost.getUrl()));
                });
                break;
            case Post.TEXT_TYPE:
                mTypeChip.setVisibility(View.GONE);
                if(!mPost.getSelfText().equals("")) {
                    mContentMarkdownView.setVisibility(View.VISIBLE);
                    mContentMarkdownView.setMarkdown(getCustomSpannableConfiguration(), mPost.getSelfText());
                }
                break;
        }
        queryComment();

        mUpvoteButton.setOnClickListener(view -> {
            final boolean isDownvotedBefore = mDownvoteButton.getColorFilter() != null;

            final ColorFilter downVoteButtonColorFilter = mDownvoteButton.getColorFilter();
            mDownvoteButton.clearColorFilter();

            if (mUpvoteButton.getColorFilter() == null) {
                mUpvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                if(isDownvotedBefore) {
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + 2));
                } else {
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + 1));
                }

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        mPost.setVoteType(1);
                        if(isDownvotedBefore) {
                            mPost.setScore(mPost.getScore() + 2);
                        } else {
                            mPost.setScore(mPost.getScore() + 1);
                        }
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(ViewPostDetailActivity.this, "Cannot upvote this post", Toast.LENGTH_SHORT).show();
                        mUpvoteButton.clearColorFilter();
                        mScoreTextView.setText(Integer.toString(mPost.getScore()));
                        mDownvoteButton.setColorFilter(downVoteButtonColorFilter);
                    }
                }, mPost.getFullName(), RedditUtils.DIR_UPVOTE);
            } else {
                //Upvoted before
                mUpvoteButton.clearColorFilter();
                mScoreTextView.setText(Integer.toString(mPost.getScore() - 1));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        mPost.setVoteType(0);
                        mPost.setScore(mPost.getScore() - 1);
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                        mScoreTextView.setText(Integer.toString(mPost.getScore() + 1));
                        mUpvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
                        mPost.setScore(mPost.getScore() + 1);
                    }
                }, mPost.getFullName(), RedditUtils.DIR_UNVOTE);
            }
        });

        mDownvoteButton.setOnClickListener(view -> {
            final boolean isUpvotedBefore = mUpvoteButton.getColorFilter() != null;

            final ColorFilter upvoteButtonColorFilter = mUpvoteButton.getColorFilter();
            mUpvoteButton.clearColorFilter();

            if (mDownvoteButton.getColorFilter() == null) {
                mDownvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), PorterDuff.Mode.SRC_IN);
                if (isUpvotedBefore) {
                    mScoreTextView.setText(Integer.toString(mPost.getScore() - 2));
                } else {
                    mScoreTextView.setText(Integer.toString(mPost.getScore() - 1));
                }

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        mPost.setVoteType(-1);
                        if(isUpvotedBefore) {
                            mPost.setScore(mPost.getScore() - 2);
                        } else {
                            mPost.setScore(mPost.getScore() - 1);
                        }
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(ViewPostDetailActivity.this, "Cannot downvote this post", Toast.LENGTH_SHORT).show();
                        mDownvoteButton.clearColorFilter();
                        mScoreTextView.setText(Integer.toString(mPost.getScore()));
                        mUpvoteButton.setColorFilter(upvoteButtonColorFilter);
                    }
                }, mPost.getFullName(), RedditUtils.DIR_DOWNVOTE);
            } else {
                //Down voted before
                mDownvoteButton.clearColorFilter();
                mScoreTextView.setText(Integer.toString(mPost.getScore() + 1));

                VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                    @Override
                    public void onVoteThingSuccess() {
                        mPost.setVoteType(0);
                        mPost.setScore(mPost.getScore());
                    }

                    @Override
                    public void onVoteThingFail() {
                        Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                        mDownvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), PorterDuff.Mode.SRC_IN);
                        mScoreTextView.setText(Integer.toString(mPost.getScore()));
                        mPost.setScore(mPost.getScore());
                    }
                }, mPost.getFullName(), RedditUtils.DIR_UNVOTE);
            }
        });
    }

    private void queryComment() {
        mCommentProgressbar.setVisibility(View.VISIBLE);
        mNoCommentWrapperLinearLayout.setVisibility(View.GONE);
        FetchComment.fetchComment(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getId(),
                null, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(String response) {
                        ParseComment.parseComment(response, new ArrayList<CommentData>(),
                                getResources().getConfiguration().locale, true, 0,
                                new ParseComment.ParseCommentListener() {
                                    @Override
                                    public void onParseCommentSuccess(List<?> commentData, int moreCommentCount) {
                                        mCommentProgressbar.setVisibility(View.GONE);
                                        mMoreCommentCount = moreCommentCount;
                                        if (commentData.size() > 0) {
                                            CommentMultiLevelRecyclerViewAdapter adapter = new CommentMultiLevelRecyclerViewAdapter(
                                                    ViewPostDetailActivity.this, mRetrofit, mOauthRetrofit,
                                                    mSharedPreferences, (ArrayList<CommentData>) commentData,
                                                    mRecyclerView, mPost.getSubredditNamePrefixed(),
                                                    mPost.getId(), getResources().getConfiguration().locale);
                                            mRecyclerView.removeItemClickListeners();
                                            mRecyclerView.setToggleItemOnClick(false);
                                            mRecyclerView.setAccordion(false);
                                            mRecyclerView.setAdapter(adapter);
                                            mCommentCardView.setVisibility(View.VISIBLE);
                                        } else {
                                            mNoCommentWrapperLinearLayout.setVisibility(View.VISIBLE);
                                            glide.load(R.drawable.no_comment_indicator).into(mNoCommentImageView);
                                        }
                                    }

                                    @Override
                                    public void onParseCommentFail() {
                                        mCommentProgressbar.setVisibility(View.GONE);
                                        showRetrySnackbar();
                                    }
                                });
                    }

                    @Override
                    public void onFetchCommentFail() {
                        mCommentProgressbar.setVisibility(View.GONE);
                        showRetrySnackbar();
                    }
                });
    }

    private void loadImage() {
        RequestBuilder imageRequestBuilder = glide.load(mPost.getPreviewUrl())
                .apply(new RequestOptions().override(mPost.getPreviewWidth(), mPost.getPreviewHeight()))
                .listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                mLoadImageProgressBar.setVisibility(View.GONE);
                mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                mLoadImageErrorTextView.setOnClickListener(view -> {
                    mLoadImageProgressBar.setVisibility(View.VISIBLE);
                    mLoadImageErrorTextView.setVisibility(View.GONE);
                    loadImage();
                });
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                mLoadWrapper.setVisibility(View.GONE);
                return false;
            }
        });

        if(mPost.isNSFW()) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 2)))
                    .into(mImageView);
        } else {
            imageRequestBuilder.into(mImageView);
        }
    }

    private SpannableConfiguration getCustomSpannableConfiguration() {
        return SpannableConfiguration.builder(this).linkResolver((view, link) -> {
            if(link.startsWith("/u/")) {
                Intent intent = new Intent(ViewPostDetailActivity.this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                startActivity(intent);
            } else if(link.startsWith("/r/")) {
                Intent intent = new Intent(ViewPostDetailActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, link.substring(3));
                startActivity(intent);
            } else {
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                // add share action to menu list
                builder.addDefaultShareMenuItem();
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(link));
            }
        }).build();
    }

    private void showRetrySnackbar() {
        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.load_comment_failed, Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                queryComment();
            }
        });
        snackbar.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(orientationState, orientation);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        orientation = savedInstanceState.getInt(orientationState);
    }

    @Override
    public void onBackPressed() {
        if(orientation == getResources().getConfiguration().orientation) {
            super.onBackPressed();
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mLoadSubredditIconAsyncTask != null) {
            mLoadSubredditIconAsyncTask.cancel(true);
        }
    }
}
