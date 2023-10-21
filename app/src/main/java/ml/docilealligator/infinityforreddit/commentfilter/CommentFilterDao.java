package ml.docilealligator.infinityforreddit.commentfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface CommentFilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(CommentFilter CommentFilter);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<CommentFilter> CommentFilters);

    @Query("DELETE FROM comment_filter")
    void deleteAllCommentFilters();

    @Delete
    void deleteCommentFilter(CommentFilter CommentFilter);

    @Query("DELETE FROM comment_filter WHERE name = :name")
    void deleteCommentFilter(String name);

    @Query("SELECT * FROM comment_filter WHERE name = :name LIMIT 1")
    CommentFilter getCommentFilter(String name);

    @Query("SELECT * FROM comment_filter ORDER BY name")
    LiveData<List<CommentFilter>> getAllCommentFiltersLiveData();

    @Query("SELECT * FROM comment_filter")
    List<CommentFilter> getAllCommentFilters();

    @Query("SELECT * FROM comment_filter WHERE (comment_filter.name IN " +
            "(SELECT comment_filter_usage.name FROM comment_filter_usage WHERE (usage = :usage AND name_of_usage = :nameOfUsage COLLATE NOCASE)))" +
            " OR (comment_filter.name NOT IN (SELECT comment_filter_usage.name FROM comment_filter_usage))")
    List<CommentFilter> getValidCommentFilters(int usage, String nameOfUsage);

    @Transaction
    @Query("SELECT * FROM comment_filter ORDER BY name")
    public LiveData<List<CommentFilterWithUsage>> getAllCommentFilterWithUsageLiveData();
}
