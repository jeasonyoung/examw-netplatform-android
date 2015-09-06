package com.examw.netschool.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.examw.netschool.R;
import com.examw.netschool.dao.MyCourseDao;
import com.examw.netschool.model.MyCourse;
/**
 * 数据适配器。
 * 
 * @author jeasonyoung
 * @since 2015年9月5日
 */
public class MyExpandableAdapter extends BaseExpandableListAdapter{
	private static final String TAG = "MyExpandableAdapter";
	private final Context context;
	private final String userId;
	private final List<MyCourse> groups;
	private final SparseArray<MyCourse[]> childArrays;
	private final LayoutInflater inflater;
	private MyCourseDao dao;
	/**
	 * 构造函数。
	 * @param context
	 * @param userId
	 * @param groups
	 */
	public MyExpandableAdapter(Context context, String userId, List<MyCourse> groups){
		Log.d(TAG, "构造函数...");
		this.context = context;
		this.inflater = LayoutInflater.from(this.context);
		
		this.userId = userId;
		this.groups = groups;
		this.childArrays = new SparseArray<MyCourse[]>(this.groups.size());
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
		MyCourse[] childs = this.childArrays.get(groupPosition, null);
		if(childs == null || childs.length == 0){
			final MyCourse parent = this.groups.get(groupPosition);
			if(parent == null){
				Log.d(TAG, "加载分组["+groupPosition+"]课程数据失败!");
				return 0;
			}
			if(this.dao == null){
				Log.d(TAG, "惰性初始化我的课程数据操作...");
				this.dao = new MyCourseDao(this.context, this.userId);
			}
			final List<MyCourse> list = this.dao.loadCourses(parent.getId());
			if(list != null && list.size() > 0){
				childs = list.toArray(new MyCourse[0]);
				this.childArrays.put(groupPosition, childs);
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
		Log.d(TAG, "获取分组下子节点数据...");
		final MyCourse[] courses = this.childArrays.get(groupPosition, null);
		if(courses != null && courses.length > childPosition){
			return courses[childPosition];
		}
		return null;
	}
	/*
	 * 获取分组ID。
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
	 * 判断指定的子选择项是否被选择
	 * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
	 */
	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
	/*
	 * 加载分组视图UI。
	 * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		Log.d(TAG, "加载分组["+groupPosition+"]视图UI...");
		GroupViewHolder viewHolder = null;
		if(convertView == null){
			Log.d(TAG, "新增分组["+groupPosition+"]View...");
			//加载视图
			convertView = this.inflater.inflate(R.layout.listlayout_2, parent, false);
			//初始化
			viewHolder = new GroupViewHolder(convertView);
			//缓存
			convertView.setTag(viewHolder);
		}else {
			Log.d(TAG, "重复分组["+groupPosition+"]View...");
			viewHolder = (GroupViewHolder)convertView.getTag();
		}
		//加载数据
		viewHolder.loadData(groupPosition);
		//返回视图
		return convertView;
	}
	/*
	 * 获取分组下子节点视图UI。
	 * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
		Log.d(TAG, "加载分组["+groupPosition+"]下子节点["+childPosition+"]视图UI...");
		ChildViewHolder viewHolder = null;
		if(convertView == null){
			Log.d(TAG, "新增分组["+groupPosition+"]下节点["+childPosition+"]View...");
			//加载视图
			convertView = this.inflater.inflate(R.layout.listlayout_3, parent, false);
			//初始化
			viewHolder = new ChildViewHolder(convertView);
			//缓存
			convertView.setTag(viewHolder);
		}else {
			Log.d(TAG, "重复分组["+groupPosition+"]下节点["+childPosition+"]View...");
			viewHolder = (ChildViewHolder)convertView.getTag();
		}
		//加载数据
		viewHolder.loadData(groupPosition, childPosition);
		//返回视图
		return convertView;
	}
	
	//
	private class GroupViewHolder{
		private TextView textView;
		/**
		 * 构造函数。
		 * @param converView
		 */
		public GroupViewHolder(View converView){
			this.textView = (TextView)converView.findViewById(R.id.text2);
		}
		/**
		 * 加载数据。
		 * @param pos
		 */
		public void loadData(int pos){
			final MyCourse course = (MyCourse)MyExpandableAdapter.this.getGroup(pos);
			if(course != null){
				this.textView.setText(course.getName());
			}
		}
	}
	//
	private class ChildViewHolder{
		private TextView textView;
		/**
		 * 构造函数。
		 * @param converView
		 */
		public ChildViewHolder(View converView){
			this.textView = (TextView)converView.findViewById(R.id.text3);
		}
		/**
		 * 加载数据。
		 * @param groupPos
		 * @param childPos
		 */
		public void loadData(int groupPos,int childPos){
			final MyCourse course = (MyCourse)MyExpandableAdapter.this.getChild(groupPos, childPos);
			if(course != null){
				this.textView.setText(course.getName());
			}
		}
	}
}