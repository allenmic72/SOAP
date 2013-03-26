package edu.neu.madcourse.jameshardy.MultiplayerBoggle;

public class MP_BoggleUser {
	String name;
	String number;
	int score;
	
	public MP_BoggleUser()
	{
		this.name = "";
		this.number = "";
		this.score = 0;
	}
	public MP_BoggleUser(String user, String phone, int points) {
		this.name = user;
		this.number = phone;
		this.score = points;
	}
	
	
	public String getName() {
		return this.name;
	}
	public String getNumber() {
		return this.number;
	}
	public int getScore() {
		return this.score;
	}
	public void setScore(int s) {
		this.score = s;
	}

}
