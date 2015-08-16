package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.examw.netschool.db.MyDBHelper;
import com.examw.netschool.entity.ExamErrorQuestion;
import com.examw.netschool.entity.ExamFavor;
import com.examw.netschool.entity.ExamNote;
import com.examw.netschool.entity.ExamQuestion;
import com.examw.netschool.entity.ExamRecord;
import com.examw.netschool.entity.ExamRule;
import com.examw.netschool.entity.Paper;
import com.examw.netschool.entity.QuestionAdapterData;
import com.google.gson.Gson;

public class PaperDao {
	private static final String TAG = "PaperDao";
	private MyDBHelper dbHelper;
	/**
	 *  构造函数。
	 * @param context 上下文。
	 */
	public PaperDao(Context context) {
		this.dbHelper = new MyDBHelper(context);
	}
	
	/**
	 * 插入试卷和大题
	 * @param paper	试卷
	 * @param rules 大题的集合
	 */
	public void insertPaper(Paper paper,List<ExamRule> rules)
	{
		//先看存不存在,不存在就加入
		if(paper==null) return;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Log.d(TAG, "insertPaper方法打开了数据库连接");
		Cursor cursor = db.rawQuery("select * from ExamPaperTab where paperid = ?",new String[]{paper.getPaperId()});
		if(cursor.getCount()>0)
		{
			Log.d(TAG,"该试卷已经加过了");
			cursor.close();
			//dbhelper.closeDb();
			db.close();
			return;
		}
		cursor.close();
		String sql = "insert into ExamPaperTab(paperid,papername,paperscore,papertime,courseid,examid)values(?,?,?,?,?,?)";
		Object[] params = new Object[]{paper.getPaperId(),paper.getPaperName(),paper.getPaperSorce(),paper.getPaperTime(),paper.getCourseId(),paper.getExamId()};
		db.beginTransaction();
		try
		{
			db.execSQL(sql, params);
			if(rules!=null&&rules.size()>0)
			{
				for(ExamRule r:rules)
				{
					db.execSQL("insert into ExamRuleTab(ruleid,paperid,ruletitle,ruletitleinfo,ruletype,questionnum,scoreforeach,scoreset,orderinpaper)values(?,?,?,?,?,?,?,?,?)", 
						new Object[]{r.getRuleId(),r.getPaperId(),r.getRuleTitle(),r.getFullTitle(),r.getRuleType(),r.getQuestionNum(),r.getScoreForEach(),r.getScoreSet(),r.getOrderInPaper()});
				}
			}
			db.setTransactionSuccessful();
		}finally {
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
		Log.d(TAG, "insertPaper方法关闭了数据库连接");
	}
	public List<Paper> findPapers(String gid)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String paperId, String paperName, int paperSorce, int paperTime,String courseId,String examId
		String sql = "select paperid,papername,papersorce,papertime,courseid,examid from ExamPaperTab where gid = ?";
		String[] params = new String[]{gid};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0) {
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<Paper> list = new ArrayList<Paper>();
		while(cursor.moveToNext())
		{
			Paper p = new Paper(cursor.getString(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getString(4),cursor.getString(5));
			list.add(p);
		}
		cursor.close();
		db.close();
		return list;
	}
	public List<Paper> findAllPapers(String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String paperId, String paperName, int paperSorce, int paperTime,String courseId,String examId
		String sql = "select paperid,papername,paperscore,papertime,courseid,examid from ExamPaperTab where courseid in ("+
						"select courseid from CourseTab where username = ? )";
		String[] params = new String[]{username};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			//dbhelper.closeDb();
			db.close();
			return null;
		}
		List<Paper> list = new ArrayList<Paper>();
		Gson gson = new Gson();
		while(cursor.moveToNext())
		{
			Paper p = new Paper(cursor.getString(0),cursor.getString(1),cursor.getInt(2),cursor.getInt(3),cursor.getString(4),cursor.getString(5));
			p.setJsonString(gson.toJson(p));
			list.add(p);
		}
		cursor.close();
		//dbhelper.closeDb();
		db.close();
		return list;
	}
	/**
	 * 插入大题组
	 * @param rules
	 */
	public void insertRules(List<ExamRule> rules)
	{
		if(rules!=null&&rules.size()>0)
		{
			SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
			db.beginTransaction();
			try
			{
			for(ExamRule r:rules)
			{
				db.execSQL("insert into ExamRuleTab(ruleid,paperid,ruletitle,ruletype,questionnum,scoreforeach,scoreset,orderinpaper)values(?,?,?,?,?,?,?,?)", 
						new Object[]{r.getRuleId(),r.getPaperId(),r.getRuleTitle(),r.getRuleType(),r.getQuestionNum(),r.getScoreForEach(),r.getScoreSet(),r.getOrderInPaper()});
			}
			db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
				db.close();
			}
			//dbhelper.closeDb();
		}
	}
	
	public List<ExamRule> findRules(String paperid)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String ruleId, String paperId, String ruleTitle,String ruleTitleInfo,String ruleType,String scoreSet, int questionNum, double scoreForEach, int orderInPaper
		String sql = "select ruleid,paperid,ruletitle,ruletitleinfo,ruletype,scoreset,questionnum,scoreforeach,orderinpaper from ExamRuleTab where paperid = ? order by orderinpaper asc";
		String[] params = new String[]{paperid};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamRule> list = new ArrayList<ExamRule>();
		while(cursor.moveToNext())
		{
			ExamRule r = new ExamRule(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getInt(6),cursor.getDouble(7),cursor.getInt(8));
			list.add(r);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public void insertQuestions(List<ExamQuestion> questions)
	{
		if(questions!=null&&questions.size()>0)
		{
			SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
			///////////
			Cursor cursor = db.rawQuery("select qid from ExamQuestionTab where paperid = ?", new String[]{questions.get(0).getPaperId()});
			if(cursor.getCount()> 0)
			{
				cursor.close();
				db.close();
				//dbhelper.closeDb();
				return;
			}
			cursor.close();
			//////////
			db.beginTransaction();
			try
			{
			for(ExamQuestion q:questions)
			{
				//QID ,PAPERID ,EXAMID ,CONTENT ,ANSWER ,ANALYSIS ,QTYPE ,OPTIONNUM ,ORDERID ,LINKQID
				db.execSQL("insert into ExamQuestionTab(qid,paperid,ruleid,content,answer,analysis,qtype,optionnum,orderid,linkqid)values(?,?,?,?,?,?,?,?,?,?)", 
						new Object[]{q.getQid(),q.getPaperId(),q.getRuleId(),q.getContent(),q.getAnswer(),q.getAnalysis(),q.getQType(),q.getOptionNum(),q.getOrderId(),q.getLinkQid()});
			}
			db.setTransactionSuccessful();
			}finally{
				db.endTransaction();
				db.close();
			}
			//dbhelper.closeDb();
		}
	}
	public List<ExamQuestion> findQuestionsByRuleId(String ruleId)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String qid, String paperId, String content,
		//String answer, String analysis, String linkQid,
		//int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,paperid,content,answer,analysis,linkqid,qtype,optionnum,orderid from ExamQuestionTab where ruleid = ? order by orderid asc";
		String[] params = new String[]{ruleId};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while(cursor.moveToNext())
		{
			ExamQuestion q = new ExamQuestion(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getInt(8),cursor.getInt(9));
			list.add(q);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public List<ExamQuestion> findQuestionByPaperId(String paperId)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String qid, String paperId, String content,
		//String answer, String analysis, String linkQid,
		//int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,paperid,content,answer,analysis,linkqid,qtype,optionnum,orderid from ExamQuestionTab where paperid = ? order by ruleid asc,orderid asc";
		String[] params = new String[]{paperId};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while(cursor.moveToNext())
		{
			ExamQuestion q = new ExamQuestion(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getInt(8),cursor.getInt(9));
			list.add(q);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public List<ExamQuestion> findQuestionById(String qid)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String qid, String paperId, String content,
		//String answer, String analysis, String linkQid,
		//int qType, int optionNum, int orderId
		String sql = "select qid,ruleid,paperid,content,answer,analysis,linkqid,qtype,optionnum,orderid from ExamQuestionTab where qid = ?";
		String[] params = new String[]{qid};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while(cursor.moveToNext())
		{
			ExamQuestion q = new ExamQuestion(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getInt(8),cursor.getInt(9));
			list.add(q);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public List<ExamQuestion> findQuestionFromErrors(String username,String paperid)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String qid, String paperId, String content,
		//String answer, String analysis, String linkQid,
		//int qType, int optionNum, int orderId
		String sql = "select q.qid,q.ruleid,q.paperid,q.content,q.answer,q.analysis,q.linkqid,q.qtype,q.optionnum,q.orderid from ExamQuestionTab q,ExamErrorQuestionTab e where q.qid = e.qid and e.paperid = ? and e.username = ? order by q.ruleid asc,q.orderid asc";
		String[] params = new String[]{paperid,username};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while(cursor.moveToNext())
		{
			ExamQuestion q = new ExamQuestion(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getInt(8),cursor.getInt(9));
			list.add(q);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	
	public List<ExamQuestion> findQuestionFromFavors(String username,String paperid)
	{
		Log.i(TAG,"find QuestionfromFavors");
		SQLiteDatabase db =  this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		//String qid, String paperId, String content,
		//String answer, String analysis, String linkQid,
		//int qType, int optionNum, int orderId
		String sql = "select q.qid,q.ruleid,q.paperid,q.content,q.answer,q.analysis,q.linkqid,q.qtype,q.optionnum,q.orderid from ExamQuestionTab q,ExamFavorTab f where q.qid = f.qid and f.paperid = ? and f.username = ? order by q.ruleid asc,q.orderid asc";
		String[] params = new String[]{paperid,username};
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount() == 0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamQuestion> list = new ArrayList<ExamQuestion>();
		while(cursor.moveToNext())
		{
			ExamQuestion q = new ExamQuestion(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getString(4),cursor.getString(5),cursor.getString(6),cursor.getString(7),cursor.getInt(8),cursor.getInt(9));
			list.add(q);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	/**
	 * 保存或更新考试记录
	 * @param r
	 * PAPERID ,USERNAME ,SCORE ,LASTIME ,USETIME ,TEMPTIME ,ANSWERS ,TEMPANSWER 
	 */
	public void saveOrUpdateRecord(ExamRecord r) //每人每套试卷只有一个记录
	{
		if(r==null) return;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select * from ExamRecordTab where paperid = ? and username = ?",new String[]{r.getPaperId(),r.getUsername()});
		if(cursor.getCount()>0)
		{
			cursor.close();
			String sql = "update ExamRecordTab set score = ?,usetime=?,temptime=?,answers=?,tempanswer=?,lasttime = datetime(?),isDone = ? where paperid = ? and username = ? ";
			Object[] params = new Object[]{r.getScore(),r.getUseTime(),r.getTempTime(),r.getAnswers(),r.getTempAnswer(),r.getLastTime(),r.getIsDone(),r.getPaperId(),r.getUsername()};
			db.execSQL(sql,params);
			//dbhelper.closeDb();
			db.close();
			return;
		}
		cursor.close();
		String sql = "insert into ExamRecordTab(paperid,username,score,usetime,temptime,answers,tempanswer,lasttime,isDone)values(?,?,?,?,?,?,?,?,?)";
		Object[] params = new Object[]{r.getPaperId(),r.getUsername(),r.getScore(),r.getUseTime(),r.getTempTime(),r.getAnswers(),r.getTempAnswer(),r.getLastTime(),r.getIsDone()};
		db.beginTransaction();
		try
		{
			db.execSQL(sql, params);
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	/*
	 * 开始考试就要加记录
	 */
	public ExamRecord insertRecord(ExamRecord r)
	{
		if(r==null) return null;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		/*
		 * String paperId,
			String username, String answers, String tempAnswer, double score,
			int useTime, int tempTime,String lastTime
		 */
		Cursor cursor = db.rawQuery("select paperid,username,answers,tempanswer,score,usetime,temptime,lasttime,isDone from ExamRecordTab where paperid = ? and username = ?",new String[]{r.getPaperId(),r.getUsername()});
		if(cursor.moveToNext())
		{
			ExamRecord record = new ExamRecord(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getDouble(4),cursor.getInt(5),cursor.getInt(6),cursor.getString(7),cursor.getString(8));
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return record;
		}
		cursor.close();
		String sql = "insert into ExamRecordTab(paperid,username,score,usetime,temptime,answers,tempanswer,lasttime,isDone)values(?,?,?,?,?,?,?,?,?)";
		Object[] params = new Object[]{r.getPaperId(),r.getUsername(),r.getScore(),r.getUseTime(),r.getTempTime(),r.getAnswers(),r.getTempAnswer(),r.getLastTime(),r.getIsDone()};
		db.beginTransaction();
		try
		{
			db.execSQL(sql, params);
			db.setTransactionSuccessful();
			return r;
		}finally
		{
			db.endTransaction();
			db.close();
			//dbhelper.closeDb();
		}
	}
	/**
	 * 查找考试记录
	 */
	public ExamRecord findRecord(String username,String paperId)
		{
			ExamRecord r = null;
			SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
			Cursor cursor = db.rawQuery("select paperid,username,answers,tempanswer,score,usetime,temptime,lasttime,isDone from ExamRecordTab where paperid = ? and username = ?",new String[]{paperId,username});
			if(cursor.moveToNext())
			{
				r = new ExamRecord(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getDouble(4),cursor.getInt(5),cursor.getInt(6),cursor.getString(7),cursor.getString(8));
			}
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return r;
		}
	public List<ExamRecord> findRecordsByUsername(String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		String sql = "select r.paperid,r.username,r.answers,r.tempanswer,r.score,r.usetime,r.temptime,r.lasttime,r.isDone ,p.papername,p.papertime,p.paperscore from ExamRecordTab r,ExamPaperTab p where r.paperid = p.paperid and username = ?";
		Cursor cursor = db.rawQuery(sql, new String[]{username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			///dbhelper.closeDb();
			return null;
		}
		List<ExamRecord> list = new ArrayList<ExamRecord>();
		while(cursor.moveToNext())
		{
			ExamRecord r = new ExamRecord(cursor.getString(0),cursor.getString(1),cursor.getString(2),cursor.getString(3),cursor.getDouble(4),cursor.getInt(5),cursor.getInt(6),cursor.getString(7),cursor.getString(8));
			r.setPapername(cursor.getString(9));
			r.setPapertime(cursor.getInt(10));
			r.setPaperscore(cursor.getInt(11));
			list.add(r);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public void deleteRecord(ExamRecord r)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();// dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			db.execSQL("delete from ExamRecordTab where username = ? and paperid = ?",new Object[]{r.getUsername(),r.getPaperId()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	/**
	 * 更新临时的答案
	 * @param r
	 */
	public void updateTempAnswerForRecord(ExamRecord r)
	{
		if(r==null) return;
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			db.execSQL("update ExamRecordTab set tempanswer = ? ,isdone = ? where paperid = ? and username = ?",new Object[]{r.getTempAnswer(),r.getIsDone(),r.getPaperId(),r.getUsername()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public void insertError(ExamErrorQuestion e)
	{
		if(e==null) return;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select * from ExamErrorQuestionTab where qid = ? and username = ?", new String[]{e.getQid(),e.getUsername()});
		if(cursor.getCount()>0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return;
		}
		cursor.close();
		db.beginTransaction();
		try
		{
			//QID TEXT,EXAMID TEXT,ERRORNUM INTEGER,USERNAME TEXT
			db.execSQL("insert into ExamErrorQuestionTab(qid,paperid,username)values(?,?,?) ",new Object[]{e.getQid(),e.getPaperId(),e.getUsername()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
		}
		db.close();
		//dbhelper.closeDb();
	}
	public void insertFavor(ExamFavor f)
	{
		if(f==null) return;
		Log.i(TAG,"inserFavor");
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		db.beginTransaction();
		try
		{
			//QID TEXT,EXAMID TEXT,USERNAME TEXT)
			db.execSQL("insert into ExamFavorTab (qid,paperid,username) values (?,?,?) ",new Object[]{f.getQid(),f.getPaperId(),f.getUsername()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public void insertNote(ExamNote n)
	{
		//QID TEXT,EXAMID TEXT,CONTENT TEXT,ADDTIME DATETIME DEFAULT (datetime('now','localtime')),USERNAME TEXT
		if(n == null) return;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		String[] params = new String[]{n.getQid(),n.getUsername()};
		Cursor cursor = db.rawQuery("select * from ExamNoteTab where qid=? and username = ?", params);
		if(cursor.getCount()>0)
		{
			cursor.close();
			db.beginTransaction();
			try
			{
				db.execSQL("update ExamNoteTab set content = ?, paperid = ?, addtime=datetime(?) where qid = ? and username = ? ",params);
				db.setTransactionSuccessful();
			}finally
			{
				db.endTransaction();
				db.close();
			}
			//dbhelper.closeDb();
			return;
		}
		cursor.close();
		db.beginTransaction();
		try
		{
			db.execSQL("insert into ExamNoteTab(qid,paperid,content,addtime,username)values(?,?,?,datetime(?),?) ",new Object[]{n.getQid(),n.getPaperId(),n.getContent(),n.getAddTime(),n.getUsername()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public String findNoteContent(String qid,String username)
	{
		String content = null;
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select content from ExamNoteTab where qid = ? and username = ?", new String[]{qid,username});
		if(cursor.moveToNext())
		{
			content = cursor.getString(0);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return content;
	}
	public void deleteFavor(ExamFavor f)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		try
		{
			db.beginTransaction();
			db.execSQL("delete from ExamFavorTab where username = ? and qid = ?",new Object[]{f.getUsername(),f.getQid()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public void deleteError(String username,String qid)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		try
		{
			db.beginTransaction();
			db.execSQL("delete from ExamErrorQuestionTab where username = ? and qid = ?",new Object[]{username,qid});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public void deleteNote(ExamNote note)
	{
		SQLiteDatabase db = this.dbHelper.getWritableDatabase();//dbhelper.getDatabase(MyDBHelper.WRITE);
		try
		{
			db.beginTransaction();
			db.execSQL("delete from ExamNoteTab where username = ? and qid = ?",new Object[]{note.getUsername(),note.getQid()});
			db.setTransactionSuccessful();
		}finally
		{
			db.endTransaction();
			db.close();
		}
		//dbhelper.closeDb();
	}
	public List<ExamNote> findNotes(String paperid,String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();// dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select qid,addTime,content,paperid from ExamNoteTab where paperid =? and username = ?", new String[]{paperid,username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamNote> list = new ArrayList<ExamNote>();
		while(cursor.moveToNext())
		{
			ExamNote note = new ExamNote(cursor.getString(0),cursor.getString(1),cursor.getString(2),username,cursor.getString(3));
			list.add(note);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public List<ExamFavor> findFavors(String username)
	{
		SQLiteDatabase db =  this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select qid,paperid from ExamFavorTab where username = ?", new String[]{username});
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<ExamFavor> list = new ArrayList<ExamFavor>();
		while(cursor.moveToNext())
		{
			ExamFavor favor = new ExamFavor(cursor.getString(0),username,cursor.getString(1));
			list.add(favor);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}
	public List<QuestionAdapterData> findAdapterData(String actionName,String username)
	{
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();// dbhelper.getDatabase(MyDBHelper.READ);
		String sql = null;
		String[] params = new String[]{username};
		if("myNotes".equals(actionName))
		{
			sql = "select p.paperid,p.papername,count(n.qid) from ExamNoteTab n,ExamPaperTab p where n.paperid = p.paperid and username = ? group by n.paperid";
		}
		else if("myErrors".equals(actionName))
		{
			sql = "select p.paperid,p.papername,count(n.qid) from ExamErrorQuestionTab n,ExamPaperTab p where n.paperid = p.paperid and username = ? group by n.paperid";
		}
		else if("myFavors".equals(actionName))
		{
			sql = "select p.paperid,p.papername,count(n.qid) from ExamFavorTab n,ExamPaperTab p where n.paperid = p.paperid and username = ? group by n.paperid";
		}
		if(sql==null) return null;
		Cursor cursor = db.rawQuery(sql, params);
		if(cursor.getCount()==0)
		{
			cursor.close();
			db.close();
			//dbhelper.closeDb();
			return null;
		}
		List<QuestionAdapterData> list = new ArrayList<QuestionAdapterData>();
		while(cursor.moveToNext())
		{
			QuestionAdapterData data = new QuestionAdapterData(cursor.getString(0),cursor.getString(1),cursor.getInt(2));
			list.add(data);
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return list;
	}

	public StringBuffer findFavorQids(String username,String paperId) {
		StringBuffer buf = new StringBuffer();
		SQLiteDatabase db = this.dbHelper.getReadableDatabase();//dbhelper.getDatabase(MyDBHelper.READ);
		Cursor cursor = db.rawQuery("select qid from ExamFavorTab where username = ? and paperId = ?", new String[]{username,paperId});
		while(cursor.moveToNext())
		{
			buf.append(cursor.getString(0)).append(",");
		}
		cursor.close();
		db.close();
		//dbhelper.closeDb();
		return buf;
	}
}
