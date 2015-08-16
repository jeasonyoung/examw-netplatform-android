package com.examw.netschool;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MyExpandableAdapter extends BaseExpandableListAdapter{
	private Context context;
	private String[] groups;
	private String[][] children;
	public MyExpandableAdapter(Context context,String[] group,String[][]child) {
		this.children = child;
		this.context= context;
		this.groups = group;
	}
	 //获得指定组中的指定索引的子选项数据
	 public Object getChild(int groupPosition, int childPosition) {
		 try{
		 return children[groupPosition][childPosition];
		 }catch(Exception e)
		 {
			 return null;
		 }
	 }
	 //获得指定子项的ID
	 public long getChildId(int groupPosition, int childPosition) {
	  return childPosition;
	 }
	     //获得指定子项的view组件
	 public View getChildView(int groupPosition, int childPosition,
	   boolean isLastChild, View convertView, ViewGroup parent) {
		 	LayoutInflater inflater = LayoutInflater.from(context);
		 	convertView = inflater.inflate(R.layout.listlayout_3, null);
		 	TextView txt = (TextView) convertView.findViewById(R.id.text3);
		 	txt.setText(getChild(groupPosition,childPosition).toString());
		 	return convertView;
	 }
	    //取得指定组中所有子项的个数
	 public int getChildrenCount(int groupPosition) {
		 try{
			 return children[groupPosition].length;
		 }catch(Exception e)
		 {
			 return 0;
		 }
	 }
	     //取得指定组的数据
	 public Object getGroup(int groupPosition) {
	  return groups[groupPosition];
	 }
	  //取得指定组的个数
	 public int getGroupCount() {
	  return groups.length;
	 }
	  //取得指定索引的ID
	 public long getGroupId(int groupPosition) {
	  return groupPosition;
	 }
	  //取得指定组的View组件
	 public View getGroupView(int groupPosition, boolean isExpanded,
	   View convertView, ViewGroup parent) {
		 	LayoutInflater inflater = LayoutInflater.from(context);
		 	convertView = inflater.inflate(R.layout.listlayout_2, null);
		 	TextView txt = (TextView) convertView.findViewById(R.id.text2);
		 	txt.setText(groups[groupPosition]);
		 	return convertView;
	 }
	      //如果返回true表示子项和组的ID始终表示一个固定的组件对象
	 public boolean hasStableIds() {
	  return true;
	 }
	//判断指定的子选择项是否被选择
	 public boolean isChildSelectable(int groupPosition, int childPosition) {
	  return true;
	 }
}
