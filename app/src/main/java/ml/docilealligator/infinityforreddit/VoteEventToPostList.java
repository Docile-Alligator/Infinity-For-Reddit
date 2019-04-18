package ml.docilealligator.infinityforreddit;

public class VoteEventToPostList {
    public final int positionInList;
    public final int voteType;

    public VoteEventToPostList(int positionInList, int voteType) {
        this.positionInList = positionInList;
        this.voteType = voteType;
    }
}
