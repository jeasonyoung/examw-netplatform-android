package com.examw.netschool.entity;

public class QuestionAdapterData {
	private String paperId;
	private String title;
	private int count;
	
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getCount() {
		return count;
	}
	public void setCount(int count) {
		this.count = count;
	}
	public QuestionAdapterData(String paperId,String title, int count) {
		super();
		this.paperId = paperId;
		this.title = title;
		this.count = count;
	}
	public QuestionAdapterData() {
		// TODO Auto-generated constructor stub
	}
}
