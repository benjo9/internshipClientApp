package de.dailab.androlyzer.client.ui;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
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
public class ItemsActivity extends Activity{

	ArrayList<ArrayList<String>> myLists;
	private static int LIST_ITEMS = 8;
	private String reportName;
	private WebView diagram;
	private JSONObject packageTree;
	private ArrayList<ArrayAdapter<String>> adapters;

	final int[] backgroundColors =
		{ Color.parseColor("#a7200f"), Color.parseColor("#ee917c"), Color.parseColor("#00987f"), Color.parseColor("#decbb3"), Color.parseColor("#fac491"), Color.parseColor("#febcda"), Color.parseColor("#713290"), Color.parseColor("#7813ba"), Color.parseColor("#91fbca") };

	final String[] titles = { "Permissions", "Features", "Used Packages", "Used Third Party Packages", "Leaks","Sources", "Suspicious",  "Package Diagram"};


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.items);
		Bundle extras = getIntent().getExtras(); 
		reportName = extras.getString("fileName");

		HorizontalPager realViewSwitcher = new HorizontalPager(getApplicationContext());

		myLists = new ArrayList<ArrayList<String>>();
		for(int i = 0; i< LIST_ITEMS; i++){
			myLists.add(new ArrayList<String>());
		}

		readJSONFile();

		adapters = new ArrayList<ArrayAdapter<String>>();
		for (int i = 0; i < LIST_ITEMS; i++){
			adapters.add(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,  android.R.id.text1, myLists.get(i)));
		}

		LinearLayout firstLayout = new LinearLayout(getApplicationContext());
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		firstLayout.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout, 0);
		getLayout(layout, 1);
		firstLayout.addView(layout);
		realViewSwitcher.addView(firstLayout);

		LinearLayout secondLayout = new LinearLayout(getApplicationContext());
		LinearLayout layout2 = new LinearLayout(getApplicationContext());
		secondLayout.setOrientation(LinearLayout.VERTICAL);
		layout2.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout2, 2);
		getLayout(layout2, 3);
		secondLayout.addView(layout2);
		realViewSwitcher.addView(secondLayout);

		LinearLayout thirdLayout = new LinearLayout(getApplicationContext());
		LinearLayout layout3 = new LinearLayout(getApplicationContext());
		thirdLayout.setOrientation(LinearLayout.VERTICAL);
		layout3.setOrientation(LinearLayout.VERTICAL);
		getLayout(layout3, 4);
		getLayout(layout3, 5);
		getLayout(layout3, 6);
		thirdLayout.addView(layout3);
		realViewSwitcher.addView(thirdLayout);


		// Add some views to it


		/*for (int i = 0; i < LIST_ITEMS; i++) {

			LinearLayout myLayout = new LinearLayout(getApplicationContext());
			myLayout.setOrientation(LinearLayout.VERTICAL);


			TextView textView = new TextView(getApplicationContext());
			textView.setText(titles[i]);
			textView.setTextSize(24);
			textView.setTextColor(Color.WHITE);
			textView.setGravity(Gravity.CENTER);
			textView.setBackgroundColor(backgroundColors[i]);
			textView.setPadding(0, 20, 0, 20);
			myLayout.addView(textView);

			if(i!=7){
			ListView listView = new ListView(getApplicationContext());
			listView.setAdapter(adapters.get(i));
			myLayout.addView(listView);
			}
			else{
				Packagetree p = new Packagetree();

				diagram = new WebView(getApplicationContext());
				diagram.getSettings().setJavaScriptEnabled(true);
				diagram.loadUrl("file:///android_asset/JIT/diagram.html");
				diagram.getSettings().setBuiltInZoomControls(true);
				//diagram.getSettings().setDisplayZoomControls(true);
				diagram.setWebViewClient(new WebViewClient(){
					@Override
					public void onPageFinished(WebView view, String url) {
						try{
							diagram.loadUrl("javascript: var json = " + packageTree.toString());
							diagram.loadUrl("javascript: init()");
						}
						catch(Exception e){
							Toast.makeText(getApplicationContext(), "No decoded files found!", Toast.LENGTH_LONG).show();
						}
						super.onPageFinished(view, url);
					}
				});
				myLayout.addView(diagram);
			}

			realViewSwitcher.addView(myLayout);

		}*/

		// set as content view
		setContentView(realViewSwitcher);
	}

	public LinearLayout getLayout(LinearLayout myLayout, int i){



		TextView textView = new TextView(getApplicationContext());
		textView.setText(titles[i]);
		textView.setTextSize(24);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER);
		textView.setBackgroundColor(backgroundColors[i]);
		textView.setPadding(0, 20, 0, 20);
		myLayout.addView(textView);

		if(i!=7){
			ListView listView = new ListView(getApplicationContext());
			listView.setAdapter(adapters.get(i));
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
			params.weight = 1;
			listView.setLayoutParams(params);
			myLayout.addView(listView);

		}

		return myLayout;
	}

	public void readJSONFile(){

		BufferedReader bufferedReader = null;

		try {
			bufferedReader = new BufferedReader(new FileReader("/sdcard/reports/"+reportName));
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
			PackageTree = obj.getJSONObject("PackageTree");


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


}
