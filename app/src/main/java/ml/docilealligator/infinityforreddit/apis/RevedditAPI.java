package ml.docilealligator.infinityforreddit.apis;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface RevedditAPI {
    @GET("/short/thread-comments/")
    Call<String> getRemovedComments(@HeaderMap Map<String, String> headers,
                                    @Query("link_id") String threadId,
                                    @Query("after") long after,
                                    @Query("root_comment_id") String rootCommentId,
                                    @Query("comment_id") String commentId,
                                    @Query("num_comments") int numComments,
                                    @Query("post_created_utc") long postCreatedUtc,
                                    @Query("focus_comment_removed") boolean focusCommentRemoved);
}
