package com.examw.netschool.adapter;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.examw.netschool.DownloadActivity;
import com.examw.netschool.R;
import com.examw.netschool.VideoPlayActivity;

public class MyCourseListAdapter extends BaseAdapter{
	private Context context;
	private List<String> courses;
	private List<String> urls;
	
	@Override
	public int getCount() {
		return courses.size();
	}
	
	@Override
	public Object getItem(int position) {
		return courses.get(position);
	}
	
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	@SuppressLint("ViewHolder") 
	public android.view.View getView(final int position, android.view.View convertView, android.view.ViewGroup parent) {
		LayoutInflater inflater = LayoutInflater.from(context);
		convertView = inflater.inflate(R.layout.courselist_layout, null);
		TextView name = (TextView) convertView.findViewById(R.id.text4);
		name.setText(courses.get(position));
		TextView isDown = (TextView) convertView.findViewById(R.id.Downprogresstext);
		isDown.setText("未下载");
		ImageButton btn = (ImageButton) convertView.findViewById(R.id.playerBtn);
		
		name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context,VideoPlayActivity.class);
				intent.putExtra("name", courses.get(position));
				intent.putExtra("url", urls.get(position));
				context.startActivity(intent);
			}
		});
		btn.setOnClickListener(new ClickEvent(position,courses,urls));
		return convertView;
		
	};
	public MyCourseListAdapter(Context context,List<String> course,List<String> urls) {
		this.context = context;
		this.courses = course;
		this.urls = urls;
	}
	private class ClickEvent implements OnClickListener
	{
		private int position;
		private List<String> courses,urls;
		public ClickEvent(int position,List<String> courses,List<String>urls) {
			this.position = position;
			this.courses = courses;
			this.urls = urls;
		}
		@Override
		public void onClick(View v) {
			//检查sd卡是否可用,
			//获取文件的大小
			//检查sd的可用容量是否够
			
//			File pathFile = Environment.getExternalStorageDirectory();
//			StatFs statfs = new StatFs(pathFile.getPath());
//			//获得可供程序使用的Block数量
//			long nAvailaBlock = statfs.getAvailableBlocks();
//			//获得SDCard上每个block的SIZE
//			long nBlocSize = statfs.getBlockSize();
//			//计算SDCard剩余大小MB
//			long nSDFreeSize = nAvailaBlock * nBlocSize / 1024 / 1024;
			Intent intent = new Intent(context,DownloadActivity.class);
			intent.putExtra("name", courses.get(position));
			intent.putExtra("url", urls.get(position));
			context.startActivity(intent);
		}		
	}
}