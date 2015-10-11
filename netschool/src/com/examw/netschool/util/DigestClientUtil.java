package com.examw.netschool.util;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.examw.netschool.app.Constant;
import com.examw.netschool.model.JSONCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import android.util.Log;
/**
 * HTTP摘要认证客户端工具类
 * 
 * @author yangyong
 * @since 2014年12月22日
 */
public final class DigestClientUtil {
	private static final String TAG = "DigestClientUtil";
	private static final String ENCODING = "UTF-8", APP_JSON = "application/json";
	private static final int TIME_OUT = 5000;
	/**
	 * GET请求。
	 * @param url
	 * @param parameters
	 * @return
	 */
	public static String sendDigestGetRequest(String url){
		return sendDigestGetRequest(url, null);
	}
	/**
	 * GET请求。
	 * @param url
	 * @param parameters
	 * @return
	 */
	public static String sendDigestGetRequest(String url, Map<String, String> parameters){
		Log.d(TAG, "GET:" + url);
		if(StringUtils.isBlank(url)) return null;
		HttpGet getMethod;
		if(parameters != null && parameters.size() > 0){
			//将参数放入List,再对参数进行URL编码
			final List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			for(Entry<String, String> entity : parameters.entrySet()){
				params.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}
			//对参数编码
			final String param = URLEncodedUtils.format(params, ENCODING);
			//参数
			Log.d(TAG, "参数:" + param);
			//
			getMethod = new HttpGet(url + "?" + param);
		}else {
			getMethod = new HttpGet(url);
		}
		//Send
		return sendDigestRequest(getMethod);
	}
	/**
	 * POST请求。
	 * @param url
	 * @param parameters
	 * @return
	 */
	public static String sendDigestPOSTRequest(String url, Map<String, String> parameters){
		Log.d(TAG, "POST:" + url);
		if(StringUtils.isBlank(url)) return null;
		try {
			//将参数放入List,再对参数进行URL编码
			final List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
			for(Entry<String, String> entity : parameters.entrySet()){
				params.add(new BasicNameValuePair(entity.getKey(), entity.getValue()));
			}
			//POST
			final HttpPost postMethod = new HttpPost(url);
			postMethod.setEntity(new UrlEncodedFormEntity(params, ENCODING));
			//Send
			return sendDigestRequest(postMethod);
			
		} catch (UnsupportedEncodingException e) {
			Log.e(TAG, "POST异常:" + e.getMessage(), e);
		}
		return null;
	}
	/**
	 * POST提交JSON对象。
	 * @param url
	 * 
	 * @param json
	 * @return
	 */
	public static JSONCallback<Object> sendDigestPOSTJSONRequest(String url, Object json){
		Log.d(TAG, "POST:" + url);
		if(StringUtils.isBlank(url) || json == null) return null;
		try{
			//初始化JSON对象
			final Gson gson = new Gson();
			//JSON序列化
			final String json_data = gson.toJson(json);
			//POST
			final HttpPost postMethod = new HttpPost(url);
			//
			final StringEntity s = new StringEntity(json_data, ENCODING);
			s.setContentEncoding(ENCODING);
			s.setContentType(APP_JSON);
			//
			postMethod.setEntity(s);
			//提交数据
			final String result = sendDigestRequest(postMethod);
			//
			if(StringUtils.isNotBlank(result)){
				//类型
				final Type type = new TypeToken<JSONCallback<Object>>(){}.getType();
				return gson.fromJson(result, type);
			}
		}catch(Exception e){
			Log.e(TAG, "POST异常:" + e.getMessage(), e);
		}
		return null;
	}
	/**
	 * 摘要请求。
	 * @return
	 */
	private static String sendDigestRequest(HttpUriRequest method){
		try {
			//创建认证提供者
			final BasicCredentialsProvider bcp = new BasicCredentialsProvider();
			bcp.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(Constant.DOMAIN_Username, Constant.DOMAIN_Password));
			
			//初始化HTTP客户端
			final DefaultHttpClient client = new DefaultHttpClient();
			//连结超时
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);
			//读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);
			//设置认证
			client.setCredentialsProvider(bcp);
			
			//执行方法并返回 response
			final HttpResponse response = client.execute(method);
			
			//返回结果
			final int status = response.getStatusLine().getStatusCode();
			final String result = EntityUtils.toString(response.getEntity(), ENCODING);
			
			Log.d(TAG, "请求反馈["+status+"]:" + result);
			if(status == HttpStatus.SC_OK){//200
				return result;
			} 
		} catch (Exception e) {
			Log.e(TAG, "GET请求异常:" + e.getMessage(), e);
		}
		return null;
	}
}