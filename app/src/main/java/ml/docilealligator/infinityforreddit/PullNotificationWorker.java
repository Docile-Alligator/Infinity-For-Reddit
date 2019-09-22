package ml.docilealligator.infinityforreddit;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import Account.Account;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;

public class PullNotificationWorker extends Worker {
    public static final String WORKER_TAG = "PNWT";

    private Context context;

    @Inject
    @Named("oauth_without_authenticator")
    Retrofit mOauthWithoutAuthenticatorRetrofit;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;

    @Inject
    SharedPreferences mSharedPreferences;

    public PullNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        this.context = context;
        ((Infinity) context.getApplicationContext()).getAppComponent().inject(this);
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            List<Account> accounts = mRedditDataRoomDatabase.accountDao().getAllAccounts();
            for(int accountIndex = 0; accountIndex < accounts.size(); accountIndex++) {
                Account account = accounts.get(accountIndex);

                String accountName = account.getUsername();

                Response<String> response = fetchMessages(account, 1);

                if(response != null && response.isSuccessful()) {
                    String responseBody = response.body();
                    ArrayList<Message> messages = FetchMessages.parseMessage(responseBody, context.getResources().getConfiguration().locale);

                    if(messages != null && !messages.isEmpty()) {
                        NotificationManagerCompat notificationManager = NotificationUtils.getNotificationManager(context);

                        NotificationCompat.Builder summaryBuilder = NotificationUtils.buildSummaryNotification(context,
                                notificationManager, accountName,
                                context.getString(R.string.notification_new_messages, messages.size()),
                                NotificationUtils.CHANNEL_ID_NEW_MESSAGES, NotificationUtils.CHANNEL_NEW_MESSAGES,
                                NotificationUtils.getAccountGroupName(accountName));

                        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

                        int messageSize = messages.size() >= 5 ? 5 : messages.size();
                        long currentTime = Calendar.getInstance().getTimeInMillis();
                        long notificationInterval = Long.parseLong(
                                mSharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"))
                                * 1000 * 60 * 60;
                        boolean hasValidMessage = false;

                        for(int messageIndex = messageSize - 1; messageIndex >= 0; messageIndex--) {
                            Message message = messages.get(messageIndex);
                            if(currentTime - message.getTimeUTC() > notificationInterval) {
                                continue;
                            }

                            hasValidMessage = true;

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
                                    NotificationUtils.CHANNEL_ID_NEW_MESSAGES,
                                    NotificationUtils.CHANNEL_NEW_MESSAGES,
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

                        if(hasValidMessage) {
                            inboxStyle.setBigContentTitle(context.getString(R.string.notification_new_messages, messages.size()))
                                    .setSummaryText(accountName);

                            summaryBuilder.setStyle(inboxStyle);

                            Intent summaryIntent = new Intent(context, ViewMessageActivity.class);
                            summaryIntent.putExtra(ViewMessageActivity.EXTRA_NEW_ACCOUNT_NAME, accountName);
                            PendingIntent summaryPendingIntent = PendingIntent.getActivity(context, accountIndex * 6 + 6, summaryIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                            summaryBuilder.setContentIntent(summaryPendingIntent);

                            notificationManager.notify(NotificationUtils.getSummaryIdUnreadMessage(accountIndex), summaryBuilder.build());
                        }
                    } else {
                        return Result.success();
                    }
                } else {
                    return Result.retry();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return Result.retry();
        } catch (JSONException e) {
            e.printStackTrace();
            return Result.failure();
        }
        return Result.success();
    }

    private Response<String> fetchMessages(Account account, int retryCount) throws IOException, JSONException {
        if(retryCount < 0) {
            return null;
        }

        Call<String> call = mOauthWithoutAuthenticatorRetrofit.create(RedditAPI.class)
                .getMessages(RedditUtils.getOAuthHeader(account.getAccessToken()),
                        FetchMessages.WHERE_UNREAD, null);
        Response<String> response = call.execute();

        if(response.isSuccessful()) {
            return response;
        } else {
            if(response.code() == 401) {
                String accessToken = refreshAccessToken(account);
                if(!accessToken.equals("")) {
                    return fetchMessages(account, retryCount - 1);
                }

                return null;
            } else {
                return null;
            }
        }
    }

    private String refreshAccessToken(Account account) {
        String refreshToken = account.getRefreshToken();

        RedditAPI api = mRetrofit.create(RedditAPI.class);

        Map<String, String> params = new HashMap<>();
        params.put(RedditUtils.GRANT_TYPE_KEY, RedditUtils.GRANT_TYPE_REFRESH_TOKEN);
        params.put(RedditUtils.REFRESH_TOKEN_KEY, refreshToken);

        Call<String> accessTokenCall = api.getAccessToken(RedditUtils.getHttpBasicAuthHeader(), params);
        try {
            Response response = accessTokenCall.execute();
            if(response.isSuccessful() && response.body() != null) {
                JSONObject jsonObject = new JSONObject(response.body().toString());
                String newAccessToken = jsonObject.getString(RedditUtils.ACCESS_TOKEN_KEY);
                mRedditDataRoomDatabase.accountDao().changeAccessToken(account.getUsername(), newAccessToken);
                return newAccessToken;
            }
            return "";
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }

        return "";
    }
}
