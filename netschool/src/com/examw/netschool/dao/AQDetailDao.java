package com.examw.netschool.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.model.AQDetail;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * 答疑明细数据操作。
 * 
 * @author jeasonyoung
 * @since 2015年9月22日
 */
public class AQDetailDao extends BaseDao {
	private static final String TAG = "AQDetailDao";
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 */
	public AQDetailDao(Context context, String userId) {
		super(context, userId);
		Log.d(TAG, "初始化...");
	}
	/**
	 * 构造函数。
	 * @param dao
	 */
	public AQDetailDao(BaseDao dao){
		super(dao);
		Log.d(TAG, "初始化...");
	}
	/**
	 * 是否存在明细。
	 * @param detailId
	 * @return
	 */
	public boolean hasDetail(String detailId){
		Log.d(TAG, "是否存在明细...." + detailId);
		boolean result = false;
		if(StringUtils.isBlank(detailId)) return result;
		SQLiteDatabase db = null;
		try{
			//sql
			final String query = "SELECT COUNT(0) FROM tbl_AQDetail WHERE id = ?;";
			//
			db = this.dbHelper.getReadableDatabase();
			final Cursor cursor =  db.rawQuery(query, new String[]{ detailId });
			while(cursor.moveToNext()){
				result = cursor.getInt(0) > 0;
				break;
			}
			//
			cursor.close();
		}catch(Exception e){
			Log.e(TAG, "发生异常:" + e.getMessage(), e);
		}finally{
			if(db != null) db.close();
		}
		return result;
	}
	/**
	 * 加载答疑明细。
	 * @param id
	 * 答疑明细ID。
	 * @return
	 */
	public AQDetail getDetail(String id){
		Log.d(TAG, "加载答疑明细..." + id);
		AQDetail detail = null;
		if(StringUtils.isBlank(id)) return detail;
		SQLiteDatabase db = null;
		try{
			//sql
			final String query = "SELECT id,topicId,content,userId,userName,createTime FROM tbl_AQDetail WHERE id = ?;";
			//
			db = this.dbHelper.getReadableDatabase();
			final Cursor cursor =  db.rawQuery(query, new String[]{ id });
			while(cursor.moveToNext()){
				detail = this.read(cursor);
				break;
			}
			//
			cursor.close();
		}catch(Exception e){
			Log.e(TAG, "发生异常:" + e.getMessage(), e);
		}finally{
			if(db != null) db.close();
		}
		return detail;
	}
	/**
	 * 加载答疑主题下的明细集合。
	 * @param topicId
	 * @return
	 */
	public List<AQDetail> loadDetails(String topicId){
		Log.d(TAG, "加载答疑主题["+topicId+"]下的明细集合...");
		if(StringUtils.isBlank(topicId)) return null;
		List<AQDetail> details = null;
		SQLiteDatabase db = null;
		try{
			//sql
			final String query = " SELECT id,topicId,content,userId,userName,createTime FROM tbl_AQDetail "
					+ " WHERE topicId = ? ORDER BY createTime;";
			//
			db = this.dbHelper.getReadableDatabase();
			final Cursor cursor =  db.rawQuery(query, new String[]{ topicId });
			details = new ArrayList<AQDetail>();
			while(cursor.moveToNext()){
				//
				final AQDetail detail = this.read(cursor);
				if(detail != null){
					details.add(detail);
				}
			}
			//
			cursor.close();
		}catch(Exception e){
			Log.e(TAG, "发生异常:" + e.getMessage(), e);
		}finally{
			if(db != null) db.close();
		}
		return details;
	}
	//读取数据
	private AQDetail read(Cursor cursor){
		//初始化
		final AQDetail detail = new AQDetail();
		//答疑明细ID
		detail.setId(cursor.getString(0));
		//所属答疑主题ID
		detail.setTopicId(cursor.getString(1));
		//明细数据
		detail.setContent(cursor.getString(2));
		//用户ID
		detail.setUserId(cursor.getString(3));
		//用户姓名
		detail.setUserName(cursor.getString(4));
		//时间
		detail.setCreateTime(cursor.getString(5));
		//返回
		return detail;
	}
	/**
	 * 插入答疑主题明细。
	 * @param detail
	 * 答疑主题明细。
	 */
	public void insert(AQDetail detail){
		Log.d(TAG, "插入答疑主题...");
		if(detail == null)return;
		SQLiteDatabase db = null;
		try{
			//检查数据
			if(StringUtils.isBlank(detail.getTopicId())){
				Log.d(TAG, "所属答疑主题ID为空!");
				return;
			}
			//主题明细ID
			if(StringUtils.isBlank(detail.getId())) detail.setId(UUID.randomUUID().toString());
			//初始化
			db = this.dbHelper.getWritableDatabase();
			//开启事务
			db.beginTransaction();
			//执行操作
			db.execSQL("INSERT INTO tbl_AQDetail(id,topicId,content,userId,userName,createTime) values(?,?,?,?,?,?);", new Object[]{
				//明细ID
				StringUtils.trimToNull(detail.getId()),
				//所属主题ID
				StringUtils.trimToNull(detail.getTopicId()),
				//明细内容
				StringUtils.trimToNull(detail.getContent()),
				//用户ID
				StringUtils.trimToNull(detail.getUserId()),
				//用户姓名
				StringUtils.trimToNull(detail.getUserName()),
				//时间
				StringUtils.trimToNull(detail.getCreateTime())
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
	/**
	 * 更新答疑主题明细。
	 * @param detail
	 */
	public void update(AQDetail detail){
		Log.d(TAG, "更新答疑主题明细...");
		if(detail == null)return;
		SQLiteDatabase db = null;
		try{
			//检查数据
			if(StringUtils.isBlank(detail.getTopicId())){
				Log.d(TAG, "所属答疑主题ID为空!");
				return;
			}
			//初始化
			db = this.dbHelper.getWritableDatabase();
			//开启事务
			db.beginTransaction();
			//执行操作
			db.execSQL("UPDATE tbl_AQDetail SET topicId= ?,content = ?, userId= ?, userName = ?, createTime = ? WHERE id = ?;", new Object[]{
				//所属主题ID
				StringUtils.trimToNull(detail.getTopicId()),
				//明细内容
				StringUtils.trimToNull(detail.getContent()),
				//用户ID
				StringUtils.trimToNull(detail.getUserId()),
				//用户姓名
				StringUtils.trimToNull(detail.getUserName()),
				//时间
				StringUtils.trimToNull(detail.getCreateTime()),
				//明细ID
				StringUtils.trimToNull(detail.getId())
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
	/**
	 * 删除主题明细。
	 * @param id
	 * 主题明细ID。
	 */
	public void delete(String id){
		Log.d(TAG, "删除主题明细..." + id);
		if(StringUtils.isBlank(id)) return;
		SQLiteDatabase db = null;
		try{
			//初始化
			db = this.dbHelper.getWritableDatabase();
			//开启事务
			db.beginTransaction();
			//执行操作
			db.execSQL("DELETE FROM tbl_AQDetail WHERE id = ?;", new Object[]{ id });
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