package com.examw.netschool;

import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang3.StringUtils;

import com.examw.netschool.app.AppContext;
import com.examw.netschool.app.Constant;
import com.examw.netschool.dao.LessonDao;
import com.examw.netschool.dao.PlayRecordDao;
import com.examw.netschool.model.Lesson;
import com.examw.netschool.model.PlayRecord;

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
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
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
public class VideoPlayActivity extends Activity implements OnTouchListener, OnGestureListener {
	private static final String TAG = "VideoPlayActivity";
	private static final int VIDEO_STEP_SPEED = 5000;
	
	private PopupWindow topBar,footerBar;
	private SeekBar videoBar,volumnBar;
	private TextView tvTitle,tvCurrentTime,tvTotalTime,tvVolumnSize;
	private ImageButton btnReturn,btnPrev,btnNext,btnPlay;
	private VideoView videoView;
	
	private Timer timer;
	private RelativeLayout videoLoadingLayout;
	private GestureDetector gestureDetector;
	
	private AppContext appContext;
	
	private int volumnMax,currentPlayTimeBySecond;
	private String userId, lessonId, lessonName,recordId;
	
	private AutoUpdateVideoSeekHandler autoUpdateVideoSeekHandler;
	
	private PlayRecordDao playRecordDao;
	/*
	 * 重载创建
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "初始化...");
		//检查播放器依赖库
		if(!LibsChecker.checkVitamioLibs(this)) return;
		
		//设置屏幕常亮(必须在setContentView之前)
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, 
													WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		//设置内容布局
		this.setContentView(R.layout.videoview);
		
		//初始化
		this.appContext = (AppContext)this.getApplication();
		
		//加载传递的数据
		final Intent intent = this.getIntent();
		if(intent != null){
			//当前用户ID
			this.userId = intent.getStringExtra(Constant.CONST_USERID);
			//当前课程资源ID
			this.lessonId = intent.getStringExtra(Constant.CONST_LESSON_ID);
			//当前课程资源名称
			this.lessonName = intent.getStringExtra(Constant.CONST_CLASS_NAME);
			//当前播放记录ID
			this.recordId = intent.getStringExtra(Constant.CONST_LESSON_RECORD_ID);
		}
		//加载播放器
		this.loadVideoView();
		//
		final LayoutInflater inflater = LayoutInflater.from(this);
		//音频控制栏
		this.loadVolumnControlBar(inflater);
		//视频控制栏
		this.loadVideoControlBar(inflater);
		//加载popupwindow
		this.loadPopupWindows();
		//
		this.gestureDetector = new GestureDetector(this, this);
		this.gestureDetector.setIsLongpressEnabled(true);
		//初始化更新工具栏
		this.autoUpdateVideoSeekHandler = new AutoUpdateVideoSeekHandler(this);
		//异步加载数据
		new AsynLoadVideoData().execute((Void)null);
	}
	//加载视频播放View
	@SuppressLint("ClickableViewAccessibility")
	private void loadVideoView(){
		Log.d(TAG, "加载视频播放View...");
		//播放器
		this.videoView = (VideoView)this.findViewById(R.id.surface_view);
		//设置触摸事件监听
		this.videoView.setOnTouchListener(this);
		//设置启用长按响应
		this.videoView.setLongClickable(true);
		//设置焦点启用
		this.videoView.setFocusable(true);
		//设置点击启用
		this.videoView.setClickable(true);
		//视频加载进度
		this.videoLoadingLayout = (RelativeLayout)this.findViewById(R.id.videoloadingLayout);
		//显示加载进度
		this.videoLoadingLayout.setVisibility(View.VISIBLE);
	}
	//设置视频播放
		private void setVideoView(String url){
			Log.d(TAG, "加载视频播放View...");
			//URL不存在
			if(StringUtils.isBlank(url)){
				Log.d(TAG, "URL为空!");
				//关闭Activity
				finish();
				return;
			}
			//设置播放地址
			videoView.setVideoURI(Uri.parse(url));
			//设置播放缓冲事件监听
			videoView.setOnBufferingUpdateListener(this.onBufferingUpdateListener);
			//设置播放预备事件监听
			videoView.setOnPreparedListener(this.onPreparedListener);
			//设置播放完成事件监听
			videoView.setOnCompletionListener(this.onCompletionListener);
			//设置焦点
			videoView.requestFocus();
		}
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
			if(videoLoadingLayout != null) videoLoadingLayout.setVisibility(View.GONE);
			//获取视频总播放时间
			final long duration = mp.getDuration() / 1000;
			//设置播放总时间
			if(tvTotalTime != null) tvTotalTime.setText(getTime(duration));
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
			if(videoView != null){
				videoView.seekTo(0);
				videoView.pause();
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
					btnPlay.setBackgroundResource(R.drawable.play_button);
					//按钮图
					btnPlay.setImageResource(R.drawable.player_play);
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
	//转换为时间字符串(MM:ss)
	private static String getTime(long time) {
		return StringUtils.leftPad(String.valueOf(time / 60), 2, '0') + ":" + StringUtils.leftPad(String.valueOf(time % 60), 2, '0'); 
	}
	//加载音频控制工具栏。
	private void loadVolumnControlBar(LayoutInflater inflater){
		Log.d(TAG, "加载音频控制工具栏...");
		final View titleView = inflater.inflate(R.layout.volumn_control, null);
		titleView.getBackground().setAlpha(0);
		//播放视频名称
		this.tvTitle = (TextView)titleView.findViewById(R.id.videoName);
		//返回按钮
		this.btnReturn =  (ImageButton)titleView.findViewById(R.id.imageBack);
		this.btnReturn.setOnClickListener(new OnClickListener() { @Override public void onClick(View v) { backRecord(); finish(); } });
		//音量拖拽条
		this.volumnBar = (SeekBar)titleView.findViewById(R.id.seekBar1);
		//音量百分数
		this.tvVolumnSize = (TextView)titleView.findViewById(R.id.volumnSize);
		//获取音频管理
		final AudioManager audioManager = this.appContext.getAudioManager();
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
	private void loadVideoControlBar(LayoutInflater inflater){
		final View barView = inflater.inflate(R.layout.video_control, null);
		//视频进度条
		this.videoBar = (SeekBar)barView.findViewById(R.id.seekBar);
		//设置进度条更新事件监听
		this.videoBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			int pos = 0;
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				//视频位置seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
				if(videoView != null){  videoView.seekTo(this.pos); }
				//更改播放时间显示
				if(tvCurrentTime != null){ tvCurrentTime.setText(getTime(this.pos / 1000)); }
			}
			@Override
			public void onStartTrackingTouch(SeekBar seekBar) { }
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if(videoView != null){ 
					this.pos = (int)(progress * videoView.getDuration() / seekBar.getMax()); 
					Log.d(TAG, "视频进度条滚动位置["+progress+"]=>对应视频位置["+this.pos+"]....");
				}
			}
		});
		//播放按钮
		this.btnPlay = (ImageButton)barView.findViewById(R.id.imagePlay);
		//播放按钮事件
		this.btnPlay.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d(TAG, "播放按钮事件处理..." + v);
				if(videoView == null) return;
				int bgResId,imgResId;
				if(videoView.isPlaying()){//播放中=>暂停
					//暂停
					videoPause();
					//资源
					bgResId = R.drawable.play_button;
					imgResId = R.drawable.player_play;
				}else{//暂停=>播放
					//播放
					videoPlay();
					//资源
					bgResId = R.drawable.pause_button;
					imgResId = R.drawable.player_pause;
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
		this.btnPrev = (ImageButton)barView.findViewById(R.id.imagePrevious);
		this.btnPrev.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) { 
				Log.d(TAG, "准备视频回退...");
				if(videoView == null) return;
				final long pos = videoView.getCurrentPosition() - VIDEO_STEP_SPEED;
				if(pos >= 0){
					//设置视频位置
					videoView.seekTo(pos);
					//更新视频工具栏
					if(autoUpdateVideoSeekHandler != null){
						autoUpdateVideoSeekHandler.sendEmptyMessage(0);
					}
				}
			}
		});
		//视频快进
		this.btnNext = (ImageButton)barView.findViewById(R.id.imageNext);
		this.btnNext.setOnClickListener(new OnClickListener() { 
			@Override 
			public void onClick(View v) {
				Log.d(TAG, "准备视频快进...");
				if(videoView == null) return;
				final long pos = videoView.getCurrentPosition(), total = videoView.getDuration();
				if(pos + VIDEO_STEP_SPEED <= total){
					//设置视频位置
					videoView.seekTo(pos + VIDEO_STEP_SPEED);
					//更新视频工具栏
					if(autoUpdateVideoSeekHandler != null){
						autoUpdateVideoSeekHandler.sendEmptyMessage(0);
					}
				}
			} 
		});
		//播放总时长
		this.tvTotalTime = (TextView)barView.findViewById(R.id.totalTime);
		//当前播放时间
		this.tvCurrentTime = (TextView)barView.findViewById(R.id.playTime);
		
		//工具栏
		this.footerBar = new PopupWindow(barView, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		this.footerBar.setAnimationStyle(R.style.AnimationFade);
	}
	//视频播放
	private synchronized void videoPlay(){
		Log.d(TAG, "视频播放...");
		if(videoView == null) return;
		//开始播放
		videoView.start();
		//启动定时器
		if(timer == null){
			//初始化定时器
			timer = new Timer();
			//定时任务
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					try{
						if(videoView != null && videoView.isPlaying() && videoBar != null && !videoBar.isPressed() && autoUpdateVideoSeekHandler != null){
							autoUpdateVideoSeekHandler.sendEmptyMessage(0);
						}
					}catch(Exception e){
						Log.e(TAG, "定时器执行异常:" + e.getMessage(), e);
					}
				}
			}, 500, 1000);
		}
	}
	//视频播放暂停
	private synchronized void videoPause(){
		Log.d(TAG, "播放暂停...");
		if(videoView != null){
			//暂停
			videoView.pause();
			//更新到数据库
			asyncUpdatePlayRecord();
		}
	}
	//加载popupwindow
	private void loadPopupWindows(){
		Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
			@Override
			public boolean queueIdle() {
				//显示两个popupwindow
				if(topBar != null && videoView.isShown()){
					Log.d(TAG, "显示音频控制栏....");
					showTopBar();
				}
				if(footerBar != null && videoView.isShown()){
					Log.d(TAG, "显示视频控制栏...");
					showFooterBar();
				}
				//显示正在加载
				return false;
			}
		});
	}
	/*
	 * OnTouchListener接口事件处理。
	 * @see android.view.View.OnTouchListener#onTouch(android.view.View, android.view.MotionEvent)
	 */
	@SuppressLint("ClickableViewAccessibility")
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		return this.gestureDetector.onTouchEvent(event);
	}
	/*
	 * OnGestureListener接口事件处理。
	 * 轻触屏幕，控制条出现或者消失
	 * @see android.view.GestureDetector.OnGestureListener#onDown(android.view.MotionEvent)
	 */
	@Override
	public boolean onDown(MotionEvent e) {
		Log.d(TAG, "轻触屏幕，控制条出现或者消失...");
		//顶部工具栏
		if(this.topBar != null){
			if(this.topBar.isShowing()){//显示=>隐藏
				this.topBar.dismiss();
			}else {//隐藏=>显示
				this.showTopBar();
			}
		}
		//底部工具栏
		if(this.footerBar != null){
			if(this.footerBar.isShowing()){//显示=>隐藏
				this.footerBar.dismiss();
			}else {//隐藏=>显示
				this.showFooterBar();
			}
		}
		return true;
	}
	/*
	 * OnGestureListener 接口处理。
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress(MotionEvent e) { }
	/*
	 * OnGestureListener 接口处理。
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp(MotionEvent e) { return false; }
	/*
	 * OnGestureListener 接口处理。
	 * @see android.view.GestureDetector.OnGestureListener#onScroll(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,float distanceY) { return false;}
	/*
	 * OnGestureListener 接口处理。
	 * @see android.view.GestureDetector.OnGestureListener#onLongPress(android.view.MotionEvent)
	 */
	@Override
	public void onLongPress(MotionEvent e) { }
	/*
	 * OnGestureListener 接口处理。
	 * @see android.view.GestureDetector.OnGestureListener#onFling(android.view.MotionEvent, android.view.MotionEvent, float, float)
	 */
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,float velocityY) { return false;}
	
	//显示顶部工具栏
	private void showTopBar(){
		if(topBar != null && videoView != null){
			Log.d(TAG, "显示顶部工具栏...");
			topBar.showAtLocation(videoView, Gravity.TOP, 0, 0);
			//topBar.update(height - 50, 0, width, 50);
			topBar.update();
		}
	}
	//显示底部工具栏
	private void showFooterBar(){
		if(footerBar != null && videoView != null){
			Log.d(TAG, "显示底部工具栏...");
			footerBar.showAtLocation(videoView, Gravity.BOTTOM, 0, 0);
			//footerBar.update(0, 0, width, 120);
			footerBar.update();
		}
	}
	//返回按钮的监听方式实现
	private void backRecord(){
			Log.d(TAG, "返回按钮监听实现...");
			//关闭工具栏
			if(topBar != null) topBar.dismiss();
			if(footerBar != null) footerBar.dismiss();
			//更新播放记录
			asyncUpdatePlayRecord();
			//停止播放
			if(this.videoView != null){
				Log.d(TAG, "准备停止播放...");
				this.videoView.stopPlayback();
				Log.d(TAG, "已停止播放");
			}
	}
	//异步线程更新播放记录时间
	private void asyncUpdatePlayRecord(){
		if(StringUtils.isBlank(recordId) || videoView == null) return;
		//异步线程处理返回保存数据
		AppContext.pools_fixed.execute(new Runnable() {
			@Override
			public void run() {
				try {
					Log.d(TAG, "异步线程更新播放记录时间...");
					//惰性加载
					if(playRecordDao == null){
						Log.d(TAG, "惰性加载播放记录...");
						playRecordDao = new PlayRecordDao(VideoPlayActivity.this, userId);
					}
					//更新播放时间
					if(videoView == null) return;
					final int pos = (int)(videoView.getCurrentPosition() / 1000);
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
				final AudioManager audioManager = this.appContext.getAudioManager();
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
		super.onResume();
		//播放
		videoPlay();
	}
	/*
	 * 重载暂停处理。
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		//播放暂停
		videoPause();
	}
	/*
	 * 重载停止处理。
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		this.backRecord();
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
					if(activity.videoView != null && activity.videoBar != null && activity.tvCurrentTime != null){
						final long pos = activity.videoView.getCurrentPosition(), total = activity.videoView.getDuration();
						if(pos >= 0 && total > 0){
							//设置视频进度条
							activity.videoBar.setProgress((int)((activity.videoBar.getMax() * pos) / total));
							//设置当前播放时间
							activity.tvCurrentTime.setText(getTime(pos / 1000));
						}
					}
				}
				super.handleMessage(msg);
			}catch(Exception e){
				Log.e(TAG, "自动更新视频工具栏异常:" + e.getMessage(), e);
			}
		}
	}
	//异步加载视频数据。
	private class AsynLoadVideoData extends AsyncTask<Void, Void, String>{
		/*
		 * 重载后台线程处理。
		 * @see android.os.AsyncTask#doInBackground(java.lang.Object[])
		 */
		@Override
		protected String doInBackground(Void... params) {
			try{
				Log.d(TAG, "后台线程加载数据处理...");
				//惰性初始化播放记录
				if(playRecordDao == null){
					playRecordDao = new PlayRecordDao(VideoPlayActivity.this, userId);
				}
				//如果存在播放记录
				if(StringUtils.isNotBlank(recordId)){
					Log.d(TAG, "根据播放记录["+recordId+"]加载数据....");
					final PlayRecord record = playRecordDao.getPlayRecord(recordId);
					if(record == null || StringUtils.isBlank(record.getLessonId())){
						Log.e(TAG, "播放记录["+recordId+"]不存在!");
						return null;
					}
					//设置课程ID
					lessonId = record.getLessonId();
					lessonName = record.getLessonName();
				}
				//课程资源ID不存在
				if(StringUtils.isBlank(lessonId)){
					Log.e(TAG, "课程资源ID不存在!");
					return null;
				}
				///TODO:优先加载下载到本地的数据
				//加载课程资源。
				final LessonDao lessonDao = new LessonDao(playRecordDao);
				final Lesson lesson = lessonDao.getLesson(lessonId);
				if(lesson == null){
					Log.e(TAG, "课程资源["+lessonId+"]不存在!");
					return null;
				}
				//设置课程名称
				lessonName = lesson.getName();
				//获取优先视频URL
				final String url = lesson.getPriorityUrl();
				Log.d(TAG, "video-url:" + url);
				return url;
			}catch(Exception e){
				Log.e(TAG, "后台线程加载数据异常:" + e.getMessage(), e);
			}
			return null;
		}
		/*
		 * 重载主线程处理。
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(String result) {
			Log.d(TAG, "前台主线程处理...");
			//重置标题
			if(tvTitle != null) tvTitle.setText(lessonName);
			//播放视频
			setVideoView(result);
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
//	//加载传递数据
//	private Uri loadIntentData(){
//		
//		String url = intent.getStringExtra("url");
//		if(!StringUtils.isEmpty(url)){
//			if(url.indexOf(this.username) == -1 && url.indexOf(Constant.NGINX_URL) == -1){
//				return Uri.parse(Constant.NGINX_URL + url);
//			}
//			if(url.indexOf(this.username) > -1){
//				File file = new File(url);
//				if(file.exists()){
//					return Uri.fromFile(file);
//				}
//				Toast.makeText(this, "本地文件已经被删除", Toast.LENGTH_SHORT).show();
//				//修改courseTab中的记录
//				//new CourseDao(this).updateState(this.username, this.httpUrl, 0);
//				this.finish();
//				return null;
//			}
//		}
//		if(!StringUtils.isEmpty(this.httpUrl)){
//			return Uri.parse(this.httpUrl);
//		}
//		this.finish();
//		return null;
//	}
}