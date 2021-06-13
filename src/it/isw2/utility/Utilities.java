package it.isw2.utility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utilities {

	private Utilities() {
		
	}
	
	private static Logger logger = Logger.getLogger(Utilities.class.getName());
	
	private static String readAll(Reader rd) throws IOException {
		StringBuilder sb = new StringBuilder();
	    int cp;
	    while ((cp = rd.read()) != -1) {
	    	sb.append((char) cp);
	    }
	  	return sb.toString();
	}

	public static JSONArray readJsonArrayFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			) {
			String jsonText = readAll(rd);
			return new JSONArray(jsonText);
		} finally {
			is.close();
		}
	}

	public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
		InputStream is = new URL(url).openStream();
		try (
				BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
			) {
			String jsonText = readAll(rd);
			return new JSONObject(jsonText);
		} finally {
			is.close();
		}
	}
	
	public static double computeAverage(List<Integer> values) {
		Integer sum = 0;
		if (!values.isEmpty()) {
			for (Integer value : values) {
				sum += value;
			}
			return sum.doubleValue() / values.size();
		}
		return sum;
	}
	
	public static double round(double value, int places) {
		
		if (String.valueOf(value).equals("NaN")) {
			return value;
		}
		
	    if (places < 0) {
	    	throw new IllegalArgumentException();
	    }

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static void logError(Exception e) {
		logger.log(Level.SEVERE, "Something went wrong: {0}", e.toString());
	}
	
	public static void logMsg(String msg) {
		logger.log(Level.INFO, msg);
	}
}
