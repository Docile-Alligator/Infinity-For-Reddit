package ml.docilealligator.infinityforreddit.message;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class Message implements Parcelable {
    public static final String TYPE_COMMENT = "t1";
    public static final String TYPE_ACCOUNT = "t2";
    public static final String TYPE_LINK = "t3";
    public static final String TYPE_MESSAGE = "t4";
    public static final String TYPE_SUBREDDIT = "t5";
    static final String TYPE_AWARD = "t6";

    private String kind;
    private String subredditName;
    private String subredditNamePrefixed;
    private String id;
    private String fullname;
    private String subject;
    private String author;
    private String destination;
    private String parentFullName;
    private String title;
    private String body;
    private String context;
    private String distinguished;
    private String formattedTime;
    private boolean wasComment;
    private boolean isNew;
    private int score;
    private int nComments;
    private long timeUTC;
    private ArrayList<Message> replies;

    Message(String kind, String subredditName, String subredditNamePrefixed, String id, String fullname,
            String subject, String author, String destination, String parentFullName, String title, String body,
            String context, String distinguished, String formattedTime, boolean wasComment, boolean isNew,
            int score, int nComments, long timeUTC) {
        this.kind = kind;
        this.subredditName = subredditName;
        this.subredditNamePrefixed = subredditNamePrefixed;
        this.id = id;
        this.fullname = fullname;
        this.subject = subject;
        this.author = author;
        this.destination = destination;
        this.parentFullName = parentFullName;
        this.title = title;
        this.body = body;
        this.context = context;
        this.distinguished = distinguished;
        this.formattedTime = formattedTime;
        this.wasComment = wasComment;
        this.isNew = isNew;
        this.score = score;
        this.nComments = nComments;
        this.timeUTC = timeUTC;
    }


    protected Message(Parcel in) {
        kind = in.readString();
        subredditName = in.readString();
        subredditNamePrefixed = in.readString();
        id = in.readString();
        fullname = in.readString();
        subject = in.readString();
        author = in.readString();
        destination = in.readString();
        parentFullName = in.readString();
        title = in.readString();
        body = in.readString();
        context = in.readString();
        distinguished = in.readString();
        formattedTime = in.readString();
        wasComment = in.readByte() != 0;
        isNew = in.readByte() != 0;
        score = in.readInt();
        nComments = in.readInt();
        timeUTC = in.readLong();
        replies = in.createTypedArrayList(Message.CREATOR);
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

    public String getKind() {
        return kind;
    }

    public String getSubredditName() {
        return subredditName;
    }

    public String getSubredditNamePrefixed() {
        return subredditNamePrefixed;
    }

    public String getId() {
        return id;
    }

    public String getFullname() {
        return fullname;
    }

    public String getSubject() {
        return subject;
    }

    public String getAuthor() {
        return author;
    }

    public String getDestination() {
        return destination;
    }

    public String getParentFullName() {
        return parentFullName;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public String getContext() {
        return context;
    }

    public String getDistinguished() {
        return distinguished;
    }

    public String getFormattedTime() {
        return formattedTime;
    }

    public boolean wasComment() {
        return wasComment;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public int getScore() {
        return score;
    }

    public int getnComments() {
        return nComments;
    }

    public long getTimeUTC() {
        return timeUTC;
    }

    public ArrayList<Message> getReplies() {
        return replies;
    }

    public void setReplies(ArrayList<Message> replies) {
        this.replies = replies;
    }

    public void addReply(Message reply) {
        if (replies == null) {
            replies = new ArrayList<>();
        }
        replies.add(reply);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(kind);
        parcel.writeString(subredditName);
        parcel.writeString(subredditNamePrefixed);
        parcel.writeString(id);
        parcel.writeString(fullname);
        parcel.writeString(subject);
        parcel.writeString(author);
        parcel.writeString(destination);
        parcel.writeString(parentFullName);
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(context);
        parcel.writeString(distinguished);
        parcel.writeString(formattedTime);
        parcel.writeByte((byte) (wasComment ? 1 : 0));
        parcel.writeByte((byte) (isNew ? 1 : 0));
        parcel.writeInt(score);
        parcel.writeInt(nComments);
        parcel.writeLong(timeUTC);
        parcel.writeTypedList(replies);
    }
}
