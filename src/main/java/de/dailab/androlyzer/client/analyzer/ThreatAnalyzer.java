package de.dailab.androlyzer.client.analyzer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ThreatAnalyzer extends IAnalyzer{
	private Map<String, Pattern> sourcepatterns, sinkpatterns, suspiciouspatterns;
	private Map<String, Set<ArrayList<String>>> sources, sinks;
	private Map<String, Integer> suspicious;
	private HashSet<String> leaks;
//	private static Logger LOGGER = Logger.getLogger(ThreatAnalyzer.class.getName());

	public ThreatAnalyzer(String pathToDecompiledAPK, String packagename, String version,
			int versioncode, String format){
		super(pathToDecompiledAPK, packagename, version, versioncode, format);

		JSONParser parser = new JSONParser();
		try {
			JSONObject jsonobj = (JSONObject) parser.parse(new FileReader(new File("/sdcard/dataflowpatterns.json")));
			//JSONObject jsonobj = (JSONObject) parser.parse(new FileReader(new File(pathToDecompiledAPK.replace("decoded/", "")+"patterns/dataflowpatterns.json")));
			JSONArray sourcesdata = (JSONArray) jsonobj.get("sources");
			JSONArray sinkdata = (JSONArray) jsonobj.get("sinks");
			JSONArray suspiciousdata = (JSONArray) jsonobj.get("suspicious");
			sourcepatterns = compile_patterns(sourcesdata);
			suspiciouspatterns = compile_patterns(suspiciousdata);
			sinkpatterns = compile_patterns(sinkdata);
			sources = new HashMap<String, Set<ArrayList<String>>>();
			sinks = new HashMap<String, Set<ArrayList<String>>>();
			suspicious = new HashMap<String, Integer>();
			leaks = new HashSet<String>();
		} catch (FileNotFoundException e) {
			e.getCause();
		} catch (IOException e) {
			e.getCause();
		} catch (ParseException e) {
			e.getCause();
		}
	}

	private Map<String, Pattern> compile_patterns(JSONArray jsonarray){
		Map<String, Pattern> pattern_dict = new HashMap<String, Pattern>();
		JSONObject temp_json;
		Pattern pattern;
		for(int i=0;i<jsonarray.size();i++){
			temp_json = (JSONObject) jsonarray.get(i);
//			LOGGER.info("Compiling"+temp_json.get("name")+" : " + temp_json.get("pattern"));
			if((Boolean) temp_json.get("multi_line"))
				pattern = Pattern.compile((String) temp_json.get("pattern"),Pattern.DOTALL);
			else
				pattern = Pattern.compile((String) temp_json.get("pattern"));
			pattern_dict.put((String) temp_json.get("name"), pattern);
		}
//		LOGGER.info("compile_patterns ended");
		return pattern_dict;
	} 

	@Override
	public int processFile(String filePath, StringBuffer contents) {
		try{
			if(!filePath.endsWith(".smali"))
				return 0;
			for(Entry<String, Pattern> entry : sourcepatterns.entrySet()){
				Matcher matcher = entry.getValue().matcher(contents);
				while(matcher.find()){
					ArrayList<String> list = new ArrayList<String>();
					for(int i=1;i<=matcher.groupCount();i++){
						list.add(matcher.group(i));
//						LOGGER.info("src pattern: "+matcher.group(i));
					}					
					if(!sources.containsKey(entry.getKey())){
						Set<ArrayList<String>> set = new HashSet<ArrayList<String>>();
						sources.put(entry.getKey(), set);
					}
					sources.get(entry.getKey()).add(list);
				}
			}


			for(Entry<String, Pattern> entry : sinkpatterns.entrySet()){
				Matcher matcher = entry.getValue().matcher(contents);
				while(matcher.find()){
					ArrayList<String> list = new ArrayList<String>();
					for(int i=1;i<=matcher.groupCount();i++){
						list.add(matcher.group(i));
//						LOGGER.info("sink pattern: "+matcher.group(i));
					}
					if(!sinks.containsKey(entry.getKey())){
						Set<ArrayList<String>> set = new HashSet<ArrayList<String>>();
						sinks.put(entry.getKey(), set);
					}
					sinks.get(entry.getKey()).add(list);
				}
			}

			for(Entry<String, Pattern> entry: suspiciouspatterns.entrySet()){
				int findings = -1;
				Matcher matcher = entry.getValue().matcher(contents);
				while(matcher.find()){
					findings++;
				}
				if(findings != -1){
					suspicious.put(entry.getKey(), findings);
				}
			}
	}
	catch(Exception ex){
		ex.getCause();
		ex.printStackTrace();
		return -1;
	}
	return 0;
}

@Override
public JSONObject getReport() {

	for(Entry<String, Set<ArrayList<String>>> entry_src : sources.entrySet()){
		for(ArrayList<String> pattern_src : entry_src.getValue()){
			for(Entry<String, Set<ArrayList<String>>> entry_sink : sinks.entrySet()){
				for(ArrayList<String> pattern_sink : entry_sink.getValue()){
					if(pattern_sink.get(1).equals(pattern_src.get(0))){
						leaks.add(entry_src.getKey()+"-"+entry_sink.getKey());
						//LOGGER.info("leak: "+entry_src.getKey()+"-"+entry_sink.getKey());
					}
				}
			}
		}
	}

	JSONObject Report = new JSONObject();
	try{
		JSONArray Sources = new JSONArray();
		for(String entry : sources.keySet())
			Sources.add(entry);
		Report.put("confidential_sources", Sources);
	}
	catch(Exception ex){
		ex.printStackTrace();
	}
	try{
		JSONArray Leaks = new JSONArray();
		for(String entry : leaks)
			Leaks.add(entry);
		Report.put("leaks", Leaks);
	}
	catch(Exception ex){
		ex.printStackTrace();
	}
	try{
		JSONArray Suspicious = new JSONArray();
		for(String entry : suspicious.keySet())
			Suspicious.add(entry);
		Report.put("suspicious", Suspicious);
	}
	catch(Exception ex){
		ex.printStackTrace();
	}

	return Report;
}
}
