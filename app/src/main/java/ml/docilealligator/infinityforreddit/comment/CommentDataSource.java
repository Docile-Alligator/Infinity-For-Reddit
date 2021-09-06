package ml.docilealligator.infinityforreddit.comment;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.post.PostPagingSource;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommentDataSource extends PageKeyedDataSource<String, Comment> {

    private Retrofit retrofit;
    private Locale locale;
    @Nullable
    private String accessToken;
    private String username;
    private SortType sortType;
    private boolean areSavedComments;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasPostLiveData;

    private LoadParams<String> params;
    private LoadCallback<String, Comment> callback;

    CommentDataSource(Retrofit retrofit, Locale locale, @Nullable String accessToken, String username, SortType sortType,
                      boolean areSavedComments) {
        this.retrofit = retrofit;
        this.locale = locale;
        this.accessToken = accessToken;
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
            if (sortType.getTime() != null) {
                commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED,
                        null, sortType.getType().value, sortType.getTime().value,
                        APIUtils.getOAuthHeader(accessToken));
            } else {
                commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED,
                        null, sortType.getType().value, APIUtils.getOAuthHeader(accessToken));
            }
        } else {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    commentsCall = api.getUserComments(username, null, sortType.getType().value,
                            sortType.getTime().value);
                } else {
                    commentsCall = api.getUserComments(username, null, sortType.getType().value);
                }
            } else {
                if (sortType.getTime() != null) {
                    commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken), username,
                            null, sortType.getType().value, sortType.getTime().value);
                } else {
                    commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken), username,
                            null, sortType.getType().value);
                }
            }
        }
        commentsCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseCommentAsyncTask(response.body(), locale, new ParseCommentAsyncTask.ParseCommentAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<Comment> comments, String after) {
                            if (comments.size() == 0) {
                                hasPostLiveData.postValue(false);
                            } else {
                                hasPostLiveData.postValue(true);
                            }

                            if (after == null || after.equals("") || after.equals("null")) {
                                callback.onResult(comments, null, null);
                            } else {
                                callback.onResult(comments, null, after);
                            }
                            initialLoadStateLiveData.postValue(NetworkState.LOADED);
                        }

                        @Override
                        public void parseFailed() {
                            initialLoadStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    }).execute();
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
            if (sortType.getTime() != null) {
                commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED, params.key,
                        sortType.getType().value, sortType.getTime().value, APIUtils.getOAuthHeader(accessToken));
            } else {
                commentsCall = api.getUserSavedCommentsOauth(username, PostPagingSource.USER_WHERE_SAVED, params.key,
                        sortType.getType().value, APIUtils.getOAuthHeader(accessToken));
            }
        } else {
            if (accessToken == null) {
                if (sortType.getTime() != null) {
                    commentsCall = api.getUserComments(username, params.key, sortType.getType().value,
                            sortType.getTime().value);
                } else {
                    commentsCall = api.getUserComments(username, params.key, sortType.getType().value);
                }
            } else {
                if (sortType.getTime() != null) {
                    commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken),
                            username, params.key, sortType.getType().value, sortType.getTime().value);
                } else {
                    commentsCall = api.getUserCommentsOauth(APIUtils.getOAuthHeader(accessToken),
                            username, params.key, sortType.getType().value);
                }
            }
        }
        commentsCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    new ParseCommentAsyncTask(response.body(), locale, new ParseCommentAsyncTask.ParseCommentAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<Comment> comments, String after) {
                            if (after == null || after.equals("") || after.equals("null")) {
                                callback.onResult(comments, null);
                            } else {
                                callback.onResult(comments, after);
                            }
                            paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                        }

                        @Override
                        public void parseFailed() {
                            paginationNetworkStateLiveData.postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                        }
                    }).execute();
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

    private static class ParseCommentAsyncTask extends AsyncTask<Void, ArrayList<Comment>, ArrayList<Comment>> {
        private String after;
        private Locale locale;
        private JSONArray commentsJSONArray;
        private boolean parseFailed;
        private ParseCommentAsyncTaskListener parseCommentAsyncTaskListener;

        ParseCommentAsyncTask(String response, Locale locale, ParseCommentAsyncTaskListener parseCommentAsyncTaskListener) {
            this.locale = locale;
            this.parseCommentAsyncTaskListener = parseCommentAsyncTaskListener;
            try {
                JSONObject data = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
                commentsJSONArray = data.getJSONArray(JSONUtils.CHILDREN_KEY);
                after = data.getString(JSONUtils.AFTER_KEY);
                parseFailed = false;
            } catch (JSONException e) {
                parseFailed = true;
                e.printStackTrace();
            }
        }

        @Override
        protected ArrayList<Comment> doInBackground(Void... voids) {
            if (parseFailed) {
                return null;
            }

            ArrayList<Comment> comments = new ArrayList<>();
            for (int i = 0; i < commentsJSONArray.length(); i++) {
                try {
                    JSONObject commentJSON = commentsJSONArray.getJSONObject(i).getJSONObject(JSONUtils.DATA_KEY);
                    comments.add(ParseComment.parseSingleComment(commentJSON, 0));
                } catch (JSONException ignored) {
                }
            }
            return comments;
        }

        @Override
        protected void onPostExecute(ArrayList<Comment> commentData) {
            super.onPostExecute(commentData);
            if (commentData != null) {
                parseCommentAsyncTaskListener.parseSuccessful(commentData, after);
            } else {
                parseCommentAsyncTaskListener.parseFailed();
            }
        }

        interface ParseCommentAsyncTaskListener {
            void parseSuccessful(ArrayList<Comment> comments, String after);

            void parseFailed();
        }
    }
}
