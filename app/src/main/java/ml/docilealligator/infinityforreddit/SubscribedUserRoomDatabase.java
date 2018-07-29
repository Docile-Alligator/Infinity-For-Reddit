package ml.docilealligator.infinityforreddit;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

@Database(entities = {SubscribedUserData.class}, version = 1)
public abstract class SubscribedUserRoomDatabase extends RoomDatabase {
    private static SubscribedUserRoomDatabase INSTANCE;

    public abstract SubscribedUserDao subscribedUserDao();

    public static SubscribedUserRoomDatabase getDatabase(final Context context) {
        if(INSTANCE == null) {
            synchronized (SubscribedUserRoomDatabase.class) {
                if(INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            SubscribedUserRoomDatabase.class, "subscribed_users")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
