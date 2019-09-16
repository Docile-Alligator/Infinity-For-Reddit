package ml.docilealligator.infinityforreddit;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.paging.DataSource;
import java.util.Locale;
import retrofit2.Retrofit;

class CommentDataSourceFactory extends DataSource.Factory {

  private final Retrofit retrofit;
  private final Locale locale;
  private final String username;
  private final MutableLiveData<CommentDataSource> commentDataSourceLiveData;
  private String sortType;
  private CommentDataSource commentDataSource;

  CommentDataSourceFactory(Retrofit retrofit, Locale locale, String username, String sortType) {
    this.retrofit = retrofit;
    this.locale = locale;
    this.username = username;
    this.sortType = sortType;
    commentDataSourceLiveData = new MutableLiveData<>();
  }

  @NonNull
  @Override
  public DataSource create() {
    commentDataSource = new CommentDataSource(retrofit, locale, username, sortType);
    commentDataSourceLiveData.postValue(commentDataSource);
    return commentDataSource;
  }

  public MutableLiveData<CommentDataSource> getCommentDataSourceLiveData() {
    return commentDataSourceLiveData;
  }

  CommentDataSource getCommentDataSource() {
    return commentDataSource;
  }

  void changeSortType(String sortType) {
    this.sortType = sortType;
  }
}
