package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class PostDataSource extends PageKeyedDataSource<String, Post> {

    static final int TYPE_FRONT_PAGE = 0;
    static final int TYPE_SUBREDDIT = 1;
    static final int TYPE_USER = 2;
    static final int TYPE_SEARCH = 3;

    static final String SORT_TYPE_BEST = "best";
    static final String SORT_TYPE_HOT = "hot";
    static final String SORT_TYPE_NEW = "new";
    static final String SORT_TYPE_RANDOM = "random";
    static final String SORT_TYPE_RISING = "rising";
    static final String SORT_TYPE_TOP = "top";
    static final String SORT_TYPE_CONTROVERSIAL = "controversial";
    static final String SORT_TYPE_RELEVANCE = "relevance";
    static final String SORT_TYPE_COMMENTS = "comments";

    static final String USER_WHERE_SUBMITTED = "submitted";
    static final String USER_WHERE_UPVOTED = "upvoted";
    static final String USER_WHERE_DOWNVOTED = "downvoted";
    static final String USER_WHERE_HIDDEN = "hidden";
    static final String USER_WHERE_SAVED = "saved";
    static final String USER_WHERE_GILDED = "gilded";

    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private String subredditOrUserName;
    private String query;
    private int postType;
    private String sortType;
    private boolean nsfw;
    private int filter;
    private String userWhere;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Post> callback;

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, int postType, String sortType,
                   int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, int postType,
                   String sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, int postType,
                   String sortType, String where, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
        userWhere = where;
        this.filter = filter;
        this.nsfw = nsfw;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditOrUserName, String query,
                   int postType, String sortType, int filter, boolean nsfw) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditOrUserName = subredditOrUserName;
        this.query = query;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType;
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

        if(params.key.equals("") || params.key.equals("null")) {
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

        Call<String> bestPost = api.getBestPosts(sortType, lastItem, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    if(sortType.equals(SORT_TYPE_RANDOM)) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(Post post) {
                                ArrayList<Post> singlePostList = new ArrayList<>();
                                singlePostList.add(post);
                                hasPostLiveData.postValue(true);
                                initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(singlePostList, null, null);
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
                                        if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                            nextPageKey = null;
                                        } else {
                                            nextPageKey = lastItem;
                                        }

                                        if(newPosts.size() != 0) {
                                            hasPostLiveData.postValue(true);
                                        } else if(nextPageKey != null) {
                                            loadBestPostsInitial(callback, nextPageKey);
                                            return;
                                        } else {
                                            hasPostLiveData.postValue(false);
                                        }

                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                        callback.onResult(newPosts, null, nextPageKey);
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
        Call<String> bestPost = api.getBestPosts(sortType, after, RedditUtils.getOAuthHeader(accessToken));

        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                loadBestPostsAfter(params, callback, lastItem);
                            } else {
                                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(newPosts, lastItem);
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
        if(accessToken == null) {
            getPost = api.getSubredditBestPosts(subredditOrUserName, sortType, lastItem);
        } else {
            getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType, lastItem, RedditUtils.getOAuthHeader(accessToken));
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    if(sortType.equals(SORT_TYPE_RANDOM)) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(Post post) {
                                ArrayList<Post> singlePostList = new ArrayList<>();
                                singlePostList.add(post);
                                hasPostLiveData.postValue(true);
                                initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(singlePostList, null, null);
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
                                        if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                            nextPageKey = null;
                                        } else {
                                            nextPageKey = lastItem;
                                        }

                                        if(newPosts.size() != 0) {
                                            hasPostLiveData.postValue(true);
                                        } else if(nextPageKey != null) {
                                            loadSubredditPostsInitial(callback, nextPageKey);
                                            return;
                                        } else {
                                            hasPostLiveData.postValue(false);
                                        }

                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                        callback.onResult(newPosts, null, nextPageKey);
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
        if(accessToken == null) {
            getPost = api.getSubredditBestPosts(subredditOrUserName, sortType, after);
        } else {
            getPost = api.getSubredditBestPostsOauth(subredditOrUserName, sortType, after, RedditUtils.getOAuthHeader(accessToken));
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                loadSubredditPostsAfter(params, callback, lastItem);
                            } else {
                                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(newPosts, lastItem);
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
        if(accessToken == null) {
            getPost = api.getUserPosts(subredditOrUserName, lastItem, sortType);
        } else {
            getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, lastItem, sortType,
                    RedditUtils.getOAuthHeader(accessToken));
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if(newPosts.size() != 0) {
                                        hasPostLiveData.postValue(true);
                                    } else if(nextPageKey != null) {
                                        loadUserPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                    callback.onResult(newPosts, null, nextPageKey);
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
        if(accessToken == null) {
            getPost = api.getUserPosts(subredditOrUserName, after, sortType);
        } else {
            getPost = api.getUserPostsOauth(subredditOrUserName, userWhere, after, sortType,
                    RedditUtils.getOAuthHeader(accessToken));
        }
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                loadUserPostsAfter(params, callback, lastItem);
                            } else {
                                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(newPosts, lastItem);
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

        if(subredditOrUserName == null) {
            if(accessToken == null) {
                getPost = api.searchPosts(query, lastItem, sortType);
            } else {
                getPost = api.searchPostsOauth(query, lastItem, sortType, RedditUtils.getOAuthHeader(accessToken));
            }
        } else {
            if(accessToken == null) {
                getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query, lastItem);
            } else {
                getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query, lastItem,
                        RedditUtils.getOAuthHeader(accessToken));
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    String nextPageKey;
                                    if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                        nextPageKey = null;
                                    } else {
                                        nextPageKey = lastItem;
                                    }

                                    if(newPosts.size() != 0) {
                                        hasPostLiveData.postValue(true);
                                    } else if(nextPageKey != null) {
                                        loadSearchPostsInitial(callback, nextPageKey);
                                        return;
                                    } else {
                                        hasPostLiveData.postValue(false);
                                    }

                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                    callback.onResult(newPosts, null, nextPageKey);
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

        if(subredditOrUserName == null) {
            if(accessToken == null) {
                getPost = api.searchPosts(query, after, sortType);
            } else {
                getPost = api.searchPostsOauth(query, after, sortType, RedditUtils.getOAuthHeader(accessToken));
            }
        } else {
            if(accessToken == null) {
                getPost = api.searchPostsInSpecificSubreddit(subredditOrUserName, query, after);
            } else {
                getPost = api.searchPostsInSpecificSubredditOauth(subredditOrUserName, query, after,
                        RedditUtils.getOAuthHeader(accessToken));
            }
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, filter, nsfw,
                            new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(newPosts.size() == 0 && lastItem != null && !lastItem.equals("") && !lastItem.equals("null")) {
                                loadSearchPostsAfter(params, callback, lastItem);
                            } else {
                                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                callback.onResult(newPosts, lastItem);
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
