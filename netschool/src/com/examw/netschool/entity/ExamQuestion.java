package com.examw.netschool.entity;

public class ExamQuestion {
	private long _id;
	private String qid,ruleId,paperId,content,answer,analysis,linkQid,qType,userAnswer;
	private int optionNum,orderId;
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
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getAnswer() {
		return answer;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public String getAnalysis() {
		return analysis;
	}
	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}
	public String getLinkQid() {
		return linkQid;
	}
	public void setLinkQid(String linkQid) {
		this.linkQid = linkQid;
	}
	public String getQType() {
		return qType;
	}
	public void setQType(String qType) {
		this.qType = qType;
	}
	public int getOptionNum() {
		return optionNum;
	}
	public void setOptionNum(int optionNum) {
		this.optionNum = optionNum;
	}
	public int getOrderId() {
		return orderId;
	}
	public void setOrderId(int orderId) {
		this.orderId = orderId;
	}
	public ExamQuestion(String qid,String ruleid, String paperId, String content,
			String answer, String analysis, String linkQid,
			String qType, int optionNum, int orderId) {
		super();
		this.qid = qid;
		this.ruleId = ruleid;
		this.paperId = paperId;
		this.content = content;
		this.answer = answer;
		this.analysis = analysis;
		this.linkQid = linkQid;
		this.qType = qType;
		this.optionNum = optionNum;
		this.orderId = orderId;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleid) {
		this.ruleId = ruleid;
	}
	public String getUserAnswer() {
		return userAnswer;
	}
	public void setUserAnswer(String userAnswer) {
		this.userAnswer = userAnswer;
	}
	
} 
