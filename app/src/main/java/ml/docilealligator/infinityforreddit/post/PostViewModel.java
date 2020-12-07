package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

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
import java.util.Locale;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SortType;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;
import ml.docilealligator.infinityforreddit.subredditfilter.SubredditFilter;
import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private PostDataSourceFactory postDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasPostLiveData;
    private LiveData<PagedList<Post>> posts;
    private MutableLiveData<Boolean> nsfwLiveData;
    private MutableLiveData<SortType> sortTypeLiveData;
    private NSFWAndSortTypeLiveData nsfwAndSortTypeLiveData;

    public PostViewModel(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                         SharedPreferences sharedPreferences, SharedPreferences cache, int postType,
                         SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, accountName, locale,
                sharedPreferences, cache, postType, sortType, filter, nsfw, readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        nsfwLiveData = new MutableLiveData<>();
        nsfwLiveData.postValue(nsfw);
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
            postDataSourceFactory.changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         int postType, SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList,
                         List<SubredditFilter> subredditFilterList) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, accountName, locale,
                sharedPreferences, cache, subredditName, postType, sortType, filter, nsfw, readPostList,
                subredditFilterList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        nsfwLiveData = new MutableLiveData<>();
        nsfwLiveData.postValue(nsfw);
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
            postDataSourceFactory.changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         int postType, SortType sortType, String where, int filter, boolean nsfw,
                         List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, accountName, locale,
                sharedPreferences, cache, subredditName, postType, sortType, where, filter, nsfw, readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        nsfwLiveData = new MutableLiveData<>();
        nsfwLiveData.postValue(nsfw);
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
            postDataSourceFactory.changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(postDataSourceFactory, pagedListConfig)).build();
        });
    }

    public PostViewModel(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                         SharedPreferences sharedPreferences, SharedPreferences cache, String subredditName,
                         String query, int postType, SortType sortType, int filter, boolean nsfw,
                         List<ReadPost> readPostList) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, accountName, locale,
                sharedPreferences, cache, subredditName, query, postType, sortType, filter, nsfw, readPostList);

        initialLoadingState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::getPaginationNetworkStateLiveData);
        hasPostLiveData = Transformations.switchMap(postDataSourceFactory.getPostDataSourceLiveData(),
                PostDataSource::hasPostLiveData);

        nsfwLiveData = new MutableLiveData<>();
        nsfwLiveData.postValue(nsfw);
        sortTypeLiveData = new MutableLiveData<>();
        sortTypeLiveData.postValue(sortType);

        nsfwAndSortTypeLiveData = new NSFWAndSortTypeLiveData(nsfwLiveData, sortTypeLiveData);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(25)
                        .build();

        posts = Transformations.switchMap(nsfwAndSortTypeLiveData, nsfwAndSort -> {
            postDataSourceFactory.changeNSFWAndSortType(nsfwLiveData.getValue(), sortTypeLiveData.getValue());
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

    public void changeNSFW(boolean nsfw) {
        nsfwLiveData.postValue(nsfw);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String accessToken;
        private String accountName;
        private Locale locale;
        private SharedPreferences sharedPreferences;
        private SharedPreferences postFeedScrolledPositionSharedPreferences;
        private String subredditName;
        private String query;
        private int postType;
        private SortType sortType;
        private String userWhere;
        private int filter;
        private boolean nsfw;
        private List<ReadPost> readPostList;
        private List<SubredditFilter> subredditFilterList;

        public Factory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       int postType, SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.locale = locale;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
            this.readPostList = readPostList;
        }

        public Factory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences, String subredditName,
                       int postType, SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.locale = locale;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
            this.readPostList = readPostList;
        }

        //With subreddit filter
        public Factory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences, String subredditName,
                       int postType, SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList,
                       List<SubredditFilter> subredditFilterList) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.locale = locale;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
            this.readPostList = readPostList;
            this.subredditFilterList = subredditFilterList;
        }

        //User posts
        public Factory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences, String subredditName,
                       int postType, SortType sortType, String where, int filter, boolean nsfw, List<ReadPost> readPostList) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.locale = locale;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            userWhere = where;
            this.filter = filter;
            this.nsfw = nsfw;
            this.readPostList = readPostList;
        }

        public Factory(Retrofit retrofit, String accessToken, String accountName, Locale locale,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences, String subredditName,
                       String query, int postType, SortType sortType, int filter, boolean nsfw, List<ReadPost> readPostList) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.locale = locale;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.subredditName = subredditName;
            this.query = query;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
            this.readPostList = readPostList;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == PostDataSource.TYPE_FRONT_PAGE) {
                return (T) new PostViewModel(retrofit, accessToken, accountName, locale, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postType, sortType, filter, nsfw, readPostList);
            } else if (postType == PostDataSource.TYPE_SEARCH) {
                return (T) new PostViewModel(retrofit, accessToken, accountName, locale, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, subredditName, query, postType, sortType,
                        filter, nsfw, readPostList);
            } else if (postType == PostDataSource.TYPE_SUBREDDIT || postType == PostDataSource.TYPE_MULTI_REDDIT) {
                return (T) new PostViewModel(retrofit, accessToken, accountName, locale, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, subredditName, postType, sortType,
                        filter, nsfw, readPostList, subredditFilterList);
            } else {
                return (T) new PostViewModel(retrofit, accessToken, accountName, locale, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, subredditName, postType, sortType,
                        userWhere, filter, nsfw, readPostList);
            }
        }
    }

    private static class NSFWAndSortTypeLiveData extends MediatorLiveData<Pair<Boolean, SortType>> {
        public NSFWAndSortTypeLiveData(LiveData<Boolean> nsfw, LiveData<SortType> sortType) {
            addSource(nsfw, accessToken1 -> setValue(Pair.create(accessToken1, sortType.getValue())));
            addSource(sortType, sortType1 -> setValue(Pair.create(nsfw.getValue(), sortType1)));
        }
    }
}
