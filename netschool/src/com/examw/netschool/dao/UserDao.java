package com.examw.netschool.dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.entity.User;

public class UserDao {
	private static final String TAG = "UserDao";
	private MyDBHelper dbHelper;
	/**
	 * 构造函数。
	 * @param context 上下文。
	 */
	public UserDao(Context context)
	{
		this.dbHelper = new MyDBHelper(context);
	}
	public long addUser(User user) throws IllegalArgumentException, IllegalAccessException
	{
		long i = 0;
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		Log.d(TAG, "addUser方法打开了数据库连接");
		db.beginTransaction();
		try{
			i = db.insert("UserTab", null,ContentValuesBuilder.getInstance().bulid(user));
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
		Log.d(TAG, "addUser方法关闭了数据库连接");
		return i;
	}
	public User findByUsername(String username)
	{
		User user = null;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "findByUsername方法打开了数据库连接");
		Cursor cursor= db.rawQuery("select uid,username,password from UserTab where username = ?", new String[]{username});
		if(cursor.moveToNext())
		{
			user = new User();
			user.setUid(cursor.getString(0));
			user.setUsername(cursor.getString(1));
			user.setPassword(cursor.getString(2));
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "findByUsername方法关闭了数据库连接");
		return user;
	}
	public void update(User user) throws IllegalArgumentException, IllegalAccessException
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		Log.d(TAG, "update方法打开了数据库连接");
		db.update("UserTab", ContentValuesBuilder.getInstance().bulid(user), "username=?", new String[]{user.getUsername()});
		db.close();
		//dbhelper.closeDb();
		Log.d(TAG, "update方法关闭了数据库连接");
	}
	public void saveOrUpdate(User user) throws IllegalArgumentException, IllegalAccessException
	{
		User user1 = findByUsername(user.getUsername());
		if(user1!=null)
		{
			if(!user1.getPassword().equals(user.getPassword()))
			{
				update(user);
			}
		}else
		{
			addUser(user);
		}
	}
//	public void closeDB()
//	{
//		dbhelper.closeDb();
//	}
}
