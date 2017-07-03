package in.quantumtech.xmpp.xmpp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.databases.ContactTableHelper;
import in.quantumtech.xmpp.databases.GroupTableHelper;
import in.quantumtech.xmpp.listeners.OnTypingListener;
import in.quantumtech.xmpp.model.FailedMessageModel;
import in.quantumtech.xmpp.model.SelectUserModel;
import in.quantumtech.xmpp.model.SubscribeInfo;
import in.quantumtech.xmpp.model.UserProfile;
import in.quantumtech.xmpp.service.MessageService;
import in.quantumtech.xmpp.tasks.JoinGroupsTask;
import in.quantumtech.xmpp.tasks.Response;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.EventUtils;
import in.quantumtech.xmpp.utils.NetworkUtils;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.utils.SavePrefs;
import in.quantumtech.xmpp.utils.ShareUtil;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.ReconnectionManager;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.StanzaListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.StanzaTypeFilter;
import org.jivesoftware.smack.packet.ExtensionElement;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.packet.RosterPacket;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.iqregister.AccountManager;
import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.InvitationListener;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.vcardtemp.packet.VCard;
import org.jivesoftware.smackx.xdata.Form;
import org.jivesoftware.smackx.xdata.FormField;
import org.jivesoftware.smackx.xevent.DefaultMessageEventRequestListener;
import org.jivesoftware.smackx.xevent.MessageEventManager;
import org.jivesoftware.smackx.xevent.MessageEventNotificationListener;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static in.quantumtech.xmpp.bitmapcache.ImageCache.RetainFragment.TAG;

public class SmackHelper {
    private static final String LOG_TAG = "SmackHelper";
    private static final int PORT = 5222; //Openfire port for client.
    public static final String RESOURCE_PART = "Smack";
    private static XMPPTCPConnection xmpptcpConnection;
    private ConnectionListener connectionListener;
    private Context context;
    private State state;
    private StanzaListener messagePacketListener, groupPacketListener, presencePacketListener;
    private InvitationListener groupInvitationListener;
    private static SmackHelper instance;
    private SmackContactHelper contactHelper;
    private SmackVCardHelper vCardHelper;
    private PingManager pingManager;
    private long lastPing = new Date().getTime();
    public static final String ACTION_CONNECTION_CHANGED = "com.mstr.letschat.intent.action.CONNECTION_CHANGED";
    public static final String EXTRA_NAME_STATE = "com.mstr.letschat.State";
    private OnTypingListener onTypingListener;
    private Set<String> typingUsers = new HashSet<>(); //list of users which are typing at same type.
    private SmackHelper(Context context) {
        this.context = context;
        messagePacketListener = new MessagePacketListener(context);
        presencePacketListener = new PresencePacketListener(context);
        groupPacketListener = new GroupChatMsgListener(context);
        groupInvitationListener = new GroupInvitationListener(context);

        SmackConfiguration.setDefaultPacketReplyTimeout(2000 * 1000);
        Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
        /**
         * Register custom Extension Provider to Smack, (UserLocation Extension Provider is for sending Location in chat)
         */
        ProviderManager.addExtensionProvider(UserLocation.ELEMENT_NAME, UserLocation.NAMESPACE, new LocationMessageProvider());
    }

    public static synchronized SmackHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SmackHelper(context.getApplicationContext());
        }

        return instance;
    }

    /**
     * Save current state of connection.
     * @param state
     */
    public void setState(State state) {
        if (this.state != state) {
            Log.d(LOG_TAG, "enter state: " + state.name());

            this.state = state;
        }
    }

    /**
     * to register new account and after registration direct login with same credentials call this method.
     * @param user unique userId of user.
     * @param password password of user.
     * @param nickname custom name of user
     * @param avatar convert bitmap to byte array to store image to server. this can be null.
     * @throws SmackInvocationException throw exception related to Smack.
     */
    public void signupAndLogin(String user, String password, String nickname, byte[] avatar) throws SmackInvocationException {
        connect();

        Map<String, String> attributes = new HashMap<>();
        attributes.put("name", nickname);
        try {
            AccountManager.getInstance(xmpptcpConnection).createAccount(user, password, attributes);
        } catch (Exception e) {
            throw new SmackInvocationException(e);
        }

        login(user, password);

        vCardHelper.save(nickname, avatar);
    }

    /**
     * @param message         variable of Message class.
     * @param body            message which we want to send to other user.
     * @param packetExtension location will be sent in packetExtension.
     * @param subject         subject of message like Video,image,normal etc.
     * @throws SmackInvocationException
     */
    public void sendChatMessage(Message message, String body, ExtensionElement packetExtension, String subject) throws SmackInvocationException {
        message.setBody(body);
        message.setSubject(subject);
        MessageEventManager.addNotificationsRequests(message, true, true, true, true);
        try {
            MessageEventManager.getInstanceFor(xmpptcpConnection).sendDisplayedNotification((message.getTo()), message.getStanzaId());
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        if (packetExtension != null) {
            message.addExtension(packetExtension);
        }
        try {
            xmpptcpConnection.sendStanza(message);
        } catch (NotConnectedException e) {
            throw new SmackInvocationException(e);
        }
    }

    /**
     * to search any user to send friend request.
     * @param username unique username of user
     * @return it returns user's profile.
     * @throws SmackInvocationException
     */
    public UserProfile search(String username) throws SmackInvocationException {
        String name = parseName(username);
        String jid;
        if (name == null || name.trim().length() == 0) {
            jid = username + "@" + xmpptcpConnection.getServiceName();
        } else {
            jid = parseBareAddress(username);
        }

        if (vCardHelper == null) {
            return null;
        }
        VCard vCard = vCardHelper.loadVCard(jid); //get user info from VCard Manager.
        String nickname = vCard.getNickName();

        return nickname == null ? null : new UserProfile(jid, vCard);
    }

    /**
     * @param jid this is jid of user to get his name from openfire server.
     * @return nickname of user.
     * @throws SmackInvocationException
     */
    public String getNickname(String jid) throws SmackInvocationException {
        VCard vCard = vCardHelper.loadVCard(jid);
        return vCard.getNickName();
    }

    /**
     * this method will connect connection.
     *
     * @throws SmackInvocationException
     */
    private void connect() throws SmackInvocationException {
        if (!isConnected()) {
            setState(State.CONNECTING);

            if (xmpptcpConnection == null) {
                xmpptcpConnection = createXmppConnection();
            }

            try {
                xmpptcpConnection.connect();
            } catch (Exception e) {
                Log.e(LOG_TAG, String.format("Unhandled exception %s", e.toString()), e);

                startReconnectIfNecessary();

                throw new SmackInvocationException(e);
            }
        }
    }

    /**
     * @return XMPPTCPConnection after creating connection.
     */
    private XMPPTCPConnection createXmppConnection() {
        XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
        config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
        config.setServiceName(PreferenceUtils.getServerHost(context));
        config.setHost(PreferenceUtils.getServerHost(context));
        config.setPort(PORT);
        config.setSendPresence(true);
        XMPPTCPConnection.setUseStreamManagementResumptionDefault(true);
        XMPPTCPConnection.setUseStreamManagementDefault(true);
        config.setCompressionEnabled(true);
        XMPPTCPConnection xmpptcpConnection = new XMPPTCPConnection(config.build());
        ReconnectionManager reconnectionManager = ReconnectionManager.getInstanceFor(xmpptcpConnection);
        reconnectionManager.enableAutomaticReconnection();
        return xmpptcpConnection;
    }

    /**
     * clean connection when connection fail.
     */
    public void cleanupConnection() {
        if (xmpptcpConnection != null) {
            xmpptcpConnection.removeStanzaAcknowledgedListener(messagePacketListener);

            if (connectionListener != null) {
                xmpptcpConnection.removeConnectionListener(connectionListener);
            }
        }

        if (isConnected()) {
            if (xmpptcpConnection != null) {
                xmpptcpConnection.disconnect();
            }
        }
    }

    /**
     * call this method after creating connection. and rest of the tasks will be executed inside
     * this method to insure that connection is not null.
     *
     * @throws SmackInvocationException
     */
    private void onConnectionEstablished() throws SmackInvocationException, XMPPException.XMPPErrorException, SmackException {
        if (state != State.CONNECTED) {
            //processOfflineMessages();

            try {
                xmpptcpConnection.sendStanza(new Presence(Presence.Type.available));
            } catch (NotConnectedException e) {
                e.printStackTrace();
            }

            contactHelper = new SmackContactHelper(context, xmpptcpConnection);
            vCardHelper = new SmackVCardHelper(context, xmpptcpConnection);
            processFailedMessages();
            pingManager = PingManager.getInstanceFor(xmpptcpConnection);
            pingManager.registerPingFailedListener(new PingFailedListener() {
                @Override
                public void pingFailed() {
                    // Note: remember that maybeStartReconnect is called from a different thread (the PingTask) here, it may causes synchronization problems
                    long now = new Date().getTime();
                    if (now - lastPing > 30000) {
                        Log.e(LOG_TAG, "Ping failure, reconnect");
                        startReconnectIfNecessary();
                        lastPing = now;
                    } else {
                        Log.e(LOG_TAG, "Ping failure reported too early. Skipping this occurrence.");
                    }
                }
            });

            //register Message Packet Listener for 1 to 1 chat.
            xmpptcpConnection.addSyncStanzaListener(messagePacketListener, MessageTypeFilter.CHAT);
            //register Message packet listener for Group Chat.
            xmpptcpConnection.addSyncStanzaListener(groupPacketListener, MessageTypeFilter.GROUPCHAT);
            //this listener will be called when any user go offline or online.
            xmpptcpConnection.addSyncStanzaListener(presencePacketListener, new StanzaTypeFilter(Stanza.class));

            xmpptcpConnection.addConnectionListener(createConnectionListener());
            //get Message events like typing status, seen, delivered etc.
            MessageEventManager messageEventManager = MessageEventManager.getInstanceFor(xmpptcpConnection);
            messageEventManager.addMessageEventNotificationListener(messageEventNotificationListener);
            messageEventManager.addMessageEventRequestListener(messageEventRequestListener);
            if (mucManager == null) {
                mucManager = getMucManager();
            }
            //to handle the invitation in SmackHelper
            MultiUserChatManager.getInstanceFor(xmpptcpConnection).addInvitationListener(groupInvitationListener);
            new JoinGroupsTask(new Response.Listener<Boolean>() {
                @Override
                public void onResponse(Boolean result) {
                    Log.d(TAG, "onResponse: join group " + result);
                }

                @Override
                public void onErrorResponse(Exception exception) {
                    exception.printStackTrace();
                }
            },context).execute();
            getFriends(); //get list of friends, we call this method when user install app first time.
            setState(State.CONNECTED); //set current status to Connected
            broadcastState(State.CONNECTED);
            MessageService.reconnectCount = 0; // after successful connection set this variable to 0.
        }
    }

    /**
     * get failed messages from local database and send them again and set their updated status into local database..
     *
     * @throws SmackInvocationException
     */
    private void processFailedMessages() throws SmackInvocationException {
        ChatDbHelper chatDbHelper = ChatDbHelper.getInstance(context);
        String subject = DBConstants.NORMAL;
        ArrayList<FailedMessageModel> offlineMessages = chatDbHelper.getOfflineMessages();
        for (FailedMessageModel model : offlineMessages) {
            Message message = new Message(model.getJid(), Message.Type.chat);
            message.setStanzaId(model.getStanzaId());
            try {
                JSONObject jsonObject = new JSONObject(model.getBody());
                if (jsonObject.has(ShareUtil.RESPONSE)) {
                    subject = jsonObject.getJSONArray(ShareUtil.RESPONSE).getJSONObject(0).getString(ShareUtil.TYPE);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            sendChatMessage(message, model.getBody(), null, subject);
            chatDbHelper.updateFailedMessages(model.getId());
        }
    }

    /**
     * @param state current state of network.
     */
    private void broadcastState(State state) {
        Intent intent = new Intent(ACTION_CONNECTION_CHANGED);
        intent.putExtra(EXTRA_NAME_STATE, state.toString());
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * @param username this is username of current user.
     * @param password this is password of current user.
     * @throws SmackInvocationException
     */
    public void login(String username, String password) throws SmackInvocationException {
        connect();

        try {
            if (!xmpptcpConnection.isAuthenticated()) {
                xmpptcpConnection.login(username, password, RESOURCE_PART);
            }

            onConnectionEstablished();
        } catch (Exception e) {
            SmackInvocationException exception = new SmackInvocationException(e);
            // this is caused by wrong username/password, do not reconnect
            if (exception.isCausedBySASLError()) {
                cleanupConnection();
            } else {
                startReconnectIfNecessary();
            }

            throw exception;
        }
    }

    /**
     * Get current user nickname from xmpp connection.
     * @return return nick name of user.
     * @throws SmackInvocationException
     */
    public String getLoginUserNickname() throws SmackInvocationException {
        try {
            return AccountManager.getInstance(xmpptcpConnection).getAccountAttribute("name");
        } catch (Exception e) {
            throw new SmackInvocationException(e);
        }
    }

    private void processOfflineMessages() {
        Log.i(LOG_TAG, "Begin retrieval of offline messages from server");

        OfflineMessageManager offlineMessageManager = new OfflineMessageManager(xmpptcpConnection);
        try {
            if (!offlineMessageManager.supportsFlexibleRetrieval()) {
                Log.d(LOG_TAG, "Offline messages not supported");
                return;
            }

            List<Message> msgs = offlineMessageManager.getMessages();
            for (Message msg : msgs) {
                Intent intent = new Intent(MessageService.ACTION_MESSAGE_RECEIVED, null, context, MessageService.class);
                intent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, parseBareAddress(msg.getFrom()));
                intent.putExtra(MessageService.EXTRA_DATA_NAME_MESSAGE_BODY, msg.getBody());

                context.startService(intent);
            }

            offlineMessageManager.deleteMessages();
        } catch (Exception e) {
            Log.e(LOG_TAG, "handle offline messages error ", e);
        }

        Log.i(LOG_TAG, "End of retrieval of offline messages from server");
    }


    private ConnectionListener createConnectionListener() {
        connectionListener = new ConnectionListener() {
            @Override
            public void connected(XMPPConnection arg0) {
                Log.d("Connected", "true");
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.available));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void authenticated(XMPPConnection xmppConnection, boolean b) {
                if (xmppConnection != null) {
                    try {
                        xmppConnection.sendStanza(new Presence(Presence.Type.available));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void connectionClosed() {
                Log.e(TAG, "connection closed");
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.unavailable));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void connectionClosedOnError(Exception arg0) {
                // it may be due to network is not available or server is down, update state to WAITING_TO_CONNECT
                // and schedule an automatic reconnect
                Log.e(TAG, "connection closed due to error ", arg0);
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.error));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
                startReconnectIfNecessary();
            }

            @Override
            public void reconnectingIn(int arg0) {
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.unavailable));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.unavailable));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void reconnectionSuccessful() {
                if (xmpptcpConnection != null) {
                    try {
                        xmpptcpConnection.sendStanza(new Presence(Presence.Type.available));
                    } catch (SmackException.NotConnectedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        return connectionListener;
    }

    private void startReconnectIfNecessary() {
        cleanupConnection();

        setState(State.WAITING_TO_CONNECT);

        if (NetworkUtils.isNetworkConnected(context)) {
            context.startService(new Intent(MessageService.ACTION_RECONNECT, null, context, MessageService.class));
        }
    }

    private boolean isConnected() {
        return xmpptcpConnection != null && xmpptcpConnection.isConnected();
    }

    public void onNetworkDisconnected() {
        setState(State.WAITING_FOR_NETWORK);
    }

    public void requestSubscription(String to, String nickname) throws SmackInvocationException {
        contactHelper.requestSubscription(to, nickname);
    }

    public void approveSubscription(String to, String nickname, boolean shouldRequest) throws SmackInvocationException {
        contactHelper.approveSubscription(to);

        if (shouldRequest) {
            requestSubscription(to, nickname);
        }
    }

    public void delete(String jid) throws SmackInvocationException {
        contactHelper.delete(jid);
    }

    public String loadStatus() throws SmackInvocationException {
        if (vCardHelper == null) {
            throw new SmackInvocationException("server not connected");
        }
        return vCardHelper.loadStatus();
    }

    public VCard loadVCard(String jid) throws SmackInvocationException {
        if (vCardHelper == null) {
            throw new SmackInvocationException("server not connected");
        }

        return vCardHelper.loadVCard(jid);
    }

    public VCard loadVCard() throws SmackInvocationException {
        if (vCardHelper == null) {
            throw new SmackInvocationException("server not connected");
        }

        return vCardHelper.loadVCard();
    }

    public void saveStatus(String status) throws SmackInvocationException {
        if (vCardHelper == null) {
            throw new SmackInvocationException("server not connected");
        }

        vCardHelper.saveStatus(status);
        contactHelper.broadcastStatus(status);
    }

    public SubscribeInfo processSubscribe(String from) throws SmackInvocationException {
        SubscribeInfo result = new SubscribeInfo();

        RosterEntry rosterEntry = contactHelper.getRosterEntry(from);
        RosterPacket.ItemType rosterType = rosterEntry != null ? rosterEntry.getType() : null;

        if (rosterEntry == null || rosterType == RosterPacket.ItemType.none) {
            result.setType(SubscribeInfo.TYPE_WAIT_FOR_APPROVAL);
            result.setNickname(getNickname(from));
        } else if (rosterType == RosterPacket.ItemType.to) {
            result.setType(SubscribeInfo.TYPE_APPROVED);
            result.setNickname(rosterEntry.getName());

            approveSubscription(from, null, false);
        }

        result.setFrom(from);
        return result;
    }

    public static String parseBareAddress(String var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.indexOf("/");
            return var1 < 0 ? var0 : (var1 == 0 ? "" : var0.substring(0, var1));
        }
    }

    public static String parseName(String var0) {
        if (var0 == null) {
            return null;
        } else {
            int var1 = var0.lastIndexOf("@");
            return var1 <= 0 ? "" : var0.substring(0, var1);
        }
    }

    public static String getFullJID(String userId) {
        return userId + "@" + PreferenceUtils.AWS_SERVER_IP + PreferenceUtils.RESOURCE_PART;
    }

    public void onDestroy() {
        cleanupConnection();
    }

    private MultiUserChat muc;

    public void createRoom(String roomName, List<SelectUserModel> alSelectedContacts, boolean isGroup) {

        if (xmpptcpConnection == null) {
            return;
        }
        try {
            // Create a MultiUserChat
            //creating group id by adding | and current time, to make unique id.
            String groupId = PreferenceUtils.getUser(context) + System.currentTimeMillis();
            muc = getMUC(groupId);
            muc.create(PreferenceUtils.getUser(context) + "," + PreferenceUtils.getNickname(context));
            muc.addParticipantStatusListener(new GroupUserStatusListener(context));
            // Get the the room's configuration form
            Form form = muc.getConfigurationForm();
            // Create a new form to submit based on the original form
            Form submitForm = form.createAnswerForm();
            // Add default answers to the form to submit

            List<FormField> fields = form.getFields();
            for (FormField formField : fields) {
                if (!FormField.Type.hidden.equals(formField.getType()) && formField.getVariable() != null) {
                    // Sets the default value as the answer
                    submitForm.setDefaultAnswer(formField.getVariable());
                }
            }
            // Sets the new owner of the room
            List<String> owners = new ArrayList<>();
            owners.add(xmpptcpConnection.getUser());
            submitForm.setAnswer("muc#roomconfig_roomowners", owners);
            submitForm.setAnswer("muc#roomconfig_allowinvites", true);
            //TODO: set different Group name
            submitForm.setAnswer("muc#roomconfig_roomname", roomName);
            // Send the completed form (with default values) to the server to configure the room
            muc.sendConfigurationForm(submitForm);
            for (int i = 0; i < alSelectedContacts.size(); i++) {
                try {
                    Message message = new Message();
                    message.setBody(PreferenceUtils.getNickname(context) + "^" + context.getString(R.string.new_group_created));
                    message.setThread(roomName);
                    message.setSubject(DBConstants.TYPE_GROUP_CREATED);
                    muc.invite(message, alSelectedContacts.get(i).getJid(), alSelectedContacts.get(i).getUsername());
                    context.getContentResolver().insert(ChatContract.GroupTable.CONTENT_URI, GroupTableHelper.newContentValues(groupId,roomName,alSelectedContacts.get(i).getJid(), alSelectedContacts.get(i).getUsername()));
                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }

            try {
                String gjid = null;
                if (!groupId.contains("@conference." + PreferenceUtils.getServerHost(context))) {
                    gjid = groupId + "@conference." + PreferenceUtils.getServerHost(context);
                }
                Message message = new Message(gjid, Message.Type.groupchat);
                message.setBody(context.getString(R.string.new_group_created));
                message.setSubject(DBConstants.TYPE_GROUP_CREATED);
                message.setThread(roomName);
                muc.sendMessage(message);

            } catch (SmackException.NotConnectedException e1) {
                e1.printStackTrace();
            }

        } catch (XMPPException | SmackException e) {
            e.printStackTrace();
        }
    }

    private MultiUserChatManager mucManager;

    // get muc chat manager
    private MultiUserChatManager getMucManager() {

        if (mucManager != null)
            return mucManager;

        if (xmpptcpConnection != null) {
            return MultiUserChatManager.getInstanceFor(xmpptcpConnection);
        } else {
            if (createXmppConnection() != null)
                return MultiUserChatManager.getInstanceFor(xmpptcpConnection);
            else {
                Log.v("error", "Some Error Occurred");
                Toast.makeText(context, "Cant Connect to Xmpp", Toast.LENGTH_SHORT).show();
                return null;
            }

        }

    }

    /**
     * to get group from MultiUserChat manager.
     * @param groupName unique id of group
     * @return it will return MultiUserChat group with provided id.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    // get muc
    public MultiUserChat getMUC(String groupName) throws XMPPException.XMPPErrorException, SmackException {

        if (!groupName.contains("@conference." + PreferenceUtils.getServerHost(context))) {
            groupName = groupName + "@conference." + PreferenceUtils.getServerHost(context);
        }

        if (muc != null) {
            if (muc.getRoom().contains(groupName)) {
                Log.v(TAG, muc.getNickname() + " , g = " + groupName);
                return muc;
            } else {
                try {
                    if (mucManager != null) {
                        muc = mucManager.getMultiUserChat(groupName);

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return muc;
            }
        }

        if (xmpptcpConnection != null) {
            mucManager = getMucManager();
            if (mucManager != null) {
                return mucManager.getMultiUserChat(groupName);

            } else {
                Toast.makeText(context, "Can't create Chat", Toast.LENGTH_SHORT).show();
                return null;
            }
        } else {
            if (createXmppConnection() != null) {
                mucManager = getMucManager();
                if (mucManager != null) {
                    return mucManager.getMultiUserChat(groupName);
                } else {
                    Toast.makeText(context, "Can't create Chat", Toast.LENGTH_SHORT).show();
                    return null;
                }
            } else {
                Toast.makeText(context, "Can't create Chat", Toast.LENGTH_SHORT).show();
                return null;
            }

        }
    }

    /**
     * get list of members in group
     * @param roomName group id
     * @return return List of users.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public List<String> getMUCInfo(String roomName) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(roomName);
        assert muc != null;
        if (muc.isJoined()) {
            return muc.getOccupants();
        }
        else {
            return new ArrayList<>();
        }
    }

    /**
     * get admins of group
     * @param groupId unique id of group
     * @return return list of Admins.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public List<Affiliate> getGroupOwners(String groupId) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        assert muc != null;
        return muc.getOwners();
    }

    /**
     * add user to existing group
     * @param groupJID unique id of group
     * @param selectedUsers list of users which we want to add.
     * @param roomName name of room to display.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void addMUCUser(String groupJID, List<SelectUserModel> selectedUsers, String roomName) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupJID);
        assert muc != null;
        if (muc.isJoined()) {
            for (int i = 0; i < selectedUsers.size(); i++) {
                try {
                    final Message message = new Message();
                    message.setBody(PreferenceUtils.getNickname(context) + "^ " + "added ^" + selectedUsers.get(i).getUsername());
                    message.setThread(roomName);
                    message.setSubject(DBConstants.TYPE_GROUP_INVITE);
                    muc.invite(selectedUsers.get(i).getJid(), selectedUsers.get(i).getUsername());
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                muc.sendMessage(message);
                            } catch (NotConnectedException e) {
                                e.printStackTrace();
                            }
                        }
                    },3000);

                } catch (SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }

            }
        }
    }

    /**
     * to send message in group to all users of group
     * @param gjid unique id of group
     * @param roomName name of group
     * @param msg message you want to send
     * @param subject subject of message.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void sendMUCChatMsg(String gjid, String roomName, String msg, String subject) throws XMPPException.XMPPErrorException, SmackException {

        muc = getMUC(gjid);

        if (muc != null)
            try {
                Message message = muc.createMessage();
                message.setBody(msg);
                message.setSubject(subject);
                message.setThread(roomName);
                xmpptcpConnection.sendStanza(message);
            } catch (SmackException.NotConnectedException e) {
                e.printStackTrace();
            }
    }

    /**
     * this method will extract user's jid,name etc from id, in group user id will be group jid + user jid.
     * @param groupUserId unique id of group
     * @return return GroupUserModel model containing jid,nickname of user.
     * @throws Exception
     */
    public GroupUserModel getGroupUserInfo(String groupUserId) throws Exception {
        //groupUserId contains group jid + user jid
        int i = groupUserId.indexOf("/");
        String tmpId = groupUserId.substring(i + 1);
        String[] split = tmpId.split(",");
        String id = split[0];
        String name = split[1];
        String jid = getJid(id);
        return new GroupUserModel(jid, name);
    }

    /**
     * get jid from username
     * @param name username of user
     * @return
     */
    private static String getJid(String name) {
        return name + "@" + PreferenceUtils.AWS_SERVER_IP;
    }

    /**
     * to remove user from group
     * @param userJid jid of user
     * @param groupId id of group
     * @param groupName name of group
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void removeGroupUser(String userJid, String groupId, String groupName) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        if (muc != null) {
            final Message message = new Message();
            message.setBody(PreferenceUtils.getNickname(context) + "^ " + "removed ^" + userJid.split(",")[1]);
            message.setThread(groupName);
            message.setSubject(DBConstants.TYPE_GROUP_INVITE);
            muc.sendMessage(message);
            muc.kickParticipant(userJid, userJid);
        }
    }

    /**
     * to leave group
     * first we will leave that group and then send unavailable presence to group.
     * @param groupId id of group
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void leaveGroup(String groupId) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        if (muc != null) {
            muc.leave();
            Presence presence = new Presence(Presence.Type.unavailable);
            String me = PreferenceUtils.getUser(context) + "," + PreferenceUtils.getNickname(context);
            presence.setTo(groupId + "/" + me);
            xmpptcpConnection.sendStanza(presence);
        }
    }

    /**
     * to remove group.
     * this will remove group from server. after that we will remove group from local database.
     * @param groupId group id.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void removeGroup(String groupId) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        if (muc != null) {
            muc.destroy("Just removing this group", null);
        }
    }

    /**
     * to change name of group,we will gt submit form and send new name with form,
     * only admin can send configuration form so only admin can rename groups.
     * after renaming group we will send new blank message so all users get new name.
     * we will send name with setThread method.
     * @param groupId id of group
     * @param newName new name of group
     * @param oldName old name to show in chat.
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public void renameGroup(String groupId,@NonNull String newName, @NonNull String oldName) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        if (muc != null){
            Form configurationForm = muc.getConfigurationForm();
            Form answerForm = configurationForm.createAnswerForm();
            answerForm.setAnswer("muc#roomconfig_roomname",newName);
            muc.sendConfigurationForm(answerForm);

            //after renaming group send blank message to all users to update group name on their database.
            Message notify = new Message();
            notify.setThread(newName);
            notify.setSubject(DBConstants.TYPE_GROUP_RENAME);
            notify.setBody(String.format("%s^renamed group from \"%s\" to \"%s\"",PreferenceUtils.getNickname(context),oldName,newName));
            muc.sendMessage(notify);

        }
    }

    /**
     * to get current user is admin or not, so we can hide or show delete group option.
     * @param groupId unique id of group.
     * @return
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    public boolean isCurrentUserAdmin(String groupId) throws XMPPException.XMPPErrorException, SmackException {
        boolean isAdmin = false;
        muc = getMUC(groupId);
        if (muc != null) {
            List<Affiliate> owners = muc.getOwners();
            for (Affiliate owner : owners) {
                if ((PreferenceUtils.getUser(context) + "," + PreferenceUtils.getNickname(context)).equalsIgnoreCase(owner.getNick())) {
                    isAdmin = true;
                }
            }
        }
        return isAdmin;
    }

    /**
     * to join existing or new group.
     * @param groupId unique id of group
     * @throws XMPPException.XMPPErrorException
     * @throws SmackException
     */
    private void joinGroup(String groupId) throws XMPPException.XMPPErrorException, SmackException {
        muc = getMUC(groupId);
        if (muc != null) {
            if (!muc.isJoined()) {
                DiscussionHistory history = new DiscussionHistory();
                history.setMaxStanzas(-1);
                muc.join(PreferenceUtils.getUser(context) + "," + PreferenceUtils.getNickname(context), "", history, SmackConfiguration.getDefaultPacketReplyTimeout());
            }
        }
    }

    /**
     * to get all friends forom openfire server,
     * we will call this method only when user first time install our app.
     */
    private void getFriends() {
        String syncFriends = SavePrefs.readPrefs(context, EventUtils.SYNC_FRIENDS, null);
        if (syncFriends == null) {
            new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                @Override
                public void run() {
                    Set<RosterEntry> entries = Roster.getInstanceFor(xmpptcpConnection).getEntries();
                    ChatDbHelper chatDbHelper = ChatDbHelper.getInstance(context);
                    for (RosterEntry entry : entries) {
                        ContentValues contentValues = ContactTableHelper.newContentValues(entry.getUser(), entry.getName(), "unavailable");
                        chatDbHelper.insertFriends(contentValues);
                    }
                    SavePrefs.writePrefs(context, EventUtils.SYNC_FRIENDS, "done");
                }
            }, 2000);
        }

    }

    /**
     * This listener we will get delivery status of every message.
     */
    private MessageEventNotificationListener messageEventNotificationListener = new MessageEventNotificationListener() {
        /**
         * we will get delivery status of each message here
         * @param from full jid (jid + /Smack) of user who got our message.
         * @param packetID message unique id of that message
         */
        @Override
        public void deliveredNotification(String from, String packetID) {
            if (from != null) {
                from = from.replace("/Smack", "");
            }
            Intent serviceIntent = new Intent(MessageService.ACTION_DELIVERY_STATUS, null, context, MessageService.class);
            serviceIntent.putExtra(MessageService.EXTRA_DATA_NAME_STANZA_ID, packetID);
            serviceIntent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, from);
            context.startService(serviceIntent);
        }

        /**
         * this method will we called when user read our message.
         * @param from other user's full jid (jid + /Smack)
         * @param packetID message id of that user.
         */
        @Override
        public void displayedNotification(String from, String packetID) {
            if (from != null) {
                from = from.replace("/Smack", "");
            }
            Intent serviceIntent = new Intent(MessageService.ACTION_SEEN_STATUS, null, context, MessageService.class);
            serviceIntent.putExtra(MessageService.EXTRA_DATA_NAME_STANZA_ID, packetID);
            serviceIntent.putExtra(MessageService.EXTRA_DATA_NAME_FROM, from);
            context.startService(serviceIntent);
        }

        /**
         * this method will be called when other user start typing.or stop typing.
         * @param from full jid (jid + /Smack) of other user
         * @param packetID message id of that user.
         */
        @Override
        public void composingNotification(String from, String packetID) {
            //Log.d(TAG,"composingNotification " + from + " | " + packetID);
            typingUsers.add(from); //add current user to list to get list of users currently typing in group chat.
            if (from != null && packetID != null) {
                String fromJID = from.replace("/Smack", "");


                if (onTypingListener != null){
                    if (DBConstants.TYPE_SINGLE_CHAT.equals(packetID)){
                        onTypingListener.startTyping(fromJID,null,null,1);
                    }
                    else {
                        String fromName = null;
                        String groupId = null;
                        try{
                            fromName = packetID.split("\\^")[0];
                            groupId = packetID.split("\\^")[1];
                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        onTypingListener.startTyping(groupId,fromName,fromJID,typingUsers.size());
                    }
                }
            }
        }

        @Override
        public void offlineNotification(String from, String packetID) {
            Log.d(TAG, "offlineNotification " + from + " | " + packetID);
        }

        @Override
        public void cancelledNotification(String from, String packetID) {
            //Log.d(TAG, "cancelledNotification " + from + " | " + packetID);
            typingUsers.remove(from);
            if (from != null && packetID != null){
                String fromJID = from.replace("/Smack", "");
                if (onTypingListener != null){
                    if (DBConstants.TYPE_SINGLE_CHAT.equals(packetID)){
                        onTypingListener.stopTyping(fromJID,null);
                    }
                    else {
                        onTypingListener.stopTyping(packetID,from);
                    }

                }
            }

        }
    };

    private DefaultMessageEventRequestListener messageEventRequestListener = new DefaultMessageEventRequestListener() {
        @Override
        public void deliveredNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) throws SmackException.NotConnectedException {
            super.deliveredNotificationRequested(from, packetID, messageEventManager);

            Log.d(TAG, "deliveredNotificationRequested " + from + " | " + packetID);
        }

        @Override
        public void displayedNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
            super.displayedNotificationRequested(from, packetID, messageEventManager);
            Log.d(TAG, "displayedNotificationRequested " + from + " | " + packetID);
        }

        @Override
        public void composingNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
            super.composingNotificationRequested(from, packetID, messageEventManager);
            Log.d(TAG, "composingNotificationRequested " + from + " | " + packetID);
        }

        @Override
        public void offlineNotificationRequested(String from, String packetID, MessageEventManager messageEventManager) {
            super.offlineNotificationRequested(from, packetID, messageEventManager);
            Log.d(TAG, "offlineNotificationRequested " + from + " | " + packetID);
        }
    };

    /**
     * get xmpp connection outside this class.
     * @return XMPPTCPConnection
     */
    public XMPPTCPConnection getXmpptcpConnection() {
        if (xmpptcpConnection == null) {
            xmpptcpConnection = createXmppConnection();
        }

        return xmpptcpConnection;
    }

    /**
     * get MessageEventManager outside this class.
     * @return
     */
    public MessageEventManager getEventManager(){
        return MessageEventManager.getInstanceFor(xmpptcpConnection);
    }
    public void setOnTypingListener(OnTypingListener onTypingListener){
        this.onTypingListener = onTypingListener;
    }
    public static enum State {
        CONNECTING,

        CONNECTED,

        DISCONNECTED,

        // this is a state that client is trying to reconnect to server
        WAITING_TO_CONNECT,

        WAITING_FOR_NETWORK;
    }

    public class GroupUserModel {
        String jid, name;
        boolean isAdmin;

        GroupUserModel(String jid, String name) {
            this.jid = jid;
            this.name = name;
        }

        public String getJid() {
            return jid;
        }

        public String getName() {
            return name;
        }

        public boolean isAdmin() {
            return isAdmin;
        }

        public void setAdmin(boolean admin) {
            isAdmin = admin;
        }

        public void setJid(String jid) {
            this.jid = jid;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}