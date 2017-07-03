package in.quantumtech.xmpp.tasks;

import java.lang.ref.WeakReference;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.databases.ChatContract.ContactRequestTable;
import in.quantumtech.xmpp.model.UserProfile;
import in.quantumtech.xmpp.utils.AppLog;
import in.quantumtech.xmpp.utils.ProviderUtils;
import in.quantumtech.xmpp.xmpp.SmackHelper;
import in.quantumtech.xmpp.xmpp.SmackVCardHelper;

public class AcceptContactRequestTask extends BaseAsyncTask<Void, Void, UserProfile> {
	private WeakReference<Uri> uriWrapper;
	
	public AcceptContactRequestTask(Response.Listener<UserProfile> listener, Context context, Uri uri) {
		super(listener, context);
		uriWrapper = new WeakReference<Uri>(uri);
	}
	
	@Override
	protected Response<UserProfile> doInBackground(Void... params) {
		Uri requestUri = uriWrapper.get();
		Context context = getContext();
		if (requestUri != null && context != null) {
			Cursor cursor = context.getContentResolver().query(requestUri, 
					new String[]{ContactRequestTable.COLUMN_NAME_NICKNAME, ContactRequestTable.COLUMN_NAME_JID},
					null, null, null);
			try {
				if (cursor.moveToFirst()) {
					String jid = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_JID));
					String nickname = cursor.getString(cursor.getColumnIndex(ContactRequestTable.COLUMN_NAME_NICKNAME));
					
					SmackHelper smackHelper = SmackHelper.getInstance(context);
					// 1. grant subscription to initiator, and request subscription afterwards
					smackHelper.approveSubscription(jid, nickname, true);
					
					// 2. load VCard
					VCard vCard = smackHelper.loadVCard(jid);
					
					// 3. save new contact into db
					ProviderUtils.addNewContact(context, jid, nickname, vCard.getField(SmackVCardHelper.FIELD_STATUS));
					
					return Response.success(new UserProfile(jid, vCard, UserProfile.TYPE_CONTACT));
				}
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("accept contact request error %s", e.toString()), e);
				
				return Response.error(e);
			} finally {
				cursor.close();
			} 
		}
		
		return null;
	}
}