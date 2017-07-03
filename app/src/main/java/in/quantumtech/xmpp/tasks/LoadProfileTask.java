package in.quantumtech.xmpp.tasks;

import org.jivesoftware.smackx.vcardtemp.packet.VCard;

import android.content.Context;
import android.graphics.Bitmap;

import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.bitmapcache.BitmapUtils;
import in.quantumtech.xmpp.bitmapcache.ImageCache;
import in.quantumtech.xmpp.model.LoginUserProfile;
import in.quantumtech.xmpp.tasks.Response.Listener;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.xmpp.SmackHelper;

public class LoadProfileTask extends BaseAsyncTask<Void, Void, LoginUserProfile> {
	public LoadProfileTask(Listener<LoginUserProfile> listener, Context context) {
		super(listener, context);
	}
	
	@Override
	protected Response<LoginUserProfile> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				String user = PreferenceUtils.getUser(context);
				
				// first check cache file to find avatar, and if not existing, load vcard from server
				Bitmap avatar = ImageCache.getAvatarFromFile(context, user);
				if (avatar == null) {
					VCard vcard = SmackHelper.getInstance(context).loadVCard();
					if (vcard != null) {
						byte[] data = vcard.getAvatar();
						if (data != null) {
							avatar = BitmapUtils.decodeSampledBitmapFromByteArray(data, Integer.MAX_VALUE, Integer.MAX_VALUE, null);
						}
					}
					
					if (avatar != null) {
						ImageCache.addAvatarToFile(context, user, avatar);
					}
				}
				
				LoginUserProfile result = new LoginUserProfile();
				result.setAvatar(avatar);
				result.setNickname(PreferenceUtils.getNickname(context));
				
				return Response.success(result);
			} catch (SmackInvocationException e) {
				return Response.error(e);
			}
		}
		
		return null;
	}
}
