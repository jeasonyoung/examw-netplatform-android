package com.examw.netschool.adapter;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.R;
import com.examw.netschool.customview.DownloadButton;
import com.examw.netschool.entity.DowningCourse;
import com.examw.netschool.service.DownloadService;
import com.examw.netschool.service.DownloadService.IFileDownloadService;
import com.examw.netschool.util.StringUtils;

/**
 * 正在下载列表数据适配器。
 * @author jeasonyoung
 *
 */
public class DowningListAdapter extends BaseAdapter {
	private static final String TAG = "DowningListAdapter";
	private Context context;
	private LayoutInflater layoutInflater;
	private AdapterServiceConnection connection = new AdapterServiceConnection();
	private IFileDownloadService downloadService;
	private List<DowningCourse> list;
	/*
	 * 构造函数。
	 */
	public DowningListAdapter(Context context, List<DowningCourse> list) {
		Log.d(TAG, "初始化构造函数...");
		this.list = list;
		this.context = context;
		//绑定服务。
		context.bindService(new Intent(this.context, DownloadService.class), this.connection, Context.BIND_AUTO_CREATE);
		//加载布局
		this.layoutInflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}
	/*
	 * 获取数据总数。
	 * @see android.widget.Adapter#getCount()
	 */
	@Override
	public int getCount() {
		return list.size();
	}
	/*
	 * 获取指定行数据。
	 * @see android.widget.Adapter#getItem(int)
	 */
	@Override
	public Object getItem(int position) {
		return list.get(position);
	}
	/*
	 * 获取指定行ID。
	 * @see android.widget.Adapter#getItemId(int)
	 */
	@Override
	public long getItemId(int position) {
		return position;
	}
	/*
	 * 创建行UI。
	 * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Log.d(TAG, "开始创建行:"+position+"...");
		TextView tvFileName,tvDowning,tvPercent,tvConnecting;
		ProgressBar progressBar;
		DownloadButton btnPause;
		
		DowningItemContentWrapper wrapper = null;
		if(convertView == null){
			Log.d(TAG, "创建新的行：" + position);
			convertView = this.layoutInflater.inflate(R.layout.list_downing_layout, null);
			//文件名
			tvFileName = (TextView)convertView.findViewById(R.id.filenameLab);
			//进度条
			progressBar = (ProgressBar) convertView.findViewById(R.id.finishProgress);
			progressBar.setMax(100);
			//下载中
			tvDowning = (TextView) convertView.findViewById(R.id.fileDownText);
			//百分数
			tvPercent = (TextView) convertView.findViewById(R.id.fileFininshProgressLab);
			//连接中
			tvConnecting = (TextView) convertView.findViewById(R.id.finishSizeTextView);
			//暂停或继续按钮
			btnPause = (DownloadButton) convertView.findViewById(R.id.pauseBtn);
			//创建包装对象
			wrapper = new DowningItemContentWrapper(tvFileName, tvDowning, tvPercent, tvConnecting, btnPause, progressBar);
			//存储包装对象
			convertView.setTag(wrapper);
		}else {
			Log.d(TAG, "重复利用行:" + position);
			//获取包装对象
			wrapper = (DowningItemContentWrapper)convertView.getTag();
			//文件名
			tvFileName = wrapper.fileName;
			//进度条
			progressBar = wrapper.progressBar;
			//下载中
			tvDowning = wrapper.downing;
			//百分数
			tvPercent = wrapper.percent;
			//连接中
			tvConnecting = wrapper.connecting;
			//暂停或继续按钮
			btnPause = wrapper.pause;
		}
		Log.d(TAG, "开始装载行["+position+"]数据...");
		DowningCourse course = (DowningCourse)this.getItem(position);
		//文件名
		tvFileName.setText(course.getCourseName());
		//注册按钮事件
		btnPause.setOnClickListener(new ItemContentOnClickListener(wrapper,course, position));
		//进度百分比
		int percent = (int) (course.getFinishSize() * 100.0/ course.getFileSize());
		if(percent > 0){
			tvPercent.setText(percent + "%");
			progressBar.setProgress(percent);
		}
		switch(course.getState()){
			case DowningCourse.STATE_INIT:{//初始状态
				tvConnecting.setText("排队连接中...");
				tvDowning.setText("");// 还没开始下载
				btnPause.setEnabled(false);
				break;
			}
			case DowningCourse.STATE_NETFAIL:{//连接失败
				tvConnecting.setText("连接失败!");
				tvDowning.setText("");
				btnPause.setImageResource(R.drawable.retry);
				btnPause.setText(R.string.retry);
				btnPause.setEnabled(true);
				break;
			}
			case DowningCourse.STATE_PAUSE:{//暂停中
				tvConnecting.setText("");
				tvDowning.setText("暂停中");
				btnPause.setImageResource(R.drawable.continuedown);// 显示继续按钮
				btnPause.setText(R.string.continueDown);// 显示继续
				btnPause.setEnabled(true);
				break;
			}
			case DowningCourse.STATE_DOWNING:{//下载中
				tvConnecting.setText("");
				tvDowning.setText("下载中");
				
				if(percent == 0){
					tvPercent.setText("0%");
					progressBar.setProgress(0);
				}
				
				btnPause.setImageResource(R.drawable.pausedown);// 显示暂停按钮
				btnPause.setText(R.string.pauseDown);// 显示暂停
				btnPause.setEnabled(true);
				break;
			}
			case DowningCourse.STATE_WAITTING:{//排队中
				tvConnecting.setText("");
				tvDowning.setText("排队中");
				btnPause.setImageResource(R.drawable.waitdown);// 显示等待按钮
				btnPause.setText(R.string.waitDown);// 显示等待
				btnPause.setEnabled(false);
				break;
			}
			case DowningCourse.STATE_FINISH:{
				tvConnecting.setText("");
				tvDowning.setText("已完成");
				btnPause.setImageResource(R.drawable.pausedown);// 显示暂停按钮
				btnPause.setText(R.string.pauseDown);// 显示暂停
				btnPause.setEnabled(false);
			}
			default:{
				Log.e(TAG, position +"." + course.getCourseName() + ",state:" + course.getState() + ",未加载数据...");
				break;
			}
		}
		return convertView;
	}
	/**
	 * 下载行内容。
	 * @author jeasonyoung
	 */
	private final class DowningItemContentWrapper{
		public TextView fileName,downing,percent,connecting;
		public DownloadButton pause;
		public ProgressBar progressBar;
		/**
		 * 构造函数。
		 * @param fileName
		 * @param downing
		 * @param percent
		 * @param connecting
		 * @param pause
		 * @param progressBar
		 */
		public DowningItemContentWrapper(TextView fileName,TextView downing,TextView percent,TextView connecting,DownloadButton pause,ProgressBar progressBar){
			this.fileName = fileName;
			this.downing = downing;
			this.percent = percent;
			this.connecting = connecting;
			this.pause = pause;
			this.progressBar = progressBar;
		}
	}
	/**
	 * 下载行内容点击事件处理。
	 * @author jeasonyoung
	 *
	 */
	private class ItemContentOnClickListener implements View.OnClickListener{
		private DowningItemContentWrapper wrapper;
		private DowningCourse course;
		private int position;
		/**
		 * 构造函数。
		 * @param wrapper
		 * @param course
		 * @param position
		 */
		public ItemContentOnClickListener(DowningItemContentWrapper wrapper,DowningCourse course, int position) {
			this.wrapper = wrapper;
			this.course = course;
			this.position = position;
		}
		/*
		 * 实现点击事件
		 * @see android.view.View.OnClickListener#onClick(android.view.View)
		 */
		@Override
		public void onClick(View v) {
			DownloadButton btn = (DownloadButton)v;
			switch(course.getState()){
				case DowningCourse.STATE_NETFAIL:{//连接失败
					//重新连接
					Log.d(TAG, "重新连接课程:"+ this.position+"." + this.course.getCourseName());
					//继续
					if(downloadService != null){
						downloadService.continueDownload(course);
					}
					wrapper.connecting.setText("重新连接中..."); 
					btn.setEnabled(false);
					break;
				}
				case DowningCourse.STATE_PAUSE:{//暂停中
					//继续下载
					Log.d(TAG, "继续下载课程:"+ this.position+"." + this.course.getCourseName());
					if(downloadService != null){
						downloadService.continueDownload(course);
					}
					wrapper.connecting.setText("重启下载中...");
					btn.setEnabled(false);
					break;
				}
				case DowningCourse.STATE_DOWNING:{//下载中
					//暂停课程
					Log.d(TAG, "暂停下载课程:"+ this.position+"." + this.course.getCourseName());
					if(downloadService != null){
						downloadService.pauseDownload(course);
					}
					wrapper.connecting.setText("暂停下载中...");
					btn.setEnabled(false);
					break;
				}
				default:{
					Log.e(TAG, "课程["+position+"."+course.getCourseName()+"]按钮事件类型["+course.getState()+"]未设置处理函数！");
					break;
				}
			}
		}
	}
	
	/**
	 * 下载服务连接器。
	 * @author jeasonyoung
	 *
	 */
	private final class AdapterServiceConnection implements ServiceConnection{
		/*
		 * 服务连接。
		 * @see android.content.ServiceConnection#onServiceConnected(android.content.ComponentName, android.os.IBinder)
		 */
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			downloadService = (IFileDownloadService)service;
			 if(downloadService != null){
				 Log.d(TAG, "设置UI更新Handler...");
				 downloadService.setHandler(new UpdateUIHandler(DowningListAdapter.this));
				 if(list.size() == 0)return;
				 Log.d(TAG, "添加列表数据到下载队列...");
				 int index = 0;
				for(DowningCourse data : list){
					if(data != null) downloadService.addDownload(data, index);
					index++;
				}
				Log.d(TAG, "通知更新列表数据状态...");
				notifyDataSetChanged();
			 }
		}
		/*
		 * 服务断开。
		 * @see android.content.ServiceConnection#onServiceDisconnected(android.content.ComponentName)
		 */
		@Override
		public void onServiceDisconnected(ComponentName name) {
			 downloadService = null;
		}
	}
	
	/**
	 * 更新UI的Handler。
	 * @author jeasonyoung
	 *
	 */
 	private static final class UpdateUIHandler extends Handler{
		private Map<DowningCourse, Integer> coursePercentMap;
		private final WeakReference<DowningListAdapter> downingAdapter;
		/**
		 * 构造函数。
		 * @param adapter
		 */
		public UpdateUIHandler(DowningListAdapter adapter){
			this.coursePercentMap = new HashMap<DowningCourse, Integer>();
			this.downingAdapter = new WeakReference<DowningListAdapter>(adapter);
		}
		
		@Override
		public void handleMessage(Message msg) {
			DowningListAdapter adapter = this.downingAdapter.get();
			if(adapter == null)return;
			
			Integer pos = msg.arg1;
			switch(msg.what){
				case DowningCourse.STATE_NETFAIL:{//连接失败
					if(pos > -1 && pos < adapter.getCount()){
						Log.d(TAG, "更新连接失败UI...");
						DowningCourse data = adapter.list.get(pos);
						data.setState(DowningCourse.STATE_NETFAIL);
						//通知事件适配器
						adapter.notifyDataSetChanged();
					}
					break;
				}
				case DowningCourse.STATE_WAITTING:{//等待
					if(pos > -1 && pos < adapter.getCount()){
						Log.d(TAG, "更新连接等待UI...");
						DowningCourse data = adapter.list.get(pos);
						data.setState(DowningCourse.STATE_WAITTING);
						//通知事件适配器
						adapter.notifyDataSetChanged();
					}
					break;
				}
				case DowningCourse.STATE_PAUSE:{//暂停
					Log.d(TAG, "更新暂停连接失败UI...");
					if(pos > -1 && pos < adapter.getCount()){
						DowningCourse data = adapter.list.get(pos);
						data.setState(DowningCourse.STATE_PAUSE);
						//通知事件适配器
						adapter.notifyDataSetChanged();
					}
					break;
				}
				case DowningCourse.STATE_DOWNING:{//下载进度
					if(pos > -1 && pos < adapter.getCount()){
						DowningCourse data = adapter.list.get(pos);
						data.setState(DowningCourse.STATE_DOWNING);
						Long finishTotal = (Long)msg.obj;
						if(finishTotal != null && finishTotal > 0){
							//更新下载的数据
							data.setFinishSize(finishTotal);
							Integer oldPercent = this.coursePercentMap.get(data);							
							//新的百分比
							int newPercent = (int) (data.getFinishSize() * 100.0/ data.getFileSize());
							if(oldPercent == null || newPercent > oldPercent){
								this.coursePercentMap.put(data, newPercent);
								//通知事件适配器
								adapter.notifyDataSetChanged();
								//
								Log.d(TAG, "更新下载进度:"+newPercent+"%("+data.getFinishSize()+"/"+data.getFileSize()+")...");
							}
						}
					}
					return;
				}
				case DowningCourse.STATE_FINISH:{//下载完成
					Log.d(TAG, "下载完成...");
					if(pos > -1 && pos < adapter.getCount()){
						DowningCourse data = adapter.list.get(pos);
						data.setState(DowningCourse.STATE_FINISH);
						//通知UI更新适配器
						adapter.notifyDataSetChanged(); 
					}
					break;
				}
			}
			//通知提示
			if((msg.obj instanceof String)){
				String content = (String)msg.obj;
				if(StringUtils.isEmpty(content))return;
				
				Toast.makeText(adapter.context, content, Toast.LENGTH_LONG).show();
			}
		}
	}
}