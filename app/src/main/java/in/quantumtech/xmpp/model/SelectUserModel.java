package in.quantumtech.xmpp.model;

import java.io.Serializable;

/**
 * Created by admin on 25-Nov-16.
 */

public class SelectUserModel implements Serializable {
    String jid,username;

    public String getJid() {
        return jid;
    }

    public void setJid(String jid) {
        this.jid = jid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
