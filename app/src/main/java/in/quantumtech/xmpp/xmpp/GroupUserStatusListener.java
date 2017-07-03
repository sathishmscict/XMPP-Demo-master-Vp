package in.quantumtech.xmpp.xmpp;

import android.content.Context;
import android.util.Log;

import org.jivesoftware.smackx.muc.ParticipantStatusListener;

/**
 * Created by quantum on 9/3/17.
 */

public class GroupUserStatusListener implements ParticipantStatusListener {
    private Context context;
    private static final String TAG = "GroupUserStatusListener";

    public GroupUserStatusListener(Context context){
        this.context = context;
    }

    @Override
    public void joined(String participant) {
        Log.d(TAG, "joined: " + participant);
    }

    @Override
    public void left(String participant) {
        Log.d(TAG, "left: " + participant);
    }

    @Override
    public void kicked(String participant, String actor, String reason) {
        Log.d(TAG, "kicked: " + participant + ":" + actor + ":" + reason);
    }

    @Override
    public void voiceGranted(String participant) {
        Log.d(TAG, "voiceGranted: " + participant);
    }

    @Override
    public void voiceRevoked(String participant) {
        Log.d(TAG, "voiceRevoked: " + participant);
    }

    @Override
    public void banned(String participant, String actor, String reason) {
        Log.d(TAG, "banned: " + participant);
    }

    @Override
    public void membershipGranted(String participant) {
        Log.d(TAG, "membershipGranted: " + participant);
    }

    @Override
    public void membershipRevoked(String participant) {
        Log.d(TAG, "membershipRevoked: " + participant);
    }

    @Override
    public void moderatorGranted(String participant) {
        Log.d(TAG, "moderatorGranted: " + participant);
    }

    @Override
    public void moderatorRevoked(String participant) {
        Log.d(TAG, "moderatorRevoked: " + participant);
    }

    @Override
    public void ownershipGranted(String participant) {
        Log.d(TAG, "ownershipGranted: " + participant);
    }

    @Override
    public void ownershipRevoked(String participant) {
        Log.d(TAG, "ownershipRevoked: " + participant);
    }

    @Override
    public void adminGranted(String participant) {
        Log.d(TAG, "adminGranted: " + participant);
    }

    @Override
    public void adminRevoked(String participant) {
        Log.d(TAG, "adminRevoked: " + participant);
    }

    @Override
    public void nicknameChanged(String participant, String newNickname) {
        Log.d(TAG, "nicknameChanged: " + participant);
    }
}
