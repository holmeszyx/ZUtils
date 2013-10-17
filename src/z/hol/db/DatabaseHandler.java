package z.hol.db;

import android.database.sqlite.SQLiteDatabase;


/**
 * 数据库操作
 * @author holmes
 *
 */
public interface DatabaseHandler extends Transactional{

	/**
	 * 打开一个可读写数据库
	 * @return
	 */
	public SQLiteDatabase getWriteableDb();
	
	/**
	 * 打开一个只读数据库
	 * @return
	 */
	public SQLiteDatabase getReadableDb();
	
	/**
	 * 数据库是否已经打开
	 * @return
	 */
	public boolean isOpened();
	
	/**
	 * 关闭打开的数据库
	 */
	public void closeDb();
}
