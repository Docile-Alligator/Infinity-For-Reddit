package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

class PaginationSynchronizer implements Parcelable {
    private boolean loadingState;
    private boolean loadSuccess;
    private PaginationNotifier paginationNotifier;
    private PaginationRetryNotifier paginationRetryNotifier;
    private LastItemSynchronizer lastItemSynchronizer;

    PaginationSynchronizer() {
        loadingState = false;
        loadSuccess = true;
    }

    protected PaginationSynchronizer(Parcel in) {
        loadingState = in.readByte() != 0;
        loadSuccess = in.readByte() != 0;
    }

    public static final Creator<PaginationSynchronizer> CREATOR = new Creator<PaginationSynchronizer>() {
        @Override
        public PaginationSynchronizer createFromParcel(Parcel in) {
            return new PaginationSynchronizer(in);
        }

        @Override
        public PaginationSynchronizer[] newArray(int size) {
            return new PaginationSynchronizer[size];
        }
    };

    public void setLoading(boolean isLoading) {
        this.loadingState = isLoading;
    }

    public boolean isLoading() {
        return loadingState;
    }

    public void setLoadingState(boolean state) {
        loadSuccess = state;
        if(loadSuccess) {
            paginationNotifier.LoadMorePostSuccess();
        } else {
            paginationNotifier.LoadMorePostFail();
        }
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

    public void setLastItemSynchronizer(LastItemSynchronizer lastItemSynchronizer) {
        this.lastItemSynchronizer = lastItemSynchronizer;
    }

    public LastItemSynchronizer getLastItemSynchronizer() {
        return lastItemSynchronizer;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeByte((byte) (loadingState ? 1 : 0));
        parcel.writeByte((byte) (loadSuccess ? 1 : 0));
    }
}

