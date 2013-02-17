package de.dailab.androlyzer.client.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.dailab.androlyzer.client.R;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class AppListAdapter extends BaseAdapter {

	private Context mContext;
	private JSONArray mResults;
	private LayoutInflater mInflater;
	private PackageManager mPackMan;

	public AppListAdapter(final Context context) {
		this.mContext = context;
		this.mPackMan = context.getPackageManager();
		this.mInflater = LayoutInflater.from(mContext);
	}
	
	public void setData(JSONArray results){
		this.mResults = results;
		this.notifyDataSetChanged();
	}
	

	@Override
	public final View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final AppListViewHolder holder;
		View view = convertView;

		if (view == null || convertView.getTag() == null) {
			// || ((ViewHolder) convertView.getTag()).position != position) {
			view = mInflater.inflate(R.layout.app_list_item, null);

			holder = new AppListViewHolder();
			holder.appIcon = (ImageView) view
					.findViewById(R.id.app_list_item_icon);
			holder.appName = (TextView) view
					.findViewById(R.id.app_list_item_packagename);
			holder.version = (TextView) view
					.findViewById(R.id.app_list_item_versioncode);

		} else {
			holder = (AppListViewHolder) view.getTag();
		}

		JSONObject appJson;
		try {
			appJson = mResults.getJSONObject(position);

			// get icon
			try {
				Drawable appIcon = mPackMan.getApplicationIcon(appJson
						.getString("packagename"));
				holder.appIcon.setImageDrawable(appIcon);
				holder.appIcon.setVisibility(View.VISIBLE);
			} catch (NameNotFoundException e) {
				Log.e(Androlyzer.TAG, e.getMessage(), e);
			}

			holder.appName.setText(appJson.optString("title", "n/a"));
			holder.appName.setVisibility(View.VISIBLE);

			holder.version.setText(appJson.optString("version", "n/a"));
			holder.version.setVisibility(View.VISIBLE);


		} catch (JSONException e) {
			Log.e(Androlyzer.TAG, e.getMessage(), e);
		}

		return view;
	}

	public final AppListItem getAppListItem(final int position) {
		try {
			JSONObject resIt = mResults.getJSONObject(position);
			AppListItem newRes = new AppListItem(resIt);
			return newRes;
		} catch (JSONException e) {
			Log.e(Androlyzer.TAG, e.getMessage(), e);
		}
		return null;
	}

	public final int getCount() {
		if (this.mResults != null) {
			return this.mResults.length();
		} else {
			return 0;
		}
	}

	public final Object getItem(final int position) {
		try {
			return mResults.get(position);
		} catch (JSONException e) {
			Log.e(Androlyzer.TAG, e.getMessage(), e);
		}
		return null;
	}

	public final long getItemId(final int position) {
		return position;
	}
}
