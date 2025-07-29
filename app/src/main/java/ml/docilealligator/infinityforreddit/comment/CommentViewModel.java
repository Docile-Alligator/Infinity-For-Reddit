package ml.docilealligator.infinityforreddit.comment;

import android.os.Handler;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.SingleLiveEvent;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.moderation.CommentModerationEvent;
import ml.docilealligator.infinityforreddit.thing.SortType;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CommentViewModel extends ViewModel {
    private final Retrofit retrofit;
    private final String accessToken;
    private final String accountName;
    private final CommentDataSourceFactory commentDataSourceFactory;
    private final LiveData<NetworkState> paginationNetworkState;
    private final LiveData<NetworkState> initialLoadingState;
    private final LiveData<Boolean> hasCommentLiveData;
    private final LiveData<PagedList<Comment>> comments;
    private final MutableLiveData<SortType> sortTypeLiveData;
    public final SingleLiveEvent<CommentModerationEvent> commentModerationEventLiveData = new SingleLiveEvent<>();

    public CommentViewModel(Executor executor, Handler handler, Retrofit retrofit, @Nullable String accessToken,
                            @NonNull String accountName, String username, SortType sortType, boolean areSavedComments) {
        this.retrofit = retrofit;
        this.accessToken = accessToken;
        this.accountName = accountName;
        commentDataSourceFactory = new CommentDataSourceFactory(executor, handler, retrofit, accessToken,
                accountName, username, sortType, areSavedComments);

        initialLoadingState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getInitialLoadStateLiveData);
        paginationNetworkState = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::getPaginationNetworkStateLiveData);
        hasCommentLiveData = Transformations.switchMap(commentDataSourceFactory.getCommentDataSourceLiveData(),
                CommentDataSource::hasPostLiveData);

        sortTypeLiveData = new MutableLiveData<>(sortType);

        PagedList.Config pagedListConfig =
                (new PagedList.Config.Builder())
                        .setEnablePlaceholders(false)
                        .setPageSize(100)
                        .setPrefetchDistance(10)
                        .setInitialLoadSizeHint(10)
                        .build();

        comments = Transformations.switchMap(sortTypeLiveData, sort -> {
            commentDataSourceFactory.changeSortType(sortTypeLiveData.getValue());
            return (new LivePagedListBuilder(commentDataSourceFactory, pagedListConfig)).build();
        });
    }

    public LiveData<PagedList<Comment>> getComments() {
        return comments;
    }

    public LiveData<NetworkState> getPaginationNetworkState() {
        return paginationNetworkState;
    }

    public LiveData<NetworkState> getInitialLoadingState() {
        return initialLoadingState;
    }

    public LiveData<Boolean> hasComment() {
        return hasCommentLiveData;
    }

    public void refresh() {
        commentDataSourceFactory.getCommentDataSource().invalidate();
    }

    public void retryLoadingMore() {
        commentDataSourceFactory.getCommentDataSource().retryLoadingMore();
    }

    public void changeSortType(SortType sortType) {
        sortTypeLiveData.postValue(sortType);
    }

    public void approveComment(Comment comment, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, comment.getFullName());

        retrofit.create(RedditAPI.class)
                .approveThing(APIUtils.getOAuthHeader(accessToken), params)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            List<Comment> snapshot = comments.getValue();
                            if (snapshot != null) {
                                if (position < snapshot.size() && position >= 0) {
                                    Comment moddedComment = snapshot.get(position);
                                    if (moddedComment != null) {
                                        moddedComment.setApproved(true);
                                        moddedComment.setApprovedAtUTC(System.currentTimeMillis());
                                        moddedComment.setApprovedBy(accountName);
                                        moddedComment.setRemoved(false, false);
                                    }
                                }
                            }
                            commentModerationEventLiveData.postValue(
                                    new CommentModerationEvent.Approved(comment, position)
                            );
                        } else {
                            commentModerationEventLiveData.postValue(
                                    new CommentModerationEvent.ApproveFailed(comment, position)
                            );
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        commentModerationEventLiveData.postValue(
                                new CommentModerationEvent.ApproveFailed(comment, position)
                        );
                    }
                });
    }

    public void removeComment(Comment comment, int position, boolean isSpam) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, comment.getFullName());
        params.put(APIUtils.SPAM_KEY, Boolean.toString(isSpam));

        retrofit.create(RedditAPI.class)
                .removeThing(APIUtils.getOAuthHeader(accessToken), params)
                .enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                        if (response.isSuccessful()) {
                            List<Comment> snapshot = comments.getValue();
                            if (snapshot != null) {
                                if (position < snapshot.size() && position >= 0) {
                                    Comment moddedComment = snapshot.get(position);
                                    if (moddedComment != null) {
                                        moddedComment.setRemoved(true, isSpam);
                                        moddedComment.setApproved(false);
                                        moddedComment.setApprovedAtUTC(0);
                                        moddedComment.setApprovedBy(null);
                                    }
                                }
                            }

                            commentModerationEventLiveData.postValue(
                                    isSpam
                                            ? new CommentModerationEvent.MarkedAsSpam(comment, position)
                                            : new CommentModerationEvent.Removed(comment, position)
                            );
                        } else {
                            commentModerationEventLiveData.postValue(
                                    isSpam
                                            ? new CommentModerationEvent.MarkAsSpamFailed(comment, position)
                                            : new CommentModerationEvent.RemoveFailed(comment, position)
                            );
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                        commentModerationEventLiveData.postValue(
                                isSpam
                                        ? new CommentModerationEvent.MarkAsSpamFailed(comment, position)
                                        : new CommentModerationEvent.RemoveFailed(comment, position)
                        );
                    }
                });
    }

    public void toggleLock(Comment comment, int position) {
        Map<String, String> params = new HashMap<>();
        params.put(APIUtils.ID_KEY, comment.getFullName());

        Call<String> call;
        if (comment.isLocked()) {
            call = retrofit.create(RedditAPI.class)
                    .unLockThing(APIUtils.getOAuthHeader(accessToken), params);
        } else {
            call = retrofit.create(RedditAPI.class)
                    .lockThing(APIUtils.getOAuthHeader(accessToken), params);
        }

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    commentModerationEventLiveData.postValue(
                            comment.isLocked()
                                    ? new CommentModerationEvent.Unlocked(comment, position)
                                    : new CommentModerationEvent.Locked(comment, position)
                    );

                    List<Comment> snapshot = comments.getValue();
                    if (snapshot != null) {
                        if (position < snapshot.size() && position >= 0) {
                            Comment moddedComment = snapshot.get(position);
                            if (moddedComment != null) {
                                moddedComment.setLocked(!moddedComment.isLocked());
                            }
                        }
                    }
                } else {
                    commentModerationEventLiveData.postValue(
                            comment.isLocked()
                                    ? new CommentModerationEvent.UnlockFailed(comment, position)
                                    : new CommentModerationEvent.LockFailed(comment, position)
                    );
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                commentModerationEventLiveData.postValue(
                        comment.isLocked()
                                ? new CommentModerationEvent.UnlockFailed(comment, position)
                                : new CommentModerationEvent.LockFailed(comment, position)
                );
            }
        });
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {
        private final Executor executor;
        private final Handler handler;
        private final Retrofit retrofit;
        private final String accessToken;
        private final String accountName;
        private final String username;
        private final SortType sortType;
        private final boolean areSavedComments;

        public Factory(Executor executor, Handler handler, Retrofit retrofit, @Nullable String accessToken,
                       @NonNull String accountName, String username, SortType sortType, boolean areSavedComments) {
            this.executor = executor;
            this.handler = handler;
            this.retrofit = retrofit;
            this.accessToken = accessToken;
            this.accountName = accountName;
            this.username = username;
            this.sortType = sortType;
            this.areSavedComments = areSavedComments;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new CommentViewModel(executor, handler, retrofit, accessToken, accountName, username,
                    sortType, areSavedComments);
        }
    }
}
