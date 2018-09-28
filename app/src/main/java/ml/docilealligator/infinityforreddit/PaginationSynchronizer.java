package ml.docilealligator.infinityforreddit;

class PaginationSynchronizer {
    private boolean loadingState;
    private boolean loadSuccess;
    private PaginationNotifier paginationNotifier;
    private PaginationRetryNotifier paginationRetryNotifier;
    private LastItemSynchronizer lastItemSynchronizer;

    PaginationSynchronizer(LastItemSynchronizer lastItemSynchronizer) {
        loadingState = false;
        loadSuccess = true;
        this. lastItemSynchronizer = lastItemSynchronizer;
    }

    public void setLoadingState(boolean isLoading) {
        this.loadingState = isLoading;
    }

    public boolean isLoading() {
        return loadingState;
    }

    public void loadSuccess(boolean state) {
        loadSuccess = state;
        if(loadSuccess) {
            paginationNotifier.LoadMorePostSuccess();
        } else {
            paginationNotifier.LoadMorePostFail();
        }
    }

    public void setLoadSuccess(boolean loadSuccess) {
        this.loadSuccess = loadSuccess;
    }

    public boolean isLoadSuccess() {
        return loadSuccess;
    }

    public void setPaginationNotifier(PaginationNotifier paginationNotifier) {
        this.paginationNotifier = paginationNotifier;
    }

    public void setPaginationRetryNotifier(PaginationRetryNotifier paginationRetryNotifier) {
        this.paginationRetryNotifier = paginationRetryNotifier;
    }

    public PaginationRetryNotifier getPaginationRetryNotifier() {
        return paginationRetryNotifier;
    }

    public LastItemSynchronizer getLastItemSynchronizer() {
        return lastItemSynchronizer;
    }
}

