package ml.docilealligator.infinityforreddit.multireddit;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AnonymousMultiredditSubredditDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(AnonymousMultiredditSubreddit anonymousMultiredditSubreddit);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<AnonymousMultiredditSubreddit> anonymousMultiredditSubreddits);

    @Query("SELECT * FROM anonymous_multireddit_subreddits WHERE path = :path ORDER BY subreddit_name COLLATE NOCASE ASC")
    List<AnonymousMultiredditSubreddit> getAllAnonymousMultiRedditSubreddits(String path);

    @Query("SELECT * FROM anonymous_multireddit_subreddits")
    List<AnonymousMultiredditSubreddit> getAllSubreddits();
}
