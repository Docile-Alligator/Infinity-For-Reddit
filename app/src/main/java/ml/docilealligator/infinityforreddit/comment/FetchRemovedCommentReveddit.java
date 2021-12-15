package ml.docilealligator.infinityforreddit.comment;

import android.os.AsyncTask;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import ml.docilealligator.infinityforreddit.apis.RevedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedCommentReveddit {

    public static void fetchRemovedComment(Retrofit retrofit, Comment comment, long postCreatedUtc, int nComments, FetchRemovedCommentListener listener) {
        String parentIdWithoutPrefix = comment.getParentId().substring(3);
        String rootCommentId = parentIdWithoutPrefix.equals(comment.getLinkId()) ? comment.getId() : parentIdWithoutPrefix;
        retrofit.create(RevedditAPI.class).getRemovedComments(
                APIUtils.getRevedditHeader(),
                comment.getLinkId(),
                (comment.getCommentTimeMillis() / 1000) - 1,
                rootCommentId,
                comment.getId(),
                nComments,
                postCreatedUtc / 1000,
                true)
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

    private static Comment parseRemovedComment(JSONObject result, Comment comment) throws JSONException {
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
                JSONObject commentJSON = new JSONObject(responseBody).getJSONObject(comment.getId());
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
