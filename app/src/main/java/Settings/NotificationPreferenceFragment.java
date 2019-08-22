package Settings;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreference;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
import androidx.work.WorkManager;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;
import java.util.concurrent.ExecutionException;
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

    private boolean enableNotification;
    private long notificationInterval;

    private WorkManager workManager;

    @Inject
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);

        Activity activity = getActivity();

        if(activity != null) {
            workManager = WorkManager.getInstance(activity);

            ((Infinity) activity.getApplication()).getAppComponent().inject(this);

            SwitchPreference enableNotificationSwitchPreference = findPreference(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY);
            ListPreference notificationIntervalListPreference = findPreference(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY);

            enableNotification = sharedPreferences.getBoolean(SharedPreferencesUtils.ENABLE_NOTIFICATION_KEY, true);
            notificationInterval = Long.parseLong(sharedPreferences.getString(SharedPreferencesUtils.NOTIFICATION_INTERVAL_KEY, "1"));

            if(enableNotification) {
                if (notificationIntervalListPreference != null) {
                    notificationIntervalListPreference.setVisible(true);
                }
            }

            if(enableNotificationSwitchPreference != null) {
                enableNotificationSwitchPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                    enableNotification = ((Boolean) newValue);
                    if(notificationIntervalListPreference != null) {
                        notificationIntervalListPreference.setVisible(enableNotification);
                    }

                    if(enableNotification) {
                        Constraints constraints = new Constraints.Builder()
                                .setRequiredNetworkType(NetworkType.CONNECTED)
                                .build();

                        PeriodicWorkRequest pullNotificationRequest =
                                new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                        notificationInterval, TimeUnit.HOURS)
                                        .setConstraints(constraints)
                                        .build();

                        workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
                    } else {
                        ListenableFuture<List<WorkInfo>> workInfo = workManager.getWorkInfosByTag(PullNotificationWorker.WORKER_TAG);
                        try {
                            List<WorkInfo> list = workInfo.get();
                            if(list != null && list.size() != 0) {
                                workManager.cancelAllWorkByTag(PullNotificationWorker.WORKER_TAG);
                            }
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                });
            }

            if(notificationIntervalListPreference != null) {
                notificationIntervalListPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        notificationInterval = (Long) newValue;

                        if(enableNotification) {
                            Constraints constraints = new Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build();

                            PeriodicWorkRequest pullNotificationRequest =
                                    new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                            notificationInterval, TimeUnit.HOURS)
                                            .setConstraints(constraints)
                                            .build();

                            workManager.enqueueUniquePeriodicWork(PullNotificationWorker.WORKER_TAG,
                                    ExistingPeriodicWorkPolicy.KEEP, pullNotificationRequest);
                        } else {
                            ListenableFuture<List<WorkInfo>> workInfo = workManager.getWorkInfosByTag(PullNotificationWorker.WORKER_TAG);
                            try {
                                List<WorkInfo> list = workInfo.get();
                                if(list != null && list.size() != 0) {
                                    workManager.cancelAllWorkByTag(PullNotificationWorker.WORKER_TAG);
                                }
                            } catch (InterruptedException | ExecutionException e) {
                                e.printStackTrace();
                            }
                        }

                        return true;
                    }
                });
            }
        }
    }
}
