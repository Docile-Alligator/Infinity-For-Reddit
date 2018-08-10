package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_TITLE = "ET";
    static final String EXTRA_POST_DATA = "EPD";

    private int orientation;
    private String orientationState = "OS";

    private int mMoreCommentCount;
    private BestPostData mPostData;

    private CoordinatorLayout mCoordinatorLayout;
    private ProgressBar mCommentProgressbar;
    private CardView mCommentCardView;
    private RecyclerView mRecyclerView;

    private RequestQueue mVoteThingQueue;
    private RequestQueue mCommentQueue;

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

        CircleImageView subredditIconCircleImageView = findViewById(R.id.subreddit_icon_circle_image_view_view_post_detail);
        TextView postTimeTextView = findViewById(R.id.post_time_text_view_view_post_detail);
        TextView subredditTextView = findViewById(R.id.subreddit_text_view_view_post_detail);
        TextView contentTextView = findViewById(R.id.content_text_view_view_post_detail);
        TextView typeTextView = findViewById(R.id.type_text_view_view_post_detail);
        TextView nsfwTextView = findViewById(R.id.nsfw_text_view_view_post_detail);
        RelativeLayout relativeLayout = findViewById(R.id.image_view_wrapper_view_post_detail);
        final ProgressBar progressBar = findViewById(R.id.progress_bar_view_post_detail);
        ImageView imageView = findViewById(R.id.image_view_view_post_detail);
        ImageView noPreviewLinkImageView = findViewById(R.id.image_view_no_preview_link_view_post_detail);

        ImageView plusButton = findViewById(R.id.plus_button_view_post_detail);
        TextView scoreTextView = findViewById(R.id.score_text_view_view_post_detail);
        ImageView minusButton = findViewById(R.id.minus_button_view_post_detail);
        ImageView shareButton = findViewById(R.id.share_button_view_post_detail);

        mCommentProgressbar = findViewById(R.id.comment_progress_bar_view_post_detail);
        mCommentCardView = findViewById(R.id.comment_card_view_view_post_detail);
        mRecyclerView = findViewById(R.id.recycler_view_view_post_detail);

        if(mPostData.getSubredditIconUrl() == null) {
            new LoadSubredditIconAsyncTask(this, subredditIconCircleImageView,
                    SubredditRoomDatabase.getDatabase(this).subredditDao(), mPostData.getSubredditName(),
                    mPostData).execute();
        } else if(!mPostData.getSubredditIconUrl().equals("")) {
            Glide.with(this).load(mPostData.getSubredditIconUrl()).into(subredditIconCircleImageView);
        } else {
            Glide.with(this).load(R.drawable.subreddit_default_icon).into(subredditIconCircleImageView);
        }

        mRecyclerView.setNestedScrollingEnabled(false);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mVoteThingQueue = Volley.newRequestQueue(this);
        mCommentQueue = Volley.newRequestQueue(this);

        subredditTextView.setText(mPostData.getSubredditName());
        postTimeTextView.setText(mPostData.getPostTime());
        if(mPostData.getNSFW()) {
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
            case BestPostData.IMAGE_TYPE:
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
                        intent.putExtra(ViewImageActivity.IMAGE_URL_KEY, mPostData.getPreviewUrl());
                        intent.putExtra(ViewImageActivity.TITLE_KEY, mPostData.getTitle());
                        startActivity(intent);
                    }
                });
                break;
            case BestPostData.LINK_TYPE:
                relativeLayout.setVisibility(View.VISIBLE);
                typeTextView.setText("LINK");
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
            case BestPostData.GIF_VIDEO_TYPE:
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
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPostData.getSubredditName());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPostData.getId());
                        }
                        startActivity(intent);
                    }
                });
                break;
            case BestPostData.VIDEO_TYPE:
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
                            intent.putExtra(ViewVideoActivity.SUBREDDIT_KEY, mPostData.getSubredditName());
                            intent.putExtra(ViewVideoActivity.ID_KEY, mPostData.getId());
                        }
                        startActivity(intent);
                    }
                });
                break;
            case BestPostData.NO_PREVIEW_LINK_TYPE:
                typeTextView.setText("LINK");
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
            case BestPostData.TEXT_TYPE:
                typeTextView.setText("TEXT");
                if(!mPostData.getSelfText().equals("")) {
                    contentTextView.setVisibility(View.VISIBLE);
                    contentTextView.setText(mPostData.getSelfText());
                }
        }
        queryComment();
    }

    private void queryComment() {
        mCommentProgressbar.setVisibility(View.VISIBLE);
        new FetchComment(mCommentQueue, mPostData.getSubredditName(), mPostData.getId()).queryComment(new FetchComment.FetchCommentListener() {
            @Override
            public void onFetchCommentSuccess(String response) {
                new ParseComment().parseComment(ViewPostDetailActivity.this, response, new ArrayList<CommentData>(), new ParseComment.ParseCommentListener() {
                    @Override
                    public void onParseCommentSuccess(ArrayList<CommentData> commentData, int moreCommentCount) {
                        mCommentProgressbar.setVisibility(View.GONE);
                        mMoreCommentCount = moreCommentCount;
                        if(commentData.size() > 0) {
                            CommentRecyclerViewAdapter adapter = new CommentRecyclerViewAdapter(ViewPostDetailActivity.this, commentData, mVoteThingQueue);
                            mRecyclerView.setAdapter(adapter);
                            mCommentCardView.setVisibility(View.VISIBLE);
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
}
