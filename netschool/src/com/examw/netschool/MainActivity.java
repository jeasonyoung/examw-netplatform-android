package com.examw.netschool;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.AppContext.LoginState;
import com.examw.netschool.app.Constant;
/**
 * 主Activity类。
 * @author jeasonyoung
 *
 */
public class MainActivity extends Activity{
	private static final String TAG = "MainActivity";
	private AppContext appContext;
	private String uid,username;
	private GridView g;
	 /*
	  * 重载创建。
	  * @see android.app.Activity#onCreate(android.os.Bundle)
	  */
	 @Override
	 protected void onCreate(Bundle paramBundle){
		    super.onCreate(paramBundle);
		    Log.d(TAG, "初始化...");
		    this.setContentView(R.layout.activity_main);
		    
		    this.appContext = (AppContext)this.getApplication();
		    
		    final Intent intent = this.getIntent();
		    this.uid = intent.getStringExtra(Constant.CONST_USERID);
		    this.username = intent.getStringExtra(Constant.CONST_USERNAME);
		    
		    this.g = (GridView) this.findViewById(R.id.gridview1);
		    this.g.setAdapter(new MyAdapter(this));
	 }
	 /*
	  * 重载按键处理。
	  * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	  */
	 @Override
	 public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent){
	    if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getRepeatCount() == 0)){
	    	showDialog ();
	    	return true;
	    }
	    return super.onKeyDown(paramInt, paramKeyEvent);
	 }
	/*
	 * 重载销毁。
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		//发广播,通知下载服务service结束所有的线程,同时结束自己
        this.sendBroadcast(new Intent("commandFromActivity"));//发送广播  
		//
        super.onDestroy();
	}
	/*
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "设置").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST+2, 2, "帮助").setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, Menu.FIRST+4, 4, "注销").setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}
	/*
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		 switch(item.getItemId()){
			 case Menu.FIRST+1:{
				 //设置
				final Intent intent = new Intent(this,SettingActivity.class);
				intent.putExtra(Constant.CONST_USERNAME, username);	
				this.startActivity(intent);
				 break;
			 }
			 case Menu.FIRST+2:{
				 //帮助
				 this.startActivity(new Intent(this, HelpActivity.class));
				 break;
			 }
			 case Menu.FIRST+4:{
				 this.showDialog();		
				 break;
			 }
		 }
		 return true;
	 }
	 /**
		 * 自定义一个消息提示窗口
		 * @param msg
		 */
		protected void showDialog(){
			 AlertDialog.Builder localBuilder = new AlertDialog.Builder(this);
			 localBuilder.setTitle("注销").setMessage("是否注销用户").setCancelable(false).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						//注销
						appContext.setLoginState(LoginState.NONE);
						//
						MainActivity.this.startActivity(new Intent(MainActivity.this,LoginActivity.class));
						MainActivity.this.finish();
					}                      
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}                      
				});
			 localBuilder.create().show();
		}
		/**
		 * 数据适配器。 
		 * 
		 * @author jeasonyoung
		 * @since 2015年9月4日
		 */
		private class MyAdapter extends BaseAdapter{
			private Context context;
			private final int[] imagebtns = {
						R.drawable.mycourse_state,
						R.drawable.experience_center_state,
						R.drawable.answer_sheet_state,
						R.drawable.palyrecord_state,
						R.drawable.ssuggestion_state
					};
			private final int[] texts = {
					R.string.mycourse,
					R.string.experience_center,
					R.string.answer_sheet,
					R.string.LearningRecord,
					R.string.suggestionStr
				};
			private final Class<?>[] classes = {
					MyCourseActivity.class,
					Class1Activity.class,
					AnswerMainActivity.class,
					PlayrecordActivity.class,
					SuggestionActivity.class
				};
			/**
			 * 构造函数。
			 * @param context
			 */
			public MyAdapter(Context context){
				this.context = context;
			}
			/*
			 *  获取数据量。
			 * @see android.widget.Adapter#getCount()
			 */
			@Override
			public int getCount() {  return imagebtns.length; }
			/*
			 * 
			 * @see android.widget.Adapter#getItemId(int)
			 */
			 @Override
			public long getItemId(int position) { return position; }
			/*
			 * 
			 * @see android.widget.Adapter#getItem(int)
			 */
			@Override
			public Object getItem(int position) { return Integer.valueOf(this.imagebtns[position]); }
			/*
			 * 
			 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
			 */
			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				Log.d(TAG, "准备创建行...");
				ViewHolder viewHolder = null;
				if(convertView == null){
					Log.d(TAG, "创建新行..." + position);
					convertView = LayoutInflater.from(this.context).inflate(R.layout.gridviewlayout, parent, false);
					//初始化
					viewHolder = new ViewHolder(convertView);
					//缓存
					convertView.setTag(viewHolder);
				}else {
					Log.d(TAG, "重复使用行..." + position);
					//重用
					viewHolder = (ViewHolder)convertView.getTag();
				}
				//加载数据
				viewHolder.loadData(position);
				//返回
				return convertView;
			}
			//
			private class ViewHolder{
				private ImageButton localImgBtn;
				private TextView localTextView;
				/**
				 * 构造函数。
				 * @param convertView
				 */
				public ViewHolder(View convertView){
					  this.localImgBtn = (ImageButton)convertView.findViewById(R.id.ImgBtn);
					  this.localTextView = (TextView)convertView.findViewById(R.id.Imglab);
				}
				/**
				 * 加载数据。
				 * @param pos
				 */
				public void loadData(int pos){
					//加载图片
					if(imagebtns.length > pos) this.localImgBtn.setImageResource(imagebtns[pos]);
					//设置按钮事件
					if(classes.length > pos) this.localImgBtn.setOnClickListener(new ItemClickListener(classes[pos]));
					//标题
					if(texts.length > pos) this.localTextView.setText(texts[pos]);
				}
			}
		 }
		
		 //为每个条目设置监听方法
		 private class ItemClickListener implements OnClickListener {
			 private Class<?> c;
			 /**
			  * 构造函数。
			  * @param c
			  */
			 public ItemClickListener(Class<?> c) {
				 this.c = c;
			 }
			 /*
			  * 点击事件处理。
			  * @see android.view.View.OnClickListener#onClick(android.view.View)
			  */
			 @Override
			 public void onClick(View v) {
				 Log.d(TAG, "启动activity:" + this.c);
				 
				 final Intent intent = new Intent(MainActivity.this,c);
				 intent.putExtra(Constant.CONST_USERID, uid);
				 intent.putExtra(Constant.CONST_USERNAME, username);
				 
				 MainActivity.this.startActivity(intent);
			}
		}
}