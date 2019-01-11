package User;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;

@Dao
public interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserData userData);

    @Query("DELETE FROM users")
    void deleteAllUsers();

    @Query("SELECT * FROM users WHERE name = :userName LIMIT 1")
    LiveData<UserData> getUserLiveData(String userName);

    @Query("SELECT * FROM users WHERE name = :userName LIMIT 1")
    UserData getUserData(String userName);
}
