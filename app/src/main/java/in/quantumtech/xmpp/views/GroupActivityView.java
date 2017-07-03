package in.quantumtech.xmpp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

import com.mstr.letschat.R;

/**
 * Created by ieglobe on 16/1/17.
 *
 */

public class GroupActivityView extends MessageView{
    private TextView textView;
    public GroupActivityView(Context context) {
        super(context,null);
    }

    public GroupActivityView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void showProgress(boolean sent) {

    }
    public void setText(String text){
        textView.setText(text);
    }

    @Override
    protected void init(Context context) {
        super.init(context);
        textView = (TextView) findViewById(R.id.tv_activity);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.group_activity_layout;
    }
}
