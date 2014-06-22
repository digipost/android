package no.digipost.android.gui.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.model.Folder;
import no.digipost.android.utilities.DialogUtitities;

public class EditFolderFragment extends DialogFragment {
	int content;
	boolean editFolder;
	Folder folder;
	View editName;
	String folderIcon;
    String newFolderName;
    String validationRules;
	GridView gridView;

	private Integer[] iconsNormal = { R.drawable.folder_128, R.drawable.envelope_128, R.drawable.file_128, R.drawable.star_128,
			R.drawable.tags_128, R.drawable.usd_128, R.drawable.heart_128, R.drawable.home_128, R.drawable.archive_128,
			R.drawable.trophy_128, R.drawable.suitcase_128, R.drawable.camera_128 };

	private Integer[] iconsSelected = { R.drawable.folder_128_selected, R.drawable.envelope_128_selected, R.drawable.file_128_selected,
			R.drawable.star_128_selected, R.drawable.tags_128_selected, R.drawable.usd_128_selected, R.drawable.heart_128_selected,
			R.drawable.home_128_selected, R.drawable.archive_128_selected, R.drawable.trophy_128_selected,
			R.drawable.suitcase_128_selected, R.drawable.camera_128_selected };

	private String[] iconNames = { "FOLDER", "ENVELOPE", "PAPER", "STAR", "TAGS", "MONEY", "HEART", "HOME", "BOX", "TROPHY", "SUITCASE",
			"CAMERA" };

	public static EditFolderFragment newInstance(int content,String validationRules, boolean editFolder) {
		EditFolderFragment editFolderfragment = new EditFolderFragment();
		Bundle args = new Bundle();
		args.putInt("content", content);
		args.putBoolean("editFolder", editFolder);
        args.putString("validationRules",validationRules);
		editFolderfragment.setArguments(args);

		return editFolderfragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		content = getArguments().getInt("content");
		editFolder = getArguments().getBoolean("editFolder");
        validationRules = getArguments().getString("validationRules");

		if (editFolder) {
			try {
				folder = MainContentActivity.folders.get(content - ApplicationConstants.numberOfStaticFolders);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_edit_folder, container, false);

		editName = view.findViewById(R.id.edit_folder_fragment_name);
		gridView = (GridView) view.findViewById(R.id.edit_folder_fragment_gridview);
		final ImageAdapter imageAdapter = new ImageAdapter();
		gridView.requestFocusFromTouch();
		gridView.setAdapter(imageAdapter);
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				folderIcon = iconNames[position];
				gridView.setItemChecked(position, true);
				imageAdapter.notifyDataSetChanged();
			}
		});

		Button positiveButton = (Button) view.findViewById(R.id.edit_folder_fragment_save_button);

		positiveButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				newFolderName = ((EditText) editName).getText().toString().trim();

                if(!editFolder){
                    folder = new Folder();
                }

				if (folderIsValid()) {
                    folder.setName(newFolderName);
                    folder.setIcon(folderIcon);
                    if(editFolder) {
                        ((MainContentActivity) getActivity()).saveEditFolder(folder);
                    }else{
                        ((MainContentActivity) getActivity()).createFolder(folder);
                    }
                    dismiss();
				} else {
                    DialogUtitities.showToast(getActivity(), getString(R.string.dialog_edit_folder_invalid_folder_name));
                }
			}
		});

		Button negativeButton = (Button) view.findViewById(R.id.edit_folder_fragment_delete_button);
		negativeButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (editFolder) {
					((MainContentActivity) getActivity()).deleteEditFolder(folder);
				}
				dismiss();
			}
		});

		if (editFolder) {
			getDialog().setTitle(getString(R.string.dialog_edit_folder_title));
			((EditText) editName).setText(folder.getName());
			folderIcon = folder.getIcon();
		} else {
			getDialog().setTitle(getString(R.string.dialog_create_folder_title));
			positiveButton.setText(getString(R.string.dialog_create_folder_save_button));
			negativeButton.setText(getString(R.string.abort));
            folderIcon = getString(R.string.icon_folder);
		}

		return view;
	}
    private boolean folderIsValid(){
        ArrayList<Folder> folders = MainContentActivity.folders;

        for(Folder f : folders){
            if((f).getName().equals(newFolderName)){
                return false;
            }
        }

        return newFolderName.matches(validationRules);

    }

	private class ImageAdapter extends BaseAdapter {

		public ImageAdapter() {
		}

		public int getCount() {
			return iconsNormal.length;
		}

		public Object getItem(int position) {
			String newFolderIcon = folderIcon;

			try {
				folderIcon = iconNames[position];
				return iconNames[position];
			} catch (IndexOutOfBoundsException e) {
				return iconNames[0];
			}

		}

		public int getCurrentPosition() {
			for (int i = 0; i < iconNames.length; i++) {
				if (iconNames[i].equals(folderIcon)) {
					return i;
				}
			}
			return 0;
		}

		public long getItemId(int position) {
			return 0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView imageView;

			if (convertView == null) {
				imageView = new ImageView(getActivity().getApplicationContext());
				imageView.setLayoutParams(new GridView.LayoutParams(100, 100));
				imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
				imageView.setPadding(4, 4, 4, 4);
			} else {
				imageView = (ImageView) convertView;
			}

			if (position == getCurrentPosition()) {
				imageView.setImageResource(iconsSelected[position]);
			} else {
				imageView.setImageResource(iconsNormal[position]);
			}
			return imageView;
		}
	}
}
