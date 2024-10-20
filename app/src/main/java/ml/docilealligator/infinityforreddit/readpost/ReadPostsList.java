package ml.docilealligator.infinityforreddit.readpost;

import androidx.annotation.WorkerThread;

import java.util.HashSet;
import java.util.List;

public class ReadPostsList implements ReadPostsListInterface {
    private final ReadPostDao readPostDao;
    private final String accountName;
    private final boolean readPostsDisabled;

    public ReadPostsList(ReadPostDao readPostDao, String accountName, boolean readPostsDisabled) {
        this.accountName = accountName;
        this.readPostDao = readPostDao;
        this.readPostsDisabled = readPostsDisabled;
    }

    @WorkerThread
    @Override
    public HashSet<String> getReadPostsIdsByIds(List<String> ids) {
        return readPostsDisabled ? new HashSet<>() : new HashSet<>(this.readPostDao.getReadPostsIdsByIds(ids, accountName));
    }
}
