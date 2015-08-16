package com.examw.netschool.entity;

public class ExamRecord {
	private long _id;
	private String examId,paperId,username,answers,tempAnswer,lastTime,isDone,papername;
	private double score;
	private int useTime,tempTime,papertime,paperscore;
	public long get_id() {
		return _id;
	}
	public void set_id(long _id) {
		this._id = _id;
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
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getAnswers() {
		return answers;
	}
	public void setAnswers(String answers) {
		this.answers = answers;
	}
	public String getTempAnswer() {
		return tempAnswer;
	}
	public void setTempAnswer(String tempAnswer) {
		this.tempAnswer = tempAnswer;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public int getUseTime() {
		return useTime;
	}
	public void setUseTime(int useTime) {
		this.useTime = useTime;
	}
	public int getTempTime() {
		return tempTime;
	}
	public void setTempTime(int tempTime) {
		this.tempTime = tempTime;
	}
	
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	
	public String getIsDone() {
		return isDone;
	}
	public void setIsDone(String isDone) {
		this.isDone = isDone;
	}
	public ExamRecord(String paperId,
			String username, String answers, String tempAnswer, double score,
			int useTime, int tempTime,String lastTime,String isDone) {
		super();
		this.paperId = paperId;
		this.username = username;
		this.answers = answers;
		this.tempAnswer = tempAnswer;
		this.score = score;
		this.useTime = useTime;
		this.tempTime = tempTime;
		this.lastTime = lastTime;
		this.isDone = isDone;
	}
	public ExamRecord() {
		// TODO Auto-generated constructor stub
	}
	public ExamRecord(String paperId, String username) {
		super();
		this.paperId = paperId;
		this.username = username;
	}
	public String getPapername() {
		return papername;
	}
	public void setPapername(String papername) {
		this.papername = papername;
	}
	public int getPapertime() {
		return papertime;
	}
	public void setPapertime(int papertime) {
		this.papertime = papertime;
	}
	public int getPaperscore() {
		return paperscore;
	}
	public void setPaperscore(int paperscore) {
		this.paperscore = paperscore;
	}
	
	
}
