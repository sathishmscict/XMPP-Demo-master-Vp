package in.quantumtech.xmpp.xmpp;

import android.content.Context;
import android.content.Intent;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.ShareUtil;

import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class MessagePacketListener implements StanzaListener {
	private Context context;
	
	public MessagePacketListener(Context context) {
		this.context = context;
	}
	
	@Override
	public void processPacket(Stanza packet) {
		Message msg = (Message)packet;
		String subject = msg.getSubject();
		Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, SmackHelper.parseBareAddress(msg.getFrom()));
		intent.putExtra(MessageService.EXTRA_DATA_NAME_STANZA_ID,msg.getStanzaId());
		intent.putExtra(MessageService.EXTRA_DATA_IS_INCOMING,1);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_CHAT_TYPE, DBConstants.TYPE_SINGLE_CHAT);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT,subject);
		intent.putExtra(MessageService.EXTRA_DATA_NAME_FORWARD,msg.getBody());

		switch (subject){

			case ShareUtil.VIDEO:
				try {
					JSONObject videoJson = new JSONObject(msg.getBody());
					JSONArray videoArray = videoJson.getJSONArray(ShareUtil.RESPONSE);
					for (int i = 0; i < videoArray.length(); i++) {
						intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_VIDEO);
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH,videoArray.getJSONObject(i).getString(ShareUtil.URL));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.video_message_body));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE,videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
						processPacketExtension(intent, msg);
						context.startService(intent);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}

				break;

			case ShareUtil.IMAGE_JPEG:
				try {
					JSONObject videoJson = new JSONObject(msg.getBody());
					JSONArray videoArray = videoJson.getJSONArray(ShareUtil.RESPONSE);
					for (int i = 0; i < videoArray.length(); i++) {
						intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_IMAGE);
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH,videoArray.getJSONObject(i).getString(ShareUtil.URL));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.image_message_body));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE,videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
						processPacketExtension(intent, msg);
						context.startService(intent);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case ShareUtil.IMAGE_PNG:
				try {
					JSONObject videoJson = new JSONObject(msg.getBody());
					JSONArray videoArray = videoJson.getJSONArray(ShareUtil.RESPONSE);
					for (int i = 0; i < videoArray.length(); i++) {
						intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_IMAGE);
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH,videoArray.getJSONObject(i).getString(ShareUtil.URL));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.image_message_body));
						intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE,videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
						processPacketExtension(intent, msg);
						context.startService(intent);
					}

				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			default:
				intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, msg.getBody());
				intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_PLAIN_TEXT);
				processPacketExtension(intent, msg);
				context.startService(intent);
				break;
		}
	}

	private void processPacketExtension(Intent intent, Message msg) {
		List<ExtensionElement> extensions = msg.getExtensions();
		if (extensions != null) {
			for (ExtensionElement extension : extensions) {
				if (extension instanceof UserLocation) {

					intent.putExtra(MessageService.EXTRA_DATA_NAME_LOCATION, (UserLocation) extension);
					intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_LOCATION);
				}
			}
		}
	}

}