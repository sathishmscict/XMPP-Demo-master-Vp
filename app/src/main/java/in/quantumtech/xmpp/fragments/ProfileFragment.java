package in.quantumtech.xmpp.fragments;

import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.model.LoginUserProfile;
import in.quantumtech.xmpp.tasks.LoadProfileTask;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.tasks.Response;

public class ProfileFragment extends PreferenceFragment implements Response.Listener<LoginUserProfile> {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.profile_preferences);

		new LoadProfileTask(this, getActivity()).execute();
	}

	@Override
	public void onResponse(LoginUserProfile profile) {
		if (profile != null) {
			findPreference(PreferenceUtils.AVATAR).setIcon(new BitmapDrawable(getResources(), profile.getAvatar()));
			findPreference(PreferenceUtils.NICKNAME).setSummary(profile.getNickname());
		}
	}
	
	@Override
	public void onErrorResponse(Exception exception) {}
}