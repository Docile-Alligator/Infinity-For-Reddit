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
        CONFIDENCE("confidence", "Confidence"),
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
