package no.digipost.android.gui;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.model.Attachment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class AttachmentArrayAdapter extends ArrayAdapter<Attachment> {
	private final Context con;
	private final ArrayList<Attachment> attachments;



	public AttachmentArrayAdapter(final Context context, final int resource, final ArrayList<Attachment> objects) {
		super(context, resource, objects);
		con = context;
		attachments = objects;
	}

	@Override
	public View getView(final int position, final View convertView, final ViewGroup parent) {

		LayoutInflater inflater = (LayoutInflater) con.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View row = inflater.inflate(R.layout.attachentdialog_list_item, parent, false);

		Attachment attachment = attachments.get(position);

		TextView title = (TextView)row.findViewById(R.id.attachment_title);
		TextView filetype = (TextView)row.findViewById(R.id.attachment_filetype);
		TextView filesize =  (TextView)row.findViewById(R.id.attachment_filesize);

		title.setText(attachment.getSubject());
		filetype.setText(attachment.getFileType());
		filesize.setText(getSizeFormatted(attachment.getFileSize()));
		return row;
	}

	private String getSizeFormatted(final String byteString) {
		long bytes = Long.parseLong(byteString);
		String[] units = new String[] { "", "KB", "MB" };
		for (int i = 2; i > 0; i--) {
			double exp = Math.pow(1024, i);
			if (bytes > exp) {
				float n = (float) (bytes / exp);
				if(i==1){
					return (int) n + " "+ units[i];
				}
				return String.format("%3.1f %s", n, units[i]);
			}
		}
		return Long.toString(bytes);
	}

	public Attachment findMain() {
		for(Attachment a : attachments) {
			if(a.getMainDocument().equals("true")) {
				return a;
			}
		}
		return null;
	}
}
