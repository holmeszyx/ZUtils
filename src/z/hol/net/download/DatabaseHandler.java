package z.hol.net.download;

import android.database.sqlite.SQLiteDatabase;


/**
 * 数据库操作
 * @author holmes
 *
 */
public interface DatabaseHandler {

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
	
	/**
	 * 开始事务
	 */
	public void beginTransaction();
	
	/**
	 * 事务成功
	 */
	public void setTransactionSuccessful();
	
	/**
	 * 事务结束
	 */
	public void endTransaction();
}
