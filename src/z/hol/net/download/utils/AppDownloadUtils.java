package z.hol.net.download.utils;

import z.hol.net.download.AppStatusSaver.APP;

public class AppDownloadUtils {

	/**
	 * 得到一个APP的默认下载保存地址
	 * @param pkg
	 * @return
	 */
	 public static String getAppSavePath(String pkg){
		return APP.DEFAULT_SAVE_PATH + pkg + ".apk"; 
	 }
}
