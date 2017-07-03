package in.quantumtech.xmpp.tasks;

import android.content.ContentValues;
import android.content.Context;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.model.ShareResponse;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.ShareUtil;
import in.quantumtech.xmpp.xmpp.SmackHelper;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SendPlainTextTask extends SendMessageTask {
    private String type;
    private String subject;
    private List<ShareResponse> responses = new ArrayList<>();
    private Message message;

    public SendPlainTextTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, String body, String type, String subject) {
        super(listener, context, to, nickname, body);
        this.type = type;
        this.subject = subject;
        message = new Message(to, Message.Type.chat);
        try {
            JSONObject jsonBody = new JSONObject(body);
            if (jsonBody.has(ShareUtil.RESPONSE)) {
                JSONArray jsonArray = jsonBody.getJSONArray(ShareUtil.RESPONSE);
                for (int i = 0; i < jsonArray.length(); i++) {
                    ShareResponse response = new ShareResponse();
                    response.setUrl(jsonArray.getJSONObject(i).getString(ShareUtil.URL));
                    response.setThumb(jsonArray.getJSONObject(i).getString(ShareUtil.THUMB));
                    response.setType(jsonArray.getJSONObject(i).getString(ShareUtil.TYPE));
                    this.subject = jsonArray.getJSONObject(i).getString(ShareUtil.TYPE);
                    response.setFileSize(jsonArray.getJSONObject(i).getLong(ShareUtil.FILE_SIZE));
                    if (jsonArray.getJSONObject(i).has(ShareUtil.LOCAL_PATH)) {
                        response.setLocalPath(jsonArray.getJSONObject(i).getString(ShareUtil.LOCAL_PATH));
                    }
                    responses.add(response);
                }
            }


        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected ContentValues newMessage(long timeMillis) {
        ContentValues contentValues = null;
        switch (subject) {
            case ShareUtil.IMAGE_JPEG:
                switch (type) {
                    case DBConstants.TYPE_GROUP_CHAT:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newGroupImageMessage(to, to, nickname, getContext().getString(R.string.image_message_body), timeMillis, "", true, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), String.valueOf(response.getFileSize()),body);
                        }
                        break;
                    default:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newImageMessage(to, getContext().getString(R.string.image_message_body), timeMillis, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), true, String.valueOf(response.getFileSize()), message.getStanzaId(),0,body);
                        }
                        break;
                }
                break;
            case ShareUtil.IMAGE_PNG:
                switch (type) {
                    case DBConstants.TYPE_GROUP_CHAT:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newGroupImageMessage(to, to, nickname, getContext().getString(R.string.image_message_body), timeMillis, "", true, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), String.valueOf(response.getFileSize()),body);
                        }
                        break;
                    default:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newImageMessage(to, getContext().getString(R.string.image_message_body), timeMillis, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), true, String.valueOf(response.getFileSize()), message.getStanzaId(),0,body);
                        }
                        break;
                }
                break;
            case ShareUtil.VIDEO:

                switch (type) {
                    case DBConstants.TYPE_GROUP_CHAT:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newGroupVideoMessage(to, to, nickname, getContext().getString(R.string.video_message_body), timeMillis, "", true, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), String.valueOf(response.getFileSize()),body);
                        }
                        break;
                    default:
                        for (ShareResponse response : responses) {
                            contentValues = ChatMessageTableHelper.newVideoMessage(to, getContext().getString(R.string.video_message_body), timeMillis, response.getLocalPath() != null ? response.getLocalPath() : response.getUrl(), response.getThumb(), true, String.valueOf(response.getFileSize()), message.getStanzaId(),0,body);
                        }
                        break;
                }
                break;
            default:
                switch (type) {
                    case DBConstants.TYPE_GROUP_CHAT:
                        contentValues = ChatMessageTableHelper.newGroupTextMessage(to, to, nickname, body, timeMillis, "", true, DBConstants.NORMAL,body);
                        break;
                    default:
                        contentValues = ChatMessageTableHelper.newPlainTextMessage(to, body, timeMillis, true, DBConstants.NORMAL, message.getStanzaId(),0,body);
                        break;
                }
                break;
        }
        return contentValues;
    }

    @Override
    protected void doSend(Context context) throws SmackInvocationException {
        if (type != null) {
            switch (type) {
                case DBConstants.TYPE_SINGLE_CHAT:
                    SmackHelper.getInstance(context).sendChatMessage(message, body, null, subject);
                    break;
                case DBConstants.TYPE_GROUP_CHAT:
                    try {
                        SmackHelper.getInstance(context).sendMUCChatMsg(to,nickname, body,subject);
                    } catch (XMPPException.XMPPErrorException | SmackException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }

    }
}