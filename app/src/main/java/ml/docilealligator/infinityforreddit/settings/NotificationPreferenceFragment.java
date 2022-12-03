package ml.docilealligator.infinityforreddit.settings;


import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.ListPreference;
import androidx.preference.SwitchPreference;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.PullNotificationWorker;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customviews.CustomFontPreferenceFragmentCompat;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationPreferenceFragment extends CustomFontPreferenceFragmentCompat {

    @Inject
    @Named("default")
    SharedPreferences sharedPreferences;
    @Inject
    @Named("internal")
    SharedPreferences mInternalSharedPreferences;
    private boolean enableNotification;
    private long notificationInterval;
    private WorkManager workManager;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.notification_preferences, rootKey);

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
                    TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();

                    PeriodicWorkRequest pullNotificationRequest =
                            new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                    notificationInterval, timeUnit)
                                    .setConstraints(constraints)
                                    .setInitialDelay(notificationInterval, timeUnit)
                                    .build();

                    workManager.enqueueUniquePeriodicWork(PullNotificationWorker.UNIQUE_WORKER_NAME,
                            ExistingPeriodicWorkPolicy.REPLACE, pullNotificationRequest);
                } else {
                    workManager.cancelUniqueWork(PullNotificationWorker.UNIQUE_WORKER_NAME);
                }
                return true;
            });
        }

        if (notificationIntervalListPreference != null) {
            notificationIntervalListPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                notificationInterval = Long.parseLong((String) newValue);

                if (enableNotification) {
                    TimeUnit timeUnit = (notificationInterval == 15 || notificationInterval == 30) ? TimeUnit.MINUTES : TimeUnit.HOURS;

                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build();

                    PeriodicWorkRequest pullNotificationRequest =
                            new PeriodicWorkRequest.Builder(PullNotificationWorker.class,
                                    notificationInterval, timeUnit)
                                    .setConstraints(constraints)
                                    .setInitialDelay(notificationInterval, timeUnit)
                                    .build();

                    workManager.enqueueUniquePeriodicWork(PullNotificationWorker.UNIQUE_WORKER_NAME,
                            ExistingPeriodicWorkPolicy.REPLACE, pullNotificationRequest);
                } else {
                    workManager.cancelUniqueWork(PullNotificationWorker.UNIQUE_WORKER_NAME);
                }

                return true;
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), result -> {
                mInternalSharedPreferences.edit().putBoolean(SharedPreferencesUtils.HAS_REQUESTED_NOTIFICATION_PERMISSION, true).apply();
                if (!result) {
                    activity.showSnackbar(R.string.denied_notification_permission, R.string.go_to_settings, new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
                            intent.setData(uri);
                            startActivity(intent);
                        }
                    });
                }
            });
            
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}
