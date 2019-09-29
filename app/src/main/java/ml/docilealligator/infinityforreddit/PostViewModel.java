package ml.docilealligator.infinityforreddit;

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

import java.util.Locale;

import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private PostDataSourceFactory postDataSourceFactory;
    private LiveData<NetworkState> paginationNetworkState;
    private LiveData<NetworkState> initialLoadingState;
    private LiveData<Boolean> hasPostLiveData;
    private LiveData<PagedList<Post>> posts;
    private MutableLiveData<Boolean> nsfwLiveData;
    private MutableLiveData<String> sortTypeLiveData;
    private NSFWAndSortTypeLiveData nsfwAndSortTypeLiveData;

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, int postType, String sortType,
                         int filter, boolean nsfw) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, postType,
                sortType, filter, nsfw);

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

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                         String sortType, int filter, boolean nsfw) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                postType, sortType, filter, nsfw);

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

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                         String sortType, String where, int filter, boolean nsfw) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                postType, sortType, where, filter, nsfw);

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

    public PostViewModel(Retrofit retrofit, String accessToken, Locale locale, String subredditName, String query,
                         int postType, String sortType, int filter, boolean nsfw) {
        postDataSourceFactory = new PostDataSourceFactory(retrofit, accessToken, locale, subredditName,
                query, postType, sortType, filter, nsfw);

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

    public void changeSortType(String sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public void changeNSFW(boolean nsfw) {
        nsfwLiveData.postValue(nsfw);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private Retrofit retrofit;
        private String accessToken;
        private Locale locale;
        private String subredditName;
        private String query;
        private int postType;
        private String sortType;
        private String userWhere;
        private int filter;
        private boolean nsfw;

        public Factory(Retrofit retrofit, String accessToken, Locale locale, int postType, String sortType,
                       int filter, boolean nsfw) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                       String sortType, int filter, boolean nsfw) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, int postType,
                       String sortType, String where, int filter, boolean nsfw) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.postType = postType;
            this.sortType = sortType;
            userWhere = where;
            this.filter = filter;
            this.nsfw = nsfw;
        }

        public Factory(Retrofit retrofit, String accessToken, Locale locale, String subredditName, String query,
                       int postType, String sortType, int filter, boolean nsfw) {
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.locale = locale;
            this.subredditName = subredditName;
            this.query = query;
            this.postType = postType;
            this.sortType = sortType;
            this.filter = filter;
            this.nsfw = nsfw;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == PostDataSource.TYPE_FRONT_PAGE) {
                return (T) new PostViewModel(retrofit, accessToken, locale, postType, sortType, filter,
                        nsfw);
            } else if (postType == PostDataSource.TYPE_SEARCH) {
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, query,
                        postType, sortType, filter, nsfw);
            } else if (postType == PostDataSource.TYPE_SUBREDDIT) {
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType,
                        sortType, filter, nsfw);
            } else {
                return (T) new PostViewModel(retrofit, accessToken, locale, subredditName, postType,
                        sortType, userWhere, filter, nsfw);
            }
        }
    }

    private static class NSFWAndSortTypeLiveData extends MediatorLiveData<Pair<Boolean, String>> {
        public NSFWAndSortTypeLiveData(LiveData<Boolean> nsfw, LiveData<String> sortType) {
            addSource(nsfw, accessToken1 -> setValue(Pair.create(accessToken1, sortType.getValue())));
            addSource(sortType, sortType1 -> setValue(Pair.create(nsfw.getValue(), sortType1)));
        }
    }
}
