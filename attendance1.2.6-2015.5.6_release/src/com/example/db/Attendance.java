package com.example.db;

public class Attendance {

	public int autoID = -1;
	public int ID = -1;
	public String time = null;
	public String date = null;
	public int year = -1;
	public int month = -1;
	public int day = -1;
	public int hour = -1;
	public int minute = -1;
	public int second = -1;
	public String dateTime = null;
	public String NAME = null;
	
	  @Override
	    public String toString()
	    {
	        String result ="编号: "+this.autoID+"," 
	        +"ID : "+this.ID+","
	        +"year : "+this.year+","
	        +"month : "+this.month+","
	        +"day : "+this.day+","
	        +"hour : "+this.hour+","
	        +"minute : "+this.minute+","
	        +"second : "+this.second+","
	        +"date : "+this.date+","
	        +"time :"+this.time+","
	        +"dateTime :"+this.dateTime+","
	        +"NAME : "+this.NAME;
	        return result;
	    }
}
