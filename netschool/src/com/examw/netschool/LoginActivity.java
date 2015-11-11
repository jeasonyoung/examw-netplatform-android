package com.examw.netschool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.codec.binary.Base64;
import com.examw.netschool.codec.digest.DigestUtils;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.model.LoginResult;
import com.examw.netschool.util.APIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

/**
 * 登录Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月2日
 */
public class LoginActivity extends Activity implements OnClickListener {
	private static final String TAG = "LoginActivity";
	private static final long waitTime = 2000;//按两次退出程序等待时间2s
	
	private long touchTime = 0;
	
	private AutoCompleteTextView txtUsername;
	private EditText txtPassword;
	private CheckBox chkRememeber;
	private ProgressDialog progressDialog;
	
	private final List<String> usernames;
	private ArrayAdapter<String> adapter;
	private String userName, userPassword;
	/**
	 * 构造函数。
	 */
	public LoginActivity(){
		Log.d(TAG, "初始化...");
		this.usernames = new ArrayList<String>();
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//设置布局文件
		this.setContentView(R.layout.activity_login);
		//用户名
		this.txtUsername = (AutoCompleteTextView)this.findViewById(R.id.txt_username);
		//初始化数据适配器
		this.adapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, this.usernames);
		//设置数据适配器
		this.txtUsername.setAdapter(this.adapter);
		//设置编辑监听事件处理
		this.txtUsername.setOnEditorActionListener(this.onEditorActionListener);
		//添加输入变更事件处理
		this.txtUsername.addTextChangedListener(this.textWatcher);
		
		//密码
		this.txtPassword = (EditText)this.findViewById(R.id.txt_password);
		
		//记住密码
		this.chkRememeber = (CheckBox)this.findViewById(R.id.chk_rememeber);
		
		//在线登录
		final View btnOnlineLogin = this.findViewById(R.id.btn_login_online);
		btnOnlineLogin.setOnClickListener(this);
		//离线登录
		final View btnOfflineLogin = this.findViewById(R.id.btn_login_offline);
		btnOfflineLogin.setOnClickListener(this);
		//
		super.onCreate(savedInstanceState);
	}
	//用户名编辑监听事件处理。
	private OnEditorActionListener onEditorActionListener = new OnEditorActionListener() {
		
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if(txtUsername != null){
				txtUsername.requestFocus();
				return true;
			}
			return false;
		}
	};
	//用户名输入变更事件处理。
	private TextWatcher textWatcher = new TextWatcher() {
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) { }
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
		
		@Override
		public void afterTextChanged(Editable s) {
			try{
				final String username = s.toString();
				if(StringUtils.isNotBlank(username) && txtPassword != null){
					Log.d(TAG, "用户名:" + username);
					//初始化
					final SharedPreferences userShared = getSharedPreferences(Constant.PREFERENCES_CONFIG_SHARE_USER, Context.MODE_PRIVATE);
					//是否存在用户
					if(userShared.contains(username)){
						//获取用户密码
						final String enpwd = userShared.getString(username, null);
						if(StringUtils.isNotBlank(enpwd)){
							//设置密码
							txtPassword.setText(new String(Base64.decodeBase64(enpwd)));
							txtPassword.requestFocus();
						}
					}
				}
			}catch(Exception e){
				Log.e(TAG, "自动添加用户密码异常:" + e.getMessage(), e);
			}
		}
	};
	/*
	 * 重载开始。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载开始...");
		//异步线程加载用户名
		new AsyncTask<Void, Void, List<String>>(){

			@Override
			protected List<String> doInBackground(Void... params) {
				try {
					Log.d(TAG, "异步线程加载账号数据...");
					//初始化
					final SharedPreferences userShared = getSharedPreferences(Constant.PREFERENCES_CONFIG_SHARE_USER, Context.MODE_PRIVATE);
					if(userShared != null){
						final Map<String, ?> users =  userShared.getAll();
						if(users != null && users.size() > 0){
							return Arrays.asList(users.keySet().toArray(new String[0]));
						}
					}
				} catch (Exception e) {
					Log.e(TAG, "异步加载账号数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			
			@Override
			protected void onPostExecute(java.util.List<String> result) {
				//清空原有数据
				usernames.clear();
				//添加新数据
				if(result != null && result.size() > 0){
					Log.d(TAG, "添加新的用户集合...");
					usernames.addAll(result);
				}
				//通知数据适配器更新
				adapter.notifyDataSetChanged();
			};
			
		}.execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 按键事件处理。
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {	
		if (event.getAction() == KeyEvent.ACTION_DOWN && KeyEvent.KEYCODE_BACK == keyCode) {
			long currentTime = System.currentTimeMillis();
			if ((currentTime - this.touchTime) >= waitTime) {
				Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
				this.touchTime = currentTime;
			} else {
				this.finish();
				System.exit(0);
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	/*
	 * 按钮点击事件。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件...");
		//初始化登录等待
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, this.getResources().getText(R.string.login_progress), true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//实现等待
		this.progressDialog.show();
		//
		switch(v.getId()){
			case R.id.btn_login_online:{//在线登录
				Log.d(TAG, "在线登录...");
				this.onlineLogin();
				break;
			}
			case R.id.btn_login_offline:{//本地登录
				Log.d(TAG, "本地登录...");
				this.offlineLogin();
				break;
			}
		}
	}
	// 检查输入框的内容
	private boolean checkInput() {
		Log.d(TAG, "检查登录输入框的内容...");
		//用户名
		this.userName = this.txtUsername.getText().toString();
		if(StringUtils.isBlank(this.userName)){
			Toast.makeText(this, this.getResources().getText(R.string.login_username_valid_msg), Toast.LENGTH_SHORT).show();
			return false;
		}
		//密码
		this.userPassword = this.txtPassword.getText().toString();
		if(StringUtils.isBlank(this.userPassword)){
			Toast.makeText(this, this.getResources().getText(R.string.login_password_valid_msg), Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}
	//在线登录
	private void onlineLogin(){
		//检查用户名/密码
		if(!this.checkInput()){
			this.progressDialog.dismiss();
			return;
		}
		//初始化
		final AppContext appContext = (AppContext)getApplicationContext();
		//异步线程处理登录
		new AsyncTask<String, Void, String>() {
			private String _userId,_username, _password;
			/*
			 * 后台异步线程网络登录处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected String doInBackground(String... params) {
				try{
					Log.d(TAG, "开始在线登录...");
					//获取参数
					_username = params[0];
					_password = params[1];
					//检查网络
					if(!appContext.isNetworkConnected()){
						Log.e(TAG, "网络不可用!");
						return getResources().getText(R.string.login_fail_net).toString();
					}
					//参数处理
					final String pwd = DigestUtils.md5Hex( _username + _password);
					final Map<String, Object> parameters = new HashMap<String, Object>();
					parameters.put("username", _username);
					parameters.put("pwd", pwd);
					parameters.put("terminal", Constant.TERMINAL);
					//验证用户
					final JSONCallback<LoginResult> callback = new APIUtils.CallbackJSON<LoginResult>().sendPOSTRequest(getResources(), 
							R.string.api_user_login_url, parameters);
//					if(callback == null){
//						Log.e(TAG, "服务器无响应!");
//						return getResources().getText(R.string.login_fail_server).toString();
//					}
					if(callback.getSuccess()){
						//获取用户ID
						_userId = callback.getData().rand_user_id; 
						//返回
						return null;
					}
					Log.e(TAG, callback.getSuccess() + "/" + callback.getMsg());
					
					return callback.getMsg();
				}catch(Exception e){
					Log.e(TAG, "在线登录异常:" + e.getMessage(), e);
					return e.getMessage();
				}
			}
			/*
			 * 前台主线程更新处理。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(String result) {
				Log.d(TAG, "在线登录验证完成...");
				//清除等待动画
				progressDialog.dismiss();
				//判断登录结果
				if(StringUtils.isNotBlank(result)){
					//显示失败信息
					Toast.makeText(appContext, result, Toast.LENGTH_SHORT).show();
				}else{
					//设置当前用户ID
					appContext.setCurrentUserId(_userId);
					//记住用户
					rememberUserPassword(_username, _password);
					//保存用户到本地
					saveUserToLocal(_userId, _username,  _password);
					//跳转
					gotoMain(_userId, _username);
				}
			}
		}.execute(this.userName, this.userPassword);
	}
	//本地登录
	private void offlineLogin(){
		//检查用户名/密码
		if(!this.checkInput()){
			this.progressDialog.dismiss();
			return;
		}
		//初始化
		final AppContext appContext = (AppContext)getApplicationContext();
		//异步线程处理登录
		new AsyncTask<String, Void, String>() {
			private String  _userId,_username, _password;
			/*
			 * 后台异步线程本地登录处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected String doInBackground(String... params) {
				try{
					Log.d(TAG, "开始本地登录...");
					//获取参数
					_username = params[0];
					_password = params[1];
					//初始化用户密码储存
					final SharedPreferences userPwdShared = getSharedPreferences(Constant.PREFERENCES_CONFIG_USERPWD, Context.MODE_PRIVATE);
					//检查用户名是否存在
					if(userPwdShared == null || !userPwdShared.contains(_username)){
						Log.e(TAG, "用户名["+_username+"]未在本地存储须在线登录!");
						return getResources().getText(R.string.login_fail_local_none).toString();
					}
					//校验密码
					final String enpwd = userPwdShared.getString(_username, "");
					if(!StringUtils.equalsIgnoreCase(enpwd, DigestUtils.md5Hex(_username + _password))){
						Log.d(TAG, "本地登录密码验证失败!" + _password);
						return getResources().getText(R.string.login_fail_local_pwd).toString();
					}
					//获取用户ID
					_userId = userPwdShared.getString(Constant.PREFERENCES_CONFIG_USERPWD_USERID + DigestUtils.md5Hex(_username), "");
					if(StringUtils.isBlank(_userId)){
						Log.e(TAG, "存储的用户ID实效，须在线登录!");
						return getResources().getText(R.string.login_fail_local_none).toString();
					}
					return null;
				}catch(Exception e){
					Log.e(TAG, "在线登录异常:" + e.getMessage(), e);
					return e.getMessage();
				}
			}
			/*
			 * 前台主线程更新处理。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(String result) {
				Log.d(TAG, "离线登录验证完成...");
				//清除等待动画
				progressDialog.dismiss();
				//判断登录结果
				if(StringUtils.isNotBlank(result)){
					//显示失败信息
					Toast.makeText(appContext, result, Toast.LENGTH_SHORT).show();
				}else{
					//设置当前用户ID
					appContext.setCurrentUserId(_userId);
					//记住用户
					rememberUserPassword(_username, _password);
					//跳转
					gotoMain(_userId, _username);
				}
			}
		}.execute(this.userName, this.userPassword);
	}
	//记住用户/密码
 	private void rememberUserPassword(final String userName, final String password){
		Log.d(TAG, "准备记住用户/密码操作...");
		if(!this.chkRememeber.isChecked() || StringUtils.isBlank(userName) || StringUtils.isBlank(password)) return;
		//异步线程处理
		AppContext.pools_fixed.execute(new Runnable() {
			/*
			 * 后台异步线程保存数据。
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				try{
					Log.d(TAG, "异步线程记住用户/密码...");
					final SharedPreferences userSharedPreferences = getSharedPreferences(Constant.PREFERENCES_CONFIG_SHARE_USER, Context.MODE_PRIVATE);
					if(userSharedPreferences != null){
						userSharedPreferences.edit()
															  .putString(userName, Base64.encodeBase64String(password.getBytes()))
															  .commit();
					}
				}catch(Exception e){
					Log.e(TAG, "记住用户/密码:" + e.getMessage(), e);
				}
			}
		});
	}
	//异步保存用户信息到本地
	private void saveUserToLocal(final String userId, final String username,final String password){
		Log.d(TAG, "准备异步线程保存用户信息数据到本地...");
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(username) || StringUtils.isBlank(password)) return;
		//异步线程处理
		AppContext.pools_fixed.execute(new Runnable() {
			/*
			 * 后台异步线程保存数据。
			 * @see java.lang.Runnable#run()
			 */
			@Override
			public void run() {
				try {
					Log.d(TAG, "异步线程保存用户数据...");
					//初始化保存用户/密码
					final SharedPreferences userPwdSharedPreferences = getSharedPreferences(Constant.PREFERENCES_CONFIG_USERPWD, Context.MODE_PRIVATE);
					if(userPwdSharedPreferences != null){
						//记住用户密码
						userPwdSharedPreferences.edit()
																	 .putString(Constant.PREFERENCES_CONFIG_USERPWD_USERID + DigestUtils.md5Hex(username), userId)
																	 .putString(username, DigestUtils.md5Hex(username + password))
																	 .commit();
					}
					//初始化保存当前用户
					final SharedPreferences currentUserPreferences = getSharedPreferences(Constant.PREFERENCES_CONFIG_CURRENT_USER, Context.MODE_PRIVATE);
					if(currentUserPreferences != null){
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
	//跳转到主Activity。
	private void gotoMain(String userId, String userName){
		Log.d(TAG, "跳转到主界面..." + userName + "[" + userId+"]");
		if(StringUtils.isNotBlank(userId) && StringUtils.isNotBlank(userName)){
			//初始化意图
			final Intent intent = new Intent(this, MainActivity.class);
			intent.putExtra(Constant.CONST_USERID, userId);
			intent.putExtra(Constant.CONST_USERNAME, userName);
			//启动意图
			this.startActivity(intent);
		}
		//关闭
		this.finish();
	}
	/*
	 * 重载恢复。
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		Log.d(TAG, "重载初始化输入框");
		try{
			//初始化记住的用户/密码
			final SharedPreferences userShared = this.getSharedPreferences(Constant.PREFERENCES_CONFIG_SHARE_USER, Context.MODE_PRIVATE);
			if(userShared != null && this.txtUsername != null){
				final Map<String, ?> users = userShared.getAll();
				if(users != null && users.size() > 0){
					final String[] userArrays = users.keySet().toArray(new String[0]);
					if(userArrays != null && userArrays.length > 0){
						final String username = userArrays[userArrays.length - 1];
						if(StringUtils.isNotBlank(username)){
							//设置账号
							this.txtUsername.setText(username);
							//密码
							final String enpwd = userShared.getString(username, null);
							if(StringUtils.isNotBlank(enpwd) && this.txtPassword != null){
								this.txtPassword.setText(new String(Base64.decodeBase64(enpwd)));
								this.txtPassword.requestFocus();
							}
						}
					}
				}
			}
		}catch(Exception e){
			Log.e(TAG, "初始化输入框异常:" + e.getMessage(), e);
		}
		//
		super.onResume();
	}
	/*
	 * 重载停止。
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		super.onDestroy();
	}
	/*
	 * 重载销毁。
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		Log.d(TAG, "重载销毁...");
		if(this.progressDialog != null){
			this.progressDialog.dismiss();
		}
		super.onDestroy();
	}
}