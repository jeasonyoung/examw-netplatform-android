package com.examw.netschool.player;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.MediaPlayer.OnBufferingUpdateListener;
import io.vov.vitamio.MediaPlayer.OnCompletionListener;
import io.vov.vitamio.MediaPlayer.OnPreparedListener;
import io.vov.vitamio.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.examw.netschool.VideoPlayActivity;
/**
 * 播放器类
 * @author jeasonyoung
 * 通用的播放器处理类
 */
public class VitamioVideoPlayer {
	private static final String TAG = "VitamioVideoPlayer";
	private VideoView videoView;
	private SeekBar skbProgress;
	private TextView currentTime,totalTime;
	private RelativeLayout loadLayout;
	private VideoPlayActivity videoPlayActivity;
	private Timer timer ;
	private int recordTime; 
	private HandlePlayProgress handlePlayProgress;
	/**
	 * 构造函数
	 * @param vpActivity
	 * @param videoView
	 * @param skbProgress
	 * @param currentTime
	 * @param totalTime
	 * @param recordTime
	 * @param loadLayout
	 * @param url
	 */
	public VitamioVideoPlayer(VideoPlayActivity vpActivity, VideoView videoView,SeekBar skbProgress, TextView currentTime, TextView totalTime,
			int recordTime, RelativeLayout loadLayout,Uri uri, String userName) {
		
		this.videoPlayActivity = vpActivity;
		this.skbProgress = skbProgress;
		this.currentTime = currentTime;
		this.totalTime = totalTime;
		this.recordTime = recordTime;
		this.loadLayout = loadLayout;
		
		Log.d(TAG, "最终的播放地址为:"+uri);

		this.videoView = videoView;
		this.videoView.setVideoURI(uri);
		this.videoView.requestFocus();
		this.videoView.setOnBufferingUpdateListener(this.onBufferingUpdateListener);
		this.videoView.setOnCompletionListener(this.onCompletionListener);
		this.videoView.setOnPreparedListener(this.onPreparedListener);
	}
	//
	private OnBufferingUpdateListener onBufferingUpdateListener = new OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
			if(skbProgress != null){
				skbProgress.setSecondaryProgress(percent);
			}
		}
	};
	//
	private OnCompletionListener onCompletionListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			Log.d(TAG, "onCompletion 播放完毕...");
			if(skbProgress != null)skbProgress.setProgress(0);
			if(currentTime != null)currentTime.setText("00:00");
			if(videoView != null){
				videoView.seekTo(0);
				videoView.pause();
			}
			if(timer != null){
				timer.cancel();
				timer = null;
			}
			if(videoPlayActivity != null)videoPlayActivity.onCompletion(mp);
		}
	};
	//
	private OnPreparedListener onPreparedListener = new OnPreparedListener() {
		@Override
		public void onPrepared(MediaPlayer mp) {
			Log.d(TAG, "onPrepared..");
			if(loadLayout != null)loadLayout.setVisibility(View.GONE);
			long duration = mp.getDuration();
			if(totalTime != null)totalTime.setText(getTime(duration / 1000));
			mp.setPlaybackSpeed(1.0f);
			if(skbProgress != null){
				if(recordTime > 0 && recordTime < duration - 10){
					skbProgress.setProgress((int)(recordTime * skbProgress.getMax() / duration));
					mp.seekTo(recordTime);
				}
				//
				skbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
					private int progress;
					/*
					 * 重载。
					 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStopTrackingTouch(android.widget.SeekBar)
					 */
					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						//seekTo()的参数是相对与影片时间的数字，而不是与seekBar.getMax()相对的数字
						if(videoView != null)videoView.seekTo(this.progress);
						//更改时间
						if(currentTime != null)currentTime.setText(getTime(this.progress / 1000));
					}
					/*
					 * 重载。
					 * @see android.widget.SeekBar.OnSeekBarChangeListener#onStartTrackingTouch(android.widget.SeekBar)
					 */
					@Override
					public void onStartTrackingTouch(SeekBar seekBar) { }
					/*
					 * 重载。
					 * @see android.widget.SeekBar.OnSeekBarChangeListener#onProgressChanged(android.widget.SeekBar, int, boolean)
					 */
					@Override
					public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
						if(videoView == null)return;
						this.progress = (int)(progress * videoView.getDuration() / seekBar.getMax());
					}
				});
			}
			//
			if(!mp.isPlaying()){
				mp.start();
			}
		}
	};
	/**
	 * 是否在播放中。
	 * @return
	 */
	public boolean isPlaying() {
		Log.d(TAG, "检查是否在播放...");
		return (this.videoView != null && this.videoView.isPlaying()); //videoView.isPlaying();
	}
	/**
	 * 开始播放。
	 */
	public void play()
	{
		Log.d(TAG,"准备播放...");
		if(this.videoView != null){
			this.videoView.start();
			if(this.timer == null){//启动定时器
				this.timer = new Timer();
				this.handlePlayProgress = new HandlePlayProgress(this.videoView, this.skbProgress, this.currentTime);
				Log.d(TAG, "启动定时器...");
				this.timer.schedule(new TimerTask() {
					@Override
					public void run() {
						 try {
							 if(videoView == null || skbProgress == null)return;
							 if(videoView.isPlaying() && !skbProgress.isPressed()){
								 handlePlayProgress.sendEmptyMessage(0);
							 }
						}catch(NullPointerException e){
							Log.e(TAG, "播放器还没有创建",e);
						}
						catch (Exception e) {
							Log.e(TAG, "定时器运行发生异常:" + e.getMessage(), e);
						}
					}
				}, 0, 1000);
			}
			Log.d(TAG, "已启动播放");
		}
	}
	/**
	 * 暂停播放。
	 */
	public void pause()
	{
		Log.d(TAG, "准备暂停播放...");
		if(this.isPlaying()){
			this.videoView.pause();
			Log.d(TAG, "已暂停播放");
		}
	}
	/**
	 * 停止播放。
	 */
	public void stop()
	{
		Log.d(TAG, "准备停止播放...");
		if(this.videoView != null){
			this.videoView.stopPlayback();
			Log.d(TAG, "已停止播放");
		}
	}
	/**
	 * 后退。
	 */
	public void setBack() {
		Log.d(TAG, "准备后退...");
		if(this.videoView == null)return;
		long pos = this.videoView.getCurrentPosition();
		if(pos - 5000 > 0){
			pos -= 5000;
			this.videoView.seekTo(pos);
			if(this.skbProgress != null){
				int max = this.skbProgress.getMax();
				long duration = this.videoView.getDuration();
				this.skbProgress.setProgress((int)((pos * max)/duration));
				if(this.currentTime != null){
					this.currentTime.setText(getTime(pos/1000));
				}
			}
		}
	}
	/**
	 * 前进。
	 */
	public void setForward(){
		Log.d(TAG, "准备前进...");
		if(this.videoView == null)return;
		long pos = this.videoView.getCurrentPosition(),duration = this.videoView.getDuration();
		if(pos + 5000 < duration){
			pos += 5000;
			this.videoView.seekTo(pos);
			if(this.skbProgress != null){
				int max = this.skbProgress.getMax();
				this.skbProgress.setProgress((int)(pos * max / duration));
				if(this.currentTime != null){
					this.currentTime.setText(getTime(pos / 1000));
				}
			}
		}
	}
	/**
	 * 获取当前播放位置。
	 * @return 当前播放位置。
	 */
	public long getCurrentTime()
	{
		long time  = 0;
		if(this.videoView != null){
			time = this.videoView.getCurrentPosition();
		}
		Log.d(TAG, "获取当前播放位置:" + time);
		return time;
	}
	/**
	 * 内置类
	 * @author jeasonyoung
	 *
	 */
	 static class HandlePlayProgress extends Handler {
		 private VideoView videoView;
		 private SeekBar seekBar;
		 private TextView tvCurrentTime;
		 /**
		  * 构造函数
		  * @param videoView
		  * @param seekBar
		  */
		 public HandlePlayProgress(VideoView videoView,SeekBar seekBar,TextView currentTime){
			 super();
			 this.videoView = videoView;
			 this.seekBar = seekBar;
			 this.tvCurrentTime = currentTime;
		 }
		/*
		 * 重载消息处理。
		 * @see android.os.Handler#handleMessage(android.os.Message)
		 */
		@Override
		public void handleMessage(Message msg) {
			 if(msg.what == 0){
				 try {
					  if(this.videoView == null || this.seekBar == null || this.tvCurrentTime == null)return;
					  long position = this.videoView.getCurrentPosition(),duration = this.videoView.getDuration();
					  if(duration > 0){
						  long  pos = (this.seekBar.getMax() * position) / duration;
						  this.seekBar.setProgress((int)pos); 
						  this.tvCurrentTime.setText(getTime(position / 1000));
					  }
				} catch (Exception e) {
					Log.e(TAG, "消息处理异常:" + e.getMessage(), e);
				}
			 }
		}
	}
	/**
	 * 获取显示的时间
	 * @param count
	 * @return
	 */
	private static String getTime(long count) {
		long m = count / 60, s = count % 60;
		String ms = m < 10 ? "0" + m : m + "";
		return ms + ":" + (s < 10 ? "0" + s : s + "");
	}
}