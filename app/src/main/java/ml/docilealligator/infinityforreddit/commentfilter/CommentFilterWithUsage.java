package ml.docilealligator.infinityforreddit.commentfilter;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class CommentFilterWithUsage {
    @Embedded
    public CommentFilter commentFilter;
    @Relation(
            parentColumn = "name",
            entityColumn = "name"
    )
    public List<CommentFilterUsage> commentFilterUsageList;
}
