package User;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import android.content.Context;

@Database(entities = {UserData.class}, version = 1)
public abstract class UserRoomDatabase extends RoomDatabase {
    private static UserRoomDatabase INSTANCE;

    public abstract UserDao userDao();

    public static UserRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (UserRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            UserRoomDatabase.class, "users")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
