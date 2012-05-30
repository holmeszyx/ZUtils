package z.hol.net.http;

/**
 * http 运行状态回调
 * @author holmes
 *
 */
public interface HttpProgress {

	/**
	 * Http开始执行
	 */
	public void onHttpStart();
	
	/**
	 * Http执行完成
	 * @param obj http返回的数据，格式化后得到的对象
	 */
	public void onHttpEnd(Object obj, int httpStatusCode);
	
	/**
	 * Http出错，状态非200
	 * @param httpStatusCode 错误的Http状态码
	 */
	public void onHttpError(int httpStatusCode);
}
