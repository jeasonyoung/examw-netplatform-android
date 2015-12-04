package com.examw.netschool.util;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;

import com.examw.netschool.ch.LoginActivity;
import com.examw.netschool.ch.R;
import com.examw.netschool.codec.digest.DigestUtils;
import com.examw.netschool.model.JSONCallback;
import com.google.gson.Gson;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * API工具类。
 * 
 * @author jeasonyoung
 * @since 2015年11月11日
 */
public final class APIUtils {
	private static final String TAG = "APIUtils";
	//编码格式
	private static final String ENCODING = "UTF-8";
	// 超时
	private static final int TIME_OUT = 5000;
	//令牌参数名。
	private static final String TOKEN_PARAM_NAME = "token";
	//签名参数名。
	private static final String SIGN_PARAM_NAME = "sign"; 
	
	private static final int ERROR_CODE = -9;
	
	/**
	 * 发送POST请求。
	 * @param resources
	 * 资源对象。
	 * @param resUrl
	 * 请求URL资源。
	 * @param parameters
	 * 参数集合。
	 * @return
	 * 返回数据字符串。
	 */
	public static String sendPOST(final Resources resources, final  int resUrl, final  Map<String, Object> parameters){
		try{
			Log.d(TAG, "发送POST请求...");
			final String url = createRequestURL(resources, resUrl);
			//POST
			final HttpPost postMethod = new HttpPost(url);
			postMethod.setEntity(new UrlEncodedFormEntity(createParametersSignature(resources, parameters), ENCODING));
			//send
			return sendRequest(postMethod);
		}catch(Exception e){
			Log.e(TAG, "异常:" + e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 *  发送GET请求。
	 * @param resources
	 * 资源对象。
	 * @param resUrl
	 * 请求URL资源。
	 * @param parameters
	 * 参数集合。
	 * @return
	 * JSON结果数据字符串。
	 */
	public static  String sendGET(final Resources resources, final  int resUrl, final  Map<String, Object> parameters){
		try{
			Log.d(TAG, "发送GET请求...");
			//创建请求URL
			String url = createRequestURL(resources, resUrl);
			//对参数编码
			final String param = URLEncodedUtils.format(createParametersSignature(resources, parameters), ENCODING);
			if(url.indexOf('?') == -1){
				url += "?" + param;
			}else{
				url += "&" + param;
			}
			Log.d(TAG, "请求URL:" + url);
			//发送请求
			return sendRequest(new HttpGet(url));
		}catch(Exception e){
			Log.e(TAG, "异常:" + e.getMessage(), e);
		}
		return null;
	}
	
	/**
	 * 创建请求URL。
	 * @param resources
	 * 资源对象。
	 * @param resUrl
	 * 请求URL资源。
	 * @return
	 * URL。
	 */
	private static String createRequestURL(final Resources resources, final  int resUrl){
		Log.d(TAG, "创建请求URL...");
		//根URL
		final String root_url = resources.getString(R.string.api_url_root);
		//业务URL
		String url = resources.getString(resUrl);
		if(StringUtils.isBlank(url)) throw new IllegalArgumentException("resUrl参数未设置值！");
		//判断业务URL是否包含根URL
		if(url.indexOf(root_url) == -1){
			url = root_url + url;
		}
		Log.d(TAG, "请求URL:" + url);
		return url;
	}

	/**
	 * 创建参数签名。
	 * @param parameters
	 * 参数集合。
	 * @return
	 * 
	 */
	private static List<BasicNameValuePair> createParametersSignature(final Resources resources, Map<String, Object> parameters){
		 Log.d(TAG, "创建参数签名...");
		 //获取令牌
		 final String token = resources.getString(R.string.api_token);
		 Log.d(TAG, "令牌:" + token);
		 //获取密钥
		 final String secretkey = resources.getString(R.string.api_secretkey);
		 Log.d(TAG, "密钥:" + secretkey);
		 //初始化参数
		 final List<BasicNameValuePair> params = new LinkedList<BasicNameValuePair>();
		 //添加令牌参数
		 params.add(new BasicNameValuePair(TOKEN_PARAM_NAME, token));
		 //初始化参与签名计算集合
		 final List<String> sign_params = new ArrayList<String>();
		 //添加令牌
		 sign_params.add(TOKEN_PARAM_NAME + "=" + token);
		 //参数处理
		 if(parameters != null && parameters.size() > 0){
			for(Entry<String, Object> entry : parameters.entrySet()){
				//剔除含有令牌名称的参数
				if(StringUtils.equalsIgnoreCase(entry.getKey(), TOKEN_PARAM_NAME)) continue;
				//剔除含有签名的参数
				if(StringUtils.equalsIgnoreCase(entry.getKey(), SIGN_PARAM_NAME)) continue;
				//剔除参数值为NULL的参数
				if(entry.getValue() == null) continue;
				//剔除参数值类型为布尔型且值为false的参数
				if((entry.getValue() instanceof Boolean) && !((Boolean)entry.getValue())) continue;
				//剔除参数值为数字类型且值为0的参数
				if((entry.getValue() instanceof Number) && ((Number)entry.getValue()).floatValue() == 0) continue;
				//添加到参数集合
				params.add(new BasicNameValuePair(entry.getKey(), entry.getValue().toString()));
				//添加到签名计算集合
				sign_params.add(entry.getKey()+ "=" + entry.getValue().toString());
			}
		 }
		//签名处理
		if(sign_params.size() > 0){
			//排序
			Collections.sort(sign_params);
			//字符串拼接
			final String source = StringUtils.join(sign_params, "&") + secretkey;
			Log.d(TAG, "拼接后的字符串:" + source);
			//计算签名
			final String sign = DigestUtils.md5Hex(source);
			Log.d(TAG, "签名:" + sign);
			//添加签名
			params.add(new BasicNameValuePair(SIGN_PARAM_NAME, sign));
		}
		return params;
	}
	
	/**
	 * 发送请求。
	 * @param method
	 * @return
	 */
	private static String sendRequest(HttpUriRequest method){
		try{
			Log.d(TAG, "发送请求...");
			//初始化HTTP客户端
			final HttpClient client = new DefaultHttpClient();
			//设置连结超时
			client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIME_OUT);
			//设置读取超时
			client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIME_OUT);
			
			method.addHeader("Accept", "application/json");
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
			Log.e(TAG, "请求异常:" + e.getMessage(), e);
		}
		return null;
	}
	/**
	 * JSON结果反馈。
	 * @param <T>
	 * 数据类型。
	 * @author jeasonyoung
	 * @since 2015年11月11日
	 */
	public static class CallbackJSON<T>{
		private Class<T> clazz;
		private Context context;
		/**
		 * 构造函数。
		 * @param context
		 * 当前上下文。
		 * @param clazz
		 */
		public CallbackJSON(Context context, Class<T> clazz){
			this.context = context;
			this.clazz = clazz;
		}
		
		/**
		 * 数据转换。
		 * @param json
		 * JSON字符串。
		 * @return
		 */
		public  JSONCallback<T> convert(String json){
			Log.d(TAG, "返回的JSON字符串转换为对象....");
			if(StringUtils.isBlank(json)){
				return new JSONCallback<T>(false, "服务器未响应!");
			}
			//返回类型处理
			final Type type = type(JSONCallback.class, this.clazz);
			//初始化JSON对象转换
			final Gson gson = new Gson();
			//返回结果
			final JSONCallback<T> result = gson.fromJson(json, type);
			if(!result.getSuccess() && result.getCode() != null && result.getCode() == ERROR_CODE && this.context != null){
				Log.d(TAG, "返回结果:" + result.getCode() + "-" + result.getMsg());
				new CallReLoginHandler(this.context).sendEmptyMessage(result.getCode());
			}
			return result;
		}	
		
		//类型转换
		private static ParameterizedType type(final Class<?> raw, final Type ...args){
			return new ParameterizedType() {
				
				@Override
				public Type getRawType() {
					return raw;
				}
				 
				@Override
				public Type getOwnerType() {
					return null;
				}
				
				@Override
				public Type[] getActualTypeArguments() {
					return args;
				}
			};
		}
		
		/**
		 * 发送POST请求。
		 * @param resources
		 * 资源对象。
		 * @param resUrl
		 * 请求URL资源。
		 * @param parameters
		 * 参数集合。
		 * @return
		 * 返回对象。
		 */
		public JSONCallback<T> sendPOSTRequest(final Resources resources, final  int resUrl, final  Map<String, Object> parameters){
			return this.convert(sendPOST(resources, resUrl, parameters));
		}
		/**
		 * 发送GET请求。
		 * @param resources
		 * 资源对象。
		 * @param resUrl
		 * 请求URL资源。
		 * @param parameters
		 * 参数集合。
		 * @return
		 * 返回对象。
		 */
		public JSONCallback<T> sendGETRequest(final Resources resources, final  int resUrl, final  Map<String, Object> parameters){
			return this.convert(sendGET(resources, resUrl, parameters));
		}
	}
	//
	private static class CallReLoginHandler extends Handler{
		private Context context;
		/**
		 * 构造函数。
		 * @param context
		 */
		public CallReLoginHandler(Context context){
			super(context.getMainLooper());
			this.context = context;
		}
		
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			//
			if(msg.what == ERROR_CODE && context != null){ 
				//重新启动登录界面
				context.startActivity(new Intent(context, LoginActivity.class));
			}
		}
	}
}