package ml.docilealligator.infinityforreddit;

import android.os.AsyncTask;

class LoadSubredditIconAsyncTask extends AsyncTask<Void, Void, Void> {
    interface LoadSubredditIconAsyncTaskListener {
        void loadIconSuccess(String iconImageUrl);
    }

    private SubredditDao subredditDao;
    private String subredditName;
    private String iconImageUrl;
    private LoadSubredditIconAsyncTaskListener loadSubredditIconAsyncTaskListener;

    LoadSubredditIconAsyncTask(SubredditDao subredditDao, String subredditName,
                               LoadSubredditIconAsyncTaskListener loadSubredditIconAsyncTaskListener) {
        this.subredditDao = subredditDao;
        this.subredditName = subredditName;
        this.loadSubredditIconAsyncTaskListener = loadSubredditIconAsyncTaskListener;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        if(subredditDao.getSubredditData(subredditName) != null) {
            iconImageUrl = subredditDao.getSubredditData(subredditName).getIconUrl();
        } else {
            iconImageUrl = "";
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        loadSubredditIconAsyncTaskListener.loadIconSuccess(iconImageUrl);
        /*Context context = contextWeakReference.get();
        CircleImageView circleImageView = circleImageViewWeakReference.get();

        if(context != null && circleImageView != null) {
            if(!iconImageUrl.equals("")) {
                Glide.with(context).load(iconImageUrl).into(circleImageView);
            } else {
                Glide.with(context).load(R.drawable.subreddit_default_icon).into(circleImageView);
            }
        }

        postData.setSubredditIconUrl(iconImageUrl);*/
    }
}
