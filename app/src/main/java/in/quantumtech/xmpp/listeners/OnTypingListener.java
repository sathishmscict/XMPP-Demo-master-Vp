package in.quantumtech.xmpp.listeners;

/**
 * Created by ieglobe on 3/2/17.
 */

public interface OnTypingListener {
    void startTyping(String jid, String groupUserName, String groupUserJid, int count);

    void stopTyping(String jid, String groupUserName);
}
