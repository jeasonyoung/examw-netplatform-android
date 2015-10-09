package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.model.DownloadComplete;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 下载数据操作类。
 * @author jeasonyoung
 */
public class DownloadDao extends BaseDao {
	private static final String TAG = "DownloadDao";
	private SQLiteDatabase db;
	/**
	 * 是否存在下载课程资源。
	 * @param lessonId
	 * @return
	 */
	public boolean hasDownload(String lessonId){
		Log.d(TAG, "是否存在下载课程资源["+lessonId+"]...");
		boolean result = false;
		if(StringUtils.isBlank(lessonId)) return result;
		synchronized(dbHelper){
			try{
				final String query = "SELECT COUNT(0) FROM tbl_Downloads WHERE lessonId = ? ";
				Log.d(TAG, "sql:" + query);
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery(query, new String[]{ StringUtils.trimToEmpty(lessonId) });
				if(cursor.moveToFirst()){
					result = cursor.getInt(0) > 0;
				}
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(),	e);
			}finally{
				if(db != null) db.close();
			}
		}
		return result;
	}
	/**
	 * 加载下载数据。
	 * @param state
	 * @return
	 */
	public List<Download> loadDownloads(DownloadState state){
		Log.d(TAG, "加载下载状态["+ state+"]数据...");
		if(state == null) return null;
		final List<Download> list = new ArrayList<Download>();
		synchronized(dbHelper){
			try{
				final String query = "SELECT a.lessonId,b.name,a.filePath,a.fileSize,a.state FROM tbl_Downloads a "
						+ " INNER JOIN tbl_Lessones b ON b.id = a.lessonId "
						+ " WHERE a.state = ? ";
				Log.d(TAG, "sql:" + query);
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(state.getValue()) });
				while(cursor.moveToNext()){
					//添加到数据集合
					list.add(this.createDownload(cursor));
				}
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(),	e);
			}finally{
				if(db != null) db.close();
			}
		}
		return list;
	}
	/**
	 * 加载正在的下载数据。
	 * @param state
	 * @return
	 */
	public List<DownloadComplete> loadDownings(){
		Log.d(TAG, " 加载正在的下载数据...");
		final List<DownloadComplete> list = new ArrayList<DownloadComplete>();
		synchronized(dbHelper){
			try{
				final String query = "SELECT a.lessonId,b.name,a.filePath,a.fileSize,a.state,IFNULL(SUM(completeSize),0) completeSize FROM tbl_Downloads a "
						+ " INNER JOIN tbl_Lessones b ON b.id = a.lessonId "
						+ " LEFT OUTER JOIN tbl_Downing c ON c.lessonId = a.lessonId "
						+ " WHERE a.state <> ? "
						+ " GROUP BY a.lessonId,b.name,a.filePath,a.fileSize,a.state";
				Log.d(TAG, "sql:" + query);
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery(query, new String[]{ String.valueOf(DownloadState.FINISH.getValue()) });
				while(cursor.moveToNext()){
					//初始化
					final DownloadComplete data = new DownloadComplete(this.createDownload(cursor));
					//设置下载量
					data.setCompleteSize(cursor.getLong(5));
					//添加到数据集合
					list.add(data);
				}
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(),	e);
			}finally{
				if(db != null) db.close();
			}
		}
		return list;
	}
	//创建数据转换
	private Download createDownload(final Cursor cursor){
		 //初始化
		final Download data = new Download();
		//0.课程资源ID 
		data.setLessonId(cursor.getString(0));
		//1.课程资源名称
		data.setLessonName(cursor.getString(1));
		//2.下载文件路径
		data.setFilePath(cursor.getString(2));
		//3.下载文件大小
		data.setFileSize(cursor.getLong(3));
		//4.下载状态
		data.setState(cursor.getInt(4));
		//
		return data;
	}
	/**
	 * 获取下载数据。
	 * @param lessonId
	 * @return
	 */
	public Download getDownload(String lessonId){
		Log.d(TAG, "加载课程资源["+lessonId+"]下载数据...");
		Download data = null;
		if(StringUtils.isBlank(lessonId)) return data;
		synchronized(dbHelper){
			try{
				final String query = "SELECT a.lessonId,b.name,a.filePath,a.fileSize,a.state FROM tbl_Downloads a "
						+ " INNER JOIN tbl_Lessones b ON b.id = a.lessonId "
						+ " WHERE a.lessonId = ? ";
				Log.d(TAG, "sql:" + query);
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery(query, new String[]{ lessonId });
				if (cursor.moveToFirst()){
					data = this.createDownload(cursor);
				}
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(),	e);
			}finally{
				if(db != null) db.close();
			}
		}
		return data;
	}
	/**
	 * 添加课程资源下载。
	 * @param data
	 * @return
	 */
	public boolean add(Download data){
		boolean result = false;
		if(data == null) return result;
		Log.d(TAG, "创建课程资源["+data.getLessonId()+"]下载数据...");
		if(StringUtils.isBlank(data.getLessonId())) return result;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("INSERT INTO tbl_Downloads(lessonId,filePath,fileSize,state) VALUES(?,?,?,?)", new Object[]{
					data.getLessonId(),data.getFilePath(),data.getFileSize(), data.getState()	
				});
				//提交事务
				db.setTransactionSuccessful();
				result = true;
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null){
					//结束事务
					db.endTransaction();
					//关闭连接
					db.close();
				}
			}
		}
		return result;
	}
	/**
	 * 更新数据。
	 * @param data
	 * @return
	 */
	public boolean update(Download data){
		boolean result = false;
		if(data == null) return result;
		Log.d(TAG, "更新课程资源["+data.getLessonId()+"]下载数据...");
		if(StringUtils.isBlank(data.getLessonId())) return result;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("UPDATE tbl_Downloads SET filePath = ?,fileSize = ?,state = ? WHERE lessonId = ?", new Object[]{
					data.getFilePath(), data.getFileSize(), data.getState(),data.getLessonId()
				});
				//提交事务
				db.setTransactionSuccessful();
				result = true;
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null){
					//结束事务
					db.endTransaction();
					//关闭连接
					db.close();
				}
			}
		}
		return result;
	}
	/**
	 * 更新数据状态。
	 * @param lessonId
	 * @param state
	 */
	public void update(String lessonId, DownloadState state){
		Log.d(TAG, "更新课程资["+lessonId+"]源状态");
		if(StringUtils.isBlank(lessonId) || state == null) return;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("UPDATE tbl_Downloads SET state = ? WHERE lessonId = ?", new Object[]{ state.getValue(), lessonId });
				//提交事务
				db.setTransactionSuccessful();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null){
					//结束事务
					db.endTransaction();
					//关闭连接
					db.close();
				}
			}
		}
	}
	/**
	 * 删除数据。
	 * @param lessonId
	 */
	public void delete(String lessonId){
		Log.d(TAG, "删除数据..." + lessonId);
		if(StringUtils.isBlank(lessonId)) return;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("DELETE FROM tbl_Downloads WHERE lessonId = ?", new Object[]{ lessonId });
				//提交事务
				db.setTransactionSuccessful();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null){
					//结束事务
					db.endTransaction();
					//关闭连接
					db.close();
				}
			}
		}
	}
}