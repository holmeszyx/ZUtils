package z.hol.utils.file;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import z.hol.utils.codec.DigestUtils;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

public class FileUtils {

	public static final long B = 1;
	public static final long KB = B * 1024;
	public static final long MB = KB * 1024;
	public static final long GB = MB * 1024;
	
	
    
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
    
    /**
     * 获取文件的MD5值
     * @param f
     * @return
     */
    public static String getFileMd5(File f){
        if (f != null){
            FileInputStream in = null;
            try {
                in = new FileInputStream(f);
                return DigestUtils.md5Hex(in);
            } catch (FileNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } finally{
                if (in != null){
                    try {
                        in.close();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }
}
