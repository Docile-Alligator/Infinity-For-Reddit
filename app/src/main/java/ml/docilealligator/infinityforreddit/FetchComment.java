package ml.docilealligator.infinityforreddit;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchComment {
    interface FetchCommentListener {
        void onFetchCommentSuccess(ArrayList<CommentData> expandedComments, String parentId, ArrayList<String> children);
        void onFetchCommentFailed();
    }

    interface FetchMoreCommentListener {
        void onFetchMoreCommentSuccess(ArrayList<CommentData> expandedComments, int childrenStartingIndex);
        void onFetchMoreCommentFailed();
    }

    static void fetchComment(Retrofit retrofit, String subredditNamePrefixed, String article,
                             Locale locale, FetchCommentListener fetchCommentListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> comments = api.getComments(subredditNamePrefixed, article);
        comments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseComment(response.body(), new ArrayList<>(),
                            locale, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<CommentData> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenFullnames) {
                                    fetchCommentListener.onFetchCommentSuccess(expandedComments, parentId,
                                            moreChildrenFullnames);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    Log.i("parse failed", "parse failed");
                                    fetchCommentListener.onFetchCommentFailed();
                                }
                            });
                } else {
                    Log.i("call failed", response.message());
                    fetchCommentListener.onFetchCommentFailed();
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("call failed", t.getMessage());
                fetchCommentListener.onFetchCommentFailed();
            }
        });
    }

    static void fetchMoreComment(Retrofit retrofit, String subredditNamePrefixed,
                                 ArrayList<String> allChildren, int startingIndex, int depth,
                                 Locale locale, FetchMoreCommentListener fetchMoreCommentListener) {
        StringBuilder stringBuilder = new StringBuilder();
        for(int i = 0; i < 100; i++) {
            if(allChildren.size() <= startingIndex + i) {
                break;
            }
            stringBuilder.append(allChildren.get(startingIndex + i)).append(",");
        }

        if(stringBuilder.length() == 0) {
            return;
        }

        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        RedditAPI api = retrofit.create(RedditAPI.class);

        Call<String> moreComments = api.getInfo(subredditNamePrefixed, stringBuilder.toString());
        moreComments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseMoreComment(response.body(), new ArrayList<>(), locale,
                            depth, new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(ArrayList<CommentData> expandedComments,
                                                                  String parentId, ArrayList<String> moreChildrenFullnames) {
                                    fetchMoreCommentListener.onFetchMoreCommentSuccess(expandedComments,
                                            startingIndex + 100);
                                }

                                @Override
                                public void onParseCommentFailed() {
                                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                                    Log.i("comment parse failed", "comment parse failed");
                                }
                            });
                } else {
                    Log.i("more comment failed", response.message());
                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("more comment failed", t.getMessage());
                fetchMoreCommentListener.onFetchMoreCommentFailed();
            }
        });
    }
}
