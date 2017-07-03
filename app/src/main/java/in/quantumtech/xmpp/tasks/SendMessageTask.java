package in.quantumtech.xmpp.tasks;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;

import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.databases.ConversationTableHelper;
import in.quantumtech.xmpp.providers.DatabaseContentProvider;
import in.quantumtech.xmpp.utils.AppLog;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.ShareUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by dilli on 1/29/2016.
 */
public abstract class SendMessageTask extends BaseAsyncTask<Void, Void, Boolean> {
    protected String to;
    protected String nickname;
    protected String body;
    protected String attachmentBody;

    public SendMessageTask(Response.Listener<Boolean> listener, Context context, String to, String nickname, String body) {
        super(listener, context);

        this.to = to;
        this.nickname = nickname;
        this.body = body;
        this.attachmentBody = body;
    }

    @Override
    public Response<Boolean> doInBackground(Void... params) {
        Context context = getContext();
        if (context == null) {
            return null;
        }

        ContentValues values;
        try {
            values = newMessage(System.currentTimeMillis());
        } catch(Exception e) {
            return Response.error(e);
        }

        ContentResolver contentResolver = context.getContentResolver();
        Uri newMessageUri;
        try {
            newMessageUri = insertNewMessage(contentResolver, values);
        } catch(Exception e) {
            return Response.error(e);
        }

        try {
            doSend(context);
        } catch(Exception e) {
            AppLog.e(String.format("send message to %s error", to), e);

            contentResolver.update(newMessageUri, ChatMessageTableHelper.newFailureStatusContentValues(), null, null);
            return Response.error(e);
        }

        contentResolver.update(newMessageUri, ChatMessageTableHelper.newSuccessStatusContentValues(), null, null);
        return Response.success(true);
    }

    protected Uri insertNewMessage(ContentResolver contentResolver, ContentValues messageValues) throws RemoteException, OperationApplicationException {
        long timeMillis = messageValues.getAsLong(ChatContract.ChatMessageTable.COLUMN_NAME_TIME);

        ArrayList<ContentProviderOperation> operations = new ArrayList<ContentProviderOperation>();
        operations.add(ContentProviderOperation.newInsert(ChatContract.ChatMessageTable.CONTENT_URI).withValues(messageValues).build());
        try {
            JSONObject jsonObject = new JSONObject(body);
            if (jsonObject.has(ShareUtil.RESPONSE)){
                String type = jsonObject.getJSONArray(ShareUtil.RESPONSE).getJSONObject(0).getString(ShareUtil.TYPE);
                switch (type){
                    case ShareUtil.IMAGE_JPEG:
                        attachmentBody = "Image";
                        break;
                    case ShareUtil.IMAGE_PNG:
                        attachmentBody = "Image";
                        break;
                    case ShareUtil.VIDEO:
                        attachmentBody = "Video";
                        break;
                    default:
                        attachmentBody = body;
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Cursor cursor = contentResolver.query(ChatContract.ConversationTable.CONTENT_URI,
                new String[]{ChatContract.ConversationTable._ID}, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?", new String[]{to}, null);
        if (cursor.moveToFirst()) { // there is a conversation already
            Uri conversationItemUri = ContentUris.withAppendedId(ChatContract.ConversationTable.CONTENT_URI,
                    cursor.getInt(cursor.getColumnIndex(ChatContract.ConversationTable._ID)));

            ContentValues values = ConversationTableHelper.newUpdateContentValues(attachmentBody, timeMillis);
            operations.add(ContentProviderOperation.newUpdate(conversationItemUri).withValues(values).build());
        } else {
            ContentValues values = null;
            if (to != null) {
                if (to.contains("@conference")) {
                    values = ConversationTableHelper.newInsertContentValues(to, nickname, attachmentBody, timeMillis, 0, DBConstants.TYPE_GROUP_CHAT);
                } else {
                    values = ConversationTableHelper.newInsertContentValues(to, nickname, attachmentBody, timeMillis, 0, DBConstants.TYPE_SINGLE_CHAT);
                }
            }
            operations.add(ContentProviderOperation.newInsert(ChatContract.ConversationTable.CONTENT_URI).withValues(values).build());
        }

        cursor.close();

        ContentProviderResult[] result = contentResolver.applyBatch(DatabaseContentProvider.AUTHORITY, operations);
        return result[0].uri;
    }

    protected abstract ContentValues newMessage(long sendTimeMillis) throws Exception;

    protected abstract void doSend(Context context) throws Exception;
}