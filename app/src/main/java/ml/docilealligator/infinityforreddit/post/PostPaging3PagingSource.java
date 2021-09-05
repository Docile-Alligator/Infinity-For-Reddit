package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
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

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.HttpException;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostPaging3PagingSource extends ListenableFuturePagingSource<String, Post> {
    private Executor executor;
    private Retrofit retrofit;
    private String accessToken;
    private String accountName;
    private SharedPreferences sharedPreferences;
    private SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String subredditOrUserName;
    private String query;
    private String trendingSource;
    private int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private List<ReadPost> readPostList;
    private String userWhere;
    private String multiRedditPath;
    private LinkedHashSet<Post> postLinkedHashSet;

    private MutableLiveData<NetworkState> paginationNetworkStateLiveData;
    private MutableLiveData<NetworkState> initialLoadStateLiveData;
    private MutableLiveData<Boolean> hasPostLiveData;

    PostPaging3PagingSource(Executor executor, Retrofit retrofit, String accessToken, String accountName,
                   SharedPreferences sharedPreferences,
                   SharedPreferences postFeedScrolledPositionSharedPreferences, int postType,
                   SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        paginationNetworkStateLiveData = new MutableLiveData<>();
        initialLoadStateLiveData = new MutableLiveData<>();
        hasPostLiveData = new MutableLiveData<>();
        this.postType = postType;
        this.sortType = sortType == null ? new SortType(SortType.Type.BEST) : sortType;
        this.postFilter = postFilter;
        this.readPostList = readPostList;
        postLinkedHashSet = new LinkedHashSet<>();
    }

    @Nullable
    @Override
    public String getRefreshKey(@NonNull PagingState<String, Post> pagingState) {
        return null;
    }

    @NonNull
    @Override
    public ListenableFuture<LoadResult<String, Post>> loadFuture(@NonNull LoadParams<String> loadParams) {
        Log.i("asfsaf", "s" + loadParams.getKey());
        RedditAPI api = retrofit.create(RedditAPI.class);
        ListenableFuture<Response<String>> bestPost;
        if(sortType.getTime() != null) {
            bestPost = api.getBestPostsListenableFuture(sortType.getType().value, sortType.getTime().value, loadParams.getKey(),
                    APIUtils.getOAuthHeader(accessToken));
        } else {
            bestPost = api.getBestPostsListenableFuture(sortType.getType().value, loadParams.getKey(), APIUtils.getOAuthHeader(accessToken));
        }

        ListenableFuture<LoadResult<String, Post>> pageFuture = Futures.transform(bestPost, this::transformData, executor);

        ListenableFuture<LoadResult<String, Post>> partialLoadResultFuture =
                Futures.catching(pageFuture, HttpException.class,
                        LoadResult.Error::new, executor);

        return Futures.catching(partialLoadResultFuture,
                IOException.class, LoadResult.Error::new, executor);
    }

    public LoadResult<String, Post> transformData(Response<String> response) {
        if (response.isSuccessful()) {
            String responseString = response.body();
            LinkedHashSet<Post> newPosts = ParsePost.parsePostsSync(responseString, -1, postFilter, readPostList);
            String lastItem = ParsePost.getLastItem(responseString);
            if (newPosts == null) {
                return new LoadResult.Error<>(new Exception("Error parsing more posts"));
            } else {
                int currentPostsSize = postLinkedHashSet.size();
                postLinkedHashSet.addAll(newPosts);
                if (currentPostsSize == postLinkedHashSet.size()) {
                    return new LoadResult.Page<>(new ArrayList<>(), null, lastItem);
                } else {
                    return new LoadResult.Page<>(new ArrayList<>(postLinkedHashSet).subList(currentPostsSize, postLinkedHashSet.size()), null, lastItem);
                }
            }
        } else {
            return new LoadResult.Error<>(new Exception("Response failed"));
        }
    }
}
