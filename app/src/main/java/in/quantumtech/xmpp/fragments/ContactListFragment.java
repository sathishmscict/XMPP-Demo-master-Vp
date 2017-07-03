package in.quantumtech.xmpp.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.activities.ChatActivity;
import in.quantumtech.xmpp.activities.ContactRequestListActivity;
import in.quantumtech.xmpp.activities.MainActivity;
import in.quantumtech.xmpp.adapters.ContactCursorAdapter;
import in.quantumtech.xmpp.databases.ChatContract.ContactTable;
import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.model.SelectUserModel;
import in.quantumtech.xmpp.xmpp.SmackHelper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

public class ContactListFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener, OnClickListener, MenuItemCompat.OnActionExpandListener, AbsListView.MultiChoiceModeListener {
    private Activity activity;
    private TextView newContactsText;
    private View contactsDivider;
    private ContactCursorAdapter adapter;
    private String query;
    private ListView listView;
    private AdapterView.OnItemClickListener onItemClickListener;
    private int screenWidth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        setHasOptionsMenu(true);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;

        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= listView.getHeaderViewsCount();
                if (position < 0) {
                    return;
                }

                Cursor cursor = (Cursor) adapter.getItem(position);
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_TO, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_JID)));
                intent.putExtra(ChatActivity.EXTRA_DATA_NAME_NICKNAME, cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME)));
                if (((MainActivity)activity).data != null){
                    intent.putExtra("data",((MainActivity)activity).data);
                    ((MainActivity)activity).data = null;
                }
                startActivity(intent);
            }
        };

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        listView = (ListView) view.findViewById(R.id.list);
        listView.setEmptyView(view.findViewById(R.id.empty));
        listView.setOnItemClickListener(onItemClickListener);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View headerView = LayoutInflater.from(getActivity()).inflate(R.layout.contact_list_header, listView, false);
        listView.addHeaderView(headerView);
        newContactsText = (TextView) headerView.findViewById(R.id.tv_new_contacts);
        newContactsText.setOnClickListener(this);
        contactsDivider = headerView.findViewById(R.id.contacts_divider);

        adapter = new ContactCursorAdapter(getActivity(), null, ((MainActivity) getActivity()).getImageFetcher());
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, this);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        restartLoader(query);

        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        restartLoader(newText);

        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = new String[]{
                ContactTable._ID,
                ContactTable.COLUMN_NAME_JID,
                ContactTable.COLUMN_NAME_NICKNAME,
                ContactTable.COLUMN_NAME_STATUS
        };

        String selection = null;
        String[] selectionArgs = null;
        if (hasQueryText()) {
            selection = ContactTable.COLUMN_NAME_NICKNAME + " like ?";
            selectionArgs = new String[]{query + "%"};
        }

        return new CursorLoader(getActivity(), ContactTable.CONTENT_URI, projection, selection, selectionArgs, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void restartLoader(String query) {
        this.query = query;
        getLoaderManager().restartLoader(0, null, this);
    }

    private boolean hasQueryText() {
        return query != null && !query.equals("");
    }

    @Override
    public void onClick(View v) {
        if (v == newContactsText) {
            startActivity(new Intent(getActivity(), ContactRequestListActivity.class));
        }
    }

    @Override
    public boolean onMenuItemActionExpand(MenuItem item) {
        setNewContactsVisibility(View.GONE);

        return true;
    }

    @Override
    public boolean onMenuItemActionCollapse(MenuItem item) {
        setNewContactsVisibility(View.VISIBLE);

        if (hasQueryText()) {
            restartLoader(null);
        }

        return true;
    }

    private void setNewContactsVisibility(int visibility) {
        newContactsText.setVisibility(visibility);
        contactsDivider.setVisibility(visibility);
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
        actionMode.setTitle(String.valueOf(listView.getCheckedItemCount()));
    }

    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        MenuInflater inflater = actionMode.getMenuInflater();
        inflater.inflate(R.menu.create_group_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_group_chat:

                final Dialog dialog = new Dialog(activity);
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

                btnCancel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        actionMode.finish();
                    }
                });
                btnCreate.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        long[] checkedItemIds = listView.getCheckedItemIds();
                        ArrayList<SelectUserModel> selectUserArrayList = new ArrayList<>();
                        for (long id : checkedItemIds) {
                            ChatDbHelper dbHelper = ChatDbHelper.getInstance(activity);
                            SelectUserModel selectedUser = dbHelper.getSelectedUser((int) id);
                            selectUserArrayList.add(selectedUser);
                        }
                        if (groupName.length() > 0) {
                            try {
                                String encode = URLEncoder.encode(groupName.getText().toString().trim(), "UTF-8");
                                SmackHelper.getInstance(activity).createRoom(encode, selectUserArrayList,true);
                                ((MainActivity)activity).gotoChat();
                                dialog.dismiss();
                                actionMode.finish();
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                        }
                        else {
                            groupName.setError("Enter group name");
                        }


                    }
                });
                dialog.show();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }
}