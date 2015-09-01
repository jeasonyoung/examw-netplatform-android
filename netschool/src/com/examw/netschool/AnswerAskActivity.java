package com.examw.netschool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;

public class AnswerAskActivity extends Activity implements OnClickListener,OnItemSelectedListener{
	private ImageButton returnbtn;
	private Button submitBtn,submitBtn2;
	private Spinner spinner,spinner2,spinner3,spinner4;
	private EditText editView;
	private TextView listenerText,loadInfoText;
	private LinearLayout loadLayout,uiLayout,nodataLayout;
	private String username;
	private int uid;
	private int classId;
	private int examId;
	private int gradeId;
	private int questionSource;
	private String questionTitle;
	private String questionContent;
	private String questionPath;
	private String[] names = new String[4];
	private List<Data> spinnerData;
	private List<Data> spinner2Data;
	private String[] data1,data2;
	private static int maxLength = 200;
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_answerask);
		//进入时,隐藏键盘
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		Intent intent = getIntent();
		this.username = intent.getStringExtra("username");
		this.uid = intent.getIntExtra("uid", 0);
		findView();
		handler = new MyHandler(this);
		new CheckThread().start();
	}
	private void findView()
	{
		this.returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.submitBtn = (Button) this.findViewById(R.id.submitBtn);
		this.submitBtn2 = (Button) this.findViewById(R.id.submitBtn2);
		this.editView = (EditText) this.findViewById(R.id.answerTitletext);
		this.spinner = (Spinner) this.findViewById(R.id.spinner);
		this.spinner2 = (Spinner) this.findViewById(R.id.spinner2);
		this.spinner3 = (Spinner) this.findViewById(R.id.spinner3);
		this.spinner4 = (Spinner) this.findViewById(R.id.spinner4);
		this.listenerText = (TextView) this.findViewById(R.id.listenerText);
		this.loadInfoText = (TextView) this.findViewById(R.id.loadingTextView);
		this.loadLayout = (LinearLayout) this.findViewById(R.id.loadLayout);
		this.uiLayout = (LinearLayout) this.findViewById(R.id.uiLayout);
		this.loadLayout.setVisibility(View.VISIBLE);
		this.nodataLayout = (LinearLayout) this.findViewById(R.id.nodataLayout);
		this.nodataLayout.setVisibility(View.GONE);
	}
	private void initView()
	{
		String examUrl = Constant.DOMAIN_URL+"mobile/getExam?stuid="+uid;
		String gradeUrl = Constant.DOMAIN_URL+"mobile/getGrade?stuid="+uid;
		new GetDataTask().execute(new String[]{examUrl,gradeUrl});
		ArrayAdapter<String> s3Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,spinner3Data);
		s3Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner3.setAdapter(s3Adapter);
		this.spinner3.setPrompt("请选择讲数");
		ArrayAdapter<String> s4Adapter = new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,spinner4Data);
		s4Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		this.spinner4.setAdapter(s4Adapter);
		this.spinner4.setPrompt("请选择问题类型");
		this.spinner3.setOnItemSelectedListener(this);
		this.spinner4.setOnItemSelectedListener(this);
		this.editView.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				AnswerAskActivity.this.listenerText.setText("已输入: "
						+ s.length() + "/" + maxLength);
			}
		});
		//限制字数
		this.editView.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
		//设置监听
		this.returnbtn.setOnClickListener(this);
		this.submitBtn.setOnClickListener(this);
		this.submitBtn2.setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.returnbtn:
			this.finish();
			return;
		case R.id.submitBtn:
			submit();
			return;
		case R.id.submitBtn2:
			submit();
			return;
		}
	}
	private void submit()
	{
		if(this.nodataLayout.getVisibility()==View.VISIBLE)
		{
			Toast.makeText(this, "您没有购买课程", Toast.LENGTH_SHORT).show();
			return;
		}
		String content = this.editView.getText().toString();
		if("".equals(content.trim()))
		{
			Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
			return;
		}
		int length = content.length();
		if(length<20)
		{
			questionTitle = content;
		}
		else{
			questionTitle = content.substring(0,20)+"...";
		}
		//questionTitle = URLEncoder.encode(questionTitle);
		questionContent = content;
		questionPath = new StringBuffer().append(names[0]).append(" > ")
				.append(names[1]).append(" > ").append(names[2]).append(" > ")
				.append(names[3]).toString();
		//questionPath = URLEncoder.encode(questionPath);
		new SubmitTask().execute();
	}
	private static String[] spinner3Data = new String[]{"第1讲","第2讲","第3讲","第4讲","第5讲","第6讲",
		"第7讲","第8讲","第9讲","第10讲","第11讲","第12讲","第13讲","第14讲","第15讲","第16讲","第17讲","第18讲",
		"第19讲","第20讲","第21讲","第22讲","第23讲","第24讲","第25讲","第26讲","第27讲","第28讲","第29讲","第30讲"};
	private static String[] spinner4Data = new String[]{"视频讲座","历年真题","教务安排"};
	
	private String clientPost() {
		BufferedReader reader = null;
		try {
			HttpClient client = new DefaultHttpClient();
			HttpPost request = new HttpPost();
			request.setURI(new URI(Constant.DOMAIN_URL+"mobile/addQuestion"));
			//设置参数 用URLEncodedFormEntity  NameValuePair
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			//username;classId;examId;gradeId;questionSource;questionTitle;questionContent;questionPath;
			params.add(new BasicNameValuePair("username",username));
			params.add(new BasicNameValuePair("classId",String.valueOf(classId)));
			params.add(new BasicNameValuePair("examId",String.valueOf(examId)));
			params.add(new BasicNameValuePair("gradeId",String.valueOf(gradeId)));
			params.add(new BasicNameValuePair("questionSource",String.valueOf(questionSource)));
			params.add(new BasicNameValuePair("questionTitle",questionTitle));
			params.add(new BasicNameValuePair("questionContent",questionContent));
			params.add(new BasicNameValuePair("questionPath",questionPath));
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params,"utf-8"); //注意要设置编码
			request.setEntity(entity);
			HttpResponse response = client.execute(request); 
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));

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
	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		switch(arg0.getId())
		{
		case R.id.spinner:
			this.examId = spinnerData.get(arg2).id;
			this.names[0] = spinnerData.get(arg2).name;
			break;
		case R.id.spinner2:
			this.gradeId = spinner2Data.get(arg2).id;
			this.names[1] = spinner2Data.get(arg2).name;
			break;
		case R.id.spinner3:
			this.classId = arg2+1;
			this.names[2] = spinner3Data[arg2];
			break;
		case R.id.spinner4:
			this.questionSource = arg2+2;
			this.names[3] = spinner4Data[arg2];
			break;
		}
	}
	 private class GetDataTask extends AsyncTask<String, Void, String[]> {
	        @Override
	        protected String[] doInBackground(String... params) {
	            // Simulates a background job.
	            try {
	            	String[] result = new String[2];
	            	result[0] = HttpConnectUtil.httpGetRequest(AnswerAskActivity.this, params[0]);
	            	result[1] = HttpConnectUtil.httpGetRequest(AnswerAskActivity.this, params[1]);
	            	return result;
	            } catch (Exception e) {
	                e.printStackTrace();
	                return null;
	            }
	        }

	        @Override
	        protected void onPostExecute(String[] result) {
	            //解析json
	        	spinnerData = parseJson(result[0],0);
	        	spinner2Data = parseJson(result[1],1);
	        	ArrayAdapter<String> sAdapter = new ArrayAdapter<String>(AnswerAskActivity.this,android.R.layout.simple_spinner_item,data1);
	    		sAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		AnswerAskActivity.this.spinner.setAdapter(sAdapter);
	    		AnswerAskActivity.this.spinner.setPrompt("请选择考试");
	    		ArrayAdapter<String> s2Adapter = new ArrayAdapter<String>(AnswerAskActivity.this,android.R.layout.simple_spinner_item,data2);
	    		s2Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    		AnswerAskActivity.this.spinner2.setAdapter(s2Adapter);
	    		AnswerAskActivity.this.spinner2.setPrompt("请选择班级");
	    		AnswerAskActivity.this.loadLayout.setVisibility(View.GONE);
	    		AnswerAskActivity.this.spinner.setOnItemSelectedListener(AnswerAskActivity.this);
	    		AnswerAskActivity.this.spinner2.setOnItemSelectedListener(AnswerAskActivity.this);
	            super.onPostExecute(result);
	        }
	        private List<Data> parseJson(String result,int flag)
	        {
	        	JSONArray examJson;
				try {
					examJson = new JSONArray(result);
					int eLength = examJson.length();
					List<Data> list = new ArrayList<Data>();
					if(flag == 0)
					{
						data1 = new String[eLength];
					}else
					{
						data2 = new String[eLength];
					}
					for(int i=0;i<eLength;i++)
					{
						JSONObject obj = examJson.getJSONObject(i);
						Data d = null;
						if(flag ==0)
						{
							d = new Data(obj.getString("examName"),obj.getInt("examId"));
							data1[i] = obj.getString("examName");
						}else
						{
							d = new Data(obj.getString("name"),obj.getInt("gradeId"));
							data2[i] = obj.getString("name");
						}
						list.add(d);
					}
					return list;
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
	        }
	 }
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	class Data
	{
		String name;
		int id;
		public Data() {
			// TODO Auto-generated constructor stub
		}
		public Data(String name,int id) {
			// TODO Auto-generated constructor stub
			this.name = name;
			this.id = id;
		}
	}
	private class SubmitTask extends AsyncTask<String, Void, String> {
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			uiLayout.setVisibility(View.GONE);
			loadLayout.setVisibility(View.VISIBLE);
			loadInfoText.setText("问题提交中");
			super.onPreExecute();
		}
		@Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			//return clientPost();
				try {
					clientPost();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return "true";
		}
		@Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			if("true".equals(result))
			{
				Toast.makeText(AnswerAskActivity.this, "提交成功", Toast.LENGTH_SHORT).show();
				finish();
			}else
			{
				Toast.makeText(AnswerAskActivity.this, "提交失败,稍后再试", Toast.LENGTH_SHORT).show();
			}
			AnswerAskActivity.this.finish();
			super.onPostExecute(result);
		}
	}
	private class CheckThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try{
			String result = HttpConnectUtil.httpGetRequest(
					AnswerAskActivity.this,
					Constant.DOMAIN_URL+"mobile/myLessons?stuId="
							+ uid);
			if(result!=null&&!result.equals("null"))
			{
				handler.sendEmptyMessage(1);
			}else
			{
				handler.sendEmptyMessage(-1);
			}
			}catch(Exception e)
			{
				handler.sendEmptyMessage(-2);
			}
		}
	}
	static class MyHandler extends Handler
	{
		WeakReference<AnswerAskActivity> mActivity;

		MyHandler(AnswerAskActivity activity) {
			mActivity = new WeakReference<AnswerAskActivity>(activity);
		}
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			AnswerAskActivity theActivity = mActivity.get();
			switch(msg.what)
			{
			case 1:
				theActivity.initView();
				break;
			case -1:
				theActivity.loadLayout.setVisibility(View.GONE);
				theActivity.nodataLayout.setVisibility(View.VISIBLE);
				break;
			case -2:
				theActivity.loadLayout.setVisibility(View.GONE);
				theActivity.nodataLayout.setVisibility(View.VISIBLE);
				Toast.makeText(theActivity, "无法连接", Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
}