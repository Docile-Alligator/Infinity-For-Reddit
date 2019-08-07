package Account;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

@Dao
public interface AccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Account account);

    @Query("DELETE FROM accounts")
    void deleteAllAccounts();

    @Query("SELECT * FROM accounts WHERE username = :userName COLLATE NOCASE LIMIT 1")
    LiveData<Account> getAccountLiveData(String userName);

    @Query("SELECT * FROM accounts WHERE username = :userName COLLATE NOCASE LIMIT 1")
    Account getAccountData(String userName);
}
