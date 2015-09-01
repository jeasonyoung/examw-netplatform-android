package com.examw.netschool;

import android.app.AlertDialog;
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
import android.widget.Toast;

import com.examw.netschool.app.AppContext;
/**
 * 主Activity类。
 * @author jeasonyoung
 *
 */
public class MainActivity extends BaseActivity{
	 private GridView g;
	 private AppContext appContext;
	 private String username;
	 private int uid;
	 private boolean isLocalLogin;
	 /*
	  * 重载创建。
	  * @see android.app.Activity#onCreate(android.os.Bundle)
	  */
	 @Override
	 protected void onCreate(Bundle paramBundle)
	  {
	    super.onCreate(paramBundle);
	    setContentView(R.layout.activity_mian);
	    Intent intent = this.getIntent();
	    isLocalLogin = "local".equals(intent.getStringExtra("loginType"));
	    this.username = intent.getStringExtra("MAP_USERNAME");
	    uid = intent.getIntExtra("uid", 0);
	    g = (GridView) this.findViewById(R.id.gridview1);
		g.setAdapter(new MyAdapter());
		appContext = (AppContext) getApplication();
		//g.setOnItemClickListener(new ItemClickListener());
	  }
	 //初始化条目
	 private class MyAdapter extends BaseAdapter
	 {
		private LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
		private int[] imagebtns = { 
													R.drawable.mycourse_state,
													R.drawable.experience_center_state,
													R.drawable.question_bank_state,
													R.drawable.answer_sheet_state,
													R.drawable.palyrecord_state,
													R.drawable.ssuggestion_state
												   };
		
		private int[] texts = {
											R.string.mycourse,
											R.string.experience_center,
											R.string.question_bank,
											R.string.answer_sheet,
											R.string.LearningRecord,
											R.string.suggestionStr
										};
		
		private Class<?>[] classes = { 
													MyCourseActivity.class, 
													Class1Activity.class, 
													QuestionMainActivity.class,
													AnswerMainActivity.class, 
													PlayrecordActivity.class, 
													SuggestionActivity.class
												};
		/*
		 *  (non-Javadoc)
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			 return imagebtns.length;
		 }
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return Integer.valueOf(this.imagebtns[position]);
		}
		/*
		 *  (non-Javadoc)
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View paramView, ViewGroup parent) {
			    if (paramView == null){
			    	paramView = this.inflater.inflate(R.layout.gridviewlayout, null);
			    } 
			    ImageButton localImageButton = (ImageButton)paramView.findViewById(R.id.ImgBtn);
			    TextView localTextView = (TextView)paramView.findViewById(R.id.Imglab);
			    localImageButton.setImageResource(this.imagebtns[position]);
			    localImageButton.setOnClickListener(new ItemClickListener(this.classes[position])); 
			    localTextView.setText(this.texts[position]);
			    return paramView;
		}
		/*
		 * (non-Javadoc)
		 * @see android.widget.Adapter#getItemId(int)
		 */
		 @Override
		public long getItemId(int position) {
			return position;
		}
	 }
	 //为每个条目设置监听方法
	 private class ItemClickListener implements OnClickListener
	{
		 private Class<?> c;
		 public ItemClickListener(Class<?> c) {
			 this.c = c;
		}
		/*
		 *  (non-Javadoc)
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			 if(isLocalLogin)
			 {
				 if(c.equals(MyCourseActivity.class))
				 {
					 Intent intent = new Intent(MainActivity.this,c);
					 intent.putExtra("username", username);
					 intent.putExtra("loginType", "local");
					 MainActivity.this.startActivity(intent);
					 return;
				 }
				 if(c.equals(QuestionMainActivity.class))
				 {
					 Intent intent = new Intent(MainActivity.this,c);
					 intent.putExtra("username", username);
					 intent.putExtra("loginType", "local");
					 MainActivity.this.startActivity(intent);
					 return;
				 }
				 Toast.makeText(MainActivity.this, "请在线登录", Toast.LENGTH_SHORT).show();
				 return;
			 }
			 Log.v("debug", "启动activity"+c.toString());
			 Intent intent = new Intent(MainActivity.this,c);
			 intent.putExtra("username", username);
			 intent.putExtra("uid", uid);
			 MainActivity.this.startActivity(intent);
		}
	}
	public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent)
	{
	    if ((paramKeyEvent.getKeyCode() == 4) && (paramKeyEvent.getRepeatCount() == 0))
	    {
	      showDialog ();
	      return true;
	    }
	    return super.onKeyDown(paramInt, paramKeyEvent);
	 }
	/*
	 * (non-Javadoc)
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		//发广播,通知下载服务service结束所有的线程,同时结束自己
        Intent myIntent = new Intent();
        myIntent.setAction("commandFromActivity");  
        sendBroadcast(myIntent);//发送广播  
		super.onDestroy();
	}
	/*
	 * (non-Javadoc)
	 * @see com.youeclass.BaseActivity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	 public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, Menu.FIRST+1, 1, "设置").setIcon(android.R.drawable.ic_menu_edit);
		menu.add(Menu.NONE, Menu.FIRST+2, 2, "帮助").setIcon(android.R.drawable.ic_menu_help);
		menu.add(Menu.NONE, Menu.FIRST+4, 4, "注销").setIcon(android.R.drawable.ic_menu_set_as);
		return true;
	}
	/*
	 * (non-Javadoc)
	 * @see com.youeclass.BaseActivity#onOptionsItemSelected(android.view.MenuItem)
	 */
	 @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		 switch(item.getItemId()){
			 case Menu.FIRST+1:{
				 //设置
				 this.startSettingActivity();
				 break;
			 }
			 case Menu.FIRST+2:{
				 //帮助
				 this.startActivity(new Intent(this,HelpActivity.class));
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
						//停止下载服务
						//Toast.makeText(this, "发请求注销", Toast.LENGTH_LONG).show();
						//MobclickAgent.onEvent(MainActivity.this,"LoginOut");
						appContext.cleanLoginInfo();
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
		
		protected void startSettingActivity(){
			Intent intent = new Intent(this,SettingActivity.class);
			intent.putExtra("username", username);
			this.startActivity(intent);
		};
}