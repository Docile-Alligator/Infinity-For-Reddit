package ml.docilealligator.infinityforreddit;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface SubscribedUserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SubscribedUserData subscribedUserData);

    @Query("DELETE FROM subscribed_users")
    void deleteAllSubscribedUsers();

    @Query("SELECT * from subscribed_users ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllSubscribedUsers();
}
