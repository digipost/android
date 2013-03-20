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

public class AttachmentListAdapter extends ArrayAdapter<Attachment> {
	private final Context con;
	private final ArrayList<Attachment> attachments;
	private final Attachment main;



	public AttachmentListAdapter(final Context context, final int resource, final ArrayList<Attachment> objects) {
		super(context, resource, objects);
		con = context;
		attachments = objects;
		main = findMain();
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
		String[] units = new String[] { "", "KB", "MB", "GB" };
		for (int i = 3; i > 0; i--) {
			double exp = Math.pow(1024, i);
			if (bytes > exp) {
				return String.format("%3.1f %s", bytes / exp, units[i]);
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
