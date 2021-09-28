package ml.docilealligator.infinityforreddit.asynctasks;

import android.os.Handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.multireddit.AnonymousMultiredditSubreddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiReddit;
import ml.docilealligator.infinityforreddit.multireddit.MultiRedditDao;

public class InsertMultireddit {

    public static void insertMultireddits(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                          ArrayList<MultiReddit> multiReddits, String accountName,
                                          InsertMultiRedditListener insertMultiRedditListener) {
        executor.execute(() -> {
            MultiRedditDao multiRedditDao = redditDataRoomDatabase.multiRedditDao();
            List<MultiReddit> existingMultiReddits = multiRedditDao.getAllMultiRedditsList(accountName);
            Collections.sort(multiReddits, (multiReddit, t1) -> multiReddit.getName().compareToIgnoreCase(t1.getName()));
            List<String> deletedMultiredditNames = new ArrayList<>();
            compareTwoMultiRedditList(multiReddits, existingMultiReddits, deletedMultiredditNames);

            for (String deleted : deletedMultiredditNames) {
                multiRedditDao.deleteMultiReddit(deleted, accountName);
            }

            for (MultiReddit multiReddit : multiReddits) {
                multiRedditDao.insert(multiReddit);
            }

            handler.post(insertMultiRedditListener::success);
        });
    }

    public static void insertMultireddit(Executor executor, Handler handler, RedditDataRoomDatabase redditDataRoomDatabase,
                                         MultiReddit multiReddit,
                                         InsertMultiRedditListener insertMultiRedditListener) {
        executor.execute(() -> {
            if (multiReddit.getOwner().equals("-")) {
                ArrayList<AnonymousMultiredditSubreddit> allAnonymousMultiRedditSubreddits =
                        (ArrayList<AnonymousMultiredditSubreddit>) redditDataRoomDatabase.anonymousMultiredditSubredditDao().getAllAnonymousMultiRedditSubreddits(multiReddit.getPath());
                redditDataRoomDatabase.multiRedditDao().insert(multiReddit);
                if (allAnonymousMultiRedditSubreddits != null) {
                    redditDataRoomDatabase.anonymousMultiredditSubredditDao().insertAll(allAnonymousMultiRedditSubreddits);
                }
            } else {
                redditDataRoomDatabase.multiRedditDao().insert(multiReddit);
            }
            handler.post(insertMultiRedditListener::success);
        });
    }

    private static void compareTwoMultiRedditList(List<MultiReddit> newMultiReddits,
                                              List<MultiReddit> oldMultiReddits,
                                              List<String> deletedMultiReddits) {
        int newIndex = 0;
        for (int oldIndex = 0; oldIndex < oldMultiReddits.size(); oldIndex++) {
            if (newIndex >= newMultiReddits.size()) {
                for (; oldIndex < oldMultiReddits.size(); oldIndex++) {
                    deletedMultiReddits.add(oldMultiReddits.get(oldIndex).getName());
                }
                return;
            }

            MultiReddit old = oldMultiReddits.get(oldIndex);
            for (; newIndex < newMultiReddits.size(); newIndex++) {
                if (newMultiReddits.get(newIndex).getName().compareToIgnoreCase(old.getName()) == 0) {
                    newIndex++;
                    break;
                }
                if (newMultiReddits.get(newIndex).getName().compareToIgnoreCase(old.getName()) > 0) {
                    deletedMultiReddits.add(old.getName());
                    break;
                }
            }
        }
    }

    public interface InsertMultiRedditListener {
        void success();
    }
}
