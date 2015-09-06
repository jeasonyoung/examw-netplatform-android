package com.examw.netschool;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.app.Constant;

public class SettingActivity extends Activity implements OnCheckedChangeListener,OnClickListener{
	private ImageButton returnBtn,clearCache;
	private TextView availableSpace,downFilePath;
	private CheckBox isUse3GDown,isUse3GPlay;
	private String username;
	private SharedPreferences setting;
	private Spinner spinner;
	private Button checkBtn,aboutusBtn;
	private AlertDialog alertDialog = null;
	private ProgressDialog progressDialog,o;
	private boolean isCanceled=false;
	private AsyncTask<String,Integer,String> downloader = null;
	private static String[] data = new String[]{"每次启动","一天一次","一星期一次","一个月一次"};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_setting);
		this.returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		this.returnBtn.setOnClickListener(this);
		this.availableSpace = (TextView) this.findViewById(R.id.set_down_spaceavailable);
		this.downFilePath = (TextView) this.findViewById(R.id.down_filepathTxt);
		this.username = getIntent().getStringExtra("username");
		String path = getString(R.string.Downfilepath);
		this.downFilePath.setText(path+this.username+"/");
		this.setting = this.getSharedPreferences("settingfile", 0);
		this.isUse3GDown = (CheckBox) this.findViewById(R.id.set_IsUser3G_check);
		this.isUse3GPlay = (CheckBox) this.findViewById(R.id.set_IsUser3G_check2);
		this.clearCache = (ImageButton) this.findViewById(R.id.set_clearPicBtn);
		this.clearCache.setOnClickListener(this);
		this.checkBtn = (Button) this.findViewById(R.id.check);
		this.checkBtn.setOnClickListener(this);
		this.aboutusBtn = (Button) this.findViewById(R.id.aboutus);
		this.aboutusBtn.setOnClickListener(this);
		this.spinner = (Spinner) this.findViewById(R.id.checkupdateSpinner);
		this.spinner.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,data));
		this.spinner.setPrompt("请选择检测更新周期");
		this.spinner.setSelection(this.setting.getInt("setCheckUpdateMode", 0));//设置默认值
		this.spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				SharedPreferences.Editor editor = setting.edit();
				editor.remove("setCheckUpdateMode");
				editor.putInt("setCheckUpdateMode", arg2);
				editor.commit();
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		initAvailableSpaceTextView();
		initCheckBox();
		this.isUse3GDown.setOnCheckedChangeListener(this);
		this.isUse3GPlay.setOnCheckedChangeListener(this);
	}
	
	private void initCheckBox() {
		this.isUse3GDown.setChecked(this.setting.getBoolean("setDownIsUse3G", true));
		this.isUse3GPlay.setChecked(this.setting.getBoolean("setPlayIsUse3G", true));
		if(!this.setting.contains("IsFirst"))
		{
			SharedPreferences.Editor editor = this.setting.edit();
			editor.putString("Isfirst", "No");
			editor.putBoolean("setDownIsUse3G", true);
			editor.putString("setDownfilepath", getString(R.string.Downfilepath));
			editor.putBoolean("setDownfiletype", true);
			editor.putBoolean("setPlayIsUse3G", true);
			editor.putBoolean("setPlayfiletype", true);
			editor.putInt("setCheckUpdateMode",0);
			editor.putLong("lastCheckUpdateTime", 0);
			editor.commit();
		}
	}
	
	private void initAvailableSpaceTextView() {
		//判断SD卡是否可用,计算SD卡的可用空间
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			//计算 sd可用空间的大小
			//取得SD文件的路径
			File path = Environment.getExternalStorageDirectory(); 
			StatFs statfs = new StatFs(path.getPath());
			//获得block区的大小
			long blockSize = statfs.getBlockSize();
			//获得可用block的数量 
			long availableBlocks = statfs.getAvailableBlocks();
			long size = availableBlocks * blockSize/1024/1024;	//MB
			this.availableSpace.setText(" "+size+" MB");
			return;
		}
		this.availableSpace.setTextColor(getResources().getColor(R.color.grey));
		this.availableSpace.setText(" SD卡不存在");
		this.downFilePath.setTextColor(getResources().getColor(R.color.grey));
		this.downFilePath.setText(this.downFilePath.getText()+" 路径不可用");
	}
	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		int id = buttonView.getId();
		SharedPreferences.Editor editor = this.setting.edit();
		switch(id)
		{
			case R.id.set_IsUser3G_check:
				editor.remove("setDownIsUse3G");
				editor.putBoolean("setDownIsUse3G", this.isUse3GDown.isChecked());
				editor.commit();
			case R.id.set_IsUser3G_check2:
				editor.remove("setPlayIsUse3G");
				editor.putBoolean("setPlayIsUse3G", this.isUse3GPlay.isChecked());
				editor.commit();
		}
	}
	@Override
	public void onClick(View v) {
		switch(v.getId())
		{
			case R.id.set_clearPicBtn:
				Toast.makeText(this, "清除成功", Toast.LENGTH_SHORT).show();
				break;
			case R.id.returnbtn:
				this.finish();
				break;
			case R.id.aboutus:
				startActivity(new Intent(this,AboutusActivity.class));
				break;
			case R.id.check:
				check();
				break;
		}
	}
	private void check()
	{
		o = ProgressDialog.show(SettingActivity.this, null, "检测中请稍候", true, false);
		o.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		CheckUpdateTask checkup = new CheckUpdateTask();
		try {
			String oldVersion = getVersionName();	//获取旧的版本号
			System.out.println(oldVersion);
			checkup.execute(Constant.DOMAIN_URL+"mobile/checkup?appType=1&oldVersion="+oldVersion);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	//获取当前应用的版本号
	private String getVersionName() throws Exception
	 {
           // 获取packagemanager的实例
           PackageManager packageManager = getPackageManager();
           // getPackageName()是你当前类的包名，0代表是获取版本信息
           PackageInfo packInfo = packageManager.getPackageInfo(getPackageName(),0);
           String version = packInfo.versionName;
           return version;
	}
	
	private class CheckUpdateTask extends AsyncTask<String,Integer,String>
	{
		@Override
		protected void onPreExecute() {
			o.show();
			SharedPreferences.Editor editor = setting.edit();
			editor.remove("lastCheckUpdateTime");
			editor.putLong("lastCheckUpdateTime", System.currentTimeMillis());
			editor.commit();
			super.onPreExecute();
		}
		//检查，返回值就是结束时的结果参数
		@Override
		protected String doInBackground(String... params) {
			String result = null;
			HttpURLConnection conn = null;
			try{
				URL url = new URL(params[0]);
				conn = (HttpURLConnection) url.openConnection();
				conn.setConnectTimeout(5000);//连接超时
				conn.setRequestMethod("GET");//请求方式
				conn.connect();// 连接
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					Log.d(this.toString(), "getResponseCode() not HttpURLConnection.HTTP_OK");
					return null;
				}
				InputStream in = conn.getInputStream();
				//创建一个缓冲字节数			//字节不多时这么做
				byte[] buffer = new byte[in.available()];
				//在输入流中读取数据并存放到缓冲字节数组中
				in.read(buffer);
				//将字节转换成字符串
				result = new String(buffer);
				System.out.println(result);
			}catch(Exception e) {
				//各种错误
				Log.d(this.toString(), e.getMessage());
				return null;
			}finally {
				if(conn!=null)
				{
					conn.disconnect();
				}
			}
			return result;
		}
		//结束检查
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(o!=null) o.dismiss();
			if(result==null)
			{
				//跳转到登录界面
				Toast.makeText(SettingActivity.this, "无法检测,稍后再试", Toast.LENGTH_SHORT).show();
				return;
			}
			try {
				JSONObject json = new JSONObject(result);
				if(json.getInt("S")==1)//表示有更新
				{
					final String version = json.optString("version");
			        final String url = URLDecoder.decode(json.optString("url"),"UTF-8");
			        System.out.println(url);
			        String content=null;
			        content = json.optString("Content");
			        alertDialog = new AlertDialog.Builder(SettingActivity.this)
					.setTitle("更新检测")
					.setMessage("检测到最新版本：" + version + "\n" + "更新内容：" + "\n" + content)
					.setPositiveButton("更新", new DialogInterface.OnClickListener() {
						
						@SuppressWarnings("deprecation")
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							//开启下载服务，以及进度对话框，进行下载
							//to do something 
							progressDialog = new ProgressDialog(SettingActivity.this);
						    progressDialog.setProgressStyle(1);
						    progressDialog.setTitle("软件更新");
						    progressDialog.setMessage("最新版本：" + version);
						    progressDialog.setIcon(R.drawable.down2);
						    progressDialog.setProgress(10);
						    progressDialog.setMax(100);
						    progressDialog.setIndeterminate(false);
						    progressDialog.setCancelable(false);
						    //开启一个异步任务进行app下载
						    downloader = new DownLoaderTask();
						    progressDialog.setButton("取消", new DialogInterface.OnClickListener() {	//取消下载
								@Override
								public void onClick(DialogInterface dialog, int which) {
									//取消
									dialog.cancel();
									isCanceled =true;
									downloader.cancel(true);
								}
							});
						    downloader.execute(url);	//下载地址
						    progressDialog.show();
						  //监听按键事件,如果按取消什么都不做,相当于对这个对话框禁用了返回键
						    progressDialog.setOnKeyListener(new OnKeyListener() {
								@Override
								public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
									if(event.getKeyCode()==4)
									{
										return true;
									}
									return false;
								}
							});
						}
					})
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					})
					.show();
			        //监听按键事件,如果按取消什么都不做,相当于对这个对话框禁用了返回键
			        alertDialog.setOnKeyListener(new OnKeyListener() {
						@Override
						public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
							if(event.getKeyCode()==KeyEvent.KEYCODE_BACK&&event.getRepeatCount()==0)
							{
								return true;
							}
							return false;
						}
					});
				}else{
					Toast.makeText(SettingActivity.this, "已经是最新版本", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	//下载更新app
	private class DownLoaderTask extends AsyncTask<String, Integer, String> {
		//The method (doInBackground) runs always on a background thread. You shouldn't do any UI tasks there.
	    @Override
	    protected String doInBackground(String... sUrl) {
	        try {
	            URL url = new URL(sUrl[0]);
	            URLConnection connection = url.openConnection();
	            connection.connect();
	            // this will be useful so that you can show a typical 0-100% progress bar
	            int fileLength = connection.getContentLength();
	            // download the file
	            InputStream input = new BufferedInputStream(url.openStream());
	            //StartActivity.createFile("Hello");
	            OutputStream output = new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk");
	            byte data[] = new byte[1024];
	            long total = 0;
	            int count;
	            while ((count = input.read(data)) != -1) {
	            	if(!isCanceled&&!this.isCancelled()){
		                total += count;
		                // publishing the progress....
		                publishProgress((int) (total * 100 / fileLength));
		                output.write(data, 0, count);
	            	}else {
	            		break;
	            	}
	            }
	            output.flush();
	            output.close();
	            input.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	        if(isCanceled||this.isCancelled()){
	        	System.out.println("取消了下载");
	        	new File(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk").delete();
	        }
	        return null;
	    }
	    //the onProgressUpdate and onPreExecute run on the UI thread, so there you can change the progress bar:
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        progressDialog.show();
	    }
	    @Override
	    protected void onProgressUpdate(Integer... progress) {
	        super.onProgressUpdate(progress);
	        progressDialog.setProgress(progress[0]);
	    }
	    @Override
	    protected void onPostExecute(String result) {
	    	super.onPostExecute(result);
	    	if(!isCanceled&&!this.isCancelled()){
	    		progressDialog.dismiss();
	    		//显示这个应用，让用户安装
	    		Uri localUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath()+"/Hello.apk"));
	        	Intent localIntent = new Intent("android.intent.action.VIEW");
	        	localIntent.setDataAndType(localUri, "application/vnd.android.package-archive");
	        	SettingActivity.this.startActivity(localIntent);
	    	}
	    }
//	    @Override
//	    protected void onCancelled() {
//	    	super.onCancelled();
//	    }
	}
	public static void createFile(String paramString){
		File a = null, b = null;
	    if ("mounted".equals(Environment.getExternalStorageState()))
	    {
	      a = new File(Environment.getExternalStorageDirectory() + "/" + "eschool/");
	      if (!a.exists()) a.mkdirs();
	      b = new File(Environment.getExternalStorageDirectory() + "/eschool/" + paramString + ".apk");
	      if (!b.exists())
	      {
	    	  try {
	    		  b.createNewFile();
	    		  return;
	  	    	}
	  	    	catch (IOException localIOException) {
	  	    		localIOException.printStackTrace();
	  	    	}
	      }
	    }
	}
}