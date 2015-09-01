package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.adapter.MyExpandableAdapter2;
import com.examw.netschool.entity.UserClass;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;

public class QuestionFromCourseActivity extends Activity{
	private SharedPreferences userinfo;
	private ProgressDialog dialog;
	private static String username;
	private Handler handler;
	private ExpandableListView expandList;
	private LinearLayout nodata;
	private ImageButton returnBtn;
	private String[] group;
	private String[][] child;
	private int[][] gids;
	private int id;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_mycourselist2);
		userinfo = getSharedPreferences("userinfo", 0);
		id = userinfo.getInt("id", 0);
		username = getIntent().getStringExtra("username");
		dialog = ProgressDialog.show(QuestionFromCourseActivity.this,null,"努力加载中请稍候",true,false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		expandList = (ExpandableListView) this.findViewById(R.id.explist2);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		expandList.setGroupIndicator(null); 
		expandList.setOnChildClickListener(new ChildClickListener());
		expandList.setOnGroupClickListener(new GroupClickListener());
		nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		new GetMyLessonThread().start();
		handler = new MyHandler(this);
		
	}
	private class GetMyLessonThread extends Thread
	{
		@Override
		public void run() {
			try{
				String result = HttpConnectUtil.httpGetRequest(QuestionFromCourseActivity.this, Constant.DOMAIN_URL+"mobile/myLessons?stuId="+id);
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置expandableListView的adapter
            		try
            		{
            			JSONObject json = new JSONObject(result);
            			JSONArray packages = json.optJSONArray("classPackages");
            			JSONArray grades = json.getJSONArray("grade");
            			int plength = packages.length();
            			int glength = grades.length();
            			List<UserClass> list = new ArrayList<UserClass>();
            			//大项,套餐或单班级
            			group = new String[plength+glength];
            			//子项,套餐下的班级,单班级没有子项
            			//注意子项的长度比大项的短
            			child = new String[plength][];
            			//课程详细,点击班级时跳转
            			gids = new int[plength+glength][];
            			//循环套餐( classid,classname,username,fatherid,classtype)
            			for(int i=0;i<plength;i++)
            			{
            				JSONObject p = packages.getJSONObject(i);
            				group[i]=p.optString("pkgName");
            				JSONArray p_grades = p.getJSONArray("grade");
            				child[i] = new String[p_grades.length()];
            				gids[i] = new int[p_grades.length()];
            				UserClass c = new UserClass(p.optInt("pkgId")+"",p.optString("pkgName"),username,0+"",1+"");
            				list.add(c);
            				//循环套餐下的班级
            				for(int k=0;k<p_grades.length();k++)
            				{
            					p = p_grades.getJSONObject(k);
            					child[i][k]=p.getString("name");
            					int gid = p.optInt("gradeId",0);
            					gids[i][k] = gid;
            				}
            			}
            			//循环班级
            			for(int j=0;j<glength;j++)
            			{
            				JSONObject p = grades.getJSONObject(j);
            				group[j+plength]=p.optString("name");
            				int gid = p.optInt("gradeId",0);
            				gids[plength+j] = new int[1];
        					gids[plength+j][0] = gid;
            			}
            			handler.sendEmptyMessage(1);
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            			handler.sendEmptyMessage(-2);
            		}
            	}else
            	{
            		handler.sendEmptyMessage(0);
            	}
			}catch(Exception e)
			{
				Message msg = handler.obtainMessage();
				msg.what = -1;
				handler.sendMessage(msg);
			}
		}
	}
	static class MyHandler extends Handler {
        WeakReference<QuestionFromCourseActivity> mActivity;
        MyHandler(QuestionFromCourseActivity activity) {
                mActivity = new WeakReference<QuestionFromCourseActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	QuestionFromCourseActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
        			theActivity.expandList.setAdapter(new MyExpandableAdapter2(theActivity, theActivity.group, theActivity.child));
                	break;
                case 0:
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "您没有购买课程", Toast.LENGTH_SHORT).show();//提示
                	break;
                case -1:
                	//连不上,
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "暂时连不上服务器,请稍候", Toast.LENGTH_SHORT).show();//提示
            		break;
                case -2:
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "解析数据出错", Toast.LENGTH_SHORT).show();//提示
            		break;
                }
        }
	}
	private class ChildClickListener implements OnChildClickListener
	{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) { 
			Intent intent = new Intent(QuestionFromCourseActivity.this,QuestionPaperListActivity.class);
			intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
			intent.putExtra("gid", QuestionFromCourseActivity.this.gids[groupPosition][childPosition]);
			intent.putExtra("username", username);
			QuestionFromCourseActivity.this.startActivity(intent);
			return true;
		}
	}
	private class GroupClickListener implements OnGroupClickListener
	{
		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			//如果没有子类了,表示是单独的课程
			if(parent.getExpandableListAdapter().getChildrenCount(groupPosition)==0)
			{
				Intent intent = new Intent(QuestionFromCourseActivity.this,QuestionPaperListActivity.class);
				intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
				intent.putExtra("gid", QuestionFromCourseActivity.this.gids[groupPosition][0]);
				QuestionFromCourseActivity.this.startActivity(intent);
				return true;
			}
			return false;
		}
	}
	@Override
	protected void onDestroy() {
		if(dialog!=null)
		{
			dialog.dismiss();	
		}
		super.onDestroy();
	}
}