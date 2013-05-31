package z.hol.db;

/**
 * 带事务处理
 * @author holmes
 *
 */
public interface Transactional {
	
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
