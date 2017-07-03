package in.quantumtech.xmpp.tasks;

import android.content.Context;

import in.quantumtech.xmpp.SmackInvocationException;
import in.quantumtech.xmpp.xmpp.SmackHelper;

public class DeleteContactTask extends BaseAsyncTask<Void, Void, Boolean> {
	private String jid;
	
	public DeleteContactTask(Response.Listener<Boolean> listener, Context context, String jid) {
		super(listener, context);
		
		this.jid = jid;
	}
	
	@Override
	protected Response<Boolean> doInBackground(Void... params) {
		Context context = getContext();
		
		if (context != null) {
			try {
				SmackHelper.getInstance(context).delete(jid);
				
				//ContactTableHelper.getInstance(context).delete(jid);
				
				return Response.success(true);
			} catch(SmackInvocationException e) {
				return Response.error(e);
			}
		} else {
			return null;
		}
	}
}