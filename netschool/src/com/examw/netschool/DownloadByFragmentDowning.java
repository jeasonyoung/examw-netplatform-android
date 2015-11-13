package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.model.DownloadComplete;
import com.examw.netschool.service.DownloadService;
import com.examw.netschool.service.IDownloadService;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 下载中Fragment。
 * 
 * @author jeasonyoung
 * @since 2015年9月11日
 */
public class DownloadByFragmentDowning extends Fragment implements ServiceConnection  {
	private static final String TAG = "DownloadByFragmentDowning";
	private LinearLayout nodataView;
	private IDownloadService downloadService;
	
	private final List<DownloadComplete> dataSource;
	private final DowningAdapter adapter;
	/**
	 * 构造函数。
	 * @param userId
	 */
	public DownloadByFragmentDowning(){
		Log.d(TAG, "初始化...");
		//绑定文件下载服务
		final Context context = AppContext.getContext();
		if(context != null){
			context.bindService(new Intent(context, DownloadService.class), this, Context.BIND_AUTO_CREATE);	
		}
		this.dataSource = new ArrayList<DownloadComplete>();
		this.adapter = new DowningAdapter(this.dataSource);
	}
	/*
	 * 重载创建视图。
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		//super.onCreateView(inflater, container, savedInstanceState);
		Log.d(TAG, "重载创建视图....");
		//加载视图
		final View view = inflater.inflate(R.layout.activity_download_downing, container, false);
		//没有数据
		this.nodataView = (LinearLayout)view.findViewById(R.id.nodata_view);
		this.nodataView.setVisibility(View.VISIBLE);
		
		//列表
		final ListView listView = (ListView)view.findViewById(R.id.download_listview_downing);
		//长按弹出取消下载的PopupWindow
		listView.setOnItemLongClickListener(this.onItemLongClickListener);
		//设置数据适配器
		listView.setAdapter(this.adapter);
		//返回
		return view;
	}
	/*
	 * 重载启动。
	 * @see android.support.v4.app.Fragment#onStart()
	 */
	@Override
	public void onStart() {
		super.onStart();
		Log.d(TAG, "重载启动...");
		//异步加载数据
		this.reloadData();
	}
	//异步加载数据。
	private void reloadData(){
		Log.d(TAG, "异步加载数据...");
		new AsyncLoadData().executeOnExecutor(AppContext.pools_single, (Void)null);
	}
	//下载长按取消
	private OnItemLongClickListener onItemLongClickListener = new OnItemLongClickListener(){
		@Override
		public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
			Log.d(TAG, "下载["+position+"]取消...");
			if(dataSource.size() > position){
				//获取下载课程
				final Download download = dataSource.get(position);
				if(download != null){
					//取消下载二次确认
					new AlertDialog.Builder(getActivity()).setIcon(android.R.drawable.ic_dialog_alert)
					.setTitle(R.string.download_group_downing_cancel_title).setMessage(R.string.download_group_downing_cancel_msg)
					.setNegativeButton(R.string.download_group_downing_cancel_btn_cancel, new DialogInterface.OnClickListener(){
						/*
						 * 取消退出。
						 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
						 */
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "取消退出");
							dialog.dismiss();
						}
					})
					.setPositiveButton(R.string.download_group_downing_cancel_btn_submit, new DialogInterface.OnClickListener(){
						/*
						 * 确定取消下载。
						 * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
						 */
						@Override
						public void onClick(DialogInterface dialog, int which) {
							Log.d(TAG, "取消下载课程资源["+download+"]...");
							//取消下载服务
							if(downloadService != null && StringUtils.isNotBlank(download.getLessonId())){
								Log.d(TAG, "取消后台服务下载["+download+"]...");
								downloadService.cancelDownload(download.getLessonId());
							}
							//初始化
							final DownloadDao downloadDao = new DownloadDao();
							//从数据库中删除
							downloadDao.delete(download.getLessonId());
							//重新刷新数据
							reloadData();
						}
					}).show();
					return true;
				}
			}
			return false;
		}
	};
	/*
	 * 下载服务联结。
	 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
	 */
	@Override
	public void onServiceConnected(ComponentName name, IBinder service) {
		Log.d(TAG, "下载服务联结..." + name);
		this.downloadService = (IDownloadService)service;
		if(this.downloadService != null){
			Log.d(TAG, "设置下载服务消息关联...");
			this.downloadService.setHandler(new DowningUIHandler(this));
			//初始化
			final DownloadDao downloadDao = new DownloadDao();
			Log.d(TAG, "添加下载课程资源...");
			final  List<DownloadComplete> list = downloadDao.loadDownings();
			if(list != null && list.size() > 0){
				for(DownloadComplete data : list){
					if(data == null) continue;
					this.downloadService.addDownload(data);
				}
			}
		}
	}
	/*
	 * 下载服务连接断开。
	 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
	 */
	@Override
	public void onServiceDisconnected(ComponentName name) {
		Log.d(TAG, "下载服务断开..." + name);
		this.downloadService = null;
	}
	//异步加载数据。
	private class AsyncLoadData extends AsyncTask<Void, Void, List<DownloadComplete>>{
		/*
		 * 后台异步线程处理.
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected List<DownloadComplete> doInBackground(Void... params) {
			try{
				Log.d(TAG, "异步加载列表数据...");
				//初始化
				final DownloadDao downloadDao = new DownloadDao();
				//返回未下载完成的数据
				return downloadDao.loadDownings();
			}catch(Exception e){
				Log.e(TAG, " 异步加载列表数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 前台主线程处理
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<DownloadComplete> result) {
			Log.d(TAG, "前台主线程更新数据...");
			//移除数据源
			dataSource.clear();
			//
			if(result != null && result.size() > 0){
				//填充数据
				dataSource.addAll(result);
			}
			//是否显示无数据View
			nodataView.setVisibility(dataSource.size() > 0 ? View.GONE : View.VISIBLE);
			//通知适配器更新
			adapter.notifyDataSetChanged();
		}
	}
	//数据适配器。
	private class DowningAdapter extends BaseAdapter{
		private static final String TAG = "DowningAdapter";
		private final List<DownloadComplete> downloads;
		/**
		 * 构造函数。
		 * @param downloads
		 */
		public DowningAdapter(List<DownloadComplete> downloads){
			this.downloads = downloads;
		}
		/*
		 * 获取数据量。
		 * @see android.widget.Adapter#getCount()
		 */
		@Override
		public int getCount() {
			return (downloads == null) ? 0 : downloads.size();
		}
		/*
		 * 获取数据。
		 * @see android.widget.Adapter#getItem(int)
		 */
		@Override
		public Object getItem(int position) {
			if(downloads != null && downloads.size() > position)
				return downloads.get(position);
			return null;
		}
		/*
		 * 获取数据ID。
		 * @see android.widget.Adapter#getItemId(int)
		 */
		@Override
		public long getItemId(int position) {
			return position;
		}
		/*
		 * 获取数据行View。
		 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
		 */
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d(TAG, "创建数据行["+position+"]...");
			ViewHolder viewHolder = null;
			if(convertView == null){
				Log.d(TAG, "创建新行..." + position);
				//加载布局文件
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.activity_download_downing_item, parent, false);
				//初始化
				viewHolder = new ViewHolder(convertView);
				//缓存
				convertView.setTag(viewHolder);
			}else{
				Log.d(TAG, "重用行:" + position);
				//加载控件
				viewHolder = (ViewHolder)convertView.getTag();
			}
			//加载数据
			viewHolder.loadData((DownloadComplete)this.getItem(position));
			//返回View
			return convertView;
		}
	}
	//数据行View
	private class ViewHolder implements OnClickListener{
		private TextView tvTitle,tvMsg,tvPercent;
		private ProgressBar progressBar;
		private ImageButton btnPause;
		private DownloadComplete data;
		/**
		 * 构造函数。
		 * @param convertView
		 */
		public ViewHolder(View convertView){
			//标题
			this.tvTitle = (TextView)convertView.findViewById(R.id.tv_title);
			//进度条
			this.progressBar = (ProgressBar)convertView.findViewById(R.id.progress);
			//消息
			this.tvMsg = (TextView)convertView.findViewById(R.id.tv_msg);
			//百分比
			this.tvPercent = (TextView)convertView.findViewById(R.id.tv_percent);
			//按钮
			this.btnPause = (ImageButton)convertView.findViewById(R.id.btn_pause);
			//设置按钮事件处理
			this.btnPause.setOnClickListener(this);
		}
		/**
		 * 加载数据。
		 * @param download
		 */
		public void loadData(DownloadComplete download){
			Log.d(TAG, "加载行数据...");
			if((this.data = download) == null) return;
			//标题
			this.tvTitle.setText(download.getLessonName());
			//进度
			if(download.getFileSize() == 0 || download.getCompleteSize() == 0){
				//进度条
				this.progressBar.setProgress(0);
				//百分比
				this.tvPercent.setText(null);
			}else{
				final  int per = (int)((download.getCompleteSize() / (double)download.getFileSize()) * 100);
				//进度条
				this.progressBar.setProgress(per);
				//百分比
				this.tvPercent.setText(per  + "%");
			}
			//状态
			final DownloadState state = DownloadState.parse(download.getState());
			//消息
			this.tvMsg.setText(state.getName());
			//资源
			final Resources res = getActivity().getResources();
			//按钮处理
			switch(state){
				case NONE:{//排队等待
					this.btnPause.setVisibility(View.GONE);
					break;
				}
				case FAIL:{//连接失败
					final Drawable top = res.getDrawable(R.drawable.download_group_downing_item_retry);
					if(top != null){
						top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
						this.btnPause.setImageDrawable(top);
					}
					this.btnPause.setContentDescription(res.getText(R.string.download_group_downing_item_btn_start));
					this.btnPause.setVisibility(View.VISIBLE);
					this.btnPause.setEnabled(true);
					break;
				}
				case CANCEL:{//取消
					this.btnPause.setVisibility(View.GONE);
					break;
				}
				case PAUSE:{//暂停
					final Drawable top = res.getDrawable(R.drawable.download_group_downing_item_btn_continue_icon);
					if(top != null){
						top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
						this.btnPause.setImageDrawable(top);
					}
					this.btnPause.setContentDescription(res.getText(R.string.download_group_downing_item_btn_pause));
					this.btnPause.setVisibility(View.VISIBLE);
					this.btnPause.setEnabled(true);
					break;
				}
				case DOWNING:{//下载中
					final Drawable top = res.getDrawable(R.drawable.download_group_downing_item_btn_pause_icon);
					if(top != null){
						top.setBounds(0, 0, top.getMinimumWidth(), top.getMinimumHeight());
						this.btnPause.setImageDrawable(top);
					}
					this.btnPause.setContentDescription(res.getText(R.string.download_group_downing_item_btn_start));
					this.btnPause.setVisibility(View.VISIBLE);
					this.btnPause.setEnabled(true);
					break;
				}
				case FINISH:{//下载完成
					this.btnPause.setVisibility(View.GONE);
					//进度条
					this.progressBar.setProgress(100);
					//百分比
					this.tvPercent.setText("100%");
					break;
				}
			}
		}
		/*
		 *按钮事件处理。
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			try{
				Log.d(TAG, "按钮点击事件处理..." + v + "["+ this.data+"]");
				if(this.data == null) return;
				DownloadState state = DownloadState.parse(this.data.getState());
				if(state == DownloadState.DOWNING){//下载=>暂停
					//通知后台下载服务暂停
					if(downloadService != null) downloadService.pauseDownload(this.data.getLessonId());
					//暂停
					state = DownloadState.PAUSE;
				}else{//暂停=>下载
					//通知后台下载服务继续
					if(downloadService != null) downloadService.continueDownload(this.data.getLessonId());
					//下载
					state = DownloadState.DOWNING;
				}
				//设置下载状态
				this.data.setState(state.getValue());
				//初始化
				final DownloadDao downloadDao = new DownloadDao();
				//更新数据到数据库 
				Log.d(TAG, "更新状态到数据库...");
				downloadDao.update(this.data); 
				//重新加载数据
				reloadData();
			}catch(Exception e){
				Log.e(TAG, "按钮点击操作时异常:" + e.getMessage(), e);
			}
		}
	}
	//下载UI更新处理
	private static class DowningUIHandler extends Handler{
		private final WeakReference<DownloadByFragmentDowning> fragmentDowningRef;
		/**
		 * 构造函数。
		 * @param adapter
		 */
		public DowningUIHandler(DownloadByFragmentDowning fragmentDowning){
			this.fragmentDowningRef = new WeakReference<DownloadByFragmentDowning>(fragmentDowning);
		}
		/*
		 * 重载消息处理。
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			final DownloadByFragmentDowning fragmentDowning = this.fragmentDowningRef.get();
			if(fragmentDowning == null) return;
			switch(msg.what){
				case Constant.HANLDER_WHAT_MSG:{//文本消息。
					final String message = (String)msg.obj;
					if(StringUtils.isNotBlank(message)){
						//显示消息
						Toast.makeText(AppContext.getContext(), message, Toast.LENGTH_SHORT).show();;
					}
					break;
				}
				case Constant.HANLDER_WHAT_PROGRESS://更新进度。 
				case Constant.HANLDER_WHAT_STATE:{//更新状态
					//重新加载数据。 
					fragmentDowning.reloadData();
					break;
				}
			}
		}
	}
}