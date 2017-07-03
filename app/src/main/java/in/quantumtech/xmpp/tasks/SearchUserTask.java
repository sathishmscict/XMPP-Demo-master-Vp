package in.quantumtech.xmpp.tasks;

import android.content.Context;
import android.database.Cursor;

import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.databases.ChatContract.ContactTable;
import in.quantumtech.xmpp.model.UserProfile;
import in.quantumtech.xmpp.utils.AppLog;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.xmpp.SmackHelper;

public class SearchUserTask extends BaseAsyncTask<Void, Void, UserProfile> {
	private String username;
	
	public SearchUserTask(Response.Listener<UserProfile> listener, Context context, String username) {
		super(listener,context);
		
		this.username = username;
	}

	@Override
	protected Response<UserProfile> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				UserProfile user = SmackHelper.getInstance(context).search(username);
				if (user != null) {
					if (user.getUserName().equals(PreferenceUtils.getUser(context))) {
						user.setType(UserProfile.TYPE_MYSELF);
					} else {
						Cursor c = context.getContentResolver().query(ContactTable.CONTENT_URI, new String[]{ContactTable._ID},
								ContactTable.COLUMN_NAME_JID + " = ?", new String[] {user.getJid()}, null);
						if (c.moveToFirst()) {
							user.setType(UserProfile.TYPE_CONTACT);
						} else {
							user.setType(UserProfile.TYPE_NOT_CONTACT);
						}
					}
				}
				
				return Response.success(user);
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("search user error %s", e.toString()), e);
				
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}