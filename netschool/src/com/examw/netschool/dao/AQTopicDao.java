package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.AQTopic;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 答疑主题。
 * 
 * @author jeasonyoung
 * @since 2015年9月22日
 */
public class AQTopicDao extends BaseDao {
	private static final String TAG = "AQTopicDao";
	private SQLiteDatabase db;
	/**
	 * 是否存在答疑主题。
	 * @param id
	 * @return
	 */
	public boolean hasTopic(String id){
		Log.d(TAG, "是否存在答疑主题..." + id);
		boolean result = false;
		if(StringUtils.isBlank(id)) return result;
		synchronized(dbHelper){
			try{
				final String query = "SELECT COUNT(0) FROM tbl_AQTopic WHERE id = ?;";
				db = dbHelper.getReadableDatabase();
				final Cursor cursor =  db.rawQuery(query, new String[]{ id });
				if(cursor.moveToFirst()){
					result = cursor.getInt(0) > 0;
				}
				//
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null) db.close();
			}
		}
		return result;
	}
	/**
	 * 获取答疑主题。
	 * @param id
	 * @return
	 */
	public AQTopic getTopic(String id){
		Log.d(TAG, "获取答疑主题["+id+"]数据...");
		AQTopic topic = null;
		if(StringUtils.isBlank(id)) return topic;
		synchronized(dbHelper){
			try{
				//sql
				final String query = "SELECT a.id,a.lessonId,b.name,a.title,a.content,a.lastTime from tbl_AQTopic a "
						+ " INNER JOIN tbl_Lessones b ON b.id = a.lessonId "
						+ " WHERE a.id = ?"
						+ " ORDER BY a.lastTime desc ";
				//
				db = dbHelper.getReadableDatabase();
				final Cursor cursor =  db.rawQuery(query, new String[]{ id });
				if(cursor.moveToFirst()){
					topic = this.read(cursor);
				}
				//
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null) db.close();
			}
		}
		return topic;
	}
	/**
	 * 加载全部数据。
	 * @return
	 */
	public List<AQTopic> loadTopics(){
		Log.d(TAG, "加载全部数据集合....");
		final List<AQTopic> topics = new ArrayList<AQTopic>();
		synchronized(dbHelper){
			try{
				//sql
				final String query = "SELECT a.id,a.lessonId,b.name,a.title,a.content,a.lastTime from tbl_AQTopic a "
						+ " INNER JOIN tbl_Lessones b ON b.id = a.lessonId "
						+ " ORDER BY a.lastTime desc ";
				//
				db = dbHelper.getReadableDatabase();
				final Cursor cursor =  db.rawQuery(query, null);
				while(cursor.moveToNext()){
					final AQTopic topic = this.read(cursor);
					if(topic != null){
						topics.add(topic);
					}
				}
				//
				cursor.close();
			}catch(Exception e){
				Log.e(TAG, "发生异常:" + e.getMessage(), e);
			}finally{
				if(db != null) db.close();
			}
		}
		return topics;
	}
	//读取数据
	private AQTopic read(final Cursor cursor){
		//初始化
		final AQTopic topic = new AQTopic();
		//答疑主题ID
		topic.setId(cursor.getString(0));
		//所属课程资源ID
		topic.setLessonId(cursor.getString(1));
		//所属课程资源名称
		topic.setLessonName(cursor.getString(2));
		//答疑主题标题
		topic.setTitle(cursor.getString(3));
		//答疑主题内容
		topic.setContent(cursor.getString(4));
		//答疑主题时间
		topic.setLastTime(cursor.getString(5));
		return topic;
	}
	/**
	 * 插入答疑主题数据。
	 * @param topics
	 * 主题数据。
	 */
	public void insert(AQTopic topic){
		Log.d(TAG, "插入答疑主题数据..." + topic);
		if(topic == null)return;
		//检查数据
		if(StringUtils.isBlank(topic.getId())){
			Log.d(TAG, "所属课程资源ID为空!");
			return;
		}
		synchronized(dbHelper){
			try{
				//主题ID
				if(StringUtils.isBlank(topic.getId())) return;
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//执行操作
				db.execSQL("INSERT INTO tbl_AQTopic(id,lessonId,title,content,lastTime) values(?,?,?,?,?);", new Object[]{
						StringUtils.trimToNull(topic.getId()),
						StringUtils.trimToNull(topic.getLessonId()),
						StringUtils.trimToNull(topic.getTitle()),
						StringUtils.trimToNull(topic.getContent()),
						StringUtils.trimToNull(topic.getLastTime())
				});
				//设置事务成功
				db.setTransactionSuccessful();
			}catch(Exception e){
				Log.e(TAG, "插入数据异常:" + e.getMessage(), e);
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
	 * 更新答疑主题。
	 * @param topic
	 * 答疑主题。
	 */
	public void update(AQTopic topic){
		Log.d(TAG, "更新答疑主题...." + topic);
		if(topic == null || StringUtils.isBlank(topic.getId())) return;
		//检查数据
		if(StringUtils.isBlank(topic.getLessonId())){
			Log.d(TAG, "所属课程资源ID为空!");
			return;
		}
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//执行操作
				db.execSQL("UPDATE tbl_AQTopic SET lessonId = ?,title = ?,content = ?,lastTime = ? WHERE id = ?;", new Object[]{
						StringUtils.trimToNull(topic.getLessonId()),
						StringUtils.trimToNull(topic.getTitle()),
						StringUtils.trimToNull(topic.getContent()),
						StringUtils.trimToNull(topic.getLastTime()),
						StringUtils.trimToNull(topic.getId())
				});
				//设置事务成功
				db.setTransactionSuccessful();
			}catch(Exception e){
				Log.e(TAG, "插入数据异常:" + e.getMessage(), e);
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
	 * 删除答疑数据。
	 * @param topicId
	 * 主题ID。
	 */
	public void delete(String topicId){
		Log.d(TAG, "删除答疑数据..." + topicId);
		if(StringUtils.isBlank(topicId)) return;
		synchronized(dbHelper){
			try{
				//初始化
				db = dbHelper.getWritableDatabase();
				//开启事务
				db.beginTransaction();
				//执行操作
				//删除明细
				db.execSQL("DELETE FROM  tbl_AQDetail  WHERE topicId = ?;", new Object[]{ topicId });
				//删除主题
				db.execSQL("DELETE FROM  tbl_AQTopic  WHERE id = ?;", new Object[]{topicId });
				//设置事务成功
				db.setTransactionSuccessful();
			}catch(Exception e){
				Log.e(TAG, "插入数据异常:" + e.getMessage(), e);
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
}