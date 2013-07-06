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
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;

import no.digipost.android.R;
import no.digipost.android.model.Letter;

public abstract class ContentArrayAdapter<T> extends ArrayAdapter<T> {
    public static final String TEXT_HIGHLIGHT_COLOR = "#EBEB86";

    protected Context context;
    protected ArrayList<T> objects;
    protected ArrayList<T> filtered;

    protected TextView title;
    protected TextView subTitle;
    protected TextView metaTop;
    protected TextView metaMiddle;
    protected ImageView metaBottom;

    protected Filter contentFilter;

    protected String titleFilterText;
    protected String subTitleFilterText;
    protected String metaTopFilterText;

    public ContentArrayAdapter(final Context context, final int resource, final ArrayList<T> objects) {
        super(context, resource, objects);

        this.context = context;
        this.filtered = objects;
        this.objects = this.filtered;

        this.titleFilterText = null;
        this.subTitleFilterText = null;
        this.metaTopFilterText = null;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View row = inflater.inflate(R.layout.content_list_item, parent, false);

        this.title = (TextView) row.findViewById(R.id.content_title);
        this.subTitle = (TextView) row.findViewById(R.id.content_subTitle);
        this.metaTop = (TextView) row.findViewById(R.id.content_meta_top);
        this.metaMiddle = (TextView) row.findViewById(R.id.content_meta_middle);
        this.metaBottom = (ImageView) row.findViewById(R.id.content_meta_bottom);

        return row;
    }

    public void replaceAll(Collection<? extends T> collection) {
        this.filtered.clear();
        this.filtered.addAll(collection);
        this.objects = this.filtered;
        notifyDataSetChanged();
    }

    @Override
    public void add(final T object) {
        filtered.add(object);
        notifyDataSetChanged();
    }

    @Override
    public T getItem(final int position) {
        return filtered.get(position);
    }

    @Override
    public int getCount() {
        return filtered.size();
    }

    @Override
    public void remove(final T object) {
        filtered.remove(object);
        notifyDataSetChanged();
    }

    @Override
    public void addAll(Collection<? extends T> collection) {
        filtered.addAll(collection);
        notifyDataSetChanged();
    }

    @Override
    public abstract Filter getFilter();

    public void setFilterTextColor() {
        if (titleFilterText != null) {
            setTextViewFilterTextColor(title, titleFilterText);
        }

        if (subTitleFilterText != null) {
            setTextViewFilterTextColor(subTitle, subTitleFilterText);
        }

        if (metaTopFilterText != null) {
            setTextViewFilterTextColor(metaTop, metaTopFilterText);
        }
    }

    private void setTextViewFilterTextColor(final TextView v, final String filterText) {
        int l = filterText.length();
        int i = v.getText().toString().toLowerCase().indexOf(filterText.toLowerCase());

        if (i < 0) {
            return;
        }

        Spannable sb = new SpannableString(v.getText().toString());
        sb.setSpan(new BackgroundColorSpan(Color.parseColor(TEXT_HIGHLIGHT_COLOR)), i, i + l, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        v.setText(sb);
    }
}
