package com.examw.netschool.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.util.Base64;

public class QuestionContentDecoder {
	public static String md5Digest(String seq) {
		try {
			//����MD5�㷨����
			char hexDigits[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',  
	                'a', 'b', 'c', 'd', 'e', 'f' };  
			MessageDigest md5Code =
				MessageDigest.getInstance("md5");
			byte[] md=md5Code.digest(seq.getBytes());
			int j = md.length;  
            char str[] = new char[j * 2];  
            int k = 0;  
            for (int i = 0; i < j; i++) {  
                byte byte0 = md[i];  
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];  
                str[k++] = hexDigits[byte0 & 0xf];  
            }  
            return new String(str);  
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return null;
		}
	}
	public static String getContent(String content,int random) {
		String key = md5Digest(getCode(random));
		String base = content.replaceAll(key,String.valueOf((char)(64+random)));
		try {
			return new String(Base64.decode(base.getBytes(),0),"GBK");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "";
		}
	}
	private static String getCode(int random)
	{
		switch(random)
		{
		case 1:
			return "sde851";
		case 2:
			return "dwqs12";
		case 3:
			return "25sdsa";
		case 4:
			return "14dsaj";
		case 5:
			return "3rfsdd";
		case 6:
			return "hbhdg5";
		case 7:
			return "acs812";
		case 8:
			return "5824vd";
		case 9:
			return "h5ds2e";
		case 10:
			return "5jhe8d";
		case 11:
			return "cs5822";
		case 12:
			return "aq925f";
		case 13:
			return "zx825e";
		case 14:
			return "mg52po";
		case 15:
			return "mvn85v";
		case 16:
			return "jhb9f2";
		case 17:
			return "m5pc5d";
		case 18:
			return "vm1oie";
		case 19:
			return "nc5xzv";
		case 20:
			return "bn5fg5";
		case 21:
			return "dr1wc7";
		case 22:
			return "vc2xz5";
		case 23:
			return "5bfs2d";
		case 24:
			return "yrre8f";
		case 25:
			return "xcss52";
		case 26:
			return "bxzfa8";
			
		}
		return null;
	}
}
