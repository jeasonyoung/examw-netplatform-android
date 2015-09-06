package com.examw.netschool;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.examw.netschool.adapter.MyCourseLessonAdapter;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.AppContext.LoginState;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 课程资源列表Activity.
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class MyCourseLessonActivity extends Activity implements OnClickListener{
	private static final String TAG = "MyCourseLessonActivity";
	
	private ImageButton rbtn;
	private TextView title;
	private ListView list;
	private LinearLayout online,outline,playrecord,downloaded,nodata;
	private String userId,userName,classId;
	
	private AppContext appContext;
	
	private List<Lesson> lessons;
	private MyCourseLessonAdapter adapter;
	
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "初始化...");
		
		this.setContentView(R.layout.activity_mycourse);
		
		this.appContext = (AppContext)this.getApplication();
		
		final Intent intent = this.getIntent();
		this.userId = intent.getStringExtra(Constant.CONST_USERID);
		this.userName = intent.getStringExtra(Constant.CONST_USERNAME);
		this.classId = intent.getStringExtra(Constant.CONST_CLASS_ID);
		
		this.rbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.rbtn.setOnClickListener(this);
		
		this.title = (TextView) this.findViewById(R.id.courseTitle);
		this.title.setText(intent.getStringExtra(Constant.CONST_CLASS_NAME));
		
		this.list = (ListView) this.findViewById(R.id.courserList);
		//设置缓存颜色为透明
		this.list.setCacheColorHint(Color.TRANSPARENT);
		this.list.setAlwaysDrawnWithCacheEnabled(true);
		this.lessons = new ArrayList<Lesson>();
		this.adapter = new MyCourseLessonAdapter(this, this.userId, this.lessons);
		this.list.setAdapter(this.adapter);
		
		
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		
		this.downloaded = (LinearLayout) this.findViewById(R.id.Downloadto_layout_btn);
		this.downloaded.setOnClickListener(this);
		
		this.outline = (LinearLayout) this.findViewById(R.id.MyfileDown_layout_btn);
		this.outline.setOnClickListener(this);
		
		this.online = (LinearLayout) this.findViewById(R.id.Lookonline_layout_btn);
		this.online.setOnClickListener(this);
		
		this.playrecord = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		this.playrecord.setOnClickListener(this);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "重载启动...");
		new AsyncLoadData().execute((Void)null);
	}
	/*
	 * 按钮点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件处理..." + v);
		switch(v.getId()){
			case R.id.returnbtn:{
				Log.d(TAG, "返回事件处理...");
				this.finish();
				break;
			}
			case R.id.Downloadto_layout_btn:{
				Log.d(TAG, "下载点击处理...");
				//
				final Intent intent = new Intent(this, DownloadActivity.class);
				intent.putExtra(Constant.CONST_USERID, this.userId);
				intent.putExtra(Constant.CONST_USERNAME, this.userName);
				this.startActivity(intent);
				
				break;
			}
			case R.id.MyfileDown_layout_btn:{
				Log.d(TAG, "离线点击处理...");
				//
				final Intent intent = new Intent(this, DownloadActivity.class);
				intent.putExtra(Constant.CONST_USERID, this.userId);
				intent.putExtra(Constant.CONST_USERNAME, this.userName);
				this.startActivity(intent);
				
				break;
			}
			case R.id.Lookonline_layout_btn:{
				Log.d(TAG, "在线播放...");
				break;
			}
			case R.id.LearningRecord_layout_btn:{
				Log.d(TAG, "播放记录点击处理...");
				
				final Intent intent = new Intent(this, PlayrecordActivity.class);
				intent.putExtra(Constant.CONST_USERID, this.userId);
				intent.putExtra(Constant.CONST_USERNAME, this.userName);
				this.startActivity(intent);
				
				break;
			}
		}
	}
	//异步加载数据。
	private class AsyncLoadData extends AsyncTask<Void, Void, Lesson[]>{
		/*
		 * 重载后台异步线程。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected Lesson[] doInBackground(Void... params) {
			try {
				Log.d(TAG, "异步加载数据...");
				if(StringUtils.isBlank(userId)){
					Log.d(TAG, "当前用户ID为空!");
					return null;
				}
				if(StringUtils.isBlank(classId)){
					Log.d(TAG, "当前ClassID为空!");
					return null;
				}
				//初始化数据操作
				final LessonDao dao = new LessonDao(MyCourseLessonActivity.this, userId);
				//在线登录，且存在网络连接则下载
				if(appContext != null && appContext.getLoginState() == LoginState.LOGINED && appContext.isNetworkConnected()){
					//请求网络数据
					final String result = DigestClientUtil.sendDigestGetRequest(Constant.DOMAIN_URL + "/api/m/lessons/"+ classId  +".do");
					if(StringUtils.isNotBlank(result)){
						//解析反馈JSON
						final Gson gson = new Gson();
						final Type type = new TypeToken<JSONCallback<Lesson[]>>(){}.getType();
						//
						final JSONCallback<Lesson[]> callback = gson.fromJson(result, type);
						if(callback.getSuccess()){
							//删除原有记录
							dao.deleteByClass(classId);
							//新增记录
							dao.add(classId, callback.getData());
							//返回
							return callback.getData();
						}else{
							Log.e(TAG, "下载课程资源失败:" + callback.getMsg());
						}
					}
				}
				//加载数据库中的数据
				final List<Lesson> list =  dao.loadLessonsByClass(classId);
				return (list == null || list.size() == 0) ? null : list.toArray(new Lesson[0]); 
			} catch (Exception e) {
				Log.e(TAG, "异步加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主界面更新。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Lesson[] result) {
			Log.d(TAG, "前台主界面更新...");
			if(result == null || result.length == 0){
				nodata.setVisibility(View.VISIBLE);
				return;
			}
			//添加数据
			lessons.clear();
			lessons.addAll(Arrays.asList(result));
			//通知数据适配器更新
			adapter.notifyDataSetChanged();
		}
	}
}