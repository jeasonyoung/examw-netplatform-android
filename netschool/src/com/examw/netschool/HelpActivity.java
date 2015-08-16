package com.examw.netschool;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.TextView;

import com.umeng.analytics.MobclickAgent;

public class HelpActivity extends Activity{
	private ImageButton returnBtn;
	private ExpandableListView helpExpList; 
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_help);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.helpExpList = (ExpandableListView) this.findViewById(R.id.helpExpList);
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		this.helpExpList.setGroupIndicator(null);	//取消组图标
		initExpList();
		//this.helpExpList.setAdapter(new HelpAdapter());
	}
	private void initExpList()
	{
		String[] group = new String[5];
		group[0] = this.getResources().getString(R.string.helpTitleStr1);
		group[1] = this.getResources().getString(R.string.helpTitleStr2);
		group[2] = this.getResources().getString(R.string.helpTitleStr3);
		group[3] = this.getResources().getString(R.string.helpTitleStr4);
		group[4] = this.getResources().getString(R.string.helpTitleStr5);
		String[][] child = new String[5][1];
		child[0][0] = this.getResources().getString(R.string.helpChildStr1);
		child[1][0] = this.getResources().getString(R.string.helpChildStr2);
		child[2][0] = this.getResources().getString(R.string.helpChildStr3);
		child[3][0] = this.getResources().getString(R.string.helpChildStr4);
		child[4][0] = this.getResources().getString(R.string.helpChildStr5);
		this.helpExpList.setAdapter(new HelpAdapter(this,group,child));
	}
	private class HelpAdapter extends BaseExpandableListAdapter
	{
		private LayoutInflater inflater;
		private String[] g;
		private String[][]c;
		public HelpAdapter(Context context,String[] g,String[][]c) {
			// TODO Auto-generated constructor stub
			this.c = c;
			this.g = g;
			this.inflater = LayoutInflater.from(context);
		}
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			if(c!=null)
				return c[groupPosition][childPosition];
			return null;
		}
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			if(c!=null)
				return childPosition;
			return 0;
		}
		@Override
		public int getChildrenCount(int groupPosition) {
			// TODO Auto-generated method stub
			if(c!=null)
				return c[groupPosition].length;
			return 0;
		}
		@Override
		public View getChildView(int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView ==null)
			{
				convertView = inflater.inflate(R.layout.list_help_c, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.helpChildText);
			tv.setText(c[groupPosition][childPosition]);
			return convertView;
		}
		@Override
		public Object getGroup(int groupPosition) {
			// TODO Auto-generated method stub
			if(g!=null)
				return g[groupPosition];
			return null;
		}
		@Override
		public int getGroupCount() {
			// TODO Auto-generated method stub
			if(c!=null)
				return g.length;
			return 0;
		}
		@Override
		public long getGroupId(int groupPosition) {
			// TODO Auto-generated method stub
			return groupPosition;
		}
		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(convertView ==null)
			{
				convertView = inflater.inflate(R.layout.list_help_g, null);
			}
			TextView tv = (TextView) convertView.findViewById(R.id.helpTitleText);
			tv.setText(g[groupPosition]);
			return convertView;
		}
		@Override
		public boolean hasStableIds() {
			// TODO Auto-generated method stub
			return false;
		}
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			// TODO Auto-generated method stub
			return false;
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
	}
}
