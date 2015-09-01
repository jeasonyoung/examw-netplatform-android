package com.examw.netschool;

import java.lang.ref.WeakReference;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
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
import com.examw.netschool.codec.digest.DigestUtils;
import com.examw.netschool.dao.UserDao;
import com.examw.netschool.entity.User;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;

public class LoginActivity extends Activity implements TextWatcher {
	private static final String TAG = "LoginActivity";
	
	private AutoCompleteTextView usernameText;
	private String[] items;// 适配autoCompleteTextView的数据
	private EditText pwdText;
	private ImageButton loginBtn1,loginBtn2;
	private ProgressDialog o;
	
	private Handler handler;
	private CheckBox rememeberCheck;
	
	private String password;
	private SharedPreferences share, share2, userinfo;
	
	private UserDao userdao;
	private AppContext appContext;
	/** 如果登陆失败,这个可以给用户确切的消息显示,true是网络连接失败,false是用户名和密码错误 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Log.d(TAG, "初始化...");
		
		setContentView(R.layout.activity_login);
		usernameText = (AutoCompleteTextView) this.findViewById(R.id.usernameText);// 用户名
		pwdText = (EditText) this.findViewById(R.id.pwdText);// 密码
		loginBtn1 = (ImageButton) this.findViewById(R.id.login1Btn);// 在线登录
		loginBtn2 = (ImageButton) this.findViewById(R.id.login2Btn);// 本地登录
		rememeberCheck = (CheckBox) this.findViewById(R.id.rememeberCheck);// 记住密码
//		goRegisterBtn = (Button) this.findViewById(R.id.goRegisterBtn);// 注册
//		goRegisterBtn.setText(Html.fromHtml("<u>免费注册</u>"));
		userdao = new UserDao(this); 	//操作数据库
		share = getSharedPreferences("passwordfile", 0);
		share2 = getSharedPreferences("abfile", 0);
		userinfo = getSharedPreferences("userinfo", 0);
		items = share.getAll().keySet().toArray(new String[0]);
		appContext = (AppContext) getApplication();
		usernameText.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, items));
		usernameText.setOnEditorActionListener(new OnEditorActionListener() {
				public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
					usernameText.requestFocus();
					return true;
				}
		});
		usernameText.addTextChangedListener(this);
		//在线登录
		loginBtn1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Log.d(TAG, "在线登录...");
					final String name = usernameText.getText().toString();
					Log.d(TAG, "username => " + name);
					password = pwdText.getText().toString();
					Log.d(TAG, "password => " + password);
					
					if (checkInput()) {
						if(checkNetWork())
						{
							// 提示正在登录
							if(o == null)
							{
								o = ProgressDialog.show(LoginActivity.this, null, "登录中请稍候", true, true);
								o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
							}else{ 
								o.show(); 
							}
							//
							if(appContext.getLoginState() == AppContext.LOGINING) return;
							// 开启一个线程用来登录
							new Thread() {
									public void run() {
										final String url = Constant.DOMAIN_URL + "/api/m/login.do";
										appContext.setLoginState(AppContext.LOGINING);
										try {
											final String pwd = DigestUtils.md5Hex(DigestUtils.md5Hex(Constant.DOMAIN_AGENCY_ID + name) + password);
											final String post = "agencyId="+Constant.DOMAIN_AGENCY_ID+"&username="+ name +"&pwd=" + pwd;
											final String result = DigestClientUtil.sendDigestRequest(Constant.DOMAIN_Username, Constant.DOMAIN_Password, "POST",  url,  post);
											Log.d(TAG, "登录反馈:" + result);
											if (StringUtils.isNotBlank(result)) {
												try {
													// 解析字符串
													final JSONObject json = new JSONObject(result);
													if (json.optBoolean("success")) { // 登录成功
														final String userId = json.getString("data");
														Log.d(TAG, "userId => " + userId);
														
														if (isRememberMe()) { saveSharePreferences(true,true); }
														
														userinfo.edit().putString("id", userId).commit();
														userinfo.edit().putString("name", name).commit();
														
														password = new String(Base64.encode(Base64.encode(password.getBytes(), 0), 0));
														final User user = new User(userId,name,password);
														saveToLocaleDB(user);
														
														appContext.saveLoginInfo(user);
														Message msg = handler.obtainMessage();
														msg.what = 1;
														Bundle data = new Bundle();
														data.putString("username", name);
														data.putString("uid", userId);
														
														msg.setData(data);
														handler.sendMessage(msg);//登录成功
													}else
													{
														handler.sendEmptyMessage(-3); //用户名密码错误
														appContext.setLoginState(AppContext.LOGIN_FAIL);
													}
												} catch (Exception e) {
													handler.sendEmptyMessage(-2);
													appContext.setLoginState(AppContext.LOGIN_FAIL);
												}
											} else {
												handler.sendEmptyMessage(-2);
												appContext.setLoginState(AppContext.LOGIN_FAIL);
											}
										} catch (Exception e) {
											e.printStackTrace();
											handler.sendEmptyMessage(-1); // 连接有误
											appContext.setLoginState(AppContext.LOGIN_FAIL);
										}
							};
						}.start();
					}
				}
			}
		});
		//本地登录
		loginBtn2.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					Log.d(TAG, "本地登录...");
					if(checkInput()){
						final String name = usernameText.getText().toString();
						Log.d(TAG, "username => " + name);
						final User user = userdao.findByUsername(name);
						Log.d(TAG, new Gson().toJson(user));
						if(user != null)
						{
							Log.d(TAG, new Gson().toJson(user));
							
							final String password = pwdText.getText().toString();
							Log.d(TAG, "password => " + password);
							//
							if(StringUtils.equalsIgnoreCase(password, new String(Base64.decode(Base64.decode(user.getPassword(), 0), 0))))
							{
								showToast("登录成功");
								appContext.saveLocalLoginInfo(name);
								Intent intent = new Intent();
								intent.setClass(LoginActivity.this, MainActivity.class);
								Bundle bundle = new Bundle();
								bundle.putString("MAP_USERNAME", name);
								bundle.putString("loginType", "local");
								intent.putExtras(bundle);
								// 转向登陆后的页面
								BaseActivity.username = name;
								BaseActivity.loginType = "local";
								
								startActivity(intent);
								
								LoginActivity.this.finish();
								
							}else{
								showToast("请先在线登录");
							}
						}else{
							showToast("请先在线登录");
						}
					}
				}
		});
		//
		handler = new MyHandler(this);
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
					
					final Intent intent = new Intent(login,MainActivity.class);
					intent.putExtra("MAP_USERNAME", data.getString("username"));
					intent.putExtra("uid",data.getString("uid"));
					intent.putExtra("loginType", "online");
					
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

	/**
	 * 如果登录成功过,则将登陆用户名和密码记录在SharePreferences
	 * 
	 * @param saveUserName
	 *            是否将用户名保存到SharePreferences
	 * @param savePassword
	 *            是否将密码保存到SharePreferences
	 * */
	private void saveSharePreferences(boolean saveUserName, boolean savePassword) {
		if (saveUserName) {
			Log.d(this.toString(), "saveUserName=" + usernameText.getText().toString());
			share.edit().putString(usernameText.getText().toString(),Base64.encodeToString(Base64.encode(password.getBytes(), 0), 0)).commit();
			share2.edit().putString("n", usernameText.getText().toString()).commit();
			share2.edit().putString("p", Base64.encodeToString(Base64.encode(password.getBytes(), 0), 0)).commit();
		}
	}
	//
	public void saveToLocaleDB(User user){
		try {
			if(userdao==null)userdao = new UserDao(this);
			
			userdao.saveOrUpdate(user);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	/** 记住密码选项是否勾选 */
	private boolean isRememberMe() {
		if (rememeberCheck.isChecked()) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_login, menu);
		return true;
	}

	// 检查网络
	private boolean checkNetWork() {
		final ConnectivityManager manager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(manager != null){
			final NetworkInfo info = manager.getActiveNetworkInfo();
			if (info == null || !info.isConnected()) {
				Toast.makeText(this,"请检查网络", Toast.LENGTH_SHORT).show();
				return false;
			}
		}
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

	// 初始化输入框
	@Override
	protected void onResume() {
		super.onResume();
		this.usernameText.setText(this.getSharedPreferences("abfile", 0).getString("n", ""));
		String pwd = this.getSharedPreferences("abfile", 0).getString("p", "");
		this.pwdText.setText(new String(Base64.decode(Base64.decode(pwd, 0), 0)));
	}

	// 按两次退出程序
	long waitTime = 2000;// 等待时间2s
	long touchTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN
				&& KeyEvent.KEYCODE_BACK == keyCode) {
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

	@Override
	public void afterTextChanged(Editable s) {
		String name = usernameText.getText().toString();
		pwdText.setText(new String(Base64.decode(Base64.decode(share.getString(name, ""), 0), 0)));
		if (pwdText.getText().toString().length() > 0)
			pwdText.requestFocus();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) { }
	
	private void showToast(String content)
	{
		Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	protected void onStop() {
		super.onDestroy();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		if(userdao == null) userdao = new UserDao(this);
	}
	
	@Override
	protected void onDestroy() {
		if(o != null) o.dismiss(); 
		super.onDestroy();
	}
}