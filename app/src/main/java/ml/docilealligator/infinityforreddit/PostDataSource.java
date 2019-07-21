package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class PostDataSource extends PageKeyedDataSource<String, Post> {
    interface OnPostFetchedCallback {
        void hasPost();
        void noPost();
    }

    static final int TYPE_FRONT_PAGE = 0;
    static final int TYPE_SUBREDDIT = 1;
    static final int TYPE_USER = 2;
    static final int TYPE_SEARCH = 3;

    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private String subredditName;
    private String query;
    private int postType;
    private OnPostFetchedCallback onPostFetchedCallback;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;

    private LoadInitialParams<String> initialParams;
    private LoadInitialCallback<String, Post> initialCallback;
    private LoadParams<String> params;
    private LoadCallback<String, Post> callback;

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, int postType, OnPostFetchedCallback onPostFetchedCallback) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        paginationNetworkStateLiveData = new MutableLiveData();
        initialLoadStateLiveData = new MutableLiveData();
        this.postType = postType;
        this.onPostFetchedCallback = onPostFetchedCallback;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType, OnPostFetchedCallback onPostFetchedCallback) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditName = subredditName;
        paginationNetworkStateLiveData = new MutableLiveData();
        initialLoadStateLiveData = new MutableLiveData();
        this.postType = postType;
        this.onPostFetchedCallback = onPostFetchedCallback;
    }

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, String subredditName, String query,
                   int postType, OnPostFetchedCallback onPostFetchedCallback) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.subredditName = subredditName;
        this.query = query;
        paginationNetworkStateLiveData = new MutableLiveData();
        initialLoadStateLiveData = new MutableLiveData();
        this.postType = postType;
        this.onPostFetchedCallback = onPostFetchedCallback;
    }

    MutableLiveData getPaginationNetworkStateLiveData() {
        return paginationNetworkStateLiveData;
    }

    MutableLiveData getInitialLoadStateLiveData() {
        return initialLoadStateLiveData;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Post> callback) {
        initialParams = params;
        initialCallback = callback;

        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        switch (postType) {
            case TYPE_FRONT_PAGE:
                loadBestPostsInitial(callback);
                break;
            case TYPE_SUBREDDIT:
                loadSubredditPostsInitial(callback);
                break;
            case TYPE_USER:
                loadUserPostsInitial(callback, null);
                break;
            case TYPE_SEARCH:
                loadSearchPostsInitial(callback);
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

        if(params.key.equals("null")) {
            return;
        }

        paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

        switch (postType) {
            case TYPE_FRONT_PAGE:
                loadBestPostsAfter(params, callback);
                break;
            case TYPE_SUBREDDIT:
                loadSubredditPostsAfter(params, callback);
                break;
            case TYPE_USER:
                loadUserPostsAfter(params, callback, null);
                break;
            case TYPE_SEARCH:
                loadSearchPostsAfter(params, callback);
                break;
        }
    }

    private void loadBestPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> bestPost = api.getBestPosts(null, RedditUtils.getOAuthHeader(accessToken));
        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if (response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if(newPosts.size() == 0) {
                                        onPostFetchedCallback.noPost();
                                    } else {
                                        onPostFetchedCallback.hasPost();
                                    }

                                    callback.onResult(newPosts, null, lastItem);
                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    Log.i("Post fetch error", "Error parsing data");
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    Log.i("Post fetch error", response.message());
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

    private void loadBestPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> bestPost = api.getBestPosts(params.key, RedditUtils.getOAuthHeader(accessToken));

        bestPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                callback.onResult(newPosts, null);
                            } else {
                                callback.onResult(newPosts, lastItem);
                            }
                            paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                        }

                        @Override
                        public void onParsePostsListingFail() {
                            Log.i("Best post", "Error parsing data");
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    });
                } else {
                    Log.i("best post", response.message());
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

    private void loadSubredditPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost = api.getSubredditBestPosts(subredditName, null, RedditUtils.getOAuthHeader(accessToken));
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if(newPosts.size() == 0) {
                                        onPostFetchedCallback.noPost();
                                    } else {
                                        onPostFetchedCallback.hasPost();
                                    }

                                    callback.onResult(newPosts, null, lastItem);
                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    Log.i("Post fetch error", "Error parsing data");
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    Log.i("Post fetch error", response.message());
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

    private void loadSubredditPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost = api.getSubredditBestPosts(subredditName, params.key, RedditUtils.getOAuthHeader(accessToken));
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                callback.onResult(newPosts, null);
                            } else {
                                callback.onResult(newPosts, lastItem);
                            }
                            paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                        }

                        @Override
                        public void onParsePostsListingFail() {
                            Log.i("Best post", "Error parsing data");
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    });
                } else {
                    Log.i("Best post", response.message());
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
        Call<String> getPost = api.getUserBestPosts(subredditName, lastItem, RedditUtils.getOAuthHeader(accessToken));
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if(newPosts.size() == 0 && lastItem.equals("null")) {
                                        callback.onResult(newPosts, null, lastItem);
                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                        onPostFetchedCallback.noPost();
                                    } else if(newPosts.size() == 0) {
                                        loadUserPostsInitial(callback, lastItem);
                                    } else {
                                        callback.onResult(newPosts, null, lastItem);
                                        initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                        onPostFetchedCallback.hasPost();
                                    }
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    Log.i("Post fetch error", "Error parsing data");
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    Log.i("Post fetch error", response.message());
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
        Call<String> getPost = api.getUserBestPosts(subredditName, after, RedditUtils.getOAuthHeader(accessToken));
        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(newPosts.size() == 0 && !lastItem.equals("null")) {
                                loadUserPostsAfter(params, callback, lastItem);
                            } else {
                                callback.onResult(newPosts, lastItem);
                                paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                            }
                        }

                        @Override
                        public void onParsePostsListingFail() {
                            Log.i("User posts", "Error parsing data");
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    });
                } else {
                    Log.i("User posts", response.message());
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

    private void loadSearchPostsInitial(@NonNull final LoadInitialCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost;

        if(subredditName == null) {
            getPost = api.searchPosts(query, null, RedditUtils.getOAuthHeader(accessToken));
        } else {
            getPost = api.searchPostsInSpecificSubreddit(subredditName, query, null, RedditUtils.getOAuthHeader(accessToken));
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1,
                            new ParsePost.ParsePostsListingListener() {
                                @Override
                                public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                                    if(newPosts.size() == 0) {
                                        onPostFetchedCallback.noPost();
                                    } else {
                                        onPostFetchedCallback.hasPost();
                                    }

                                    callback.onResult(newPosts, null, lastItem);
                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParsePostsListingFail() {
                                    Log.i("Post fetch error", "Error parsing data");
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                }
                            });
                } else {
                    Log.i("Post fetch error", response.message());
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

    private void loadSearchPostsAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> getPost;

        if(subredditName == null) {
            getPost = api.searchPosts(subredditName, params.key, RedditUtils.getOAuthHeader(accessToken));
        } else {
            getPost = api.searchPostsInSpecificSubreddit(subredditName, query, params.key, RedditUtils.getOAuthHeader(accessToken));
        }

        getPost.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull retrofit2.Response<String> response) {
                if(response.isSuccessful()) {
                    ParsePost.parsePosts(response.body(), locale, -1, new ParsePost.ParsePostsListingListener() {
                        @Override
                        public void onParsePostsListingSuccess(ArrayList<Post> newPosts, String lastItem) {
                            if(lastItem == null || lastItem.equals("") || lastItem.equals("null")) {
                                callback.onResult(newPosts, null);
                            } else {
                                callback.onResult(newPosts, lastItem);
                            }
                            paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                        }

                        @Override
                        public void onParsePostsListingFail() {
                            Log.i("Search post", "Error parsing data");
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    });
                } else {
                    Log.i("Search post", response.message());
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

    void retry() {
        loadInitial(initialParams, initialCallback);
    }

    void retryLoadingMore() {
        loadAfter(params, callback);
    }
}
