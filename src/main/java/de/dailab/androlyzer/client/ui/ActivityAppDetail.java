package de.dailab.androlyzer.client.ui;

import com.actionbarsherlock.view.MenuItem;
import de.dailab.androlyzer.client.R;
import de.dailab.androlyzer.client.util.Androlyzer;
import de.dailab.androlyzer.client.util.StyledActivity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class ActivityAppDetail extends StyledActivity {
	FragmentAppDetail mAppDetail;

	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_appview);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		FragmentManager fm = getSupportFragmentManager();
		mAppDetail = (FragmentAppDetail) fm
				.findFragmentById(R.id.fragment_app_detail);
		mAppDetail.showReport((String) getIntent().getCharSequenceExtra("packageName"),
				(String) getIntent().getCharSequenceExtra("version"));
/*
		String url = (String) getIntent().getCharSequenceExtra(Androlyzer.EXTRA_URL);
		String appName = (String) getIntent().getCharSequenceExtra(Androlyzer.EXTRA_APP_NAME);

		if (url != null && appName != null) {
			mAppDetail.loadUrl(url, appName);
		} else {
			mAppDetail.loadUrl(Androlyzer.DEFAULT_WEBVIEW_URL, null);
		}*/
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent intent = new Intent(this, ActivityHome.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

}
