package com.examw.netschool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.examw.netschool.util.Constant;
import com.umeng.analytics.MobclickAgent;

public class SuggestionActivity extends Activity implements OnClickListener{
	private ImageButton returnbtn;
	private EditText suggestionText;
	private Button submitSugBtn;
	private String username;
	private int uid;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_suggestion);
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnbtn.setOnClickListener(this); 	
		this.suggestionText = (EditText) this.findViewById(R.id.suggestionText);
		this.submitSugBtn = (Button) this.findViewById(R.id.submitSugBtn);
		this.submitSugBtn.setOnClickListener(this);
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		this.uid = intent.getIntExtra("uid", 0);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			default:return;
			case R.id.returnbtn:
				this.finish();
				return;
			case R.id.submitSugBtn:
//				InputMethodManager localInputMethodManager = (InputMethodManager)getSystemService("input_method");
//			    if (localInputMethodManager.isActive())
//			      localInputMethodManager.toggleSoftInput(1, 2);
			    String str = this.suggestionText.getText().toString();
			    if (str.length() == 0)
			    {
			      Toast.makeText(this, "请输入内容！", Toast.LENGTH_LONG).show();
			      return;
			    }
			    new SuggestTask(str).execute(Constant.DOMAIN_URL+"mobile/addAdvice");
			    
		}
	}
	private class SuggestTask extends AsyncTask<String,Integer,String>
	{
		private String text;
		@Override
		protected String doInBackground(String... sUrl) {
			BufferedReader reader = null;
			try {
				HttpClient client = new DefaultHttpClient();
				HttpPost request = new HttpPost();
				request.setURI(new URI(sUrl[0]));
				//设置参数 用URLEncodedFormEntity  NameValuePair
				List<NameValuePair> params = new ArrayList<NameValuePair>();
				params.add(new BasicNameValuePair("appType","1"));
				params.add(new BasicNameValuePair("username",username));
				params.add(new BasicNameValuePair("content",text));
				params.add(new BasicNameValuePair("stuId",uid+""));
				UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params);
				request.setEntity(entity);
				HttpResponse response = client.execute(request); 
				reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				StringBuffer strBuffer = new StringBuffer("");
				String line = null;
				while ((line = reader.readLine()) != null) {
					strBuffer.append(line);
				}
				System.out.println(strBuffer.toString());
				return strBuffer.toString();
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			} finally {
				if (reader != null) {
					try {
						reader.close();
						reader = null;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		}
		@SuppressWarnings("deprecation")
		public SuggestTask(String t) {
			this.text = URLEncoder.encode(t);
		}
		@Override
		protected void onPostExecute(String result) {
			try
			{
				JSONObject json = new JSONObject(result);
				int ok = json.optInt("ok", 0);
				String msg = json.optString("msg");
				Toast.makeText(SuggestionActivity.this, msg, Toast.LENGTH_LONG).show();
				if(ok==1)
				{
					finish();
				}
			}catch(Exception e)
			{
				e.printStackTrace();
				Toast.makeText(SuggestionActivity.this, "提交失败,稍后再试", Toast.LENGTH_LONG).show();
			}
		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
}