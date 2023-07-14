package ml.ino6962.postinfinityforreddit.postfilter;

import android.os.Handler;

import java.util.List;
import java.util.concurrent.Executor;

import ml.ino6962.postinfinityforreddit.RedditDataRoomDatabase;

public class SavePostFilter {
    public interface SavePostFilterListener {
        void success();
        void duplicate();
    }

    public static void savePostFilter(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                      PostFilter postFilter, String originalName, SavePostFilterListener savePostFilterListener) {
        executor.execute(() -> {
            if (!originalName.equals(postFilter.name) &&
                    redditDataRoomDatabase.postFilterDao().getPostFilter(postFilter.name) != null) {
                handler.post(savePostFilterListener::duplicate);
            } else {
                List<PostFilterUsage> postFilterUsages = redditDataRoomDatabase.postFilterUsageDao().getAllPostFilterUsage(originalName);
                if (!originalName.equals(postFilter.name)) {
                    redditDataRoomDatabase.postFilterDao().deletePostFilter(originalName);
                }
                redditDataRoomDatabase.postFilterDao().insert(postFilter);
                for (PostFilterUsage postFilterUsage : postFilterUsages) {
                    postFilterUsage.name = postFilter.name;
                    redditDataRoomDatabase.postFilterUsageDao().insert(postFilterUsage);
                }
                handler.post(savePostFilterListener::success);
            }
        });
    }
}
