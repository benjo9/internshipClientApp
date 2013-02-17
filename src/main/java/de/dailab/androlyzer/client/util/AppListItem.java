package de.dailab.androlyzer.client.util;

import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;

public class AppListItem {

	public static final String VERSION_MATCH_EQUAL = "equal";
	public static final String VERSION_MATCH_SERVERLAG = "serverlag";
	public static final String VERSION_MATCH_CLIENTLAG = "clientlag";
	
	public Drawable appIcon;
	public String appName;
	public String packageName;
	public String versionMatch;
	public String version;
	public int versionCode;
	public String apkDir;
	/*	public int suspCount;
	public int confCount;
	public int leakCount;
	public String status;
*/
	public AppListItem(JSONObject resIt) {
	
		try {
			this.appName = resIt.getString("title");
			this.packageName = resIt.getString("packagename");
			this.versionCode = resIt.getInt("versioncode");
			this.versionMatch = resIt.optString("versionmatch", "equal");
			this.version = resIt.getString("version");
			this.apkDir = resIt.optString("apkDir");
			/*
			this.suspCount = resIt.optInt("suspicious",0);
			this.confCount = resIt.optInt("confidential_sources",0);
			this.leakCount = resIt.optInt("leaks", 0);
			*/
			//this.status = resIt.getString("status");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
