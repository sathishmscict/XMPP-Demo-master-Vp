package in.quantumtech.xmpp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;


/**
 * Created by dilli on 1/29/2016.
 *
 */
public abstract class ImageMessageView extends MessageView {
    private ImageView image,btnCancel,btnDownload,deliveryStatus;
    private CircleProgressBar progressBar;
    private FrameLayout downloadBackground;
    private TextView mediaSize,groupUserName;

    public ImageMessageView(Context context) {
        this(context, null);
    }

    public ImageMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        image = (ImageView)findViewById(R.id.image);
        progressBar = (CircleProgressBar) findViewById(R.id.sending_progress);
        downloadBackground = (FrameLayout) findViewById(R.id.download_layout);
        deliveryStatus = (ImageView) findViewById(R.id.deliver_status);
        btnCancel = (ImageView) findViewById(R.id.cancel);
        btnDownload = (ImageView) findViewById(R.id.download);
        groupUserName = (TextView) findViewById(R.id.group_user_name);
        mediaSize = (TextView) findViewById(R.id.media_size);
        setOrientation(LinearLayout.VERTICAL);
    }

    public ImageView getImageView() {
        return image;
    }

    public CircleProgressBar getProgressBar() {
        return progressBar;
    }

    public FrameLayout getDownloadBackground() {
        return downloadBackground;
    }

    public ImageView getBtnCancel() {
        return btnCancel;
    }

    public ImageView getBtnDownload() {
        return btnDownload;
    }

    public TextView getMediaSize() {
        return mediaSize;
    }

    public TextView getGroupUserName() {
        return groupUserName;
    }

    public ImageView getDeliveryStatus() {
        return deliveryStatus;
    }
}