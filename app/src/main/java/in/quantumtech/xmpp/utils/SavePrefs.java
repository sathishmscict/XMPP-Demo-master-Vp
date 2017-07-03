package in.quantumtech.xmpp.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Pawneshwer on 25-Apr-16.
 */
public class SavePrefs {

    public static void writePrefs(Context context, String prefName, String prefValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("xmpp", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(prefName, prefValue);
        editor.apply();

    }
    public static String readPrefs(Context context, String prefName, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("xmpp", Context.MODE_PRIVATE);
        return sharedPreferences.getString(prefName, defaultValue);
    }

    public static boolean contains(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("xmpp", Context.MODE_PRIVATE);
        return sharedPreferences.contains(prefName);
    }

    public static void clear(Context context, String prefName) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("xmpp", Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(prefName).apply();
    }

}
