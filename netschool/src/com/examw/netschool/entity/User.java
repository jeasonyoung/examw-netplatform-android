package com.examw.netschool.entity;

import com.examw.netschool.annotation.Column;

public class User extends BaseEntity{
	private long _id;
	@Column(name="uid")
	private String uid;
	@Column(name="username")
	private String username;
	@Column(name="password")
	private String password;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	public User() { }
	
	public User(String uid, String username, String password) {
		super();
		this.uid = uid;
		this.username = username;
		this.password = password;
	}
	
}
