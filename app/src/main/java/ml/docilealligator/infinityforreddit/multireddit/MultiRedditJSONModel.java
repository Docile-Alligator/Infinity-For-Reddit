package ml.docilealligator.infinityforreddit.multireddit;

import com.google.gson.Gson;

import java.util.ArrayList;

public class MultiRedditJSONModel {
    private String display_name;
    private String description_md;
    private String visibility;
    private SubredditInMultiReddit[] subreddits;

    public MultiRedditJSONModel() {}

    public MultiRedditJSONModel(String display_name, String description_md, boolean isPrivate,
                                ArrayList<String> subreddits) {
        this.display_name = display_name;
        this.description_md = description_md;
        if (isPrivate) {
            visibility = "private";
        } else {
            visibility = "public";
        }

        if (subreddits != null) {
            this.subreddits = new SubredditInMultiReddit[subreddits.size()];
            for (int i = 0; i < subreddits.size(); i++) {
                SubredditInMultiReddit subredditInMultiReddit = new SubredditInMultiReddit(subreddits.get(i));
                this.subreddits[i] = subredditInMultiReddit;
            }
        }
    }

    public String createJSONModel() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
