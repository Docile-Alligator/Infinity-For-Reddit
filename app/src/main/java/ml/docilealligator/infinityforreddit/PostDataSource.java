package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.Utils.RedditUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class PostDataSource extends PageKeyedDataSource<String, Post> {

    public static final int TYPE_FRONT_PAGE = 0;
    public static final int TYPE_SUBREDDIT = 1;
    public static final int TYPE_USER = 2;
    public static final int TYPE_SEARCH = 3;

    public static final String USER_WHERE_SUBMITTED = "submitted";
    public static final String USER_WHERE_UPVOTED = "upvoted";
    public static final String USER_WHERE_DOWNVOTED = "downvoted";
    public static final String USER_WHERE_HIDDEN = "hidden";
    public static final String USER_WHERE_SAVED = "saved";
    public static final String USER_WHERE_GILDED = "gilded";

    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private String subredditOrUserName;
    private String query;
    private int postType;
    private SortType sortType;
    private boolean nsfw;
    private int filter;
    private String userWhere;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Post> callback;

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, int postType, SortType sortType,
                   int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.BEST) : sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, int postType,
                   SortType sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        if (sortType == null) {
            if (subredditOrUserName.equals("popular") || subredditOrUserName.equals("all")) {
                this.sortType = new SortType(SortType.Type.HOT);
            } else {
                this.sortType = new SortType(SortType.Type.BEST);
            }
        } else {
            this.sortType = sortType;
        }
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, int postType,
                   SortType sortType, String where, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.NEW) : sortType;
        userWhere = where;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, String query,
                   int postType, SortType sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        this.query = query;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.RELEVANCE) : sortType;
        this.filter = filter;
        this.nsfw = nsfw;
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
                loadBestPostsInitial(callback, null);
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
        }
    }

    private void loadBestPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback, String lastItem) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> bestPost;
        if(sortType.getTime() != null) {
            bestPost = api.getBestPosts(sortType.getType().value, sortType.getTime().value, lastItem,
                    RedditUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPosts(sortType.getType().value, lastItem, RedditUtils.getOAuthHeader(accessToken));
        }
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (sortType.getType().value.equals(SortType.Type.RANDOM.value)) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(Post post) {
                                ArrayList<Post> singlePostList = new ArrayList<>();
                                singlePostList.add(post);
                                callback.onResult(singlePostList, null, null);
                                hasPostLiveData.postValue(true);
                                initialLoadStateLiveData.postValue(NetworkState.LOADED);
                            }

                            @Override
                            public void onParsePostFail() {
                                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                            }
                        });
                    } else {
                        ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                                new ParsePost.ParsePostsListingListener() {
                                    @Override
                                    public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                        String nextPageKey;
                                        if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                            nextPageKey = null;
                                        } else {
                                            nextPageKey = lastItem;
                                        }

                                        if (newPosts.size() != 0) {
                                            callback.onResult(newPosts, null, nextPageKey);
                                            hasPostLiveData.postValue(true);
                                        } else if (nextPageKey != null) {
                                            loadBestPostsInitial(callback, nextPageKey);
                                            return;
                                        } else {
                                            callback.onResult(newPosts, null, nextPageKey);
                                            hasPostLiveData.postValue(false);
                                        }

                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                    }

                                    @Override
                                    public void onParsePostsListingFail() {
                                        initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing posts"));
                                    }
                                });
                    }
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
                    RedditUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPosts(sortType.getType().value, after, RedditUtils.getOAuthHeader(accessToken));
        }

        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadBestPostsAfter(params, callback, lastItem);
                                    } else {
                                        callback.onResult(newPosts, lastItem);
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
                        sortType.getTime().value, lastItem, RedditUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        lastItem, RedditUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if (sortType.getType().value.equals(SortType.Type.RANDOM.value)) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(Post post) {
                                ArrayList<Post> singlePostList = new ArrayList<>();
                                singlePostList.add(post);
                                callback.onResult(singlePostList, null, null);
                                hasPostLiveData.postValue(true);
                                initialLoadStateLiveData.postValue(NetworkState.LOADED);
                            }

                            @Override
                            public void onParsePostFail() {
                                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                            }
                        });
                    } else {
                        ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                                new ParsePost.ParsePostsListingListener() {
                                    @Override
                                    public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                        String nextPageKey;
                                        if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                            nextPageKey = null;
                                        } else {
                                            nextPageKey = lastItem;
                                        }

                                        if (newPosts.size() != 0) {
                                            callback.onResult(newPosts, null, nextPageKey);
                                            hasPostLiveData.postValue(true);
                                        } else if (nextPageKey != null) {
                                            loadSubredditPostsInitial(callback, nextPageKey);
                                            return;
                                        } else {
                                            callback.onResult(newPosts, null, nextPageKey);
                                            hasPostLiveData.postValue(false);
                                        }

                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                    }

                                    @Override
                                    public void onParsePostsListingFail() {
                                        initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing posts"));
                                    }
                                });
                    }
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
                        sortType.getTime().value, after, RedditUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType.getType().value,
                        after, RedditUtils.getOAuthHeader(accessToken));
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadSubredditPostsAfter(params, callback, lastItem);
                                    } else {
                                        callback.onResult(newPosts, lastItem);
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
                        sortType.getTime().value, RedditUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, lastItem, sortType.getType().value,
                        RedditUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        callback.onResult(newPosts, null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadUserPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        callback.onResult(newPosts, null, nextPageKey);
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
                        sortType.getTime().value, RedditUtils.getOAuthHeader(accessToken));
            } else {
                getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, after, sortType.getType().value,
                        RedditUtils.getOAuthHeader(accessToken));
            }
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadUserPostsAfter(params, callback, lastItem);
                                    } else {
                                        callback.onResult(newPosts, lastItem);
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
                            sortType.getTime().value, RedditUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsOauth(query, lastItem, sortType.getType().value,
                            RedditUtils.getOAuthHeader(accessToken));
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
                            RedditUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, lastItem,
                            RedditUtils.getOAuthHeader(accessToken));
                }
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if (lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if (newPosts.size() != 0) {
                                        callback.onResult(newPosts, null, nextPageKey);
                                        hasPostLiveData.postValue(true);
                                    } else if (nextPageKey != null) {
                                        loadSearchPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        callback.onResult(newPosts, null, nextPageKey);
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
                            sortType.getTime().value, RedditUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsOauth(query, after, sortType.getType().value, RedditUtils.getOAuthHeader(accessToken));
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
                            RedditUtils.getOAuthHeader(accessToken));
                } else {
                    getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query,
                            sortType.getType().value, after, RedditUtils.getOAuthHeader(accessToken));
                }
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if (newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                        loadSearchPostsAfter(params, callback, lastItem);
                                    } else {
                                        callback.onResult(newPosts, lastItem);
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
