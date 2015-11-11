package com.examw.netschool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.FreeExperienceActivity.Search;
import com.examw.netschool.app.AppContext;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.PackageClass;
import com.examw.netschool.util.APIUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.TextView;

/**
 * 考试下套餐/班级Fragment.
 * 
 * @author jeasonyoung
 * @since 2015年9月18日
 */
public class FreeExperienceFragmentByPackages extends Fragment {
	private static final String TAG = "FreeExperienceFragmentByPackages";
	private final String userId, examId;
	private final Search search;
	
	private final List<PackageClass> packageClasses;
	private final PackageAdapter adapter;
	
	private View nodataView;
	/**
	 * 构造函数。
	 * @param userId
	 * @param examId
	 * @param search
	 */
	public FreeExperienceFragmentByPackages(String userId, String examId, Search search){
		Log.d(TAG, "初始化...");
		this.userId = userId;
		this.examId = examId;
		this.search = search;
		this.search.setOnClickListener(this.onSearchClickListener);
		
		this.packageClasses = new ArrayList<PackageClass>();
		this.adapter = new PackageAdapter(this.packageClasses);
	}
	/*
	 * 重载创建View。
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		Log.d(TAG, "重载创建View... ");
		//加载View布局文件
		final View view = inflater.inflate(R.layout.activity_free_experience_packages, container, false);
		//无数据View
		this.nodataView = view.findViewById(R.id.nodata_view);
		//列表View
		final ExpandableListView listView = (ExpandableListView)view.findViewById(R.id.list_packages);
		listView.setGroupIndicator(null);
		listView.setOnGroupClickListener(this.onGroupClickListener);
		listView.setOnChildClickListener(this.onChildClickListener);
		listView.setAdapter(this.adapter);
		//返回View。
		return view;
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		Log.d(TAG, "重载启动...");
		//异步加载数据
		new AsyncLoadData().execute((Void)null);
		//
		super.onStart();
	}
	//搜索事件处理
	private OnClickListener onSearchClickListener = new OnClickListener(){
		/*
		 * 事件处理。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			final String searchKey = search.getSearchKey();
			if(StringUtils.isBlank(searchKey)){
				//异步加载数据
				new AsyncLoadData().execute((Void)null);
				return;
			}
			//没有数据
			if(packageClasses == null || packageClasses.size() == 0) return;
			//过滤数据
			new AsyncTask<String, Void, List<PackageClass>>(){
				/*
				 * 后台线程处理数据。
				 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
				 */
				@Override
				protected List<PackageClass> doInBackground(String... params) {
					if(StringUtils.isBlank(params[0])) return packageClasses;
					//初始化 
					final  List<PackageClass> taget = new ArrayList<PackageClass>();
					for(PackageClass data : packageClasses){
						if(data == null || StringUtils.isBlank(data.name) || !data.IsClass()) continue;
						if(data.name.indexOf(params[0]) > -1){
							data.pid = (null);
							taget.add(data);
						}
					}
					return taget;
				}
				/*
				 * 前台主线程更新UI。
				 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
				 */
				protected void onPostExecute(List<PackageClass> result) {
					Log.d(TAG, "前台数据处理...");
					//清空数据源
					packageClasses.clear();
					//填充结果数据
					if(result != null && result.size() > 0){
						packageClasses.addAll(result);
					}
					//没有数据
					nodataView.setVisibility(packageClasses.size() == 0 ? View.VISIBLE : View.GONE);
					//通知适配器更新
					adapter.notifyDataSetChanged();
				};
			}.execute(searchKey);
		}
	};
	//分组事件点击
	private OnGroupClickListener onGroupClickListener = new OnGroupClickListener() {
		/*
		 * 点击事件处理。
		 * @see android.widget.ExpandableListView.OnGroupClickListener#onGroupClick(android.widget.ExpandableListView, android.view.View, int, long)
		 */
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			Log.d(TAG, "分组点击事件处理..." + groupPosition);
			// 如果没有子类
			if(parent.getExpandableListAdapter().getChildrenCount(groupPosition) == 0){
				//获取数据适配器
				final PackageAdapter adapter = (PackageAdapter)parent.getExpandableListAdapter();
				if(adapter == null){
					Log.d(TAG, "获取数据适配器失败!分组:" + groupPosition);
					return false;
				}
				//套餐/班级
				final PackageClass data = (PackageClass)adapter.getGroup(groupPosition);
				if(data != null && data.IsClass()){
					//
					gotoLessonFragment(data);
					//返回
					return true;
				}
			}
			return false;
		}
	};
	//分组子数据事件点击。
	private OnChildClickListener onChildClickListener = new OnChildClickListener() {
		/*
		 * 点击事件处理。
		 * @see android.widget.ExpandableListView.OnChildClickListener#onChildClick(android.widget.ExpandableListView, android.view.View, int, int, long)
		 */
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			Log.d(TAG, "分组["+groupPosition+"]下节点["+childPosition+"]点击事件处理...");
			//获取数据适配器
			final PackageAdapter adapter = (PackageAdapter)parent.getExpandableListAdapter();
			if(adapter == null){
				Log.d(TAG, "获取数据适配器失败![" + groupPosition + "]["+ childPosition +"]");
				return false;
			}
			//套餐/班级
			final PackageClass data = (PackageClass)adapter.getChild(groupPosition, childPosition);
			if(data != null && data.IsClass()){
				//
				gotoLessonFragment(data);
				//返回
				return true;
			}
			return false;
		}
	};
	//
	private void gotoLessonFragment(PackageClass data){
		if(data == null || StringUtils.isBlank(data.id) || !data.IsClass()) return;
		//替换Frame
		getActivity().getSupportFragmentManager()
						.beginTransaction()
						.addToBackStack(null)
						.replace(R.id.fragment_container, new FreeExperienceFragmentByLesson(userId, data.id, this.search))
						.commit();
	}
	//异步加载数据。
	private class AsyncLoadData  extends AsyncTask<Void, Void, List<PackageClass>>{
		/*
		 * 后台线程加载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<PackageClass> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步线程下载数据...");
				//检查数据
				if(StringUtils.isBlank(examId)){
					Log.d(TAG, "所属考试ID为空!");
					return null;
				}
				//检查网络
				final AppContext appContext = (AppContext)getActivity().getApplicationContext();
				if(appContext == null || !appContext.isNetworkConnected()){
					Log.d(TAG, "获取上线文失败或网络不可用!");
					return null;
				}
				//初始化参数
				final Map<String, Object> parameters = new HashMap<String, Object>();
				//设置考试ID
				parameters.put("examId", examId);
				//请求数据
				final JSONCallback<PackageClass[]> callback = new APIUtils.CallbackJSON<PackageClass[]>().sendGETRequest(getResources(),
						R.string.api_packages_url, parameters);
				 //
			    if(callback.getSuccess()){
			    	return Arrays.asList(callback.getData());
			    }else{
			    	Log.e(TAG, "下载网络异常:" + callback.getMsg());
			    }
			}catch(Exception e){
				Log.e(TAG, "异步线程下载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程处理数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<PackageClass> result) {
			Log.d(TAG, "前台数据处理...");
			//清空数据源
			packageClasses.clear();
			//填充结果数据
			if(result != null && result.size() > 0){
				packageClasses.addAll(result);
			}
			//没有数据
			nodataView.setVisibility(packageClasses.size() == 0 ? View.VISIBLE : View.GONE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器
	private class PackageAdapter extends BaseExpandableListAdapter{
		private static final String TAG = "PackageAdapter";
		private final List<PackageClass> list;
		private List<PackageClass>  groups;
		private SparseArray<PackageClass[]> childArrays;
		/**
		 * 构造函数。
		 * @param list
		 */
		public PackageAdapter(List<PackageClass> list){
			Log.d(TAG, "初始化...");
			this.list = list;
		}
		/*
		 * 获取分组总数。
		 * @see android.widget.ExpandableListAdapter#getGroupCount()
		 */
		@Override
		public int getGroupCount() {
			Log.d(TAG, "获取分组数...");
			this.groups = new ArrayList<PackageClass>();
			if(this.list != null && this.list.size() > 0){
				for(PackageClass data : this.list){
					if(data != null && StringUtils.isBlank(data.pid)){
						this.groups.add(data);
					}
				}
			}
			return this.groups.size();
		}
		/*
		 * 获取子节点数。
		 * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
		 */
		@Override
		public int getChildrenCount(int groupPosition) {
			this.childArrays = new SparseArray<PackageClass[]>();
			if(this.list != null && this.groups != null && this.groups.size() > groupPosition){
				final PackageClass group = this.groups.get(groupPosition);
				if(group != null && StringUtils.isNotBlank(group.id)){
					final List<PackageClass> childs = new ArrayList<PackageClass>();
					for(PackageClass data : this.list){
						if(data != null && StringUtils.equalsIgnoreCase(data.pid, group.id)){
							childs.add(data);
						}
					}
					this.childArrays.put(groupPosition, childs.toArray(new PackageClass[0]));
				}
			}
			return this.childArrays.size();
		}
		/*
		 * 获取分组数据对象。
		 * @see android.widget.ExpandableListAdapter#getGroup(int)
		 */
		@Override
		public Object getGroup(int groupPosition) {
			if(this.groups != null && this.groups.size() > groupPosition){
				return this.groups.get(groupPosition);
			}
			return null;
		}
		/*
		 * 获取子数据对象。
		 * @see android.widget.ExpandableListAdapter#getChild(int, int)
		 */
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			if(this.childArrays != null && this.childArrays.size() > 0){
				final PackageClass[] childs = this.childArrays.get(groupPosition);
				if(childs != null && childs.length > childPosition){
					return childs[childPosition];
				}
			}
			return null;
		}
		/*
		 * 获取分组数据ID。
		 * @see android.widget.ExpandableListAdapter#getGroupId(int)
		 */
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		/*
		 * 获取子数据ID。
		 * @see android.widget.ExpandableListAdapter#getChildId(int, int)
		 */
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return groupPosition * 100 + childPosition;
		}
		/*
		 *  如果返回true表示子项和组的ID始终表示一个固定的组件对象
		 * @see android.widget.ExpandableListAdapter#hasStableIds()
		 */
		@Override
		public boolean hasStableIds() {
			return true;
		}
		/*
		 * 获取分组数据View
		 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取分组数据View... " + groupPosition);
			GroupViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建分组View..." + groupPosition);
				//加载布局文件
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_free_experience_packages_group, parent, false);
				//初始化
				viewHolder = new GroupViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用分组View..." + groupPosition);
				viewHolder = (GroupViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((PackageClass)this.getGroup(groupPosition));
			//返回
			return convertView;
		}
		/*
		 * 获取子数据View
		 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取分组["+groupPosition+"]子["+childPosition+"]数据View... ");
			ChildViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建子数据View..." + childPosition);
				//加载布局
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_free_experience_packages_child, parent, false);
				//初始化
				viewHolder = new ChildViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用分组子View..." + childPosition);
				viewHolder = (ChildViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((PackageClass)this.getChild(groupPosition, childPosition));
			//返回
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
	//分组数据View
	private class GroupViewHolder{
		private final TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public GroupViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.group_title);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(PackageClass data){
			if(data != null){
				this.tvTitle.setText(data.name);
			}
		}
	}
	//子数据View
	private class ChildViewHolder{
		private final TextView tvTitle;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ChildViewHolder(View convertView){
			this.tvTitle = (TextView)convertView.findViewById(R.id.child_title);
		}
		/**
		 * 加载数据。
		 * @param data
		 */
		public void loadData(PackageClass data){
			if(data != null){
				this.tvTitle.setText(data.name);
			}
		}
	}
}