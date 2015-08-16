package com.examw.netschool.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;

/** 
 * 字符串操作工具包
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class StringUtils 
{
	private final static Pattern emailer = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
	//private final static SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//private final static SimpleDateFormat dateFormater2 = new SimpleDateFormat("yyyy-MM-dd");
	
	private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss",Locale.CHINA);
		}
	};

	private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy-MM-dd",Locale.CHINA);
		}
	};
	private final static ThreadLocal<SimpleDateFormat> dateFormater3 = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			return new SimpleDateFormat("yyyy年MM月dd日",Locale.CHINA);
		}
	};
	/**
	 * 将字符串转位日期类型
	 * @param sdate
	 * @return
	 */
	public static Date toDate(String sdate) {
		try {
			return dateFormater.get().parse(sdate);
		} catch (ParseException e) {
			return null;
		}
	}
	/**
	 * 将日期转化为字符串显�?
	 */
	public static String toDateStr(long date)
	{
		if(date==0)
		{
			return null;
		}
		return dateFormater3.get().format(new Date(date));
	}
	/**
	 * 将日期转化为字符串显�?
	 */
	public static String toDateStr(Date date)
	{
		if(date==null)
		{
			return null;
		}
		return dateFormater.get().format(date);
	}
	/**
	 * 以友好的方式显示时间
	 * @param sdate
	 * @return
	 */
	public static String friendly_time(String sdate) {
		Date time = toDate(sdate);
		if(time == null) {
			return "Unknown";
		}
		String ftime = "";
		Calendar cal = Calendar.getInstance();
		
		//判断是否是同�?��
		String curDate = dateFormater2.get().format(cal.getTime());
		String paramDate = dateFormater2.get().format(time);
		if(curDate.equals(paramDate)){
			int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
			if(hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
			else 
				ftime = hour+"小时前";
			return ftime;
		}
		
		long lt = time.getTime()/86400000;
		long ct = cal.getTimeInMillis()/86400000;
		int days = (int)(ct - lt);		
		if(days == 0){
			int hour = (int)((cal.getTimeInMillis() - time.getTime())/3600000);
			if(hour == 0)
				ftime = Math.max((cal.getTimeInMillis() - time.getTime()) / 60000,1)+"分钟前";
			else 
				ftime = hour+"小时前";
		}
		else if(days == 1){
			ftime = "昨天";
		}
		else if(days == 2){
			ftime = "前天";
		}
		else if(days > 2 && days <= 5){ 
			ftime = days+"天前";			
		}
		else if(days > 5){			
			ftime = dateFormater2.get().format(time);
		}
		return ftime;
	}
	
	/**
	 * 判断给定字符串时间是否为今日
	 * @param sdate
	 * @return boolean
	 */
	public static boolean isToday(String sdate){
		boolean b = false;
		Date time = toDate(sdate);
		Date today = new Date();
		if(time != null){
			String nowDate = dateFormater2.get().format(today);
			String timeDate = dateFormater2.get().format(time);
			if(nowDate.equals(timeDate)){
				b = true;
			}
		}
		return b;
	}
	
	/**
	 * 判断给定字符串是否空白串�?
	 * 空白串是指由空格、制表符、回车符、换行符组成的字符串
	 * 若输入字符串为null或空字符串，返回true
	 * @param input
	 * @return boolean
	 */
	public static boolean isEmpty( String input ) 
	{
		if ( input == null || "".equals(input)) return true;
		int len = input.length();
		for ( int i = 0; i < len; i++ ) 
		{
			char c = input.charAt( i );
			if ( c != ' ' && c != '\t' && c != '\r' && c != '\n' )
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * 判断是不是一个合法的电子邮件地址
	 * @param email
	 * @return
	 */
	public static boolean isEmail(String email){
		if(email == null || email.trim().length()==0) 
			return false;
	    return emailer.matcher(email).matches();
	}
	/**
	 * 字符串转整数
	 * @param str
	 * @param defValue
	 * @return
	 */
	public static int toInt(String str, int defValue) {
		try{
			return Integer.parseInt(str);
		}catch(Exception e){}
		return defValue;
	}
	/**
	 * 对象转整�?
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static int toInt(Object obj) {
		if(obj==null) return 0;
		return toInt(obj.toString(),0);
	}
	/**
	 * 对象转整�?
	 * @param obj
	 * @return 转换异常返回 0
	 */
	public static long toLong(String obj) {
		try{
			return Long.parseLong(obj);
		}catch(Exception e){}
		return 0;
	}
	/**
	 * 字符串转布尔�?
	 * @param b
	 * @return 转换异常返回 false
	 */
	public static boolean toBool(String b) {
		try{
			return Boolean.parseBoolean(b);
		}catch(Exception e){}
		return false;
	}
	public static String toSelfOrEmpty(String b)
	{
		return b==null?"":b;
	}
	public static double toDouble(String nextText, double d) {
		// TODO Auto-generated method stub
		try{
			return Double.parseDouble(nextText);
		}catch(Exception e){}
		return d;
	}
	public static String toRuleTitle(String type)
	{
		try
		{
			switch(Integer.parseInt(type))
			{
			case 1:
				return "单选题";
			case 2:
				return "多选题";
			case 3:
				return "不定项选择";
			case 4:
				return "判断题";
			default:
					return "";
			}
		}catch(Exception e)
		{
			return "";
		}
	}
	
	/**
	 * 从一群id中随机一组id
	 * @param qids	整个ids
	 * @param qnum	�?��的个�?
	 * @return
	 */
	public static String randomQids(String qids,int qnum) 
	{
		StringBuffer buf = new StringBuffer();
		try
		{
			String[] arr = qids.split(",");
			LinkedList<String> list = new LinkedList<String>(Arrays.asList(arr));
			if(arr.length <= qnum)
			{
				return qids;
			}else
			{
				Random r = new Random();
				for(int i=0;i<qnum;i++)
				{
					int index = r.nextInt(list.size());
					buf.append(list.remove(index));
					buf.append(",");
				}
				return buf.substring(0, buf.length()-1);
			}
		}catch(Exception e)
		{
			return "";
		}
	}
	public static String dealImgOfBody(String body)
	{
		if(body == null) return null;
		body = body.replaceAll(
				"(<img[^>]*?)\\s+width\\s*=\\s*\\S+", "$1");
		body = body.replaceAll(
				"(<img[^>]*?)\\s+height\\s*=\\s*\\S+", "$1");

		// 添加点击图片放大支持
		body = body.replaceAll("(<img[^>]+src=\")(\\S+)\"",
				"$1$2\" onClick=\"javascript:mWebViewImageListener.onImageClick('$2')\"");
		return body;
	}
	public static float toFloat(String obj) {
		try{
			return Float.parseFloat(obj);
		}catch(Exception e){}
		return 16.0f;	//默认�?6
	}
}
