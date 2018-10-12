package ml.docilealligator.infinityforreddit;

import android.app.Application;

public class Infinity extends Application {
    private NetworkComponent mNetworkComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        mNetworkComponent = DaggerNetworkComponent.create();
    }

    public NetworkComponent getmNetworkComponent() {
        return mNetworkComponent;
    }
}
