package com.examw.netschool.entity;

public class ExamRule {
	private long _id;
	private String type,ruleId,paperId,title,ruleScoreSet,fullTitle;
	private int ruleQuestionNum,orderInPaper;
	private double ruleScoreForEach;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
	}
	public String getRuleId() {
		return ruleId;
	}
	public void setRuleId(String ruleId) {
		this.ruleId = ruleId;
	}
	public String getPaperId() {
		return paperId;
	}
	public void setPaperId(String paperId) {
		this.paperId = paperId;
	}
	public String getRuleTitle() {
		return title;
	}
	public void setRuleTitle(String ruleTitle) {
		this.title = ruleTitle;
	}
	public String getScoreSet() {
		return ruleScoreSet;
	}
	public void setScoreSet(String scoreSet) {
		this.ruleScoreSet = scoreSet;
	}
	public int getQuestionNum() {
		return ruleQuestionNum;
	}
	public void setQuestionNum(int questionNum) {
		this.ruleQuestionNum = questionNum;
	}
	public int getOrderInPaper() {
		return orderInPaper;
	}
	public void setOrderInPaper(int orderInPaper) {
		this.orderInPaper = orderInPaper;
	}
	public ExamRule(String ruleId, String paperId, String ruleTitle,String ruleTitleInfo,
			String ruleType,String scoreSet, int questionNum, double scoreForEach, int orderInPaper) {
		super();
		this.ruleId = ruleId;
		this.paperId = paperId;
		this.title = ruleTitle;
		this.fullTitle = ruleTitleInfo;
		this.type = ruleType;
		this.ruleScoreSet = scoreSet;
		this.ruleQuestionNum = questionNum;
		this.ruleScoreForEach = scoreForEach;
		this.orderInPaper = orderInPaper;
	}
	public String getRuleType() {
		return type;
	}
	public void setRuleType(String ruleType) {
		this.type = ruleType;
	}
	public double getScoreForEach() {
		return ruleScoreForEach;
	}
	public void setScoreForEach(double scoreForEach) {
		this.ruleScoreForEach = scoreForEach;
	}
	public String getFullTitle() {
		return fullTitle;
	}
	public void setFullTitle(String ruleTitleInfo) {
		this.fullTitle = ruleTitleInfo;
	}
	public ExamRule(String ruleId,String paperId) {
		super();
		this.ruleId = ruleId;
		this.paperId = paperId;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paperId == null) ? 0 : paperId.hashCode());
		result = prime * result + ((ruleId == null) ? 0 : ruleId.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ExamRule other = (ExamRule) obj;
		if (paperId == null) {
			if (other.paperId != null)
				return false;
		} else if (!paperId.equals(other.paperId))
			return false;
		if (ruleId == null) {
			if (other.ruleId != null)
				return false;
		} else if (!ruleId.equals(other.ruleId))
			return false;
		return true;
	}
	
}
