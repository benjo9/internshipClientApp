package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.util.HashSet;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ThirdPartyAnalyzer extends IAnalyzer{

	private HashSet<String> third_party_pkgs;
	public ThirdPartyAnalyzer(String pathToDecompiledAPK, String packagename,
			String version, int versioncode, String format) {
		super(pathToDecompiledAPK, packagename, version, versioncode, format);
		third_party_pkgs = new HashSet<String>();
	}

	@Override
	public int processFile(String filePath, StringBuffer contents) {
		try{
			if (filePath.contains("smali") && (new File(filePath)).isDirectory()){
				String pkg_name = filePath.replace(pathToDecompiledAPK+"smali/","")
						.replace('/', '.');
				if(!pkg_name.contains(packagename) && pkg_name.indexOf('.') != -1) 
					third_party_pkgs.add(pkg_name);
			}
		}
		catch(Exception ex){
			ex.printStackTrace();
			ex.getCause();
		}
		return 0;
	}

	@Override
	public JSONObject getReport() {
		JSONObject Report = new JSONObject();
		JSONArray third_party = new JSONArray();
		for(String entry : third_party_pkgs)
			third_party.add(entry);
		Report.put("third_party_packages", third_party_pkgs);
		return Report;
	}

}
