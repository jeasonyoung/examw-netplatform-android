package com.examw.netschool.entity;

public class ExamFavor {
	private long _id;
	private String qid,username,paperId;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getQid() {
		return qid;
	}
	public void setQid(String qid) {
		this.qid = qid;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public ExamFavor() {
		// TODO Auto-generated constructor stub
	}
	public ExamFavor(String qid, String username,String paperId) {
		super();
		this.qid = qid;
		this.username = username;
		this.paperId = paperId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public ExamFavor(String username, String paperId) {
		super();
		this.username = username;
		this.paperId = paperId;
	}
	
}	
