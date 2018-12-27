package ml.docilealligator.infinityforreddit;

class NetworkState {
    public enum Status{
        RUNNING,
        SUCCESS,
        FAILED
    }


    private final Status status;
    private final String msg;

    static final NetworkState LOADED;
    static final NetworkState LOADING;

    NetworkState(Status status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    static {
        LOADED=new NetworkState(Status.SUCCESS,"Success");
        LOADING=new NetworkState(Status.RUNNING,"Running");
    }

    public Status getStatus() {
        return status;
    }

    public String getMsg() {
        return msg;
    }
}
