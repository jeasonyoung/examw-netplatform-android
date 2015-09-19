package com.examw.netschool.model;
/**
 * 下载完成数据。
 * 
 * @author jeasonyoung
 * @since 2015年9月16日
 */
public class DownloadComplete extends Download {
	private static final long serialVersionUID = 1L;
	private long completeSize;
	/**
	 * 构造函数。
	 * @param download
	 */
	public DownloadComplete(Download download){
		if(download != null){
			//设置课程资源ID
			this.setLessonId(download.getLessonId());
			//设置课程资源名称
			this.setLessonName(download.getLessonName());
			//设置下载文件路径
			this.setFilePath(download.getFilePath());
			//设置文件大小
			this.setFileSize(download.getFileSize());
			//设置状态
			this.setState(download.getState());
		}
	}
	/**
	 * 获取完成的数据量。
	 * @return 完成的数据量。
	 */
	public long getCompleteSize() {
		return completeSize;
	}
	/**
	 * 设置完成的数据量。
	 * @param completeSize 
	 *	  完成的数据量。
	 */
	public void setCompleteSize(long completeSize) {
		this.completeSize = completeSize;
	}
}