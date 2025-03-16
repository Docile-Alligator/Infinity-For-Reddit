package ml.docilealligator.infinityforreddit.readpost;

import java.util.List;
import java.util.Set;

public interface ReadPostsListInterface {
    Set<String> getReadPostsIdsByIds(List<String> ids);
}
