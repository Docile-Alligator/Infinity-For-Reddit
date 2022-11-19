package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SortType {

    @NonNull
    private final Type type;
    @Nullable
    private final Time time;

    public SortType(@NonNull Type type) {
        this(type, null);
    }

    public SortType(@NonNull Type type, @Nullable Time time) {
        this.type = type;
        this.time = time;
    }

    @NonNull
    public Type getType() {
        return type;
    }

    @Nullable
    public Time getTime() {
        return time;
    }

    public enum Type {
        BEST("best", "Best"),
        HOT("hot", "Hot"),
        NEW("new", "New"),
        RANDOM("random", "Random"),
        RISING("rising", "Rising"),
        TOP("top", "Top"),
        CONTROVERSIAL("controversial", "Controversial"),
        RELEVANCE("relevance", "Relevance"),
        COMMENTS("comments", "Comments"),
        ACTIVITY("activity", "Activity"),
        CONFIDENCE("confidence", "Best"),
        OLD("old", "Old"),
        QA("qa", "QA"),
        LIVE("live", "Live");

        public final String value;
        public final String fullName;

        Type(String value, String fullName) {
            this.value = value;
            this.fullName = fullName;
        }
    }

    public enum Time {
        HOUR("hour", "Hour"),
        DAY("day", "Day"),
        WEEK("week", "Week"),
        MONTH("month", "Month"),
        YEAR("year", "Year"),
        ALL("all", "All Time");

        public final String value;
        public final String fullName;

        Time(String value, String fullName) {
            this.value = value;
            this.fullName = fullName;
        }
    }
}
