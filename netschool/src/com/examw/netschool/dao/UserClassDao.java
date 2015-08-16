package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.entity.UserClass;

public class UserClassDao {
	private final static String TAG = "UserClassDao";
	private MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context 上下文。
	 */
	public UserClassDao(Context context)
	{
		this.dbHelper = new MyDBHelper(context);
	}
	public void addClasses(List<UserClass> classes) throws IllegalArgumentException, IllegalAccessException
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "addClasses方法打开了数据库连接");
		db.beginTransaction();
		try
		{
			for(UserClass c:classes)
			{
				db.insert("ClassTab",null, ContentValuesBuilder.getInstance().bulid(c));
			}
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
		Log.d(TAG, "addClasses方法关闭了数据库连接");
	}
	public List<UserClass> findByUsername(String username)
	{
		List<UserClass> list = new ArrayList<UserClass>();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		String sql = "select classid,classname,username,fatherid,classtype from ClassTab where username = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		while(cursor.moveToNext())
		{
			UserClass uc = new UserClass(cursor.getString(0),
					cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4));
			list.add(uc);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		return list;
	}
	public String[] findBigClassName(String username)
	{
		String[] arr = null;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		String sql = "select classname from ClassTab where username=? and fatherclassid = 0 order by classtype desc,classid asc";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return arr;
		}
		arr = new String[cursor.getCount()];
		while(cursor.moveToNext())
		{
			int i=0;
			arr[i]=cursor.getString(0);
			i++;
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		return arr;
	}
	public String[][] findChildrenClass(String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		String sql = "select classid from ClassTab where username=? and fatherclassid = 0 and classtype = 1 order by classtype desc,classid asc";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		String[][] arr = new String[cursor.getCount()][];
		String sql1 = "select classname from ClassTab where username=? and fatherclassid =? order by classid asc";
		int i=0,j=0;
		while(cursor.moveToNext())
		{
			String classid = cursor.getString(0);
			Cursor cursor1 = db.rawQuery(sql1, new String[]{username,classid});
			arr[i] = new String[cursor1.getCount()];
			while(cursor1.moveToNext())
			{
				arr[i][j] = cursor1.getString(0);
				j++;
			}
			cursor1.close();
			i++;
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		System.out.println(Arrays.toString(arr[0]));
		return arr;
	}
	public String[][] findChildrenClassid(String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		String sql = "select classid,classtype from ClassTab where username=? and fatherclassid = 0 order by classtype desc,classid asc";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		String[][] arr = new String[cursor.getCount()][];
		String sql1 = "select classid from ClassTab where username=? and fatherclassid =? order by classid asc";
		int i=0,j=0;
		while(cursor.moveToNext())
		{
			String classid = cursor.getString(0);
			String classtype = cursor.getString(1);
			if("1".equals(classtype))
			{
				Cursor cursor1 = db.rawQuery(sql1, new String[]{username,classid});
				arr[i] = new String[cursor1.getCount()];
				while(cursor1.moveToNext())
				{
					arr[i][j] = cursor1.getString(0);
					j++;
				}
				cursor1.close();
			}else
			{
				arr[i][0] = classid;
			}
			i++;
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		return arr;
	}
	public void deleteAll(String username)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "deleteAll方法打开了数据库连接");
		db.beginTransaction();
		try
		{
			String sql = "delete from ClassTab where username = ?";
			db.execSQL(sql,new String[]{username});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
		Log.d(TAG, "deleteAll方法关闭了数据库连接");
	}
//	public void closeDB()
//	{
//		dbhelper.closeDb();
//	}
}