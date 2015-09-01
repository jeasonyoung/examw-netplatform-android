package com.examw.netschool.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import android.util.Log;

import com.examw.netschool.codec.digest.DigestUtils;

/**
 * HTTP摘要认证客户端工具类
 * 
 * @author yangyong
 * @since 2014年12月22日
 */
public final class DigestClientUtil {
	private static final String TAG = "DigestClientUtil";
	private static final String default_utf8_charset = "UTF-8", default_get_method = "GET", authenticate_header = "WWW-Authenticate", authorization_header = "Authorization";
	private static final int max_http_send_count = 3; 
	/**
	 * 发送HTTP摘要认证请求。
	 * @param username
	 * 用户名。
	 * @param password
	 * 密码。
	 * @param method
	 * 方法。
	 * @param uri
	 * 目标uri。
	 * @param data
	 * 数据。
	 * @return
	 * 反馈数据。
	 * @throws IOException
	 */
	public static String sendDigestRequest(String username,String password,String method,String uri,String data) throws Exception{
		Log.d(TAG, "发送HTTP摘要认证请求:" + uri);
		Log.d(TAG, "username => " + username);
		Log.d(TAG, "password => " + password);
		Log.d(TAG, "method => " + method);
		Log.d(TAG, "data => " + data);
		return sendDigestRequest(username, password, null, method, uri, data);
	}
	/**
	 * 发送JSON数据摘要认证请求。
	 * @param username
	 * 用户名。
	 * @param password
	 * 密码。
	 * @param method
	 * 方法(POST/GET)。
	 * @param uri
	 * 目标uri。
	 * @param data
	 * 数据。
	 * @return
	 * 返回数据。
	 * @throws Exception
	 */
	public static String sendJSONDigestRequest(String username,String password,String method,String uri,String data) throws Exception{
		Log.d(TAG, "发送JSON数据摘要认证请求:" + uri);
		Log.d(TAG, "username => " + username);
		Log.d(TAG, "password => " + password);
		Log.d(TAG, "method => " + method);
		Log.d(TAG, "data => " + data);
		
		Map<String, String> headers = null;
		if(StringUtils.isNotBlank(data)){
			headers = new HashMap<String, String>();
			headers.put("Content-type","application/json;charset=UTF-8");
		}
		
		return sendDigestRequest(username, password, headers, method, uri, data);
	}
	/**
	 * 发送HTTP摘要认证请求。
	 * @param username
	 * 用户名。
	 * @param password
	 * 密码。
	 * @param headers
	 * 头消息集合。
	 * @param method
	 * 方法。
	 * @param uri
	 * 目标uri
	 * @param data
	 * 数据。
	 * @return
	 * @throws Exception
	 */
	public static String sendDigestRequest(String username, String password, Map<String, String> headers, String method, String uri, String data) throws Exception{	
		Log.d(TAG, String.format("发送HTTP摘要认证请求:[username=%1$s][password=%2$s][method=%3$s][uri=%4$s][data=%5$s]",username,password,method,uri,data));
		if(StringUtils.isBlank(uri)){
			throw new Exception("uri不能为空！");
		}
		return sendRequest(createConnection(uri), 
											headers,
											new DigestAuthcProvider(username, password, (StringUtils.isBlank(method) ? default_get_method : method), uri), 
											data);
	}
	//创建uri连接
	private static HttpURLConnection createConnection(String uri) throws IOException{
		return (HttpURLConnection)(new URL(uri).openConnection());
	}
	//发送HTTP请求。
	private static String sendRequest(HttpURLConnection connection,Map<String, String> headers,DigestAuthcProvider provider, String data) throws IOException{
		Log.d(TAG, "发送HTTP请求...");
		if(connection == null) return null;
		connection.setDoOutput(true);
		connection.setDoInput(true);
		//http摘要头信息
		final String authz = provider.toAuthorization();
		if(!StringUtils.isEmpty(authz)){
			if(headers == null) headers = new HashMap<String, String>();
			Log.d(TAG, String.format("添加摘要认证头信息:%1$s=%2$s", authorization_header,authz));
			//connection.addRequestProperty(authorization_header, authz);
			headers.put(authorization_header, authz);
		}
		//添加头信息
		if(headers != null && headers.size() > 0){
			for(Entry<String, String> entry : headers.entrySet()){
				if(StringUtils.isEmpty(entry.getKey())) continue;
				connection.addRequestProperty(entry.getKey(), entry.getValue());
			}
		}
		//设置请求方式
		connection.setRequestMethod(provider.getMethod());
		if(provider.getMethod().equalsIgnoreCase(default_get_method)){
			connection.connect();
		}
		//当有数据需要提交时
		if(!StringUtils.isEmpty(data)){
			OutputStream outputStream = connection.getOutputStream();
			//注意编码格式，防止中文乱码
			outputStream.write(data.getBytes(default_utf8_charset));
			outputStream.close();
		}
		int status = connection.getResponseCode();
		Log.d(TAG, String.format("HTTP反馈状态：%d", status));
		//401
		if(status == HttpURLConnection.HTTP_UNAUTHORIZED){
			if(provider.getNumberCount() > max_http_send_count){
				throw new RuntimeException("HTTP摘要认证失败！");
			}
		  String authc = connection.getHeaderField(authenticate_header);
		  Log.d(TAG, String.format("获取HTTP摘要认证头信息：%1$s=%2$s", authenticate_header, authc));
		  if(StringUtils.isEmpty(authc)) throw new RuntimeException("获取摘要认证头信息失败！");
		  provider.parser(authc);
		  connection.disconnect();
		  return sendRequest(createConnection(provider.getUri()), headers, provider, data);
		}
		//200
		if(status == HttpURLConnection.HTTP_OK){
			StringBuilder builder = new StringBuilder();
			//将返回的输入流转换成字符串
		    final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),default_utf8_charset));
			String out = null;
			while(!StringUtils.isEmpty(out = bufferedReader.readLine())){
				builder.append(out);
			}
			//释放资源
			bufferedReader.close();
			connection.disconnect();
			String callback = builder.toString();
			Log.d(TAG, "反馈数据=>" + callback);
			return callback; 
		}else{
			throw new RuntimeException(String.format("%1$d:%2$s", status, connection.getResponseMessage()));
		}
	}
	/**
	 * 摘要认证提供者
	 * 
	 * @author yangyong
	 * @since 2014年12月22日
	 */
 	 private static class DigestAuthcProvider{
		private String username,password,realm,nonce,method,uri,qop = "auth",cnonce,opaque;
		private int numberCount = 0;
		/**
		 * 构造函数。
		 * @param username
		 * 用户名。
		 * @param password
		 * 密码。
		 * @param method
		 * 请求方法。
		 * @param uri
		 * 请求地址。
		 */
		public DigestAuthcProvider(String username,String password,String method, String uri){
			this.username = username;
			this.password = password;
			this.method = method;
			this.uri = uri;
		}
		/**
		 * 获取Uri。
		 * @return Uri。
		 */
		public String getUri(){
			return this.uri;
		}
		/**
		 * 获取请求方法名称。
		 * @return 请求方法名称。
		 */
		public String getMethod(){
			return this.method;
		}
		/**
		 * 获取计数器。
		 * @return 计数器。
		 */
		public int getNumberCount() {
			return numberCount;
		}
		/**
		 * 分析认证头数据。
		 * @param authz
		 * 认证头数据。
		 */
		public void parser(String authc){
			Log.d(TAG, "分析认证头数据:" + authc);
			this.realm = this.getParameter(authc, "realm");
			Log.d(TAG, "realm => " + this.realm);
			if(StringUtils.isEmpty(this.realm)){
				throw new RuntimeException("从请求头信息中获取参数［realm］失败");
			}
			this.nonce = this.getParameter(authc, "nonce");
			Log.d(TAG, "nonce => " + this.nonce);
			if(StringUtils.isEmpty(this.nonce)){
				throw new RuntimeException("从请求头信息中获取参数［nonce］失败");
			}
			this.opaque = this.getParameter(authc, "opaque");
			Log.d(TAG, "opaque => " + this.opaque);
			if(StringUtils.isEmpty(this.opaque)){
				throw new RuntimeException("从请求头信息中获取参数［opaque］失败");
			}
			this.numberCount += 1;
		}
		//获取参数
		private String getParameter(String authz,String name){
			if(StringUtils.isEmpty(authz) || StringUtils.isEmpty(name)) return null;
			String regex = name + "=((.+?,)|((.+?)$))";
			Matcher m = Pattern.compile(regex).matcher(authz);
			if(m.find()){
				String p = m.group(1);
				if(!StringUtils.isEmpty(p)){
					if(p.endsWith(",")){
						p = p.substring(0, p.length() - 1);
					}
					if(p.startsWith("\"")){
						p = p.substring(1);
					}
					if(p.endsWith("\"")){
						p = p.substring(0, p.length() - 1);
					}
					return p;
				}
			}
			return null;
		}
		/**
		 * 生成摘要认证应答请求头信息。
		 * @return
		 * 应答请求头信息。
		 */
		public String toAuthorization(){
			if(StringUtils.isEmpty(this.realm) || StringUtils.isEmpty(this.nonce) || StringUtils.isEmpty(this.opaque)) return null;
			
			StringBuilder builder = new StringBuilder();
			String nc = String.format(Locale.getDefault(), "%08d", this.numberCount);
			this.cnonce = this.generateRadomCode(8);
			Charset charset = Charset.forName(DigestClientUtil.default_utf8_charset);
			String ha1 = DigestUtils.md5Hex((this.username + ":" + this.realm + ":" + this.password).getBytes(charset)),
					  ha2 = DigestUtils.md5Hex((this.method + ":" + this.uri).getBytes(charset));
			String response = DigestUtils.md5Hex((ha1 + ":" + this.nonce + ":" + nc + ":" + this.cnonce + ":" + this.qop + ":" + ha2).getBytes(charset));
			
			builder.append("Digest").append(" ")
					  .append("username").append("=").append("\"").append(this.username).append("\",")
					  .append("realm").append("=").append("\"").append(this.realm).append("\",")
					  .append("nonce").append("=").append("\"").append(this.nonce).append("\",")
					  .append("uri").append("=").append("\"").append(this.uri).append("\",")
					  .append("qop").append("=").append("\"").append(this.qop).append("\",")
					  .append("nc").append("=").append("\"").append(nc).append("\",")
					  .append("cnonce").append("=").append("\"").append(this.cnonce).append("\",")
					  .append("response").append("=").append("\"").append(response).append("\",")
					  .append("opaque").append("=").append("\"").append(this.opaque).append("\"");
			return builder.toString();
		}
		//创建随机数
		private String generateRadomCode(int length){
			if(length <= 0) return null;
			StringBuffer radomCodeBuffer = new StringBuffer();
			Random random = new Random(System.currentTimeMillis());
			int i = 0;
			while(i < length){
				int t = random.nextInt(123);
				if(t >= 97 || (t >= 48 && t <= 57)){
					radomCodeBuffer.append((char)t);
					i++;
				}
			}
			return radomCodeBuffer.toString();
		}
	}
}