package ml.docilealligator.infinityforreddit.postfilter;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PostFilterWithUsage {
    @Embedded
    public PostFilter postFilter;
    @Relation(
            parentColumn = "name",
            entityColumn = "name"
    )
    public List<PostFilterUsage> postFilterUsages;
}
