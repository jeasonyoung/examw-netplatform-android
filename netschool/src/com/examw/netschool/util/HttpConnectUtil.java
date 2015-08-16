package com.examw.netschool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.util.Log;

public class HttpConnectUtil {
	public static String httpGetRequest(Context context,String urladdr)throws Exception
	{
		System.out.println("get url = "+urladdr);
		HttpURLConnection conn = null;
		BufferedReader br = null;
		String result="";
		try {
			URL url = new URL(urladdr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(600000);
			conn.setReadTimeout(600000);
			conn.setRequestMethod("GET");
			// 检查网络
			conn.connect();
			// 连接错误
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				Log.d(context.toString(), "请求错误");
				throw new Exception();
			}
			InputStream in = conn.getInputStream();
			br = new BufferedReader(new InputStreamReader(in));
			StringBuffer buf = new StringBuffer();
			String line = null;
			while ((line = br.readLine()) != null) {
				buf.append(line);
			}
			result = buf.toString();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return result;
	}
}
