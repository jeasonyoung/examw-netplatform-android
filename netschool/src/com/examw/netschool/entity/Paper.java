package com.examw.netschool.entity;

import java.util.List;

public class Paper {
	private long _id;
	private String paperId;
	private String paperName;
	private int paperSorce;
	private int paperTime;
	private String courseId,examId;
	private String jsonString;
	private List<ExamRule> examRules;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getPaperName() {
		return paperName;
	}
	public void setPaperName(String paperName) {
		this.paperName = paperName;
	}
	public int getPaperSorce() {
		return paperSorce;
	}
	public void setPaperSorce(int paperSorce) {
		this.paperSorce = paperSorce;
	}
	public int getPaperTime() {
		return paperTime;
	}
	public void setPaperTime(int paperTime) {
		this.paperTime = paperTime;
	}
	
	public String getExamId() {
		return examId;
	}
	public void setExamId(String examId) {
		this.examId = examId;
	}
	public Paper(String paperId, String paperName, int paperSorce, int paperTime,String courseId,String examId) {
		super();
		this.paperId = paperId;
		this.paperName = paperName;
		this.paperSorce = paperSorce;
		this.paperTime = paperTime;
		this.courseId = courseId;
		this.examId = examId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getCourseId() {
		return courseId;
	}
	public void setCourseId(String courseId) {
		this.courseId = courseId;
	}
	public List<ExamRule> getRuleList() {
		return examRules;
	}
	public void setRuleList(List<ExamRule> ruleList) {
		this.examRules = ruleList;
	}
	public String getJsonString() {
		return jsonString;
	}
	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}
	
}
