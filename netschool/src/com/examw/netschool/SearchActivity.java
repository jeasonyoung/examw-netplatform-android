package com.examw.netschool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.examw.netschool.app.Constant;

public class SearchActivity extends ListActivity {
	private ImageButton returnBtn;
	private RelativeLayout noneDataLayout;
	private TextView title;
	private String keywords;
	private ProgressDialog dialog;
	private String[] names;
	private String[] ids;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_search);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.noneDataLayout = (RelativeLayout) this.findViewById(R.id.noneDataLayout);
		this.title = (TextView) this.findViewById(R.id.TopTitle5);
		Intent intent = this.getIntent();
		this.keywords = intent.getStringExtra("keywords");
		this.title.setText("搜索词:"+this.keywords);
		dialog = ProgressDialog.show(SearchActivity.this,null,"搜索中请稍候",true,true);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		try {
			String words = URLEncoder.encode(URLEncoder.encode(keywords,"gbk"),"gbk");
			new GetDataTask().execute(Constant.DOMAIN_URL+"mobile/searchExam?keywords="+words);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		this.returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent(SearchActivity.this,Class3Activity.class);
		intent.putExtra("name", names[position]);
		intent.putExtra("examId", ids[position]);
		SearchActivity.this.startActivity(intent);
	}
	
	private class GetDataTask extends AsyncTask<String,Void,String>
	{
		@Override
		protected String doInBackground(String... params) {
			try{
				String result = "";//HttpConnectUtil.httpGetRequest(SearchActivity.this, params[0]);
				if(result == null||"null".equals(result))
				{
					return null;
				}
				//解析result
				try
				{
					JSONArray json = new JSONArray(result);
					int length = json.length();
					names = new String[length];
					ids = new String[length];
					for(int i=0;i<length;i++)
					{
						JSONObject obj = json.getJSONObject(i);
						names[i] = obj.getString("examName");
						ids[i] = obj.getInt("examId")+"";
					}
				}catch(Exception e)
				{
					e.printStackTrace();
					return null;
				}
				return "success";
			}catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		}
		@Override
		protected void onPostExecute(String result) {
			if(dialog!=null&&dialog.isShowing())
			{
				dialog.dismiss();
			}
			if(result == null)
			{
				noneDataLayout.setVisibility(View.VISIBLE);
			}else
			{
				if(names==null||names.length==0)
				{
					noneDataLayout.setVisibility(View.VISIBLE);
					return;
				}
				setListAdapter(new ArrayAdapter<String>(SearchActivity.this, R.layout.listlayout_1,R.id.text1, names));
			}
			super.onPostExecute(result);
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