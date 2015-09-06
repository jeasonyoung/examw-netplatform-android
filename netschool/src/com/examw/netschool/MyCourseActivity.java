package com.examw.netschool;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.examw.netschool.adapter.MyExpandableAdapter;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.AppContext.LoginState;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.MyCourseDao;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.MyCourse;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 我的课程。
 * 
 * @author jeasonyoung
 * @since 2015年9月1日
 */
public class MyCourseActivity extends Activity implements OnClickListener {
	private static final String TAG = "MyCourseActivity";
	
	private ProgressDialog dialog;
	private ExpandableListView expandList;
	private LinearLayout nodata, outlineCourse, playrecord;
	private ImageButton returnBtn;
	private String userId,userName;
	
	private List<MyCourse> listGroups;
	private MyExpandableAdapter adapter;
	
	private AppContext appContext;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "初始化...");
		
		this.setContentView(R.layout.activity_mycourselist);
		
		this.appContext = (AppContext)this.getApplication();
		
		final Intent intent = this.getIntent();
		this.userId = intent.getStringExtra(Constant.CONST_USERID);
		this.userName = intent.getStringExtra(Constant.CONST_USERNAME);
		
		this.expandList = (ExpandableListView) this.findViewById(R.id.explist2);
		this.expandList.setGroupIndicator(null);
		this.expandList.setOnGroupClickListener(new GroupClickListener());
		this.expandList.setOnChildClickListener(new ChildClickListener());
		this.listGroups = new ArrayList<MyCourse>();
		this.adapter = new MyExpandableAdapter(this, this.userId, this.listGroups);
		this.expandList.setAdapter(this.adapter);
		
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnBtn.setOnClickListener(this);
		
		this.outlineCourse = (LinearLayout) this.findViewById(R.id.MyfileDown_layout_btn);
		this.outlineCourse.setOnClickListener(this);
		
		this.playrecord = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		this.playrecord.setOnClickListener(this);
		
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		
		this.dialog = ProgressDialog.show(MyCourseActivity.this, null, "努力加载中请稍候",true, true);
		this.dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
	}
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		super.onStart();
		//
		this.dialog.show();
		//异步加载我的课程。
		new GetMyLessonThread().execute((Void)null);
	}
	/*
	 * 点击事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件处理..." + v);
		switch(v.getId()){
			case R.id.returnbtn:{
				Log.d(TAG, "返回按钮点击处理...");
				this.finish();
				break;
			}
			case R.id.MyfileDown_layout_btn:{
				Log.d(TAG, "离线课程点击处理...");
				
				final Intent intent = new Intent(this, DownloadActivity.class);
				intent.putExtra(Constant.CONST_USERID, this.userId);
				intent.putExtra(Constant.CONST_USERNAME, this.userName);
				this.startActivity(intent);
				
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
	/**
	 * 异步线程加载我的课程。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月5日
	 */
	private class GetMyLessonThread extends AsyncTask<Void, Void, Void>  {
		private MyCourseDao dao;
		/*
		 * 异步线程下载我的课程。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected Void doInBackground(Void... params) {
			try {
				Log.d(TAG, "异步线程下载我的课程...");
				//
				if(StringUtils.isBlank(userId)) return null;
				//
				if(MyCourseActivity.this.appContext.getLoginState() == LoginState.LOCAL) return null;
				//
				final String result = DigestClientUtil.sendDigestGetRequest(Constant.DOMAIN_URL + "/api/m/courses/" + userId + ".do");
				if(StringUtils.isBlank(result)) return null;
				//解析数据
				final Gson gson = new Gson();
				final Type type = new TypeToken<JSONCallback<MyCourse[]>>(){}.getType();
				final JSONCallback<MyCourse[]> callback = gson.fromJson(result, type);
				//
				if(!callback.getSuccess() || callback.getData() == null || callback.getData().length == 0) return null;
				//惰性加载
				if(this.dao == null){
					this.dao = new MyCourseDao(MyCourseActivity.this, userId);
				}
				//清空数据
				this.dao.deleteAll();
				//新增数据
				this.dao.add(callback.getData());
			} catch (Exception e) {
				Log.e(TAG, "下载课程异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 加载我的课程。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(Void result) {
			try {
				Log.d(TAG, "加载我的课程...");
				//惰性加载
				if(this.dao == null){
					this.dao = new MyCourseDao(MyCourseActivity.this, userId);
				}
				//加载根节点数据
				final List<MyCourse> listCourses = this.dao.loadCourses(null);
				if(listCourses != null && listCourses.size() > 0){
					Log.d(TAG, "加载数据显示...");
					//清空数据集合
					listGroups.clear();
					//添加数据分组数据集合。
					listGroups.addAll(listCourses);
					//通知适配器更新
					adapter.notifyDataSetChanged();
				}else {
					Log.d(TAG, "无数据显示...");
					nodata.setVisibility(View.VISIBLE);// 无数据显示
				}
			} catch (Exception e) {
				Log.e(TAG, "加载我的课程异常:" + e.getMessage(), e);
				nodata.setVisibility(View.VISIBLE);// 无数据显示
			} finally{
				dialog.dismiss();
			}
		}
	}
	/**
	 * 分组点击事件监听器。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月5日
	 */
	private class GroupClickListener implements OnGroupClickListener {
		/*
		 * 分组点击事件处理。
		 * @see android.widget.ExpandableListView.OnGroupClickListener#onGroupClick(android.widget.ExpandableListView, android.view.View, int, long)
		 */
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			Log.d(TAG, "分组点击事件处理..." + groupPosition);
			// 如果没有子类了,表示是单独的课程
			if(parent.getExpandableListAdapter().getChildrenCount(groupPosition) == 0){
				//获取数据适配器
				final MyExpandableAdapter adapter = (MyExpandableAdapter)parent.getExpandableListAdapter();
				if(adapter == null){
					Log.d(TAG, "获取数据适配器失败!分组:" + groupPosition);
					return false;
				}
				//我的课程
				final MyCourse course = (MyCourse)adapter.getGroup(groupPosition);
				if(course == null){
					Log.d(TAG, "获取分组["+groupPosition+"]数据失败!");
					return false;
				}
				//判断是否为班级
				if(StringUtils.equalsIgnoreCase(course.getType(), MyCourse.TYPE_CLASS)){
					//班级。
					final Intent intent = new Intent(MyCourseActivity.this, MyCourseLessonActivity.class);
					
					intent.putExtra(Constant.CONST_USERID, userId);
					intent.putExtra(Constant.CONST_USERNAME, userName);
					intent.putExtra(Constant.CONST_CLASS_ID, course.getId());
					intent.putExtra(Constant.CONST_CLASS_NAME, course.getName());
					
					startActivity(intent);
					
					return true;
				}
			}
			return false;
		}
	}
	/**
	 * 子节点点击事件处理。
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月5日
	 */
	private class ChildClickListener implements OnChildClickListener {
		/*
		 * 子节点事件处理。
		 * @see android.widget.ExpandableListView.OnChildClickListener#onChildClick(android.widget.ExpandableListView, android.view.View, int, int, long)
		 */
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			Log.d(TAG, "分组["+groupPosition+"]下节点["+childPosition+"]点击事件处理...");
			//获取数据适配器
			final MyExpandableAdapter adapter = (MyExpandableAdapter)parent.getExpandableListAdapter();
			if(adapter == null){
				Log.d(TAG, "获取数据适配器失败![" + groupPosition + "]["+ childPosition +"]");
				return false;
			}
			//我的课程
			final MyCourse course = (MyCourse)adapter.getChild(groupPosition, childPosition);
			if(course == null){
				Log.d(TAG, "获取分组["+groupPosition+"]下节点["+childPosition+"]数据失败!");
				return false;
			}
			//判断是否为班级
			if(StringUtils.equalsIgnoreCase(course.getType(), MyCourse.TYPE_CLASS)){
				//班级。
				final Intent intent = new Intent(MyCourseActivity.this, MyCourseLessonActivity.class);
				
				intent.putExtra(Constant.CONST_USERID, userId);
				intent.putExtra(Constant.CONST_USERNAME, userName);
				intent.putExtra(Constant.CONST_CLASS_ID, course.getId());
				intent.putExtra(Constant.CONST_CLASS_NAME, course.getName());
				
				startActivity(intent);
				
				return true;
			}
			return false;
		}
	}
}