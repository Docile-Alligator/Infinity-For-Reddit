package ml.docilealligator.infinityforreddit.readpost;

import java.util.HashSet;
import java.util.List;

public class ReadPostsList implements ReadPostsListInterface {
    private final ReadPostDao readPostDao;
    private final String accountName;

    public ReadPostsList(ReadPostDao readPostDao, String accountName) {
        this.accountName = accountName;
        this.readPostDao = readPostDao;
    }

    @Override
    public HashSet<String> getReadPostsIdsByIds(List<String> ids) {
        return new HashSet<>(
                this.readPostDao.getReadPostsIdsByIds(ids, accountName)
        );
    }
}
