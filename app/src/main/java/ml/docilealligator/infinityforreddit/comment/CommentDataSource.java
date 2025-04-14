package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommentDataSource extends PageKeyedDataSource<String, Comment> {

    private final Executor executor;
    private final Handler handler;
    private final Retrofit retrofit;
    @Nullable
    private final String accessToken;
    @NonNull
    private final String accountName;
    private final String username;
    private final SortType sortType;
    private final boolean areSavedComments;

    private final MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private final MutableLiveData<NetworkState> initialLoadStateLiveData;
    private final MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Comment> callback;

    CommentDataSource(Executor executor, Handler handler, Retrofit retrofit, @Nullable String accessToken,
                      @NonNull String accountName, String username, SortType sortType,
                      boolean areSavedComments) {
        this.executor = executor;
        this.handler = handler;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.username = username;
        this.sortType = sortType;
        this.areSavedComments = areSavedComments;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
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

    void retryLoadingMore() {
        loadAfter(params, callback);
    }

    @Override
    public void loadInitial(@NonNull LoadInitialParams<String> params, @NonNull LoadInitialCallback<String, Comment> callback) {
        initialLoadStateLiveData.postValue(NetworkState.LOADING);

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> commentsCall;
        if (areSavedComments) {
            commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED,
                    null, sortType.getType(), sortType.getTime(),
                    APIUtils.getOAuthHeader(accessToken));
        } else {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                commentsCall = api.getUserComments(username, null, sortType.getType(),
                        sortType.getTime());
            } else {
                commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken), username,
                        null, sortType.getType(), sortType.getTime());
            }
        }
        commentsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        parseComments(response.body(), new ParseCommentsAsyncTaskListener() {
                            @Override
                            public void parseSuccessful(ArrayList<Comment> comments, String after) {
                                handler.post(() -> {
                                    if (comments.isEmpty()) {
                                        hasPostLiveData.postValue(false);
                                    } else {
                                        hasPostLiveData.postValue(true);
                                    }

                                    if (after == null || after.isEmpty() || after.equals("null")) {
                                        callback.onResult(comments, null, null);
                                    } else {
                                        callback.onResult(comments, null, after);
                                    }
                                    initialLoadStateLiveData.postValue(NetworkState.LOADED);
                                });
                            }

                            @Override
                            public void parseFailed() {
                                handler.post(() -> {
                                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                                });
                            }
                        });
                    });
                } else {
                    initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
            }
        });
    }

    @Override
    public void loadBefore(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Comment> callback) {

    }

    @Override
    public void loadAfter(@NonNull LoadParams<String> params, @NonNull LoadCallback<String, Comment> callback) {
        this.params = params;
        this.callback = callback;

        paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> commentsCall;
        if (areSavedComments) {
            commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED, params.key,
                    sortType.getType(), sortType.getTime(), APIUtils.getOAuthHeader(accessToken));
        } else {
            if (accountName.equals(Account.ANONYMOUS_ACCOUNT)) {
                commentsCall = api.getUserComments(username, params.key, sortType.getType(),
                        sortType.getTime());
            } else {
                commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken),
                        username, params.key, sortType.getType(), sortType.getTime());
            }
        }
        commentsCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    executor.execute(() -> {
                        parseComments(response.body(), new ParseCommentsAsyncTaskListener() {
                            @Override
                            public void parseSuccessful(ArrayList<Comment> comments, String after) {
                                handler.post(() -> {
                                    if (after == null || after.isEmpty() || after.equals("null")) {
                                        callback.onResult(comments, null);
                                    } else {
                                        callback.onResult(comments, after);
                                    }
                                    paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                                });
                            }

                            @Override
                            public void parseFailed() {
                                handler.post(() -> paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data")));
                            }
                        });
                    });
                } else {
                    paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
            }
        });
    }

    @WorkerThread
    private static void parseComments(String response, ParseCommentsAsyncTaskListener parseCommentsAsyncTaskListener) {
        try {
            JSONObject data = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
            JSONArray commentsJSONArray = data.getJSONArray(JSONUtils.CHILDREN_KEY);
            String after = data.getString(JSONUtils.AFTER_KEY);
            ArrayList<Comment> comments = new ArrayList<>();
            for (int i = 0; i < commentsJSONArray.length(); i++) {
                try {
                    JSONObject commentJSON = commentsJSONArray.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    comments.add(ParseComment.parseSingleComment(commentJSON, 0));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            parseCommentsAsyncTaskListener.parseSuccessful(comments, after);
        } catch (JSONException e) {
            e.printStackTrace();
            parseCommentsAsyncTaskListener.parseFailed();
        }
    }

    interface ParseCommentsAsyncTaskListener {
        void parseSuccessful(ArrayList<Comment> comments, String after);

        void parseFailed();
    }
}
