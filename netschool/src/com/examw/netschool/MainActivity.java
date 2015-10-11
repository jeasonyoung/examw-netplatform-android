package com.examw.netschool;

import java.util.ArrayList;
import java.util.List;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.model.MainItem;
import com.examw.netschool.service.DownloadService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

/**
 * 主界面Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月26日
 */
public class MainActivity extends Activity implements OnItemClickListener{
	private static final String TAG = "MainActivity";
	
	private String userId,username;
	private final List<MainItem> items;
	private final MainAdapter adapter;
	/**
	 * 构造函数。
	 */
	public MainActivity(){
		Log.d(TAG, "初始化...");
		this.items = new ArrayList<MainItem>();
		this.adapter = new MainAdapter(this.items);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//设置布局文件
		this.setContentView(R.layout.activity_main);
		//获取传递数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//设置用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//设置用账号
			this.username = intent.getStringExtra(Constant.CONST_USERNAME);
		}
		//GridView
		final GridView gridView = (GridView)this.findViewById(R.id.main_gridview);
		//设置数据适配器
		gridView.setAdapter(this.adapter);
		//设置选中事件处理。
		gridView.setOnItemClickListener(this);
		//
		super.onCreate(savedInstanceState);
	}
	/*
	 * 数据项点击事件处理。
	 * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView, android.view.View, int, long)
	 */
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Log.d(TAG, "数据项点击事件处理..." + position);
		if(this.items != null && this.items.size() >  position){
			final MainItem item = this.items.get(position);
			if(item == null || item.getActivityClass() == null) return;
			//初始化意图
			final Intent intent = new Intent(this, item.getActivityClass());
			//设置用户ID
			intent.putExtra(Constant.CONST_USERID, userId);
			//设置用户名
			intent.putExtra(Constant.CONST_USERNAME, username);
			//启动意图
			this.startActivity(intent);
		}
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	 @Override
	 protected void onStart() {
		 Log.d(TAG, "重载启动...");
		 //异步线程加载数据
		 new AsyncTask<Void, Void, List<MainItem>>() {
			 /*
			  * 后台线程加载数据。
			  * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			  */
			@Override
			protected List<MainItem> doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载数据...");
					//初始化
					final  List<MainItem> list = new ArrayList<MainItem>();
					//我的课程
					list.add(new MainItem(R.string.main_icon_mycourse, 
														 R.drawable.main_icon_mycourse, 
														 MyCourseActivity.class));
					//免费体验
					list.add(new MainItem(R.string.main_icon_free_experience, 
														 R.drawable.main_icon_free_experience, 
														 FreeExperienceActivity.class));
					//你问我答
					list.add(new MainItem(R.string.main_icon_answer,
							                             R.drawable.main_icon_answer, 
							                             AnswerActivity.class));
					//播放记录
					list.add(new MainItem(R.string.main_icon_play_record,
							                             R.drawable.main_icon_play_record, 
							                             PlayRecordActivity.class));
					//意见反馈
					list.add(new MainItem(R.string.main_icon_suggest,
							                             R.drawable.main_icon_suggest,
							                             SuggestActivity.class));
					//返回
					return list;
				}catch(Exception e){
					Log.e(TAG, "加载线程异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前台主线程更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(List<MainItem> result) {
				Log.d(TAG, "前台线程更新数据...");
				//清空数据
				items.clear();
				//添加数据
				if(result != null && result.size() > 0){
					Log.d(TAG, "添加数据...");
					items.addAll(result);
				}
				//通知适配器更新数据
				adapter.notifyDataSetChanged();
			}
			
		}.execute((Void)null);
		 //
		 super.onStart();
	 }
	 /*
	  * 重载按钮事件处理。
	  * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	  */
	 @Override
	 public boolean onKeyDown(int keyCode, KeyEvent event) {
		 //返回按键处理
		 if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0){
			 Log.d(TAG, "返回按钮事件处理...");
			 final AppContext appContext = (AppContext)this.getApplicationContext();
			 if(appContext == null) return true;
			 //弹出二次确认框
			 Log.d(TAG, "弹出二次确认框...");
			 new AlertDialog.Builder(this)
			 						   .setTitle(R.string.main_logout_title)
			 						   .setMessage(R.string.main_logout_title_msg)
			 						   .setCancelable(false)
			 						   .setPositiveButton(R.string.btn_main_logout_submit, new DialogInterface.OnClickListener(){
			 							   
											@Override
											public void onClick(DialogInterface dialog, int which) {
												//注销当前用户ID
												appContext.setCurrentUserId(null);
												//跳转到登录activity
												startActivity(new Intent(appContext, LoginActivity.class));
												//关闭当前Activity
												finish();
											}
											
			 						   }).setNegativeButton(R.string.btn_main_logout_cancel, new DialogInterface.OnClickListener() {
			 							   
											@Override
											public void onClick(DialogInterface dialog, int which) {
												Log.d(TAG, "取消处理...");
												dialog.dismiss();
											}
			 						   }).show();
			 return true;			 
		 }
		 return super.onKeyDown(keyCode, event);
	}
	/*
	 * 重载销毁。
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		//发广播,通知下载服务service结束所有的线程,同时结束自己
        this.sendBroadcast(new Intent(DownloadService.BROADCAST_SERVICE_STOP));//发送广播  
		//
        super.onDestroy();
	}
	//数据适配器
	private class MainAdapter extends BaseAdapter{
		private static final String TAG = "MainAdapter";
		private final List<MainItem> list;
		/**
		 * 构造函数。
		 * @param items
		 */
		public MainAdapter(List<MainItem> items){
			Log.d(TAG, "初始化...");
			this.list = items;
		}
		/*
		 * 获取数据总数。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return this.list == null ? 0 : this.list.size();
		}
		/*
		 * 获取数据对象。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return (this.list == null || this.list.size() < position) ? null : this.list.get(position);
		}
		/*
		 * 获取数据对象ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据项。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取数据项..." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建数据项..." + position);
				//加载数据项布局
				convertView = LayoutInflater.from(MainActivity.this).inflate(R.layout.activity_main_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用数据项..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((MainItem)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//数据项View
	private class ViewHolder{
		private TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.btn_main_item);
		}
		/**
		 * 加载数据。
		 * @param item
		 */
		public void loadData(MainItem item){
			if(item != null && this.tvTitle != null){
				//图标
				if(item.getIconResId() > 0){
					final Drawable topDrawable = getResources().getDrawable(item.getIconResId());
					if(topDrawable != null){
						topDrawable.setBounds(0, 0, topDrawable.getMinimumWidth(), topDrawable.getMinimumHeight());
						this.tvTitle.setCompoundDrawables(null, topDrawable, null, null);
					}
				}
				//标题
				if(item.getTextResId() > 0){
					this.tvTitle.setText(item.getTextResId());
				}
			}
		}
	}
}