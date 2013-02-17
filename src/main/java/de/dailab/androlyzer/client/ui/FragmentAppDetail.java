package de.dailab.androlyzer.client.ui;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;
import de.dailab.androlyzer.client.R;
import de.dailab.androlyzer.client.util.Androlyzer;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.content.Context;

public class FragmentAppDetail extends SherlockFragment {
	private WebView mWebView;
	private View mAboutBlank;
	private String mUrl;
	private String mAppName;
	
	ArrayList<ArrayList<String>> myLists;
	private static int LIST_ITEMS = 8;
	private String reportName;
	private WebView diagram;
	private JSONObject packageTree;
	private ArrayList<ArrayAdapter<String>> adapters;
	private Context mCtx;
	
	private LayoutInflater mInflater;
	private ViewGroup mContainer;
	
	final int[] backgroundColors =
		{ Color.parseColor("#a7200f"), Color.parseColor("#ee917c"), Color.parseColor("#00987f"), Color.parseColor("#decbb3"), Color.parseColor("#fac491"), Color.parseColor("#febcda"), Color.parseColor("#713290"), Color.parseColor("#7813ba"), Color.parseColor("#91fbca") };

	final String[] titles = { "Permissions", "Features", "Used Packages", "Used Third Party Packages", "Leaks","Sources", "Suspicious",  "Package Diagram"};

	@Override
	public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
		setHasOptionsMenu(true);
		mInflater = inflater;
		mContainer = container;
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_app_detail, container);
		mAboutBlank = vg.findViewById(R.id.about_blank);
		return vg;
	}
	
	public void showReport(String packagename, String version){
		((ViewGroup)getView()).removeAllViews();
		((ViewGroup)getView()).addView(createReportView(packagename, version));
	}
	
	public View createReportView(String packagename, String version) {
		setHasOptionsMenu(true);
		reportName = "com.androlyzer.json";
		mCtx = ((SherlockFragmentActivity) getActivity()).getApplicationContext();
		HorizontalPager realViewSwitcher = new HorizontalPager(mCtx);


		myLists = new ArrayList<ArrayList<String>>();
		for(int i = 0; i< LIST_ITEMS; i++){
			myLists.add(new ArrayList<String>());
		}

		readJSONFile(packagename, version);

		adapters = new ArrayList<ArrayAdapter<String>>();
		for (int i = 0; i < LIST_ITEMS; i++){
			adapters.add(new ArrayAdapter<String>(mCtx, android.R.layout.simple_list_item_2,  android.R.id.text1, myLists.get(i)){
				@Override
        		public View getView(int position, View convertView, ViewGroup parent) {
            		View view =super.getView(position, convertView, parent);

            		TextView textView=(TextView) view.findViewById(android.R.id.text1);

            		/*YOUR CHOICE OF COLOR*/
            		textView.setTextColor(Color.parseColor("#6495ed"));

           			return view;
        			}
    			}
			);
		}

		LinearLayout firstLayout = new LinearLayout(mCtx);
		LinearLayout layout = new LinearLayout(mCtx);
		layout.setOrientation(LinearLayout.VERTICAL);
		firstLayout.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout, 0);
		getLayout(layout, 1);
		firstLayout.addView(layout);
		realViewSwitcher.addView(firstLayout);

		LinearLayout secondLayout = new LinearLayout(mCtx);
		LinearLayout layout2 = new LinearLayout(mCtx);
		secondLayout.setOrientation(LinearLayout.VERTICAL);
		layout2.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout2, 2);
		getLayout(layout2, 3);
		secondLayout.addView(layout2);
		realViewSwitcher.addView(secondLayout);

		LinearLayout thirdLayout = new LinearLayout(mCtx);
		LinearLayout layout3 = new LinearLayout(mCtx);
		thirdLayout.setOrientation(LinearLayout.VERTICAL);
		layout3.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout3, 4);
		getLayout(layout3, 5);
		getLayout(layout3, 6);
		thirdLayout.addView(layout3);
		realViewSwitcher.addView(thirdLayout);
		
		
		return realViewSwitcher;
		
	}
	
	public LinearLayout getLayout(LinearLayout myLayout, int i){



		TextView textView = new TextView(mCtx);
		textView.setText(titles[i]);
		textView.setTextSize(24);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER);
		textView.setBackgroundColor(backgroundColors[i]);
		textView.setPadding(0, 20, 0, 20);
		myLayout.addView(textView);

		if(i!=7){
			ListView listView = new ListView(mCtx);
			listView.setAdapter(adapters.get(i));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 1;
			listView.setLayoutParams(params);
			myLayout.addView(listView);

		}

		return myLayout;
	}
	
		public void readJSONFile(String packagename, String version){

		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new FileReader("/sdcard/Android/data/" +
					"de.dailab.androlyzer.client/reports/"+packagename+"-"+version+".json"));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String line = "";
		String jsonText = "";

		try {
			while ((line = bufferedReader.readLine()) != null)
			{
				jsonText += line;
			}
			bufferedReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// close the BufferedReader when we're done


		JSONObject obj = null;
		try {
			obj = new JSONObject(jsonText);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject ManifestAnalyzer = null;
		JSONObject ThirdPartyAnalyzer = null;
		JSONObject ThreatAnalyzer = null;
		JSONObject UsedPkgsAnalyzer = null;
		JSONObject PackageTree = null;

		try{
			ManifestAnalyzer = obj.getJSONObject("ManifestAnalyzer");
			ThirdPartyAnalyzer = obj.getJSONObject("ThirdPartyAnalyzer");
			ThreatAnalyzer = obj.getJSONObject("ThreatAnalyzer");
			UsedPkgsAnalyzer = obj.getJSONObject("UsedPkgsAnalyzer");
			//PackageTree = obj.getJSONObject("PackageTree");


		}
		catch(JSONException ex){
			ex.printStackTrace();
		}
		try{
			JSONArray permissionArray = ManifestAnalyzer.getJSONArray("permissions");
			for(int i=0; i< permissionArray.length(); i ++){
				myLists.get(0).add(permissionArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray featuresArray = ManifestAnalyzer.getJSONArray("used_features");
			for(int i=0; i< featuresArray.length(); i ++){
				myLists.get(1).add(featuresArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray packagesArray = UsedPkgsAnalyzer.getJSONArray("android_packages_used_by_the_application");
			for(int i=0; i< packagesArray.length(); i ++){
				myLists.get(2).add(packagesArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray thirdPartyPackagesArray = ThirdPartyAnalyzer.getJSONArray("third_party_packages");
			for(int i=0; i< thirdPartyPackagesArray.length(); i ++){
				myLists.get(3).add(thirdPartyPackagesArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray leaksArray = ThreatAnalyzer.getJSONArray("leaks");
			for(int i=0; i< leaksArray.length(); i ++){
				myLists.get(4).add(leaksArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray sourcesArray = ThreatAnalyzer.getJSONArray("confidential_sources");
			for(int i=0; i< sourcesArray.length(); i ++){
				myLists.get(5).add(sourcesArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}

		try{
			JSONArray suspiciousArray = ThreatAnalyzer.getJSONArray("suspicious");
			for(int i=0; i< suspiciousArray.length(); i ++){
				myLists.get(6).add(suspiciousArray.getString(i));
			}
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}
		/*try{
			packageTree = PackageTree.getJSONObject("ptree");
		}
		catch(JSONException ex){
			ex.printStackTrace();
		}*/

	}


	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.app_detail, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent = new Intent(android.content.Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

		// Add data to the intent, the receiving app will decide what to do with
		// it.
		intent.putExtra(Intent.EXTRA_SUBJECT, "Androlyzer");

		if (item.getItemId() == R.id.menu_share) {
			if (mUrl == null || mUrl.equals(Androlyzer.DEFAULT_WEBVIEW_URL)) {
				intent.putExtra(Intent.EXTRA_TEXT,
						"I have checked out my apps on http://androlyzer.com. Give it a try, too!");
			} else {
				intent.putExtra(Intent.EXTRA_TEXT, "I have checked out "
						+ this.mAppName + " at " + this.mUrl);
			}
			startActivity(Intent.createChooser(intent,
					"How do you want to share?"));
			return true;
		} else {
			return false;
		}
	}
}
