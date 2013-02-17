package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import android.os.Environment;

public class UsedPkgsAnalyzer extends IAnalyzer{

	private HashSet<String> used_pkgs, third_party_pkgs;

	public UsedPkgsAnalyzer(String pathToDecompiledAPK, String packagename,
			String version, int versioncode, String format) {
		super(pathToDecompiledAPK, packagename, version, versioncode, format);
		used_pkgs = new HashSet<String>();
		third_party_pkgs = new HashSet<String>();
	}

	@Override
	public int processFile(String filePath, StringBuffer contents) {
		// used packages
		try{
			if(filePath.endsWith(".smali")){
				Matcher matcher = Pattern.compile("(?<=L)(?:[a-z]+[/]+)+").matcher(contents);
				while(matcher.find()){
					String match = matcher.group().replace('/', '.');
					match = match.substring(0, match.length() -1);
					if(match.indexOf('.') != -1)
						used_pkgs.add(match);
				}
			}
			if (filePath.contains("smali") && (new File(filePath)).isDirectory()){
				String pkg_name = filePath.replace(pathToDecompiledAPK+"smali/","")
						.replace('/', '.');
				if(pkg_name.indexOf('.') != -1) 
					third_party_pkgs.add(pkg_name);
			}
		}
		catch(Exception e){
			e.printStackTrace();
			e.getCause();
			return -1;
		}
		return 0;
	}

	@Override
	public JSONObject getReport() {
		JSONObject Report = new JSONObject();
		JSONArray used_packages = new JSONArray();
		used_pkgs.removeAll(third_party_pkgs);
		for(String entry : used_pkgs)
			used_packages.add(entry);
		Report.put("android_packages_used_by_the_application", used_packages);
		return Report;
	}

}
