package ml.docilealligator.infinityforreddit;

import java.util.ArrayList;

class PaginationSynchronizer {
    private boolean loadingState;
    private boolean loadSuccess;
    private PaginationNotifier paginationNotifier;
    private PaginationRetryNotifier paginationRetryNotifier;
    private ArrayList<LastItemSynchronizer> lastItemSynchronizers;

    PaginationSynchronizer() {
        lastItemSynchronizers = new ArrayList<>();
        loadingState = false;
        loadSuccess = true;
    }

    void setLoadingState(boolean isLoading) {
        this.loadingState = isLoading;
    }

    public boolean isLoading() {
        return loadingState;
    }

    void loadSuccess(boolean state) {
        loadSuccess = state;
        if(loadSuccess) {
            paginationNotifier.LoadMorePostSuccess();
        } else {
            paginationNotifier.LoadMorePostFail();
        }
    }

    void setLoadSuccess(boolean loadSuccess) {
        this.loadSuccess = loadSuccess;
    }

    boolean isLoadingMorePostsSuccess() {
        return loadSuccess;
    }

    void setPaginationNotifier(PaginationNotifier paginationNotifier) {
        this.paginationNotifier = paginationNotifier;
    }

    void setPaginationRetryNotifier(PaginationRetryNotifier paginationRetryNotifier) {
        this.paginationRetryNotifier = paginationRetryNotifier;
    }

    PaginationRetryNotifier getPaginationRetryNotifier() {
        return paginationRetryNotifier;
    }

    void addLastItemSynchronizer(LastItemSynchronizer lastItemSynchronizer) {
        lastItemSynchronizers.add(lastItemSynchronizer);
    }

    void notifyLastItemChanged(String lastItem) {
        for(LastItemSynchronizer l : lastItemSynchronizers) {
            l.lastItemChanged(lastItem);
        }
    }
}

