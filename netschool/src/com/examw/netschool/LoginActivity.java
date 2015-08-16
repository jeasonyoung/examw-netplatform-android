package com.examw.netschool;

import java.lang.ref.WeakReference;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.dao.UserDao;
import com.examw.netschool.entity.User;
import com.examw.netschool.util.Constant;
import com.examw.netschool.util.HttpConnectUtil;
import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends Activity implements TextWatcher {
	private AutoCompleteTextView usernameText;
	private String[] items;// 适配autoCompleteTextView的数据
	private EditText pwdText;
	private Button goRegisterBtn;
	private ImageButton loginBtn1,loginBtn2;
	private ProgressDialog o;
	private Handler handler;
	private CheckBox rememeberCheck;
	private String password;
	private SharedPreferences share;
	private SharedPreferences share2;
	private SharedPreferences userinfo;
	private UserDao userdao;
	private AppContext appContext;
	/** 如果登陆失败,这个可以给用户确切的消息显示,true是网络连接失败,false是用户名和密码错误 */

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		usernameText = (AutoCompleteTextView) this
				.findViewById(R.id.usernameText);// 用户名
		pwdText = (EditText) this.findViewById(R.id.pwdText);// 密码
		loginBtn1 = (ImageButton) this.findViewById(R.id.login1Btn);// 在线登录
		loginBtn2 = (ImageButton) this.findViewById(R.id.login2Btn);// 本地登录
		rememeberCheck = (CheckBox) this.findViewById(R.id.rememeberCheck);// 记住密码
		goRegisterBtn = (Button) this.findViewById(R.id.goRegisterBtn);// 注册
		goRegisterBtn.setText(Html.fromHtml("<u>免费注册</u>"));
		userdao = new UserDao(this); 	//操作数据库
		share = getSharedPreferences("passwordfile", 0);
		share2 = getSharedPreferences("abfile", 0);
		userinfo = getSharedPreferences("userinfo", 0);
		items = share.getAll().keySet().toArray(new String[0]);
		appContext = (AppContext) getApplication();
		usernameText.setAdapter(new ArrayAdapter<String>(this,
				android.R.layout.simple_dropdown_item_1line, items));
		usernameText.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				// TODO Auto-generated method stub
				usernameText.requestFocus();
				return true;
			}
		});
		usernameText.addTextChangedListener(this);
		loginBtn1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				//记录登录事件
				MobclickAgent.onEvent(LoginActivity.this,"LoginIn_online");
				
				final String name = usernameText.getText().toString();
				password = pwdText.getText().toString();
				if (checkInput()) {
					if(checkNetWork())
					{
						// 提示正在登录
						if(o==null)
						{
							o = ProgressDialog.show(LoginActivity.this, null, "登录中请稍候",
									true, true	);
							o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						}else
						{
							o.show();
						}
						if(appContext.getLoginState() == AppContext.LOGINING)
						{
							return;
						}
						// 开启一个线程用来登录
						new Thread() {
								public void run() {
									String url = Constant.DOMAIN_URL+"mobile/login?username="+name+"&password="+password;
									String result = null;
									appContext.setLoginState(AppContext.LOGINING);
									try {
										result = HttpConnectUtil.httpGetRequest(
												LoginActivity.this, url);
										if (!"".equals(result)) {
											try {
												// 解析字符串
												JSONObject json = new JSONObject(result);
												int ok = json.optInt("OK", 0);
												int id = json.optInt("uid", 0);
												if (ok == 1) { // 登录成功
													if (isRememberMe()) {
														saveSharePreferences(true,true);
													}
													userinfo.edit().putInt("id", id)
															.commit();
													userinfo.edit()
															.putString("name", name)
															.commit();
													password = new String(Base64.encode(Base64.encode(password.getBytes(), 0), 0));
													User user = new User(String.valueOf(id),name,password);
													saveToLocaleDB(user);
													appContext.saveLoginInfo(user);
													Message msg = handler.obtainMessage();
													msg.what = 1;
													Bundle data = new Bundle();
													data.putString("username", name);
													data.putInt("uid", id);
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
		loginBtn2.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(checkInput()){
				String name = usernameText.getText().toString();
				User user = userdao.findByUsername(name);
				System.out.println(user);
				if(user!=null)
				{
					String password = pwdText.getText().toString();
					if(password.equals(new String(Base64.decode(Base64.decode(user.getPassword(), 0), 0))))
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
					}else
					{
						showToast("请先在线登录");
					}
				}else
				{
					showToast("请先在线登录");
				}
				}
			}
		});
		goRegisterBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				startActivity(new Intent(LoginActivity.this,
						RegisterActivity.class));
			}
		});
		findViewById(R.id.findPwd).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				viewWebSite();
			}
		});
		handler = new MyHandler(this);

	}
	private void viewWebSite()
	{
		 String url = getResources().getString(R.string.findPwdUrl);
		 Uri uri = Uri.parse(url);          
	     Intent it = new Intent(Intent.ACTION_VIEW, uri);
	     startActivity(it);
	}
	private static class MyHandler extends Handler {
		WeakReference<LoginActivity> mActivity;

		MyHandler(LoginActivity activity) {
			mActivity = new WeakReference<LoginActivity>(activity);
		}

		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			LoginActivity login = mActivity.get();
			if (login.o != null) {
				login.o.dismiss();
			 }
			switch(msg.what)
			{
			case 1:
				//登录成功
				Bundle data = msg.getData();
				Intent intent = new Intent(login,MainActivity.class);
				intent.putExtra("MAP_USERNAME",data.getString("username"));
				intent.putExtra("uid",data.getInt("uid"));
				intent.putExtra("loginType", "online");
				login.startActivity(intent);
				login.finish();
				break;
			case -1:
				Toast.makeText(login, "连接不到服务器", Toast.LENGTH_SHORT).show();
				break;
			case -2:
				Toast.makeText(login, "连接错误", Toast.LENGTH_SHORT).show();
				break;
			case -3:
				Toast.makeText(login, "用户名或密码错误", Toast.LENGTH_SHORT).show();
				break;
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
			share.edit()
					.putString(
							usernameText.getText().toString(),
							Base64.encodeToString(Base64.encode(password.getBytes(), 0), 0))
					.commit();
			share2.edit().putString("n", usernameText.getText().toString())
					.commit();
			share2.edit()
					.putString(
							"p",
							Base64.encodeToString(Base64.encode(password.getBytes(), 0), 0))
					.commit();
		}
		//share = null;
	}
	//
	public void saveToLocaleDB(User user){
		if(userdao==null)
		{
			userdao = new UserDao(this);
		}
			try {
				userdao.saveOrUpdate(user);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
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
		ConnectivityManager manager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = manager.getActiveNetworkInfo();
		if (info == null || !info.isConnected()) {
			Toast.makeText(this,"请检查网络", Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	// 检查输入框的内容
	private boolean checkInput() {
		String username = usernameText.getText().toString().trim();
		String password = pwdText.getText().toString().trim();
		if (username.equals("") || password.equals("")) {
			Toast.makeText(LoginActivity.this, "用户名或密码不能为空", Toast.LENGTH_SHORT)
					.show();
			return false;
		}
		return true;
	}

	// 初始化输入框
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		this.usernameText.setText(this.getSharedPreferences("abfile", 0).getString("n", ""));
		String pwd = this.getSharedPreferences("abfile", 0).getString("p", "");
		this.pwdText.setText(new String(Base64.decode(Base64.decode(pwd, 0), 0)));
		MobclickAgent.onResume(this);
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
		// TODO Auto-generated method stub
		String name = usernameText.getText().toString();
		pwdText.setText(new String(Base64.decode(Base64.decode(share.getString(name, ""), 0), 0)));
		if (pwdText.getText().toString().length() > 0)
			pwdText.requestFocus();
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		// TODO Auto-generated method stub
	}
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
		// TODO Auto-generated method stub
		super.onStart();
		if(userdao==null)
		{
			userdao = new UserDao(this);
		}
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(o!=null)
		{
			o.dismiss();
		}
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
}