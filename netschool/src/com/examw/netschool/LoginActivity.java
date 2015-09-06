package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.AppContext.LoginState;
import com.examw.netschool.app.Constant;
import com.examw.netschool.codec.binary.Base64;
import com.examw.netschool.codec.digest.DigestUtils;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * 登录Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class LoginActivity extends Activity implements TextWatcher, OnClickListener {
	private static final String TAG = "LoginActivity";
	
	private long touchTime = 0, waitTime = 2000;//按两次退出程序等待时间2s
	
	private AutoCompleteTextView usernameText;
	private EditText pwdText;
	
	private ImageButton loginBtnOnline,loginBtnLocal;
	private ProgressDialog o;
	private CheckBox rememeberCheck;
	private Handler handler;
	
	private String username, password;
	
	private ArrayAdapter<String> unAdapter;
	private List<String> unDataList;
	
	private SharedPreferences userPwdSharedPreferences, currentUserPreferences, userSharedPreferences;
	
	private AppContext appContext;
	/*
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "初始化...");
		
		this.setContentView(R.layout.activity_login);
		
		this.appContext = (AppContext)this.getApplication();
		this.handler = new MyHandler(this);
		
		this.usernameText = (AutoCompleteTextView)this.findViewById(R.id.usernameText);// 用户名
		this.unDataList = new ArrayList<String>();
		this.unAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.unDataList);
		this.usernameText.setAdapter(this.unAdapter);
		this.usernameText.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { usernameText.requestFocus(); return true; }
		});
		this.usernameText.addTextChangedListener(this);
		
		this.pwdText = (EditText) this.findViewById(R.id.pwdText);// 密码
		
		this.loginBtnOnline = (ImageButton) this.findViewById(R.id.login1Btn);//在线登录
		this.loginBtnOnline.setOnClickListener(this);
		
		this.loginBtnLocal = (ImageButton) this.findViewById(R.id.login2Btn);//本地登录
		this.loginBtnLocal.setOnClickListener(this);
		
		this.rememeberCheck = (CheckBox) this.findViewById(R.id.rememeberCheck);// 记住密码
		
		//用户密码存储
		this.userPwdSharedPreferences = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_USERPWD, Context.MODE_PRIVATE);
		//当前用户存储
		this.currentUserPreferences = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_CURRENT_USER, Context.MODE_PRIVATE);
		//共享用户名
		this.userSharedPreferences = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_SHARE_USER, Context.MODE_PRIVATE);
	}
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		new AsyncTask<Void, Void, String[]>(){
			@Override
			protected String[] doInBackground(Void... params) {
				try {
					Log.d(TAG, "异步线程加载数据...");
					return userSharedPreferences.getAll().keySet().toArray(new String[0]);
				} catch (Exception e) { 
					Log.e(TAG, "异步线程加载数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(String [] result) {
				if(result != null && result.length > 0){
					//添加数据
					for(String key : result){
						if(StringUtils.isBlank(key)) continue;
						unDataList.add(key);
					}
					//通知适配器更新
					unAdapter.notifyDataSetChanged(); 
				}
			};
			
		}.execute((Void)null);
		//
		super.onStart();
	}
	
	/*
	 * 按钮点击事件。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件...");
		switch(v.getId()){
			case R.id.login1Btn:{//在线登录
				Log.d(TAG, "在线登录...");
				this.loginOnline();
				break;
			}
			case R.id.login2Btn:{//本地登录
				Log.d(TAG, "本地登录...");
				this.loginLocal();
				break;
			}
		}
	}
	//在线登录
	private void loginOnline(){
		Log.d(TAG, "在线登录...");
		if(this.checkInput()){//
			this.username = StringUtils.trimToEmpty(this.usernameText.getText().toString());
			this.password = StringUtils.trimToEmpty(this.pwdText.getText().toString());
			Log.d(TAG, "username => " + this.username + ", password => " + password);
			//检查网络
			if(this.appContext.isNetworkConnected()){
				// 提示正在登录
				if(o == null){
					o = ProgressDialog.show(LoginActivity.this, null, "登录中请稍候", true, true);
					o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
				}
				o.show();
				//
				if(this.appContext.getLoginState() == LoginState.LOGINING) return;
				//异步线程登录
				AppContext.pools_single.execute(new Runnable() {
					@Override
					public void run() {
						try {
							Log.d(TAG, "异步线程在线登录...");
							//
							appContext.setLoginState(LoginState.LOGINING);
							//
							final String url = Constant.DOMAIN_URL + "/api/m/login.do";
							final String pwd = DigestUtils.md5Hex(DigestUtils.md5Hex(Constant.DOMAIN_AGENCY_ID + username) + password);
							//参数
							final Map<String, String> parameters = new HashMap<String, String>();
							parameters.put("agencyId", Constant.DOMAIN_AGENCY_ID);
							parameters.put("username", username);
							parameters.put("pwd", pwd);
							
							final String result = DigestClientUtil.sendDigestPOSTRequest(url, parameters);
							Log.d(TAG, "登录反馈:" + result);
							if (StringUtils.isNotBlank(result)) {
								try {
									// 解析字符串
									final Gson gson = new Gson();
									final Type type = new TypeToken<JSONCallback<String>>(){}.getType();
									final JSONCallback<String> callback = gson.fromJson(result, type);
									 //
									if (callback.getSuccess()) { // 登录成功
										final String userId = callback.getData();
										Log.d(TAG, "userId => " + userId);
										
										//记住用户
										if (isRememberMe()) {
											userSharedPreferences.edit().putString(username, Base64.encodeBase64String(password.getBytes())).commit();
										}
										//异步保存用户信息
										asyncSaveUserToLocal(userId, username, password);
										//保存到上下文
										appContext.setCurrentUserId(userId);
										appContext.setLoginState(LoginState.LOGINED);
										//
										final Message msg = handler.obtainMessage();
										msg.what = 1;
										final Bundle data = new Bundle();
										data.putString(Constant.CONST_USERID, userId);
										data.putString(Constant.CONST_USERNAME, username);
										msg.setData(data);
										handler.sendMessage(msg);//登录成功
										
									}else {
										handler.sendEmptyMessage(-3); //用户名密码错误
										appContext.setLoginState(LoginState.FAIL);
									}
								} catch (Exception e) {
									handler.sendEmptyMessage(-2);
									appContext.setLoginState(LoginState.FAIL);
								}
							} else {
								handler.sendEmptyMessage(-2);
								appContext.setLoginState(LoginState.FAIL);
							}
						} catch (Exception e) {
							handler.sendEmptyMessage(-1); // 连接有误
							appContext.setLoginState(LoginState.FAIL);
							Log.e(TAG, "在线登录异常:" + e.getMessage(), e);
						}
					}
				});
			}else {
				//本地登录
				this.loginLocal();
			}
		}
	}
	//异步保存用户信息到本地
	private void asyncSaveUserToLocal(final String userId, final String username,final String password){
		Log.d(TAG, "准备异步线程保存用户信息数据到本地...");
		AppContext.pools_fixed.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "异步线程保存用户数据...");
					//
					if(userPwdSharedPreferences != null && StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)){
						//记住用户密码
						userPwdSharedPreferences.edit()
																	 .putString(Constant.PREFERENCES_CONFIG_USERPWD_USERID + DigestUtils.md5Hex(username), userId)
																	 .putString(username, DigestUtils.md5Hex(username + password))
																	 .commit();
					}
					//
					if(currentUserPreferences != null && StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(username)){
						//记住当前用户ID
						currentUserPreferences.edit()
															  .putString(Constant.PREFERENCES_CONFIG_CURRENT_USER_ID, userId)
															  .putString(Constant.PREFERENCES_CONFIG_CURRENT_USER_NAME, username)
															  .commit();
					}
				} catch (Exception e) {
					Log.e(TAG, "异步保存用户异常:" + e.getMessage(), e);
				}
			}
		});
	}
	//本地登录
	private void loginLocal(){
		Log.d(TAG, "本地登录...");
		if(this.checkInput() && this.userPwdSharedPreferences != null){
			this.username = StringUtils.trimToEmpty(this.usernameText.getText().toString());
			this.password = StringUtils.trimToEmpty(this.pwdText.getText().toString());
			Log.d(TAG, "username => " + this.username + ", password => " + password);
			//
			if(!this.userPwdSharedPreferences.contains(this.username)){
				Log.d(TAG, "用户名未本地存储!");
				Toast.makeText(LoginActivity.this, "请先在线登录", Toast.LENGTH_SHORT).show();
				return;
			}
			//enpwd
			final String enpwd = this.userPwdSharedPreferences.getString(this.username, "");
			if(!StringUtils.equalsIgnoreCase(enpwd, DigestUtils.md5Hex(this.username + this.password))){
				Log.d(TAG, "本地登录密码验证失败!" + this.password);
				Toast.makeText(LoginActivity.this, "请先在线登录", Toast.LENGTH_SHORT).show();
				return;
			}
			//userId
			final String key = Constant.PREFERENCES_CONFIG_USERPWD_USERID + DigestUtils.md5Hex(this.username);
			final String userId = this.userPwdSharedPreferences.getString(key, "");
			if(StringUtils.isBlank(userId)){
				Log.d(TAG, "本地登录获取用户ID失败!" + key);
				Toast.makeText(LoginActivity.this, "请先在线登录", Toast.LENGTH_SHORT).show();
				return;
			}
			//
			this.appContext.setLoginState(LoginState.LOCAL);
			this.appContext.setCurrentUserId(userId);
			Log.d(TAG, "本地登录成功!");
			
			final Message msg = handler.obtainMessage();
			msg.what = 1;
			final Bundle data = new Bundle();
			data.putString(Constant.CONST_USERID, userId);
			data.putString(Constant.CONST_USERNAME, username);
			msg.setData(data);
			handler.sendMessage(msg);//登录成功
		}
	}
	/** 记住密码选项是否勾选 */
	private boolean isRememberMe() {
		return this.rememeberCheck.isChecked();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		this.getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}
	// 检查输入框的内容
	private boolean checkInput() {
		Log.d(TAG, "检查登录输入框的内容...");
		//用户名
		final String username =  StringUtils.trimToNull(usernameText.getText().toString());
		Log.d(TAG, "username => " + username);
		//密码
		final String password = StringUtils.trimToNull(pwdText.getText().toString());
		Log.d(TAG, "password => " + password);
		//验证
		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	/*
	 * 初始化输入框。
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if(this.userSharedPreferences != null){
			final Map<String, ?> usersMap = this.userSharedPreferences.getAll();
			if(usersMap != null && usersMap.size() > 0){
				 final String[] userArrays = usersMap.keySet().toArray(new String[0]);
				 if(userArrays != null && userArrays.length > 0){
					 final String username = userArrays[userArrays.length - 1];
					 if(StringUtils.isNotBlank(username)){
						 this.usernameText.setText(username);
						 final String enpwd = this.userSharedPreferences.getString(username, null);
						 if(StringUtils.isNotBlank(enpwd)){
							 this.pwdText.setText(new String(Base64.decodeBase64(enpwd)));
							 this.pwdText.requestFocus();
						 }
					 }
				 }
			}
		}
	}
	/*
	 *
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - touchTime) >= waitTime) {
				Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
				touchTime = currentTime;
			} else {
				this.finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/*
	 * 
	 * @see android.text.TextWatcher#afterTextChanged(android.text.Editable)
	 */
	@Override
	public void afterTextChanged(Editable s) {
		final String name = this.usernameText.getText().toString();
		if(StringUtils.isNotBlank(name) && this.userSharedPreferences != null){
			 final String enpwd = this.userSharedPreferences.getString(username, null);
			 if(StringUtils.isNotBlank(enpwd)){
				 this.pwdText.setText(new String(Base64.decodeBase64(enpwd)));
				 this.pwdText.requestFocus();
			 }
		}
	}
	/*
	 *
	 * @see android.text.TextWatcher#beforeTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
	/*
	 * 
	 * @see android.text.TextWatcher#onTextChanged(java.lang.CharSequence, int, int, int)
	 */
	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }
	/*
	 * 
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onDestroy();
	}
	/*
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if(o != null) o.dismiss(); 
		super.onDestroy();
	}
	
	/**
	 * 
	 * 
	 * @author jeasonyoung
	 * @since 2015年9月1日
	 */
 	private static class MyHandler extends Handler {
		private final WeakReference<LoginActivity> mActivity;
		/**
		 * 构造函数。
		 * @param activity
		 */
		public MyHandler(LoginActivity activity) {
			this.mActivity = new WeakReference<LoginActivity>(activity);
		}
		/**
		 * 消息处理。
		 */
		public void handleMessage(Message msg) {
			final LoginActivity login = mActivity.get();
			if (login.o != null) login.o.dismiss(); 
			Log.d(TAG, "消息处理...");
			switch(msg.what)
			{
				case 1:{
					Log.d(TAG, "登录成功...");
					//登录成功
					final Bundle data = msg.getData();
					
					final Intent intent = new Intent(login, MainActivity.class);
					intent.putExtra(Constant.CONST_USERID,data.getString(Constant.CONST_USERID));
					intent.putExtra(Constant.CONST_USERNAME, data.getString(Constant.CONST_USERNAME));
					
					login.startActivity(intent);
					login.finish();
					
					break;
				}
				case -1:{
					Log.d(TAG, "连接不到服务器...");
					Toast.makeText(login, "连接不到服务器", Toast.LENGTH_SHORT).show();
					break;
				}
				case -2:{
					Log.d(TAG, "连接错误...");
					Toast.makeText(login, "连接错误", Toast.LENGTH_SHORT).show();
					break;
				}
				case -3:{
					Log.d(TAG, "用户名或密码错误...");
					Toast.makeText(login, "用户名或密码错误", Toast.LENGTH_SHORT).show();
					break;
				}
			}
		}
	}
}