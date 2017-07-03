package in.quantumtech.xmpp.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ResourceCursorAdapter;
import android.widget.TextView;

import com.mstr.letschat.R;
import in.quantumtech.xmpp.bitmapcache.AvatarImageFetcher;
import in.quantumtech.xmpp.databases.ChatContract.ContactTable;

public class ContactCursorAdapter extends ResourceCursorAdapter {
    private AvatarImageFetcher imageFetcher;

    public ContactCursorAdapter(Context context, Cursor c, AvatarImageFetcher imageFetcher) {
        super(context, R.layout.contact_list_item, c, 0);

        this.imageFetcher = imageFetcher;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        imageFetcher.loadImage(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_JID)), viewHolder.avatar);
        viewHolder.nameText.setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_NICKNAME)));
        //viewHolder.statusText.setText(cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_STATUS)));
        String Statuss = cursor.getString(cursor.getColumnIndex(ContactTable.COLUMN_NAME_STATUS));

        if (Statuss != null) {

            switch (Statuss) {
                case "Available":
                    viewHolder.statusText.setImageResource(R.drawable.avaible);
                    break;
                case "available":
                    viewHolder.statusText.setImageResource(R.drawable.avaible);
                    break;
                case "unavailable":
                    viewHolder.statusText.setImageResource(R.drawable.invisible);
                    break;
                case "Busy":
                    viewHolder.statusText.setImageResource(R.drawable.busy);
                    break;
                case "Invisible":
                    viewHolder.statusText.setImageResource(R.drawable.invisible);
                    break;
                case "Away":
                    viewHolder.statusText.setImageResource(R.drawable.away);
                    break;
                case "In a meeting":
                    viewHolder.statusText.setImageResource(R.drawable.meting);
                    break;
            }


        }


    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = super.newView(context, cursor, parent);

        ViewHolder viewHolder = new ViewHolder();
        viewHolder.nameText = (TextView) view.findViewById(R.id.tv_nickname);
        viewHolder.statusText = (ImageView) view.findViewById(R.id.tv_status);
        viewHolder.avatar = (ImageView) view.findViewById(R.id.avatar);
        view.setTag(viewHolder);

        return view;
    }

    static class ViewHolder {
        TextView nameText;
        ImageView statusText;
        ImageView avatar;
    }
}