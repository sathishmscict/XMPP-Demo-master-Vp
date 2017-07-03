package in.quantumtech.xmpp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.CursorAdapter;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.mstr.letschat.R;
import in.quantumtech.xmpp.activities.ChatActivity;
import in.quantumtech.xmpp.bitmapcache.ImageMessageFetcher;
import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.ChatContract.ChatMessageTable;
import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;
import in.quantumtech.xmpp.utils.EventUtils;
import in.quantumtech.xmpp.utils.ShareUtil;
import in.quantumtech.xmpp.utils.Utils;
import in.quantumtech.xmpp.views.GroupActivityView;
import in.quantumtech.xmpp.views.ImageMessageView;
import in.quantumtech.xmpp.views.LocationView;
import in.quantumtech.xmpp.views.MessageView;
import in.quantumtech.xmpp.views.PlainTextView;
import in.quantumtech.xmpp.xmpp.UserLocation;
import com.thin.downloadmanager.DefaultRetryPolicy;
import com.thin.downloadmanager.DownloadRequest;
import com.thin.downloadmanager.DownloadStatusListenerV1;
import com.thin.downloadmanager.ThinDownloadManager;

import java.io.File;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MessageCursorAdapter extends CursorAdapter {
    private DateFormat timeFormat;
    private DateFormat dateFormat;
    private ThinDownloadManager downloadManager;
    private ImageMessageFetcher imageFetcher;

    public MessageCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        downloadManager = new ThinDownloadManager();
        timeFormat = new SimpleDateFormat("HH:mm");
        dateFormat = DateFormat.getDateInstance();
        imageFetcher = new ImageMessageFetcher(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        MessageView messageView = (MessageView) view;

        if (ChatMessageTableHelper.isPlainTextMessage(cursor)) {
            bindPlainTextMessage((PlainTextView) messageView, cursor);
        } else if (ChatMessageTableHelper.isLocationMessage(cursor)) {
            bindLocation((LocationView) view, cursor);
        } else if (ChatMessageTableHelper.isImageMessage(cursor)) {
            bindMedia(context, (ImageMessageView) view, cursor, ShareUtil.IMAGE_JPEG);
        } else if (ChatMessageTableHelper.isVideoMessage(cursor)) {
            bindMedia(context, (ImageMessageView) view, cursor, ShareUtil.VIDEO);
        } else if (ChatMessageTableHelper.isGroupActivity(cursor)){
            bindGroupActivity((GroupActivityView) messageView,cursor);
        }

        // set message status, sent or pending, for example
        bindStatus(messageView, cursor);

        // whether to display date at header
        bindDateSeparator(messageView, cursor);
    }

    private void bindMedia(final Context context, final ImageMessageView messageView, Cursor cursor, String type) {
        String thumb = Utils.getFullPath(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_THUMB_URL)));
        final String id = cursor.getString(cursor.getColumnIndex(ChatContract.ChatMessageTable._ID));
        final String fileUrl = Utils.getFullPath(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MEDIA_URL)));
        int downloadStatus = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_DOWNLOAD_STATUS));
        String cLength = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_FILE_LENGTH));
        final long fileSize = Long.parseLong(cLength != null ? cLength : "0");
        messageView.getMediaSize().setText(Utils.humanReadableByteCount(fileSize, false));
        Log.d("download_status", downloadStatus + "");
        String groupName = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_GROUP_NAME));
        if (groupName != null) {
            String groupUserName = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_NICKNAME));
            if (messageView.getGroupUserName() != null) {
                messageView.getGroupUserName().setVisibility(View.VISIBLE);
                messageView.getGroupUserName().setText(groupUserName);
            }
        } else {
            if (messageView.getGroupUserName() != null) {
                messageView.getGroupUserName().setVisibility(View.GONE);
            }
        }
        if (thumb != null && fileUrl != null) {
            final String extensionFromUrl = MimeTypeMap.getFileExtensionFromUrl(fileUrl);
            Glide.with(context).load(thumb).asBitmap().centerCrop().into(new BitmapImageViewTarget(messageView.getImageView()) {
                @Override
                protected void setResource(Bitmap resource) {
                    RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                    circularBitmapDrawable.setCornerRadius(8.0f);
                    messageView.getImageView().setImageDrawable(circularBitmapDrawable);
                }
            });
            if (messageView.getDeliveryStatus() != null) {
                messageView.getDeliveryStatus().setVisibility(View.VISIBLE);
                messageView.getDeliveryStatus().setImageResource(getStatusImage(cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_STATUS))));
            }

            if (downloadStatus == 1) {
                messageView.getDownloadBackground().setVisibility(View.GONE);

            } else {
                messageView.getDownloadBackground().setVisibility(View.VISIBLE);
                messageView.getBtnDownload().setVisibility(View.VISIBLE);
                messageView.getBtnCancel().setVisibility(View.GONE);
                messageView.getProgressBar().setProgress(0);

                Uri downloadUri = Uri.parse(fileUrl);
                Uri destinationUri = null;
                if (extensionFromUrl != null) {
                    if (extensionFromUrl.contains("jpeg") || extensionFromUrl.contains(ShareUtil.IMAGE_JPEG)) {
                        File dir = new File(Environment.getExternalStorageDirectory() + "/XMPP/images");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        destinationUri = Uri.parse(Environment.getExternalStorageDirectory() + "/XMPP/images/" + "IMG_" + Long.toString(System.currentTimeMillis()) + ".jpg");
                    } else if (extensionFromUrl.contains(ShareUtil.IMAGE_PNG)) {
                        File dir = new File(Environment.getExternalStorageDirectory() + "/XMPP/images");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        destinationUri = Uri.parse(Environment.getExternalStorageDirectory() + "/XMPP/images/" + "IMG_" + Long.toString(System.currentTimeMillis()) + ".png");
                    } else if (extensionFromUrl.contains(ShareUtil.VIDEO_EXT)) {
                        File dir = new File(Environment.getExternalStorageDirectory() + "/XMPP/videos");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        destinationUri = Uri.parse(Environment.getExternalStorageDirectory() + "/XMPP/videos/" + "VID_" + Long.toString(System.currentTimeMillis()) + ".mp4");
                    }
                }
                if (destinationUri != null) {
                    try {
                        final DownloadRequest downloadRequest = new DownloadRequest(downloadUri)
                                .setRetryPolicy(new DefaultRetryPolicy())
                                .setDestinationURI(destinationUri).setPriority(DownloadRequest.Priority.HIGH)
                                .setStatusListener(new DownloadStatusListenerV1() {
                                    @Override
                                    public void onDownloadComplete(DownloadRequest downloadRequest) {
                                        messageView.getDownloadBackground().setVisibility(View.GONE);
                                        ChatDbHelper chatDbHelper = ChatDbHelper.getInstance(context);
                                        chatDbHelper.updateDownloadStatus(id, downloadRequest.getDestinationURI().getPath());
                                        MediaScannerConnection.scanFile(context, new String[]{downloadRequest.getDestinationURI().getPath()}, null, new MediaScannerConnection.OnScanCompletedListener() {
                                            @Override
                                            public void onScanCompleted(String path, Uri uri) {

                                            }
                                        });
                                        ((ChatActivity) context).getLoaderManager().restartLoader(0, null, ((ChatActivity) context));
                                    }

                                    @Override
                                    public void onDownloadFailed(DownloadRequest downloadRequest, int errorCode, String errorMessage) {
                                        messageView.getDownloadBackground().setVisibility(View.VISIBLE);
                                        messageView.getBtnDownload().setVisibility(View.VISIBLE);
                                        messageView.getBtnCancel().setVisibility(View.GONE);
                                    }

                                    @Override
                                    public void onProgress(DownloadRequest downloadRequest, long totalBytes, long downloadedBytes, int progress) {
                                        messageView.getProgressBar().setProgress(progress);
                                    }
                                });

                        final int[] add = {0};
                        messageView.getBtnDownload().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                messageView.getBtnDownload().setVisibility(View.GONE);
                                messageView.getBtnCancel().setVisibility(View.VISIBLE);
                                add[0] = downloadManager.add(downloadRequest);

                            }
                        });
                        messageView.getBtnCancel().setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                messageView.getBtnDownload().setVisibility(View.VISIBLE);
                                messageView.getBtnCancel().setVisibility(View.GONE);
                                downloadManager.cancel(add[0]);
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

        }

    }
    private void bindGroupActivity(GroupActivityView view,Cursor cursor){
        view.setText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)));
    }
    private void bindPlainTextMessage(PlainTextView view, Cursor cursor) {
        String groupName = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_GROUP_NAME));
        int messageType = getMessageType(cursor);

        if (groupName != null) {
            String groupUserName = cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_NICKNAME));
            view.setMessageText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)), groupUserName, messageType);
        } else {
            if (view.getDeliverStatus() != null) {
                view.getDeliverStatus().setVisibility(View.VISIBLE);
                view.getDeliverStatus().setImageResource(getStatusImage(cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_STATUS))));
            }
            view.setMessageText(cursor.getString(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_MESSAGE)), messageType);
        }
    }

    private void bindStatus(MessageView view, Cursor cursor) {
        if (!ChatMessageTableHelper.isIncomingMessage(cursor) && !ChatMessageTableHelper.isGroupActivity(cursor)) {
            int status = cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_STATUS));
            view.showProgress(status == ChatMessageTableHelper.STATUS_FAILURE);
            //TODO: set status.
        }
    }

    private void bindDateSeparator(MessageView view, Cursor cursor) {
        if (!ChatMessageTableHelper.isGroupActivity(cursor)){
            long timeMillis = getTime(cursor);

            view.setTimeText(timeFormat.format(new Date(timeMillis)));

            if (isSameDayToPreviousPosition(timeMillis, cursor)) {
                view.hideDateSeparator();
            } else {
                view.displayDateSeparator(dateFormat.format(new Date(timeMillis)));
            }
        }

    }

    private void bindLocation(LocationView view, Cursor cursor) {
        UserLocation location = new UserLocation(cursor);
        // Get the UserLocation for this item and attach it to the MapView
        view.getMapView().setTag(location);

        // Ensure the map has been initialised by the on map ready callback in ViewHolder.
        // If it is not ready yet, it will be initialised with the NamedLocation set as its tag
        // when the callback is received.
        if (view.getMap() != null) {
            // The map is already ready to be used
            view.setMapLocation(location);
        }

        view.setAddress(location.getAddress());
        view.setName(location.getName());
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return MessageView.newView(getMessageType(cursor), context);
    }

    @Override
    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);

        return getMessageType(cursor) - 1;
    }

    @Override
    public int getViewTypeCount() {
        return ChatMessageTableHelper.VIEW_TYPE_COUNT;
    }

    private boolean isSameDayToPreviousPosition(long time, Cursor cursor) {
        // get previous item's date, for comparison
        if (cursor.getPosition() > 0 && cursor.moveToPrevious()) {
            long prevTime = getTime(cursor);
            cursor.moveToNext();

            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            cal1.setTimeInMillis(time);
            cal2.setTimeInMillis(prevTime);
            return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                    cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
        } else {
            return false;
        }
    }

    private long getTime(Cursor cursor) {
        return cursor.getLong(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TIME));
    }

    private int getMessageType(Cursor cursor) {
        return cursor.getInt(cursor.getColumnIndex(ChatMessageTable.COLUMN_NAME_TYPE));
    }

    private int getStatusImage(int id) {
        switch (id) {
            case EventUtils.STATUS_SUCCESS:
                return R.drawable.ic_sent;
            case EventUtils.STATUS_PENDING:
                return R.drawable.ic_sent;
            case EventUtils.STATUS_FAILURE:
                return R.drawable.ic_sent;
            case EventUtils.STATUS_DELIVERED:
                return R.drawable.ic_delivered;
            case EventUtils.STATUS_SEEN:
                return R.drawable.ic_seen;
            default:
                return R.drawable.ic_sent;
        }
    }
}