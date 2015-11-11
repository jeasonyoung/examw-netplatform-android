package com.examw.netschool;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.DownloadDao;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.dao.PlayRecordDao;
import com.examw.netschool.model.Download;
import com.examw.netschool.model.Download.DownloadState;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.model.PlayRecord;
import com.examw.netschool.util.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import io.vov.vitamio.LibsChecker;
import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;
/**
 * 视频播放Activity基础类
 * @author jeasonyoung
 *
 */
public class VideoPlayActivity extends Activity /*implements OnTouchListener, OnGestureListener*/ {
	private static final String TAG = "VideoPlayActivity";
	private static final int VIDEO_STEP_SPEED = 5000;
	
	private PopupWindow topBar,footerBar;
	
	private SeekBar videoBar,volumnBar;
	private TextView tvTitle,tvCurrentTime,tvTotalTime,tvVolumnSize;
	
	private ImageButton btnPlay;
	private VideoView playVideoView;
	
	private Timer timer;
	private View videoLoadingView;
	private GestureDetector gestureDetector;
	
	private int volumnMax,currentPlayTimeBySecond;
	private String lessonId, lessonName,recordId;
	
	private AutoUpdateVideoSeekHandler autoUpdateVideoSeekHandler;
	/*
	 * 重载创建
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "重载创建...");
		//检查播放器依赖库
		if(!LibsChecker.checkVitamioLibs(this)) return;
		//设置屏幕常亮(必须在setContentView之前)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
													WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		//设置内容布局
		this.setContentView(R.layout.activity_play_video);
		//加载传递的数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//当前用户ID
			//this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//当前课程资源ID
			this.lessonId = intent.getStringExtra(Constant.CONST_LESSON_ID);
			//当前课程资源名称
			this.lessonName = intent.getStringExtra(Constant.CONST_LESSON_NAME);
			//当前播放记录ID
			this.recordId = intent.getStringExtra(Constant.CONST_LESSON_RECORD_ID);
		}
		//加载播放器
		this.createVideoView();
		//初始化布局化
		final LayoutInflater inflater = LayoutInflater.from(this);
		//音频控制栏
		this.createVolumnControlBar(inflater);
		//视频控制栏
		this.createVideoControlBar(inflater);
		//加载popupwindow
		this.createPopupWindows();
		//创建手势检测器
		this.createGestureDetector();
		//初始化更新工具栏
		this.autoUpdateVideoSeekHandler = new AutoUpdateVideoSeekHandler(this);
		//
		super.onCreate(savedInstanceState);
	}
	//加载视频播放View
	@SuppressLint("ClickableViewAccessibility")
	private void createVideoView(){
		Log.d(TAG, "加载视频播放View...");
		//播放器
		this.playVideoView = (VideoView)this.findViewById(R.id.play_video_view);
		//设置触摸事件监听
		this.playVideoView.setOnTouchListener(this.onTouchListener);
		//设置启用长按响应
		this.playVideoView.setLongClickable(true);
		//设置焦点启用
		this.playVideoView.setFocusable(true);
		//设置点击启用
		this.playVideoView.setClickable(true);
		//视频加载进度
		this.videoLoadingView = this.findViewById(R.id.video_loading_view);
		//显示加载进度
		this.videoLoadingView.setVisibility(View.VISIBLE);
	}
	//加载音频控制工具栏。
	private void createVolumnControlBar(LayoutInflater inflater){
		Log.d(TAG, "加载音频控制工具栏...");
		final View titleView = inflater.inflate(R.layout.activity_play_video_volumn_control, null);
		titleView.getBackground().setAlpha(0);
		//播放视频名称
		this.tvTitle = (TextView)titleView.findViewById(R.id.txt_title);
		//返回按钮
		final View btnReturn =  titleView.findViewById(R.id.btn_return);
		btnReturn.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) { 
				backRecord(); 
				finish(); 
			} 
		});
		//音量拖拽条
		this.volumnBar = (SeekBar)titleView.findViewById(R.id.seek_bar_vol);
		//音量百分数
		this.tvVolumnSize = (TextView)titleView.findViewById(R.id.tv_vol_size);
		//获取上下文字
		final AppContext appContext = (AppContext)this.getApplicationContext();
		//获取音频管理
		final AudioManager audioManager = appContext.getAudioManager();
		if(audioManager != null){
			//音量最大值
			this.volumnMax = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			//设置音量
			this.volumnBar.setMax(this.volumnMax);
			//当前音量
			final int current = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
			//设置进度
			this.volumnBar.setProgress(current);
			//显示音量百分数
			this.tvVolumnSize.setText((current * 100 / volumnMax) + "%");
		}
		//设置音量控制器
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		//设置拖拽事件
		this.volumnBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
			    final  int vol = seekBar.getProgress();
				if(audioManager != null) audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, vol, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
				if(tvVolumnSize != null) tvVolumnSize.setText((vol * 100 / seekBar.getMax()) + "%");
			}
			
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) { }
		});
		//
		this.topBar = new PopupWindow(titleView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.topBar.setAnimationStyle(R.style.AnimationFade);
	}
	//加载视频控制工具栏。
	private void createVideoControlBar(LayoutInflater inflater){
		final View barView = inflater.inflate(R.layout.activity_play_video_video_control, null);
		//视频进度条
		this.videoBar = (SeekBar)barView.findViewById(R.id.seek_bar_progress);
		//设置进度条更新事件监听
		this.videoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int pos = 0;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//视频位置seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
				if(playVideoView != null){  playVideoView.seekTo(this.pos); }
				//更改播放时间显示
				if(tvCurrentTime != null){ tvCurrentTime.setText(Utils.getTime(this.pos / 1000)); }
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(playVideoView != null){ 
					this.pos = (int)(progress * playVideoView.getDuration() / seekBar.getMax()); 
					Log.d(TAG, "视频进度条滚动位置["+progress+"]=>对应视频位置["+this.pos+"]....");
				}
			}
		});
		//播放按钮
		this.btnPlay = (ImageButton)barView.findViewById(R.id.btn_play);
		//播放按钮事件
		this.btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "播放按钮事件处理..." + v);
				if(playVideoView == null) return;
				int bgResId,imgResId;
				if(playVideoView.isPlaying()){//播放中=>暂停
					//暂停
					videoPause();
					//资源
					bgResId = R.drawable.play_video_video_control_play_bg;
					imgResId = R.drawable.play_video_video_control_play_icon;
				}else{//暂停=>播放
					//播放
					videoPlay();
					//资源
					bgResId = R.drawable.play_video_video_control_pause_bg;
					imgResId = R.drawable.play_video_video_control_pause_icon;
				}
				//更换图片
				if(btnPlay != null){
					//设置背景
					btnPlay.setBackgroundResource(bgResId);
					//设置图片
					btnPlay.setImageResource(imgResId);
				}
			}
		});
		//视频回退。
		final View btnPrev = barView.findViewById(R.id.btn_previous);
		btnPrev.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) { 
				Log.d(TAG, "准备视频回退...");
				if(playVideoView == null) return;
				final long pos = playVideoView.getCurrentPosition() - VIDEO_STEP_SPEED;
				if(pos >= 0){
					//设置视频位置
					playVideoView.seekTo(pos);
					//更新视频工具栏
					if(autoUpdateVideoSeekHandler != null){
						autoUpdateVideoSeekHandler.sendEmptyMessage(0);
					}
				}
			}
		});
		//视频快进
		final View btnNext = barView.findViewById(R.id.btn_next);
		btnNext.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) {
				Log.d(TAG, "准备视频快进...");
				if(playVideoView == null) return;
				final long pos = playVideoView.getCurrentPosition(), total = playVideoView.getDuration();
				if(pos + VIDEO_STEP_SPEED <= total){
					//设置视频位置
					playVideoView.seekTo(pos + VIDEO_STEP_SPEED);
					//更新视频工具栏
					if(autoUpdateVideoSeekHandler != null){
						autoUpdateVideoSeekHandler.sendEmptyMessage(0);
					}
				}
			} 
		});
		//播放总时长
		this.tvTotalTime = (TextView)barView.findViewById(R.id.tv_total_time);
		//当前播放时间
		this.tvCurrentTime = (TextView)barView.findViewById(R.id.tv_play_time);
		
		//工具栏
		this.footerBar = new PopupWindow(barView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.footerBar.setAnimationStyle(R.style.AnimationFade);
	}
	//加载popupwindow
	private void createPopupWindows(){
		Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
			@Override
			public boolean queueIdle() {
				//显示两个popupwindow
				if(topBar != null && playVideoView.isShown()){
					Log.d(TAG, "显示音频控制栏....");
					showTopBar();
				}
				if(footerBar != null && playVideoView.isShown()){
					Log.d(TAG, "显示视频控制栏...");
					showFooterBar();
				}
				//显示正在加载
				return false;
			}
		});
	}
	//创建手势检测器。
	private void createGestureDetector(){
		Log.d(TAG, "创建手势检测器...");
		this.gestureDetector = new GestureDetector(this, new OnGestureListener() {
			@Override
			public boolean onSingleTapUp(MotionEvent e) { return false; }
			@Override
			public void onShowPress(MotionEvent e) { }
			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }
			@Override
			public void onLongPress(MotionEvent e) { }
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
			/*
			 * 轻触屏幕，控制条出现或者消失
			 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
			 */
			@Override
			public boolean onDown(MotionEvent e) {
				Log.d(TAG, "轻触屏幕，控制条出现或者消失...");
				//顶部工具栏
				if(topBar != null){
					if(topBar.isShowing()){//显示=>隐藏
						topBar.dismiss();
					}else {//隐藏=>显示
						showTopBar();
					}
				}
				//底部工具栏
				if(footerBar != null){
					if(footerBar.isShowing()){//显示=>隐藏
						footerBar.dismiss();
					}else {//隐藏=>显示
						showFooterBar();
					}
				}
				return true;
			}
		});
		//
		gestureDetector.setIsLongpressEnabled(true);
	}
	/*
	 * 重置启动。
	 * @see android.app.Activity#onStart()
	 */
	@Override
 	protected void onStart() {
		Log.d(TAG, "重置启动...");
		//异步加载数据
		new AsyncTask<Void, Void, Object>(){
			/*
			 * 后台线程加载数据。
			 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
			 */
			@Override
			protected Object doInBackground(Void... params) {
				try{
					Log.d(TAG, "后台线程加载数据处理...");
					//初始化
					final PlayRecordDao playRecordDao = new PlayRecordDao();
					//设置当前播放时间
					currentPlayTimeBySecond = 0;
					//如果存在播放记录
					if(StringUtils.isNotBlank(recordId)){
						Log.d(TAG, "根据播放记录["+recordId+"]加载数据....");
						final PlayRecord record = playRecordDao.getPlayRecord(recordId);
						if(record == null || StringUtils.isBlank(record.getLessonId())){
							Log.e(TAG, "播放记录["+recordId+"]不存在!");
							return null;
						}
						//播放位置
						currentPlayTimeBySecond = record.getPlayTime();
						//设置课程ID
						lessonId = record.getLessonId();
						lessonName = record.getLessonName();
					}else{//如果不存在播放记录ID则新增播放记录
						Log.d(TAG, "新增课程资源["+ lessonId +"]播放记录...");
						recordId = playRecordDao.add(lessonId);
					}
					//课程资源ID不存在
					if(StringUtils.isBlank(lessonId)){
						Log.e(TAG, "课程资源ID不存在!");
						return null;
					}
					//检查视频是否被下载,优先加载下载到本地的数据
					final DownloadDao downloadDao = new DownloadDao();
					if(downloadDao.hasDownload(lessonId)){
						Log.d(TAG, "检查是否存在本地视频可以播放...");
						final Download download = downloadDao.getDownload(lessonId);
						if(download != null && download.getState() == DownloadState.FINISH.getValue()){
							lessonName = download.getLessonName();
							final File file = new File(download.getFilePath());
							if(file.exists()){
								///TODO:解密视频
								Log.d(TAG, "播放本地视频:" + file.getAbsolutePath());
								return Uri.parse(file.getAbsolutePath());
							}
						}
					}
					//加载课程资源。
					final LessonDao lessonDao = new LessonDao();
					final Lesson lesson = lessonDao.getLesson(lessonId);
					if(lesson == null){
						Log.e(TAG, "课程资源["+lessonId+"]不存在!");
						return null;
					}
					//设置课程名称
					lessonName = lesson.name;
					//获取优先视频URL
					final String url = lesson.getPriorityUrl();
					Log.d(TAG, "video-url:" + url);
					//检查网络状态
					final AppContext appContext = (AppContext)getApplicationContext();
					if(appContext != null && !appContext.isNetworkConnected()){
						Log.d(TAG, "未连接网络!");
						return "未连接网络!";
					}
					return Uri.parse(url);
				}catch(Exception e){
					Log.e(TAG, "后台线程加载数据异常:" + e.getMessage(), e);
				}
				return null;
			}
			/*
			 * 前台主线更新数据。
			 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
			 */
			@Override
			protected void onPostExecute(Object result) {
				Log.d(TAG, "前台主线程处理...");
				//设置标题
				if(tvTitle != null) tvTitle.setText(lessonName);
				//无反馈
				if(result == null || (result instanceof String)){
					//反馈为消息
					if(result instanceof String){
						final String msg = (String)result;
						if(StringUtils.isNotBlank(msg)) Toast.makeText(VideoPlayActivity.this, msg, Toast.LENGTH_SHORT).show();
					}
					//关闭activity
					finish();
					//
					return;
				}
				if(result instanceof Uri){
					//设置播放地址
					playVideoView.setVideoURI((Uri)result);
					//设置播放缓冲事件监听
					playVideoView.setOnBufferingUpdateListener(onBufferingUpdateListener);
					//设置播放预备事件监听
					playVideoView.setOnPreparedListener(onPreparedListener);
					//设置播放完成事件监听
					playVideoView.setOnCompletionListener(onCompletionListener);
					//设置焦点
					playVideoView.requestFocus();
					return;
				}
				//关闭activity
				finish();
			}
			
		}.execute((Void)null);
		//
		super.onStart();
	}
	
	//触摸事件处理。
	private OnTouchListener onTouchListener = new OnTouchListener() {
		@SuppressLint("ClickableViewAccessibility")
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			//
			return gestureDetector.onTouchEvent(event);
		}
	};
	
	//播放缓冲事件处理
	private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			Log.d(TAG, "缓冲视频进度..." + percent);
			if(videoBar != null) videoBar.setSecondaryProgress(percent); 
		}
	};
	
	//播放预处理事件处理
	private OnPreparedListener onPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "预备播放事件处理...");
			//隐藏加载
			if(videoLoadingView != null) videoLoadingView.setVisibility(View.GONE);
			//获取视频总播放时间
			final long duration = mp.getDuration() / 1000;
			//设置播放总时间
			if(tvTotalTime != null) tvTotalTime.setText(Utils.getTime(duration));
			//设置播放速度
			mp.setPlaybackSpeed(1.0f);
			//设置开始播放位置
			if(currentPlayTimeBySecond > 0 && currentPlayTimeBySecond < duration - 10){
				//设置进度条
				if(videoBar != null) videoBar.setProgress((int)((currentPlayTimeBySecond * videoBar.getMax()) / duration));
				//设置播放位置
				mp.seekTo(currentPlayTimeBySecond * 1000);
			}else{
				//设置进度条
				if(videoBar != null) videoBar.setProgress(0);
				//设置播放位置
				mp.seekTo(0);
			}
			//播放
			if(!mp.isPlaying()){
				Log.d(TAG, "开始播放视频...");
				mp.start();
			}
		}
	};
	
	//播放完成处理
	private OnCompletionListener onCompletionListener = new OnCompletionListener(){
		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, "播放完成...");
			//设置视频进度
			if(videoBar != null) videoBar.setProgress(0);
			//播放时间
			if(tvCurrentTime != null) tvCurrentTime.setText("00:00");
			//播放器
			if(playVideoView != null){
				playVideoView.seekTo(0);
				playVideoView.pause();
			}
			//进度更新定时器
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			//显示顶部工具栏
			if(topBar != null && !topBar.isShowing()){
				//显示音频控制栏
				showTopBar();
			}
			//显示底部工具栏
			if(footerBar != null && !footerBar.isShowing()){
				//替换播放按钮图片
				if(btnPlay != null){
					//背景图
					btnPlay.setBackgroundResource(R.drawable.play_video_video_control_play_bg);
					//按钮图
					btnPlay.setImageResource(R.drawable.play_video_video_control_play_icon);
				}
				//设置进度条
				if(videoBar != null) videoBar.setProgress(0);
				//播放时间
				if(tvCurrentTime != null) tvCurrentTime.setText("00:00");
				//显示视频控制栏
				showFooterBar();
			}
		}
	};
	
	//视频播放
	private synchronized void videoPlay(){
		Log.d(TAG, "视频播放...");
		if(this.playVideoView == null) return;
		//开始播放
		this.playVideoView.start();
		//启动定时器
		if(this.timer == null){
			//初始化定时器
			this.timer = new Timer();
			//定时任务
			this.timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try{
						if(playVideoView != null && playVideoView.isPlaying() && videoBar != null && !videoBar.isPressed() && autoUpdateVideoSeekHandler != null){
							autoUpdateVideoSeekHandler.sendEmptyMessage(0);
						}
					}catch(Exception e){
						Log.e(TAG, "定时器执行异常:" + e.getMessage(), e);
					}
				}
			}, 500, 1000);
		}
	}
	
	//暂停播放
	//视频播放暂停
	private synchronized void videoPause(){
		Log.d(TAG, "播放暂停...");
		if(this.playVideoView != null){
			//暂停
			this.playVideoView.pause();
			//更新到数据库
			this.asyncUpdatePlayRecord();
		}
	}
	
	//显示顶部工具栏
	private void showTopBar(){
		if(this.topBar != null && this.playVideoView != null){
			Log.d(TAG, "显示顶部工具栏...");
			this.topBar.showAtLocation(this.playVideoView, Gravity.TOP, 0, 0);
			this.topBar.update();
		}
	}
	
	//显示底部工具栏
	private void showFooterBar(){
		if(this.footerBar != null && this.playVideoView != null){
			Log.d(TAG, "显示底部工具栏...");
			this.footerBar.showAtLocation(this.playVideoView, Gravity.BOTTOM, 0, 0);
			this.footerBar.update();
		}
	}
	
	//返回按钮的监听方式实现
	private void backRecord(){
			Log.d(TAG, "返回按钮监听实现...");
			//关闭工具栏
			if(this.topBar != null) this.topBar.dismiss();
			if(this.footerBar != null) this.footerBar.dismiss();
			//更新播放记录
			this.asyncUpdatePlayRecord();
			//停止播放
			if(this.playVideoView != null){
				Log.d(TAG, "准备停止播放...");
				this.playVideoView.stopPlayback();
				Log.d(TAG, "已停止播放");
			}
	}
	
	//异步线程更新播放记录时间
	private void asyncUpdatePlayRecord(){
		if(StringUtils.isBlank(this.recordId) || this.playVideoView == null) return;
		final int pos = (int)(playVideoView.getCurrentPosition() / 1000);
		if(pos <= 0)return;
		//异步线程处理返回保存数据
		AppContext.pools_fixed.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "异步线程更新播放记录时间...");
					//初始化
					final PlayRecordDao playRecordDao = new PlayRecordDao();
					playRecordDao.updatePlayTime(recordId,  Integer.valueOf(pos));
				} catch (Exception e) {
					Log.e(TAG, "更新播放记录异常:" + e.getMessage(), e);
				}
			}
		});
	}
	
	/*
	 * 按键事件处理。
	 * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
			case KeyEvent.KEYCODE_VOLUME_UP://音量增大
			case KeyEvent.KEYCODE_VOLUME_DOWN://音量减小
			{
				final AppContext appContext = (AppContext)this.getApplicationContext();
				final AudioManager audioManager = appContext.getAudioManager();
				if(audioManager != null ){
					Log.d(TAG, "音量增大...");
					//获取当前音量
					final int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
					final int newVolume = (keyCode == KeyEvent.KEYCODE_VOLUME_UP) ? currentVolume + 1 : currentVolume - 1;
					if(newVolume >= 0){
						//增大音量
						audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						//显示音量
						if(this.topBar != null && this.topBar.isShowing()){
							//进度条
							if(this.volumnBar != null) this.volumnBar.setProgress(newVolume);
							//百分比
							if(this.tvVolumnSize != null && newVolume < this.volumnMax){
								this.tvVolumnSize.setText((newVolume * 100 / this.volumnMax) + "%");
							}
						}
					}
				}
				break;
			}
			case KeyEvent.KEYCODE_BACK:{//返回
				return super.onKeyDown(keyCode, event);
			}
			default: break;
		}
		return true;
	}
	
	/*
	 * 重载恢复处理。
	 * @see android.app.Activity#onResume()
	 */
	@Override
	protected void onResume() {
		//播放
		this.videoPlay();
		//
		super.onResume();
	}
	
	/*
	 * 重载暂停处理。
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		//播放暂停
		this.videoPause();
		//
		super.onPause();
	}
	
	/*
	 * 重载停止处理。
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		//
		this.backRecord();
		//
		super.onStop();
	}
	/*
	 * 重载销毁处理。
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		///TODO:
//		if(this.videoUri != null && !StringUtils.isEmpty(this.username) &&  
//				ContentResolver.SCHEME_FILE.equals(this.videoUri.getScheme()) && this.videoUri.getPath().indexOf(this.username) > -1){
//			Log.d(TAG, "开始加密...");
//			File file = new File(this.videoUri.getPath());
//			if(file.exists()){
//				 try {
//					 MultiThreadDownload.encryptFile(file, 0, this.username.getBytes("UTF-8"));
//					 Log.d(TAG, "加密文件完成");
//				} catch (Exception e) {
//					Log.e(TAG, "加密时发生异常:" + e.getMessage(), e);
//				}
//			}
//		}
//		//this.endFlag = true;
//		if(this.title != null && this.title.isShowing()){
//			this.title.dismiss();
//		}
//		if(this.toolbar != null && this.toolbar.isShowing()){
//			this.toolbar.dismiss();
//		}
		super.onDestroy();
	}
	
	//自动更新视频工具栏消息数据处理。
	private static class AutoUpdateVideoSeekHandler extends Handler{
		private WeakReference<VideoPlayActivity> reference;
		/**
		 * 构造函数。
		 * @param activity
		 */
		public AutoUpdateVideoSeekHandler(VideoPlayActivity activity){
			this.reference = new WeakReference<VideoPlayActivity>(activity);
		}
		/*
		 * 消息处理。
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			try{
				final VideoPlayActivity activity;
				if(msg.what == 0 && (activity = this.reference.get()) != null){
					if(activity.playVideoView != null && activity.videoBar != null && activity.tvCurrentTime != null){
						final long pos = activity.playVideoView.getCurrentPosition(), total = activity.playVideoView.getDuration();
						if(pos >= 0 && total > 0){
							//设置视频进度条
							activity.videoBar.setProgress((int)((activity.videoBar.getMax() * pos) / total));
							//设置当前播放时间
							activity.tvCurrentTime.setText(Utils.getTime(pos / 1000));
						}
					}
				}
				super.handleMessage(msg);
			}catch(Exception e){
				Log.e(TAG, "自动更新视频工具栏异常:" + e.getMessage(), e);
			}
		}
	}	
	
//	//播放文件解密操作
//	private AsyncTask<Object, Integer, Uri> asyncVideoTask = new AsyncTask<Object, Integer, Uri> (){
//		/*
//		 * 解密播放文件
//		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
//		 */
//		@Override
//		protected Uri doInBackground(Object... params) {
//			Log.d(TAG, "开始处理视频加/解密...");
//			Uri uri = (Uri)params[0];
//			String userName = (String)params[1];
//			if(uri != null && !StringUtils.isEmpty(userName) &&  
//					ContentResolver.SCHEME_FILE.equals(uri.getScheme()) && uri.getPath().indexOf(userName) > -1){
//				File file = new File(uri.getPath());
//				if(file.exists() && !isDecrypted ){
//					 try {
//						 Log.d(TAG, "开始加/解密文件...");
//						 byte[] keys = userName.getBytes("UTF-8");
//						 //解密(异或运算)
//						 MultiThreadDownload.encryptFile(file, 0, keys);
//						 //
//						 isDecrypted = true;
//					} catch (Exception e) {
//						Log.e(TAG, "加/解密时发生异常:" + e.getMessage(), e);
//					}
//				}
//			}
//			return uri;
//		}
//		/*
//		 * 更新UI线程
//		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
//		 */
//		protected void onPostExecute(Uri result) {
//			//player = new VitamioVideoPlayer(VideoPlayActivity.this, videoView, seekBar, tvCurrentTime, tvTotalTime, recordTime, videoLoadingLayout, result, username);
//		};
//	};

}