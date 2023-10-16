package ml.docilealligator.infinityforreddit.commentfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CommentFilterUsageDao {
    @Query("SELECT * FROM comment_filter_usage WHERE name = :name")
    LiveData<List<CommentFilterUsage>> getAllCommentFilterUsageLiveData(String name);

    @Query("SELECT * FROM comment_filter_usage WHERE name = :name")
    List<CommentFilterUsage> getAllCommentFilterUsage(String name);

    @Query("SELECT * FROM comment_filter_usage")
    List<CommentFilterUsage> getAllCommentFilterUsageForBackup();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CommentFilterUsage CommentFilterUsage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentFilterUsage> CommentFilterUsageList);

    @Delete
    void deleteCommentFilterUsage(CommentFilterUsage CommentFilterUsage);
}
