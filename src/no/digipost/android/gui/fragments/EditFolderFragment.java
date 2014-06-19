package no.digipost.android.gui.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.model.Folder;


public class EditFolderFragment extends DialogFragment {
    int content;
    Folder folder;
    View editName;

    public static EditFolderFragment newInstance(int content) {
        EditFolderFragment editFolderfragment = new EditFolderFragment();
        Bundle args = new Bundle();
        args.putInt("content",content);
        editFolderfragment.setArguments(args);

        return editFolderfragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = getArguments().getInt("content");
        try {
            folder = MainContentActivity.folders.get(content - ApplicationConstants.numberOfStaticFolders);
            System.out.println("folder name:" + folder.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_folder, container, false);
        editName = view.findViewById(R.id.edit_folder_fragment_name);
        ((EditText)editName).setText(folder.getName());

        Button saveButton = (Button)view.findViewById(R.id.edit_folder_fragment_save_button);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                folder.setName(((EditText)editName).getText().toString());
                ((MainContentActivity)getActivity()).saveEditFolder(folder);
            }
        });

        Button deleteButton = (Button)view.findViewById(R.id.edit_folder_fragment_delete_button);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainContentActivity)getActivity()).deleteEditFolder(folder);
            }
        });

        return view;
    }
}
