package z.hol.net.download.file;

import java.util.List;

import z.hol.db.DatabaseHandler;
import z.hol.model.SimpleFile;
import z.hol.net.download.ContinuinglyDownloader.DownloadListener;

public interface FileStatusSaver extends DatabaseHandler{

	public static interface File{
		 public static final String _ID = "_id";
		 public static final String SUB_ID = "sub_id";
		 public static final String LEN = "len";
		 public static final String LEN_FORMATED = "len_formated";
		 public static final String STATE = "state";
		 public static final String URL = "url";
		 public static final String SAVE_FILE = "save_file";
		 public static final String START_POS = "start_pos";
		 public static final String NAME = "name";
		 public static final String SUB_TYPE = "sub_type";
		 public static final String ADD_TIME = "add_time";
		 public static final String DONE_TIME = "done_time";
		 public static final String INT1 = "_int1";
		 public static final String INT2 = "_int2";
		 public static final String INT3 = "_int3";
		 public static final String INT4 = "_int4";
		 public static final String DATA1 = "data1";
		 public static final String DATA2 = "data2";
		 public static final String DATA3 = "data3";
		 public static final String DATA4 = "data4";
		 public static final String DATA5 = "data5";
	}
	
	public static String[] FILE_PROJECTION = new String[]{
		File._ID,	// 0
		File.URL,	// 1
		File.LEN,	// 2
		File.LEN_FORMATED,	// 3
		File.STATE,	// 4
		File.SAVE_FILE,	// 5
		File.START_POS,	// 6
		File.NAME,	// 7
		File.INT1,	// 8
		File.INT2,	// 9
		File.INT3,	// 10
		File.INT4,	// 11
		File.DATA1,	// 12
		File.DATA2,	// 13
		File.DATA3,	// 14
		File.DATA4,	// 15
		File.DATA5,	// 16
		File.ADD_TIME,	// 17
		File.DONE_TIME, // 18
		File.SUB_ID, // 19
		File.SUB_TYPE// 20
	};
	
	/**
	 * 添加一个下载任务
	 * @param file
	 * @param saveFile
	 */
	public void addTask(SimpleFile file, String saveFile);
	
	 /**
	  * 修改下载Url<br>
	  * 一般只有重定向时才用修改
	  * @param id
	  * @param url
	  */
	 public void changUrl(long id, String url);
	 
	/**
	 * 更新Task的块大小
	 * @param id
	 * @param size
	 */
	public void updateTaskSize(long id, long size);
	
	/**
	 * 更新Task下载的断点
	 * @param id
	 * @param currentPos
	 */
	public void updateDownloadPos(long id, long currentPos);
	
	/**
	 * 修改Task状态
	 * @param id
	 * @param state
	 */
	public void changeTaskState(long id, int state);
	
	/**
	 * 删除Task
	 * @param id
	 */
	public void removeTask(long id);

	 /**
	  * 获取一个下载任务TASK(恢复数据)
	  * @param Id
	  * @param task
	  * @return
	  */
	 public FileDownloadTask getDownloadTask(long id, FileDownloadTask task);

	 /**
	  * 获取已保存的下载任务列表<br>
	  * 包括下载完成, 主要用于恢复下载管理器数据
	  * @return
	  */
	 public List<FileDownloadTask> getDownloadTaskList(FileStatusSaver saver, DownloadListener listener);
}
