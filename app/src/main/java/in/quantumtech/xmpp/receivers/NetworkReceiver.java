package in.quantumtech.xmpp.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.utils.NetworkUtils;

public class NetworkReceiver extends BroadcastReceiver {
	public static final String EXTRA_DATA_NAME_NETWORK_CONNECTED = "com.mstr.letschat.NetworkConnected";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)) {
			Intent serviceIntent = new Intent(MessageService.ACTION_NETWORK_STATUS, null, context, MessageService.class);
			serviceIntent.putExtra(EXTRA_DATA_NAME_NETWORK_CONNECTED, NetworkUtils.isNetworkConnected(context));
			context.startService(serviceIntent);
		}
	}
}