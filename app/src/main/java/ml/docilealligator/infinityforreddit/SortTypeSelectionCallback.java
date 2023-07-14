package ml.ino6962.postinfinityforreddit;

public interface SortTypeSelectionCallback {
    default void sortTypeSelected(SortType sortType){}

    default void sortTypeSelected(String sortType){}

    default void searchUserAndSubredditSortTypeSelected(SortType sortType, int fragmentPosition){}
}
