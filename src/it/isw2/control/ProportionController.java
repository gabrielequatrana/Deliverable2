package it.isw2.control;

import java.util.ArrayList;
import java.util.List;

import it.isw2.entity.Ticket;

public class ProportionController {

	// Using a sliding window proportion, only the 1% 
	// of tickets will be used to compute proportion
	private static int percentage;
	
	private ProportionController() {
		
	}
	
	public static void proportion(List<Ticket> tickets) {
		List<Ticket> checkedTickets = initialCheck(tickets);
		
		int numTickets = tickets.size();
		percentage = numTickets * 1/100;
		
		List<Ticket> propList = new ArrayList<>();
		for (Ticket ticket : tickets) {
			if (!checkedTickets.contains(ticket)) {
				if (ticket.getIv() != 0) {
					updatePropWindow(propList, ticket);
				}
				else {
					setIV(propList, ticket);
				}
			}
		}
	}
	
	// Check which ticket has OV = FV (cause IV = FV - (FV - OV)*P so IV = FV)
	private static List<Ticket> initialCheck(List<Ticket> tickets) {
		List<Ticket> checkedTickets = new ArrayList<>();
		
		for (Ticket ticket : tickets) {
			if (ticket.getOv().equals(ticket.getFv()) && ticket.getIv() == 0) {
				ticket.setIv(ticket.getFv());
				checkedTickets.add(ticket);
			}
		}
		
		return checkedTickets;
	}
	
	// Update proportion window
	private static void updatePropWindow(List<Ticket> propList, Ticket ticket) {
		if (propList.size() < percentage) {
			propList.add(ticket);
		}
		else {
			propList.remove(0);
			propList.add(ticket);
		}
	}
	
	private static int computeP(Ticket ticket) {
		int fv = ticket.getFv();
		int ov = ticket.getOv();
		int iv = ticket.getIv();
		
		var roundedP = 0;
		
		// If FV=OV then P=0
		if (fv != ov) {
			float p = (float) (fv-iv)/(fv-ov);
			roundedP = Math.round(p);
		}
		
		return roundedP;
	}
	
	private static void setIV(List<Ticket> propList, Ticket ticket) {
		List<Integer> listP = new ArrayList<>();
		int p;
		var sum = 0;
		
		for (Ticket t : propList) {
			p = computeP(t);
			listP.add(p);
			sum += p;
		}
		
		float avgP = (float)sum/percentage;
		int roundedAvgP = Math.round(avgP);
		
		int fv = ticket.getFv();
		int ov = ticket.getOv();
		int predictedIV = fv - (fv - ov)*roundedAvgP;
		
		// If predicted IV > OV then IV = OV
		if (predictedIV > ov) {
			ticket.setIv(ov);
		}
		else {
			ticket.setIv(predictedIV);
		}
	}
}
