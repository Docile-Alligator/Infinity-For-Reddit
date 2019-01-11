package SubscribedUserDatabase;

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

    @Query("SELECT * FROM subscribed_users ORDER BY name COLLATE NOCASE ASC")
    LiveData<List<SubscribedUserData>> getAllSubscribedUsers();

    @Query("SELECT * FROM subscribed_users WHERE name = :userName LIMIT 1")
    SubscribedUserData getSubscribedUser(String userName);

    @Query("DELETE FROM subscribed_users WHERE name = :userName")
    void deleteSubscribedUser(String userName);
}
