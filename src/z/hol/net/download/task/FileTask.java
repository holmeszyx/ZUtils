package z.hol.net.download.task;

import z.hol.net.download.AbsDownloadManager.Task;

/**
 * 文件下载任务
 * @author holmes
 *
 */
public interface FileTask extends Task{
	
	/**
	 * 获取文件保存位置
	 * @return
	 */
	public String getFileSavePath();
	
	/**
	 * 设置文件保存位置
	 * @param filePath
	 * @return
	 */
	public String setFileSavePath(String filePath);
	
	/**
	 * 设置断点
	 * @param startPos
	 * @return
	 */
	public void setStartPos(long startPos);
	
	/**
	 * 获取当前已下载的位置
	 * @return
	 */
	public long getCurrentPos();
	
	/**
	 * 获取最全要下载的块大小
	 * @return
	 */
	public long getTotal();
}
