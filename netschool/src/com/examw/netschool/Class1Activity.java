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
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.app.Constant;

public class Class1Activity extends Activity implements OnClickListener{
	private ListView list;
	private LinearLayout nodata;
	private LinearLayout myCourseBtn,learnRecordBtn;
	private ProgressDialog dialog;
	private Handler handler;
	private ImageButton returnBtn,searchBtn;
	private EditText searchEdit;
	private static SparseArray<JSONArray> map;
	private static String[] array;
	private String username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_class1);
		this.list = (ListView) this.findViewById(R.id.list);
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.myCourseBtn = (LinearLayout) this.findViewById(R.id.MyCourse_layout_btn);
		this.learnRecordBtn = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		this.searchBtn = (ImageButton) this.findViewById(R.id.searchImgBtn);
		this.searchEdit = (EditText) this.findViewById(R.id.serchkey);
		this.username = this.getIntent().getStringExtra("username");
		//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog = ProgressDialog.show(Class1Activity.this,null,"努力加载中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		returnBtn.setOnClickListener(this);
		myCourseBtn.setOnClickListener(this);
		searchBtn.setOnClickListener(this);
		learnRecordBtn.setOnClickListener(this);
		this.handler = new MyHandler(this);
		new GetMyLessonThread().start();
		this.list.setOnItemClickListener(new ItemClickListener());
	}
	private class GetMyLessonThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
				String result = "";//HttpConnectUtil.httpGetRequest(Class1Activity.this, Constant.DOMAIN_URL+"mobile/examTree");
				if(result!=null&&!result.equals("null"))
            	{
            		//解析json字符串,配置ListView的adapter
            		try
            		{
            			JSONArray exams = new JSONArray(result);
            			array = new String[exams.length()];
            			map = new SparseArray<JSONArray>();
            			for(int i=0;i<exams.length();i++)
            			{
            				JSONObject obj = exams.getJSONObject(i);
            				array[i] = obj.getString("examName");
            				JSONArray ary = obj.getJSONArray("children");
            				map.put(i, ary);
            			}
            			handler.sendEmptyMessage(1);
            		}catch(Exception e)
            		{
            			e.printStackTrace();
            			handler.sendEmptyMessage(-2);
            		}
            	}else
            	{
            		handler.sendEmptyMessage(-2);
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
        WeakReference<Class1Activity> mActivity;
        MyHandler(Class1Activity activity) {
                mActivity = new WeakReference<Class1Activity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        	Class1Activity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
        			mActivity.get().list.setAdapter(new ArrayAdapter<String>(mActivity.get(), R.layout.listlayout_1,R.id.text1, array));
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
                	break;
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
			Intent intent = new Intent(Class1Activity.this,Class2Activity.class);
			intent.putExtra("username",username);
			intent.putExtra("name", ((TextView)arg1.findViewById(R.id.text1)).getText().toString());
			intent.putExtra("children", map.get(arg2).toString());
			Class1Activity.this.startActivity(intent);
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
		case R.id.searchImgBtn:
			search();
			return;
		}
	}
	private void search()
	{
		String keywords = this.searchEdit.getText().toString();
		if("".equals(keywords.trim()))
		{
			Toast.makeText(this, "请输入搜索关键字", Toast.LENGTH_SHORT).show();
			return;
		}
		Intent intent = new Intent(Class1Activity.this,SearchActivity.class);
		intent.putExtra("keywords", keywords.trim());
		startActivity(intent);
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