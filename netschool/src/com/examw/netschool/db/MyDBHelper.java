package com.examw.netschool.db;

import org.apache.commons.lang3.StringUtils;

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
	public MyDBHelper(Context context, String userId)
	{
		super(context, "eschool_" + (StringUtils.isBlank(userId) ? "_" :  userId) + ".db", null, VERSION);
		Log.d(TAG, "初始化数据库操作:eschool_" +(StringUtils.isBlank(userId) ? "_" :  userId) + ".db...");
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
		//3
		
		
//		db.execSQL("CREATE TABLE ClassTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,CLASSID TEXT,CLASSNAME TEXT,USERNAME TEXT,FATHERCLASSID TEXT,CLASSTYPE TEXT)");
//		db.execSQL("CREATE TABLE UserTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,UID TEXT,USERNAME TEXT,PASSWORD TEXT,NICKNAME TEXT)");
//		db.execSQL("CREATE TABLE CourseTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,COURSEID TEXT,COURSENAME TEXT,CLASSID TEXT,COURSETYPE TEXT,COURSEMODE TEXT,COURSEGROUP TEXT,FILESIZE INTEGER,FINISHSIZE INTEGER,FILEPATH TEXT,FILEURL TEXT,STATE INTEGER,USERNAME TEXT)");
//		db.execSQL("CREATE TABLE DownloadTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,THREAD_ID INTEGER,START_POS INTEGER,END_POS INTEGER,COMPLETE_SIZE INTEGER,URL TEXT,USERNAME TEXT)");
//		db.execSQL("CREATE TABLE PlayrecordTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,COURSEID TEXT,PLAYTIME DATETIME DEFAULT (datetime('now','localtime')),CURRENTTIME INTEGER,USERNAME TEXT)");
//		db.execSQL("CREATE TABLE ExamPaperTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,PAPERNAME TEXT, PAPERTIME INTEGER ,PAPERSCORE INTEGER ,COURSEID TEXT,EXAMID TEXT)");
//		db.execSQL("CREATE TABLE ExamRuleTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,RULEID TEXT,PAPERID TEXT,RULETITLE TEXT,RULETITLEINFO TEXT,RULETYPE TEXT,QUESTIONNUM INTEGER,SCOREFOREACH FLOAT,SCORESET TEXT,ORDERINPAPER INTEGER)");
//		db.execSQL("CREATE TABLE ExamQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,RULEID TEXT,PAPERID TEXT,EXAMID TEXT,CONTENT TEXT,ANSWER TEXT,ANALYSIS TEXT,QTYPE TEXT,OPTIONNUM INTEGER,ORDERID INTEGER,LINKQID TEXT)");
//		db.execSQL("CREATE TABLE ExamRecordTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,PAPERID TEXT,USERNAME TEXT,SCORE FLOAT,LASTTIME DATETIME DEFAULT (datetime('now','localtime')),USETIME INTEGER,TEMPTIME INTEGER,ANSWERS TEXT,TEMPANSWER TEXT,ISDONE TEXT)");
//		db.execSQL("CREATE TABLE ExamErrorQuestionTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,EXAMID TEXT,ERRORNUM INTEGER,USERNAME TEXT)");
//		db.execSQL("CREATE TABLE ExamNoteTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,EXAMID TEXT,CONTENT TEXT,ADDTIME DATETIME DEFAULT (datetime('now','localtime')),USERNAME TEXT)");
//		db.execSQL("CREATE TABLE ExamFavorTab(_ID INTEGER PRIMARY KEY AUTOINCREMENT,QID TEXT,PAPERID TEXT,EXAMID TEXT,USERNAME TEXT)");
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
		
		
		//重新创建表结构
		this.onCreate(db);
		
//		db.execSQL("DROP TABLE  IF EXISTS ClassTab");
//		db.execSQL("DROP TABLE  IF EXISTS UserTab");
//		db.execSQL("DROP TABLE  IF EXISTS CourseTab");
//		db.execSQL("DROP TABLE  IF EXISTS DownloadTab");
//		db.execSQL("DROP TABLE  IF EXISTS PlayrecordTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamPaperTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamRuleTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamQuestionTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamErrorQuestionTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamRecordTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamNoteTab");
//		db.execSQL("DROP TABLE  IF EXISTS ExamFavorTab");
//		onCreate(db);
	}
}