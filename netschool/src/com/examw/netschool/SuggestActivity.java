package com.examw.netschool;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.APIUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
/**
 * 意见反馈Activity。
 * 
 * @author jeasonyoung
 * @since 2015年9月25日
 */
public class SuggestActivity extends Activity implements OnClickListener{
	private static final String TAG = "SuggestActivity";
	private EditText txtContent;
	private ProgressDialog progressDialog;
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "重载创建...");
		//加载布局文件
		this.setContentView(R.layout.activity_suggest);
		//返回按钮
		final View btnReturn = this.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(this);
		//设置标题
		final TextView tvTopTitle = (TextView)this.findViewById(R.id.top_title);
		tvTopTitle.setText(R.string.suggest_title);
		//学员名称
		final TextView txtStudentName = (TextView)this.findViewById(R.id.txt_suggest_student_name);
		txtStudentName.setText(AppContext.getCurrentUsername());
		//建议内容
		this.txtContent = (EditText)this.findViewById(R.id.txt_suggest_content);
		//提交按钮
		final View btnSubmit = this.findViewById(R.id.btn_suggest_submit);
		btnSubmit.setOnClickListener(this);
	}
	/*
	 * 按钮事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮点击事件处理..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回按钮事件处理
				Log.d(TAG, "返回按钮事件处理...");
				this.finish();
				break;
			}
			case R.id.btn_suggest_submit:{//上传建议。
				Log.d(TAG, "上传建议...");
				this.postToServer();
				break;
			}
		}
	}
	//上传到服务器。
	private void postToServer(){
		//检查建议内容
		final String content = this.txtContent.getText().toString();
		if(StringUtils.isBlank(content)){
			Toast.makeText(this, R.string.suggest_content_error, Toast.LENGTH_SHORT).show();
			return;
		}
		//初始化等待动画
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, this.getResources().getText(R.string.suggest_post_upload_msg), true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//启动等待动画
		this.progressDialog.show();
		//初始化上下文
		final AppContext appContext = (AppContext)this.getApplicationContext();
		//异步线程上传数据
		new AsyncTask<Void, Void, String>() {
			/*
			 * 后台线程处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected String doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程上传数据处理...");
					if(!appContext.isNetworkConnected()){
						Log.d(TAG, "没有网络!");
						return "没有网路!";
					}
					//初始化参数
					final Map<String, Object> parameters = new HashMap<String, Object>();
					//添加当前用户ID
					parameters.put("randUserId", AppContext.getCurrentUserId());
					//添加建议内容
					parameters.put("content", content);
					
					//上传数据
					final JSONCallback<Object> callback = new APIUtils.CallbackJSON<Object>(Object.class)
							.sendPOSTRequest(getResources(),R.string.api_suggest_add_url, parameters);
					if(callback.getSuccess()){
						Log.d(TAG, "上传数据成功...");
						return null;
					}
					Log.e(TAG,  callback.getSuccess() + " / " + callback.getMsg());
					return callback.getMsg();
				}catch(Exception e){
					Log.e(TAG, "上传数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 主线程数据处理。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(String result) {
				Log.d(TAG, "主线程数据处理...");
				//隐藏等待动画
				progressDialog.dismiss();
				//消息处理
				if(StringUtils.isNotBlank(result)){
					Log.d(TAG, result);
					//消息显示
					Toast.makeText(SuggestActivity.this, result, Toast.LENGTH_SHORT).show();
				}else{
					//关闭窗体
					finish();
				}
			}
		}.execute((Void)null);	
	}
}