package ml.docilealligator.infinityforreddit.settings;

import android.net.Uri;

public class Acknowledgement {
    private String name;
    private String introduction;
    private Uri link;

    Acknowledgement(String name, String introduction, Uri link) {
        this.name = name;
        this.introduction = introduction;
        this.link = link;
    }

    public String getName() {
        return name;
    }

    public String getIntroduction() {
        return introduction;
    }

    public Uri getLink() {
        return link;
    }
}
