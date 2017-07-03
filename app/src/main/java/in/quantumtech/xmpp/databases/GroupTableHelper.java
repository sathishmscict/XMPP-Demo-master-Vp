package in.quantumtech.xmpp.databases;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by ieglobe on 10/2/17.
 */

public class GroupTableHelper {
    public static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + ChatContract.GroupTable.TABLE_NAME + " (" +
                    ChatContract.GroupTable._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    ChatContract.GroupTable.COLUMN_NAME_GID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
                    ChatContract.GroupTable.COLUMN_NAME_GNAME + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
                    ChatContract.GroupTable.COLUMN_NAME_USER_ID + ChatDbHelper.TEXT_TYPE + ChatDbHelper.COMMA_SEP +
                    ChatContract.GroupTable.COLUMN_NAME_USER_NAME + ChatDbHelper.TEXT_TYPE +
                    " )";
    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + ChatContract.GroupTable.TABLE_NAME;
    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(SQL_CREATE_ENTRIES);
    }

    public static void onUpgrade(SQLiteDatabase database) {
        database.execSQL(SQL_DELETE_ENTRIES);
    }

    public static ContentValues newContentValues(String gid,String groupName,String jid, String nickname) {
        ContentValues values = new ContentValues();
        values.put(ChatContract.GroupTable.COLUMN_NAME_GID, gid);
        values.put(ChatContract.GroupTable.COLUMN_NAME_GNAME, groupName);
        values.put(ChatContract.GroupTable.COLUMN_NAME_USER_ID, jid);
        values.put(ChatContract.GroupTable.COLUMN_NAME_USER_NAME, nickname);

        return values;
    }
}
