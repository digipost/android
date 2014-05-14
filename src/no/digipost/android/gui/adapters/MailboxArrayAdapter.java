package no.digipost.android.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Mailbox;

public class MailboxArrayAdapter extends ArrayAdapter<Mailbox> {
    private Context context;
    private ArrayList<Mailbox> mailboxes;

    public MailboxArrayAdapter(final Context context, final int resource, final ArrayList<Mailbox> objects) {
        super(context, resource, objects);
        this.context = context;
        this.mailboxes = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.attachmentdialog_list_item, parent, false);

        TextView title = (TextView) row.findViewById(R.id.attachment_title);
        Mailbox mailbox = mailboxes.get(position);
        title.setText(mailbox.getName());

        return row;
    }

    public String getName(){
        return mailboxes.get(0).getName();
    }

    @Override
    public Mailbox getItem(int position) {
        return mailboxes.get(position);
    }

}
