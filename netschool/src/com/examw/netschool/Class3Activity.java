package com.examw.netschool;

import java.lang.ref.WeakReference;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.adapter.MyExpandableAdapter2;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;

public class Class3Activity extends Activity implements OnClickListener{
	private TextView title;
	private ExpandableListView expandlist;
	private ImageButton returnBtn;
	private ProgressDialog dialog;
	private Handler handler;
	private LinearLayout nodata;
	private LinearLayout myCourseBtn,learnRecordBtn;
	private String examId;
	private String[] group,flag;
	private String[][] child;
	private String[][] urls;
	private String username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_bjlist);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.title = (TextView) this.findViewById(R.id.TopTitle2);
		Intent intent = getIntent();
		this.examId = intent.getStringExtra("examId");
		this.title.setText(intent.getStringExtra("name"));
		this.username = intent.getStringExtra("username");
		this.myCourseBtn = (LinearLayout) this.findViewById(R.id.MyCourse_layout_btn);
		this.learnRecordBtn = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		returnBtn.setOnClickListener(this);
		this.myCourseBtn.setOnClickListener(this);
		this.learnRecordBtn.setOnClickListener(this);
		dialog = ProgressDialog.show(Class3Activity.this,null,"努力加载中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.expandlist = (ExpandableListView) this.findViewById(R.id.expandlist);
		expandlist.setGroupIndicator(null); 
		expandlist.setOnChildClickListener(new ChildClickListener());
		nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		new GetMyLessonThread().start();
		handler = new MyHandler(this);
	}
	private class GetMyLessonThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				System.out.println(Constant.DOMAIN_URL+"mobile/classGroup?examId="+examId);
				String result = HttpConnectUtil.httpGetRequest(Class3Activity.this, Constant.DOMAIN_URL+"mobile/classGroup?examId="+examId);
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置expandableListView的adapter
            		try
            		{
            			JSONArray json = new JSONArray(result);
            			int plength = json.length();
            			group = new String[plength];
            			flag = new String[plength];
            			child = new String[plength][];
            			urls = new String[plength][];
            			for(int i=0;i<plength;i++)
            			{
            				JSONObject p = json.getJSONObject(i);
            				group[i]=p.optString("name");
            				JSONArray p_grades = p.getJSONArray("obj");
            				flag[i] = p.optInt("complex",0)+"";
            				child[i] = new String[p_grades.length()];
            				urls[i] = new String[p_grades.length()];
            				//循环分类下的班级
            				for(int k=0;k<p_grades.length();k++)
            				{
            					p = p_grades.getJSONObject(k);
            					//child[i][k]=p.getString("name"); 2014.02.17修改
            					if("1".equals(flag[i]))	//套餐
            					{
            						urls[i][k] = p.optString("pkgId");
            						child[i][k]=p.getString("pkgName");	//2014.02.17增加
            					}else{
            						urls[i][k] = p.optString("gradeId");
            						child[i][k]=p.getString("name");//2014.02.17增加
            					}
            				}
            			}
            			if(group.length==0)
            			{
							handler.sendEmptyMessage(0);
            			}else
							handler.sendEmptyMessage(1);
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            			handler.sendEmptyMessage(0);
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
        WeakReference<Class3Activity> mActivity;
        MyHandler(Class3Activity activity) {
                mActivity = new WeakReference<Class3Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	Class3Activity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
                    theActivity.expandlist.setAdapter(new MyExpandableAdapter2(theActivity, theActivity.group, theActivity.child));
                			//设置adapter
                	break;
                case 0:
                	theActivity.dialog.dismiss();
                	theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
                	break;
                case -1:
                	//连不上,
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "暂时连不上服务器,请稍候", Toast.LENGTH_SHORT).show();//提示
            		break;
                }
        }
	}
	private class ChildClickListener implements OnChildClickListener
	{
		@Override
		public boolean onChildClick(ExpandableListView parent, View v,
				int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			if(flag[groupPosition].equals("1"))
			{
				Intent intent = new Intent(Class3Activity.this,ZuHeClassActivity.class);
				intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
				intent.putExtra("pid", Class3Activity.this.urls[groupPosition][childPosition]);
				intent.putExtra("username", username);
				Class3Activity.this.startActivity(intent);
			}else
			{
				Intent intent = new Intent(Class3Activity.this,ClassDetailActivity.class);
				intent.putExtra("name",((TextView)v.findViewById(R.id.text3)).getText().toString());
				intent.putExtra("gid", Class3Activity.this.urls[groupPosition][childPosition]);
				intent.putExtra("username", username);
				Class3Activity.this.startActivity(intent);
			}
			return true;
		}
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		default:return;
		case R.id.returnbtn:
			this.finish();return;
		case R.id.LearningRecord_layout_btn:
			Toast.makeText(this, "免费体验不提供该功能", Toast.LENGTH_SHORT).show();
			return;
		case R.id.MyCourse_layout_btn:
			Intent intent = new Intent(this,MyCourseActivity.class);
			intent.putExtra("username", username);
			this.startActivity(intent);
			return;
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null)
		{
			dialog.dismiss();
		}
		super.onDestroy();
	}
}