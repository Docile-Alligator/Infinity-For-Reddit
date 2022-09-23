package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RevedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchRemovedCommentReveddit {

    public static void fetchRemovedComment(Executor executor, Handler handler, Retrofit retrofit, Comment comment,
                                           long postCreatedUtc, int nComments, FetchRemovedCommentListener listener) {
        executor.execute(() -> {
            String parentIdWithoutPrefix = comment.getParentId().substring(3);
            String rootCommentId = parentIdWithoutPrefix.equals(comment.getLinkId()) ? comment.getId() : parentIdWithoutPrefix;
            try {
                Response<String> response = retrofit.create(RevedditAPI.class).getRemovedComments(
                        APIUtils.getRevedditHeader(),
                        comment.getLinkId(),
                        (comment.getCommentTimeMillis() / 1000) - 1,
                        rootCommentId,
                        comment.getId(),
                        nComments,
                        postCreatedUtc / 1000,
                        true).execute();
                if (response.isSuccessful()) {
                    Comment removedComment = parseRemovedComment(new JSONObject(response.body()).getJSONObject(comment.getId()), comment);
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
            } catch (IOException | JSONException e) {
                e.printStackTrace();
                handler.post(listener::fetchFailed);
            }
        });
    }

    private static Comment parseRemovedComment(JSONObject result, Comment comment) throws JSONException {
        String id = result.getString(JSONUtils.ID_KEY);
        String author = result.getString(JSONUtils.AUTHOR_KEY);
        String body = Utils.modifyMarkdown(result.optString(JSONUtils.BODY_KEY).trim());

        if (id.equals(comment.getId()) && (!author.equals(comment.getAuthor()) || !body.equals(comment.getCommentRawText()))) {
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
