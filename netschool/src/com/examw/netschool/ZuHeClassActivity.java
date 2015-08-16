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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;
import com.umeng.analytics.MobclickAgent;

public class ZuHeClassActivity extends Activity implements OnClickListener{
	private ImageButton returnBtn;
	private TextView title;
	private ListView list;
	private LinearLayout nodata;
	private LinearLayout myCourseBtn,learnRecordBtn;
	private String pid;
	private String[] array,gids;
	private ProgressDialog dialog;
	private Handler handler;
	private String username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_zuhe);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.myCourseBtn = (LinearLayout) this.findViewById(R.id.MyCourse_layout_btn);
		this.learnRecordBtn = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		returnBtn.setOnClickListener(this);
		this.myCourseBtn.setOnClickListener(this);
		this.learnRecordBtn.setOnClickListener(this);
		this.title = (TextView) this.findViewById(R.id.TopTitle4);
		this.list = (ListView) this.findViewById(R.id.list);
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		dialog = ProgressDialog.show(ZuHeClassActivity.this,null,"努力加载中请稍候",true,false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		Intent intent = this.getIntent();
		this.title.setText(intent.getStringExtra("name"));
		this.pid = intent.getStringExtra("pid");
		this.username = intent.getStringExtra("username");
		new GetMyLessonThread().start();
		handler = new MyHandler(this);
		this.list.setOnItemClickListener(new ItemClickListener());
	}
	private class GetMyLessonThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				String result = HttpConnectUtil.httpGetRequest(ZuHeClassActivity.this, Constant.DOMAIN_URL+"mobile/findGradeOfPkg?pkgId="+pid);
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置expandableListView的adapter
            		try
            		{
            			JSONObject json = new JSONObject(result);
            			int ok = json.getInt("ok");
            			if(ok==1)
            			{
            				JSONArray grades = json.getJSONArray("data");
            				array = new String[grades.length()];
            				gids = new String[grades.length()];
            				for(int i=0;i<grades.length();i++)
            				{
            					JSONObject g = grades.getJSONObject(i);
            					array[i] = g.optString("name");
            					gids[i] = g.optString("gradeId");
            				}
            				handler.sendEmptyMessage(1);
            			}else
            			{
            				handler.sendEmptyMessage(0);
            			}
            			//设置adapter
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
        WeakReference<ZuHeClassActivity> mActivity;
        MyHandler(ZuHeClassActivity activity) {
                mActivity = new WeakReference<ZuHeClassActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	ZuHeClassActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
        			theActivity.dialog.dismiss();
          			theActivity.list.setAdapter(new ArrayAdapter<String>(theActivity, R.layout.listlayout_1,R.id.text1,theActivity.array));
                	break;
                case 0:
                	theActivity.dialog.dismiss();
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		break;
                case -1:
                	//连不上,
                	if(theActivity.dialog!=null)
                	{
                		theActivity.dialog.dismiss();
                	}
            		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "暂时连不上服务器,请稍候", Toast.LENGTH_SHORT).show();//提示
                }
        }
	}
	private class ItemClickListener implements OnItemClickListener
	{
		public ItemClickListener() {
			// TODO Auto-generated constructor stub
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(ZuHeClassActivity.this,ClassDetailActivity.class);
			intent.putExtra("name", ((TextView)arg1.findViewById(R.id.text1)).getText().toString());
			intent.putExtra("gid", gids[arg2]);
			intent.putExtra("username", username);
			ZuHeClassActivity.this.startActivity(intent);
		} 
	}
	
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
