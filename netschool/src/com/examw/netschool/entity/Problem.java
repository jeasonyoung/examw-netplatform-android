package com.examw.netschool.entity;

public class Problem {
	private String content;
	private String title;
	private String path;
	private String addTime;
	private String answersJson;
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getAddTime() {
		return addTime;
	}
	public void setAddTime(String addTime) {
		this.addTime = addTime;
	}
	public String getAnswersJson() {
		return answersJson;
	}
	public void setAnswersJson(String answersJson) {
		this.answersJson = answersJson;
	}
	public Problem(String content, String title, String path, String addTime,
			String answersJson) {
		super();
		this.content = content;
		this.title = title;
		this.path = path;
		this.addTime = addTime;
		this.answersJson = answersJson;
	}
	public Problem() {
		// TODO Auto-generated constructor stub
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		buf.append("content:").append(content).append(",");
		buf.append("title:").append(title).append(",");
		buf.append("path:").append(path).append(",");
		buf.append("addTime:").append(addTime).append(",");
		buf.append("answersJson:").append(answersJson);
		buf.append("}");
		return buf.toString();
	}
}
