package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.PlayRecordDao;
import com.examw.netschool.model.PlayRecord;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
/**
 * 播放记录Activity。
 * @author jeasonyoung
 *
 */
public class PlayRecordActivity extends Activity implements OnClickListener, OnItemClickListener, OnItemLongClickListener{
	private static final String TAG = "PlayRecordActivity";
	private final List<PlayRecord> playRecords;
	private final PlayRecordAdapter adapter;
	
	private LinearLayout nodataView;
	
	private String userId;
	private PlayRecordDao playRecordDao;
	/**
	 * 构造函数。
	 */
	public PlayRecordActivity(){
		Log.d(TAG, "构造函数...");
		this.playRecords = new ArrayList<PlayRecord>();
		this.adapter = new PlayRecordAdapter(this.playRecords);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//加载布局文件
		this.setContentView(R.layout.activity_play_record);
		//加载数据传递
		final Intent intent = this.getIntent();
		this.userId = intent.getStringExtra(Constant.CONST_USERID);
		//返回按钮
		final View btnReturn = this.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(this);
		//设置标题
		final TextView tvTopView = (TextView)this.findViewById(R.id.top_title);
		tvTopView.setText(R.string.play_record_title);
		
		//无数据View
		this.nodataView = (LinearLayout)this.findViewById(R.id.nodata_view);
		//列表数据
		final ListView listView = (ListView)this.findViewById(R.id.list_play_records);
		//设置数据适配器
		listView.setAdapter(this.adapter);
		//设置点击播放
		listView.setOnItemClickListener(this);
		//设置长按删除
		listView.setOnItemLongClickListener(this);
		//
		super.onCreate(savedInstanceState);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//异步线程加载数据
		new AsyncLoadData().execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 按钮点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回按钮事件
				Log.d(TAG, "返回按钮事件...");
				this.finish();
				break;
			}
		}
	}
	/*
	 * 点击播放。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "数据项点击播放..." + position);
		if(this.playRecords != null && this.playRecords.size() >  position){
			final PlayRecord record = this.playRecords.get(position);
			if(record != null && StringUtils.isNotBlank(record.getId())){
				//播放处理
				final Intent intent = new Intent(this, VideoPlayActivity.class);
				intent.putExtra(Constant.CONST_USERID, userId);
				intent.putExtra(Constant.CONST_LESSON_RECORD_ID, record.getId());
				//
				startActivity(intent);
			}
		}
	}
	/*
	 * 长按删除。
	 * @see android.widget.AdapterView.OnItemLongClickListener#onItemLongClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "数据项长按删除..." + position);
		if(this.playRecords != null && this.playRecords.size() >  position){
			final PlayRecord record = this.playRecords.get(position);
			if(record == null) return false;
			//取消下载二次确认
			new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert)
			.setTitle(R.string.delete_record_title).setMessage(R.string.delete_record_msg)
			.setNegativeButton(R.string.delete_record_btn_cancel, new DialogInterface.OnClickListener(){
				/*
				 * 取消退出。
				 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "取消删除");
					dialog.dismiss();
				}
			})
			.setPositiveButton(R.string.delete_record_btn_submit, new DialogInterface.OnClickListener(){
				/*
				 * 确定取消下载。
				 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
				 */
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d(TAG, "删除播放记录["+record+"]...");
					//惰性加载数据
					if(playRecordDao == null){
						Log.d(TAG, "惰性加载数据...");
						playRecordDao = new PlayRecordDao(PlayRecordActivity.this, userId);
					}
					//从数据库中删除
					playRecordDao.delete(record.getId());
					//重新刷新数据
					new AsyncLoadData().execute((Void)null);
				}
			}).show();
			return true;
		}
		return false;
	}
	//时间处理
	private static String getTime(long time) {
		return StringUtils.leftPad(String.valueOf(time / 60), 2, '0') + ":" + StringUtils.leftPad(String.valueOf(time % 60), 2, '0'); 
	}
	//异步线程加载数据
	private class AsyncLoadData extends AsyncTask<Void, Void, List<PlayRecord>>{
		/*
		 * 后台线程加载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<PlayRecord> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程加载数据...");
				//惰性加载数据操作
				if(playRecordDao == null){
					Log.d(TAG, "惰性初始化...");
					playRecordDao = new PlayRecordDao(PlayRecordActivity.this, userId);
				}
				//加载数据 
				return playRecordDao.loadPlayRecords();
			}catch(Exception e){
				Log.e(TAG, "异步线程加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<PlayRecord> result) {
			Log.d(TAG, "前台数据处理...");
			//清空数据源
			playRecords.clear();
			//填充结果数据
			if(result != null && result.size() > 0){
				playRecords.addAll(result);
			}
			//没有数据
			nodataView.setVisibility(playRecords.size() == 0 ? View.VISIBLE : View.GONE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//播放记录数据适配器
	private class PlayRecordAdapter extends BaseAdapter{
		private static final String TAG = "PlayRecordAdapter";
		private final List<PlayRecord> records;
		/**
		 * 构造函数。
		 */
		public PlayRecordAdapter(List<PlayRecord> records){
			Log.d(TAG, "初始化...");
			this.records = records;
		}
		/*
		 * 获取数据量。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (this.records == null) ? 0 : this.records.size();
		}
		/*
		 * 获取数据对象。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return (this.records == null || this.records.size() < position) ? null : this.records.get(position);
		}
		/*
		 * 获取数据ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据项View。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取数据项View..." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建行..." + position);
				//加载布局
				convertView = LayoutInflater.from(PlayRecordActivity.this).inflate(R.layout.activity_play_record_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用行..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((PlayRecord)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//
	private class ViewHolder{
		private TextView tvTitle,tvTime;
		/**
		 *构造函数。 
		 */
		public ViewHolder(View convertView){
			//标题
			this.tvTitle = (TextView)convertView.findViewById(R.id.record_title);
			//时间
			this.tvTime = (TextView)convertView.findViewById(R.id.record_time);
		}
		/**
		 * 加载数据。
		 * @parameter record
		 * 数据
		 */
		public void loadData(PlayRecord record){
			if(record == null)return;
			//标题
			this.tvTitle.setText(record.getLessonName());
			//时间
			this.tvTime.setText(getTime(record.getPlayTime()) + " / " + record.getCreateTime());
		}
	}
}