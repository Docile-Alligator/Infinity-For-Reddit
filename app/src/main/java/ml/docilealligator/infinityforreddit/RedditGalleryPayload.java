package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

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
    @SerializedName("sendreplies")
    public boolean sendReplies;
    @SerializedName("validate_on_submit")
    public boolean validateOnSubmit = true;
    @SerializedName("flair_id")
    public String flairId;
    @SerializedName("flair_text")
    public String flairText;
    public ArrayList<Item> items;

    public RedditGalleryPayload(String subredditName, String submitType, String title,
                                boolean isSpoiler, boolean isNSFW, boolean sendReplies, Flair flair, ArrayList<Item> items) {
        this.subredditName = subredditName;
        this.submitType = submitType;
        this.title = title;
        this.isSpoiler = isSpoiler;
        this.isNSFW = isNSFW;
        this.sendReplies = sendReplies;
        if (flair != null) {
            flairId = flair.getId();
            flairText = flair.getText();
        }
        this.items = items;
    }

    public static class Item implements Parcelable {
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

        protected Item(Parcel in) {
            caption = in.readString();
            outboundUrl = in.readString();
            mediaId = in.readString();
        }

        public static final Creator<Item> CREATOR = new Creator<Item>() {
            @Override
            public Item createFromParcel(Parcel in) {
                return new Item(in);
            }

            @Override
            public Item[] newArray(int size) {
                return new Item[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(caption);
            parcel.writeString(outboundUrl);
            parcel.writeString(mediaId);
        }

        public String getCaption() {
            return caption;
        }

        public void setCaption(String caption) {
            this.caption = caption == null ? "" : caption;
        }

        public String getOutboundUrl() {
            return outboundUrl;
        }

        public void setOutboundUrl(String outboundUrl) {
            this.outboundUrl = outboundUrl;
        }
    }
}
