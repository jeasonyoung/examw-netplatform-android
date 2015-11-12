package com.examw.netschool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.MyCourseDao;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.PackageClass;
import com.examw.netschool.util.APIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 我的课程Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月1日
 */
public class MyCourseActivity extends Activity {
	private static final String TAG = "MyCourseActivity";
	
	private LinearLayout nodataView;
	private ProgressDialog progressDialog;
	
	private final List<PackageClass> courses;
	private final MyCourseAdapter adapter;
	/**
	 * 构造函数。
	 */
	public MyCourseActivity(){
		Log.d(TAG, "初始化...");
		this.courses = new ArrayList<PackageClass>();
		this.adapter = new MyCourseAdapter(this.courses);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, " 重载创建...");
		//设置布局文件
		this.setContentView(R.layout.activity_my_courses);
		//返回按钮处理
		final View btnReturn = this.findViewById(R.id.btn_return);
		//设置返回按钮事件处理
		btnReturn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d(TAG, "返回按钮点击处理..." + v);
				finish();
			}
		});
		//设置标题
		final TextView tvTopTitle = (TextView)this.findViewById(R.id.top_title);
		if(tvTopTitle != null){
			tvTopTitle.setText(R.string.my_course_title);
		}
		//无数据View
		this.nodataView = (LinearLayout)this.findViewById(R.id.nodata_view);
		//数据列表
		final ExpandableListView listView = (ExpandableListView)this.findViewById(R.id.list_courses);
		//设置分组分隔
		listView.setGroupIndicator(null);
		//设置数据适配器
		listView.setAdapter(this.adapter);
		//设置分组点击事件处理
		listView.setOnGroupClickListener(this.onGroupClickListener);
		//设置分组子节点点击事件处理
		listView.setOnChildClickListener(this.onChildClickListener);
		
		//在线课程
		final TextView btnOnline = (TextView)this.findViewById(R.id.btn_free_experience);
		//设置图片
		final Drawable topOnlineIcon = this.getResources().getDrawable(R.drawable.my_course_footer_online_icon);
		if(topOnlineIcon != null){
			topOnlineIcon.setBounds(0, 0, topOnlineIcon.getMinimumWidth(), topOnlineIcon.getMinimumHeight());
			btnOnline.setCompoundDrawables(null, topOnlineIcon, null, null);
		}
		//设置文字
		btnOnline.setText(R.string.my_course_footer_online_title);
		
		//离线课程
		final TextView btnOffline = (TextView)this.findViewById(R.id.btn_my_course);
		//设置图片
		final Drawable topOfflineIcon = this.getResources().getDrawable(R.drawable.my_course_footer_offline_icon);
		if(topOfflineIcon != null){
			topOfflineIcon.setBounds(0, 0, topOfflineIcon.getMinimumWidth(), topOfflineIcon.getMinimumHeight());
			btnOffline.setCompoundDrawables(null, topOfflineIcon, null, null);
		}
		//设置文字
		btnOffline.setText(R.string.my_course_footer_offline_title);
		//设置点击事件处理
		btnOffline.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d(TAG, "离线课程点击事件... " + v);
				//初始化意图
				final Intent intent = new Intent(MyCourseActivity.this, DownloadActivity.class);
				//设置离线课程索引
				intent.putExtra(DownloadActivity.CONST_FRAGMENT_INDEX, DownloadActivity.CONST_FRAGMENT_FINISH);
				//发送意图
				startActivity(intent);
				//关闭当前
				finish();
			}
		});
		
		//播放记录
		final View btnPlayRecord = this.findViewById(R.id.btn_play_record);
		btnPlayRecord.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Log.d(TAG, "播放记录点击处理...");
				//发送意图
				startActivity(new Intent(MyCourseActivity.this, PlayRecordActivity.class));
				//关闭当前
				finish();
			}
		});
		//
		super.onCreate(savedInstanceState);
	}
	//分组点击事件处理
	private OnGroupClickListener onGroupClickListener = new OnGroupClickListener(){
		/*
		 * 点击分组事件处理。
		 * @see android.widget.ExpandableListView.OnGroupClickListener#onGroupClick(android.widget.ExpandableListView, android.view.View, int, long)
		 */
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			Log.d(TAG, "分组点击事件处理..." + groupPosition);
			// 如果没有子类了,表示是单独的课程
			if(parent.getExpandableListAdapter().getChildrenCount(groupPosition) == 0){
				//我的课程
				final PackageClass course = (PackageClass)adapter.getGroup(groupPosition);
				if(course == null){
					Log.d(TAG, "获取分组["+groupPosition+"]数据失败!");
					return false;
				}
				//判断是否为班级
				if(course.IsClass()){
					//班级
					gotoActivity(course);
					
					return true;
				}
			}
			return false;
		}
		
	};
	//分组子节点事件处理
	private OnChildClickListener onChildClickListener = new OnChildClickListener(){
		/*
		 * 分组子节点事件处理。
		 * @see android.widget.ExpandableListView.OnChildClickListener#onChildClick(android.widget.ExpandableListView, android.view.View, int, int, long)
		 */
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			Log.d(TAG, "分组["+groupPosition+"]下节点["+childPosition+"]点击事件处理...");
			//我的课程
			final PackageClass course = (PackageClass)adapter.getChild(groupPosition, childPosition);
			if(course == null){
				Log.d(TAG, "获取分组["+groupPosition+"]下节点["+childPosition+"]数据失败!");
				return false;
			}
			//判断是否为班级
			if(course.IsClass()){
				//
				gotoActivity(course);
				return true;
			}
			return false;
		}
		
	};
	//跳转到Activity。
	private void gotoActivity(final PackageClass course){
		if(course == null) return;
		//我的课程－班级处理
		final Intent intent = new Intent(this, MyCourseLessonActivity.class);
		intent.putExtra(Constant.CONST_CLASS_ID, course.getId());
		intent.putExtra(Constant.CONST_CLASS_NAME, course.getName());
		//跳转
		this.startActivity(intent);
	}
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载开始...");
		//数据加载等待
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, this.getResources().getText(R.string.progress_msg), true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//显示等待
		this.progressDialog.show();
		//异步线程加载数据
		new AsyncTask<Void, Void, List<PackageClass>>() {
			/*
			 * 后台线程加载数据。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected List<PackageClass> doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载数据...");
					//初始化
					final MyCourseDao courseDao = new MyCourseDao();
					//初始化上下文
					final AppContext appContext = (AppContext)getApplicationContext();
					//检查是否从网络下载数据
					if(appContext != null && appContext.isNetworkConnected()){
						Log.d(TAG, " 将从网络下载课程数据...");
						//初始化参数
						final Map<String, Object> parameters = new HashMap<String, Object>();
						//设置用户ID
						parameters.put("randUserId", AppContext.getCurrentUserId());
						//发送请求
						final JSONCallback<PackageClass[]> callback = new APIUtils.CallbackJSON<PackageClass[]>(PackageClass[].class)
								.sendGETRequest(getResources(), R.string.api_courses_url, parameters);
						//
						if(callback.getSuccess()){
							//清空数据
							courseDao.deleteAll();
							//新增数据
							courseDao.add(callback.getData());
						}else{
							Log.e(TAG, callback.getMsg());
						}
						
					}
					//查询数据
					return courseDao.loadCourses(null);
				}catch(Exception e){
					Log.d(TAG, "加载数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前台主线程更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(List<PackageClass> result) {
				Log.d(TAG, "前台主线程处理...");
				//关闭等待动画
				if(progressDialog != null) progressDialog.dismiss();
				//清空数据
				courses.clear();
				//更新数据
				if(result != null && result.size() > 0){
					Log.d(TAG, "更新数据...");
					courses.addAll(result);
				}
				//是否显示有无数据
				nodataView.setVisibility(courses.size() > 0 ? View.GONE : View.VISIBLE);
				//通知数据适配器更新数据
				adapter.notifyDataSetChanged();
			}
		}.execute((Void)null);
		//
		super.onStart();
	}
	//数据适配器
	private class MyCourseAdapter extends BaseExpandableListAdapter{
		private static final String TAG = "MyCourseAdapter";
		private final List<PackageClass> groups;
		private final SparseArray<PackageClass[]> childCourses;
		/**
		 * 构造函数。
		 * @param courses
		 */
		public MyCourseAdapter(List<PackageClass> courses){
			Log.d(TAG, "初始化...");
			this.groups = courses;
			this.childCourses = new SparseArray<PackageClass[]>();
		}
		/*
		 * 获取分组数据总数。
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			return (this.groups == null) ? 0 : this.groups.size();
		}
		/*
		 * 获取分组下子节点总数。
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		@Override
		public int getChildrenCount(int groupPosition) {
			Log.d(TAG, "获取分组["+groupPosition+"]下子节点总数...");
			PackageClass[] childs = this.childCourses.get(groupPosition, null);
			if(childs == null || childs.length == 0){
				final PackageClass parent = this.groups.get(groupPosition);
				if(parent == null){
					Log.d(TAG, "加载分组["+groupPosition+"]课程数据失败!");
					return 0;
				}
				//初始化
				final MyCourseDao courseDao = new MyCourseDao();
				//加载数据
				final List<PackageClass> list = courseDao.loadCourses(parent.getId());
				if(list != null && list.size() > 0){
					childs = list.toArray(new PackageClass[0]);
					this.childCourses.put(groupPosition, childs);
				}
			}
			return (childs == null) ? 0 : childs.length;
		}
		/*
		 * 获取分组数据。
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		@Override
		public Object getGroup(int groupPosition) {
			Log.d(TAG, "获取分组数据...." + groupPosition);
			return (this.groups == null || this.groups.size() < groupPosition) ? null : this.groups.get(groupPosition);
		}
		/*
		 * 获取分组下子节点数据。
		 * @see android.widget.ExpandableListAdapter#getChild(int, int)
		 */
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			Log.d(TAG, "获取分组["+groupPosition+"]下子节点["+childPosition+"]数据...");
			final PackageClass[] courses = this.childCourses.get(groupPosition, null);
			if(courses != null && courses.length > childPosition){
				return courses[childPosition];
			}
			return null;
		}
		/*
		 *  获取分组ID。
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {
			Log.d(TAG, "获取分组ID..." + groupPosition);
			return groupPosition;
		}
		/*
		 * 获取分组下子节点ID。
		 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
		 */
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			Log.d(TAG, "获取分组["+groupPosition+"]下子节点ID["+childPosition+"]...");
			return groupPosition * 100 + childPosition;
		}
		/*
		 * 如果返回true表示子项和组的ID始终表示一个固定的组件对象
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}
		/*
		 * 加载分组视图View。
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			Log.d(TAG, "加载分组["+groupPosition+"]视图UI...");
			GroupViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新增分组["+groupPosition+"]View...");
				//加载视图
				convertView = LayoutInflater.from(MyCourseActivity.this).inflate(R.layout.activity_my_courses_item_group, parent, false);
				//初始化
				viewHolder = new GroupViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else {
				Log.d(TAG, "重复分组["+groupPosition+"]View...");
				viewHolder = (GroupViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((PackageClass)this.getGroup(groupPosition));
			//返回视图
			return convertView;
		}
		/*
		 * 获取分组下子节点视图View。
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			Log.d(TAG, "加载分组["+groupPosition+"]下子节点["+childPosition+"]视图UI...");
			ChildViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新增分组["+groupPosition+"]下节点["+childPosition+"]View...");
				//加载视图
				convertView = LayoutInflater.from(MyCourseActivity.this).inflate(R.layout.activity_my_courses_item_child, parent, false);
				//初始化
				viewHolder = new ChildViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else {
				Log.d(TAG, "重复分组["+groupPosition+"]下节点["+childPosition+"]View...");
				viewHolder = (ChildViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((PackageClass)this.getChild(groupPosition, childPosition));
			//返回视图
			return convertView;
		}
		/*
		 * 判断指定的子选择项是否被选择
		 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
		 */
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return true;
		}
		
	}
	//分组View
	private class GroupViewHolder{
		private TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public GroupViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.tv_my_course_item_group);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(PackageClass data){
			if(data != null && this.tvTitle != null){
				this.tvTitle.setText(data.getName());
			}
		}
	}
	//子View
	private class ChildViewHolder{
		private TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ChildViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.tv_my_course_item_child);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(PackageClass data){
			if(data != null && this.tvTitle != null){
				this.tvTitle.setText(data.getName());
			}
		}
	}
}