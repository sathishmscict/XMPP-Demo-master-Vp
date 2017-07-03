package in.quantumtech.xmpp.tasks;

import android.content.ContentValues;
import android.content.Context;

import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.xmpp.SmackHelper;
import in.quantumtech.xmpp.xmpp.UserLocation;

import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by dilli on 11/27/2015.
 */
public class SendLocationTask extends SendMessageTask {
    private UserLocation location;
    protected ExtensionElement packetExtension;
    private Message message;
    private String subject;
    public SendLocationTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, UserLocation location, String subject) {
        super(listener, context, to, nickname, location.getName());
        message = new Message(to, Message.Type.chat);
        this.location = location;
        packetExtension = location;
        this.subject = subject;
    }

    @Override
    protected ContentValues newMessage(long timeMillis) {
        return ChatMessageTableHelper.newLocationMessage(to, body, timeMillis, location, true,"");
    }

    @Override
    protected void doSend(Context context) throws SmackInvocationException {
        SmackHelper.getInstance(context).sendChatMessage(message, body, packetExtension,subject);
    }
}