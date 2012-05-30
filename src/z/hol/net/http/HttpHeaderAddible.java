package z.hol.net.http;

/**
 * http 添加头部头接口
 * @author holmes
 *
 */
public interface HttpHeaderAddible {
	
	/**
	 * 添加头部
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value);
	
	/**
	 * 清空自己添加的头部<br />
	 *  
	 */
	public void clearAddedHeaders();
}
