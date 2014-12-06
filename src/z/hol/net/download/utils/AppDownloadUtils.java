package z.hol.net.download.utils;

public class AppDownloadUtils {
	public static String APP_SAVE_PATH = "/sdcard/Download/";

	/**
	 * 得到一个APP的默认下载保存地址
	 * @param pkg
	 * @return
	 */
	 public static String getAppSavePath(String pkg){
		return APP_SAVE_PATH + pkg + ".apk"; 
	 }
}
