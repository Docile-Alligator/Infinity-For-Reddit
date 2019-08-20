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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import Account.Account;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PullNotificationWorker extends Worker {
    static final String WORKER_TAG = "PNWT";

    private Context context;

    @Inject
    @Named("oauth_without_authenticator")
    Retrofit mOauthWithoutAuthenticatorRetrofit;

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

            List<Account> accounts = redditDataRoomDatabase.accountDao().getAllAccounts();
            for(int accountIndex = 0; accountIndex < accounts.size(); accountIndex++) {
                Account account = accounts.get(accountIndex);
                String accountName = account.getUsername();

                Response<String> response = fetchMessages(account);

                if(response != null && response.isSuccessful()) {
                    Log.i("workmanager", "has response");
                    String responseBody = response.body();
                    ArrayList<Message> messages = FetchMessages.parseMessage(responseBody, context.getResources().getConfiguration().locale);

                    if(messages != null && !messages.isEmpty()) {
                        NotificationManagerCompat notificationManager = NotificationUtils.getNotificationManager(context);

                        NotificationCompat.Builder summaryBuilder = NotificationUtils.buildSummaryNotification(context,
                                notificationManager, accountName,
                                context.getString(R.string.notification_new_messages, messages.size()),
                                NotificationUtils.CHANNEL_ID_NEW_COMMENTS, NotificationUtils.CHANNEL_NEW_COMMENTS,
                                NotificationUtils.getAccountGroupName(accountName));

                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                        int messageSize = messages.size() >= 5 ? 5 : messages.size();

                        for(int messageIndex = messageSize - 1; messageIndex >= 0; messageIndex--) {
                            Message message = messages.get(messageIndex);

                            inboxStyle.addLine(message.getAuthor() + " " + message.getBody());

                            String kind = message.getKind();
                            String title;
                            String summary;
                            if(kind.equals(Message.TYPE_COMMENT) || kind.equals(Message.TYPE_LINK)) {
                                title = message.getAuthor();
                                summary = message.getSubject().substring(0, 1).toUpperCase() + message.getSubject().substring(1);
                            } else {
                                title = message.getTitle() == null || message.getTitle().equals("") ? message.getSubject() : message.getTitle();
                                if(kind.equals(Message.TYPE_ACCOUNT)) {
                                    summary = context.getString(R.string.notification_summary_account);
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
                                    NotificationUtils.CHANNEL_NEW_COMMENTS,
                                    NotificationUtils.getAccountGroupName(accountName));

                            if(kind.equals(Message.TYPE_COMMENT)) {
                                Intent intent = new Intent(context, LinkResolverActivity.class);
                                Uri uri = LinkResolverActivity.getRedditUriByPath(message.getContext());
                                intent.setData(uri);
                                intent.putExtra(LinkResolverActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                intent.putExtra(LinkResolverActivity.EXTRA_MESSAGE_FULLNAME, message.getFullname());
                                PendingIntent pendingIntent = PendingIntent.getActivity(context, accountIndex * 6, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(pendingIntent);
                            } else if(kind.equals(Message.TYPE_ACCOUNT)) {
                                Intent intent = new Intent(context, ViewMessageActivity.class);
                                intent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(summaryPendingIntent);
                            } else if(kind.equals(Message.TYPE_LINK)) {
                                Intent intent = new Intent(context, LinkResolverActivity.class);
                                Uri uri = LinkResolverActivity.getRedditUriByPath(message.getContext());
                                intent.setData(uri);
                                intent.putExtra(LinkResolverActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                intent.putExtra(LinkResolverActivity.EXTRA_MESSAGE_FULLNAME, message.getFullname());
                                PendingIntent pendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 2, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(pendingIntent);
                            } else if(kind.equals(Message.TYPE_MESSAGE)) {
                                Intent intent = new Intent(context, ViewMessageActivity.class);
                                intent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 3, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(summaryPendingIntent);
                            } else if(kind.equals(Message.TYPE_SUBREDDIT)) {
                                Intent intent = new Intent(context, ViewMessageActivity.class);
                                intent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 4, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(summaryPendingIntent);
                            } else {
                                Intent intent = new Intent(context, ViewMessageActivity.class);
                                intent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                                PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 5, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(summaryPendingIntent);
                            }
                            notificationManager.notify(NotificationUtils.getNotificationIdUnreadMessage(accountIndex, messageIndex), builder.build());
                        }

                        inboxStyle.setBigContentTitle(context.getString(R.string.notification_new_messages, messages.size()))
                                .setSummaryText(accountName);

                        summaryBuilder.setStyle(inboxStyle);

                        Intent summaryIntent = new Intent(context, ViewMessageActivity.class);
                        summaryIntent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                        PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 6, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                        summaryBuilder.setContentIntent(summaryPendingIntent);

                        notificationManager.notify(NotificationUtils.getSummaryIdUnreadMessage(accountIndex), summaryBuilder.build());

                        Log.i("workmanager", "message size " + messages.size());
                    } else {
                        Log.i("workmanager", "retry1");
                        return Result.retry();
                    }
                } else {
                    if(response != null) {
                        Log.i("workmanager", "retry2 " + response.code());
                    }
                    return Result.retry();
                }
            }
        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.i("workmanager", "retry3");
            return Result.retry();
        }

        Log.i("workmanager", "success");
        return Result.success();
    }

    private Response<String> fetchMessages(Account account) throws IOException, JSONException {
        Response<String> response = mOauthWithoutAuthenticatorRetrofit.create(RedditAPI.class)
                .getMessages(RedditUtils.getOAuthHeader(account.getAccessToken()),
                        FetchMessages.WHERE_UNREAD, null).execute();

        if(response.isSuccessful()) {
            return response;
        } else {
            if(response.code() == 401) {
                String refreshToken = account.getRefreshToken();

                Map<String, String> params = new HashMap<>();
                params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
                params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

                Response accessTokenResponse = mOauthWithoutAuthenticatorRetrofit.create(RedditAPI.class)
                        .getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params).execute();
                if(accessTokenResponse.isSuccessful() && accessTokenResponse.body() != null) {
                    JSONObject jsonObject = new JSONObject((String) accessTokenResponse.body());
                    String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);
                    account.setAccessToken(newAccessToken);
                    redditDataRoomDatabase.accountDao().changeAccessToken(account.getUsername(), newAccessToken);

                    return fetchMessages(account);
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
