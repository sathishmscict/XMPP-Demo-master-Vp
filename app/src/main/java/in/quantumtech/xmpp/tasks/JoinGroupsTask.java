package in.quantumtech.xmpp.tasks;

import android.content.Context;

import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.xmpp.GroupUserStatusListener;
import in.quantumtech.xmpp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.util.Date;
import java.util.List;

/**
 * Created by ieglobe on 15/2/17.
 *
 */

public class JoinGroupsTask extends BaseAsyncTask<Void,Void,Boolean>{
    public JoinGroupsTask(Response.Listener<Boolean> listener, Context context) {
        super(listener, context);
    }

    @Override
    protected Response<Boolean> doInBackground(Void... params) {
        List<ChatDbHelper.GroupsModel> allGroups = ChatDbHelper.getInstance(getContext()).getAllGroups();
        for (ChatDbHelper.GroupsModel model : allGroups) {
            Date date = new Date(model.getLastMessage());

            try {
                MultiUserChat muc = SmackHelper.getInstance(getContext()).getMUC(model.getGroupId());
                muc.addParticipantStatusListener(new GroupUserStatusListener(getContext()));
                DiscussionHistory history = new DiscussionHistory();
                if (model.getLastMessage() == 0)
                    history.setMaxStanzas(300);
                else
                    history.setSince(date); //timestamp from your last message

                muc.join(PreferenceUtils.getUser(getContext()) + "," + PreferenceUtils.getNickname(getContext()), null, history, SmackConfiguration.getDefaultPacketReplyTimeout());
                Response.success(true);
            } catch (Exception e) {
                Response.error(e);
            }

        }
        return null;
    }
}
