package com.examw.netschool.dao;

import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;

/**
 * 下载进度数据操作类。
 * @author jeasonyoung
 *负责视频文件多线程下载的过程的记录，以便进行断点下载。
 */
public class DownloadDao {
	private static final String TAG = "DownloadDao";
	private MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context 上下文。
	 */
	public DownloadDao(Context context) {
		this.dbHelper = new MyDBHelper(context);
	}
	/**
	 *  获取每条线程已经下载的文件长度。
	 * @param url
	 * @param userName
	 * @return
	 */
	@SuppressLint("UseSparseArrays") 
	public  Map<Integer, Long> loadAllData(String url, String userName){
		Log.d(TAG, "获取每条线程已经下载的文件长度...");
		final String query_sql = "select thread_id,complete_size from DownloadTab where url =? and username = ?";
		Map<Integer, Long> map = new HashMap<Integer, Long>();
 		SQLiteDatabase db =  this.dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(query_sql, new String[]{url,userName});
		while(cursor.moveToNext()){
			map.put(Integer.valueOf(cursor.getInt(0)), Long.valueOf(cursor.getLong(1)));
		}
		//关闭游标
		cursor.close();
		//关闭链接
		db.close();
		return map;
	}
	/**
	 * 保存每条线程已经下载的文件长度
	 * @param url
	 * @param userName
	 * @param map
	 */
	public void save(String url,String userName,Map<Integer, Long> map){
		Log.d(TAG, "开始保存每条线程已经下载的文件长度...");
		final String insert_sql = "insert into DownloadTab(thread_id,complete_size,url,username)values(?,?,?,?)";
		SQLiteDatabase db = null;
		try {
			if(map == null || map.size() == 0)return;
			db = this.dbHelper.getWritableDatabase();
			db.beginTransaction();
			for(Map.Entry<Integer, Long> entry : map.entrySet()){
				db.execSQL(insert_sql, new Object[]{entry.getKey(),entry.getValue(),url, userName });
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, "保存每条线程已经下载的文件长度时发生异常：" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
	/**
	 * 实时更新每条线程已下载的文件长度。
	 * @param url
	 * @param userName
	 * @param threadId
	 * @param pos
	 */
	public void update(String url,String userName, int threadId, long pos){
		Log.d(TAG, "开始更新线程["+threadId+"]已下载的文件长度["+pos+"]...");
		final String update_sql = "update DownloadTab set complete_size=?  where url=? and username=? and thread_id=?";
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			db.beginTransaction();
			db.execSQL(update_sql,new Object[] { pos, url, userName, threadId });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.d(TAG, "更新线程["+threadId+"]下载量["+pos+"]发生异常:" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
	/**
	 * 更新下载课程文件信息
	 * @param url
	 * @param userName
	 * @param filePath
	 * @param fileSize
	 */
	public void updateDowningCourseFile(String url,String userName,String filePath, long fileSize){
		Log.d(TAG, "更新下载课程文件信息...");
		final String update_sql = "update CourseTab set filesize=?,filepath=?,state = 1 where fileurl = ? and username = ? ";
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			db.beginTransaction();
			db.execSQL(update_sql, new Object[]{ fileSize, filePath, url, userName });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.d(TAG, "更新下载课程文件信息异常:" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
	/**
	 * 删除下载记录
	 * @param url
	 * @param userName
	 */
	public void delete(String url,String userName){
		Log.d(TAG, "开始删除课程["+url+"]下载记录...");
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			db.beginTransaction();
			this.deleteAllRecord(db, url, userName);
			db.setTransactionSuccessful();
		} catch (Exception e) { 
			Log.e(TAG, "删除课程["+url+"]下载记录发生异常：" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
	//删除全部下载记录。
	private void deleteAllRecord(SQLiteDatabase db, String url,String userName){
		final String delete_sql = "delete from DownloadTab where url=? and username=?";
		if(db == null) return;
		db.execSQL(delete_sql, new Object[]{url, userName});
	}
	/**
	 * 更新课程完成的数据
	 * @param url
	 * @param userName
	 * @param finishSize
	 * @param fileSize
	 */
	public void updateCourseFinish(String url,String userName,long finishSize, long fileSize){
		Log.d(TAG, "更新课程["+url+"]完成文件下载量["+finishSize+"/"+fileSize+"]...");
		final String update_sql = "update CourseTab set finishsize=?,filesize=?  where fileurl=? and username=?";
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			db.beginTransaction();
			db.execSQL(update_sql, new Object[]{finishSize,fileSize, url, userName});
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.d(TAG, "更新课程["+url+"]完成文件下载量["+finishSize+"/"+fileSize+"]异常:" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
	/**
	 * 完成文件下载后，删除对应的下载记录，更新课程信息。
	 * @param url
	 * @param userName
	 * @param filePath
	 */
	public void finish(String url,String userName, String filePath){
		Log.d(TAG, "更新完成课程["+url+"]文件下载["+filePath+"]操作...");
		final String update_sql = "update CourseTab set filepath=?, state=2 where fileurl=? and username=?";
		SQLiteDatabase db = null;
		try {
			db = this.dbHelper.getWritableDatabase();
			//开始事务处理
			db.beginTransaction();
			//删除下载记录
			this.deleteAllRecord(db, url, userName);
			//更新数据
			db.execSQL(update_sql, new Object[]{filePath, url, userName});
			//提交事务
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, "更新完成课程["+url+"]下载后发生异常：" + e.getMessage(), e);
		}finally{
			if(db != null){
				db.endTransaction();
				db.close();
			}
		}
	}
}