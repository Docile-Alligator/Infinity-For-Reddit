package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
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
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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

    private static final int EDIT_POST_REQUEST_CODE = 2;
    static final int EDIT_COMMENT_REQUEST_CODE = 3;

    private RequestManager mGlide;
    private Locale mLocale;
    private Menu mMenu;

    private int orientation;
    private int postListPosition = -1;

    @State
    boolean mNullAccessToken = false;
    @State
    String mAccessToken;
    @State
    String mAccountName;
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

    private boolean showToast = false;

    private LinearLayoutManager mLinearLayoutManager;
    private CommentAndPostRecyclerViewAdapter mAdapter;
    private LoadSubredditIconAsyncTask mLoadSubredditIconAsyncTask;

    @BindView(R.id.coordinator_layout_view_post_detail) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.appbar_layout_view_post_detail_activity) AppBarLayout appBarLayout;
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

        Resources resources = getResources();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1
                && (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
                || resources.getBoolean(R.bool.isTablet))) {
            Window window = getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            boolean lightNavBar = false;
            if((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) != Configuration.UI_MODE_NIGHT_YES) {
                lightNavBar = true;
            }
            boolean finalLightNavBar = lightNavBar;

            View decorView = window.getDecorView();
            if(finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
            appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
                @Override
                void onStateChanged(AppBarLayout appBarLayout, State state) {
                    if (state == State.COLLAPSED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                    } else if (state == State.EXPANDED) {
                        if(finalLightNavBar) {
                            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
                        }
                    }
                }
            });

            int statusBarResourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusBarResourceId > 0) {
                ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar.getLayoutParams();
                params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
                toolbar.setLayoutParams(params);
            }

            int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
            if (navBarResourceId > 0) {
                mRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
                showToast = true;
            }
        }

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
                mAccountName = account.getUsername();
            }

            bindView();
        }).execute();
    }

    private void bindView() {
        if(mPost == null) {
            mPost = getIntent().getExtras().getParcelable(EXTRA_POST_DATA);
        }

        if(mPost == null) {
            fetchPostAndCommentsById(getIntent().getExtras().getString(EXTRA_POST_ID));
        } else {
            if(mMenu != null && mPost.getAuthor().equals(mAccountName)) {
                if(mPost.getPostType() == Post.TEXT_TYPE) {
                    mMenu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
                }
                mMenu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);

                MenuItem nsfwItem = mMenu.findItem(R.id.action_nsfw_view_post_detail_activity);
                nsfwItem.setVisible(true);
                if(mPost.isNSFW()) {
                    nsfwItem.setTitle(R.string.action_unmark_nsfw);
                } else {
                    nsfwItem.setTitle(R.string.action_mark_nsfw);
                }

                MenuItem spoilerItem = mMenu.findItem(R.id.action_spoiler_view_post_detail_activity);
                spoilerItem.setVisible(true);
                if(mPost.isSpoiler()) {
                    spoilerItem.setTitle(R.string.action_unmark_spoiler);
                } else {
                    spoilerItem.setTitle(R.string.action_mark_spoiler);
                }
            }
            mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                    mOauthRetrofit, mRedditDataRoomDatabase, mGlide, mAccessToken, mAccountName, mPost,
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
                    refresh(false);
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
        mProgressBar.setVisibility(View.VISIBLE);
        mGlide.clear(mFetchPostInfoImageView);

        Call<String> postAndComments;
        if(mAccessToken == null) {
            postAndComments = mRetrofit.create(RedditAPI.class).getPostAndCommentsById(subredditId);
        } else {
            postAndComments = mOauthRetrofit.create(RedditAPI.class).getPostAndCommentsByIdOauth(subredditId, RedditUtils.getOAuthHeader(mAccessToken));
        }
        postAndComments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                mProgressBar.setVisibility(View.GONE);

                if(response.isSuccessful()) {
                    ParsePost.parsePost(response.body(), mLocale, new ParsePost.ParsePostListener() {
                        @Override
                        public void onParsePostSuccess(Post post) {
                            mPost = post;

                            if(mMenu != null && mPost.getAuthor().equals(mAccountName)) {
                                if(mPost.getPostType() == Post.TEXT_TYPE) {
                                    mMenu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
                                }
                                mMenu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);
                            }

                            mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
                                    mOauthRetrofit, mRedditDataRoomDatabase, mGlide, mAccessToken, mAccountName, mPost,
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

    private void refresh(boolean onlyRefreshPost) {
        if(!isRefreshing) {
            isRefreshing = true;
            mChildrenStartingIndex = 0;

            mFetchPostInfoLinearLayout.setVisibility(View.GONE);
            mGlide.clear(mFetchPostInfoImageView);

            if(!onlyRefreshPost) {
                fetchComments();
            }

            Retrofit retrofit;
            if(mAccessToken == null) {
                retrofit = mRetrofit;
            } else {
                retrofit = mOauthRetrofit;
            }
            FetchPost.fetchPost(retrofit, mPost.getId(), mAccessToken, mLocale,
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
                            showMessage(R.string.refresh_post_failed);
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

    private void showMessage(int resId) {
        if(showToast) {
            Toast.makeText(ViewPostDetailActivity.this, resId, Toast.LENGTH_SHORT).show();
        } else {
            Snackbar.make(mCoordinatorLayout, resId, Snackbar.LENGTH_SHORT);
        }
    }

    private void markNSFW() {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).markNSFW(RedditUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    refresh(true);
                    showMessage(R.string.mark_nsfw_success);
                } else {
                    showMessage(R.string.mark_nsfw_failed);
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showMessage(R.string.mark_nsfw_failed);
            }
        });
    }

    private void unmarkNSFW() {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).unmarkNSFW(RedditUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if(response.isSuccessful()) {
                            refresh(true);
                            showMessage(R.string.unmark_nsfw_success);
                        } else {
                            showMessage(R.string.unmark_nsfw_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showMessage(R.string.unmark_nsfw_failed);
                    }
                });
    }

    private void markSpoiler() {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).markSpoiler(RedditUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if(response.isSuccessful()) {
                            refresh(true);
                            showMessage(R.string.mark_spoiler_success);
                        } else {
                            showMessage(R.string.mark_spoiler_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showMessage(R.string.mark_spoiler_failed);
                    }
                });
    }

    private void unmarkSpoiler() {
        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.ID_KEY, mPost.getFullName());
        mOauthRetrofit.create(RedditAPI.class).unmarkSpoiler(RedditUtils.getOAuthHeader(mAccessToken), params)
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if(response.isSuccessful()) {
                            refresh(true);
                            showMessage(R.string.unmark_spoiler_success);
                        } else {
                            showMessage(R.string.unmark_spoiler_failed);
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        showMessage(R.string.unmark_spoiler_failed);
                    }
                });
    }

    void deleteComment(String fullName, int position) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(R.string.delete_this_comment)
                .setMessage(R.string.are_you_sure)
                .setPositiveButton(R.string.delete, (dialogInterface, i)
                        -> DeleteThing.delete(mOauthRetrofit, fullName, mAccessToken, new DeleteThing.DeleteThingListener() {
                    @Override
                    public void deleteSuccess() {
                        Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                        mAdapter.deleteComment(position);
                    }

                    @Override
                    public void deleteFailed() {
                        Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_failed, Toast.LENGTH_SHORT).show();
                    }
                }))
                .setNegativeButton(R.string.cancel, null)
                .show();
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
        mMenu = menu;
        if(mPost != null && mPost.getAuthor().equals(mAccountName)) {
            if(mPost.getPostType() == Post.TEXT_TYPE) {
                menu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
            }
            menu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);

            MenuItem nsfwItem = menu.findItem(R.id.action_nsfw_view_post_detail_activity);
            nsfwItem.setVisible(true);
            if(mPost.isNSFW()) {
                nsfwItem.setTitle(R.string.action_unmark_nsfw);
            } else {
                nsfwItem.setTitle(R.string.action_mark_nsfw);
            }

            MenuItem spoilerItem = menu.findItem(R.id.action_spoiler_view_post_detail_activity);
            spoilerItem.setVisible(true);
            if(mPost.isSpoiler()) {
                spoilerItem.setTitle(R.string.action_unmark_spoiler);
            } else {
                spoilerItem.setTitle(R.string.action_mark_spoiler);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh_view_post_detail_activity:
                refresh(false);
                return true;
            case R.id.action_comment_view_post_detail_activity:
                if(mAccessToken == null) {
                    Toast.makeText(this, R.string.login_first, Toast.LENGTH_SHORT).show();
                    return true;
                }

                Intent intent = new Intent(this, CommentActivity.class);
                intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, mPost.getTitle());
                intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
                intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
                intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
                startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
                return true;
            case R.id.action_edit_view_post_detail_activity:
                Intent editPostItent = new Intent(this, EditPostActivity.class);
                editPostItent.putExtra(EditPostActivity.EXTRA_ACCESS_TOKEN, mAccessToken);
                editPostItent.putExtra(EditPostActivity.EXTRA_FULLNAME, mPost.getFullName());
                editPostItent.putExtra(EditPostActivity.EXTRA_TITLE, mPost.getTitle());
                editPostItent.putExtra(EditPostActivity.EXTRA_CONTENT, mPost.getSelfText());
                startActivityForResult(editPostItent, EDIT_POST_REQUEST_CODE);
                return true;
            case R.id.action_delete_view_post_detail_activity:
                new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                        .setTitle(R.string.delete_this_post)
                        .setMessage(R.string.are_you_sure)
                        .setPositiveButton(R.string.delete, (dialogInterface, i)
                                -> DeleteThing.delete(mOauthRetrofit, mPost.getFullName(), mAccessToken, new DeleteThing.DeleteThingListener() {
                            @Override
                            public void deleteSuccess() {
                                Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_success, Toast.LENGTH_SHORT).show();
                                finish();
                            }

                            @Override
                            public void deleteFailed() {
                                showMessage(R.string.delete_post_failed);
                            }
                        }))
                        .setNegativeButton(R.string.cancel, null)
                        .show();
                return true;
            case R.id.action_nsfw_view_post_detail_activity:
                if(mPost.isNSFW()) {
                    unmarkNSFW();
                } else {
                    markNSFW();
                }
                return true;
            case R.id.action_spoiler_view_post_detail_activity:
                if(mPost.isSpoiler()) {
                    unmarkSpoiler();
                } else {
                    markSpoiler();
                }
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
        if(requestCode == WRITE_COMMENT_REQUEST_CODE) {
            if(data != null && resultCode == RESULT_OK) {
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
        } else if(requestCode == EDIT_POST_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                refresh(true);
            }
        } else if(requestCode == EDIT_COMMENT_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                mAdapter.editComment(data.getExtras().getString(EditCommentActivity.EXTRA_EDITED_COMMENT_CONTENT),
                        data.getExtras().getInt(EditCommentActivity.EXTRA_EDITED_COMMENT_POSITION));
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
