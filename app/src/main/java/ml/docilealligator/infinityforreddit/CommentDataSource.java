package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.PageKeyedDataSource;
import java.util.ArrayList;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommentDataSource extends PageKeyedDataSource<String, CommentData> {

  private final Retrofit retrofit;
  private final Locale locale;
  private final String username;
  private final String sortType;

  private final MutableLiveData<NetworkState> paginationNetworkStateLiveData;
  private final MutableLiveData<NetworkState> initialLoadStateLiveData;
  private final MutableLiveData<Boolean> hasPostLiveData;

  private LoadParams<String> params;
  private LoadCallback<String, CommentData> callback;

  CommentDataSource(Retrofit retrofit, Locale locale, String username, String sortType) {
    this.retrofit = retrofit;
    this.locale = locale;
    this.username = username;
    this.sortType = sortType;
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
  public void loadInitial(@NonNull LoadInitialParams<String> params,
      @NonNull LoadInitialCallback<String, CommentData> callback) {
    initialLoadStateLiveData.postValue(NetworkState.LOADING);

    RedditAPI api = retrofit.create(RedditAPI.class);
    Call<String> commentsCall = api.getUserComments(username, null, sortType);
    commentsCall.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        if (response.isSuccessful()) {
          new ParseCommentAsyncTask(response.body(), locale,
              new ParseCommentAsyncTask.ParseCommentAsyncTaskListener() {
                @Override
                public void parseSuccessful(ArrayList<CommentData> comments, String after) {
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
                  initialLoadStateLiveData.postValue(
                      new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                }
              }).execute();
        } else {
          initialLoadStateLiveData
              .postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
        }
      }

      @Override
      public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        initialLoadStateLiveData
            .postValue(new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
      }
    });
  }

  @Override
  public void loadBefore(@NonNull LoadParams<String> params,
      @NonNull LoadCallback<String, CommentData> callback) {

  }

  @Override
  public void loadAfter(@NonNull LoadParams<String> params,
      @NonNull LoadCallback<String, CommentData> callback) {
    this.params = params;
    this.callback = callback;

    paginationNetworkStateLiveData.postValue(NetworkState.LOADING);

    RedditAPI api = retrofit.create(RedditAPI.class);
    Call<String> bestPost = api.getUserComments(username, params.key, sortType);
    bestPost.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        if (response.isSuccessful()) {
          new ParseCommentAsyncTask(response.body(), locale,
              new ParseCommentAsyncTask.ParseCommentAsyncTaskListener() {
                @Override
                public void parseSuccessful(ArrayList<CommentData> comments, String after) {
                  if (after == null || after.equals("") || after.equals("null")) {
                    callback.onResult(comments, null);
                  } else {
                    callback.onResult(comments, after);
                  }
                  paginationNetworkStateLiveData.postValue(NetworkState.LOADED);
                }

                @Override
                public void parseFailed() {
                  paginationNetworkStateLiveData.postValue(
                      new NetworkState(NetworkState.Status.FAILED, "Error parsing data"));
                }
              }).execute();
        } else {
          paginationNetworkStateLiveData
              .postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
        }
      }

      @Override
      public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        paginationNetworkStateLiveData
            .postValue(new NetworkState(NetworkState.Status.FAILED, "Error fetching data"));
      }
    });
  }

  private static class ParseCommentAsyncTask extends
      AsyncTask<Void, ArrayList<CommentData>, ArrayList<CommentData>> {

    private final String response;
    private final Locale locale;
    private final ParseCommentAsyncTaskListener parseCommentAsyncTaskListener;
    private String after;

    ParseCommentAsyncTask(String response, Locale locale,
        ParseCommentAsyncTaskListener parseCommentAsyncTaskListener) {
      this.response = response;
      this.locale = locale;
      this.parseCommentAsyncTaskListener = parseCommentAsyncTaskListener;
    }

    @Override
    protected ArrayList<CommentData> doInBackground(Void... voids) {
      try {
        JSONObject data = new JSONObject(response).getJSONObject(JSONUtils.DATA_KEY);
        JSONArray commentsJSONArray = data.getJSONArray(JSONUtils.CHILDREN_KEY);
        ArrayList<CommentData> comments = new ArrayList<>();
        for (int i = 0; i < commentsJSONArray.length(); i++) {
          JSONObject commentJSON = commentsJSONArray.getJSONObject(i)
              .getJSONObject(JSONUtils.DATA_KEY);
          comments.add(ParseComment.parseSingleComment(commentJSON, 0, locale));
        }
        after = data.getString(JSONUtils.AFTER_KEY);
        return comments;
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(ArrayList<CommentData> commentData) {
      super.onPostExecute(commentData);
      if (commentData != null) {
        parseCommentAsyncTaskListener.parseSuccessful(commentData, after);
      } else {
        parseCommentAsyncTaskListener.parseFailed();
      }
    }

    interface ParseCommentAsyncTaskListener {

      void parseSuccessful(ArrayList<CommentData> comments, String after);

      void parseFailed();
    }
  }
}
