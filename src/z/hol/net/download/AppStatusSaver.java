package z.hol.net.download;

import z.hol.model.RecommendedApp;

/**
 * APP下载状态, 保存器
 * @author holmes
 *
 */
public interface AppStatusSaver {

	/**
	 * APP 数据字段名
	 * @author holmes
	 *
	 */
	 public static interface APP{
		 public static final String _ID = "_id";
		 public static final String LEN = "len";
		 public static final String LEN_FORMATED = "len_formated";
		 public static final String STATE = "state";
		 public static final String URL = "url";
		 public static final String SAVE_FILE = "save_file";
		 public static final String PACKAGE = "pkg";
		 public static final String NAME = "name";
		 public static final String VERSION_NAME = "ver_name";
		 public static final String VERSION_CODE = "ver_code";
		 public static final String ICON = "icon";	
		 public static final String START_POS = "start_pos";
		 public static final String DEFAULT_SAVE_PATH = "/sdcard/mgyun/download/";
	 }

	 
	 /**
	  * 添加一个APP下载,并加入初始化数据
	  * @param app
	  */
	 public void addAppDownload(RecommendedApp app);
	 
	 /**
	  * 更新APP的大小 
	  * @param appId
	  * @param size
	  */
	 public void updateAppSize(long appId, long size);
	 
	 /**
	  * 更新APP的下载断点
	  * @param appId
	  * @param currentPos
	  */
	 public void updateAppDownloadPos(long appId, long currentPos);
	 
	 /**
	  * 修改APP下载状态
	  * @param appId
	  * @param state
	  */
	 public void changeAppTaskState(long appId, int state);
	 
	 /**
	  * 查找APP的字段
	  */
	 public static final String[] APP_TASK_PROJECTION = new String[]{
		APP._ID,		// 0
		APP.ICON,	// 1
		APP.LEN,		// 2
		APP.LEN_FORMATED,	// 3
		APP.NAME,	// 4
		APP.PACKAGE,		// 5
		APP.SAVE_FILE,	// 6
		APP.START_POS,	// 7
		APP.URL,		// 8
		APP.VERSION_CODE,	// 9
		APP.VERSION_NAME		//10
	 }; 
	 
	 /**
	  * 获取一个APP下载任务TASK(恢复数据)
	  * @param appId
	  * @param task
	  * @return
	  */
	 public AppDownloadTask getAppTask(long appId, AppDownloadTask task);	 
}
