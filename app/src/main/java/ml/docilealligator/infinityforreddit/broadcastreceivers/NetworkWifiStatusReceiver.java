package ml.docilealligator.infinityforreddit.broadcastreceivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetworkWifiStatusReceiver extends BroadcastReceiver {
    private NetworkWifiStatusReceiverListener networkWifiStatusReceiverListener;

    public interface NetworkWifiStatusReceiverListener {
        void networkStatusChange();
    }

    public NetworkWifiStatusReceiver(NetworkWifiStatusReceiverListener listener) {
        networkWifiStatusReceiverListener = listener;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        networkWifiStatusReceiverListener.networkStatusChange();
    }
}
