package in.quantumtech.xmpp.xmpp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.utils.ShareUtil;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Stanza;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by admin on 28-Nov-16.
 *
 */

public class GroupChatMsgListener implements StanzaListener {
    private static final String TAG = GroupChatMsgListener.class.getSimpleName();
    private Context context;

    public GroupChatMsgListener(Context context) {
        this.context = context;
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

    @Override
    public void processPacket(final Stanza packet) throws SmackException.NotConnectedException {

        final Message msg = (Message) packet;
        Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
        //TODO: gjid in group message.
        String body = msg.getBody();
        String subject = msg.getSubject();
        if (subject == null) {
            subject = DBConstants.NORMAL;
        }
        Log.d("from", msg.getFrom());
        String groupName = msg.getThread();
        String userId = null;
        String stanzaId = msg.getStanzaId();
        String gjid = msg.getFrom();
        if (gjid != null) {
            int i = gjid.indexOf("/");
            gjid = gjid.substring(0, i);
        }
        String from = msg.getFrom();

        if (from != null) {
            int i = from.indexOf("/");
            from = from.substring(i + 1);
            String[] split = from.split(",");
            userId = split[0];
            from = split[1];
        }
        intent.putExtra(MessageService.EXTRA_DATA_NAME_GROUP_NAME, groupName);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_GROUP_USER_NICKNAME, from);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, gjid);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_GJID, gjid);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_STANZA_ID, stanzaId);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_CHAT_TYPE, DBConstants.TYPE_GROUP_CHAT);
        intent.putExtra(MessageService.EXTRA_DATA_NAME_FORWARD, msg.getBody());
        if (DBConstants.TYPE_GROUP_CREATED.equalsIgnoreCase(subject)) {
            // this will display first message in group to show group name in conversation list after creation.
            if (PreferenceUtils.getUser(context).equalsIgnoreCase(userId)) {
                body = "\"You\" " + body;
                intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, body);
                intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_GROUP_ACTIVITY);
                intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT, subject);
                processPacketExtension(intent, msg);
                context.startService(intent);
            }

        } else if (DBConstants.TYPE_GROUP_RENAME.equalsIgnoreCase(subject)) {
            String[] split = body.split("\\^");
            String first;
            if (split[0].equalsIgnoreCase(PreferenceUtils.getNickname(context))) {
                first = "\"You\" ";
            } else {
                first = "\"" + split[0] + "\"";
            }
            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, first + " " + split[1]);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_GROUP_ACTIVITY);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT, subject);
            processPacketExtension(intent, msg);
            context.startService(intent);
        } else if (DBConstants.TYPE_GROUP_INVITE.equalsIgnoreCase(subject)) {
            String[] split = body.split("\\^");
            String finalBody;
            if (PreferenceUtils.getNickname(context).equalsIgnoreCase(split[2])) {
                finalBody = "\"" + split[0] + "\" " + split[1] + " \"You\"";
            } else if (PreferenceUtils.getNickname(context).equalsIgnoreCase(split[0])) {
                finalBody = "\"You\" " + split[1] + " \"" + split[2] + "\"";
            } else {
                finalBody = "\"" + split[0] + "\" " + split[1] + " \"" + split[2] + "\"";
            }
            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, finalBody);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_GROUP_ACTIVITY);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT, subject);
            context.startService(intent);
        } else if (!PreferenceUtils.getNickname(context).equalsIgnoreCase(from)) {
            //This will check duplicate message in group chat.

            switch (subject) {
                case ShareUtil.VIDEO:
                    try {
                        JSONObject videoJson = new JSONObject(msg.getBody());
                        JSONArray videoArray = videoJson.getJSONArray(ShareUtil.RESPONSE);
                        for (int i = 0; i < videoArray.length(); i++) {
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_INCOMING_VIDEO);
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH, videoArray.getJSONObject(i).getString(ShareUtil.URL));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.video_message_body));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE, videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
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
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH, videoArray.getJSONObject(i).getString(ShareUtil.URL));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.image_message_body));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE, videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
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
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_PATH, videoArray.getJSONObject(i).getString(ShareUtil.URL));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_THUMB_URL, videoArray.getJSONObject(i).getString(ShareUtil.THUMB));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, context.getString(R.string.image_message_body));
                            intent.putExtra(MessageService.EXTRA_DATA_NAME_FILE_SIZE, videoArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
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
                    intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT, subject);
                    processPacketExtension(intent, msg);
                    context.startService(intent);
                    break;
            }
        }

    }
}
