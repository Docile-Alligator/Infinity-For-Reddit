package ml.docilealligator.infinityforreddit.AsyncTask;

import android.os.AsyncTask;

import java.util.ArrayList;

import ml.docilealligator.infinityforreddit.FetchSubredditData;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditDao;
import ml.docilealligator.infinityforreddit.SubredditDatabase.SubredditData;
import retrofit2.Retrofit;

public class LoadSubredditIconAsyncTask extends AsyncTask<Void, Void, Void> {
    private RedditDataRoomDatabase redditDataRoomDatabase;
    private SubredditDao subredditDao;
    private String subredditName;
    private Retrofit retrofit;
    private String iconImageUrl;
    private boolean hasSubredditInDb;
    private LoadSubredditIconAsyncTaskListener loadSubredditIconAsyncTaskListener;
    public LoadSubredditIconAsyncTask(RedditDataRoomDatabase redditDataRoomDatabase, String subredditName, Retrofit retrofit,
                                      LoadSubredditIconAsyncTaskListener loadSubredditIconAsyncTaskListener) {
        this.redditDataRoomDatabase = redditDataRoomDatabase;
        this.subredditDao = redditDataRoomDatabase.subredditDao();
        this.subredditName = subredditName;
        this.retrofit = retrofit;
        this.loadSubredditIconAsyncTaskListener = loadSubredditIconAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        SubredditData subredditData = subredditDao.getSubredditData(subredditName);
        if (subredditData != null) {
            iconImageUrl = subredditDao.getSubredditData(subredditName).getIconUrl();
            hasSubredditInDb = true;
        } else {
            hasSubredditInDb = false;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!isCancelled()) {
            if (hasSubredditInDb) {
                loadSubredditIconAsyncTaskListener.loadIconSuccess(iconImageUrl);
            } else {
                FetchSubredditData.fetchSubredditData(retrofit, subredditName, new FetchSubredditData.FetchSubredditDataListener() {
                    @Override
                    public void onFetchSubredditDataSuccess(SubredditData subredditData, int nCurrentOnlineSubscribers) {
                        ArrayList<SubredditData> singleSubredditDataList = new ArrayList<>();
                        singleSubredditDataList.add(subredditData);
                        new InsertSubscribedThingsAsyncTask(redditDataRoomDatabase, null,
                                null, null, singleSubredditDataList,
                                () -> loadSubredditIconAsyncTaskListener.loadIconSuccess(subredditData.getIconUrl())).execute();
                    }

                    @Override
                    public void onFetchSubredditDataFail() {
                        loadSubredditIconAsyncTaskListener.loadIconSuccess(null);
                    }
                });
            }
        }
    }

    public interface LoadSubredditIconAsyncTaskListener {
        void loadIconSuccess(String iconImageUrl);
    }
}
