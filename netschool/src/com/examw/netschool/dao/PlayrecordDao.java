package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.entity.Playrecord;

public class PlayrecordDao {
	private static final String TAG = "PlayrecordDao";
	private MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context 上下文。
	 */
	public PlayrecordDao(Context context) {
		this.dbHelper = new MyDBHelper(context);
	}
	//保存播放记录
	public void save(Playrecord record)
	{
		SQLiteDatabase db =  this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try{
				db.execSQL("insert into PlayrecordTab(courseId,currenttime,username)values(?,?,?)",
						new Object[]{record.getCourseId(),record.getCurrentTime(),record.getUsername()});
			db.setTransactionSuccessful();
		}finally{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	//根据用户名取出播放记录
	public List<Playrecord> getRecordList(String username)
	{
		List<Playrecord> list = new ArrayList<Playrecord>();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select c.courseid,c.coursename,c.fileurl,c.filepath,c.state,p.playtime,p.currenttime,p.username from PlayrecordTab p join CourseTab c on c.courseid = p.courseid where p.username =?", new String[]{username});
		while(cursor.moveToNext())
		{
			int state = cursor.getInt(4);
			String filepath=null;
			if(state==2)
			{
				filepath=cursor.getString(3);
			}
			Playrecord loader = new Playrecord(cursor.getString(0),cursor.getString(1),cursor.getString(2),filepath,cursor.getString(5),cursor.getInt(6),cursor.getString(7));
			list.add(loader);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	//更新
	public void saveOrUpdate(Playrecord r)
	{
		SQLiteDatabase db =  this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		Cursor cursor = db.rawQuery("select * from PlayrecordTab where courseid = ? and username=?", new String[]{r.getCourseId(),r.getUsername()});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.beginTransaction();
			try{
					db.execSQL("insert into PlayrecordTab(courseid,currenttime,username)values(?,?,?)",
							new Object[]{r.getCourseId(),r.getCurrentTime(),r.getUsername()});
				db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
		}else
		{
			//更新
			Log.d(TAG, "更新");
			cursor.close();
			db.beginTransaction();
			try{
					System.out.println("currenttime:"+r.getCurrentTime());
					db.execSQL("update PlayrecordTab set currenttime = ? where username = ? and courseid = ?",
							new Object[]{r.getCurrentTime(),r.getUsername(),r.getCourseId()});
				db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
			}
		}
		db.close();
		//dbhelper.closeDb();
	}
	//查找
	public Playrecord findRecord(String courseid,String username)
	{
		Playrecord record = null;
		SQLiteDatabase db =  this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select courseid,playtime,currenttime,username from PlayrecordTab where courseid = ? and username = ?", new String[]{courseid,username});
		if(cursor.moveToNext())
		{
			record = new Playrecord(cursor.getString(0),cursor.getString(1),cursor.getInt(2),cursor.getString(3));
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return record;
	}
}
