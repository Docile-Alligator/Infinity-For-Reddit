package ml.docilealligator.infinityforreddit;

import android.os.Handler;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.readpost.ReadPost;

public class FetchPostFilterAndReadPosts {
    public interface FetchPostFilterAndReadPostsListener {
        void success(PostFilter postFilter, ArrayList<ReadPost> readPostList);
    }

    public static void fetchPostFilterAndReadPosts(RedditDataRoomDatabase redditDataRoomDatabase, Executor executor,
                                                   Handler handler, String accountName, int postFilterUsage,
                                                   String nameOfUsage, FetchPostFilterAndReadPostsListener fetchPostFilterAndReadPostsListener) {
        executor.execute(() -> {
            List<PostFilter> postFilters = redditDataRoomDatabase.postFilterDao().getValidPostFilters(postFilterUsage, nameOfUsage);
            PostFilter mergedPostFilter = PostFilter.mergePostFilter(postFilters);
            if (accountName != null) {
                ArrayList<ReadPost> readPosts = (ArrayList<ReadPost>) redditDataRoomDatabase.readPostDao().getAllReadPosts(accountName);
                handler.post(() -> fetchPostFilterAndReadPostsListener.success(mergedPostFilter, readPosts));
            } else {
                handler.post(() -> fetchPostFilterAndReadPostsListener.success(mergedPostFilter, null));
            }
        });
    }
}
