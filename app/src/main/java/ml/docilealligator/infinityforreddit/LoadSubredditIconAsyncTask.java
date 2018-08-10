package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.os.AsyncTask;

import com.bumptech.glide.Glide;

import java.lang.ref.WeakReference;

import de.hdodenhof.circleimageview.CircleImageView;

class LoadSubredditIconAsyncTask extends AsyncTask<Void, Void, Void> {
    private final WeakReference<Context> contextWeakReference;
    private final WeakReference<CircleImageView> circleImageViewWeakReference;

    private SubredditDao subredditDao;
    private String subredditName;
    private String iconImageUrl;
    private BestPostData bestPostData;

    LoadSubredditIconAsyncTask(Context context, CircleImageView iconImageView,
                               SubredditDao subredditDao, String subredditName,
                               BestPostData bestPostData) {
        contextWeakReference = new WeakReference<>(context);
        circleImageViewWeakReference = new WeakReference<>(iconImageView);
        this.subredditDao = subredditDao;
        this.subredditName = subredditName;
        this.bestPostData = bestPostData;
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
        Context context = contextWeakReference.get();
        CircleImageView circleImageView = circleImageViewWeakReference.get();

        if(context != null && circleImageView != null) {
            if(!iconImageUrl.equals("")) {
                Glide.with(context).load(iconImageUrl).into(circleImageView);
            } else {
                Glide.with(context).load(R.drawable.subreddit_default_icon).into(circleImageView);
            }
        }

        bestPostData.setSubredditIconUrl(iconImageUrl);
    }
}
