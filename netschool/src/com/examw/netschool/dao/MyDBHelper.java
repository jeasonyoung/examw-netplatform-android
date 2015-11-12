package com.examw.netschool.dao;

import com.examw.netschool.codec.digest.DigestUtils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
/**
 * SQLite操作工具类。
 * @author jeasonyoung
 *
 */
public class MyDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "MyDBHelper";
	private static final int VERSION = 1; 
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 */
	public MyDBHelper(Context context, String agencyId,  String userName){
		super(context, "eschool_" + DigestUtils.md5Hex(agencyId + userName) + ".db", null, VERSION);
		Log.d(TAG, "初始化数据库操作:eschool_" +DigestUtils.md5Hex(agencyId + userName) + ".db...");
	}
	/*
	 * 重载创建数据库时创建表结构。
	 * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		//1.我的课程
		db.execSQL("CREATE TABLE tbl_MyCourses(id TEXT,pid TEXT DEFAULT NULL,name TEXT,type TEXT,orderNo INTEGER DEFAULT 0,CONSTRAINT pk_tbl_MyCourses PRIMARY KEY(id,pid));");
		//2.课程资源
		db.execSQL("CREATE TABLE tbl_Lessones(id TEXT PRIMARY KEY,class_id TEXT,name TEXT,videoUrl TEXT,highVideoUrl TEXT,superVideoUrl TEXT,time INTEGER DEFAULT 0,orderNo INTEGER DEFAULT 0);");
		//3.播放记录
		db.execSQL("CREATE TABLE tbl_PlayRecords(id TEXT PRIMARY KEY,lesson_id TEXT,playTime INTEGER DEFAULT 0, createTime TIMESTAMP DEFAULT (datetime('now', 'localtime')));");
		//4.下载记录
		db.execSQL("CREATE TABLE tbl_Downloads(lessonId TEXT PRIMARY KEY,filePath TEXT,fileSize INTEGER DEFAULT 0,state INTEGER DEFAULT 0);");
		//5.下载进程
		db.execSQL("CREATE TABLE tbl_Downing(lessonId TEXT,threadId INTEGER,startPos INTEGER DEFAULT 0,endPos INTEGER DEFAULT 0,completeSize INTEGER DEFAULT 0,CONSTRAINT pk_tbl_Downing PRIMARY KEY(lessonId,threadId));");
		//6.答疑主题
		db.execSQL("CREATE TABLE tbl_AQTopic(id TEXT PRIMARY KEY,lessonId TEXT,title TEXT,content TEXT,lastTime TIMESTAMP DEFAULT (datetime('now', 'localtime')));");
		//7.答疑明细
		db.execSQL("CREATE TABLE tbl_AQDetail(id TEXT PRIMARY KEY,topicId TEXT,content TEXT,userId TEXT,userName TEXT,createTime TIMESTAMP DEFAULT (datetime('now', 'localtime')));");
	}
	/*
	 * 重载升级数据库结构。
	 * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "升级数据库结构版本v " + oldVersion + " =>v " + newVersion + "....");
		//1.我的课程
		db.execSQL("DROP TABLE  IF EXISTS tbl_MyCourses;");
		//2.课程资源
		db.execSQL("DROP TABLE  IF EXISTS tbl_Lessones;");
		//3.播放记录
		db.execSQL("DROP TABLE  IF EXISTS tbl_PlayRecords;");
		//4.下载记录
		db.execSQL("DROP TABLE  IF EXISTS tbl_Downloads;");
		//5.下载进程
		db.execSQL("DROP TABLE  IF EXISTS tbl_Downing;");
		//6.答疑主题
		db.execSQL("DROP TABLE  IF EXISTS tbl_AQTopic;");
		//7.答疑明细
		db.execSQL("DROP TABLE  IF EXISTS tbl_AQDetail;");
		//重新创建表结构
		this.onCreate(db);
	}
}