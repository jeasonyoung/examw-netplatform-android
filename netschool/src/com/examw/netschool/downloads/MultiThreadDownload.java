package com.examw.netschool.downloads;

import java.io.File;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.util.StringUtils;

/**
 * 文件下载服务实现。
 * @author jeasonyoung
 * 负责视频文件下载以及安全加密处理。
 */
public class MultiThreadDownload {
	private static final String TAG = "FileDownloadService";
	private static final int DOWNLOAD_THREADS = 4;//下载线程数
	private static final long THREAD_SLEEP = 900;//
	private static final int CONNECT_TIMEOUT = 5000;//链接超时
	private static final int CONNECT_SUCCESS = 200;//链接成功
	private static final String CONNECT_METHOD_GET  = "GET";//get链接
	private static final int NET_BUFFER_SIZE = 1024;//
	
	private File savePath;
	private String userName,url;
	private HttpURLConnection conn;
	private DownloadDao downloadDao;
	private long fileSize,block,totalDownloadSize;
	private boolean isStop;
	//缓存各线程下载的长度
	private Map<Integer, Long> threadsPosCache;
	//下载线程
	private DownloadThread[] threads;
	//线程池
    private ExecutorService pools;
    /**
     * 构造函数。
     */
    private MultiThreadDownload(){
    	this.threadsPosCache = new ConcurrentHashMap<Integer, Long>();
    	this.pools = Executors.newCachedThreadPool();
    }
	/**
	 * 构造函数。
	 * @param context
	 * @param url
	 */
	public MultiThreadDownload(Context context,String userName, String url) throws Exception{
		this();
		if(context == null) throw new IllegalArgumentException("context");
		if((StringUtils.isEmpty(this.userName = userName))) throw new IllegalArgumentException("userName");
		if(StringUtils.isEmpty((this.url = url))) throw new IllegalArgumentException("url");
		
		this.downloadDao = new DownloadDao(context);
 
		this.conn = (HttpURLConnection)(new URL(this.url).openConnection());
		this.conn.setConnectTimeout(CONNECT_TIMEOUT);
		this.conn.setRequestMethod(CONNECT_METHOD_GET);
		this.createRequestParameters(this.conn);
		this.conn.connect();
		int respCode = -1;
		if((respCode = conn.getResponseCode()) == CONNECT_SUCCESS){
			//根据响应获取文件大小
			this.fileSize = conn.getContentLength();
			if(this.fileSize <= 0) throw new Exception("Unkown file size.");
		}else {
			throw new Exception("server no respose[" + respCode + "].");
		}
	}
	
	/**
	 * 设置保存文件
	 * @param savePath
	 */
	public void setSavePath(File savePath) throws Exception {
		this.savePath = savePath;
		if(this.savePath == null){
			throw new Exception("下载文件保存路径不存在！");
		}
		if(!this.savePath.exists()){//如果不存在则创建目录
			this.savePath.mkdirs();
		}
		if(this.savePath.isDirectory() && this.conn != null){//如果是目录则创建文件名称
			this.savePath =  new File(this.savePath, this.getFileName(this.conn));
		}
		//更新下载课程文件信息
		this.downloadDao.updateDowningCourseFile(url, userName, this.savePath.getAbsolutePath(), this.fileSize);
	}
	//创建请求参数集合
	private void createRequestParameters(HttpURLConnection conn){
		if(conn == null)return;
		conn.setRequestProperty("Accept-Language", "zh-CN");
		conn.setRequestProperty("Referer", this.url);
		conn.setRequestProperty("Charset", "UTF-8");
		conn.setRequestProperty("Connection", "Keep-Alive");
	}
	//获取文件名
	private String getFileName(HttpURLConnection conn){
		String fileName = this.url.substring(this.url.lastIndexOf('/') + 1);
		if(StringUtils.isEmpty(fileName) && conn != null){
			String mine = null;
			for(int i = 0; ; i++){
				mine = conn.getHeaderField(i);
				if(mine == null)break;
				if(conn.getHeaderFieldKey(i).equalsIgnoreCase("content-disposition")){
					Matcher m = Pattern.compile(".*filename=(.*)").matcher(mine.toLowerCase(Locale.getDefault()));
					if(m.find()) return m.group(1);
				}
			}
			fileName = UUID.randomUUID() + ".tmp";
		}
		return fileName;
	}
	/**
	 * 获取文件大小。
	 * @return 文件大小。
	 */
	public long getFileSize() {
		return fileSize;
	}
	/**
	 * 累计已下载大小
	 * @param size
	 */
	protected synchronized void append(long size){
		this.totalDownloadSize += size;
	}
	/**
	 * 更新指定线程最后下载的位置。
	 * @param threadId 线程ID
	 * @param pos 最后下载的位置
	 */
	protected void update(final int threadId,final long  pos){
		//更新线程下载长度缓存。
		threadsPosCache.put(threadId, pos);
	}
	//初始化下载线程
	private void initDownloadThreads(){
		this.totalDownloadSize = 0;
		this.threadsPosCache.clear();
		int len = 0;
		//从数据库加载数据，以支持断点下载
		Map<Integer, Long> logDataMap = this.downloadDao.loadAllData(this.url, this.userName);
		if((len = logDataMap.size()) > 0){//存在下载记录(断点下载)
			this.threads = new DownloadThread[len];
			for(Map.Entry<Integer, Long> entry : logDataMap.entrySet()){
				//更新到下载缓存
				this.threadsPosCache.put(entry.getKey(), entry.getValue());
				//累加已下载的长度;
				this.append(entry.getValue());
			}
		}else {//不存在下载记录
			this.threads = new DownloadThread[DOWNLOAD_THREADS];
			for(int i = 0; i < DOWNLOAD_THREADS; i++){
				//创建下载缓存
				this.threadsPosCache.put(i + 1, Long.valueOf((long)0));
			}
			//添加数据库线程下载
			this.downloadDao.save(this.url, this.userName, this.threadsPosCache);
		}
		//计算每条线程下载的数据长度
		len =  this.threads.length;
		this.block = (this.fileSize % len) == 0 ? (this.fileSize / len) : (this.fileSize/len + 1);
	}
	/**
	 * 开始下载文件
	 * @param listener 监听下载数据的变化。
	 * @return 已下载文件大小
	 * @throws Exception
	 */
	public long download(OnDownloadProgressListener listener) throws Exception{
		 try {
			 if(this.savePath == null){
				 throw new Exception("下载文件保存地址不存在!");
			 }
			 Log.d(TAG, "开始下载文件:"+ this.url);
			 this.isStop = false;
			//初始化下载线程
			this.initDownloadThreads();
			 //如果保存文件不存在则创建
			 if(this.savePath.isFile() && !this.savePath.exists()){
				 RandomAccessFile out = new RandomAccessFile(this.savePath, "rw");
				 if(this.fileSize > 0) out.setLength(this.fileSize);
				 out.close();
			 }
			if(this.totalDownloadSize > 0 && listener != null){//初始化设置进度
				if(this.totalDownloadSize >= this.fileSize){
					this.totalDownloadSize = 0;
				}
				//通知目前已下载完成的数据长度。
				listener.onDownloadSize(this.totalDownloadSize);
			}
			//开启线程进行下载
			int threadCount = this.threads.length;
			for(int i = 0; i < threadCount; i++){
				long downLength = this.threadsPosCache.get(i + 1);
				//判断线程是否已经完成下载，否则继续下载
				if(downLength < this.block && this.totalDownloadSize < this.fileSize){
					this.threads[i] = new DownloadThread(i + 1, downLength);
					//执行线程。
					this.pools.execute(this.threads[i]);
				}else {
					this.threads[i] = null;
				}
			}
			//循环检查各线程下载状态
			long oldTotal = this.totalDownloadSize;
			boolean isFinish = false;
			while(!isFinish){//守候循环
				Thread.sleep(THREAD_SLEEP);
				isFinish = true;
				//循环检查线程下载情况
				for(int i = 0; i < threadCount; i++){
					if(this.threads[i] != null && !this.threads[i].isFinish()){//如果发现线程未完成下载
						//设置标志为下载没有完成
						isFinish = false;
						if(this.threads[i].getDownLength() == -1){//下载失败,再重新下载
							this.threads[i] = new DownloadThread(i + 1, this.threadsPosCache.get(i + 1));
							//执行线程。
							this.pools.execute(this.threads[i]);
						}
					}
				}
				//更新下载进度
				if(this.totalDownloadSize > oldTotal){
					oldTotal = this.totalDownloadSize;
					//更新线程下载数据库数据
					if(this.downloadDao != null && this.threadsPosCache.size() > 0){
						for(Map.Entry<Integer, Long> entry : this.threadsPosCache.entrySet()){
							this.downloadDao.update(this.url, this.userName, entry.getKey(), entry.getValue() == null ? 0 : entry.getValue());
						}
					}
					//通知目前已下载完成的数据长度。
					if(listener != null){
						listener.onDownloadSize(this.totalDownloadSize);
					}
				}
			}
			Log.d(TAG, "下载完成："+this.totalDownloadSize+"/" + this.fileSize);
			//更新课程下载量
			this.downloadDao.updateCourseFinish(this.url, this.userName, this.totalDownloadSize,this.fileSize);
			//已下载完成
			if(this.totalDownloadSize == this.fileSize){
				//更新数据库
				this.downloadDao.finish(this.url, this.userName, this.savePath.getAbsolutePath());
				//数据文件加密处理
				encryptFile(this.savePath, 0, this.userName.getBytes("UTF-8"));
				//循环停止
				isFinish = true;
			}
		} catch (Exception e) {
			Log.e(TAG, "下载文件发生异常:" + e.getMessage(), e);
		}
		 return this.totalDownloadSize;
	}
	/**
	 * 加密/解密文件。
	 * @param file 下载的文件
	 * @param skip 插入的位置
	 * @param data 密钥
	 */
	public synchronized static void encryptFile(File file, long skip, byte[] keys){
		if(file == null || skip < 0 || keys.length == 0)return;
		Log.d(TAG, "开始对文件进行加密处理..");
		try {
			RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			if(skip < 0 || skip > accessFile.length()){
				accessFile.close();
				Log.d(TAG, "加密跳过字节数无效...");
				return;
			}
			//密钥长度
			int len = keys.length;
			if(skip + len > accessFile.length()){
				accessFile.close();
				Log.d(TAG, "密钥长度("+len+")＋跳过长度("+skip+") > 文件长度("+accessFile.length()+")!");
				return;
			}
			//加密运算
			int source,encrypt;
			for(int i = 0;  i < len; i++){
				//指定位置
				accessFile.seek(skip + i);
				//读取数据后指针下移
				source = accessFile.read();
				encrypt = source ^ (int)keys[i];
				//重新指定位置
				accessFile.seek(skip + i);
				//用密文替换原文
				accessFile.write(encrypt);
			}
			accessFile.close();
			Log.d(TAG, "文件加密完成！");
		} catch (Exception e) {
			Log.e(TAG, "文件加密异常:" + e.getMessage(), e);
		}
	}
	/**
	 * 停止下载。
	 */
	public void Stop(){
		this.isStop = true;
	}
	/**
	 * 文件下载线程。
	 * @author jeasonyoung
	 *
	 */
	private final class DownloadThread extends Thread{
		private static final String TAG = "DownloadThread";
		private int threadId;
		private long downLength;
		private boolean finish;
		/**
		 * 构造函数。
		 * @param threadId 线程ID。
		 * @param downLength 已下载长度。
		 */
		public DownloadThread(int threadId,long downLength){
			this.finish = false;
			this.threadId = threadId;
			this.downLength =  downLength;
		}
		/**
		 * 获取是否完成。
		 * @return
		 */
		public boolean isFinish() {
			return finish;
		}
		/**
		 * 获取下载长度。
		 * @return
		 */
		public long getDownLength() {
			return downLength;
		}
		/*
		 * 分段下载文件
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			Log.d(TAG, "[线程:"+this.threadId+"]开始下载文件...");
			//线程停止。
			if(isStop)return;
			//已下载完成。
			if(this.downLength >= block){
				this.finish = true;
				return;
			}
			try {
				long startPos = block * (this.threadId - 1) + this.downLength,//开始位置
						endPos = block *this.threadId -1;//结束位置
				//创建连接
				HttpURLConnection http = (HttpURLConnection)((new URL(url)).openConnection());
				http.setConnectTimeout(CONNECT_TIMEOUT);
				http.setRequestMethod(CONNECT_METHOD_GET);
				//设置参数
				createRequestParameters(http);
				//设置获取数据的范围
				http.setRequestProperty("Range", "bytes=" + startPos + "-" + endPos);
				
				Log.d(TAG, "Thread: " + this.threadId + ": start download from start pos:[" + startPos + "/" + endPos + "]...");
				//获取输入流数据
				InputStream inStream = http.getInputStream();
				byte[] buffer = new byte[NET_BUFFER_SIZE];
				int offset = 0;
				RandomAccessFile threadFile = new RandomAccessFile(savePath, "rwd");
				threadFile.seek(startPos);
				while(!isStop && (offset = inStream.read(buffer, 0, buffer.length)) > 0){
					//写入文件
					threadFile.write(buffer,0,offset);
					//累加已下载长度
					this.downLength += offset;
					//更新数据库中各线程下载量
					update(this.threadId, this.downLength);
					//追加到整个文件下载量
					append(offset);
				}
				threadFile.close();
				inStream.close();
				Log.d(TAG, "Thread :" + this.threadId + " download finish.");
			} catch (Exception e) {
				this.downLength = -1;
				Log.e(TAG, "[线程:"+this.threadId+"]下载数据时发生异常：" + e.getMessage(), e);
			}finally{
				this.finish = true;
			}
		}
	}
	/**
	 * 下载进度监听器接口。
	 * @author jeasonyoung
	 * 向监听对象发生下载进度数据
	 */
	public interface OnDownloadProgressListener{
		/**
		 * 下载的数据量。
		 * @param size
		 */
		public void onDownloadSize(long size);
	}
}