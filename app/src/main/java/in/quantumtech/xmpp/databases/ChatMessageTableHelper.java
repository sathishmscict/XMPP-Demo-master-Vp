package in.quantumtech.xmpp.databases;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import in.quantumtech.xmpp.databases.ChatContract.ChatMessageTable;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.ShareUtil;
import in.quantumtech.xmpp.xmpp.UserLocation;

public class ChatMessageTableHelper {
	
	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + ChatMessageTable.TABLE_NAME + " (" +
					ChatMessageTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
					ChatMessageTable.COLUMN_NAME_GJID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_JID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_GROUP_NAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_NICKNAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_MESSAGE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_TYPE + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_STATUS + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_TIME + " LONG" + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_LATITUDE + " DOUBLE" + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_LONGITUDE + " DOUBLE" + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_STANZA_ID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_ADDRESS + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_CHAT_TYPE + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_THUMB_URL + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_SUBJECT + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_FILE_LENGTH + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_IS_INCOMING + ChatDbHelper.INTEGER_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_FORWARD + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
					ChatMessageTable.COLUMN_NAME_MEDIA_URL + ChatDbHelper.TEXT_TYPE + " )";
	
	private static final String SQL_DELETE_ENTRIES =
		    "DROP TABLE IF EXISTS " + ChatMessageTable.TABLE_NAME;
	
	public static final int TYPE_INCOMING_PLAIN_TEXT = 1;
	public static final int TYPE_OUTGOING_PLAIN_TEXT = 2;
	public static final int TYPE_INCOMING_LOCATION = 3;
	public static final int TYPE_OUTGOING_LOCATION = 4;
	public static final int TYPE_INCOMING_IMAGE = 5;
	public static final int TYPE_OUTGOING_IMAGE = 6;
	public static final int TYPE_INCOMING_VIDEO = 7;
	public static final int TYPE_OUTGOING_VIDEO = 8;
	public static final int TYPE_GROUP_MESSAGE = 9;
	public static final int TYPE_GROUP_ACTIVITY = 10;
	public static final int VIEW_TYPE_COUNT = 10;
	public static final int STATUS_SUCCESS = 1;
	public static final int STATUS_PENDING = 2;
	public static final int STATUS_FAILURE = 3;

	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(SQL_CREATE_ENTRIES);
	}
	
	public static void onUpgrade(SQLiteDatabase database) {
		database.execSQL(SQL_DELETE_ENTRIES);

	}

	public static ContentValues newPlainTextMessage(String jid, String body, long timeMillis, boolean outgoing, String subject, String messageId,int isIncoming, String toForward) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_PLAIN_TEXT : TYPE_INCOMING_PLAIN_TEXT);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_SUBJECT,subject);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,messageId);
		values.put(ChatMessageTable.COLUMN_NAME_IS_INCOMING,isIncoming);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}
	public static ContentValues newGroupActivityMessage(String jid, String body, long timeMillis, String messageId,int isIncoming) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, TYPE_GROUP_ACTIVITY);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, 0);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,messageId);
		values.put(ChatMessageTable.COLUMN_NAME_IS_INCOMING,isIncoming);
		return values;
	}
	public static ContentValues newLocationMessage(String jid, String body, long timeMillis, UserLocation location, boolean outgoing, String toForward) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_LOCATION : TYPE_INCOMING_LOCATION);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_LATITUDE, location.getLatitude());
		values.put(ChatMessageTable.COLUMN_NAME_LONGITUDE, location.getLongitude());
		values.put(ChatMessageTable.COLUMN_NAME_ADDRESS, location.getAddress());
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}

	public static ContentValues newImageMessage(String jid, String body, long timeMillis, String path,String thumb, boolean outgoing, String fileSize, String messageId,int isIncoming, String toForward) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_IMAGE : TYPE_INCOMING_IMAGE);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS, outgoing ? ShareUtil.DOWNLOADED : ShareUtil.NOT_DOWNLOADED);
		values.put(ChatMessageTable.COLUMN_NAME_MEDIA_URL, path);
		values.put(ChatMessageTable.COLUMN_NAME_THUMB_URL,thumb);
		values.put(ChatMessageTable.COLUMN_NAME_FILE_LENGTH,fileSize);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,messageId);
		values.put(ChatMessageTable.COLUMN_NAME_IS_INCOMING,isIncoming);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}

	public static ContentValues newVideoMessage(String jid, String body, long timeMillis, String path,String thumb, boolean outgoing, String fileSize, String messageId,int isIncoming, String toForward) {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID, jid);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE, body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME, timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_VIDEO : TYPE_INCOMING_VIDEO);
		values.put(ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS, outgoing ? ShareUtil.DOWNLOADED : ShareUtil.NOT_DOWNLOADED);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_MEDIA_URL, path);
		values.put(ChatMessageTable.COLUMN_NAME_THUMB_URL,thumb);
		values.put(ChatMessageTable.COLUMN_NAME_FILE_LENGTH,fileSize);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,messageId);
		values.put(ChatMessageTable.COLUMN_NAME_IS_INCOMING,isIncoming);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}
	public static ContentValues newGroupTextMessage(String gjid,String nickName,String groupName,String body,long timeMillis,String stanzaId, boolean outgoing, String subject, String toForward){
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GJID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GROUP_NAME,groupName);
		values.put(ChatMessageTable.COLUMN_NAME_NICKNAME,nickName);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE,body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME,timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,stanzaId);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_PLAIN_TEXT : TYPE_INCOMING_PLAIN_TEXT);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_SUBJECT,subject);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}
	public static ContentValues newGroupImageMessage(String gjid,String nickName,String groupName,String body,long timeMillis,String stanzaId, boolean outgoing, String path,String thumb, String fileSize, String toForward){
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GJID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GROUP_NAME,groupName);
		values.put(ChatMessageTable.COLUMN_NAME_NICKNAME,nickName);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE,body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME,timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,stanzaId);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_IMAGE : TYPE_INCOMING_IMAGE);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS, outgoing ? ShareUtil.DOWNLOADED : ShareUtil.NOT_DOWNLOADED);
		values.put(ChatMessageTable.COLUMN_NAME_MEDIA_URL, path);
		values.put(ChatMessageTable.COLUMN_NAME_THUMB_URL,thumb);
		values.put(ChatMessageTable.COLUMN_NAME_FILE_LENGTH,fileSize);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}
	public static ContentValues newGroupVideoMessage(String gjid,String nickName,String groupName,String body,long timeMillis,String stanzaId, boolean outgoing, String path,String thumb, String fileSize, String toForward){
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_JID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GJID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_GROUP_NAME,groupName);
		values.put(ChatMessageTable.COLUMN_NAME_NICKNAME,nickName);
		values.put(ChatMessageTable.COLUMN_NAME_MESSAGE,body);
		values.put(ChatMessageTable.COLUMN_NAME_TIME,timeMillis);
		values.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,stanzaId);
		values.put(ChatMessageTable.COLUMN_NAME_TYPE, outgoing ? TYPE_OUTGOING_VIDEO : TYPE_INCOMING_VIDEO);
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, outgoing ? STATUS_PENDING : STATUS_SUCCESS);
		values.put(ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS, outgoing ? ShareUtil.DOWNLOADED : ShareUtil.NOT_DOWNLOADED);
		values.put(ChatMessageTable.COLUMN_NAME_MEDIA_URL, path);
		values.put(ChatMessageTable.COLUMN_NAME_THUMB_URL,thumb);
		values.put(ChatMessageTable.COLUMN_NAME_FILE_LENGTH,fileSize);
		values.put(ChatMessageTable.COLUMN_NAME_FORWARD,toForward);
		return values;
	}

	/**
	 *
	 * @param status status is code for delivered,seen.
     * @return return content value set to be updated in local database.
     */
	public static ContentValues updateMessageStatus(String stanzaId,int status){
		ContentValues contentValues = new ContentValues();
		contentValues.put(ChatMessageTable.COLUMN_NAME_STANZA_ID,stanzaId);
		contentValues.put(ChatContract.ChatMessageTable.COLUMN_NAME_STATUS, status);
		return contentValues;
	}
	public static ContentValues groupUsers(String gjid,String jid){
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_GJID,gjid);
		values.put(ChatMessageTable.COLUMN_NAME_JID,jid);
		return values;
	}
	public static ContentValues newSuccessStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_SUCCESS);
		
		return values;
	}
	
	public static ContentValues newFailureStatusContentValues() {
		ContentValues values = new ContentValues();
		values.put(ChatMessageTable.COLUMN_NAME_STATUS, STATUS_FAILURE);
		
		return values;
	}

	public static boolean isIncomingMessage(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return type == TYPE_INCOMING_PLAIN_TEXT || type == TYPE_INCOMING_LOCATION;
	}

	public static boolean isPlainTextMessage(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return isPlainTextMessage(type);
	}

	public static boolean isPlainTextMessage(int type) {
		return type == TYPE_INCOMING_PLAIN_TEXT || type == TYPE_OUTGOING_PLAIN_TEXT;
	}

	public static boolean isLocationMessage(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return isLocationMessage(type);
	}

	public static boolean isLocationMessage(int type) {
		return type == TYPE_INCOMING_LOCATION || type == TYPE_OUTGOING_LOCATION;
	}
	public static boolean isGroupActivity(int type){
		return type == TYPE_GROUP_ACTIVITY;
	}
	public static boolean isGroupActivity(Cursor cursor){
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return isGroupActivity(type);
	}
	public static boolean isImageMessage(int type) {
		return type == TYPE_INCOMING_IMAGE || type == TYPE_OUTGOING_IMAGE;
	}

	public static boolean isImageMessage(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return isImageMessage(type);
	}
	public static boolean isVideoMessage(Cursor cursor) {
		int type = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
		return isVideoMessage(type);
	}

	public static boolean isVideoMessage(int type) {
		return type == TYPE_INCOMING_VIDEO || type == TYPE_OUTGOING_VIDEO;
	}
	public static boolean isGroupMessage(String groupName){
		return DBConstants.TYPE_GROUP_CHAT.equalsIgnoreCase(groupName);

	}
	public static void updateToVersion2(SQLiteDatabase db) {
		//final String sqlAddLatitude = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_LATITUDE + " DOUBLE";
		//final String sqlAddLongitude = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_LONGITUDE + " DOUBLE";
		//final String sqlAddAddress = "ALTER TABLE " + ChatMessageTable.TABLE_NAME + " ADD " + ChatMessageTable.COLUMN_NAME_ADDRESS + ChatDbHelper.TEXT_TYPE;
		final String sqlAddMediaUrl = "ALTER TABLE " + ChatContract.ConversationTable.TABLE_NAME + " ADD " + ChatContract.ConversationTable.COLUMN_NAME_GROUP_LEFT + ChatDbHelper.TEXT_TYPE;

		//db.execSQL(sqlAddLatitude);
		//db.execSQL(sqlAddLongitude);
		//db.execSQL(sqlAddAddress);
		db.execSQL(sqlAddMediaUrl);
	}
}