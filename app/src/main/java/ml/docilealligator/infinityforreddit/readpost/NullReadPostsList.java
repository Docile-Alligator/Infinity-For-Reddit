package ml.docilealligator.infinityforreddit.readpost;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;

import java.util.Collections;
import java.util.List;

public class NullReadPostsList implements ReadPostsListInterface {
    public static NullReadPostsList getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public List<ReadPost> getReadPostsByIds(List<String> ids) {
        return Collections.emptyList();
    }

    private static class InstanceHolder {
        private static final NullReadPostsList instance = new NullReadPostsList();
    }
}
