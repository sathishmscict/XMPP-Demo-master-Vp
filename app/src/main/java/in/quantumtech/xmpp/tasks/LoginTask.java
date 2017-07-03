package in.quantumtech.xmpp.tasks;

import android.app.ProgressDialog;
import android.content.Context;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.utils.AppLog;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.xmpp.SmackHelper;

public class LoginTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String username;
	private String password;

	private ProgressDialog dialog;
	
	public LoginTask(Response.Listener<Boolean> listener, Context context, String username, String password) {
		super(listener, context);
		
		this.username = username;
		this.password = password;

		dialog = ProgressDialog.show(context, null, context.getResources().getString(R.string.login));
	}
	
	@Override
	public Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		if (context != null) {
			try {
				SmackHelper smackHelper = SmackHelper.getInstance(context);
				
				smackHelper.login(username, password);

				PreferenceUtils.setLoginUser(context, username, password, smackHelper.getLoginUserNickname());
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				AppLog.e(String.format("login error %s", username), e);
				
				return Response.error(e);
			}
		} else {
			return null;
		}
	}

	@Override
	protected void onPostExecute(Response<Boolean> response) {
		dialog.dismiss();

		super.onPostExecute(response);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();

		dismissDialog();
	}

	public void dismissDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
		}
	}

	public void dismissDialogAndCancel() {
		dismissDialog();
		cancel(false);
	}
}