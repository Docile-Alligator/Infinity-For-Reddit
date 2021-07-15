package ml.docilealligator.infinityforreddit;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class RedditGalleryPayload {
    @SerializedName("sr")
    public String subredditName;
    @SerializedName("submit_type")
    public String submitType;
    @SerializedName("api_type")
    public String apiType = "json";
    @SerializedName("show_error_list")
    public boolean showErrorList = true;
    public String title;
    @SerializedName("spoiler")
    public boolean isSpoiler;
    @SerializedName("nsfw")
    public boolean isNSFW;
    public String kind = "self";
    @SerializedName("original_content")
    public boolean originalContent = false;
    @SerializedName("post_to_twitter")
    public boolean postToTwitter = false;
    public boolean sendReplies;
    @SerializedName("validate_on_submit")
    public boolean validateOnSubmit = true;
    public List<Item> items;

    public RedditGalleryPayload(String subredditName, String submitType, String kind, String title,
                                boolean isSpoiler, boolean isNSFW, boolean sendReplies, List<Item> items) {
        this.subredditName = subredditName;
        this.submitType = submitType;
        this.title = title;
        this.isSpoiler = isSpoiler;
        this.isNSFW = isNSFW;
        this.kind = kind;
        this.sendReplies = sendReplies;
        this.items = items;
    }

    public class Item {
        public String caption;
        @SerializedName("outbound_url")
        public String outboundUrl;
        @SerializedName("media_id")
        public String mediaId;

        public Item(String caption, String outboundUrl, String mediaId) {
            this.caption = caption;
            this.outboundUrl = outboundUrl;
            this.mediaId = mediaId;
        }
    }
}
