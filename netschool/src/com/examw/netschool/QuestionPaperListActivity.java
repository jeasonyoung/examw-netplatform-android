package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.adapter.PaperListAdapter;
import com.examw.netschool.dao.PaperDao;
import com.examw.netschool.entity.Paper;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;

public class QuestionPaperListActivity extends Activity{
	private int gid;
	private TextView title;
	private ImageButton returnBtn;
	private ListView paperList;
	private List<Paper> papers;
	private ProgressDialog dialog;
	private Handler handler;
	private LinearLayout nodata;
	private String username,loginType;
	private PaperDao dao;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_questionblank_examtier2);
		this.title = (TextView) this.findViewById(R.id.TopTitle1);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.nodata = (LinearLayout) this.findViewById(R.id.noneDataLayout);
		this.paperList = (ListView) this.findViewById(R.id.contentListView);
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		dao = new PaperDao(this);
		Intent intent = this.getIntent();
		String name = intent.getStringExtra("name");
		this.title.setText(name==null?"本地试卷列表":name);
		this.gid = intent.getIntExtra("gid", 0);
		this.username = intent.getStringExtra("username");
		this.loginType = intent.getStringExtra("loginType");
		dialog = ProgressDialog.show(QuestionPaperListActivity.this,null,"努力加载中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		handler = new MyHandler(this);
		new GetPaperListThread().start();
		this.paperList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Intent intent = new Intent(QuestionPaperListActivity.this,QuestionPaperInfoActivity.class);
				intent.putExtra("gid", String.valueOf(gid));
				intent.putExtra("paperid", papers.get(arg2).getPaperId());
				intent.putExtra("paperJson", papers.get(arg2).getJsonString());
				intent.putExtra("username", username);
				intent.putExtra("loginType", loginType);
				QuestionPaperListActivity.this.startActivity(intent);
			}
		});
	}
	private class GetPaperListThread extends Thread
	{
		@Override
		public void run() {
			if("local".equals(loginType))
			{
				papers = dao.findAllPapers(username);
				if(papers!=null&&papers.size()>0)
				{
					//发消息
					Message msg = handler.obtainMessage();
					msg.what = 1;
					handler.sendMessage(msg);
				}else
				{
					Message msg = handler.obtainMessage();
					msg.what = -3;
					handler.sendMessage(msg);
				}
				return;
			}
			try{
				String result = HttpConnectUtil.httpGetRequest(QuestionPaperListActivity.this, Constant.DOMAIN_URL+"mobile/paperList?pagesize=100&gradeId="+gid);
				//解析result
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置expandableListView的adapter
            		try
            		{
            			JSONArray json = new JSONArray(result);
            			int length = json.length();
            			if(length>0)
            			{
            				papers = new ArrayList<Paper>();
            				for(int i=0;i<length;i++)
            				{
            					JSONObject obj = json.getJSONObject(i);
            					Paper p = new Paper(obj.optString("paperId")+"",obj.optString("paperName"),obj.optInt("paperScore"),obj.optInt("paperTime"),gid+"",null);
            					p.setJsonString(obj.toString());
            					papers.add(p);
            				}
            			}
            			//发消息
            			Message msg = handler.obtainMessage();
        				msg.what = 1;
        				handler.sendMessage(msg);
            			//theActivity.expandList.setAdapter(new MyExpandableAdapter(theActivity, theActivity.group, theActivity.child));
            			//设置adapter
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            		}
            	}else
            	{
            		//发消息
        			Message msg = handler.obtainMessage();
    				msg.what = -2;
    				handler.sendMessage(msg);
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
        WeakReference<QuestionPaperListActivity> mActivity;
        MyHandler(QuestionPaperListActivity activity) {
                mActivity = new WeakReference<QuestionPaperListActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	QuestionPaperListActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
                	if(theActivity.papers!=null&&theActivity.papers.size()>0)
                	{
                    	theActivity.paperList.setAdapter(new PaperListAdapter(theActivity,theActivity.papers));
                	}else
                	{
                		theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
                	}
                			//theActivity.expandList.setAdapter(new MyExpandableAdapter(theActivity, theActivity.group, theActivity.child));
                			//设置adapter
                	break;
                case -2:
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
                case -3:
                	theActivity.dialog.dismiss();
                	theActivity.nodata.setVisibility(View.VISIBLE);//无数据显示
            		Toast.makeText(theActivity, "本地没有数据", Toast.LENGTH_SHORT).show();//提示
            		break;
                }
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