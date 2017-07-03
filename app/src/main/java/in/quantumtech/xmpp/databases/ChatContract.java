package in.quantumtech.xmpp.databases;

import android.net.Uri;
import android.provider.BaseColumns;

import in.quantumtech.xmpp.providers.DatabaseContentProvider;

public final class ChatContract {
	private ChatContract() {}

	public static abstract class ContactTable implements BaseColumns {
		public static final String TABLE_NAME = "contact";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_STATUS = "status";
		public static final String DEFAULT_SORT_ORDER = "nickname COLLATE LOCALIZED ASC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + DatabaseContentProvider.AUTHORITY + "/contact");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + DatabaseContentProvider.AUTHORITY + ".contact";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + DatabaseContentProvider.AUTHORITY + ".contact";
	}
	public static abstract class ChatMessageTable implements BaseColumns {
		public static final String TABLE_NAME = "message";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_MESSAGE = "message";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_STATUS = "status";
		public static final String COLUMN_NAME_LONGITUDE = "longitude";
		public static final String COLUMN_NAME_LATITUDE = "latitude";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_ADDRESS = "address";
		public static final String COLUMN_NAME_MEDIA_URL = "media_url";
		public static final String COLUMN_NAME_GROUP_NAME = "group_name";
		public static final String COLUMN_NAME_GJID = "gjid";
		public static final String DEFAULT_SORT_ORDER = "_id ASC";
		public static final String COLUMN_NAME_CHAT_TYPE = "chat_type";
		public static final String COLUMN_NAME_THUMB_URL = "thumb_url";
		public static final String COLUMN_NAME_IS_INCOMING = "is_incoming";
		public static final String COLUMN_NAME_STANZA_ID = "stanza_id";
		public static final String COLUMN_NAME_SUBJECT = "subject";
		public static final String COLUMN_NAME_FILE_LENGTH = "file_length";
		public static final String COLUMN_NAME_DOWNLOAD_STATUS = "download_status";
		public static final String COLUMN_NAME_FORWARD = "to_forward";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + DatabaseContentProvider.AUTHORITY + "/message");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + DatabaseContentProvider.AUTHORITY + ".message";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + DatabaseContentProvider.AUTHORITY + ".message";
	}
	
	public static abstract class ContactRequestTable implements BaseColumns {
		public static final String TABLE_NAME = "contactrequest";
		public static final String COLUMN_NAME_JID = "jid";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_STATUS = "status";
		
		public static final String DEFAULT_SORT_ORDER = "_id DESC";
		
		public static final Uri CONTENT_URI =  Uri.parse("content://" + DatabaseContentProvider.AUTHORITY + "/contactrequest");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + DatabaseContentProvider.AUTHORITY + ".contactrequest";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + DatabaseContentProvider.AUTHORITY + ".contactrequest";
	}
	
	public static abstract class ConversationTable implements BaseColumns {
		public static final String TABLE_NAME = "conversation";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_NICKNAME = "nickname";
		public static final String COLUMN_NAME_LATEST_MESSAGE = "latestmessage";
		public static final String COLUMN_NAME_UNREAD = "unread";
		public static final String COLUMN_NAME_TIME = "time";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_PROFILE_THUMB = "thumb";
		public static final String COLUMN_NAME_PROFILE_PIC = "profile_pic";
		public static final String COLUMN_NAME_GROUP_LEFT = "group_left";
		public static final String DEFAULT_SORT_ORDER = "time DESC";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + DatabaseContentProvider.AUTHORITY + "/conversation");
		
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + DatabaseContentProvider.AUTHORITY + ".conversation";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + DatabaseContentProvider.AUTHORITY + ".conversation";
	}

	public static abstract class GroupTable implements BaseColumns {
		public static final String TABLE_NAME = "groups";
		public static final String COLUMN_NAME_GID = "group_id";
		public static final String COLUMN_NAME_GNAME = "group_name";
		public static final String COLUMN_NAME_USER_ID = "user_id";
		public static final String COLUMN_NAME_USER_NAME = "user_name";
		public static final Uri CONTENT_URI =  Uri.parse("content://" + DatabaseContentProvider.AUTHORITY + "/groups");

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + DatabaseContentProvider.AUTHORITY + ".groups";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd." + DatabaseContentProvider.AUTHORITY + ".groups";
	}
}