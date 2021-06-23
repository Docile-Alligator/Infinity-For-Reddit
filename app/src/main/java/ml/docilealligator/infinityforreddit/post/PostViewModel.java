package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;
import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private PostDataSourceFactory postDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasPostLiveData;
    private LiveData<PagedList<Post>> posts;
    private MutableLiveData<SortType> sortTypeLiveData;
    private MutableLiveData<PostFilter> postFilterLiveData;
    private SortTypeAndPostFilterLiveData sortTypeAndPostFilterLiveData;

    public PostViewModel(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, int postType,
                         SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(executor, handler, retrofit, accessToken, accountName,
                sharedPreferences, cache, postType, sortType, postFilter, readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            postDataSourceFactory.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         int postType, SortType sortType, PostFilter postFilter,
                         List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(executor, handler, retrofit, accessToken, accountName,
                sharedPreferences, cache, subredditName, postType, sortType, postFilter,
                readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            postDataSourceFactory.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String username,
                         int postType, SortType sortType, PostFilter postFilter, String where,
                         List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(executor, handler, retrofit, accessToken, accountName,
                sharedPreferences, cache, username, postType, sortType, postFilter, where, readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            postDataSourceFactory.changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         String query, int postType, SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(executor, handler, retrofit, accessToken, accountName,
                sharedPreferences, cache, subredditName, query, postType, sortType, postFilter,
                readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);
        postFilterLiveData = new MutableLiveData<>();
        postFilterLiveData.postValue(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            postDataSourceFactory.changeSortTypeAndPostFilter(sortTypeLiveData.getValue(),
                    postFilterLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public LiveData<PagedList<Post>> getPosts() {
        return posts;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasPost() {
        return hasPostLiveData;
    }

    public void refresh() {
        postDataSourceFactory.getPostDataSource().invalidate();
    }

    public void retryLoadingMore() {
        postDataSourceFactory.getPostDataSource().retryLoadingMore();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Executor executor;
        private Handler handler;
        private Retrofit retrofit;
        private String accessToken;
        private String accountName;
        private SharedPreferences sharedPreferences;
        private SharedPreferences postFeedScrolledPositionSharedPreferences;
        private String name;
        private String query;
        private int postType;
        private SortType sortType;
        private PostFilter postFilter;
        private String userWhere;
        private List<ReadPost> readPostList;

        public Factory(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       int postType, SortType sortType, PostFilter postFilter, List<ReadPost> readPostList) {
            this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        public Factory(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String name, int postType, SortType sortType, PostFilter postFilter,
                       List<ReadPost> readPostList) {this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = name;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        //User posts
        public Factory(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String username, int postType, SortType sortType, PostFilter postFilter, String where,
                       List<ReadPost> readPostList) {
            this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = username;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            userWhere = where;
            this.readPostList = readPostList;
        }

        public Factory(Executor executor, Handler handler, Retrofit retrofit, String accessToken, String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       String name, String query, int postType, SortType sortType, PostFilter postFilter,
                       List<ReadPost> readPostList) {
            this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.name = name;
            this.query = query;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostList = readPostList;
        }

        //Anonymous Front Page
        public Factory(Executor executor, Handler handler, Retrofit retrofit, SharedPreferences sharedPreferences,
                       String concatenatedSubredditNames, int postType, SortType sortType, PostFilter postFilter) {
            this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.sharedPreferences = sharedPreferences;
            this.name = concatenatedSubredditNames;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == PostDataSource.TYPE_FRONT_PAGE) {
                return (T) new PostViewModel(executor, handler, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postType, sortType, postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_SEARCH) {
                return (T) new PostViewModel(executor, handler, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, query, postType, sortType,
                        postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_SUBREDDIT || postType == PostDataSource.TYPE_MULTI_REDDIT) {
                return (T) new PostViewModel(executor, handler, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, postType, sortType,
                        postFilter, readPostList);
            } else if (postType == PostDataSource.TYPE_ANONYMOUS_FRONT_PAGE) {
                return (T) new PostViewModel(executor, handler, retrofit, null, null, sharedPreferences,
                        null, name, postType, sortType,
                        postFilter, null);
            } else {
                return (T) new PostViewModel(executor, handler, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, name, postType, sortType,
                        postFilter, userWhere, readPostList);
            }
        }
    }

    private static class SortTypeAndPostFilterLiveData extends MediatorLiveData<Pair<PostFilter, SortType>> {
        public SortTypeAndPostFilterLiveData(LiveData<SortType> sortTypeLiveData, LiveData<PostFilter> postFilterLiveData) {
            addSource(sortTypeLiveData, sortType -> setValue(Pair.create(postFilterLiveData.getValue(), sortType)));
            addSource(postFilterLiveData, postFilter -> setValue(Pair.create(postFilter, sortTypeLiveData.getValue())));
        }
    }
}
