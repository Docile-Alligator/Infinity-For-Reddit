package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.MutableLiveData;
import android.arch.paging.PageKeyedDataSource;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

class PostDataSource extends PageKeyedDataSource<String, Post> {
    private Retrofit retrofit;
    private String accessToken;
    private Locale locale;
    private boolean isBestPost;
    private String subredditName;

    private MutableLiveData networkState;
    private MutableLiveData initialLoading;

    PostDataSource(Retrofit retrofit, String accessToken, Locale locale, boolean isBestPost) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.locale = locale;
        this.isBestPost = isBestPost;
        networkState = new MutableLiveData();
        initialLoading = new MutableLiveData();

    }

    PostDataSource(Retrofit retrofit, Locale locale, boolean isBestPost, String subredditName) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.isBestPost = isBestPost;
        this.subredditName = subredditName;
        networkState = new MutableLiveData();
        initialLoading = new MutableLiveData();
    }

    public MutableLiveData getNetworkState() {
        return networkState;
    }

    public MutableLiveData getInitialLoading() {
        return initialLoading;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull final LoadInitialCallback<String, Post> callback) {
        initialLoading.postValue(NetworkState.LOADING);
        networkState.postValue(NetworkState.LOADING);

        if(isBestPost) {
            RedditAPI api = retrofit.create(RedditAPI.class);

            Call<String> bestPost = api.getBestPost(null, RedditUtils.getOAuthHeader(accessToken));
            bestPost.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if (response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), locale,
                                new ParsePost.ParsePostListener() {
                                    @Override
                                    public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                        callback.onResult(newPosts, null, lastItem);
                                        initialLoading.postValue(NetworkState.LOADED);
                                        networkState.postValue(NetworkState.LOADED);
                                    }

                                    @Override
                                    public void onParsePostFail() {
                                        Log.i("Post fetch error", "Error parsing data");
                                    }
                                });
                    } else {
                        Log.i("Post fetch error", response.message());
                        initialLoading.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                        networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    String errorMessage = t == null ? "unknown error" : t.getMessage();
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
                }
            });
        } else {
            RedditAPI api = retrofit.create(RedditAPI.class);
            Call<String> getPost = api.getPost(subredditName, null);
            getPost.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                        if(response.isSuccessful()) {
                            ParsePost.parsePost(response.body(), locale,
                                    new ParsePost.ParsePostListener() {
                                        @Override
                                        public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                            callback.onResult(newPosts, null, lastItem);
                                            initialLoading.postValue(NetworkState.LOADED);
                                            networkState.postValue(NetworkState.LOADED);
                                        }

                                        @Override
                                        public void onParsePostFail() {
                                            Log.i("Post fetch error", "Error parsing data");
                                        }
                                    });
                        } else {
                            Log.i("Post fetch error", response.message());
                            initialLoading.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                            networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                        }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    String errorMessage = t == null ? "unknown error" : t.getMessage();
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
                }
            });
        }
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Post> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull final LoadCallback<String, Post> callback) {
        networkState.postValue(NetworkState.LOADING);

        if(isBestPost) {
            RedditAPI api = retrofit.create(RedditAPI.class);
            Call<String> bestPost = api.getBestPost(params.key, RedditUtils.getOAuthHeader(accessToken));

            bestPost.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if(response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                callback.onResult(newPosts, lastItem);
                                networkState.postValue(NetworkState.LOADED);
                            }

                            @Override
                            public void onParsePostFail() {
                                Log.i("Best post", "Error parsing data");
                            }
                        });
                    } else {
                        Log.i("best post", response.message());
                        networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    String errorMessage = t == null ? "unknown error" : t.getMessage();
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
                }
            });
        } else {
            RedditAPI api = retrofit.create(RedditAPI.class);
            Call<String> getPost = api.getPost(subredditName, params.key);
            getPost.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, retrofit2.Response<String> response) {
                    if(response.isSuccessful()) {
                        ParsePost.parsePost(response.body(), locale, new ParsePost.ParsePostListener() {
                            @Override
                            public void onParsePostSuccess(ArrayList<Post> newPosts, String lastItem) {
                                callback.onResult(newPosts, lastItem);
                                networkState.postValue(NetworkState.LOADED);
                            }

                            @Override
                            public void onParsePostFail() {
                                Log.i("Best post", "Error parsing data");
                            }
                        });
                    } else {
                        Log.i("best post", response.message());
                        networkState.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    String errorMessage = t == null ? "unknown error" : t.getMessage();
                    networkState.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
                }
            });
        }
    }
}
