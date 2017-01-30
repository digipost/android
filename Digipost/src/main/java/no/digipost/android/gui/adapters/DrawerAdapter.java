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

package no.digipost.android.gui.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.terlici.dragndroplist.DragNDropSimpleAdapter;

import java.util.ArrayList;
import java.util.Map;

import no.digipost.android.R;
import no.digipost.android.constants.ApplicationConstants;
import no.digipost.android.gui.MainContentActivity;
import no.digipost.android.model.Folder;

public class DrawerAdapter extends DragNDropSimpleAdapter {
    protected Context context;
    private TextView linkName;
    private TextView unreadView;
    private ImageView handlerImage;
    int foldersStart;
    int foldersEnd;
    private View line;
    private int unreadLetters;
    private ArrayList<Folder> folders;
    private ArrayList<String> content;

    public DrawerAdapter(final Context context, ArrayList<Map<String, Object>> map, ArrayList<String> content, ArrayList<Folder> folders, final int unreadLetters) {
        super(context, map, R.layout.drawer_list_item, new String[]{"drawer_link_name"}, new int[]{R.id.drawer_link_name}, R.id.handler);

        this.context = context;
        this.unreadLetters = unreadLetters;
        this.folders = folders;
        this.content = content;

        setFolderStartAndStop();
    }

    public View getView(final int position, final View view, final ViewGroup group) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View row = inflater.inflate(R.layout.drawer_list_item, group, false);
        this.linkName = (TextView) row.findViewById(R.id.drawer_link_name);
        this.unreadView = (TextView) row.findViewById(R.id.drawer_link_unread);
        this.line = row.findViewById(R.id.drawer_line);
        this.handlerImage = (ImageView) row.findViewById(R.id.handler_image);
        setupLinkView(position, row);

        return row;
    }

    private void updateUnreadView() {
        unreadView.setText("" + unreadLetters);
        unreadView.setVisibility(View.VISIBLE);
    }

    private void setupLinkView(int position, View row) {
        linkName.setText(content.get(position));

        Float opacity = 1.0f;
        if (MainContentActivity.editDrawerMode) {
            opacity = 0.2f;
        }

        setFolderStartAndStop();
        if (position < foldersStart) {
            row.setAlpha(opacity);
            if (position == ApplicationConstants.MAILBOX) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.inbox_32, 0, 0, 0);
                updateUnreadView();

            } else if (position == ApplicationConstants.RECEIPTS) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.tag_32, 0, 0, 0);

            } else if (position < foldersStart
                    && content.get(position).equals(context.getResources().getString(R.string.drawer_my_folders))) {
                row.setAlpha(1.0f);
                drawLabel();
            }
        } else if (position >= foldersStart && position < foldersEnd) {
            CharSequence type = context.getResources().getString(R.string.icon_folder);

            if (folders != null) {
                int folderIndex = position - ApplicationConstants.numberOfStaticFolders;
                if (folderIndex >= 0) {
                    type = folders.get(folderIndex).getIcon();
                }
            }

            linkName.setCompoundDrawablesWithIntrinsicBounds(getFolderIcon(type), 0, 0, 0);

            if (MainContentActivity.editDrawerMode) {
                handlerImage.setVisibility(View.VISIBLE);
            } else {
                handlerImage.setVisibility(View.GONE);
            }

        } else if (position >= foldersEnd) {

            if (folders != null && content.get(position).equals(context.getResources().getString(R.string.drawer_create_folder))) {
                linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.new_folder_32, 0, 0, 0);
                row.setAlpha(1.0f);
            } else {
                setIconBelowFolders(content.get(position));
                row.setAlpha(opacity);
            }
        }
    }

    private void setFolderStartAndStop() {
        foldersStart = ApplicationConstants.numberOfStaticFolders;
        foldersEnd = foldersStart;

        if (folders != null) {
            foldersEnd += folders.size();
        }
    }

    private int setIconBelowFolders(String name) {

        if (name.equals(context.getResources().getString(R.string.drawer_my_account))) {
            drawLabel();
        } else if (name.equals(context.getResources().getString(R.string.drawer_change_account))) {
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.account_32px, 0, 0, 0);
        } else if (name.equals(context.getResources().getString(R.string.drawer_settings))) {
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.admin_32px, 0, 0, 0);
        } else if(name.equals(context.getResources().getString(R.string.drawer_invoice_overview))){
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.usd_32, 0, 0, 0);
        } if (name.equals(context.getResources().getString(R.string.drawer_help))) {
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.help_32px, 0, 0, 0);
        } else if (name.equals(context.getResources().getString(R.string.drawer_logout))) {
            linkName.setCompoundDrawablesWithIntrinsicBounds(R.drawable.logout_32px, 0, 0, 0);
        }
        return 0;
    }

    private int getFolderIcon(CharSequence type) {
        if (type.equals(context.getString(R.string.icon_paper))) {
            return R.drawable.file_32;
        } else if (type.equals(context.getString(R.string.icon_tags))) {
            return R.drawable.tags_32;
        } else if (type.equals(context.getString(R.string.icon_letter))) {
            return R.drawable.envelope_32;
        } else if (type.equals(context.getString(R.string.icon_heart))) {
            return R.drawable.heart_32;
        } else if (type.equals(context.getString(R.string.icon_trophy))) {
            return R.drawable.trophy_32;
        } else if (type.equals(context.getString(R.string.icon_box))) {
            return R.drawable.archive_32;
        } else if (type.equals(context.getString(R.string.icon_home))) {
            return R.drawable.home_32;
        } else if (type.equals(context.getString(R.string.icon_star))) {
            return R.drawable.star_32;
        } else if (type.equals(context.getString(R.string.icon_suitcase))) {
            return R.drawable.suitcase_32;
        } else if (type.equals(context.getString(R.string.icon_camera))) {
            return R.drawable.camera32;
        } else if (type.equals(context.getString(R.string.icon_money))) {
            return R.drawable.usd_32;
        } else if (type.equals(context.getString(R.string.icon_beer))) {
            return R.drawable.beer_32;
        } else {
            return R.drawable.folder_32;
        }
    }

    private void drawLabel() {
        linkName.setTextColor(context.getResources().getColor(R.color.main_drawer_grey_text));
        linkName.setTextSize(16);
        linkName.setGravity(Gravity.BOTTOM);
        linkName.setTypeface(null, Typeface.BOLD);
        linkName.setPadding(0, 0, 0, 5);
        line.setVisibility(View.GONE);
    }

    @Override
    public boolean isEnabled(int position) {
        if (MainContentActivity.editDrawerMode) {
            return position >= foldersStart && position <= foldersEnd;
        } else {
            if (position != ApplicationConstants.FOLDERS_LABEL) {
                if (position != MainContentActivity.numberOfFolders + ApplicationConstants.numberOfStaticFolders + 1) {
                    return true;
                }
            }
        }
        return false;
    }
}
