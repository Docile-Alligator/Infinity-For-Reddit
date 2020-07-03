package ml.docilealligator.infinityforreddit.Event;

import ml.docilealligator.infinityforreddit.Message.Message;

public class RepliedToPrivateMessageEvent {
    public Message newReply;
    public int messagePosition;

    public RepliedToPrivateMessageEvent(Message newReply, int messagePosition) {
        this.newReply = newReply;
        this.messagePosition = messagePosition;
    }
}
