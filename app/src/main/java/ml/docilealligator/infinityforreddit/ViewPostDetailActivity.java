package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
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
import com.google.android.material.snackbar.Snackbar;

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
    private static final String ORIENTATION_STATE = "OS";
    private static final String POST_STATE = "PS";
    private static final String IS_REFRESHING_STATE = "IRS";
    private static final String IS_LOADING_MORE_CHILDREN_STATE = "ILMCS";
    private static final String COMMENTS_STATE = "CS";
    private static final String HAS_MORE_CHILDREN_STATE = "HMCS";
    private static final String MORE_CHILDREN_LIST_STATE = "MCLS";
    private static final String MORE_CHILDREN_STARTING_INDEX_STATE = "MCSIS";

    private Post mPost;
    private int postListPosition = -1;

    private boolean isLoadingMoreChildren = false;
    private boolean isRefreshing = false;
    private ArrayList<String> children;
    private int mChildrenStartingIndex = 0;
    private boolean loadMoreChildrenSuccess = true;
    private boolean hasMoreChildren;

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

        mLinearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        if(savedInstanceState == null) {
            orientation = getResources().getConfiguration().orientation;
            mPost = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);
        } else {
            orientation = savedInstanceState.getInt(ORIENTATION_STATE);
            mPost = savedInstanceState.getParcelable(POST_STATE);
        }

        if(mPost == null) {
            mProgressBar.setVisibility(View.VISIBLE);
            fetchPostAndCommentsById(getIntent().getExtras().getString(EXTRA_POST_ID));
        } else {
            mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                    mOauthRetrofit, mGlide, mSharedPreferences, mPost,
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

            if(savedInstanceState != null) {
                isRefreshing = savedInstanceState.getBoolean(IS_REFRESHING_STATE);
                if(isRefreshing) {
                    isRefreshing = false;
                    refresh();
                } else {
                    mAdapter.addComments(savedInstanceState.getParcelableArrayList(COMMENTS_STATE),
                            savedInstanceState.getBoolean(HAS_MORE_CHILDREN_STATE));
                    isLoadingMoreChildren = savedInstanceState.getBoolean(IS_LOADING_MORE_CHILDREN_STATE);
                    children = savedInstanceState.getStringArrayList(MORE_CHILDREN_LIST_STATE);
                    mChildrenStartingIndex = savedInstanceState.getInt(MORE_CHILDREN_STARTING_INDEX_STATE);
                    if(isLoadingMoreChildren) {
                        isLoadingMoreChildren = false;
                        fetchMoreComments();
                    }
                }
            } else {
                fetchComment();
            }
        }

        if(getIntent().hasExtra(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getIntent().getExtras().getInt(EXTRA_POST_LIST_POSITION);
        }
    }

    private void fetchPostAndCommentsById(String subredditId) {
        mFetchPostInfoLinearLayout.setVisibility(View.GONE);
        mGlide.clear(mFetchPostInfoImageView);

        String accessToken = getSharedPreferences(SharedPreferencesUtils.AUTH_CODE_FILE_KEY, Context.MODE_PRIVATE)
                .getString(SharedPreferencesUtils.ACCESS_TOKEN_KEY, "");

        RedditAPI api = mOauthRetrofit.create(RedditAPI.class);
        Call<String> postAndComments = api.getPostAndCommentsById(subredditId, RedditUtils.getOAuthHeader(accessToken));
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
                                    mOauthRetrofit, mGlide, mSharedPreferences, mPost,
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

    private void fetchComment() {
        mAdapter.initiallyLoading();

        FetchComment.fetchComment(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getId(),
                mLocale, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(ArrayList<CommentData> expandedComments,
                                                      String parentId, ArrayList<String> children) {
                        ViewPostDetailActivity.this.children = children;

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

            fetchComment();

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
        mFetchPostInfoTextView.setText(R.string.error_loading_post);
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
        outState.putInt(ORIENTATION_STATE, orientation);
        outState.putParcelable(POST_STATE, mPost);
        outState.putBoolean(IS_REFRESHING_STATE, isRefreshing);
        outState.putBoolean(IS_LOADING_MORE_CHILDREN_STATE, isLoadingMoreChildren);
        outState.putParcelableArrayList(COMMENTS_STATE, mAdapter.getVisibleComments());
        outState.putBoolean(HAS_MORE_CHILDREN_STATE, hasMoreChildren);
        outState.putStringArrayList(MORE_CHILDREN_LIST_STATE, children);
        outState.putInt(MORE_CHILDREN_STARTING_INDEX_STATE, mChildrenStartingIndex);
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
