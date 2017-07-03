package in.quantumtech.xmpp.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import in.quantumtech.xmpp.model.FailedMessageModel;
import in.quantumtech.xmpp.model.SelectUserModel;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.EventUtils;
import in.quantumtech.xmpp.utils.ShareUtil;

import java.util.ArrayList;
import java.util.List;

public class ChatDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "chat.db";

    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";

    private static ChatDbHelper instance;

    public static synchronized ChatDbHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChatDbHelper(context.getApplicationContext());
        }

        return instance;
    }

    private ChatDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public String getSelectedChats(int id){
        String data = null;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String[] columns = {ChatContract.ChatMessageTable.COLUMN_NAME_FORWARD};
        Cursor cursor = writableDatabase.query(ChatContract.ChatMessageTable.TABLE_NAME, columns, ChatContract.ChatMessageTable._ID + " =?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_FORWARD));
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return data;
    }



    public String getSelectedMedia(int id){
        String data = null;
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String[] columns = {ChatContract.ChatMessageTable.COLUMN_NAME_MEDIA_URL};
        Cursor cursor = writableDatabase.query(ChatContract.ChatMessageTable.TABLE_NAME, columns, ChatContract.ChatMessageTable._ID + " =?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToNext()) {
            do {
                data = cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_MEDIA_URL));
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return data;
    }

    public List<GroupsModel> getAllGroups(){
        List<GroupsModel> list = new ArrayList<>();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        Cursor cursor = writableDatabase.query(ChatContract.ConversationTable.TABLE_NAME, null, ChatContract.ConversationTable.COLUMN_NAME_TYPE + "=? AND " + ChatContract.ConversationTable.COLUMN_NAME_GROUP_LEFT + " !=?", new String[]{DBConstants.TYPE_GROUP_CHAT, String.valueOf(1)}, null, null, null);
        if (cursor != null && cursor.moveToNext()){

            do {
                GroupsModel model = new GroupsModel();
                model.setGroupId(cursor.getString(cursor.getColumnIndex(ChatContract.ConversationTable.COLUMN_NAME_NAME)));
                model.setLastMessage(cursor.getLong(cursor.getColumnIndex(ChatContract.ConversationTable.COLUMN_NAME_TIME)));
                list.add(model);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return list;
    }
    public SelectUserModel getSelectedUser(int id){
        SelectUserModel model = new SelectUserModel();
        SQLiteDatabase writableDatabase = getWritableDatabase();
        String[] columns = {ChatContract.ContactTable.COLUMN_NAME_JID, ChatContract.ContactTable.COLUMN_NAME_NICKNAME};
        Cursor cursor = writableDatabase.query(ChatContract.ContactTable.TABLE_NAME, columns, ChatContract.ContactTable._ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
        if (cursor != null && cursor.moveToNext()){
            do {
                model.setJid(cursor.getString(cursor.getColumnIndex(ChatContract.ContactTable.COLUMN_NAME_JID)));
                model.setUsername(cursor.getString(cursor.getColumnIndex(ChatContract.ContactTable.COLUMN_NAME_NICKNAME)));
            }
            while (cursor.moveToNext());
            cursor.close();
        }

        return model;
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        ContactTableHelper.onCreate(db);
        ChatMessageTableHelper.onCreate(db);
        ContactRequestTableHelper.onCreate(db);
        ConversationTableHelper.onCreate(db);
    }
    public ArrayList<FailedMessageModel> getOfflineMessages(){
        ArrayList<FailedMessageModel> pendingIds = new ArrayList<>();
        SQLiteDatabase writableDatabase = getWritableDatabase();

        String[] columns = {ChatContract.ChatMessageTable._ID, ChatContract.ChatMessageTable.COLUMN_NAME_FORWARD, ChatContract.ChatMessageTable.COLUMN_NAME_JID, ChatContract.ChatMessageTable.COLUMN_NAME_STANZA_ID};
        Cursor cursor = writableDatabase.query(ChatContract.ChatMessageTable.TABLE_NAME,columns, ChatContract.ChatMessageTable.COLUMN_NAME_STATUS + "=?",new String[]{String.valueOf(3)},null,null,null);
        if (cursor != null && cursor.moveToNext()){
            do {
                FailedMessageModel model = new FailedMessageModel();
                model.setId(cursor.getInt(cursor.getColumnIndex(ChatContract.ChatMessageTable._ID)));
                model.setBody(cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_FORWARD)));
                model.setJid(cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_JID)));
                model.setStanzaId(cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_STANZA_ID)));
                pendingIds.add(model);
            }
            while (cursor.moveToNext());
            cursor.close();
        }
        return pendingIds;
    }
    public void updateFailedMessages(int id){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_STATUS,1);
        writableDatabase.update(ChatContract.ChatMessageTable.TABLE_NAME,contentValues, ChatContract.ChatMessageTable._ID +"=?",new String[]{String.valueOf(id)});
        writableDatabase.close();
    }
    public void updateDownloadStatus(String id, String url) {
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS, ShareUtil.DOWNLOADED);
        contentValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_MEDIA_URL, url);
        writableDatabase.update(ChatContract.ChatMessageTable.TABLE_NAME, contentValues, ChatContract.ChatMessageTable._ID + "=?", new String[]{id});
        writableDatabase.close();
    }
    public void updateDeliveredMessages(String stanzaId){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = ChatMessageTableHelper.updateMessageStatus(stanzaId, EventUtils.STATUS_DELIVERED);
        writableDatabase.update(ChatContract.ChatMessageTable.TABLE_NAME, contentValues, ChatContract.ChatMessageTable.COLUMN_NAME_STANZA_ID + "=?", new String[]{stanzaId});
    }

    public void updateSeenMessages(String jid){
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_STATUS,EventUtils.STATUS_SEEN);
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.update(ChatContract.ChatMessageTable.TABLE_NAME, contentValues, ChatContract.ChatMessageTable.COLUMN_NAME_JID + "=?", new String[]{jid});
    }
    public List<String> getUnreadMessages(String jid){
        List<String> unReadMessages = new ArrayList<>();
        String[] columns = new String[]{ChatContract.ChatMessageTable.COLUMN_NAME_STANZA_ID};
        SQLiteDatabase writableDatabase = getWritableDatabase();
        Cursor cursor = writableDatabase.query(ChatContract.ChatMessageTable.TABLE_NAME, columns, ChatContract.ChatMessageTable.COLUMN_NAME_JID + "=?" + " AND " + ChatContract.ChatMessageTable.COLUMN_NAME_IS_INCOMING + "=?", new String[]{jid, String.valueOf(1)}, null, null, null);
        if (cursor != null && cursor.moveToNext()){
            do {
                unReadMessages.add(cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable.COLUMN_NAME_STANZA_ID)));
            }
            while (cursor.moveToNext());
            cursor.close();
        }

        writableDatabase.close();
        return unReadMessages;
    }
    public void removeGroup(String groupId){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.delete(ChatContract.ConversationTable.TABLE_NAME, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?",new String[]{groupId});
        writableDatabase.close();
    }
    public void leaveGroup(String groupId){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(ChatContract.ConversationTable.COLUMN_NAME_GROUP_LEFT,1);
        writableDatabase.update(ChatContract.ConversationTable.TABLE_NAME,contentValues, ChatContract.ConversationTable.COLUMN_NAME_NAME + "=?",new String[]{groupId});
        writableDatabase.close();
    }
    public void insertFriends(ContentValues contentValues){
        SQLiteDatabase writableDatabase = getWritableDatabase();
        writableDatabase.insert(ChatContract.ContactTable.TABLE_NAME,null,contentValues);
        writableDatabase.close();
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*if (newVersion == 5) {
            updateToVersion2(db);
            return;
        }*/
        ChatMessageTableHelper.onUpgrade(db);
        ConversationTableHelper.onUpgrade(db);
        ChatMessageTableHelper.onCreate(db);
        ConversationTableHelper.onCreate(db);
    }

    private void updateToVersion2(SQLiteDatabase db) {
        ChatMessageTableHelper.updateToVersion2(db);
    }

    public class GroupsModel{
        private String groupId;
        private long lastMessage;

        public String getGroupId() {
            return groupId;
        }

        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        public long getLastMessage() {
            return lastMessage;
        }

        public void setLastMessage(long lastMessage) {
            this.lastMessage = lastMessage;
        }
    }
}