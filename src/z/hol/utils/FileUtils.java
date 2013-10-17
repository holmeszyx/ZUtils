package z.hol.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

public class FileUtils {

	public static final long B = 1;
	public static final long KB = B * 1024;
	public static final long MB = KB * 1024;
	public static final long GB = MB * 1024;
	
	/**
	 * 格式化文件大小<b>
	 * 带有单位
	 * @param size
	 * @return
	 */
	public static String formatFileSize(long size){
		StringBuilder sb = new StringBuilder();
		String u = null;
		double tmpSize = 0;
		if (size < KB){
			sb.append(size).append("B");
			return sb.toString();
		}else if (size < MB){
			tmpSize = getSize(size, KB);
			u = "KB";
		}else if (size < GB){
			tmpSize = getSize(size, MB);
			u = "MB";
		}else {
			tmpSize = getSize(size, GB);
			u = "GB";
		}
		return sb.append(twodot(tmpSize)).append(u).toString();
	}
	
	/**
	 * 保留两位小数
	 * @param d
	 * @return
	 */
	public static String twodot(double d){
		return String.format("%.2f", d);
	}
	
	public static double getSize(long size, long u){
		return (double) size / (double) u;
	}
	
	public static boolean renameFile(String from, String to){
		File ff = new File(from);
		File ft = new File(to);
		return ff.renameTo(ft);
	}
	
    /** 
     * 文本文件转换为指定编码的字符串 
     * 
     * @param file     文本文件 
     * @param encoding 编码类型 
     * @return 转换后的字符串 
     * @throws IOException 
     */ 
    public static String file2String(File file, String encoding) { 
		InputStreamReader reader = null;
		StringWriter writer = new StringWriter();
		try {
			if (encoding == null || "".equals(encoding.trim())) {
				reader = new InputStreamReader(new FileInputStream(file));
			} else {
				reader = new InputStreamReader(new FileInputStream(file),
						encoding);
			}
			// 将输入流写入输出流
			char[] buffer = new char[1024];
			int n = 0;
			while (-1 != (n = reader.read(buffer))) {
				writer.write(buffer, 0, n);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		String result = writer.toString();
		try {
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return result;
    } 
    
    /** 
     * 将字符串写入指定文件(当指定的父路径中文件夹不存在时，会最大限度去创建，以保证保存成功！) 
     * @param res      原字符串 
     * @param filePath 文件路径 
     * @return 成功标记 
     */ 
    public static boolean string2File(String res, String filePath) { 
        boolean flag = true; 
        BufferedReader bufferedReader = null; 
        BufferedWriter bufferedWriter = null; 
        try { 
            File distFile = new File(filePath); 
            if (!distFile.getParentFile().exists()) 
            	distFile.getParentFile().mkdirs(); 
            bufferedReader = new BufferedReader(new StringReader(res)); 
            bufferedWriter = new BufferedWriter(new FileWriter(distFile)); 
            char buf[] = new char[1024];         //字符缓冲区 
            int len; 
            while ((len = bufferedReader.read(buf)) != -1) { 
                bufferedWriter.write(buf, 0, len); 
            } 
            bufferedWriter.flush(); 
            bufferedReader.close(); 
            bufferedWriter.close(); 
        } catch (IOException e) { 
            flag = false; 
            e.printStackTrace(); 
        } 
        return flag; 
    }
	
    
    /**
     * 文件是否存在
     * @param file
     * @return
     */
    public static boolean isFileExist(String file){
   	 File f = new File(file);
   	 boolean ex = f.exists();
   	 f = null;
   	 return ex;
    }
    
    /**
     * 删除文件
     * @param file
     * @return
     */
    public static boolean deleteFile(String file){
   	 File f = new File(file);
   	 if (f.exists()){
   		return f.delete(); 
   	 }
   	 return true;
    }
    
    /**
     * 返回剩余的sd空间
     * @return 剩余的空间的byte值
     */
    public static long getFreeSdcard(){
   	 StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
   	 long bytesAvailable = (long)stat.getBlockSize() *(long)stat.getAvailableBlocks();
   	 return bytesAvailable;
    }
    
    /**
     * 将格式化后的Size大小转为大概数值大小
     * @param formated
     * @return
     */
    public static long formateSizeToSize(String formated){
   	final String fs = formated.toLowerCase().trim();
   	char last = fs.charAt(fs.length() - 1);
   	char lastButOne = fs.charAt(fs.length() - 2);
   	long size = 0l;
   	String formatedSize = null;
   	long unit = 0;
   	if (last == 'b'){
   		boolean two = false;
   		if (lastButOne == 'k'){
   			unit = FileUtils.KB;
   			two = true;
   		}else if (lastButOne == 'm'){
   			unit = FileUtils.MB;
   			two = true;
   		}else if (lastButOne == 'g'){
   			unit = FileUtils.GB;
   			two = true;
   		}else {
   			unit = FileUtils.B;
   		}
   		if (two){
   			formatedSize = fs.substring(0, fs.length() - 2);
   		}else{
   			formatedSize = fs.substring(0, fs.length() - 1);
   		}
   		
   	}else{
   		if (last == 'k'){
   			unit = FileUtils.KB;
   		}else if (last == 'm'){
   			unit = FileUtils.MB;
   		}else if (last == 'g'){
   			unit = FileUtils.GB;
   		}
   		
			formatedSize = fs.substring(0, fs.length() - 1);
   	}
   	
   	if (formatedSize == null){
   		return 0;
   	}
   	
   	float fSize = Float.parseFloat(formatedSize.trim());
   	size = (long) (fSize * unit);
   	return size; 
    }
    
    /**
     * 是否有足够的SD容量
     * @param need
     * @return
     */
    public static boolean isEnoughSD(long need){
   	 long n = need + FileUtils.MB * 50;
   	 return getFreeSdcard() > n;
    }
    
    public static String scaleFileName(String filepath){
		int pos = filepath.lastIndexOf("/");
		String name = filepath.substring(pos + 1);
		return name;
    }
    
    
    private static String SDCARD_PATH = null;
    
    /**
     * 获取SD卡的位置
     * @return
     */
    public static String getSdcardPath(){
   	 if (TextUtils.isEmpty(SDCARD_PATH)){
   		File sdcard = Environment.getExternalStorageDirectory();
   		if (sdcard == null){
   			return "/sdcard";
   		}
   		SDCARD_PATH = sdcard.getAbsolutePath();
   		sdcard = null;
   	 }
   	 return SDCARD_PATH;
    }
    
    /**
     * 获取目录分区的块信息
     * @param dir
     * @return 0 total, 1 free
     */
    public static long[] getDirSizeInfo(String dir){
		long[] sizes = new long[2];
		StatFs stat = new StatFs(dir);
		long block = (long) stat.getBlockSize();
		long total = block * (long) stat.getBlockCount();
		long free = block * (long) stat.getAvailableBlocks();
		
		sizes[0] = total;
		sizes[1] = free;
		return sizes;
	}
}
