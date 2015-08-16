package com.examw.netschool.entity;

public class ExamErrorQuestion {
	private long _id;
	private String qid,username,examId,paperId;
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
	public ExamErrorQuestion(String qid, String username,String paperId) {
		super();
		this.qid = qid;
		this.username = username;
		this.paperId = paperId;
	}
	
	public String getExamId() {
		return examId;
	}
	public void setExamId(String examId) {
		this.examId = examId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public ExamErrorQuestion() {
		// TODO Auto-generated constructor stub
	}
}
