package com.examw.netschool.entity;

import com.examw.netschool.annotation.Column;

/**
 * 
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class UserClass extends BaseEntity{
	private String _id;
	
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
	
	public String get_id() {
		return _id;
	}
	
	public void set_id(String _id) {
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
	
	/**
	 * 构造函数。
	 */
	public UserClass() { }
	/**
	 * 构造函数。
	 * @param classid
	 * @param className
	 * @param username
	 * @param fatherClassId
	 * @param classType
	 */
	public UserClass(String classid, String className, String username, String fatherClassId, String classType) {
		super();
		
		this.classid = classid;
		this.className = className;
		this.fatherClassId = fatherClassId;
		this.classType = classType;
		this.username = username;
	}
	
}