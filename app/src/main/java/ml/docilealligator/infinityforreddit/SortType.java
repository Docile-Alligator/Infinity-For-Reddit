package ml.docilealligator.infinityforreddit;

public class SortType {

    private Type type;
    private Time time;

    public SortType(Type type) {
        this.type = type;
    }

    public SortType(Type type, Time time) {
        this.type = type;
        this.time = time;
    }

    public Type getType() {
        return type;
    }

    public Time getTime() {
        return time;
    }

    public enum Type {
        BEST("best"),
        HOT("hot"),
        NEW("new"),
        RANDOM("random"),
        RISING("rising"),
        TOP("top"),
        CONTROVERSIAL("controversial"),
        RELEVANCE("relevance"),
        COMMENTS("comments"),
        ACTIVITY("activity");

        public final String value;

        Type(String value) {
            this.value = value;
        }
    }

    public enum Time {
        HOUR("hour"),
        DAY("day"),
        WEEK("week"),
        MONTH("month"),
        YEAR("year"),
        ALL("all");

        public final String value;

        Time(String value) {
            this.value = value;
        }
    }
}
