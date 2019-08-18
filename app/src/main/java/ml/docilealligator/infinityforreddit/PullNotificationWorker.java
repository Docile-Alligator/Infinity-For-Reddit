package ml.docilealligator.infinityforreddit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.io.IOException;
import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import Account.Account;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PullNotificationWorker extends Worker {
    static final String WORKER_TAG = "PNWT";

    private Context context;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    RedditDataRoomDatabase redditDataRoomDatabase;

    public PullNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.i("workmanager", "do");
        try {
            Log.i("workmanager", "before response");
            Account currentAccount = redditDataRoomDatabase.accountDao().getCurrentAccount();
            Response<String> response = mOauthRetrofit.create(RedditAPI.class).getMessages(
                    RedditUtils.getOAuthHeader(currentAccount.getAccessToken()),
                    FetchMessages.WHERE_COMMENTS).execute();
            Log.i("workmanager", "has response");
            if(response.isSuccessful()) {
                String responseBody = response.body();
                ArrayList<Message> messages = FetchMessages.parseMessage(responseBody, context.getResources().getConfiguration().locale);

                if(messages != null) {
                    NotificationManagerCompat notificationManager = NotificationUtils.getNotificationManager(context);

                    NotificationCompat.Builder summaryBuilder = NotificationUtils.buildSummaryNotification(context,
                            notificationManager, currentAccount.getUsername(), messages.size() + " new comment replies",
                            NotificationUtils.CHANNEL_ID_NEW_COMMENTS, NotificationUtils.CHANNEL_NEW_COMMENTS,
                            NotificationUtils.GROUP_NEW_COMMENTS);

                    NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                    int messageSize = messages.size() >= 5 ? 5 : messages.size();

                    for(int i = messageSize - 1; i >= 0; i--) {
                        Message message = messages.get(i);

                        inboxStyle.addLine(message.getAuthor() + " " + message.getBody());

                        String kind = message.getKind();
                        String title;
                        String summary;
                        if(kind.equals(Message.TYPE_COMMENT)) {
                            title = message.getAuthor();
                            summary = context.getString(R.string.notification_summary_comment);
                        } else {
                            title = message.getTitle();
                            if(kind.equals(Message.TYPE_ACCOUNT)) {
                                summary = context.getString(R.string.notification_summary_account);
                            } else if(kind.equals(Message.TYPE_LINK)) {
                                summary = context.getString(R.string.notification_summary_post);
                            } else if(kind.equals(Message.TYPE_MESSAGE)) {
                                summary = context.getString(R.string.notification_summary_message);
                            } else if(kind.equals(Message.TYPE_SUBREDDIT)) {
                                summary = context.getString(R.string.notification_summary_subreddit);
                            } else {
                                summary = context.getString(R.string.notification_summary_award);
                            }
                        }

                        NotificationCompat.Builder builder = NotificationUtils.buildNotification(notificationManager,
                                context, title, message.getBody(), summary,
                                NotificationUtils.CHANNEL_ID_NEW_COMMENTS,
                                NotificationUtils.CHANNEL_NEW_COMMENTS, NotificationUtils.GROUP_NEW_COMMENTS);

                        if(kind.equals(Message.TYPE_COMMENT)) {
                            Intent intent = new Intent(context, LinkResolverActivity.class);
                            Uri uri = LinkResolverActivity.getRedditUriByPath(message.getContext());
                            intent.setData(uri);
                            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                            builder.setContentIntent(pendingIntent);
                            notificationManager.notify(NotificationUtils.BASE_ID_COMMENT + i, builder.build());
                        } else if(kind.equals(Message.TYPE_ACCOUNT)) {
                            notificationManager.notify(NotificationUtils.BASE_ID_ACCOUNT + i, builder.build());
                        } else if(kind.equals(Message.TYPE_LINK)) {
                            notificationManager.notify(NotificationUtils.BASE_ID_POST + i, builder.build());
                        } else if(kind.equals(Message.TYPE_MESSAGE)) {
                            notificationManager.notify(NotificationUtils.BASE_ID_MESSAGE + i, builder.build());
                        } else if(kind.equals(Message.TYPE_SUBREDDIT)) {
                            notificationManager.notify(NotificationUtils.BASE_ID_SUBREDDIT + i, builder.build());
                        } else {
                            notificationManager.notify(NotificationUtils.BASE_ID_AWARD + i, builder.build());
                        }
                    }

                    inboxStyle.setBigContentTitle(messages.size() + " New Messages")
                            .setSummaryText(currentAccount.getUsername());

                    summaryBuilder.setStyle(inboxStyle);

                    notificationManager.notify(NotificationUtils.SUMMARY_ID_NEW_COMMENTS, summaryBuilder.build());

                    Log.i("workmanager", "message size " + messages.size());
                } else {
                    Log.i("workmanager", "retry1");
                    return Result.retry();
                }
            } else {
                Log.i("workmanager", "retry2 " + response.code());
                return Result.retry();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("workmanager", "retry3");
            return Result.retry();
        }

        Log.i("workmanager", "success");
        return Result.success();
    }
}
