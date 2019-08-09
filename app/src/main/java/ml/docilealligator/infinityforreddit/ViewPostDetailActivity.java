package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.evernote.android.state.State;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static ml.docilealligator.infinityforreddit.CommentActivity.EXTRA_COMMENT_DATA_KEY;
import static ml.docilealligator.infinityforreddit.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

public class ViewPostDetailActivity extends AppCompatActivity {

    static final String EXTRA_POST_DATA = "EPD";
    static final String EXTRA_POST_LIST_POSITION = "EPLI";
    static final String EXTRA_POST_ID = "EPI";

    private RequestManager mGlide;
    private Locale mLocale;

    private int orientation;
    private int postListPosition = -1;

    @State
    boolean mNullAccessToken = false;
    @State
    String mAccessToken;
    @State
    Post mPost;
    @State
    boolean isLoadingMoreChildren = false;
    @State
    boolean isRefreshing = false;
    @State
    ArrayList<CommentData> comments;
    @State
    ArrayList<String> children;
    @State
    int mChildrenStartingIndex = 0;
    @State
    boolean loadMoreChildrenSuccess = true;
    @State
    boolean hasMoreChildren;

    private LinearLayoutManager mLinearLayoutManager;
    private CommentAndPostRecyclerViewAdapter mAdapter;
    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar_view_post_detail_activity) Toolbar toolbar;
    @BindView(R.id.progress_bar_view_post_detail_activity) ProgressBar mProgressBar;
    @BindView(R.id.recycler_view_view_post_detail) RecyclerView mRecyclerView;
    @BindView(R.id.fetch_post_info_linear_layout_view_post_detail_activity) LinearLayout mFetchPostInfoLinearLayout;
    @BindView(R.id.fetch_post_info_image_view_view_post_detail_activity) ImageView mFetchPostInfoImageView;
    @BindView(R.id.fetch_post_info_text_view_view_post_detail_activity) TextView mFetchPostInfoTextView;

    @Inject @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post_detail);

        Bridge.restoreInstanceState(this, savedInstanceState);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        ((Infinity) getApplication()).getAppComponent().inject(this);

        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        mGlide = Glide.with(this);
        mLocale = getResources().getConfiguration().locale;

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        orientation = getResources().getConfiguration().orientation;

        if(!mNullAccessToken && mAccessToken == null) {
            getCurrentAccountAndBindView();
        } else {
            bindView();
        }

        if(getIntent().hasExtra(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getIntent().getExtras().getInt(EXTRA_POST_LIST_POSITION);
        }
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if(account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
            }

            bindView();
        }).execute();
    }

    private void bindView() {
        if(mPost == null) {
            mPost = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);
        }

        if(mPost == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            fetchPostAndCommentsById(getIntent().getExtras().getString(EXTRA_POST_ID));
        } else {
            mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                    mOauthRetrofit, mRedditDataRoomDatabase, mGlide, mAccessToken, mPost,
                    mPost.getSubredditNamePrefixed(), mLocale, mLoadSubredditIconAsyncTask,
                    new CommentAndPostRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                        @Override
                        public void updatePost(Post post) {
                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                        }

                        @Override
                        public void retryFetchingMoreComments() {
                            isLoadingMoreChildren = false;
                            loadMoreChildrenSuccess = true;

                            fetchMoreComments();
                        }
                    });
            mRecyclerView.setAdapter(mAdapter);

            if(comments == null) {
                fetchComments();
            } else {
                if(isRefreshing) {
                    isRefreshing = false;
                    refresh();
                } else {
                    mAdapter.addComments(comments, hasMoreChildren);
                    if(isLoadingMoreChildren) {
                        isLoadingMoreChildren = false;
                        fetchMoreComments();
                    }
                }
            }
        }
    }


    private void fetchPostAndCommentsById(String subredditId) {
        mFetchPostInfoLinearLayout.setVisibility(View.GONE);
        mGlide.clear(mFetchPostInfoImageView);

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        RedditAPI api = mOauthRetrofit.create(RedditAPI.class);
        Call<String> postAndComments = api.getPostAndCommentsByIdOauth(subredditId, RedditUtils.getOAuthHeader(accessToken));
        postAndComments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                mProgressBar.setVisibility(View.GONE);

                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), mLocale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            mPost = post;

                            mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                                    mOauthRetrofit, mRedditDataRoomDatabase, mGlide, mAccessToken, mPost,
                                    mPost.getSubredditNamePrefixed(), mLocale, mLoadSubredditIconAsyncTask,
                                    new CommentAndPostRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                                        @Override
                                        public void updatePost(Post post) {
                                            EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                        }

                                        @Override
                                        public void retryFetchingMoreComments() {
                                            isLoadingMoreChildren = false;
                                            loadMoreChildrenSuccess = true;

                                            fetchMoreComments();
                                        }
                                    });
                            mRecyclerView.setAdapter(mAdapter);

                            ParseComment.parseComment(response.body(), new ArrayList<>(), mLocale,
                                    new ParseComment.ParseCommentListener() {
                                        @Override
                                        public void onParseCommentSuccess(ArrayList<CommentData> expandedComments, String parentId, ArrayList<String> moreChildrenFullnames) {
                                            ViewPostDetailActivity.this.children = moreChildrenFullnames;

                                            hasMoreChildren = children.size() != 0;
                                            mAdapter.addComments(expandedComments, hasMoreChildren);

                                            if(children.size() > 0) {
                                                mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                                    @Override
                                                    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                                        super.onScrolled(recyclerView, dx, dy);

                                                        if(!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                                                            int visibleItemCount = mLinearLayoutManager.getChildCount();
                                                            int totalItemCount = mLinearLayoutManager.getItemCount();
                                                            int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                                                            if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                                                                fetchMoreComments();
                                                            }
                                                        }
                                                    }
                                                });
                                            }
                                        }

                                        @Override
                                        public void onParseCommentFailed() {
                                            mAdapter.initiallyLoadCommentsFailed();
                                        }
                                    });
                        }

                        @Override
                        public void onParsePostFail() {
                            showErrorView(subredditId);
                        }
                    });
                } else {
                    showErrorView(subredditId);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showErrorView(subredditId);
            }
        });
    }

    private void fetchComments() {
        mAdapter.initiallyLoading();

        FetchComment.fetchComments(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getId(),
                mLocale, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(ArrayList<CommentData> expandedComments,
                                                      String parentId, ArrayList<String> children) {
                        ViewPostDetailActivity.this.children = children;

                        comments = expandedComments;
                        if(comments != null) {
                            Log.i("thisis ", "not null");
                        }
                        hasMoreChildren = children.size() != 0;
                        mAdapter.addComments(expandedComments, hasMoreChildren);

                        if(children.size() > 0) {
                            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                                @Override
                                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                                    super.onScrolled(recyclerView, dx, dy);

                                    if(!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                                        int visibleItemCount = mLinearLayoutManager.getChildCount();
                                        int totalItemCount = mLinearLayoutManager.getItemCount();
                                        int firstVisibleItemPosition = mLinearLayoutManager.findFirstVisibleItemPosition();

                                        if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount) && firstVisibleItemPosition >= 0) {
                                            fetchMoreComments();
                                        }
                                    }
                                }
                            });
                        }
                    }

                    @Override
                    public void onFetchCommentFailed() {
                        mAdapter.initiallyLoadCommentsFailed();
                    }
                });
    }

    void fetchMoreComments() {
        if(isLoadingMoreChildren || !loadMoreChildrenSuccess) {
            return;
        }

        isLoadingMoreChildren = true;
        FetchComment.fetchMoreComment(mRetrofit, mPost.getSubredditNamePrefixed(), children, mChildrenStartingIndex,
                0, mLocale, new FetchComment.FetchMoreCommentListener() {
                    @Override
                    public void onFetchMoreCommentSuccess(ArrayList<CommentData> expandedComments, int childrenStartingIndex) {
                        hasMoreChildren = childrenStartingIndex < children.size();
                        mAdapter.addComments(expandedComments, hasMoreChildren);
                        mChildrenStartingIndex = childrenStartingIndex;
                        isLoadingMoreChildren = false;
                        loadMoreChildrenSuccess = true;
                    }

                    @Override
                    public void onFetchMoreCommentFailed() {
                        isLoadingMoreChildren = false;
                        loadMoreChildrenSuccess = false;
                        mAdapter.loadMoreCommentsFailed();
                    }
                });
    }

    private void refresh() {
        if(!isRefreshing) {
            isRefreshing = true;
            mChildrenStartingIndex = 0;

            mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            mGlide.clear(mFetchPostInfoImageView);

            fetchComments();

            String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                    .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");
            FetchPost.fetchPost(mOauthRetrofit, mPost.getId(), accessToken, mLocale,
                    new FetchPost.FetchPostListener() {
                        @Override
                        public void fetchPostSuccess(Post post) {
                            mPost = post;
                            mAdapter.updatePost(mPost);
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

    private void showErrorView(String subredditId) {
        mProgressBar.setVisibility(View.GONE);
        mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
        mFetchPostInfoLinearLayout.setOnClickListener(view -> fetchPostAndCommentsById(subredditId));
        mFetchPostInfoTextView.setText(R.string.load_posts_error);
        mGlide.load(R.drawable.load_post_error_indicator).into(mFetchPostInfoImageView);
    }

    @Subscribe
    public void onPostUpdateEvent(PostUpdateEventToDetailActivity event) {
        if(mPost.getId().equals(event.postId)) {
            mPost.setVoteType(event.voteType);
            mAdapter.updatePost(mPost);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.view_post_detail_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
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
                    String parentFullname = data.getExtras().getString(CommentActivity.EXTRA_PARENT_FULLNAME_KEY);
                    int parentPosition = data.getExtras().getInt(CommentActivity.EXTRA_PARENT_POSITION_KEY);
                    mAdapter.addChildComment(comment, parentFullname, parentPosition);
                }
            } else {
                Toast.makeText(this, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Bridge.saveInstanceState(this, outState);
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
        Bridge.clear(this);
        if(mLoadSubredditIconAsyncTask != null) {
            mLoadSubredditIconAsyncTask.cancel(true);
        }
    }
}
