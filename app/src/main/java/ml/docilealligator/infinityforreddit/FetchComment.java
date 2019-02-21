package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class FetchComment {
    interface FetchCommentListener {
        void onFetchCommentSuccess(List<?> commentData,
                                   String parentId, String commaSeparatedChildren);
        void onFetchCommentFailed();
    }

    interface FetchMoreCommentListener {
        void onFetchMoreCommentSuccess(List<?> commentData);
        void onFetchMoreCommentFailed();
    }

    interface FetchAllCommentListener {
        void onFetchAllCommentSuccess(List<?> commentData);
        void onFetchAllCommentFailed();
    }

    static void fetchComment(Retrofit retrofit, String subredditNamePrefixed, String article,
                             String comment, Locale locale, boolean isPost, int parentDepth,
                             final FetchCommentListener fetchCommentListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> comments = api.getComments(subredditNamePrefixed, article, comment);
        comments.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseComment(response.body(), new ArrayList<>(),
                            locale, isPost, parentDepth,
                            new ParseComment.ParseCommentListener() {
                                @Override
                                public void onParseCommentSuccess(List<?> commentData,
                                                                  String parentId, String commaSeparatedChildren) {
                                    fetchCommentListener.onFetchCommentSuccess(commentData, parentId,
                                            commaSeparatedChildren);
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

    static void fetchMoreComment(Retrofit retrofit, String subredditNamePrefixed, String mParentId,
                                 String children, Locale locale, FetchMoreCommentListener fetchMoreCommentListener) {
        RedditAPI api = retrofit.create(RedditAPI.class);
        Call<String> moreChildrenBasicInfo = api.getMoreChildren(mParentId, children);
        moreChildrenBasicInfo.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()) {
                    ParseComment.parseMoreCommentBasicInfo(response.body(), new ParseComment.ParseMoreCommentBasicInfoListener() {
                        @Override
                        public void onParseMoreCommentBasicInfoSuccess(String commaSeparatedChildrenId) {
                            Call<String> moreComments = api.getInfo(subredditNamePrefixed, commaSeparatedChildrenId);
                            moreComments.enqueue(new Callback<String>() {
                                @Override
                                public void onResponse(Call<String> call, Response<String> response) {
                                    if(response.isSuccessful()) {
                                        ParseComment.parseMoreComment(response.body(), new ArrayList<>(), locale,
                                                0, new ParseComment.ParseCommentListener() {
                                                    @Override
                                                    public void onParseCommentSuccess(List<?> commentData, String parentId,
                                                                                      String commaSeparatedChildren) {
                                                        fetchMoreCommentListener.onFetchMoreCommentSuccess(commentData);
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

                        @Override
                        public void onParseMoreCommentBasicInfoFailed() {
                            Log.i("comment parse failed", "comment parse failed");
                            fetchMoreCommentListener.onFetchMoreCommentFailed();
                        }
                    });
                } else {
                    Log.i("basic info failed", response.message());
                    fetchMoreCommentListener.onFetchMoreCommentFailed();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.i("basic info failed", t.getMessage());
                fetchMoreCommentListener.onFetchMoreCommentFailed();
            }
        });
    }

    static void fetchAllComment(Retrofit retrofit, String subredditNamePrefixed, String article,
                                String comment, Locale locale, boolean isPost, int parentDepth,
                                FetchAllCommentListener fetchAllCommentListener) {
        fetchComment(retrofit, subredditNamePrefixed, article, comment, locale, isPost, parentDepth,
                new FetchCommentListener() {
                    @Override
                    public void onFetchCommentSuccess(List<?> commentData, String parentId, String commaSeparatedChildren) {
                        if(!commaSeparatedChildren.equals("")) {
                            fetchMoreComment(retrofit, subredditNamePrefixed, parentId, commaSeparatedChildren,
                                    locale, new FetchMoreCommentListener() {
                                        @Override
                                        public void onFetchMoreCommentSuccess(List<?> moreCommentData) {
                                            ((ArrayList<CommentData>)commentData).addAll((ArrayList<CommentData>) moreCommentData);
                                            fetchAllCommentListener.onFetchAllCommentSuccess(commentData);
                                        }

                                        @Override
                                        public void onFetchMoreCommentFailed() {
                                            Log.i("fetch more comment", "error");
                                            fetchAllCommentListener.onFetchAllCommentFailed();
                                        }
                                    });
                        } else {
                            fetchAllCommentListener.onFetchAllCommentSuccess(commentData);
                        }
                    }

                    @Override
                    public void onFetchCommentFailed() {
                        Log.i("fetch comment", "error");
                        fetchAllCommentListener.onFetchAllCommentFailed();
                    }
                });

    }
}
