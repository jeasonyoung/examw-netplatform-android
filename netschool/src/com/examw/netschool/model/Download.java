package com.examw.netschool.model;

import java.io.Serializable;

/**
 * 课程资源下载。
 * 
 * @author jeasonyoung
 * @since 2015年9月11日
 */
public class Download implements Serializable {
	private static final long serialVersionUID = 1L;
	private String lessonId,lessonName,filePath;
	private long fileSize;
	private int state;
	/**
	 * 构造函数。
	 */
	public Download(){}
	/**
	 * 构造函数。
	 * @param lesson
	 * 课程资源。
	 */
	public Download(Lesson lesson){
		//课程资源ID
		this.setLessonId(lesson.id);
		//课程资源名称
		this.setLessonName(lesson.name);
		//设置初始化状态
		this.setState(DownloadState.NONE.value);
	}
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
	 * 获取课程资源名称。
	 * @return 课程资源名称。
	 */
	public String getLessonName() {
		return lessonName;
	}
	/**
	 * 设置课程资源名称。
	 * @param lessonName 
	 *	  课程资源名称。
	 */
	public void setLessonName(String lessonName) {
		this.lessonName = lessonName;
	}
	/**
	 * 获取文件路径。
	 * @return 文件路径。
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * 设置文件路径。
	 * @param filePath 
	 *	  文件路径。
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * 获取文件大小。
	 * @return 文件大小。
	 */
	public long getFileSize() {
		return fileSize;
	}
	/**
	 * 设置文件大小。
	 * @param fileSize 
	 *	  文件大小。
	 */
	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}
	/**
	 * 获取状态。
	 * @return 状态。
	 */
	public int getState() {
		return state;
	}
	/**
	 * 设置状态。
	 * @param state 
	 *	  状态。
	 */
	public void setState(int state) {
		this.state = state;
	}
	/*
	 * 重载。
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[" + this.getLessonId() + ","+ this.getLessonName() +"]:" + this.getState();
	}
	
	/**
	 * 课程下载状态。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月11日
	 */
	public static enum DownloadState{
		/**
		 * 排队等待
		 */
		NONE(-1,"排队等待"),
		/**
		 * 连接失败
		 */
		FAIL(-2,"连接失败"),
		/**
		 * 取消。
		 */
		CANCEL(-3,"取消"),
		/**
		 * 暂停
		 */
		PAUSE(0,"暂停"),
		/**
		 * 下载中
		 */
		DOWNING(1,"下载中"),
		/**
		 * 下载完成
		 */
		FINISH(2,"下载完成");
		
		private int value;
		private String name;
		//
		private DownloadState(int value, String name){
			this.value = value;
			this.name = name;
		}
		/**
		 * 获取枚举值。
		 * @return 枚举值。
		 */
		public int getValue() {
			return value;
		}
		/**
		 * 获取枚举名称。
		 * @return 枚举名称。
		 */
		public String getName() {
			return name;
		}
		/**
		 * 枚举值类型转换。
		 * @param value
		 * @return
		 */
		public static DownloadState parse(int value){
			for(DownloadState state : DownloadState.values()){
				if(state.getValue() == value) return state;
			}
			return NONE;
		}	
	}
}