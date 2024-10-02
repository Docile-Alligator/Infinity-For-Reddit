package ml.docilealligator.infinityforreddit.readpost;

import java.util.List;

public class ReadPostsList implements ReadPostsListInterface {
    private final ReadPostDao readPostDao;
    private final String accountName;

    public ReadPostsList(ReadPostDao readPostDao, String accountName) {
        this.accountName = accountName;
        this.readPostDao = readPostDao;
    }

    @Override
    public List<ReadPost> getReadPostsByIds(List<String> ids) {
        return this.readPostDao.getReadPostsByIds(ids, accountName);
    }
}
