package ml.docilealligator.infinityforreddit;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.evernote.android.state.StateSaver;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.activities.LockScreenActivity;
import ml.docilealligator.infinityforreddit.broadcastreceivers.NetworkWifiStatusReceiver;
import ml.docilealligator.infinityforreddit.broadcastreceivers.WallpaperChangeReceiver;
import ml.docilealligator.infinityforreddit.events.ChangeAppLockEvent;
import ml.docilealligator.infinityforreddit.events.ChangeNetworkStatusEvent;
import ml.docilealligator.infinityforreddit.events.ToggleSecureModeEvent;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class Infinity extends Application implements LifecycleObserver {
    private AppComponent mAppComponent;
    private NetworkWifiStatusReceiver mNetworkWifiStatusReceiver;
    private boolean appLock;
    private long appLockTimeout;
    private boolean canStartLockScreenActivity = false;
    private boolean isSecureMode;
    private Typeface typeface;
    private Typeface titleTypeface;
    private Typeface contentTypeface;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("security")
    SharedPreferences mSecuritySharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

        mAppComponent.inject(this);

        appLock = mSecuritySharedPreferences.getBoolean(SharedPreferencesUtils.APP_LOCK, false);
        appLockTimeout = Long.parseLong(mSecuritySharedPreferences.getString(SharedPreferencesUtils.APP_LOCK_TIMEOUT, "600000"));
        isSecureMode = mSecuritySharedPreferences.getBoolean(SharedPreferencesUtils.SECURE_MODE, false);

        try {
            typeface = Typeface.createFromFile(getCacheDir() + "/opensans.ttf");
            titleTypeface = Typeface.createFromFile(getCacheDir() + "/opensans.ttf");
            contentTypeface = Typeface.createFromFile(getCacheDir() + "/opensans.ttf");
        } catch (RuntimeException e) {
            e.printStackTrace();
            Toast.makeText(this, "Some font files do not exist", Toast.LENGTH_SHORT).show();
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityPreCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
                if (activity instanceof BaseActivity) {
                    ((BaseActivity) activity).setCustomFont(typeface, titleTypeface, contentTypeface);
                }
            }

            @Override
            public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle bundle) {
                if (isSecureMode) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE);
                }
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {

            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (canStartLockScreenActivity && appLock
                        && System.currentTimeMillis() - mSecuritySharedPreferences.getLong(SharedPreferencesUtils.LAST_FOREGROUND_TIME, 0) >= appLockTimeout
                        && !(activity instanceof LockScreenActivity)) {
                    Intent intent = new Intent(activity, LockScreenActivity.class);
                    activity.startActivity(intent);
                }
                canStartLockScreenActivity = false;
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {

            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle bundle) {

            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {

            }
        });

        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        Bridge.initialize(getApplicationContext(), new SavedStateHandler() {
            @Override
            public void saveInstanceState(@NonNull Object target, @NonNull Bundle state) {
                StateSaver.saveInstanceState(target, state);
            }

            @Override
            public void restoreInstanceState(@NonNull Object target, @Nullable Bundle state) {
                StateSaver.restoreInstanceState(target, state);
            }
        });

        EventBus.builder().addIndex(new EventBusIndex()).installDefaultEventBus();

        EventBus.getDefault().register(this);

        mNetworkWifiStatusReceiver =
                new NetworkWifiStatusReceiver(() -> EventBus.getDefault().post(new ChangeNetworkStatusEvent(Utils.getConnectedNetwork(getApplicationContext()))));
        registerReceiver(mNetworkWifiStatusReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

        registerReceiver(new WallpaperChangeReceiver(mSharedPreferences), new IntentFilter(Intent.ACTION_WALLPAPER_CHANGED));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void appInForeground() {
        canStartLockScreenActivity = true;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void appInBackground() {
        if (appLock) {
            mSecuritySharedPreferences.edit().putLong(SharedPreferencesUtils.LAST_FOREGROUND_TIME, System.currentTimeMillis()).apply();
        }
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }

    @Subscribe
    public void onToggleSecureModeEvent(ToggleSecureModeEvent secureModeEvent) {
        isSecureMode = secureModeEvent.isSecureMode;
    }

    @Subscribe
    public void onChangeAppLockEvent(ChangeAppLockEvent changeAppLockEvent) {
        appLock = changeAppLockEvent.appLock;
        appLockTimeout = changeAppLockEvent.appLockTimeout;
    }
}
