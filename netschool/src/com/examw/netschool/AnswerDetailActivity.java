package com.examw.netschool;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.AQDetailDao;
import com.examw.netschool.model.AQDetail;
import com.examw.netschool.model.JSONCallback;
import com.examw.netschool.util.DigestClientUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

/**
 * 答疑明细Activity.
 * 
 * @author jeasonyoung
 * @since 2015年9月23日
 */
public class AnswerDetailActivity extends Activity implements OnClickListener {
	private static final String TAG = "AnswerDetailActivity";
	private EditText txtCallback;
	private ProgressDialog progressDialog;
	
	private String userId,userName,topicId;
	private AQDetailDao detailDao;
	
	private final List<AQDetail> details;
	private final DetailsAdapter adapter;
	/**
	 * 构造函数。
	 */
	public AnswerDetailActivity(){
		Log.d(TAG, "初始化...");
		this.details = new ArrayList<AQDetail>();
		this.adapter = new DetailsAdapter(this.details);
	}
	/*
	 * 重载创建。
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//加载布局
		this.setContentView(R.layout.activity_answer_detail);
		//获取传递数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//用户姓名
			this.userName = intent.getStringExtra(Constant.CONST_USERNAME);
			//主题ID
			this.topicId = intent.getStringExtra(AnswerActivity.CONST_TOPIC_ID);
		}
		//返回按钮
		final View btnBack = this.findViewById(R.id.btn_return);
		btnBack.setOnClickListener(this);
		//答疑主题标题
		final TextView tvTopicTitle = (TextView)this.findViewById(R.id.answer_topic_title);
		if(intent != null)tvTopicTitle.setText(intent.getStringExtra(AnswerActivity.CONST_TOPIC_TITLE));
		//答疑主题内容
		final TextView tvTopicContent = (TextView)this.findViewById(R.id.answer_topic_content);
		if(intent != null)tvTopicContent.setText(intent.getStringExtra(AnswerActivity.CONST_TOPIC_CONTENT));
		//数据列表
		final ListView listView = (ListView)this.findViewById(R.id.list_answer_details);
		//设置数据适配器
		listView.setAdapter(this.adapter);
		//回复内容
		this.txtCallback = (EditText)this.findViewById(R.id.answer_detail_callback);
		//回复按钮
		final View btnCallback = this.findViewById(R.id.btn_callback);
		btnCallback.setOnClickListener(this);
		//
		super.onCreate(savedInstanceState);
	}
	/*
	 * 重载启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		Log.d(TAG, "重载启动...");
		//异步加载数据
		new AsyncLoadData().execute((Void)null);
		//
		super.onStart();
	}
	/*
	 * 按钮事件处理。
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		Log.d(TAG, "按钮事件处理..." + v);
		switch(v.getId()){
			case R.id.btn_return:{//返回按钮事件处理
				Log.d(TAG, "返回按钮事件处理...");
				this.finish();
				break;
			}
			case R.id.btn_callback:{//回复按钮处理。
				//上传回复
				this.pushCallbackToServer();
				break;
			}
		}
	}
	//上传到服务器。
	private void pushCallbackToServer(){
		if(StringUtils.isBlank(userId) || StringUtils.isBlank(topicId)) return;
		//回复数据
		final String callback_content =  this.txtCallback.getText().toString();
		if(StringUtils.isBlank(callback_content)) return;
		//
		final AppContext appContext = (AppContext)this.getApplicationContext();
		//检查网络
		if(!appContext.isNetworkConnected()) return;
		//初始化等待动画
		if(this.progressDialog == null){
			this.progressDialog = ProgressDialog.show(this, null, "上传回复...", true, true);
			this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		}
		//启动等待动画
		this.progressDialog.show();
		//后台线程上传处理
		new AsyncTask<Void, Void, AQDetail>(){
			/*
			 * 后台线程上传处理。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected AQDetail doInBackground(Void... params) {
				try{
					Log.d(TAG, "异步线程上传答疑反馈...");
					//初始化回复
					final AQDetail detail = new AQDetail();
					//所属主题ID
					detail.setTopicId(topicId);
					//设置回复ID
					detail.setId(UUID.randomUUID().toString());
					//设置回复内容
					detail.setContent(callback_content);
					//设置回复用户ID
					detail.setUserId(userId);
					//设置回复用户姓名
					detail.setUserName(userName);
					//上传数据
					final JSONCallback<Object> callback = DigestClientUtil.sendDigestPOSTJSONRequest(Constant.DOMAIN_URL + "/api/m/aq/details.do", detail);
					if(callback.getSuccess()){
						Log.d(TAG, "上传数据成功...");
						return detail;
					}
					Log.e(TAG,  callback.getSuccess() + " / " + callback.getMsg());
				}catch(Exception e){
					Log.e(TAG, "上传答疑反馈异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前台主线程处理。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(AQDetail result) {
				//关闭等待动画
				if(progressDialog != null){
					progressDialog.dismiss();
				}
				//清除回复
				txtCallback.setText(null);
				//添加到数据源
				if(result != null){
					//添加到数据源
					details.add(result);
					//通知适配器更新数据
					adapter.notifyDataSetChanged();
				}
			};
		}.execute((Void)null);
	}
	//异步加载数据
	//异步加载数据
	private class AsyncLoadData extends AsyncTask<Void, Void, List<AQDetail>>{
		/*
		 * 后台异步线程加载数据。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<AQDetail> doInBackground(Void... params) {
			try{
				Log.d(TAG, "后台异步线程加载数据...");
				//检查网络
				final AppContext appContext = (AppContext)getApplicationContext();
				if(appContext != null && appContext.isNetworkConnected() && StringUtils.isNotBlank(topicId)){
					//网络下载数据
					final String result = DigestClientUtil.sendDigestGetRequest(Constant.DOMAIN_URL + "/api/m/aq/details/"+topicId+".do");
					if(StringUtils.isNotBlank(result)){
						//解析结果数据
						final Gson gson = new Gson();
						final Type type = new TypeToken<JSONCallback<AQDetail[]>>(){}.getType();
						final JSONCallback<AQDetail[]> callback = gson.fromJson(result, type);
						//获取数据成功
						if(callback.getSuccess() && callback.getData() != null && callback.getData().length > 0){
							//惰性加载数据
							if(detailDao == null){
								Log.d(TAG, "惰性加载数据操作...");
								detailDao = new AQDetailDao(AnswerDetailActivity.this, userId);
							}
							//更新数据
							for(AQDetail detail : callback.getData()){
								if(detail == null || StringUtils.isBlank(detail.getId()) || StringUtils.isBlank(detail.getTopicId())) continue;
								if(detailDao.hasDetail(detail.getId())){//存在
									Log.d(TAG, "更新数据...");
									detailDao.update(detail);
								}else{
									Log.d(TAG, "新增数据...");
									detailDao.insert(detail);
								}
							}
						}
					}
				}
				//惰性加载数据
				if(detailDao == null){
					Log.d(TAG, "惰性加载数据操作...");
					detailDao = new AQDetailDao(AnswerDetailActivity.this, userId);
				}
				//返回数据
				return detailDao.loadDetails(topicId);
			}catch(Exception e){
				Log.e(TAG, "加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程更新数据。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<AQDetail> result) {
			Log.d(TAG, "前台主线程更新数据...");
			//清除数据
			details.clear();
			//更新数据
			if(result != null && result.size() > 0){
				Log.d(TAG, "更新数据...");
				 details.addAll(result);
			}
			//通知适配器更新数据
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器
	private class DetailsAdapter  extends BaseAdapter{
		private static final String TAG = "DetailsAdapter";
		private List<AQDetail> list;
		/**
		 * 构造函数。
		 * @param details
		 */
		public DetailsAdapter(List<AQDetail> details){
			Log.d(TAG, "初始化...");
			this.list = details;
		}
		/*
		 * 获取数据总数。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (this.list == null) ? 0 : this.list.size();
		}
		/*
		 * 获取数据对象。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			return (this.list == null || this.list.size() < position) ? null : this.list.get(position);
		}
		/*
		 * 获取数据对象ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据行。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "获取数据行..." + position);
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "新建行..." + position);
				//加载数据行布局
				convertView = LayoutInflater.from(AnswerDetailActivity.this).inflate(R.layout.activity_answer_detail_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else {
				Log.d(TAG, "重用行..." + position);
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((AQDetail)this.getItem(position));
			//返回
			return convertView;
		}
	}
	//数据行View
	private class ViewHolder{
		private TextView tvContent,tvUsername,tvTime;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			//明细内容
			this.tvContent = (TextView)convertView.findViewById(R.id.answer_detail_content);
			//用户姓名
			this.tvUsername = (TextView)convertView.findViewById(R.id.answer_detail_username);
			//时间
			this.tvTime = (TextView)convertView.findViewById(R.id.answer_detail_time);
		}
		/**
		 * 加载数据。
		 * @param detail
		 */
		public void loadData(AQDetail detail){
			if(detail != null){
				//明细内容
				this.tvContent.setText(detail.getContent());
				//用户姓名
				this.tvUsername.setText(detail.getUserName());
				//时间
				this.tvTime.setText(detail.getCreateTime());
			}
		}
	}
}