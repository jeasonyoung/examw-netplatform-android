package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 正在下载的课程资源。
 * @author jeasonyoung
 *
 */
public class Downing implements Serializable, Comparable<Downing> {
	private static final long serialVersionUID = 1L;
	private String lessonId;
	private long startPos,endPos, completeSize;
	private int threadId;
	/**
	 * 获取课程资源ID。
	 * @return 课程资源ID。
	 */
	public String getLessonId() {
		return lessonId;
	}
	/**
	 * 设置课程资源ID。
	 * @param lessonId 
	 *	  课程资源ID。
	 */
	public void setLessonId(String lessonId) {
		this.lessonId = lessonId;
	}
	/**
	 * 获取线程ID。
	 * @return 线程ID。
	 */
	public int getThreadId() {
		return threadId;
	}
	/**
	 * 设置线程ID。
	 * @param threadId 
	 *	  线程ID。
	 */
	public void setThreadId(int threadId) {
		this.threadId = threadId;
	}
	/**
	 * 获取开始位置。
	 * @return 开始位置。
	 */
	public long getStartPos() {
		return startPos;
	}
	/**
	 * 设置开始位置。
	 * @param startPos 
	 *	  开始位置。
	 */
	public void setStartPos(long startPos) {
		this.startPos = startPos;
	}
	/**
	 * 获取结束位置。
	 * @return 结束位置。
	 */
	public long getEndPos() {
		return endPos;
	}
	/**
	 * 设置结束位置。
	 * @param endPos 
	 *	  结束位置。
	 */
	public void setEndPos(long endPos) {
		this.endPos = endPos;
	}
	/**
	 * 获取已完成下载数据。
	 * @return 已完成下载数据。
	 */
	public long getCompleteSize() {
		return completeSize;
	}
	/**
	 * 设置已完成下载数据。
	 * @param completeSize 
	 *	  已完成下载数据。
	 */
	public void setCompleteSize(long completeSize) {
		this.completeSize = completeSize;
	}
	/*
	 * 排序比较。
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Downing obj) {
		return this.threadId - obj.threadId;
	}
	/*
	 * 重载。
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return this.getLessonId() + ":" + this.getThreadId();
	}
}