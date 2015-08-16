package com.examw.netschool;

import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;
import com.umeng.analytics.MobclickAgent;

public class RegisterActivity extends Activity{
	 private ImageButton returnbtn;
	 private ProgressDialog dialog;
	 private EditText username;
	 private EditText pwd;
	 private EditText email;
	 private EditText phone;
	 private EditText qq;
	 private CheckBox checkBox;
	 private Button submitBtn;
	 private Button treatyBtn;//协议
	 private SharedPreferences abfile;
	 protected void onCreate(Bundle paramBundle)
	  {
	    super.onCreate(paramBundle);
	    setContentView(R.layout.activity_register);
	    findViewsById();
	  }
	 //初始化控�?
	 private void findViewsById()
	 {
		 returnbtn = (ImageButton) this.findViewById(R.id.returnbtn);
		 username =(EditText) this.findViewById(R.id.reg_userNameText);
		 pwd = (EditText) this.findViewById(R.id.reg_userPwd1Text);
		 email = (EditText) this.findViewById(R.id.reg_userEmail);
		 phone = (EditText) this.findViewById(R.id.reg_userPhone);
		 qq = (EditText) this.findViewById(R.id.reg_userQQ);
		 checkBox = (CheckBox) this.findViewById(R.id.reg_checkBox);
		 submitBtn = (Button) this.findViewById(R.id.reg_submitBtn);
		 treatyBtn = (Button) this.findViewById(R.id.reg_treatyBtn);
		 returnbtn.setOnClickListener(new ReturnBtnClickListener(this));
		 abfile = getSharedPreferences("abfile", 0);
		 submitBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(checkInput())
				{
					//验证输入无误
					dialog = ProgressDialog.show(RegisterActivity.this,null,"注册中请稍侯",true,true);
	        		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
					//�?��个线程去注册
//					new RegisterThread().start();
					//异步任务注册�?
	        		String params="username="+username.getText().toString()
	        					+"&pwd="+pwd.getText().toString()+
	        					"&email="+email.getText().toString()+
	        					"&phone="+phone.getText().toString()+
	        					"&qq="+qq.getText().toString();
	        		RegisterTask register = new RegisterTask();
	        		register.execute(Constant.DOMAIN_URL+"mobile/register?"+params);
				}
			}
		});
		 treatyBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(RegisterActivity.this,TreatyActivity.class);
				RegisterActivity.this.startActivity(intent);
			}
		 });
	 }
	 //验证输入
	 private boolean checkInput()
	 {
		 	String str1 = this.username.getText().toString().trim();
		    String str2 = this.email.getText().toString().trim();
		    String str3 = this.pwd.getText().toString().trim();
		    String str4 = this.phone.getText().toString().trim();
		    String qqq = this.qq.getText().toString().trim();
		    boolean bool2 = Pattern.compile("^\\d+$").matcher(str1).matches();
		    boolean bool3 = Pattern.compile(".*[\u4e00-\u9fa5]+.*").matcher(str1).matches();
		    boolean bool4 = Pattern.compile(".{0,2}$").matcher(str1).matches();
		    boolean bool5 = Pattern.compile("^[-,_]{1}.*").matcher(str1).matches();
		    boolean bool6 = Pattern.compile("^[-,_,0-9,a-z,A-Z]+$").matcher(str1).matches();
		    boolean bool = Pattern.compile("^1[3,4,5,6,8]{1}[0-9]{9}$").matcher(str4).matches();
		    boolean bool_qq = Pattern.compile("^[1-9]{1}[0-9]{4,}$").matcher(qqq).matches();
		    if (str1.equals(""))
		    {
		      showMsg("用户名不能为空");
		      return false;
		    }
		    if (bool2)
		    {
		    	showMsg("用户名不能全部为数字!");
		      return false;
		    }
		    if (bool3)
		    {
		    	showMsg("用户名不能包含汉字");
		      return false;
		    }
		    if (bool4)
		    {
		    	showMsg("用户名至3位字符");
		      return false;
		    }
		    if (bool5)
		    {
		    	showMsg("用户名不能以‘-’或‘_’开头");
		      return false;
		    }
		    if (!bool6)
		    {
		    	showMsg("用户名只能使用字母,数字,下划线'-'和'_'组成!");
		    	return false;
		    }
		    boolean bool7 = Pattern.compile("^.{6,}$").matcher(str3).matches();
		    if (str3.equals(""))
		    {
		    	showMsg("密码不能为空!");
		      	return false;
		    }
		    if (!bool7)
		    {
		    	showMsg("密码至少6位字符");
		    	return false;
		    }
		    boolean bool1 = Pattern.compile("^([a-z0-9A-Z]+[-|_\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$").matcher(str2).matches();
		    if (str2.equals(""))
		    {
		      showMsg("邮箱不能为空!");
		      return false;
		    }
		    if (!bool1)
		    {
		      showMsg("邮箱格式错误!");
		      return false;
		    }
		    if(str4.equals(""))
		    {
		    	showMsg("手机号不能为空");
		    	return false;
		    }
		    if(!bool)
		    {
		    	showMsg("请输入正确的手机号码");
		    	return false;
		    }
		    if(("".equals(qqq)))
		    {
		    	showMsg("QQ号不能为空");
		    	return false;
		    }
		    if(!bool_qq)
		    {
		    	showMsg("请输入正确的QQ号码");
		    	return false;
		    }
		    if(!checkBox.isChecked())
		    {
		    	showMsg("请仔细阅读网校协议");
		    	return false;
		    }
		 return true;
	 }
	 private void showMsg(String msg)
	 {
		 Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_SHORT).show();
	 }
	 private class RegisterTask extends AsyncTask<String,Integer,String>
	 {
		 @Override
		protected String doInBackground(String... params) {
			// TODO Auto-generated method stub
			 try{
				 return HttpConnectUtil.httpGetRequest(RegisterActivity.this, params[0]);
			 }catch(Exception e)
			 {
				 return "暂时连不上服务器";
			 }
		 }
		 @Override
		protected void onPostExecute(String result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dialog.dismiss();
			try {
				JSONObject json = new JSONObject(result);
				int ok = json.getInt("OK");
				String msg = json.optString("msg","");
				String username = json.optString("username","");
				if(ok==0)//注册失败
				{
					Toast.makeText(RegisterActivity.this, msg, Toast.LENGTH_LONG).show();
				}else
				{
					abfile.edit().putString("n",username).commit();
					abfile.edit().putString("p","").commit();
					Intent intent = new Intent(RegisterActivity.this,RegSuccessActivity.class);
					intent.putExtra("username", username);
					RegisterActivity.this.startActivity(intent);
					RegisterActivity.this.finish();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				if("暂时连不上服务器".equals(result))
				{
					Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_LONG).show();
				}
			}
			
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
