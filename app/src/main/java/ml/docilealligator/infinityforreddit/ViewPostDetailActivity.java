package ml.docilealligator.infinityforreddit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;
import static ml.docilealligator.infinityforreddit.CommentActivity.EXTRA_COMMENT_DATA_KEY;
import static ml.docilealligator.infinityforreddit.CommentActivity.WRITE_COMMENT_REQUEST_CODE;

import android.content.Intent;
import android.content.SharedPreferences;
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
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.evernote.android.state.State;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.livefront.bridge.Bridge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Named;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewPostDetailActivity extends AppCompatActivity implements
    FlairBottomSheetFragment.FlairSelectionCallback {

  static final String EXTRA_POST_DATA = "EPD";
  static final String EXTRA_POST_LIST_POSITION = "EPLI";
  static final String EXTRA_POST_ID = "EPI";
  static final String EXTRA_SINGLE_COMMENT_ID = "ESCI";
  static final String EXTRA_MESSAGE_FULLNAME = "ENI";
  static final String EXTRA_NEW_ACCOUNT_NAME = "ENAN";
  static final int EDIT_COMMENT_REQUEST_CODE = 3;
  private static final int EDIT_POST_REQUEST_CODE = 2;
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
  boolean isSingleCommentThreadMode = false;
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
  @State
  String mMessageFullname;
  @State
  String mNewAccountName;
  @BindView(R.id.coordinator_layout_view_post_detail)
  CoordinatorLayout mCoordinatorLayout;
  @BindView(R.id.appbar_layout_view_post_detail_activity)
  AppBarLayout appBarLayout;
  @BindView(R.id.toolbar_view_post_detail_activity)
  Toolbar toolbar;
  @BindView(R.id.progress_bar_view_post_detail_activity)
  ProgressBar mProgressBar;
  @BindView(R.id.recycler_view_view_post_detail)
  RecyclerView mRecyclerView;
  @BindView(R.id.fetch_post_info_linear_layout_view_post_detail_activity)
  LinearLayout mFetchPostInfoLinearLayout;
  @BindView(R.id.fetch_post_info_image_view_view_post_detail_activity)
  ImageView mFetchPostInfoImageView;
  @BindView(R.id.fetch_post_info_text_view_view_post_detail_activity)
  TextView mFetchPostInfoTextView;
  @Inject
  @Named("no_oauth")
  Retrofit mRetrofit;
  @Inject
  @Named("oauth")
  Retrofit mOauthRetrofit;
  @Inject
  RedditDataRoomDatabase mRedditDataRoomDatabase;
  @Inject
  SharedPreferences mSharedPreferences;
  private RequestManager mGlide;
  private Locale mLocale;
  private Menu mMenu;
  private int orientation;
  private int postListPosition = -1;
  private String mSingleCommentId;
  private boolean showToast = false;
  private LinearLayoutManager mLinearLayoutManager;
  private CommentAndPostRecyclerViewAdapter mAdapter;

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
      window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
          WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

      boolean lightNavBar = false;
      if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
          != Configuration.UI_MODE_NIGHT_YES) {
        lightNavBar = true;
      }
      boolean finalLightNavBar = lightNavBar;

      View decorView = window.getDecorView();
      if (finalLightNavBar) {
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
      }
      appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
        @Override
        void onStateChanged(AppBarLayout appBarLayout, State state) {
          if (state == State.COLLAPSED) {
            if (finalLightNavBar) {
              decorView.setSystemUiVisibility(
                  View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
          } else if (state == State.EXPANDED) {
            if (finalLightNavBar) {
              decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
            }
          }
        }
      });

      int statusBarResourceId = getResources()
          .getIdentifier("status_bar_height", "dimen", "android");
      if (statusBarResourceId > 0) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar
            .getLayoutParams();
        params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
        toolbar.setLayoutParams(params);
      }

      int navBarResourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
      if (navBarResourceId > 0) {
        mRecyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
        showToast = true;
      }
    }

    boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    int themeType = Integer
        .parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
    switch (themeType) {
      case 0:
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
        break;
      case 1:
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        break;
      case 2:
        if (systemDefault) {
          AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
          AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
        }

    }

    toolbar.setTitle("");
    setSupportActionBar(toolbar);

    mGlide = Glide.with(this);
    mLocale = getResources().getConfiguration().locale;

    mLinearLayoutManager = new LinearLayoutManager(this);
    mRecyclerView.setLayoutManager(mLinearLayoutManager);

    mSingleCommentId = getIntent().hasExtra(EXTRA_SINGLE_COMMENT_ID) ? getIntent().getExtras()
        .getString(EXTRA_SINGLE_COMMENT_ID) : null;
    if (savedInstanceState == null) {
      if (mSingleCommentId != null) {
        isSingleCommentThreadMode = true;
      }
      mMessageFullname = getIntent().getStringExtra(EXTRA_MESSAGE_FULLNAME);
      mNewAccountName = getIntent().getStringExtra(EXTRA_NEW_ACCOUNT_NAME);
    }

    orientation = getResources().getConfiguration().orientation;

    if (!mNullAccessToken && mAccessToken == null) {
      getCurrentAccountAndBindView();
    } else {
      bindView();
    }

    if (getIntent().hasExtra(EXTRA_POST_LIST_POSITION)) {
      postListPosition = getIntent().getIntExtra(EXTRA_POST_LIST_POSITION, -1);
    }
  }

  private void getCurrentAccountAndBindView() {
    new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
      if (mNewAccountName != null) {
        if (account == null || !account.getUsername().equals(mNewAccountName)) {
          new SwitchAccountAsyncTask(mRedditDataRoomDatabase, mNewAccountName, newAccount -> {
            EventBus.getDefault().post(new SwitchAccountEvent(getClass().getName()));
            Toast.makeText(this, R.string.account_switched, Toast.LENGTH_SHORT).show();

            mNewAccountName = null;
            if (newAccount == null) {
              mNullAccessToken = true;
            } else {
              mAccessToken = newAccount.getAccessToken();
              mAccountName = newAccount.getUsername();
            }

            bindView();
          }).execute();
        } else {
          mAccessToken = account.getAccessToken();
          mAccountName = account.getUsername();
          bindView();
        }
      } else {
        if (account == null) {
          mNullAccessToken = true;
        } else {
          mAccessToken = account.getAccessToken();
          mAccountName = account.getUsername();
        }

        bindView();
      }
    }).execute();
  }

  private void bindView() {
    if (mAccessToken != null && mMessageFullname != null) {
      ReadMessage.readMessage(mOauthRetrofit, mAccessToken, mMessageFullname,
          new ReadMessage.ReadMessageListener() {
            @Override
            public void readSuccess() {
              mMessageFullname = null;
            }

            @Override
            public void readFailed() {

            }
          });
    }

    if (mPost == null) {
      mPost = getIntent().getParcelableExtra(EXTRA_POST_DATA);
    }

    if (mPost == null) {
      fetchPostAndCommentsById(getIntent().getStringExtra(EXTRA_POST_ID));
    } else {
      if (mMenu != null) {
        MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_activity);
        MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_activity);
        if (mAccessToken != null) {
          if (mPost.isSaved()) {
            saveItem.setVisible(true);
            saveItem.setIcon(R.drawable.ic_baseline_bookmark_24px);
          } else {
            saveItem.setVisible(true);
            saveItem.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
          }

          if (mPost.isHidden()) {
            hideItem.setVisible(true);
            hideItem.setTitle(R.string.action_unhide_post);
          } else {
            hideItem.setVisible(true);
            hideItem.setTitle(R.string.action_hide_post);
          }
        } else {
          saveItem.setVisible(false);
          hideItem.setVisible(false);
        }

        if (mPost.getAuthor().equals(mAccountName)) {
          if (mPost.getPostType() == Post.TEXT_TYPE) {
            mMenu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
          }
          mMenu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);

          MenuItem nsfwItem = mMenu.findItem(R.id.action_nsfw_view_post_detail_activity);
          nsfwItem.setVisible(true);
          if (mPost.isNSFW()) {
            nsfwItem.setTitle(R.string.action_unmark_nsfw);
          } else {
            nsfwItem.setTitle(R.string.action_mark_nsfw);
          }

          MenuItem spoilerItem = mMenu.findItem(R.id.action_spoiler_view_post_detail_activity);
          spoilerItem.setVisible(true);
          if (mPost.isSpoiler()) {
            spoilerItem.setTitle(R.string.action_unmark_spoiler);
          } else {
            spoilerItem.setTitle(R.string.action_mark_spoiler);
          }

          mMenu.findItem(R.id.action_edit_flair_view_post_detail_activity).setVisible(true);
        }

        mMenu.findItem(R.id.action_view_crosspost_parent_view_post_detail_activity)
            .setVisible(mPost.getCrosspostParentId() != null);
      }

      mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this, mRetrofit,
          mOauthRetrofit, mRedditDataRoomDatabase, mGlide, mAccessToken, mAccountName, mPost,
          mLocale, mSingleCommentId, isSingleCommentThreadMode,
          new CommentAndPostRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
            @Override
            public void updatePost(Post post) {
              EventBus.getDefault().post(new PostUpdateEventToPostList(mPost, postListPosition));
            }

            @Override
            public void retryFetchingComments() {
              fetchComments(false);
            }

            @Override
            public void retryFetchingMoreComments() {
              isLoadingMoreChildren = false;
              loadMoreChildrenSuccess = true;

              fetchMoreComments();
            }
          });
      mRecyclerView.setAdapter(mAdapter);

      if (comments == null) {
        fetchComments(false);
      } else {
        if (isRefreshing) {
          isRefreshing = false;
          refresh(true, true);
        } else {
          mAdapter.addComments(comments, hasMoreChildren);
          if (isLoadingMoreChildren) {
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
    if (mAccessToken == null) {
      if (isSingleCommentThreadMode && mSingleCommentId != null) {
        postAndComments = mRetrofit.create(RedditAPI.class)
            .getPostAndCommentsSingleThreadById(subredditId, mSingleCommentId);
      } else {
        postAndComments = mRetrofit.create(RedditAPI.class).getPostAndCommentsById(subredditId);
      }
    } else {
      if (isSingleCommentThreadMode && mSingleCommentId != null) {
        postAndComments = mOauthRetrofit.create(RedditAPI.class)
            .getPostAndCommentsSingleThreadByIdOauth(subredditId,
                mSingleCommentId, RedditUtils.getOAuthHeader(mAccessToken));
      } else {
        postAndComments = mOauthRetrofit.create(RedditAPI.class)
            .getPostAndCommentsByIdOauth(subredditId,
                RedditUtils.getOAuthHeader(mAccessToken));
      }
    }
    postAndComments.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        mProgressBar.setVisibility(View.GONE);

        if (response.isSuccessful()) {
          ParsePost.parsePost(response.body(), mLocale, new ParsePost.ParsePostListener() {
            @Override
            public void onParsePostSuccess(Post post) {
              mPost = post;

              if (mMenu != null) {
                MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_activity);
                MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_activity);
                if (mAccessToken != null) {
                  if (post.isSaved()) {
                    saveItem.setVisible(true);
                    saveItem.setIcon(R.drawable.ic_baseline_bookmark_24px);
                  } else {
                    saveItem.setVisible(true);
                    saveItem.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
                  }

                  if (post.isHidden()) {
                    hideItem.setVisible(true);
                    hideItem.setTitle(R.string.action_unhide_post);
                  } else {
                    hideItem.setVisible(true);
                    hideItem.setTitle(R.string.action_hide_post);
                  }
                } else {
                  saveItem.setVisible(false);
                  hideItem.setVisible(false);
                }

                if (mPost.getAuthor().equals(mAccountName)) {
                  if (mPost.getPostType() == Post.TEXT_TYPE) {
                    mMenu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
                  }
                  mMenu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);
                }

                mMenu.findItem(R.id.action_view_crosspost_parent_view_post_detail_activity)
                    .setVisible(mPost.getCrosspostParentId() != null);
              }

              mAdapter = new CommentAndPostRecyclerViewAdapter(ViewPostDetailActivity.this,
                  mRetrofit, mOauthRetrofit, mRedditDataRoomDatabase, mGlide,
                  mAccessToken, mAccountName, mPost, mLocale, mSingleCommentId,
                  isSingleCommentThreadMode,
                  new CommentAndPostRecyclerViewAdapter.CommentRecyclerViewAdapterCallback() {
                    @Override
                    public void updatePost(Post post) {
                      EventBus.getDefault()
                          .post(new PostUpdateEventToPostList(mPost, postListPosition));
                    }

                    @Override
                    public void retryFetchingComments() {
                      fetchComments(false);
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
                    public void onParseCommentSuccess(ArrayList<CommentData> expandedComments,
                        String parentId, ArrayList<String> moreChildrenFullnames) {
                      ViewPostDetailActivity.this.children = moreChildrenFullnames;

                      hasMoreChildren = children.size() != 0;
                      mAdapter.addComments(expandedComments, hasMoreChildren);

                      if (children.size() > 0) {
                        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                          @Override
                          public void onScrolled(@NonNull RecyclerView recyclerView, int dx,
                              int dy) {
                            super.onScrolled(recyclerView, dx, dy);

                            if (!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                              int visibleItemCount = mLinearLayoutManager.getChildCount();
                              int totalItemCount = mLinearLayoutManager.getItemCount();
                              int firstVisibleItemPosition = mLinearLayoutManager
                                  .findFirstVisibleItemPosition();

                              if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount)
                                  && firstVisibleItemPosition >= 0) {
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

  private void fetchComments(boolean changeRefreshState) {
    mAdapter.setSingleComment(mSingleCommentId, isSingleCommentThreadMode);
    mAdapter.initiallyLoading();
    String commentId = null;
    if (isSingleCommentThreadMode) {
      commentId = mSingleCommentId;
    }

    Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
    FetchComment.fetchComments(retrofit, mAccessToken, mPost.getId(), commentId, mLocale,
        new FetchComment.FetchCommentListener() {
          @Override
          public void onFetchCommentSuccess(ArrayList<CommentData> expandedComments,
              String parentId, ArrayList<String> children) {
            ViewPostDetailActivity.this.children = children;

            comments = expandedComments;
            hasMoreChildren = children.size() != 0;
            mAdapter.addComments(expandedComments, hasMoreChildren);

            if (children.size() > 0) {
              mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                  super.onScrolled(recyclerView, dx, dy);

                  if (!isLoadingMoreChildren && loadMoreChildrenSuccess) {
                    int visibleItemCount = mLinearLayoutManager.getChildCount();
                    int totalItemCount = mLinearLayoutManager.getItemCount();
                    int firstVisibleItemPosition = mLinearLayoutManager
                        .findFirstVisibleItemPosition();

                    if ((visibleItemCount + firstVisibleItemPosition >= totalItemCount)
                        && firstVisibleItemPosition >= 0) {
                      fetchMoreComments();
                    }
                  }
                }
              });
            }
            if (changeRefreshState) {
              isRefreshing = false;
            }
          }

          @Override
          public void onFetchCommentFailed() {
            mAdapter.initiallyLoadCommentsFailed();
            if (changeRefreshState) {
              isRefreshing = false;
            }
          }
        });
  }

  void fetchMoreComments() {
    if (isLoadingMoreChildren || !loadMoreChildrenSuccess) {
      return;
    }

    isLoadingMoreChildren = true;

    Retrofit retrofit = mAccessToken == null ? mRetrofit : mOauthRetrofit;
    FetchComment.fetchMoreComment(retrofit, mAccessToken, children, mChildrenStartingIndex,
        0, mLocale, new FetchComment.FetchMoreCommentListener() {
          @Override
          public void onFetchMoreCommentSuccess(ArrayList<CommentData> expandedComments,
              int childrenStartingIndex) {
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

  private void refresh(boolean fetchPost, boolean fetchComments) {
    if (!isRefreshing) {
      isRefreshing = true;
      mChildrenStartingIndex = 0;

      mFetchPostInfoLinearLayout.setVisibility(View.GONE);
      mGlide.clear(mFetchPostInfoImageView);

      if (fetchComments) {
        if (!fetchPost) {
          fetchComments(true);
        } else {
          fetchComments(false);
        }
      }

      if (fetchPost) {
        Retrofit retrofit;
        if (mAccessToken == null) {
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
                if (mMenu != null) {
                  MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_activity);
                  MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_activity);
                  if (mAccessToken != null) {
                    if (post.isSaved()) {
                      saveItem.setVisible(true);
                      saveItem.setIcon(R.drawable.ic_baseline_bookmark_24px);
                    } else {
                      saveItem.setVisible(true);
                      saveItem.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
                    }

                    if (post.isHidden()) {
                      hideItem.setVisible(true);
                      hideItem.setTitle(R.string.action_unhide_post);
                    } else {
                      hideItem.setVisible(true);
                      hideItem.setTitle(R.string.action_hide_post);
                    }
                  } else {
                    saveItem.setVisible(false);
                    hideItem.setVisible(false);
                  }

                  mMenu.findItem(R.id.action_view_crosspost_parent_view_post_detail_activity)
                      .setVisible(mPost.getCrosspostParentId() != null);
                }
              }

              @Override
              public void fetchPostFailed() {
                showMessage(R.string.refresh_post_failed);
                isRefreshing = false;
              }
            });
      }
    }
  }

  private void showErrorView(String subredditId) {
    mProgressBar.setVisibility(View.GONE);
    mFetchPostInfoLinearLayout.setVisibility(View.VISIBLE);
    mFetchPostInfoLinearLayout.setOnClickListener(view -> fetchPostAndCommentsById(subredditId));
    mFetchPostInfoTextView.setText(R.string.load_post_error);
    mGlide.load(R.drawable.error_image).into(mFetchPostInfoImageView);
  }

  private void showMessage(int resId) {
    if (showToast) {
      Toast.makeText(ViewPostDetailActivity.this, resId, Toast.LENGTH_SHORT).show();
    } else {
      Snackbar.make(mCoordinatorLayout, resId, Snackbar.LENGTH_SHORT).show();
    }
  }

  private void markNSFW() {
    if (mMenu != null) {
      mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
          .setTitle(R.string.action_unmark_nsfw);
    }

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ID_KEY, mPost.getFullName());
    mOauthRetrofit.create(RedditAPI.class)
        .markNSFW(RedditUtils.getOAuthHeader(mAccessToken), params)
        .enqueue(new Callback<String>() {
          @Override
          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            if (response.isSuccessful()) {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                    .setTitle(R.string.action_unmark_nsfw);
              }

              refresh(true, false);
              showMessage(R.string.mark_nsfw_success);
            } else {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                    .setTitle(R.string.action_mark_nsfw);
              }

              showMessage(R.string.mark_nsfw_failed);
            }
          }

          @Override
          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            if (mMenu != null) {
              mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                  .setTitle(R.string.action_mark_nsfw);
            }

            showMessage(R.string.mark_nsfw_failed);
          }
        });
  }

  private void unmarkNSFW() {
    if (mMenu != null) {
      mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
          .setTitle(R.string.action_mark_nsfw);
    }

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ID_KEY, mPost.getFullName());
    mOauthRetrofit.create(RedditAPI.class)
        .unmarkNSFW(RedditUtils.getOAuthHeader(mAccessToken), params)
        .enqueue(new Callback<String>() {
          @Override
          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            if (response.isSuccessful()) {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                    .setTitle(R.string.action_mark_nsfw);
              }

              refresh(true, false);
              showMessage(R.string.unmark_nsfw_success);
            } else {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                    .setTitle(R.string.action_unmark_nsfw);
              }

              showMessage(R.string.unmark_nsfw_failed);
            }
          }

          @Override
          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            if (mMenu != null) {
              mMenu.findItem(R.id.action_nsfw_view_post_detail_activity)
                  .setTitle(R.string.action_unmark_nsfw);
            }

            showMessage(R.string.unmark_nsfw_failed);
          }
        });
  }

  private void markSpoiler() {
    if (mMenu != null) {
      mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
          .setTitle(R.string.action_unmark_spoiler);
    }

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ID_KEY, mPost.getFullName());
    mOauthRetrofit.create(RedditAPI.class)
        .markSpoiler(RedditUtils.getOAuthHeader(mAccessToken), params)
        .enqueue(new Callback<String>() {
          @Override
          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            if (response.isSuccessful()) {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                    .setTitle(R.string.action_unmark_spoiler);
              }

              refresh(true, false);
              showMessage(R.string.mark_spoiler_success);
            } else {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                    .setTitle(R.string.action_mark_spoiler);
              }

              showMessage(R.string.mark_spoiler_failed);
            }
          }

          @Override
          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            if (mMenu != null) {
              mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                  .setTitle(R.string.action_mark_spoiler);
            }

            showMessage(R.string.mark_spoiler_failed);
          }
        });
  }

  private void unmarkSpoiler() {
    if (mMenu != null) {
      mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
          .setTitle(R.string.action_mark_spoiler);
    }

    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.ID_KEY, mPost.getFullName());
    mOauthRetrofit.create(RedditAPI.class)
        .unmarkSpoiler(RedditUtils.getOAuthHeader(mAccessToken), params)
        .enqueue(new Callback<String>() {
          @Override
          public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
            if (response.isSuccessful()) {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                    .setTitle(R.string.action_mark_spoiler);
              }

              refresh(true, false);
              showMessage(R.string.unmark_spoiler_success);
            } else {
              if (mMenu != null) {
                mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                    .setTitle(R.string.action_unmark_spoiler);
              }

              showMessage(R.string.unmark_spoiler_failed);
            }
          }

          @Override
          public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
            if (mMenu != null) {
              mMenu.findItem(R.id.action_spoiler_view_post_detail_activity)
                  .setTitle(R.string.action_unmark_spoiler);
            }

            showMessage(R.string.unmark_spoiler_failed);
          }
        });
  }

  void deleteComment(String fullName, int position) {
    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
        .setTitle(R.string.delete_this_comment)
        .setMessage(R.string.are_you_sure)
        .setPositiveButton(R.string.delete, (dialogInterface, i)
            -> DeleteThing
            .delete(mOauthRetrofit, fullName, mAccessToken, new DeleteThing.DeleteThingListener() {
              @Override
              public void deleteSuccess() {
                Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_success,
                    Toast.LENGTH_SHORT).show();
                mAdapter.deleteComment(position);
              }

              @Override
              public void deleteFailed() {
                Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_failed,
                    Toast.LENGTH_SHORT).show();
              }
            }))
        .setNegativeButton(R.string.cancel, null)
        .show();
  }

  void changeToSingleThreadMode() {
    isSingleCommentThreadMode = false;
    mSingleCommentId = null;
    refresh(false, true);
  }

  @Subscribe
  public void onPostUpdateEvent(PostUpdateEventToDetailActivity event) {
    if (mPost.getId().equals(event.postId)) {
      mPost.setVoteType(event.voteType);
      mAdapter.updatePost(mPost);
    }
  }

  @Subscribe
  public void onAccountSwitchEvent(SwitchAccountEvent event) {
    if (!getClass().getName().equals(event.excludeActivityClassName)) {
      finish();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.view_post_detail_activity, menu);
    mMenu = menu;
    if (mPost != null) {
      MenuItem saveItem = mMenu.findItem(R.id.action_save_view_post_detail_activity);
      MenuItem hideItem = mMenu.findItem(R.id.action_hide_view_post_detail_activity);
      if (mAccessToken != null) {
        if (mPost.isSaved()) {
          saveItem.setVisible(true);
          saveItem.setIcon(R.drawable.ic_baseline_bookmark_24px);
        } else {
          saveItem.setVisible(true);
          saveItem.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
        }

        if (mPost.isHidden()) {
          hideItem.setVisible(true);
          hideItem.setTitle(R.string.action_unhide_post);
        } else {
          hideItem.setVisible(true);
          hideItem.setTitle(R.string.action_hide_post);
        }
      } else {
        saveItem.setVisible(false);
        hideItem.setVisible(false);
      }

      if (mPost.getAuthor().equals(mAccountName)) {
        if (mPost.getPostType() == Post.TEXT_TYPE) {
          menu.findItem(R.id.action_edit_view_post_detail_activity).setVisible(true);
        }
        menu.findItem(R.id.action_delete_view_post_detail_activity).setVisible(true);

        MenuItem nsfwItem = menu.findItem(R.id.action_nsfw_view_post_detail_activity);
        nsfwItem.setVisible(true);
        if (mPost.isNSFW()) {
          nsfwItem.setTitle(R.string.action_unmark_nsfw);
        } else {
          nsfwItem.setTitle(R.string.action_mark_nsfw);
        }

        MenuItem spoilerItem = menu.findItem(R.id.action_spoiler_view_post_detail_activity);
        spoilerItem.setVisible(true);
        if (mPost.isSpoiler()) {
          spoilerItem.setTitle(R.string.action_unmark_spoiler);
        } else {
          spoilerItem.setTitle(R.string.action_mark_spoiler);
        }

        menu.findItem(R.id.action_edit_flair_view_post_detail_activity).setVisible(true);
      }

      menu.findItem(R.id.action_view_crosspost_parent_view_post_detail_activity)
          .setVisible(mPost.getCrosspostParentId() != null);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_refresh_view_post_detail_activity:
        refresh(true, true);
        return true;
      case R.id.action_comment_view_post_detail_activity:
        if (mPost.isArchived()) {
          showMessage(R.string.archived_post_reply_unavailable);
          return true;
        }

        if (mPost.isLocked()) {
          showMessage(R.string.locked_post_comment_unavailable);
          return true;
        }

        if (mAccessToken == null) {
          showMessage(R.string.login_first);
          return true;
        }

        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra(CommentActivity.EXTRA_COMMENT_PARENT_TEXT_KEY, mPost.getTitle());
        intent.putExtra(CommentActivity.EXTRA_PARENT_FULLNAME_KEY, mPost.getFullName());
        intent.putExtra(CommentActivity.EXTRA_PARENT_DEPTH_KEY, 0);
        intent.putExtra(CommentActivity.EXTRA_IS_REPLYING_KEY, false);
        startActivityForResult(intent, WRITE_COMMENT_REQUEST_CODE);
        return true;
      case R.id.action_save_view_post_detail_activity:
        if (mPost != null && mAccessToken != null) {
          if (mPost.isSaved()) {
            item.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
            SaveThing.unsaveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                new SaveThing.SaveThingListener() {
                  @Override
                  public void success() {
                    mPost.setSaved(false);
                    item.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
                    showMessage(R.string.post_unsaved_success);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }

                  @Override
                  public void failed() {
                    mPost.setSaved(true);
                    item.setIcon(R.drawable.ic_baseline_bookmark_24px);
                    showMessage(R.string.post_unsaved_failed);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }
                });
          } else {
            item.setIcon(R.drawable.ic_baseline_bookmark_24px);
            SaveThing.saveThing(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                new SaveThing.SaveThingListener() {
                  @Override
                  public void success() {
                    mPost.setSaved(true);
                    item.setIcon(R.drawable.ic_baseline_bookmark_24px);
                    showMessage(R.string.post_saved_success);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }

                  @Override
                  public void failed() {
                    mPost.setSaved(false);
                    item.setIcon(R.drawable.ic_baseline_bookmark_border_24px);
                    showMessage(R.string.post_saved_failed);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }
                });
          }
        }
        return true;
      case R.id.action_view_crosspost_parent_view_post_detail_activity:
        Intent crosspostIntent = new Intent(this, ViewPostDetailActivity.class);
        crosspostIntent
            .putExtra(ViewPostDetailActivity.EXTRA_POST_ID, mPost.getCrosspostParentId());
        startActivity(crosspostIntent);
        return true;
      case R.id.action_hide_view_post_detail_activity:
        if (mPost != null && mAccessToken != null) {
          if (mPost.isHidden()) {
            item.setTitle(R.string.action_hide_post);

            HidePost.unhidePost(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                new HidePost.HidePostListener() {
                  @Override
                  public void success() {
                    mPost.setHidden(false);
                    item.setTitle(R.string.action_hide_post);
                    showMessage(R.string.post_unhide_success);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }

                  @Override
                  public void failed() {
                    mPost.setHidden(true);
                    item.setTitle(R.string.action_unhide_post);
                    showMessage(R.string.post_unhide_failed);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }
                });
          } else {
            item.setTitle(R.string.action_unhide_post);

            HidePost.hidePost(mOauthRetrofit, mAccessToken, mPost.getFullName(),
                new HidePost.HidePostListener() {
                  @Override
                  public void success() {
                    mPost.setHidden(true);
                    item.setTitle(R.string.action_unhide_post);
                    showMessage(R.string.post_hide_success);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }

                  @Override
                  public void failed() {
                    mPost.setHidden(false);
                    item.setTitle(R.string.action_hide_post);
                    showMessage(R.string.post_hide_failed);
                    EventBus.getDefault()
                        .post(new PostUpdateEventToPostList(mPost, postListPosition));
                  }
                });
          }
        }
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
                -> DeleteThing.delete(mOauthRetrofit, mPost.getFullName(), mAccessToken,
                new DeleteThing.DeleteThingListener() {
                  @Override
                  public void deleteSuccess() {
                    Toast.makeText(ViewPostDetailActivity.this, R.string.delete_post_success,
                        Toast.LENGTH_SHORT).show();
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
        if (mPost.isNSFW()) {
          unmarkNSFW();
        } else {
          markNSFW();
        }
        return true;
      case R.id.action_spoiler_view_post_detail_activity:
        if (mPost.isSpoiler()) {
          unmarkSpoiler();
        } else {
          markSpoiler();
        }
        return true;
      case R.id.action_edit_flair_view_post_detail_activity:
        FlairBottomSheetFragment flairBottomSheetFragment = new FlairBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, mPost.getSubredditName());
        flairBottomSheetFragment.setArguments(bundle);
        flairBottomSheetFragment
            .show(getSupportFragmentManager(), flairBottomSheetFragment.getTag());
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
    if (requestCode == WRITE_COMMENT_REQUEST_CODE) {
      if (data != null && resultCode == RESULT_OK) {
        if (data.hasExtra(EXTRA_COMMENT_DATA_KEY)) {
          CommentData comment = data.getExtras().getParcelable(EXTRA_COMMENT_DATA_KEY);
          if (comment.getDepth() == 0) {
            mAdapter.addComment(comment);
          } else {
            String parentFullname = data.getExtras()
                .getString(CommentActivity.EXTRA_PARENT_FULLNAME_KEY);
            int parentPosition = data.getExtras().getInt(CommentActivity.EXTRA_PARENT_POSITION_KEY);
            mAdapter.addChildComment(comment, parentFullname, parentPosition);
          }
        } else {
          Toast.makeText(this, R.string.send_comment_failed, Toast.LENGTH_SHORT).show();
        }
      }
    } else if (requestCode == EDIT_POST_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        refresh(true, false);
      }
    } else if (requestCode == EDIT_COMMENT_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        mAdapter.editComment(
            data.getExtras().getString(EditCommentActivity.EXTRA_EDITED_COMMENT_CONTENT),
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
    if (orientation == getResources().getConfiguration().orientation) {
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
  }

  @Override
  public void flairSelected(Flair flair) {
    Map<String, String> params = new HashMap<>();
    params.put(RedditUtils.API_TYPE_KEY, RedditUtils.API_TYPE_JSON);
    params.put(RedditUtils.FLAIR_TEMPLATE_ID_KEY, flair.getId());
    params.put(RedditUtils.LINK_KEY, mPost.getFullName());
    params.put(RedditUtils.TEXT_KEY, flair.getText());

    mOauthRetrofit.create(RedditAPI.class).selectFlair(mPost.getSubredditNamePrefixed(),
        RedditUtils.getOAuthHeader(mAccessToken), params).enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        if (response.isSuccessful()) {
          refresh(true, false);
          showMessage(R.string.update_flair_success);
        } else {
          showMessage(R.string.update_flair_failed);
        }
      }

      @Override
      public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        showMessage(R.string.update_flair_failed);
      }
    });
  }
}
