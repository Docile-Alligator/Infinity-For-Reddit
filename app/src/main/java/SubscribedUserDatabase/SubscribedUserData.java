package SubscribedUserDatabase;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity(tableName = "subscribed_users")
public class SubscribedUserData {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;
    @ColumnInfo(name = "icon")
    private String iconUrl;

    public SubscribedUserData(@NonNull String name, String iconUrl) {
        this.name = name;
        this.iconUrl = iconUrl;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }
}
