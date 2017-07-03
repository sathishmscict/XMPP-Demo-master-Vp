package in.quantumtech.xmpp.tasks;

import android.content.Context;

import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.GroupTableHelper;
import in.quantumtech.xmpp.xmpp.SmackHelper;

import org.jivesoftware.smackx.muc.Affiliate;
import org.jivesoftware.smackx.muc.MultiUserChat;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by quantum on 9/3/17.
 */

public class InsertGroupMembersTask extends BaseAsyncTask<String,Void,Boolean> {
    private WeakReference<MultiUserChat> chatWeakReference;
    private boolean isAdmin;

    public InsertGroupMembersTask(Response.Listener<Boolean> listener, Context context,MultiUserChat room) {
        super(listener, context);
        chatWeakReference = new WeakReference<>(room);
    }

    @Override
    protected Response<Boolean> doInBackground(String... params) {
        Context context = getContext();
        if (context != null){
            if (chatWeakReference.get() != null){

                try {
                    isAdmin = SmackHelper.getInstance(context).isCurrentUserAdmin(params[0]);
                    List<String> mucInfo = SmackHelper.getInstance(context).getMUCInfo(params[0]);
                    List<Affiliate> groupOwners = SmackHelper.getInstance(context).getGroupOwners(params[0]);
                    if (mucInfo != null) {
                        for (Affiliate groupAdmin : groupOwners) {
                            for (String userId : mucInfo) {
                                SmackHelper.GroupUserModel groupUserInfo = SmackHelper.getInstance(context).getGroupUserInfo(userId);
                                if (groupAdmin.getJid().equalsIgnoreCase(groupUserInfo.getJid())) {
                                    groupUserInfo.setAdmin(true);
                                }
                                context.getContentResolver().insert(ChatContract.GroupTable.CONTENT_URI, GroupTableHelper.newContentValues(params[0],params[1],groupUserInfo.getJid(),groupUserInfo.getName()));
                            }
                        }

                    }
                    return Response.success(true);
                } catch (Exception e) {
                    return Response.error(e);
                }

            }

        }
        return null;
    }
}
