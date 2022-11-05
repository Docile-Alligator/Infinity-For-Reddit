package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.paging.ListenableFuturePagingSource;
import androidx.paging.PagingState;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HistoryPostPagingSource extends ListenableFuturePagingSource<String, Post> {
    public static final int TYPE_READ_POSTS = 100;

    private Retrofit retrofit;
    private Executor executor;
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private String username;
    private int postType;
    private PostFilter postFilter;

    public HistoryPostPagingSource(Retrofit retrofit, Executor executor, RedditDataRoomDatabase redditDataRoomDatabase,
                                   String accessToken, String accountName, SharedPreferences sharedPreferences,
                                   String username, int postType, PostFilter postFilter) {
        this.retrofit = retrofit;
        this.executor = executor;
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.username = username;
        this.postType = postType;
        this.postFilter = postFilter;
    }

    @Nullable
    @Override
    public String getRefreshKey(@NonNull PagingState<String, Post> pagingState) {
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<String, Post>> loadFuture(@NonNull LoadParams<String> loadParams) {
        if (postType == TYPE_READ_POSTS) {
            return loadHomePosts(loadParams, redditDataRoomDatabase);
        } else {
            return loadHomePosts(loadParams, redditDataRoomDatabase);
        }
    }

    public LoadResult<String, Post> transformData(List<ReadPost> readPosts) {
        StringBuilder ids = new StringBuilder();
        long lastItem = 0;
        for (ReadPost readPost : readPosts) {
            ids.append("t3_").append(readPost.getId()).append(",");
            lastItem = readPost.getTime();
        }
        if (ids.length() > 0) {
            ids.deleteCharAt(ids.length() - 1);
        }

        Call<String> historyPosts;
        if (accessToken != null && !accessToken.isEmpty()) {
            historyPosts = retrofit.create(RedditAPI.class).getInfoOauth(ids.toString(), APIUtils.getOAuthHeader(accessToken));
        } else {
            historyPosts = retrofit.create(RedditAPI.class).getInfo(ids.toString());
        }

        try {
            Response<String> response = historyPosts.execute();
            if (response.isSuccessful()) {
                String responseString = response.body();
                LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, null);
                if (newPosts == null) {
                    return new LoadResult.Error<>(new Exception("Error parsing posts"));
                } else {
                    if (newPosts.size() < 25) {
                        return new LoadResult.Page<>(new ArrayList<>(newPosts), null, null);
                    }
                    return new LoadResult.Page<>(new ArrayList<>(newPosts), null, Long.toString(lastItem));
                }
            } else {
                return new LoadResult.Error<>(new Exception("Response failed"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new LoadResult.Error<>(new Exception("Response failed"));
        }
    }

    private ListenableFuture<LoadResult<String, Post>> loadHomePosts(@NonNull LoadParams<String> loadParams, RedditDataRoomDatabase redditDataRoomDatabase) {
        String after = loadParams.getKey();
        ListenableFuture<List<ReadPost>> readPosts = redditDataRoomDatabase.readPostDao().getAllReadPostsListenableFuture(username, Long.parseLong(after == null ? "0" : after));

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(readPosts, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }
}
