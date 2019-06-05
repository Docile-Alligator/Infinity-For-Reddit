package ml.docilealligator.infinityforreddit;

import android.app.Application;

public class Infinity extends Application {
    private AppComponent mAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
    }

    public AppComponent getmAppComponent() {
        return mAppComponent;
    }
}
