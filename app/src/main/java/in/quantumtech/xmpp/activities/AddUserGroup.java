package in.quantumtech.xmpp.activities;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.adapters.ContactCursorAdapter;
import in.quantumtech.xmpp.bitmapcache.AvatarImageFetcher;
import in.quantumtech.xmpp.databases.ChatContract;
import in.quantumtech.xmpp.databases.ChatDbHelper;
import in.quantumtech.xmpp.model.SelectUserModel;
import in.quantumtech.xmpp.utils.EventUtils;

import java.util.ArrayList;

public class AddUserGroup extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        SearchView.OnQueryTextListener, View.OnClickListener, MenuItemCompat.OnActionExpandListener, AbsListView.MultiChoiceModeListener {
    private TextView newContactsText;
    private View contactsDivider;
    private ContactCursorAdapter adapter;
    private String query;
    private ListView listView;
    private AvatarImageFetcher imageFetcher;
    private AdapterView.OnItemClickListener onItemClickListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_user_group);
        imageFetcher = AvatarImageFetcher.getAvatarImageFetcher(this);
        listView = (ListView) findViewById(R.id.list);
        listView.setEmptyView(findViewById(R.id.empty));
        listView.setOnItemClickListener(onItemClickListener);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(this);

        View headerView = LayoutInflater.from(this).inflate(R.layout.contact_list_header, listView, false);
        listView.addHeaderView(headerView);
        newContactsText = (TextView) headerView.findViewById(R.id.tv_new_contacts);
        newContactsText.setOnClickListener(this);
        contactsDivider = headerView.findViewById(R.id.contacts_divider);

        adapter = new ContactCursorAdapter(this, null, imageFetcher);
        listView.setAdapter(adapter);
        final ArrayList<String> selectedUsers = new ArrayList<>();
        onItemClickListener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                position -= listView.getHeaderViewsCount();
                if (position < 0) {
                    return;
                }

                Cursor cursor = (Cursor) adapter.getItem(position);
                selectedUsers.add(cursor.getString(cursor.getColumnIndex(ChatContract.ContactTable.COLUMN_NAME_JID)));
                Intent intent = new Intent();
                intent.putExtra("selected_users", cursor.getString(cursor.getColumnIndex(ChatContract.ContactTable.COLUMN_NAME_NICKNAME)));
                setResult(EventUtils.ADD_USERS_RESULT_CODE,intent);
                finish();
            }
        };

        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(this);
        MenuItemCompat.setOnActionExpandListener(searchItem, this);

        return true;
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
                ChatContract.ContactTable._ID,
                ChatContract.ContactTable.COLUMN_NAME_JID,
                ChatContract.ContactTable.COLUMN_NAME_NICKNAME,
                ChatContract.ContactTable.COLUMN_NAME_STATUS
        };

        String selection = null;
        String[] selectionArgs = null;
        if (hasQueryText()) {
            selection = ChatContract.ContactTable.COLUMN_NAME_NICKNAME + " like ?";
            selectionArgs = new String[]{query + "%"};
        }

        return new CursorLoader(this, ChatContract.ContactTable.CONTENT_URI, projection, selection, selectionArgs, null);
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
            startActivity(new Intent(this, ContactRequestListActivity.class));
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
        inflater.inflate(R.menu.add_group_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_add_users:
                long[] checkedItemIds = listView.getCheckedItemIds();
                ArrayList<SelectUserModel> selectUserArrayList = new ArrayList<>();
                for (long id : checkedItemIds) {
                    SelectUserModel model = new SelectUserModel();
                    ChatDbHelper dbHelper = ChatDbHelper.getInstance(AddUserGroup.this);
                    model.setJid(dbHelper.getSelectedUser((int) id).getJid());
                    model.setUsername(dbHelper.getSelectedUser((int) id).getUsername());
                    selectUserArrayList.add(model);
                }
                Intent intent = new Intent();
                intent.putExtra("selected_users",selectUserArrayList);
                setResult(EventUtils.ADD_USERS_RESULT_CODE,intent);
                actionMode.finish();
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {

    }
}
