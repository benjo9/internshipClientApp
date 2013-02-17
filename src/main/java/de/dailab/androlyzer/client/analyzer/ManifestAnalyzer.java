package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ManifestAnalyzer extends IAnalyzer{

	String appName, versionCode, versionName, label;
	ArrayList<String> featuresList, permissionsList;
	private String pathToDecompiledAPK;

	public ManifestAnalyzer(String pathToDecompiledAPK, String packagename, String version,
			int versioncode, String format){
		super(pathToDecompiledAPK, packagename, version, versioncode, format);
		this.pathToDecompiledAPK = pathToDecompiledAPK;
		appName = "";
		versionCode = "";
		versionName = "";
		label = "";
		permissionsList = new ArrayList<String>();
		featuresList = new ArrayList<String>();
	}

	private String pick_one_values(){
		File[] file_list = (new File(pathToDecompiledAPK + "/res")).listFiles();
		String values_dir = "";
		for(int i=0;i<file_list.length;i++){
			if(file_list[i].getName().contains("values")){
				values_dir = file_list[i].getAbsolutePath();
				break;
			}
		}
		return values_dir;
	}

	@Override
	public int processFile(String filePath, StringBuffer contents){
		return 0;
	}

	private int collectData(File manifest_file) {
		try {
			DocumentBuilderFactory dFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder dBuilder = dFactory.newDocumentBuilder();
			Document document = dBuilder
					.parse(manifest_file);
			document.getDocumentElement().normalize();
			/*
			NodeList permissionNodes = document
					.getElementsByTagName("manifest");
			 */
			NodeList appNodes= document
					.getElementsByTagName("manifest");
			appName = appNodes.item(0).getAttributes().getNamedItem("package").getTextContent();
			versionCode = appNodes.item(0).getAttributes().getNamedItem("android:versionCode").getTextContent();
			versionName = appNodes.item(0).getAttributes().getNamedItem("android:versionName").getTextContent();


			NodeList application = document.getElementsByTagName("application");
			label = application.item(0).getAttributes().getNamedItem("android:label").getTextContent();

			if(label.contains("@string")){
				StringBuffer string_buf = new StringBuffer("");
				try{
					File string_file;
					File values_dir = new File(pathToDecompiledAPK + "/res/values");
					if(values_dir.exists()){
						string_file = new File(values_dir.getAbsolutePath() + "/strings.xml");
					}
					else{
						values_dir = new File(pathToDecompiledAPK + "/res/values-en");
						if(values_dir.exists())
							string_file = new File(values_dir.getAbsolutePath() + "/strings.xml");
						else{
							String values_folder = pick_one_values();
							if(values_folder.isEmpty())
								throw new Exception("No values folder!");
							string_file = new File(values_folder + "/strings.xml");
						}
					}
					try{
						int ch;
						FileInputStream f_in = new FileInputStream(string_file);
						while((ch = f_in.read()) != -1)
							string_buf.append((char) ch);
						f_in.close();
					}
					catch(Exception ex){
						ex.getCause();
					}
				}
				catch(Exception ex){
					ex.getCause();
					ex.printStackTrace();
				}

				try{
					String res_name = label.substring(label.lastIndexOf("/")+1);
					Matcher matcher = Pattern.compile("<string name=\"" + res_name + "\">(.*)</string>")
							.matcher(string_buf);
					if(matcher.find()){
						label = matcher.group(1);
					}
				}
				catch(Exception ex){
					ex.getCause();
					ex.printStackTrace();
				}
			}



			NodeList permissionNodes = document
					.getElementsByTagName("uses-permission");

			for(int i = 0; i< permissionNodes.getLength(); i++){
				String raw = permissionNodes.item(i).getAttributes().item(0).getTextContent();
				//String permission = raw.substring(19);
				permissionsList.add(raw);
			}

			NodeList featureNodes = document
					.getElementsByTagName("uses-feature");

			for(int i = 0; i< featureNodes.getLength(); i++){
				String raw = featureNodes.item(i).getAttributes().item(0).getTextContent();
				//String feature = raw.substring(17);
				featuresList.add(raw);
			}
		} catch (MalformedURLException e) {
			System.out.println("error");
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			System.out.println("error");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("error");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("error");
			e.printStackTrace();
		}
		catch (Exception ex){
			System.out.println("error");
			ex.getCause();
			ex.printStackTrace();
		}
		return 0;
	}

	@Override
	public JSONObject getReport() {
		try{
			File manifest_file = new File(pathToDecompiledAPK + "/AndroidManifest.xml");
			if(manifest_file.exists())
				collectData(manifest_file);
			else
				throw new FileNotFoundException("AndroidManifest.xml NOT FOUND!");
		}
		catch(FileNotFoundException ex){
			ex.getCause();
			ex.printStackTrace();
		}
		JSONObject Report = new JSONObject();
		try{
			JSONArray Features = new JSONArray();
			for(String entry : featuresList)
				Features.add(entry);
			Report.put("used_features", Features);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		try{
			JSONArray Permissions = new JSONArray();
			for(String entry : permissionsList)
				Permissions.add(entry);
			Report.put("permissions", Permissions);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}

		try{
			Report.put("versioncode", versionCode);
			Report.put("version", versionName);
			Report.put("title", label);
			Report.put("packagename", appName);
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return Report;
	}
}
