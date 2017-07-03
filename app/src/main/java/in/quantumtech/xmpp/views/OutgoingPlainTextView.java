package in.quantumtech.xmpp.views;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.mstr.letschat.R;


public class OutgoingPlainTextView extends PlainTextView {
	private ProgressBar progressBar;

	public OutgoingPlainTextView(Context context) {
		this(context, null);
	}
	
	public OutgoingPlainTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	public void init(Context context) {
		super.init(context);
		progressBar = (ProgressBar)findViewById(R.id.sending_progress);

	}


	protected int getLayoutResource() {
		return R.layout.outgoing_plain_text_view;
	}



	@Override
	public void showProgress(boolean fail) {
		if (fail) {
			progressBar.setVisibility(VISIBLE);
		} else {
			progressBar.setVisibility(INVISIBLE);
		}
	}
}