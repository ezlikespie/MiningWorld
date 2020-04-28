package me.ezlikespie.miningworld.utility;

import java.util.ArrayList;

public class TimeParse {

	public static Long parse(String message) {
			
		String[] list = message.split(",");
		
		Long totalTime = (long) 0;
		
		for(int i = 0; i<list.length; i++) {
			
			String timeSegment = list[i];
			
			//Seconds
			if(timeSegment.contains("s")||timeSegment.contains("second")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000;
				
			}
			
			//Minutes
			else if(timeSegment.contains("m")&&!timeSegment.contains("mm")||timeSegment.contains("minute")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000*60;
				
			}
			
			//Hours
			else if(timeSegment.contains("h")||timeSegment.contains("hour")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000*60*60;
				
			}
			
			//Days
			else if(timeSegment.contains("d")||timeSegment.contains("day")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000*60*60*24;
				
			}
			
			//Months
			else if(timeSegment.contains("mm")||timeSegment.contains("month")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000*60*60*24*30;
				
			}
			
			//Years
			else if(timeSegment.contains("y")||timeSegment.contains("year")) {
				
				timeSegment = timeSegment.replaceAll("[^0-9]", "");
				totalTime += Long.parseLong(timeSegment)*1000*60*60*24*365;
				
			}
			else {
				return null;
			}
			
		}
		
		return totalTime;
			
	}
	
	public static String toString(Long time) {
		
		ArrayList<Long> valuesList = new ArrayList<Long>();
		ArrayList<String> nameList = new ArrayList<String>();
		nameList.add("Year");
		valuesList.add((long)1000*60*60*24*365);
		nameList.add("Month");
		valuesList.add((long)1000*60*60*24*30);
		nameList.add("Day");
		valuesList.add((long)1000*60*60*24);
		nameList.add("Hour");
		valuesList.add((long)1000*60*60);
		nameList.add("Minute");
		valuesList.add((long)1000*60);
		nameList.add("Second");
		valuesList.add((long)1000);
		
		String message = "";
		
		Long subtractTime = time;
		for(int i = 0; i<nameList.size(); i++) {
			
			String name = nameList.get(i);
			Long number = valuesList.get(i);
			Long subtractVal = (long)(Math.floor(subtractTime/number));
			subtractTime -= subtractVal*number;
			
			if(subtractVal==1) {
				
				message += "1 "+name+" ";
				
			}
			else if(subtractVal!=0){
				
				message += Long.toString(subtractVal)+" "+name+"s ";
				
			}
			
		}
		
		return message;
		
	}
	
}
