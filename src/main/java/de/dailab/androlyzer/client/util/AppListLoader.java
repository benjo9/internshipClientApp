package de.dailab.androlyzer.client.util;

import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

public class AppListLoader extends AsyncTaskLoader<JSONArray> {
	final PackageManager mPm;
	private JSONArray mApps;
	private String mSecToken;

	public AppListLoader(Context context) {
		this(context, null);
	}

	public AppListLoader(Context context, String secToken) {
		super(context);
		mSecToken = secToken;
		mPm = getContext().getPackageManager();

		if (mSecToken != null) {
			Log.d(Androlyzer.TAG, "AppListLoader constr w/secToken");
		} else {
			Log.d(Androlyzer.TAG, "AppListLoader constr");
		}
	}

	@Override
	protected void onStartLoading() {
		Log.d(Androlyzer.TAG, "onStartLoading");
		if (mApps != null) {
			Log.d(Androlyzer.TAG, "mApps not null!");
			deliverResult(mApps);
		} else {
			forceLoad();
		}
	}
	/*
	@Override
	public JSONArray loadInBackground() {
		Log.d(Androlyzer.TAG, "loadInBackground");
		JSONArray localApps = getInstalledApps();
		
		JSONArray response;
		if (mSecToken != null) {
			response = Androlyzer.queryServer(this.getContext(), localApps,
					mSecToken);
		} else {
			response = Androlyzer.queryServer(this.getContext(), localApps);
		}
		Log.d(Androlyzer.TAG, "finished loading");
		if (response == null) {
			return new JSONArray();
		} else {
			List<JSONObject> list = Androlyzer.jsonArrayToList(response);
			// sort apps by badness
			Collections.sort(list, APP_COMPARATOR);
			JSONArray result = Androlyzer.listToJsonArray(list);
			Log.d("HHHHHEEEEEEEEEEEEEEEEYYYYYYYYYYYYYYYYYY!!!!",result.toString());
			return Androlyzer.listToJsonArray(list);
		}
		
		return localApps;
	}*/

	@Override
	public JSONArray loadInBackground() {
		final List<ApplicationInfo> appList = mPm
				.getInstalledApplications(PackageManager.GET_META_DATA);

		JSONArray simpleAppList = new JSONArray();

		for (ApplicationInfo ai : appList) {

			PackageInfo pi;
			try {
				if((ai.flags & ApplicationInfo.FLAG_SYSTEM)!=1){
					pi = mPm.getPackageInfo(ai.packageName, 0);

					JSONObject curApp = new JSONObject();
					curApp.put("packagename", pi.packageName);
					curApp.put("versioncode", pi.versionCode);
					curApp.put("title", ai.loadLabel(mPm).toString());
					curApp.put("version", pi.versionName);
					curApp.put("apkDir", ai.publicSourceDir);
					simpleAppList.put(curApp);
				}

			} catch (NameNotFoundException e) {
				Log.e(Androlyzer.TAG, e.getMessage(), e);
			} catch (JSONException e) {
				Log.e(Androlyzer.TAG, e.getMessage(), e);
			}

		}
		return simpleAppList;

	}
/*
	public static final Comparator<JSONObject> APP_COMPARATOR = new Comparator<JSONObject>() {

		@Override
		public int compare(JSONObject object1, JSONObject object2) {
			if (object1.optInt("leaks", 0) != object2.optInt("leaks", 0)) {
				return object1.optInt("leaks", 0) > object2.optInt("leaks", 0) ? -1
						: 1;
			} else if (object1.optInt("confidential_sources", 0) != object2
					.optInt("confidential_sources", 0)) {
				return object1.optInt("confidential_sources", 0) > object2
						.optInt("confidential_sources", 0) ? -1 : 1;
			} else if (object1.optInt("suspicious", 0) != object2.optInt(
					"suspicious", 0)) {
				return object1.optInt("suspicious", 0) > object2.optInt(
						"suspicious", 0) ? -1 : 1;
			} else {
				return 0;
			}
		}
	};
*/
}
