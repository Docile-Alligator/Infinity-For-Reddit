package ml.docilealligator.infinityforreddit;

import android.os.Parcel;
import android.os.Parcelable;

import ml.docilealligator.infinityforreddit.utils.APIUtils;

public class RPANBroadcast implements Parcelable {

    public int upvotes;
    public int downvotes;
    public int uniqueWatchers;
    public int continuousWatchers;
    public int totalContinuousWatchers;
    public boolean chatDisabled;
    public double broadcastTime;
    public double estimatedRemainingTime;

    public RPANPost rpanPost;
    public RPANStream rpanStream;

    public RPANBroadcast(int upvotes, int downvotes, int uniqueWatchers, int continuousWatchers,
                         int totalContinuousWatchers, boolean chatDisabled, double broadcastTime,
                         double estimatedRemainingTime, RPANPost rpanPost, RPANStream rpanStream) {
        this.upvotes = upvotes;
        this.downvotes = downvotes;
        this.uniqueWatchers = uniqueWatchers;
        this.continuousWatchers = continuousWatchers;
        this.totalContinuousWatchers = totalContinuousWatchers;
        this.chatDisabled = chatDisabled;
        this.broadcastTime = broadcastTime;
        this.estimatedRemainingTime = estimatedRemainingTime;
        this.rpanPost = rpanPost;
        this.rpanStream = rpanStream;
    }

    protected RPANBroadcast(Parcel in) {
        upvotes = in.readInt();
        downvotes = in.readInt();
        uniqueWatchers = in.readInt();
        continuousWatchers = in.readInt();
        totalContinuousWatchers = in.readInt();
        chatDisabled = in.readByte() != 0;
        broadcastTime = in.readDouble();
        estimatedRemainingTime = in.readDouble();
        rpanPost = in.readParcelable(RPANPost.class.getClassLoader());
        rpanStream = in.readParcelable(RPANStream.class.getClassLoader());
    }

    public static final Creator<RPANBroadcast> CREATOR = new Creator<RPANBroadcast>() {
        @Override
        public RPANBroadcast createFromParcel(Parcel in) {
            return new RPANBroadcast(in);
        }

        @Override
        public RPANBroadcast[] newArray(int size) {
            return new RPANBroadcast[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(upvotes);
        parcel.writeInt(downvotes);
        parcel.writeInt(uniqueWatchers);
        parcel.writeInt(continuousWatchers);
        parcel.writeInt(totalContinuousWatchers);
        parcel.writeByte((byte) (chatDisabled ? 1 : 0));
        parcel.writeDouble(broadcastTime);
        parcel.writeDouble(estimatedRemainingTime);
        parcel.writeParcelable(rpanPost, i);
        parcel.writeParcelable(rpanStream, i);
    }

    public static class RPANPost implements Parcelable {
        public String fullname;
        public String title;
        public String subredditName;
        public String subredditIconUrl;
        public String username;
        public int postScore;
        public String voteState;
        public double upvoteRatio;
        public String postPermalink;
        public String rpanUrl;
        public boolean isNsfw;
        public boolean isLocked;
        public boolean isArchived;
        public boolean isSpoiler;
        public String suggestedCommentSort;
        public String liveCommentsWebsocketUrl;

        public RPANPost(String fullname, String title, String subredditName, String subredditIconUrl, String username,
                        int postScore, String voteState, double upvoteRatio, String postPermalink, String rpanUrl,
                        boolean isNsfw, boolean isLocked, boolean isArchived, boolean isSpoiler,
                        String suggestedCommentSort, String liveCommentsWebsocketUrl) {
            this.fullname = fullname;
            this.title = title;
            this.subredditName = subredditName;
            this.subredditIconUrl = subredditIconUrl;
            this.username = username;
            this.postScore = postScore;
            this.voteState = voteState;
            this.upvoteRatio = upvoteRatio;
            this.postPermalink = APIUtils.API_BASE_URI + postPermalink;
            this.rpanUrl = rpanUrl;
            this.isNsfw = isNsfw;
            this.isLocked = isLocked;
            this.isArchived = isArchived;
            this.isSpoiler = isSpoiler;
            this.suggestedCommentSort = suggestedCommentSort;
            this.liveCommentsWebsocketUrl = liveCommentsWebsocketUrl;
        }

        protected RPANPost(Parcel in) {
            fullname = in.readString();
            title = in.readString();
            subredditName = in.readString();
            subredditIconUrl = in.readString();
            username = in.readString();
            postScore = in.readInt();
            voteState = in.readString();
            upvoteRatio = in.readDouble();
            postPermalink = in.readString();
            rpanUrl = in.readString();
            isNsfw = in.readByte() != 0;
            isLocked = in.readByte() != 0;
            isArchived = in.readByte() != 0;
            isSpoiler = in.readByte() != 0;
            suggestedCommentSort = in.readString();
            liveCommentsWebsocketUrl = in.readString();
        }

        public static final Creator<RPANPost> CREATOR = new Creator<RPANPost>() {
            @Override
            public RPANPost createFromParcel(Parcel in) {
                return new RPANPost(in);
            }

            @Override
            public RPANPost[] newArray(int size) {
                return new RPANPost[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(fullname);
            parcel.writeString(title);
            parcel.writeString(subredditName);
            parcel.writeString(subredditIconUrl);
            parcel.writeString(username);
            parcel.writeInt(postScore);
            parcel.writeString(voteState);
            parcel.writeDouble(upvoteRatio);
            parcel.writeString(postPermalink);
            parcel.writeString(rpanUrl);
            parcel.writeByte((byte) (isNsfw ? 1 : 0));
            parcel.writeByte((byte) (isLocked ? 1 : 0));
            parcel.writeByte((byte) (isArchived ? 1 : 0));
            parcel.writeByte((byte) (isSpoiler ? 1 : 0));
            parcel.writeString(suggestedCommentSort);
            parcel.writeString(liveCommentsWebsocketUrl);
        }
    }

    public static class RPANStream implements Parcelable {
        public String streamId;
        public String hlsUrl;
        public String thumbnail;
        public int width;
        public int height;
        public long publishAt;
        public String state;

        public RPANStream(String streamId, String hlsUrl, String thumbnail, int width, int height, long publishAt,
                          String state) {
            this.streamId = streamId;
            this.hlsUrl = hlsUrl;
            this.thumbnail = thumbnail;
            this.width = width;
            this.height = height;
            this.publishAt = publishAt;
            this.state = state;
        }

        protected RPANStream(Parcel in) {
            streamId = in.readString();
            hlsUrl = in.readString();
            thumbnail = in.readString();
            width = in.readInt();
            height = in.readInt();
            publishAt = in.readLong();
            state = in.readString();
        }

        public static final Creator<RPANStream> CREATOR = new Creator<RPANStream>() {
            @Override
            public RPANStream createFromParcel(Parcel in) {
                return new RPANStream(in);
            }

            @Override
            public RPANStream[] newArray(int size) {
                return new RPANStream[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(streamId);
            parcel.writeString(hlsUrl);
            parcel.writeString(thumbnail);
            parcel.writeInt(width);
            parcel.writeInt(height);
            parcel.writeLong(publishAt);
            parcel.writeString(state);
        }
    }
}
