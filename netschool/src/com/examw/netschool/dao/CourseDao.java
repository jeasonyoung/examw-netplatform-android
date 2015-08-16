package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.entity.Course;
import com.examw.netschool.entity.DowningCourse;
import com.examw.netschool.util.StringUtils;

/**
 * 课程数据操作类。
 * @author jeasonyoung
 *
 */
public class CourseDao {
	private static final String TAG = "CourseDao";
	private MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context
	 */
	public CourseDao(Context context) {
		this.dbHelper = new MyDBHelper(context);
	}
	/**
	 * 根据班级ID加载用户课程。
	 * @param classId
	 * 班级ID。
	 * @param userName
	 * 用户
	 * @return
	 */
	public List<Course> findByClassId(String classId,String userName) {
		Log.d(TAG, "开始加载班级["+classId+"]用户["+userName+"]课程...");
		final String query_sql = "select _id,courseid,coursename,coursetype,coursemode,coursegroup,filesize,finishsize,filepath,fileurl,state from CourseTab where classid = ? and username = ?";
		List<Course> list = new ArrayList<Course>();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(query_sql, new String[] { classId,userName });
		while (cursor.moveToNext()) {
			Course data = new Course();
			data.setId(cursor.getLong(0));
			data.setCourseId(cursor.getString(1));
			data.setCourseName(cursor.getString(2));
			
			data.setCourseType(cursor.getString(3));
			data.setCourseMode(cursor.getString(4));
			data.setCourseGroup(cursor.getString(5));
			data.setFileSize(cursor.getLong(6));
			data.setFinishSize(cursor.getLong(7));
			
			data.setFilePath(cursor.getString(8));
			data.setFileUrl(cursor.getString(9));
			
			data.setState(cursor.getInt(10));
			
			data.setClassId(classId);
			data.setUserName(userName);
			
			list.add(data);
		}
		cursor.close();
		db.close();
		Log.d(TAG, "完成加载班级["+classId+"]用户["+userName+"]课程=>" + list.size());
		return list;
	}
	/**
	 * 保存或更新数据。
	 * @param userName
	 * @param courses
	 */
	public void save(String userName,List<Course> courses) {
		if(StringUtils.isEmpty(userName) || courses == null || courses.size() == 0)return;
		Log.d(TAG, "开始保存用户["+userName+"]课程数["+courses.size()+"]...");
		final String query_sql = "select 0 from CourseTab where username=?";
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		boolean isAdded = false;
		Cursor cursor = db.rawQuery(query_sql, new String[]{ userName });
		if(cursor.getCount() == 0){
			isAdded = true;
		}
		cursor.close();
		try {
			db.beginTransaction();
			if(isAdded){
				for(Course course : courses){
					course.setUserName(userName);
					this.save(db, course);
				}
			}else {
				final String query = "select 0 from CourseTab where fileurl=? and username=?";
				for(Course course : courses){
					 if(course == null)continue;
					 course.setUserName(userName);
					 Cursor c = db.rawQuery(query, new String[]{ course.getFileUrl(),  course.getUserName() });
					 if(c.getCount() > 0){//有记录则跳过
						 c.close();
						 continue;
					 }
					 c.close();
					 this.save(db, course);
				}
			}
			db.setTransactionSuccessful();
		} catch (Exception e) {
			 Log.e(TAG, "保存数据发生异常：" + e.getMessage(), e);
		}finally{
			db.endTransaction();
			db.close();
		}
	}
	//
	private static final String course_insert_sql =  "insert into CourseTab(courseid,coursename,classid,coursetype,coursemode,coursegroup,filesize,finishsize,filepath,fileurl,state,username) values (?,?,?,?,?,?,?,?,?,?,?,?)";
	//添加数据
	private void save(SQLiteDatabase db, Course data){
		if(db == null || data == null)return;
		Object[] insertValues = new Object[]{
				data.getCourseId(),
				data.getCourseName(),
				data.getClassId(),
				data.getCourseType(),
				data.getCourseMode(),
				data.getCourseGroup(),
				data.getFileSize(),
				data.getFinishSize(),
				data.getFilePath(),
				data.getFileUrl(),
				data.getState(),
				data.getUserName()
		};
		db.execSQL(course_insert_sql, insertValues);
	}
	/**
	 * 删除班级ID下的全部课程数据。
	 * @param classid
	 * 班级ID
	 * @param username
	 * 用户
	 */
	public void deleteAllByClassId(String classId, String userName) {
		if(StringUtils.isEmpty(classId) || StringUtils.isEmpty(userName)) return;
		Log.d(TAG, "开始删除班级["+classId+"]下用户["+userName+"]的全部课程数据...");
		final String delete_sql = "delete from CourseTab where classid = ? and username = ?";
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL(delete_sql, new String[] { classId, userName });
			db.setTransactionSuccessful();
		} catch (Exception e) {
			Log.e(TAG, "删除班级课程发生异常："+ e.getMessage(), e);
		}finally {
			db.endTransaction();
			db.close();
		}
	}
//	public List<DowningCourse> findAll(String username) {
//		List<DowningCourse> list = new ArrayList<DowningCourse>();
//		SQLiteDatabase db = this.dbhelper.getReadableDatabase();//dbhelper.getDatabase(0);
//		Log.d(TAG, "findAll方法打开了数据库连接");
//		String sql = "select coursename,filesize,finishsize,filepath,fileurl from CourseTab where username = ?";
//		Cursor cursor = db.rawQuery(sql, new String[]{username});
//		while (cursor.moveToNext()) {
//			DowningCourse dc = new DowningCourse(cursor.getString(0),
//					cursor.getInt(1), cursor.getInt(2), cursor.getString(3),
//					cursor.getString(4),username);
//			list.add(dc);
//		}
//		cursor.close();
//		//dbhelper.closeDb();
//		db.close();
//		Log.d(TAG, "findAll方法关闭了数据库连接");
//		return list;
//	}

	/**
	 * 加载正在下载的课程。
	 * @param userName
	 * @return
	 */
	public List<DowningCourse> findAllDowning(String userName) {
		Log.d(TAG, "开始加载正在下载的课程:" + userName +"...");
		List<DowningCourse> list = new ArrayList<DowningCourse>();
		final String query_sql = "select coursename,filesize,finishsize,filepath,fileurl from CourseTab a where state = 1 and username = ?";
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(query_sql, new String[] {userName});
		while (cursor.moveToNext()) {
			DowningCourse data = new DowningCourse();
			data.setCourseName(cursor.getString(0));
			data.setFileSize(cursor.getLong(1));
			data.setFinishSize(cursor.getLong(2));
			data.setFilePath(cursor.getString(3));
			data.setFileUrl(cursor.getString(4));
			data.setUserName(userName);
			data.setState(DowningCourse.STATE_WAITTING);
			list.add(data);
		}
		cursor.close();
		db.close();
		Log.d(TAG, "完成加载正在下载的课程["+userName+"]=>" + list.size());
		return list;
	}
	/**
	 * 加载已下载完成的课程。
	 * @param username
	 * @return
	 */
	public List<Course> findAllDowned(String userName)
	{
		Log.d(TAG, "开始加载已下载完成的课程["+userName+"]...");
		List<Course> list = new ArrayList<Course>();
		final String query_sql = "select courseid,coursename,filepath,fileurl from CourseTab where state = 2 and username = ?";
		SQLiteDatabase db =  this.dbHelper.getReadableDatabase();
		Cursor cursor = db.rawQuery(query_sql, new String[] { userName });
		while (cursor.moveToNext()) {
			Course c = new Course();
			c.setCourseId(cursor.getString(0));
			c.setCourseName(cursor.getString(1));
			c.setFilePath(cursor.getString(2));
			c.setFileUrl(cursor.getString(3));
			c.setUserName(userName);
			list.add(c);
		}
		cursor.close();
		db.close();
		Log.d(TAG, "完成加载已下载完成的课程["+userName+"]=>" + list.size());
		return list;
	}
	/**
	 * 更新状态。
	 * @param userName
	 * @param fileUrl
	 * @param state
	 */
	public void updateState(String userName,String fileUrl, int state) {
		Log.d(TAG, "开始更新状态[username="+userName+"][url="+fileUrl+"][state="+state+"]...");
		final String update_sql = "update CourseTab set state = ? where fileurl = ? and username = ?";
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			db.beginTransaction();
			db.execSQL(update_sql, new Object[] { state, fileUrl,userName });
			db.setTransactionSuccessful();
		}catch (Exception e) {
			Log.e(TAG, "更新状态发生异常:"+ e.getMessage(), e);
		}finally {
			db.endTransaction();
			db.close();
		}
		Log.d(TAG, "完成更新状态[username="+userName+"][url="+fileUrl+"][state="+state+"].");
	}
	/**
	 * 删除正在下载的课程。
	 * @param userName
	 * @param fileUrl
	 */
	public void deleteDowing(String userName,String fileUrl)
	{
		Log.d(TAG, "开始删除正在下载的课程[username="+userName+"][url="+fileUrl+"]...");
		final String update_sql = "update CourseTab set filesize = 0 ,state = 0,filepath = null,finishsize = 0 where fileurl = ? and username = ?";
		final String delete_sql = "delete from DownloadTab where url = ? and username = ?";
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();
		try {
			Object[] parameters = new Object[]{ fileUrl,userName };
			db.beginTransaction();
			db.execSQL(update_sql, parameters);
			db.execSQL(delete_sql, parameters);
			db.setTransactionSuccessful();
		} catch (Exception e) {
			 Log.e(TAG, "删除正在下载的课程异常:" + e.getMessage(), e);
		}finally{
			db.endTransaction();
			db.close();
		}
	}
//	public void updateDowningCourse(DowningCourse course, int state , String username) {
//		SQLiteDatabase db = dbhelper.getDatabase(0);
//		db.beginTransaction();
//		try {
//			String sql = "update CourseTab set coursename = ?,filepath = ? ,filesize = ?,finishsize = ?,state = ? where fileurl = ? and username = ?";
//			db.execSQL(
//					sql,
//					new Object[] { course.getCourseName(),
//							course.getFilePath(), course.getFilesize(),
//							course.getFinishsize(), state, course.getFileurl(),course.getUsername() });
//			db.setTransactionSuccessful();
//		} finally {
//			db.endTransaction();
//		}
//		dbhelper.closeDb();
//	}
}
