package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.PackageClass;

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
	public void add(PackageClass [] courses){
		Log.d(TAG, " 新增课程..." + courses.length);
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//新增课程
				for(PackageClass course : courses){
					if(course == null || StringUtils.isBlank(course.id)) continue;
					db.execSQL("INSERT INTO tbl_MyCourses(id,pid,name,type,orderNo) VALUES (?, ?, ?, ?, ?)", new Object[]{
							StringUtils.trimToNull(course.id), 
							StringUtils.trimToNull(course.pid),
							StringUtils.trimToNull(course.name),
							StringUtils.trimToNull(course.type),
							course.order_no
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
	public List<PackageClass> loadCourses(String pid){
		Log.d(TAG, "加载全部课程数据集合...");
		final List<PackageClass> courses = new ArrayList<PackageClass>();
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery("SELECT pid,id,name,type,orderNo FROM tbl_MyCourses WHERE ifnull(pid, '') = ? ORDER BY orderNo", new String[]{  
						StringUtils.trimToEmpty(pid)
				});
				while(cursor.moveToNext()){
					final PackageClass course = new PackageClass();
					//上级课程ID
					course.pid = StringUtils.trimToNull(cursor.getString(0));
					//课程ID
					course.id = StringUtils.trimToNull(cursor.getString(1));
					//课程名称
					course.name = cursor.getString(2);
					//类型
					course.type = cursor.getString(3);
					//排序
					course.order_no = Integer.valueOf(cursor.getInt(4));
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
	public List<PackageClass> loadCoursesByClass(){
		Log.d(TAG, "加载全部的班级数据...");
		final List<PackageClass> courses = new ArrayList<PackageClass>();
		synchronized(dbHelper){
			try {
				//初始化
				db = dbHelper.getReadableDatabase();
				//查询数据
				final Cursor cursor = db.rawQuery("SELECT DISTINCT id,name,type FROM tbl_MyCourses WHERE type = ? ORDER BY name", new String[]{  
						PackageClass.TYPE_CLASS
				});
				while(cursor.moveToNext()){
					final PackageClass course = new PackageClass();
					//上级课程ID
					course.pid = null;
					//课程ID
					course.id = StringUtils.trimToNull(cursor.getString(0));
					//课程名称
					course.name = cursor.getString(1);
					//类型
					course.type = cursor.getString(2);
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