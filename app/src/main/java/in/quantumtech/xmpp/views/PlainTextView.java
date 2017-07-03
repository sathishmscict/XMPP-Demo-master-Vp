package in.quantumtech.xmpp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.databases.ChatMessageTableHelper;


/**
 * Created by dilli on 12/1/2015.
 */
public abstract class PlainTextView extends MessageView {
    protected TextView messageText;
    protected TextView groupUserName;
    protected LinearLayout msgBg;
    private ImageView deliverStatus;

    public PlainTextView(Context context) {
        this(context, null);
    }

    public PlainTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setMessageText(String text, int messageType) {
       messageText.setText(text);
        this.groupUserName.setVisibility(GONE);
    }
    public void setMessageText(String text,String groupUserName,int messageType) {
        messageText.setText(text);
        if (messageType == ChatMessageTableHelper.TYPE_INCOMING_PLAIN_TEXT){
            this.groupUserName.setVisibility(VISIBLE);
            this.groupUserName.setText(groupUserName);
        }
        else {
            this.groupUserName.setVisibility(GONE);
        }
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        messageText = (TextView)findViewById(R.id.tv_message);
        groupUserName = (TextView) findViewById(R.id.group_user_name);
        msgBg = (LinearLayout) findViewById(R.id.msg_bg);
        deliverStatus = (ImageView) findViewById(R.id.deliver_status);
        setOrientation(LinearLayout.VERTICAL);
    }

    public ImageView getDeliverStatus() {
        return deliverStatus;
    }
}