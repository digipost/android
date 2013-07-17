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

import java.util.ArrayList;
import java.util.Collection;

import no.digipost.android.R;
import no.digipost.android.constants.ApiConstants;
import no.digipost.android.gui.content.SettingsActivity;
import no.digipost.android.model.Letter;
import no.digipost.android.utilities.DataFormatUtilities;
import no.digipost.android.utilities.SharedPreferencesUtilities;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;

public class LetterArrayAdapter extends ContentArrayAdapter<Letter> {

	public LetterArrayAdapter(final Context context, final int resource, final View.OnClickListener onClickListener) {
		super(context, resource, new ArrayList<Letter>(), onClickListener);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = super.getView(position, convertView, parent);

		Letter letter = super.filtered.get(position);

		super.title.setText(letter.getSubject());
		super.subTitle.setText(letter.getCreatorName());
		super.metaTop.setText(DataFormatUtilities.getFormattedDate(letter.getCreated()));
		super.metaMiddle.setText(DataFormatUtilities.getFormattedFileSize(Long.parseLong(letter.getFileSize())));

		if (!letter.getRead().equals("true")) {
			row.setBackgroundResource(R.drawable.content_list_item_unread);
			super.setTitleAndSubTitleBold();
		}

		super.setFilterTextColor();

		setMetaBottom(letter);

		return row;
	}

	@Override
	public void replaceAll(Collection<? extends Letter> collection) {
		SharedPreferences sharedPreferences = SharedPreferencesUtilities.getSharedPreferences(context);
		boolean showLettersWithTwoFactor = sharedPreferences.getBoolean(SettingsActivity.KEY_PREF_SHOW_BANK_ID_DOCUMENTS, true);

		if (!showLettersWithTwoFactor) {
			ArrayList<Letter> letters = new ArrayList<Letter>();

			for (Letter letter : collection) {
				if (!letter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
					letters.add(letter);
				}
			}

			super.replaceAll(letters);
		} else {
			super.replaceAll(collection);
		}
	}

	private void setMetaBottom(Letter letter) {
		if (letter.getAttachment().size() > 1) {
			setMetaBottomDrawable(R.drawable.paper_clip_dark);
		} else if (letter.getAuthenticationLevel().equals(ApiConstants.AUTHENTICATION_LEVEL_TWO_FACTOR)) {
			setMetaBottomDrawable(R.drawable.lock_dark);
		} else if (letter.getOpeningReceiptUri() != null) {
			setMetaBottomDrawable(R.drawable.exclamation_sign_dark);
		}
	}

	private void setMetaBottomDrawable(int resId) {
		super.metaBottom.setImageDrawable(context.getResources().getDrawable(resId));
		super.metaBottom.setVisibility(View.VISIBLE);
	}

	@Override
	public Filter getFilter() {
		return (super.contentFilter != null) ? super.contentFilter : new LetterFilter();
	}

	private class LetterFilter extends Filter {
		@Override
		protected FilterResults performFiltering(final CharSequence constraint) {
			FilterResults results = new FilterResults();
			ArrayList<Letter> i = new ArrayList<Letter>();

			LetterArrayAdapter.super.titleFilterText = null;
			LetterArrayAdapter.super.subTitleFilterText = null;
			LetterArrayAdapter.super.metaTopFilterText = null;

			if ((constraint != null) && (constraint.toString().length() > 0)) {
				String constraintLowerCase = constraint.toString().toLowerCase();

				for (Letter l : LetterArrayAdapter.super.objects) {
					boolean addLetter = false;

					if (l.getSubject().toLowerCase().contains(constraintLowerCase)) {
						LetterArrayAdapter.super.titleFilterText = constraint.toString();
						addLetter = true;
					}

					if (l.getCreatorName().toLowerCase().contains(constraintLowerCase)) {
						LetterArrayAdapter.super.subTitleFilterText = constraint.toString();
						addLetter = true;
					}

					if (DataFormatUtilities.getFormattedDate(l.getCreated()).toLowerCase().contains(constraintLowerCase)) {
						LetterArrayAdapter.super.metaTopFilterText = constraint.toString();
						addLetter = true;
					}

					if (addLetter) {
						i.add(l);
					}
				}

				results.values = i;
				results.count = i.size();
			} else {

				synchronized (LetterArrayAdapter.super.objects) {
					results.values = LetterArrayAdapter.super.objects;
					results.count = LetterArrayAdapter.super.objects.size();
				}
			}

			return results;
		}

		@SuppressWarnings("unchecked")
		@Override
		protected void publishResults(final CharSequence constraint, final FilterResults results) {
			filtered = (ArrayList<Letter>) results.values;
			notifyDataSetChanged();
		}
	}
}
