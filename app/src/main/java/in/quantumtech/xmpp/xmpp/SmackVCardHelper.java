package in.quantumtech.xmpp.xmpp;

import android.content.Context;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.SmackInvocationException;

import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smackx.vcardtemp.VCardManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;

public class SmackVCardHelper {
	public static final String FIELD_STATUS = "status";
	
	private Context context;
	private XMPPTCPConnection con;
	
	public SmackVCardHelper(Context context, XMPPTCPConnection con) {
		this.context = context;
		this.con = con;
	}
	
	public void save(String nickname, byte[] avatar) throws SmackInvocationException {
		VCard vCard = new VCard();
		try {
			vCard.setNickName(nickname);
			if (avatar != null) {
				vCard.setAvatar(avatar);
			}
			vCard.setField(FIELD_STATUS, context.getString(R.string.default_status));
			VCardManager.getInstanceFor(con).saveVCard(vCard);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public void saveStatus(String status) throws SmackInvocationException {
		VCard vCard = loadVCard();
		vCard.setField(FIELD_STATUS, status);
		
		try {
			VCardManager.getInstanceFor(con).saveVCard(vCard);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public String loadStatus() throws SmackInvocationException {
		return loadVCard().getField(FIELD_STATUS);
	}
	
	public VCard loadVCard(String jid) throws SmackInvocationException {
		try {
			return VCardManager.getInstanceFor(con).loadVCard(jid);
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
	
	public VCard loadVCard() throws SmackInvocationException {
		try {
			return VCardManager.getInstanceFor(con).loadVCard();
		} catch (Exception e) {
			throw new SmackInvocationException(e);
		}
	}
 }