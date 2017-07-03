package in.quantumtech.xmpp.activities;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mstr.letschat.R;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.muc.Affiliate;

import java.util.ArrayList;
import java.util.List;

import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.model.SelectUserModel;
import in.quantumtech.xmpp.utils.DBConstants;
import in.quantumtech.xmpp.utils.EventUtils;
import in.quantumtech.xmpp.utils.PreferenceUtils;
import in.quantumtech.xmpp.utils.Utils;
import in.quantumtech.xmpp.xmpp.SmackHelper;

public class GroupInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private String jid, name, ownerName;
    private ListView listView;
    private CardView layoutLeave, layoutRemove;
    private LinearLayout layoutAddUser;
    private boolean isAdmin;
    private UserAdapter adapter;
    private int screenWidth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        jid = getIntent().getStringExtra("jid");
        name = getIntent().getStringExtra("name");
        setContentView(R.layout.activity_group_info);
        ownerName = PreferenceUtils.getNickname(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        findIds();
    }

    private void findIds() {
        listView = (ListView) findViewById(R.id.user_list);
        layoutAddUser = (LinearLayout) findViewById(R.id.layout_add_user);
        layoutLeave = (CardView) findViewById(R.id.layout_leave);
        layoutRemove = (CardView) findViewById(R.id.layout_remove);
        init();
    }

    private void init() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adapter = new UserAdapter(this, new ArrayList<SmackHelper.GroupUserModel>());
        listView.setAdapter(adapter);
        setTitle(name);
        try {
            isAdmin = SmackHelper.getInstance(this).isCurrentUserAdmin(jid);
            List<String> mucInfo = SmackHelper.getInstance(this).getMUCInfo(jid);
            List<Affiliate> groupOwners = SmackHelper.getInstance(this).getGroupOwners(jid);
            List<SmackHelper.GroupUserModel> users = new ArrayList<>();
            if (mucInfo != null) {
                for (Affiliate groupAdmin : groupOwners) {
                    for (String userId : mucInfo) {
                        SmackHelper.GroupUserModel groupUserInfo = SmackHelper.getInstance(this).getGroupUserInfo(userId);
                        if (groupAdmin.getJid().equalsIgnoreCase(groupUserInfo.getJid())) {
                            groupUserInfo.setAdmin(true);
                        }
                        if (ownerName.equalsIgnoreCase(groupUserInfo.getName())) {
                            //this method will show current user name as "You" in group users list.
                            groupUserInfo.setName(EventUtils.YOU);
                        }
                        users.add(groupUserInfo);
                    }
                }
                adapter.addList(users);
                Utils.setListViewHeightBasedOnChildren(listView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_edit_group);
        fab.setOnClickListener(this);
        layoutAddUser.setOnClickListener(this);
        layoutLeave.setOnClickListener(this);
        layoutRemove.setOnClickListener(this);
        layoutRemove.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_person:
                startActivityForResult(new Intent(this, AddUserGroup.class), EventUtils.ADD_USERS_RESULT_CODE);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == EventUtils.ADD_USERS_RESULT_CODE) {
            ArrayList<SelectUserModel> selectedUsers = (ArrayList<SelectUserModel>) data.getSerializableExtra("selected_users");
            try {
                SmackHelper.getInstance(GroupInfoActivity.this).addMUCUser(jid, selectedUsers,name);
                final ProgressDialog dialog = new ProgressDialog(this);
                dialog.setTitle("Please wait");
                dialog.setMessage("Adding user to group, please wait.");
                dialog.show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            List<String> mucInfo = SmackHelper.getInstance(GroupInfoActivity.this).getMUCInfo(jid);
                            List<Affiliate> groupOwners = SmackHelper.getInstance(GroupInfoActivity.this).getGroupOwners(jid);
                            List<SmackHelper.GroupUserModel> users = new ArrayList<>();
                            if (mucInfo != null) {
                                for (Affiliate groupAdmin : groupOwners) {
                                    for (String userId : mucInfo) {
                                        SmackHelper.GroupUserModel groupUserInfo = SmackHelper.getInstance(GroupInfoActivity.this).getGroupUserInfo(userId);
                                        if (groupAdmin.getJid().equalsIgnoreCase(groupUserInfo.getJid())) {
                                            groupUserInfo.setAdmin(true);
                                        }
                                        if (ownerName.equalsIgnoreCase(groupUserInfo.getName())) {
                                            //this method will show current user name as "You" in group users list.
                                            groupUserInfo.setName(EventUtils.YOU);
                                        }
                                        users.add(groupUserInfo);
                                    }
                                }
                                adapter.addList(users);
                                Utils.setListViewHeightBasedOnChildren(listView);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                    }
                }, 2000);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.layout_add_user:
                startActivityForResult(new Intent(this, AddUserGroup.class), EventUtils.ADD_USERS_RESULT_CODE);
                break;
            case R.id.layout_leave:
                try {
                    SmackHelper.getInstance(this).leaveGroup(jid);
                    ChatDbHelper chatDbHelper = ChatDbHelper.getInstance(this);
                    chatDbHelper.leaveGroup(jid);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (XMPPException.XMPPErrorException | SmackException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.layout_remove:
                try {
                    SmackHelper.getInstance(this).removeGroup(jid);
                    ChatDbHelper chatDbHelper = ChatDbHelper.getInstance(this);
                    chatDbHelper.removeGroup(jid);
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                } catch (XMPPException.XMPPErrorException | SmackException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.fab_edit_group:
                final Dialog dialog = new Dialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCancelable(false);
                dialog.setContentView(R.layout.create_group_layout);
                WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
                layoutParams.copyFrom(dialog.getWindow().getAttributes());
                layoutParams.width = screenWidth;
                dialog.getWindow().setAttributes(layoutParams);
                Button btnCancel = (Button) dialog.findViewById(R.id.btn_cancel);
                Button btnCreate = (Button) dialog.findViewById(R.id.btn_create);
                final EditText groupName = (EditText) dialog.findViewById(R.id.txt_group_name);
                groupName.setText(name);
                btnCreate.setText(R.string.rename);
                btnCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                btnCreate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long[] checkedItemIds = listView.getCheckedItemIds();
                        ArrayList<SelectUserModel> selectUserArrayList = new ArrayList<>();
                        for (long id : checkedItemIds) {
                            ChatDbHelper dbHelper = ChatDbHelper.getInstance(GroupInfoActivity.this);
                            SelectUserModel selectedUser = dbHelper.getSelectedUser((int) id);
                            selectUserArrayList.add(selectedUser);
                        }
                        if (groupName.length() > 0) {
                            try {
                                SmackHelper.getInstance(GroupInfoActivity.this).renameGroup(jid,groupName.getText().toString().trim(),name);
                            } catch (XMPPException.XMPPErrorException | SmackException e) {
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                            finish();
                        }
                        else {
                            groupName.setError("Enter group name");
                        }


                    }
                });
                dialog.show();
                break;
        }
    }

    class UserAdapter extends BaseAdapter {
        private Context context;
        private List<SmackHelper.GroupUserModel> arrayList;
        private LayoutInflater inflater;

        public UserAdapter(Context context, List<SmackHelper.GroupUserModel> arrayList) {
            this.context = context;
            this.arrayList = arrayList;
            inflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return arrayList.size();
        }

        @Override
        public Object getItem(int position) {
            return arrayList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void addList(List<SmackHelper.GroupUserModel> list) {
            arrayList = list;
            notifyDataSetChanged();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            UserAdapter.ViewHolder holder;
            final SmackHelper.GroupUserModel model = arrayList.get(position);
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.contact_list_item_group, null);
                holder = new UserAdapter.ViewHolder();
                holder.nameText = (TextView) convertView.findViewById(R.id.tv_nickname);
                holder.statusText = (ImageView) convertView.findViewById(R.id.tv_status);
                holder.avatar = (ImageView) convertView.findViewById(R.id.avatar);
                holder.admin = (TextView) convertView.findViewById(R.id.txt_admin);
                convertView.setTag(holder);
            } else {
                holder = (UserAdapter.ViewHolder) convertView.getTag();
            }
            holder.nameText.setText(model.getName());
            holder.admin.setVisibility(model.isAdmin() ? View.VISIBLE : View.GONE);
            //disable click on own name in group users list.
            if (!EventUtils.YOU.equalsIgnoreCase(model.getName())) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showInfoDialog(model);
                    }
                });
            }

            return convertView;
        }

        private void showInfoDialog(final SmackHelper.GroupUserModel model) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            if (isAdmin) {
                String[] items = new String[]{"Remove " + model.getName(), "Message " + model.getName()};
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                try {
                                    String[] split = model.getJid().split("@");
                                    SmackHelper.getInstance(context).removeGroupUser(split[0] + "," + model.getName(), jid,name);
                                    List<String> mucInfo = SmackHelper.getInstance(context).getMUCInfo(jid);
                                    List<Affiliate> groupOwners = SmackHelper.getInstance(context).getGroupOwners(jid);
                                    List<SmackHelper.GroupUserModel> users = new ArrayList<>();
                                    if (mucInfo != null) {
                                        for (Affiliate groupAdmin : groupOwners) {
                                            for (String userId : mucInfo) {
                                                SmackHelper.GroupUserModel groupUserInfo = SmackHelper.getInstance(context).getGroupUserInfo(userId);
                                                if (groupAdmin.getJid().equalsIgnoreCase(groupUserInfo.getJid())) {
                                                    groupUserInfo.setAdmin(true);
                                                }
                                                if (ownerName.equalsIgnoreCase(groupUserInfo.getName())) {
                                                    //this method will show current user name as "You" in group users list.
                                                    groupUserInfo.setName(EventUtils.YOU);
                                                }
                                                users.add(groupUserInfo);
                                            }
                                        }
                                        addList(users);
                                        Utils.setListViewHeightBasedOnChildren(listView);
                                    }
                                } catch (XMPPException.XMPPErrorException | SmackException e) {
                                    e.printStackTrace();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                break;
                            case 1:
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, model.getJid());
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, model.getName());
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_CHAT_TYPE, DBConstants.TYPE_SINGLE_CHAT);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                dialog.show();
            } else {
                String[] items = new String[]{"Message " + model.getName()};
                dialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(context, ChatActivity.class);
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, model.getJid());
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, model.getName());
                                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_CHAT_TYPE, DBConstants.TYPE_SINGLE_CHAT);
                                context.startActivity(intent);
                                break;
                        }
                    }
                });
                dialog.show();
            }

        }

        private class ViewHolder {
            TextView nameText, admin;
            ImageView statusText;
            ImageView avatar;
        }
    }
}
