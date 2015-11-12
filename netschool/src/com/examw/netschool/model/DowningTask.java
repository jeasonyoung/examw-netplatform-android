package com.examw.netschool.model;

import java.io.File;

import org.apache.commons.lang3.StringUtils;

/**
 * 下载任务数据。
 * 
 * @author jeasonyoung
 * @since 2015年9月12日
 */
public class DowningTask extends Downing {
	private static final long serialVersionUID = 1L;
	private String url;
	private File saveFile;
	/**
	 * 构造函数。
	 * @param data
	 */
	public DowningTask(Downing data){
		if(data != null){
			//1.课程资源ID
			this.setLessonId(data.getLessonId());
			//2.当前线程ID
			this.setThreadId(data.getThreadId());
			//3.起始位置
			this.setStartPos(data.getStartPos());
			//4.终止位置
			this.setEndPos(data.getEndPos());
			//5.完成数据
			this.setCompleteSize(data.getCompleteSize());
		}
	}
	/**
	 * 构造函数。
	 * @param data
	 * @param lesson
	 * @param saveFile
	 */
	public DowningTask(Downing data, Lesson lesson, File saveFile){
		this(data);
		this.setLesson(lesson);
		this.setSaveFile(saveFile);
	}
	/**
	 * 设置课程资源。
	 * @param lesson
	 */
	public void setLesson(Lesson lesson){
		if(lesson != null && StringUtils.equalsIgnoreCase(lesson.getId(), this.getLessonId())){
			this.url = lesson.getPriorityUrl();
		}
	}
	/**
	 * 获取下载URL。
	 * @return 下载URL。
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * 获取保存文件。
	 * @return 保存文件。
	 */
	public File getSaveFile() {
		return saveFile;
	}
	/**
	 * 设置保存文件。
	 * @param saveFile 
	 *	  保存文件。
	 */
	public void setSaveFile(File saveFile) {
		this.saveFile = saveFile;
	}
	/*
	 * 重载。
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "线程:" + this.getThreadId()+ "("+this.getStartPos()+","+this.getEndPos()+")url:" + this.getUrl();
	}
}