package com.examw.netschool.entity;

import com.examw.netschool.annotation.Column;

public class UserClass extends BaseEntity{
	private long _id;
	@Column(name="classid")
	private String classid;
	@Column(name="className")
	private String className;
	@Column(name="fatherClassId")
	private String fatherClassId;
	@Column(name="classType")
	private String classType;
	@Column(name="username")
	private String username;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getClassid() {
		return classid;
	}
	public void setClassid(String classid) {
		this.classid = classid;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getFatherClassId() {
		return fatherClassId;
	}
	public void setFatherClassId(String fatherClassId) {
		this.fatherClassId = fatherClassId;
	}
	public String getClassType() {
		return classType;
	}
	public void setClassType(String classType) {
		this.classType = classType;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public UserClass() {
		// TODO Auto-generated constructor stub
	}
	public UserClass(String classid, String className, String username, String fatherClassId,
			String classType) {
		super();
		this.classid = classid;
		this.className = className;
		this.fatherClassId = fatherClassId;
		this.classType = classType;
		this.username = username;
	}
	
}
