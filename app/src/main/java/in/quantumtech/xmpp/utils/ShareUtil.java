package in.quantumtech.xmpp.utils;

/**
 * Created by pawneshwer on 30-Dec-16.
 *
 */

public interface ShareUtil {
    //file types
    String VIDEO = "video";
    String VIDEO_CLIPS = "clips";
    String IMAGE_JPEG = "jpg";
    String IMAGE_PNG = "png";
    String GIF= "gif";

    //file ext
    String VIDEO_EXT = "mp4";

    //fields
    String URL= "url";
    String TYPE = "type";
    String FILE_SIZE = "file_size";
    String LOCAL_PATH = "localPath";
    String THUMB = "thumb";
    String COUNT = "count";
    String RESPONSE = "response";

    //download status
    int DOWNLOADED = 1;
    int NOT_DOWNLOADED = 0;
}
