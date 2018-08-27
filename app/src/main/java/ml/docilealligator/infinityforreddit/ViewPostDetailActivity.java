package ml.docilealligator.infinityforreddit;

import android.content.Intent;
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
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.sufficientlysecure.htmltextview.HtmlTextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_POST_DATA = "EPD";

    private int orientation;
    private String orientationState = "OS";

    private int mMoreCommentCount;
    private PostData mPostData;

    private CoordinatorLayout mCoordinatorLayout;
    private ProgressBar mCommentProgressbar;
    private CardView mCommentCardView;
    private RecyclerView mRecyclerView;

    private LinearLayout mNoCommentWrapperLinearLayout;
    private ImageView mNoCommentImageView;

    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    private RequestQueue mVoteThingRequestQueue;
    private RequestQueue mAcquireAccessTokenRequestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_detail);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        orientation = getResources().getConfiguration().orientation;

        mPostData = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);

        TextView titleTextView = findViewById(R.id.title_text_view_view_post_detail);
        titleTextView.setText(mPostData.getTitle());

        mCoordinatorLayout = findViewById(R.id.coordinator_layout_view_post_detail);

        final CircleImageView subredditIconCircleImageView = findViewById(R.id.subreddit_icon_circle_image_view_view_post_detail);
        TextView postTimeTextView = findViewById(R.id.post_time_text_view_view_post_detail);
        TextView subredditTextView = findViewById(R.id.subreddit_text_view_view_post_detail);
        HtmlTextView contentTextView = findViewById(R.id.content_html_text_view_view_post_detail);
        TextView typeTextView = findViewById(R.id.type_text_view_view_post_detail);
        ImageView gildedImageView = findViewById(R.id.gilded_image_view_view_post_detail);
        TextView gildedNumberTextView = findViewById(R.id.gilded_number_text_view_view_post_detail);
        TextView nsfwTextView = findViewById(R.id.nsfw_text_view_view_post_detail);
        RelativeLayout relativeLayout = findViewById(R.id.image_view_wrapper_view_post_detail);
        final ProgressBar progressBar = findViewById(R.id.progress_bar_view_post_detail);
        ImageView imageView = findViewById(R.id.image_view_view_post_detail);
        ImageView noPreviewLinkImageView = findViewById(R.id.image_view_no_preview_link_view_post_detail);

        final ImageView upvoteButton = findViewById(R.id.plus_button_view_post_detail);
        final TextView scoreTextView = findViewById(R.id.score_text_view_view_post_detail);
        final ImageView downvoteButton = findViewById(R.id.minus_button_view_post_detail);
        ImageView shareButton = findViewById(R.id.share_button_view_post_detail);

        mCommentProgressbar = findViewById(R.id.comment_progress_bar_view_post_detail);
        mCommentCardView = findViewById(R.id.comment_card_view_view_post_detail);
        mRecyclerView = findViewById(R.id.recycler_view_view_post_detail);

        mNoCommentWrapperLinearLayout = findViewById(R.id.no_comment_wrapper_linear_layout_view_post_detail);
        mNoCommentImageView = findViewById(R.id.no_comment_image_view_view_post_detail);

        if(mPostData.getSubredditIconUrl() == null) {
            mLoadSubredditIconAsyncTask = new LoadSubredditIconAsyncTask(
                    SubredditRoomDatabase.getDatabase(this).subredditDao(), mPostData.getSubredditNamePrefixed(),
                    new LoadSubredditIconAsyncTask.LoadSubredditIconAsyncTaskListener() {
                @Override
                public void loadIconSuccess(String iconImageUrl) {
                    if(!iconImageUrl.equals("")) {
                        Glide.with(ViewPostDetailActivity.this).load(iconImageUrl)
                                .into(subredditIconCircleImageView);
                    } else {
                        Glide.with(ViewPostDetailActivity.this).load(R.drawable.subreddit_default_icon)
                                .into(subredditIconCircleImageView);
                    }

                    mPostData.setSubredditIconUrl(iconImageUrl);
                }
            });
            mLoadSubredditIconAsyncTask.execute();
        } else if(!mPostData.getSubredditIconUrl().equals("")) {
            Glide.with(this).load(mPostData.getSubredditIconUrl()).into(subredditIconCircleImageView);
        } else {
            Glide.with(this).load(R.drawable.subreddit_default_icon).into(subredditIconCircleImageView);
        }

        switch (mPostData.getVoteType()) {
            case 1:
                //Upvote
                upvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
            case -1:
                //Downvote
                downvoteButton.setColorFilter(ContextCompat.getColor(this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                break;
        }

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mVoteThingRequestQueue = Volley.newRequestQueue(this);
        mAcquireAccessTokenRequestQueue = Volley.newRequestQueue(this);

        subredditTextView.setText(mPostData.getSubredditNamePrefixed());
        postTimeTextView.setText(mPostData.getPostTime());

        if(mPostData.getGilded() > 0) {
            gildedImageView.setVisibility(View.VISIBLE);
            Glide.with(this).load(R.drawable.gold).into(gildedImageView);
            gildedNumberTextView.setVisibility(View.VISIBLE);
            String gildedNumber = getResources().getString(R.string.gilded, mPostData.getGilded());
            gildedNumberTextView.setText(gildedNumber);
        }

        if(mPostData.isNSFW()) {
            nsfwTextView.setVisibility(View.VISIBLE);
        }
        scoreTextView.setText(Integer.toString(mPostData.getScore()));

        shareButton.setOnClickListener(new View.OnClickListener() {
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
                typeTextView.setText("IMAGE");
                relativeLayout.setVisibility(View.VISIBLE);
                Glide.with(this).load(mPostData.getPreviewUrl()).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        //Need to be implemented
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);
                imageView.setOnClickListener(new View.OnClickListener() {
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
                relativeLayout.setVisibility(View.VISIBLE);
                typeTextView.setText("LINK");
                if(!mPostData.getSelfText().equals("")) {
                    contentTextView.setVisibility(View.VISIBLE);
                    contentTextView.setHtml(mPostData.getSelfText());
                }
                String linkPreviewUrl = mPostData.getPreviewUrl();
                Glide.with(this).load(linkPreviewUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);
                final String linkUrl = mPostData.getUrl();
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                        // add share action to menu list
                        builder.addDefaultShareMenuItem();
                        builder.setToolbarColor(getResources().getColor(R.color.colorPrimary));
                        CustomTabsIntent customTabsIntent = builder.build();
                        customTabsIntent.launchUrl(ViewPostDetailActivity.this, Uri.parse(linkUrl));
                    }
                });
                break;
            case PostData.GIF_VIDEO_TYPE:
                relativeLayout.setVisibility(View.VISIBLE);
                typeTextView.setText("VIDEO");
                String gifVideoPreviewUrl = mPostData.getPreviewUrl();
                Glide.with(this).load(gifVideoPreviewUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);

                String gifVideoUrl = mPostData.getVideoUrl();
                final Uri gifVideoUri = Uri.parse(gifVideoUrl);

                imageView.setOnClickListener(new View.OnClickListener() {
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
                relativeLayout.setVisibility(View.VISIBLE);
                typeTextView.setText("VIDEO");
                String videoPreviewUrl = mPostData.getPreviewUrl();
                Glide.with(this).load(videoPreviewUrl).listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                }).into(imageView);

                String videoUrl = mPostData.getVideoUrl();
                final Uri videoUri = Uri.parse(videoUrl);

                imageView.setOnClickListener(new View.OnClickListener() {
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
                typeTextView.setText("LINK");
                if(!mPostData.getSelfText().equals("")) {
                    contentTextView.setVisibility(View.VISIBLE);
                    contentTextView.setHtml(mPostData.getSelfText());
                }
                noPreviewLinkImageView.setVisibility(View.VISIBLE);
                noPreviewLinkImageView.setOnClickListener(new View.OnClickListener() {
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
                typeTextView.setText("TEXT");
                if(!mPostData.getSelfText().equals("")) {
                    contentTextView.setVisibility(View.VISIBLE);
                    contentTextView.setHtml(mPostData.getSelfText());
                }
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

        upvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //observable.subscribe(observer);
                final boolean isDownvotedBefore = downvoteButton.getColorFilter() != null;

                final ColorFilter downVoteButtonColorFilter = downvoteButton.getColorFilter();
                downvoteButton.clearColorFilter();

                if (upvoteButton.getColorFilter() == null) {
                    upvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                    if(isDownvotedBefore) {
                        scoreTextView.setText(Integer.toString(mPostData.getScore() + 2));
                    } else {
                        scoreTextView.setText(Integer.toString(mPostData.getScore() + 1));
                    }

                    new VoteThing(ViewPostDetailActivity.this, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingWithoutPositionListener() {
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
                            upvoteButton.clearColorFilter();
                            scoreTextView.setText(Integer.toString(mPostData.getScore()));
                            downvoteButton.setColorFilter(downVoteButtonColorFilter);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UPVOTE, 1);
                } else {
                    //Upvoted before
                    upvoteButton.clearColorFilter();
                    scoreTextView.setText(Integer.toString(mPostData.getScore() - 1));

                    new VoteThing(ViewPostDetailActivity.this, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(0);
                            mPostData.setScore(mPostData.getScore() - 1);
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                            scoreTextView.setText(Integer.toString(mPostData.getScore() + 1));
                            upvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.colorPrimary), android.graphics.PorterDuff.Mode.SRC_IN);
                            mPostData.setScore(mPostData.getScore() + 1);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UNVOTE, 1);
                }
            }
        });

        downvoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //observable.subscribe(observer);
                final boolean isUpvotedBefore = upvoteButton.getColorFilter() != null;

                final ColorFilter upvoteButtonColorFilter = upvoteButton.getColorFilter();
                upvoteButton.clearColorFilter();

                if (downvoteButton.getColorFilter() == null) {
                    downvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                    if (isUpvotedBefore) {
                        scoreTextView.setText(Integer.toString(mPostData.getScore() - 2));
                    } else {
                        scoreTextView.setText(Integer.toString(mPostData.getScore() - 1));
                    }

                    new VoteThing(ViewPostDetailActivity.this, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingWithoutPositionListener() {
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
                            downvoteButton.clearColorFilter();
                            scoreTextView.setText(Integer.toString(mPostData.getScore()));
                            upvoteButton.setColorFilter(upvoteButtonColorFilter);
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_DOWNVOTE, 1);
                } else {
                    //Down voted before
                    downvoteButton.clearColorFilter();
                    scoreTextView.setText(Integer.toString(mPostData.getScore() + 1));

                    new VoteThing(ViewPostDetailActivity.this, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue).votePost(new VoteThing.VoteThingWithoutPositionListener() {
                        @Override
                        public void onVoteThingSuccess() {
                            mPostData.setVoteType(0);
                            mPostData.setScore(mPostData.getScore());
                        }

                        @Override
                        public void onVoteThingFail() {
                            Toast.makeText(ViewPostDetailActivity.this, "Cannot unvote this post", Toast.LENGTH_SHORT).show();
                            downvoteButton.setColorFilter(ContextCompat.getColor(ViewPostDetailActivity.this, R.color.minusButtonColor), android.graphics.PorterDuff.Mode.SRC_IN);
                            scoreTextView.setText(Integer.toString(mPostData.getScore()));
                            mPostData.setScore(mPostData.getScore());
                        }
                    }, mPostData.getFullName(), RedditUtils.DIR_UNVOTE, 1);
                }
            }
        });
    }

    private void queryComment() {
        mCommentProgressbar.setVisibility(View.VISIBLE);
        mNoCommentWrapperLinearLayout.setVisibility(View.GONE);
        new FetchComment(mPostData.getSubredditNamePrefixed(), mPostData.getId()).queryComment(new FetchComment.FetchCommentListener() {
            @Override
            public void onFetchCommentSuccess(String response) {
                ParseComment.parseComment(response, new ArrayList<CommentData>(),
                        getResources().getConfiguration().locale, new ParseComment.ParseCommentListener() {
                            @Override
                            public void onParseCommentSuccess(ArrayList<CommentData> commentData, int moreCommentCount) {
                                mCommentProgressbar.setVisibility(View.GONE);
                                mMoreCommentCount = moreCommentCount;
                                if (commentData.size() > 0) {
                                    CommentRecyclerViewAdapter adapter = new CommentRecyclerViewAdapter(
                                            ViewPostDetailActivity.this, commentData, mVoteThingRequestQueue, mAcquireAccessTokenRequestQueue);
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
