package com.sameh.goeuro;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.sameh.goeuro.util.CSVWriter;

public class CityFinder {
	
	public static void main(String... args) {
		try {
			writeCityToCSV(args[0].replace(" ", "%20"));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
	private static String URL_PREFIX = "http://api.goeuro.com/api/v2/position/suggest/en/";
	
	private static final String FILE_NAME_SUFFIX = ".csv";
	
	private static final String NAME_KEY = "name";
	private static final String GEO_POSITION_KEY = "geo_position";
	
	private static final Map<String, BiFunction<JSONObject, Object, String>> KEY_FUNCTION_MAP;
	/**
	 * A list of keys to be able to iterate through the keys of the map in the order of insertion.
	 */
	private static final List<String> KEYS;
	static {
		BiFunction<JSONObject, Object, String> defaultFunction = (jsonObj, key) -> {
			return getStringFromJSONObject(jsonObj, key);
		};
		
		BiFunction<JSONObject, Object, String> geoPositionFunction = (jsonObj, key) -> {
			JSONObject pos = (JSONObject)jsonObj.get(GEO_POSITION_KEY);
			return pos != null ? getStringFromJSONObject(pos, key) : "";
		};
		
		Map<String, BiFunction<JSONObject, Object, String>> tmpMap = new HashMap<>();
		List<String> tmpKeys = new ArrayList<>();
		
		// Add a new field by adding its key to the map and list
		// e.g. addToMapAndList(tmpMap, tmpKeys, "country", defaultFunction);
		addToMapAndList(tmpMap, tmpKeys, "_id", defaultFunction);
		addToMapAndList(tmpMap, tmpKeys, NAME_KEY, defaultFunction);
		addToMapAndList(tmpMap, tmpKeys, "type", defaultFunction);
		addToMapAndList(tmpMap, tmpKeys, "latitude", geoPositionFunction);
		addToMapAndList(tmpMap, tmpKeys, "longitude", geoPositionFunction);
		KEY_FUNCTION_MAP = Collections.unmodifiableMap(tmpMap);
		KEYS = Collections.unmodifiableList(tmpKeys);
	}
	
	private static String getStringFromJSONObject(JSONObject jsonObj, Object key) {
		if (jsonObj == null) {
			return "";
		}
		Object obj = jsonObj.get(key);
		return obj != null ? obj.toString() : "";
	}
	
	private static <K, V> void addToMapAndList(Map<K, V> map, List<K> list, K key, V value) {
		map.put(key, value);
		list.add(key);
	}
	
	public static JSONArray findCity(String city) throws MalformedURLException {
		JSONArray array = null;
		try {
			URL url = new URL(URL_PREFIX + city);
			System.out.println(url);
			InputStream is = url.openStream();
			JSONParser parser = new JSONParser();
			StringBuilder builder = new StringBuilder("");
			int charCode;
			while ((charCode = is.read()) != -1) {
				builder.append((char)charCode);
			}
			String content = builder.toString();
			array = (JSONArray)parser.parse(content);
			if (array.isEmpty()) {
				throw new MalformedURLException("Invalid city name \"" + city + "\"");
			}
			
		} catch (ParseException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			if (e instanceof MalformedURLException) {
				throw (MalformedURLException)e;
			} else {
				e.printStackTrace();
			}
		}
		
		return array;
	}
	
	public static void writeCityToCSV(String cityName) throws MalformedURLException {
		writeCityToCSV(findCity(cityName), cityName);
	}
	
	public static void writeCityToCSV(JSONArray jsonArray) {
		JSONObject jsonObj = (JSONObject)jsonArray.iterator().next();
		String cityName = (String)jsonObj.get(NAME_KEY);
		if (cityName != null) {
			writeCityToCSV(jsonArray, cityName);
		}
	}
	
	public static void writeCityToCSV(JSONArray jsonArray, String cityName) {
		String fileName = cityName.replace("%20", " ") + FILE_NAME_SUFFIX;
		CSVWriter csvWriter = new CSVWriter(fileName);
		for (String key : KEYS) {
			csvWriter.addValue(key);
		}
		csvWriter.nextRow();
		for (Object obj : jsonArray) {
			JSONObject jsonObj = (JSONObject)obj;
			for (String key : KEYS) {
				csvWriter.addValue(KEY_FUNCTION_MAP.get(key).apply(jsonObj, key));
			}
			csvWriter.nextRow();
		}
		try {
			csvWriter.write();
		} catch (IOException e) {
			System.err.println("Could not write to file \"" + fileName + "\"");
			e.printStackTrace();
		}
	}
}
