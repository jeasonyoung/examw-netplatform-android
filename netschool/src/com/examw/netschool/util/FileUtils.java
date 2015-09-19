package com.examw.netschool.util;

import java.io.File;
import java.io.RandomAccessFile;

import android.util.Log;

/**
 * 文件工具类。
 * 
 * @author jeasonyoung
 * @since 2015年9月13日
 */
public final class FileUtils {
	private static final String TAG = "FileUtils";
	/**
	 * 加密/解密文件。
	 * @param file 下载的文件
	 * @param skip 插入的位置
	 * @param data 密钥
	 */
	public synchronized static void encryptFile(File file, long skip, byte[] keys){
		if(file == null || skip < 0 || keys.length == 0)return;
		Log.d(TAG, "开始对文件进行加密处理..");
		try {
			final RandomAccessFile accessFile = new RandomAccessFile(file, "rwd");
			if(skip < 0 || skip > accessFile.length()){
				accessFile.close();
				Log.d(TAG, "加密跳过字节数无效...");
				return;
			}
			//密钥长度
			int len = keys.length;
			if(skip + len > accessFile.length()){
				accessFile.close();
				Log.d(TAG, "密钥长度("+len+")＋跳过长度("+skip+") > 文件长度("+accessFile.length()+")!");
				return;
			}
			//加密运算
			int source,encrypt;
			for(int i = 0;  i < len; i++){
				//指定位置
				accessFile.seek(skip + i);
				//读取数据后指针下移
				source = accessFile.read();
				encrypt = source ^ (int)keys[i];
				//重新指定位置
				accessFile.seek(skip + i);
				//用密文替换原文
				accessFile.write(encrypt);
			}
			accessFile.close();
			Log.d(TAG, "文件加密完成！");
		} catch (Exception e) {
			Log.e(TAG, "文件加密异常:" + e.getMessage(), e);
		}
	}
}