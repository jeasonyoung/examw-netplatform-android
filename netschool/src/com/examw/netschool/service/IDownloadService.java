package com.examw.netschool.service;

import com.examw.netschool.model.Download;

import android.os.Handler;
/**
 * 文件下载服务接口。
 * @author jeasonyoung
 *
 */
public interface IDownloadService{
	/**
	 * 设置UI处理Handler。
	 * @param handler
	 */
	void setHandler(Handler handler);
	/**
	 * 添加下载。
	 * @param download
	 */
	void addDownload(Download download);
	/**
	 * 取消下载。
	 * @param lessonId
	 */
	void cancelDownload(String lessonId);
	/**
	 * 暂停下载。
	 * @param lessonId
	 */
	void pauseDownload(String lessonId);
	/**
	 * 继续下载。
	 * @param lessonId
	 */
	void continueDownload(String lessonId);
}