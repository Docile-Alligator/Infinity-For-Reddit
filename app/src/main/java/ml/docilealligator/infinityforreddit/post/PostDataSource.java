package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.PostFilter;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PostDataSource extends PageKeyedDataSource<String, Post> {

    public static final int TYPE_FRONT_PAGE = 0;
    public static final int TYPE_SUBREDDIT = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_SEARCH = 3;
    public static final int TYPE_MULTI_REDDIT = 4;

    public static final String USER_WHERE_SUBMITTED = "submitted";
    public static final String USER_WHERE_UPVOTED = "upvoted";
    public static final String USER_WHERE_DOWNVOTED = "downvoted";
    public static final String USER_WHERE_HIDDEN = "hidden";
    public static final String USER_WHERE_SAVED = "saved";
    public static final String USER_WHERE_GILDED = "gilded";

    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String subredditOrUserName;
    private String query;
    private int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private List<ReadPost> readPostList;
    private List<SubredditFilter> subredditFilterList;
    private String userWhere;
    private String multiRedditPath;
    private LinkedHashSet<Post> postLinkedHashSet;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Post> callback;

    PostDataSource(Retrofit retrofit, String accessToken, String accountName,
                   SharedPreferences sharedPreferences,
                   SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                   SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.BEST) : sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostDataSource(Retrofit retrofit, String accessToken, String accountName,
                   SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                   String path, int postType, SortType sortType, PostFilter postFilter,
                   List<ReadPost> readPostList, List<SubredditFilter> subredditFilterList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        if (postType == TYPE_SUBREDDIT) {
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
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
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
        this.subredditFilterList = subredditFilterList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostDataSource(Retrofit retrofit, String accessToken, String accountName,
                   SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                   String subredditOrUserName, int postType, SortType sortType, PostFilter postFilter,
                   String where, List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.NEW) : sortType;
        this.postFilter = postFilter;
        userWhere = where;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    PostDataSource(Retrofit retrofit, String accessToken, String accountName,
                   SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                   String subredditOrUserName, String query, int postType, SortType sortType, PostFilter postFilter,
                   List<ReadPost> readPostList) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.subredditOrUserName = subredditOrUserName;
        this.query = query;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.RELEVANCE) : sortType;
        this.postFilter = postFilter;
        postLinkedHashSet = new LinkedHashSet<>();
        this.readPostList = readPostList;
    }

    MutableLiveData<NetworkState> getPaginationNetworkStateLiveData() {
        return paginationNetworkStateLiveData;
    }

    MutableLiveData<NetworkState> getInitialLoadStateLiveData() {
        return initialLoadStateLiveData;
    }

    MutableLiveData<Boolean> hasPostLiveData() {
        return hasPostLiveData;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Post> callback) {
        initialLoadStateLiveData.postValue(NetworkState.LOADING);
        switch (postType) {
            case TYPE_FRONT_PAGE:
                boolean savePostFeedScrolledPosition = sortType != null && sortType.getType() == SortType.Type.BEST && sharedPreferences.getBoolean(SharedPreferencesUtils.SAVE_FRONT_PAGE_SCROLLED_POSITION, false);
                String accountNameForCache = accountName == null ? SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_ANONYMOUS : accountName;
                if (savePostFeedScrolledPosition) {
                    loadBestPostsInitial(callback, postFeedScrolledPositionSharedPreferences.getString(accountNameForCache + SharedPreferencesUtils.FRONT_PAGE_SCROLLED_POSITION_FRONT_PAGE_BASE, null));
                } else {
                    loadBestPostsInitial(callback, null);
                }
                break;
            case TYPE_SUBREDDIT:
                loadSubredditPostsInitial(callback, null);
                break;
            case TYPE_USER:
                loadUserPostsInitial(callback, null);
                break;
            case TYPE_SEARCH:
                loadSearchPostsInitial(callback, null);
                break;
            case TYPE_MULTI_REDDIT:
                loadMultiRedditPostsInitial(callback, null);
                break;
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Post> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback) {
        this.params = params;
        this.callback = callback;

        if (params == null || "".equals(params.key) || "null".equals(params.key)) {
            return;
        }

        paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

        switch (postType) {
            case TYPE_FRONT_PAGE:
                loadBestPostsAfter(params, callback, null);
                break;
            case TYPE_SUBREDDIT:
                loadSubredditPostsAfter(params, callback, null);
                break;
            case TYPE_USER:
                loadUserPostsAfter(params, callback, null);
                break;
            case TYPE_SEARCH:
                loadSearchPostsAfter(params, callback, null);
                break;
            case TYPE_MULTI_REDDIT:
                loadMultiRedditPostsAfter(params, callback, null);
                break;
        }
    }

    private void loadBestPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> bestPost;
        if(sortType.getTime() != null) {
            bestPost = api.getBestPosts(sortType.getType().value, sortType.getTime().value, lastItem,
                    APIUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPosts(sortType.getType().value, lastItem, APIUtils.getOAuthHeader(accessToken));
        }
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadBestPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing posts"));
                                }
                            });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED,
                            "code: " + response.code() + " message: " + response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED,
                        errorMessage + " " + call.request().url().toString()));
            }
        });
    }

    private void loadBestPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback, String lastItem) {
        String after = lastItem == null ? params.key : lastItem;

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> bestPost;
        if(sortType.getTime() != null) {
            bestPost = api.getBestPosts(sortType.getType().value, sortType.getTime().value, after,
                    APIUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPosts(sortType.getType().value, after, APIUtils.getOAuthHeader(accessToken));
        }

        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadBestPostsAfter(params, callback, lastItem);
                                    } else {
                                        int currentPostsSize = postLinkedHashSet.size();
                                        postLinkedHashSet.addAll(newPosts);
                                        if (currentPostsSize == postLinkedHashSet.size()) {
                                            loadBestPostsAfter(params, callback, lastItem);
                                        } else {
                                            List<Post> newPostsList = new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size());
                                            callback.onResult(newPostsList, lastItem);
                                        }
                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing more posts"));
                                }
                            });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadSubredditPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getSubredditBestPosts(subredditOrUserName, sortType.getType().value, sortType.getTime().value, lastItem);
            } else {
                getPost = api.getSubredditBestPosts(subredditOrUserName, sortType.getType().value, lastItem);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        sortType.getTime().value, lastItem, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        lastItem, APIUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList, subredditFilterList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadSubredditPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing posts"));
                                }
                            });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED,
                            "code: " + response + " message: " + response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED,
                        errorMessage + " " + call.request().url().toString()));
            }
        });
    }

    private void loadSubredditPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback, String lastItem) {
        String after = lastItem == null ? params.key : lastItem;

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getSubredditBestPosts(subredditOrUserName, sortType.getType().value,
                        sortType.getTime().value, after);
            } else {
                getPost = api.getSubredditBestPosts(subredditOrUserName, sortType.getType().value, after);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        sortType.getTime().value, after, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        after, APIUtils.getOAuthHeader(accessToken));
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList, subredditFilterList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadSubredditPostsAfter(params, callback, lastItem);
                                    } else {
                                        int currentPostsSize = postLinkedHashSet.size();
                                        postLinkedHashSet.addAll(newPosts);
                                        if (currentPostsSize == postLinkedHashSet.size()) {
                                            loadSubredditPostsAfter(params, callback, lastItem);
                                        } else {
                                            List<Post> newPostsList = new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size());
                                            callback.onResult(newPostsList, lastItem);
                                        }
                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadUserPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getUserPosts(subredditOrUserName, lastItem, sortType.getType().value,
                        sortType.getTime().value);
            } else {
                getPost = api.getUserPosts(subredditOrUserName, lastItem, sortType.getType().value);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, lastItem, sortType.getType().value,
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, lastItem, sortType.getType().value,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadUserPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadUserPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback, String lastItem) {
        String after = lastItem == null ? params.key : lastItem;

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getUserPosts(subredditOrUserName, after, sortType.getType().value,
                        sortType.getTime().value);
            } else {
                getPost = api.getUserPosts(subredditOrUserName, after, sortType.getType().value);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, after, sortType.getType().value,
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, after, sortType.getType().value,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadUserPostsAfter(params, callback, lastItem);
                                    } else {
                                        int currentPostsSize = postLinkedHashSet.size();
                                        postLinkedHashSet.addAll(newPosts);
                                        if (currentPostsSize == postLinkedHashSet.size()) {
                                            loadUserPostsAfter(params, callback, lastItem);
                                        } else {
                                            List<Post> newPostsList = new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size());
                                            callback.onResult(newPostsList, lastItem);
                                        }
                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadSearchPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost;

        if (subredditOrUserName == null) {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    getPost = api.searchPosts(query, lastItem, sortType.getType().value, sortType.getTime().value);
                } else {
                    getPost = api.searchPosts(query, lastItem, sortType.getType().value);
                }
            } else {
                if(sortType.getTime() != null) {
                    getPost = api.searchPostsOauth(query, lastItem, sortType.getType().value,
                            sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsOauth(query, lastItem, sortType.getType().value,
                            APIUtils.getOAuthHeader(accessToken));
                }
            }
        } else {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, lastItem);
                } else {
                    getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query,
                            sortType.getType().value, lastItem);
                }
            } else {
                if (sortType.getTime() != null) {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, lastItem,
                            APIUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, lastItem,
                            APIUtils.getOAuthHeader(accessToken));
                }
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadSearchPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadSearchPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback, String lastItem) {
        String after = lastItem == null ? params.key : lastItem;

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost;

        if (subredditOrUserName == null) {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    getPost = api.searchPosts(query, after, sortType.getType().value, sortType.getTime().value);
                } else {
                    getPost = api.searchPosts(query, after, sortType.getType().value);
                }
            } else {
                if (sortType.getTime() != null) {
                    getPost = api.searchPostsOauth(query, after, sortType.getType().value,
                            sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsOauth(query, after, sortType.getType().value, APIUtils.getOAuthHeader(accessToken));
                }
            }
        } else {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, after);
                } else {
                    getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query,
                            sortType.getType().value, after);
                }
            } else {
                if (sortType.getTime() != null) {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, sortType.getTime().value, after,
                            APIUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, after, APIUtils.getOAuthHeader(accessToken));
                }
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadSearchPostsAfter(params, callback, lastItem);
                                    } else {
                                        int currentPostsSize = postLinkedHashSet.size();
                                        postLinkedHashSet.addAll(newPosts);
                                        if (currentPostsSize == postLinkedHashSet.size()) {
                                            loadSearchPostsAfter(params, callback, lastItem);
                                        } else {
                                            List<Post> newPostsList = new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size());
                                            callback.onResult(newPostsList, lastItem);
                                        }
                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadMultiRedditPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getMultiRedditPosts(multiRedditPath, lastItem, sortType.getTime().value);
            } else {
                getPost = api.getMultiRedditPosts(multiRedditPath, lastItem);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getMultiRedditPostsOauth(multiRedditPath, lastItem,
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getMultiRedditPostsOauth(multiRedditPath, lastItem,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadMultiRedditPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        postLinkedHashSet.addAll(newPosts);
                                        callback.onResult(new ArrayList<>(newPosts), null, nextPageKey);
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    private void loadMultiRedditPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback, String lastItem) {
        String after = lastItem == null ? params.key : lastItem;

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> getPost;
        if (accessToken == null) {
            if (sortType.getTime() != null) {
                getPost = api.getMultiRedditPosts(multiRedditPath, after,
                        sortType.getTime().value);
            } else {
                getPost = api.getMultiRedditPosts(multiRedditPath, after);
            }
        } else {
            if (sortType.getTime() != null) {
                getPost = api.getMultiRedditPostsOauth(multiRedditPath, after,
                        sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getMultiRedditPostsOauth(multiRedditPath, after,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), -1, postFilter, readPostList,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(LinkedHashSet<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadMultiRedditPostsAfter(params, callback, lastItem);
                                    } else {
                                        int currentPostsSize = postLinkedHashSet.size();
                                        postLinkedHashSet.addAll(newPosts);
                                        if (currentPostsSize == postLinkedHashSet.size()) {
                                            loadMultiRedditPostsAfter(params, callback, lastItem);
                                        } else {
                                            List<Post> newPostsList = new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size());
                                            callback.onResult(newPostsList, lastItem);
                                        }
                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                String errorMessage = t.getMessage();
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
            }
        });
    }

    void retryLoadingMore() {
        loadAfter(params, callback);
    }
}
