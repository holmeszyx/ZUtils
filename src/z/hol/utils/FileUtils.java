package z.hol.utils;

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
}
