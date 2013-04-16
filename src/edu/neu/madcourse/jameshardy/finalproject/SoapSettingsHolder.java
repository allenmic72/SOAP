package edu.neu.madcourse.jameshardy.finalproject;

import java.util.Calendar;

import com.google.gson.annotations.Expose;

public class SoapSettingsHolder{
	@Expose
	public boolean autoMonitor = true;
	
	@Expose
	public int startDay = 1; //monday in the array of days
	
	@Expose
	public int endDay = 5; //friday in the array of days
	
	@Expose
	public int startTimeHour = 9;
	
	@Expose
	public int startTimeMinute = 00;
	
	@Expose
	public int endTimeHour = 17; //5pm
	
	@Expose
	public int endTimeMinute = 00;
}