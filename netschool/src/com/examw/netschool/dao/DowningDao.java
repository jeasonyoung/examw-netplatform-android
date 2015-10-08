package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.Downing;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 下载进程数据操作。
 * 负责视频文件多线程下载的过程的记录，以便进行断点下载。
 * @author jeasonyoung
 * @since 2015年9月11日
 */
public class DowningDao extends BaseDao {
	private static final String TAG = "DowningDao";
	private SQLiteDatabase db;
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 */
	public DowningDao(Context context, String userId) {
		super(context, userId);
		Log.d(TAG, "初始化...");
	}
	/**
	 * 构造函数。
	 * @param dao
	 */
	public DowningDao(BaseDao dao){
		super(dao);
		Log.d(TAG, "初始化...");
	}
	/**
	 * 是否存在课程资源下载。
	 * @param lessonId
	 * @return
	 */
	public boolean hasDowning(String lessonId){
		Log.d(TAG, "是否存在课程资源下载..." + lessonId);
		boolean result = false;
		if(StringUtils.isBlank(lessonId)) return result;
		try{
			final String query = "SELECT COUNT(0) FROM tbl_Downing WHERE lessonId = ? ";
			Log.d(TAG, "sql:" + query);
			//初始化
			db = dbHelper.getReadableDatabase();
			//查询数据
			final Cursor cursor = db.rawQuery(query, new String[]{ StringUtils.trimToEmpty(lessonId) });
			while(cursor.moveToNext()){
				result = cursor.getInt(0) > 0;
				break;
			}
			cursor.close();
		}catch(Exception e){
			Log.e(TAG, "发生异常:" + e.getMessage(),	e);
		}finally{
			if(db != null) db.close();
		}
		return result;
	}
	/**
	 * 加载课程资源数据下载进程。
	 * @param lessonId
	 * 课程资源ID
	 * @return
	 */
	public List<Downing> loadDowningByLesson(String lessonId){
		Log.d(TAG, "加载课程资源["+lessonId+"]数据下载进程...");
		if(StringUtils.isBlank(lessonId)) return null;
		final List<Downing> list = new ArrayList<Downing>();
		try{
			final String query = "SELECT threadId,startPos,endPos,completeSize FROM tbl_Downing WHERE lessonId = ? ";
			Log.d(TAG, "sql:" + query);
			//初始化
			db = this.dbHelper.getReadableDatabase();
			//查询数据
			final Cursor cursor = db.rawQuery(query, new String[]{ StringUtils.trimToEmpty(lessonId) });
			while(cursor.moveToNext()){
				//初始化
				final Downing data = new Downing();
				//0.课程资源ID
				data.setLessonId(lessonId);
				//1.线程ID
				data.setThreadId(cursor.getInt(0));
				//2.起始位置
				data.setStartPos(cursor.getLong(1));
				//3.结束位置
				data.setEndPos(cursor.getLong(2));
				//4.完成下载
				data.setCompleteSize(cursor.getLong(3));
				//添加到集合
				list.add(data);
			}
			cursor.close();
		}catch(Exception e){
			Log.e(TAG, "发生异常:" + e.getMessage(), e);
		}finally{
			if(db != null) db.close();
		}
		return list;
	}
	/**
	 * 新增下载线程数据。
	 * @return
	 */
	public boolean add(Downing data){
		boolean result = false;
		if(data == null) return result;
		Log.d(TAG, "创建课程资源["+data.getLessonId()+"]下载线程["+data.getThreadId()+"]数据...");
		if(StringUtils.isBlank(data.getLessonId()) || data.getThreadId() < 0) return result;
		synchronized (dbHelper) {
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//插入数据
				this.insert(db, data);
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
	//插入数据
	private synchronized void insert(final SQLiteDatabase db, Downing downing){
		if(db == null || downing == null) return;
		Log.d(TAG, "插入课程资源["+downing.getLessonId()+"]下载线程["+downing.getThreadId()+"]数据...");
		if(StringUtils.isBlank(downing.getLessonId()) || downing.getThreadId() < 0) return;
		//新增数据
		db.execSQL("insert into tbl_Downing(lessonId,threadId,startPos,endPos,completeSize) values(?,?,?,?,?)", new Object[]{
				downing.getLessonId(),downing.getThreadId(),downing.getStartPos(),downing.getEndPos(), downing.getCompleteSize()	
		});
	}
	/**
	 * 新增下载线程数据集合。
	 * @param list
	 */
	public void add(List<Downing> list){
		Log.d(TAG, "批量添加下载数据集合...");
		if(list == null || list.size() == 0) return;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				for(Downing downing : list){
					if(downing == null) continue;
					this.insert(db, downing);
				}
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
	 * 更新数据。
	 * @param data
	 * @return
	 */
	public boolean update(Downing data){
		boolean result = false;
		if(data == null) return result;
		Log.d(TAG, "更新课程资源["+data.getLessonId()+"]下载线程["+data.getThreadId()+"]数据...");
		if(StringUtils.isBlank(data.getLessonId()) || data.getThreadId() < 0) return result; 
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("UPDATE tbl_Downing SET startPos = ?,endPos = ?,completeSize = ? WHERE lessonId = ? AND threadId = ?", new Object[]{
					data.getStartPos(), data.getEndPos(), data.getCompleteSize(),data.getLessonId(),data.getThreadId()
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
	 * 删除数据。
	 * @param lessonId
	 */
	public void deleteByLesson(String lessonId){
		Log.d(TAG, "更新课程资源["+lessonId+"]下载线程数据...");
		if(StringUtils.isBlank(lessonId)) return;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开始事务
				db.beginTransaction();
				//新增数据
				db.execSQL("DELETE FROM tbl_Downing WHERE lessonId = ? ", new Object[]{
					StringUtils.trimToEmpty(lessonId)
				});
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