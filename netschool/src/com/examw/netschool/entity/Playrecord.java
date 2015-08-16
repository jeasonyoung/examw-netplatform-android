package com.examw.netschool.entity;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class Playrecord {
	private long _id;
	private String courseId;
	private String courseName;
	private String courseUrl;
	private String courseFilePath;
	private String playTime;
	private int currentTime;
	private String username;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public int getCurrentTime() {
		return currentTime;
	}
	public void setCurrentTime(int currentTime) {
		this.currentTime = currentTime;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}
	public String getCourseUrl() {
		return courseUrl;
	}
	public void setCourseUrl(String courseUrl) {
		this.courseUrl = courseUrl;
	}
	public String getCourseFilePath() {
		return courseFilePath;
	}
	public void setCourseFilePath(String courseFilePath) {
		this.courseFilePath = courseFilePath;
	}
	public String getPlayTime() {
		return playTime;
	}
	public void setPlayTime(String playTime) {
		this.playTime = playTime;
	}
	public Playrecord(String courseId, String playTime, int currentTime,
			String username) {
		super();
		this.courseId = courseId;
		this.playTime = playTime;
		this.currentTime = currentTime;
		this.username = username;
	}
	public Playrecord(String courseId, String courseName, String courseUrl,
			String courseFilePath, String playTime, int currentTime,
			String username) {
		super();
		this.courseId = courseId;
		this.courseName = courseName;
		this.courseUrl = courseUrl;
		this.courseFilePath = courseFilePath;
		this.playTime = playTime;
		this.currentTime = currentTime;
		this.username = username;
	}
	public Playrecord() {
		// TODO Auto-generated constructor stub
	}
	public String getFormatCurrentTime()
	{
		return new SimpleDateFormat("mm:ss").format(new Date(currentTime));
	}
}
