package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostPagingSource extends ListenableFuturePagingSource<String, Post> {
    public static final int TYPE_FRONT_PAGE = 0;
    public static final int TYPE_SUBREDDIT = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_SEARCH = 3;
    public static final int TYPE_MULTI_REDDIT = 4;
    public static final int TYPE_ANONYMOUS_FRONT_PAGE = 5;
    public static final int TYPE_ANONYMOUS_MULTIREDDIT = 6;

    public static final String USER_WHERE_SUBMITTED = "submitted";
    public static final String USER_WHERE_UPVOTED = "upvoted";
    public static final String USER_WHERE_DOWNVOTED = "downvoted";
    public static final String USER_WHERE_HIDDEN = "hidden";
    public static final String USER_WHERE_SAVED = "saved";
    public static final String USER_WHERE_GILDED = "gilded";

    private Executor executor;
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String subredditOrUserName;
    private String query;
    private String trendingSource;
    private int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private List<String> readPostList;
    private String userWhere;
    private String multiRedditPath;
    private LinkedHashSet<Post> postLinkedHashSet;

    PostPagingSource(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences,
                     SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                     SortType sortType, PostFilter postFilter, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.BEST) : sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String path, int postType, SortType sortType, PostFilter postFilter,
                     List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        if (postType == TYPE_SUBREDDIT || postType == TYPE_ANONYMOUS_FRONT_PAGE) {
            this.subredditOrUserName = path;
        } else {
            if (sortType != null) {
                if (path.endsWith("/")) {
                    multiRedditPath = path + sortType.getType().value;
                } else {
                    multiRedditPath = path + "/" + sortType.getType().value;
                }
            } else {
                multiRedditPath = path;
            }
        }
        this.postType = postType;
        if (sortType == null) {
            if (path.equals("popular") || path.equals("all")) {
                this.sortType = new SortType(SortType.Type.HOT);
            } else {
                this.sortType = new SortType(SortType.Type.BEST);
            }
        } else {
            this.sortType = sortType;
        }
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String subredditOrUserName, int postType, SortType sortType, PostFilter postFilter,
                     String where, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.NEW) : sortType;
        this.postFilter = postFilter;
        userWhere = where;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostPagingSource(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                     SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                     String subredditOrUserName, String query, String trendingSource, int postType,
                     SortType sortType, PostFilter postFilter, List<String> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        this.query = query;
        this.trendingSource = trendingSource;
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.RELEVANCE) : sortType;
        this.postFilter = postFilter;
        postLinkedHashSet = new LinkedHashSet<>();
        this.readPostList = readPostList;
    }

    @Nullable
    @Override
    public String getRefreshKey(@NonNull PagingState<String, Post> pagingState) {
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<String, Post>> loadFuture(@NonNull LoadParams<String> loadParams) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        switch (postType) {
            case TYPE_FRONT_PAGE:
                return loadHomePosts(loadParams, api);
            case TYPE_SUBREDDIT:
                return loadSubredditPosts(loadParams, api);
            case TYPE_USER:
                return loadUserPosts(loadParams, api);
            case TYPE_SEARCH:
                return loadSearchPosts(loadParams, api);
            case TYPE_MULTI_REDDIT:
                return loadMultiRedditPosts(loadParams, api);
            default:
                return loadAnonymousHomePosts(loadParams, api);
        }
    }

    public LoadResult<String, Post> transformData(Response<String> response) {
        if (response.isSuccessful()) {
            String responseString = response.body();
            LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, readPostList);
            String lastItem = ParsePost.getLastItem(responseString);
            if (newPosts == null) {
                return new LoadResult.Error<>(new Exception("Error parsing posts"));
            } else {
                int currentPostsSize = postLinkedHashSet.size();
                postLinkedHashSet.addAll(newPosts);
                if (currentPostsSize == postLinkedHashSet.size()) {
                    return new LoadResult.Page<>(new ArrayList<>(), null, lastItem);
                } else {
                    return new LoadResult.Page<>(new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size()), null, lastItem);
                }
            }
        } else {
            return new LoadResult.Error<>(new Exception("Response failed"));
        }
    }

    private ListenableFuture<LoadResult<String, Post>> loadHomePosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> bestPost;
        String afterKey;
        if (loadParams.getKey() == null) {
            boolean savePostFeedScrolledPosition = sortType != null && sortType.getType() == SortType.Type.BEST && sharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION, false);
            if (savePostFeedScrolledPosition) {
                String accountNameForCache = accountName == null ? SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_ANONYMOUS : accountName;
                afterKey = postFeedScrolledPositionSharedPreferences.getString(accountNameForCache + SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_FRONT_PAGE_BASE, null);
            } else {
                afterKey = null;
            }
        } else {
            afterKey = loadParams.getKey();
        }
        if (sortType.getTime() != null) {
            bestPost = api.getBestPostsListenableFuture(sortType.getType().value, sortType.getTime().value, afterKey,
                    APIUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPostsListenableFuture(sortType.getType().value, afterKey, APIUtils.getOAuthHeader(accessToken));
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(bestPost, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadSubredditPosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> subredditPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                subredditPost = api.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType().value, sortType.getTime().value, loadParams.getKey());
            } else {
                subredditPost = api.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType().value, loadParams.getKey());
            }
        } else {
            if (sortType.getTime() != null) {
                subredditPost = api.getSubredditBestPostsOauthListenableFuture(subredditOrUserName, sortType.getType().value,
                        sortType.getTime().value, loadParams.getKey(), APIUtils.getOAuthHeader(accessToken));
            } else {
                subredditPost = api.getSubredditBestPostsOauthListenableFuture(subredditOrUserName, sortType.getType().value,
                        loadParams.getKey(), APIUtils.getOAuthHeader(accessToken));
            }
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(subredditPost, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadUserPosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> userPosts;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                userPosts = api.getUserPostsListenableFuture(subredditOrUserName, loadParams.getKey(), sortType.getType().value,
                        sortType.getTime().value);
            } else {
                userPosts = api.getUserPostsListenableFuture(subredditOrUserName, loadParams.getKey(), sortType.getType().value);
            }
        } else {
            if (sortType.getTime() != null) {
                userPosts = api.getUserPostsOauthListenableFuture(subredditOrUserName, userWhere, loadParams.getKey(), sortType.getType().value,
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                userPosts = api.getUserPostsOauthListenableFuture(subredditOrUserName, userWhere, loadParams.getKey(), sortType.getType().value,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(userPosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadSearchPosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> searchPosts;
        if (subredditOrUserName == null) {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    searchPosts = api.searchPostsListenableFuture(query, loadParams.getKey(), sortType.getType().value, sortType.getTime().value,
                            trendingSource);
                } else {
                    searchPosts = api.searchPostsListenableFuture(query, loadParams.getKey(), sortType.getType().value, trendingSource);
                }
            } else {
                if (sortType.getTime() != null) {
                    searchPosts = api.searchPostsOauthListenableFuture(query, loadParams.getKey(), sortType.getType().value,
                            sortType.getTime().value, trendingSource, APIUtils.getOAuthHeader(accessToken));
                } else {
                    searchPosts = api.searchPostsOauthListenableFuture(query, loadParams.getKey(), sortType.getType().value, trendingSource,
                            APIUtils.getOAuthHeader(accessToken));
                }
            }
        } else {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    searchPosts = api.searchPostsInSpecificSubredditListenableFuture(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, loadParams.getKey());
                } else {
                    searchPosts = api.searchPostsInSpecificSubredditListenableFuture(subredditOrUserName, query,
                            sortType.getType().value, loadParams.getKey());
                }
            } else {
                if (sortType.getTime() != null) {
                    searchPosts = api.searchPostsInSpecificSubredditOauthListenableFuture(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, loadParams.getKey(),
                            APIUtils.getOAuthHeader(accessToken));
                } else {
                    searchPosts = api.searchPostsInSpecificSubredditOauthListenableFuture(subredditOrUserName, query,
                            sortType.getType().value, loadParams.getKey(),
                            APIUtils.getOAuthHeader(accessToken));
                }
            }
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(searchPosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadMultiRedditPosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> multiRedditPosts;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                multiRedditPosts = api.getMultiRedditPostsListenableFuture(multiRedditPath, loadParams.getKey(), sortType.getTime().value);
            } else {
                multiRedditPosts = api.getMultiRedditPostsListenableFuture(multiRedditPath, loadParams.getKey());
            }
        } else {
            if (sortType.getTime() != null) {
                multiRedditPosts = api.getMultiRedditPostsOauthListenableFuture(multiRedditPath, loadParams.getKey(),
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                multiRedditPosts = api.getMultiRedditPostsOauthListenableFuture(multiRedditPath, loadParams.getKey(),
                        APIUtils.getOAuthHeader(accessToken));
            }
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(multiRedditPosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    private ListenableFuture<LoadResult<String, Post>> loadAnonymousHomePosts(@NonNull LoadParams<String> loadParams, RedditAPI api) {
        ListenableFuture<Response<String>> anonymousHomePosts;
        if (sortType.getTime() != null) {
            anonymousHomePosts = api.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType().value, sortType.getTime().value, loadParams.getKey());
        } else {
            anonymousHomePosts = api.getSubredditBestPostsListenableFuture(subredditOrUserName, sortType.getType().value, loadParams.getKey());
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(anonymousHomePosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }
}
