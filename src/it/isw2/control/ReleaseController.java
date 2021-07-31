package it.isw2.control;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.isw2.entity.Release;
import it.isw2.entity.Ticket;
import it.isw2.utility.Utilities;

public class ReleaseController {

	private static Map<LocalDateTime, String> releaseNames;
	private static Map<LocalDateTime, String> releaseID;
	private static List<LocalDateTime> releases;
	
	private ReleaseController() {
		
	}

	/**
	 * Retrievs the releases of a project with the Rest API of JIRA
	 * @param projName
	 * @return releases
	 * @throws IOException
	 * @throws JSONException
	 */
	public static List<Release> getReleases(String projName) throws IOException, JSONException {
		List<Release> releaseList = new ArrayList<>();

		// Fills the arraylist with releases dates and orders them
		// Ignores releases with missing dates
		releases = new ArrayList<>();
		Integer i;
		String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		JSONObject json = Utilities.readJsonFromUrl(url);
		JSONArray versions = json.getJSONArray("versions");
		releaseNames = new HashMap<>();
		releaseID = new HashMap<>();
		for (i = 0; i < versions.length(); i++) {
			var name = "";
			var id = "";
			if (versions.getJSONObject(i).has("releaseDate")) {
				if (versions.getJSONObject(i).has("name"))
					name = versions.getJSONObject(i).get("name").toString();
				if (versions.getJSONObject(i).has("id"))
					id = versions.getJSONObject(i).get("id").toString();
				addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name, id);
			}
		}

		// order releases by date
		Collections.sort(releases, (o1, o2) -> o1.compareTo(o2));

		if (releases.size() < 6)
			return releaseList;

		// Order map by date
		Map<LocalDateTime, String> map = new TreeMap<>(releaseNames);
		Integer index = 1;
		for (Map.Entry<LocalDateTime, String> entry : map.entrySet()) {
			var date = LocalDateTime.parse(entry.getKey().toString());
			String name = entry.getValue();
			Release release = new Release(index, date, name);
			releaseList.add(release);
			index++;
		}
		
		return releaseList;
	}
	
	private static void addRelease(String strDate, String name, String id) {
		var date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();

		if (!releases.contains(dateTime))
			releases.add(dateTime);
		
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
	}
	
	/**
	 * Remove the last half of releases because they are noisy
	 * @param releases
	 * @param tickets
	 */
	public static void removeHalfReleases(List<Release> releases, List<Ticket> tickets) {
		int numReleases = releases.size();
		int halfReleases = numReleases / 2;
		
		Iterator<Release> release = releases.iterator();
		while (release.hasNext()) {
			Release r = release.next();
			if (r.getIndex() > halfReleases) {
				release.remove();
			}
		}
		
		TicketController.removeTickets(halfReleases, tickets);
	}
}
