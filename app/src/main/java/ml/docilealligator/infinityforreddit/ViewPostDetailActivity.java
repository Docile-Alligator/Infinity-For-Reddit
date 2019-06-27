package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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
import retrofit2.Retrofit;

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
    private ArrayList<String> children;
    private int mChildrenStartingIndex = 0;
    private boolean loadMoreChildrenSuccess = true;

    private LinearLayoutManager mLinearLayoutManager;
    private CommentRecyclerViewAdapter mAdapter;
    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar_view_post_detail_activity) Toolbar toolbar;
    @BindView(R.id.recycler_view_view_post_detail) RecyclerView mRecyclerView;

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

        mAdapter = new CommentRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                mOauthRetrofit, mGlide, mSharedPreferences, mPost,
                mPost.getSubredditNamePrefixed(), mLocale, mLoadSubredditIconAsyncTask,
                new CommentRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
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
            }
        }

        if(getIntent().hasExtra(EXTRA_POST_LIST_POSITION)) {
            postListPosition = getIntent().getExtras().getInt(EXTRA_POST_LIST_POSITION);
        }

        fetchComment();
    }

    private void fetchComment() {
        mAdapter.initiallyLoading();

        FetchComment.fetchComment(mRetrofit, mPost.getSubredditNamePrefixed(), mPost.getId(),
                mLocale, new FetchComment.FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(ArrayList<CommentData> expandedComments,
                                                      String parentId, ArrayList<String> children) {
                        ViewPostDetailActivity.this.children = children;

                        mAdapter.addComments(expandedComments, children.size() != 0);

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

                        /*mCommentProgressbar.setVisibility(View.GONE);

                        if (expandedComments.size() > 0) {
                            if(mAdapter == null) {
                                mNestedScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                                    if(!isLoadingMoreChildren) {
                                        View view = mNestedScrollView.getChildAt(mNestedScrollView.getChildCount() - 1);
                                        int diff = view.getBottom() - (mNestedScrollView.getHeight() +
                                                mNestedScrollView.getScrollY());
                                        if(diff == 0) {
                                            fetchMoreComments(mChildrenStartingIndex);
                                        }
                                    }
                                });
                            }

                            mAdapter = new CommentRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                                    mOauthRetrofit, mGlide, mSharedPreferences, mPost,
                                    mPost.getSubredditNamePrefixed(), mLocale, new CommentRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                                @Override
                                public void updatePost(Post post) {
                                    EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
                                }
                            });
                            mRecyclerView.setAdapter(mAdapter);

                            //mCommentCardView.setVisibility(View.VISIBLE);
                        } else {
                            mNoCommentWrapperLinearLayout.setVisibility(View.VISIBLE);
                            mGlide.load(R.drawable.no_comment_placeholder).into(mNoCommentImageView);
                        }*/
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
                        mAdapter.addComments(expandedComments, childrenStartingIndex < children.size());
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
