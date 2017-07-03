package in.quantumtech.xmpp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import in.quantumtech.xmpp.utils.NetworkUtils;

/**
 * Created by dilli on 1/21/2016.
 */
public class ShutDownReceiver extends BroadcastReceiver {
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SHUTDOWN.equals(intent.getAction())) {
            NetworkUtils.saveNetworkUsageOnShutDown(context);
        }
    }
}