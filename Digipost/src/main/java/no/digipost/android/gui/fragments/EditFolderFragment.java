/**
 * Copyright (C) Posten Norge AS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

    public static String fragmentName = "editFolderFragment";
    private int folderIndex;
    private boolean editFolder;
    private Folder folder;
    private View editName;
    private String folderIcon;
    private String newFolderName;
    private String validationRules;
    private GridView gridView;

    private Integer[] iconsNormal = {R.drawable.folder2x, R.drawable.envelope2x, R.drawable.file2x, R.drawable.star2x,
            R.drawable.tags2x, R.drawable.usd2x, R.drawable.heart2x, R.drawable.home2x, R.drawable.archive2x,
            R.drawable.trophy2x, R.drawable.suitcase2x, R.drawable.camera2x};

    private Integer[] iconsSelected = {R.drawable.folder_active2x, R.drawable.envelope_active2x, R.drawable.file_active2x,
            R.drawable.star_active2x, R.drawable.tags_active2x, R.drawable.usd_active2x, R.drawable.heart_active2x,
            R.drawable.home_active2x, R.drawable.archive_active2x, R.drawable.trophy_active2x,
            R.drawable.suitcase_active2x, R.drawable.camera_active2x};

    private String[] iconNames = {"FOLDER", "LETTER", "PAPER", "STAR", "TAGS", "MONEY", "HEART", "HOME", "BOX", "TROPHY", "SUITCASE",
            "CAMERA"};

    public static EditFolderFragment newInstance(int content, String validationRules, boolean editFolder) {
        EditFolderFragment editFolderfragment = new EditFolderFragment();
        Bundle args = new Bundle();
        args.putInt("content", content);
        args.putBoolean("editFolder", editFolder);
        args.putString("validationRules", validationRules);
        editFolderfragment.setArguments(args);

        return editFolderfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int content = getArguments().getInt("content");
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.Digipost_Dialog);
        editFolder = getArguments().getBoolean("editFolder");
        validationRules = getArguments().getString("validationRules");

        if (editFolder) {
            try {
                folderIndex = content - ApplicationConstants.numberOfStaticFolders;
                folder = MainContentActivity.folders.get(folderIndex);
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
        editName.setFocusableInTouchMode(true);
        editName.setFocusable(true);
        editName.requestFocus();

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

                if (!editFolder) {
                    folder = new Folder();
                }

                if (folderIsValid()) {
                    folder.setName(newFolderName);
                    folder.setIcon(folderIcon);
                    if (editFolder) {
                        ((MainContentActivity) getActivity()).saveEditFolder(folder, folderIndex);
                    } else {
                        ((MainContentActivity) getActivity()).createFolder(folder);
                    }
                    dismiss();
                } else {
                    DialogUtitities.showLongToast(getActivity(), getString(R.string.dialog_edit_folder_invalid_folder_name));
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

    private boolean folderIsValid() {
        ArrayList<Folder> folders = MainContentActivity.folders;
        folder.setName(newFolderName);
        for (Folder f : folders) {
            if (!(folder.getName().toLowerCase().equals(newFolderName.toLowerCase()))) {
                if ((f).getName().toLowerCase().equals(newFolderName.toLowerCase())) {
                    return false;
                }
            }
        }

        return newFolderName.matches(validationRules);

    }

    private class ImageAdapter extends BaseAdapter {

        private ImageAdapter() {
        }

        public int getCount() {
            return iconsNormal.length;
        }

        public Object getItem(int position) {

            if (isFolderIconBeer()) {
                folderIcon = getString(R.string.icon_beer);
                return folderIcon;
            }
            try {
                folderIcon = iconNames[position];
                return iconNames[position];
            } catch (IndexOutOfBoundsException e) {
                return iconNames[0];
            }

        }

        private boolean isFolderIconBeer() {
            return editFolder && folder.getIcon().equals(getString(R.string.icon_beer));
        }

        private int getCurrentPosition() {

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
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            } else {
                imageView = (ImageView) convertView;
            }

            if (position == getCurrentPosition() && !isFolderIconBeer()) {
                imageView.setImageResource(iconsSelected[position]);
            } else {
                imageView.setImageResource(iconsNormal[position]);
            }
            return imageView;
        }
    }
}
