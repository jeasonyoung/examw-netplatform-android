package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.MyCourse;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
/**
 * 我的课程数据操作。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class MyCourseDao extends BaseDao {
	private final static String TAG = "MyCourseDao";
	private SQLiteDatabase db;
	/**
	 * 删除全部数据。
	 */
	public void deleteAll(){
		Log.d(TAG, "删除全部数据...");
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//删除数据
				db.execSQL("DELETE FROM  tbl_MyCourses");
				//设置事务成功
				db.setTransactionSuccessful();
			}catch (Exception e) {
				Log.e(TAG, "删除全部数据异常:" + e.getMessage(), e);
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
	 * 新增课程。
	 * @param courses
	 */
	public void add(MyCourse [] courses){
		Log.d(TAG, " 新增课程..." + courses.length);
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//新增课程
				for(MyCourse course : courses){
					if(course == null || StringUtils.isBlank(course.getId())) continue;
					db.execSQL("INSERT INTO tbl_MyCourses(id,pid,name,type,orderNo) VALUES (?, ?, ?, ?, ?)", new Object[]{
							StringUtils.trimToNull(course.getId()), 
							StringUtils.trimToNull(course.getPid()),
							StringUtils.trimToNull(course.getName()),
							StringUtils.trimToNull(course.getType()),
							course.getOrderNo()
					});
				}
				//设置事务成功
				db.setTransactionSuccessful();
			} catch (Exception e) {
				Log.e(TAG, "新增课程数据异常:" + e.getMessage(), e);
			} finally{
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
	 * 加载全部课程数据集合。
	 * @return
	 */
	public List<MyCourse> loadCourses(String pid){
		Log.d(TAG, "加载全部课程数据集合...");
		final List<MyCourse> courses = new ArrayList<MyCourse>();
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery("SELECT pid,id,name,type,orderNo FROM tbl_MyCourses WHERE ifnull(pid, '') = ? ORDER BY orderNo", new String[]{  
						StringUtils.trimToEmpty(pid)
				});
				while(cursor.moveToNext()){
					final MyCourse course = new MyCourse();
					//上级课程ID
					course.setPid(StringUtils.trimToNull(cursor.getString(0)));
					//课程ID
					course.setId(StringUtils.trimToNull(cursor.getString(1)));
					//课程名称
					course.setName(cursor.getString(2));
					//类型
					course.setType(cursor.getString(3));
					//排序
					course.setOrderNo(Integer.valueOf(cursor.getInt(4)));
					//添加到集合
					courses.add(course);
				}
				cursor.close();
			} catch (Exception e) {
				Log.e(TAG, "加载数据异常:" + e.getMessage(), e);
			} finally {
				//关闭连接
				if(db != null) db.close();
			}
		}
		return courses;
	}
	/**
	 * 加载班级数据。
	 * @return
	 */
	public List<MyCourse> loadCoursesByClass(){
		Log.d(TAG, "加载全部的班级数据...");
		final List<MyCourse> courses = new ArrayList<MyCourse>();
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery("SELECT DISTINCT id,name,type,orderNo FROM tbl_MyCourses WHERE type = ? ORDER BY orderNo", new String[]{  
						MyCourse.TYPE_CLASS
				});
				while(cursor.moveToNext()){
					final MyCourse course = new MyCourse();
					//上级课程ID
					course.setPid(null);
					//课程ID
					course.setId(StringUtils.trimToNull(cursor.getString(0)));
					//课程名称
					course.setName(cursor.getString(1));
					//类型
					course.setType(cursor.getString(2));
					//排序
					course.setOrderNo(Integer.valueOf(cursor.getInt(3)));
					//添加到集合
					courses.add(course);
				}
				cursor.close();
			} catch (Exception e) {
				Log.e(TAG, "加载数据异常:" + e.getMessage(), e);
			} finally {
				//关闭连接
				if(db != null) db.close();
			}
		}
		return courses;
	}
}