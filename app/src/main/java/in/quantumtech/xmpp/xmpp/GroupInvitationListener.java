package in.quantumtech.xmpp.xmpp;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;

import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.tasks.InsertGroupMembersTask;
import in.quantumtech.xmpp.tasks.Response;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.PreferenceUtils;

/**
 * Created by ieglobe on 14/1/17.
 */

public class GroupInvitationListener implements InvitationListener {
    private Context context;

    public GroupInvitationListener(Context context) {
        this.context = context;
    }

    @Override
    public void invitationReceived(XMPPConnection conn, MultiUserChat room, String inviter, String reason, String password, Message message) {
        try {
            Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
            DiscussionHistory history = new DiscussionHistory();
            history.setMaxStanzas(-1);
            room.join(PreferenceUtils.getUser(context) + "," + PreferenceUtils.getNickname(context));
            String rawBody = message.getBody();
            String[] split = rawBody.split("\\^");
            String groupOwner = "\"" + split[1] + "\"";
            if (PreferenceUtils.getNickname(context).equalsIgnoreCase(groupOwner)) {
                groupOwner = "\"You\" ";
            }
            String body = split[0] + " " + groupOwner;
            String subject = message.getSubject();
            String gjid = message.getFrom();
            String groupName = message.getThread();
            new InsertGroupMembersTask(userInserted,context,room).execute(gjid,groupName);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_GROUP_NAME, groupName);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, gjid);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_GJID, gjid);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_CHAT_TYPE, DBConstants.TYPE_GROUP_CHAT);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, body);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_TYPE, ChatMessageTableHelper.TYPE_GROUP_ACTIVITY);
            intent.putExtra(MessageService.EXTRA_DATA_NAME_SUBJECT, subject);
            context.startService(intent);
        } catch (XMPPException e) {
            e.printStackTrace();
            Log.e("abc", "join room failed!");
            
        } catch (SmackException.NotConnectedException | SmackException.NoResponseException e) {
            e.printStackTrace();
        }
    }

    private Response.Listener<Boolean> userInserted = new Response.Listener<Boolean>() {
        @Override
        public void onResponse(Boolean result) {

        }

        @Override
        public void onErrorResponse(Exception exception) {

        }
    };
}
