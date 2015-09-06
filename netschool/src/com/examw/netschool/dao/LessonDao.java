package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.model.Lesson;

/**
 * 课程资源数据操作。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class LessonDao {
	private static final String TAG = "LessonDao";
	private final MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 */
	public LessonDao(Context context, String userId){
		Log.d(TAG, "初始化...");
		this.dbHelper = new MyDBHelper(context, userId);
	}
	/**
	 * 删除班级ID下的课程资源数据。
	 * @param classId
	 */
	public void deleteByClass(String classId){
		Log.d(TAG, "删除班级ID["+classId+"]下的课程资源数据...");
		SQLiteDatabase db = null;
		try {
			//初始化
			db = this.dbHelper.getWritableDatabase();
			//开启事务
			db.beginTransaction();
			//删除数据
			db.execSQL("DELETE FROM  tbl_Lessones WHERE class_id = ?", new Object[]{ StringUtils.trimToEmpty(classId) });
			//设置事务成功
			db.setTransactionSuccessful();
		}catch (Exception e) {
			Log.e(TAG, "删除班级ID["+classId+"]下的课程资源数据异常:" + e.getMessage(), e);
		}finally{
			if(db != null){
				//结束事务
				db.endTransaction();
				//关闭连接
				db.close();
			}
		}
	}
	/**
	 * 新增班级ID下的课程资源数据集合。
	 * @param classId
	 * @param lessons
	 */
	public void add(String classId, Lesson[] lessons){
		Log.d(TAG, "新增班级ID["+classId+"]下的课程资源数据集合...");
		if(StringUtils.isBlank(classId) || lessons == null || lessons.length == 0) return;
		SQLiteDatabase db = null;
		try {
			//初始化
			db = this.dbHelper.getWritableDatabase();
			//开启事务
			db.beginTransaction();
			//新增数据
			for(Lesson lesson : lessons){
				if(lesson == null || StringUtils.isBlank(lesson.getId())) continue;
				db.execSQL("INSERT INTO tbl_Lessones(id,class_id,name,videoUrl,highVideoUrl,superVideoUrl,time,orderNo) values (?,?,?,?,?,?,?,?)", new Object[]{
						StringUtils.trimToEmpty(lesson.getId()), StringUtils.trimToEmpty(classId), StringUtils.trimToEmpty(lesson.getName()),
						StringUtils.trimToEmpty(lesson.getVideoUrl()), StringUtils.trimToEmpty(lesson.getHighVideoUrl()), StringUtils.trimToEmpty(lesson.getSuperVideoUrl()),
						lesson.getTime(), lesson.getOrderNo()
				});
			}
			//设置事务成功
			db.setTransactionSuccessful();
		}catch (Exception e) {
			Log.e(TAG, "新增班级ID["+classId+"]下的课程资源数据集合异常:" + e.getMessage(), e);
		}finally{
			if(db != null){
				//结束事务
				db.endTransaction();
				//关闭连接
				db.close();
			}
		}
	}
	/**
	 * 加载班级下的课程资源数据。
	 * @param classId
	 * @return
	 */
	public List<Lesson> loadLessonsByClass(String classId){
		Log.d(TAG, "加载班级["+classId+"]下的课程资源数据...");
		final List<Lesson> lessons = new ArrayList<Lesson>();
		if(StringUtils.isBlank(classId)) return lessons;
		SQLiteDatabase db = null;
		try {
			//初始化
			db = this.dbHelper.getReadableDatabase();
			//查询数据
			final Cursor cursor = db.rawQuery("SELECT id,name,videoUrl,highVideoUrl,superVideoUrl,time,orderNo FROM tbl_Lessones WHERE class_id = ? ORDER BY orderNo", new String[]{  
					StringUtils.trimToEmpty(classId)
			});
			while(cursor.moveToNext()){
				final Lesson lesson = new Lesson();
				//课程资源ID
				lesson.setId(StringUtils.trimToNull(cursor.getString(0)));
				//课程资源名称
				lesson.setName(StringUtils.trimToNull(cursor.getString(1)));
				//课程资源视频URL
				lesson.setVideoUrl(StringUtils.trimToNull(cursor.getString(2)));
				//课程资源高清视频URL
				lesson.setHighVideoUrl(StringUtils.trimToNull(cursor.getString(3)));
				//课程资源超清视频URL
				lesson.setSuperVideoUrl(StringUtils.trimToNull(cursor.getString(4)));
				//考试时长
				lesson.setTime(Integer.valueOf(cursor.getInt(5)));
				//排序号
				lesson.setOrderNo(Integer.valueOf(cursor.getInt(6)));
				//添加到集合
				lessons.add(lesson);
			}
			cursor.close();
		} catch (Exception e) {
			Log.e(TAG, "加载数据异常:" + e.getMessage(), e);
		} finally {
			//关闭连接
			if(db != null) db.close();
		} 
		return lessons;
	}
}