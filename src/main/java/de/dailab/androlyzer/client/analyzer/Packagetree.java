package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.graphics.Color;
import android.util.Log;

/**
 * Packagetree object that simply creates a JSONObject in a format that can be used to display with Javascript InfoVis Toolkit.
 * It can be given to Sunburst represantiton as a JSONObject Input.
 */
public class Packagetree extends IAnalyzer{

	private static final String TAG = "Packagetree Generator";
	private String pathToDecompiledAPK;
	
	public Packagetree(String pathToDecompiledAPK, String packagename, String version,
			int versioncode, String format){
		super(pathToDecompiledAPK, packagename, version, versioncode, format);
		this.pathToDecompiledAPK = pathToDecompiledAPK;
	}

	public static JSONObject getJSONObject (String root){

		JSONObject json = new JSONObject();
		File f = new File(root);
		try{
			json.put("id", f.getPath());
			json.put("name", f.getName());
			if(f.isDirectory()){

				//Log.i(TAG, f.getName() + " is a directory!");
				long size = FileUtils.sizeOfDirectory(f);
				JSONArray children = new JSONArray();
				if(f.list().length != 0){
					for (String fp: f.list()){
						children.add(getJSONObject(joinPath(root, fp)));
						json.put("children", children);
						
					}
					
				}
				else json.put("children", "[]");
				
				JSONObject datajson = new JSONObject();

				datajson.put("size", size);
				datajson.put("$angularWidth", size);
				datajson.put("$color", color(f.getName()));
				json.put("data", datajson);
			}
			else if(f.isFile()){
				long size = f.length();

				JSONObject datajson = new JSONObject();

				datajson.put("size", size);
				datajson.put("$angularWidth", size);
				datajson.put("$color", color(f.getName()));
				json.put("data", datajson);
				
				//Log.i(TAG, f.getName() + " is a file!");

			}
			return json;
		}
		catch(Exception je){
			Log.e("Packagetree !!!!", "JSONException: " + je.getMessage());
			return json;
		}


	}

	private static String color(String p){


		byte bytes[] = p.getBytes();
		Checksum checksum = new CRC32();
		checksum.update(bytes,0,bytes.length);
		long lngChecksum = checksum.getValue();

		try{
			float[] hsv = { (float) ((lngChecksum / 10e9)*256),(float) 0.5, 1};

			String rgb = Integer.toHexString(Color.HSVToColor(hsv));
			return "#"+rgb.substring(2);

		}catch(IllegalArgumentException e){
			return "#000000";
		}
		catch(NullPointerException e){
			return "#000000";
		}


	}
	private static String joinPath (String path1, String path2)
	{
		File file1 = new File(path1);
		File file2 = new File(file1, path2);
		return file2.getPath();
		
	}

	@Override
	public int processFile(String filePath, StringBuffer contents) {
		return 0;
	}

	@Override
	public org.json.simple.JSONObject getReport() {
		return getJSONObject(pathToDecompiledAPK + "/smali");
	}



}
