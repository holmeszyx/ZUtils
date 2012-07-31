package z.hol.net.download;

public interface FileStatusSaver extends DatabaseHandler{

	public static interface File{
		 public static final String _ID = "_id";
		 public static final String LEN = "len";
		 public static final String LEN_FORMATED = "len_formated";
		 public static final String STATE = "state";
		 public static final String URL = "url";
		 public static final String SAVE_FILE = "save_file";
		 public static final String START_POS = "start_pos";
		 public static final String NAME = "name";
		 public static final String INT1 = "_int1";
		 public static final String INT2 = "_int2";
		 public static final String DATA1 = "data1";
		 public static final String DATA2 = "data2";
		 public static final String DATA3 = "data3";
		 public static final String DATA4 = "data4";
		 public static final String DATA5 = "data5";
	}
	
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
}
