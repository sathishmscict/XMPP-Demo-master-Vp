package in.quantumtech.xmpp.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.utils.PreferenceUtils;

public class StartupActivity extends AppCompatActivity implements OnClickListener {
	private static final int REQUEST_CODE_LOGIN = 1;
	private static final int REQUEST_CODE_SIGNUP = 2;
	static final int REQUEST_CODE_STORAGE_PERMS = 101;
	private SelectedButton selectedButton;
	private String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_CONTACTS,
			Manifest.permission.ACCESS_FINE_LOCATION};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_startup);
		
		if (PreferenceUtils.getUser(this) != null) {
			startMainActivity();
			return;
		} else {
			findViewById(R.id.ll_buttons_container).setVisibility(View.VISIBLE);
		}
		
		findViewById(R.id.btn_login).setOnClickListener(this);
		findViewById(R.id.btn_signup).setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.connectivity_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_server) {
			startActivity(new Intent(this, ServerSettingsActivity.class));
			return true;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_login:
			setSelectedButton(SelectedButton.login);
			if(hasPermissions())
			{
				startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
			}
			else
			{
				requestNecessaryPermissions();
			}

			break;
			
		case R.id.btn_signup:
			setSelectedButton(SelectedButton.signup);
			if(hasPermissions())
			{
				startActivityForResult(new Intent(this, SignupActivity.class), REQUEST_CODE_SIGNUP);
			}
			else
			{
				requestNecessaryPermissions();
			}

			break;
		}
	}
	private boolean hasPermissions() {
		int res = 0;
		// list all permissions which you want to check are granted or not.
		for (String perms : permissions) {
			res = checkCallingOrSelfPermission(perms);
			if (!(res == PackageManager.PERMISSION_GRANTED)) {
				// it return false because your app dosen't have permissions.
				return false;
			}

		}
		// it return true, your app has permissions.
		return true;
	}

	private void requestNecessaryPermissions() {
		// make array of permissions which you want to ask from user.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			// have arry for permissions to requestPermissions method.
			// and also send unique Request code.
			requestPermissions(permissions, REQUEST_CODE_STORAGE_PERMS);
		}
	}

	/* when user grant or deny permission then your app will check in
      onRequestPermissionsReqult about user's response. */
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grandResults) {
		// this boolean will tell us that user granted permission or not.
		boolean allowed = true;
		switch (requestCode) {
			case REQUEST_CODE_STORAGE_PERMS:
				for (int res : grandResults) {
					// if user granted all required permissions then 'allowed' will return true.
					allowed = allowed && (res == PackageManager.PERMISSION_GRANTED);
				}
				break;
			default:
				// if user denied then 'allowed' return false.
				allowed = false;
				break;
		}
		if (allowed) {
			switch (selectedButton){
				case login:
					startActivityForResult(new Intent(this, LoginActivity.class), REQUEST_CODE_LOGIN);
					break;
				case signup:
					startActivityForResult(new Intent(this, SignupActivity.class), REQUEST_CODE_SIGNUP);
					break;
			}


			// if user granted permissions then do your work.
		} else {
			// else give any custom waring message.
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
				if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
					Toast.makeText(getApplicationContext(), "Camera Permissions denied", Toast.LENGTH_SHORT).show();
				} else if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
					Toast.makeText(getApplicationContext(), "Storage Permissions denied", Toast.LENGTH_SHORT).show();
				}
				else if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
					Toast.makeText(getApplicationContext(), "Contacts Permissions denied", Toast.LENGTH_SHORT).show();
				} else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
					Toast.makeText(getApplicationContext(), "Location Permissions denied", Toast.LENGTH_SHORT).show();
				}
			}

		}

	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			finish();
		}
	}

	private void startMainActivity() {
		startActivity(new Intent(this, MainActivity.class));
		finish();
	}

	public SelectedButton getSelectedButton() {
		return selectedButton;
	}

	public void setSelectedButton(SelectedButton selectedButton) {
		this.selectedButton = selectedButton;
	}

	enum SelectedButton{
		login,signup
	}
}