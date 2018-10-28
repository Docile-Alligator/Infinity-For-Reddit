package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.ColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
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
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.multilevelview.MultiLevelRecyclerView;
import com.santalu.aspectratioimageview.AspectRatioImageView;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import jp.wasabeef.glide.transformations.BlurTransformation;
import retrofit2.Retrofit;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_POST_DATA = "EPD";

    private int orientation;
    private String orientationState = "OS";

    private int mMoreCommentCount;
    private PostData mPostData;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.subreddit_icon_circle_image_view_view_post_detail) CircleImageView mSubredditIconCircleImageView;
    @BindView(R.id.post_time_text_view_view_post_detail) TextView mPostTimeTextView;
    @BindView(R.id.subreddit_text_view_view_post_detail) TextView mSubredditTextView;
    @BindView(R.id.content_html_text_view_view_post_detail) HtmlTextView mContentTextView;
    @BindView(R.id.type_text_view_view_post_detail) TextView mTypeTextView;
    @BindView(R.id.gilded_image_view_view_post_detail) ImageView mGildedImageView;
    @BindView(R.id.gilded_number_text_view_view_post_detail) TextView mGildedNumberTextView;
    @BindView(R.id.crosspost_image_view_view_post_detail) ImageView mCrosspostImageView;
    @BindView(R.id.nsfw_text_view_view_post_detail) TextView mNSFWTextView;
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

    @BindView(R.id.comment_progress_bar_view_post_detail) ProgressBar mCommentProgressbar;
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

        mPostData = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);

        TextView titleTextView = findViewById(R.id.title_text_view_view_post_detail);
        titleTextView.setText(mPostData.getTitle());

        if(mPostData.getSubredditIconUrl() == null) {
            mLoadSubredditIconAsyncTask = new LoadSubredditIconAsyncTask(
                    SubredditRoomDatabase.getDatabase(this).subredditDao(), mPostData.getSubredditNamePrefixed(),
                    new LoadSubredditIconAsyncTask.LoadSubredditIconAsyncTaskListener() {
                @Override
                public void loadIconSuccess(String iconImageUrl) {
                    if(!iconImageUrl.equals("")) {
                        Glide.with(ViewPostDetailActivity.this).load(iconImageUrl)
                                .into(mSubredditIconCircleImageView);
                    } else {
                        Glide.with(ViewPostDetailActivity.this).load(R.drawable.subreddit_default_icon)
                                .into(mSubredditIconCircleImageView);
                    }

                    mPostData.setSubredditIconUrl(iconImageUrl);
                }
            });
            mLoadSubredditIconAsyncTask.execute();
        } else if(!mPostData.getSubredditIconUrl().equals("")) {
            Glide.with(this).load(mPostData.getSubredditIconUrl()).into(mSubredditIconCircleImageView);
        } else {
            Glide.with(this).load(R.drawable.subreddit_default_icon).into(mSubredditIconCircleImageView);
        }

        mSubredditIconCircleImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPostDetailActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                        mPostData.getSubredditNamePrefixed().substring(2));
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_VALUE_KEY,
                        mPostData.getSubredditNamePrefixed());
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_QUERY_BY_ID_KEY, false);
                startActivity(intent);
            }
        });

        switch (mPostData.getVoteType()) {
            case 1:
                //Upvote
                mUpvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            case -1:
                //Downvote
                mDownvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
        }

        if(mPostData.getPostType() != PostData.TEXT_TYPE && mPostData.getPostType() != PostData.NO_PREVIEW_LINK_TYPE) {
            mRelativeLayout.setVisibility(View.VISIBLE);
            mImageView.setVisibility(View.VISIBLE);
            mImageView.setRatio((float) mPostData.getPreviewHeight() / (float) mPostData.getPreviewWidth());
            loadImage();
        }

        if(mPostData.isCrosspost()) {
            mCrosspostImageView.setVisibility(View.VISIBLE);
        }

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        mSubredditTextView.setText(mPostData.getSubredditNamePrefixed());
        mSubredditTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewPostDetailActivity.this, ViewSubredditDetailActivity.class);
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY,
                        mPostData.getSubredditNamePrefixed().substring(2));
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_VALUE_KEY,
                        mPostData.getSubredditNamePrefixed());
                intent.putExtra(ViewSubredditDetailActivity.EXTRA_QUERY_BY_ID_KEY, false);
                startActivity(intent);
            }
        });

        mPostTimeTextView.setText(mPostData.getPostTime());

        if(mPostData.getGilded() > 0) {
            mGildedImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.gold).into(mGildedImageView);
            mGildedNumberTextView.setVisibility(View.VISIBLE);
            String gildedNumber = getResources().getString(R.string.gilded, mPostData.getGilded());
            mGildedNumberTextView.setText(gildedNumber);
        }

        if(mPostData.isNSFW()) {
            mNSFWTextView.setVisibility(View.VISIBLE);
        }
        mScoreTextView.setText(Integer.toString(mPostData.getScore()));

        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                String extraText = mPostData.getTitle() + "\n" + mPostData.getPermalink();
                intent.putExtra(Intent.EXTRA_TEXT, extraText);
                startActivity(Intent.createChooser(intent, "Share"));
            }
        });

        switch (mPostData.getPostType()) {
            case PostData.IMAGE_TYPE:
                mTypeTextView.setText("IMAGE");
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewPostDetailActivity.this, ViewImageActivity.class);
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, mPostData.getUrl());
                        intent.putExtra(ViewImageActivity.TITLE_KEY, mPostData.getTitle());
                        intent.putExtra(ViewImageActivity.FILE_NAME_KEY, mPostData.getSubredditNamePrefixed().substring(2)
                                + "-" + mPostData.getId().substring(3));
                        startActivity(intent);
                    }
                });
                break;
            case PostData.LINK_TYPE:
                mTypeTextView.setText("LINK");

                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        // add share action to menu list
                        builder.addDefaultShareMenuItem();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(mPostData.getUrl()));
                    }
                });
                break;
            case PostData.GIF_VIDEO_TYPE:
                mTypeTextView.setText("GIF");

                final Uri gifVideoUri = Uri.parse(mPostData.getVideoUrl());
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewPostDetailActivity.this, ViewVideoActivity.class);
                        intent.setData(gifVideoUri);
                        intent.putExtra(ViewVideoActivity.TITLE_KEY, mPostData.getTitle());
                        intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPostData.isDashVideo());
                        intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPostData.isDownloadableGifOrVideo());
                        if(mPostData.isDownloadableGifOrVideo()) {
                            intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPostData.getGifOrVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPostData.getSubredditNamePrefixed());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPostData.getId());
                        }
                        startActivity(intent);
                    }
                });
                break;
            case PostData.VIDEO_TYPE:
                mTypeTextView.setText("VIDEO");

                final Uri videoUri = Uri.parse(mPostData.getVideoUrl());
                mImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ViewPostDetailActivity.this, ViewVideoActivity.class);
                        intent.setData(videoUri);
                        intent.putExtra(ViewVideoActivity.TITLE_KEY, mPostData.getTitle());
                        intent.putExtra(ViewVideoActivity.IS_DASH_VIDEO_KEY, mPostData.isDashVideo());
                        intent.putExtra(ViewVideoActivity.IS_DOWNLOADABLE_KEY, mPostData.isDownloadableGifOrVideo());
                        if(mPostData.isDownloadableGifOrVideo()) {
                            intent.putExtra(ViewVideoActivity.DOWNLOAD_URL_KEY, mPostData.getGifOrVideoDownloadUrl());
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPostData.getSubredditNamePrefixed());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPostData.getId());
                        }
                        startActivity(intent);
                    }
                });
                break;
            case PostData.NO_PREVIEW_LINK_TYPE:
                mTypeTextView.setText("LINK");
                if(!mPostData.getSelfText().equals("")) {
                    mContentTextView.setVisibility(View.VISIBLE);
                    mContentTextView.setHtml(mPostData.getSelfText());
                }
                mNoPreviewLinkImageView.setVisibility(View.VISIBLE);
                mNoPreviewLinkImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        // add share action to menu list
                        builder.addDefaultShareMenuItem();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(mPostData.getUrl()));
                    }
                });
                break;
            case PostData.TEXT_TYPE:
                mTypeTextView.setText("TEXT");
                if(!mPostData.getSelfText().equals("")) {
                    mContentTextView.setVisibility(View.VISIBLE);
                    mContentTextView.setHtml(mPostData.getSelfText());
                }
                break;
        }
        queryComment();

        /*final Observable<Integer> observable = Observable.create(
                new ObservableOnSubscribe<Integer>() {
                    @Override
                    public void subscribe(ObservableEmitter<Integer> emitter) throws Exception {
                        emitter.onNext(mPostData.getVoteType());
                        emitter.onComplete();
                        Log.i("asdasdf", "adasdfasdf");
                        Toast.makeText(ViewPostDetailActivity.this, "observable", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        final Observer observer = new Observer() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Object o) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                Toast.makeText(ViewPostDetailActivity.this, "complete", Toast.LENGTH_SHORT).show();
            }
        };*/

        mUpvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //observable.subscribe(observer);
                final boolean isDownvotedBefore = mDownvoteButton.getColorFilter() != null;

                final ColorFilter downVoteButtonColorFilter = mDownvoteButton.getColorFilter();
                mDownvoteButton.clearColorFilter();

                if (mUpvoteButton.getColorFilter() == null) {
                    mUpvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    if(isDownvotedBefore) {
                        mScoreTextView.setText(Integer.toString(mPostData.getScore() + 2));
                    } else {
                        mScoreTextView.setText(Integer.toString(mPostData.getScore() + 1));
                    }

                    VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(1);
                            if(isDownvotedBefore) {
                                mPostData.setScore(mPostData.getScore() + 2);
                            } else {
                                mPostData.setScore(mPostData.getScore() + 1);
                            }
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot upvote this post", Toast.LENGTH_SHORT).show();
                            mUpvoteButton.clearColorFilter();
                            mScoreTextView.setText(Integer.toString(mPostData.getScore()));
                            mDownvoteButton.setColorFilter(downVoteButtonColorFilter);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UPVOTE);
                } else {
                    //Upvoted before
                    mUpvoteButton.clearColorFilter();
                    mScoreTextView.setText(Integer.toString(mPostData.getScore() - 1));

                    VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(0);
                            mPostData.setScore(mPostData.getScore() - 1);
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                            mScoreTextView.setText(Integer.toString(mPostData.getScore() + 1));
                            mUpvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                            mPostData.setScore(mPostData.getScore() + 1);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UNVOTE);
                }
            }
        });

        mDownvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //observable.subscribe(observer);
                final boolean isUpvotedBefore = mUpvoteButton.getColorFilter() != null;

                final ColorFilter upvoteButtonColorFilter = mUpvoteButton.getColorFilter();
                mUpvoteButton.clearColorFilter();

                if (mDownvoteButton.getColorFilter() == null) {
                    mDownvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    if (isUpvotedBefore) {
                        mScoreTextView.setText(Integer.toString(mPostData.getScore() - 2));
                    } else {
                        mScoreTextView.setText(Integer.toString(mPostData.getScore() - 1));
                    }

                    VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(-1);
                            if(isUpvotedBefore) {
                                mPostData.setScore(mPostData.getScore() - 2);
                            } else {
                                mPostData.setScore(mPostData.getScore() - 1);
                            }
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot downvote this post", Toast.LENGTH_SHORT).show();
                            mDownvoteButton.clearColorFilter();
                            mScoreTextView.setText(Integer.toString(mPostData.getScore()));
                            mUpvoteButton.setColorFilter(upvoteButtonColorFilter);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_DOWNVOTE);
                } else {
                    //Down voted before
                    mDownvoteButton.clearColorFilter();
                    mScoreTextView.setText(Integer.toString(mPostData.getScore() + 1));

                    VoteThing.voteThing(mOauthRetrofit, mSharedPreferences, new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(0);
                            mPostData.setScore(mPostData.getScore());
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                            mDownvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                            mScoreTextView.setText(Integer.toString(mPostData.getScore()));
                            mPostData.setScore(mPostData.getScore());
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UNVOTE);
                }
            }
        });
    }

    private void queryComment() {
        mCommentProgressbar.setVisibility(View.VISIBLE);
        mNoCommentWrapperLinearLayout.setVisibility(View.GONE);
        FetchComment.fetchComment(mRetrofit, mPostData.getSubredditNamePrefixed(), mPostData.getId(),
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
                                                    mRecyclerView, mPostData.getSubredditNamePrefixed(),
                                                    mPostData.getId(), getResources().getConfiguration().locale);
                                            mRecyclerView.removeItemClickListeners();
                                            mRecyclerView.setToggleItemOnClick(false);
                                            mRecyclerView.setAccordion(false);
                                            mRecyclerView.setAdapter(adapter);
                                            mCommentCardView.setVisibility(View.VISIBLE);
                                        } else {
                                            mNoCommentWrapperLinearLayout.setVisibility(View.VISIBLE);
                                            Glide.with(ViewPostDetailActivity.this).load(R.drawable.no_comment_indicator).into(mNoCommentImageView);
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
        RequestBuilder imageRequestBuilder = Glide.with(this).load(mPostData.getPreviewUrl())
                .apply(new RequestOptions().override(mPostData.getPreviewWidth(), mPostData.getPreviewHeight()))
                .listener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                mLoadImageProgressBar.setVisibility(View.GONE);
                mLoadImageErrorTextView.setVisibility(View.VISIBLE);
                mLoadImageErrorTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mLoadImageProgressBar.setVisibility(View.VISIBLE);
                        mLoadImageErrorTextView.setVisibility(View.GONE);
                        loadImage();
                    }
                });
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                mLoadWrapper.setVisibility(View.GONE);
                return false;
            }
        });

        if(mPostData.isNSFW()) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 3)))
                    .into(mImageView);
        } else {
            imageRequestBuilder.into(mImageView);
        }
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
