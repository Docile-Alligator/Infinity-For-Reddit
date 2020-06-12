package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.API.PushshiftAPI;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedComment {

    public static void fetchRemovedComment(Retrofit retrofit, CommentData comment, FetchRemovedCommentListener listener) {
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

    private static CommentData parseRemovedComment(JSONObject comment, CommentData commentData) throws JSONException {
        String id = comment.getString(JSONUtils.ID_KEY);
        if (id.equals(commentData.getId())) {
            String author = comment.getString(JSONUtils.AUTHOR_KEY);
            String commentMarkdown = "";
            if (!comment.isNull(JSONUtils.BODY_KEY)) {
                commentMarkdown = Utils.modifyMarkdown(comment.getString(JSONUtils.BODY_KEY).trim());
            }

            commentData.setAuthor(author);
            commentData.setCommentMarkdown(commentMarkdown);
            commentData.setCommentRawText(commentMarkdown);
            return commentData;
        } else {
            return null;
        }
    }

    public interface FetchRemovedCommentListener {
        void fetchSuccess(CommentData comment);

        void fetchFailed();
    }

    private static class ParseCommentAsyncTask extends AsyncTask<Void, Void, Void> {

        private String responseBody;
        private FetchRemovedCommentListener listener;
        CommentData comment;

        public ParseCommentAsyncTask(String responseBody, CommentData comment, FetchRemovedCommentListener listener) {
            this.responseBody = responseBody;
            this.comment = comment;
            this.listener = listener;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                JSONObject commentJSON = new JSONObject(responseBody).getJSONArray(JSONUtils.DATA_KEY).getJSONObject(0);
                comment = parseRemovedComment(commentJSON, comment);
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
