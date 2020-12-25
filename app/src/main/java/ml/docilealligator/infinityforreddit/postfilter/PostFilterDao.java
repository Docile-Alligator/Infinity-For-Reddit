package ml.docilealligator.infinityforreddit.postfilter;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PostFilterDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(PostFilter postFilter);

    @Query("DELETE FROM post_filter")
    void deleteAllPostFilters();

    @Delete
    void deletePostFilter(PostFilter postFilter);

    @Query("SELECT * FROM post_filter WHERE name = :name LIMIT 1")
    PostFilter getPostFilter(String name);

    @Query("SELECT * FROM post_filter")
    LiveData<List<PostFilter>> getAllPostFiltersLiveData();
}
