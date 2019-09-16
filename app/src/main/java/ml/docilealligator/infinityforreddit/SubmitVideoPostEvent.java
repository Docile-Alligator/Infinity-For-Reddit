package ml.docilealligator.infinityforreddit;

public class SubmitVideoPostEvent {

  public final boolean postSuccess;
  public final boolean errorProcessingVideo;
  public final String errorMessage;

  public SubmitVideoPostEvent(boolean postSuccess, boolean errorProcessingVideo,
      String errorMessage) {
    this.postSuccess = postSuccess;
    this.errorProcessingVideo = errorProcessingVideo;
    this.errorMessage = errorMessage;
  }
}
