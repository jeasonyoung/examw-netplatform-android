package com.examw.netschool.service;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.service.MultiThreadDownload.OnDownloadProgressListener;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
/**
 * 课程视频下载服务。
 * @author jeasonyoung
 *
 */
public class DownloadService extends Service implements IDownloadService {
	 private static final String TAG = "DownloadService";
	 private static final long THREAD_SLEEP = 500;
	 
	 private final ExecutorService pools;	 
	 private final Queue<Download> downloadQueue;
	 private final Map<String, Download> downloadPosCache;
	 private final Map<String, MultiThreadDownload> downloadThreads;
	 private Handler handler;
	
	 private IBinder binder;
	 private boolean stop;
	 
	 private DownloadDao downloadDao;
	/**
	 * 构造函数。
	 */
	public DownloadService(){
		//下载服务队列轮询单线程池
		this.pools = Executors.newSingleThreadExecutor();
		//下载队列
		this.downloadQueue = new LinkedList<Download>();
		//下载位置集合缓存
		this.downloadPosCache = new HashMap<String, Download>();
 		//下载线程集合
 		this.downloadThreads =  new HashMap<String, MultiThreadDownload>();
		//服务绑定接口
		this.binder = new FileDownloadServiceBinder(this);
	}
	/*
	 * 重载服务绑定。
	 * @see android.app.Service#onBind(android.content.Intent)
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "下载服务被绑定...");
		//重置下载。
		this.stop = false;
		//
		return this.binder;
	}
	/*
	 * 重载创建。
	 * @see android.app.Service#onCreate()
	 */
	@Override
	public void onCreate() {
		Log.d(TAG, "下载服务正在被创建...");
		//执行文件下载管理线程。
		pools.execute(this.downloadThreadMgr);
		//
		super.onCreate();
	}
	//下载线程管理
	private Runnable downloadThreadMgr = new Runnable() {
		@Override
		public void run() {
			Log.d(TAG, "下载轮询线程...");
			while(!stop){
				try{
					//线程等待
					Thread.sleep(THREAD_SLEEP);
					//检查网络是否可用
					if(!checkNetwork(AppContext.getContext())){
						Log.d(TAG, "网络不可用...");
						sendMessage(DownloadState.FAIL, "网络不可用!");
						continue;
					}
					//从队列中弹出需要下载课程
					final Download download = downloadQueue.poll();
					if(download == null || StringUtils.isBlank(download.getLessonId())){
						Log.d(TAG, "下载队列已空!");
						continue;
					}
					Log.d(TAG, "准备开始下载课程["+download+"]...");
					//下载线程
					MultiThreadDownload threadDownload = downloadThreads.get(download.getLessonId());
					if(threadDownload == null ){
						Log.d(TAG, "初始化下载线程["+download+"]...");
						//初始化下载线程
						threadDownload = new MultiThreadDownload(download);
						//设置进度监听器
						threadDownload.setOnDownloadProgressListener(downloadProgressListeners);
						//添加下载线程
						downloadThreads.put(download.getLessonId(), threadDownload);
					}
					//下载状态
					DownloadState state = DownloadState.parse(download.getState());
					try{
						//下载中状态
						state = DownloadState.DOWNING;
						download.setState(state.getValue());
						//更新下载状态
						sendUpdateState(download.getLessonId(), state);
						//更新数据
						asyncUpdateDownload(download);
						
						//启动下载
						threadDownload.start();
						
						//下载完成
						download.setState((state = DownloadState.FINISH).getValue());
						asyncUpdateDownload(download);
						sendUpdateState(download.getLessonId(), state);
					}catch(Exception e){
						download.setState((state = DownloadState.FAIL).getValue());
						Log.e(TAG, "下载课程["+download+"]异常:" + e.getMessage(), e);
						//更新下载状态
						sendUpdateState(download.getLessonId(),state);
						//发生下载消息
						sendMessage(state, e.getMessage());
						//取消下载
						cancelDownload(download.getLessonId());
					}
				}catch(Exception e){
					Log.e(TAG, "下载轮询线程:" + e.getMessage(), e);
				}
			}
		}
	};
	//下载进度监听器
	private OnDownloadProgressListener downloadProgressListeners = new OnDownloadProgressListener(){
		/*
		 * 更新下载进度。
		 * @see com.examw.netschool.downloads.MultiThreadDownload.OnDownloadProgressListener#onProgress(com.examw.netschool.model.Download, int)
		 */
		@Override
		public void onProgress(Download download, int per) {
			if(download == null || StringUtils.isBlank(download.getLessonId()) || per < 0) return;
			Log.d(TAG, "更新课程["+download+"]下载进度:" + per);
			sendProgressUpdate(download.getLessonId(), per);
		}
	};
	/*
	 * 重载服务摧毁时方法。
	 * @see android.app.Service#onDestroy()
	 */
	@Override
	public void onDestroy() {
		Log.d(TAG, "下载服务将被摧毁...");
		//设置下载停止
		this.stop = true;
		//停止下载线程
		pools.shutdown();
		//销毁下载销毁
		super.onDestroy();
	}
	/*
	 * 设置UI处理Handler。
	 * @see com.examw.netschool.service.FileDownloadService#setHandler(android.os.Handler)
	 */
	@Override
	public void setHandler(Handler handler) {
		Log.d(TAG, "设置UI消息处理Handler...");
		this.handler = handler;
	}
	//发送进度更新
	private void sendProgressUpdate(String id, int per){
		if(this.handler != null && StringUtils.isNotBlank(id)){
			//初始化消息
			final Message msg = new Message();
			//消息类型:更新进度
			msg.what = Constant.HANLDER_WHAT_PROGRESS;
			//下载课程ID
			msg.obj = id;
			//进度
			msg.arg1 = per;
			//发送消息
			this.handler.sendMessage(msg);
		}
	}
	//发送消息
	private void sendMessage(DownloadState state, String msg){
		if(this.handler != null && StringUtils.isNotBlank(msg)){
			Log.d(TAG, "发送文本消息:" + msg);
			//初始化消息
			final Message message = new Message();
			//消息类型:文本消息
			message.what = Constant.HANLDER_WHAT_MSG;
			//状态
			message.arg1 = state.getValue();
			//消息内容
			message.obj = msg;
			//发送消息
			this.handler.sendMessage(message);
		}
	}
	//发送状态更新
	private synchronized void sendUpdateState(String id, DownloadState state){
		if(this.handler != null && StringUtils.isNotBlank(id)){
			Log.d(TAG, "发送状态更新:["+id+"]" + state);
			//初始化消息
			final Message msg = new Message();
			//消息类型:更新进度
			msg.what = Constant.HANLDER_WHAT_PROGRESS;
			//下载课程ID
			msg.obj = id;
			//状态
			msg.arg1 = state.getValue();
			//发送消息
			this.handler.sendMessage(msg);
		}
	}
	/*
	 *  添加下载。
	 * @see com.examw.netschool.service.IDownloadService#addDownload(com.examw.netschool.model.Download)
	 */
	@Override
	public void addDownload(Download download) {
		Log.d(TAG, "添加下载课程["+download+"]到队列...");
		final String lessonId;
		if(download == null || StringUtils.isBlank(lessonId = download.getLessonId())) return;
		//下载状态
		final DownloadState state = DownloadState.NONE;
		//设置下载状态值。
		download.setState(state.getValue());
		//如果在队列中存在则忽略
		if(this.downloadQueue.contains(download)){
			Log.d(TAG, "课程["+download+"]已在队列中存在!"); 
			return;
		}
		//将课程加入到队列尾部
		final boolean result = this.downloadQueue.offer(download);
		Log.d(TAG, "课程["+download+"]压入到队尾:" + result); 
		//设置位置集合
		this.downloadPosCache.put(lessonId, download);
		//更新到数据库
		this.asyncUpdateDownload(download);
		//发送UIHandler
		this.sendUpdateState(lessonId, state);
	}
	/*
	 * 取消下载。
	 * @see com.examw.netschool.service.IDownloadService#cancelDownload(java.lang.String)
	 */
	@Override
	public void cancelDownload(String id) {
		if(StringUtils.isBlank(id)) return;
		final Download download = this.downloadPosCache.get(id);
		if(download == null) return;
		Log.d(TAG, "取消课程["+ download +"]下载...");
		//下载状态
		final DownloadState state = DownloadState.CANCEL;
		//设置下载状态值。
		download.setState(state.getValue());
		//如果在队列中排序则从队列中移除
		if(this.downloadQueue.contains(download)){
			boolean result = this.downloadQueue.remove(download);
			Log.d(TAG, "课程["+download+"]从队列中移除:" + result);
		}
		//取消下载
		final MultiThreadDownload multiThreadDownload = this.downloadThreads.get(id);
		if(multiThreadDownload != null){
			//停止下载
			multiThreadDownload.setStop(true);
			//移除下载线程集合
			this.downloadThreads.remove(id);
		}
		//移除下载位置缓存
		if(this.downloadPosCache.containsKey(id)){
			this.downloadPosCache.remove(id);
		}
		//更新到数据库
		this.asyncDeleteDownload(download);
		//发送状态消息
		this.sendUpdateState(id, state);
	}
	/*
	 * 暂停下载。
	 * @see com.examw.netschool.service.IDownloadService#pauseDownload(java.lang.String)
	 */
	@Override
	public void pauseDownload(String id) {
		if(StringUtils.isBlank(id)) return;
		final Download download = this.downloadPosCache.get(id);
		if(download == null) return;
		Log.d(TAG, "暂停课程["+ download +"]下载...");
		//下载状态
		final DownloadState state = DownloadState.PAUSE;
		//设置下载状态值。
		download.setState(state.getValue());
		//如果在队列中排序则从队列中移除
		if(this.downloadQueue.contains(download)){
			boolean result = this.downloadQueue.remove(download);
			Log.d(TAG, "课程["+download+"]从队列中移除:" + result);
		}
		//暂停下载
		final MultiThreadDownload multiThreadDownload = this.downloadThreads.get(id);
		if(multiThreadDownload != null){
			//停止下载
			multiThreadDownload.setStop(true);
			//移除下载线程集合
			this.downloadThreads.remove(id);
		}
		//更新到数据库
		this.asyncUpdateDownload(download);
		//发送状态消息
		this.sendUpdateState(id, state);
	}
	/*
	 * 继续下载。
	 * @see com.examw.netschool.service.IDownloadService#continueDownload(java.lang.String)
	 */
	@Override
	public void continueDownload(String id) {
		if(StringUtils.isBlank(id)) return;
		final Download download = this.downloadPosCache.get(id);
		if(download == null) return;
		Log.d(TAG, "继续课程["+ download +"]下载...");
		//添加到下载
		this.addDownload(download);
	}
	//异步更新下载状态
	private void asyncUpdateDownload(final Download download){
		if(download == null) return;
		AppContext.pools_fixed.execute(new Runnable() {
			@Override
			public void run() {
				try{
					Log.d(TAG, "开始异步更新下载数据..");
					if(downloadDao == null){
						Log.d(TAG, "惰性初始化下载数据操作...");
						downloadDao = new DownloadDao(AppContext.getContext(), AppContext.getCurrentUserId());
					}
					//更新数据
					downloadDao.update(download);
				}catch(Exception e){
					Log.e(TAG, "异步更新下载数据异常:" + e.getMessage(), e);
				}
			}
		});
	}
	//异步删除下载课程
	private void asyncDeleteDownload(final Download download){
		if(download == null) return;
		AppContext.pools_fixed.execute(new Runnable() {
			@Override
			public void run() {
				try{
					Log.d(TAG, "开始异步更新下载数据..");
					if(downloadDao == null){
						Log.d(TAG, "惰性初始化下载数据操作...");
						downloadDao = new DownloadDao(AppContext.getContext(), AppContext.getCurrentUserId());
					}
					//删除数据
					downloadDao.delete(download.getLessonId());
				}catch(Exception e){
					Log.e(TAG, "异步更新下载数据异常:" + e.getMessage(), e);
				}
			}
		});
	}
	//检测网络
	private synchronized static final boolean checkNetwork(Context context) throws Exception{
		Log.d(TAG, "开始检查网络...");
		final AppContext appContext =  (AppContext)context.getApplicationContext();
		if(appContext != null){
			switch(appContext.getNetworkType()){
				case WIFI:{//Wi-Fi
					return appContext.isNetworkConnected();
				}
				case CNNET:
				case CNWAP:{
					throw new Exception("当前网络为2G/3G,要下载请修改设置或开启wifi!");
				}
				case NONE:{//没有网络
					throw new Exception("请检查您的网络!");
				}
			}
		}
		return false;
	}
	/**
	 * 课程下载服务接口实现。
	 * @author jeasonyoung
	 *
	 */
	private final class FileDownloadServiceBinder extends Binder implements IDownloadService{
		private static final String TAG = "FileDownloadServiceBinder";
		private final  IDownloadService service;
		/**
		 * 构造函数。
		 * @param service
		 */
		public FileDownloadServiceBinder(IDownloadService service){
			Log.d(TAG, "初始化...");
			this.service = service;
		}
		/*
		 * 设置UI处理Handler。
		 * @see com.examw.netschool.service.DownloadService.IFileDownloadService#setHandler(android.os.Handler)
		 */
		@Override
		public void setHandler(Handler handler) {
			Log.d(TAG, "设置UI处理Handler...");
			this.service.setHandler(handler);
		}
		/*
		 * 添加下载。
		 * @see com.examw.netschool.service.IDownloadService#addDownload(com.examw.netschool.model.Download)
		 */
		@Override
		public void addDownload(Download download) {
			Log.d(TAG, "添加下载["+download+"]...");
			this.service.addDownload(download);
		}
		/*
		 * 取消下载。
		 * @see com.examw.netschool.service.IDownloadService#cancelDownload(java.lang.String)
		 */
		@Override
		public void cancelDownload(String lessonId) {
			Log.d(TAG, "取消下载["+lessonId+"]...");
			this.service.cancelDownload(lessonId);
		}
		/*
		 * 暂停下载。
		 * @see com.examw.netschool.service.IDownloadService#pauseDownload(java.lang.String)
		 */
		@Override
		public void pauseDownload(String lessonId) {
			Log.d(TAG, "暂停下载["+lessonId+"]...");
			this.service.pauseDownload(lessonId);
		}
		/*
		 * 继续下载。
		 * @see com.examw.netschool.service.IDownloadService#continueDownload(java.lang.String)
		 */
		@Override
		public void continueDownload(String lessonId) {
			Log.d(TAG, "继续下载["+lessonId+"]...");
			this.service.continueDownload(lessonId);
		}
	}
}