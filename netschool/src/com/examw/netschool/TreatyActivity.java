package com.examw.netschool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.examw.netschool.util.Constant;
import com.umeng.analytics.MobclickAgent;

public class TreatyActivity extends Activity{
	private TextView content;
	private ProgressDialog dialog;
	private ImageButton returnBtn;
	private Handler handler;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_treaty);
		content = (TextView) this.findViewById(R.id.treatyText);
		returnBtn = (ImageButton) this.findViewById(R.id.returnbtn);
		dialog = ProgressDialog.show(TreatyActivity.this,null,"努力加载中请稍候",true,false);
		dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		returnBtn.setOnClickListener(new ReturnBtnClickListener(this));
		new ShowTreatyThread().start();
		handler = new MyHandler(this);
		
	}
	private class ShowTreatyThread extends Thread
	{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			HttpURLConnection conn = null;
			try {
				URL url = new URL(Constant.DOMAIN_URL+"mobile/getTreaty");
				System.out.println(url);
				conn = (HttpURLConnection) url.openConnection();
				conn.connect();
				if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
					handler.sendEmptyMessage(-2);
					return;
				}
				InputStream in = conn.getInputStream();
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
				StringBuffer buf = new StringBuffer();
				String line = null;
				while ((line = br.readLine()) != null) {
					buf.append(line);
				}
				String result = buf.toString();
				in.close();
				br.close();
				try{
					JSONObject obj = new JSONObject(result);
					result = obj.getString("content");
				}catch(Exception e)
				{
					result = null;
				}
				//content = "%3Cp+align%3D%22center%22%3E%3Ch4+align%3D%22center%22%3E+233%CD%F8%D0%A3%BB%E1%D4%B1%D7%A2%B2%E1%B7%FE%CE%F1%D0%AD%D2%E9%3C%2Fh4%3E%3C%2Fp%3E%3Cp+align%3D%22left%22%3E%D4%DA233%CD%F8%D0%A3%CD%F8%D5%BE%D7%A2%B2%E1%CA%C7%CD%EA%C8%AB%C3%E2%B7%D1%B5%C4%A3%AC%BC%CC%D0%F8%D7%A2%B2%E1%C7%B0%C7%EB%CF%C8%D4%C4%B6%C1%B7%FE%CE%F1%CC%F5%BF%EE%A3%BA%3Cbr%3E%D3%C3%BB%A7%B5%A5%B6%C0%B3%D0%B5%A3%B7%A2%B2%BC%C4%DA%C8%DD%B5%C4%D4%F0%C8%CE%A1%A3%D3%C3%BB%A7%B6%D4%B7%FE%CE%F1%B5%C4%CA%B9%D3%C3%CA%C7%B8%F9%BE%DD%CB%F9%D3%D0%CA%CA%D3%C3%D3%DA%B7%FE%CE%F1%B5%C4%B5%D8%B7%BD%B7%A8%C2%C9%A1%A2%B9%FA%BC%D2%B7%A8%C2%C9%BA%CD%B9%FA%BC%CA%B7%A8%C2%C9%B1%EA%D7%BC%B5%C4%A1%A3%3Cbr%3E%D3%C3%BB%A7%B3%D0%C5%B5%A3%BA%3Cbr%3E%D2%BB%A1%A2%D4%DA%B1%BE%D5%BE%B5%C4%CD%F8%D2%B3%C9%CF%B7%A2%B2%BC%D0%C5%CF%A2%BB%F2%D5%DF%C0%FB%D3%C3%B1%BE%D5%BE%B5%C4%B7%FE%CE%F1%CA%B1%B1%D8%D0%EB%B7%FB%BA%CF%D6%D0%B9%FA%D3%D0%B9%D8%B7%A8%B9%E6%A3%AC%B2%BB%B5%C3%D4%DA%B1%BE%D5%BE%B5%C4%CD%F8%D2%B3%C9%CF%BB%F2%D5%DF%C0%FB%D3%C3%B1%BE%D5%BE%B5%C4%B7%FE%CE%F1%D6%C6%D7%F7%A1%A2%B8%B4%D6%C6%A1%A2%B7%A2%B2%BC%A1%A2%B4%AB%B2%A5%D2%D4%CF%C2%D0%C5%CF%A2%A3%BA%3Cbr%3E1%29+%B7%B4%B6%D4%CF%DC%B7%A8%CB%F9%C8%B7%B6%A8%B5%C4%BB%F9%B1%BE%D4%AD%D4%F2%B5%C4%A3%BB%3Cbr%3E2%29+%CE%A3%BA%A6%B9%FA%BC%D2%B0%B2%C8%AB%A3%AC%D0%B9%C2%B6%B9%FA%BC%D2%C3%D8%C3%DC%A3%AC%B5%DF%B8%B2%B9%FA%BC%D2%D5%FE%C8%A8%A3%AC%C6%C6%BB%B5%B9%FA%BC%D2%CD%B3%D2%BB%B5%C4%A3%BB%3Cbr%3E3%29+%CB%F0%BA%A6%B9%FA%BC%D2%C8%D9%D3%FE%BA%CD%C0%FB%D2%E6%B5%C4%A3%BB%3Cbr%3E4%29+%C9%BF%B6%AF%C3%F1%D7%E5%B3%F0%BA%DE%A1%A2%C3%F1%D7%E5%C6%E7%CA%D3%A3%AC%C6%C6%BB%B5%C3%F1%D7%E5%CD%C5%BD%E1%B5%C4%A3%BB%3Cbr%3E5%29+%C6%C6%BB%B5%B9%FA%BC%D2%D7%DA%BD%CC%D5%FE%B2%DF%A3%AC%D0%FB%D1%EF%D0%B0%BD%CC%BA%CD%B7%E2%BD%A8%C3%D4%D0%C5%B5%C4%A3%BB%3Cbr%3E6%29+%C9%A2%B2%BC%D2%A5%D1%D4%A3%AC%C8%C5%C2%D2%C9%E7%BB%E1%D6%C8%D0%F2%A3%AC%C6%C6%BB%B5%C9%E7%BB%E1%CE%C8%B6%A8%B5%C4%A3%BB%3Cbr%3E7%29+%C9%A2%B2%BC%D2%F9%BB%E0%A1%A2%C9%AB%C7%E9%A1%A2%B6%C4%B2%A9%A1%A2%B1%A9%C1%A6%A1%A2%D0%D7%C9%B1%A1%A2%BF%D6%B2%C0%BB%F2%D5%DF%BD%CC%CB%F4%B7%B8%D7%EF%B5%C4%A3%BB%3Cbr%3E8%29+%CE%EA%C8%E8%BB%F2%D5%DF%B7%CC%B0%F9%CB%FB%C8%CB%A3%AC%C7%D6%BA%A6%CB%FB%C8%CB%BA%CF%B7%A8%C8%A8%D2%E6%B5%C4%A3%BB%3Cbr%3E9%29+%BA%AC%D3%D0%B7%A8%C2%C9%A1%A2%D0%D0%D5%FE%B7%A8%B9%E6%BD%FB%D6%B9%B5%C4%C6%E4%CB%FB%C4%DA%C8%DD%B5%C4%A1%A3%3Cbr%3E%B6%FE%A1%A2%D4%DA%B1%BE%D5%BE%B5%C4%CD%F8%D2%B3%C9%CF%B7%A2%B2%BC%D0%C5%CF%A2%BB%F2%D5%DF%C0%FB%D3%C3%B1%BE%D5%BE%B5%C4%B7%FE%CE%F1%CA%B1%BB%B9%B1%D8%D0%EB%B7%FB%BA%CF%C6%E4%CB%FB%D3%D0%B9%D8%B9%FA%BC%D2%BA%CD%B5%D8%C7%F8%B5%C4%B7%A8%C2%C9%B9%E6%B6%A8%D2%D4%BC%B0%B9%FA%BC%CA%B7%A8%B5%C4%D3%D0%B9%D8%B9%E6%B6%A8%A1%A3%3Cbr%3E%C8%FD%A1%A2%B2%BB%C0%FB%D3%C3%B1%BE%D5%BE%B5%C4%B7%FE%CE%F1%B4%D3%CA%C2%D2%D4%CF%C2%BB%EE%B6%AF%A3%BA%3Cbr%3E1%29+%CE%B4%BE%AD%D4%CA%D0%ED%A3%AC%BD%F8%C8%EB%BC%C6%CB%E3%BB%FA%D0%C5%CF%A2%CD%F8%C2%E7%BB%F2%D5%DF%CA%B9%D3%C3%BC%C6%CB%E3%BB%FA%D0%C5%CF%A2%CD%F8%C2%E7%D7%CA%D4%B4%B5%C4%A3%BB%3Cbr%3E2%29+%CE%B4%BE%AD%D4%CA%D0%ED%A3%AC%B6%D4%BC%C6%CB%E3%BB%FA%D0%C5%CF%A2%CD%F8%C2%E7%B9%A6%C4%DC%BD%F8%D0%D0%C9%BE%B3%FD%A1%A2%D0%DE%B8%C4%BB%F2%D5%DF%D4%F6%BC%D3%B5%C4%A3%BB%3Cbr%3E3%29+%CE%B4%BE%AD%D4%CA%D0%ED%A3%AC%B6%D4%BD%F8%C8%EB%BC%C6%CB%E3%BB%FA%D0%C5%CF%A2%CD%F8%C2%E7%D6%D0%B4%E6%B4%A2%A1%A2%B4%A6%C0%ED%BB%F2%D5%DF%B4%AB%CA%E4%B5%C4%CA%FD%BE%DD%BA%CD%D3%A6%D3%C3%B3%CC%D0%F2%BD%F8%D0%D0%C9%BE%B3%FD%A1%A2%D0%DE%B8%C4%BB%F2%D5%DF%D4%F6%BC%D3%B5%C4%A3%BB%3Cbr%3E4%29+%B9%CA%D2%E2%D6%C6%D7%F7%A1%A2%B4%AB%B2%A5%BC%C6%CB%E3%BB%FA%B2%A1%B6%BE%B5%C8%C6%C6%BB%B5%D0%D4%B3%CC%D0%F2%B5%C4%A3%BB%3Cbr%3E5%29+%C6%E4%CB%FB%CE%A3%BA%A6%BC%C6%CB%E3%BB%FA%D0%C5%CF%A2%CD%F8%C2%E7%B0%B2%C8%AB%B5%C4%D0%D0%CE%AA%A1%A3%3Cbr%3E%CB%C4%A1%A2%B2%BB%D2%D4%C8%CE%BA%CE%B7%BD%CA%BD%B8%C9%C8%C5%B1%BE%D5%BE%B5%C4%B7%FE%CE%F1%A1%A3%3Cbr%3E%CE%E5%A1%A2%D7%F1%CA%D8%B1%BE%D5%BE%B5%C4%CB%F9%D3%D0%C6%E4%CB%FB%B9%E6%B6%A8%BA%CD%B3%CC%D0%F2%A1%A3%3Cbr%3E%C7%EB%C8%B7%C8%CF%C4%FA%D2%D1%D7%D0%CF%B8%D4%C4%B6%C1%C1%CB%B1%BE%B7%FE%CE%F1%CC%F5%BF%EE%A3%AC%BD%D3%CA%DC%B1%BE%D5%BE%B7%FE%CE%F1%CC%F5%BF%EE%C8%AB%B2%BF%C4%DA%C8%DD%A3%AC%B3%C9%CE%AA233%CD%F8%D0%A3%CD%F8%D5%BE%B5%C4%D5%FD%CA%BD%D3%C3%BB%A7%A1%A3%D3%C3%BB%A7%D4%DA%CF%ED%CA%DC233%CD%F8%D0%A3%CD%F8%D5%BE%B7%FE%CE%F1%CA%B1%B1%D8%D0%EB%CD%EA%C8%AB%A1%A2%D1%CF%B8%F1%D7%F1%CA%D8%B1%BE%B7%FE%CE%F1%CC%F5%BF%EE%A1%A3%3Cbr%3E%3Cstrong%3E%C9%F9%C3%F7%A3%BA233%CD%F8%D0%A3%CD%F8%D5%BE%B5%C4%CB%F9%D3%D0%C8%A8%A1%A2%B7%FE%CE%F1%CC%F5%BF%EE%B5%C4%BD%E2%CA%CD%C8%A8%B9%E9233%CD%F8%D0%A3%CD%F8%D5%BE%CB%F9%D3%D0%A1%A3%3C%2Fstrong%3E%3C%2Fp%3E";
				Bundle b = new Bundle();
				b.putString("treatyStr", URLDecoder.decode(result,"gbk"));
				Message msg = handler.obtainMessage();
				msg.what = 1;
				msg.setData(b);
				handler.sendMessage(msg);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				handler.sendEmptyMessage(-1);
			}finally
			{
				if(conn!=null)
				{
					conn.disconnect();
				}
			}
		}
	}
	static class MyHandler extends Handler {
        WeakReference<TreatyActivity> mActivity;
        MyHandler(TreatyActivity activity) {
                mActivity = new WeakReference<TreatyActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
        		TreatyActivity theActivity = mActivity.get();
                switch (msg.what) {
                case 1:
                	theActivity.dialog.dismiss();
                	String content = msg.getData().getString("treatyStr");
                	theActivity.content.setText(Html.fromHtml(content));
                	break;
                case -1:
                	theActivity.dialog.dismiss();
                	Toast.makeText(theActivity, "无法连接到服务器", Toast.LENGTH_SHORT).show();
                	break;
                case -2:
                	theActivity.dialog.dismiss();
                	Toast.makeText(theActivity, "无法连接到地址", Toast.LENGTH_SHORT).show();
                	break;
                }
        }
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		if(dialog!=null)
		{
			dialog.dismiss();	
		}
		super.onDestroy();
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	};
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		MobclickAgent.onResume(this);
		
	}
}
