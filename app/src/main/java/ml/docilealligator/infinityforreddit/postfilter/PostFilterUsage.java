package ml.docilealligator.infinityforreddit.postfilter;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(tableName = "post_filter_usage", primaryKeys = {"name", "usage", "name_of_usage"},
        foreignKeys = @ForeignKey(entity = PostFilter.class, parentColumns = "name",
                childColumns = "name", onDelete = ForeignKey.CASCADE))
public class PostFilterUsage {
    @NonNull
    @ColumnInfo(name = "name")
    public String name;
    @ColumnInfo(name = "usage")
    public int usage;
    @NonNull
    @ColumnInfo(name = "name_of_usage")
    public String nameOfUsage;

    public PostFilterUsage(@NonNull String name, int usage, String nameOfUsage) {
        this.name = name;
        this.usage = usage;
        this.nameOfUsage = nameOfUsage;
    }
}
