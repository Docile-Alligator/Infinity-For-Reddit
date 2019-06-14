package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.lsjwzh.widget.materialloadingprogressbar.CircleProgressBar;
import com.multilevelview.MultiLevelRecyclerView;
import com.santalu.aspectratioimageview.AspectRatioImageView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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

import static ml.docilealligator.infinityforreddit.CommentActivity.EXTRA_COMMENT_DATA_KEY;
import static ml.docilealligator.infinityforreddit.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_POST_DATA = "EPD";
    static final String EXTRA_POST_LIST_POSITION = "EPLI";

    private RequestManager mGlide;
    private Locale mLocale;

    private int orientation;
    private static final String ORIENTATION_STATE = "OS";
    private static final String POST_STATE = "PS";
    private static final String IS_REFRESHING_STATE = "IRS";

    private Post mPost;
    private int postListPosition = -1;

    private boolean isLoadingMoreChildren = false;
    private boolean isRefreshing = false;
    private ArrayList<CommentData> mCommentsData;
    private ArrayList<String> children;
    private int mChildrenStartingIndex = 0;

    private CommentMultiLevelRecyclerViewAdapter mAdapter;
    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar_view_post_detail_activity) Toolbar toolbar;
    @BindView(R.id.nested_scroll_view_view_post_detail_activity) NestedScrollView mNestedScrollView;
    @BindView(R.id.subreddit_icon_name_linear_layout_view_post_detail) LinearLayout mSubredditIconNameLinearLayout;
    @BindView(R.id.subreddit_icon_circle_image_view_view_post_detail) AspectRatioGifImageView mSubredditIconGifImageView;
    @BindView(R.id.subreddit_text_view_view_post_detail) TextView mSubredditTextView;
    @BindView(R.id.post_time_text_view_view_post_detail) TextView mPostTimeTextView;
    @BindView(R.id.title_text_view_view_post_detail) TextView mTitleTextView;
    @BindView(R.id.content_markdown_view_view_post_detail) MarkwonView mContentMarkdownView;
    @BindView(R.id.type_text_view_view_post_detail) Chip mTypeChip;
    @BindView(R.id.gilded_image_view_view_post_detail) ImageView mGildedImageView;
    @BindView(R.id.gilded_number_text_view_view_post_detail) TextView mGildedNumberTextView;
    @BindView(R.id.crosspost_image_view_view_post_detail) ImageView mCrosspostImageView;
    @BindView(R.id.nsfw_text_view_view_post_detail) Chip mNSFWChip;
    @BindView(R.id.link_text_view_view_post_detail) TextView linkTextView;
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
    @BindView(R.id.comment_card_view_view_post_detail) MaterialCardView mCommentCardView;
    @BindView(R.id.recycler_view_view_post_detail) MultiLevelRecyclerView mRecyclerView;
    @BindView(R.id.no_comment_wrapper_linear_layout_view_post_detail) LinearLayout mNoCommentWrapperLinearLayout;
    @BindView(R.id.no_comment_image_view_view_post_detail) ImageView mNoCommentImageView;

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

        EventBus.getDefault().register(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mGlide = Glide.with(this);
        mLocale = getResources().getConfiguration().locale;

        if(savedInstanceState == null) {
            orientation = getResources().getConfiguration().orientation;
            mPost = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);
        } else {
            orientation = savedInstanceState.getInt(ORIENTATION_STATE);
            mPost = savedInstanceState.getParcelable(POST_STATE);
            isRefreshing = savedInstanceState.getBoolean(IS_REFRESHING_STATE);

            if(isRefreshing) {
                isRefreshing = false;
                refresh();
            }
        }

        if(getIntent().hasExtra(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getIntent().getExtras().getInt(EXTRA_POST_LIST_POSITION);
        }

        mCommentsData = new ArrayList<>();

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        fetchComment();
        bindView();
        initializeButtonOnClickListener();
    }

    private void bindView() {
        mTitleTextView.setText(mPost.getTitle());

        if(mPost.getSubredditIconUrl() == null) {
            mLoadSubredditIconAsyncTask = new LoadSubredditIconAsyncTask(
                    SubredditRoomDatabase.getDatabase(this).subredditDao(), mPost.getSubredditNamePrefixed().substring(2),
                    iconImageUrl -> {
                        if(!iconImageUrl.equals("")) {
                            mGlide.load(iconImageUrl)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                    .into(mSubredditIconGifImageView);
                        } else {
                            mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                    .into(mSubredditIconGifImageView);
                        }

                        mPost.setSubredditIconUrl(iconImageUrl);
                    });
            mLoadSubredditIconAsyncTask.execute();
        } else if(!mPost.getSubredditIconUrl().equals("")) {
            mGlide.load(mPost.getSubredditIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(mSubredditIconGifImageView);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(mSubredditIconGifImageView);
        }

        switch (mPost.getVoteType()) {
            case 1:
                //Upvote
                mUpvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.backgroundColorPrimaryDark), PorterDuff.Mode.SRC_IN);
                break;
            case -1:
                //Downvote
                mDownvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.minusButtonColor), PorterDuff.Mode.SRC_IN);
                break;
            case 0:
                mUpvoteButton.clearColorFilter();
                mDownvoteButton.clearColorFilter();
        }

        if(mPost.getPostType() != Post.TEXT_TYPE && mPost.getPostType() != Post.NO_PREVIEW_LINK_TYPE) {
            mRelativeLayout.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setRatio((float) mPost.getPreviewHeight() / (float) mPost.getPreviewWidth());
            loadImage();
        } else {
            mRelativeLayout.setVisibility(View.GONE);
            mImageView.setVisibility(View.GONE);
        }

        if(mPost.isCrosspost()) {
            mCrosspostImageView.setVisibility(View.VISIBLE);
        }

        mSubredditTextView.setText(mPost.getSubredditNamePrefixed());

        mPostTimeTextView.setText(mPost.getPostTime());

        if(mPost.getGilded() > 0) {
            mGildedImageView.setVisibility(View.VISIBLE);
            mGlide.load(R.drawable.gold).into(mGildedImageView);
            mGildedNumberTextView.setVisibility(View.VISIBLE);
            String gildedNumber = getResources().getString(R.string.gilded, mPost.getGilded());
            mGildedNumberTextView.setText(gildedNumber);
        }

        if(mPost.isNSFW()) {
            mNSFWChip.setVisibility(View.VISIBLE);
        } else {
            mNSFWChip.setVisibility(View.GONE);
        }

        String scoreWithVote = Integer.toString(mPost.getScore() + mPost.getVoteType());
        mScoreTextView.setText(scoreWithVote);

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

                linkTextView.setVisibility(View.VISIBLE);
                String domain = Uri.parse(mPost.getUrl()).getHost();
                linkTextView.setText(domain);

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

                linkTextView.setVisibility(View.VISIBLE);
                String noPreviewLinkDomain = Uri.parse(mPost.getUrl()).getHost();
                linkTextView.setText(noPreviewLinkDomain);

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
                mTypeChip.setText("TEXT");

                if(!mPost.getSelfText().equals("")) {
                    mContentMarkdownView.setVisibility(View.VISIBLE);
                    mContentMarkdownView.setMarkdown(getCustomSpannableConfiguration(), mPost.getSelfText());
                }
                break;
        }
    }

    private void initializeButtonOnClickListener() {
        mSubredditIconNameLinearLayout.setOnClickListener(view -> {
            Intent intent = new Intent(ViewPostDetailActivity.this, ViewSubredditDetailActivity.class);
            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                    mPost.getSubredditNamePrefixed().substring(2));
            startActivity(intent);
        });

        mShareButton.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String extraText = mPost.getTitle() + "\n" + mPost.getPermalink();
            intent.putExtra(Intent.EXTRA_TEXT, extraText);
            startActivity(Intent.createChooser(intent, "Share"));
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
                mUpvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                //Upvoted before
                mPost.setVoteType(0);
                newVoteType = RedditUtils.DIR_UNVOTE;
                mUpvoteButton.clearColorFilter();
            }

            mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

            if(postListPosition != -1) {
                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
            }

            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                @Override
                public void onVoteThingSuccess() {
                    if(newVoteType.equals(RedditUtils.DIR_UPVOTE)) {
                        mPost.setVoteType(1);
                        mUpvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.backgroundColorPrimaryDark), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        mPost.setVoteType(0);
                        mUpvoteButton.clearColorFilter();
                    }

                    mDownvoteButton.clearColorFilter();
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                    if(postListPosition != -1) {
                        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                    }
                }

                @Override
                public void onVoteThingFail() {
                    Toast.makeText(ViewPostDetailActivity.this, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                    mPost.setVoteType(previousVoteType);
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + previousVoteType));
                    mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                    mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);

                    if(postListPosition != -1) {
                        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                    }
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
                mDownvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                //Upvoted before
                mPost.setVoteType(0);
                newVoteType = RedditUtils.DIR_UNVOTE;
                mDownvoteButton.clearColorFilter();
            }

            mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

            if(postListPosition != -1) {
                EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
            }

            VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                @Override
                public void onVoteThingSuccess() {
                    if(newVoteType.equals(RedditUtils.DIR_DOWNVOTE)) {
                        mPost.setVoteType(-1);
                        mDownvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorAccent), android.graphics.PorterDuff.Mode.SRC_IN);
                    } else {
                        mPost.setVoteType(0);
                        mDownvoteButton.clearColorFilter();
                    }

                    mUpvoteButton.clearColorFilter();
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + mPost.getVoteType()));

                    if(postListPosition != -1) {
                        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                    }
                }

                @Override
                public void onVoteThingFail() {
                    Toast.makeText(ViewPostDetailActivity.this, R.string.vote_failed, Toast.LENGTH_SHORT).show();
                    mPost.setVoteType(previousVoteType);
                    mScoreTextView.setText(Integer.toString(mPost.getScore() + previousVoteType));
                    mUpvoteButton.setColorFilter(previousUpvoteButtonColorFilter);
                    mDownvoteButton.setColorFilter(previousDownvoteButtonColorFilter);

                    if(postListPosition != -1) {
                        EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                    }
                }
            }, mPost.getFullName(), newVoteType);
        });
    }

    private void fetchComment() {
        mCommentCardView.setVisibility(View.GONE);
        mCommentProgressbar.setVisibility(View.VISIBLE);
        mNoCommentWrapperLinearLayout.setVisibility(View.GONE);

        FetchComment.fetchComment(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getId(),
                null, mLocale, true, 0, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(List<?> commentsData,
                                                      String parentId, ArrayList<String> children) {
                        ViewPostDetailActivity.this.children = children;
                        mCommentProgressbar.setVisibility(View.GONE);
                        mCommentsData.addAll((ArrayList<CommentData>) commentsData);

                        if (mCommentsData.size() > 0) {
                            if(mAdapter == null) {
                                mAdapter = new CommentMultiLevelRecyclerViewAdapter(
                                        ViewPostDetailActivity.this, mRetrofit, mOauthRetrofit,
                                        mSharedPreferences, mCommentsData,
                                        mRecyclerView, mPost.getSubredditNamePrefixed(),
                                        mPost.getId(), mLocale);
                                mRecyclerView.setAdapter(mAdapter);
                                mRecyclerView.removeItemClickListeners();
                                mRecyclerView.setToggleItemOnClick(false);
                                mRecyclerView.setAccordion(false);

                                mNestedScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                                    if(!isLoadingMoreChildren) {
                                        View view = mNestedScrollView.getChildAt(mNestedScrollView.getChildCount() - 1);
                                        int diff = view.getBottom() - (mNestedScrollView.getHeight() +
                                                mNestedScrollView.getScrollY());
                                        if(diff == 0) {
                                            fetchMoreComment(mChildrenStartingIndex);
                                        }
                                    }
                                });
                            } else {
                                mAdapter.notifyDataSetChanged();
                            }

                            mCommentCardView.setVisibility(View.VISIBLE);
                        } else {
                            mNoCommentWrapperLinearLayout.setVisibility(View.VISIBLE);
                            mGlide.load(R.drawable.no_comment_indicator).into(mNoCommentImageView);
                        }
                    }

                    @Override
                    public void onFetchCommentFailed() {
                        mCommentProgressbar.setVisibility(View.GONE);
                        showRetrySnackbar();
                    }
                });
    }

    private void fetchMoreComment(int startingIndex) {
        isLoadingMoreChildren = true;
        FetchComment.fetchMoreComment(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getFullName(),
                children, startingIndex, mLocale, new FetchComment.FetchMoreCommentListener() {
                    @Override
                    public void onFetchMoreCommentSuccess(List<?> commentsData, int childrenStartingIndex) {
                        mAdapter.addComments((ArrayList<CommentData>) commentsData);
                        mChildrenStartingIndex = childrenStartingIndex;
                        isLoadingMoreChildren = false;
                    }

                    @Override
                    public void onFetchMoreCommentFailed() {
                        isLoadingMoreChildren = false;
                        Snackbar snackbar = Snackbar.make(mCoordinatorLayout, R.string.load_more_comment_failed, Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction(R.string.retry, view -> fetchMoreComment(startingIndex));
                        snackbar.show();
                    }
                });
    }

    private void loadImage() {
        RequestBuilder imageRequestBuilder = mGlide.load(mPost.getPreviewUrl())
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
            if(link.startsWith("/u/") || link.startsWith("u/")) {
                Intent intent = new Intent(ViewPostDetailActivity.this, ViewUserDetailActivity.class);
                intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, link.substring(3));
                startActivity(intent);
            } else if(link.startsWith("/r/") || link.startsWith("r/")) {
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
        snackbar.setAction(R.string.retry, view -> fetchComment());
        snackbar.show();
    }

    private void refresh() {
        if(!isRefreshing) {
            isRefreshing = true;

            if(mAdapter != null && mCommentsData != null) {
                int size = mCommentsData.size();
                mCommentsData.clear();
                mAdapter.notifyItemRangeRemoved(0, size);
            }

            fetchComment();

            String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
            FetchPost.fetchPost(mOauthRetrofit, mPost.getId(), accessToken, mLocale,
                    new FetchPost.FetchPostListener() {
                        @Override
                        public void fetchPostSuccess(Post post) {
                            mPost = post;
                            bindView();
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                            isRefreshing = false;
                        }

                        @Override
                        public void fetchPostFailed() {
                            Snackbar.make(mCoordinatorLayout, R.string.refresh_post_failed, Snackbar.LENGTH_SHORT);
                            isRefreshing = false;
                        }
                    });
        }
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToDetailActivity event) {
        if(mPost.getId().equals(event.postId)) {
            mPost.setVoteType(event.voteType);
            mScoreTextView.setText(Integer.toString(mPost.getScore() + event.voteType));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_post_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_view_post_detail_activity:
                refresh();
                return true;
            case R.id.action_comment_view_post_detail_activity:
                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, mPost.getTitle());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data != null && resultCode == RESULT_OK && requestCode == WRITE_COMMENT_REQUEST_CODE) {
            if(data.hasExtra(EXTRA_COMMENT_DATA_KEY)) {
                CommentData comment = data.getExtras().getParcelable(EXTRA_COMMENT_DATA_KEY);
                if(comment.getDepth() == 0) {
                    mAdapter.addComment(comment);
                } else {
                    int parentPosition = data.getExtras().getInt(CommentActivity.EXTRA_PARENT_POSITION_KEY);
                    mAdapter.addChildComment(comment, parentPosition);
                }
            } else {
                Toast.makeText(this, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ORIENTATION_STATE, orientation);
        outState.putParcelable(POST_STATE, mPost);
        outState.putBoolean(IS_REFRESHING_STATE, isRefreshing);
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
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        if(mLoadSubredditIconAsyncTask != null) {
            mLoadSubredditIconAsyncTask.cancel(true);
        }
    }
}
