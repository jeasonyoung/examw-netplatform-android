package com.examw.netschool;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

public class Class2Activity extends Activity implements OnClickListener{
	private ListView list;
	private TextView title;
	private LinearLayout nodata;
	private ProgressDialog dialog;
	private ImageButton returnBtn;
	private LinearLayout myCourseBtn,learnRecordBtn;
	private static String[] array,idArr;
	private ImageButton searchBtn;
	private EditText searchEdit;
	private String username;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_class1);
		this.list = (ListView) this.findViewById(R.id.list);
		this.title = (TextView) this.findViewById(R.id.TopTitle1);
		this.nodata = (LinearLayout) this.findViewById(R.id.nodataLayout);
		//this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		dialog = ProgressDialog.show(Class2Activity.this,null,"努力加载中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.myCourseBtn = (LinearLayout) this.findViewById(R.id.MyCourse_layout_btn);
		this.learnRecordBtn = (LinearLayout) this.findViewById(R.id.LearningRecord_layout_btn);
		this.searchBtn = (ImageButton) this.findViewById(R.id.searchImgBtn);
		this.searchEdit = (EditText) this.findViewById(R.id.serchkey);
		returnBtn.setOnClickListener(this);
		this.searchBtn.setOnClickListener(this);
		this.myCourseBtn.setOnClickListener(this);
		this.learnRecordBtn.setOnClickListener(this);
		Intent intent = this.getIntent();
		String name = intent.getStringExtra("name");
		this.username = intent.getStringExtra("username");
		this.title.setText(name);	//设置标题
		String arr = intent.getStringExtra("children");
		try {
			JSONArray json = new JSONArray(arr);
			array = new String[json.length()];
			idArr = new String[json.length()];
			for(int i=0;i<json.length();i++)
			{
				JSONObject obj = json.getJSONObject(i);
				array[i] = obj.getString("examName");
				idArr[i] = obj.getString("examId");
			}
			dialog.dismiss();
			this.list.setAdapter(new ArrayAdapter<String>(this, R.layout.listlayout_1,R.id.text1, array));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.nodata.setVisibility(View.VISIBLE);
		}
		this.list.setOnItemClickListener(new ItemClickListener(array,idArr));
	}
	private class ItemClickListener implements OnItemClickListener
	{
		private String[] array;
		private String[] idArr;
		public ItemClickListener(String[] array,String[] idArr) {
			// TODO Auto-generated constructor stub
			this.array = array;
			this.idArr = idArr;
		}
		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(Class2Activity.this,Class3Activity.class);
			intent.putExtra("name", array[arg2]);
			intent.putExtra("examId", idArr[arg2]);
			intent.putExtra("username", username);
			Log.e("Class2","username = "+username);
			Class2Activity.this.startActivity(intent);
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
		Intent intent = new Intent(Class2Activity.this,SearchActivity.class);
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