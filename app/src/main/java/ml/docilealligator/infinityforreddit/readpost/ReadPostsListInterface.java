package ml.docilealligator.infinityforreddit.readpost;


import java.util.List;

public interface ReadPostsListInterface {
    List<ReadPost> getReadPostsByIds(List<String> ids);
}
