package ml.docilealligator.infinityforreddit;

import com.google.gson.annotations.SerializedName;

public class PollPayload {
    @SerializedName("api_type")
    public String apiType = "json";
    @SerializedName("duration")
    public int duration;
    @SerializedName("nsfw")
    public boolean isNsfw;
    public String[] options;
    @SerializedName("flair_id")
    public String flairId;
    @SerializedName("flair_text")
    public String flairText;
    @SerializedName("post_to_twitter")
    public boolean postToTwitter = false;
    @SerializedName("sendreplies")
    public boolean sendReplies;
    @SerializedName("show_error_list")
    public boolean showErrorList = true;
    @SerializedName("spoiler")
    public boolean isSpoiler;
    @SerializedName("sr")
    public String subredditName;
    @SerializedName("submit_type")
    public String submitType;
    public String title;
    @SerializedName("validate_on_submit")
    public boolean validateOnSubmit = true;

    public PollPayload(String subredditName, String title, String[] options, int duration, boolean isNsfw,
                       boolean isSpoiler, Flair flair, boolean sendReplies, String submitType) {
        this.subredditName = subredditName;
        this.title = title;
        this.options = options;
        this.duration = duration;
        this.isNsfw = isNsfw;
        this.isSpoiler = isSpoiler;
        if (flair != null) {
            flairId = flair.getId();
            flairText = flair.getText();
        }
        this.sendReplies = sendReplies;
        this.submitType = submitType;
    }
}
