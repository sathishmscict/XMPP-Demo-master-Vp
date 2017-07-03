package in.quantumtech.xmpp.utils;

/**
 * Created by admin on 8/24/2016.
 */

public interface DBConstants {
    int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 100;
    int CAMERA_CAPTURE_VIDEO_REQUEST_CODE = 200;
    int GALLERY_IMAGE_REQUEST_CODE = 300;
    int GALLERY_VIDEO_REQUEST_CODE = 301;
    int MEDIA_TYPE_IMAGE = 1;
    int MEDIA_TYPE_VIDEO = 2;

    //user status
    String TYPING = "typing ...";
    String ONLINE = "online";
    String OFFLINE = "offline";

    String CONVERSATION_TYPE = "con_type";
    String TYPE_GROUP_CHAT = "group_chat";
    String TYPE_SINGLE_CHAT = "single_chat";
    String TYPE_GROUP_CREATED = "group_created";
    String TYPE_GROUP_RENAME = "group_rename";
    String TYPE_GROUP_INVITE = "group_invite";

    //intent service constants.
    String EXTRA_DIALOG = "dialog";
    String EXTRA_LIST = "send_list";
    String SENDER = "sender";
    String TOP_POST = "top";
    String COMMUNITY = "community";
    int COMMENT_RESULT_CODE = 125;
    int PROFILE_REQUEST_CODE = 202;
    String PREF_UPDATE = "update_profile";
    String DISAPPEAR = "disappear";
    String NORMAL = "normal";
}
