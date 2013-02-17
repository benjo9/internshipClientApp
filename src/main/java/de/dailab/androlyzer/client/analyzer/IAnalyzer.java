package de.dailab.androlyzer.client.analyzer;

import org.json.simple.JSONObject;

abstract public class IAnalyzer {
	public IAnalyzer(String pathToDecompiledAPK, String packagename, String version, int versioncode, String format) {
		this.pathToDecompiledAPK = pathToDecompiledAPK;
		this.format = format;
		this.packagename = packagename;
		this.version = version;
		this.versioncode = versioncode;
		result = new JSONObject();
	};
	protected String pathToDecompiledAPK, packagename, version, format;
	protected JSONObject result;
	private int versioncode;
//	public abstract String getName();
	public int getVersion(){
		return versioncode;
	};
	public String getFormat(){
		return format;
	};
	public abstract int processFile(String filePath, StringBuffer contents);
	public abstract JSONObject getReport();

}
