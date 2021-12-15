package ml.docilealligator.infinityforreddit.comment;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.apis.PushshiftAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedComment {

    public static void fetchRemovedComment(Retrofit retrofit, Comment comment, FetchRemovedCommentListener listener) {
        retrofit.create(PushshiftAPI.class).getRemovedComment(comment.getId())
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            new ParseCommentAsyncTask(response.body(), comment, listener).execute();
                        } else {
                            listener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.fetchFailed();
                    }
                });
    }

    // At the moment of writing this code, directly fetching a removed comment from
    // Pushift.io API returns an Internal Server Error, so we temporarily do it this way instead.
    // If this fails to return the removed comment, we try our luck with Reveddit API
    public static void searchRemovedComment(Retrofit retrofit, Comment comment, FetchRemovedCommentListener listener) {
        long after = (comment.getCommentTimeMillis() / 1000) - 1; // 1 second before comment creation epoch
        retrofit.create(PushshiftAPI.class).searchComments(
                comment.getLinkId(),
                3000,
                "asc",
                "id,author,body",
                after,
                after + 43200, // 12 Hours later
                "*")
                .enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            new ParseCommentAsyncTask(response.body(), comment, listener).execute();
                        } else {
                            listener.fetchFailed();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        t.printStackTrace();
                        listener.fetchFailed();
                    }
                });
    }

    private static Comment parseRemovedComment(@NonNull JSONObject result, Comment comment) throws JSONException {
        String id = result.getString(JSONUtils.ID_KEY);
        String author = result.getString(JSONUtils.AUTHOR_KEY);
        String body = Utils.modifyMarkdown(result.optString(JSONUtils.BODY_KEY).trim());

        if (id.equals(comment.getId()) &&
                (!author.equals(comment.getAuthor()) ||
                        !body.equals(comment.getCommentRawText()))
        ) {
            comment.setAuthor(author);
            comment.setCommentMarkdown(body);
            comment.setCommentRawText(body);
            return comment;
        } else {
            return null;
        }
    }

    public interface FetchRemovedCommentListener {
        void fetchSuccess(Comment comment);

        void fetchFailed();
    }

    private static class ParseCommentAsyncTask extends AsyncTask<Void, Void, Void> {

        private final String responseBody;
        private final FetchRemovedCommentListener listener;
        Comment comment;

        public ParseCommentAsyncTask(String responseBody, Comment comment, FetchRemovedCommentListener listener) {
            this.responseBody = responseBody;
            this.comment = comment;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONArray commentJSONArray = new JSONObject(responseBody).getJSONArray(JSONUtils.DATA_KEY);
                JSONObject commentFound = null;
                for (int i = 0; i < commentJSONArray.length(); i++) {
                    JSONObject commentJSON = commentJSONArray.getJSONObject(i);
                    if (!commentJSON.isNull("id") && commentJSON.getString("id").equals(comment.getId())) {
                        commentFound = commentJSON;
                        break;
                    }
                }
                assert commentFound != null;
                comment = parseRemovedComment(commentFound, comment);
            } catch (JSONException e) {
                e.printStackTrace();
                comment = null;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (comment != null)
                listener.fetchSuccess(comment);
            else
                listener.fetchFailed();
        }
    }
}
