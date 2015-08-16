package com.examw.netschool.util;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;

public class FileUtil {
	public static boolean checkSDCard(int fileSize)
	{
		File pathFile = Environment.getExternalStorageDirectory();
		if(!pathFile.exists())
		{
			return false;
		}
		if(fileSize>0)
		{
			StatFs statfs = new StatFs(pathFile.getPath());
			//获得可供程序使用的Block数量
			long nAvailaBlock = statfs.getAvailableBlocks();
			//获得SDCard上每个block的SIZE
			long nBlocSize = statfs.getBlockSize();
			//计算SDCard剩余大小 Byte
			long nSDFreeSize = nAvailaBlock * nBlocSize;
			return nSDFreeSize > fileSize;
		}else
		{
			return true;
		}
	}
}
