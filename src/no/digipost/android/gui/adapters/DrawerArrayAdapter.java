package no.digipost.android.gui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import no.digipost.android.R;

public class DrawerArrayAdapter<String> extends ArrayAdapter<String>{
    protected Context context;

    public DrawerArrayAdapter(final Context context, final int resource, final String[] objects) {
        super(context, resource, objects);
        this.context = context;
    }

    public View getView(final int position, final View convertView, final ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.drawer_list_item, parent, false);

        return row;
    }

}