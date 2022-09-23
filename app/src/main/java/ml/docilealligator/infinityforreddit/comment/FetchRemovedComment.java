package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.PushshiftAPI;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedComment {

    public static void fetchRemovedComment(Executor executor, Handler handler, Retrofit retrofit, Comment comment,
                                           FetchRemovedCommentListener listener) {
        executor.execute(() -> {
            try {
                Response<String> response = retrofit.create(PushshiftAPI.class).getRemovedComment(comment.getId()).execute();
                if (response.isSuccessful()) {
                    Comment removedComment = parseComment(response.body(), comment);
                    handler.post(() -> {
                        if (removedComment != null) {
                            listener.fetchSuccess(removedComment);
                        } else {
                            listener.fetchFailed();
                        }
                    });
                } else {
                    handler.post(listener::fetchFailed);
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(listener::fetchFailed);
            }
        });
    }

    // At the moment of writing this code, directly fetching a removed comment from
    // Pushift.io API returns an Internal Server Error, so we temporarily do it this way instead.
    // If this fails to return the removed comment, we try our luck with Reveddit API
    public static void searchRemovedComment(Executor executor, Handler handler, Retrofit retrofit, Comment comment,
                                            FetchRemovedCommentListener listener) {
        executor.execute(() -> {
            long after = (comment.getCommentTimeMillis() / 1000) - 1; // 1 second before comment creation epoch
            try {
                Response<String> response = retrofit.create(PushshiftAPI.class).searchComments(
                        comment.getLinkId(),
                        3000,
                        "asc",
                        "id,author,body",
                        after,
                        after + 43200, // 12 Hours later
                        "*").execute();
                if (response.isSuccessful()) {
                    Comment removedComment = parseComment(response.body(), comment);
                    handler.post(() -> {
                        if (removedComment != null) {
                            listener.fetchSuccess(removedComment);
                        } else {
                            listener.fetchFailed();
                        }
                    });
                } else {
                    handler.post(listener::fetchFailed);
                }
            } catch (IOException e) {
                e.printStackTrace();
                handler.post(listener::fetchFailed);
            }
        });
    }

    @Nullable
    private static Comment parseComment(String responseBody, Comment comment) {
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
            if (commentFound == null) {
                return null;
            }

            return parseRemovedComment(commentFound, comment);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Nullable
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
}
