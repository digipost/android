package no.digipost.android.gui.content;

import no.digipost.android.R;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class PersonalInformationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_personal_information);

		getActionBar().setHomeButtonEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
			return true;
		}

		return onOptionsItemSelected(item);
	}
}
