package it.isw2.control;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.isw2.entity.Release;
import it.isw2.entity.Ticket;
import it.isw2.utility.Utilities;

public class TicketController {
	
	private TicketController() {
		
	}

	public static List<Ticket> getTickets(String projName, List<Release> releases) throws IOException {
		List<Ticket> tickets = new ArrayList<>();
		
		Integer i = 0;
		Integer j = 0;
		Integer total = 1;
		
		// Get JSON API for closed bugs w/ AV in the project
		do {
			//Only gets a max of 1000 at a time, so must do this multiple times if bugs >1000
	        j = i + 1000;
	        String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project=%22"
	               + projName + "%22AND%22issueType%22=%22Bug%22AND(%22status%22=%22closed%22OR"
	               + "%22status%22=%22resolved%22)AND%22resolution%22=%22fixed%22&fields=key,resolutiondate,affectedVersion,versions,created&startAt="
	               + i.toString() + "&maxResults=" + j.toString();
	        
	        try {
	        	JSONObject json = Utilities.readJsonFromUrl(url);
	        	JSONArray issues = json.getJSONArray("issues");
	        	total = json.getInt("total");
	        	
	        	for (; i < total && i < j; i++) {
	        		// Iterate through each bug
	        		String key = issues.getJSONObject(i%1000).get("key").toString();
	        		LocalDateTime creationDate = LocalDateTime.parse(issues.getJSONObject(i%1000).getJSONObject("fields").getString("created").substring(0,16));
	        	
	        		JSONArray versions = issues.getJSONObject(i % 1000).getJSONObject("fields").getJSONArray("versions");
	 	           	List<Integer> listAV = getAV(versions, releases);
	 	           	Ticket ticket = new Ticket(key, creationDate, listAV);
	 	           	
	 	           	if (!(listAV.isEmpty() || listAV.get(0) == null)) {
	 	           		ticket.setIv(listAV.get(0));
	 	           	}
	 	           	else {
	 	           		ticket.setIv(0);
	 	           	}
	 	           	
	 	           	ticket.setOv(getOVIndex(creationDate, releases));
	 	           	tickets.add(ticket);
	        	}
	        	
	        } catch (JSONException | IOException e) {
	        	Utilities.logError(e);
	        }
	        
		} while (i < total);
		
		return tickets;
	}
	
	// Return AV of a ticket
	private static List<Integer> getAV(JSONArray versions, List<Release> releases) throws JSONException {
		List<Integer> avList = new ArrayList<>();
		
		if (versions.length() == 0) {
			avList.add(null);
		}
		else {
			for (var i = 0; i < versions.length(); i++) {
				String av = versions.getJSONObject(i).getString("name");
				for (var j = 0; j < releases.size(); j++) {
					if (av.endsWith(releases.get(j).getName())) {
						avList.add(releases.get(j).getIndex());
					}
				}
			}
		}
		
		return avList;
	}
	
	// Return OV for a ticket
	private static Integer getOVIndex(LocalDateTime date, List<Release> releases) {
		Integer releaseIndex = 0;
		
		for (var i = 0; i < releases.size(); i++) {
			if (date.isBefore(releases.get(i).getReleaseDate())) {
				releaseIndex = releases.get(i).getIndex();
				break;
			}
			
			if (date.isAfter(releases.get(releases.size()-1).getReleaseDate())) {
				releaseIndex = releases.get(releases.size()-1).getIndex();
			}
		}
		
		return releaseIndex;
	}
	
	// Set correct AV list for every ticket
	public static void checkAV(List<Ticket> tickets) {
		for (Ticket ticket : tickets) {
			if (ticket.getIv() != 0) {
				if (ticket.getFv() > ticket.getIv() && ticket.getOv() >= ticket.getIv()) {
					setAV(ticket);
				}
				if (ticket.getFv() >= ticket.getIv() && ticket.getOv() < ticket.getIv() || ticket.getFv() < ticket.getIv()) {
					ticket.setIv(0);
					ticket.getAv().clear();
					ticket.getAv().add(0);
				}
				if (ticket.getFv().equals(ticket.getIv())) {
					ticket.getAv().clear();
					ticket.getAv().add(0);
				}
			}
		}
	}
	
	// Fill AV list of a ticket with all versions between IV and FV
	private static void setAV(Ticket ticket) {
		ticket.getAv().clear();
		for (int i = ticket.getIv(); i < ticket.getFv(); i++) {
			ticket.getAv().add(i);
		}
	}
	
	// Remove tickets associated with last half of releases
	public static void removeTickets(int halfReleases, List<Ticket> tickets) {
		Iterator<Ticket> ticket = tickets.iterator();
		while (ticket.hasNext()) {
			Ticket t = ticket.next();
			if (t.getIv() > halfReleases) {
				ticket.remove();
			}
			if (t.getOv() > halfReleases || t.getFv() > halfReleases) {
				List<Integer> av = new ArrayList<>();
				for (int i = t.getIv(); i < halfReleases + 1; i++) {
					av.add(i);
				}
				t.setAv(av);
			}
		}
	}
}
