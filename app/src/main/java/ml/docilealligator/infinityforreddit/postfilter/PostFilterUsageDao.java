package ml.docilealligator.infinityforreddit.postfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostFilterUsageDao {
    @Query("SELECT * FROM post_filter_usage WHERE name = :name")
    LiveData<List<PostFilterUsage>> getAllPostFilterUsageLiveData(String name);

    @Query("SELECT * FROM post_filter_usage WHERE name = :name")
    List<PostFilterUsage> getAllPostFilterUsage(String name);

    @Query("SELECT * FROM post_filter_usage")
    List<PostFilterUsage> getAllPostFilterUsageForBackup();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PostFilterUsage postFilterUsage);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<PostFilterUsage> postFilterUsageList);

    @Delete
    void deletePostFilterUsage(PostFilterUsage postFilterUsage);
}
