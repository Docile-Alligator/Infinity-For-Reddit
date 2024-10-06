package ml.docilealligator.infinityforreddit.readpost;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class NullReadPostsList implements ReadPostsListInterface {
    public static NullReadPostsList getInstance() {
        return InstanceHolder.instance;
    }

    @Override
    public Set<String> getReadPostsIdsByIds(List<String> ids) {
        return Collections.emptySet();
    }

    private static class InstanceHolder {
        private static final NullReadPostsList instance = new NullReadPostsList();
    }
}
