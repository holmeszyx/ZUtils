package z.hol.net.download;

import z.hol.net.download.ContinuinglyDownloader.DownloadListener;

/**
 * 下载管理器任务状态回调
 */
public interface DownloadTaskListener extends DownloadListener{
	
	/**
	 * 任务添加
	 * @param id
	 */
	public void onAdd(long id);
	
	/**
	 * 任务等待
	 * @param id
	 */
	public void onWait(long id);
	
	/**
	 * 任务被移除
	 * @param id
	 */
	public void onRemove(long id);
}
