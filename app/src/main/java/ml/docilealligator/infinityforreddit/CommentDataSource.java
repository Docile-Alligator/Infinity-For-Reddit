package ml.docilealligator.infinityforreddit;

import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommentDataSource extends PageKeyedDataSource<String, Post> {
    interface OnCommentFetchedCallback {
        void hasComment();
        void noComment();
    }

    private Retrofit retrofit;
    private Locale locale;
    private String subredditNamePrefixed;
    private String article;
    private String comment;
    private boolean isPost;
    private OnCommentFetchedCallback onCommentFetchedCallback;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;

    private LoadInitialParams<String> initialParams;
    private LoadInitialCallback<String, Post> initialCallback;
    private LoadParams<String> params;
    private LoadCallback<String, Post> callback;

    private String mParentId;

    CommentDataSource(Retrofit retrofit, Locale locale, String subredditNamePrefixed, String article,
                      String comment, boolean isPost, OnCommentFetchedCallback onCommentFetchedCallback) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.article = article;
        this.comment = comment;
        this.isPost = isPost;
        paginationNetworkStateLiveData = new MutableLiveData();
        initialLoadStateLiveData = new MutableLiveData();
        this.onCommentFetchedCallback = onCommentFetchedCallback;
    }

    MutableLiveData getPaginationNetworkStateLiveData() {
        return paginationNetworkStateLiveData;
    }

    MutableLiveData getInitialLoadStateLiveData() {
        return initialLoadStateLiveData;
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, Post> callback) {
        initialParams = params;
        initialCallback = callback;

        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> comments = api.getComments(subredditNamePrefixed, article, comment);
        comments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseComment(response.body(), new ArrayList<>(), locale, isPost,
                            0, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(List<?> commentData, String parentId,
                                                                  String commaSeparatedChildren) {
                                    if(commentData.size() > 0) {
                                        onCommentFetchedCallback.hasComment();
                                        mParentId = parentId;
                                    } else {
                                        onCommentFetchedCallback.noComment();
                                    }

                                    callback.onResult((List<Post>) commentData, null, commaSeparatedChildren);
                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing comment"));
                                }
                            });
                } else {
                    Log.i("comment call failed", response.message());
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing comment"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching comment"));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Post> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Post> callback) {
        this.params = params;
        this.callback = callback;

        if(params.key.equals("null") || params.key.equals("")) {
            return;
        }

        paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> moreChildrenBasicInfo = api.getMoreChildren(mParentId, params.key);
        moreChildrenBasicInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseMoreCommentBasicInfo(response.body(), new ParseComment.ParseMoreCommentBasicInfoListener() {
                        @Override
                        public void onParseMoreCommentBasicInfoSuccess(String commaSeparatedChildrenId) {
                            Call<String> moreComments = api.getInfo(subredditNamePrefixed, commaSeparatedChildrenId);
                            moreComments.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.isSuccessful()) {
                                        ParseComment.parseMoreComment(response.body(), new ArrayList<>(), locale,
                                                0, new ParseComment.ParseCommentListener() {
                                                    @Override
                                                    public void onParseCommentSuccess(List<?> commentData, String parentId,
                                                                                      String commaSeparatedChildren) {
                                                        callback.onResult((List<Post>) commentData, null);
                                                        paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                                    }

                                                    @Override
                                                    public void onParseCommentFailed() {
                                                        paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                                    }
                                                });
                                    } else {
                                        Log.i("comment call failed", response.message());
                                        paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                                    }
                                }

                                @Override
                                public void onFailure(Call<String> call, Throwable t) {
                                    String errorMessage = t == null ? "unknown error" : t.getMessage();
                                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, errorMessage));
                                }
                            });
                        }

                        @Override
                        public void onParseMoreCommentBasicInfoFailed() {
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Parse more comments basic info failed"));
                        }
                    });
                } else {
                    Log.i("comment call failed", response.message());
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, response.message()));
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                String errorMessage = t == null ? "unknown error" : t.getMessage();
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
