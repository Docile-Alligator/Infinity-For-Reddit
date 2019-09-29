package ml.docilealligator.infinityforreddit.Settings;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationPreferenceFragment extends PreferenceFragmentCompat {

    @Inject
    SharedPreferences sharedPreferences;
    private boolean enableNotification;
    private long notificationInterval;
    private WorkManager workManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);

        Activity activity = getActivity();

        if (activity != null) {
            workManager = WorkManager.getInstance(activity);

            ((Infinity) activity.getApplication()).getAppComponent().inject(this);

            SwitchPreference enableNotificationSwitchPreference = findPreference(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY);
            ListPreference notificationIntervalListPreference = findPreference(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY);

            enableNotification = sharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
            notificationInterval = Long.parseLong(sharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));

            if (enableNotification) {
                if (notificationIntervalListPreference != null) {
                    notificationIntervalListPreference.setVisible(true);
                }
            }

            if (enableNotificationSwitchPreference != null) {
                enableNotificationSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    enableNotification = ((Boolean) newValue);
                    if (notificationIntervalListPreference != null) {
                        notificationIntervalListPreference.setVisible(enableNotification);
                    }

                    if (enableNotification) {
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();

                        PeriodicWorkRequest pullNotificationRequest =
                                new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                        notificationInterval, TimeUnit.HOURS)
                                        .setConstraints(constraints)
                                        .setInitialDelay(notificationInterval, TimeUnit.HOURS)
                                        .build();

                        workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                ExistingPeriodicWorkPolicy.REPLACE, pullNotificationRequest);
                    } else {
                        workManager.cancelUniqueWork(PullNotificationWorker.WORKER_TAG);
                    }
                    return true;
                });
            }

            if (notificationIntervalListPreference != null) {
                notificationIntervalListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    notificationInterval = Long.parseLong((String) newValue);

                    if (enableNotification) {
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();

                        PeriodicWorkRequest pullNotificationRequest =
                                new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                        notificationInterval, TimeUnit.HOURS)
                                        .setConstraints(constraints)
                                        .setInitialDelay(notificationInterval, TimeUnit.HOURS)
                                        .build();

                        workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                ExistingPeriodicWorkPolicy.REPLACE, pullNotificationRequest);
                    } else {
                        workManager.cancelUniqueWork(PullNotificationWorker.WORKER_TAG);
                    }

                    return true;
                });
            }
        }
    }
}
