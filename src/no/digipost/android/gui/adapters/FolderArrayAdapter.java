package no.digipost.android.gui.adapters;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Folder;


public class FolderArrayAdapter extends ArrayAdapter<Folder> {
    private Context context;
    private ArrayList<Folder> folders;

    public FolderArrayAdapter(final Context context, final int resource, final ArrayList<Folder> objects) {
        super(context, resource, objects);
        this.context = context;
        this.folders = objects;
    }

    @Override
    public View getView(final int position, final View convertView, final ViewGroup parent) {

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.attachmentdialog_list_item, parent, false);

        TextView title = (TextView) row.findViewById(R.id.attachment_title);
        Folder folder = folders.get(position);
        title.setText(folder.getName());

        return row;
    }

    public String getName() {
        return folders.get(0).getName();
    }

    @Override
    public Folder getItem(int position) {
        return folders.get(position);
    }

}
