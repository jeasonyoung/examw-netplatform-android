package com.examw.netschool.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.codec.digest.DigestUtils;
import com.examw.netschool.dao.DowningDao;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.model.Downing;
import com.examw.netschool.model.DowningTask;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.model.Lesson;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/**
 * 文件下载服务实现。
 * @author jeasonyoung
 * 负责视频文件下载以及安全加密处理。
 */
public final class MultiThreadDownload {
	private static final String TAG = "MultiThreadDownload";
	private static final int  NET_STATE_SUCCESS = 200, NET_STATE_RANGE = 206, BUFFER_SIZE = 1024,THREAD_SLEEP = 100;
	private static final int THREADS = 4;//下载线程数
	private final DownloadDao downloadDao;
	private final DowningDao downingDao;
	private final Download download;
	private final Lesson lesson;
	private final String current_user_id;
	private final ExecutorService threadPools;
	private boolean stop = false;
	private OnDownloadProgressListener onDownloadProgressListener;
	/**
	 * 构造函数。
	 * @param download
	 */
	public MultiThreadDownload(Download download){
		Log.d(TAG, "初始化...");
		if(download == null || StringUtils.isBlank(download.getLessonId())) throw new RuntimeException("下载记录或课程资源ID不存在!");
		this.download = download;
		//初始化下载记录Dao
		this.downloadDao = new DownloadDao(AppContext.getContext(), this.current_user_id = AppContext.getCurrentUserId());
		if(!this.downloadDao.hasDownload(this.download.getLessonId())){
			throw new RuntimeException("课程资源["+this.download.getLessonId()+"]未有下载记录!");
		}
		//初始化课程资源Dao
		final LessonDao lessonDao = new LessonDao(this.downloadDao);
		this.lesson = lessonDao.getLesson(this.download.getLessonId()); 
		if(this.lesson == null) throw new RuntimeException("课程资源["+this.download.getLessonName()+"]不存在!");
		if(StringUtils.isBlank(this.lesson.getPriorityUrl())) throw new RuntimeException("课程资源URL不存在!");
		//初始化下载线程Dao
		this.downingDao = new DowningDao(this.downloadDao);
		//下载线程池
		this.threadPools = Executors.newFixedThreadPool(THREADS * 2);
	}
	/**
	 * 设置下载进度监听器。
	 * @param onDownloadProgressListener 
	 *	  下载进度监听器。
	 */
	public void setOnDownloadProgressListener(OnDownloadProgressListener onDownloadProgressListener) {
		Log.d(TAG, "设置下载进度监听器...");
		this.onDownloadProgressListener = onDownloadProgressListener;
	}
	/**
	 * 开始下载。
	 */
	public void start() throws Exception {
		Log.d(TAG, "开始下载课程资源["+ this.lesson.getName()+"]...");
		this.stop = false;
		//加载下载线程
		final List<Downing> downings =  this.downingDao.loadDowningByLesson(this.lesson.getId());
		if(downings != null && downings.size() > 0){//继续下载
			Log.d(TAG, "继续下载...");
			//检查下载文件是否存在
			if(download.getFilePath() == null || !(new File(download.getFilePath()).exists())){
				Log.d(TAG, "已下载的文件不存在..." + download.getFilePath());
				//删除下载线程记录
				this.downingDao.deleteByLesson(this.lesson.getId());
				//新增现在
				this.initStart();
			}else{
				//开始下载
				this.start(downings);
			}
		}else{//新增下载
			this.initStart();
		}
	}
	//开始新的下载。
	private void initStart() throws Exception{
		Log.d(TAG, "初始化开始下载...");
		//初始化
		final HttpClient httpClient = new DefaultHttpClient();
		//获取下载文件大小
		HttpHead httpHead = new HttpHead(this.lesson.getPriorityUrl());
		HttpResponse response = httpClient.execute(httpHead);
		int status = 0;
		if((status = response.getStatusLine().getStatusCode()) != NET_STATE_SUCCESS){
			Log.e(TAG, "网络连接状态:" + status);
			throw new Exception("["+this.lesson+"]http-head-status:" + status);
		}
		final Header[] headers = response.getHeaders("Content-Length");
		if(headers != null && headers.length > 0){
			this.download.setFileSize(Long.parseLong(headers[0].getValue()));
			Log.d(TAG, "获取下载文件["+this.lesson+"]长度:" + this.download.getFileSize());
		}else{
			Log.e(TAG, "获取下载文件["+this.lesson+"]长度失败!");
		}
		httpHead.abort();
		if(this.download.getFileSize() <= 0) throw new Exception("获取下载文件["+this.lesson+"]长度失败!");
		//初始化根路径并检查容量
		final File root = createDownloadSaveFileDir(this.current_user_id, this.download.getFileSize());
		//检查路径是否存在
		if(!root.exists()){
			root.mkdirs();
		}
		//文件下载路径
		final String fileName = DigestUtils.md5Hex(this.download.getLessonId()) + "." + parseSuffix(this.lesson.getPriorityUrl());
		this.download.setFilePath(new File(root, fileName).getAbsolutePath());
		//是否支持多线程分段下载
		boolean acceptRangs = false;
		httpHead = new HttpHead(this.lesson.getPriorityUrl());
		httpHead.addHeader("Range", "bytes=0-" + (this.download.getFileSize() - 1));
		response = httpClient.execute(httpHead);
		if((status = response.getStatusLine().getStatusCode()) == NET_STATE_RANGE){
			acceptRangs = true;
		}
		Log.d(TAG, "["+this.lesson+"] acceptRangs("+NET_STATE_RANGE+"):" + status );
		httpHead.abort();
		//下载线程处理
		final String lessonId = this.download.getLessonId();
		final int threadNum = acceptRangs ? THREADS : 1;
		final long fileSize = this.download.getFileSize(), blockSize = (fileSize / threadNum) + (fileSize % threadNum == 0 ? 0 : 1);
		final List<Downing> downings = new ArrayList<Downing>(threadNum);
		for(int i = 0; i < threadNum; i++){
			//初始化下载线程
			final Downing data = new Downing();
			//所属课程资源ID
			data.setLessonId(lessonId);
			//设置当前线程ID
			data.setThreadId(i);
			//设置下载起始位置
			data.setStartPos(i * blockSize);
			//设置下载结束位置
			data.setEndPos((i + 1) * blockSize - 1); 
			//完成下载量
			data.setCompleteSize(0);
			//添加到集合
			downings.add(data);
		}
		//添加下载线程数据库
		if(downingDao != null) downingDao.add(downings);
		//启动下载
		this.start(downings);
	}
	//创建文件下载目录。
 	private static final File createDownloadSaveFileDir(String userId, long fileSize) throws Exception{
		Log.d(TAG, "创建下载目录.");
		File root = Environment.getExternalStorageDirectory();
		if(root == null || !root.exists()){//如果SD卡不存在，则检查内部存储
			Log.d(TAG, "未检测到SD卡,将使用内部存储!");
			 root =  Environment.getDataDirectory();
		}
		if(fileSize > 0){
			StatFs statFs = new StatFs(root.getPath());
			long  space = (long)(statFs.getAvailableBlocks() * statFs.getBlockSize());
			if(space > 0 &&  space <  fileSize){
				Log.d(TAG, "路径["+root.getPath()+"]剩余空间["+space+"]不足["+fileSize+"],不能下载!");
				throw new Exception("可用容量不足，不能下载！");
			}
		}
		return new File(root + File.separator + "netschool" + File.separator + DigestUtils.md5Hex(userId) + File.separator + "video");
	}
	//获取链接后缀名
	private static final String parseSuffix(String url){
		if(StringUtils.isBlank(url)) return StringUtils.EMPTY;
		return StringUtils.substringAfterLast(url, ".");
	}
	/**
	 * 设置停止下载。
	 * @param stop 
	 *	  停止下载。
	 */
	public void setStop(boolean stop) {
		Log.d(TAG, "设置["+this.lesson+"]下载暂停...");
		this.stop = stop;
	}
	//开始下载
	private void start(List<Downing> downings) throws Exception{
		Log.d(TAG, "准备开始下载...");
		if(this.stop) return;
		//检查下载保存文件是否存在
		final File saveFile = new File(this.download.getFilePath());
		if(!saveFile.exists()){
			Log.d(TAG, "创建保存文件..." + saveFile.getAbsolutePath());
			final RandomAccessFile accessFile = new RandomAccessFile(saveFile, "rwd");
			accessFile.setLength(this.download.getFileSize());
			accessFile.close();
		}
		//设置下载状态
		this.download.setState(DownloadState.DOWNING.getValue());
		//更新下载信息到数据
		if(this.downloadDao != null){
			Log.d(TAG, "更新下载信息数据...");
			this.downloadDao.update(this.download);
		}
		//初始化线程
		final DownloadThreadTask[] tasks  = new DownloadThreadTask[downings.size()];
		for(int i = 0; i < downings.size(); i++){
			//初始化下载线程任务
			tasks[i] = new DownloadThreadTask(new DefaultHttpClient(), new DowningTask(downings.get(i), this.lesson, saveFile));
			//执行下载线程
			this.threadPools.execute(tasks[i]);
		}
		//循环等待下载完成
		boolean isDown = !this.stop;
		while(isDown){
			try{
				//下载进度统计
				int finish_count = 0;
				long total = 0;
				//循环线程
				for(DownloadThreadTask task : tasks){
					final Downing downing;
					if(task == null || (downing = task.getTaskDowning()) == null) continue;
					try{
						//累加各线程下载数据
						total += downing.getCompleteSize();
						//停止线程
						if(this.stop)task.setStop(true);
						//更新下载线程到数据
						if(downingDao != null) downingDao.update(downing);
					}catch(Exception e){
						Log.e(TAG, "轮询线程["+task+"]异常:" + e.getMessage(), e);
					}finally{
						finish_count += task.isFinish() ? 1 : 0 ;
					}
				}
				//设置是否停止
				isDown = !this.stop;
				if(isDown){
					isDown = !(finish_count == tasks.length);
					if(!isDown){
						//设置下载完成状态
						this.download.setState(DownloadState.FINISH.getValue());
						//更新到数据库
						if(this.downloadDao != null){
							Log.d(TAG, "更新下载完成状态到数据库...");
							this.downloadDao.update(this.download);
						}
					}
				}
				//通知监听器
				if(this.onDownloadProgressListener != null){
					final long fileSize = this.download.getFileSize();
					this.onDownloadProgressListener.onProgress(this.download.getLessonId(), (int)(((float)total / fileSize) * 100));
				}
			}catch(Exception e){
				Log.e(TAG, "循环等待下载时异常:" + e.getMessage(), e);
			}finally{
				//休眠1秒
				Thread.sleep(1000);
			}
		}
	}
	/**
	 * 下载线程任务。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月12日
	 */
	private class DownloadThreadTask implements Runnable{
		private static final String TAG = "DownloadThreadTask";
		private final HttpClient httpClient;
		private final DowningTask task;
		private boolean stop = false, finish = false;
		/**
		 * 构造函数。
		 * @param httpClient
		 * @param task
		 */
		public DownloadThreadTask(HttpClient httpClient, DowningTask task){
			if(httpClient == null || task == null) throw new RuntimeException("httpClient或task为空!");
			Log.d(TAG, "初始化下载线程:"+ task.getThreadId() +"["+ task.getStartPos() +"=>"+ task.getEndPos() +"]..." );
			this.httpClient = httpClient;
			this.task = task;
		}
		/**
		 * 获取线程下载进度。
		 * @return 线程下载进度。
		 */
		public Downing getTaskDowning() {
			return task;
		}
		/**
		 * 设置是否停止。
		 * @param finished
		 */
		public void setStop(boolean stop) {
			this.stop = stop;
		}
		/**
		 * 获取是否完成。
		 * @return 是否完成。
		 */
		public boolean isFinish() {
			return finish;
		}
		/*
		 * 下载执行。
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run() {
			try{
				//检查URL是否存在
				if(StringUtils.isBlank(this.task.getUrl())){
					Log.d(TAG,  "["+this.task+"]下载URL不存在!");
					this.finish = true;
					return;
				}
				//检查保存文件是否存在
				if(this.task.getSaveFile() == null || !this.task.getSaveFile().exists()){
					Log.d(TAG,  "["+this.task+"]下载保存文件不存在!");
					this.finish = true;
					return;
				}
				//检查起至点
				final long startPos = this.task.getStartPos() + this.task.getCompleteSize();
				if(this.task.getEndPos() - startPos <= 0){
					Log.d(TAG,  "["+this.task+"]分段下载数起始点大于终点!");
					this.finish = true;
					return;
				}
				//请求GET
				final HttpGet httpGet = new HttpGet(this.task.getUrl());
				//当前线程下载起止点.
				httpGet.addHeader("Range", "bytes=" + startPos + "-" + this.task.getEndPos());
				//执行请求
				final HttpResponse response = this.httpClient.execute(httpGet);
				//获取状态
				final int status = response.getStatusLine().getStatusCode();
				if(status != NET_STATE_SUCCESS && status != NET_STATE_RANGE){
					Log.d(TAG, "["+this.task+"]网络请求返回状态:" + status);
					return;
				}
				//读取请求返回数据
				final HttpEntity entity = response.getEntity();
				//流缓冲
				final BufferedInputStream inputStream = new BufferedInputStream(entity.getContent());
				
				//存储文件随机读取操作(用于分段大文件处理)
				final RandomAccessFile randomAccessFile = new RandomAccessFile(this.task.getSaveFile(), "rwd");
				//设置开始写文件位置
				randomAccessFile.seek(startPos);
				
				//读取缓存
				byte[] buf = new byte[BUFFER_SIZE];
				//开始循环读取
				long total = 0;
				int len = 0;
				while(!this.stop && (len = inputStream.read(buf, 0, buf.length)) > -1){
					//写入保存文件
					randomAccessFile.write(buf, 0, len);
					//累计下载量
					total += len;
					//设置当前完成数据
					this.task.setCompleteSize(total);
					//线程休眠
					Thread.sleep(THREAD_SLEEP);
				}
				//关闭随机文件写入
				randomAccessFile.close();
			}catch(Exception e){
				Log.e(TAG, "["+this.task+"]下载异常:" + e.getMessage(), e);
			}finally{
				this.finish = true;
			}
		}
		/*
		 * 重载。
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return this.task.toString();
		}
	}
	
	/**
	 * 下载进度监听器。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月13日
	 */
	public interface OnDownloadProgressListener{
		/**
		 * 下载进度。
		 * @param lessonId
		 * @param per
		 */
		void onProgress(String lessonId, int per);
	}
}