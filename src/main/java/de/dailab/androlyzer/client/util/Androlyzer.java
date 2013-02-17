package de.dailab.androlyzer.client.util;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import android.util.Log;

public class Androlyzer {
	private static final String PREF_SERVICE_PASSWD = "service_passwd";
	private static final String PREF_SERVICE_USER = "service_user";
	private static final String PREF_SERVICE_AUTH = "service_auth";
	public static final String TAG = "ANDROLYZER";
	public static final String PREF_ID = "phone_random_id";
	public static final String DEFAULT_WEBVIEW_URL = "about:blank";
	public static final String EXTRA_URL = "WEBVIEW_URL";
	public static final String EXTRA_APP_NAME = "WEBVIEW_APP_NAME";
	public static String serverURL = "https://androlyzer.com/api/mobile";

	static String getPhoneID(Context ctx) {
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(ctx);
		String phoneID;
		if (!sp.contains(PREF_ID)) {
			phoneID = generatePhoneID(sp);
		} else {
			phoneID = sp.getString(PREF_ID, null);
			if (phoneID == null) {
				phoneID = generatePhoneID(sp);
			}
		}
		return phoneID;
	}

	private static String generatePhoneID(SharedPreferences sp) {
		String phoneID;
		Editor e = sp.edit();
		phoneID = UUID.randomUUID().toString();
		e.putString(PREF_ID, phoneID);
		e.commit();
		return phoneID;
	}

	static JSONArray queryServer(final Context ctx, final JSONArray appList) {
		return queryServer(ctx, appList, null);
	}

	static JSONArray queryServer(final Context ctx, final JSONArray appList,
			final String secToken) {
		JSONArray results = null;
		try {
			SharedPreferences sp = PreferenceManager
					.getDefaultSharedPreferences(ctx);
			URL serviceURL = new URL(sp.getString("serviceURL", serverURL));

			DefaultHttpClient httpClient = new DefaultHttpClient();
			ConnectionKeepAliveStrategy strategy = new ConnectionKeepAliveStrategy() {

				public long getKeepAliveDuration(final HttpResponse response,
						final HttpContext context) {
					return 2000;
				}
			};

			httpClient.setKeepAliveStrategy(strategy);
			httpClient.setReuseStrategy(new NoConnectionReuseStrategy());
			httpClient.getParams().setBooleanParameter(
					ClientPNames.REJECT_RELATIVE_REDIRECT, false);
			httpClient.getParams().setBooleanParameter(
					ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

			if (sp.getBoolean(PREF_SERVICE_AUTH, false)) {

				String username = sp.getString(PREF_SERVICE_USER, "user");
				String password = sp.getString(PREF_SERVICE_PASSWD, "secret");
				AuthScope authScope = new AuthScope(serviceURL.getHost(),
						serviceURL.getPort() == -1 ? serviceURL
								.getDefaultPort() : serviceURL.getPort());
				UsernamePasswordCredentials creds = new UsernamePasswordCredentials(
						username, password);

				httpClient.getCredentialsProvider().setCredentials(authScope,
						creds);
			}

			HttpPost req = new HttpPost(serviceURL + "/");
			req.setHeader("Content-Type", "application/json");

			JSONObject jsonRequest = new JSONObject();

			if (secToken != null) {
				jsonRequest.put("secToken", secToken);
			}

			jsonRequest.put("deviceId", getPhoneID(ctx));
			jsonRequest.put("appList", appList);

			HttpEntity entity = new StringEntity(jsonRequest.toString());
			req.setEntity(entity);

			HttpResponse resp = httpClient.execute(req);

			int statusCode = resp.getStatusLine().getStatusCode();

			if (statusCode >= 400) {
				return null;
			}

			HttpEntity response = resp.getEntity();

			String content = EntityUtils.toString(response);

			// return result
			results = new JSONArray(content);

		} catch (IOException e) {
			Log.d(Androlyzer.TAG, e.getMessage());
			return null;

		} catch (JSONException e) {
			Log.d(Androlyzer.TAG, e.getMessage());
			return null;
		}
		return results;
	}

	static List<JSONObject> jsonArrayToList(JSONArray jsonArray) {
		if (jsonArray != null && jsonArray.length() > 0) {
			List<JSONObject> list = new ArrayList<JSONObject>(
					jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				try {
					list.add(jsonArray.getJSONObject(i));
				} catch (JSONException e) {
					Log.e(TAG, e.getMessage(), e);
				}
			}
			return list;
		} else {

			return new ArrayList<JSONObject>();
		}
	}

	static JSONArray listToJsonArray(List<JSONObject> list) {
		if (list != null && !list.isEmpty()) {
			JSONArray array = new JSONArray();
			for (JSONObject obj : list) {
				array.put(obj);
			}
			return array;
		} else {
			return new JSONArray();
		}
	}

}
