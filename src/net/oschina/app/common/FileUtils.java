package net.oschina.app.common;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

/** 
 * 文件操作工具包
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class FileUtils 
{
	/**
	 * 写文本文件
	 * 在Android系统中，文件保存在 /data/data/PACKAGE_NAME/files 目录下
	 * @param context
	 * @param msg
	 */
	public static void write(Context context, String fileName, String content) 
	{ 
		if( content == null )	content = "";
		
		try 
		{
			FileOutputStream fos = context.openFileOutput(fileName, Context.MODE_PRIVATE);
			fos.write( content.getBytes() ); 
			
			fos.close();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		} 
	}
	
	/**
	 * 读取文本文件
	 * @param context
	 * @param fileName
	 * @return
	 */
	public static String read( Context context, String fileName ) 
	{
		try 
		{
			FileInputStream in = context.openFileInput(fileName);
			return readInStream(in);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		return "";
	} 
	
	private static String readInStream(FileInputStream inStream)
	{
		try 
		{
		   ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		   byte[] buffer = new byte[512];
		   int length = -1;
		   while((length = inStream.read(buffer)) != -1 )
		   {
			   outStream.write(buffer, 0, length);
		   }
		   
		   outStream.close();
		   inStream.close();
		   return outStream.toString();
		} 
		catch (IOException e)
		{
		   Log.i("FileTest", e.getMessage()); 
		}
		return null;
	}
	
	public static File createFile( String folderPath, String fileName )
	{
		File destDir = new File(folderPath);
		if (!destDir.exists()) 
		{
			destDir.mkdirs();
		}
		return new File(folderPath,  fileName + fileName );
	}
	
	/**
	 * 向手机写图片
	 * @param buffer   
	 * @param folder
	 * @param fileName
	 * @return
	 */
	public static boolean writeFile( byte[] buffer, String folder, String fileName )
	{
		boolean writeSucc = false;
		
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
		
		String folderPath = "";
		if( sdCardExist )
		{
			folderPath = Environment.getExternalStorageDirectory() + File.separator +  folder + File.separator;
		}
		else
		{
			writeSucc =false;
		}
		
		File fileDir = new File(folderPath);
		if(!fileDir.exists()) 
		{
			fileDir.mkdirs();
		}
		  
		File file = new File( folderPath + fileName );
		FileOutputStream out = null;
		try 
		{
			out = new FileOutputStream( file );
			out.write(buffer);
			writeSucc = true;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			try {out.close();} catch (IOException e) {e.printStackTrace();}
		}
		
		return writeSucc;
	}
	
	/**
	 * 根据文件绝对路径获取文件名
	 * @param filePath
	 * @return
	 */
	public static String getFileName( String filePath )
	{
		if( StringUtils.isEmpty(filePath) )	return "";
		return filePath.substring( filePath.lastIndexOf( File.separator )+1 );
	}
	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat( String filePath){
		if(StringUtils.isEmpty(filePath)){
			return "";
		}
		int point = filePath.lastIndexOf('.');
		return filePath.substring(filePath.lastIndexOf(File.separator)+1,point);
	}
	
	/**
	 * 获取文件扩展名
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat( String fileName )
	{
		if( StringUtils.isEmpty(fileName) )	return "";
		
		int point = fileName.lastIndexOf( '.' );
		return fileName.substring( point+1 );
	}
	
	/**
	 * 获取文件大小
	 * @param filePath
	 * @return
	 */
	public static long getFileSize( String filePath )
	{
		long size = 0;
		
		File file = new File( filePath );
		if(file!=null && file.exists())
		{
			size = file.length();
		} 
		return size;
	}
	
	/**
	 * 获取文件大小
	 * @param size 字节
	 * @return
	 */
	public static String getFileSize(long size) 
	{
		if (size <= 0)	return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float)size / 1024;
		if (temp >= 1024) 
		{
			return df.format(temp / 1024) + "M";
		}
		else 
		{
			return df.format(temp) + "K";
		}
	}

	/**
	 * 转换文件大小
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

	/**
	 * 获取目录文件大小
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
	    if (!dir.isDirectory()) {
	    	return 0;
	    }
	    long dirSize = 0;
	    File[] files = dir.listFiles();
	    for (File file : files) {
	    	if (file.isFile()) {
	    		dirSize += file.length();
	    	} else if (file.isDirectory()) {
	    		dirSize += file.length();
	    		dirSize += getDirSize(file); //递归调用继续统计
	    	}
	    }
	    return dirSize;
	}
	
	/**
	 * 获取目录文件个数
	 * @param f
	 * @return
	 */
	public long getFileList(File dir){
        long count = 0;
        File[] files = dir.listFiles();
        count = files.length;
        for (File file : files) {
            if (file.isDirectory()) {
            	count = count + getFileList(file);//递归
            	count--;
            }
        }
        return count;  
    }
	
	public static byte[] toBytes(InputStream in) throws IOException 
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
	    int ch;
	    while ((ch = in.read()) != -1)
	    {
	    	out.write(ch);
	    }
	    byte buffer[]=out.toByteArray();
	    out.close();
	    return buffer;
	}
	
	/**
	 * 检查文件是否存在
	 * @param name
	 * @return
	 */
	public static boolean checkFileExists(String name) {
		boolean status;
		if (!name.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + name);
			status = newPath.exists();
		} else {
			status = false;
		}
		return status;

	}
	
	/**
	 * 计算SD卡的剩余空间
	 * @return 返回-1，说明没有安装sd卡
	 */
	public static long getFreeDiskSpace() {
		String status = Environment.getExternalStorageState();
		long freeSpace = 0;
		if (status.equals(Environment.MEDIA_MOUNTED)) {
			try {
				File path = Environment.getExternalStorageDirectory();
				StatFs stat = new StatFs(path.getPath());
				long blockSize = stat.getBlockSize();
				long availableBlocks = stat.getAvailableBlocks();
				freeSpace = availableBlocks * blockSize / 1024;
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			return -1;
		}
		return (freeSpace);
	}

	/**
	 * 新建目录
	 * @param directoryName
	 * @return
	 */
	public static boolean createDirectory(String directoryName) {
		boolean status;
		if (!directoryName.equals("")) {
			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + directoryName);
			status = newPath.mkdir();
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 检查是否安装SD卡
	 * @return
	 */
	public static boolean checkSaveLocationExists() {
		String sDCardStatus = Environment.getExternalStorageState();
		boolean status;
		if (sDCardStatus.equals(Environment.MEDIA_MOUNTED)) {
			status = true;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除目录(包括：目录里的所有文件)
	 * @param fileName
	 * @return
	 */
	public static boolean deleteDirectory(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isDirectory()) {
				String[] listfile = newPath.list();
				// delete all files within the specified directory and then
				// delete the directory
				try {
					for (int i = 0; i < listfile.length; i++) {
						File deletedFile = new File(newPath.toString() + "/"
								+ listfile[i].toString());
						deletedFile.delete();
					}
					newPath.delete();
					Log.i("DirectoryManager deleteDirectory", fileName);
					status = true;
				} catch (Exception e) {
					e.printStackTrace();
					status = false;
				}

			} else
				status = false;
		} else
			status = false;
		return status;
	}

	/**
	 * 删除文件
	 * @param fileName
	 * @return
	 */
	public static boolean deleteFile(String fileName) {
		boolean status;
		SecurityManager checker = new SecurityManager();

		if (!fileName.equals("")) {

			File path = Environment.getExternalStorageDirectory();
			File newPath = new File(path.toString() + fileName);
			checker.checkDelete(newPath.toString());
			if (newPath.isFile()) {
				try {
					Log.i("DirectoryManager deleteFile", fileName);
					newPath.delete();
					status = true;
				} catch (SecurityException se) {
					se.printStackTrace();
					status = false;
				}
			} else
				status = false;
		} else
			status = false;
		return status;
	}
}