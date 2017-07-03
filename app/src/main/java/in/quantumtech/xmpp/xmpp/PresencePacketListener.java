package in.quantumtech.xmpp.xmpp;

import android.content.Context;
import android.content.Intent;

import in.quantumtech.xmpp.service.MessageService;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;

public class PresencePacketListener implements StanzaListener {
	public static final String EXTRA_DATA_NAME_TYPE = "com.mstr.letschat.Type";
	public static final String EXTRA_DATA_NAME_STATUS = "com.mstr.letschat.Status";
	
	private Context context;
	
	public PresencePacketListener(Context context) {
		this.context = context;
	}

	@Override
	public void processPacket(Stanza packet) throws SmackException.NotConnectedException {
		Presence presence = (Presence) packet;
		Presence.Type presenceType = presence.getType();

		Intent intent = new Intent(MessageService.ACTION_PRESENCE_RECEIVED, null, context, MessageService.class);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, SmackHelper.parseBareAddress(presence.getFrom()));
		intent.putExtra(EXTRA_DATA_NAME_TYPE, presenceType.ordinal());
		String status = presence.getStatus();
		if (status != null) {
			intent.putExtra(EXTRA_DATA_NAME_STATUS, presence.getStatus());
		}
		context.startService(intent);
	}
}