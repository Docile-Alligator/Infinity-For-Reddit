package ml.docilealligator.infinityforreddit;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Response;

public class Okhttp3DebugInterceptor implements Interceptor {
    private Application context;

    public Okhttp3DebugInterceptor(Application context) {
        this.context = context;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response response = chain.proceed(chain.request());
        if (!response.isSuccessful()) {
            String message = "No body";
            if (response.body() != null) {
                message = response.body().string();
            }
            NotificationManagerCompat notificationManager = NotificationUtils.getNotificationManager(context);
            NotificationCompat.Builder builder = NotificationUtils.buildNotification(notificationManager,
                    context, "debug", message, Integer.toString(response.code()),
                    NotificationUtils.CHANNEL_ID_NEW_MESSAGES,
                    NotificationUtils.CHANNEL_NEW_MESSAGES,
                    NotificationUtils.getAccountGroupName("Debug"));
            notificationManager.notify(9765, builder.build());
        }
        return response;
    }
}
