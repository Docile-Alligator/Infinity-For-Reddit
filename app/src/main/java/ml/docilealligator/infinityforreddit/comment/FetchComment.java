package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class FetchComment {
    public static void fetchComments(Executor executor, Handler handler, Retrofit retrofit,
                                     @Nullable String accessToken, String article,
                                     String commentId, String sortType, String contextNumber, boolean expandChildren,
                                     Locale locale, FetchCommentListener fetchCommentListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> comments;
        if (accessToken == null) {
            if (commentId == null) {
                comments = api.getPostAndCommentsById(article, sortType);
            } else {
                comments = api.getPostAndCommentsSingleThreadById(article, commentId, sortType, contextNumber);
            }
        } else {
            if (commentId == null) {
                comments = api.getPostAndCommentsByIdOauth(article, sortType, APIUtils.getOAuthHeader(accessToken));
            } else {
                comments = api.getPostAndCommentsSingleThreadByIdOauth(article, commentId, sortType, contextNumber,
                        APIUtils.getOAuthHeader(accessToken));
            }
        }

        comments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseComment.parseComment(executor, handler, response.body(), new ArrayList<>(),
                            expandChildren, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<Comment> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenFullnames) {
                                    fetchCommentListener.onFetchCommentSuccess(expandedComments, parentId,
                                            moreChildrenFullnames);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    fetchCommentListener.onFetchCommentFailed();
                                }
                            });
                } else {
                    fetchCommentListener.onFetchCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchCommentListener.onFetchCommentFailed();
            }
        });
    }

    public static void fetchMoreComment(Executor executor, Handler handler, Retrofit retrofit,
                                        @Nullable String accessToken,
                                        ArrayList<String> allChildren, int startingIndex,
                                        boolean expandChildren, String postFullName,
                                        FetchMoreCommentListener fetchMoreCommentListener) {
        if (allChildren == null) {
            return;
        }

        // todo: build query from all comments and use POST method to avoid "414 Request-URI Too Large"
        //  see https://www.reddit.com/r/redditdev/comments/3hr3ls/get_apimorechildren_returns_414_requesturi_too/
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            if (allChildren.size() <= startingIndex + i) {
                break;
            }
            String child = allChildren.get(startingIndex + i);
            stringBuilder.append(child.substring(3)).append(",");
        }

        if (stringBuilder.length() == 0) {
            return;
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> moreComments;
        if (accessToken == null) {
            moreComments = api.moreChildren(postFullName, stringBuilder.toString());
        } else {
            moreComments = api.moreChildrenOauth(postFullName, stringBuilder.toString(), APIUtils.getOAuthHeader(accessToken));
        }

        moreComments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    ParseComment.parseMoreComment(executor, handler, response.body(), new ArrayList<>(),
                            expandChildren, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<Comment> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenFullnames) {
                                    fetchMoreCommentListener.onFetchMoreCommentSuccess(expandedComments,
                                            startingIndex + 100);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                                }
                            });
                } else {
                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                fetchMoreCommentListener.onFetchMoreCommentFailed();
            }
        });
    }

    public interface FetchCommentListener {
        void onFetchCommentSuccess(ArrayList<Comment> expandedComments, String parentId, ArrayList<String> children);

        void onFetchCommentFailed();
    }

    public interface FetchMoreCommentListener {
        void onFetchMoreCommentSuccess(ArrayList<Comment> expandedComments, int childrenStartingIndex);

        void onFetchMoreCommentFailed();
    }
}
