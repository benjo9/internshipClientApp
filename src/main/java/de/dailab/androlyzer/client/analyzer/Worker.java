package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.HashSet;
import org.json.simple.JSONObject;

import android.os.Environment;

public class Worker {
	HashSet<IAnalyzer> analyzers;
	private String pathToDecompiledAPK, packagename, version, format;
	private int versioncode;
	public Worker(String pathToDecompiledAPK, String packagename, String version, int versioncode) {
		this.pathToDecompiledAPK = pathToDecompiledAPK;
		this.packagename = packagename;
		this.version = version;
		this.versioncode = versioncode;
		analyzers = new HashSet<IAnalyzer>();
		analyzers.add(new ManifestAnalyzer(pathToDecompiledAPK, 
				packagename, version, versioncode, "xml"));
		analyzers.add(new ThirdPartyAnalyzer(pathToDecompiledAPK, 
				packagename, version, versioncode, "dir"));
		analyzers.add(new ThreatAnalyzer(pathToDecompiledAPK, 
				packagename, version, versioncode, "smali"));
		analyzers.add(new UsedPkgsAnalyzer(pathToDecompiledAPK, 
				packagename, version, versioncode, "smali"));
		analyzers.add(new Packagetree(pathToDecompiledAPK, 
				packagename, version, versioncode, "dir"));
	}

	public void work(){
		analyze_manifest(pathToDecompiledAPK);
		analyze_src(pathToDecompiledAPK + "/smali");
		analyze_binary(pathToDecompiledAPK);
		JSONObject report_json = new JSONObject();
		for(IAnalyzer analyzer : analyzers){
			report_json.put(analyzer.getClass().getSimpleName(), analyzer.getReport());
		}
		try{
			if(!(new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/de.dailab.androlyzer.client/reports/").exists())){
				(new File(Environment.getExternalStorageDirectory()
						+ "/Android/data/de.dailab.androlyzer.client/reports/")).mkdirs();
			}
			FileWriter file = new FileWriter(new File(Environment.getExternalStorageDirectory()
					+ "/Android/data/de.dailab.androlyzer.client/reports/"+this.packagename+"-"
					+version+".json"));
			file.write(report_json.toString());
			file.flush();
			file.close();
		}
		catch(IOException ex){
			ex.getCause();
			ex.printStackTrace();
		}
	}

	private void analyze_binary(String root_dir){
		File[] file_list = (new File(root_dir)).listFiles();
		for(File file : file_list){
			if(file.isDirectory() && !file.getAbsolutePath().endsWith("/smali")){
				analyze_src(file.getAbsolutePath());
			}
		}
	}

	private void analyze_manifest(String manifest_path){
		File manifest = new File(manifest_path + "/AndroidManifest.xml");
		StringBuffer string_buf = new StringBuffer("");
		try{
			int ch;
			FileInputStream f_in = new FileInputStream(manifest);
			while((ch = f_in.read()) != -1)
				string_buf.append((char) ch);
			f_in.close();
		}
		catch(Exception ex){
			ex.getCause();
		}
		for(IAnalyzer analyzer : analyzers)
			analyzer.processFile(manifest.getAbsolutePath(), string_buf);
		return;
	}

	private void analyze_src(String root_dir){
		File[] file_list = (new File(root_dir)).listFiles();
		for(File file : file_list){
			try{
				System.out.println("Worker.java: FilePath = "+file.getAbsolutePath());
				StringBuffer string_buf = new StringBuffer("");
				if(file.isFile()){
					try{
						int ch;
						FileInputStream f_in = new FileInputStream(file);
						while((ch = f_in.read()) != -1)
							string_buf.append((char) ch);
						f_in.close();
					}
					catch(Exception ex){
						ex.getCause();
					}
				}
				for(IAnalyzer analyzer : analyzers)
					analyzer.processFile(file.getAbsolutePath(), string_buf);
			}
			catch(Exception ex){
				ex.getCause();
			}
			if(file.isDirectory()){
				analyze_src(file.getAbsolutePath());
			}
		}
		return;
	}
}
