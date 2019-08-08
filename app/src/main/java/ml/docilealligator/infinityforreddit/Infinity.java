package ml.docilealligator.infinityforreddit;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.evernote.android.state.StateSaver;
import com.livefront.bridge.Bridge;
import com.livefront.bridge.SavedStateHandler;

public class Infinity extends Application {
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

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
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
