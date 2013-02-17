package de.dailab.androlyzer.client.ui;


import de.dailab.androlyzer.client.R;
import de.dailab.androlyzer.client.util.Androlyzer;
import de.dailab.androlyzer.client.util.AppListItem;
import de.dailab.androlyzer.client.util.StyledActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;

public class ActivityHome extends StyledActivity implements
		FragmentAppList.OnAppSelectedListener {
	private FragmentAppDetail mAppDetail;
	private FragmentAppList mAppList;
	private FragmentManager fm;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
		fm = getSupportFragmentManager();
		mAppList = (FragmentAppList) fm.findFragmentById(R.id.fragment_app_list);
		//fixBackground();
		mAppDetail = (FragmentAppDetail) fm
				.findFragmentById(R.id.fragment_app_detail);
		mAppList.getContext(ActivityHome.this);
/*		if (mAppDetail != null) {
			mAppDetail.loadUrl(Androlyzer.DEFAULT_WEBVIEW_URL, null);
		}*/
	}

	private void fixBackground() {
		BitmapDrawable bg = (BitmapDrawable) getResources().getDrawable(
				R.drawable.bg);
		bg.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
		mAppList.getListView().setBackgroundDrawable(bg);
	}

	@Override
	public void onAppSelected(AppListItem item) {
		super.changeActionBarTitle(item.appName+" "+item.version);
		if (mAppDetail != null) {
			mAppDetail.showReport(item.packageName, item.version);
			//mAppDetail.loadUrl(appUrl, appName);
		} else {
			Intent i = new Intent(ActivityHome.this, ActivityAppDetail.class);
			i.putExtra("packageName",  item.packageName);
			i.putExtra("version", item.version);
			startActivity(i);
		}
	}
}
