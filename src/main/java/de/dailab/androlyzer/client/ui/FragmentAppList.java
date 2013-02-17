package de.dailab.androlyzer.client.ui;

import java.net.MalformedURLException;
import java.net.URL;
import android.app.AlertDialog;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import org.json.JSONArray;
import java.io.IOException;
import android.os.AsyncTask;
import com.actionbarsherlock.app.SherlockListFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import de.dailab.androlyzer.client.R;
import de.dailab.androlyzer.client.util.Androlyzer;
import de.dailab.androlyzer.client.util.AppListAdapter;
import de.dailab.androlyzer.client.util.AppListItem;
import de.dailab.androlyzer.client.util.AppListLoader;

import brut.androlib.Androlib;
import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;

import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import java.io.File;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import de.dailab.androlyzer.client.analyzer.Worker;

public class FragmentAppList extends SherlockListFragment implements
		LoaderManager.LoaderCallbacks<JSONArray> {
	private AlertDialog.Builder customDialog;
	private static final int APP_LOADER = 0;
	private String mHost, mScheme;
	private int mPort;
	OnAppSelectedListener mListener;
	AppListAdapter mAdapter;
	Context mCtx;
	private static final int READ = 0;
	private static final String SEC_TOKEN_KEY = "secToken";
	private Activity activity;
	private boolean isDone;

	public void getContext(Activity activity){
		this.activity = activity;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Log.d(Androlyzer.TAG, "onActivityCreated");
		setHasOptionsMenu(true);
		mAdapter = new AppListAdapter(getActivity());
		setListAdapter(mAdapter);
		getLoaderManager().initLoader(APP_LOADER, null, this);
		mCtx = ((SherlockFragmentActivity) getActivity()).getApplicationContext();
		isDone = true;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_app_list,
				container);
		return vg;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		SharedPreferences sp = PreferenceManager
				.getDefaultSharedPreferences(this.getActivity());

		final String serviceURLStr = sp.getString("serviceURL",
				Androlyzer.serverURL);

		try {
			URL serviceURL = new URL(serviceURLStr);
			mHost = serviceURL.getHost();
			mPort = serviceURL.getPort();
			mScheme = serviceURL.getProtocol();
		} catch (MalformedURLException e) {
			Log.e(Androlyzer.TAG, e.getMessage(), e);
		}

		try {
			mListener = (OnAppSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(
					activity.toString()
							+ "must implement AppListFragment.OnAppListSelectedListener");
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		AppListItem item = mAdapter.getAppListItem(position);

		for (int i = 0; i < l.getCount(); i++) {
			View ve = l.getChildAt(i);
			if (ve != null) {
				View vi = ve.findViewById(R.id.app_list_item_active_indicator);
				if (vi != null) {
					vi.setVisibility(View.INVISIBLE);
				}
			}
		}
		String packname = item.packageName;
		int vercode = item.versionCode;

		if(!(new File(Environment.getExternalStorageDirectory()
				+ "/Android/data/" + mCtx.getPackageName()
				+ "/reports/"+item.packageName+"-"+
				item.version+".json")).exists()){
			if(!isDone)
				return;
			openCustomDialog(position);
		}
		else{
			View indicator = v.findViewById(R.id.app_list_item_active_indicator);
			if (indicator != null) {
				indicator.setVisibility(View.VISIBLE);
			}
			
			mListener.onAppSelected(item);
			Toast.makeText(mCtx, "Report is being displayed", Toast.LENGTH_LONG).show();
		}
		
		
	}
	
	public void openCustomDialog(final int position){
		customDialog 
		= new AlertDialog.Builder(activity);
		customDialog.setTitle("This may take time!\nDo you want to proceed?");

		LayoutInflater layoutInflater 
		= (LayoutInflater)mCtx.getSystemService(mCtx.LAYOUT_INFLATER_SERVICE);
		View view=layoutInflater.inflate(R.layout.listalert,null);
		

		customDialog.setNegativeButton("Back", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		customDialog.setPositiveButton("Decode and Analyze", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				isDone = false;
				new DecodeTask(position).execute();
				dialog.cancel();
			}
		});

		customDialog.setView(view);

		customDialog.show();
	}
	
	private class DecodeTask extends AsyncTask<Void, Integer, Void>
	{
		private boolean successful = false;
		private int position = 0;
		private long ts = 0;
		final int NUM_PHASES = 4;
		//ProgressBar progressBar;
		AppListItem item;
		SherlockFragmentActivity activity;

		//Before running code in separate thread
		public DecodeTask(int pos){
			position = pos;
			item = mAdapter.getAppListItem(pos);
			activity = (SherlockFragmentActivity) getActivity();
			Log.d("DecodeTask", "Decode Task Started!!!");
		}
		@Override
		protected void onPreExecute()
		{
			//progressDialog = new ProgressDialog(PackagesListActivity.this);
			//progressDialog.setMessage("Decoding started, please wait...");
			//progressDialog.show();
			//progressBar = (ProgressBar)findViewById(R.id.progressbar_Horizontal);
			if (activity != null) {
				activity.setSupportProgressBarVisibility(true);
			}

		}

		//The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params)
		{

			//String path = thirdPartyAppList.get(position).publicSourceDir;
			File apkFile = new File(item.apkDir);
			ApkDecoder dedex = new ApkDecoder(apkFile);

			try {
				File outFile = new File(Environment.getExternalStorageDirectory()
						+ "/Android/data/" + mCtx.getPackageName()
						+ "/decoded/");
				
				dedex.setOutDir(outFile);
				
				activity.setSupportProgress(0);
				dedex.decode();
				activity.setSupportProgress((int) ((1 / (float) NUM_PHASES) * 100));
				Log.d("DecodeTask doInBG", "dedex.decode() finished!!!");
				dedex.decode_src();
				activity.setSupportProgress((int) ((2 / (float) NUM_PHASES) * 100));
				dedex.decode_res();
				activity.setSupportProgress((int) ((3 / (float) NUM_PHASES) * 100));
				dedex.decode_finish();
				activity.setSupportProgress((int) ((4 / (float) NUM_PHASES) * 100));
				
				successful = true;
				Log.d("DecodeTask", "Decoding is finished!!!");
				Worker worker = new Worker(Environment.getExternalStorageDirectory()
						+ "/Android/Data/" + mCtx.getPackageName()
						+ "/decoded/",
						item.packageName, item.version, item.versionCode);
				
				worker.work();

			} catch (AndrolibException e) {

				successful = false;
			}

			return null;
		}

		//Update the progress
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			//progressBar.setProgress(values[0]);
			
		}

		//after executing the code in the thread
		@Override
		protected void onPostExecute(Void result)
		{
			//progressDialog.dismiss();  
			activity.setSupportProgressBarVisibility(false);
			isDone = true;
			/*if(successful){ Toast.makeText(mCtx, "Decoding is finished after "
					+ ((System.currentTimeMillis() - ts) / 1000) + "s", Toast.LENGTH_LONG).show();*/
			
			//Toast.makeText(mCtx, "Started scanning...", Toast.LENGTH_SHORT).show();
			mListener.onAppSelected(item);
			Toast.makeText(mCtx, "Decoding and Scanning finished!", Toast.LENGTH_SHORT).show();
			
			//else Toast.makeText(mCtx, "Decoding Error!", Toast.LENGTH_SHORT).show();

		}


	}
	
	
	
	
	@Override
	public final void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		switch (requestCode) {
		case READ:

			if (resultCode == Activity.RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				String format = data.getStringExtra("SCAN_RESULT_FORMAT");

				Log.i(Androlyzer.TAG, contents + " " + format);

				String checkedContent = checkQRCodeResult(contents);

				if (checkedContent != null) {
					Bundle b = new Bundle();
					b.putString(SEC_TOKEN_KEY, checkedContent);
					getLoaderManager().restartLoader(APP_LOADER, b, this);
				} else {
					Toast.makeText(this.getActivity(),
							"No valid QR-code found", Toast.LENGTH_LONG).show();
					return;
				}
			} else if (resultCode == Activity.RESULT_CANCELED) {
				Toast.makeText(this.getActivity(),
						"No QR-code found, please try again", Toast.LENGTH_LONG)
						.show();
			}
			break;
		default:
			break;

		}
	}

	private String checkQRCodeResult(final String contents) {
		// TODO do a serious sanity check here

		Uri result = Uri.parse(contents);
		if (result.getScheme().equalsIgnoreCase("androlyzer")) {
			if (result.getQueryParameter("sr") != null) {
				return result.getQueryParameter("sr");
			}
		}
		return null;
	}

	public final void readQRCode() {
		// TODO check for QR-reader to be installed
		Intent intent = new Intent("com.google.zxing.client.android.SCAN");
		intent.setPackage("com.google.zxing.client.android");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
		intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
		startActivityForResult(intent, READ);
	}

	public interface OnAppSelectedListener {
		public void onAppSelected(AppListItem item);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.app_list, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean consumed = true;
		switch (item.getItemId()) {
		case R.id.menu_qrsync:
			readQRCode();
			break;
		case R.id.menu_refresh:
			getLoaderManager().restartLoader(APP_LOADER, null, this);
			break;
		case R.id.menu_settings:
			Intent intent = new Intent(getActivity(), ActivityPreference.class);
			startActivity(intent);
			break;
		default:
			break;
		}
		return consumed;
	}

	@Override
	public Loader<JSONArray> onCreateLoader(int loaderID, Bundle bundle) {
		Log.d(Androlyzer.TAG, "onCreateLoader");

		if (bundle != null && bundle.containsKey(SEC_TOKEN_KEY)) {
			return new AppListLoader(getActivity(),
					bundle.getString(SEC_TOKEN_KEY));
		} else {
			return new AppListLoader(getActivity());
		}
	}

	@Override
	public void onLoadFinished(Loader<JSONArray> loader, JSONArray data) {
		Log.d(Androlyzer.TAG, "onLoadFinished");
		mAdapter.setData(data);
	}

	@Override
	public void onLoaderReset(Loader<JSONArray> arg0) {
		Log.d(Androlyzer.TAG, "onLoaderReset");
		mAdapter.setData(null);
	}
}
