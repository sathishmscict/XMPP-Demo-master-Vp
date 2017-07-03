package in.quantumtech.xmpp.model;

import com.google.gson.annotations.SerializedName;
import in.quantumtech.xmpp.utils.Utils;

/**
 * Created by android on 16/12/16.
 */

public class ChatAttachmentResponse {
    private String url,thumb,type,localPath;
    @SerializedName("file_size")
    private long fileSize;

    public String getUrl() {
        return Utils.getFullPath(url);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumb() {
        return Utils.getFullPath(thumb);
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getLocalPath() {
        return localPath;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }
}
