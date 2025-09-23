package ml.docilealligator.infinityforreddit.post;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelKt;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.Pager;
import androidx.paging.PagingConfig;
import androidx.paging.PagingData;
import androidx.paging.PagingDataTransforms;
import androidx.paging.PagingLiveData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.SingleLiveEvent;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.moderation.PostModerationEvent;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPostsListInterface;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PostViewModel extends ViewModel {
    private final Executor executor;
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final SharedPreferences sharedPreferences;
    private final SharedPreferences postFeedScrolledPositionSharedPreferences;
    private String name;
    private String query;
    private String trendingSource;
    private final int postType;
    private SortType sortType;
    private PostFilter postFilter;
    private String userWhere;
    private ReadPostsListInterface readPostsList;
    private final MutableLiveData<Boolean> hideReadPostsValue = new MutableLiveData<>();

    private final LiveData<PagingData<Post>> posts;
    private final LiveData<PagingData<Post>> postsWithReadPostsHidden;

    private final MutableLiveData<SortType> sortTypeLiveData;
    private final MutableLiveData<PostFilter> postFilterLiveData;
    private final SortTypeAndPostFilterLiveData sortTypeAndPostFilterLiveData;

    public final SingleLiveEvent<PostModerationEvent> moderationEventLiveData = new SingleLiveEvent<>();

    // PostPagingSource.TYPE_FRONT_PAGE
    public PostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                         @Nullable SharedPreferences postHistorySharedPreferences, int postType,
                         SortType sortType, PostFilter postFilter, ReadPostsListInterface readPostsList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostsList = readPostsList;

        sortTypeLiveData = new MutableLiveData<>(sortType);
        postFilterLiveData = new MutableLiveData<>(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(100, 4, false, 10), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });

        postsWithReadPostsHidden = PagingLiveData.cachedIn(Transformations.switchMap(hideReadPostsValue,
                currentlyReadPostIds -> Transformations.map(
                        posts,
                        postPagingData -> PagingDataTransforms.filter(
                                postPagingData, executor,
                                post -> !post.isRead() || !hideReadPostsValue.getValue()))), ViewModelKt.getViewModelScope(this));

        hideReadPostsValue.setValue(postHistorySharedPreferences != null
                && postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));
    }

    // PostPagingSource.TYPE_SUBREDDIT || PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE || PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT
    public PostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                         @Nullable SharedPreferences postHistorySharedPreferences, String subredditName, int postType,
                         SortType sortType, PostFilter postFilter, ReadPostsListInterface readPostsList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostsList = readPostsList;
        this.name = subredditName;

        sortTypeLiveData = new MutableLiveData<>(sortType);
        postFilterLiveData = new MutableLiveData<>(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(100, 4, false, 10), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });

        postsWithReadPostsHidden = PagingLiveData.cachedIn(Transformations.switchMap(hideReadPostsValue,
                currentlyReadPostIds -> Transformations.map(
                        posts,
                        postPagingData -> PagingDataTransforms.filter(
                                postPagingData, executor,
                                post -> !post.isRead() || !hideReadPostsValue.getValue()))), ViewModelKt.getViewModelScope(this));

        hideReadPostsValue.setValue(postHistorySharedPreferences != null
                && postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false)
                && (postType != PostPagingSource.TYPE_SUBREDDIT || subredditName.equals("all") || subredditName.equals("popular")));
    }

    // PostPagingSource.TYPE_MULTI_REDDIT
    public PostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                         @Nullable SharedPreferences postHistorySharedPreferences, String multiredditPath, String query, int postType,
                         SortType sortType, PostFilter postFilter, ReadPostsListInterface readPostsList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostsList = readPostsList;
        this.name = multiredditPath;
        this.query = query;

        sortTypeLiveData = new MutableLiveData<>(sortType);
        postFilterLiveData = new MutableLiveData<>(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(100, 4, false, 10), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });

        postsWithReadPostsHidden = PagingLiveData.cachedIn(Transformations.switchMap(hideReadPostsValue,
                currentlyReadPostIds -> Transformations.map(
                        posts,
                        postPagingData -> PagingDataTransforms.filter(
                                postPagingData, executor,
                                post -> !post.isRead() || !hideReadPostsValue.getValue()))), ViewModelKt.getViewModelScope(this));

        hideReadPostsValue.setValue(postHistorySharedPreferences != null
                && postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));
    }

    // PostPagingSource.TYPE_USER
    public PostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                         SharedPreferences sharedPreferences,
                         SharedPreferences postFeedScrolledPositionSharedPreferences,
                         @Nullable SharedPreferences postHistorySharedPreferences, String username,
                         int postType, SortType sortType, PostFilter postFilter, String userWhere,
                         ReadPostsListInterface readPostsList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostsList = readPostsList;
        this.name = username;
        this.userWhere = userWhere;

        sortTypeLiveData = new MutableLiveData<>(sortType);
        postFilterLiveData = new MutableLiveData<>(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(100, 4, false, 10), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });

        postsWithReadPostsHidden = PagingLiveData.cachedIn(Transformations.switchMap(hideReadPostsValue,
                currentlyReadPostIds -> Transformations.map(
                        posts,
                        postPagingData -> PagingDataTransforms.filter(
                                postPagingData, executor,
                                post -> !post.isRead() || !hideReadPostsValue.getValue()))), ViewModelKt.getViewModelScope(this));

        hideReadPostsValue.setValue(false);
        /*hideReadPostsValue.setValue(postHistorySharedPreferences != null
                && postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));*/
    }

    // postType == PostPagingSource.TYPE_SEARCH
    public PostViewModel(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                         SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                         @Nullable SharedPreferences postHistorySharedPreferences, String subredditName, String query,
                         String trendingSource, int postType, SortType sortType, PostFilter postFilter,
                         ReadPostsListInterface readPostsList) {
        this.executor = executor;
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        this.sharedPreferences = sharedPreferences;
        this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
        this.postType = postType;
        this.sortType = sortType;
        this.postFilter = postFilter;
        this.readPostsList = readPostsList;
        this.name = subredditName;
        this.query = query;
        this.trendingSource = trendingSource;

        sortTypeLiveData = new MutableLiveData<>(sortType);
        postFilterLiveData = new MutableLiveData<>(postFilter);

        sortTypeAndPostFilterLiveData = new SortTypeAndPostFilterLiveData(sortTypeLiveData, postFilterLiveData);

        Pager<String, Post> pager = new Pager<>(new PagingConfig(100, 4, false, 10), this::returnPagingSoruce);

        posts = Transformations.switchMap(sortTypeAndPostFilterLiveData, sortAndPostFilter -> {
            changeSortTypeAndPostFilter(
                    sortTypeLiveData.getValue(), postFilterLiveData.getValue());
            return PagingLiveData.cachedIn(PagingLiveData.getLiveData(pager), ViewModelKt.getViewModelScope(this));
        });

        postsWithReadPostsHidden = PagingLiveData.cachedIn(Transformations.switchMap(hideReadPostsValue,
                currentlyReadPostIds -> Transformations.map(
                        posts,
                        postPagingData -> PagingDataTransforms.filter(
                                postPagingData, executor,
                                post -> !post.isRead() || !hideReadPostsValue.getValue()))), ViewModelKt.getViewModelScope(this));

        hideReadPostsValue.setValue(false);
        /*hideReadPostsValue.setValue(postHistorySharedPreferences != null
                && postHistorySharedPreferences.getBoolean((accountName.equals(Account.ANONYMOUS_ACCOUNT) ? "" : accountName) + SharedPreferencesUtils.HIDE_READ_POSTS_AUTOMATICALLY_BASE, false));*/
    }

    public LiveData<PagingData<Post>> getPosts() {
        return postsWithReadPostsHidden;
    }

    public void hideReadPosts() {
        hideReadPostsValue.setValue(true);
    }

    public PostPagingSource returnPagingSoruce() {
        PostPagingSource paging3PagingSource;
        switch (postType) {
            case PostPagingSource.TYPE_FRONT_PAGE:
                paging3PagingSource = new PostPagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, postType, sortType,
                        postFilter, readPostsList);
                break;
            case PostPagingSource.TYPE_SUBREDDIT:
            case PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE:
            case PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT:
                paging3PagingSource = new PostPagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                        sortType, postFilter, readPostsList);
                break;
            case PostPagingSource.TYPE_MULTI_REDDIT:
                paging3PagingSource = new PostPagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, query, postType,
                        sortType, postFilter, readPostsList);
                break;
            case PostPagingSource.TYPE_SEARCH:
                paging3PagingSource = new PostPagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, query, trendingSource,
                        postType, sortType, postFilter, readPostsList);
                break;
            default:
                //User
                paging3PagingSource = new PostPagingSource(executor, retrofit, accessToken, accountName,
                        sharedPreferences, postFeedScrolledPositionSharedPreferences, name, postType,
                        sortType, postFilter, userWhere, readPostsList);
                break;
        }
        return paging3PagingSource;
    }

    private void changeSortTypeAndPostFilter(SortType sortType, PostFilter postFilter) {
        this.sortType = sortType;
        this.postFilter = postFilter;
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public void changePostFilter(PostFilter postFilter) {
        postFilterLiveData.postValue(postFilter);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Executor executor;
        private final Retrofit retrofit;
        private String accessToken;
        private String accountName;
        private final SharedPreferences sharedPreferences;
        private SharedPreferences postFeedScrolledPositionSharedPreferences;
        private SharedPreferences postHistorySharedPreferences;
        private String name;
        private String query;
        private String trendingSource;
        private final int postType;
        private final SortType sortType;
        private final PostFilter postFilter;
        private String userWhere;
        private final ReadPostsListInterface readPostsList;

        // Front page
        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       SharedPreferences postHistorySharedPreferences, int postType, SortType sortType,
                       PostFilter postFilter, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postHistorySharedPreferences = postHistorySharedPreferences;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostsList = readPostsList;
        }

        // PostPagingSource.TYPE_SUBREDDIT
        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       SharedPreferences postHistorySharedPreferences, String name, int postType, SortType sortType,
                       PostFilter postFilter, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postHistorySharedPreferences = postHistorySharedPreferences;
            this.name = name;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostsList = readPostsList;
        }

        // PostPagingSource.TYPE_MULTI_REDDIT
        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       SharedPreferences postHistorySharedPreferences, String name, String query, int postType, SortType sortType,
                       PostFilter postFilter, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postHistorySharedPreferences = postHistorySharedPreferences;
            this.name = name;
            this.query = query;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostsList = readPostsList;
        }

        //User posts
        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       SharedPreferences postHistorySharedPreferences, String username, int postType,
                       SortType sortType, PostFilter postFilter, String where, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postHistorySharedPreferences = postHistorySharedPreferences;
            this.name = username;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            userWhere = where;
            this.readPostsList = readPostsList;
        }

        // PostPagingSource.TYPE_SEARCH
        public Factory(Executor executor, Retrofit retrofit, @Nullable String accessToken, @NonNull String accountName,
                       SharedPreferences sharedPreferences, SharedPreferences postFeedScrolledPositionSharedPreferences,
                       SharedPreferences postHistorySharedPreferences, String name, String query, String trendingSource,
                       int postType, SortType sortType, PostFilter postFilter, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.sharedPreferences = sharedPreferences;
            this.postFeedScrolledPositionSharedPreferences = postFeedScrolledPositionSharedPreferences;
            this.postHistorySharedPreferences = postHistorySharedPreferences;
            this.name = name;
            this.query = query;
            this.trendingSource = trendingSource;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostsList = readPostsList;
        }

        //Anonymous Front Page
        public Factory(Executor executor, Retrofit retrofit, SharedPreferences sharedPreferences,
                       String concatenatedSubredditNames, int postType, SortType sortType, PostFilter postFilter, ReadPostsListInterface readPostsList) {
            this.executor = executor;
            this.retrofit = retrofit;
            this.sharedPreferences = sharedPreferences;
            this.name = concatenatedSubredditNames;
            this.postType = postType;
            this.sortType = sortType;
            this.postFilter = postFilter;
            this.readPostsList = readPostsList;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            if (postType == PostPagingSource.TYPE_FRONT_PAGE) {
                return (T) new PostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postHistorySharedPreferences, postType,
                        sortType, postFilter, readPostsList);
            } else if (postType == PostPagingSource.TYPE_SEARCH) {
                return (T) new PostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postHistorySharedPreferences, name, query,
                        trendingSource, postType, sortType, postFilter, readPostsList);
            } else if (postType == PostPagingSource.TYPE_SUBREDDIT) {
                return (T) new PostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postHistorySharedPreferences, name,
                        postType, sortType, postFilter, readPostsList);
            } else if (postType == PostPagingSource.TYPE_MULTI_REDDIT) {
                return (T) new PostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postHistorySharedPreferences, name, query,
                        postType, sortType, postFilter, readPostsList);
            } else if (postType == PostPagingSource.TYPE_ANONYMOUS_FRONT_PAGE || postType == PostPagingSource.TYPE_ANONYMOUS_MULTIREDDIT) {
                return (T) new PostViewModel(executor, retrofit, null, null, sharedPreferences,
                        null, null, name, postType, sortType,
                        postFilter, readPostsList);
            } else {
                return (T) new PostViewModel(executor, retrofit, accessToken, accountName, sharedPreferences,
                        postFeedScrolledPositionSharedPreferences, postHistorySharedPreferences, name,
                        postType, sortType, postFilter, userWhere, readPostsList);
            }
        }
    }

    private static class SortTypeAndPostFilterLiveData extends MediatorLiveData<Pair<PostFilter, SortType>> {
        public SortTypeAndPostFilterLiveData(LiveData<SortType> sortTypeLiveData, LiveData<PostFilter> postFilterLiveData) {
            addSource(sortTypeLiveData, sortType -> setValue(Pair.create(postFilterLiveData.getValue(), sortType)));
            addSource(postFilterLiveData, postFilter -> setValue(Pair.create(postFilter, sortTypeLiveData.getValue())));
        }
    }

    public void approvePost(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        retrofit.create(RedditAPI.class).approveThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setApproved(true);
                    post.setApprovedBy(accountName);
                    post.setApprovedAtUTC(System.currentTimeMillis());
                    post.setRemoved(false, false);
                    moderationEventLiveData.postValue(new PostModerationEvent.Approved(post, position));
                } else {
                    moderationEventLiveData.postValue(new PostModerationEvent.ApproveFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(new PostModerationEvent.ApproveFailed(post, position));
            }
        });
    }

    public void removePost(@NonNull Post post, int position, boolean isSpam) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        params.put(APIUtils.SPAM_KEY, Boolean.toString(isSpam));
        retrofit.create(RedditAPI.class).removeThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setApproved(false);
                    post.setApprovedBy(null);
                    post.setApprovedAtUTC(0);
                    post.setRemoved(true, isSpam);
                    moderationEventLiveData.postValue(isSpam ? new PostModerationEvent.MarkedAsSpam(post, position): new PostModerationEvent.Removed(post, position));
                } else {
                    moderationEventLiveData.postValue(isSpam ? new PostModerationEvent.MarkAsSpamFailed(post, position) : new PostModerationEvent.RemoveFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(isSpam ? new PostModerationEvent.MarkAsSpamFailed(post, position) : new PostModerationEvent.RemoveFailed(post, position));
            }
        });
    }

    public void toggleSticky(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        params.put(APIUtils.STATE_KEY, Boolean.toString(!post.isStickied()));
        params.put(APIUtils.API_TYPE_KEY, APIUtils.API_TYPE_JSON);
        retrofit.create(RedditAPI.class).toggleStickyPost(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setIsStickied(!post.isStickied());
                    moderationEventLiveData.postValue(post.isStickied() ? new PostModerationEvent.SetStickyPost(post, position): new PostModerationEvent.UnsetStickyPost(post, position));
                } else {
                    moderationEventLiveData.postValue(post.isStickied() ? new PostModerationEvent.UnsetStickyPostFailed(post, position) : new PostModerationEvent.SetStickyPostFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(post.isStickied() ? new PostModerationEvent.UnsetStickyPostFailed(post, position) : new PostModerationEvent.SetStickyPostFailed(post, position));
            }
        });
    }

    public void toggleLock(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        Call<String> call = post.isLocked() ? retrofit.create(RedditAPI.class).unLockThing(APIUtils.getOAuthHeader(accessToken), params) : retrofit.create(RedditAPI.class).lockThing(APIUtils.getOAuthHeader(accessToken), params);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setIsLocked(!post.isLocked());
                    moderationEventLiveData.postValue(post.isLocked() ? new PostModerationEvent.Locked(post, position): new PostModerationEvent.Unlocked(post, position));
                } else {
                    moderationEventLiveData.postValue(post.isLocked() ? new PostModerationEvent.UnlockFailed(post, position) : new PostModerationEvent.LockFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(post.isLocked() ? new PostModerationEvent.UnlockFailed(post, position) : new PostModerationEvent.LockFailed(post, position));
            }
        });
    }

    public void toggleNSFW(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        Call<String> call = post.isNSFW() ? retrofit.create(RedditAPI.class).unmarkNSFW(APIUtils.getOAuthHeader(accessToken), params) : retrofit.create(RedditAPI.class).markNSFW(APIUtils.getOAuthHeader(accessToken), params);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setNSFW(!post.isNSFW());
                    moderationEventLiveData.postValue(post.isNSFW() ? new PostModerationEvent.MarkedNSFW(post, position): new PostModerationEvent.UnmarkedNSFW(post, position));
                } else {
                    moderationEventLiveData.postValue(post.isNSFW() ? new PostModerationEvent.UnmarkNSFWFailed(post, position) : new PostModerationEvent.MarkNSFWFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(post.isNSFW() ? new PostModerationEvent.UnmarkNSFWFailed(post, position) : new PostModerationEvent.MarkNSFWFailed(post, position));
            }
        });
    }

    public void toggleSpoiler(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        Call<String> call = post.isSpoiler() ? retrofit.create(RedditAPI.class).unmarkSpoiler(APIUtils.getOAuthHeader(accessToken), params) : retrofit.create(RedditAPI.class).markSpoiler(APIUtils.getOAuthHeader(accessToken), params);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setSpoiler(!post.isSpoiler());
                    moderationEventLiveData.postValue(post.isSpoiler() ? new PostModerationEvent.MarkedSpoiler(post, position): new PostModerationEvent.UnmarkedSpoiler(post, position));
                } else {
                    moderationEventLiveData.postValue(post.isSpoiler() ? new PostModerationEvent.UnmarkSpoilerFailed(post, position) : new PostModerationEvent.MarkSpoilerFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(post.isSpoiler() ? new PostModerationEvent.UnmarkSpoilerFailed(post, position) : new PostModerationEvent.MarkSpoilerFailed(post, position));
            }
        });
    }

    public void toggleMod(@NonNull Post post, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, post.getFullName());
        params.put(APIUtils.HOW_KEY, post.isModerator() ? APIUtils.HOW_NO : APIUtils.HOW_YES);
        retrofit.create(RedditAPI.class).toggleDistinguishedThing(APIUtils.getOAuthHeader(accessToken), params).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    post.setIsModerator(!post.isModerator());
                    moderationEventLiveData.postValue(post.isModerator() ? new PostModerationEvent.DistinguishedAsMod(post, position): new PostModerationEvent.UndistinguishedAsMod(post, position));
                } else {
                    moderationEventLiveData.postValue(post.isModerator() ? new PostModerationEvent.UndistinguishAsModFailed(post, position) : new PostModerationEvent.DistinguishAsModFailed(post, position));
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable throwable) {
                moderationEventLiveData.postValue(post.isModerator() ? new PostModerationEvent.UndistinguishAsModFailed(post, position) : new PostModerationEvent.DistinguishAsModFailed(post, position));
            }
        });
    }
}
