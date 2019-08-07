package Account;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Account.class}, version = 1)
public abstract class AccountRoomDatabase extends RoomDatabase {
    private static AccountRoomDatabase INSTANCE;

    public abstract AccountDao accountDao();

    public static AccountRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (AccountRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AccountRoomDatabase.class, "accounts")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
