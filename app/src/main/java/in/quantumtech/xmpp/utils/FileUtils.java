package in.quantumtech.xmpp.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Environment;

import java.io.File;

public class FileUtils {
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String VIDEO_EXTENSION = ".mp4";
	/**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = isExternalStorageWritable() || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }
    public static File getSentVideosDir(Context context) {
        File dir = new File(getVideosDir(context), "sent");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File getReceivedVideosDir(Context context) {
        File dir = new File(getVideosDir(context), "received");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }
    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }
    private static File getVideosDir(Context context) {
        return new File(Environment.getExternalStorageDirectory(),"xmpp/videos");
    }
    public static File getVideosThumbDir() {
        File dir = new File(Environment.getExternalStorageDirectory(),".xmpp/videos/thumb");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }
    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
    	if (Utils.hasFroyo()) {
    		return context.getExternalCacheDir();
    	}

    	// Before Froyo we need to construct the external cache dir ourselves
    	final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
    	return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static File getSentImagesDir(Context context) {
        File dir = new File(getImagesDir(context), ".sent");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File getReceivedImagesDir(Context context) {
        File dir = new File(getImagesDir(context), "received");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    private static File getImagesDir(Context context) {
        return new File(Environment.getExternalStorageDirectory(),"xmpp/images");
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
    public static void galleryAddPic(Context context,Uri contentUri) throws Exception {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
    }
}