package ml.docilealligator.infinityforreddit;

class NetworkState {

  static final NetworkState LOADED;
  static final NetworkState LOADING;

  static {
    LOADED = new NetworkState(Status.SUCCESS, "Success");
    LOADING = new NetworkState(Status.LOADING, "Loading");
  }

  private final Status status;
  private final String msg;

  NetworkState(Status status, String msg) {
    this.status = status;
    this.msg = msg;
  }

  public Status getStatus() {
    return status;
  }

  public String getMsg() {
    return msg;
  }

  public enum Status {
    LOADING,
    SUCCESS,
    FAILED
  }
}
