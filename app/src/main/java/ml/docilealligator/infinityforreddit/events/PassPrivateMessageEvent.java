package ml.ino6962.postinfinityforreddit.events;

import ml.ino6962.postinfinityforreddit.message.Message;

public class PassPrivateMessageEvent {
    public Message message;

    public PassPrivateMessageEvent(Message message) {
        this.message = message;
    }
}
